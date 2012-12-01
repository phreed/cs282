package edu.vanderbilt.cs282.feisele.testutils;

/**
 * Commonly-needed functions for testing, e.g. random string generation
 *
 *
 */

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Arrays;

import java.lang.Double;
import java.lang.Float;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Short;
import java.lang.Long;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.util.Log;

public class TestUtils
{
    @SuppressWarnings("unused")
    private static final String TAG = "TestUtils";

    // Fixed test values for different types
    public static final short TEST_SHORT_INTEGER = new Short(Short.MAX_VALUE).shortValue();
    public static final int TEST_INTEGER = new Integer(Integer.MAX_VALUE-1).intValue();
    public static final long TEST_LONG_INTEGER = new Long("9223372036854775806").longValue();
    public static final boolean TEST_BOOLEAN = new Boolean("true").booleanValue();
    public static final float TEST_FLOAT = new Float("3.141500001").floatValue();
    public static final double TEST_DOUBLE = new Double("3.141500001").doubleValue();
    public static final String TEST_GUID_STR = "6676f490-49a0-41cd-adf1-a4ddbc3f870d";
    public static final String TEST_FIXED_STRING = "this is some text";

    // fixed-value tiny blob 
    private static final String TEST_TINYBLOB_CONTENT = new String("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789)!@#$%^&*(");
    public static final byte[] TEST_TINY_BLOB = TEST_TINYBLOB_CONTENT.getBytes();

    // fixed-value small blob 
    public static final int SMALL_BLOB_SIZE = 1000;
    public static final byte[] TEST_SMALL_BLOB = new byte[SMALL_BLOB_SIZE];
    static 
    {
	byte value = new Byte("99").byteValue();
	Arrays.fill(TEST_SMALL_BLOB, 0, SMALL_BLOB_SIZE-1, value);
    }

    // fixed-value large blob
    public static final int LARGE_BLOB_SIZE = 1000000;
    public static final byte[] TEST_LARGE_BLOB = new byte[LARGE_BLOB_SIZE];
    static 
    {
	byte value = new Byte("83").byteValue();
	Arrays.fill(TEST_LARGE_BLOB, 0, LARGE_BLOB_SIZE-1, value);
    }

    // Acceptable error for floating-point comparisons
    public static final double DBL_DELTA = Double.MIN_VALUE;
    public static final float FLOAT_DELTA = Float.MIN_VALUE;

    // random seed
    private static final Random random = new Random();

    // Symbol set from which to choose random text
    private static final char[] symbols = new char[36];
    static
    {
        for (int idx = 0; idx < 10; ++idx)
            symbols[idx] = (char) ('0' + idx);
        for (int idx = 10; idx < 36; ++idx)
            symbols[idx] = (char) ('a' + idx - 10);
    }


    // =========================================================
    // pseudoRandomString()
    // =========================================================
    private static String pseudoRandomString(int length)
    {
        if (length < 1) {
            throw new IllegalArgumentException("length < 1: " + length);
        }
        final char[] nonsecureBuffer = new char[length];
        for (int idx = 0; idx < nonsecureBuffer.length; ++idx) {
            nonsecureBuffer[idx] = symbols[random.nextInt(symbols.length)];
        }
        return new String(nonsecureBuffer);
    }

    // =========================================================
    // another way to generate a pseudorandom string
    // =========================================================
    @SuppressWarnings("unused")
    private static String pseudoRandomString2()
    {
        return new BigInteger(130, random).toString(32);
    }

    // =========================================================
    // randomText()
    // =========================================================
    public static String randomText(int size)
    {
        return pseudoRandomString(size);
    }

    // =========================================================
    // randomInt()
    // =========================================================
    public static int randomInt(int boundary)
    {
        int limit = boundary;
        if (boundary <= 1) {
            limit = 1;
        }

        // random int on interval [0, limit)
        int f = random.nextInt(limit);
        return f;
    }
    
    // =========================================================
    // randomInt()
    // =========================================================
    public static int randomInt()
    {
        // random uniformly distributed int
        int f = random.nextInt();
        return f;
    }
    
