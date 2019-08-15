package com.android.touchanimatortest.test;

/**
 * @description:
 * @author: Xeldow
 * @date: 2019/8/1
 */
public class FirstActivity {
    /**
     * 我们可以用2*1的小矩形横着或者竖着去覆盖更大的矩形。请问用n个2*1的小矩形无重叠地覆盖一个2*n的大矩形，总共有多少种方法？
     */
    public class Solution {
        public int RectCover(int target) {
            if (target < 1) {//可能出现-2之后小于0,总之考虑各种情况
                return 0;
            }
            if (target == 1) {
                return 1;
            } else if (target == 2) {
                return 2;
            } else {
                return RectCover(target - 1) + RectCover(target - 2);
            }
        }
    }

    /**
     * 输入一个整数，输出该数二进制表示中1的个数。其中负数用补码表示。
     */

    public class Solution1 {
        public int NumberOf1(int n) {
            int count = 0;
            while (n != 0) {
                count++;
                n = n & (n - 1);
            }
            return count;
        }
    }

    /**
     * 给定一个double类型的浮点数base和int类型的整数exponent。求base的exponent次方。
     */
    public class Solution2 {
        public double Power(double base, int exponent) {
            if (exponent == 0) {
                return 1;
            }
            double a = base;
            boolean f = true;
            if (exponent < 0) {
                exponent = -exponent;
                f = false;
            }
            while (exponent - 1 > 0) {
                base = a * base;
                exponent--;
            }
            if (f) {
                return base;
            } else {
                return 1 / base;
            }
        }
    }

    /**
     * 输入一个整数数组，实现一个函数来调整该数组中数字的顺序，使得所有的奇数位于数组的前半部分，所有的偶数位于数组的后半部分，并保证奇数和奇数，偶数和偶数之间的相对位置不变。
     */

    public class Solution3 {
        public void reOrderArray(int[] array) {
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

    /**
     * 输入一个链表，输出该链表中倒数第k个结点。
     */
    public class ListNode {
        ListNode next;
    }

    public class Solution4 {
        public ListNode FindKthToTail(ListNode head, int k) {
            if (head == null || k <= 0) {//注意要小心
                return null;
            }
            ListNode pre = head;
            ListNode last = head;
            for (int i = 1; i < k; i++) {
                if (pre.next != null) {
                    pre = pre.next;
                } else {
                    return null;
                }
            }
            while (pre.next != null) {
                pre = pre.next;
                last = last.next;
            }
            return last;
        }
    }
}
