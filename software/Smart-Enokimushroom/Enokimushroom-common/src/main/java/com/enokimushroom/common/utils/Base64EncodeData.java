package com.enokimushroom.common.utils;

import java.util.Arrays;

public class Base64EncodeData {
    private static final String base64char = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
    public static byte[] base64Decode(String base64) {
        int i = 0, j = 0;
        int[] trans = new int[4];
        byte[] decodedData = new byte[(base64.length() * 3 / 4)];

        for (; i < base64.length(); i += 4) {
            // 每四个一组，译码成三个字符
            trans[0] = base64char.indexOf(base64.charAt(i));
            trans[1] = base64char.indexOf(base64.charAt(i + 1));

            // 1/3
            decodedData[j++] = (byte) (((trans[0] << 2) & 0xfc) | ((trans[1] >> 4) & 0x03));

            if (i + 2 < base64.length() && base64.charAt(i + 2) != '=') {
                trans[2] = base64char.indexOf(base64.charAt(i + 2));
                // 2/3
                decodedData[j++] = (byte) (((trans[1] << 4) & 0xf0) | ((trans[2] >> 2) & 0x0f));

                if (i + 3 < base64.length() && base64.charAt(i + 3) != '=') {
                    trans[3] = base64char.indexOf(base64.charAt(i + 3));
                    // 3/3
                    decodedData[j++] = (byte) (((trans[2] << 6) & 0xc0) | (trans[3] & 0x3f));
                }
            }
        }

        return Arrays.copyOf(decodedData, j);
    }
}
