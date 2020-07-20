package com.bbstone.comm.util;

import java.util.UUID;

/**
 * 
 *
 * @author bbstone
 *
 */
public class TokenUtil {

	public static String UUID32() {
		String str = UUID.randomUUID().toString();
		return str.replaceAll("-", "");
	}

	public static String UUID36() {
		return UUID.randomUUID().toString();
	}

	public static void main(String[] args) {

		System.out.println(UUID32());
	}

}
