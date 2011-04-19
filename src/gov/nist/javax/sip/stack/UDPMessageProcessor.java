/*******************************************************************************
 *   Product of NIST/ITL Advanced Networking Technologies Division (ANTD).     *
 *******************************************************************************/
package gov.nist.javax.sip.stack;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.DatagramPacket;
import java.io.IOException;
import java.util.LinkedList;
import java.net.InetAddress;
import java.net.UnknownHostException;
import gov.nist.core.*;
import java.lang.reflect.*;


/**
 * Sit in a loop and handle incoming udp datagram messages. For each Datagram
 * packet, a new UDPMessageChannel is created (upto the max thread pool size). 
 * Each UDP message is processed in its own thread). 
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.23 $ $Date: 2004/12/12 23:47:19 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * <a href="{@docRoot}/../uml/udp-request-processing-sequence-diagram.jpg">
 * See the implementation sequence diagram for processing incoming requests.
 * </a>
 * 
 *
 * Acknowledgement: Jeff Keyser contributed ideas on
 * starting and stoppping the stack that were incorporated into this code.
 * Niklas Uhrberg suggested that thread pooling be added to limit the number
 * of threads and improve performance. 
 */
public class UDPMessageProcessor extends MessageProcessor {


	/**
	 * port on which to listen for incoming messaes.
	 */
	private int port;

	/**
	 * The Mapped port (in case STUN suport is enabled)
	 */
	private int mappedPort;

	/**
	 * Incoming messages are queued here.
	 */
	protected LinkedList messageQueue;


	/**
	 * A list of message channels that we have started.
	 */
	protected LinkedList messageChannels;

	/**
	 * Max # of udp message channels
	 */
	protected int threadPoolSize;

	/**
	 * Max datagram size.
	 */
	protected static final int MAX_DATAGRAM_SIZE = 8 * 1024;

	/**
	 * Our stack (that created us).
	 */
	protected SIPMessageStack sipStack;


	/**
	* The message processor deaemon thread (used for wait)
	*/
	protected Thread  thread;

	protected DatagramSocket sock;

	/**
	 * A flag that is set to false to exit the message processor
	 * (suggestion by Jeff Keyser).
	 */
	protected boolean isRunning;

	/**
	 * Constructor.
	 * @param sipStack pointer to the stack.
	 */
	protected UDPMessageProcessor(SIPMessageStack sipStack, int port)  
		throws IOException {
		this.sipStack = sipStack;
		this.messageQueue = new LinkedList();
		this.port = port;
		this.mappedPort = port;
		try  {
		  this.sock = sipStack.getNetworkLayer().createDatagramSocket(port, sipStack.stackInetAddress);
		   // Create a new datagram socket.
		   sock.setReceiveBufferSize(MAX_DATAGRAM_SIZE);
		} catch (SocketException ex) {
			throw new IOException (ex.getMessage());
		}
	}

	/**
	* Get my thread.
	*/
	public Thread getThread() {
		return this.thread;
	}

	/**
	 * Get port on which to listen for incoming stuff.
	 * @return port on which I am listening.
	 */
	public int getPort() {
		return this.mappedPort;
	}

