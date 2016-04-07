/* RandomUtil.java
 *
 * Copyright (C) 2009 Pieter van Zyl
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 */
package rhp.aof4oop.oo7.benchmarck;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class RandomUtil 
{
	private static Random randomGenerator = null;
	private static SecureRandom random = null;

	/**
	 * 
	 * This method is taken from OZONE's BenchmarkImpl class
	 * 
	 * @param lower
	 * @param upper
	 * @return 19-Apr-2006
	 */
	public static int getRandomInt(int lower, int upper) 
	{
		if (randomGenerator == null) {
			randomGenerator = new Random();
			randomGenerator.setSeed(1L);
		}

		int value;
		do {
			value = randomGenerator.nextInt();
			value %= upper;
			System.out.println("value: " + value + " lower: " + lower
					+ " upper: " + upper);
		} while (value < lower || value >= upper);
		return value;
	}

	/**
	 * 
	 * nextValue >= 1
	 * 
	 * @return 19-Apr-2006public
	 */
	public static int nextPositiveInt() {
		int nextValue = 0;

		while (nextValue < 1) {
			 nextValue = getRandomGenerator().nextInt();
			//nextValue = getSecureRandom().nextInt();
		}
		return nextValue;

	}

	public static int nextPositiveInt2(int range) {
		int nextValue = 0;

		nextValue = getRandomGenerator().nextInt(range);

		// nextValue = Math.abs(nextValue);
		return nextValue;

	}

	/**
	 * 
	 * nextValue >= 0
	 * 
	 * @return 19-Apr-2006
	 */
	public static int nextInt() {
		int nextValue = 0;

		nextValue = Math.abs(getRandomGenerator().nextInt());
		//nextValue = nextPositiveInt();

		return nextValue;

	}

	/**
	 * 
	 * added RandomUtil.getRandomGenerator() becuase Math.random() % value
	 * always returned 1 as the value. ALSO: using the same random number
	 * generator creates a better spread and less repetition that using a new
	 * one/obj each time
	 * 
	 * @return 19-Apr-2006
	 */
	private static Random getRandomGenerator() {
		if (randomGenerator == null) {
			randomGenerator = new Random();
			// SecureRandom
			randomGenerator.setSeed(1L);
		}
		return randomGenerator;
	}

	private static SecureRandom getSecureRandom() {		
		try {
			if (random == null) {
				System.out.println(" get securerandom");
				random = SecureRandom.getInstance("SHA1PRNG");
				byte[] seed = random.generateSeed(20);
				random.setSeed(seed);
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
//		System.out.println(" get securerandom done");
		return random;
	}

	private static void setRandomGenerator(Random randomGenerator) {
		RandomUtil.randomGenerator = randomGenerator;
	}

}
