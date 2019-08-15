package com.android.touchanimatortest.test;

import java.util.ArrayList;
import java.util.Stack;

/**
 * @description:
 * @author: Xeldow
 * @date: 2019/8/14
 */
public class Coder {
    public class ListNode {
        int val;
        ListNode next = null;

        ListNode(int val) {
            this.val = val;
        }
    }

    public ArrayList<Integer> printListFromTailToHead(ListNode listNode) {
        ArrayList<Integer> list = new ArrayList<>();
        Stack<Integer> stack = new Stack<>();
        while (listNode != null) {
            stack.push(listNode.val);
            listNode = listNode.next;
        }
        while (!stack.empty()) {
            list.add(stack.pop());
        }
        return list;
    }

    ArrayList<Integer> list = new ArrayList<Integer>();

    public ArrayList<Integer> printListFromTailToHead1(ListNode listNode) {
        if (listNode != null) {
            this.printListFromTailToHead1(listNode.next);
            list.add(listNode.val);//操作一定要在递归之后
        }
        return list;
    }

    public class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int x) {
            val = x;
        }
    }

    /*
    不会
     */
    public TreeNode reConstructBinaryTree(int[] pre, int[] in) {//应该有更好的方法的
        TreeNode root = reConstructBinaryTree(pre, 0, pre.length - 1, in, 0, in.length - 1);
        return root;
    }

    //前序遍历{1,2,4,7,3,5,6,8}和中序遍历序列{4,7,2,1,5,3,8,6}
    private TreeNode reConstructBinaryTree(int[] pre, int startPre, int endPre, int[] in, int startIn, int endIn) {

        if (startPre > endPre || startIn > endIn)
            return null;
        //前序的第一个是根
        TreeNode root = new TreeNode(pre[startPre]);

        for (int i = startIn; i <= endIn; i++)
            if (in[i] == pre[startPre]) {//在中序中找到根,i对应着根的位置
                //新前序是前序根到中序根位置i之间,新中序是原中序到根位置i之间
                root.left = reConstructBinaryTree(pre, startPre + 1, startPre + i - startIn, in, startIn, i - 1);
                //新前序是除去中序的影响后加上前序的根...
                root.right = reConstructBinaryTree(pre, i - startIn + startPre + 1, endPre, in, i + 1, endIn);
                break;
            }

        return root;
    }

    Stack<Integer> stack1 = new Stack<Integer>();
    Stack<Integer> stack2 = new Stack<Integer>();

    public void push(int node) {
        while (!stack2.empty()) {//防止先出再进再出
            stack1.push(stack2.pop());
        }
        stack1.push(node);
    }

    public int pop() {
        while (!stack1.empty()) {
            stack2.push(stack1.pop());
        }
        return stack2.pop();
    }

    public int minNumberInRotateArray(int[] array) {
        int n = array.length;
        if (n == 0) return 0;
        if (n == 1) return array[0];
        for (int i = 1; i < n; i++) {
            if (array[i - 1] > array[i]) {
                return array[i];
            }
        }
        return 0;
    }

    public int minNumberInRotateArray1(int[] array) {
        int low = 0;
        int high = array.length - 1;
        while (low < high) {
            //用这种二分比较稳
            int mid = low + (high - low) / 2;
            if (array[mid] > array[high]) {
                low = mid + 1;
            } else if (array[high] == array[low]) {
                high = high - 1;
            } else {
                high = mid;
            }
        }
        return array[low];
    }

    public int Fibonacci(int n) {//递归超时了
        if (n == 1 || n == 2) return 1;
        int a = 1;
        int b = 1;
        int c = 0;
        for (int i = 3; i <= n; i++) {
            c = a + b;
            a = b;
            b = c;
        }
        return c;
    }

    /*
一只青蛙一次可以跳上1级台阶，也可以跳上2级。求该青蛙跳上一个n级的台阶总共有多少种跳法（先后次序不同算不同的结果）。
 */
    public int JumpFloor(int target) {
        if (target == 1) return 1;
        if (target == 2) return 2;
        return JumpFloor(target - 1) + JumpFloor(target - 2);
    }

    //    一只青蛙一次可以跳上1级台阶，也可以跳上2级……它也可以跳上n级。求该青蛙跳上一个n级的台阶总共有多少种跳法。
    //fn=fn-1+fn-2...f1
    //fn-1=fn-2...f1
    //fn-1=fn-fn-1
    public int JumpFloorII(int target) {
        if (target == 1) return 1;
        if (target == 2) return 2;
        return 2*JumpFloorII(target - 1);
    }

    //    我们可以用2*1的小矩形横着或者竖着去覆盖更大的矩形。请问用n个2*1的小矩形无重叠地覆盖一个2*n的大矩形，总共有多少种方法？
    public int RectCover(int n) {//这些说不定可以理解递归的思想
        if (n <= 2) return n;
        return RectCover(n - 1) + RectCover(n - 2);
    }

    //    输入一个整数，输出该数二进制表示中1的个数。其中负数用补码表示。
    public int NumberOf1(int n) {
        int count = 0;
        while (n != 0) {
            count++;
            n = n & (n - 1);//关于二进制肯定有&
        }
        return count;
    }

    public double Power(double base, int exponent) {
        boolean f = true;
        if (exponent < 0) {
            f = false;
            exponent = -exponent;
        }
        double ret = 1;
        while (exponent != 0) {
            ret = base * ret;
            exponent--;
        }
        if (f) return ret;
        return 1 / ret;
    }

    //1234567
    //1357246
    //1357264
    public void reOrderArray(int[] array) {
        int n = array.length;
        int idx = 0;
        int[] a1 = new int[n];
        int[] a2 = new int[n];
        int j = 0;
        int k = 0;
        for (int i = 0; i < n; i++) {//13245
            if (array[i] % 2 != 0) {
                a1[j] = array[i];
                j++;
            } else {
                a2[k] = array[i];
                k++;
            }
        }
        System.arraycopy(a1, 0, array, 0, j);//从a1的第0个开始拷贝j个到array的第0位开始
        System.arraycopy(a2, 0, array, j, k);
    }

    public void reOrderArray1(int[] array) {
        //相对位置不变，稳定性
        //插入排序的思想
        int m = array.length;
        int k = 0;//记录已经摆好位置的奇数的个数
        for (int i = 0; i < m; i++) {
            if (array[i] % 2 == 1) {
                int j = i;
                while (j > k) {//j >= k+1
                    int tmp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = tmp;
                    j--;
                }
                k++;
            }
        }
    }
}