	/**
	 * Start our processor thread.
	 */
	public void start() throws IOException {
		// The following code is all done using introspection and looks
		// pretty ugly. It was written this way 
		// in order to keep it essentially independent of STUN implementation.
		// You need the stun4J stack in your classpath to enable this code. 
		// The stack can be configured with or without stun support.
		if (sipStack.stunServerAddress != null) {
			try {
				String stunServer = sipStack.stunServerAddress;
				Class stunAddressClass =
					Class.forName("net.java.stun4j.StunAddress");

				// Define stun address for the stun server.
				Class parm[] = new Class[2];
				parm[0] = String.class;
				parm[1] = Integer.TYPE;
				Constructor cons = stunAddressClass.getConstructor(parm);
				Object ca[] = new Object[2];
				ca[0] = sipStack.stunServerAddress;
				ca[1] = new Integer(sipStack.stunServerPort);
				Object stunAddress = cons.newInstance(ca);

				// Get the  Simple Address Detector instance.
				Class sadClass =
					Class.forName(
						"net.java.stun4j.client.SimpleAddressDetector");
				Class[] parms = new Class[1];
				parms[0] = stunAddressClass;
				cons = sadClass.getConstructor(parms);
				Object cargs[] = new Object[1];
				cargs[0] = stunAddress;
				Object simpleAddressDetector = cons.newInstance(cargs);

				// Start the detector
				Method meth = sadClass.getMethod("start", null);
				meth.invoke(simpleAddressDetector, null);

				// Invoke the method to get the mapping.
				Class parms1[] = new Class[1];
				parms1[0] = DatagramSocket.class;
				meth = sadClass.getMethod("getMappingFor", parms1);
				Object args[] = new Object[1];
				args[0] = this.sock;
				stunAddress = meth.invoke(simpleAddressDetector, args);
				meth = stunAddressClass.getMethod("getHostName", null);
				String hostName = (String) meth.invoke(stunAddress, null);
				meth = stunAddressClass.getMethod("getPort", null);
				Character port = (Character) meth.invoke(stunAddress, null);
				this.mappedPort = (int) port.charValue();
				sipStack.setHostAddress(hostName);

			} catch (Throwable ex) {
				if (LogWriter.needsLogging) {
					sipStack.getLogWriter().logMessage("Stun initializationFailed" );
					sipStack.getLogWriter().logException(ex);
				}
				System.out.println("Stun stack initialization failed!");
			}
		}

		this.isRunning = true;
		this.thread = new Thread(this);
		thread.setDaemon(true);
		// Issue #32 on java.net
		thread.setName("UDPMessageProcessorThread");
		thread.start();
	}

	/**
	 * Thread main routine.
	 */
	public void run() {
		// Check for running flag.
		this.messageChannels = new LinkedList();
		// start all our messageChannels (unless the thread pool size is
		// infinity.
		if (sipStack.threadPoolSize != -1) {
			for (int i = 0; i < sipStack.threadPoolSize; i++) {
				UDPMessageChannel channel =
					new UDPMessageChannel(sipStack, this);
				this.messageChannels.add(channel);

			}
		}
		while (this.isRunning) {
			// Somebody asked us to exit.
			try {
				int bufsize = sock.getReceiveBufferSize();
				byte message[] = new byte[bufsize];
				DatagramPacket packet = new DatagramPacket(message, bufsize);
				// System.out.println("received " + new String(message));
				sock.receive(packet);

				// Count of # of packets in process.
				// this.useCount++;
				if (sipStack.threadPoolSize != -1) {
					// Note: the only condition watched for by threads 
					// synchronizing on the messageQueue member is that it is 
					// not empty. As soon as you introduce some other
					// condition you will have to call notifyAll instead of 
					// notify below.

					synchronized (this.messageQueue) {
						this.messageQueue.addLast(packet);
						this.messageQueue.notify();
					}
				} else {
					new UDPMessageChannel(sipStack, this, packet);
				}
			} catch (SocketException ex) {
				if (LogWriter.needsLogging)
					getSIPStack().logWriter.logMessage(
						"UDPMessageProcessor: Stopping");
				isRunning = false;
				// The notifyAll should be in a synchronized block.
				// ( bug report by Niklas Uhrberg ).
				synchronized (this.messageQueue) {
					this.messageQueue.notifyAll();
				}
			} catch (IOException ex) {
				isRunning = false;
				ex.printStackTrace();
				if (LogWriter.needsLogging)
					getSIPStack().logWriter.logMessage(
						"UDPMessageProcessor: Got an IO Exception");
			} catch (Exception ex) {
				if (LogWriter.needsLogging)
					getSIPStack().logWriter.logMessage(
						"UDPMessageProcessor: Unexpected Exception - quitting");
				InternalErrorHandler.handleException(ex);
				return;
			}
		}
	}

	/**
	 * Shut down the message processor. Close the socket for recieving
	 * incoming messages.
	 */
	public void stop() {
		synchronized (this.messageQueue) {
			this.isRunning = false;
			this.messageQueue.notifyAll();
			this.listeningPoint = null;
			sock.close();
		}
	}

	/**
	 * Return the transport string.
	 * @return the transport string
	 */
	public String getTransport() {
		return "udp";
	}

