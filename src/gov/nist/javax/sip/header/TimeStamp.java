/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
 *******************************************************************************/

package gov.nist.javax.sip.header;

import javax.sip.InvalidArgumentException;
import javax.sip.header.*;

/**  
 * TimeStamp SIP Header.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004/01/22 13:26:30 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @author Olivier Deruelle <deruelle@nist.gov><br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class TimeStamp extends SIPHeader implements TimeStampHeader {

	/** timeStamp field
	 */
	protected float timeStamp;

	/** delay field
	 */
	protected float delay;

	/** Default Constructor
	  */
	public TimeStamp() {
		super(TIMESTAMP);
		delay = -1;
	}

	/**
	 * Return canonical form of the header.
	 * @return String
	 */
	public String encodeBody() {
		if (delay != -1)
			return new Float(timeStamp).toString()
				+ SP
				+ new Float(delay).toString();
		else
			return new Float(timeStamp).toString();
	}

	/** return true if delay exists
	 * @return boolean
	 */
	public boolean hasDelay() {
		return delay != -1;
	}

	/* remove the Delay field
	 */
	public void removeDelay() {
		delay = -1;
	}

	/********************************************************************************/
	/********************** JAIN-SIP 1.1 methods ************************************/
	/********************************************************************************/

	/**
	* Sets the timestamp value of this TimeStampHeader to the new timestamp
	* value passed to this method.
	*
	* @param timeStamp - the new float timestamp value
	* @throws InvalidArgumentException if the timestamp value argument is a
	* negative value.
	*/
	public void setTimeStamp(float timeStamp) throws InvalidArgumentException {
		if (timeStamp < 0)
			throw new InvalidArgumentException(
				"JAIN-SIP Exception, TimeStamp, "
					+ "setTimeStamp(), the timeStamp parameter is <0");
		this.timeStamp = timeStamp;
	}

	/**
	 * Gets the timestamp value of this TimeStampHeader.
	 *
	 * @return the timestamp value of this TimeStampHeader
	 */
	public float getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Gets delay of TimeStampHeader. This method return <code>-1</code> if the
	 * delay paramater is not set.
	 *
	 * @return the delay value of this TimeStampHeader
	 */

	public float getDelay() {
		return delay;
	}

	/**
	 * Sets the new delay value of the TimestampHeader to the delay paramter
	 * passed to this method
	 *
	 * @param delay - the new float delay value
	 * @throws InvalidArgumentException if the delay value argumenmt is a
	 * negative value other than <code>-1</code>.
	 */

	public void setDelay(float delay) throws InvalidArgumentException {
		if (delay < 0 && delay != -1)
			throw new InvalidArgumentException(
				"JAIN-SIP Exception, TimeStamp, "
					+ "setDelay(), the delay parameter is <0");
		this.delay = delay;
	}
}
/*
 * $Log: TimeStamp.java,v $
 * Revision 1.2  2004/01/22 13:26:30  sverker
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
