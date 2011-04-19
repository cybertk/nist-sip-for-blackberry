package examples.multi;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*; 

/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme 
 * is the guy that gets shot.
 *
 *@author M. Ranganathan
 */

public class Shootme implements SipListener {

	private static AddressFactory addressFactory;
	private static MessageFactory messageFactory;
	private static HeaderFactory headerFactory;
	private static SipStack sipStack;
	private static String myAddress = "127.0.0.1";
	private static int myPort    = 5070;
	int numInvite = 0;


	Dialog dialog;

	class TTask extends TimerTask {

		RequestEvent requestEvent;
		ServerTransaction st;

		public TTask ( RequestEvent requestEvent ,ServerTransaction st) {
		    this.requestEvent  = requestEvent;
		    this.st = st;
		}

		public void run() {
		 SipProvider sipProvider = 
				(SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
		try {
			System.out.println("shootme: got an Invite sending OK");
			Response response = messageFactory.createResponse(180, request);
			ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag("4321"); // Application is supposed to set.
			Address address =
				addressFactory.createAddress("Shootme <sip:" + myAddress+ ":" + myPort + ">");
			ContactHeader contactHeader =
				headerFactory.createContactHeader(address);
			response.addHeader(contactHeader);

			System.out.println("got a server tranasaction " + st);
			dialog = st.getDialog();
			if (dialog != null) {
			    System.out.println("Dialog " + dialog);
			    System.out.println("Dialog state " 
					+ dialog.getState());
			}
			st.sendResponse(response); // send 180(RING)
			response = messageFactory.createResponse(200, request);
			toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag("4321"); // Application is supposed to set.
			response.addHeader(contactHeader);
			
			
			st.sendResponse(response);// send 200(OK)
			
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
			

		}

	}

	protected static final String usageString =
		"java "
			+ "examples.shootist.Shootist \n"
			+ ">>>> is your class path set to the root?";

	private static void usage() {
		System.out.println(usageString);
		System.exit(0);

	}

	public void processRequest(RequestEvent requestEvent) {
		Request request = requestEvent.getRequest();
		ServerTransaction serverTransactionId =
			requestEvent.getServerTransaction();

		System.out.println(
			"\n\nRequest "
				+ request.getMethod()
				+ " received at "
				+ sipStack.getStackName()
				+ " with server transaction id "
				+ serverTransactionId);

		if (request.getMethod().equals(Request.INVITE)) {
			processInvite(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.ACK)) {
			processAck(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.BYE)) {
			processBye(requestEvent, serverTransactionId);
		}

	}

	/** Process the ACK request. Send the bye and complete the call flow.
	*/
	public void processAck(
		RequestEvent requestEvent,
		ServerTransaction serverTransaction) {
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		try {
		    System.out.println("*** shootme: got an ACK " 
				+ requestEvent.getRequest());
		    dialog = serverTransaction.getDialog();
		    Request byeRequest = dialog.createRequest(Request.BYE);
		    ClientTransaction tr =
				sipProvider.getNewClientTransaction(byeRequest);
		    System.out.println("shootme: got an ACK -- sending bye! ");
		    dialog.sendRequest(tr);
		    System.out.println("Dialog State = " + dialog.getState());
		} catch (Exception ex) {
		    ex.printStackTrace();
		    System.exit(0);
		}
	}

	/** Process the invite request.
	 */
	public void processInvite(
		RequestEvent requestEvent,
		ServerTransaction serverTransaction) {
	    try {
		System.out.println("ProcessInvite" );
		Request request = requestEvent.getRequest();
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		// Note you need to create the Request Event
		// before the listener returns.

		ServerTransaction st = sipProvider.getNewServerTransaction(request);
		TTask ttask = new TTask( requestEvent,st);
		int ttime;
		if ((numInvite%4) ==0)
				ttime = 5*1000;
		else if ((numInvite%4) ==1)
			 ttime = 1*1000;
		else 
		       ttime = 300;
		numInvite ++;
		new Timer().schedule(ttask,ttime);
	   } catch (Exception  ex ) {
		ex.printStackTrace();
	   }
	}

	/** Process the bye request.
	 */
	public void processBye(
		RequestEvent requestEvent,
		ServerTransaction serverTransactionId) {
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
		try {
			System.out.println("shootme:  got a bye sending OK.");
			Response response =
				messageFactory.createResponse(200, request, null, null);
			serverTransactionId.sendResponse(response);
			System.out.println("Dialog State is " + serverTransactionId.getDialog().getState());

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}
	}

	public void processResponse(ResponseEvent responseReceivedEvent) {
		System.out.println("Got a response");
		Response response = (Response) responseReceivedEvent.getResponse();
		Transaction tid = responseReceivedEvent.getClientTransaction();

		System.out.println(
			"Response received with client transaction id "
				+ tid
				+ ":\n"
				+ response);
		try {
			if (response.getStatusCode() == Response.OK
				&& ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
					.getMethod()
					.equals(
					Request.INVITE)) {
				Dialog dialog = tid.getDialog();
				// Save the tags for the dialog here.
				Request request = tid.getRequest();
				dialog.sendAck(request);
			}
			Dialog dialog = tid.getDialog();
			System.out.println("Dalog State = " + dialog.getState());
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}

	}

	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
		Transaction transaction;
		if (timeoutEvent.isServerTransaction()) {
			transaction = timeoutEvent.getServerTransaction();
		} else {
			transaction = timeoutEvent.getClientTransaction();
		}
		System.out.println("state = " + transaction.getState());
		System.out.println("dialog = " + transaction.getDialog());
		System.out.println(
			"dialogState = " + transaction.getDialog().getState());
		System.out.println("Transaction Time out");
	}

