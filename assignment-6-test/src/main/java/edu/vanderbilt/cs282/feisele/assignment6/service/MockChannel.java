package edu.vanderbilt.cs282.feisele.assignment6.service;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import edu.vu.isis.ammo.core.distributor.DistributorDataStore.DisposalState;
import edu.vu.isis.ammo.core.network.AmmoGatewayMessage;
import edu.vu.isis.ammo.core.network.IChannelManager;
import edu.vu.isis.ammo.core.network.INetChannel;
import edu.vu.isis.ammo.core.network.INetworkService;
import edu.vu.isis.ammo.core.network.ISecurityObject;
import edu.vu.isis.ammo.core.network.NetChannel;
import edu.vu.isis.ammo.core.pb.AmmoMessages;

/**
 * This class provides a mock channel for testing the Ammo distributor.
 * <p>
 * In particular it extends the NetChannel abstract class and implements the
 * INetChannel interface. Mostly it provides a sendRequest() method which
 * receives messages. Typically the sendRequest() method places messages into a
 * queue. This class does the same but rather than delivering those messages via
 * some network medium it provides an API for mock checking and otherwise
 * processing messages from the queue. The mock API also allows mock incoming
 * messages.
 * <p>
 */
public class MockChannel extends NetChannel
{
    static public final Logger logger = LoggerFactory.getLogger("mock.channel");

    /** 5 seconds expressed in milliseconds */
    private static final int BURP_TIME = 5 * 1000;

    private boolean isEnabled = true;

    private ConnectorThread connectorThread;

    // New threads
    private SenderThread mSender;
    private ReceiverThread mReceiver;

    private ByteOrder endian = ByteOrder.LITTLE_ENDIAN;
    private final Object syncObj;

    private boolean shouldBeDisabled = false;

    private SenderQueue mSenderQueue;
    private final AtomicBoolean mIsAuthorized;

    public final IChannelManager mChannelManager;
    private final AtomicReference<ISecurityObject> mSecurityObject = new AtomicReference<ISecurityObject>();

    private final AtomicInteger mMessagesSent = new AtomicInteger();
    private final AtomicInteger mMessagesReceived = new AtomicInteger();

    private MockChannel(String name, IChannelManager iChannelManager) {
        super(name);

        logger.trace("Thread <{}>MockChannel::<constructor>", Thread.currentThread().getId());
        this.syncObj = this;

        this.mIsAuthorized = new AtomicBoolean(false);
        this.mChannelManager = iChannelManager;
        this.mSenderQueue = new SenderQueue(this);

        this.connectorThread = new ConnectorThread(this);
        this.mockNetworkStack = new MockNetworkStack();

        this.mockLinkSwitch = true;
    }

    public static MockChannel getInstance(String name, IChannelManager iChannelManager)
    {
        logger.trace("Thread <{}> MockChannel::getInstance()",
                Thread.currentThread().getId());
        final MockChannel instance = new MockChannel(name, iChannelManager);
        return instance;
    }

    @Override
    public String getSendReceiveStats() {
        StringBuilder countsString = new StringBuilder();
        countsString.append("S:").append(mMessagesSent.get()).append(" ");
        countsString.append("R:").append(mMessagesReceived.get());
        return countsString.toString();
    }

    public boolean isConnected() {
        return this.connectorThread.isConnected();
    }

    /**
     * Was the status changed as a result of enabling the connection.
     * 
     * @return
     */
    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void enable() {
        logger.trace("Thread <{}>::enable", Thread.currentThread().getId());
        synchronized (this.syncObj) {
            if (!this.isEnabled) {
                this.isEnabled = true;

                // if (! this.connectorThread.isAlive())
                // this.connectorThread.start();

                logger.warn("::enable - Setting the state to STALE");
                this.shouldBeDisabled = false;
                this.connectorThread.state.set(NetChannel.STALE);
            }
        }
    }

