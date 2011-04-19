/*****************************************************************************
 *   Product of NIST/ITL Advanced Networking Technologies Division (ANTD).    *
 *****************************************************************************/

package gov.nist.javax.sip.stack;
import java.net.*;
import gov.nist.javax.sip.*;
import gov.nist.core.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.parser.*;
import gov.nist.javax.sip.message.*;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.String;
import java.text.ParseException;

/**
 * This is the UDP Message handler that gets created when a UDP message
 * needs to be processed. The message is processed by creating a String
 * Message parser and invoking it on the message read from the UPD socket.
 * The parsed structure is handed off via a SIP stack request for further
 * processing. This stack structure isolates the message handling logic
 * from the mechanics of sending and recieving messages (which could
 * be either udp or tcp.
 *
 * @see gov.nist.javax.sip.parser.StringMsgParser
 * @see gov.nist.javax.sip.stack.ServerRequestInterface
 * @author <A href=mailto:mranga@nist.gov> M. Ranganathan </A>
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * Acknowledgement: Kim Kirby of Keyvoice suggested that duplicate checking
 * should be added to the stack (later removed).
 * Lamine Brahimi suggested a single threaded behavior flag
 * be added to this. Niklas Uhrberg suggested that thread pooling support
 * be added to this for performance and resource management.
 * Peter Parnes found a bug with
 * this code that was sending it into an infinite loop when a bad incoming
 * message was parsed.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.28 $ $Date: 2005/02/24 16:14:30 $
 */
