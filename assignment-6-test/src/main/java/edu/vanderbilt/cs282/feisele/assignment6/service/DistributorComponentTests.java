package edu.vanderbilt.cs282.feisele.assignment6.service;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Map;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Application;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.test.mock.MockApplication;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import edu.vu.isis.ammo.annotation.TestPreamble;
import edu.vu.isis.ammo.api.AmmoRequest;
import edu.vu.isis.ammo.api.IAmmoRequest;
import edu.vu.isis.ammo.api.type.Notice;
import edu.vu.isis.ammo.api.type.TimeInterval;
import edu.vu.isis.ammo.core.AmmoService;
import edu.vu.isis.ammo.core.MockContextRenaming;
import edu.vu.isis.ammo.core.AmmoService.DistributorServiceAidl;
import edu.vu.isis.ammo.core.pb.AmmoMessages;
import edu.vu.isis.ammo.core.pb.AmmoMessages.MessageWrapper.MessageType;

/**
 * This is a simple framework for a test of a Service.  
 * See {@link android.test.ServiceTestCase ServiceTestCase} 
 * for more information on how to write and extend service tests.
 * <p>
 * To run this test, you can type:
 * <code>
 adb shell am instrument -w \
 -e class edu.vu.isis.ammo.core.distributor.DistributorComponentTests \
 edu.vu.isis.ammo.core.tests/pl.polidea.instrumentation.PolideaInstrumentationTestRunner
 * </code>
 */
/**
 * This test treats the distributor as a component. the distributor is bounded
 * by:
 * <ul>
 * <li>the ammolib api {post, subscribe, retrieve}
 * <li>the application content providers via RequestSerializer
 * <li>the channels which MockProvider is used by the test.
 * </ul>
 */
public class DistributorComponentTests extends AmmoServiceTestLogger {
    private Logger logger;

    private AmmoRequest.Builder builder;
    private Application application;
    @SuppressWarnings("unused")
    final private String packageName;
    @SuppressWarnings("unused")
    final private String className;

    private AmmoService service;

    @SuppressWarnings("unused")
    private final Uri provider = Uri.parse("content://edu.vu.isis.ammo.core/distributor");

    private final String expectedTopic = "ammo/arbitrary-topic";
    private final Calendar now = Calendar.getInstance();
    final TimeInterval expiration = new TimeInterval(TimeInterval.Unit.HOUR, 1);
    private final int worth = 5;
    private final String filter = "no filter";
    /** time in seconds */
    @SuppressWarnings("unused")
    private final int lifetime = 10;

    // final Notice notice = new Notice(new PendingIntent());

    public DistributorComponentTests() {
        this(AmmoService.class.getPackage().getName(), AmmoService.class.getName());
    }

    static final String LOGBACK_XML =
            "<configuration debug='true'>" +
                    " <property name='LOG_DIR' value='/mnt/sdcard' />" +
                    "  <appender name='FILE' class='ch.qos.logback.core.FileAppender'>" +
                    "    <file>${LOG_DIR}/ammo-dist-comp-test.log</file>" +
                    "    <append>true</append>" +
                    "    <encoder>" +
                    "      <pattern>%-4r [%t] %-5p %c{35} - %m%n</pattern>" +
                    "    </encoder>" +
                    "  </appender>" +
                    "  <logger name='api' level='TRACE'/>" +
                    "  <logger name='dist.state' level='TRACE'/>" +
                    "  <logger name='dist.thread' level='TRACE'/>" +
                    "  <logger name='dist.store' level='TRACE'/>" +
                    "  <logger name='dist.serializer' level='TRACE'/>" +
                    "  <logger name='dist.policy.class' level='TRACE'/>" +
                    "  <logger name='service' level='TRACE'/>" +
                    "  <logger name='mock.channel' level='TRACE'/>" +
                    "  <logger name='mock.net' level='TRACE'/>" +
                    "  <logger name='queue' level='TRACE'/>" +
                    "  <logger name='test.context.mock' level='TRACE'/>" +
                    "  <logger name='test.request.distribute' level='TRACE'/>" +
                    "  <logger name='test.service.lifecycle' level='TRACE'/>" +
                    "  <root level='OFF'>" +
                    "    <appender-ref ref='FILE' />" +
                    "  </root>" +
                    "</configuration>";

