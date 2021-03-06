package gov.nist.javax.sdp.parser;
import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;
import java.text.*;
import java.util.*;

/**
 * Parser for Media field.
 *
 * @version JAIN-SDP-PUBLIC-RELEASE $Revision: 1.2 $ $Date: 2004/01/22 13:26:28 $
 *
 * @author  Olivier Deruelle <deruelle@nist.gov>
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class MediaFieldParser extends SDPParser {

	/** Creates new MediaFieldParser */
	public MediaFieldParser(String mediaField) {
		lexer = new Lexer("charLexer", mediaField);
	}

	public MediaField mediaField() throws ParseException {
		if (Debug.parserDebug)
			dbg_enter("mediaField");
		try {
			MediaField mediaField = new MediaField();

			lexer.match('m');
			lexer.SPorHT();
			lexer.match('=');
			lexer.SPorHT();

			lexer.match(Lexer.ID);
			Token media = lexer.getNextToken();
			mediaField.setMedia(media.getTokenValue());
			this.lexer.SPorHT();

			lexer.match(Lexer.ID);
			Token port = lexer.getNextToken();
			mediaField.setPort(Integer.parseInt(port.getTokenValue()));

			this.lexer.SPorHT();

			// Some strange media formatting from Sun Ray systems with media
			// reported by Emil Ivov and Iain Macdonnell at Sun 
			if (lexer.hasMoreChars() && lexer.lookAhead(1) == '\n')
				return mediaField;

			if (lexer.lookAhead(0) == '/') {
				// The number of ports is present:
				lexer.consume(1);
				lexer.match(Lexer.ID);
				Token portsNumber = lexer.getNextToken();
				mediaField.setNports(
					Integer.parseInt(portsNumber.getTokenValue()));
				this.lexer.SPorHT();
			}

			lexer.match(Lexer.ID);
			Token token = lexer.getNextToken();
			this.lexer.SPorHT();
			String transport = token.getTokenValue();
			if (lexer.lookAhead(0) == '/') {
				lexer.consume(1);
				lexer.match(Lexer.ID);
				Token transportTemp = lexer.getNextToken();
				transport = transport + "/" + transportTemp.getTokenValue();
				this.lexer.SPorHT();
			}

			mediaField.setProto(transport);

			// The formats list:
			Vector formatList = new Vector();
			while (lexer.hasMoreChars()) {
				if (lexer.lookAhead(0) == '\n' || lexer.lookAhead(0) == '\r')
					break;
				this.lexer.SPorHT();
				//while(lexer.lookAhead(0) == ' ') lexer.consume(1);
				lexer.match(Lexer.ID);
				Token tok = lexer.getNextToken();
				this.lexer.SPorHT();
				String format = tok.getTokenValue().trim();
				if (!format.equals(""))
					formatList.add(format);
			}
			mediaField.setFormats(formatList);

			return mediaField;
		} catch (Exception e) {
			e.printStackTrace();
			//System.out.println(lexer.getPtr());
			//System.out.println("char = [" + lexer.lookAhead(0) +"]");
			throw new ParseException(lexer.getBuffer(), lexer.getPtr());
		} finally {
			dbg_leave("mediaField");
		}
	}

	public SDPField parse() throws ParseException {
		return this.mediaField();
	}

	/**
	    public static void main(String[] args) throws ParseException {
		    String media[] = {
				"m=video 0\r\n",
				"m=audio 50006 RTP/AVP 0 8 4 18\r\n",
				"m=video 49170/2 RTP/AVP 31\n",
	                        "m=video 49232 RTP/AVP 0\n",
	                        "m=audio 49230 RTP/AVP 96 97 98\n",
	                        "m=application 32416 udp wb\n",
	                        "m=control 49234 H323 mc\n",
	                        "m=audio 50012 RTP/AVP 0 8 4 18\n",
	                        "m=image 49172 udptl t38\n"
	                };
	
		    for (int i = 0; i < media.length; i++) {
		       MediaFieldParser mediaFieldParser=new MediaFieldParser(
	                media[i] );
			System.out.println("toParse: " + media[i]);
			MediaField mediaField=mediaFieldParser.mediaField();
			System.out.println("encoded: " +mediaField.encode());
		    }
	
		}
	**/
}
/*
 * $Log: MediaFieldParser.java,v $
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
