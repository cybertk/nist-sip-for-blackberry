package gov.nist.javax.sip.parser;

import java.text.ParseException;
import gov.nist.javax.sip.header.*;

/**
 * Parser for a list of route headers.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004/01/22 13:26:31 $
 *
 * @author Olivier Deruelle <deruelle@nist.gov>  <br/>
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class RecordRouteParser extends AddressParametersParser {

	/**
	 * Constructor
	 * @param recordRoute message to parse to set
	 */
	public RecordRouteParser(String recordRoute) {
		super(recordRoute);
	}

	protected RecordRouteParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String message and generate the RecordRoute List Object
	 * @return SIPHeader the RecordRoute List object
	 * @throws ParseException if errors occur during the parsing
	 */
	public SIPHeader parse() throws ParseException {
		RecordRouteList recordRouteList = new RecordRouteList();

		if (debug)
			dbg_enter("RecordRouteParser.parse");

		try {
			this.lexer.match(TokenTypes.RECORD_ROUTE);
			this.lexer.SPorHT();
			this.lexer.match(':');
			this.lexer.SPorHT();
			while (true) {
				RecordRoute recordRoute = new RecordRoute();
				super.parse(recordRoute);
				recordRouteList.add(recordRoute);
				this.lexer.SPorHT();
				if (lexer.lookAhead(0) == ',') {
					this.lexer.match(',');
					this.lexer.SPorHT();
				} else if (lexer.lookAhead(0) == '\n')
					break;
				else
					throw createParseException("unexpected char");
			}
			return recordRouteList;
		} finally {
			if (debug)
				dbg_leave("RecordRouteParser.parse");
		}

	}

	/**
	        public static void main(String args[]) throws ParseException {
			String rou[] = {
				"Record-Route: <sip:bob@biloxi.com;maddr=10.1.1.1>,"+
	                        "<sip:bob@biloxi.com;maddr=10.2.1.1>\n",
	                        
				"Record-Route: <sip:UserB@there.com;maddr=ss2.wcom.com>\n",
	                        
	                        "Record-Route: <sip:+1-650-555-2222@iftgw.there.com;"+
	                        "maddr=ss1.wcom.com>\n",
	                        
	                        "Record-Route: <sip:UserB@there.com;maddr=ss2.wcom.com>,"+
	                        "<sip:UserB@there.com;maddr=ss1.wcom.com>\n"  
	                };
				
			for (int i = 0; i < rou.length; i++ ) {
			    RecordRouteParser rp = 
				  new RecordRouteParser(rou[i]);
			    RecordRouteList recordRouteList = (RecordRouteList) rp.parse();
			    System.out.println("encoded = " +recordRouteList.encode());
			}
				
		}
	*/
}
/*
 * $Log: RecordRouteParser.java,v $
 * Revision 1.4  2004/01/22 13:26:31  sverker
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