    public void disable() {
        logger.trace("Thread <{}>::disable", Thread.currentThread().getId());
        synchronized (this.syncObj) {
            if (this.isEnabled) {
                this.isEnabled = false;
                logger.warn("::disable - Setting the state to DISABLED");
                this.shouldBeDisabled = true;
                this.connectorThread.state.set(NetChannel.DISABLED);

                // this.connectorThread.stop();
            }
        }
    }

    public boolean close() {
        return false;
    }

    public String toString() {
        return new StringBuilder()
                .append("channel=[").append(this.name)
                .toString();
    }

    @Override
    public void linkUp(String name) {
        this.connectorThread.state.linkUp();
    }

    @Override
    public void linkDown(String name) {
        this.connectorThread.state.linkDown();
    }

    /**
     * forces a reconnection.
     */
    public void reset() {
        logger.trace("Thread <{}>::reset", Thread.currentThread().getId());
        logger.trace("connector: {} sender: {} receiver: {}",
                new Object[] {
                        this.connectorThread.showState(),
                        (this.mSender == null ? "none" : this.mSender.getSenderState()),
                        (this.mReceiver == null ? "none" : this.mReceiver.getReceiverState())
                });

        synchronized (this.syncObj) {
            if (!this.connectorThread.isAlive()) {
                this.connectorThread = new ConnectorThread(this);
                this.connectorThread.start();
            }

            this.connectorThread.reset();
        }
    }

    private void statusChange()
    {
        int senderState = (mSender != null) ? mSender.getSenderState() : INetChannel.PENDING;
        int receiverState = (mReceiver != null) ? mReceiver.getReceiverState()
                : INetChannel.PENDING;

        try {
            mChannelManager.statusChange(this,
                    this.connectorThread.state.value,
                    senderState,
                    receiverState);
        } catch (Exception ex) {
            logger.error("Exception thrown in statusChange()", ex);
        }
    }

    private void setSecurityObject(ISecurityObject iSecurityObject)
    {
        mSecurityObject.set(iSecurityObject);
    }

    private ISecurityObject getSecurityObject()
    {
        return mSecurityObject.get();
    }

    private void setIsAuthorized(boolean iValue)
    {
        logger.trace("In setIsAuthorized(). value={}", iValue);

        mIsAuthorized.set(iValue);
    }

    public boolean getIsAuthorized()
    {
        return mIsAuthorized.get();
    }

    public void authorizationSucceeded(AmmoGatewayMessage agm)
    {
        setIsAuthorized(true);
        mSenderQueue.markAsAuthorized();

        // Tell the AmmoService that we're authorized and have it
        // notify the apps.
        mChannelManager.authorizationSucceeded(this, agm);
    }

    public void authorizationFailed()
    {
        // Disconnect the channel.
        reset();
    }

    // Called by ReceiverThread to send an incoming message to the
    // appropriate destination.
    private boolean deliverMessage(AmmoGatewayMessage agm)
    {
        logger.debug("deliverMessage() {} ", agm);

        boolean result;
        if (mIsAuthorized.get())
        {
            logger.trace(" delivering to channel manager");
            result = mChannelManager.deliver(agm);
        }
        else
        {
            logger.trace(" delivering to security object");
            result = getSecurityObject().deliverMessage(agm);
        }
        return result;
    }

    /**
     * Called by the SenderThread. This exists primarily to make a place to add
     * instrumentation. Also, follows the delegation pattern.
     */
    private boolean ackToHandler(INetworkService.OnSendMessageHandler handler,
            DisposalState status)
    {
        logger.trace("ack to handler {}", status, new Exception("BAD CHECK"));
        return handler.ack(this.name, status);
    }

    public boolean mockLinkSwitch;

    // Called by the ConnectorThread.
    public boolean isAnyLinkUp()
    {
        return mockLinkSwitch;
    }

    @SuppressWarnings("unused")
    private final AtomicLong mTimeOfLastGoodRead = new AtomicLong(0);

