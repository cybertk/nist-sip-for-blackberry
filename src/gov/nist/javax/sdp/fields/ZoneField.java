/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import java.util.*;
import javax.sdp.*;

/**
* Z= SDP field.
*
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public class ZoneField
	extends SDPField
	implements javax.sdp.TimeZoneAdjustment {

	protected SDPObjectList zoneAdjustments;

	/**
	* Constructor.
	*/
	public ZoneField() {
		super(ZONE_FIELD);
		zoneAdjustments = new SDPObjectList();
	}

	/**
	* Add an element to the zone adjustment list.
	*@param za zone adjustment to add.
	*/
	public void addZoneAdjustment(ZoneAdjustment za) {
		zoneAdjustments.add(za);
	}

	/**
	* Get the zone adjustment list.
	*@return the list of zone adjustments.
	*/

	public SDPObjectList getZoneAdjustments() {
		return zoneAdjustments;
	}

	/**
	* Encode this structure into a canonical form.
	*/
	public String encode() {
		StringBuffer retval = new StringBuffer(ZONE_FIELD);
		ListIterator li = zoneAdjustments.listIterator();
		int num = 0;
		while (li.hasNext()) {
			ZoneAdjustment za = (ZoneAdjustment) li.next();
			if (num > 0)
				retval.append(Separators.SP);
			retval.append(za.encode());
			num++;
		}
		retval.append(Separators.NEWLINE);
		return retval.toString();
	}

	/** Returns a Hashtable of adjustment times, where:
	 *        key = Date. This is the equivalent of the decimal NTP time value.
	 *        value = Int Adjustment. This is a relative time value in seconds.
	 * @param create to set
	 * @throws SdpParseException
	 * @return create - when true, an empty Hashtable is created, if it is null.
	 */
	public Hashtable getZoneAdjustments(boolean create)
		throws SdpParseException {
		Hashtable result = new Hashtable();
		SDPObjectList zoneAdjustments = getZoneAdjustments();
		ZoneAdjustment zone;
		if (zoneAdjustments == null)
			if (create)
				return new Hashtable();
			else
				return null;
		else {
			while ((zone = (ZoneAdjustment) zoneAdjustments.next()) != null) {
				Long l = new Long(zone.getTime());
				Integer time = new Integer(l.toString());
				Date date = new Date(zone.getTime());
				result.put(date, time);
			}
			return result;
		}
	}

	/** Sets the Hashtable of adjustment times, where:
	 *          key = Date. This is the equivalent of the decimal NTP time value.
	 *          value = Int Adjustment. This is a relative time value in seconds.
	 * @param map Hashtable to set
	 * @throws SdpException if the parameter is null
	 */
	public void setZoneAdjustments(Hashtable map) throws SdpException {
		if (map == null)
			throw new SdpException("The map is null");
		else {
			SDPObjectList zoneAdjustments = getZoneAdjustments();
			for (Enumeration e = map.keys(); e.hasMoreElements();) {
				Object o = e.nextElement();
				if (o instanceof Date) {
					Date date = (Date) o;
					ZoneAdjustment zone = new ZoneAdjustment();
					zone.setTime(date.getTime());
					addZoneAdjustment(zone);
				} else
					throw new SdpException("The map is not well-formated ");
			}
		}
	}

	/** Sets whether the field will be output as a typed time or a integer value.
	 *
	 *     Typed time is formatted as an integer followed by a unit character. 
	 * The unit indicates an appropriate multiplier for
	 *     the integer.
	 *
	 *     The following unit types are allowed.
	 *          d - days (86400 seconds)
	 *          h - hours (3600 seconds)
	 *          m - minutes (60 seconds)
	 *          s - seconds ( 1 seconds)
	 * @param typedTime typedTime - if set true, the start and stop times will be
	 * output in an optimal typed time format; if false, the
	 *          times will be output as integers.
	 */
	public void setTypedTime(boolean typedTime) {
		// Dummy -- feature not implemented.
	}

	/** Returns whether the field will be output as a typed time or a integer value.
	 *
	 *     Typed time is formatted as an integer followed by a unit character.
	 * The unit indicates an appropriate multiplier for
	 *     the integer.
	 *
	 *     The following unit types are allowed.
	 *          d - days (86400 seconds)
	 *          h - hours (3600 seconds)
	 *          m - minutes (60 seconds)
	 *          s - seconds ( 1 seconds)
	 * @return true, if the field will be output as a typed time; false, if as an integer value.
	 */
	public boolean getTypedTime() {
		return false;
	}

	public Object clone() {
		ZoneField retval = (ZoneField) super.clone();
		if (this.zoneAdjustments != null)
			retval.zoneAdjustments = (SDPObjectList) this.zoneAdjustments.clone();
		return retval;
	}
}
/*
 * $Log: ZoneField.java,v $
 * Revision 1.5  2005/04/16 20:38:45  dmuresan
 * Canonical clone() implementations for the GenericObject and GenericObjectList hierarchies
 *
 * Revision 1.4  2005/04/04 10:01:28  dmuresan
 * Used StringBuffer instead of String += for concatenation in
 * various encode() methods in javax.sdp.
 *
 * Revision 1.3  2004/01/22 13:26:28  sverker
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
