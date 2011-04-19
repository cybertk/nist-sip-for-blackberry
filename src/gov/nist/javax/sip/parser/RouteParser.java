package gov.nist.javax.sip.parser;
import java.text.ParseException;
import gov.nist.javax.sip.header.*;

/**
 * Parser for a list of route headers.
 *
 * @version  JAIN-SIP-1.1
 *
 * @author Olivier Deruelle <deruelle@nist.gov>  <br/>
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 *@version 1.0
 */
public class RouteParser extends AddressParametersParser {

	/**
	 * Constructor
	 * @param route message to parse to set
	 */
	public RouteParser(String route) {
		super(route);
	}

	protected RouteParser(Lexer lexer) {
		super(lexer);
	}

	/** parse the String message and generate the Route List Object
	 * @return SIPHeader the Route List object
	 * @throws SIPParseException if errors occur during the parsing
	 */
	public SIPHeader parse() throws ParseException {
		RouteList routeList = new RouteList();
		if (debug)
			dbg_enter("parse");

		try {
			this.lexer.match(TokenTypes.ROUTE);
			this.lexer.SPorHT();
			this.lexer.match(':');
			this.lexer.SPorHT();
			while (true) {
				Route route = new Route();
				super.parse(route);
				routeList.add(route);
				this.lexer.SPorHT();
				if (lexer.lookAhead(0) == ',') {
					this.lexer.match(',');
					this.lexer.SPorHT();
				} else if (lexer.lookAhead(0) == '\n')
					break;
				else
					throw createParseException("unexpected char");
			}
			return routeList;
		} finally {
			if (debug)
				dbg_leave("parse");
		}

	}

	/**
	        public static void main(String args[]) throws ParseException {
		String rou[] = {
	     "Route: <sip:alice@atlanta.com>\n",
	     "Route: sip:bob@biloxi.com \n",
	     "Route: sip:alice@atlanta.com, sip:bob@biloxi.com, sip:carol@chicago.com\n"
	         };
				
			for (int i = 0; i < rou.length; i++ ) {
			    RouteParser rp = 
				  new RouteParser(rou[i]);
			    RouteList routeList = (RouteList) rp.parse();
			    System.out.println("encoded = " +routeList.encode());
			}
				
		}
		
	*/
}
/*
 * $Log: RouteParser.java,v $
 * Revision 1.4  2004/01/22 13:26:32  sverker
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
