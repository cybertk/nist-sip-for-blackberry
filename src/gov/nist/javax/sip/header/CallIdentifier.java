/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

/**
 * The call identifer that goes into a callID header and a in-reply-to header.
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004/01/22 13:26:29 $
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @see CallID
 * @see InReplyTo
 */
public final class CallIdentifier extends SIPObject {

	/**
	 * localId field
	 */
	protected String localId;

	/**
	 * host field
	 */
	protected String host;

	/**
	 * Default constructor
	 */
	public CallIdentifier() {
	}

	/**
	 * Constructor
	 * @param localId id is the local id.
	 * @param host is the host.
	 */
	public CallIdentifier(String localId, String host) {
		this.localId = localId;
		this.host = host;
	}

	/**
	 * constructor
	 * @param cid String to set
	 * @throws IllegalArgumentException if cid is null or is not a token, 
	 * or token@token
	 */
	public CallIdentifier(String cid) throws IllegalArgumentException {
		setCallID(cid);
	}

	/**
	 * Get the encoded version of this id.
	 * @return String to set
	 */
	public String encode() {
		if (host != null) {
			return localId + AT + host;
		} else {
			return localId;
		}
	}

	/**
	 * Compare two call identifiers for equality.
	 * @param other Object to set
	 * @return true if the two call identifiers are equals, false
	 * otherwise
	 */
	public boolean equals(Object other) {
		if (!other.getClass().equals(this.getClass())) {
			return false;
		}
		CallIdentifier that = (CallIdentifier) other;
		if (this.localId.compareTo(that.localId) != 0) {
			return false;
		}
		if (this.host == that.host)
			return true;
		if ((this.host == null && that.host != null)
			|| (this.host != null && that.host == null))
			return false;
		if (host.compareToIgnoreCase(that.host) != 0) {
			return false;
		}
		return true;
	}

	/** get the LocalId field
	 * @return String
	 */
	public String getLocalId() {
		return localId;
	}

	/** get the host field
	 * @return host member String
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Set the localId member
	 * @param localId String to set
	 */
	public void setLocalId(String localId) {
		this.localId = localId;
	}

	/** set the callId field
	 * @param cid Strimg to set
	 * @throws IllegalArgumentException if cid is null or is not a token or 
	 * token@token
	 */
	public void setCallID(String cid) throws IllegalArgumentException {
		if (cid == null)
			throw new IllegalArgumentException("NULL!");
		int index = cid.indexOf('@');
		if (index == -1) {
			localId = cid;
			host = null;
		} else {
			localId = cid.substring(0, index);
			host = cid.substring(index + 1, cid.length());
			if (localId == null || host == null) {
				throw new IllegalArgumentException("CallID  must be token@token or token");
			}
		}
	}

	/**
	 * Set the host member
	 * @param host String to set
	 */
	public void setHost(String host) {
		this.host = host;
	}
}
/*
 * $Log: CallIdentifier.java,v $
 * Revision 1.2  2004/01/22 13:26:29  sverker
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
