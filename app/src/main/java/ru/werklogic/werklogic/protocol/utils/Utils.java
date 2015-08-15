package ru.werklogic.werklogic.protocol.utils;

import java.io.UnsupportedEncodingException;

public class Utils {

    private static final String ENCODING = "ISO-8859-1";
    private static final byte CR = '\r';
    private static final String[] HEX = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    public static byte[] string2bytes(String string) {
        try {
            return string.getBytes(ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new byte[]{};
    }

    public static boolean findCR(byte[] bytes) {
        for (byte b : bytes) {
            if (b == CR)
                return true;
        }
        return false;
    }

    public static String bytes2string(byte[] bytes) {
        try {
            return new String(bytes, ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String byte2hex(int b) {
        return "" + HEX[(b >> 4) & 0xF] + HEX[b & 0xF];
    }
}
