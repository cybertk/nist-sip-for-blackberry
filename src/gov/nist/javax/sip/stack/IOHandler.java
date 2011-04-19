/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 *******************************************************************************/
package gov.nist.javax.sip.stack;

import gov.nist.core.*;
import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.Enumeration;

// Added by Daniel J. Martinez Manzano <dani@dif.um.es>
import javax.net.ssl.SSLSocket;

/**
 * Low level Input output to a socket. Caches TCP connections and takes care of
 * re-connecting to the remote party if the other end drops the connection
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov><br/>TLS support Added by Daniel J.
 *         Martinez Manzano <dani@dif.um.es>
 * 
 * <a href=" {@docRoot}/uncopyright.html">This code is in the public domain.
 * </a>
 *  
 */

class IOHandler {

    private SIPMessageStack sipStack;

    private static String UDP = "udp";

    private static String TCP = "tcp";

    // Added by Daniel J. Martinez Manzano <dani@dif.um.es>
    private static String TLS = "tls";

    // A cache of client sockets that can be re-used for
    // sending tcp messages.
    private Hashtable socketTable;

    protected static String makeKey(InetAddress addr, int port) {
        return addr.getHostAddress() + ":" + port;

    }

    protected IOHandler(SIPMessageStack sipStack) {
        this.sipStack = sipStack;
        this.socketTable = new Hashtable();
    }

    protected synchronized void putSocket(String key, Socket sock) {
        socketTable.put(key, sock);
    }

    private synchronized Socket getSocket(String key) {
        return (Socket) socketTable.get(key);
    }

    private void removeSocket(String key) {
        socketTable.remove(key);
    }

    /**
     * A private function to write things out. This needs to be syncrhonized as
     * writes can occur from multiple threads. We write in chunks to allow the
     * other side to synchronize for large sized writes.
     */
    private void writeChunks(OutputStream outputStream, byte[] bytes, int length)
            throws IOException {
        // Chunk size is 16K - this hack is for large
        // writes over slow connections.
        synchronized (outputStream) {
            // outputStream.write(bytes,0,length);
            int chunksize = 8 * 1024;
            for (int p = 0; p < length; p += chunksize) {
                int chunk = p + chunksize < length ? chunksize : length - p;
                outputStream.write(bytes, p, chunk);
            }
        }
        outputStream.flush();
    }

    /**
     * Send an array of bytes.
     * 
     * @param inaddr --
     *            inet address
     * @param contactPort --
     *            port to connect to.
     * @param transport --
     *            tcp or udp.
     * @param retry --
     *            retry to connect if the other end closed connection
     * @throws IOException --
     *             if there is an IO exception sending message.
     */

