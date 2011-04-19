/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import gov.nist.core.*;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Locale;
import java.util.GregorianCalendar;
import java.lang.IllegalArgumentException;

/**
* Implements a parser class for tracking expiration time 
* when specified as a Date value.
*<pre>
* From the HTTP 1.1 spec
*14.18 Date
*
*   The Date general-header field represents the date and time at which
*   the message was originated, having the same semantics as orig-date in
*   RFC 822. The field value is an HTTP-date, as described in section
*   3.3.1; it MUST be sent in RFC 1123 [8]-date format.

*       Date  = "Date" ":" HTTP-date
*
*   An example is
*
*       Date: Tue, 15 Nov 1994 08:12:31 GMT
*</pre>
*
*@version JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2005/04/16 20:35:10 $
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*
*/

public class SIPDate implements Cloneable {
	public static final String GMT = "GMT";
	public static final String MON = "Mon";
	public static final String TUE = "Tue";
	public static final String WED = "Wed";
	public static final String THU = "Thu";
	public static final String FRI = "Fri";
	public static final String SAT = "Sat";
	public static final String SUN = "Sun";
	public static final String JAN = "Jan";
	public static final String FEB = "Feb";
	public static final String MAR = "Mar";
	public static final String APR = "Apr";
	public static final String MAY = "May";
	public static final String JUN = "Jun";
	public static final String JUL = "Jul";
	public static final String AUG = "Aug";
	public static final String SEP = "Sep";
	public static final String OCT = "Oct";
	public static final String NOV = "Nov";
	public static final String DEC = "Dec";

	/** sipWkDay member
	 */
	protected String sipWkDay;

	/** sipMonth member
	*/
	protected String sipMonth;

	/** wkday member
	*/
	protected int wkday;

	/** day member
	*/
	protected int day;

	/** month member
	*/
	protected int month;

	/** year member
	*/
	protected int year;

	/** hour member
	*/
	protected int hour;

	/** minute member
	*/
	protected int minute;

	/** second member
	*/
	protected int second;

	/** javaCal member
	*/
	private java.util.Calendar javaCal;
	
	/** equality check.
	 *
	 *@return true if the two date fields are equals
	 */
	public boolean equals(Object that){
		if (that.getClass() != this.getClass())return false;
		SIPDate other = (SIPDate)that;
		return this.wkday == other.wkday &&
		this.day == other.day && 
		this.month == other.month && 
		this.year == other.year &&
		this.hour == other.hour && 
		this.minute == other.minute && 
		this.second == other.second;
	}

	/**
	 * Initializer, sets all the fields to invalid values. 
	 */
	public SIPDate() {
		wkday = -1;
		day = -1;
		month = -1;
		year = -1;
		hour = -1;
		minute = -1;
		second = -1;
		javaCal = null;
	}

	/**
	 * Construct a SIP date from the time offset given in miliseconds
	 * @param timeMillis long to set
	 */
	public SIPDate(long timeMillis) {
		javaCal =
			new GregorianCalendar(
				TimeZone.getTimeZone("GMT:0"),
				Locale.getDefault());
		java.util.Date date = new java.util.Date(timeMillis);
		javaCal.setTime(date);
		wkday = javaCal.get(Calendar.DAY_OF_WEEK);
		switch (wkday) {
			case Calendar.MONDAY :
				sipWkDay = MON;
				break;
			case Calendar.TUESDAY :
				sipWkDay = TUE;
				break;
			case Calendar.WEDNESDAY :
				sipWkDay = WED;
				break;
			case Calendar.THURSDAY :
				sipWkDay = THU;
				break;
			case Calendar.FRIDAY :
				sipWkDay = FRI;
				break;
			case Calendar.SATURDAY :
				sipWkDay = SAT;
				break;
			case Calendar.SUNDAY :
				sipWkDay = SUN;
				break;
			default :
				InternalErrorHandler.handleException(
					"No date map for wkday " + wkday);
		}

		day = javaCal.get(Calendar.DAY_OF_MONTH);
		month = javaCal.get(Calendar.MONTH);
		switch (month) {
			case Calendar.JANUARY :
				sipMonth = JAN;
				break;
			case Calendar.FEBRUARY :
				sipMonth = FEB;
				break;
			case Calendar.MARCH :
				sipMonth = MAR;
				break;
			case Calendar.APRIL :
				sipMonth = APR;
				break;
			case Calendar.MAY :
				sipMonth = MAY;
				break;
			case Calendar.JUNE :
				sipMonth = JUN;
				break;
			case Calendar.JULY :
				sipMonth = JUL;
				break;
			case Calendar.AUGUST :
				sipMonth = AUG;
				break;
			case Calendar.SEPTEMBER :
				sipMonth = SEP;
				break;
			case Calendar.OCTOBER :
				sipMonth = OCT;
				break;
			case Calendar.NOVEMBER :
				sipMonth = NOV;
				break;
			case Calendar.DECEMBER :
				sipMonth = DEC;
				break;
			default :
				InternalErrorHandler.handleException(
					"No date map for month " + month);
		}
		year = javaCal.get(Calendar.YEAR);
		// Bug report by Bruno Konik
		hour = javaCal.get(Calendar.HOUR_OF_DAY);
		minute = javaCal.get(Calendar.MINUTE);
		second = javaCal.get(Calendar.SECOND);
	}

