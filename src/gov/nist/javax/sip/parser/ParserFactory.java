package gov.nist.javax.sip.parser;
import java.util.Hashtable;
import java.lang.reflect.*;
import javax.sip.header.*;
import java.text.ParseException;
import gov.nist.core.*;

/**
 * A factory class that does a name lookup on a registered parser and
 * returns a header parser for the given name.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2005/10/14 20:00:24 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ParserFactory {

	private static Hashtable parserTable;
	private static final String PARSER_PACKAGE = "gov.nist.javax.sip.parser.";
	private static Class[] constructorArgs;

	static {
		parserTable = new Hashtable();
		constructorArgs = new Class[1];
		constructorArgs[0] = String.class;
		parserTable.put(ReplyToHeader.NAME.toLowerCase(), ReplyToParser.class);

		parserTable.put(
			InReplyToHeader.NAME.toLowerCase(),
			InReplyToParser.class);

		parserTable.put(
			AcceptEncodingHeader.NAME.toLowerCase(),
			AcceptEncodingParser.class);

		parserTable.put(
			AcceptLanguageHeader.NAME.toLowerCase(),
			AcceptLanguageParser.class);

		parserTable.put("t", ToParser.class);
		parserTable.put(ToHeader.NAME.toLowerCase(), ToParser.class);

		parserTable.put(FromHeader.NAME.toLowerCase(), FromParser.class);
		parserTable.put("f", FromParser.class);

		parserTable.put(CSeqHeader.NAME.toLowerCase(), CSeqParser.class);

		parserTable.put(ViaHeader.NAME.toLowerCase(), ViaParser.class);
		parserTable.put("v", ViaParser.class);

		parserTable.put(ContactHeader.NAME.toLowerCase(), ContactParser.class);
		parserTable.put("m", ContactParser.class);

		parserTable.put(
			ContentTypeHeader.NAME.toLowerCase(),
			ContentTypeParser.class);
		parserTable.put("c", ContentTypeParser.class);

		parserTable.put(
			ContentLengthHeader.NAME.toLowerCase(),
			ContentLengthParser.class);
		parserTable.put("l", ContentLengthParser.class);

		parserTable.put(
			AuthorizationHeader.NAME.toLowerCase(),
			AuthorizationParser.class);

		parserTable.put(
			WWWAuthenticateHeader.NAME.toLowerCase(),
			WWWAuthenticateParser.class);

		parserTable.put(CallIdHeader.NAME.toLowerCase(), CallIDParser.class);
		parserTable.put("i", CallIDParser.class);

		parserTable.put(RouteHeader.NAME.toLowerCase(), RouteParser.class);

		parserTable.put(
			RecordRouteHeader.NAME.toLowerCase(),
			RecordRouteParser.class);

		parserTable.put(DateHeader.NAME.toLowerCase(), DateParser.class);

		parserTable.put(
			ProxyAuthorizationHeader.NAME.toLowerCase(),
			ProxyAuthorizationParser.class);

		parserTable.put(
			ProxyAuthenticateHeader.NAME.toLowerCase(),
			ProxyAuthenticateParser.class);

		parserTable.put(
			RetryAfterHeader.NAME.toLowerCase(),
			RetryAfterParser.class);

		parserTable.put(RequireHeader.NAME.toLowerCase(), RequireParser.class);

		parserTable.put(
			ProxyRequireHeader.NAME.toLowerCase(),
			ProxyRequireParser.class);

		parserTable.put(
			TimeStampHeader.NAME.toLowerCase(),
			TimeStampParser.class);

		parserTable.put(
			UnsupportedHeader.NAME.toLowerCase(),
			UnsupportedParser.class);

		parserTable.put(
			UserAgentHeader.NAME.toLowerCase(),
			UserAgentParser.class);

		parserTable.put(
			SupportedHeader.NAME.toLowerCase(),
			SupportedParser.class);
		// bug fix by Steve Crosley
		parserTable.put("k", SupportedParser.class);

		parserTable.put(ServerHeader.NAME.toLowerCase(), ServerParser.class);

		parserTable.put(SubjectHeader.NAME.toLowerCase(), SubjectParser.class);
		parserTable.put("s", SubjectParser.class);	// JvB: added

		parserTable.put(
			SubscriptionStateHeader.NAME.toLowerCase(),
			SubscriptionStateParser.class);

		parserTable.put(
			MaxForwardsHeader.NAME.toLowerCase(),
			MaxForwardsParser.class);

		parserTable.put(
			MimeVersionHeader.NAME.toLowerCase(),
			MimeVersionParser.class);

		parserTable.put(
			MinExpiresHeader.NAME.toLowerCase(),
			MinExpiresParser.class);

		parserTable.put(
			OrganizationHeader.NAME.toLowerCase(),
			OrganizationParser.class);

		parserTable.put(
			PriorityHeader.NAME.toLowerCase(),
			PriorityParser.class);

		parserTable.put(RAckHeader.NAME.toLowerCase(), RAckParser.class);

		parserTable.put(RSeqHeader.NAME.toLowerCase(), RSeqParser.class);

		parserTable.put(ReasonHeader.NAME.toLowerCase(), ReasonParser.class);

		parserTable.put(WarningHeader.NAME.toLowerCase(), WarningParser.class);

		parserTable.put(ExpiresHeader.NAME.toLowerCase(), ExpiresParser.class);

		parserTable.put(EventHeader.NAME.toLowerCase(), EventParser.class);
		parserTable.put("o", EventParser.class);

		parserTable.put(
			ErrorInfoHeader.NAME.toLowerCase(),
			ErrorInfoParser.class);

		parserTable.put(
			ContentLanguageHeader.NAME.toLowerCase(),
			ContentLanguageParser.class);

		parserTable.put(
			ContentEncodingHeader.NAME.toLowerCase(),
			ContentEncodingParser.class);
		parserTable.put("e", ContentEncodingParser.class);

		parserTable.put(
			ContentDispositionHeader.NAME.toLowerCase(),
			ContentDispositionParser.class);

		parserTable.put(
			CallInfoHeader.NAME.toLowerCase(),
			CallInfoParser.class);

		parserTable.put(
			AuthenticationInfoHeader.NAME.toLowerCase(),
			AuthenticationInfoParser.class);

		parserTable.put(AllowHeader.NAME.toLowerCase(), AllowParser.class);

		parserTable.put(
			AllowEventsHeader.NAME.toLowerCase(),
			AllowEventsParser.class);
		parserTable.put("u", AllowEventsParser.class);

		parserTable.put(
			AlertInfoHeader.NAME.toLowerCase(),
			AlertInfoParser.class);

		parserTable.put(AcceptHeader.NAME.toLowerCase(), AcceptParser.class);

		parserTable.put(ReferToHeader.NAME.toLowerCase(), ReferToParser.class);
		// Was missing (bug noticed by Steve Crossley)
		parserTable.put("r", ReferToParser.class);

	}

	/**
	 * create a parser for a header. This is the parser factory.
	 */
	public static HeaderParser createParser(String line)
		throws ParseException {
		String headerName = Lexer.getHeaderName(line);
		String headerValue = Lexer.getHeaderValue(line);
		if (headerName == null || headerValue == null)
			throw new ParseException("The header name or value is null", 0);

		Class parserClass = (Class) parserTable.get(headerName.toLowerCase());

		if (parserClass != null) {
			try {

				Constructor cons = parserClass.getConstructor(constructorArgs);
				Object[] args = new Object[1];
				args[0] = line;
				HeaderParser retval = (HeaderParser) cons.newInstance(args);
				return retval;

			} catch (Exception ex) {
				InternalErrorHandler.handleException(ex);
				return null; // to placate the compiler.
			}

		} else {
			// Just generate a generic SIPHeader. We define
			// parsers only for the above.
			return new HeaderParser(line);
		}
	}
}
/*
 * $Log: ParserFactory.java,v $
 * Revision 1.5  2005/10/14 20:00:24  jbemmel
 * bugfix: missing parser for shortform of Subject (s)
 *
 * Revision 1.4  2005/04/04 09:29:03  dmuresan
 * Replaced new String().getClass() with String.class.
 *
 * Revision 1.3  2004/01/22 13:26:31  sverker
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
