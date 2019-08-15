package com.android.touchanimatortest.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Stack;

/**
 * @description:
 * @author: Xeldow
 * @date: 2019/8/1
 */
public class AnActivity {

    public class ListNode {
        int val;
        ListNode next = null;

        ListNode(int val) {
            this.val = val;
        }
    }

    public class Solution {
        public ListNode ReverseList(ListNode head) {

            if (head == null)
                return null;
            //head为当前节点，如果当前节点为空的话，那就什么也不做，直接返回null；
            ListNode pre = null;
            ListNode next = null;
            //当前节点是head，pre为当前节点的前一节点，next为当前节点的下一节点
            //需要pre和next的目的是让当前节点从pre->head->next1->next2变成pre<-head next1->next2
            //即pre让节点可以反转所指方向，但反转之后如果不用next节点保存next1节点的话，此单链表就此断开了
            //所以需要用到pre和next两个节点
            //1->2->3->4->5
            //1<-2<-3 4->5
            while (head != null) {
                //做循环，如果当前节点不为空的话，始终执行此循环，此循环的目的就是让当前节点从指向next到指向pre
                //如此就可以做到反转链表的效果
                //先用next保存head的下一个节点的信息，保证单链表不会因为失去head节点的原next节点而就此断裂
                next = head.next;
                //保存完next，就可以让head从指向next变成指向pre了，代码如下
                head.next = pre;
                //head指向pre后，就继续依次反转下一个节点
                //让pre，head，next依次向后移动一个节点，继续下一次的指针反转
                pre = head;
                head = next;
            }
            //如果head为null的时候，pre就为最后一个节点了，但是链表已经反转完毕，pre就是反转后链表的第一个节点
            //直接输出pre就是我们想要得到的反转后的链表
            return pre;
        }
    }


    public class Solution2 {
        public ListNode Merge(ListNode list1, ListNode list2) {
            //新建一个头节点，用来存合并的链表。
            ListNode head = new ListNode(-1);
            head.next = null;
            ListNode root = head;
            while (list1 != null && list2 != null) {
                if (list1.val < list2.val) {
                    head.next = list1;
                    head = list1;
                    list1 = list1.next;
                } else {
                    head.next = list2;
                    head = list2;
                    list2 = list2.next;
                }
            }
            //把未结束的链表连接到合并后的链表尾部
            if (list1 != null) {
                head.next = list1;
            }
            if (list2 != null) {
                head.next = list2;
            }
            return root.next;
        }
    }


/*思路：参考剑指offer
1、首先设置标志位result = false，因为一旦匹配成功result就设为true，
剩下的代码不会执行，如果匹配不成功，默认返回false
2、递归思想，如果根节点相同则递归调用DoesTree1HaveTree2（），
如果根节点不相同，则判断tree1的左子树和tree2是否相同，
再判断右子树和tree2是否相同
3、注意null的条件，HasSubTree中，如果两棵树都不为空才进行判断，
DoesTree1HasTree2中，如果Tree2为空，则说明第二棵树遍历完了，即匹配成功，
tree1为空有两种情况（1）如果tree1为空&&tree2不为空说明不匹配，
（2）如果tree1为空，tree2为空，说明匹配。

*/

    public class TreeNode {
        int val = 0;
        TreeNode left = null;
        TreeNode right = null;

        public TreeNode(int val) {
            this.val = val;

        }

    }

    public class Solution3 {
        public boolean HasSubtree(TreeNode root1, TreeNode root2) {
            boolean result = false;
            if (root1 != null && root2 != null) {
                if (root1.val == root2.val) {
                    result = DoesTree1HaveTree2(root1, root2);
                }
                if (!result) {
                    result = HasSubtree(root1.left, root2);
                }
                if (!result) {
                    result = HasSubtree(root1.right, root2);
                }
            }
            return result;
        }

        public boolean DoesTree1HaveTree2(TreeNode root1, TreeNode root2) {
            if (root1 == null && root2 != null) return false;
            if (root2 == null) return true;
            if (root1.val != root2.val) return false;
            return DoesTree1HaveTree2(root1.left, root2.left) && DoesTree1HaveTree2(root1.right, root2.right);
        }
    }



/* 先前序遍历这棵树的每个结点，如果遍历到的结点有子结点，就交换它的两个子节点，
当交换完所有的非叶子结点的左右子结点之后，就得到了树的镜像 */
    /**
     public class TreeNode {
     int val = 0;
     TreeNode left = null;
     TreeNode right = null;

     public TreeNode(int val) {
     this.val = val;

     }

     }
     */
    //1.root遍历左右
//2.左右继续遍历
//3.左右为null停止遍历
//4.左右只要有一个就交换
    public class Solution4 {
        public void Mirror(TreeNode root) {
            if(root == null)
                return;
            if(root.left == null && root.right == null)
                return;

            TreeNode pTemp = root.left;
            root.left = root.right;
            root.right = pTemp;

            if(root.left != null)
                Mirror(root.left);
            if(root.right != null)
                Mirror(root.right);
        }
    }


