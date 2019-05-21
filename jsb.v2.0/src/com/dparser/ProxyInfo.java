package com.dparser;

public class ProxyInfo {
	String proxyHost;
	String proxyState;

	public ProxyInfo(String pHost, String pState) {
		this.proxyHost = pHost;
		this.proxyState = pState;
	}

	public String getProxyHost() {
		return this.proxyHost;
	}

	public String getProxyState() {
		return this.proxyState;
	}
}