	/**
	 * Get canonical string representation.
	 * @return String 
	 */
	public String encode() {

		String dayString;
		if (day < 10) {
			dayString = "0" + day;
		} else
			dayString = "" + day;

		String hourString;
		if (hour < 10) {
			hourString = "0" + hour;
		} else
			hourString = "" + hour;

		String minuteString;
		if (minute < 10) {
			minuteString = "0" + minute;
		} else
			minuteString = "" + minute;

		String secondString;
		if (second < 10) {
			secondString = "0" + second;
		} else
			secondString = "" + second;

		String encoding = "";

		if (sipWkDay != null)
			encoding += sipWkDay + Separators.COMMA + Separators.SP;

		encoding += dayString + Separators.SP;

		if (sipMonth != null)
			encoding += sipMonth + Separators.SP;

		encoding += year
			+ Separators.SP
			+ hourString
			+ Separators.COLON
			+ minuteString
			+ Separators.COLON
			+ secondString
			+ Separators.SP
			+ GMT;

		return encoding;
	}

	/**
	 * The only accessor we allow is to the java calendar record.
	 * All other fields are for this package only.
	 * @return Calendar
	 */
	public java.util.Calendar getJavaCal() {
		if (javaCal == null)
			setJavaCal();
		return javaCal;
	}

	/** get the WkDay field
	 * @return String
	 */
	public String getWkday() {
		return sipWkDay;
	}

	/** get the month
	 * @return String
	 */
	public String getMonth() {
		return sipMonth;
	}

	/** get the hour
	 * @return int
	 */
	public int getHour() {
		return hour;
	}

	/** get the minute
	 * @return int
	 */
	public int getMinute() {
		return minute;
	}

	/** get the second
	 *  @return int
	 */
	public int getSecond() {
		return second;
	}

	/**
	 * convert the SIP Date of this structure to a Java Date.
	 * SIP Dates are forced to be GMT. Stores the converted time
	 * as a java Calendar class.
	 */
	private void setJavaCal() {
		javaCal =
			new GregorianCalendar(
				TimeZone.getTimeZone("GMT:0"),
				Locale.getDefault());
		if (year != -1)
			javaCal.set(Calendar.YEAR, year);
		if (day != -1)
			javaCal.set(Calendar.DAY_OF_MONTH, day);
		if (month != -1)
			javaCal.set(Calendar.MONTH, month);
		if (wkday != -1)
			javaCal.set(Calendar.DAY_OF_WEEK, wkday);
		if (hour != -1)
			javaCal.set(Calendar.HOUR, hour);
		if (minute != -1)
			javaCal.set(Calendar.MINUTE, minute);
		if (second != -1)
			javaCal.set(Calendar.SECOND, second);
	}

	/**
	 * Set the wkday member
	 * @param w String to set
	 * @throws IllegalArgumentException if w is not a valid day.
	 */
	public void setWkday(String w) throws IllegalArgumentException {
		sipWkDay = w;
		if (sipWkDay.compareToIgnoreCase(MON) == 0) {
			wkday = Calendar.MONDAY;
		} else if (sipWkDay.compareToIgnoreCase(TUE) == 0) {
			wkday = Calendar.TUESDAY;
		} else if (sipWkDay.compareToIgnoreCase(WED) == 0) {
			wkday = Calendar.WEDNESDAY;
		} else if (sipWkDay.compareToIgnoreCase(THU) == 0) {
			wkday = Calendar.THURSDAY;
		} else if (sipWkDay.compareToIgnoreCase(FRI) == 0) {
			wkday = Calendar.FRIDAY;
		} else if (sipWkDay.compareToIgnoreCase(SAT) == 0) {
			wkday = Calendar.SATURDAY;
		} else if (sipWkDay.compareToIgnoreCase(SUN) == 0) {
			wkday = Calendar.SUNDAY;
		} else {
			throw new IllegalArgumentException("Illegal Week day :" + w);
		}
	}

