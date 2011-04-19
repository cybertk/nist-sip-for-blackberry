/*****************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).     *
 *****************************************************************************/
package gov.nist.javax.sip.header;
import java.lang.reflect.*;
import gov.nist.core.*;

/**
 * Root class for all singleton objects in this package:
 * specializes the gov.nist.sip.header.GenericObject class for SIPHeader
 * related objects.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2005/04/16 20:38:50 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */

public abstract class SIPObject extends GenericObject {
	protected static final String SIPHEADERS_PACKAGE =
		PackageNames.SIPHEADERS_PACKAGE;
	protected static final String SIP_PACKAGE = PackageNames.SIP_PACKAGE;

	/** default Constructor
	 */
	protected SIPObject() {
		super();
	}

	/**
	 * Recursively override the fields of this object with the fields
	 * of a new object. This is useful when you want to genrate a template
	 * and override the fields of an incoming SIPMessage with another
	 * SIP message that you have already generated.
	 *
	 * @param mergeObject is the replacement object.  The override
	 * obect must be of the same class as this object.
	 * Set any fields that you do not want to override as null in the
	 * mergeOject object.
	 */
	public void merge(Object mergeObject) {
		if (!mergeObject.getClass().equals(this.getClass()))
			throw new IllegalArgumentException("Bad override object");
		// Base case.
		if (mergeObject == null)
			return;
		Class myclass = this.getClass();
		while (true) {
			Field[] fields = myclass.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field f = fields[i];
				int modifier = f.getModifiers();
				if (Modifier.isPrivate(modifier)) {
					continue;
				} else if (Modifier.isStatic(modifier)) {
					continue;
				} else if (Modifier.isInterface(modifier)) {
					continue;
				} else if (Modifier.isFinal(modifier)) {
					continue;
				}
				Class fieldType = f.getType();
				String fieldName = f.getName();
				String fname = fieldType.toString();
				try {
					// Primitive fields are printed with type: value
					if (fieldType.isPrimitive()) {
						if (fname.compareTo("int") == 0) {
							int intfield = f.getInt(mergeObject);
							f.setInt(this, intfield);
						} else if (fname.compareTo("short") == 0) {
							short shortField = f.getShort(mergeObject);
							f.setShort(this, shortField);
						} else if (fname.compareTo("char") == 0) {
							char charField = f.getChar(mergeObject);
							f.setChar(this, charField);
						} else if (fname.compareTo("long") == 0) {
							long longField = f.getLong(mergeObject);
							f.setLong(this, longField);
						} else if (fname.compareTo("boolean") == 0) {
							boolean booleanField = f.getBoolean(mergeObject);
							f.setBoolean(this, booleanField);
						} else if (fname.compareTo("double") == 0) {
							double doubleField = f.getDouble(mergeObject);
							f.setDouble(this, doubleField);
						} else if (fname.compareTo("float") == 0) {
							float floatField = f.getFloat(mergeObject);
							f.setFloat(this, floatField);
						}
					} else {
						Object obj = f.get(this);
						Object mobj = f.get(mergeObject);
						if (mobj == null)
							continue;
						if (obj == null) {
							f.set(this, mobj);
							continue;
						}
						if (obj instanceof GenericObject) {
							GenericObject gobj = (GenericObject) obj;
							gobj.merge(mobj);
						} else if (obj instanceof GenericObjectList) {
							GenericObjectList gobjList =
								(GenericObjectList) obj;
							gobjList.mergeObjects((GenericObjectList) mobj);
						} else {
							f.set(this, mobj);
						}
					}
				} catch (IllegalAccessException ex1) {
					System.out.println("fieldName = " + fieldName);
					ex1.printStackTrace();
					continue; // we are accessing a private field...
				}
			}
			if (myclass.equals(SIPObject.class))
				break;
			else
				myclass = myclass.getSuperclass();
		}
	}

	/** Debug function
	 */
	public void dbgPrint() {
		super.dbgPrint();
	}

	/** Encode the header into a String.
	 * @return String
	 */
	public abstract String encode();

	/**
	 * An introspection based equality predicate for SIPObjects.
	 *@param other the other object to test against.
	 */
	public boolean equals(Object other) {
		if (!this.getClass().equals(other.getClass()))
			return false;
		SIPObject that = (SIPObject) other;
		Class myclass = this.getClass();
		Class hisclass = other.getClass();
		while (true) {
			Field[] fields = myclass.getDeclaredFields();
			if (!hisclass.equals(myclass))
				return false;
			Field[] hisfields = hisclass.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field f = fields[i];
				Field g = hisfields[i];
				// Only print protected and public members.
				int modifier = f.getModifiers();
				if ((modifier & Modifier.PRIVATE) == Modifier.PRIVATE)
					continue;
				Class fieldType = f.getType();
				String fieldName = f.getName();
				if (fieldName.compareTo("stringRepresentation") == 0) {
					continue;
				}
				if (fieldName.compareTo("indentation") == 0) {
					continue;
				}
				try {
					// Primitive fields are printed with type: value
					if (fieldType.isPrimitive()) {
						String fname = fieldType.toString();
						if (fname.compareTo("int") == 0) {
							if (f.getInt(this) != g.getInt(that))
								return false;
						} else if (fname.compareTo("short") == 0) {
							if (f.getShort(this) != g.getShort(that))
								return false;
						} else if (fname.compareTo("char") == 0) {
							if (f.getChar(this) != g.getChar(that))
								return false;
						} else if (fname.compareTo("long") == 0) {
							if (f.getLong(this) != g.getLong(that))
								return false;
						} else if (fname.compareTo("boolean") == 0) {
							if (f.getBoolean(this) != g.getBoolean(that))
								return false;
						} else if (fname.compareTo("double") == 0) {
							if (f.getDouble(this) != g.getDouble(that))
								return false;
						} else if (fname.compareTo("float") == 0) {
							if (f.getFloat(this) != g.getFloat(that))
								return false;
						}
					} else if (g.get(that) == f.get(this))
						continue;
					else if (f.get(this) == null && g.get(that) != null)
						return false;
					else if (g.get(that) == null && f.get(this) != null)
						return false;
					else if (!f.get(this).equals(g.get(that)))
						return false;
				} catch (IllegalAccessException ex1) {
					System.out.println("accessed field " + fieldName);
					System.out.println("modifier  " + modifier);
					System.out.println("modifier.private  " + Modifier.PRIVATE);
					InternalErrorHandler.handleException(ex1);
				}
			}
			if (myclass.equals(SIPObject.class))
				break;
			else {
				myclass = myclass.getSuperclass();
				hisclass = hisclass.getSuperclass();
			}
		}
		return true;
	}

	/** An introspection based predicate matching using a template
	 * object. Allows for partial match of two protocl Objects.
	 * You can set a generalized matcher (using regular expressions
	 * for example) by implementing the Match interface and registering
	 * it with the template.
	 *@param other the match pattern to test against. The match object
	 * has to be of the same type (class). Primitive types
	 * and non-sip fields that are non null are matched for equality.
	 * Null in any field  matches anything. Some book-keeping fields
	 * are ignored when making the comparison.
	 *
	 */
	public boolean match(Object other) {
		if (other == null) {
			return true;
		}
		
		if (!this.getClass().equals(other.getClass()))
			return false;
		GenericObject that = (GenericObject) other;
		Class myclass = this.getClass();
		Class hisclass = other.getClass();
		while (true) {
			Field[] fields = myclass.getDeclaredFields();
			Field[] hisfields = hisclass.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field f = fields[i];
				Field g = hisfields[i];
				// Only print protected and public members.
				int modifier = f.getModifiers();
				if ((modifier & Modifier.PRIVATE) == Modifier.PRIVATE)
					continue;
				Class fieldType = f.getType();
				String fieldName = f.getName();
				if (fieldName.compareTo("stringRepresentation") == 0) {
					continue;
				}
				if (fieldName.compareTo("indentation") == 0) {
					continue;
				}
				try {
					if (fieldType.isPrimitive()) {
						String fname = fieldType.toString();
						if (fname.compareTo("int") == 0) {
							if (f.getInt(this) != g.getInt(that))
								return false;
						} else if (fname.compareTo("short") == 0) {
							if (f.getShort(this) != g.getShort(that))
								return false;
						} else if (fname.compareTo("char") == 0) {
							if (f.getChar(this) != g.getChar(that))
								return false;
						} else if (fname.compareTo("long") == 0) {
							if (f.getLong(this) != g.getLong(that))
								return false;
						} else if (fname.compareTo("boolean") == 0) {
							if (f.getBoolean(this) != g.getBoolean(that))
								return false;
						} else if (fname.compareTo("double") == 0) {
							if (f.getDouble(this) != g.getDouble(that))
								return false;
						} else if (fname.compareTo("float") == 0) {
							if (f.getFloat(this) != g.getFloat(that))
								return false;
						} else {
							InternalErrorHandler.handleException(
								"unknown type");
						}
					} else {
						Object myObj = f.get(this);
						Object hisObj = g.get(that);
						if (hisObj != null && myObj == null)
							return false;
						else if (hisObj == null && myObj != null)
							continue;
						else if (hisObj == null && myObj == null)
							continue;
						else if (
							hisObj instanceof java.lang.String
								&& myObj instanceof java.lang.String) {
							if ((((String) hisObj).trim()).equals(""))
								continue;
							if (((String) myObj)
								.compareToIgnoreCase((String) hisObj)
								!= 0)
								return false;
						} else if (
							hisObj != null
								&& GenericObject.isMySubclass(myObj.getClass())
								&& GenericObject.isMySubclass(hisObj.getClass())
								&& myObj.getClass().equals(hisObj.getClass())
								&& ((GenericObject) hisObj).getMatcher()
									!= null) {
							String myObjEncoded =
								((GenericObject) myObj).encode();
							boolean retval =
								((GenericObject) hisObj).getMatcher().match(
									myObjEncoded);
							if (!retval)
								return false;
						} else if (
							GenericObject.isMySubclass(myObj.getClass())
								&& !((GenericObject) myObj).match(hisObj))
							return false;
						else if (
							GenericObjectList.isMySubclass(myObj.getClass())
								&& !((GenericObjectList) myObj).match(hisObj))
							return false;

					}
				} catch (IllegalAccessException ex1) {
					InternalErrorHandler.handleException(ex1);
				}
			}
			if (myclass.equals(SIPObject.class))
				break;
			else {
				myclass = myclass.getSuperclass();
				hisclass = hisclass.getSuperclass();
			}
		}
		return true;
	}

	/**
	 * An introspection based string formatting method. We need this because
	 * in this package (although it is an exact duplicate of the one in
	 * the superclass) because it needs to access the protected members
	 * of the other objects in this class.
	 * @return String
	 */
	public String debugDump() {
		stringRepresentation = "";
		Class myclass = getClass();
		sprint(myclass.getName());
		sprint("{");
		Field[] fields = myclass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			// Only print protected and public members.
			int modifier = f.getModifiers();
			if ((modifier & Modifier.PRIVATE) == Modifier.PRIVATE)
				continue;
			Class fieldType = f.getType();
			String fieldName = f.getName();
			if (fieldName.compareTo("stringRepresentation") == 0) {
				// avoid nasty recursions...
				continue;
			}
			if (fieldName.compareTo("indentation") == 0) {
				// formatting stuff - not relevant here.
				continue;
			}
			sprint(fieldName + ":");
			try {
				// Primitive fields are printed with type: value
				if (fieldType.isPrimitive()) {
					String fname = fieldType.toString();
					sprint(fname + ":");
					if (fname.compareTo("int") == 0) {
						int intfield = f.getInt(this);
						sprint(intfield);
					} else if (fname.compareTo("short") == 0) {
						short shortField = f.getShort(this);
						sprint(shortField);
					} else if (fname.compareTo("char") == 0) {
						char charField = f.getChar(this);
						sprint(charField);
					} else if (fname.compareTo("long") == 0) {
						long longField = f.getLong(this);
						sprint(longField);
					} else if (fname.compareTo("boolean") == 0) {
						boolean booleanField = f.getBoolean(this);
						sprint(booleanField);
					} else if (fname.compareTo("double") == 0) {
						double doubleField = f.getDouble(this);
						sprint(doubleField);
					} else if (fname.compareTo("float") == 0) {
						float floatField = f.getFloat(this);
						sprint(floatField);
					}
				} else if (GenericObject.class.isAssignableFrom(fieldType)) {
					if (f.get(this) != null) {
						sprint(
							((GenericObject) f.get(this)).debugDump(
								indentation + 1));
					} else {
						sprint("<null>");
					}

				} else if (
					GenericObjectList.class.isAssignableFrom(fieldType)) {
					if (f.get(this) != null) {
						sprint(
							((GenericObjectList) f.get(this)).debugDump(
								indentation + 1));
					} else {
						sprint("<null>");
					}

				} else {
					// Dont do recursion on things that are not
					// of our header type...
					if (f.get(this) != null) {
						sprint(f.get(this).getClass().getName() + ":");
					} else {
						sprint(fieldType.getName() + ":");
					}

					sprint("{");
					if (f.get(this) != null) {
						sprint(f.get(this).toString());
					} else {
						sprint("<null>");
					}
					sprint("}");
				}
			} catch (IllegalAccessException ex1) {
				continue; // we are accessing a private field...
			}
		}
		sprint("}");
		return stringRepresentation;
	}

	/**
	 * Formatter with a given starting indentation (for nested structs).
	 * @param indent int to set
	 * @return String
	 */
	public String debugDump(int indent) {
		int save = indentation;
		indentation = indent;
		String retval = this.debugDump();
		indentation = save;
		return retval;
	}

	/**
	 * Do a recursive find and replace of objects pointed to by this
	 * object.
	 * @since v1.0
	 * @param objectText is the canonical string representation of
	 *		the object that we want to replace.
	 * @param replacement is the object that we want to replace it
	 *		with.
	 * @param matchSubstring a boolean which tells if we should match
	 * 		a substring of the target object
	 * A replacement will occur if a portion of the structure is found
	 * with matching encoded text as objectText and with the same class
	 * as replacement.
	 * (i.e. if matchSubstring is true an object is a  candidate for
	 *  replacement if objectText is a substring of
	 *  candidate.encode() && candidate.class.equals(replacement.class)
	 * otherwise the match test is an equality test.)
	 *@exception IllegalArgumentException on null args and
	 * if replacementObject does not derive from GenericObject or
	 * GenericObjectList
	 */
	public void replace(
		String objectText,
		GenericObject replacement,
		boolean matchSubstring)
		throws IllegalArgumentException {
		if (objectText == null || replacement == null) {
			throw new IllegalArgumentException("null argument!");
		}
		Class replacementClass = replacement.getClass();
		Class myclass = getClass();
		while (true) {
			Field[] fields = myclass.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field f = fields[i];
				Class fieldType = f.getType();
				if (!GenericObject.class.isAssignableFrom(fieldType)
					&& !GenericObjectList.class.isAssignableFrom(fieldType)) {
					continue;
				} else if (
					(f.getModifiers() & Modifier.PRIVATE)
						== Modifier.PRIVATE) {
					continue;
				}

				try {
					if (fieldType.equals(replacementClass)) {

						if (GenericObject.isMySubclass(replacementClass)) {
							GenericObject obj = (GenericObject) f.get(this);
							if (!matchSubstring) {
								if (objectText.compareTo(obj.encode()) == 0) {
									f.set(this, replacement);
								}
							} else {
								// Substring match is specified
								if (obj.encode().indexOf(objectText) >= 0) {
									f.set(this, replacement);
								}
							}
						}
					} else if (
						GenericObjectList.isMySubclass(replacementClass)) {
						GenericObjectList obj = (GenericObjectList) f.get(this);
						if (!matchSubstring) {
							if (objectText.compareTo(obj.encode()) == 0) {
								f.set(this, replacement);
							}
						} else {
							if (obj.encode().indexOf(objectText) >= 0) {
								f.set(this, replacement);
							}
						}
					} else if (
						gov.nist.core.GenericObject.class.isAssignableFrom(
							fieldType)) {
						GenericObject g = (GenericObject) f.get(this);
						g.replace(objectText, replacement, matchSubstring);
					} else if (
						gov.nist.core.GenericObjectList.class.isAssignableFrom(
							fieldType)) {
						GenericObjectList g = (GenericObjectList) f.get(this);
						g.replace(objectText, replacement, matchSubstring);
					}
				} catch (IllegalAccessException ex) {
					InternalErrorHandler.handleException(ex);
				}
			}
			if (myclass.equals(SIPObject.class))
				break;
			else {
				myclass = myclass.getSuperclass();
			}
		}

	}
	/**
	 * Do a find and replace of objects.
	 * @since v1.0
	 *@param objectText Canonical string representation of the
	 *  portion we want to replace.
	 *@param replacement object we want to replace this portion with.
	 * A replacement will occur if a portion of the structure is found
	 * with the matching encoded text as objectText and with the same class
	 * as the replacement.
	 *@param matchSubstring is true if we want to match the encoded
	 * text of a candidate object as a substring of the encoded
	 * target text. ( match occurs is objectText is a substring of
	 * the encoded text of an object with the same class as replacement.)
	 *
	 *@throws  IllegalArgumentException on null args and if
	 * replacementObject
	 * does not derive from GenericObject or GenericObjectList
	 */
	public void replace(
		String objectText,
		GenericObjectList replacement,
		boolean matchSubstring)
		throws IllegalArgumentException {

		if (objectText == null || replacement == null) {
			throw new IllegalArgumentException("null argument!");
		}
		Class replacementClass = replacement.getClass();
		Class myclass = getClass();
		while (true) {
			Field[] fields = myclass.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field f = fields[i];
				Class fieldType = f.getType();
				if (!GenericObject.class.isAssignableFrom(fieldType)
					&& !GenericObjectList.class.isAssignableFrom(fieldType)) {
					continue;
				} else if (f.getModifiers() == Modifier.PRIVATE) {
					continue;
				}
				try {
					if (fieldType.equals(replacementClass)) {
						if (GenericObject.isMySubclass(replacementClass)) {
							GenericObject obj = (GenericObject) f.get(this);
							if (!matchSubstring) {
								if (objectText.compareTo(obj.encode()) == 0) {
									f.set(this, replacement);
								}
							} else {
								if (obj.encode().indexOf(objectText) >= 0) {
									f.set(this, replacement);
								}
							}
						} else if (
							GenericObjectList.isMySubclass(replacementClass)) {
							GenericObjectList obj =
								(GenericObjectList) f.get(this);
							if (!matchSubstring) {
								if (objectText.compareTo(obj.encode()) == 0) {
									f.set(this, replacement);
								}
							} else {
								if (obj.encode().indexOf(objectText) >= 0) {
									f.set(this, replacement);
								}
							}
						}

					} else if (
						gov.nist.core.GenericObject.class.isAssignableFrom(
							fieldType)) {
						GenericObject g = (GenericObject) f.get(this);
						g.replace(objectText, replacement, matchSubstring);
					} else if (
						gov.nist.core.GenericObjectList.class.isAssignableFrom(
							fieldType)) {
						GenericObjectList g = (GenericObjectList) f.get(this);
						g.replace(objectText, replacement, matchSubstring);
					}
				} catch (IllegalAccessException ex) {
					InternalErrorHandler.handleException(ex);
				}
			}
			if (myclass.equals(SIPObject.class))
				break;
			else {
				myclass = myclass.getSuperclass();
			}
		}

	}
	/**
	 * Do a recursive find and replace of objects pointed to by this
	 * object based on regular expression pattern matching.
	 * @since v1.0
	 *@param regexp  regular expression for the object we want to find.
	 * This is generated using a regular expression matching package
	 * such as the apache regexp package.
	 *@param replacement object we want to replace this portion with.
	 * A replacement will occur if a portion of the structure is found
	 * with a match of the  encoded text
	 * with objectText and with the same class as replacement.
	 */
	public void replace(Match regexp, GenericObjectList replacement)
		throws IllegalArgumentException {

		if (regexp == null || replacement == null) {
			throw new IllegalArgumentException("null argument!");
		}
		Class replacementClass = replacement.getClass();
		Class myclass = getClass();
		Field[] fields = myclass.getDeclaredFields();
		while (true) {
			for (int i = 0; i < fields.length; i++) {
				Field f = fields[i];
				Class fieldType = f.getType();
				if ((!GenericObject.class.isAssignableFrom(fieldType))
					&& (!GenericObjectList.class.isAssignableFrom(fieldType))) {
					continue;
				} else if (
					(f.getModifiers() & Modifier.PRIVATE)
						== Modifier.PRIVATE) {
					continue;
				}
				try {
					if (fieldType.equals(replacementClass)) {
						if (GenericObject.isMySubclass(replacementClass)) {
							GenericObject obj = (GenericObject) f.get(this);
							if (regexp.match(obj.encode()))
								f.set(this, replacement);
						} else if (
							GenericObjectList.isMySubclass(replacementClass)) {
							GenericObjectList obj =
								(GenericObjectList) f.get(this);
							if (regexp.match(obj.encode()))
								f.set(this, replacement);
						}

					} else if (
						GenericObject.class.isAssignableFrom(fieldType)) {
						GenericObject g = (GenericObject) f.get(this);
						g.replace(regexp, replacement);
					} else if (
						GenericObjectList.class.isAssignableFrom(fieldType)) {
						GenericObjectList g = (GenericObjectList) f.get(this);
						g.replace(regexp, replacement);
					}
				} catch (IllegalAccessException ex) {
					InternalErrorHandler.handleException(ex);
				}
			}
			if (myclass.equals(SIPObject.class))
				break;
			else {
				myclass = myclass.getSuperclass();
			}
		}
	}

	/**
	 * Do a find and replace of objects based on regular expression
	 * matching of fields.
	 * @param regexp is the match expression (i.e. implementation of
	 *		the Match interface) for
	 *		the object that we want to replace.
	 * @param replacement is the object that we want to replace it
	 *		with.
	 * A replacement will occur if a portion of the structure is found
	 * that matches according to the given regexp and if the class of
	 * the replaced field matches the replacement.
	 */
	public void replace(Match regexp, GenericObject replacement)
		throws IllegalArgumentException {
		if (regexp == null || replacement == null) {
			throw new IllegalArgumentException("null argument!");
		}
		Class replacementClass = replacement.getClass();
		Class myclass = getClass();
		Field[] fields = myclass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			Class fieldType = f.getType();
			if ((!GenericObject.class.isAssignableFrom(fieldType))
				&& (!GenericObjectList.class.isAssignableFrom(fieldType))) {
				continue;
			} else if (f.getModifiers() == Modifier.PRIVATE) {
				continue;
			}

			try {
				if (fieldType.equals(replacementClass)) {

					if (GenericObject.isMySubclass(replacementClass)) {
						GenericObject obj = (GenericObject) f.get(this);
						if (regexp.match(obj.encode()))
							f.set(this, replacement);
					}
				} else if (GenericObjectList.isMySubclass(replacementClass)) {
					GenericObjectList obj = (GenericObjectList) f.get(this);
					if (regexp.match(obj.encode()))
						f.set(this, replacement);
				} else if (
					gov.nist.core.GenericObject.class.isAssignableFrom(
						fieldType)) {
					GenericObject g = (GenericObject) f.get(this);
					g.replace(regexp, replacement);
				} else if (
					gov.nist.core.GenericObjectList.class.isAssignableFrom(
						fieldType)) {
					GenericObjectList g = (GenericObjectList) f.get(this);
					g.replace(regexp, replacement);
				}
			} catch (IllegalAccessException ex) {
				InternalErrorHandler.handleException(ex);
			}
		}

	}

	public String toString() {
		return this.encode();
	}

}
/*
 * $Log: SIPObject.java,v $
 * Revision 1.5  2005/04/16 20:38:50  dmuresan
 * Canonical clone() implementations for the GenericObject and GenericObjectList hierarchies
 *
 * Revision 1.4  2004/09/13 15:12:27  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  Ben Evans (Open Cloud)
 * Reviewed by:   M. Ranganathan (NIST)
 *
 * Fixes numerous TCK problems
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
 * Revision 1.3  2004/09/10 18:26:07  mranga
 * Submitted by:  mranga
 * Reviewed by:   mranga
 * added match examples for the benifit of those building test frameworks.
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
