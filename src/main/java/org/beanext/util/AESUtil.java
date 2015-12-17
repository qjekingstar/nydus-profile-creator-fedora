package org.beanext.util;

import org.apache.commons.io.Charsets;
import org.beanext.commons.lang.AES;


public class AESUtil {

	public static String encrypt(String content, String password) {
		return encodeHex((new AES(password, 128, "beanext").encrypt(content.getBytes(Charsets.UTF_8))));
	}

	public static String decrypt(String content, String password) {
		try {
			return new String(new AES(password, 128, "beanext").decrypt(decodeHex(content.toCharArray())));			
		} catch (Exception e) {
			return new String(new AES("beanext", 128, "beanext").decrypt(decodeHex(content.toCharArray())));
		}
	}

	private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	
	private static String encodeHex(final byte[] data) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS_UPPER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_UPPER[0x0F & data[i]];
        }
        return new String(out);
    }
	
	private static byte[] decodeHex(final char[] data)  {

        final int len = data.length;

        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }

        final byte[] out = new byte[len >> 1];
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }
	
	private static int toDigit(final char ch, final int index) {
        final int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }
}