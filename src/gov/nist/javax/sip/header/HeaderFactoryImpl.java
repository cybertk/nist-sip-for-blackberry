/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import javax.sip.header.*;
import gov.nist.javax.sip.parser.*;
import javax.sip.address.*;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import java.util.*;
import gov.nist.javax.sip.address.*;

/** Implementation of the JAIN SIP  HeaderFactory
* 
* @version JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2005/05/24 08:50:49 $
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class HeaderFactoryImpl implements HeaderFactory {

	/**
	* Creates a new AcceptEncodingHeader based on the newly supplied encoding 
	* value.
	*
	* @param encoding - the new string containing the encoding value.
	* @throws ParseException which signals that an error has been reached
	* unexpectedly while parsing the encoding value.
	* @return the newly created AcceptEncodingHeader object.
	*/
	public AcceptEncodingHeader createAcceptEncodingHeader(String encoding)
		throws ParseException {
		if (encoding == null)
			throw new NullPointerException("the encoding parameter is null");
		AcceptEncoding acceptEncoding = new AcceptEncoding();
		acceptEncoding.setEncoding(encoding);
		return acceptEncoding;
	}

	/**
	  * Creates a new AcceptHeader based on the newly supplied contentType and 
	  * contentSubType values.
	  *
	  * @param contentType The new string content type value.
	  * @param contentSubType The new string content sub-type value.
	  * @throws ParseException which signals that an error has been reached
	  * unexpectedly while parsing the content type or content subtype value.
	  * @return the newly created AcceptHeader object.
	  */
	public AcceptHeader createAcceptHeader(
		String contentType,
		String contentSubType)
		throws ParseException {
		if (contentType == null || contentSubType == null)
			throw new NullPointerException("contentType or subtype is null ");
		Accept accept = new Accept();
		accept.setContentType(contentType);
		accept.setContentSubType(contentSubType);

		return accept;
	}

	/**
	 * Creates a new AcceptLanguageHeader based on the newly supplied 
	 * language value.
	 *
	 * @param language - the new Locale value of the language
	 * @return the newly created AcceptLanguageHeader object.
	 */
	public AcceptLanguageHeader createAcceptLanguageHeader(Locale language) {
		if (language == null)
			throw new NullPointerException("null arg");
		AcceptLanguage acceptLanguage = new AcceptLanguage();
		acceptLanguage.setAcceptLanguage(language);

		return acceptLanguage;
	}

	/**
	 * Creates a new AlertInfoHeader based on the newly supplied alertInfo value.
	 *
	 * @param alertInfo - the new string value of the alertInfo
	 * @return the newly created AlertInfoHeader object.
	 * @since v1.1
	 */
	public AlertInfoHeader createAlertInfoHeader(URI alertInfo) {
		if (alertInfo == null)
			throw new NullPointerException("null arg alertInfo");
		AlertInfo a = new AlertInfo();
		a.setAlertInfo(alertInfo);
		return a;
	}

	/**
	 * Creates a new AllowEventsHeader based on the newly supplied event type
	 * value.
	 *
	 * @param eventType - the new string containing the eventType value.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the eventType value.
	 * @return the newly created AllowEventsHeader object.
	 * @since v1.1
	 */
	public AllowEventsHeader createAllowEventsHeader(String eventType)
		throws ParseException {
		if (eventType == null)
			throw new NullPointerException("null arg eventType");
		AllowEvents allowEvents = new AllowEvents();
		allowEvents.setEventType(eventType);
		return allowEvents;
	}

	/**
	  * Creates a new AllowHeader based on the newly supplied method value.
	  *
	  * @param method - the new string containing the method value.
	  * @throws ParseException which signals that an error has been reached
	  * unexpectedly while parsing the method value.
	  * @return the newly created AllowHeader object.
	  */
	public AllowHeader createAllowHeader(String method) throws ParseException {
		if (method == null)
			throw new NullPointerException("null arg method");
		Allow allow = new Allow();
		allow.setMethod(method);

		return allow;
	}

	/**
	 * Creates a new AuthenticationInfoHeader based on the newly supplied 
	 * response value.
	 *
	 * @param response - the new string value of the response.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the response value.
	 * @return the newly created AuthenticationInfoHeader object.
	 * @since v1.1
	 */
	public AuthenticationInfoHeader createAuthenticationInfoHeader(String response)
		throws ParseException {
		if (response == null)
			throw new NullPointerException("null arg response");
		AuthenticationInfo auth = new AuthenticationInfo();
		auth.setResponse(response);

		return auth;
	}

	/**
	 * Creates a new AuthorizationHeader based on the newly supplied 
	 * scheme value.
	 *
	 * @param scheme - the new string value of the scheme.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the scheme value.
	 * @return the newly created AuthorizationHeader object.
	 */
	public AuthorizationHeader createAuthorizationHeader(String scheme)
		throws ParseException {
		if (scheme == null)
			throw new NullPointerException("null arg scheme ");
		Authorization auth = new Authorization();
		auth.setScheme(scheme);

		return auth;
	}

	/**
	 * Creates a new CSeqHeader based on the newly supplied sequence number and 
	 * method values.
	 *
	 * @param sequenceNumber - the new integer value of the sequence number.
	 * @param method - the new string value of the method.
	 * @throws InvalidArgumentException if supplied sequence number is less 
	 * than zero.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the method value.
	 * @return the newly created CSeqHeader object.
	 */
	public CSeqHeader createCSeqHeader(int sequenceNumber, String method)
		throws ParseException, InvalidArgumentException {
		if (sequenceNumber < 0)
			throw new InvalidArgumentException("bad arg " + sequenceNumber);
		if (method == null)
			throw new NullPointerException("null arg method");
		CSeq cseq = new CSeq();
		cseq.setMethod(method);
		cseq.setSequenceNumber(sequenceNumber);

		return cseq;
	}

	/**
	 * Creates a new CallIdHeader based on the newly supplied callId value. 
	 * 
	 * @param callId - the new string value of the call-id.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the callId value.
	 * @return the newly created CallIdHeader object.
	 */
	public CallIdHeader createCallIdHeader(String callId)
		throws ParseException {
		if (callId == null)
			throw new NullPointerException("null arg callId");
		CallID c = new CallID();
		c.setCallId(callId);
		return c;
	}

	/**
	 * Creates a new CallInfoHeader based on the newly supplied callInfo value.
	 *
	 * @param callInfo The new string value of the callInfo.
	 * @return the newly created CallInfoHeader object.
	 */
	public CallInfoHeader createCallInfoHeader(URI callInfo) {
		if (callInfo == null)
			throw new NullPointerException("null arg callInfo");

		CallInfo c = new CallInfo();
		c.setInfo(callInfo);
		return c;
	}

	/**
	 * Creates a new ContactHeader based on the newly supplied address value.
	 *
	 * @param address - the new Address value of the address.
	 * @return the newly created ContactHeader object.
	 */
	public ContactHeader createContactHeader(Address address) {
		if (address == null)
			throw new NullPointerException("null arg address");
		Contact contact = new Contact();
		contact.setAddress(address);

		return contact;
	}

	/**
	* Creates a new wildcard ContactHeader. This is used in Register requests
	* to indicate to the server that it should remove all locations the
	* at which the user is currently available. This implies that the 
	* following conditions are met:
	* <ul>
	* <li><code>ContactHeader.getAddress.getAddress.getUserInfo() == *;</code>
	* <li><code>ContactHeader.getAddress.getAddress.isWildCard() == true;</code>
	* <li><code>ContactHeader.getExpires() == 0;</code>
	* </ul>
	*
	* @return the newly created wildcard ContactHeader.
	*/
	public ContactHeader createContactHeader() {
		Contact contact = new Contact();
		contact.setWildCardFlag(true);
		contact.setExpires(0);

		return contact;
	}

	/**
	 * Creates a new ContentDispositionHeader based on the newly supplied 
	 * contentDisposition value.
	 *
	 * @param contentDisposition - the new string value of the contentDisposition.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the contentDisposition value.
	 * @return the newly created ContentDispositionHeader object.
	 * @since v1.1
	 */
	public ContentDispositionHeader createContentDispositionHeader(String contentDisposition)
		throws ParseException {
		if (contentDisposition == null)
			throw new NullPointerException("null arg contentDisposition");
		ContentDisposition c = new ContentDisposition();
		c.setDispositionType(contentDisposition);

		return c;
	}

	/**
	* Creates a new ContentEncodingHeader based on the newly supplied encoding 
	* value.
	*
	* @param encoding - the new string containing the encoding value.
	* @throws ParseException which signals that an error has been reached
	* unexpectedly while parsing the encoding value.
	* @return the newly created ContentEncodingHeader object.
	*/
	public ContentEncodingHeader createContentEncodingHeader(String encoding)
		throws ParseException {
		if (encoding == null)
			throw new NullPointerException("null encoding");
		ContentEncoding c = new ContentEncoding();
		c.setEncoding(encoding);

		return c;
	}

	/**
	 * Creates a new ContentLanguageHeader based on the newly supplied 
	 * contentLanguage value.
	 *
	 * @param contentLanguage - the new Locale value of the contentLanguage.
	 * @return the newly created ContentLanguageHeader object.
	 * @since v1.1
	 */
	public ContentLanguageHeader createContentLanguageHeader(Locale contentLanguage) {
		if (contentLanguage == null)
			throw new NullPointerException("null arg contentLanguage");
		ContentLanguage c = new ContentLanguage();
		c.setContentLanguage(contentLanguage);

		return c;
	}

	/**
	 * Creates a new CSeqHeader based on the newly supplied contentLength value.
	 *
	 * @param contentLength - the new integer value of the contentLength.
	 * @throws InvalidArgumentException if supplied contentLength is less 
	 * than zero.
	 * @return the newly created ContentLengthHeader object.
	 */
	public ContentLengthHeader createContentLengthHeader(int contentLength)
		throws InvalidArgumentException {
		if (contentLength < 0)
			throw new InvalidArgumentException("bad contentLength");
		ContentLength c = new ContentLength();
		c.setContentLength(contentLength);

		return c;
	}

	/**
	 * Creates a new ContentTypeHeader based on the newly supplied contentType and 
	 * contentSubType values.
	 *
	 * @param contentType - the new string content type value.
	 * @param contentSubType - the new string content sub-type value.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the content type or content subtype value.
	 * @return the newly created ContentTypeHeader object.
	 */
	public ContentTypeHeader createContentTypeHeader(
		String contentType,
		String contentSubType)
		throws ParseException {
		if (contentType == null || contentSubType == null)
			throw new NullPointerException("null contentType or subType");
		ContentType c = new ContentType();
		c.setContentType(contentType);
		c.setContentSubType(contentSubType);
		return c;
	}

	/**
	* Creates a new DateHeader based on the newly supplied date value.
	*
	* @param date - the new Calender value of the date.
	* @return the newly created DateHeader object. 
	*/
	public DateHeader createDateHeader(Calendar date) {
		SIPDateHeader d = new SIPDateHeader();
		if (date == null)
			throw new NullPointerException("null date");
		d.setDate(date);

		return d;
	}

	/**
	 * Creates a new EventHeader based on the newly supplied eventType value.
	 *
	 * @param eventType - the new string value of the eventType.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the eventType value.
	 * @return the newly created EventHeader object.
	 * @since v1.1
	 */
	public EventHeader createEventHeader(String eventType)
		throws ParseException {
		if (eventType == null)
			throw new NullPointerException("null eventType");
		Event event = new Event();
		event.setEventType(eventType);

		return event;
	}

	/**
	 * Creates a new ExpiresHeader based on the newly supplied expires value.
	 *
	 * @param expires - the new integer value of the expires.
	 * @throws InvalidArgumentException if supplied expires is less 
	 * than zero.
	 * @return the newly created ExpiresHeader object.
	 */
	public ExpiresHeader createExpiresHeader(int expires)
		throws InvalidArgumentException {
		if (expires < 0)
			throw new InvalidArgumentException("bad value " + expires);
		Expires e = new Expires();
		e.setExpires(expires);

		return e;
	}

	/**
	 * Creates a new ExtensionHeader based on the newly supplied name and 
	 * value values.
	 *
	 * @param name - the new string name of the ExtensionHeader value.
	 * @param value - the new string value of the ExtensionHeader.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the name or value values.
	 * @return the newly created ExtensionHeader object.
	 */
	public javax.sip.header.ExtensionHeader createExtensionHeader(
		String name,
		String value)
		throws ParseException {
		if (name == null)
			throw new NullPointerException("bad name");

		gov.nist.javax.sip.header.ExtensionHeaderImpl ext =
			new gov.nist.javax.sip.header.ExtensionHeaderImpl();
		ext.setName(name);
		ext.setValue(value);

		return ext;
	}

	/**
	 * Creates a new FromHeader based on the newly supplied address and 
	 * tag values.
	 *
	 * @param address - the new Address object of the address.
	 * @param tag - the new string value of the tag.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the tag value.
	 * @return the newly created FromHeader object.  
	 */
	public FromHeader createFromHeader(Address address, String tag)
		throws ParseException {
		if (address == null)
			throw new NullPointerException("null address arg");
		From from = new From();
		from.setAddress(address);
		if (tag != null)
			from.setTag(tag);

		return from;
	}

	/**
	 * Creates a new InReplyToHeader based on the newly supplied callId 
	 * value.
	 *
	 * @param callId - the new string containing the callId value.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the callId value.
	 * @return the newly created InReplyToHeader object.
	 * @since v1.1
	 */
	public InReplyToHeader createInReplyToHeader(String callId)
		throws ParseException {
		if (callId == null)
			throw new NullPointerException("null callId arg");
		InReplyTo inReplyTo = new InReplyTo();
		inReplyTo.setCallId(callId);

		return inReplyTo;
	}
	/**
	* Creates a new MaxForwardsHeader based on the newly 
	* supplied maxForwards value.
	*
	* @param maxForwards The new integer value of the maxForwards.
	* @throws InvalidArgumentException if supplied maxForwards is less 
	* than zero or greater than 255.
	* @return the newly created MaxForwardsHeader object.
	*/
	public MaxForwardsHeader createMaxForwardsHeader(int maxForwards)
		throws InvalidArgumentException {
		if (maxForwards < 0 || maxForwards > 255)
			throw new InvalidArgumentException(
				"bad maxForwards arg " + maxForwards);
		MaxForwards m = new MaxForwards();
		m.setMaxForwards(maxForwards);

		return m;
	}

	/**
	 * Creates a new MimeVersionHeader based on the newly 
	 * supplied mimeVersion value.
	 *
	 * @param majorVersion - the new integer value of the majorVersion.
	 * @param minorVersion - the new integer value of the minorVersion.
	 * @throws InvalidArgumentException if supplied mimeVersion is less 
	 * than zero.
	 * @return the newly created MimeVersionHeader object.
	 * @since v1.1
	 */
	public MimeVersionHeader createMimeVersionHeader(
		int majorVersion,
		int minorVersion)
		throws InvalidArgumentException {
		if (majorVersion < 0 || minorVersion < 0)
			throw new javax.sip.InvalidArgumentException(
				"bad major/minor version");
		MimeVersion m = new MimeVersion();
		m.setMajorVersion(majorVersion);
		m.setMinorVersion(minorVersion);

		return m;
	}

	/**
	 * Creates a new MinExpiresHeader based on the newly supplied minExpires value.
	 *
	 * @param minExpires - the new integer value of the minExpires.
	 * @throws InvalidArgumentException if supplied minExpires is less 
	 * than zero.
	 * @return the newly created MinExpiresHeader object.
	 * @since v1.1
	 */
	public MinExpiresHeader createMinExpiresHeader(int minExpires)
		throws InvalidArgumentException {
		if (minExpires < 0)
			throw new InvalidArgumentException("bad minExpires " + minExpires);
		MinExpires min = new MinExpires();
		min.setExpires(minExpires);

		return min;
	}

	/**
	 * Creates a new OrganizationHeader based on the newly supplied 
	 * organization value.
	 *
	 * @param organization - the new string value of the organization.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the organization value.
	 * @return the newly created OrganizationHeader object.
	 */
	public OrganizationHeader createOrganizationHeader(String organization)
		throws ParseException {
		if (organization == null)
			throw new NullPointerException("bad organization arg");
		Organization o = new Organization();
		o.setOrganization(organization);

		return o;
	}

	/**
	 * Creates a new PriorityHeader based on the newly supplied priority value.
	 *
	 * @param priority - the new string value of the priority.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the priority value.
	 * @return the newly created PriorityHeader object.
	 */
	public PriorityHeader createPriorityHeader(String priority)
		throws ParseException {
		if (priority == null)
			throw new NullPointerException("bad priority arg");
		Priority p = new Priority();
		p.setPriority(priority);

		return p;
	}

	/**
	 * Creates a new ProxyAuthenticateHeader based on the newly supplied 
	 * scheme value.
	 *
	 * @param scheme - the new string value of the scheme.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the scheme value.
	 * @return the newly created ProxyAuthenticateHeader object.
	 */
	public ProxyAuthenticateHeader createProxyAuthenticateHeader(String scheme)
		throws ParseException {
		if (scheme == null)
			throw new NullPointerException("bad scheme arg");
		ProxyAuthenticate p = new ProxyAuthenticate();
		p.setScheme(scheme);

		return p;
	}

	/**
	 * Creates a new ProxyAuthorizationHeader based on the newly supplied 
	 * scheme value.
	 *
	 * @param scheme - the new string value of the scheme.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the scheme value.
	 * @return the newly created ProxyAuthorizationHeader object.
	 */
	public ProxyAuthorizationHeader createProxyAuthorizationHeader(String scheme)
		throws ParseException {
		if (scheme == null)
			throw new NullPointerException("bad scheme arg");
		ProxyAuthorization p = new ProxyAuthorization();
		p.setScheme(scheme);

		return p;
	}

	/**
	 * Creates a new ProxyRequireHeader based on the newly supplied optionTag 
	 * value.
	 *
	 * @param optionTag - the new string OptionTag value.
	 * @return the newly created ProxyRequireHeader object.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the optionTag value.
	 */
	public ProxyRequireHeader createProxyRequireHeader(String optionTag)
		throws ParseException {
		if (optionTag == null)
			throw new NullPointerException("bad optionTag arg");
		ProxyRequire p = new ProxyRequire();
		p.setOptionTag(optionTag);

		return p;
	}

	/**
	 * Creates a new RAckHeader based on the newly supplied rSeqNumber, 
	 * cSeqNumber and method values.
	 *
	 * @param rSeqNumber - the new integer value of the rSeqNumber.
	 * @param cSeqNumber - the new integer value of the cSeqNumber.
	 * @param method - the new string value of the method.
	 * @throws InvalidArgumentException if supplied rSeqNumber or cSeqNumber is 
	 * less than zero or greater than than 2**31-1.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the method value.
	 * @return the newly created RAckHeader object.
	 * @since v1.1
	 */
	public RAckHeader createRAckHeader(
		int rSeqNumber,
		int cSeqNumber,
		String method)
		throws InvalidArgumentException, ParseException {
		if (method == null)
			throw new NullPointerException("Bad method");
		if (cSeqNumber < 0 || rSeqNumber < 0)
			throw new InvalidArgumentException("bad cseq/rseq arg");
		RAck rack = new RAck();
		rack.setMethod(method);
		rack.setCSeqNumber(cSeqNumber);
		rack.setRSeqNumber(rSeqNumber);

		return rack;
	}

	/**
	 * Creates a new RSeqHeader based on the newly supplied sequenceNumber value.
	 *
	 * @param sequenceNumber - the new integer value of the sequenceNumber.
	 * @throws InvalidArgumentException if supplied sequenceNumber is 
	 * less than zero or greater than than 2**31-1.
	 * @return the newly created RSeqHeader object.
	 * @since v1.1
	 */
	public RSeqHeader createRSeqHeader(int sequenceNumber)
		throws InvalidArgumentException {
		if (sequenceNumber < 0)
			throw new InvalidArgumentException(
				"invalid sequenceNumber arg " + sequenceNumber);
		RSeq rseq = new RSeq();
		rseq.setSequenceNumber(sequenceNumber);

		return rseq;
	}

	/**
	 * Creates a new ReasonHeader based on the newly supplied reason value.
	 *
	 * @param protocol - the new string value of the protocol.
	 * @param cause - the new integer value of the cause.
	 * @param text - the new string value of the text.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the protocol, cause or text value.
	 * @return the newly created ReasonHeader object.
	 * @since v1.1
	 */
	public ReasonHeader createReasonHeader(
		String protocol,
		int cause,
		String text)
		throws InvalidArgumentException, ParseException {
		if (protocol == null)
			throw new NullPointerException("bad protocol arg");
		if (cause < 0)
			throw new InvalidArgumentException("bad cause");
		Reason reason = new Reason();
		reason.setProtocol(protocol);
		reason.setCause(cause);
		reason.setText(text);

		return reason;
	}

	/**
	* Creates a new RecordRouteHeader based on the newly supplied address value.
	*
	* @param address - the new Address object of the address.
	* @return the newly created RecordRouteHeader object.  
	*/
	public RecordRouteHeader createRecordRouteHeader(Address address) {
		RecordRoute recordRoute = new RecordRoute();
		recordRoute.setAddress(address);

		return recordRoute;
	}

	/**
	* Creates a new ReplyToHeader based on the newly supplied address value.
	*
	* @param address - the new Address object of the address.
	* @return the newly created ReplyToHeader object.  
	* @since v1.1
	*/
	public ReplyToHeader createReplyToHeader(Address address) {
		if (address == null)
			throw new NullPointerException("null address");
		ReplyTo replyTo = new ReplyTo();
		replyTo.setAddress(address);

		return replyTo;
	}

	/**
	 * Creates a new RequireHeader based on the newly supplied optionTag 
	 * value.
	 *
	 * @param optionTag - the new string value containing the optionTag value.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the List of optionTag value.
	 * @return the newly created RequireHeader object.
	 */
	public RequireHeader createRequireHeader(String optionTag)
		throws ParseException {
		if (optionTag == null)
			throw new NullPointerException("null optionTag");
		Require require = new Require();
		require.setOptionTag(optionTag);

		return require;
	}

	/**
	 * Creates a new RetryAfterHeader based on the newly supplied retryAfter 
	 * value.
	 *
	 * @param retryAfter - the new integer value of the retryAfter.
	 * @throws InvalidArgumentException if supplied retryAfter is less 
	 * than zero.
	 * @return the newly created RetryAfterHeader object.
	 */
	public RetryAfterHeader createRetryAfterHeader(int retryAfter)
		throws InvalidArgumentException {
		if (retryAfter < 0)
			throw new InvalidArgumentException("bad retryAfter arg");
		RetryAfter r = new RetryAfter();
		r.setRetryAfter(retryAfter);

		return r;
	}

	/**
	 * Creates a new RouteHeader based on the newly supplied address value.
	 *
	 * @param address - the new Address object of the address.
	 * @return the newly created RouteHeader object.  
	 */
	public RouteHeader createRouteHeader(Address address) {
		if (address == null)
			throw new NullPointerException("null address arg");
		Route route = new Route();
		route.setAddress(address);

		return route;
	}

	/**
	 * Creates a new ServerHeader based on the newly supplied product value.
	 *
	 * @param product - the new list value of the product.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the product value.
	 * @return the newly created ServerHeader object.
	 */
	public ServerHeader createServerHeader(List product)
		throws ParseException {
		if (product == null)
			throw new NullPointerException("null productList arg");
		Server server = new Server();
		server.setProduct(product);

		return server;
	}

	/**
	  * Creates a new SubjectHeader based on the newly supplied subject value.
	  *
	  * @param subject - the new string value of the subject.
	  * @throws ParseException which signals that an error has been reached
	  * unexpectedly while parsing the subject value.
	  * @return the newly created SubjectHeader object.
	  */
	public SubjectHeader createSubjectHeader(String subject)
		throws ParseException {
		if (subject == null)
			throw new NullPointerException("null subject arg");
		Subject s = new Subject();
		s.setSubject(subject);

		return s;
	}

	/**
	 * Creates a new SubscriptionStateHeader based on the newly supplied 
	 * subscriptionState value.
	 *
	 * @param subscriptionState - the new string value of the subscriptionState.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the subscriptionState value.
	 * @return the newly created SubscriptionStateHeader object.
	 * @since v1.1
	 */
	public SubscriptionStateHeader createSubscriptionStateHeader(String subscriptionState)
		throws ParseException {
		if (subscriptionState == null)
			throw new NullPointerException("null subscriptionState arg");
		SubscriptionState s = new SubscriptionState();
		s.setState(subscriptionState);

		return s;
	}

	/**
	 * Creates a new SupportedHeader based on the newly supplied optionTag 
	 * value.
	 *
	 * @param optionTag - the new string containing the optionTag value.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the optionTag value.
	 * @return the newly created SupportedHeader object.
	 */
	public SupportedHeader createSupportedHeader(String optionTag)
		throws ParseException {
		if (optionTag == null)
			throw new NullPointerException("null optionTag arg");
		Supported supported = new Supported();
		supported.setOptionTag(optionTag);

		return supported;
	}

	/**
	 * Creates a new TimeStampHeader based on the newly supplied timeStamp value.
	 *
	 * @param timeStamp - the new float value of the timeStamp.
	 * @throws InvalidArgumentException if supplied timeStamp is less 
	 * than zero.
	 * @return the newly created TimeStampHeader object.
	 */
	public TimeStampHeader createTimeStampHeader(float timeStamp)
		throws InvalidArgumentException {
		if (timeStamp < 0)
			throw new IllegalArgumentException("illegal timeStamp");
		TimeStamp t = new TimeStamp();
		t.setTimeStamp(timeStamp);

		return t;
	}

	/**
	 * Creates a new ToHeader based on the newly supplied address and 
	 * tag values.
	 *
	 * @param address - the new Address object of the address.
	 * @param tag - the new string value of the tag.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the tag value.
	 * @return the newly created ToHeader object.  
	 */
	public ToHeader createToHeader(Address address, String tag)
		throws ParseException {
		if (address == null)
			throw new NullPointerException("null address");
		To to = new To();
		to.setAddress(address);
		if (tag != null)
			to.setTag(tag);

		return to;
	}

	/**
	 * Creates a new UnsupportedHeader based on the newly supplied optionTag 
	 * value.
	 *
	 * @param optionTag - the new string containing the optionTag value.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the List of optionTag value.
	 * @return the newly created UnsupportedHeader object.
	 */
	public UnsupportedHeader createUnsupportedHeader(String optionTag)
		throws ParseException {
		if (optionTag == null)
			throw new NullPointerException(optionTag);
		Unsupported unsupported = new Unsupported();
		unsupported.setOptionTag(optionTag);

		return unsupported;
	}

	/**
	 * Creates a new UserAgentHeader based on the newly supplied product value.
	 *
	 * @param product - the new list value of the product.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the product value.
	 * @return the newly created UserAgentHeader object.
	 */
	public UserAgentHeader createUserAgentHeader(List product)
		throws ParseException {

		if (product == null)
			throw new NullPointerException("null user agent");
		UserAgent userAgent = new UserAgent();
		userAgent.setProduct(product);

		return userAgent;
	}

	/**
	 * Creates a new ViaHeader based on the newly supplied uri and branch values.
	 *
	 * @param host the new host value of uri.
	 * @param port the new port value of uri.
	 * @param transport the new transport value of uri.
	 * @param branch the new string value of the branch.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the branch value.
	 * @return the newly created ViaHeader object.
	 */
	public ViaHeader createViaHeader(
		String host,
		int port,
		String transport,
		String branch)
		throws ParseException {
		// This should be changed.
		if (host == null || transport == null)
			throw new NullPointerException("null arg");
		Via via = new Via();
		if (branch != null)
			via.setBranch(branch);

		// Added by Daniel Martinez <dani@dif.um.es>
		// for supporting IPv6 addresses
		if(host.indexOf(':') >= 0 &&
		   host.indexOf('[') < 0)
			host = '[' + host + ']';

		via.setHost(host);
		via.setPort(port);
		via.setTransport(transport);

		return via;
	}

	/**
	 * Creates a new WWWAuthenticateHeader based on the newly supplied 
	 * scheme value.
	 *
	 * @param scheme - the new string value of the scheme.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the scheme values.
	 * @return the newly created WWWAuthenticateHeader object.
	 */
	public WWWAuthenticateHeader createWWWAuthenticateHeader(String scheme)
		throws ParseException {
		if (scheme == null)
			throw new NullPointerException("null scheme");
		WWWAuthenticate www = new WWWAuthenticate();
		www.setScheme(scheme);

		return www;
	}

	/**
	 * Creates a new WarningHeader based on the newly supplied 
	 * agent, code and comment values.
	 *
	 * @param agent - the new string value of the agent.
	 * @param code - the new boolean integer of the code.
	 * @param comment - the new string value of the comment.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the agent or comment values.
	 * @throws InvalidArgumentException if an invalid integer code is given for
	 * the WarningHeader.
	 * @return the newly created WarningHeader object.
	 */
	public WarningHeader createWarningHeader(
		String agent,
		int code,
		String comment)
		throws ParseException, InvalidArgumentException {
		if (agent == null)
			throw new NullPointerException("null arg");
		Warning warning = new Warning();
		warning.setAgent(agent);
		warning.setCode(code);
		warning.setText(comment);

		return warning;
	}

	/** Creates a new ErrorInfoHeader based on the newly 
	 * supplied errorInfo value.
	 *
	 * @param errorInfo - the new URI value of the errorInfo.
	 * @return the newly created ErrorInfoHeader object.
	 * @since v1.1
	 */
	public ErrorInfoHeader createErrorInfoHeader(URI errorInfo) {
		if (errorInfo == null)
			throw new NullPointerException("null arg");
		return new ErrorInfo((GenericURI) errorInfo);
	}

	/** Create and parse a header.
	 *
	 * @param headerName -- header name for the header to parse.
	 * @param headerValue -- header value for the header to parse.
	 * @throws ParseException
	 * @return  the parsed sip header
	 */
	public javax.sip.header.Header createHeader(
		String headerName,
		String headerValue)
		throws java.text.ParseException {
		if (headerName == null)
			throw new NullPointerException("header name is null");
		String hdrText =
			new StringBuffer()
				.append(headerName)
				.append(":")
				.append(headerValue)
				.toString();
		StringMsgParser smp = new StringMsgParser();
		SIPHeader sipHeader = smp.parseSIPHeader(hdrText);
		if (sipHeader instanceof SIPHeaderList) {
			if (((SIPHeaderList) sipHeader).size() > 1) {
				throw new ParseException(
					"Only singleton allowed " + hdrText,
					0);
			} else if (((SIPHeaderList) sipHeader).size() == 0) {
				try {
					return (Header) ((SIPHeaderList) sipHeader)
						.getMyClass()
						.newInstance();
				} catch (InstantiationException ex) {
					ex.printStackTrace();
					return null;
				} catch (IllegalAccessException ex) {
					ex.printStackTrace();
					return null;
				}
			} else {
				return (Header) ((SIPHeaderList) sipHeader).first();
			}
		} else {
			return (Header) sipHeader;
		}
	}

	/** Create and return a list of headers.
	 *@param headers -- list of headers.
	 *@throws ParseException -- if a parse exception occurs or a List
	 * of that type of header is not alowed.
	 *@return a List containing the headers.
	 */
	public java.util.List createHeaders(String headers)
		throws java.text.ParseException {
		if (headers == null)
			throw new NullPointerException("null arg!");
		StringMsgParser smp = new StringMsgParser();
		SIPHeader shdr = smp.parseSIPHeader(headers);
		if (shdr instanceof SIPHeaderList)
			return (SIPHeaderList) shdr;
		else
			throw new ParseException(
				"List of headers of this type is not allowed in a message",
				0);
	}

	/** Create a ReferTo Header.
	 *@param address -- address for the header.
	 */
	public ReferToHeader createReferToHeader(Address address) {
		if (address == null)
			throw new NullPointerException("null address!");
		ReferTo referTo = new ReferTo();
		referTo.setAddress(address);
		return referTo;
	}

	/**
	 * Default constructor.
	 */
	public HeaderFactoryImpl() {

	}

}
/*
 * $Log: HeaderFactoryImpl.java,v $
 * Revision 1.4  2005/05/24 08:50:49  djmartinez
 *
 * Added code for supporting correctly IPv6 addresses in Via headers.
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