    public class Solution5 {
        public ArrayList<Integer> printMatrix(int [][] array) {
            ArrayList<Integer> result = new ArrayList<Integer> ();
            if(array.length==0) return result;
            int n = array.length,m = array[0].length;
            if(m==0) return result;
            int layers = (Math.min(n,m)-1)/2+1;//这个是层数
            for(int i=0;i<layers;i++){
                for(int k = i;k<m-i;k++) result.add(array[i][k]);//左至右
                for(int j=i+1;j<n-i;j++) result.add(array[j][m-i-1]);//右上至右下
                for(int k=m-i-2;(k>=i)&&(n-i-1!=i);k--) result.add(array[n-i-1][k]);//右至左
                for(int j=n-i-2;(j>i)&&(m-i-1!=i);j--) result.add(array[j][i]);//左下至左上
            }
            return result;
        }
    }


//    思路：用一个栈data保存数据，用另外一个栈min保存依次入栈最小的数
//    比如，data中依次入栈，5,  4,  3, 8, 10, 11, 12, 1
//    则min依次入栈，5,  4,  3，no,no, no, no, 1
//
//    no代表此次不如栈
//    每次入栈的时候，如果入栈的元素比min中的栈顶元素小或等于则入栈，否则不如栈。

    public class Solution6 {
        Stack<Integer> data = new Stack<Integer>();
        Stack<Integer> min = new Stack<Integer>();
        Integer temp = null;
        public void push(int node) {
            if(temp != null){
                if(node <= temp ){
                    temp = node;
                    min.push(node);
                }
                data.push(node);
            }else{
                temp = node;
                data.push(node);
                min.push(node);
            }
        }

        public void pop() {
            int num = data.pop();
            int num2 = min.pop();
            if(num != num2){
                min.push(num2);
            }
        }

        public int top() {
            int num = data.pop();
            data.push(num);
            return num;
        }

        public int min() {
            int num = min.pop();
            min.push(num);
            return num;
        }
    }


    /**
     * 思路看微信
     */
    public class Solution7 {
        public boolean IsPopOrder(int [] pushA,int [] popA) {
            if(pushA.length == 0 || popA.length == 0)
                return false;
            Stack<Integer> s = new Stack<Integer>();
            //用于标识弹出序列的位置
            int popIndex = 0;
            for(int i = 0; i< pushA.length;i++){
                s.push(pushA[i]);
                //如果栈不为空，且栈顶元素等于弹出序列
                while(!s.empty() &&s.peek() == popA[popIndex]){
                    //出栈
                    s.pop();
                    //弹出序列向后一位
                    popIndex++;
                }
            }
            return s.empty();
        }
    }

            /*
        输入一个正整数数组，把数组里所有数字拼接起来排成一个数
        打印能拼接出的所有数字中最小的一个。
        例如输入数组{3，32，321}，则打印出这三个数字能排成的最小数字为321323。
        3,36,345
         */

    public String PrintMinNumber(int[] numbers) {
        if (numbers == null || numbers.length == 0) return "";
        int len = numbers.length;
        String[] str = new String[len];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            str[i] = String.valueOf(numbers[i]);
        }
        Arrays.sort(str, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                String c1 = s1 + s2;//332
                String c2 = s2 + s1;//323
                return c1.compareTo(c2);
            }
        });
        for (int i = 0; i < len; i++) {
            sb.append(str[i]);
        }
        return sb.toString();
    }

    /*
    把只包含质因子2、3和5的数称作丑数（Ugly Number）。
    例如6、8都是丑数，但14不是，因为它包含质因子7。
    习惯上我们把1当做是第一个丑数。求按从小到大的顺序的第N个丑数。
     */
    public int GetUglyNumber_Solution(int index) {
        //不包含肯定不是
        //包含
        if (index <= 6) {
            return index;
        }
        int i2 = 0;
        int i3 = 0;
        int i5 = 0;
        int[] dp = new int[index];
        dp[0] = 1;
        //构建法,自己也分析了直接跳到指定的数是不可能的
        for (int i = 0; i < index; i++) {
            int next2 = dp[i2] * 2;
            int next3 = dp[i3] * 2;
            int next5 = dp[i5] * 2;
            dp[i] = Math.min(next2, Math.min(next3, next5));
            if (dp[i] == next2) i2++;
            if (dp[i] == next3) i3++;
            if (dp[i] == next5) i5++;
        }
        return dp[index - 1];
    }

    /*
    在一个字符串(0<=字符串长度<=10000，全部由字母组成)中找到第一个只出现一次的字符
    并返回它的位置,
    如果没有则返回 -1（需要区分大小写）.
     */
    public int FirstNotRepeatingChar(String str) {
        if (str == null) {
            return -1;
        }
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        int n = str.length();
        char[] c = str.toCharArray();
        for (int i = 0; i < n; i++) {
            int s = map.size();
            map.put(String.valueOf(c[i]), i);
            if (map.size() == s) {
                map.remove(String.valueOf(c[i]));
            }
        }
        return map.entrySet().iterator().next().getValue();
    }
}
