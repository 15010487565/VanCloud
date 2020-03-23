package com.vgtech.common.utils;


import org.apache.commons.lang.StringUtils;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;


/**
 * 3DES加密工具类
 */
public class Des3Util
{
	// 密钥 长度不得小于24
	private String secretKey = "123456789012345678901234"; //不足24或许小于24位 以 0 补位
	// 向量 可有可无 终端后台也要约定
	private final static String iv = "01234567";
	// 加解密统一使用的编码方式
	private final static String encoding = "utf-8";

	/**
	* 3DES加密
	*
	* @param plainText
	*            普通文本
	* @return
	* @throws Exception
	*/
	public String encode(String plainText) throws Exception
	{
		Key deskey = null;
		DESedeKeySpec spec = new DESedeKeySpec(secretKey.getBytes());
		SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
		deskey = keyfactory.generateSecret(spec);
		Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
		IvParameterSpec ips = new IvParameterSpec(iv.getBytes());
		cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);
		byte[] encryptData = cipher.doFinal(plainText.getBytes(encoding));
		return Base64.encode(encryptData);
	}

	/**
	* 3DES解密
	*
	* @param encryptText
	*            加密文本
	* @return
	* @throws Exception
	*/
	public String decode(String encryptText) throws Exception
	{
		Key deskey = null;
		DESedeKeySpec spec = new DESedeKeySpec(secretKey.getBytes());
		SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
		deskey = keyfactory.generateSecret(spec);
		Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
		IvParameterSpec ips = new IvParameterSpec(iv.getBytes());
		cipher.init(Cipher.DECRYPT_MODE, deskey, ips);
		byte[] decryptData = cipher.doFinal(Base64.decode(encryptText));
		return new String(decryptData, encoding);
	}

	public String getSecretKey()
	{
		return secretKey;
	}

	public void setSecretKey(String secretKey)
	{

		if (StringUtils.length(secretKey) > 24)
		{
			this.secretKey = StringUtils.substring(secretKey,0,24);
		} else
		{
			this.secretKey = StringUtils.rightPad(secretKey, 24, "0");
		}
	}
	public static void main(String args[]) throws Exception
	{
		Des3Util Des3Util = new Des3Util();
		Des3Util.setSecretKey("123456789012345678901234");
		String str = "你好";
		System.out.println("----加密前-----：" + str + " secretkey  = " + Des3Util.getSecretKey());
		String encodeStr = Des3Util.encode(str);
		System.out.println("----加密后-----：" + encodeStr);
		System.out.println("----解密后-----：" + Des3Util.decode(encodeStr));
	}
}