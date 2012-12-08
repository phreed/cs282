package edu.vanderbilt.cs282.feisele.lab06.service;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.test.mock.MockApplication;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import edu.vanderbilt.cs282.feisele.lab06.annotation.TestPreamble;
import edu.vanderbilt.cs282.feisele.lab06.mock.MockContextRenaming;
import edu.vanderbilt.cs282.feisele.lab06.provider.DownloadContentProviderSchema;
import edu.vanderbilt.cs282.feisele.lab06.service.NetworkProxy.JsoupProxy;
import edu.vanderbilt.cs282.feisele.lab06.service.NetworkProxy.UrlProxy;

/**
 * This is a simple framework for a test of a Service.  
 * See {@link android.test.ServiceTestCase ServiceTestCase} 
 * for more information on how to write and extend service tests.
 * <p>
 * To run this test, you can type:
 * <code>
 adb shell am instrument -w \
 -e class edu.vanderbilt.isis.ammo.core.distributor.DistributorComponentTests \
 edu.vanderbilt.isis.ammo.core.tests/pl.polidea.instrumentation.PolideaInstrumentationTestRunner
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
public class DownloadServiceTest extends DownloadServiceTestLogger {
    private Logger logger;

    private Application application;
    @SuppressWarnings("unused")
    final private String packageName;
    @SuppressWarnings("unused")
    final private String className;


    @SuppressWarnings("unused")
    private final Uri provider = DownloadContentProviderSchema.BASE_URI;

	private DownloadService service;

    // final Notice notice = new Notice(new PendingIntent());

    public DownloadServiceTest() {
        this(DownloadService.class.getPackage().getName(), DownloadService.class.getName());
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

    public DownloadServiceTest(String packageName, String className) {
        super(DownloadService.class);
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
        final JsoupProxy jsoupProxy = MockChannel.JsoupProxy.getInstance();
        final UrlProxy urlProxy = MockChannel.UrlProxy.getInstance();
        this.getService().setNetworkProxy(jsoupProxy, urlProxy);
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

            final Intent startIntent = new Intent();
            startIntent.setClass(getContext(), DownloadService.class);

            logger.info("startup: binder intent {}", startIntent);
            final IBinder serviceBinder = this.bindService(startIntent);
            // final boolean serviceBindable = getContext().
            // bindService(startIntent, this.conn, Context.BIND_AUTO_CREATE);
            assertNotNull("could not bind", serviceBinder);

            this.service = this.getService();
            logger.info("test service {}",
                    Integer.toHexString(System.identityHashCode(this.service)));
            // this.service = this.getService();
            logger.info("test service {}",
                    Integer.toHexString(System.identityHashCode(this.getService())));

            this.service.getAssets();
            assertNotNull("the service is null", this.service);
           
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
    public void testRequest() {
        logger.info("test postal : start");
        try {
            this.startUp("dist-policy-single-rule.xml");
        } catch (Exception ex) {
            Assert.fail("test failed, could not start environment " + ex.getLocalizedMessage());
        }
        final MockChannel mockChannel = MockChannel.getInstance("mock", this.service);
        logger.info("postal : exercise the distributor");

        Assert.assertNotNull("mock channel not available", mockChannel);
        final MockNetworkStack network = mockChannel.mockNetworkStack;
        final ByteBuffer sentBuf = network.getSent();
        Assert.assertNotNull("not received into send buffer", sentBuf);
    }

}
