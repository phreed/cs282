package edu.vanderbilt.cs282.feisele.lab06.mock;

import junit.framework.Test;
import junit.framework.TestSuite;
import android.test.AndroidTestCase;

/**
 * Template unit test for a plain Java class 
 * 
 * Use this class as a template to create new unit tests
 * for classes which use Android-specific components.
 */

public class TemplateAndroidClassTest extends AndroidTestCase 
{
    public TemplateAndroidClassTest() 
    {
    }

    public TemplateAndroidClassTest( String testName )
    {
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TemplateAndroidClassTest.class );
    }

    /**
     * Called before every test
     */
    protected void setUp() throws Exception
    {
	// ...
    }

    /**
     * Called after every test
     */
    protected void tearDown() throws Exception
    {
	// ...
    }

    /**
     * Test methods
     */
    public void testNumberOne()
    {
	assertTrue(true);
    }
    
    public void testNumberTwo()
    {
	assertTrue(true);
    }
}
