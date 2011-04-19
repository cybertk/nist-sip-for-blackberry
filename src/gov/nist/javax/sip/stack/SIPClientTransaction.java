package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.*;
import gov.nist.core.*;
import gov.nist.javax.sip.SIPConstants;
import javax.sip.message.*;
import java.util.*;
import gov.nist.javax.sip.address.*;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;

import java.io.IOException;

/**
 * Represents a client transaction. Implements the following state machines.
 * (From RFC 3261)
 * 
 * <pre>
 * 
 *  
 *   
 *   
 *   
 *                                  |INVITE from TU
 *                Timer A fires     |INVITE sent
 *                Reset A,          V                      Timer B fires
 *                INVITE sent +-----------+                or Transport Err.
 *                  +---------|           |---------------+inform TU
 *                  |         |  Calling  |               |
 *                  +--------&gt;|           |--------------&gt;|
 *                            +-----------+ 2xx           |
 *                               |  |       2xx to TU     |
 *                               |  |1xx                  |
 *       300-699 +---------------+  |1xx to TU            |
 *      ACK sent |                  |                     |
 *   resp. to TU |  1xx             V                     |
 *               |  1xx to TU  -----------+               |
 *               |  +---------|           |               |
 *               |  |         |Proceeding |--------------&gt;|
 *               |  +--------&gt;|           | 2xx           |
 *               |            +-----------+ 2xx to TU     |
 *               |       300-699    |                     |
 *               |       ACK sent,  |                     |
 *               |       resp. to TU|                     |
 *               |                  |                     |      NOTE:
 *               |  300-699         V                     |
 *               |  ACK sent  +-----------+Transport Err. |  transitions
 *               |  +---------|           |Inform TU      |  labeled with
 *               |  |         | Completed |--------------&gt;|  the event
 *               |  +--------&gt;|           |               |  over the action
 *               |            +-----------+               |  to take
 *               |              &circ;   |                     |
 *               |              |   | Timer D fires       |
 *               +--------------+   | -                   |
 *                                  |                     |
 *                                  V                     |
 *                            +-----------+               |
 *                            |           |               |
 *                            | Terminated|&lt;--------------+
 *                            |           |
 *                            +-----------+
 *   
 *                    Figure 5: INVITE client transaction
 *   
 *   
 *                                      |Request from TU
 *                                      |send request
 *                  Timer E             V
 *                  send request  +-----------+
 *                      +---------|           |-------------------+
 *                      |         |  Trying   |  Timer F          |
 *                      +--------&gt;|           |  or Transport Err.|
 *                                +-----------+  inform TU        |
 *                   200-699         |  |                         |
 *                   resp. to TU     |  |1xx                      |
 *                   +---------------+  |resp. to TU              |
 *                   |                  |                         |
 *                   |   Timer E        V       Timer F           |
 *                   |   send req +-----------+ or Transport Err. |
 *                   |  +---------|           | inform TU         |
 *                   |  |         |Proceeding |------------------&gt;|
 *                   |  +--------&gt;|           |-----+             |
 *                   |            +-----------+     |1xx          |
 *                   |              |      &circ;        |resp to TU   |
 *                   | 200-699      |      +--------+             |
 *                   | resp. to TU  |                             |
 *                   |              |                             |
 *                   |              V                             |
 *                   |            +-----------+                   |
 *                   |            |           |                   |
 *                   |            | Completed |                   |
 *                   |            |           |                   |
 *                   |            +-----------+                   |
 *                   |              &circ;   |                         |
 *                   |              |   | Timer K                 |
 *                   +--------------+   | -                       |
 *                                      |                         |
 *                                      V                         |
 *                NOTE:           +-----------+                   |
 *                                |           |                   |
 *            transitions         | Terminated|&lt;------------------+
 *            labeled with        |           |
 *            the event           +-----------+
 *            over the action
 *            to take
 *   
 *                    Figure 6: non-INVITE client transaction
 *   
 *   
 *   
 *   
 *  
 * </pre>
 * 
 * @author Jeff Keyser
 * @author M. Ranganathan <mranga@nist.gov>
 * @author Bug fixes by Emil Ivov. <a href=" {@docRoot}/uncopyright.html">This
 *         code is in the public domain. </a>
 * 
 * @version JAIN-SIP-1.1 $Revision: 1.47 $ $Date: 2005/05/06 15:06:50 $
 */