    // Heartbeat-related members.
    private final long mHeartbeatInterval = 10 * 1000; // ms
    private final AtomicLong mNextHeartbeatTime = new AtomicLong(0);

    public final MockNetworkStack mockNetworkStack;

    /**
     * Send a heartbeat packet to the gateway if enough time has elapsed.
     * <p>
     * Note: the way this currently works, the heartbeat can only be sent in
     * intervals that are multiples of the burp time. This may change later if I
     * can eliminate some of the wait()s.
     */
    @SuppressWarnings("unused")
    private void sendHeartbeatIfNeeded()
    {
        logger.debug("In sendHeartbeatIfNeeded().");

        long nowInMillis = System.currentTimeMillis();
        if (nowInMillis < mNextHeartbeatTime.get())
            return;

        logger.trace("Sending a heartbeat. t={}", nowInMillis);

        final AmmoMessages.MessageWrapper.Builder mw = AmmoMessages.MessageWrapper.newBuilder();
        mw.setType(AmmoMessages.MessageWrapper.MessageType.HEARTBEAT);
        mw.setMessagePriority(AmmoGatewayMessage.PriorityLevel.FLASH.v);

        final AmmoMessages.Heartbeat.Builder message = AmmoMessages.Heartbeat.newBuilder();
        message.setSequenceNumber(nowInMillis); // Just for testing

        mw.setHeartbeat(message);

        final AmmoGatewayMessage.Builder agmb = AmmoGatewayMessage.newBuilder(mw, null);
        agmb.isGateway(true);
        sendRequest(agmb.build());

        mNextHeartbeatTime.set(nowInMillis + mHeartbeatInterval);
    }

    /**
     * manages the connection. enable or disable expresses the operator intent.
     * There is no reason to run the thread unless the channel is enabled. Any
     * of the properties of the channel
     */
    private class ConnectorThread extends Thread {
        private final Logger logger = LoggerFactory.getLogger("mock.channel.connector");

        private MockChannel parent;
        private final State state;

        private AtomicBoolean mIsConnected;

        public void statusChange()
        {
            parent.statusChange();
        }

        /**
         * Called by the sender and receiver when they have an exception on the
         * socket. We only want to call reset() once, so we use an AtomicBoolean
         * to keep track of whether we need to call it.
         */
        public void socketOperationFailed()
        {
            if (mIsConnected.compareAndSet(true, false))
                state.reset();
        }

        private ConnectorThread(MockChannel parent) {
            super(new StringBuilder("Mock-Connect-").append(Thread.activeCount()).toString());
            logger.trace("Thread <{}>ConnectorThread::<constructor>", Thread.currentThread()
                    .getId());
            this.parent = parent;
            this.state = new State();
            mIsConnected = new AtomicBoolean(false);
        }

        private class State {
            private int value;
            private int actual;

            private long attempt; // used to uniquely name the connection

            public State() {
                this.value = STALE;
                this.attempt = Long.MIN_VALUE;
            }

            public synchronized void linkUp() {
                this.notifyAll();
            }

            public synchronized void linkDown() {
                this.reset();
            }

            public synchronized void set(int state) {
                logger.trace("Thread <{}>State::set",
                        Thread.currentThread().getId());
                if (state == STALE) {
                    this.reset();
                } else {
                    this.value = state;
                    this.notifyAll();
                }
            }

            /**
             * changes the state as requested unless the current state is
             * disabled.
             * 
             * @param state
             * @return false if disabled; true otherwise
             */
            public synchronized boolean setUnlessDisabled(int state) {
                logger.trace("Thread <{}>State::setUnlessDisabled",
                        Thread.currentThread().getId());
                if (state == DISABLED)
                    return false;
                this.set(state);
                return true;
            }

            public synchronized int get() {
                return this.value;
            }

            public synchronized boolean isConnected() {
                return this.value == INetChannel.CONNECTED;
            }

            public synchronized boolean isDisabled() {
                return this.value == INetChannel.DISABLED;
            }

