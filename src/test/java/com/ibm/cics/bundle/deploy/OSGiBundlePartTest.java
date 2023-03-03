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
import com.ibm.cics.bundle.parts.OsgiBundlePart;

public class OSGiBundlePartTest extends AbstractJavaBundlePartTestCase {

	@Override
	protected AbstractJavaBundlePart createAbstractJavaBundlePart(File bin) {
		return new OsgiBundlePart("blah", "foo", "1.2.3.456789", "bar", bin);
	}

	@Override
	protected String getRootElementName() {
		return "osgibundle";
	}

	@Override
	protected String getBinExtension() {
		return "jar";
	}

	@Override
	protected void applyDefaults() {
		setExpectedSymbolicName("foo");
		setExpectedAttribute("version", "1.2.3.456789");
		setExpectedJVMServer("bar");
	}

}
