package com.ibm.cics.bundle.parts;

/*-
 * #%L
 * CICS Bundle Common Parent
 * %%
 * Copyright (C) 2019 IBM Corp.
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.w3c.dom.Element;

public class OsgiBundlePart extends AbstractJavaBundlePart  {
	
	private String osgiVersion;

	public OsgiBundlePart(String symbolicName, String osgiVersion, String jvmServer, File osgiBundle) {
		super(
			symbolicName + "_" + osgiVersion,
			BundlePartType.OSGIBUNDLE,
			symbolicName,
			jvmServer,
			osgiBundle,
			"jar"
		);

		this.osgiVersion = osgiVersion;
	}
	
	@Override
	protected void addAdditionalNodes(Element rootElement) {
		rootElement.setAttribute("version", osgiVersion);
	}
	
	/**
	 * Takes a given Maven version and turns it into an OSGi-compatible version.
	 * 
	 * This simple algorithm purely replaces the first hyphen (if found) with a period.
	 * @param mavenVersion The Maven-style version string
	 * @return The converted, OSGi-style version
	 */
	public static String convertMavenVersionToOSGiVersion(String mavenVersion) {
		return mavenVersion.replaceFirst("-", ".");
	}

	/**
	 * Gets the Bundle-Version header inside the given artifact's manifest.
	 * @param osgiBundle The OSGi bundle file to find the Bundle-Version of 
	 * @throws IOException if an I/O error has occurred
	 * @return The version or null if the manifest, or the header in the manifest, is not present
	 */
	public static String getBundleVersion(File osgiBundle) throws IOException {
		Manifest manifest = getManifest(osgiBundle);
		return manifest != null ? getManifestHeader(manifest, "Bundle-Version") : null;
	}
	
	/**
	 * Attempts to retrieve the manifest of the given artifact. Will search inside the artifact, if it's a JAR,
	 * or will search inside a directory, if (as happens during incremental builds in the IDE), the artifact file
	 * is still pointing into the classes directory.
	 * 
	 * @param osgiBundle The OSGi bundle file to find the Bundle-Version of
	 * @throws IOException if there was a problem reading the manifest
	 * @return The manifest, or null if none was found
	 */
	private static Manifest getManifest(File osgiBundle) throws IOException {
		if (osgiBundle != null && osgiBundle.exists()) {
			if (osgiBundle.isFile()) {
				try (JarFile jarFile = new JarFile(osgiBundle)) {
					return jarFile.getManifest();
				}
			} else {
				File manifestFile = new File(osgiBundle, "META-INF/MANIFEST.MF");
				if (manifestFile.exists()) {
					try (InputStream is = new BufferedInputStream(new FileInputStream(manifestFile))) { 
						return new Manifest(is);
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Gets a specific header from the given manifest.
	 * @param manifest The manifest to search in
	 * @param headerName The name of the header to find
	 * @return The header, or null if the header is not present.
	 */
	private static String getManifestHeader(Manifest manifest, String headerName) {
		return manifest.getMainAttributes().getValue(headerName);
	}

}