public class UDPMessageChannel
extends MessageChannel
implements ParseExceptionListener, Runnable {
    
    /**
     * SIP Stack structure for this channel.
     */
    protected SIPMessageStack stack;
    
    /**
     * The parser we are using for messages received from this channel.
     */
    protected StringMsgParser myParser;
    
    /**
     * Where we got the stuff from
     */
    private InetAddress peerAddress;
    
    private String myAddress;

    //@@@ hagai
    private int peerPacketSourcePort;
    private InetAddress peerPacketSourceAddress;
    
    /**
     * Reciever port -- port of the destination.
     */
    private int peerPort;
    
    /**
     * Protocol to use when talking to receiver (i.e. when sending replies).
     */
    private String peerProtocol;
    
    protected int myPort;
    
    private byte[] msgBytes;
    
    
    private DatagramPacket incomingPacket;
    
    private long receptionTime;
    
    /**
     * Constructor - takes a datagram packet and a stack structure
     * Extracts the address of the other from the datagram packet and
     * stashes away the pointer to the passed stack structure.
     * @param packet is the UDP Packet that contains the request.
     * @param stack is the shared SIPStack structure
     * @param notifier Channel notifier (not very useful for UDP).
     */
    protected UDPMessageChannel(
    SIPMessageStack stack,
    UDPMessageProcessor messageProcessor) {
        super.messageProcessor = messageProcessor;
        this.stack = stack;
        Thread mythread = new Thread(this);

        this.myAddress = stack.getHostAddress();
        this.myPort = messageProcessor.getPort();

        mythread.setName("UDPMessageChannelThread");
	mythread.setDaemon(true);
        mythread.start();
        
    }
    
    /**
     * Constructor. We create one of these in order to process an incoming
     * message.
     *
     * @param stack is the SIP stack.
     * @param notifier is the channel notifier (not particularly relevant here).
     * @param messageProcesor is the creating message processor.
     * @param packet is the incoming datagram packet.
     */
    protected UDPMessageChannel(
    SIPMessageStack stack,
    UDPMessageProcessor messageProcessor,
    DatagramPacket packet) {
        
        this.incomingPacket = packet;
        super.messageProcessor = messageProcessor;
        this.stack = stack;
        this.myAddress = stack.getHostAddress();
        this.myPort = messageProcessor.getPort();
        Thread mythread = new Thread(this);
	mythread.setDaemon(true);
        mythread.start();
        
    }
    
    /**
     * Constructor. We create one of these when we send out a message.
     * @param targetAddr INET address of the place where we want to send
     *		messages.
     * @param port target port (where we want to send the message).
     * @param stack our SIP Stack.
     */
    protected UDPMessageChannel(
    InetAddress targetAddr,
    int port,
    SIPMessageStack sipStack,
    UDPMessageProcessor messageProcessor) {
        peerAddress = targetAddr;
        peerPort = port;
        peerProtocol = "UDP";
        super.messageProcessor = messageProcessor;
        this.myAddress = sipStack.getHostAddress();
        this.myPort = messageProcessor.getPort();
        this.stack = sipStack;
        if (LogWriter.needsLogging) {
            this.stack.logWriter.logMessage(
            "Creating message channel "
            + targetAddr.getHostAddress()
            + "/"
            + port);
        }
    }
    
    /**
     * Run method specified by runnnable.
     */
    public void run() {
        
        while (true) {
            // Create a new string message parser to parse the list of messages.
            // This is a huge performance hit -- need to optimize by pre-create
            // parser when one is needed....
            if (myParser == null) {
                myParser = new StringMsgParser();
                myParser.setParseExceptionListener(this);
            }
            // messages that we write out to him.
            DatagramPacket packet;
            
            if (stack.threadPoolSize != -1) {
                synchronized (
                ((UDPMessageProcessor) messageProcessor).messageQueue) {
                    while (((UDPMessageProcessor) messageProcessor)
                    .messageQueue
                    .isEmpty()) {
                        // Check to see if we need to exit.
                        if (!((UDPMessageProcessor) messageProcessor)
                        .isRunning)
                            return;
                        try {
                            ((UDPMessageProcessor) messageProcessor).messageQueue.wait();
                        } catch (InterruptedException ex) {
                            if (!((UDPMessageProcessor) messageProcessor)
                            .isRunning)
                                return;
                        }
                    }
                    packet =
                    (DatagramPacket)
                    ((UDPMessageProcessor) messageProcessor)
                    .messageQueue
                    .removeFirst();
                    
                }
                this.incomingPacket = packet;
            } else {
                packet = this.incomingPacket;
            }
            
            this.peerAddress = packet.getAddress();
            int packetLength = packet.getLength();
            // Read bytes and put it in a eueue.
            byte[] bytes = packet.getData();
            byte[] msgBytes = new byte[packetLength];
            System.arraycopy(bytes, 0, msgBytes, 0, packetLength);
            
            // Do debug logging.
            if (LogWriter.needsLogging) {
                this.stack.logWriter.logMessage(
                "UDPMessageChannel: peerAddress = "
                + peerAddress.getHostAddress()
                + "/"
                + packet.getPort());
                this.stack.logWriter.logMessage("Length = " + packetLength);
                String msgString = new String(msgBytes, 0, packetLength);
                this.stack.logWriter.logMessage(msgString);
            }
            
            SIPMessage[] sipMessages = null;
            SIPMessage sipMessage = null;
            try {
                
                this.receptionTime = System.currentTimeMillis();
                sipMessage = myParser.parseSIPMessage(msgBytes);
                myParser = null;
            } catch (ParseException ex) {
                myParser = null; // let go of the parser reference.
                if (LogWriter.needsLogging) {
                    this.stack.logWriter.logMessage(
                    "Rejecting message !  " + new String(msgBytes));
                    this.stack.logWriter.logMessage(
                    "error message " + ex.getMessage());
                    this.stack.logWriter.logException(ex);
                }
                stack.logBadMessage(new String(msgBytes));
                if (stack.threadPoolSize == -1)
                    return;
                else
                    continue;
            }
            // No parse exception but null message - reject it and
            // march on (or return). Bug report from Peter Parnes.
            // exit this message processor if the message did not parse.
            
            if (sipMessage == null) {
                if (LogWriter.needsLogging) {
                    this.stack.logWriter.logMessage(
                    "Rejecting message !  " + new String(msgBytes));
                    this.stack.logWriter.logMessage("Null message parsed.");
                }
                if (stack.threadPoolSize == -1)
                    return;
                else
                    continue;
            }
            ViaList viaList = sipMessage.getViaHeaders();
            // Check for the required headers.
            if (sipMessage.getFrom() == null
            || //sipMessage.getFrom().getTag() == null  ||
            sipMessage.getTo()
            == null
            || sipMessage.getCallId() == null
            || sipMessage.getCSeq() == null
            || sipMessage.getViaHeaders() == null) {
                String badmsg = new String(msgBytes);
                if (LogWriter.needsLogging) {
                    this.stack.logWriter.logMessage("bad message " + badmsg);
                    this.stack.logWriter.logMessage(
                    ">>> Dropped Bad Msg "
                    + "From = "
                    + sipMessage.getFrom()
                    + "To = "
                    + sipMessage.getTo()
                    + "CallId = "
                    + sipMessage.getCallId()
                    + "CSeq = "
                    + sipMessage.getCSeq()
                    + "Via = "
                    + sipMessage.getViaHeaders());
                }
                
                stack.logBadMessage(badmsg);
                if (stack.threadPoolSize == -1)
                    return;
                else
                    continue;
            }
            // For a request first via header tells where the message
            // is coming from.
            // For response, just get the port from the packet.
            if (sipMessage instanceof SIPRequest) {
                Via v = (Via) viaList.first();
                if (v.hasPort()) {
                    if (sipMessage instanceof SIPRequest) {
                        this.peerPort = v.getPort();
                    }
                } else
                    this.peerPort = SIPMessageStack.DEFAULT_PORT;
                this.peerProtocol = v.getTransport();

                this.peerPacketSourceAddress = packet.getAddress();
                this.peerPacketSourcePort = packet.getPort();
                try {
                    this.peerAddress = packet.getAddress();
                    // Check to see if the received parameter matches
                    // the peer address and tag it appropriately.
                    // Bug fix by viswashanti.kadiyala@antepo.com
                    if (!v
                    .getSentBy()
                    .getInetAddress()
                    .equals(this.peerAddress)) {
                        v.setParameter(
                        Via.RECEIVED,
                        this.peerAddress.getHostName());
			//@@@hagai

                        v.setParameter(
                                Via.RPORT,
                                new Integer(this.peerPacketSourcePort).toString());
		    }
                    
                    // this.peerAddress = v.getSentBy().getInetAddress();
                } catch (java.net.UnknownHostException ex) {
                    // Could not resolve the sender address.
                    if (stack
                    .serverLog
                    .needsLogging(ServerLog.TRACE_MESSAGES)) {
                        this.stack.serverLog.logMessage(
                        sipMessage,
                        this.getViaHost() + ":" + this.getViaPort(),
                        stack.getHostAddress()
                        + ":"
                        + stack.getPort(this.getTransport()),
                        "Dropped -- "
                        + "Could not resolve VIA header address!",
                        false);
                    }
                    if (LogWriter.needsLogging) {
                        this.stack.logWriter.logMessage(
                        "Rejecting message -- "
                        + "could not resolve Via Address");
                    }
                    
                    continue;
                } catch (java.text.ParseException ex1) {
                    InternalErrorHandler.handleException(ex1);
                }
                
            }
            
            if (sipMessage instanceof SIPRequest) {
                SIPRequest sipRequest = (SIPRequest) sipMessage;
                
                // This is a request - process it.
                ServerRequestInterface sipServerRequest =
                stack.newSIPServerRequest(sipRequest, this);
                // Drop it if there is no request returned
                if (sipServerRequest == null) {
                    if (LogWriter.needsLogging) {
                        this.stack.logWriter.logMessage(
                        "Null request interface returned");
                    }
                    continue;
                }
                if (LogWriter.needsLogging)
                    this.stack.logWriter.logMessage(
                    "About to process "
                    + sipRequest.getFirstLine()
                    + "/"
                    + sipServerRequest);
                sipServerRequest.processRequest(sipRequest, this);
                if (LogWriter.needsLogging)
                    this.stack.logWriter.logMessage(
                    "Done processing "
                    + sipRequest.getFirstLine()
                    + "/"
                    + sipServerRequest);
                
                // So far so good -- we will commit this message if
                // all processing is OK.
                if (stack
                .serverLog
                .needsLogging(ServerLog.TRACE_MESSAGES)) {
                    if (sipServerRequest.getProcessingInfo() == null) {
                        this.stack.serverLog.logMessage(
                        sipMessage,
                        sipRequest.getViaHost()
                        + ":"
                        + sipRequest.getViaPort(),
                        stack.getHostAddress()
                        + ":"
                        + this.myPort,
                        false,
                        new Long(receptionTime).toString());
                    } else {
                        this.stack.serverLog.logMessage(
                        sipMessage,
                        sipRequest.getViaHost()
                        + ":"
                        + sipRequest.getViaPort(),
                        stack.getHostAddress()
                        + ":"
                        +  this.myPort,  //stack.getPort(this.getTransport()),
                        sipServerRequest.getProcessingInfo(),
                        false,
                        new Long(receptionTime).toString());
                    }
                }
            } else {
                // Handle a SIP Reply message.
                SIPResponse sipResponse = (SIPResponse) sipMessage;
                ServerResponseInterface sipServerResponse =
                stack.newSIPServerResponse(sipResponse, this);
                if (sipServerResponse != null) {
                    sipServerResponse.processResponse(sipResponse, this);
                    // Normal processing of message.
                } else {
                    if (LogWriter.needsLogging) {
                        this.stack.logWriter.logMessage(
                        "null sipServerResponse!");
                    }
                }
                
            }
            //((UDPMessageProcessor) messageProcessor).useCount--;
            if (stack.threadPoolSize == -1) {
                return;
            }
        }
    }
    
    /**
     * Implementation of the ParseExceptionListener interface.
     * @param ex Exception that is given to us by the parser.
     * @throws ParseException If we choose to reject the header or message.
     */
    public void handleException(
    ParseException ex,
    SIPMessage sipMessage,
    Class hdrClass,
    String header,
    String message)
    throws ParseException {
        if (LogWriter.needsLogging)
            this.stack.logWriter.logException(ex);
        // Log the bad message for later reference.
        if ((hdrClass!=null)&&(hdrClass.equals(From.class)
        || hdrClass.equals(To.class)
        || hdrClass.equals(CSeq.class)
        || hdrClass.equals(Via.class)
        || hdrClass.equals(CallID.class)
        || hdrClass.equals(RequestLine.class)
        || hdrClass.equals(StatusLine.class))) {
            stack.logBadMessage(message);
            throw ex;
        } else {
            sipMessage.addUnparsed(header);
        }
    }
    
    /**
     * Return a reply from a pre-constructed reply. This sends the message
     * back to the entity who caused us to create this channel in the
     * first place.
     * @param sipMessage Message string to send.
     * @throws IOException If there is a problem with sending the message.
     */
    public void sendMessage(SIPMessage sipMessage) throws IOException {
        if (LogWriter.needsLogging)
            this.stack.logWriter.logStackTrace();
        byte[] msg = sipMessage.encodeAsBytes();
        
      	long time = System.currentTimeMillis();
        
        sendMessage( msg, peerAddress, peerPort, peerProtocol, 
			sipMessage instanceof SIPRequest);

        if (stack.serverLog.needsLogging(ServerLog.TRACE_MESSAGES))
            logMessage(sipMessage, peerAddress, peerPort, time);
    }
    
    /**
     * Send a message to a specified receiver address.
     * @param msgmessage string to send.
     * @param peerAddress Address of the place to send it to.
     * @param peerPort the port to send it to.
     * @throws IOException If there is trouble sending this message.
     */
    protected void sendMessage(
    byte[] msg,
    InetAddress peerAddress,
    int peerPort,
    boolean reConnect)
    throws IOException {
        // Via is not included in the request so silently drop the reply.
        if (LogWriter.needsLogging)
            this.stack.logWriter.logStackTrace();
        if (peerPort == -1) {
            if (LogWriter.needsLogging) {
                this.stack.logWriter.logMessage(
                getClass().getName() + ":sendMessage: Dropping reply!");
            }
            throw new IOException("Receiver port not set ");
        } else {
            if (LogWriter.needsLogging) {
                this.stack.logWriter.logMessage(
                getClass().getName()
                + ":sendMessage "
                + peerAddress.getHostAddress()
                + "/"
                + peerPort
                + "\n"
                + new String(msg));
                this.stack.logWriter.logMessage("*******************\n");
            }

        }
        DatagramPacket reply =
        new DatagramPacket(msg, msg.length, peerAddress, peerPort);
        try {
          	DatagramSocket sock;
            if (stack.udpFlag) {
                // Use the socket from the message processor (for firewall
                // support use the same socket as the message processor
                // socket -- feature request # 18 from java.net). This also
                // makes the whole thing run faster!
                
                sock = ((UDPMessageProcessor) messageProcessor).sock;
                
                // Bind the socket to the stack address in case there
                // are multiple interfaces on the machine (feature reqeust
                // by Will Scullin) 0 binds to an ephemeral port.
                // sock = new DatagramSocket(0,stack.stackInetAddress);
            } else {
                // bind to any interface and port.
              	sock = new DatagramSocket();
            }
            sock.send(reply);
            if (!stack.udpFlag)
                sock.close();
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
        }
    }
    
    /**
     * Send a message to a specified receiver address.
     * @param msg message string to send.
     * @param peerAddress Address of the place to send it to.
     * @param peerPort the port to send it to.
     * @param peerProtocol protocol to use to send.
     * @throws IOException If there is trouble sending this message.
     */
    protected void sendMessage(
    byte[] msg,
    InetAddress peerAddress,
    int peerPort,
    String peerProtocol,
    boolean retry)
    throws IOException {
        // Via is not included in the request so silently drop the reply.
        if (peerPort == -1) {
            if (LogWriter.needsLogging) {
                this.stack.logWriter.logMessage(
                getClass().getName() + ":sendMessage: Dropping reply!");
            }
            throw new IOException("Receiver port not set ");
        } else {
            if (LogWriter.needsLogging) {
                this.stack.logWriter.logMessage(
                getClass().getName()
                + ":sendMessage "
                + peerAddress.getHostAddress()
                + "/"
                + peerPort
                + "\n"
                + new String(msg));
                this.stack.logWriter.logMessage("*******************\n");
            }
        }
        if (peerProtocol.compareToIgnoreCase("UDP") == 0) {
            DatagramPacket reply =
            new DatagramPacket(msg, msg.length, peerAddress, peerPort);
            
            try {
                DatagramSocket sock;
                if (stack.udpFlag) {
                    sock = ((UDPMessageProcessor) messageProcessor).sock;
                    
                } else {
                    // bind to any interface and port.
                   sock = stack.getNetworkLayer().createDatagramSocket();
                }
                sock.send(reply);
                if (!stack.udpFlag)
                    sock.close();
            } catch (IOException ex) {
                throw ex;
            } catch (Exception ex) {
                InternalErrorHandler.handleException(ex);
            }
            
        } else {
            // Use TCP to talk back to the sender.
            Socket outputSocket =
            stack.ioHandler.sendBytes(
            peerAddress,
            peerPort,
            "tcp",
            msg,
            retry);
            OutputStream myOutputStream = outputSocket.getOutputStream();
            myOutputStream.write(msg, 0, msg.length);
            myOutputStream.flush();
	    // The socket is cached (dont close it!);
        }
    }
    
    /**
     * get the stack pointer.
     * @return The sip stack for this channel.
     */
    public SIPMessageStack getSIPStack() {
        return stack;
    }
    
    /**
     * Return a transport string.
     * @return the string "udp" in this case.
     */
    public String getTransport() {
        return SIPConstants.UDP;
    }
    
    /**
     * get the stack address for the stack that received this message.
     * @return The stack address for our stack.
     */
    public String getHost() {
        return stack.stackAddress;
    }
    
    /**
     * get the port.
     * @return Our port (on which we are getting datagram packets).
     */
    public int getPort() {
        return ((UDPMessageProcessor) messageProcessor).getPort();
    }
    
    
    /**
     * get the name (address) of the host that sent me the message
     * @return The name of the sender (from the datagram packet).
     */
    public String getPeerName() {
        return peerAddress.getHostName();
    }
    
    /**
     * get the address of the host that sent me the message
     * @return The senders ip address.
     */
    public String getPeerAddress() {
        return peerAddress.getHostAddress();
    }

    protected InetAddress getPeerInetAddress() {
		return peerAddress;
    }
    
    /**
     * Compare two UDP Message channels for equality.
     * @param other The other message channel with which to compare oursleves.
     */
    public boolean equals(Object other) {
        
        if (other == null)
            return false;
        boolean retval;
        if (!this.getClass().equals(other.getClass())) {
            retval = false;
        } else {
            UDPMessageChannel that = (UDPMessageChannel) other;
            retval = this.getKey().equals(that.getKey());
        }
        
        return retval;
    }
    
    public String getKey() {
        return getKey(peerAddress, peerPort, "UDP");
    }
    
    
    public int getPeerPacketSourcePort()
    {
        return peerPacketSourcePort;
    }

    public InetAddress getPeerPacketSourceAddress()
    {
        return peerPacketSourceAddress;
    }
    
    /**
     * Get the logical originator of the message (from the top via header).
     * @return topmost via header sentby field
     */
    public String getViaHost() {
        return this.myAddress;
    }
    
    /**
     * Get the logical port of the message orginator (from the top via hdr).
     * @return the via port from the topmost via header.
     */
    public int getViaPort() {
        return this.myPort;
    }
    
    /**
     * Returns "false" as this is an unreliable transport.
     */
    public boolean isReliable() {
        return false;
    }
    
    /**
     * UDP is not a secure protocol.
     */
    public boolean isSecure() {
        return false;
    }
    
    public int getPeerPort() {
        return peerPort;
    }


    public String getPeerProtocol() {
	return this.peerProtocol;
    }
    
    /**
     * Close the message channel.
     */
    public void close() {
    }
}
/*
 * $Log: UDPMessageChannel.java,v $
 * Revision 1.28  2005/02/24 16:14:30  mranga
 * Submitted by:  mranga
 * Reviewed by:   mranga
 *
 * Bug fixes applied -- now you can support multiple proxy authorization headers
 *
 * Revision 1.27  2004/12/01 19:05:16  mranga
 * Reviewed by:   mranga
 * Code cleanup remove the unused SIMULATION code to reduce the clutter.
 * Fix bug in Dialog state machine.
 *
 * Revision 1.26  2004/11/28 17:32:26  mranga
 * Submitted by:  hagai sela
 * Reviewed by:   mranga
 *
 * Support for symmetric nats
 *
 * Revision 1.25  2004/09/07 20:13:06  mranga
 * Submitted by:  mranga
 * Reviewed by:   mranga
 *
 * Fixed some logging code.
 *
 * Revision 1.24  2004/08/30 16:04:48  mranga
 * Submitted by:  Mike Andrews
 * Reviewed by:   mranga
 *
 * Added a network layer.
 *
 * Revision 1.23  2004/07/16 17:13:56  mranga
 * Submitted by:  Damand Joost
 * Reviewed by:   mranga
 *
 * Make threads into daemon threads, use address for received = parameter on via
 *
 * Revision 1.22  2004/06/21 05:42:33  mranga
 * Reviewed by:  mranga
 * more code smithing
 *
 * Revision 1.21  2004/06/21 04:59:53  mranga
 * Refactored code - no functional changes.
 *
 * Revision 1.20  2004/05/30 18:55:58  mranga
 * Reviewed by:   mranga
 * Move to timers and eliminate the Transaction scanner Thread
 * to improve scalability and reduce cpu usage.
 *
 * Revision 1.19  2004/05/18 15:26:45  mranga
 * Reviewed by:   mranga
 * Attempted fix at race condition bug. Remove redundant exception (never thrown).
 * Clean up some extraneous junk.
 *
 * Revision 1.18  2004/05/16 14:13:23  mranga
 * Reviewed by:   mranga
 * Fixed the use-count issue reported by Peter Parnes.
 * Added property to prevent against content-length dos attacks.
 *
 * Revision 1.17  2004/05/06 15:45:52  mranga
 * Reviewed by:   mranga
 * delete dialog when 4xx other than 401 or 407 are received in the early state.
 *
 * Revision 1.16  2004/04/22 22:51:19  mranga
 * Submitted by:  Thomas Froment
 * Reviewed by:   mranga
 *
 * Fixed corner cases.
 *
 * Revision 1.15  2004/03/30 15:38:18  mranga
 * Reviewed by:   mranga
 * Name the threads so as to facilitate debugging.
 *
 * Revision 1.14  2004/01/22 18:39:42  mranga
 * Reviewed by:   M. Ranganathan
 * Moved the ifdef SIMULATION and associated tags to the first column so Prep preprocessor can deal with them.
 *
 * Revision 1.13  2004/01/22 14:23:45  mranga
 * Reviewed by:   mranga
 * Fixed some minor formatting issues.
 *
 * Revision 1.12  2004/01/22 13:26:33  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
