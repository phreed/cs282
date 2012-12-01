package edu.vanderbilt.cs282.feisele.network;

import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import edu.vu.isis.ammo.core.distributor.store.Presence;
import edu.vu.isis.ammo.core.distributor.store.Presence.Builder;
import edu.vu.isis.ammo.core.distributor.store.Presence.Item;
import edu.vu.isis.ammo.core.provider.TemporalState;
/**
 * Unit test for TemporalState
 * 
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class edu.vu.isis.ammo.core.provider.TemporalStateTest \
 * edu.vu.isis.ammo.core.tests/android.test.InstrumentationTestRunner
 */

public class TemporalStateTest extends TestCase
{
    public TemporalStateTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( TemporalStateTest.class );
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
	
	// The user was just now "seen" so state should be present
	TemporalState ts = item.getDominantState();
	assertEquals(ts, TemporalState.PRESENT);
    }
    
    public void testEncodeState()
    {
	Builder b = Presence.newBuilder();
	assertNotNull(b);
	b.operator("bubba");
	b.origin("ffffffff");
	Item item = b.buildItem();
	assertNotNull(item);
	item.update();	
	TemporalState ts = item.getDominantState();
	
	HashSet<TemporalState> set = new HashSet<TemporalState>();
	set.add(ts);
	int encoded = TemporalState.encodeState(set);
	assertEquals(encoded, 1);
    }
    
    public void testDecodeState()
    {
	Builder b = Presence.newBuilder();
	assertNotNull(b);
	b.operator("bubba");
	b.origin("ffffffff");
	Item item = b.buildItem();
	assertNotNull(item);
	item.update();	
	TemporalState ts = item.getDominantState();
	
	HashSet<TemporalState> set = new HashSet<TemporalState>();
	set.add(ts);
	int encoded = TemporalState.encodeState(set);
	
	TemporalState ts2 = TemporalState.decodeState(encoded);
	assertEquals(ts2, TemporalState.PRESENT);
    }
}

