package com.android.touchanimatortest.test;

/**
 * @description:
 * @author: Xeldow
 * @date: 2019/8/1
 */
public class Main {
    public static void main(String[] args) {
        System.out.print(1%1000000007);
    }

    public static String LeftRotateString(String str, int n) {
        if (str.length() <= 1) return str;
        //asd 4 sda das asd sda 4-3=1 考虑到了n大于字符串长度 的问题
        int l = str.length();
        int k = n % l;//8%3=2
        String s1 = "";
        String s2 = "";
        if (n - l > 0 && k > 0) {
            s1 = str.substring(0, k);
            s2 = str.substring(k, l);
        } else {
            s1 = str.substring(0, n);
            s2 = str.substring(n, l);
        }

        return s2 + s1;
    }
}

