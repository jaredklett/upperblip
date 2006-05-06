/*
 * RandomGUID
 * @version 1.2.1 11/05/02
 * @author Marc A. Mnich
 *
 * From www.JavaExchange.com, Open Software licensing
 *
 * 11/05/02 -- Performance enhancement from Mike Dubman.
 *			   Moved InetAddr.getLocal to static block.	 Mike has measured
 *			   a 10 fold improvement in run time.
 * 01/29/02 -- Bug fix: Improper seeding of nonsecure Random object
 *			   caused duplicate GUIDs to be produced.  Random object
 *			   is now only created once per JVM.
 * 01/19/02 -- Modified random seeding and added new constructor
 *			   to allow secure random feature.
 * 01/14/02 -- Added random function seeding with JVM run time
 *
 */

package com.blipnetworks.util;

import java.net.*;
import java.util.*;
import java.security.*;

/**
 * I found this class here:
 * <a href="http://www.javaexchange.com/aboutRandomGUID.html">Java Exchange</a>
 * I added it to the Blip utility package and made some minor corrections.
 * Otherwise this class is unchanged from the original.
 *
 * @version $Id: RandomGUID.java,v 1.2 2006/05/06 23:56:46 jklett Exp $
 */

public class RandomGUID {

	public String valueBeforeMD5 = "";
	public String valueAfterMD5 = "";
	private static Random myRand;
	private static SecureRandom mySecureRand;

	private static String s_id;

	/*
	 * Static block to take care of one time secureRandom seed.
	 * It takes a few seconds to initialize SecureRandom.  You might
	 * want to consider removing this static block or replacing
	 * it with a "time since first loaded" seed to reduce this time.
	 * This block will run only once per JVM instance.
	 */

	static {
		mySecureRand = new SecureRandom();
		long secureInitializer = mySecureRand.nextLong();
		myRand = new Random(secureInitializer);
		try {
			s_id = InetAddress.getLocalHost().toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}


	/*
	 * Default constructor.	 With no specification of security option,
	 * this constructor defaults to lower security, high performance.
	 */
	public RandomGUID() {
		getRandomGUID(false);
	}

	/*
	 * Constructor with security option.  Setting secure true
	 * enables each random number generated to be cryptographically
	 * strong.	Secure false defaults to the standard Random function seeded
	 * with a single cryptographically strong random number.
	 */
	public RandomGUID(boolean secure) {
		getRandomGUID(secure);
	}

	/*
	 * Method to generate the random GUID
	 */
	private void getRandomGUID(boolean secure) {
		MessageDigest md5;
		StringBuffer sbValueBeforeMD5 = new StringBuffer();

		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error: " + e);
            // TODO: better solution?
            return;
        }

		try {
			long time = System.currentTimeMillis();
			long rand;

			if (secure) {
				rand = mySecureRand.nextLong();
			} else {
				rand = myRand.nextLong();
			}

			// This StringBuffer can be a long as you need; the MD5
			// hash will always return 128 bits.  You can change
			// the seed to include anything you want here.
			// You could even stream a file through the MD5 making
			// the odds of guessing it at least as great as that
			// of guessing the contents of the file!
			sbValueBeforeMD5.append(s_id);
			sbValueBeforeMD5.append(":");
			sbValueBeforeMD5.append(Long.toString(time));
			sbValueBeforeMD5.append(":");
			sbValueBeforeMD5.append(Long.toString(rand));

			valueBeforeMD5 = sbValueBeforeMD5.toString();
			md5.update(valueBeforeMD5.getBytes());

			byte[] array = md5.digest();
			StringBuffer sb = new StringBuffer();
            for (byte anArray : array) {
                int b = anArray & 0xFF;
                if (b < 0x10) sb.append('0');
                sb.append(Integer.toHexString(b));
            }

			valueAfterMD5 = sb.toString();

		} catch (Exception e) {
			System.out.println("Error:" + e);
		}
	}


	/*
	 * Convert to the standard format for GUID
	 * (Useful for SQL Server UniqueIdentifiers, etc.)
	 * Example: C2FEEEAC-CFCD-11D1-8B05-00600806D9B6
	 */
	public String toString() {
		String raw = valueAfterMD5.toUpperCase();
		StringBuffer sb = new StringBuffer();
		sb.append(raw.substring(0, 8));
		sb.append("-");
		sb.append(raw.substring(8, 12));
		sb.append("-");
		sb.append(raw.substring(12, 16));
		sb.append("-");
		sb.append(raw.substring(16, 20));
		sb.append("-");
		sb.append(raw.substring(20));

		return sb.toString();
	}

	/*
	 * Demonstraton and self test of class
	 */
	public static void main(String args[]) {
		for (int i=0; i< 100; i++) {
		RandomGUID myGUID = new RandomGUID();
		System.out.println("Seeding String=" + myGUID.valueBeforeMD5);
		System.out.println("rawGUID=" + myGUID.valueAfterMD5);
		System.out.println("RandomGUID=" + myGUID.toString());
		}
	}
}
