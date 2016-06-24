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

import org.eclipse.jetty.servlet.ServletHolder;

class CameraProxyServletHolder extends ServletHolder {

	/**
	 * 
	 * @param realm
	 * @param user
	 * @param password
	 * @param proxyTo
	 * @param prefix
	 */
	CameraProxyServletHolder(String realm, String user, String password, String proxyTo,
			String prefix) {
		super(new CameraProxyServlet(realm, user, password));
		setInitParameter("proxyTo", proxyTo);
		setInitParameter("prefix", prefix);
		setInitParameter("timeout", "0");
	}

}
