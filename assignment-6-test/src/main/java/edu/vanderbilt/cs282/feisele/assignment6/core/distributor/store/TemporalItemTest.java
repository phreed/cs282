package edu.vanderbilt.cs282.feisele.store;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

//import edu.vu.isis.ammo.core.distributor.store.TemporalItem; 
import edu.vu.isis.ammo.core.distributor.store.Presence.Item;
import edu.vu.isis.ammo.core.distributor.store.Presence.Builder;
import edu.vu.isis.ammo.core.distributor.store.Presence;

/**
 * Unit test for TemporalItem
 * 
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class edu.vu.isis.ammo.core.distributor.store.TemporalItem \
 * edu.vu.isis.ammo.core.tests/android.test.InstrumentationTestRunner
 */

public class TemporalItemTest extends TestCase
{
    public TemporalItemTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( TemporalItemTest.class );
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
    public void testBasicConstruction()
    {
	Builder b = Presence.newBuilder();
	assertNotNull(b);
	b.operator("bubba");
	b.origin("ffffffff");
	Item item = b.buildItem();
	assertNotNull(item);
	assertEquals(item.count, 1);
	
	item.update();
	assertEquals(item.count, 2);
	
	item.update();
	assertEquals(item.count, 3);
	
	assertEquals(item.key.operator, "bubba");
	assertEquals(item.key.origin, "ffffffff");
    }
    
    public void testKeyEquals()
    {
	Builder b1 = Presence.newBuilder();
	assertNotNull(b1);
	b1.operator("bubba");
	b1.origin("ffffffff");
	Item item1 = b1.buildItem();
	assertNotNull(item1);
	
	Builder b2 = Presence.newBuilder();
	assertNotNull(b2);
	b2.operator("bubba");
	b2.origin("ffffffff");
	Item item2 = b2.buildItem();
	assertNotNull(item2);
	
	assertTrue(item2.key.equals(item1.key));
    }
    
}

