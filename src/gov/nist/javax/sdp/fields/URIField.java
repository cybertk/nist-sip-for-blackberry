/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;

import gov.nist.core.*;
import java.net.*;

/** Implementation of URI field.
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*/

public class URIField extends SDPField implements javax.sdp.URI {
	protected URL url;
	protected String urlString;

	public URIField() {
		super(URI_FIELD);
	}

	public String getURI() {
		return urlString;
	}

	public void setURI(String uri) {
		this.urlString = uri;
		this.url = null;
	}

	public URL get() {
		if (this.url != null) {
			return this.url;
		} else {
			try {
				this.url = new URL(this.urlString);
				return this.url;
			} catch (Exception ex) {
				return null;
			}
		}
	}

	public void set(URL uri) {
		this.url = uri;
		this.urlString = null;
	}

	/**
	 *  Get the string encoded version of this object
	 * @since v1.0
	 */
	public String encode() {
		if (urlString != null) {
			return URI_FIELD + urlString + Separators.NEWLINE;
		} else if (url != null) {
			return URI_FIELD + url.toString() + Separators.NEWLINE;
		} else
			return "";
	}

}
/*
 * $Log: URIField.java,v $
 * Revision 1.2  2004/01/22 13:26:28  sverker
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
