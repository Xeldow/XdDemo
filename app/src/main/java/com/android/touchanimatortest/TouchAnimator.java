package com.android.touchanimatortest;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.FloatProperty;
import android.util.Log;
import android.util.MathUtils;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: Xeldow
 * @date: 2019/8/2
 */
public class TouchAnimator {
    public static final String TAG = "TouchAnimator";
    private final Object[] mTargets;
    private final KeyframeSet[] mKeyframeSets;
    /**
     * 时间控制
     */
    private final float mStartDelay;
    private final float mEndDelay;
    /**
     * 这是干啥的
     * 间隔吗？
     */
    private final float mSpan;
    /**
     * 插值器
     */
    private final Interpolator mInterpolator;
    private final Listener mListener;
    /**
     * 这是？
     */
    private float mLastT = -1;

    Handler mHandler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
        }
    };

    private TouchAnimator(Object[] targets, KeyframeSet[] keyframeSets,
                          float startDelay, float endDelay, Interpolator interpolator, Listener listener) {
        mTargets = targets;
        mKeyframeSets = keyframeSets;
        mStartDelay = startDelay;
        mEndDelay = endDelay;
        mSpan = (1 - mEndDelay - mStartDelay);
        Log.e(TAG, "TouchAnimator: " + mSpan);
        mInterpolator = interpolator;
        mListener = listener;
    }

    public void setAnimatPosition(final float start, final float end) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setPosition(start / end);
                setAnimatPosition(start + 1, end);
            }
        });
    }

    public void setPosition(float fraction) {
        //t的范围是0到1
        float t = MathUtils.constrain((fraction - mStartDelay) / mSpan, 0, 1);
        Log.e(TAG, "setPosition: " + t);
        if (mInterpolator != null) {
            t = mInterpolator.getInterpolation(t);
            Log.e(TAG, "getInterpolation: " + t);
        }
        if (t == mLastT) {
            return;
        }
        if (mListener != null) {
            if (t == 1) {
                mListener.onAnimationAtEnd();
            } else if (t == 0) {
                mListener.onAnimationAtStart();
            } else if (mLastT <= 0 || mLastT == 1) {
                mListener.onAnimationStarted();
            }
            mLastT = t;
        }
        for (int i = 0; i < mTargets.length; i++) {
            mKeyframeSets[i].setValue(t, mTargets[i]);
        }
    }

    /**
     * Builder
     */
    public static class Builder {
        private List<Object> mTargets = new ArrayList<>();
        private List<TouchAnimator.KeyframeSet> mValues = new ArrayList<>();

        private float mStartDelay;
        private float mEndDelay;
        private Interpolator mInterpolator;
        private Listener mListener;

        @SuppressLint("NewApi")
        public Builder addFloat(Object target, String property, float... values) {
            add(target, TouchAnimator.KeyframeSet.ofFloat(getProperty(target, property, float.class), values));
            return this;
        }

        private void add(Object target, TouchAnimator.KeyframeSet keyframeSet) {
            mTargets.add(target);
            mValues.add(keyframeSet);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private static Property getProperty(Object target, String property, Class<?> cls) {
            if (target instanceof View) {
                switch (property) {
                    case "translationX":
                        return View.TRANSLATION_X;
                    case "translationY":
                        return View.TRANSLATION_Y;
                    case "translationZ":
                        return View.TRANSLATION_Z;
                    case "alpha":
                        return View.ALPHA;
                    case "rotation":
                        return View.ROTATION;
                    case "x":
                        return View.X;
                    case "y":
                        return View.Y;
                    case "scaleX":
                        return View.SCALE_X;
                    case "scaleY":
                        return View.SCALE_Y;
                }
            }
            if (target instanceof TouchAnimator && "position".equals(property)) {
                return POSITION;
            }
            return Property.of(target.getClass(), cls, property);
        }

        public Builder setStartDelay(float startDelay) {
            mStartDelay = startDelay;
            return this;
        }

        public Builder setEndDelay(float endDelay) {
            mEndDelay = endDelay;
            return this;
        }

        public Builder setInterpolator(Interpolator intepolator) {
            mInterpolator = intepolator;
            return this;
        }

        public Builder setListener(Listener listener) {
            mListener = listener;
            return this;
        }

        public TouchAnimator build() {
            return new TouchAnimator(mTargets.toArray(new Object[mTargets.size()]),
                    mValues.toArray(new TouchAnimator.KeyframeSet[mValues.size()]),
                    mStartDelay, mEndDelay, mInterpolator, mListener);
        }
    }

    /**
     * KeyframeSet
     */
    private static abstract class KeyframeSet {

        private final float mFrameWidth;
        private final int mSize;

        public KeyframeSet(int size) {
            mSize = size;
            mFrameWidth = 1 / (float) (size - 1);
        }

        void setValue(float fraction, Object target) {
            int i;
            for (i = 1; i < mSize - 1 && fraction > mFrameWidth; i++) ;
                float amount = fraction / mFrameWidth;
                interpolate(i, amount, target);
        }

        protected abstract void interpolate(int index, float amount, Object target);


        public static KeyframeSet ofFloat(Property property, float... values) {
            return new FloatKeyframeSet((Property<?, Float>) property, values);
        }
    }

    /**
     * FloatKeyframeSet
     *
     * @param <T>
     */
    private static class FloatKeyframeSet<T> extends TouchAnimator.KeyframeSet {
        private final float[] mValues;
        private final Property<T, Float> mProperty;

        public FloatKeyframeSet(Property<T, Float> property, float[] values) {
            super(values.length);
            mProperty = property;
            mValues = values;
        }

        @Override
        protected void interpolate(int index, float amount, Object target) {
            float firstFloat = mValues[index - 1];
            float secondFloat = mValues[index];
            mProperty.set((T) target, firstFloat + (secondFloat - firstFloat) * amount);
        }
    }
    @SuppressLint("NewApi")
    private static final FloatProperty<TouchAnimator> POSITION =
            new FloatProperty<TouchAnimator>("position") {
                @Override
                public void setValue(TouchAnimator touchAnimator, float value) {
                    touchAnimator.setPosition(value);
                }

                @Override
                public Float get(TouchAnimator touchAnimator) {
                    return touchAnimator.mLastT;
                }
            };

    public static class ListenerAdapter implements Listener {
        @Override
        public void onAnimationAtStart() {
        }

        @Override
        public void onAnimationAtEnd() {
        }

        @Override
        public void onAnimationStarted() {
        }
    }

    public interface Listener {
        /**
         * Called when the animator moves into a position of "0". Start and end delays are
         * taken into account, so this position may cover a range of fractional inputs.
         */
        void onAnimationAtStart();

        /**
         * Called when the animator moves into a position of "0". Start and end delays are
         * taken into account, so this position may cover a range of fractional inputs.
         */
        void onAnimationAtEnd();

        /**
         * Called when the animator moves out of the start or end position and is in a transient
         * state.
         */
        void onAnimationStarted();
    }
}
