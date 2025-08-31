// StringMaker.java
package com.qi.qismorecommands.command;

import java.util.Random;

public class StringMaker {
    private static final Random RANDOM = new Random();

    // 合并字符串
    public static String joinStrings(String str1, String str2) {
        return str1 + str2;
    }

    // 转换为大写
    public static String toUpperCase(String str) {
        return str.toUpperCase();
    }

    // 转换为小写
    public static String toLowerCase(String str) {
        return str.toLowerCase();
    }

    // 生成随机字母字符串
    public static String randomLetters(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = (char) (RANDOM.nextBoolean() ?
                    RANDOM.nextInt(26) + 'a' : RANDOM.nextInt(26) + 'A');
            sb.append(c);
        }
        return sb.toString();
    }

    // 生成随机数字字符串
    public static String randomNumbers(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    // 生成随机混合字符串
    public static String randomMixed(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int type = RANDOM.nextInt(3);
            if (type == 0) {
                sb.append((char) (RANDOM.nextInt(26) + 'a'));
            } else if (type == 1) {
                sb.append((char) (RANDOM.nextInt(26) + 'A'));
            } else {
                sb.append(RANDOM.nextInt(10));
            }
        }
        return sb.toString();
    }

    // 生成自定义字符集随机字符串
    public static String randomChars(String charset, int length) {
        if (charset == null || charset.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(charset.length());
            sb.append(charset.charAt(index));
        }
        return sb.toString();
    }

    // 阿拉伯数字转简单中文数字 (12345 → 一二三四五)
    public static String numberToChineseSimple(String numberStr) {
        if (numberStr == null || numberStr.isEmpty()) {
            return "";
        }
        char[] digits = numberStr.toCharArray();
        StringBuilder result = new StringBuilder();
        for (char digit : digits) {
            if (Character.isDigit(digit)) {
                result.append("零一二三四五六七八九".charAt(digit - '0'));
            } else {
                result.append(digit); // 保留非数字字符
            }
        }
        return result.toString();
    }

    // 阿拉伯数字转大写中文数字 (12345 → 壹贰叁肆伍)
    public static String numberToChineseCapital(String numberStr) {
        if (numberStr == null || numberStr.isEmpty()) {
            return "";
        }
        char[] digits = numberStr.toCharArray();
        StringBuilder result = new StringBuilder();
        for (char digit : digits) {
            if (Character.isDigit(digit)) {
                result.append("零壹贰叁肆伍陆柒捌玖".charAt(digit - '0'));
            } else {
                result.append(digit); // 保留非数字字符
            }
        }
        return result.toString();
    }

    // 阿拉伯数字转标准中文数字 (12345 → 一万二千三百四十五)
    public static String numberToChineseNormal(String numberStr) {
        try {
            long number = Long.parseLong(numberStr);
            return convertWithUnits(number, false);
        } catch (NumberFormatException e) {
            return "无效数字";
        }
    }

    // 阿拉伯数字转财务中文数字 (12345 → 壹万贰仟叁佰肆拾伍)
    public static String numberToChineseFinancial(String numberStr) {
        try {
            long number = Long.parseLong(numberStr);
            return convertWithUnits(number, true);
        } catch (NumberFormatException e) {
            return "无效数字";
        }
    }

    // 带单位的数字转换核心方法
    private static String convertWithUnits(long number, boolean financial) {
        if (number == 0) {
            return "零";
        }

        // 定义单位和数字
        String[] units = financial ?
                new String[]{"", "拾", "佰", "仟", "万", "亿", "兆", "京"} :
                new String[]{"", "十", "百", "千", "万", "亿", "兆", "京"};

        String[] digits = financial ?
                new String[]{"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"} :
                new String[]{"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};

        // 处理负数
        if (number < 0) {
            return ("负") + convertWithUnits(-number, financial);
        }

        StringBuilder result = new StringBuilder();
        long[] unitValues = {1, 10, 100, 1000, 10000, 100000000L, 1000000000000L, 10000000000000000L};

        // 从最大单位开始处理
        for (int i = unitValues.length - 1; i >= 0; i--) {
            if (number >= unitValues[i]) {
                long quotient = number / unitValues[i];
                if (quotient > 0) {
                    if (quotient < 10) {
                        // 直接添加数字和单位
                        result.append(digits[(int) quotient]).append(units[i]);
                    } else {
                        // 递归处理更大的数字
                        result.append(convertWithUnits(quotient, financial)).append(units[i]);
                    }
                }
                number %= unitValues[i];

                // 添加零（如果需要）
                if (number > 0 && number < unitValues[i] / 10) {
                    result.append(digits[0]);
                }
            }
        }

        // 处理十位数的一十 → 十
        String resultStr = result.toString();
        if (resultStr.startsWith("一十")) {
            resultStr = resultStr.substring(1);
        } else if (resultStr.startsWith("壹拾")) {
            resultStr = resultStr.substring(1);
        }

        return resultStr;
    }
}