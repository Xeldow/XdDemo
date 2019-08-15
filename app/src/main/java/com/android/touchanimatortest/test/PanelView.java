package com.android.touchanimatortest.test;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.statusbartest.R;
import com.android.statusbartest.utils.BlurUtil;
import com.android.statusbartest.utils.ScreenShotUtil;
import com.android.statusbartest.utils.TouchAnimator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * 所有SystemUI的BaseView
 * <p/>
 * Created by XiaoZhenLin on 2019/7/25.
 */
public class PanelView extends FrameLayout {
    private static final String TAG = "StatusBarView";
    private static final int MIN_EXPEND_HIGH = 250;

    private Context mContext;

    private GestureDetector mGestureDetector;

    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private PanelView thisView;

    /**
     * 状态栏
     */
    private RelativeLayout statusBar;
    private float statusBarHeight;
    /**
     * 展开部分
     */
    private RelativeLayout statusBarExpanded;
    /**
     * 整个下拉栏
     */
    private LinearLayout panelView;
    private DisplayMetrics metrics;
    /**
     * 高斯模糊相关
     */
    private ImageView blurView;
    private Bitmap finalBitmap;
    private Rect blurArea;
    private Bitmap overlay;
    private Canvas canvas;
    /**
     * 状态判断
     */
    private boolean isStatusBarShown;
    private boolean enablePullDownPanelView = false;
    private boolean isTouching = false;
    private boolean isStatusBarExpandedShown = false;
    /**
     * Animator
     * 可以一举实现多个View多个动画效果
     */
    private TouchAnimator statusBarAnimator;
    private TouchAnimator.Listener sbAnimatorListener;


    public PanelView(Context context, WindowManager windowManager, WindowManager.LayoutParams params) {
        super(context);

        // TODO：设置背景方法
//        setBackgroundColor(Color.parseColor("#858585"));
        mContext = context;
        this.windowManager = windowManager;
        this.params = params;
        thisView = PanelView.this;

        initView();
        initListener();

        // TODO：设置margin方法
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(statusBarExpanded.getLayoutParams());
//        layoutParams.setMargins(0, statusBarHeight, 0, 0);
    }

    /**
     * 这个方法不能在View初始化的时候调用
     * 因为这时候子View还没测量好,无法获取状态栏的高度
     */
    private void initAnimator() {
        TouchAnimator.Builder statusBarBuilder = new TouchAnimator.Builder();
        statusBarBuilder.addFloat(statusBar, "translationY", -statusBarHeight, 0);
        statusBarBuilder.addFloat(statusBar, "alpha", (float) 0.3, (float) 0.7);
        statusBarAnimator = statusBarBuilder
                .setListener(sbAnimatorListener)
                .build();
    }

    private void initListener() {
        mGestureDetector = new GestureDetector(mContext, new MyGestureListener());
        mGestureDetector.setOnDoubleTapListener(new MyGestureListener());
        sbAnimatorListener = new TouchAnimator.Listener() {
            @Override
            public void onAnimationAtStart() {
                Log.e("TouchAnimator: ", "onAnimationAtStart: ");
            }

            @Override
            public void onAnimationAtEnd() {
                isStatusBarShown = true;
                Log.e("TouchAnimator: ", "onAnimationAtEnd: ");
            }

            @Override
            public void onAnimationStarted() {
                Log.e("TouchAnimator: ", "onAnimationStarted: ");
            }
        };
    }

    private void initView() {
        inflate(mContext, R.layout.layout_base_systemui, this);

        metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        statusBar = (RelativeLayout) findViewById(R.id.status_bar_parent);
        statusBarHeight = getResources().getDimensionPixelSize(R.dimen.dimen_status_bar_height);
        Log.e("mLog ", "height" + statusBarHeight);
        statusBarExpanded = (RelativeLayout) findViewById(R.id.status_bar_expanded);
        panelView = (LinearLayout) findViewById(R.id.panel_view);
        //帧布局中视图显示是按照栈的方式这样就可以把高斯模糊置底了
        blurView = (ImageView) findViewById(R.id.blur_background);
    }


