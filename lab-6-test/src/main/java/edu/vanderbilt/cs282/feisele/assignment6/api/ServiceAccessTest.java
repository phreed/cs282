package edu.vanderbilt.cs282.feisele.assignment6.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.vanderbilt.cs282.feisele.assignment6.service.DownloadService;

import android.content.Intent;
import android.os.IBinder;
import android.test.suitebuilder.annotation.SmallTest;


/**
 * This is a simple framework for a test of a Service.  
 * See {@link android.test.ServiceTestCase ServiceTestCase} 
 * for more information on how to write and extend service tests.
 * <p>
 * To run this test, you can type:
 * <code>
 * adb shell am instrument -w \
 *   -e class edu.vu.isis.ammo.core.test.DownloadServiceTestDeprecated \
 *   edu.vu.isis.ammo.core.test/android.test.InstrumentationTestRunner
 *   </code>
 */
/**
 * Test for AmmoCore::AmmoActivity
 * 
 *
 */

public class ServiceAccessTest  extends android.test.ServiceTestCase<DownloadService> {
    private Logger logger;
    
    public ServiceAccessTest() {
          super(DownloadService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        logger = LoggerFactory.getLogger("test.service.access");
        logger.info("Set Up " );
    }
    

    /** 
     * Tear down is run once everything is complete.
     */
    @Override
    protected void tearDown () throws Exception {
        logger.info("Tear Down" );
        super.tearDown();
    }

    /**
     * The name 'test preconditions' is a convention to signal that if this test
     * doesn't pass, the test case was not set up properly and it might explain
     * any and all failures in other tests. This is not guaranteed to run before
     * other tests, as junit uses reflection to find the tests.
     */
    @SmallTest
    public void testPreconditions() {
          // assertNotNull(this.ad);
    }

    /**
     * Test basic startup/shutdown of Service
     */
    @SmallTest
    public void testStartable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), DownloadService.class);
        startService(startIntent); 
    }

    /**
     * Test binding to service
     */
    @SmallTest
    public void testBindable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), DownloadService.class);
        @SuppressWarnings("unused")
        IBinder service = bindService(startIntent); 
    }

}