            /**
             * Previously this method would only set the state to stale if the
             * current state were CONNECTED. It may be important to return to
             * STALE from other states as well. For example during a failed link
             * attempt. Therefore if the attempt value matches then reset to
             * STALE This also causes a reset to reliably perform a notify.
             * 
             * @param attempt value (an increasing integer)
             * @return
             */
            public synchronized boolean failure(long attempt) {
                if (attempt != this.attempt)
                    return true;
                return this.reset();
            }

            public synchronized boolean failureUnlessDisabled(long attempt) {
                if (this.value == INetChannel.DISABLED)
                    return false;
                return this.failure(attempt);
            }

            public synchronized boolean reset() {
                attempt++;
                this.value = STALE;
                this.notifyAll();
                return true;
            }

            public String showState() {
                if (this.value == this.actual)
                    return NetChannel.showState(this.value);
                else
                    return NetChannel.showState(this.actual) + "->"
                            + NetChannel.showState(this.value);
            }
        }

        public boolean isConnected() {
            return this.state.isConnected();
        }

        public long getAttempt() {
            return this.state.attempt;
        }

        public String showState() {
            return this.state.showState();
        }

        /**
         * reset forces the channel closed if open.
         */
        public void reset() {
            this.state.failure(this.state.attempt);
        }

        /**
         * A value machine based. Most of the time this machine will be in a
         * CONNECTED value. In that CONNECTED value the machine wait for the
         * connection value to change or for an interrupt indicating that the
         * thread is being shut down. The value machine takes care of the
         * following constraints: We don't need to reconnect unless. 1) the
         * connection has been lost 2) the connection has been marked stale 3)
         * the connection is enabled. 4) an explicit reconnection was requested
         * 
         * @return
         */
        @Override
        public void run() {
            try {
                logger.trace("Thread <{}>ConnectorThread::run", Thread.currentThread().getId());
                MAINTAIN_CONNECTION: while (true) {
                    logger.trace("connector state: {}", this.showState());

                    if (this.parent.shouldBeDisabled)
                        this.state.set(NetChannel.DISABLED);
                    switch (this.state.get()) {
                        case NetChannel.DISABLED:
                            try {
                                synchronized (this.state) {
                                    logger.trace("this.state.get() = {}", this.state.get());
                                    this.parent.statusChange();
                                    disconnect();

                                    // Wait for a link interface.
                                    while (this.state.isDisabled())
                                    {
                                        logger.trace("Looping in Disabled");
                                        this.state.wait(BURP_TIME);
                                    }
                                }
                            } catch (InterruptedException ex) {
                                logger.warn("connection intentionally disabled {}", this.state);
                                this.state.setUnlessDisabled(NetChannel.STALE);
                                break MAINTAIN_CONNECTION;
                            }
                            break;
                        case NetChannel.STALE:
                            disconnect();
                            this.state.setUnlessDisabled(NetChannel.LINK_WAIT);
                            break;

                        case NetChannel.LINK_WAIT:
                            this.parent.statusChange();
                            try {
                                synchronized (this.state) {
                                    while (!parent.isAnyLinkUp() && !this.state.isDisabled()) {
                                        // wait for a link interface
                                        this.state.wait(BURP_TIME);
                                    }
                                    this.state.setUnlessDisabled(NetChannel.DISCONNECTED);
                                }
                            } catch (InterruptedException ex) {
                                logger.warn("connection intentionally disabled {}", this.state);
                                this.state.setUnlessDisabled(NetChannel.STALE);
                                break MAINTAIN_CONNECTION;
                            }
                            this.parent.statusChange();
                            // or else wait for link to come up, triggered
                            // through broadcast receiver
                            break;

                        case NetChannel.DISCONNECTED:
                            this.parent.statusChange();
                            if (!this.connect()) {
                                this.state.setUnlessDisabled(NetChannel.CONNECTING);
                            } else {
                                this.state.setUnlessDisabled(NetChannel.CONNECTED);
                            }
                            break;

                        case NetChannel.CONNECTING: // keep trying
                            try {
                                this.parent.statusChange();
                                long attempt = this.getAttempt();
                                synchronized (this.state) {
                                    this.state.wait(NetChannel.CONNECTION_RETRY_DELAY);
                                    if (this.connect()) {
                                        this.state.setUnlessDisabled(NetChannel.CONNECTED);
                                    } else {
                                        this.state.failureUnlessDisabled(attempt);
                                    }
                                }
                                this.parent.statusChange();
                            } catch (InterruptedException ex) {
                                logger.warn(
                                        "sleep interrupted - intentional disable, exiting thread...",
                                        ex);
                                this.reset();
                                break MAINTAIN_CONNECTION;
                            }
                            break;

                        case NetChannel.CONNECTED: {
                            this.parent.statusChange();
                            try {
                                synchronized (this.state) {
                                    while (this.isConnected()) // this is
                                                               // IMPORTANT
                                                               // don't remove
                                                               // it.
                                    {
                                        // if ( HEARTBEAT_ENABLED )
                                        // parent.sendHeartbeatIfNeeded();

                                        // wait for somebody to change the
                                        // connection status
                                        this.state.wait(BURP_TIME);
                                    }
                                }
                            } catch (InterruptedException ex) {
                                logger.warn("connection intentionally disabled {}", this.state);
                                this.state.setUnlessDisabled(NetChannel.STALE);
                                break MAINTAIN_CONNECTION;
                            }
                            this.parent.statusChange();
                        }
                            break;
                        default:
                            try {
                                long attempt = this.getAttempt();
                                this.parent.statusChange();
                                synchronized (this.state) {
                                    this.state.wait(NetChannel.CONNECTION_RETRY_DELAY);
                                    this.state.failureUnlessDisabled(attempt);
                                }
                                this.parent.statusChange();
                            } catch (InterruptedException ex) {
                                logger.warn(
                                        "sleep interrupted - intentional disable, exiting thread...",
                                        ex);
                                this.reset();
                                break MAINTAIN_CONNECTION;
                            }
                    }
                }

            } catch (Exception ex) {
                logger.error("failed to run multicast", ex);
                this.state.set(NetChannel.EXCEPTION);
            }

            logger.error("channel closing");
        }