    @SuppressLint("NewApi")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            /*
            将按下和拖动交给Gesture处理
             */
            case MotionEvent.ACTION_DOWN:
                isTouching = true;
                if (canvas != null && !isStatusBarExpandedShown) {
//                    changeBitmap(ScreenShotUtil.getBitmap());
                }
                //初始化Animator
                // TODO：使用getResource就可以把这个初始化提前了
                if (statusBarAnimator == null) {
                    statusBar.setY(0 - statusBarHeight);
                    initAnimator();
                    Log.e("mLog ", "h2" + statusBar.getHeight());
                }
            case MotionEvent.ACTION_MOVE:
                return mGestureDetector.onTouchEvent(event);
            case MotionEvent.ACTION_UP:
                isTouching = false;
                /*
                处理自动展开和收回
                 */
                if (panelView.getY() + panelView.getHeight() < MIN_EXPEND_HIGH  //getY是指View的顶部位置,小于最小展开高度的话就自动回收
                        && enablePullDownPanelView
                        && isStatusBarShown) {
                    statusBar.setVisibility(VISIBLE);
                    /*
                    整个布局先滚出顶部边缘
                    这是为了重新只显示状态栏
                     */
                    panelView.setTranslationY(0 - panelView.getHeight());
                    statusBarExpanded.setVisibility(GONE);
                    isStatusBarExpandedShown = false;
                    blurView.setVisibility(GONE);
                    panelView.setTranslationY(0);
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    windowManager.updateViewLayout(thisView, params);
                } else if (isStatusBarExpandedShown) {//自动下拉到底部
                    // TODO：获取屏幕高度
                    //setTranslationY设置的是这个View顶部在的位置,要注意
                    panelView.setTranslationY(metrics.heightPixels - panelView.getHeight());
                    blurArea.set(0, 0, metrics.widthPixels, metrics.heightPixels);
                    blurView.setClipBounds(blurArea);
                }
                //在放手后无任何操作就隐藏状态栏
                statusBar.postDelayed(statusBarHideRunnable, 1000);
        }
        return true;
    }

    Runnable statusBarHideRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isTouching && !isStatusBarExpandedShown) {
                statusBar.setVisibility(INVISIBLE);
                enablePullDownPanelView = false;
                isStatusBarShown = false;
            }
        }
    };

    /**
     * 手势处理
     */
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        public MyGestureListener() {
            super();
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.e(TAG, "onDoubleTap");
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            Log.e(TAG, "onDoubleTapEvent");
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.e(TAG, "onSingleTapConfirmed");
            return true;
        }

        @Override
        public boolean onContextClick(MotionEvent e) {
            Log.e(TAG, "onContextClick");
            return true;
        }

        @SuppressLint("NewApi")
        @Override
        public boolean onDown(MotionEvent e) {
            if (isStatusBarShown) {
                //只要状态栏显示了,就可以下拉
                enablePullDownPanelView = true;
                //重置状态栏是否要隐藏的时间
                statusBar.removeCallbacks(statusBarHideRunnable);
                //防止一按下状态栏就闪烁出下拉栏
                if (!isStatusBarExpandedShown) {
                    statusBarExpanded.setVisibility(INVISIBLE);
                    isStatusBarExpandedShown = true;
                }
                //覆盖整个屏幕,不然没有位置提供下拉
                params.height = WindowManager.LayoutParams.MATCH_PARENT;
                windowManager.updateViewLayout(thisView, params);
            }
            Log.e(TAG, "onDown");
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            Log.e(TAG, "onShowPress");
        }


        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.e(TAG, "onSingleTapUp");
            return true;
        }


        /*
        输入一个正整数数组，把数组里所有数字拼接起来排成一个数，打印能拼接出的所有数字中最小的一个。例如输入数组{3，32，321}，则打印出这三个数字能排成的最小数字为321323。
         */
        public String PrintMinNumber(int[] numbers) {
            String result = null;
            if (numbers.length == 0) return "";
            int n = numbers.length;
            String[] s = new String[n];
            for (int i = 0; i < n; i++) {
                s[i] = numbers[i] + "";
            }
            Arrays.sort(s, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    String s1 = o1 + o2;
                    String s2 = o2 + o1;
                    return s1.compareTo(s2);
                }
            });
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < n; i++) {
                stringBuilder.append(s[i]);
            }
            return stringBuilder.toString();
        }

        /*
        扑克牌
        01234
        12345
        9 10 11 12 13
        56

        5 7 10 6 1
        1 5 6 7 10
        1
         */
        public boolean isContinuous(int[] numbers) {
            //有重复、长度为0、还是要走一遍
            if (numbers.length == 0) return false;
            int count = 0;
            Arrays.sort(numbers);
            int id = 0;
            for (int i = 0; i < 5; i++) {
                if (numbers[i] == 0) {
                    count++;
                    id++;//10101
                } else {
                    break;
                }
            }
            for (int i = id; i < 4; i++) {
                int k = numbers[i + 1] - numbers[i];
                if (k == 1) {

                } else if (k == 0) {
                    return false;
                } else if (count > 0) {
                    count -= k;
                    count++;
                    if (count < 0) return false;
                } else {
                    return false;
                }
            }
            return true;
        }

        /*
        儿童节
        0-n-1
        m-1出列
        5 2
        0 1 2 3 4 5
         */

        public int LastRemaining_Solution(int n, int m) {
            LinkedList<Integer> list = new LinkedList<Integer>();
            for (int i = 0; i < n; i++) {
                list.add(i);
            }

            int bt = 0;
            while (list.size() > 1) {
                bt = (bt + m - 1) % list.size();
                list.remove(bt);
            }

            return list.size() == 1 ? list.get(0) : -1;
        }

        /*
        求1+2+3+...+n，要求不能使用乘除法、for、while、if、else、switch、case等关键字及条件判断语句（A?B:C）。
         */
        public int Sum_Solution(int n) {
            int sum = n;
            //短路特性
            boolean ans = (n > 0) && ((sum += Sum_Solution(n - 1)) > 0);
            return sum;
        }

        /*
        写一个函数，求两个整数之和，要求在函数体内不得使用+、-、*、/四则运算符号。
         */
        public int Add(int num1, int num2) {
            /*
             -5+-6=-11
             -5+6=1
             -6+5=-1
              */
            while (num2 > 0) {
                num1++;
                num2--;
            }
            return num1;
        }


        public int StrToInt(String str) {
            if (str.equals("") || str.length() == 0)
                return 0;
            char[] a = str.toCharArray();
            int fuhao = 0;
            if (a[0] == '-')
                fuhao = 1;
            int sum = 0;
            for (int i = fuhao; i < a.length; i++) {
                if (a[i] == '+')
                    continue;
                if (a[i] < 48 || a[i] > 57)// '0'是48
                    return 0;
                sum = sum * 10 + a[i] - 48;
            }
            return fuhao == 0 ? sum : sum * -1;
        }

        /*
        在一个长度为n的数组里的所有数字都在0到n-1的范围内。 数组中某些数字是重复的，但不知道有几个数字是重复的。
        也不知道每个数字重复几次。请找出数组中任意一个重复的数字。
        例如，如果输入长度为7的数组{2,3,1,0,2,5,3}，那么对应的输出是第一个重复的数字2。
         */
        // Parameters:
        //    numbers:     an array of integers
        //    length:      the length of array numbers
        //    duplication: (Output) the duplicated number in the array number,length of duplication array is 1,so using duplication[0] = ? in implementation;
        //                  Here duplication like pointor in C/C++, duplication[0] equal *duplication in C/C++
        //    这里要特别注意~返回任意重复的一个，赋值duplication[0]
        // Return value:       true if the input is valid, and there are some duplications in the array number
        //                     otherwise false
        public boolean duplicate(int numbers[], int length, int[] duplication) {
            if (length == 0) return false;
            HashSet<Integer> set = new HashSet<>();
            for (int i = 0; i < length; i++) {
                int l = set.size();
                set.add(numbers[i]);
                if (set.size() == l) {
                    duplication[0] = numbers[i];
                    return true;
                }
            }
            return false;
        }

        /*
        给定一个数组A[0,1,...,n-1],请构建一个数组B[0,1,...,n-1],其中B中的元素B[i]=A[0]*A[1]*...*A[i-1]*A[i+1]*...*A[n-1]。不能使用除法。
         */

        public int[] multiply(int[] A) {
            int length = A.length;
            int[] B = new int[length];
            if (length != 0) {
                B[0] = 1;
                //计算下三角连乘
                for (int i = 1; i < length; i++) {
                    B[i] = B[i - 1] * A[i - 1];
                }
                int temp = 1;
                //计算上三角
                for (int j = length - 2; j >= 0; j--) {
                    temp *= A[j + 1];
                    B[j] *= temp;
                }
            }
            return B;
        }

        /*
        请实现一个函数用来匹配包括'.'和'*'的正则表达式。模式中的字符'.'表示任意一个字符，而'*'表示它前面的字符可以出现任意次（包含0次）。
        在本题中，匹配是指字符串的所有字符匹配整个模式。例如，字符串"aaa"与模式"a.a"和"ab*ac*a"匹配，但是与"aa.a"和"ab*a"均不匹配
         */
        public boolean match(char[] str, char[] pattern) {
            if (str == null || pattern == null) {
                return false;
            }
            int strIndex = 0;
            int patternIndex = 0;
            return matchCore(str, strIndex, pattern, patternIndex);
        }

        public boolean matchCore(char[] str, int strIndex, char[] pattern, int patternIndex) {
            //有效性检验：str到尾，pattern到尾，匹配成功
            if (strIndex == str.length && patternIndex == pattern.length) {
                return true;
            }
            //pattern先到尾，匹配失败
            if (strIndex != str.length && patternIndex == pattern.length) {
                return false;
            }
            //模式第2个是*，且字符串第1个跟模式第1个匹配,分3种匹配模式；如不匹配，模式后移2位
            if (patternIndex + 1 < pattern.length && pattern[patternIndex + 1] == '*') {
                if ((strIndex != str.length && pattern[patternIndex] == str[strIndex]) || (pattern[patternIndex] == '.' && strIndex != str.length)) {
                    return matchCore(str, strIndex, pattern, patternIndex + 2)//模式后移2，视为x*匹配0个字符
                            || matchCore(str, strIndex + 1, pattern, patternIndex + 2)//视为模式匹配1个字符
                            || matchCore(str, strIndex + 1, pattern, patternIndex);//*匹配1个，再匹配str中的下一个
                } else {
                    return matchCore(str, strIndex, pattern, patternIndex + 2);
                }
            }
            //模式第2个不是*，且字符串第1个跟模式第1个匹配，则都后移1位，否则直接返回false
            if ((strIndex != str.length && pattern[patternIndex] == str[strIndex]) || (pattern[patternIndex] == '.' && strIndex != str.length)) {
                return matchCore(str, strIndex + 1, pattern, patternIndex + 1);
            }
            return false;
        }

        /*
        请实现一个函数用来判断字符串是否表示数值（包括整数和小数）。
        例如，字符串"+100","5e2","-123","3.1416"和"-1E-16"都表示数值。 但是"12e","1a3.14","1.2.3","+-5"和"12e+4.3"都不是。
         */
        public boolean isNumeric(char[] str) {//虽然过不了,但是这个中断法还是可以参考一下的
            boolean f1 = true;
            boolean e = false;
            boolean must = false;
            int count = 0;
            int count1 = 0;
            for (int i = 0; i < str.length; i++) {
                if (count > 1 || count1 > 1 || (count == 1 && count1 == 1)) return false;
                if (f1) {
                    if (str[i] == '+' || str[i] == '-') {
                        f1 = false;
                        continue;
                    }
                }
                if (str[i] >= '0' && str[i] <= '9') {
                    f1 = false;
                    must = false;
                    continue;
                }
                if (!must) {
                    if (str[i] == 'e' || str[i] == 'E') {
                        f1 = true;
                        must = true;
                        count1++;
                        continue;
                    }
                }
                if (!must && str[i] == '.') {
                    must = true;
                    count++;
                    continue;
                }
                return false;
            }
            if (must) return false;
            return true;
        }

        //Insert one char from stringstream

        HashMap<Character, Integer> map = new HashMap();
        ArrayList<Character> list = new ArrayList<Character>();

        //Insert one char from stringstream
        public void Insert(char ch) {
            if (map.containsKey(ch)) {
                map.put(ch, map.get(ch) + 1);
            } else {
                map.put(ch, 1);
            }

            list.add(ch);
        }

        //return the first appearence once char in current stringstream
        public char FirstAppearingOnce() {
            char c = '#';
            for (char key : list) {
                if (map.get(key) == 1) {
                    c = key;
                    break;
                }
            }
            return c;
        }

        /*
        给一个链表，若其中包含环，请找出该链表的环的入口结点，否则，输出null。
         */
        public class ListNode {
            int val;
            ListNode next = null;

            ListNode(int val) {
                this.val = val;
            }
        }

        /*

         */
        public ListNode EntryNodeOfLoop(ListNode pHead) {
            if (pHead == null || pHead.next == null || pHead.next.next == null) return null;
            ListNode fast = pHead.next.next;
            ListNode slow = pHead.next;
            while (fast != slow) {
                if (slow.next == null || fast.next == null) return null;
                fast = fast.next.next;
                slow = slow.next;
            }
            fast = pHead;
            while (fast != slow) {
                fast = fast.next;
                slow = slow.next;
            }
            return slow;
        }

        /*
        在一个排序的链表中，存在重复的结点，请删除该链表中重复的结点，重复的结点不保留，返回链表头指针。 例如，链表1->2->3->3->4->4->5 处理后为 1->2->5
         */
        public ListNode deleteDuplication(ListNode pHead) {
            ArrayList<Integer> list = new ArrayList<>();
            ListNode t = pHead;
            ListNode pre = null;
            while (pHead != null) {
                if (list.contains(pHead.val)) {
                    pre.next = pHead.next;
                    continue;
                } else {
                    list.add(pHead.val);
                }
                pre = pHead;
                pHead = pHead.next;
            }
            return t;
        }

        /*
        给定一个二叉树和其中的一个结点，请找出中序遍历顺序的下一个结点并且返回。注意，树中的结点不仅包含左右子结点，同时包含指向父结点的指针。
         */
        public class TreeLinkNode {
            int val;
            TreeLinkNode left = null;
            TreeLinkNode right = null;
            TreeLinkNode next = null;

            TreeLinkNode(int val) {
                this.val = val;
            }
        }

        public TreeLinkNode GetNext(TreeLinkNode pNode) {

        }

        /*
        请实现一个函数，用来判断一颗二叉树是不是对称的。注意，如果一个二叉树同此二叉树的镜像是同样的，定义其为对称的。
         */
        public class TreeNode {
            int val = 0;
            TreeNode left = null;
            TreeNode right = null;

            public TreeNode(int val) {
                this.val = val;

            }

        }


        boolean isSymmetrical(TreeNode pRoot) {
            if (pRoot == null) {
                return true;
            }
            return comRoot(pRoot.left, pRoot.right);
        }

        private boolean comRoot(TreeNode left, TreeNode right) {
            // TODO Auto-generated method stub
            if (left == null) return right == null;//左null,右一定null
            if (right == null) return false;//左不null,右为null就不行
            if (left.val != right.val) return false;//不相等就false
            return comRoot(left.right, right.left) && comRoot(left.left, right.right);
        }


        /*
        请实现一个函数按照之字形打印二叉树，即第一行按照从左到右的顺序打印，第二层按照从右至左的顺序打印，第三行按照从左到右的顺序打印，其他行以此类推。
         */


        public ArrayList<ArrayList<Integer>> Print1(TreeNode pRoot) {
            ArrayList<ArrayList<Integer>> ret = new ArrayList<>();
            if (pRoot == null) {
                return ret;
            }
            ArrayList<Integer> list = new ArrayList<>();
            LinkedList<TreeNode> queue = new LinkedList<>();
            queue.addLast(null);//层分隔符
            queue.addLast(pRoot);//模拟先进先出
            boolean leftToRight = true;

            while (queue.size() != 1) {
                TreeNode node = queue.removeFirst();//出队
                if (node == null) {//到达层分隔符
                    Iterator<TreeNode> iter = null;
                    if (leftToRight) {
                        iter = queue.iterator();//从前往后遍历
                    } else {
                        iter = queue.descendingIterator();//从后往前遍历
                    }
                    leftToRight = !leftToRight;//快速反转flag
                    while (iter.hasNext()) {
                        TreeNode temp = (TreeNode) iter.next();
                        list.add(temp.val);
                    }
                    ret.add(new ArrayList<Integer>(list));//应该是类似拷贝的
                    list.clear();
                    queue.addLast(null);//添加层分隔符
                    continue;//一定要continue
                }
                if (node.left != null) {
                    queue.addLast(node.left);
                }
                if (node.right != null) {
                    queue.addLast(node.right);
                }
            }
            return ret;
        }


        /*
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
         */
        /*
        从上到下按层打印二叉树，同一层结点从左至右输出。每一层输出一行。
         */

        ArrayList<ArrayList<Integer>> Print(TreeNode pRoot) {
            ArrayList<ArrayList<Integer>> ret = new ArrayList<>();
            if (pRoot == null) return ret;
            LinkedList<TreeNode> queue = new LinkedList<>();
            ArrayList<Integer> list = new ArrayList<>();
            queue.addLast(null);
            queue.addLast(pRoot);
            while (queue.size() != 1) {
                TreeNode t = queue.removeFirst();
                if (t == null) {
                    Iterator<TreeNode> n = null;
                    n = queue.iterator();
                    while (n.hasNext()) {
                        TreeNode tt = n.next();
                        list.add(tt.val);
                    }
                    ret.add(new ArrayList<Integer>(list));
                    list.clear();
                    queue.addLast(null);
                    continue;
                }
                if (t.left != null) queue.addLast(t.left);
                if (t.right != null) queue.addLast(t.right);
            }
            return ret;
        }

        /*
        递归
         */
        ArrayList<ArrayList<Integer>> Print2(TreeNode pRoot) {
            ArrayList<ArrayList<Integer>> list = new ArrayList<>();
            depth(pRoot, 1, list);
            return list;
        }

        private void depth(TreeNode root, int depth, ArrayList<ArrayList<Integer>> list) {//void的递归也可以
            if (root == null) return;
            if (depth > list.size())
                list.add(new ArrayList<Integer>());
            list.get(depth - 1).add(root.val);//只有匹配了depth的才会放入

            depth(root.left, depth + 1, list);
            depth(root.right, depth + 1, list);
        }

        /*
        请实现两个函数，分别用来序列化和反序列化二叉树
        实际上就是实现出字符串
         */

        public int index = -1;

        String Serialize(TreeNode root) {//1,2,#,#,3,#,#
            StringBuffer sb = new StringBuffer();
            if (root == null) {
                sb.append("#,");
                return sb.toString();
            }
            sb.append(root.val + ",");
            sb.append(Serialize(root.left));
            sb.append(Serialize(root.right));
            return sb.toString();
        }

        TreeNode Deserialize(String str) {
            index++;//0 1 2
            int len = str.length();
            if (index >= len) {
                return null;
            }
            String[] strr = str.split(",");//1 2 # # 3 # #
            TreeNode node = null;
            if (!strr[index].equals("#")) {//0 1 #
                node = new TreeNode(Integer.valueOf(strr[index]));//1 2
                node.left = Deserialize(str);
                node.right = Deserialize(str);
            }
            return node;
        }

        /*
        给定一棵二叉搜索树，请找出其中的第k小的结点。例如， （5，3，7，2，4，6，8）    中，按结点数值大小顺序第三小结点的值为4
        二叉搜索树按照中序遍历的顺序打印出来正好就是排序好的顺序。
             所以，按照中序遍历顺序找到第k个结点就是结果。
         */
        int index1 = 0; //计数器

        TreeNode KthNode(TreeNode root, int k) {
            if (root != null) { //中序遍历寻找第k个,考虑性质
                TreeNode node = KthNode(root.left, k);
                if (node != null)
                    return node;
                index1++;
                if (index1 == k)
                    return root;
                node = KthNode(root.right, k);
                if (node != null)
                    return node;
            }
            return null;
        }

        /*
        如何得到一个数据流中的中位数？如果从数据流中读出奇数个数值，那么中位数就是所有数值排序之后位于中间的数值。
        如果从数据流中读出偶数个数值，那么中位数就是所有数值排序之后中间两个数的平均值。我们使用Insert()方法读取数据流，使用GetMedian()方法获取当前读取数据的中位数。
         */
        int count;
        PriorityQueue<Integer> minHeap = new PriorityQueue<Integer>();
        PriorityQueue<Integer> maxHeap = new PriorityQueue<Integer>(11, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                //PriorityQueue默认是小顶堆，实现大顶堆，需要反转默认排序器
                return o2.compareTo(o1);
            }
        });

        public void Insert(Integer num) {// 5 3 2 1 4
            count++;
            if ((count & 1) == 0) { // 判断偶数的高效写法
                if (!maxHeap.isEmpty() && num < maxHeap.peek()) {
                    maxHeap.offer(num);//3 2 1
                    num = maxHeap.poll();//3
                }
                minHeap.offer(num);//5 3
            } else {
                if (!minHeap.isEmpty() && num > minHeap.peek()) {//
                    minHeap.offer(num);
                    num = minHeap.poll();//
                }
                maxHeap.offer(num);//3 2
            }
        }

        public Double GetMedian() {
            if (count == 0)
                throw new RuntimeException("no available number!");
            double result;
            //总数为奇数时，大顶堆堆顶就是中位数
            if ((count & 1) == 1)
                result = maxHeap.peek();
            else
                result = (minHeap.peek() + maxHeap.peek()) / 2.0;//注意这个2.0
            return result;
        }

        public ArrayList<Integer> maxInWindows3(int[] num, int size) {
            //如果不是第一个最大就不重新比较了
            ArrayList<Integer> list = new ArrayList<>();
            PriorityQueue<Integer> queue = new PriorityQueue<>(11, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o2 - o1;
                }
            });
            if (num.length == 0 || size == 0 || size > num.length) return list;
            int s = 0;
            int e = size - 1;
            while (s <= e) {
                queue.offer(num[s]);
                s++;
            }
            list.add(queue.peek());
            for (int i = size; i < num.length; i++) {
                queue.offer(num[i]);
                queue.poll();
                list.add(queue.peek());
            }
            return list;
        }

        /*
        给定一个数组和滑动窗口的大小，找出所有滑动窗口里数值的最大值。例如，如果输入数组{2,3,4,2,6,2,5,1}及滑动窗口的大小3，那么一共存在6个滑动窗口，他们的最大值分别为{4,4,6,6,6,5}；
         针对数组{2,3,4,2,6,2,5,1}的滑动窗口有以下6个： {[2,3,4],2,6,2,5,1}， {2,[3,4,2],6,2,5,1}， {2,3,[4,2,6],2,5,1}， {2,3,4,[2,6,2],5,1}， {2,3,4,2,[6,2,5],1}， {2,3,4,2,6,[2,5,1]}。
         */
        public ArrayList<Integer> maxInWindows(int[] num, int size) {
            //如果不是第一个最大就不重新比较了
            ArrayList<Integer> list = new ArrayList<>();
            if (num.length == 0 || size == 0 || size > num.length) return list;
            int s = 0;
            int e = size - 1;
            boolean flag = false;
            int t = -9999;
            while (e < num.length) {
                int max = 0;
                if (t != -9999) {
                    if (num[e] > t) {
                        max = num[e];
                    } else {
                        max = t;
                    }
                    t = -9999;
                } else {
                    for (int i = s; i <= e; i++) {
                        if (num[i] > max) {
                            max = num[i];
                        }
                    }
                }
                if (max != num[s]) {
                    t = max;//4
                }
                list.add(max);
                s++;
                e++;
            }
            return list;
        }


        public ArrayList<Integer> maxInWindows1(int[] num, int size) {
            ArrayList<Integer> res = new ArrayList<>();
            if (size == 0) return res;
            int begin;
            ArrayDeque<Integer> q = new ArrayDeque<>();
            for (int i = 0; i < num.length; i++) {
                begin = i - size + 1;
                if (q.isEmpty())
                    q.add(i);
                else if (begin > q.peekFirst())
                    q.pollFirst();

                while ((!q.isEmpty()) && num[q.peekLast()] <= num[i])
                    q.pollLast();
                q.add(i);
                if (begin >= 0)
                    res.add(num[q.peekFirst()]);
            }
            return res;
        }

        /*
        请设计一个函数，用来判断在一个矩阵中是否存在一条包含某字符串所有字符的路径。路径可以从矩阵中的任意一个格子开始，
        每一步可以在矩阵中向左，向右，向上，向下移动一个格子。如果一条路径经过了矩阵中的某一个格子，则之后不能再次进入这个格子。
         例如 a b c e s f c s a d e e 这样的3 X 4 矩阵中包含一条字符串"bcced"的路径，但是矩阵中不包含"abcb"路径，因为字符串的第一个字符b占据了矩阵中的第一行第二个格子之后，路径不能再次进入该格子。
         abce
         sfcs
         adee
         */
        public boolean hasPath(char[] matrix, int rows, int cols, char[] str) {
            int flag[] = new int[matrix.length];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (helper(matrix, rows, cols, i, j, str, 0, flag))
                        return true;
                }
            }
            return false;
        }

        private boolean helper(char[] matrix, int rows, int cols, int i, int j, char[] str, int k, int[] flag) {
            int index = i * cols + j;
            if (i < 0 || i >= rows || j < 0 || j >= cols || matrix[index] != str[k] || flag[index] == 1)
                return false;
            if (k == str.length - 1) return true;
            flag[index] = 1;
            if (helper(matrix, rows, cols, i - 1, j, str, k + 1, flag)
                    || helper(matrix, rows, cols, i + 1, j, str, k + 1, flag)
                    || helper(matrix, rows, cols, i, j - 1, str, k + 1, flag)
                    || helper(matrix, rows, cols, i, j + 1, str, k + 1, flag)) {
                return true;
            }
            flag[index] = 0;//注意,不太懂
            return false;
        }

        /*
        地上有一个m行和n列的方格。一个机器人从坐标0,0的格子开始移动，每一次只能向左，右，上，下四个方向移动一格，但是不能进入行坐标和列坐标的数位之和大于k的格子。
        例如，当k为18时，机器人能够进入方格（35,37），因为3+5+3+7 = 18。但是，它不能进入方格（35,38），因为3+5+3+8 = 19。请问该机器人能够达到多少个格子？
         */

        public int movingCount(int threshold, int rows, int cols) {
            int flag[][] = new int[rows][cols]; //记录是否已经走过
            return helper(0, 0, rows, cols, flag, threshold);
        }

        private int helper(int i, int j, int rows, int cols, int[][] flag, int threshold) {
            if (i < 0 || i >= rows || j < 0 || j >= cols || numSum(i) + numSum(j) > threshold || flag[i][j] == 1)
                return 0;
            flag[i][j] = 1;
            return helper(i - 1, j, rows, cols, flag, threshold)
                    + helper(i + 1, j, rows, cols, flag, threshold)
                    + helper(i, j - 1, rows, cols, flag, threshold)
                    + helper(i, j + 1, rows, cols, flag, threshold)
                    + 1;
        }

        private int numSum(int i) {
            int sum = 0;
            do {
                sum += i % 10;
            } while ((i = i / 10) > 0);
            return sum;
        }

        /*
根据每日 气温 列表，请重新生成一个列表，对应位置的输入是你需要再等待多久温度才会升高超过该日的天数。如果之后都不会升高，请在该位置用 0 来代替。
例如，给定一个列表 temperatures = [73, 74, 75, 71, 69, 72, 76, 73]，你的输出应该是 [1, 1, 4, 2, 1, 1, 0, 0]。
提示：气温 列表长度的范围是 [1, 30000]。每个气温的值的均为华氏度，都是在 [30, 100] 范围内的整数。
         */
        public int[] dailyTemperatures(int[] T) {//TreeMap默认升序
            int n = T.length;//逆向思维
            int[] a = new int[n];//果然有跳过已经比较的方法
            if (n == 0) return a;
            a[n - 1] = 0;
            for (int i = n - 2; i >= 0; i--) {
                for (int j = i + 1; j < n; j += a[j]) {
                    if (T[i] < T[j]) {
                        a[i] = j - i;
                        break;
                    } else if (a[j] == 0) {
                        a[i] = 0;
                        break;
                    }
                }
            }
            return a;
        }

        /*
        aaa 6
         */
        public int countSubstrings(String s) {//动态规划
            int len = s.length(), cnt = 0;
            for (int i = 0; i < len; i++) {
                for (int j = i + 1; j <= len; j++) {
                    String sbs = s.substring(i, j);
                    StringBuffer sb = new StringBuffer(sbs);
                    if (sb.reverse().toString().equals(sbs))//sb的reverse方法
                        cnt++;
                }
            }
            return cnt;
        }

        public int countSubstringsDP(String s) {
            char[] charArr = s.toCharArray();
            int n = charArr.length;
            int count = 0;
            boolean dp[][] = new boolean[n][n];
            for (int i = n - 1; i >= 0; i--) {//aba
                for (int j = i; j < n; j++) {
                    //j-i = {0,1,2}代表一个,两个,三个,字符时 此时可以根据charArr[i] == charArr[j]
                    //得到s[i..j]必定回文
                    if (charArr[i] == charArr[j] && (j - i <= 2 || dp[i + 1][j - 1])) {
                        dp[i][j] = true;
                        count++;
                    }
                }
            }
            return count;
        }

        /*
        AAABBB 2 8
        ABCABC 2 6
        最短,无序
         */
        public int leastInterval(char[] tasks, int n) {//考虑必然性
            int[] count = new int[26];
            for (int i = 0; i < tasks.length; i++) {
                count[tasks[i] - 'A']++;
            }//统计词频
            Arrays.sort(count);//词频排序，升序排序，count[25]是频率最高的
            int maxCount = 0;
            //统计有多少个频率最高的字母
            for (int i = 25; i >= 0; i--) {
                if (count[i] != count[25]) {
                    break;
                }
                maxCount++;
            }
            //公式算出的值可能会比数组的长度小，取两者中最大的那个
            return Math.max((count[25] - 1) * (n + 1) + maxCount, tasks.length);
        }

        public TreeNode mergeTrees(TreeNode t1, TreeNode t2) {
            if (t1 == null) return t2;
            if (t2 == null) return t1;
            t1.val = t1.val + t2.val;
            t1.left = mergeTrees(t1.left, t2.left);
            t1.right = mergeTrees(t1.right, t2.right);
            return t1;
        }

        /*
         2 6 4 8 10 9 15
         1 3 2 2
         1 2 3
         2 1
         很简单，如果最右端的一部分已经排好序，这部分的每个数都比它左边的最大值要大，同理，如果最左端的一部分排好序，这每个数都比它右边的最小值小。
         所以我们从左往右遍历，如果i位置上的数比它左边部分最大值小，则这个数肯定要排序， 就这样找到右端不用排序的部分，同理找到左端不用排序的部分，它们之间就是需要排序的部分
          */
        public int findUnsortedSubarray(int[] arr) {//暴力一个一个if是很容易出错的,要抽象出关键
            if (arr == null || arr.length < 1) {
                return 0;
            }
            if (arr.length == 2 && arr[0] > arr[1]) {
                return arr.length;
            }
            if (arr.length == 2 && arr[0] < arr[1]) {
                return 0;
            }
            int s = 0;
            int e = 0;
            for (int i = 1; i < arr.length; i++) {
                if (s != 0 && e == 0 && arr[i - 1] > arr[i]) {
                    e = i;
                }
                if (s == 0 && arr[i - 1] > arr[i]) {
                    s = i - 1;
                }
            }

            if (e == 0 && s != 0) {
                return arr.length - s;
            } else {
                return e == s ? 0 : e - s + 1;
            }
        }

        public int subarraySum(int[] nums, int k) {
            /**
             扫描一遍数组, 使用map记录出现同样的和的次数, 对每个i计算累计和sum并判断map内是否有sum-k
             **/
            Map<Integer, Integer> map = new HashMap<>();
            map.put(0, 1);
            int sum = 0, ret = 0;

            for(int i = 0; i < nums.length; ++i) {
                sum += nums[i];//21345 5
                if(map.containsKey(sum-k))
                    ret += map.get(sum-k);
                map.put(sum, map.getOrDefault(sum, 0)+1);//如果存在否则默认
            }
            return ret;
        }
        int max = 0;

        public int diameterOfBinaryTree(TreeNode root) {
            if (root != null) {
                //遍历每一个节点,求出此节点作为根的树的深度,那么,左子树深度加右子树深度的最大值即是答案
                setDepth(root);
                return max;
            }
            return 0;
        }

        public int setDepth(TreeNode root) {
            if (root != null) {
                int right = setDepth(root.right);
                int left = setDepth(root.left);
                if (right + left > max)
                    max = right + left;
                return Math.max(right, left) + 1;
            }
            return 0;
        }

        public TreeNode convertBST(TreeNode root) {

        }


        public int findTargetSumWays(int[] nums, int S) {

        }

        public int hammingDistance(int x, int y) {

        }

        public List<Integer> findDisappearedNumbers(int[] nums) {

        }

        public List<Integer> findAnagrams(String s, String p) {

        }


        /**
         * @param e1        之前的DOWN
         * @param e2        现在的MOVE
         * @param distanceX 当前MOVE和上一个MOVE的位移量
         */
        @SuppressLint("NewApi")
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!isStatusBarShown) {//第一次下拉的时候只可以下拉状态栏
                statusBar.setVisibility(VISIBLE);
                //计算移动的百分比提供给Animator
                float position = (e2.getY() - statusBarHeight) / statusBarHeight;
                statusBarAnimator.setPosition(position);
                //
                applyBlur();
            } else if (enablePullDownPanelView) {

                statusBarExpanded.setVisibility(VISIBLE);
                blurView.setVisibility(VISIBLE);
                //下拉通知栏
                panelView.setTranslationY(e2.getY() - panelView.getHeight());
                blurArea.set(0, 0, metrics.widthPixels, (int) e2.getY());
                blurView.setClipBounds(blurArea);
            }
            Log.e(TAG, "onScroll");
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.e(TAG, "onLongPress");
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.e(TAG, "onFling");
            return true;
        }

    }

    public PanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PanelView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 高斯模糊
     */

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void applyBlur() {
        overlay = Bitmap.createBitmap(metrics.widthPixels,
                metrics.heightPixels, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(overlay);
        blurArea = new Rect(0, 0, metrics.widthPixels, 10);


        canvas.drawBitmap(ScreenShotUtil.getBitmap(), 0, 0, null);
        finalBitmap = BlurUtil.with(mContext)
                .bitmap(overlay) //要模糊的图片
                .radius(7)//模糊半径
                .blur();

        blurView.setBackground(new BitmapDrawable(getResources(), finalBitmap));
    }

    @SuppressLint("NewApi")
    private void changeBitmap(Bitmap bitmap) {


        canvas.drawBitmap(bitmap, 0, 0, null);

        finalBitmap = BlurUtil.with(mContext)
                .bitmap(overlay) //要模糊的图片
                .radius(7)//模糊半径
                .blur();
        blurView.setVisibility(INVISIBLE);
        blurView.setBackground(new BitmapDrawable(getResources(), finalBitmap));

    }


    /**
     * 以下是可以实现自动动画的简单设想
     */
    Handler handler = new Handler();

    float f = 0f;
    Runnable runnable3 = new Runnable() {
        @Override
        public void run() {
            statusBarAnimator.setPosition(f);
            if (f <= 1f) {
                f += 0.2f;
                handler.postDelayed(runnable3, 100);
            }
        }
    };


}