	/**
	 * Returns the stack.
	 * @return my sip stack.
	 */
	public SIPMessageStack getSIPStack() {
		return sipStack;
	}

	/**
	 * Create and return new TCPMessageChannel for the given host/port.
	 */
	public MessageChannel createMessageChannel(HostPort targetHostPort)
		throws UnknownHostException {
		return new UDPMessageChannel(
			targetHostPort.getInetAddress(),
			targetHostPort.getPort(),
			sipStack,
			this);
	}

	public MessageChannel createMessageChannel(InetAddress host, int port)
		throws IOException {
		return new UDPMessageChannel(host, port, sipStack, this);
	}

	/**
	 * Default target port for UDP
	 */
	public int getDefaultTargetPort() {
		return 5060;
	}

	/**
	 * UDP is not a secure protocol.
	 */
	public boolean isSecure() {
		return false;
	}

	/**
	 * UDP can handle a message as large as the MAX_DATAGRAM_SIZE.
	 */
	public int getMaximumMessageSize() {
		return MAX_DATAGRAM_SIZE;
	}

	/**
	 * Return true if there are any messages in use.
	 */
	public boolean inUse() {
		synchronized (messageQueue) {
		   return messageQueue.size() != 0;
		}
	}

}
/*
 * $Log: UDPMessageProcessor.java,v $
 * Revision 1.23  2004/12/12 23:47:19  mranga
 * Issue number:  46
 * Submitted by:  espen
 * Reviewed by:   M. Ranganathan
 *
 * Use the local ip address when creating outbound TCP and TLS sockets.
 *
 * Revision 1.22  2004/12/01 19:05:16  mranga
 * Reviewed by:   mranga
 * Code cleanup remove the unused SIMULATION code to reduce the clutter.
 * Fix bug in Dialog state machine.
 *
 * Revision 1.21  2004/09/26 14:48:03  mranga
 * Submitted by:  John Martin
 * Reviewed by:   mranga
 *
 * Remove unnecssary synchronization.
 *
 * Revision 1.20  2004/09/04 14:59:54  mranga
 * Reviewed by:   mranga
 *
 * Added a method to expose the Thread for the message processors so that
 * stack.stop() can join to wait for the threads to die rather than sleep().
 * Feature requested by Mike Andrews.
 *
 * Revision 1.19  2004/08/30 16:04:48  mranga
 * Submitted by:  Mike Andrews
 * Reviewed by:   mranga
 *
 * Added a network layer.
 *
 * Revision 1.18  2004/07/16 17:13:56  mranga
 * Submitted by:  Damand Joost
 * Reviewed by:   mranga
 *
 * Make threads into daemon threads, use address for received = parameter on via
 *
 * Revision 1.17  2004/06/27 00:41:52  mranga
 * Submitted by:  Thomas Froment and Pierre De Rop
 * Reviewed by:   mranga
 * Performance improvements
 * (auxiliary data structure for fast lookup of transactions).
 *
 * Revision 1.16  2004/06/21 04:59:54  mranga
 * Refactored code - no functional changes.
 *
 * Revision 1.15  2004/06/02 15:32:12  mranga
 * Submitted by:  bdupras2
 * Reviewed by:   mranga
 * Fixes minor "issue" - forgot to set thread name for udp message processor
 * thread (was hoping that the issue would be withdrawn - OK I give up it is
 * "fixed")
 *
 * Revision 1.14  2004/05/16 14:13:23  mranga
 * Reviewed by:   mranga
 * Fixed the use-count issue reported by Peter Parnes.
 * Added property to prevent against content-length dos attacks.
 *
 * Revision 1.13  2004/04/19 21:51:04  mranga
 * Submitted by:  mranga
 * Reviewed by:  ivov
 * Support for stun.
 *
 * Revision 1.12  2004/04/03 12:30:53  mranga
 * Reviewed by:   mranga
 * fix potential race condition.
 *
 * Revision 1.11  2004/01/22 18:39:42  mranga
 * Reviewed by:   M. Ranganathan
 * Moved the ifdef SIMULATION and associated tags to the first column so Prep preprocessor can deal with them.
 *
 * Revision 1.10  2004/01/22 13:26:33  sverker
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