        private boolean connect()
        {
            logger.trace("Thread <{}>ConnectorThread::connect",
                    Thread.currentThread().getId());

            mIsConnected.set(true);

            // Create the security object. This must be done before
            // the ReceiverThread is created in case we receive a
            // message before the SecurityObject is ready to have it
            // delivered.
            if (parent.getSecurityObject() != null)
                logger.error("Tried to create SecurityObject when we already had one.");

            // Create the sending thread.
            if (parent.mSender != null)
                logger.error("Tried to create Sender when we already had one.");
            parent.mSender = new SenderThread(this,
                    parent,
                    parent.mSenderQueue,
                    parent.mockNetworkStack);
            parent.mSender.start();

            // Create the receiving thread.
            if (parent.mReceiver != null)
                logger.error("Tried to create Receiver when we already had one.");
            parent.mReceiver = new ReceiverThread(this,
                    parent,
                    parent.mockNetworkStack);
            parent.mReceiver.start();

            /**
             * FIXME: don't pass in the result of buildAuthenticationRequest().
             * This is just a temporary hack. <code>
             parent.getSecurityObject().authorize(
             mChannelManager.buildAuthenticationRequest());
             </code>
             */
            setIsAuthorized(true);
            mSenderQueue.markAsAuthorized();

            return true;
        }

