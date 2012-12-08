package edu.vanderbilt.cs282.feisele.lab06.mock;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Template unit test for a plain Java class 
 * 
 * Use this class as a template to create new unit tests
 * for "plain" Java classes (i.e. those having no Android bits).
 */

// [IMPORT AMMO CLASS(ES) TO BE TESTED HERE]

public class TemplatePlainJavaClassTest extends TestCase
{
    public TemplatePlainJavaClassTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( TemplatePlainJavaClassTest.class );
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