public class SIPClientTransaction extends SIPTransaction implements
        ServerResponseInterface, javax.sip.ClientTransaction, PendingRecord {

    // max # of pending responses that can we can buffer (to avoid
    // response flooding DOS attack).
    private static final int MAX_PENDING_RESPONSES = 4;

    private LinkedList pendingResponses;

    private SIPRequest lastRequest;

    private int viaPort;

    private String viaHost;

    // Real ResponseInterface to pass messages to
    private ServerResponseInterface respondTo;

    class PendingResponse {
        protected SIPResponse sipResponse;

        protected MessageChannel messageChannel;

        public PendingResponse(SIPResponse sipResponse,
                MessageChannel messageChannel) {
            this.sipResponse = sipResponse;
            this.messageChannel = messageChannel;
        }
    }

    public class TransactionTimer extends TimerTask {
        protected SIPClientTransaction clientTransaction;

        protected SIPTransactionStack sipStack;

        public TransactionTimer(SIPClientTransaction clientTransaction) {
            this.clientTransaction = clientTransaction;
            this.sipStack = clientTransaction.sipStack;

        }

        public void run() {

            // If the transaction has terminated,
            if (clientTransaction.isTerminated()) {

                if (LogWriter.needsLogging) {
                    sipStack.logWriter.logMessage("removing  = "
                            + clientTransaction + " isReliable "
                            + clientTransaction.isReliable());
                }
                sipStack.removeTransaction(clientTransaction);

                try {
                    this.cancel();
                } catch (IllegalStateException ex) {
                    if (!this.sipStack.isAlive())
                        return;
                }

                // Client transaction terminated. Kill connection if
                // this is a TCP after the linger timer has expired.
                // The linger timer is needed to allow any pending requests to
                // return responses.
                if ((!this.sipStack.cacheClientConnections)
                        && clientTransaction.isReliable()) {
                    // Section changed by Daniel J. Martinez Manzano
                    // <dani@dif.um.es>
                    // Added support for TLS message channel
                    int newUseCount;

                    if (clientTransaction.encapsulatedChannel instanceof TCPMessageChannel)
                        newUseCount = --((TCPMessageChannel) clientTransaction.encapsulatedChannel).useCount;
                    else
                        newUseCount = --((TLSMessageChannel) clientTransaction.encapsulatedChannel).useCount;
                    if (newUseCount == 0) {
                        // Let the connection linger for a while and then close
                        // it.
                        this.clientTransaction.myTimer = new LingerTimer(
                                this.clientTransaction);
                        sipStack.timer
                                .schedule(
                                        myTimer,
                                        SIPTransactionStack.CONNECTION_LINGER_TIME * 1000);
                    }
                } else {
                    // Cache the client connections so dont close the
                    // connection.
                    if (LogWriter.needsLogging
                            && clientTransaction.isReliable()) {
                        // Section changed by Daniel J. Martinez Manzano
                        // <dani@dif.um.es>
                        // Added support for TLS message channel

                        int UseCount;

                        if (clientTransaction.encapsulatedChannel instanceof TCPMessageChannel)
                            UseCount = ((TCPMessageChannel) clientTransaction.encapsulatedChannel).useCount;
                        else
                            UseCount = ((TLSMessageChannel) clientTransaction.encapsulatedChannel).useCount;

                        if (LogWriter.needsLogging) {
                            sipStack.logWriter.logMessage("Client Use Count = "
                                    + UseCount);
                        }
                    }
                }

                // If this transaction has not
                // terminated,
            } else {
                // Fire the transaction timer.
                clientTransaction.fireTimer();

            }

        }

    }

    /**
     * Creates a new client transaction.
     * 
     * @param newSIPStack
     *            Transaction stack this transaction belongs to.
     * @param newChannelToUse
     *            Channel to encapsulate.
     */
    protected SIPClientTransaction(SIPTransactionStack newSIPStack,
            MessageChannel newChannelToUse) {
        super(newSIPStack, newChannelToUse);
        // Create a random branch parameter for this transaction
        // setBranch( SIPConstants.BRANCH_MAGIC_COOKIE +
        // Integer.toHexString( hashCode( ) ) );
        setBranch(Utils.generateBranchId());
        if (LogWriter.needsLogging) {
            sipStack.logWriter.logMessage("Creating clientTransaction " + this);
            sipStack.logWriter.logStackTrace();
        }
        this.pendingResponses = new LinkedList();

    }

    /**
     * Sets the real ResponseInterface this transaction encapsulates.
     * 
     * @param newRespondTo
     *            ResponseInterface to send messages to.
     */
    public void setResponseInterface(ServerResponseInterface newRespondTo) {

        respondTo = newRespondTo;

    }

    public String getProcessingInfo() {

        return respondTo.getProcessingInfo();

    }

    /**
     * Returns this transaction.
     */
    public MessageChannel getRequestChannel() {

        return this;

    }

    /**
     * Deterines if the message is a part of this transaction.
     * 
     * @param messageToTest
     *            Message to check if it is part of this transaction.
     * 
     * @return True if the message is part of this transaction, false if not.
     */
    public boolean isMessagePartOfTransaction(SIPMessage messageToTest) {

        // List of Via headers in the message to test
        ViaList viaHeaders = messageToTest.getViaHeaders();
        // Flags whether the select message is part of this transaction
        boolean transactionMatches;
        String messageBranch = ((Via) viaHeaders.getFirst()).getBranch();
        boolean rfc3261Compliant = getBranch() != null && messageBranch != null
                && getBranch().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE)
                && messageBranch.startsWith(SIPConstants.BRANCH_MAGIC_COOKIE);

        transactionMatches = false;
        if (TransactionState.COMPLETED == this.getState()) {
            if (rfc3261Compliant) {
                transactionMatches = getBranch().equals(
                        ((Via) viaHeaders.getFirst()).getBranch())
                        && getMethod().equals(
                                messageToTest.getCSeq().getMethod());
            } else {
                transactionMatches = getBranch().equals(
                        messageToTest.getTransactionId());
            }
        } else if (!isTerminated()) {
            if (rfc3261Compliant) {
                if (viaHeaders != null) {
                    // If the branch parameter is the
                    //same as this transaction and the method is the same,
                    if (getBranch().equals(
                            ((Via) viaHeaders.getFirst()).getBranch())) {
                        transactionMatches = getOriginalRequest().getCSeq()
                                .getMethod().equals(
                                        messageToTest.getCSeq().getMethod());

                    }
                }
            } else {
                // not RFC 3261 compliant.
                if (getBranch() != null) {
                    transactionMatches = getBranch().equals(
                            messageToTest.getTransactionId());
                } else {
                    transactionMatches = getOriginalRequest()
                            .getTransactionId().equals(
                                    messageToTest.getTransactionId());
                }

            }

        }
        return transactionMatches;

    }

    /**
     * Send a request message through this transaction and onto the client.
     * 
     * @param messageToSend
     *            Request to process and send.
     */
    public void sendMessage(SIPMessage messageToSend) throws IOException {

        // Message typecast as a request
        SIPRequest transactionRequest;

        transactionRequest = (SIPRequest) messageToSend;

        // Set the branch id for the top via header.
        Via topVia = (Via) transactionRequest.getViaHeaders().getFirst();
        // Tack on a branch identifier to match responses.
        try {
            topVia.setBranch(getBranch());
        } catch (java.text.ParseException ex) {
        }

        // If this is the first request for this transaction,
        if (TransactionState.PROCEEDING == getState()
                || TransactionState.CALLING == getState()) {

            // If this is a TU-generated ACK request,
            if (transactionRequest.getMethod().equals(Request.ACK)) {
                // Send directly to the underlying
                // transport and close this transaction
                // Bug fix by Emil Ivov
                if (isReliable()) {
                    this.setState(TransactionState.TERMINATED);
                } else {
                    this.setState(TransactionState.COMPLETED);
                }
                super.sendMessage(transactionRequest);
                return;

            }

        }
        try {

            // Send the message to the server
            lastRequest = transactionRequest;
            if (getState() == null) {
                // Save this request as the one this transaction
                // is handling
                setOriginalRequest(transactionRequest);
                // Change to trying/calling state
                if (transactionRequest.getMethod().equals(Request.INVITE)) {
                    this.setState(TransactionState.CALLING);
                } else if (transactionRequest.getMethod().equals(Request.ACK)) {
                    // Acks are never retransmitted.
                    this.setState(TransactionState.TERMINATED);
                } else {
                    this.setState(TransactionState.TRYING);
                }
                if (!isReliable()) {
                    enableRetransmissionTimer();
                }
                if (isInviteTransaction()) {
                    enableTimeoutTimer(TIMER_B);
                } else {
                    enableTimeoutTimer(TIMER_F);
                }
            }
            // Set state first to avoid race condition..
            super.sendMessage(transactionRequest);

        } catch (IOException e) {

            this.setState(TransactionState.TERMINATED);
            throw e;

        }

    }

    /**
     * Process a new response message through this transaction. If necessary,
     * this message will also be passed onto the TU.
     * 
     * @param transactionResponse
     *            Response to process.
     * @param sourceChannel
     *            Channel that received this message.
     */
    public synchronized void processResponse(SIPResponse transactionResponse,
            MessageChannel sourceChannel) {
        // Log the incoming response in our log file.
        if (sipStack.serverLog.needsLogging(ServerLog.TRACE_MESSAGES))
            this.logResponse(transactionResponse, System.currentTimeMillis(),
                    "normal processing");

        // If the state has not yet been assigned then this is a
        // spurious response.
        if (getState() == null)
            return;

        // Ignore 1xx
        if (TransactionState.COMPLETED == this.getState()
                && transactionResponse.getStatusCode() / 100 == 1) {
            return;
        } else if (TransactionState.PROCEEDING == this.getState()
                && transactionResponse.getStatusCode() == 100) {
            // Ignore 100 if received after 180
            // bug report from Peter Parnes.
            processPending();
        }
        // Defer processing if a previous event has been placed in the
        // processing queue.
        // bug shows up on fast dual processor machines where a subsequent
        // response
        // arrives before a previous one completes processing.
        if (this.eventPending) {
            if (LogWriter.needsLogging) {
                sipStack.logWriter
                        .logMessage("Discarding early arriving Response "
                                + transactionResponse.getFirstLine());
            }
            //
            // This was leading to infinite loop. Bug fix was
            // suggested by Jordan Schidlowski ( from mdci.ca )

            synchronized (this.pendingResponses) {
                if (this.pendingResponses.size() < MAX_PENDING_RESPONSES) {
                    this.pendingResponses.add(new PendingResponse(
                            transactionResponse, sourceChannel));
                }
            }
            sipStack.putPending(this);
            return;
        }

        if (LogWriter.needsLogging)
            sipStack.logWriter.logMessage("processing "
                    + transactionResponse.getFirstLine() + "current state = "
                    + getState());

        this.lastResponse = transactionResponse;

        if (dialog != null) {
            // add the route before you process the response.
            // Bug noticed by Brad Templeton.
            dialog.addRoute(transactionResponse);
        }
        String method = transactionResponse.getCSeq().getMethod();
        if (dialog != null) {
            boolean added = false;
            SIPTransactionStack sipStackImpl = (SIPTransactionStack) getSIPStack();

            // A tag just got assigned or changed. To tag is mandatory for final
            // response
            if (dialog.getRemoteTag() == null
                    && transactionResponse.getTo().getTag() != null) {

                // Dont assign tag on provisional response
                if (transactionResponse.getStatusCode() != 100) {
                    dialog.setRemoteTag(transactionResponse.getToTag());
                }
                String dialogId = transactionResponse.getDialogId(false);
                dialog.setDialogId(dialogId);
                if (sipStackImpl.isDialogCreated(method)
                        && transactionResponse.getStatusCode() != 100) {
                    sipStackImpl.putDialog(dialog);
                    added = true;
                }

            } else if (dialog.getRemoteTag() != null
                    && transactionResponse.getToTag() != null
                    && !dialog.getRemoteTag().equals(
                            transactionResponse.getToTag())) {
                String dialogId = transactionResponse.getDialogId(false);
                dialog.setRemoteTag(transactionResponse.getToTag());
                dialog.setDialogId(dialogId);
                if (sipStackImpl.isDialogCreated(method)) {
                    sipStackImpl.putDialog(dialog);
                    added = true;
                }
            }

            // Adjust state of the Dialog state machine.
            if (sipStackImpl.isDialogCreated(method) ) {
                // Make a final tag assignment.
                if (dialog.getState() == null
                        && transactionResponse.getStatusCode() / 100 == 1) {
                    dialog.setState(SIPDialog.EARLY_STATE);
                } else if (transactionResponse.getToTag() != null
                        && transactionResponse.getStatusCode() / 100 == 2) {
                    // This is a dialog creating method (such as INVITE).
                    // 2xx response -- set the state to the confirmed
                    // state. To tag is MANDATORY for the response.
                    dialog.setRemoteTag(transactionResponse.getToTag());
                    dialog.setState(SIPDialog.CONFIRMED_STATE);
                } else if (transactionResponse.getStatusCode() >= 300
                        && transactionResponse.getStatusCode() <= 699
                        && (dialog.getState() == null || 
                           (dialog.getMethod().equals(this.getMethod()) &&
                            dialog.getState().getValue() == SIPDialog.EARLY_STATE))) {
                    // This case handles 3xx, 4xx, 5xx and 6xx responses.
                    // RFC 3261 Section 12.3 - dialog termination.
                    // Independent of the method, if a request outside of a
                    // dialog generates
                    // a non-2xx final response, any early dialogs created
                    // through
                    // provisional responses to that request are terminated.
                    dialog.setState(SIPDialog.TERMINATED_STATE);
                }
            }

            // Only terminate the dialog on 200 OK response to BYE
            if (this.getMethod().equals(Request.BYE)
                    && transactionResponse.getStatusCode() == 200) {
                dialog.setState(SIPDialog.TERMINATED_STATE);
            }
        }
        try {
            if (isInviteTransaction())
                inviteClientTransaction(transactionResponse, sourceChannel);
            else
                nonInviteClientTransaction(transactionResponse, sourceChannel);
        } catch (IOException ex) {
            this.setState(TransactionState.TERMINATED);
            raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR);
        }
    }

    /**
     * Implements the state machine for invite client transactions.
     * 
     * <pre>
     * 
     *  
     *   
     *   
     *                                      |Request from TU
     *                                      |send request
     *                  Timer E             V
     *                  send request  +-----------+
     *                      +---------|           |-------------------+
     *                      |         |  Trying   |  Timer F          |
     *                      +--------&gt;|           |  or Transport Err.|
     *                                +-----------+  inform TU        |
     *                   200-699         |  |                         |
     *                   resp. to TU     |  |1xx                      |
     *                   +---------------+  |resp. to TU              |
     *                   |                  |                         |
     *                   |   Timer E        V       Timer F           |
     *                   |   send req +-----------+ or Transport Err. |
     *                   |  +---------|           | inform TU         |
     *                   |  |         |Proceeding |------------------&gt;|
     *                   |  +--------&gt;|           |-----+             |
     *                   |            +-----------+     |1xx          |
     *                   |              |      &circ;        |resp to TU   |
     *                   | 200-699      |      +--------+             |
     *                   | resp. to TU  |                             |
     *                   |              |                             |
     *                   |              V                             |
     *                   |            +-----------+                   |
     *                   |            |           |                   |
     *                   |            | Completed |                   |
     *                   |            |           |                   |
     *                   |            +-----------+                   |
     *                   |              &circ;   |                         |
     *                   |              |   | Timer K                 |
     *                   +--------------+   | -                       |
     *                                      |                         |
     *                                      V                         |
     *                NOTE:           +-----------+                   |
     *                                |           |                   |
     *            transitions         | Terminated|&lt;------------------+
     *            labeled with        |           |
     *            the event           +-----------+
     *            over the action
     *            to take
     *   
     *                    Figure 6: non-INVITE client transaction
     *   
     *   
     *  
     * </pre>
     * 
     * @param transactionResponse --
     *            transaction response received.
     * @param sourceChannel -
     *            source channel on which the response was received.
     */
    private void nonInviteClientTransaction(SIPResponse transactionResponse,
            MessageChannel sourceChannel) throws IOException {
        int statusCode = transactionResponse.getStatusCode();
        if (TransactionState.TRYING == this.getState()) {
            if (statusCode / 100 == 1) {
                this.setState(TransactionState.PROCEEDING);
                enableRetransmissionTimer(MAXIMUM_RETRANSMISSION_TICK_COUNT);
                enableTimeoutTimer(TIMER_F);
                // According to RFC, the TU has to be informed on
                // this transition. Bug report by Emil Ivov
                if (respondTo != null)
                    respondTo.processResponse(transactionResponse, this);
            } else if (200 <= statusCode && statusCode <= 699) {
                // Send the response up to the TU.
                if (respondTo != null)
                    respondTo.processResponse(transactionResponse, this);
                if (!isReliable()) {
                    this.setState(TransactionState.COMPLETED);
                    enableTimeoutTimer(TIMER_K);
                } else {
                    this.setState(TransactionState.TERMINATED);
                }
            }
        } else if (TransactionState.PROCEEDING == this.getState()) {
            // Bug fixes by Emil Ivov
            if (statusCode / 100 == 1) {
                if (respondTo != null)
                    respondTo.processResponse(transactionResponse, this);
            } else if (200 <= statusCode && statusCode <= 699) {
                if (respondTo != null)
                    respondTo.processResponse(transactionResponse, this);
                disableRetransmissionTimer();
                disableTimeoutTimer();
                if (!isReliable()) {
                    this.setState(TransactionState.COMPLETED);
                    enableTimeoutTimer(TIMER_K);
                } else {
                    this.setState(TransactionState.TERMINATED);
                }
            }
        } else {
            if (LogWriter.needsLogging) {
                getSIPStack().logWriter
                        .logMessage(" Not sending response to TU! "
                                + getState());
            }
        }
    }

    /**
     * Implements the state machine for invite client transactions.
     * 
     * <pre>
     * 
     *  
     *   
     *   
     *                                  |INVITE from TU
     *                Timer A fires     |INVITE sent
     *                Reset A,          V                      Timer B fires
     *                INVITE sent +-----------+                or Transport Err.
     *                  +---------|           |---------------+inform TU
     *                  |         |  Calling  |               |
     *                  +--------&gt;|           |--------------&gt;|
     *                            +-----------+ 2xx           |
     *                               |  |       2xx to TU     |
     *                               |  |1xx                  |
     *       300-699 +---------------+  |1xx to TU            |
     *      ACK sent |                  |                     |
     *   resp. to TU |  1xx             V                     |
     *               |  1xx to TU  -----------+               |
     *               |  +---------|           |               |
     *               |  |         |Proceeding |--------------&gt;|
     *               |  +--------&gt;|           | 2xx           |
     *               |            +-----------+ 2xx to TU     |
     *               |       300-699    |                     |
     *               |       ACK sent,  |                     |
     *               |       resp. to TU|                     |
     *               |                  |                     |      NOTE:
     *               |  300-699         V                     |
     *               |  ACK sent  +-----------+Transport Err. |  transitions
     *               |  +---------|           |Inform TU      |  labeled with
     *               |  |         | Completed |--------------&gt;|  the event
     *               |  +--------&gt;|           |               |  over the action
     *               |            +-----------+               |  to take
     *               |              &circ;   |                     |
     *               |              |   | Timer D fires       |
     *               +--------------+   | -                   |
     *                                  |                     |
     *                                  V                     |
     *                            +-----------+               |
     *                            |           |               |
     *                            | Terminated|&lt;--------------+
     *                            |           |
     *                            +-----------+
     *   
     *   
     *  
     * </pre>
     * 
     * @param transactionResponse --
     *            transaction response received.
     * @param sourceChannel -
     *            source channel on which the response was received.
     */

    private void inviteClientTransaction(SIPResponse transactionResponse,
            MessageChannel sourceChannel) throws IOException {
        int statusCode = transactionResponse.getStatusCode();
        if (TransactionState.TERMINATED == this.getState()) {
            // Do nothing in the terminated state.
            return;
        } else if (TransactionState.CALLING == this.getState()) {
            if (statusCode / 100 == 2) {
                // 200 responses are always seen by TU.
                if (respondTo != null)
                    respondTo.processResponse(transactionResponse, this);
                disableRetransmissionTimer();
                disableTimeoutTimer();
                this.setState(TransactionState.TERMINATED);
            } else if (statusCode / 100 == 1) {
                disableRetransmissionTimer();
                disableTimeoutTimer();
                if (respondTo != null)
                    respondTo.processResponse(transactionResponse, this);
                this.setState(TransactionState.PROCEEDING);
            } else if (300 <= statusCode && statusCode <= 699) {
                // Send back an ACK request (do this before calling the
                // application (bug noticed by Andreas Bystrom).
                try {
                    sendMessage((SIPRequest) createAck());
                } catch (SipException ex) {
                    InternalErrorHandler.handleException(ex);
                }
                // When in either the "Calling" or "Proceeding" states,
                // reception of response with status code from 300-699
                // MUST cause the client transaction to
                // transition to "Completed".
                // The client transaction MUST pass the received response up to
                // the TU, and the client transaction MUST generate an
                // ACK request.

                if (respondTo != null)
                    respondTo.processResponse(transactionResponse, this);

                if (!isReliable()) {
                    this.setState(TransactionState.COMPLETED);
                    enableTimeoutTimer(TIMER_D);
                } else {
                    //Proceed immediately to the TERMINATED state.
                    this.setState(TransactionState.TERMINATED);
                }
            }
        } else if (TransactionState.PROCEEDING == this.getState()) {
            if (statusCode / 100 == 1) {
                if (respondTo != null)
                    respondTo.processResponse(transactionResponse, this);
            } else if (statusCode / 100 == 2) {
                this.setState(TransactionState.TERMINATED);
                if (respondTo != null)
                    respondTo.processResponse(transactionResponse, this);
            } else if (300 <= statusCode && statusCode <= 699) {
                // Send back an ACK request
                try {
                    sendMessage((SIPRequest) createAck());
                } catch (SipException ex) {
                    InternalErrorHandler.handleException(ex);
                }
                // Pass up to the TU for processing.
                if (respondTo != null)
                    respondTo.processResponse(transactionResponse, this);
                if (!isReliable()) {
                    this.setState(TransactionState.COMPLETED);
                    enableTimeoutTimer(TIMER_D);
                } else {
                    this.setState(TransactionState.TERMINATED);
                }
            }
        } else if (TransactionState.COMPLETED == this.getState()) {
            if (300 <= statusCode && statusCode <= 699) {
                // Send back an ACK request
                try {
                    sendMessage((SIPRequest) createAck());
                } catch (SipException ex) {
                    InternalErrorHandler.handleException(ex);
                }
            }

        }

    }

    /**
     * Sends specified {@link javax.sip.message.Request}on a unique client
     * transaction identifier. This method implies that the application is
     * functioning as either a User Agent Client or a Stateful proxy, hence the
     * underlying SipProvider acts statefully.
     * <p>
     * JAIN SIP defines a retransmission utility specific to user agent
     * behaviour and the default retransmission behaviour for each method.
     * <p>
     * When an application wishes to send a message, it creates a Request
     * message passes that Request to this method, this method returns the
     * cleintTransactionId generated by the SipProvider. The Request message
     * gets sent via the ListeningPoint that this SipProvider is attached to.
     * <ul>
     * <li>User Agent Client - must not send a BYE on a confirmed INVITE until
     * it has received an ACK for its 2xx response or until the server
     * transaction times out.
     * </ul>
     * 
     * @throws SipException
     *             if implementation cannot send request for any reason
     */
    public void sendRequest() throws SipException {
        SIPRequest sipRequest = this.getOriginalRequest();
        try {
            // Only map this after the fist request is sent out.
            this.isMapped = true;
            this.sendMessage(sipRequest);
        } catch (IOException ex) {
            throw new SipException(ex.getMessage());
        }

    }

    /**
     * Called by the transaction stack when a retransmission timer fires.
     */
    protected void fireRetransmissionTimer() {

        try {

            //if ( LogWriter.needsLogging) {
            //    sipStack.getLogWriter().logMessage( " fireRetransmitTimer: state
            // = " + this.getState() +
            //            " isMapped = " + this.isMapped );
            //}

            // Resend the last request sent
            if (this.getState() == null || !this.isMapped)
                return;
            if (TransactionState.CALLING == this.getState()
                    || TransactionState.TRYING == this.getState()) {
                // If the retransmission filter is disabled then
                // retransmission of the INVITE is the application
                // responsibility.
                if ((!(((SIPTransactionStack) getSIPStack()).retransmissionFilter))
                        && this.isInviteTransaction()) {
                    raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_RETRANSMIT);
                } else {
                    // Could have allocated the transaction but not yet
                    // sent out a request (Bug report by Dave Stuart).
                    //if (LogWriter.needsLogging)
                    //    sipStack.getLogWriter().logMessage("lastRequest = " +
                    // lastRequest);

                    if (lastRequest != null)
                        super.sendMessage(lastRequest);
                }
            }
        } catch (IOException e) {
            this.setState(TransactionState.TERMINATED);
            raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR);
        }

    }

    /**
     * Called by the transaction stack when a timeout timer fires.
     */
    protected void fireTimeoutTimer() {

        if (LogWriter.needsLogging)
            sipStack.logWriter.logMessage("fireTimeoutTimer " + this);

        SIPDialog dialogImpl = this.dialog;
        if (TransactionState.CALLING == this.getState()
                || TransactionState.TRYING == this.getState()
                || TransactionState.PROCEEDING == this.getState()) {
            // Timeout occured. If this is asociated with a transaction
            // creation then kill the dialog.
            if (dialogImpl != null) {
                if (((SIPTransactionStack) getSIPStack()).isDialogCreated(this
                        .getOriginalRequest().getMethod())) {
                    // terminate the enclosing dialog.
                    dialogImpl.setState(SIPDialog.TERMINATED_STATE);
                } else if (getOriginalRequest().getMethod().equalsIgnoreCase(
                        Request.BYE)) {
                    // Terminate the associated dialog on BYE Timeout.
                    dialogImpl.setState(SIPDialog.TERMINATED_STATE);
                }
            }
        }
        if (TransactionState.COMPLETED != this.getState()) {
            raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_ERROR);
        } else {
            this.setState(TransactionState.TERMINATED);
        }

    }

    /**
     * Creates a new Cancel message from the Request associated with this client
     * transaction. The CANCEL request, is used to cancel the previous request
     * sent by this client transaction. Specifically, it asks the UAS to cease
     * processing the request and to generate an error response to that request.
     * 
     * @return a cancel request generated from the original request.
     */
    public Request createCancel() throws SipException {
        SIPRequest originalRequest = this.getOriginalRequest();
        if (originalRequest == null)
            throw new SipException("Bad state " + getState());
        if (originalRequest.getMethod().equalsIgnoreCase(Request.ACK))
            throw new SipException("Cannot Cancel ACK!");
        else
            return originalRequest.createCancelRequest();
    }

    /**
     * Creates an ACK request for this transaction
     * 
     * @return an ack request generated from the original request.
     * 
     * @throws SipException
     *             if transaction is in the wrong state to be acked.
     */
    public Request createAck() throws SipException {
        SIPRequest originalRequest = this.getOriginalRequest();
        if (originalRequest == null)
            throw new SipException("bad state " + getState());
        if (getMethod().equalsIgnoreCase(Request.ACK)) {
            throw new SipException("Cannot ACK an ACK!");
        } else if (lastResponse == null) {
            throw new SipException("bad Transaction state");
        } else if (lastResponse.getStatusCode() < 200) {
            if (LogWriter.needsLogging) {
                sipStack.logWriter.logMessage("lastResponse = " + lastResponse);
            }
            throw new SipException("Cannot ACK a provisional response!");
        }
        SIPRequest ackRequest = originalRequest
                .createAckRequest((To) lastResponse.getTo());
        // Pull the record route headers from the last reesponse.
        RecordRouteList recordRouteList = lastResponse.getRecordRouteHeaders();
        // Matt Keller (Motorolla) sent in a bug fix for the following.
        if (recordRouteList == null) {
            Contact contact = null;
            if (lastResponse.getContactHeaders() != null) {
                contact = (Contact) lastResponse.getContactHeaders().getFirst();
                javax.sip.address.URI uri = (javax.sip.address.URI) contact
                        .getAddress().getURI().clone();
                ackRequest.setRequestURI(uri);
            }
            return ackRequest;
        }

        ackRequest.removeHeader(RouteHeader.NAME);
        RouteList routeList = new RouteList();
        // start at the end of the list and walk backwards
        ListIterator li = recordRouteList.listIterator(recordRouteList.size());
        while (li.hasPrevious()) {
            RecordRoute rr = (RecordRoute) li.previous();
            AddressImpl addr = (AddressImpl) rr.getAddress();
            Route route = new Route();
            route.setAddress((AddressImpl) ((AddressImpl) rr.getAddress())
                    .clone());
            route.setParameters((NameValueList) rr.getParameters().clone());
            routeList.add(route);
        }

        Contact contact = null;
        if (lastResponse.getContactHeaders() != null) {
            contact = (Contact) lastResponse.getContactHeaders().getFirst();
        }

        if (!((SipURI) ((Route) routeList.getFirst()).getAddress().getURI())
                .hasLrParam()) {

            // Contact may not yet be there (bug reported by Andreas B).

            Route route = null;
            if (contact != null) {
                route = new Route();
                route.setAddress((AddressImpl) ((AddressImpl) (contact
                        .getAddress())).clone());
            }

            Route firstRoute = (Route) routeList.getFirst();
            routeList.removeFirst();
            javax.sip.address.URI uri = firstRoute.getAddress().getURI();
            ackRequest.setRequestURI(uri);

            if (route != null)
                routeList.add(route);

            ackRequest.addHeader(routeList);
        } else {
            if (contact != null) {
                javax.sip.address.URI uri = (javax.sip.address.URI) contact
                        .getAddress().getURI().clone();
                ackRequest.setRequestURI(uri);
                ackRequest.addHeader(routeList);
            }
        }
        return ackRequest;

    }

    /**
     * Set the port of the recipient.
     */
    protected void setViaPort(int port) {
        this.viaPort = port;
    }

    /**
     * Set the port of the recipient.
     */
    protected void setViaHost(String host) {
        this.viaHost = host;
    }

    /**
     * Get the port of the recipient.
     */
    public int getViaPort() {
        return this.viaPort;
    }

    /**
     * Get the host of the recipient.
     */
    public String getViaHost() {
        return this.viaHost;
    }

    /**
     * get the via header for an outgoing request.
     */
    public Via getOutgoingViaHeader() {
        return this.getMessageProcessor().getViaHeader();
    }

    public boolean isSecure() {
        return encapsulatedChannel.isSecure();
    }

    /**
     * This is called by the stack after a non-invite client transaction goes to
     * completed state.
     */
    public void clearState() {
        // reduce the state to minimum
        // This assumes that the application will not need
        // to access the request once the transaction is
        // completed.
        this.lastRequest = null;
        this.originalRequest = null;
        this.lastResponse = null;
    }

    /**
     * Sets a timeout after which the connection is closed (provided the server
     * does not use the connection for outgoing requests in this time period)
     * and calls the superclass to set state.
     */
    public void setState(TransactionState newState) {
        // Set this timer for connection caching
        // of incoming connections.
        if (newState == TransactionState.TERMINATED && this.isReliable()
                && (!getSIPStack().cacheClientConnections)) {
            // Set a time after which the connection
            // is closed.
            this.collectionTime = TIMER_J;
        }
        super.setState(newState);
    }

    /**
     * Run any pending responses - gets called at the end of the event loop.
     */
    public void processPending() {
        PendingResponse pr;
        synchronized (pendingResponses) {
            if (pendingResponses.isEmpty())
                return;
            pr = (PendingResponse) this.pendingResponses.removeFirst();
        }
        this.processResponse(pr.sipResponse, pr.messageChannel);
        this.eventPending = false;
    }

    public boolean hasPending() {
        synchronized (pendingResponses) {
            return !pendingResponses.isEmpty();
        }
    }

    public void clearPending() {
        boolean toNotify = false;
        synchronized (pendingResponses) {
            super.clearPending();
            if (this.isTerminated() || !pendingResponses.isEmpty()) {
                if (LogWriter.needsLogging)
                    sipStack.logWriter
                            .logMessage("signaling pending response scanner!");
                // Clear the pending response and process it.
                toNotify = true;
            }
        }
        if (toNotify)
            sipStack.notifyPendingRecordScanner();
    }

    /**
     * Start the timer task.
     */
    protected void startTransactionTimer() {
        super.myTimer = new TransactionTimer(this);
        sipStack.timer.schedule(myTimer,
                SIPTransactionStack.BASE_TIMER_INTERVAL,
                SIPTransactionStack.BASE_TIMER_INTERVAL);
    }
}
/*
 * $Log: SIPClientTransaction.java,v $
 * Revision 1.47  2005/05/06 15:06:50  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  mranga
 *
 * Added method check while updating dialog state
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
 * Revision 1.46 2005/04/18 16:46:57 mranga
 * Issue number: Obtained from: Submitted by: mranga Reviewed by: mranga
 * 
 * Bug fixes sent in by Jordan S are incorporated. The optimization for early
 * responses is now re-incorporated into the ClientTx and the optimization for
 * early requests is now re-incorporated into the ServerTx CVS:
 * ---------------------------------------------------------------------- CVS:
 * Issue number: CVS: If this change addresses one or more issues, CVS: then
 * enter the issue number(s) here. CVS: Obtained from: CVS: If this change has
 * been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 * 
 * Revision 1.45 2005/04/15 19:17:08 mranga Issue number: Obtained from:
 * Submitted by: mranga
 * 
 * Fixed maxforwards test Reviewed by: CVS:
 * ---------------------------------------------------------------------- CVS:
 * Issue number: CVS: If this change addresses one or more issues, CVS: then
 * enter the issue number(s) here. CVS: Obtained from: CVS: If this change has
 * been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line. Revision 1.44 2005/03/30 19:57:58 mranga Issue number: Obtained
 * from: Submitted by: mranga Reviewed by: mranga Optimiization that was leading
 * to 100% Cpu util under heavy load was backed out.
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
 * Revision 1.43 2004/12/01 19:05:15 mranga Reviewed by: mranga Code cleanup
 * remove the unused SIMULATION code to reduce the clutter. Fix bug in Dialog
 * state machine.
 * 
 * Revision 1.42 2004/10/31 02:19:07 mranga Reviewed by: M. Ranganathan
 * 
 * Cancel behavior race condition bug.
 * 
 * Cancel example added.
 * 
 * Revision 1.41 2004/10/28 19:02:51 mranga Submitted by: Daniel Martinez
 * Reviewed by: M. Ranganathan
 * 
 * Added changes for TLS support contributed by Daniel Martinez
 * 
 * Revision 1.40 2004/10/05 16:22:38 mranga Issue number: Obtained from:
 * Submitted by: Xavi Ferro Reviewed by: mranga
 * 
 * Another attempted fix for memory leak. CVS:
 * ---------------------------------------------------------------------- CVS:
 * Issue number: CVS: If this change addresses one or more issues, CVS: then
 * enter the issue number(s) here. CVS: Obtained from: CVS: If this change has
 * been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line. Revision 1.39 2004/10/04 16:03:53 mranga Reviewed by: mranga
 * attempted fix for memory leak
 * 
 * Revision 1.38 2004/08/31 19:32:58 mranga Submitted by: Matt Keller Reviewed
 * by: mranga
 * 
 * Ack for 300-699 sent to wrong location.
 * 
 * Revision 1.37 2004/07/01 05:42:22 mranga Submitted by: Pierre De Rop and
 * Thomas Froment Reviewed by: M. Ranganathan
 * 
 * More performance hacks.
 * 
 * Revision 1.36 2004/06/27 00:41:52 mranga Submitted by: Thomas Froment and
 * Pierre De Rop Reviewed by: mranga Performance improvements (auxiliary data
 * structure for fast lookup of transactions).
 * 
 * Revision 1.35 2004/06/21 05:42:30 mranga Reviewed by: mranga more code
 * smithing
 * 
 * Revision 1.34 2004/06/21 04:59:50 mranga Refactored code - no functional
 * changes.
 * 
 * Revision 1.33 2004/06/17 15:22:31 mranga Reviewed by: mranga
 * 
 * Added buffering of out-of-order in-dialog requests for more efficient
 * processing of such requests (this is a performance optimization ).
 * 
 * Revision 1.32 2004/06/15 09:54:44 mranga Reviewed by: mranga re-entrant
 * listener model added. (see configuration property
 * gov.nist.javax.sip.REENTRANT_LISTENER)
 * 
 * Revision 1.31 2004/06/02 13:09:57 mranga Submitted by: Peter Parnes Reviewed
 * by: mranga Fixed illegal state exception.
 * 
 * Revision 1.30 2004/06/01 11:42:59 mranga Reviewed by: mranga timer fix missed
 * starting the transaction timer in a couple of places.
 * 
 * Revision 1.29 2004/05/30 18:55:57 mranga Reviewed by: mranga Move to timers
 * and eliminate the Transaction scanner Thread to improve scalability and
 * reduce cpu usage.
 * 
 * Revision 1.28 2004/05/20 13:59:22 mranga Reviewed by: mranga Cleaned up
 * slighly ugly code.
 * 
 * Revision 1.27 2004/05/18 15:26:43 mranga Reviewed by: mranga Attempted fix at
 * race condition bug. Remove redundant exception (never thrown). Clean up some
 * extraneous junk.
 * 
 * Revision 1.26 2004/05/17 01:00:01 mranga Reviewed by: mranga Dialog State
 * assignment fix. Terminate dialog on non 2xx final response.
 * 
 * Revision 1.25 2004/05/06 15:45:52 mranga Reviewed by: mranga delete dialog
 * when 4xx other than 401 or 407 are received in the early state.
 * 
 * Revision 1.24 2004/04/19 21:51:04 mranga Submitted by: mranga Reviewed by:
 * ivov Support for stun.
 * 
 * Revision 1.23 2004/04/09 11:51:26 mranga Reviewed by: mranga Limit size of
 * pending buffer to thwart response flooding attack.
 * 
 * Revision 1.22 2004/04/07 00:19:23 mranga Reviewed by: mranga Fixes a
 * potential race condition for client transactions. Handle re-invites
 * statefully within an established dialog.
 * 
 * Revision 1.21 2004/04/06 01:19:00 mranga Reviewed by: mranga suppress 100 if
 * invite client transaction is in the Proceeding state
 * 
 * Revision 1.20 2004/03/09 00:34:44 mranga Reviewed by: mranga Added TCP
 * connection management for client and server side Transactions. See
 * configuration parameter gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false
 * Releases Server TCP Connections after linger time
 * gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS=false Releases Client TCP
 * Connections after linger time
 * 
 * Revision 1.19 2004/03/07 22:25:24 mranga Reviewed by: mranga Added a new
 * configuration parameter that instructs the stack to drop a server connection
 * after server transaction termination set
 * gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false for this Default behavior
 * is true.
 * 
 * Revision 1.18 2004/02/24 22:39:34 mranga Reviewed by: mranga Only terminate
 * the client side dialog when the bye Terminates or times out and not when the
 * bye is initially sent out.
 * 
 * Revision 1.17 2004/02/13 13:55:32 mranga Reviewed by: mranga per the spec,
 * Transactions must always have a valid dialog pointer. Assigned a dummy dialog
 * for transactions that are not assigned to any dialog (such as Message).
 * 
 * Revision 1.16 2004/01/25 16:06:24 mranga Reviewed by: M. Ranganathan
 * 
 * Clean up setting state (Use TransactionState instead of integer). Convert to
 * UNIX file format. Remove extraneous methods.
 * 
 * Revision 1.15 2004/01/23 18:26:10 mranga Reviewed by: M. Ranganathan Check
 * for presence of contact header when creating ACK for a transaction.
 * 
 * Revision 1.14 2004/01/22 20:15:32 mranga Reviewed by: mranga Fixed a possible
 * race condition in nulling out the transaction Request (earlier added for
 * scalability).
 * 
 * Revision 1.13 2004/01/22 18:39:41 mranga Reviewed by: M. Ranganathan Moved
 * the ifdef SIMULATION and associated tags to the first column so Prep
 * preprocessor can deal with them.
 * 
 * Revision 1.12 2004/01/22 14:23:45 mranga Reviewed by: mranga Fixed some minor
 * formatting issues.
 * 
 * Revision 1.11 2004/01/22 13:26:33 sverker Issue number: Obtained from:
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
