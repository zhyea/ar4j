package org.chobit.ar4j.core.tools;

public abstract class Strings {


    /**
     * 判断字符串是否为空
     */
    public static boolean isBlank(final String s) {
        return s == null || s.trim().isEmpty();
    }


    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotBlank(final String s) {
        return !isBlank(s);
    }


    /**
     * 将数值字符串转为整型
     */
    public static int toInt(final String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }


    private Strings() {
        throw new UnsupportedOperationException("Private constructor, cannot be accessed.");
    }
}
