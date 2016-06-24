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

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpField;

import cc.sferalabs.sfera.core.Configuration;
import cc.sferalabs.sfera.drivers.Driver;
import cc.sferalabs.sfera.web.WebServer;
import cc.sferalabs.sfera.web.WebServerException;

/**
 *
 * @author Giampiero Baggiani
 *
 * @version 1.0.0
 *
 */
public class HttpProxyCamera extends Driver {

	private CameraProxyServletHolder proxy;

	public HttpProxyCamera(String id) {
		super(id);
	}

	@Override
	protected boolean onInit(Configuration config) throws InterruptedException {
		try {
			String url = config.get("url", null);
			if (url == null) {
				throw new Exception("No URL specified");
			}
			String user = config.get("user", null);
			String password = config.get("password", null);
			String camPath = "/camera/" + getId();
			String realm = getRealm(url);
			proxy = new CameraProxyServletHolder(realm, user, password, url, camPath);
			WebServer.addServlet(proxy, camPath + "/*");
			return true;

		} catch (Exception e) {
			log.error("Error initiating camera: " + e, e);
			return false;
		}
	}

	/**
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	private String getRealm(String url) throws Exception {
		HttpClient client = new HttpClient();
		try {
			client.start();
			InputStreamResponseListener listener = new InputStreamResponseListener();
			client.newRequest(url).send(listener);
			Response response = listener.get(10, TimeUnit.SECONDS);
			for (HttpField h : response.getHeaders()) {
				if (h.getHeader().is("WWW-Authenticate")) {
					for (String val : h.getValues()) {
						String[] r = val.split("=");
						if (r.length == 2 && r[0].trim().equalsIgnoreCase("Basic realm")) {
							String realm = r[1].trim();
							realm = realm.substring(1, realm.length() - 1);
							System.err.println(realm);
							return realm;
						}
					}
				}
			}

			return null;

		} finally {
			client.stop();
		}
	}

	@Override
	protected boolean loop() throws InterruptedException {
		// TODO check connection to camera is still on...
		Thread.sleep(5000000);
		return true;
	}

	@Override
	protected void onQuit() {
		if (proxy != null) {
			try {
				WebServer.removeServlet(proxy);
			} catch (WebServerException e) {
				log.error("Error removing camera servlet", e);
			}
		}
	}

}
