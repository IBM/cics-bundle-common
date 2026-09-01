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
 * Definition of an enterprise bundle CICS bundle part.
 * <p>
 * This is defined by the <code>ebabundle</code> XML element, with the
 * <code>http://www.ibm.com/xmlns/prod/cics/bundle/EBABUNDLE</code> schema.
 */
public class EbaBundlePart extends AbstractJavaBundlePart {

	public EbaBundlePart(String symbolicName, String jvmServer, File ebaFile) {
		super(
				symbolicName,
				BundlePartType.EBABUNDLE,
				symbolicName,
				jvmServer,
				ebaFile,
				"eba");
	}

}
