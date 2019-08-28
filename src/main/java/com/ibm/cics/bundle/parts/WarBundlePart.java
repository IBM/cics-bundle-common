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

public class WarBundlePart extends AbstractJavaBundlePart  {
	
	public WarBundlePart(String symbolicName, String jvmServer, File warFile) {
		super(
			symbolicName,
			BundlePartType.WARBUNDLE,
			symbolicName,
			jvmServer,
			warFile,
			"war"
		);
	}

}
