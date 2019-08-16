package com.ibm.cics.bundle.parts;

public enum BundlePartType {
	
	EARBUNDLE("earbundle"),
	EPADAPTER("epadapter"),
	EPADAPTERSET("epadapterset"),
	EVENTBINDING("evbind"),
	FILE("file"),
	LIBRARY("library"),
	POLICY("policy"),
	PIPELINE("pipeline"), //Not currently supported, but in the dependency graph
	PROGRAM("program"),
	TCPIPSERVICE("tcpipservice"),
	TRANSACTION("transaction"),
	URIMAP("urimap"),
	OSGIBUNDLE("osgibundle"),
	PACKAGESET("packageset"),
	WARBUNDLE("warbundle");
	
	public static final String NS = "http://www.ibm.com/xmlns/prod/cics/bundle";
	
	private String bundlePartExtension;

	private BundlePartType(String bundlePartExtension) {
		this.bundlePartExtension = bundlePartExtension;
	}
	
	public String getURI() {
		return NS + "/" + name();
	}
	
	public String getBundlePartExtension() {
		return bundlePartExtension;
	}

}
