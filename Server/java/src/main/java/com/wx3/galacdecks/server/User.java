/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Kevin Lin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
/**
 * 
 */
package com.wx3.galacdecks.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * @author Kevin
 *
 */
@Entity
public class User {
	
	public static final int HASH_ITERATIONS = 1024;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)	
	private long id;
	private String username;
	private String passwordHash;
	private String salt;
	private Date createDate;
	
	/**
	 * Create a user with no password. This user should not be able to login again,
	 * but can register the account in the same session.
	 * 
	 * @param username
	 * @return
	 */
	public static User CreateUser(String username) {
		User user = new User();
		user.username = username;
		user.createDate = new Date();
		return user;
	}
	
	/**
	 * Create a user with the supplied username and password.
	 * @param username
	 * @param password
	 * @return
	 */
	public static User CreateUser(String username, String password) {
		User user = new User();
		user.username = username; 
		user.passwordHash = password;
		user.createDate = new Date();
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			byte[] bSalt = new byte[8];
            random.nextBytes(bSalt);
            // Digest computation
            byte[] bDigest = getHash(HASH_ITERATIONS,password,bSalt);
            user.passwordHash = byteToBase64(bDigest);
            user.salt = byteToBase64(bSalt);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unable to access hashing algorithm: " + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unsupported encoding: " + e.getMessage());
		}
		return user;
	}
	
	/**
    * From a password, a number of iterations and a salt,
    * returns the corresponding digest
    * @param iterationNb int The number of iterations of the algorithm
    * @param password String The password to encrypt
    * @param salt byte[] The salt
    * @return byte[] The digested password
    * @throws NoSuchAlgorithmException If the algorithm doesn't exist
	 * @throws UnsupportedEncodingException 
    */
   public static byte[] getHash(int iterationNb, String password, byte[] salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
       MessageDigest digest = MessageDigest.getInstance("SHA-1");
       digest.reset();
       digest.update(salt);
       byte[] input = digest.digest(password.getBytes("UTF-8"));
       for (int i = 0; i < iterationNb; i++) {
           digest.reset();
           input = digest.digest(input);
       }
       return input;
   }
	
   /**
    * From a base 64 representation, returns the corresponding byte[] 
    * @param data String The base64 representation
    * @return byte[]
    * @throws IOException
    */
   public static byte[] base64ToByte(String data) throws IOException {
       BASE64Decoder decoder = new BASE64Decoder();
       return decoder.decodeBuffer(data);
   }
   
   /**
    * From a byte[] returns a base 64 representation
    * @param data byte[]
    * @return String
    * @throws IOException
    */
   public static String byteToBase64(byte[] data){
       BASE64Encoder endecoder = new BASE64Encoder();
       return endecoder.encode(data);
   }
   
   public long getUserId() {
	   return id;
   }
	
	public String getUsername() {
		return username;
	}
	
	public String getPasswordHash() {
		return passwordHash;
	}
	
	public String getSalt() {
		return salt;
	}
	
}
