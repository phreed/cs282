package edu.vanderbilt.cs282.feisele.testutils;

/**
 * Functions to assist in "fuzz" testing of Ammo software.
 *
 * http://en.wikipedia.org/wiki/Fuzz_testing
 */


import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;

public class FuzzTestingTools
{
  @SuppressWarnings("unused")
  private static final String TAG = "AmmoFuzzTestingTools";

  // =========================================================
  // badJsonBytes()
  // 
  // Create a JSON-encoded byte array with two bytes interchanged
  // =========================================================
  public static byte[] badJsonBytes(ContentValues cv)
  {
    // JSON-encoded string as byte array
    byte[] jsonBytes = TestUtils.createJsonAsBytes(cv);

    // interchange two bytes to make it a nearly-normal JSON string

    // First byte; choose randomly within byte array
    int xIndex = TestUtils.randomInt(jsonBytes.length);
    byte xByte = jsonBytes[xIndex];

    // Second byte; if first byte was at end of array, choose the 
    // previous byte, otherwise choose the next.
    int yIndex = (xIndex == (jsonBytes.length - 1)) ? (xIndex - 1) : (xIndex + 1);
    byte yByte = jsonBytes[yIndex];

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
  public static String badJsonString01(ContentValues cv)
  {
    // Select one key-value pair in the cv
    Set<Map.Entry<String, Object>> data = cv.valueSet();
    Iterator<Map.Entry<String, Object>> iter = data.iterator();
    // (for now just pick the first one)
    Map.Entry<String, Object> entry = (Map.Entry<String, Object>)iter.next();
    String keyToRemove = entry.getKey();

    // Remove it
    cv.remove(keyToRemove);

    // JSON-encode the altered cv
    String jsonString = TestUtils.createJsonAsString(cv);

    return jsonString;
  }

  // =========================================================
  // badJsonString02()
  // 
  // Create a JSON-encoded Ammo message with a key-value pair randomized
  // =========================================================
  public static String badJsonString02(ContentValues cv)
  {
    // Select one key-value pair in the cv
    Set<Map.Entry<String, Object>> data = cv.valueSet();
    Iterator<Map.Entry<String, Object>> iter = data.iterator();
    // (for now just pick the first one)
    Map.Entry<String, Object> entry = (Map.Entry<String, Object>)iter.next();
    String keyToRemove = entry.getKey();

    // Rename the key with a random value
    if (!cv.containsKey(keyToRemove)) { return null; }
    cv.remove(keyToRemove);
    cv.put(TestUtils.randomText(TestUtils.randomInt(20)), "value");

    // JSON-encode the altered cv
    String jsonString = TestUtils.createJsonAsString(cv);

    return jsonString;
  }

  // =========================================================
  // badJsonString03()
  // 
  // Create a JSON-encoded string with all key-value pairs randomized
  // =========================================================
  public static String badJsonString03()
  {
    // Create random ContentValues
    final int size = 5;
    ContentValues cv = new ContentValues();
    for (int i=0; i < size; i++) {
      cv.put(TestUtils.randomText(TestUtils.randomInt(20)), 
          TestUtils.randomText(TestUtils.randomInt(20)));
    }

    // JSON-encoded string of our random Content Values
    String jsonString = TestUtils.createJsonAsString(cv);

    return jsonString;
  }
}