	public void init() {
		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
//ifdef SIMULATION
/*
		        properties.setProperty("javax.sip.IP_ADDRESS","129.6.55.62");
//else
*/
		properties.setProperty("javax.sip.IP_ADDRESS", myAddress );
//endif
//
		properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");
		properties.setProperty("javax.sip.STACK_NAME", "shootme");
		// You need  16 for logging traces. 32 for debug + traces.
		// Your code will limp at 32 but it is best for debugging.
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		properties.setProperty(
			"gov.nist.javax.sip.DEBUG_LOG",
			"shootmedebug.txt");
		properties.setProperty(
			"gov.nist.javax.sip.SERVER_LOG",
			"shootmelog.txt");
		// Guard against starvation.
		properties.setProperty(
			"gov.nist.javax.sip.READ_TIMEOUT", "1000");
		// properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "4096");
		properties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "false");

		try {
			// Create SipStack object
			sipStack = sipFactory.createSipStack(properties);
			System.out.println("sipStack = " + sipStack);
		} catch (PeerUnavailableException e) {
			// could not find
			// gov.nist.jain.protocol.ip.sip.SipStackImpl
			// in the classpath
			e.printStackTrace();
			System.err.println(e.getMessage());
			if (e.getCause() != null)
				e.getCause().printStackTrace();
			System.exit(0);
		}

		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			ListeningPoint lp = sipStack.createListeningPoint(5070, "udp");
			ListeningPoint lp1 = sipStack.createListeningPoint(5070, "tcp");

			Shootme listener = this;

			SipProvider sipProvider = sipStack.createSipProvider(lp);
			System.out.println("udp provider " + sipProvider);
			sipProvider.addSipListener(listener);
			sipProvider = sipStack.createSipProvider(lp1);
			System.out.println("tcp provider " + sipProvider);
			sipProvider.addSipListener(listener);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			usage();
		}

	}

	public static void main(String args[]) {
		/* pass dynamic parameters in *.bat file(command line) */
		if ( args.length >= 1 ) 
			myAddress = args[0];
		if ( args.length >=2 )
			myPort = Integer.parseInt(args[1]);
		
		System.out.println("\n***Address=<"+myAddress+">, Port=<"+myPort+">.");
		new Shootme().init();
	}

}
