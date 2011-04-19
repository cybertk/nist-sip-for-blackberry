/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/

package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.header.*;

/**
 * Organization SIP Header.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004/01/22 13:26:29 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @author Olivier Deruelle <deruelle@nist.gov><br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class Organization extends SIPHeader implements OrganizationHeader {

	/**
	 * Organization field
	 */
	protected String organization;

	/**
	 * Return encoding of value of the header.
	 * @return String
	 */
	public String encodeBody() {
		return organization;
	}

	/**
	 * Default constructor
	 */
	public Organization() {
		super(ORGANIZATION);
	}

	/**
	 * Get the organization field.
	 * @return String
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * Set the organization member
	 * @param o String to set
	 */
	public void setOrganization(String o) throws ParseException {
		if (o == null)
			throw new NullPointerException(
				"JAIN-SIP Exception,"
					+ " Organization, setOrganization(), the organization parameter is null");
		organization = o;
	}
}
/*
 * $Log: Organization.java,v $
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
