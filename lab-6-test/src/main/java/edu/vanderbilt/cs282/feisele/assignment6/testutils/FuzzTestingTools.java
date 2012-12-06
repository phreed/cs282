package edu.vanderbilt.cs282.feisele.assignment6.testutils;

/**
 * Functions to assist in "fuzz" testing.
 *
 * http://en.wikipedia.org/wiki/Fuzz_testing
 */

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentValues;

public class FuzzTestingTools {
	private static final Logger logger = LoggerFactory
			.getLogger("AmmoFuzzTestingTools");

	// =========================================================
	// badJsonBytes()
	//
	// Create a JSON-encoded byte array with two bytes interchanged
	// =========================================================
	public static byte[] badJsonBytes(ContentValues cv) {
		logger.trace("JSON-encoded string as byte array");
		final byte[] jsonBytes = TestUtils.createJsonAsBytes(cv);

		// interchange two bytes to make it a nearly-normal JSON string

		// First byte; choose randomly within byte array
		final int xIndex = TestUtils.randomInt(jsonBytes.length);
		final byte xByte = jsonBytes[xIndex];

		// Second byte; if first byte was at end of array, choose the
		// previous byte, otherwise choose the next.
		final int yIndex = (xIndex == (jsonBytes.length - 1)) ? (xIndex - 1)
				: (xIndex + 1);
		final byte yByte = jsonBytes[yIndex];

		// Now interchange them
		jsonBytes[yIndex] = xByte;
		jsonBytes[xIndex] = yByte;

		return jsonBytes;
	}

	// =========================================================
	// badJsonString01()
	//
	// Create a JSON-encoded Ammo message with one key-value pair missing
	// =========================================================
	public static String badJsonString01(ContentValues cv) {
		logger.trace("Select one key-value pair in the cv");
		final Set<Map.Entry<String, Object>> data = cv.valueSet();
		final Iterator<Map.Entry<String, Object>> iter = data.iterator();
		final Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iter
				.next();
		final String keyToRemove = entry.getKey();
		logger.trace("key to remove {}", keyToRemove);
		cv.remove(keyToRemove);
		return TestUtils.createJsonAsString(cv);
	}

	// =========================================================
	// badJsonString02()
	//
	// Create a JSON-encoded Ammo message with a key-value pair randomized
	// =========================================================
	public static String badJsonString02(ContentValues cv) {
		// Select one key-value pair in the cv
		final Set<Map.Entry<String, Object>> data = cv.valueSet();
		final Iterator<Map.Entry<String, Object>> iter = data.iterator();
		// (for now just pick the first one)
		final Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iter
				.next();
		final String keyToRemove = entry.getKey();

		// Rename the key with a random value
		if (!cv.containsKey(keyToRemove)) {
			return null;
		}
		cv.remove(keyToRemove);
		cv.put(TestUtils.randomText(TestUtils.randomInt(20)), "value");

		logger.trace("JSON-encode the altered cv");
		return TestUtils.createJsonAsString(cv);
	}

	// =========================================================
	// badJsonString03()
	//
	// Create a JSON-encoded string with all key-value pairs randomized
	// =========================================================
	public static String badJsonString03() {
		// Create random ContentValues
		final int size = 5;
		final ContentValues cv = new ContentValues();
		for (int i = 0; i < size; i++) {
			cv.put(TestUtils.randomText(TestUtils.randomInt(20)),
					TestUtils.randomText(TestUtils.randomInt(20)));
		}
		logger.trace("JSON-encoded string of our random Content Values");
		return TestUtils.createJsonAsString(cv);
	}
}
