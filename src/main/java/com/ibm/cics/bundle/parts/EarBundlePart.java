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
 * Definition of an enterprise application CICS bundle part.
 * <p>
 * This is defined by the <code>earbundle</code> XML element, with the
 * <code>http://www.ibm.com/xmlns/prod/cics/bundle/EARBUNDLE</code> schema.
 */
public class EarBundlePart extends AbstractLibertyApplicationBundlePart {

	/**
	 * @param symbolicName The symbolic name of the application file.
	 * @param jvmServer    The JVM server the enterprise application is deployed to.
	 * @param earFile      The enterprise application file this CICS bundle part
	 *                     deploys.
	 */
	public EarBundlePart(String symbolicName, String jvmServer, File earFile) {
		this(
				symbolicName,
				jvmServer,
				true,
				null,
				earFile);
	}

	/**
	 * @param symbolicName                The symbolic name of the application file.
	 * @param jvmServer                   The JVM server the enterprise application
	 *                                    is deployed to.
	 * @param addCicsAllAuthenticatedRole Controls whether CICS adds the
	 *                                    'cicsAllAuthenticated' security role to
	 *                                    the enterprise application.
	 * @param libertyAppConfigFile        The file containing application
	 *                                    configuration to apply to the enterprise
	 *                                    application.
	 * @param earFile                     The enterprise application file this CICS
	 *                                    bundle part deploys.
	 */
	public EarBundlePart(String symbolicName, String jvmServer, boolean addCicsAllAuthenticatedRole,
			File libertyAppConfigFile, File earFile) {
		super(
				symbolicName,
				BundlePartType.EARBUNDLE,
				symbolicName,
				jvmServer,
				addCicsAllAuthenticatedRole,
				libertyAppConfigFile,
				earFile,
				"ear");
	}

}