	/**
	 * Set the day member
	 * @param d int to set
	 * @throws IllegalArgumentException if d is not a valid day
	 */
	public void setDay(int d) throws IllegalArgumentException {
		if (d < 1 || d > 31)
			throw new IllegalArgumentException(
				"Illegal Day of the month " + new Integer(d).toString());
		day = d;
	}

	/**
	 * Set the month member
	 * @param m String to set.
	 * @throws IllegalArgumentException if m is not a valid month
	 */
	public void setMonth(String m) throws IllegalArgumentException {
		sipMonth = m;
		if (sipMonth.compareToIgnoreCase(JAN) == 0) {
			month = Calendar.JANUARY;
		} else if (sipMonth.compareToIgnoreCase(FEB) == 0) {
			month = Calendar.FEBRUARY;
		} else if (sipMonth.compareToIgnoreCase(MAR) == 0) {
			month = Calendar.MARCH;
		} else if (sipMonth.compareToIgnoreCase(APR) == 0) {
			month = Calendar.APRIL;
		} else if (sipMonth.compareToIgnoreCase(MAY) == 0) {
			month = Calendar.MAY;
		} else if (sipMonth.compareToIgnoreCase(JUN) == 0) {
			month = Calendar.JUNE;
		} else if (sipMonth.compareToIgnoreCase(JUL) == 0) {
			month = Calendar.JULY;
		} else if (sipMonth.compareToIgnoreCase(AUG) == 0) {
			month = Calendar.AUGUST;
		} else if (sipMonth.compareToIgnoreCase(SEP) == 0) {
			month = Calendar.SEPTEMBER;
		} else if (sipMonth.compareToIgnoreCase(OCT) == 0) {
			month = Calendar.OCTOBER;
		} else if (sipMonth.compareToIgnoreCase(NOV) == 0) {
			month = Calendar.NOVEMBER;
		} else if (sipMonth.compareToIgnoreCase(DEC) == 0) {
			month = Calendar.DECEMBER;
		} else {
			throw new IllegalArgumentException("Illegal Month :" + m);
		}
	}

	/**
	 * Set the year member
	 * @param y int to set
	 * @throws IllegalArgumentException if y is not a valid year.
	 */
	public void setYear(int y) throws IllegalArgumentException {
		if (y < 0)
			throw new IllegalArgumentException("Illegal year : " + y);
		javaCal = null;
		year = y;
	}

	/**
	* Get the year member.
	*/
	public int getYear() {
		return year;
	}

	/**
	 * Set the hour member
	 * @param h int to set
	 * @throws IllegalArgumentException if h is not a valid hour.
	 */
	public void setHour(int h) throws IllegalArgumentException {
		if (h < 0 || h > 24)
			throw new IllegalArgumentException("Illegal hour : " + h);
		javaCal = null;
		hour = h;
	}

	/**
	 * Set the minute member
	 * @param m int to set
	 * @throws IllegalArgumentException if m is not a valid minute
	 */
	public void setMinute(int m) throws IllegalArgumentException {
		if (m < 0 || m >= 60)
			throw new IllegalArgumentException(
				"Illegal minute : " + (new Integer(m).toString()));
		javaCal = null;
		minute = m;
	}

	/**
	 * Set the second member
	 * @param s int to set
	 * @throws IllegalArgumentException if s is not a valid second
	 */
	public void setSecond(int s) throws IllegalArgumentException {
		if (s < 0 || s >= 60)
			throw new IllegalArgumentException(
				"Illegal second : " + new Integer(s).toString());
		javaCal = null;
		second = s;
	}

	/** Get the time offset from the current time.
	 *
	 *@return offset from the current time.
	 */
	public int getDeltaSeconds() {
		// long ctime = this.getJavaCal().getTimeInMillis();
		long ctime = this.getJavaCal().getTime().getTime();
		return (int) (ctime - System.currentTimeMillis()) / 1000;
	}

	public Object clone() {
		SIPDate retval;
		try {
			retval = (SIPDate) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Internal error");
		}
		if (javaCal != null)
			retval.javaCal = (java.util.Calendar) javaCal.clone();
		return retval;
	}
}
/*
 * $Log: SIPDate.java,v $
 * Revision 1.5  2005/04/16 20:35:10  dmuresan
 * SIPDate made cloneable.
 *
 * Revision 1.4  2004/07/28 14:41:53  mranga
 * Submitted by:  mranga
 *
 * fixed equality check for SIPDate.
 *
 * Revision 1.3  2004/04/05 21:46:08  mranga
 * Submitted by:  Bruno Konik
 * Reviewed by:   mranga
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
