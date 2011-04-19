/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 ******************************************************************************/

package gov.nist.javax.sip.stack;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.util.Properties;

/**
 * Log file wrapper class.
 * Log messages into the message trace file and also write the log into the
 * debug file if needed. This class keeps an XML formatted trace around for
 * later access via RMI. The trace can be viewed with a trace viewer (see
 * tools.traceviewerapp).
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.17 $ $Date: 2005/04/04 09:54:57 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class ServerLog {

	private boolean logContent;

	protected LogWriter logWriter;

	/**
	 * Dont trace
	 */
	public static final int TRACE_NONE = 0;

	public static final int TRACE_MESSAGES = 16;
	
	/**
	 * Trace exception processing
	 */
	public static final int TRACE_EXCEPTION = 17;

	/**
	 * Debug trace level (all tracing enabled).
	 */
	public static final int TRACE_DEBUG = 32;

	/**
	 * Name of the log file in which the trace is written out
	 * (default is null)
	 */
	private String logFileName;

	/**
	 * Print writer that is used to write out the log file.
	 */
	protected PrintWriter printWriter;

	/**
	 * print stream for writing out trace
	 */
	protected PrintStream traceWriter = System.out;

	/**
	 * Name to assign for the log.
	protected String logRootName;
	 */

	/**
	 * Set auxililary information to log with this trace.
	 */
	protected String auxInfo;

	protected String description;

	protected String stackIpAddress;

	private SIPMessageStack sipStack;

	private Properties configurationProperties;

	public ServerLog(SIPMessageStack sipStack ) {
		this.logWriter = sipStack.logWriter;
	}
	
	public void setProperties( Properties configurationProperties) {
		this.configurationProperties = configurationProperties;
		// Set a descriptive name for the message trace logger.
		this.description = 
			configurationProperties.getProperty
			("javax.sip.STACK_NAME") ;
		this.stackIpAddress = configurationProperties.getProperty
			("javax.sip.IP_ADDRESS") ;
		this.logFileName = 
			 configurationProperties.getProperty
			("gov.nist.javax.sip.SERVER_LOG") ;
	}

	public void setStackIpAddress(String ipAddress) {
		this.stackIpAddress = ipAddress;
	}

	

	//public static boolean isWebTesterCatchException=false;
	//public static String webTesterLogFile=null;

	/**
	 *  Debugging trace stream.
	 */
	private PrintStream trace = System.out;

	/**
	 * default trace level
	 */
	protected int traceLevel = TRACE_MESSAGES;

	public void checkLogFile() {
		if (logFileName == null || traceLevel < TRACE_MESSAGES) {
			// Dont create a log file if tracing is
			// disabled.
			return;
		}
		try {
			File logFile = new File(logFileName);
			if (!logFile.exists()) {
				logFile.createNewFile();
				printWriter = null;
			}
			// Append buffer to the end of the file.
			if (printWriter == null) {
				String s = configurationProperties.getProperty
						("gov.nist.javax.sip.LOG_MESSAGE_CONTENT");
				this.logContent =  (s != null && s.equals("true"));
				FileWriter fw = new FileWriter(logFileName, true);
				printWriter = new PrintWriter(fw, true);
				printWriter.println(
					"<!-- "
						+ "Use the  Trace Viewer in src/tools/tracesviewer to"		 	+
						" view this  trace  \n" 						+
						"Here are the stack configuration properties \n"			+
						"javax.sip.IP_ADDRESS= " 						+ 
						configurationProperties.getProperty("javax.sip.IP_ADDRESS") + "\n" 	+
						"javax.sip.STACK_NAME= " 						+ 
						configurationProperties.getProperty("javax.sip.STACK_NAME") + "\n" 	+
						"javax.sip.ROUTER_PATH= " 						+ 
						configurationProperties.getProperty("javax.sip.ROUTER_PATH") + "\n" 	+
						"javax.sip.OUTBOUND_PROXY= " 						+ 
						configurationProperties.getProperty("javax.sip.OUTBOUND_PROXY") + "\n" 	+
						"javax.sip.RETRANSMISSION_FILTER= " 					+ 
						configurationProperties.getProperty("javax.sip.RETRANSMISSION_FILTER")  + "\n" 
						+ "-->");
				if (auxInfo != null) {
					printWriter.println(
						"<description\n logDescription=\""
							+ description
							+ "\"\n name=\""
							+ stackIpAddress
							+ "\"\n auxInfo=\""
							+ auxInfo
							+ "\"/>\n ");
					if (LogWriter.needsLogging) {
					    	logWriter.logMessage(
						"Here are the stack configuration properties \n"			+
						"javax.sip.IP_ADDRESS= " 						+ 
						configurationProperties.getProperty("javax.sip.IP_ADDRESS") + "\n" 	+
						"javax.sip.ROUTER_PATH= " 						+ 
						configurationProperties.getProperty("javax.sip.ROUTER_PATH") + "\n" 	+
						"javax.sip.OUTBOUND_PROXY= " 						+ 
						configurationProperties.getProperty("javax.sip.OUTBOUND_PROXY") + "\n" 	+
						"javax.sip.RETRANSMISSION_FILTER= " 					+ 
						configurationProperties.getProperty("javax.sip.RETRANSMISSION_FILTER")  + 
						"gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS= " 					  + 
						configurationProperties.getProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS") + "\n" +
						"gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS= " 					  + 
						configurationProperties.getProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS") + "\n" +
						"gov.nist.javax.sip.REENTRANT_LISTENER= " 						  + 
						configurationProperties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER")  		  +
						"gov.nist.javax.sip.THREAD_POOL_SIZE= " 						  + 
						configurationProperties.getProperty("gov.nist.javax.sip.THREAD_POOL_SIZE")  		  +
						"\n" );
						logWriter.logMessage(" ]]> ");
						logWriter.logMessage("</debug>");
						logWriter.logMessage(
							"<description\n logDescription=\""
								+ description
								+ "\"\n name=\""
								+ stackIpAddress
								+ "\"\n auxInfo=\""
								+ auxInfo
								+ "\"/>\n ");
						logWriter.logMessage("<debug>");
						logWriter.logMessage("<![CDATA[ ");
					}
				} else {
					printWriter.println(
						"<description\n logDescription=\""
							+ description
							+ "\"\n name=\""
							+ stackIpAddress
							+ "\" />\n");
					if (LogWriter.needsLogging) {
					    	logWriter.logMessage(
						"Here are the stack configuration properties \n"			+
						"javax.sip.IP_ADDRESS= " 						+ 
						configurationProperties.getProperty("javax.sip.IP_ADDRESS") + "\n" 	+
						"javax.sip.IP_ADDRESS= " 						+ 
						configurationProperties.getProperty("javax.sip.STACK_NAME") + "\n" 	+
						"javax.sip.ROUTER_PATH= " 						+ 
						configurationProperties.getProperty("javax.sip.ROUTER_PATH") + "\n" 	+
						"javax.sip.OUTBOUND_PROXY= " 						+ 
						configurationProperties.getProperty("javax.sip.OUTBOUND_PROXY") + "\n"  +
						"javax.sip.RETRANSMISSION_FILTER= " 					+ 
						configurationProperties.getProperty("javax.sip.RETRANSMISSION_FILTER") + "\n" 		  +
						"gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS= " 					  + 
						configurationProperties.getProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS") + "\n" +
						"gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS= " 					  + 
						configurationProperties.getProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS") + "\n" +
						"gov.nist.javax.sip.REENTRANT_LISTENER= " 						  + 
						configurationProperties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER") + "\n" 	  +
						"gov.nist.javax.sip.THREAD_POOL_SIZE= " 						  + 
						configurationProperties.getProperty("gov.nist.javax.sip.THREAD_POOL_SIZE")  		  +
						"\n");
						logWriter.logMessage(" ]]>");
						logWriter.logMessage("</debug>");
						logWriter.logMessage(
							"<description\n logDescription=\""
								+ description
								+ "\"\n name=\""
								+ stackIpAddress
								+ "\" />\n");
						logWriter.logMessage("<debug>");
						logWriter.logMessage("<![CDATA[ ");
					}
				}
			}
		} catch (IOException ex) {

		}
	}

	/**
	 * Check to see if logging is enabled at a level (avoids
	 * unecessary message formatting.
	 * @param logLevel level at which to check.
	 */
	public boolean needsLogging(int logLevel) {
		return traceLevel >= logLevel;
	}

	/**
	 * Global check for whether to log or not. To minimize the time
	 * return false here.
	 *
	 * @return true -- if logging is globally enabled and false otherwise.
	 *
	 */
	public boolean needsLogging() {
		return logFileName != null;
	}

	/**
	 * Set the log file name
	 * @param name is the name of the log file to set.
	 */
	public void setLogFileName(String name) {
		logFileName = name;
	}

	/**
	 * return the name of the log file.
	 */
	public String getLogFileName() {
		return logFileName;
	}

	/**
	 * Log a message into the log file.
	 * @param message message to log into the log file.
	 */
	private void logMessage(String message) {
		// String tname = Thread.currentThread().getName();
		checkLogFile();
		String logInfo = message;
		if (printWriter == null) {
			System.out.println(logInfo);
		} else {
			printWriter.println(logInfo);
		}
		if (LogWriter.needsLogging) {
			logWriter.logMessage(" ]]>");
			logWriter.logMessage("</debug>");
			logWriter.logMessage(logInfo);
			logWriter.logMessage("<debug>");
			logWriter.logMessage("<![CDATA[ ");
		}
	}

	/**
	 * Log a message into the log directory.
	 * @param message a SIPMessage to log
	 * @param from from header of the message to log into the log directory
	 * @param to to header of the message to log into the log directory
	 * @param sender is the server the sender (true if I am the sender).
	 * @param callId CallId of the message to log into the log directory.
	 * @param firstLine First line of the message to display
	 * @param status Status information (generated while processing message).
	 * @param time the reception time (or date).
	 */
	private synchronized void logMessage(
		String message,
		String from,
		String to,
		boolean sender,
		String callId,
		String firstLine,
		String status,
		String tid,
		String time) {

		MessageLog log =
			new MessageLog(
				message,
				from,
				to,
				time,
				sender,
				firstLine,
				status,
				tid,
				callId,
				logWriter.getLineCount());
		logMessage(log.flush());
	}

	private synchronized void logMessage(
		String message,
		String from,
		String to,
		boolean sender,
		String callId,
		String firstLine,
		String status,
		String tid,
		long time) {

		MessageLog log =
			new MessageLog(
				message,
				from,
				to,
				time,
				sender,
				firstLine,
				status,
				tid,
				callId,
				logWriter.getLineCount());
		logMessage(log.flush());
	}

	/**
	 * Log a message into the log directory.
	 * @param message a SIPMessage to log
	 * @param from from header of the message to log into the log directory
	 * @param to to header of the message to log into the log directory
	 * @param sender is the server the sender
	 * @param callId CallId of the message to log into the log directory.
	 * @param firstLine First line of the message to display
	 * @param status Status information (generated while processing message).
	 * @param tid    is the transaction id for the message.
	 */
	private void logMessage(
		String message,
		String from,
		String to,
		boolean sender,
		String callId,
		String firstLine,
		String status,
		String tid) {
		String time = new Long(System.currentTimeMillis()).toString();
		logMessage(
			message,
			from,
			to,
			sender,
			callId,
			firstLine,
			status,
			tid,
			time);
	}

	/**
	 * Log a message into the log directory. 
	 * @param message a SIPMessage to log
	 * @param from from header of the message to log into the log directory
	 * @param to to header of the message to log into the log directory
	 * @param sender is the server the sender
	 * @param time is the time to associate with the message.
	 */
	public void logMessage(
		SIPMessage message,
		String from,
		String to,
		boolean sender,
		String time) {
		checkLogFile();
		CallID cid = (CallID) message.getCallId();
		String callId = null;
		if (cid != null)
			callId = ((CallID) message.getCallId()).getCallId();
		String firstLine = message.getFirstLine().trim();
		String inputText = (logContent ? message.encode() : message.encodeMessage() ) ;
		String tid = message.getTransactionId();
		logMessage(
			inputText,
			from,
			to,
			sender,
			callId,
			firstLine,
			null,
			tid,
			time);
	}

	/**
	 * Log a message into the log directory.
	 * @param message a SIPMessage to log
	 * @param from from header of the message to log into the log directory
	 * @param to to header of the message to log into the log directory
	 * @param sender is the server the sender
	 * @param time is the time to associate with the message.
	 */
	public void logMessage(
		SIPMessage message,
		String from,
		String to,
		boolean sender,
		long time) {
		checkLogFile();
		CallID cid = (CallID) message.getCallId();
		String callId = null;
		if (cid != null)
			callId = cid.getCallId();
		String firstLine = message.getFirstLine().trim();
		String inputText = (logContent ? message.encode() : message.encodeMessage() ) ;
		String tid = message.getTransactionId();
		logMessage(
			inputText,
			from,
			to,
			sender,
			callId,
			firstLine,
			null,
			tid,
			time);
	}

	/**
	 * Log a message into the log directory. Status information is extracted
	 * from SIPExtension header. The time associated with the message is the
	 * current time.
	 * @param message a SIPMessage to log
	 * @param from from header of the message to log into the log directory
	 * @param to to header of the message to log into the log directory
	 * @param sender is the server the sender
	 */
	public void logMessage(
		SIPMessage message,
		String from,
		String to,
		boolean sender) {
		logMessage(message, from, to, sender,
		new Long(System.currentTimeMillis()).toString()
		);
	}

	/**
	 * Log a message into the log directory.
	 * @param message a SIPMessage to log
	 * @param from from header of the message to log into the log directory
	 * @param to to header of the message to log into the log directory
	 * @param status the status to log. 
	 * @param sender is the server the sender or receiver (true if sender).
	 * @param time is the reception time.
	 */
	public void logMessage(
		SIPMessage message,
		String from,
		String to,
		String status,
		boolean sender,
		String time) {
		checkLogFile();
		CallID cid = (CallID) message.getCallId();
		String callId = null;
		if (cid != null)
			callId = cid.getCallId();
		String firstLine = message.getFirstLine().trim();
		String encoded = (logContent ? message.encode() : message.encodeMessage() ) ;
		String tid = message.getTransactionId();
		logMessage(
			encoded,
			from,
			to,
			sender,
			callId,
			firstLine,
			status,
			tid,
			time);
	}

	/**
	 * Log a message into the log directory.
	 * @param message a SIPMessage to log
	 * @param from from header of the message to log into the log directory
	 * @param to to header of the message to log into the log directory
	 * @param status the status to log. 
	 * @param sender is the server the sender or receiver (true if sender).
	 * @param time is the reception time.
	 */
	public void logMessage(
		SIPMessage message,
		String from,
		String to,
		String status,
		boolean sender,
		long time) {
		checkLogFile();
		CallID cid = (CallID) message.getCallId();
		String callId = null;
		if (cid != null)
			callId = cid.getCallId();
		String firstLine = message.getFirstLine().trim();
		String encoded = (logContent ? message.encode() : message.encodeMessage() ) ;
		String tid = message.getTransactionId();
		logMessage(
			encoded,
			from,
			to,
			sender,
			callId,
			firstLine,
			status,
			tid,
			time);
	}

	/**
	 * Log a message into the log directory. Time stamp associated with the
	 * message is the current time.
	 * @param message a SIPMessage to log
	 * @param from from header of the message to log into the log directory
	 * @param to to header of the message to log into the log directory
	 * @param status the status to log.
	 * @param sender is the server the sender or receiver (true if sender).
	 */
	public void logMessage(
		SIPMessage message,
		String from,
		String to,
		String status,
		boolean sender) {
		logMessage(message, from, to, status, sender,
		System.currentTimeMillis()
		);
	}

	/**
	 * Log a message into the log file.
	 * @param msgLevel Logging level for this message.
	 * @param tracemsg message to write out.
	 */
	public void traceMsg(int msgLevel, String tracemsg) {
		if (needsLogging(msgLevel)) {
			traceWriter.println(tracemsg);
			logMessage(tracemsg);
		}
	}

	/**
	 * Log an exception stack trace.
	 * @param ex Exception to log into the log file
	 */

	public void logException(Exception ex) {
		if (traceLevel >= TRACE_EXCEPTION) {
			checkLogFile();
			ex.printStackTrace();
			if (printWriter != null)
				ex.printStackTrace(printWriter);

		}
	}

	/**
	 * Set the name to assign for the log.
	public void setLogName(String name) {
		logRootName = name;
	}
	 */

	/**
	 * print a line to stdout if the traceLevel is TRACE_DEBUG.
	 * @param s String to print out.
	 */
	public void println(String s) {
		if (traceLevel == TRACE_DEBUG)
			System.out.println(s);
	}

	/**
	 * Set the trace level for the stack.
	 *
	 * @param level -- the trace level to set. The following trace levels are
	 * supported:
	 *<ul>
	 *<li>
	 *0 -- no tracing
	 *</li>
	 *
	 *<li>
	 *16 -- trace messages only
	 *</li>
	 *
	 *<li>
	 *32 Full tracing including debug messages.
	 *</li>
	 *
	 *</ul>
	 */
	public void setTraceLevel(int level) {
		traceLevel = level;
	}

	/**
	 * Get the trace level for the stack.
	 *
	 * @return the trace level
	 */
	public int getTraceLevel() {
		return traceLevel;
	}

	/**
	 * Set aux information. Auxiliary information may be associated
	 * with the log file. This is useful for remote logs.
	 *
	 * @param auxInfo -- auxiliary information.
	 */
	public void setAuxInfo(String auxInfo) {
		this.auxInfo = auxInfo;
	}

	/**
	 * Set the descriptive String for the log.
	 *
	 * @param desc is the descriptive string.
	public void setDescription(String desc) {
		description = desc;
	}
	 */
}
/*
 * $Log: ServerLog.java,v $
 * Revision 1.17  2005/04/04 09:54:57  dmuresan
 * Some constants declared final.
 *
 * Revision 1.16  2004/12/01 19:05:16  mranga
 * Reviewed by:   mranga
 * Code cleanup remove the unused SIMULATION code to reduce the clutter.
 * Fix bug in Dialog state machine.
 *
 * Revision 1.15  2004/09/01 02:04:16  xoba
 * Issue number:  no particular issue number.
 *
 * this code passes TCK
 *
 * fixed multiple javadoc errors throughout javax.* and gov.nist.*
 *
 * added junit and log4j jars to cvs module, although log4j is not being used yet.
 *
 * modified and expanded build.xml and fixed javadoc reference to outdated jre documentation (now
 * javadocs hyperlink to jre api documentation). since
 * top-level 'docs' directory already contains cvs-controlled files, i redirected output of javadocs to their
 * own separate directories, which are 'cleaned' along with 'clean' target. also created other javadoc
 * which just outputs javax.* classes for those wishing to develop sip applications without reference to nist.gov.*.
 *
 * completed switchover to NetworkLayer for network access.
 *
 * DID NOT modify makefile's.... so, developers beware.
 *
 *
 *
 *
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
 * Revision 1.14  2004/07/07 15:46:59  mranga
 * Submitted by:  Al Straub
 * Reviewed by:   mranga
 *
 * Revision 1.13  2004/06/21 04:59:53  mranga
 * Refactored code - no functional changes.
 *
 * Revision 1.12  2004/04/27 17:18:54  mranga
 * Reviewed by:   mranga
 * Turn off logging of content  by default.
 *
 * Revision 1.11  2004/04/16 16:04:35  mranga
 * Submitted by:  Thomas Froment
 * Reviewed by:   mranga
 * Do not create a log file if logging is disabled.
 *
 * Revision 1.10  2004/03/25 16:37:01  mranga
 * Reviewed by:   mranga
 * Fix up for logging messages.
 *
 * Revision 1.9  2004/03/25 15:15:05  mranga
 * Reviewed by:   mranga
 * option to log message content added.
 *
 * Revision 1.8  2004/02/20 16:36:43  mranga
 * Reviewed by:   mranga
 * Minor changes to debug logging -- record the properties with which the stack
 * was created. Be slightly more forgiving when checking for retransmission
 * filter when configuring stack.
 *
 * Revision 1.7  2004/01/22 18:39:41  mranga
 * Reviewed by:   M. Ranganathan
 * Moved the ifdef SIMULATION and associated tags to the first column so Prep preprocessor can deal with them.
 *
 * Revision 1.6  2004/01/22 13:26:33  sverker
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
