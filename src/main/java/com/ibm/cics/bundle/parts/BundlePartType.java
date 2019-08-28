package com.ibm.cics.bundle.parts;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * Adding enumerations here includes them as things we will automatically generate a define for in cics.xml
 * not supported - atom config (xml file)
 * not supported - JVMSERVER (jvmprofile has to be in EBCDIC...?)
 * not supported - jsonapp
 * not supported - web service (didn't yet test but wsbind is binary and WSDL should be transferred as-is)
 */
public enum BundlePartType {
	
	EARBUNDLE("earbundle"),
	EPADAPTER("epadapter"),
	EPADAPTERSET("epadapterset"),
	EVENTBINDING("evbind"),
	FILE("file"),
	LIBRARY("library"),
	POLICY("policy"),
//	PIPELINE("pipeline"), //Not currently supported, but in the dependency graph
	PROGRAM("program"),
	TCPIPSERVICE("tcpipservice"),
	TRANSACTION("transaction"),
	URIMAP("urimap"),
	OSGIBUNDLE("osgibundle"),
	PACKAGESET("packageset"),
	WARBUNDLE("warbundle");
	
	public static final String NS = "http://www.ibm.com/xmlns/prod/cics/bundle";
	
	private static Map<String, BundlePartType> extensionToType;

	static {
		extensionToType = Arrays
			.stream(
				BundlePartType.class.getEnumConstants()
			)
			.collect(
				Collectors.toMap(
					BundlePartType::getBundlePartExtension,
					Function.identity()
				)
			);
	}
	
	public static BundlePartType getType(String extension) {
		return extensionToType.get(extension);
	}

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
