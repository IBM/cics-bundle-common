package com.ibm.cics.bundle.deploy;

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

import com.ibm.cics.bundle.parts.AbstractJavaBundlePart;
import com.ibm.cics.bundle.parts.WarBundlePart;

public class WarBundlePartTest extends AbstractJavaBundlePartTestCase {
	
	@Override
	protected AbstractJavaBundlePart createAbstractJavaBundlePart(File bin) {
		return new WarBundlePart("foo", "bar", bin);
	}

	@Override
	protected String getRootElementName() {
		return "warbundle";
	}

	@Override
	protected String getBinExtension() {
		return "war";
	}

	@Override
	protected void applyDefaults() {
		setExpectedSymbolicName("foo");
		setExpectedJVMServer("bar");
	}

}
