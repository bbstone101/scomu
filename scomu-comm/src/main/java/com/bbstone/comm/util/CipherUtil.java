package com.bbstone.comm.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 *
 * @author bbstone
 *
 */
public class CipherUtil {

	public static String md5(String str) {
		if (StringUtils.isBlank(str))
			return null;
		return DigestUtils.md5Hex(str.getBytes());
	}


}