        private boolean disconnect()
        {
            logger.trace("Thread <{}>ConnectorThread::disconnect",
                    Thread.currentThread().getId());
            try
            {
                mIsConnected.set(false);

                if (mSender != null) {
                    logger.debug("interrupting SenderThread");
                    mSender.interrupt();
                }
                if (mReceiver != null) {
                    logger.debug("interrupting ReceiverThread");
                    mReceiver.interrupt();
                }

                // We need to wait here until the threads have stopped.
                try {
                    logger.debug("calling join() on SenderThread");
                    if (mSender != null) {
                        mSender.join();
                    }
                    logger.debug("calling join() on ReceiverThread");
                    if (mReceiver != null) {
                        mReceiver.join();
                    }
                } catch (InterruptedException ex) {
                    logger.info("socket i/o exception", ex);
                    // Do this here, too, since if we exited early because
                    // of an exception, we want to make sure that we're in
                    // an unauthorized state.
                    mSenderQueue.reset();
                    setIsAuthorized(false);
                    return false;
                }

                parent.mSender = null;
                parent.mReceiver = null;

                logger.debug("resetting SenderQueue");
                mSenderQueue.reset();

                setIsAuthorized(false);

                parent.setSecurityObject(null);
            } catch (Exception e)
            {
                logger.error("Caught General Exception", e);
                // Do this here, too, since if we exited early because
                // of an exception, we want to make sure that we're in
                // an unauthorized state.
                setIsAuthorized(false);
                return false;
            }
            logger.debug("returning after successful disconnect().");
            return true;
        }
    }

    /**
     * do your best to send the message. This makes use of the blocking "put"
     * call. A proper producer-consumer should use put or add and not offer.
     * "put" is blocking call. If this were on the UI thread then offer would be
     * used.
     * 
     * @param agm AmmoGatewayMessage
     * @return
     */
    public DisposalState sendRequest(AmmoGatewayMessage agm)
    {
        return mSenderQueue.putFromDistributor(agm);
    }

    public void putFromSecurityObject(AmmoGatewayMessage agm)
    {
        mSenderQueue.putFromSecurityObject(agm);
    }

    public void finishedPuttingFromSecurityObject()
    {
        mSenderQueue.finishedPuttingFromSecurityObject();
    }

    // /////////////////////////////////////////////////////////////////////////
    //
    class SenderQueue
    {
        public SenderQueue(MockChannel iChannel)
        {
            mChannel = iChannel;

            setIsAuthorized(false);
            // mDistQueue = new LinkedBlockingQueue<AmmoGatewayMessage>( 20 );
            mDistQueue = new PriorityBlockingQueue<AmmoGatewayMessage>(20);
            mAuthQueue = new LinkedList<AmmoGatewayMessage>();
        }

        // In the new design, aren't we supposed to let the
        // AmmoService know if the outgoing queue is full or not?
        public DisposalState putFromDistributor(AmmoGatewayMessage iMessage)
        {
            logger.info("putFromDistributor() in ChannelQueue size={}", mDistQueue.size());
            try {
                if (!mDistQueue.offer(iMessage, 1, TimeUnit.SECONDS)) {
                    logger.warn("multicast channel not taking messages {} {}",
                            DisposalState.BUSY, mDistQueue.size());
                    return DisposalState.BUSY;
                }
            } catch (InterruptedException e) {
                return DisposalState.REJECTED;
            }
            return DisposalState.QUEUED;
        }

        public synchronized void putFromSecurityObject(AmmoGatewayMessage iMessage)
        {
            logger.trace("putFromSecurityObject()");
            mAuthQueue.offer(iMessage);
        }

        public synchronized void finishedPuttingFromSecurityObject()
        {
            logger.trace("finishedPuttingFromSecurityObject()");
            notifyAll();
        }

        // This is called when the SecurityObject has successfully
        // authorized the channel.
        public synchronized void markAsAuthorized()
        {
            logger.trace("Marking channel as authorized");
            notifyAll();
        }