    // =========================================================
    // randomShort()
    // =========================================================
    public static int randomShort()
    {
        short limit = Short.MAX_VALUE;
	return randomInt(limit);
    }
    
    // =========================================================
    // randomLong()
    // =========================================================
    public static long randomLong()
    {
        // random long
        long f = random.nextLong();
        return f;
    }

    // =========================================================
    // randomDouble()
    // =========================================================
    public static double randomDouble()
    {
        // random double on interval [0, 1)
        double f = random.nextDouble();
        return f;
    }

    // =========================================================
    // randomFloat()
    // =========================================================
    public static float randomFloat()
    {
        // random float on interval [0, 1)
        float f = random.nextFloat();
        return f;
    }

    // =========================================================
    // randomBoolean()
    // =========================================================
    public static boolean randomBoolean()
    {
        // random boolean
        boolean f = random.nextBoolean();
        return f;
    }
    
    // =========================================================
    // randomBytes()
    // =========================================================
    public static byte[] randomBytes(int size)
    {
        // random byte array
        byte[] buf = new byte[size];
	random.nextBytes(buf);
	return buf;
    }

    // =========================================================
    // randomGuidAsString()
    // =========================================================
    public static String randomGuidAsString()
    {
        UUID u = UUID.randomUUID();
	return u.toString();
    }


    // =========================================================
    // randomContentValues()
    // =========================================================
    public static ContentValues randomContentValues(int size)
    {
	// CV container filled with random keys and values; a total
	// of 'size' key-value pairs will be inserted.

	// size of random strings themselves
	final int n = 20;

	ContentValues cv = new ContentValues();
	for (int i=0; i < size; i++) {
	    cv.put(randomText(n), randomText(n));
	}
	//Log.d(TAG, "cv = [   " + cv.toString() + "    ]");
	return cv;
    }

    // =========================================================
    // createJsonAsString()
    // =========================================================
    public static String createJsonAsString(ContentValues cv)
    {
        Set<Map.Entry<String, Object>> data = cv.valueSet();
        Iterator<Map.Entry<String, Object>> iter = data.iterator();
        final JSONObject json = new JSONObject();

        while (iter.hasNext())
        {
	    Map.Entry<String, Object> entry = (Map.Entry<String, Object>)iter.next();
	    try {
		if (entry.getValue() instanceof String) {
		    json.put(entry.getKey(), cv.getAsString(entry.getKey()));
		} else if (entry.getValue() instanceof Integer) {
		    json.put(entry.getKey(), cv.getAsInteger(entry.getKey()));
		} else if (entry.getValue() instanceof Long) {
		    json.put(entry.getKey(), cv.getAsLong(entry.getKey()));
		} else if (entry.getValue() instanceof Short) {
		    json.put(entry.getKey(), cv.getAsShort(entry.getKey()));
		} else if (entry.getValue() instanceof Double) {
		    json.put(entry.getKey(), cv.getAsDouble(entry.getKey()));
		} else if (entry.getValue() instanceof Boolean) {
		    json.put(entry.getKey(), cv.getAsBoolean(entry.getKey()));
		}
	    } catch (JSONException e) {
		e.printStackTrace();
		return null;
	    }
	}
	Log.d(TAG, "generated JSON = [" + json.toString() + "]");
        return json.toString();
    }
    
    // =========================================================
    // createJsonAsBytes()
    // =========================================================
    public static byte[] createJsonAsBytes(ContentValues cv)
    {
        String jsonString = createJsonAsString(cv);
	Log.d(TAG, " --> converting JSON to byte array");
        return jsonString.getBytes();
    }

    // =========================================================
    // createContentValues()
    // =========================================================
    public static ContentValues createContentValues()
    {
	// TODO: make the key-value pairs something more meaningful

	ContentValues cv = new ContentValues();
	cv.put("foo1", "bar1");
	cv.put("foo2", "bar2");
	cv.put("foo3", "bar3");
	cv.put("foo4", "bar4");
	cv.put("foo5", "bar5");
	return cv;
    }
}

