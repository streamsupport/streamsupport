/*
 * Written by Stefan Zobel and released to the
 * public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util;

import java.io.Serializable;

import java8.util.stream.DoubleStream;
import java8.util.stream.IntStream;
import java8.util.stream.LongStream;

/**
 * A compilation stub only. Must NOT be included in the binary distribution!
 */
@SuppressWarnings("serial")
public class Random implements Serializable {

	/**
	 * Throws {@link AssertionError} always.
	 * 
	 * @param streamSize
	 *            ignored
	 * @return never
	 * @throws AssertionError always
	 */
	public IntStream ints(long streamSize) {
		throw new AssertionError();
	}

	/**
	 * Throws {@link AssertionError} always.
	 * 
	 * @return never
	 * @throws AssertionError always
	 */
	public IntStream ints() {
		throw new AssertionError();
	}

	/**
	 * Throws {@link AssertionError} always.
	 * 
	 * @param streamSize
	 *            ignored
	 * @param randomNumberOrigin
	 *            ignored
	 * @param randomNumberBound
	 *            ignored
	 * @return never
	 * @throws AssertionError always
	 */
	public IntStream ints(long streamSize, int randomNumberOrigin,
			int randomNumberBound) {
		throw new AssertionError();
	}

	/**
	 * Throws {@link AssertionError} always.
	 * 
	 * @param randomNumberOrigin
	 *            ignored
	 * @param randomNumberBound
	 *            ignored
	 * @return never
	 * @throws AssertionError always
	 */
	public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
		throw new AssertionError();
	}

	/**
	 * Throws {@link AssertionError} always.
	 * 
	 * @param streamSize
	 *            ignored
	 * @return never
	 * @throws AssertionError always
	 */
	public LongStream longs(long streamSize) {
		throw new AssertionError();
	}

	/**
	 * Throws {@link AssertionError} always.
	 * 
	 * @return never
	 * @throws AssertionError always
	 */
	public LongStream longs() {
		throw new AssertionError();
	}

	/**
	 * Throws {@link AssertionError} always.
	 * 
	 * @param streamSize
	 *            ignored
	 * @param randomNumberOrigin
	 *            ignored
	 * @param randomNumberBound
	 *            ignored
	 * @return never
	 * @throws AssertionError always
	 */
	public LongStream longs(long streamSize, long randomNumberOrigin,
			long randomNumberBound) {
		throw new AssertionError();
	}

	/**
	 * Throws {@link AssertionError} always.
	 * 
	 * @param randomNumberOrigin
	 *            ignored
	 * @param randomNumberBound
	 *            ignored
	 * @return never
	 * @throws AssertionError always
	 */
	public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
		throw new AssertionError();
	}

	/**
	 * Throws {@link AssertionError} always.
	 * 
	 * @param streamSize
	 *            ignored
	 * @return never
	 * @throws AssertionError always
	 */
	public DoubleStream doubles(long streamSize) {
		throw new AssertionError();
	}

	/**
	 * Throws {@link AssertionError} always.
	 * 
	 * @return never
	 * @throws AssertionError always
	 */
	public DoubleStream doubles() {
		throw new AssertionError();
	}

	/**
	 * Throws {@link AssertionError} always.
	 * 
	 * @param streamSize
	 *            ignored
	 * @param randomNumberOrigin
	 *            ignored
	 * @param randomNumberBound
	 *            ignored
	 * @return never
	 * @throws AssertionError always
	 */
	public DoubleStream doubles(long streamSize, double randomNumberOrigin,
			double randomNumberBound) {
		throw new AssertionError();
	}

	/**
	 * Throws {@link AssertionError} always.
	 * 
	 * @param randomNumberOrigin
	 *            ignored
	 * @param randomNumberBound
	 *            ignored
	 * @return never
	 * @throws AssertionError always
	 */
	public DoubleStream doubles(double randomNumberOrigin,
			double randomNumberBound) {
		throw new AssertionError();
	}
}
