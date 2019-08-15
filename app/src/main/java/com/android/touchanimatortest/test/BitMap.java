package com.android.touchanimatortest.test;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * @description:
 * @author: Xeldow
 * @date: 2019/8/2
 */
public class BitMap {
    /**
     * 小Q正在给一条长度为n的道路设计路灯安置方案。
     * <p>
     * 为了让问题更简单,小Q把道路视为n个方格,需要照亮的地方用'.'表示, 不需要照亮的障碍物格子用'X'表示。
     * <p>
     * 小Q现在要在道路上设置一些路灯, 对于安置在pos位置的路灯, 这盏路灯可以照亮pos - 1, pos, pos + 1这三个位置。
     * <p>
     * 小Q希望能安置尽量少的路灯照亮所有'.'区域, 希望你能帮他计算一下最少需要多少盏路灯。
     */

    public class Main {

        public void main(String[] args) {
            // TODO 自动生成的方法存根
            Scanner input = new Scanner(System.in);
            int count = input.nextInt();
            for (int i = 0; i < count; i++) {
                int number = 0;
                int roadLength = input.nextInt();
                char[] road = input.nextLine().toCharArray();
                String string = input.nextLine();
                for (int j = 0; j < string.length(); j++) {
                    // System.out.println(string.charAt(j));
                    if (string.charAt(j) == '.') {
                        j += 2;
                        number++;
                    }
                }
                System.out.println(number);
            }
        }

    }

    /**
     * 牛牛总是睡过头，所以他定了很多闹钟，只有在闹钟响的时候他才会醒过来并且决定起不起床。从他起床算起他需要X分钟到达教室，上课时间为当天的A时B分，请问他最晚可以什么时间起床
     */
    public class Main2 {

        public void main(String[] args) {
            Scanner in = new Scanner(System.in);
            int count = in.nextInt();
            ArrayList<Integer> upTime = new ArrayList<Integer>();
            while (count-- != 0) {
                int h = in.nextInt();
                int m = in.nextInt();
                int min = h * 60 + m;
                upTime.add(min);
            }
            int goTime = in.nextInt();
            int h = in.nextInt();
            int m = in.nextInt();
            int classTime = h * 60 + m;
            int max = 0;
            int index = 0;
            for (int i = 0; i < upTime.size(); i++) {
                int thisTime = upTime.get(i) + goTime;
                if (thisTime <= classTime && i != 0 && thisTime > max) {
                    max = thisTime;
                    index = i;
                } else if (upTime.size() == 1) {
                    m = upTime.get(i) % 60;
                    h = upTime.get(i) / 60;
                    System.out.println(h + " " + m);
                }
            }
            m = upTime.get(index) % 60;
            h = upTime.get(index) / 60;
            System.out.println(h + " " + m);
        }

    }


}

