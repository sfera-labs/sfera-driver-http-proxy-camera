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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;

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

	private String url;
	private CameraProxyServletHolder proxy;
	private int errors;

	public HttpProxyCamera(String id) {
		super(id);
	}

	@Override
	protected boolean onInit(Configuration config) throws InterruptedException {
		try {
			url = config.get("url", null);
			if (url == null) {
				throw new Exception("No URL specified");
			}
			String user = config.get("user", null);
			String password = config.get("password", null);
			String camPath = "/camera/" + getId();
			String realm = getRealm();
			proxy = new CameraProxyServletHolder(realm, user, password, url, camPath);
			WebServer.addServlet(proxy, camPath + "/*");
			errors = 0;
			return true;

		} catch (Exception e) {
			log.error("Error initializing camera: " + e, e);
			return false;
		}
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	private String getRealm() throws Exception {
		HttpClient client = new HttpClient();
		try {
			client.start();
			InputStreamResponseListener listener = new InputStreamResponseListener();
			client.newRequest(url).send(listener);
			Response response = listener.get(10, TimeUnit.SECONDS);
			for (HttpField h : response.getHeaders()) {
				HttpHeader header = h.getHeader();
				if (header != null && header.is("WWW-Authenticate")) {
					for (String val : h.getValues()) {
						String[] r = val.split("=");
						if (r.length == 2 && r[0].trim().equalsIgnoreCase("Basic realm")) {
							String realm = r[1].trim();
							realm = realm.substring(1, realm.length() - 1);
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
		Thread.sleep(30000);

		try {
			if (!checkConnection()) {
				if (++errors > 4) {
					log.warn("Many connection errors");
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			log.error("Loop error", e);
			return false;
		}
	}

	/**
	 * 
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws Exception
	 */
	private boolean checkConnection() throws Exception {
		HttpClient client = new HttpClient();
		try {
			client.start();
			InputStreamResponseListener listener = new InputStreamResponseListener();
			client.newRequest(url).send(listener);
			listener.get(5, TimeUnit.SECONDS);
			return true;

		} catch (TimeoutException | ExecutionException e) {
			log.debug("Connection error");
			return false;
		} finally {
			client.stop();
		}
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
