/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import java.text.ParseException;

/**
*  ContentType SIP Header 
* <pre>
*14.17 Content-Type
* 
*   The Content-Type entity-header field indicates the media type of the
*   entity-body sent to the recipient or, in the case of the HEAD method,
*   the media type that would have been sent had the request been a GET.
* 
*   Content-Type   = "Content-Type" ":" media-type
* 
*   Media types are defined in section 3.7. An example of the field is
* 
*       Content-Type: text/html; charset=ISO-8859-4
* 
*   Further discussion of methods for identifying the media type of an
*   entity is provided in section 7.2.1.   
*
* From  HTTP RFC 2616
* </pre>
*
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*@version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005/04/16 20:38:49 $
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class ContentType
	extends ParametersHeader
	implements javax.sip.header.ContentTypeHeader {

	/** mediaRange field.
	 */
	protected MediaRange mediaRange;

	/** Default constructor.        
	 */
	public ContentType() {
		super(CONTENT_TYPE);
	}

	/** Constructor given a content type and subtype.
	*@param contentType is the content type.
	*@param contentSubtype is the content subtype
	*/
	public ContentType(String contentType, String contentSubtype) {
		this();
		this.setContentType(contentType, contentSubtype);
	}

	/** compare two MediaRange headers.
	 * @param media String to set
	 * @return int.
	 */
	public int compareMediaRange(String media) {
		return (
			mediaRange.type + "/" + mediaRange.subtype).compareToIgnoreCase(
			media);
	}

	/**
	 * Encode into a canonical string.
	 * @return String.
	 */
	public String encodeBody() {
		if (hasParameters())
			return new StringBuffer(mediaRange.encode())
				.append(SEMICOLON)
				.append(parameters.encode())
				.toString();
		else
			return mediaRange.encode();
	}

	/** get the mediaRange field.
	 * @return MediaRange.
	 */
	public MediaRange getMediaRange() {
		return mediaRange;
	}

	/** get the Media Type.
	 * @return String.
	 */
	public String getMediaType() {
		return mediaRange.type;
	}

	/** get the MediaSubType field.
	 * @return String.
	 */
	public String getMediaSubType() {
		return mediaRange.subtype;
	}

	/** Get the content subtype.
	*@return the content subtype string (or null if not set).
	*/
	public String getContentSubType() {
		return mediaRange == null ? null : mediaRange.getSubtype();
	}

	/** Get the content subtype.
	*@return the content tyep string (or null if not set).
	*/

	public String getContentType() {
		return mediaRange == null ? null : mediaRange.getType();
	}

	/** Get the charset parameter.
	*/
	public String getCharset() {
		return this.getParameter("charset");
	}

	/**
	 * Set the mediaRange member
	 * @param m mediaRange field.
	 */
	public void setMediaRange(MediaRange m) {
		mediaRange = m;
	}

	/**
	* set the content type and subtype.
	*@param contentType Content type string.
	*@param contentSubType content subtype string
	*/
	public void setContentType(String contentType, String contentSubType) {
		if (mediaRange == null)
			mediaRange = new MediaRange();
		mediaRange.setType(contentType);
		mediaRange.setSubtype(contentSubType);
	}

	/**
	* set the content type.
	*@param contentType Content type string.
	*/

	public void setContentType(String contentType) throws ParseException {
		if (contentType == null)
			throw new NullPointerException("null arg");
		if (mediaRange == null)
			mediaRange = new MediaRange();
		mediaRange.setType(contentType);

	}

	/** Set the content subtype.
	     * @param contentType String to set
	     */
	public void setContentSubType(String contentType) throws ParseException {
		if (contentType == null)
			throw new NullPointerException("null arg");
		if (mediaRange == null)
			mediaRange = new MediaRange();
		mediaRange.setSubtype(contentType);
	}

	public Object clone() {
		ContentType retval = (ContentType) super.clone();
		if (this.mediaRange != null)
			retval.mediaRange = (MediaRange) this.mediaRange.clone();
		return retval;
	}
}
/*
 * $Log: ContentType.java,v $
 * Revision 1.3  2005/04/16 20:38:49  dmuresan
 * Canonical clone() implementations for the GenericObject and GenericObjectList hierarchies
 *
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
