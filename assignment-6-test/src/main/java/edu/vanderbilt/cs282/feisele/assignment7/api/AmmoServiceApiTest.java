package edu.vanderbilt.cs282.feisele.api;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.test.suitebuilder.annotation.MediumTest;
import edu.vu.isis.ammo.api.type.TimeInterval;
import edu.vu.isis.ammo.core.AmmoService;

/**
 * This is a simple framework for a test of a Service.  
 * See {@link android.test.ServiceTestCase ServiceTestCase} 
 * for more information on how to write and extend service tests.
 * 
 * To run this test, you can type:
 * adb shell am instrument -w \
 *   -e class edu.vu.isis.ammo.core.test.AmmoServiceTest \
 *   edu.vu.isis.ammo.core.test/android.test.InstrumentationTestRunner
 */
/**
 * Test for AmmoCore::AmmoActivity
 */
public class AmmoServiceApiTest extends android.test.ServiceTestCase<AmmoService> {
    private Logger logger;

    private AmmoRequest.Builder builder;
    
    private final Uri provider = Uri.parse("content://edu.vu.isis.ammo.core/distributor");

    private final String topic = "arbitrary-topic";
    private final Calendar now = Calendar.getInstance();
    final TimeInterval expiration = new TimeInterval(TimeInterval.Unit.HOUR, 1);
    private final int worth = 5;
    private final String filter = "no filter";
    private final int lifetime = 10; // time in seconds
    // private final int field = Calendar.DATE;
    
    final String serializedString = "{\"greeting\":\"Hello World!\"}";
    
    // final Notice notice = new Notice(new PendingIntent());

    public AmmoServiceApiTest() {
        super(AmmoService.class);
        logger = LoggerFactory.getLogger("test.service.request");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), AmmoService.class);
        final IBinder serviceBinder = bindService(startIntent);
        this.builder = AmmoRequest.newBuilder(getContext(), serviceBinder);

    }

    /**
     * Tear down is run once everything is complete.
     */
    @Override
    protected void tearDown() throws Exception {
        logger.info("Tear Down");
    }

    @MediumTest
    public void testSubscribe() {
        logger.info("subscribe : exercise the deprecated api");

        final Uri provider = Uri.parse("content://edu.vu.isis.ammo.core/distributor");

        final ContentValues cv = new ContentValues();
        {
            cv.put("greeting", "Hello");
            cv.put("recipient", "World");
            cv.put("emphasis", "!");
        }

        logger.info(
                "args provider [{}] content [{}] topic [{}] now [{}] expire [{}] worth [{}] filter [{}]",
                new Object[] {
                        provider, cv, topic, now, expiration, worth, filter
                });

        try {
            logger.info("subscribe : provider, topic");
            builder
                    .provider(provider)
                    .topic(topic)
                    .subscribe();

            logger.info("subscribe : + expire, filter");
            builder
                    .expire(expiration)
                    .filter(filter)
                    .subscribe();

            logger.info("subscribe : change expire");
            builder
                    .expire(new TimeInterval(TimeInterval.Unit.SECOND, lifetime))
                    .filter(filter)
                    .subscribe();

            logger.info("subscribe : ");
            builder
                    .expire(new TimeInterval(TimeInterval.Unit.SECOND, lifetime))
                    .filter(filter)
                    .subscribe();

            logger.info("subscribe : check status of subscription");
            // List<IAmmoRequest> status = builder.subscribe(10);

            // assertNotNull("subscribe status null", status);

            // for (Map<String,String> request : status) {
            // for (String key : request.keySet()) {
            // logger.info("{} \t : {}", key, request.get(key));
            // }
            // }
        } catch (RemoteException ex) {
            logger.error("error with connection {}", ex.getStackTrace());
        }
        logger.info("subscribe : COMPLETED");
    }

    /**
     * Present a set of posts to the distributor.
     */
    public void testPost() {
        logger.info("post : exercise the deprecated api");


        try {
            logger.info("post : provider only");
            builder
                    .provider(provider)
                    .post();

            logger.info("post : topic with content values payload");
            builder
                    .topic(topic)
                    .payload(new AmmoValues() {
                        {
                            put("greeting", "Hello");
                            put("recipient", "World");
                            put("emphasis", "!");
                        }
                    })
                    .post();

            logger.info("post : topic with json payload");
            builder
                    .payload(serializedString)
                    .post();

            logger.info("post : provider expiration and worth");
            builder
                    .expire(expiration)
                    .post();

            // logger.info("post : provider, topic, expiration, worth and notice");
            // builder
            // .notify(notice)
            // .post();

        } catch (RemoteException ex) {
            logger.error("error with connection {}", ex.getStackTrace());
            assertTrue("could not connect to remote", false);
        }
        logger.info("post : COMPLETED");
    }

    public void testRetrieve() {
        logger.info("retrieval : exercise the deprecated api");

        try {
            logger.info("post : provider and topi");
            builder
                    .provider(provider)
                    .topic(topic)
                    .retrieve();

        } catch (RemoteException ex) {
            logger.error("error with connection {}", ex.getStackTrace());
        }
        logger.info("retrieval : COMPLETED");
    }

}
