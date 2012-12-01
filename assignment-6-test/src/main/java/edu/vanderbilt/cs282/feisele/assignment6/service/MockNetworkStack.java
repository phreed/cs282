package edu.vanderbilt.cs282.feisele.assignment6.service;

import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class stands in place of the various mechanisms used by Channels
 * generally. The network is emulated by queues of ByteBuffer objects.
 */
public class MockNetworkStack extends Socket {
    public static final Logger logger = LoggerFactory.getLogger("mock.net");
    
    /**
     * Use these queues to imitate the network stack
     * <dl>
     * <dt>input</dt><dd>going onto the network</dd>
     * <dt>output</dt><dd>coming from the network</dd>
     * </dl>
     */
    final private BlockingQueue<ByteBuffer> input;
    final private BlockingQueue<ByteBuffer> output;

    final public AtomicBoolean throwClosedChannelException;
    final public AtomicBoolean throwInterruptedException;
    final public AtomicBoolean throwSocketException;
    final public AtomicBoolean throwException;

    public MockNetworkStack() {
        this.input = new LinkedBlockingQueue<ByteBuffer>();
        this.output = new LinkedBlockingQueue<ByteBuffer>();

        this.throwInterruptedException = new AtomicBoolean(false);
        this.throwClosedChannelException = new AtomicBoolean(false);
        this.throwSocketException = new AtomicBoolean(false);
        this.throwException = new AtomicBoolean(false);
        
        logger.info("mock network stack created ");
    }

    /**
     * This method works in conjunction with receive().
     */
    public void putReceivable(ByteBuffer buf) {
        buf.flip();
        this.output.offer(buf);
        logger.info("put of [{}] receivable [{}]", this.output.size(), buf);
    }

    /**
     * This method is called by the MockChannel. It returns the next item in the
     * Mock Network.
     * 
     * @return
     * @throws InterruptedException
     * @throws ClosedChannelException
     * @throws Exception
     */
    public ByteBuffer receive() throws InterruptedException, ClosedChannelException, Exception {
        logger.info("receive queue size {}", this.output.size());
        if (this.throwInterruptedException.get()) {
            throw new InterruptedException();
        }
        if (this.throwClosedChannelException.get()) {
            throw new ClosedChannelException();
        }
        if (this.throwException.get()) {
            throw new Exception("mock socket exception");
        }
        final ByteBuffer result = this.output.poll(50, TimeUnit.SECONDS);
        logger.error("receive [{}]", result);
        return result;
    }

    /**
     * This method works in conjunction with send().
     */
    public ByteBuffer getSent() {
        try {
            final ByteBuffer result = this.input.poll(5, TimeUnit.SECONDS);
            logger.info("send queue size {}", this.input.size());
            return result;
        } catch (InterruptedException ex) {
            logger.error("unsendable ", ex);
            return null;
        }
    }

    public void send(ByteBuffer buf) throws SocketException, Exception {
        if (this.throwSocketException.get()) {
            throw new SocketException("mock socket exception");
        }
        if (this.throwException.get()) {
            throw new Exception("mock socket exception");
        }

        logger.info("send size [{}] [{}]",buf.position(), buf.array());
        this.input.offer(buf);
        logger.info("queue size [{}]", this.input.size());
    }

    private final static Charset charset = Charset.forName("UTF-8");
    @SuppressWarnings("unused")
    private final static CharsetEncoder encoder = charset.newEncoder();
    private final static CharsetDecoder decoder = charset.newDecoder();

    /**
     * Decode the byte buffer into a string.
     * 
     * @param buf
     * @return
     */
    public static String asString(ByteBuffer buf) {
        if (buf == null) return "<null>";
        final int pos = buf.position();
        decoder.reset();
        final CharBuffer cbuf = CharBuffer.allocate(buf.remaining());
        decoder.decode(buf, cbuf, true);
        decoder.flush(cbuf);
        final String result = cbuf.toString();
        buf.position(pos);
        return result;
    }

}
