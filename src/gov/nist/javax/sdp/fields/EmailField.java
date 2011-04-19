/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import javax.sdp.*;
/**
* email field in the SDP announce.
*
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*/
public class EmailField extends SDPField implements javax.sdp.EMail {

	protected EmailAddress emailAddress;

	public EmailField() {
		super(SDPFieldNames.EMAIL_FIELD);
		emailAddress = new EmailAddress();
	}

	public EmailAddress getEmailAddress() {
		return emailAddress;
	}
	/**
	 * Set the emailAddress member  
	 */
	public void setEmailAddress(EmailAddress emailAddress) {
		this.emailAddress = emailAddress;
	}

	/**
	 *  Get the string encoded version of this object
	 * @since v1.0
	 */
	public String encode() {
		return EMAIL_FIELD + emailAddress.encode() + Separators.NEWLINE;
	}

	public String toString() {
		return this.encode();
	}

	/** Returns the value.
	 * @throws SdpParseException
	 * @return the value
	 */
	public String getValue() throws SdpParseException {
		if (emailAddress == null)
			return null;
		else {
			return emailAddress.getDisplayName();
		}
	}

	/** Set the value.
	 * @param value to set
	 * @throws SdpException if the value is null
	 */
	public void setValue(String value) throws SdpException {
		if (value == null)
			throw new SdpException("The value is null");
		else {

			emailAddress.setDisplayName(value);
		}
	}

	public Object clone() {
		EmailField retval = (EmailField) super.clone();
		if (this.emailAddress != null)
			retval.emailAddress = (EmailAddress) this.emailAddress.clone();
		return retval;
	}

}
/*
 * $Log: EmailField.java,v $
 * Revision 1.3  2005/04/16 20:38:44  dmuresan
 * Canonical clone() implementations for the GenericObject and GenericObjectList hierarchies
 *
 * Revision 1.2  2004/01/22 13:26:27  sverker
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
