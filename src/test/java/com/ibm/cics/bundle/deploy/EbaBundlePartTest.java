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
import com.ibm.cics.bundle.parts.EbaBundlePart;

public class EbaBundlePartTest extends AbstractJavaBundlePartTestCase {
	
	@Override
	protected AbstractJavaBundlePart createAbstractJavaBundlePart(File bin) {
		return new EbaBundlePart("foo", "bar", bin);
	}

	@Override
	protected String getRootElementName() {
		return "ebabundle";
	}

	@Override
	protected String getBinExtension() {
		return "eba";
	}

	@Override
	protected void applyDefaults() {
		setExpectedSymbolicName("foo");
		setExpectedJVMServer("bar");
	}

}