    public Socket sendBytes(InetAddress inaddr, int contactPort,
            String transport, byte[] bytes, boolean retry) throws IOException {
        int retry_count = 0;
        int max_retry = retry ? 2 : 1;
        // Server uses TCP transport. TCP client sockets are cached
        int length = bytes.length;
        if (LogWriter.needsLogging) {
            sipStack.logWriter.logMessage("sendBytes " + transport + " inAddr "
                    + inaddr.getHostAddress() + " port = " + contactPort
                    + " length = " + length);
        }
        if (transport.compareToIgnoreCase(TCP) == 0) {
            String key = makeKey(inaddr, contactPort);
            // This should be in a synchronized block ( reported by
            // Jayashenkhar ( lucent ).
            synchronized (this.socketTable) {
                Socket clientSock = getSocket(key);
                while (retry_count < max_retry) {
                    if (clientSock == null) {
                        if (LogWriter.needsLogging) {
                            sipStack.logWriter.logMessage("inaddr = " + inaddr);
                            sipStack.logWriter.logMessage("port = "
                                    + contactPort);
                        }
                        clientSock = sipStack.getNetworkLayer().createSocket(
                                inaddr, contactPort,
                                sipStack.getRealIPAddress());
                        OutputStream outputStream = clientSock
                                .getOutputStream();
                        writeChunks(outputStream, bytes, length);
                        putSocket(key, clientSock);
                        break;
                    } else {
                        try {
                            OutputStream outputStream = clientSock
                                    .getOutputStream();
                            writeChunks(outputStream, bytes, length);
                            break;
                        } catch (IOException ex) {
                            if (LogWriter.needsLogging)
                                sipStack.logWriter.logException(ex);
                            // old connection is bad.
                            // remove from our table.
                            removeSocket(key);
                            try {
                                clientSock.close();
                            } catch (Exception e) {
                            }
                            clientSock = null;
                            retry_count++;
                        }
                    }
                }
                if (clientSock == null) {
                    throw new IOException("Could not connect to " + inaddr
                            + ":" + contactPort);
                } else
                    return clientSock;
            }

            // Added by Daniel J. Martinez Manzano <dani@dif.um.es>
            // Copied and modified from the former section for TCP
        } else if (transport.compareToIgnoreCase(TLS) == 0) {
            String key = makeKey(inaddr, contactPort);
            synchronized (this.socketTable) {
                SSLSocket clientSock = (SSLSocket) getSocket(key);
                while (retry_count < max_retry) {
                    if (clientSock == null) {
                        if (LogWriter.needsLogging) {
                            sipStack.logWriter.logMessage("inaddr = " + inaddr);
                            sipStack.logWriter.logMessage("port = "
                                    + contactPort);
                        }
                        clientSock = sipStack.getNetworkLayer()
                                .createSSLSocket(inaddr, contactPort,
                                        sipStack.getRealIPAddress());
                        OutputStream outputStream = clientSock
                                .getOutputStream();
                        writeChunks(outputStream, bytes, length);
                        putSocket(key, clientSock);
                        break;
                    } else {
                        try {
                            OutputStream outputStream = clientSock
                                    .getOutputStream();
                            writeChunks(outputStream, bytes, length);
                            break;
                        } catch (IOException ex) {
                            if (LogWriter.needsLogging)
                                sipStack.logWriter.logException(ex);
                            // old connection is bad.
                            // remove from our table.
                            removeSocket(key);
                            try {
                                clientSock.close();
                            } catch (Exception e) {
                            }
                            clientSock = null;
                            retry_count++;
                        }
                    }
                }
                if (clientSock == null) {
                    throw new IOException("Could not connect to " + inaddr
                            + ":" + contactPort);
                } else
                    return clientSock;

            }

        } else {
            // This is a UDP transport...
            DatagramSocket datagramSock = sipStack.getNetworkLayer()
                    .createDatagramSocket();
            datagramSock.connect(inaddr, contactPort);
            DatagramPacket dgPacket = new DatagramPacket(bytes, 0, length,
                    inaddr, contactPort);
            datagramSock.send(dgPacket);
            datagramSock.close();
            return null;
        }

    }

