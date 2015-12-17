package org.beanext.commons.lang;

import java.security.Key;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

	private static final IvParameterSpec DEFAULT_IV = new IvParameterSpec(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

	private static final String ALGORITHM = "AES";

	private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

	private Key key;

	private IvParameterSpec iv;

	private Cipher cipher;

	public AES(final String key) {
		this(key, 128);
	}

	public AES(final String key, final int bit) {
		this(key, bit, null);
	}

	public AES(final String key, final int bit, final String iv) {
		if (bit == 256) {
			this.key = new SecretKeySpec(getHash("SHA-256", key), ALGORITHM);
		} else {
			this.key = new SecretKeySpec(getHash("MD5", key), ALGORITHM);
		}
		if (iv != null) {
			this.iv = new IvParameterSpec(getHash("MD5", iv));
		} else {
			this.iv = DEFAULT_IV;
		}

		init();
	}

	private static byte[] getHash(final String algorithm, final String text) {
		try {
			return getHash(algorithm, text.getBytes("UTF-8"));
		} catch (final Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}

	private static byte[] getHash(final String algorithm, final byte[] data) {
		try {
			final MessageDigest digest = MessageDigest.getInstance(algorithm);
			digest.update(data);
			return digest.digest();
		} catch (final Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}

	private void init() {
		try {
			cipher = Cipher.getInstance(TRANSFORMATION);
		} catch (final Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}

	public byte[] encrypt(final byte[] data) {
		try {
			return doCrypt(data, Cipher.ENCRYPT_MODE);
		} catch (final Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}
	
	public byte[] doCrypt(final byte[] data, int mode) {
		try {
			cipher.init(mode, key, iv);
			return cipher.doFinal(data);
		} catch (final Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}

	public byte[] decrypt(final byte[] data) {
		try {
			return doCrypt(data, Cipher.DECRYPT_MODE);
		} catch (final Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}
}