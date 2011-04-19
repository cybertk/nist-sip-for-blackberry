package gov.nist.core;

import java.text.ParseException;

/**
 * Parser for host names.
 *
 *@version  JAIN-SIP-1.1
 *
 *@author M. Ranganathan <mranga@nist.gov> 
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *<br/>
 * IPv6 Support added by Emil Ivov (emil_ivov@yahoo.com)<br/>
 * Network Research Team (http://www-r2.u-strasbg.fr))<br/>
 * Louis Pasteur University - Strasbourg - France<br/>
 *
 *Bug fixes for corner cases were contributed by Thomas Froment.
 *
 */

public class HostNameParser extends ParserCore {

	public HostNameParser(String hname) {
		this.lexer = new LexerCore("charLexer", hname);
	}

	/** The lexer is initialized with the buffer.
	 */
	public HostNameParser(LexerCore lexer) {
		this.lexer = lexer;
		lexer.selectLexer("charLexer");
	}

	protected String domainLabel() throws ParseException {
		StringBuffer retval = new StringBuffer();
		if (debug)
			dbg_enter("domainLabel");
		try {
			while (lexer.hasMoreChars()) {
				char la = lexer.lookAhead(0);
				if (LexerCore.isAlpha(la)) {
					lexer.consume(1);
					retval.append(la);
				} else if (LexerCore.isDigit(la)) {
					lexer.consume(1);
					retval.append(la);
				} else if (la == '-') {
					lexer.consume(1);
					retval.append(la);
				} else
					break;
			}
			//Debug.println("returning " + retval.toString());
			return retval.toString();
		} finally {
			if (debug)
				dbg_leave("domainLabel");
		}
	}

	protected String ipv6Reference() throws ParseException {
		StringBuffer retval = new StringBuffer();
		if (debug)
			dbg_enter("ipv6Reference");
		try {
			while (lexer.hasMoreChars()) {
				char la = lexer.lookAhead(0);
				if (LexerCore.isHexDigit(la)) {
					lexer.consume(1);
					retval.append(la);
				} else if (la == '.' || la == ':' || la == '[') {
					lexer.consume(1);
					retval.append(la);
				} else if (la == ']') {
					lexer.consume(1);
					retval.append(la);
					return retval.toString();
				} else
					break;
			}

			throw new ParseException(
				lexer.getBuffer() + ": Illegal Host name ",
				lexer.getPtr());
		} finally {
			if (debug)
				dbg_leave("ipv6Reference");
		}
	}

	public Host host() throws ParseException {
		if (debug)
			dbg_enter("host");
		try {
			StringBuffer hname = new StringBuffer();

			//IPv6 referene
			if (lexer.lookAhead(0) == '[') {
				hname.append(ipv6Reference());
			}
			//IPv4 address or hostname
			else {
				String nextTok = domainLabel();
				hname.append(nextTok);
				// Bug reported by Stuart Woodsford (used to barf on
				// more than 4 components to the name).
				while (lexer.hasMoreChars()) {
					// Reached the end of the buffer.
					if (lexer.lookAhead(0) == '.') {
						lexer.consume(1);
						nextTok = domainLabel();
						hname.append(".");
						hname.append(nextTok);
					} else
						break;
				}
			}

			String hostname = hname.toString();
			if (hostname.equals(""))
				throw new ParseException(
					lexer.getBuffer() + ": Illegal Host name ",
					lexer.getPtr());
			else
				return new Host(hostname);
		} finally {
			if (debug)
				dbg_leave("host");
		}
	}

	public HostPort hostPort() throws ParseException {
		if (debug)
			dbg_enter("hostPort");
		try {
			Host host = this.host();
			HostPort hp = new HostPort();
			hp.setHost(host);
			// Has a port?
			lexer.SPorHT(); // white space before ":port" should be accepted
			if (lexer.hasMoreChars() && lexer.lookAhead(0) == ':') {
				lexer.consume(1);
				lexer.SPorHT(); // white space before port number should be accepted
				try {
					String port = lexer.number();
					hp.setPort(Integer.parseInt(port));
				} catch (NumberFormatException nfe) {
					throw new ParseException(
						lexer.getBuffer() + " :Error parsing port ",
						lexer.getPtr());
				}
			}
			return hp;
		} finally {
			if (debug)
				dbg_leave("hostPort");
		}

	}

	public static void main(String args[]) throws ParseException {
		String hostNames[] =
			{
				"foo.bar.com:1234",
				"proxima.chaplin.bt.co.uk",
				"129.6.55.181:2345",
				":1234",
				"foo.bar.com:         1234",
				"foo.bar.com     :      1234   ",
			};
			
		for (int i = 0; i < hostNames.length; i++) {
			try {
				HostNameParser hnp = new HostNameParser(hostNames[i]);
				HostPort hp = hnp.hostPort();
				System.out.println("["+hp.encode()+"]");
			} catch (ParseException ex) {
				System.out.println("exception text = " + ex.getMessage());
			}
		}

	}

}
/*
* $Log: HostNameParser.java,v $
* Revision 1.6  2004/04/22 22:51:15  mranga
* Submitted by:  Thomas Froment
* Reviewed by:   mranga
*
* Fixed corner cases.
*
 * Revision 1.5  2004/04/20 16:37:40  mranga
 * Submitted by:  Thomas Froment
 * Reviewed by:  mranga
 * Fixes a parsing corner case.
 *
 * Revision 1.4  2004/01/22 13:26:27  sverker
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