    /**
     * Close all the cached connections.
     */
    public void closeAll() {
        for (Enumeration values = socketTable.elements(); values
                .hasMoreElements();) {
            Socket s = (Socket) values.nextElement();
            try {
                s.close();
            } catch (IOException ex) {
            }
        }

    }

}
/*
 * $Log: IOHandler.java,v $
 * Revision 1.29  2005/06/08 21:42:29  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  mranga
 *
 * Fixed to tag re-entrancy problem
 * Reviewed by:
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
 * Revision 1.28 2004/12/12 23:47:19 mranga Issue
 * number: 46 Submitted by: espen Reviewed by: M. Ranganathan
 * 
 * Use the local ip address when creating outbound TCP and TLS sockets.
 * 
 * Revision 1.27 2004/12/01 19:05:15 mranga Reviewed by: mranga Code cleanup
 * remove the unused SIMULATION code to reduce the clutter. Fix bug in Dialog
 * state machine.
 * 
 * Revision 1.26 2004/10/28 19:02:51 mranga Submitted by: Daniel Martinez
 * Reviewed by: M. Ranganathan
 * 
 * Added changes for TLS support contributed by Daniel Martinez
 * 
 * Revision 1.25 2004/09/01 02:04:15 xoba Issue number: no particular issue
 * number.
 * 
 * this code passes TCK
 * 
 * fixed multiple javadoc errors throughout javax.* and gov.nist.*
 * 
 * added junit and log4j jars to cvs module, although log4j is not being used
 * yet.
 * 
 * modified and expanded build.xml and fixed javadoc reference to outdated jre
 * documentation (now javadocs hyperlink to jre api documentation). since
 * top-level 'docs' directory already contains cvs-controlled files, i
 * redirected output of javadocs to their own separate directories, which are
 * 'cleaned' along with 'clean' target. also created other javadoc which just
 * outputs javax.* classes for those wishing to develop sip applications without
 * reference to nist.gov.*.
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
 * CVS: Issue number: CVS: If this change addresses one or more issues, CVS:
 * then enter the issue number(s) here. CVS: Obtained from: CVS: If this change
 * has been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 * 
 * Revision 1.24 2004/08/30 16:04:47 mranga Submitted by: Mike Andrews Reviewed
 * by: mranga
 * 
 * Added a network layer.
 * 
 * Revision 1.23 2004/07/23 06:50:04 mranga Submitted by: mranga Reviewed by:
 * mranga
 * 
 * Clean up - Get rid of annoying eclipse warnings.
 * 
 * Revision 1.22 2004/06/21 04:59:50 mranga Refactored code - no functional
 * changes.
 * 
 * Revision 1.21 2004/04/16 21:30:22 mranga Reviewed by: mranga ignore close
 * exception.
 * 
 * Revision 1.20 2004/04/02 19:36:19 mranga Reviewed by: mranga
 * 
 * Post check for possible race condition when a listener runs for a long time
 * and a duplicate request arrives in that time window was generalized to
 * transactions that dont necessarily belong in any dialog.
 * 
 * Revision 1.19 2004/03/30 15:17:38 mranga Reviewed by: mranga Added
 * reInitialization for stack in support of applets.
 * 
 * Revision 1.18 2004/03/26 21:53:46 mranga Reviewed by: mranga Remove unused
 * function.
 * 
 * Revision 1.17 2004/03/25 16:37:00 mranga Reviewed by: mranga Fix up for
 * logging messages.
 * 
 * Revision 1.16 2004/03/23 16:16:51 mranga Reviewed by: mranga more TCP IO
 * performance hacking.
 * 
 * Revision 1.15 2004/03/22 12:53:36 mranga Reviewed by: mranga Add back
 * synchronization on output stream for writes.
 * 
 * Revision 1.14 2004/03/19 23:41:30 mranga Reviewed by: mranga Fixed connection
 * and thread caching.
 * 
 * Revision 1.13 2004/03/19 04:22:22 mranga Reviewed by: mranga Added IO Pacing
 * for long writes - split write into chunks and flush after each chunk to avoid
 * socket back pressure.
 * 
 * Revision 1.12 2004/03/18 22:01:20 mranga Reviewed by: mranga Get rid of the
 * PipedInputStream from pipelined parser to avoid a copy.
 * 
 * Revision 1.11 2004/03/07 22:25:24 mranga Reviewed by: mranga Added a new
 * configuration parameter that instructs the stack to drop a server connection
 * after server transaction termination set
 * gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false for this Default behavior
 * is true.
 * 
 * Revision 1.10 2004/01/22 18:39:41 mranga Reviewed by: M. Ranganathan Moved
 * the ifdef SIMULATION and associated tags to the first column so Prep
 * preprocessor can deal with them.
 * 
 * Revision 1.9 2004/01/22 13:26:33 sverker Issue number: Obtained from:
 * Submitted by: sverker Reviewed by: mranga
 * 
 * Major reformat of code to conform with style guide. Resolved compiler and
 * javadoc warnings. Added CVS tags.
 * 
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number: CVS: If this change addresses one or more issues, CVS:
 * then enter the issue number(s) here. CVS: Obtained from: CVS: If this change
 * has been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 *  
 */