        /**
         * Condition wait for the some request to the channel. An initial
         * request cannot be processed until the channel has authenticated. This
         * is where the authorized SenderThread blocks when taking a
         * distribution request. If not yet authorized then return the first
         * item in the authentication queue, removing that item from its queue.
         * 
         * @return
         * @throws InterruptedException
         */
        public synchronized AmmoGatewayMessage take() throws InterruptedException
        {
            logger.info("taking from SenderQueue - queue size: {}", mDistQueue.size());
            while (!mChannel.getIsAuthorized() && mAuthQueue.size() < 1) {
                logger.trace("wait()ing in SenderQueue");
                wait();
            }
            if (mChannel.getIsAuthorized()) {
                return mDistQueue.poll(5, TimeUnit.SECONDS);
            }
            // must be the mAuthQueue.size() > 0
            return mAuthQueue.remove();
        }

        /**
         * Clear out the distribution/send queue. Tell the distributor that we
         * couldn't send these packets.
         */
        public synchronized void reset()
        {
            logger.info("reseting the SenderQueue");
            while (! mDistQueue.isEmpty()) {
                final AmmoGatewayMessage msg = mDistQueue.poll();
                logger.info("rejecting msg=[{}]", msg.toString());
                if (msg.handler == null)
                    continue;
                mChannel.ackToHandler(msg.handler, DisposalState.REJECTED);
            }

            setIsAuthorized(false);
        }

        private BlockingQueue<AmmoGatewayMessage> mDistQueue;
        private LinkedList<AmmoGatewayMessage> mAuthQueue;
        private MockChannel mChannel;
    }

    /**
     * Extracts things from the send queue, processes them
     * putting the output into the mock link queue.
     */
    class SenderThread extends Thread
    {
        private final Logger logger = LoggerFactory.getLogger("mock.channel.sender");

        private int mState = INetChannel.TAKING;
        private ConnectorThread mParent;
        private MockChannel mChannel;
        private SenderQueue mQueue;
        private MockNetworkStack mNetworkStack;

        public SenderThread(ConnectorThread iParent,
                MockChannel iChannel,
                SenderQueue iQueue,
                MockNetworkStack iNetworkStack)
        {
            super(new StringBuilder("Mock-Sender-").append(Thread.activeCount()).toString());
            mParent = iParent;
            mChannel = iChannel;
            mQueue = iQueue;
            mNetworkStack = iNetworkStack;
        }

        /**
         * Block on reading from the queue until we get a message to send. Then
         * send it on the channel. Upon getting a channel error, notify our
         * parent and go into an error state.
         */
        @Override
        public void run()
        {
            logger.trace("Thread <{}>::run()", Thread.currentThread().getId());

            while (mState != INetChannel.INTERRUPTED)
            {
                final AmmoGatewayMessage msg;
                try
                {
                    setSenderState(INetChannel.TAKING);
                    msg = mQueue.take(); // The main blocking call

                } catch (InterruptedException ex)
                {
                    logger.error("interrupted taking messages from send queue", ex);
                    setSenderState(INetChannel.INTERRUPTED);
                    mParent.socketOperationFailed();
                    continue;
                } catch (Exception ex)
                {
                    logger.error("sender threw exception while take()ing", ex);
                    setSenderState(INetChannel.INTERRUPTED);
                    mParent.socketOperationFailed();
                    continue;
                }

                try
                {
                    final ByteBuffer buf = msg.serialize(endian, AmmoGatewayMessage.VERSION_1_FULL,
                            (byte) 0);
                    setSenderState(INetChannel.SENDING);

                    mNetworkStack.send(buf);

                    // update send messages ...
                    mMessagesSent.incrementAndGet();

                    // legitimately sent to gateway.
                    if (msg.handler != null)
                        mChannel.ackToHandler(msg.handler, DisposalState.SENT);
                } catch (SocketException ex)
                {
                    logger.debug("sender caught SocketException");
                    if (msg.handler != null) {
                        mChannel.ackToHandler(msg.handler, DisposalState.REJECTED);
                    }
                    setSenderState(INetChannel.INTERRUPTED);
                    mParent.socketOperationFailed();
                    continue;
                } catch (InterruptedException ex)
                {
                    logger.warn("sender interrupted (test probably finished)");
                    break;
                } catch (Exception ex) {
                    logger.trace("sender threw exception");
                    logger.warn("sender threw exception", ex);
                    if (msg == null) {
                        logger.error("message is null");
                        return;
                    }
                    if (msg.handler != null) {
                        mChannel.ackToHandler(msg.handler, DisposalState.BAD);
                    }
                    setSenderState(INetChannel.INTERRUPTED);
                    mParent.socketOperationFailed();
                    continue;
                }
            }
        }

