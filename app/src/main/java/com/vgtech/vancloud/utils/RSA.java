
package com.vgtech.vancloud.utils;

import android.util.Log;

import com.vgtech.common.utils.Base64;

import org.apache.http.impl.auth.UnsupportedDigestAlgorithmException;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class RSA {
	private static final String LOG_TAG = "RSA";
	private static final String ALGORITHM = "RSA";
	private static final String CIPHER_ALGORITHM = "RSA/2/PKCS1PADDING";
	private static Cipher sCipher;
	private static KeyFactory sKeyFactory;
	//android.security.Credentials;
	//android.security.KeyStore;
	static {
		try {
			sCipher = Cipher.getInstance(CIPHER_ALGORITHM);
			sKeyFactory = KeyFactory.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			Log.e(LOG_TAG, "Get DES Digest failed.");
			throw new UnsupportedDigestAlgorithmException(ALGORITHM, e);
		} catch (NoSuchPaddingException e) {
			Log.e(LOG_TAG, "Get DES Digest failed.");
			throw new IllegalArgumentException(ALGORITHM, e);
		}
	}

	private RSA() {
	}

	public static String x509Encode(String publickey, String source) {
		String result = null;
		try {
			byte[] pubKey = Base64.decode(adjustKey(publickey));
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubKey);
			PublicKey key = sKeyFactory.generatePublic(keySpec);

			sCipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] encoded = sCipher.doFinal(source.getBytes());
			result = new String(Base64.encode(encoded));
		} catch (Exception e) {
			Log.e(LOG_TAG, "Meet error on X.509 encode by RSA.", e);
		}
		return result;
	}

	private static String adjustKey(String key) {
		if (key.startsWith("-")) {
			key = key.substring(key.indexOf('\n'));
		}
		if (key.endsWith("-")) {
			key = key.substring(0, key.lastIndexOf('\n'));
		}
		key = key.replaceAll("\n", "");
		return key;
	}

	public static String getKey32() {
		SecureRandom sr = new SecureRandom(Long.toHexString(System.currentTimeMillis()).getBytes());
		byte[] temp = new byte[24];
		sr.nextBytes(temp);
		return new String(Base64.encode(temp));
	}
}
