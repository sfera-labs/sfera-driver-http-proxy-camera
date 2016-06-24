/*-
 * +======================================================================+
 * HttpProxyCamera
 * ---
 * Copyright (C) 2016 Sfera Labs S.r.l.
 * ---
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * -======================================================================-
 */

package cc.sferalabs.sfera.drivers.cameras.http_proxy;

import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.proxy.ProxyServlet;

@SuppressWarnings("serial")
class CameraProxyServlet extends ProxyServlet.Transparent {

	private final String realm;
	private final String user;
	private final String password;

	/**
	 * 
	 * @param realm
	 * @param user
	 * @param password
	 */
	CameraProxyServlet(String realm, String user, String password) {
		if ((user != null && password == null) || (password != null && user == null)) {
			throw new IllegalArgumentException("Must specify both user and password or none");
		}
		this.realm = realm;
		this.user = user;
		this.password = password;
	}

	@Override
	protected HttpClient createHttpClient() throws ServletException {
		HttpClient client = super.createHttpClient();
		if (user != null && password != null) {
			client.getProtocolHandlers().put(new WWWAuthenticationProtocolHandler(client));
			String proxyTo = getServletConfig().getInitParameter("proxyTo");
			AuthenticationStore a = client.getAuthenticationStore();
			a.addAuthentication(
					new BasicAuthentication(URI.create(proxyTo), realm, user, password));
		}

		return client;
	}

	@Override
	// TODO method overwritten because of this bug:
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=473624
	protected String rewriteTarget(HttpServletRequest request) {
		String _prefix = getServletConfig().getInitParameter("prefix");
		String _proxyTo = getServletConfig().getInitParameter("proxyTo");

		String path = request.getRequestURI();
		if (!path.startsWith(_prefix))
			return null;

		StringBuilder uri = new StringBuilder(_proxyTo);
		if (_proxyTo.endsWith("/"))
			uri.setLength(uri.length() - 1);
		String rest = path.substring(_prefix.length());
		if (!rest.isEmpty() && !rest.startsWith("/"))
			uri.append("/");
		uri.append(rest);
		String query = request.getQueryString();
		if (query != null)
			uri.append("?").append(query);
		URI rewrittenURI = URI.create(uri.toString()).normalize();

		if (!validateDestination(rewrittenURI.getHost(), rewrittenURI.getPort()))
			return null;

		return rewrittenURI.toString();
	}

}