        private void setSenderState(int iState)
        {
            synchronized (this)
            {
                mState = iState;
            }
            mParent.statusChange();
        }

        public synchronized int getSenderState() {
            return mState;
        }

    }

    // /////////////////////////////////////////////////////////////////////////
    //
    class ReceiverThread extends Thread
    {
        private final Logger logger = LoggerFactory.getLogger("mock.channel.receiver");

        private int mState = INetChannel.TAKING;
        private ConnectorThread mParent;
        private MockChannel mDestination;
        private MockNetworkStack mNetworkStack;

        public ReceiverThread(ConnectorThread iParent,
                MockChannel iDestination,
                MockNetworkStack iNetworkStack)
        {
            super(new StringBuilder("Mock-Receiver-").append(Thread.activeCount()).toString());
            mParent = iParent;
            mDestination = iDestination;
            mNetworkStack = iNetworkStack;
        }

        /**
         * Block on reading from the MockNetworkStack until we get some data. If
         * we get an error, notify our parent and go into an error state.
         * <p>
         */
        @Override
        public void run()
        {
            logger.trace("Thread <{}>::run()", Thread.currentThread().getId());

            while (getReceiverState() != INetChannel.INTERRUPTED)
            {
                try {
                    setReceiverState(INetChannel.START);

                    final ByteBuffer buf = mNetworkStack.receive();

                    // update received count ....
                    mMessagesReceived.incrementAndGet();

                    buf.order(endian);

                    // wrap() creates a buffer that is ready to be drained,
                    // so there is no need to flip() it.
                    AmmoGatewayMessage.Builder agmb = AmmoGatewayMessage.extractHeader(buf);

                    if (agmb == null)
                    {
                        logger.error("Deserialization failure. Discarded invalid packet.");
                        continue;
                    }

                    // extract the payload
                    byte[] payload = new byte[agmb.size()];
                    buf.get(payload, 0, buf.remaining());

                    AmmoGatewayMessage agm = agmb.payload(payload).channel(this.mDestination)
                            .build();
                    setReceiverState(INetChannel.DELIVER);
                    mDestination.deliverMessage(agm);
                    logger.trace("received a message {}", payload.length);
                } catch (ClosedChannelException ex)
                {
                    logger.warn("receiver threw ClosedChannelException", ex);
                    setReceiverState(INetChannel.INTERRUPTED);
                    mParent.socketOperationFailed();
                } catch (Exception ex)
                {
                    logger.warn("receiver threw exception", ex);
                    setReceiverState(INetChannel.INTERRUPTED);
                    mParent.socketOperationFailed();
                }
            }
        }

        private void setReceiverState(int iState)
        {
            synchronized (this)
            {
                mState = iState;
            }
            mParent.statusChange();
        }

        public synchronized int getReceiverState() {
            return mState;
        }
    }

    // ********** UTILITY METHODS ****************

    // A routine to get all local IP addresses
    //
    public List<InetAddress> getLocalIpAddresses()
    {
        List<InetAddress> addresses = new ArrayList<InetAddress>();
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                        .hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    addresses.add(inetAddress);
                    logger.error("address: {}", inetAddress);
                }
            }
        } catch (SocketException ex)
        {
            logger.error("opening socket", ex);
        }

        return addresses;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public void init(Context context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void toLog(String context) {

    }
}