    private static void logInit() {
        final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            // load a specific logback.xml
            final JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.reset(); // override default configuration
            configurator.doConfigure(
                    // "assets/logback-dist-comp.xml"
                    new ByteArrayInputStream(LOGBACK_XML.getBytes())
                    );

        } catch (JoranException je) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    }

    public DistributorComponentTests(String packageName, String className) {
        super(AmmoService.class);
        this.setName(className);

        logger = LoggerFactory.getLogger("test.request.distribute");
        this.className = className;
        this.packageName = packageName;
    }

    /**
     * Keep in mind when acquiring a context that there are multiple candidates:
     * <ul>
     * <li>the context of the service being tested [getContext() or
     * getSystemContext()]
     * <li>the context of the test itself
     * </ul>
     * see http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.
     * android/android-apps/4.1
     * .1_r1/com/android/calendar/AsyncQueryServiceTest.java#AsyncQueryServiceTe
     * s t . s e t U p % 2 8 % 2 9
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.application = new MockApplication();
        this.setApplication(this.application);

        final MockContextRenaming mockContext =
                new MockContextRenaming(this.getContext());
        this.setContext(mockContext);
        logInit();
        AmmoService.suppressStartCommand();
        // this.setupService();
    }

    /**
     * Tear down is run once everything is complete.
     */
    @Override
    protected void tearDown() throws Exception {
        logger.info("Tear Down");
        // this.service.onDestroy();
        super.tearDown();
    }

    /**
     * Start the service.
     * <p>
     * The data store is provided with a name for forensics.
     * <p>
     * Load the appropriate distribution policy.
     * 
     * @throws Exception
     */
    private void startUp(final String policyFileName) throws Exception {
        try {
            if (!(getContext() instanceof MockContextRenaming)) {
                fail("not proper context class");
            }

            DistributorDataStore.backingFileName("distributor.db");
            AmmoService.distributionPolicyFileName(policyFileName);

            final Intent startIntent = new Intent();
            startIntent.setClass(getContext(), AmmoService.class);

            logger.info("startup: binder intent {}", startIntent);
            final IBinder serviceBinder = this.bindService(startIntent);
            // final boolean serviceBindable = getContext().
            // bindService(startIntent, this.conn, Context.BIND_AUTO_CREATE);
            assertNotNull("could not bind", serviceBinder);

            this.service = ((DistributorServiceAidl) serviceBinder).getService();
            logger.info("test service {}",
                    Integer.toHexString(System.identityHashCode(this.service)));
            // this.service = this.getService();
            logger.info("test service {}",
                    Integer.toHexString(System.identityHashCode(this.getService())));

            this.service.getAssets();
            assertNotNull("the service is null", this.service);
            // assertSame("the service is not the same",
            // this.service,ammoService);

            this.builder = AmmoRequest.newBuilder(this.getContext(), serviceBinder);
        } catch (Exception ex) {
            logger.error("super exception");
        }
    }

    @SmallTest
    public void testAndroidTestCaseSetUpPropertly() throws Exception {
        logger.info("test Android TestCase Set Up Propertly : start");
        super.testAndroidTestCaseSetupProperly();
    }

    @SmallTest
    public void testServiceTestCaseSetUpPropertly() throws Exception {
        logger.info("test Service TestCase Set Up Propertly : start");
        super.testServiceTestCaseSetUpProperly();
    }

    /**
     * Post messages and verify that they meet their appropriate fates.
     */
    @TestPreamble (
            activate = "1.6.3",
            expire = "unlimited",
            onSmoke = true,
            onComponent = {},
            onUnit = {}
    )
    @MediumTest
    public void testPostalWithContentValues() {
        logger.info("test postal : start");
        try {
            this.startUp("dist-policy-single-rule.xml");
        } catch (Exception ex) {
            Assert.fail("test failed, could not start environment " + ex.getLocalizedMessage());
        }
        final MockChannel mockChannel = MockChannel.getInstance("mock", this.service);
        this.service.registerChannel(mockChannel);
        logger.info("postal : exercise the distributor");

        final Uri provider = Uri.parse("content://edu.vu.isis.ammo.core/distributor");

        final ContentValues cv = new ContentValues();
        {
            cv.put("greeting", "Hello");
            cv.put("recipient", "World");
            cv.put("emphasis", "!");
            cv.put("source", "me");
        }

        logger.info("post: provider [{}] payload [{}] topic [{}]",
                new Object[] {
                        provider, cv, expectedTopic
                });
        logger.info("args now [{}] expire [{}] worth [{}] filter [{}]",
                new Object[] {
                        now, expiration, worth, filter
                });
        try {

            final IAmmoRequest request = builder
                    .provider(provider)
                    .topic(expectedTopic)
                    .payload(cv)
                    .notice(Notice.RESET)
                    .post();
            logger.info("posted request [{}]", request);

        } catch (RemoteException ex) {
            logger.error("could not post", ex);
            Assert.fail("could not post");
        }

        Assert.assertNotNull("mock channel not available", mockChannel);
        final MockNetworkStack network = mockChannel.mockNetworkStack;
        final ByteBuffer sentBuf = network.getSent();
        Assert.assertNotNull("not received into send buffer", sentBuf);

        // See AmmoGatewayMessage for details
        final byte[] magic = new byte[4];
        sentBuf.get(magic);
        assertArrayEquals("magic error",
                new byte[] {
                        -17, -66, -19, -2
                }, magic);

        final int msgSize = sentBuf.getInt();
        logger.info("payload size=<{}>", msgSize);

        final byte priority = sentBuf.get();
        Assert.assertEquals("msg priority", (byte) 0, priority);

        final byte[] reserved = new byte[3];
        sentBuf.get(reserved);
        assertArrayEquals("reserved", new byte[] {
                0, 0, 0
        }, reserved);

        final byte[] pcheck = new byte[4];
        sentBuf.get(pcheck);
        // assertArrayEquals("payload checksum", new byte[]{-94, 118, 50,
        // 21}, pcheck);

        final byte[] hcheck = new byte[4];
        sentBuf.get(hcheck);
        // assertArrayEquals("header checksum", new byte[]{-68, -65, -2,
        // -102}, hcheck);

        final byte[] protobuf = new byte[sentBuf.remaining()];
        sentBuf.get(protobuf);
       
        try {
            logger.info("protobuf=[{}]", new String(protobuf, "US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            logger.warn("could not convert protobuf to US-ASCII");
        }

        final AmmoMessages.MessageWrapper mw;
        try {
            mw = AmmoMessages.MessageWrapper.parseFrom(protobuf);
        } catch (InvalidProtocolBufferException e) {
            logger.error("could not parse protocol buffer");
            Assert.fail("could not parse protocol buffer");
            return;
        }
        logger.info("protobuf unwrapped=<{}>", mw);

        Assert.assertTrue("no type", mw.hasType());
        Assert.assertEquals("type", MessageType.DATA_MESSAGE, mw.getType());

        Assert.assertTrue("no data message", mw.hasDataMessage());
        final AmmoMessages.DataMessage dm = mw.getDataMessage();

        Assert.assertTrue("no uuid", dm.hasUri());

        Assert.assertTrue("no data", dm.hasData());
        final ByteString payload = dm.getData();
        try {
            final JSONObject jayload = (JSONObject) new JSONTokener(payload.toStringUtf8())
                    .nextValue();

            for (Map.Entry<String, Object> entry : cv.valueSet()) {
                final String key = entry.getKey();
                Assert.assertTrue("no payload " + key, jayload.has(key));
                Assert.assertEquals("payload " + key, entry.getValue(), jayload.getString(key));
            }
        } catch (JSONException ex) {
            Assert.fail("not json");
        }

        final String expectedEncoding = "JSON";

        Assert.assertTrue("no encoding", dm.hasEncoding());
        Assert.assertEquals("encoding", expectedEncoding, dm.getEncoding());

        Assert.assertTrue("no topic", dm.hasMimeType());
        Assert.assertEquals("topic", expectedTopic, dm.getMimeType());

        Assert.assertTrue("no operator", dm.hasUserId());

        Assert.assertTrue("no device", dm.hasOriginDevice());
    }

}
