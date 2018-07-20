package cdrfile.global;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2004</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

// import java.security.*;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class Crypt {
	private javax.crypto.spec.SecretKeySpec keySpec;
	private byte[] key;
	private String algorithm;

	/** Creates a new instance of Crypt */
	public Crypt(byte[] key, String algorithm) {
		this.key = key;
		this.algorithm = algorithm;
		this.keySpec = new javax.crypto.spec.SecretKeySpec(this.key,
				this.algorithm);
	}

	/** Encrypts the give String to an array of bytes */
	public byte[] encryptString(String text) {
		try {
			javax.crypto.Cipher cipher = javax.crypto.Cipher
					.getInstance(this.algorithm);
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, this.keySpec);
			return cipher.doFinal(text.getBytes());
		} catch (Exception e) {
			return null;
		}
	}

	/** Decrypts the given array of bytes to a String */
	public String decryptString(byte[] b) {
		try {
			javax.crypto.Cipher cipher = javax.crypto.Cipher
					.getInstance(this.algorithm);
			cipher.init(javax.crypto.Cipher.DECRYPT_MODE, this.keySpec);
			return new String(cipher.doFinal(b));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Encrypts the given String to a hex representation of the array of bytes
	 */
	public String encryptHexString(String text) {
		return toHex(encryptString(text));
	}

	/**
	 * Decrypts the given hex representation of the array of bytes to a String
	 */
	public String decryptHexString(String text) {
		return decryptString(toByteArray(text));
	}

	/** Converts the given array of bytes to a hex String */
	private String toHex(byte[] buf) {
		String res = "";
		for (int i = 0; i < buf.length; i++) {
			int b = buf[i];
			if (b < 0) {
				res = res.concat("-");
				b = -b;
			}
			if (b < 16) {
				res = res.concat("0");
			}
			res = res.concat(Integer.toHexString(b).toUpperCase());
		}
		return res;
	}

	/** Converts the given hex String to an array of bytes */
	private byte[] toByteArray(String hex) {
		java.util.Vector res = new java.util.Vector();
		String part;
		int pos = 0;
		int len = 0;
		while (pos < hex.length()) {
			len = ((hex.substring(pos, pos + 1).equals("-")) ? 3 : 2);
			part = hex.substring(pos, pos + len);
			pos += len;
			int test = Integer.parseInt(part, 16);
			res.add(new Byte((byte) test));
		}
		if (res.size() > 0) {
			byte[] b = new byte[res.size()];
			for (int i = 0; i < res.size(); i++) {
				Byte a = (Byte) res.elementAt(i);
				b[i] = a.byteValue();
			}
			return b;
		} else {
			return null;
		}
	}

	public static String getPassword(Connection pConn, String pUserID)
			throws Exception {
		final byte[] key = { (byte) 0x01, (byte) 0xE3, (byte) 0xA2,
				(byte) 0x19, (byte) 0x59, (byte) 0xBD, (byte) 0xEE, (byte) 0xAB };
		Crypt crypt = new Crypt(key, "DES");
		String mSQL = "begin "
				+ "SELECT passwd INTO ? FROM user_db WHERE user_id='" + pUserID
				+ "';" + "end;";
		// String decrypted = crypt.decryptHexString(encrypted);
		CallableStatement cs = pConn.prepareCall(mSQL);
		try {
			cs.registerOutParameter(1, Types.VARCHAR);
			cs.execute();
			mSQL = crypt.decryptHexString(cs.getString(1));
			// Reconnect real user cdrfile db
			Global.strPassword = mSQL;
			Global.strUserName = "cdrfile";
			return mSQL;
		} catch (SQLException ex) {
			throw ex;
		} finally {
			try {
				cs.close();
				cs = null;
			} catch (Exception e) {
			}
		}
	}

	public static void setPasswordDB(Connection pConn, String pUserID,
			String pPassword) throws Exception {
		final byte[] key = { (byte) 0x01, (byte) 0xE3, (byte) 0xA2,
				(byte) 0x19, (byte) 0x59, (byte) 0xBD, (byte) 0xEE, (byte) 0xAB };
		Statement stmt = pConn.createStatement();
		Crypt crypt = new Crypt(key, "DES");
		String mSQL = "UPDATE user_db SET passwd='"
				+ crypt.encryptHexString(pPassword) + "'";
		// String decrypted = crypt.decryptHexString(encrypted);

		try {
			stmt.execute(mSQL);
		} catch (SQLException e) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module Crypt.getPassword " + e.toString());
		} finally {
			try {
				stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	public static void main(String[] args) throws Exception {
		final byte[] key = { (byte) 0x01, (byte) 0xE3, (byte) 0xA2,
				(byte) 0x19, (byte) 0x59, (byte) 0xBD, (byte) 0xEE, (byte) 0xAB };
		Crypt crypt = new Crypt(key, "DES");
		String pPassword = "cdrfileowner226";
		System.out.println("Unen:" + crypt.decryptHexString("-1651445F2C14-60-0F-5B-2470-3625-1B2C-2A"));
		String encrypted = crypt.encryptHexString(pPassword);
		System.out.println(pPassword);
		System.out.println("En:" + encrypted);
		System.out.println("Unen:" + crypt.decryptHexString(encrypted));
	}
}
