package com.future94.swallow.common.utils;

import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * @author weilai
 */
public class Md5Utils {

    /**
     * Md 5 string.
     *
     * @param src     the src
     * @param charset the charset
     * @return the string
     */
    @SneakyThrows
    private static String md5(final String src, final String charset) {
        MessageDigest md5;
        StringBuilder hexValue = new StringBuilder(32);
        md5 = MessageDigest.getInstance("MD5");
        byte[] byteArray;
        byteArray = src.getBytes(charset);
        byte[] md5Bytes = md5.digest(byteArray);
        for (byte md5Byte : md5Bytes) {
            int val = ((int) md5Byte) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    /**
     * Md 5 string.
     *
     * @param src the src
     * @return the string
     */
    public static String md5(final String src) {
        return md5(src, StandardCharsets.UTF_8.name());
    }
}
