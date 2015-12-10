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
