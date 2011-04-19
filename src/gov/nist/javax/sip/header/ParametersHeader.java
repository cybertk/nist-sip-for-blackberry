/**************************************************************************/
/* Product of NIST Advanced Networking Technologies Division		  */
/**************************************************************************/

package gov.nist.javax.sip.header;
import gov.nist.core.*;
import java.text.ParseException;
import java.util.*;
import gov.nist.javax.sip.address.*;

/**
 * Parameters header. Suitable for extension by headers that have parameters.
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @version JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2005/04/16 20:38:50 $
 *
 */
public abstract class ParametersHeader
	extends SIPHeader
	implements javax.sip.header.Parameters {
	protected NameValueList parameters;

	protected ParametersHeader() {
		this.parameters = new NameValueList();
	}

	protected ParametersHeader(String hdrName) {
		super(hdrName);
		this.parameters = new NameValueList();
	}

	/**
	 * Returns the value of the named parameter, or null if it is not set. A
	 * zero-length String indicates flag parameter.
	 *
	 * @param name name of parameter to retrieve
	 * @return the value of specified parameter
	 */

	public String getParameter(String name) {
		return this.parameters.getParameter(name);

	}

	/** 
	 * Return the parameter as an object (dont convert to string).
	 *
	 * @param name is the name of the parameter to get.
	 * @return the object associated with the name.
	 */
	public Object getParameterValue(String name) {
		return this.parameters.getValue(name);
	}

	/**
	 * Returns an Iterator over the names (Strings) of all parameters present
	 * in this ParametersHeader.
	 *
	 * @return an Iterator over all the parameter names
	 */

	public Iterator getParameterNames() {
		return parameters.getNames();
	}

	/** Return true if you have a parameter and false otherwise.
	 *
	 *@return true if the parameters list is non-empty.
	 */

	public boolean hasParameters() {
		return parameters != null && !parameters.isEmpty();
	}

	/**
	* Removes the specified parameter from Parameters of this ParametersHeader.
	* This method returns silently if the parameter is not part of the
	* ParametersHeader.
	*
	* @param name - a String specifying the parameter name
	*/

	public void removeParameter(String name) {
		this.parameters.delete(name);
	}

	/**
	 * Sets the value of the specified parameter. If the parameter already had
	 *
	 * a value it will be overwritten. A zero-length String indicates flag
	 *
	 * parameter.
	 *
	 *
	 *
	 * @param name - a String specifying the parameter name
	 *
	 * @param value - a String specifying the parameter value
	 *
	 * @throws ParseException which signals that an error has been reached
	 *
	 * unexpectedly while parsing the parameter name or value.
	 *
	 */
	public void setParameter(String name, String value) throws ParseException {
		NameValue nv = parameters.getNameValue(name);
		if (nv != null) {
			nv.setValue(value);
		} else {
			nv = new NameValue(name, value);
			this.parameters.set(nv);
		}
	}

	/**
	 * Sets the value of the specified parameter. If the parameter already had
	 *
	 * a value it will be overwritten. A zero-length String indicates flag
	 *
	 * parameter.
	 *
	 *
	 *
	 * @param name - a String specifying the parameter name
	 *
	 * @param value - a String specifying the parameter value
	 *
	 * @throws ParseException which signals that an error has been reached
	 *
	 * unexpectedly while parsing the parameter name or value.
	 *
	 */
	public void setQuotedParameter(String name, String value)
		throws ParseException {
		NameValue nv = parameters.getNameValue(name);
		if (nv != null) {
			nv.setValue(value);
			nv.setQuotedValue();
		} else {
			nv = new NameValue(name, value);
			nv.setQuotedValue();
			this.parameters.set(nv);
		}
	}

	/**
	 * Sets the value of the specified parameter. If the parameter already had
	 *
	 * a value it will be overwritten.
	 *
	 *
	 * @param name - a String specifying the parameter name
	 *
	 * @param value - an int specifying the parameter value
	 *
	 * @throws ParseException which signals that an error has been reached
	 *
	 * unexpectedly while parsing the parameter name or value.
	 *
	 */
	protected void setParameter(String name, int value) {
		Integer val = new Integer(value);
		NameValue nv = parameters.getNameValue(name);
		if (nv != null) {
			nv.setValue(val);
		} else {
			nv = new NameValue(name, val);
			this.parameters.set(nv);
		}
	}

	/**
	 * Sets the value of the specified parameter. If the parameter already had
	 *
	 * a value it will be overwritten. 
	 *
	 *
	 * @param name - a String specifying the parameter name
	 *
	 * @param value - a boolean specifying the parameter value
	 *
	 * @throws ParseException which signals that an error has been reached
	 *
	 * unexpectedly while parsing the parameter name or value.
	 *
	 */
	protected void setParameter(String name, boolean value) {
		Boolean val = new Boolean(value);
		NameValue nv = parameters.getNameValue(name);
		if (nv != null) {
			nv.setValue(val);
		} else {
			nv = new NameValue(name, val);
			this.parameters.set(nv);
		}
	}

	/**
	 * Sets the value of the specified parameter. If the parameter already had
	 *
	 * a value it will be overwritten. 
	 *
	 * @param name - a String specifying the parameter name
	 *
	 * @param value - a boolean specifying the parameter value
	 *
	 * @throws ParseException which signals that an error has been reached
	 *
	 * unexpectedly while parsing the parameter name or value.
	 *
	 */
	protected void setParameter(String name, float value) {
		Float val = new Float(value);
		NameValue nv = parameters.getNameValue(name);
		if (nv != null) {
			nv.setValue(val);
		} else {
			nv = new NameValue(name, val);
			this.parameters.set(nv);
		}
	}

	/**
	 * Sets the value of the specified parameter. If the parameter already had
	 *
	 * a value it will be overwritten. A zero-length String indicates flag
	 *
	 * parameter.
	 *
	 *
	 *
	 * @param name - a String specifying the parameter name
	 *
	 * @param value - a String specifying the parameter value
	 *
	 * @throws ParseException which signals that an error has been reached
	 *
	 * unexpectedly while parsing the parameter name or value.
	 *
	 */
	protected void setParameter(String name, Object value) {
		NameValue nv = parameters.getNameValue(name);
		if (nv != null) {
			nv.setValue(value);
		} else {
			nv = new NameValue(name, value);
			this.parameters.set(nv);
		}
	}

	/** 
	 * Return true if has a parameter.
	 *
	 * @param parameterName is the name of the parameter.
	 *
	 * @return true if the parameter exists and false if not.
	 */
	public boolean hasParameter(String parameterName) {
		return this.parameters.hasNameValue(parameterName);
	}

	/**
	 *Remove all parameters.
	 */
	public void removeParameters() {
		this.parameters = new NameValueList();
	}

	/**
	 * get the parameter list.
	 * @return parameter list
	 */
	public NameValueList getParameters() {
		return parameters;
	}

	/** Set the parameter given a name and value.
	 *
	 * @param nameValue - the name value of the parameter to set.
	 */
	public void setParameter(NameValue nameValue) {
		//System.out.println("setParameter " + this + " nbv = " + nameValue);
		this.parameters.set(nameValue);
	}

	/** 
	 * Set the parameter list.
	 *
	 * @param parameters The name value list to set as the parameter list.
	 */
	public void setParameters(NameValueList parameters) {
		this.parameters = parameters;
	}

	/** 
	 * Get the parameter as an integer value.
	 *
	 * @param parameterName -- the parameter name to fetch.
	 *
	 * @return -1 if the parameter is not defined in the header.
	 */
	protected int getParameterAsInt(String parameterName) {
		if (this.getParameterValue(parameterName) != null) {
			try {
				if (this.getParameterValue(parameterName) instanceof String) {
					return Integer.parseInt(this.getParameter(parameterName));
				} else {
					return ((Integer) getParameterValue(parameterName))
						.intValue();
				}
			} catch (NumberFormatException ex) {
				return -1;
			}
		} else
			return -1;
	}

	/** Get the parameter as an integer when it is entered as a hex.
	 *
	 *@param parameterName -- The parameter name to fetch.
	 *
	 *@return -1 if the parameter is not defined in the header.
	 */
	protected int getParameterAsHexInt(String parameterName) {
		if (this.getParameterValue(parameterName) != null) {
			try {
				if (this.getParameterValue(parameterName) instanceof String) {
					return Integer.parseInt(
						this.getParameter(parameterName),
						16);
				} else {
					return ((Integer) getParameterValue(parameterName))
						.intValue();
				}
			} catch (NumberFormatException ex) {
				return -1;
			}
		} else
			return -1;
	}

	/** Get the parameter as a float value.
	 *
	 *@param parameterName -- the parameter name to fetch
	 *
	 *@return -1 if the parameter is not defined or the parameter as a float.
	 */
	protected float getParameterAsFloat(String parameterName) {

		if (this.getParameterValue(parameterName) != null) {
			try {
				if (this.getParameterValue(parameterName) instanceof String) {
					return Float.parseFloat(this.getParameter(parameterName));
				} else {
					return ((Float) getParameterValue(parameterName))
						.floatValue();
				}
			} catch (NumberFormatException ex) {
				return -1;
			}
		} else
			return -1;
	}

	/**
	 * Get the parameter as a long value.
	 *
	 * @param parameterName -- the parameter name to fetch.
	 *
	 * @return -1 if the parameter is not defined or the parameter as a long.
	 */
	protected long getParameterAsLong(String parameterName) {
		if (this.getParameterValue(parameterName) != null) {
			try {
				if (this.getParameterValue(parameterName) instanceof String) {
					return Long.parseLong(this.getParameter(parameterName));
				} else {
					return ((Long) getParameterValue(parameterName))
						.longValue();
				}
			} catch (NumberFormatException ex) {
				return -1;
			}
		} else
			return -1;
	}

	/**
	 * Get the parameter value as a URI.
	 *
	 * @param parameterName -- the parameter name 
	 *
	 * @return value of the parameter as a URI or null if the parameter
	 *  not present.
	 */
	protected GenericURI getParameterAsURI(String parameterName) {
		Object val = getParameterValue(parameterName);
		if (val instanceof GenericURI)
			return (GenericURI) val;
		else {
			try {
				return new GenericURI((String) val);
			} catch (ParseException ex) {
				//catch ( URISyntaxException ex) { 
				return null;
			}
		}
	}

	/**
	 * Get the parameter value as a boolean.
	 *
	 * @param parameterName -- the parameter name
	 * @return boolean value of the parameter.
	 */
	protected boolean getParameterAsBoolean(String parameterName) {
		Object val = getParameterValue(parameterName);
		if (val == null) {
			return false;
		} else if (val instanceof Boolean) {
			return ((Boolean) val).booleanValue();
		} else if (val instanceof String) {
			return Boolean.valueOf((String) val).booleanValue();
		} else
			return false;
	}

	/**
	 * This is for the benifit of the TCK.
	 *
	 * @return the name value pair for the given parameter name.
	 */
	public NameValue getNameValue(String parameterName) {
		return parameters.getNameValue(parameterName);
	}

	protected abstract String encodeBody();

	public Object clone() {
		ParametersHeader retval = (ParametersHeader) super.clone();
		if (this.parameters != null)
			retval.parameters = (NameValueList) this.parameters.clone();
		return retval;
	}

}
/*
 * $Log: ParametersHeader.java,v $
 * Revision 1.5  2005/04/16 20:38:50  dmuresan
 * Canonical clone() implementations for the GenericObject and GenericObjectList hierarchies
 *
 * Revision 1.4  2004/04/15 16:20:38  mranga
 * Submitted by:  Dave Stuart at sipquest.
 * Reviewed by:  mranga
 * Bug converting String to boolean value.
 *
 * Revision 1.3  2004/01/22 13:26:29  sverker
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
