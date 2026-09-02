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

import java.io.File;

/**
 * Definition of an web application CICS bundle part.
 * <p>
 * This is defined by the <code>warbundle</code> XML element, with the
 * <code>http://www.ibm.com/xmlns/prod/cics/bundle/WARBUNDLE</code> schema.
 */
public class WarBundlePart extends AbstractLibertyApplicationBundlePart {

	/**
	 * @param symbolicName The symbolic name of the application file.
	 * @param jvmServer    The JVM server the enterprise application is deployed to.
	 * @param warFile      The web application file this CICS bundle part
	 *                     deploys.
	 */
	public WarBundlePart(String symbolicName, String jvmServer, File warFile) {
		this(
				symbolicName,
				jvmServer,
				true,
				null,
				warFile);
	}

	/**
	 * @param symbolicName                The symbolic name of the application file.
	 * @param jvmServer                   The JVM server the enterprise application
	 *                                    is deployed to.
	 * @param addCicsAllAuthenticatedRole Controls whether CICS adds the
	 *                                    'cicsAllAuthenticated' security role to
	 *                                    the web application.
	 * @param libertyAppConfigFile        The file containing application
	 *                                    configuration to apply to the web
	 *                                    application.
	 * @param warFile                     The web application file this CICS bundle
	 *                                    part deploys.
	 */
	public WarBundlePart(String symbolicName, String jvmServer, boolean addCicsAllAuthenticatedRole,
			File libertyAppConfigFile, File warFile) {
		super(
				symbolicName,
				BundlePartType.WARBUNDLE,
				symbolicName,
				jvmServer,
				addCicsAllAuthenticatedRole,
				libertyAppConfigFile,
				warFile,
				"war");
	}

}
