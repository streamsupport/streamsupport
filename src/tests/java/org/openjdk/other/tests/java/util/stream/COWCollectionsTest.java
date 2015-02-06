package org.openjdk.other.tests.java.util.stream;

import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.testng.Assert;
import org.testng.annotations.Test;

import java8.util.function.Consumer;
import java8.util.stream.Stream;
import java8.util.stream.StreamSupport;

@Test
public class COWCollectionsTest {

	public void testCOWList() {
		try {
			CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<String>(
					Arrays.asList("one", "two", "three", "two", "one"));

			Stream<String> s = StreamSupport.stream(list).distinct();

			s.forEach(new Consumer<String>() {
				@Override
				public void accept(String s) {
					System.out.println(s);
				}
			});

			System.out.println("Done: no failures");
			Assert.assertTrue(true);

		} catch (Throwable t) {
			System.err.println("Failed");
			t.printStackTrace();

			Assert.assertTrue(false);
		}
	}

	public void testCOWSet() {
		try {
			CopyOnWriteArraySet<String> list = new CopyOnWriteArraySet<String>(
					Arrays.asList("one", "two", "three", "four", "one"));

			Stream<String> s = StreamSupport.stream(list).sorted();

			s.forEach(new Consumer<String>() {
				@Override
				public void accept(String s) {
					System.out.println(s);
				}
			});

			System.out.println("Done: no failures");
			Assert.assertTrue(true);

		} catch (Throwable t) {
			System.err.println("Failed");
			t.printStackTrace();

			Assert.assertTrue(false);
		}
	}
}
