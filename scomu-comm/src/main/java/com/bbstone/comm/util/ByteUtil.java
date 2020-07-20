package com.bbstone.comm.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.StringUtils;

/**
 * 
 *
 * @author bbstone
 *
 */
public class ByteUtil {

	/**
	 * trim null byte
	 * @param string
	 * @return
	 */
	public static String trimNull(String string) {
		List<Byte> list = new ArrayList<>();
		byte[] bytes = StringUtils.getBytesUtf8(string);
		for (int i = 0; bytes != null && i < bytes.length; i++) {
			if (0 != bytes[i]) {
				list.add(bytes[i]);
			}
		}
		byte[] newbytes = new byte[list.size()];
		for (int i = 0; i < list.size(); i++) {
			newbytes[i] = (Byte) list.get(i);
		}
		String str = StringUtils.newStringUtf8(newbytes);
		return str;
	}

	public static int[] toCppBytes(String str) {
		return toCppBytes(StringUtils.getBytesUtf16Le(str));
	}

	public static int[] toCppBytes(byte[] unsignedByte) {
		int[] cppBytes = new int[unsignedByte.length];
		for (int i = 0; i < unsignedByte.length; i++) {
			cppBytes[i] = unsignedByte[i] & 0xFF;
		}
		return cppBytes;
	}

	public static int[] toIntArray(String str) {
		int[] result = new int[str.length()];
		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			result[i] = Integer.valueOf(chars[i]);
		}
		return result;
	}

	public static byte[] toJavaBytes(int[] cppBytes) {
		byte[] javaBytes = new byte[cppBytes.length];
		for (int i = 0; i < cppBytes.length; i++) {
			javaBytes[i] = (byte) cppBytes[i];
		}
		return javaBytes;
	}

	public static byte[] decodeHexString(String hexString) {
		if (hexString.length() % 2 == 1) {
			throw new IllegalArgumentException("Invalid hexadecimal String supplied.");
		}

		byte[] bytes = new byte[hexString.length() / 2];
		for (int i = 0; i < hexString.length(); i += 2) {
			bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
		}
		return bytes;
	}

	public static byte hexToByte(String hexString) {
		int firstDigit = toDigit(hexString.charAt(0));
		int secondDigit = toDigit(hexString.charAt(1));
		return (byte) ((firstDigit << 4) + secondDigit);
	}

	private static int toDigit(char hexChar) {
		int digit = Character.digit(hexChar, 16);
		if (digit == -1) {
			throw new IllegalArgumentException("Invalid Hexadecimal Character: " + hexChar);
		}
		return digit;
	}

	

}
