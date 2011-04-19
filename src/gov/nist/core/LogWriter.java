/***************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).    *
***************************************************************************/

package gov.nist.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;


/**
*  Log System Errors. Also used for debugging log.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*@author m.andrews
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public class LogWriter {

    // private final static Logger logger = Logger.getLogger(LogWriter.class);
    
	/** Dont trace
	 */
	public static final int TRACE_NONE = 0;
	/** Trace initialization code
	 */
	/** Trace message processing
	 */
	public static final int TRACE_MESSAGES = 16;
	/** Trace exception processing
	 */
	public static final int TRACE_EXCEPTION = 17;
	/** Debug trace level (all tracing enabled).
	 */
	public static final int TRACE_DEBUG = 32;
	/** Name of the log file in which the trace is written out
	 * (default is /tmp/sipserverlog.txt)
	 */
	private String logFileName = "debuglog.txt";
	/** Print writer that is used to write out the log file.
	 */
	private PrintWriter printWriter;
	/** print stream for writing out trace
	 */
	private PrintStream traceWriter = System.out;

	/** Flag to indicate that logging is enabled. This needs to be
	* static and public in order to globally turn logging on or off.
	*/
	public static boolean needsLogging = false;

	private int lineCount;

	/**
	*  Debugging trace stream.
	*/
	private static final PrintStream trace = System.out;
	
	/** trace level
	 */
	// protected     static int traceLevel = TRACE_DEBUG;
	protected static int traceLevel = TRACE_NONE;

	/** log a stack trace..
	*/
	public void logStackTrace() {
		if (needsLogging) {
			checkLogFile();
			if (printWriter != null) {
				println("------------ Traceback ------");
				logException(new Exception());
				println("----------- End Traceback ------");
			}
		}
	}

	public int getLineCount() {
	    return lineCount;
	}
	
	public void logException(Throwable ex) {
		if (needsLogging) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			checkLogFile();
			if (printWriter != null)
				ex.printStackTrace(pw);
			pw.close();
			println(sw.toString());
		}
	}

	public void logThrowable(Throwable throwable) {
		if (needsLogging) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			checkLogFile();
			if (printWriter != null)
				throwable.printStackTrace(pw);
			pw.close();
			println(sw.toString());
		}
	}
	
	/** Log an excption. 
	* 1.4x Code contributed by Brad Templeton
	*
	*@param sframe - frame to log grace.
	*/
	public void logTrace(Throwable sframe) {
		if (needsLogging) {
			checkLogFile();
			logException(new Exception(sframe.getMessage()));
		}
	}

	/** Set the log file name 
	*@param name is the name of the log file to set. 
	*/
	public void setLogFileName(String name) {
		logFileName = name;
	}

	public synchronized void logMessage(String message, String logFileName) {
		if (LogWriter.needsLogging) {
			try {
				File logFile = new File(logFileName);
				if (!logFile.exists()) {
					logFile.createNewFile();
					printWriter = null;
				}
				// Append buffer to the end of the file.
				FileWriter fw = new FileWriter(logFileName, true);
				PrintWriter printWriter = new PrintWriter(fw, true);
				printWriter.println(
					" ---------------------------------------------- ");
				printWriter.println(message);
				printWriter.close();
				fw.close();

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void checkLogFile() {

		if (printWriter != null)
			return;
		if (logFileName == null)
			return;
		try {
			File logFile = new File(logFileName);
			if (!logFile.exists()) {
				logFile.createNewFile();
				printWriter = null;
			}
			// Append buffer to the end of the file.
			if (printWriter == null) {
				FileWriter fw = new FileWriter(logFileName, true);
				printWriter = new PrintWriter(fw, true);
				printWriter.println("<debug>");
				printWriter.println("<![CDATA[ ");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void println(String message) {
	    char[] chars = message.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '\n')
				lineCount++;
		}
		checkLogFile();
		// String tname = Thread.currentThread().getName();
		if (printWriter != null) {
			printWriter.println(message);
		}
		lineCount++;
	}

	/** Log a message into the log file.
	     * @param message message to log into the log file.
	     */
	public void logMessage(String message) {
		if (!needsLogging)
			return;
		checkLogFile();
		println(message);
	}

	/** Set the trace level for the stack.
	 */
	public void setTraceLevel(int level) {
		traceLevel = level;
	}

	/** Get the trace level for the stack.
	 */
	public int getTraceLevel() {
		return traceLevel;
	}

}
/*
 * $Log: LogWriter.java,v $
 * Revision 1.9  2004/09/26 14:48:02  mranga
 * Submitted by:  John Martin
 * Reviewed by:   mranga
 *
 * Remove unnecssary synchronization.
 *
 * Revision 1.8  2004/09/01 02:04:33  xoba
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
 * Revision 1.7  2004/04/19 18:23:48  mranga
 * Reviewed by:   mranga
 * Fixed the tck (reset factories before getting the TI factories)
 *
 *
 * Revision 1.5  2004/01/22 13:26:27  sverker
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
