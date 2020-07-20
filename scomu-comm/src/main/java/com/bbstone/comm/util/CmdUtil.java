package com.bbstone.comm.util;

import java.util.Arrays;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import com.alibaba.fastjson.JSON;
import com.bbstone.comm.CmdConst;
import com.bbstone.comm.dto.req.EmptyReq;
import com.bbstone.comm.dto.rsp.EmptyRsp;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 *
 * @author bbstone
 *
 */
@Slf4j
public class CmdUtil {

	private static String calcRandAnswer(String random, String password) {
		byte[] passwdJavaBytes = DigestUtils.md5(StringUtils.getBytesUtf16Le(password));
		log.debug("passwdJavaBytes: {}", Arrays.toString(passwdJavaBytes));

		int[] passwdCppBytes = ByteUtil.toCppBytes(passwdJavaBytes);
		log.debug("passwdCppBytes: {}", Arrays.toString(passwdCppBytes));

		int[] webApiCppBytes = ByteUtil.toIntArray(CmdConst.WEB_API_WORD);
		log.debug("webApiCppBytes: {}", Arrays.toString(webApiCppBytes));

		int len = passwdCppBytes.length + webApiCppBytes.length;// + srvRandCppBytes.length;
		int[] mx = new int[len];
		System.arraycopy(passwdCppBytes, 0, mx, 0, passwdCppBytes.length);
		System.arraycopy(webApiCppBytes, 0, mx, passwdCppBytes.length, webApiCppBytes.length);
		// [236, 46, 146, 255, 112, 191, 70, 145, 174, 12, 104, 224, 47, 220, 108, 34,
		// 87, 101, 98, 65, 80, 73]
		log.debug("mx: {}", Arrays.toString(mx));

		byte[] mxBytes = ByteUtil.toJavaBytes(mx);
		log.debug("mxBytes: {}", Arrays.toString(mxBytes));

		String mxHexString = DigestUtils.md5Hex(mxBytes);
		// 904ba8ecb16273d2f0ae9c3b8a023752
		log.debug("mxHexString: {}", mxHexString);

		byte[] mxJavaBytes = ByteUtil.decodeHexString(mxHexString);
		int[] mxCppBytes = ByteUtil.toCppBytes(mxJavaBytes);
		// [144, 75, 168, 236, 177, 98, 115, 210, 240, 174, 156, 59, 138, 2, 55, 82]
		log.debug("mxCppBytes: {}", Arrays.toString(mxCppBytes));

		byte[] srvRandJavaBytes = ByteUtil.decodeHexString(random);
		log.debug("srvRandJavaBytes: {}", Arrays.toString(srvRandJavaBytes));
		int[] srvRandCppBytes = ByteUtil.toCppBytes(srvRandJavaBytes);
		// [115, 0, 125, 199, 24, 71, 71, 206, 15, 124, 152, 81, 110, 241, 200, 81]
		log.debug("srvRandCppBytes: {}", Arrays.toString(srvRandCppBytes));

		int len2 = mxCppBytes.length + srvRandCppBytes.length;
		int[] mx2 = new int[len2];
		System.arraycopy(mxCppBytes, 0, mx2, 0, mxCppBytes.length);
		System.arraycopy(srvRandCppBytes, 0, mx2, mxCppBytes.length, srvRandCppBytes.length);
		// [144, 75, 168, 236, 177, 98, 115, 210, 240, 174, 156, 59, 138, 2, 55, 82,
		// 115, 0, 125, 199, 24, 71, 71, 206, 15, 124, 152, 81, 110, 241, 200, 81]
		log.debug("mx2: {}", Arrays.toString(mx2));

		byte[] mx2Bytes = ByteUtil.toJavaBytes(mx2);
		log.debug("mx2Bytes: {}", Arrays.toString(mx2Bytes));
		String mx2HexString = DigestUtils.md5Hex(mx2Bytes);
		// 77fe51827f7fa69dd80fbec9aa33f1bb
		log.debug("mx2HexString: {}", mx2HexString);

		return mx2HexString;
	}

	/**
	 * 
	 * @param srvRand
	 * @param password
	 * @return
	 */
	public static String calcSrvRandAnswer(String srvRand, String password) {
		return calcRandAnswer(srvRand, password);

	}
	
	public static String calcCliRandAnswer(String cliRand, String password) {
		return calcRandAnswer(cliRand, password);
	}
	
	
	public static String getEmptyReqData() {
		return JSON.toJSONString(new EmptyReq());
	}
	
	public static String getEmptyRspData() {
		return JSON.toJSONString(new EmptyRsp());
	}

}
