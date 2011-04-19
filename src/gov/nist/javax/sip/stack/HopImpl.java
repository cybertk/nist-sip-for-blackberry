/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
 *******************************************************************************/
package gov.nist.javax.sip.stack;

import java.util.StringTokenizer;

/** 
 * Routing algorithms return a list of hops to which the request is
 * routed.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2004/10/28 19:02:51 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * IPv6 Support added by Emil Ivov (emil_ivov@yahoo.com)<br/>
 * Network Research Team (http://www-r2.u-strasbg.fr))<br/>
 * Louis Pasteur University - Strasbourg - France<br/>
 * Bug fix for correct handling of IPV6 Address added by
 * Daniel J. Martinez Manzano <dani@dif.um.es>
 *
 */
public class HopImpl extends Object implements javax.sip.address.Hop {
	protected String host;
	protected int port;
	protected String transport;
	protected boolean explicitRoute; // this is generated from a ROUTE header.
	protected boolean defaultRoute; // This is generated from the proxy addr
	protected boolean uriRoute; // This is extracted from the requestURI.

	/**
	 * Debugging println.
	 */
	public String toString() {
		return host + ":" + port + "/" + transport;
	}

	/**
	 * Create new hop given host, port and transport.
	 * @param hostName hostname
	 * @param portNumber port
	 * @param trans transport
	 */
	public HopImpl(String hostName, int portNumber, String trans) {
		host = hostName;

		// Added by Daniel J. Martinez Manzano <dani@dif.um.es>
		// for correct management of IPv6 addresses.
		if(host.indexOf(":") >= 0)
			if(host.indexOf("[") < 0)
				host = "[" + host + "]";

		port = portNumber;
		transport = trans;
	}

	/**
	 * Creates new Hop
	 * @param hop is a hop string in the form of host:port/Transport
	 * @throws IllegalArgument exception if string is not properly formatted or null.
	 */
	public HopImpl(String hop) throws IllegalArgumentException {
		if (hop == null)
			throw new IllegalArgumentException("Null arg!");
		// System.out.println("hop = " + hop);
		StringTokenizer stringTokenizer = new StringTokenizer(hop + "/");
		String hostPort = stringTokenizer.nextToken("/").trim();
		transport = stringTokenizer.nextToken().trim();
		// System.out.println("Hop: transport = " + transport);
		if (transport == null)
			transport = "UDP";
		else if (transport == "")
			transport = "UDP";
		if (transport.compareToIgnoreCase("UDP") != 0
			&& transport.compareToIgnoreCase("TLS") != 0 // Added by Daniel J. Martinez Manzano <dani@dif.um.es>
			&& transport.compareToIgnoreCase("TCP") != 0) {
			System.out.println("Bad transport string " + transport);
			throw new IllegalArgumentException(hop);
		}

		String portString = null;
		//IPv6 hostport
		if (hostPort.charAt(0) == '[') {
			int rightSqBrackIndex = hostPort.indexOf(']');
			if (rightSqBrackIndex == -1)
				throw new IllegalArgumentException("Bad IPv6 reference spec");

			host = hostPort.substring(0, rightSqBrackIndex + 1);

			int portColon = hostPort.indexOf(':', rightSqBrackIndex);
			if (portColon != -1)
				try {
					portString = hostPort.substring(portColon + 1).trim();
				} catch (IndexOutOfBoundsException exc) {
					//Do nothing - handled later
				}
		}
		//IPv6 address and no port
		else if (hostPort.indexOf(':') != hostPort.lastIndexOf(":")) {
			host = '[' + hostPort + ']';
		} else //no square brackets and a single or zero colons => IPv4 hostPort
			{
			int portColon = hostPort.indexOf(':');
			if (portColon == -1)
				host = hostPort;
			else {
				host = hostPort.substring(0, portColon).trim();
				try {
					portString = hostPort.substring(portColon + 1).trim();
				} catch (IndexOutOfBoundsException exc) {
					//Do nothing - handled later
				}
			}
		}

		if (host == null || host.equals(""))
			throw new IllegalArgumentException("no host!");
		if (portString == null || portString.equals("")) {
			port = 5060;
		} else {
			try {
				port = Integer.parseInt(portString);
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("Bad port spec");
			}
		}
	}

	/**
	 * Retruns the host string.
	 * @return host String
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Returns the port.
	 * @return port integer.
	 */
	public int getPort() {
		return port;
	}

	/** returns the transport string.
	 */
	public String getTransport() {
		return transport;
	}

	/** Return true if this is an explicit route (ie. extrcted from a ROUTE
	 * Header)
	 */
	public boolean isExplicitRoute() {
		return explicitRoute;
	}

	/** Return true if this is a default route (ie. next hop proxy address)
	 */
	public boolean isDefaultRoute() {
		return defaultRoute;
	}

	/** Return true if this is uriRoute
	 */
	public boolean isURIRoute() {
		return uriRoute;
	}

	/** Set the URIRoute flag.
	 */
	public void setURIRouteFlag() {
		uriRoute = true;
	}

	/** Set the defaultRouteFlag.
	 */
	public void setDefaultRouteFlag() {
		defaultRoute = true;
	}

	/** Set the explicitRoute flag.
	 */
	public void setExplicitRouteFlag() {
		explicitRoute = true;
	}
}
/*
 * $Log: HopImpl.java,v $
 * Revision 1.5  2004/10/28 19:02:51  mranga
 * Submitted by:  Daniel Martinez
 * Reviewed by:   M. Ranganathan
 *
 * Added changes for TLS support contributed by Daniel Martinez
 *
 * Revision 1.4  2004/01/22 13:26:33  sverker
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
