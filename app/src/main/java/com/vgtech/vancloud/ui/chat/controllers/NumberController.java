package com.vgtech.vancloud.ui.chat.controllers;

/**
 * @author xuanqiang
 * @date 14-3-14
 */
public class NumberController {
    static String[] NUMBERS = new String[]{"零", "一", "二", "三", "四", "五", "六", "七",
            "八", "九"};

    public static String arabToChinese(int n) {
        String str = null;
        int size = (n + "").length();
        if (size == 1) {
            str = NUMBERS[n];
        } else if (size == 2) {
            str = (n / 10 < 2 ? "" : NUMBERS[n / 10]) + "十";
            if (n % 10 != 0) {
                str += NUMBERS[n % 10];
            }
        } else if (size == 3) {
            str = NUMBERS[n / 100] + "百";
            if (n % 100 != 0) {
                int n1 = n % 100;
                str += NUMBERS[n1 / 10] + "十";
                if (n % 10 != 0) {
                    str += NUMBERS[n1 % 10];
                }
            }
        }
        return str;
    }

}



