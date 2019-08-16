package com.ibm.cics.bundle.parts;

/*-
 * #%L
 * CICS Bundle Maven Plugin
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
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

import com.ibm.cics.bundle.parts.BundlePublisher.PublishException;

public abstract class BundlePart {

	private final String name;
	private final BundlePartType type;
	
	public BundlePart(String name, BundlePartType type) {
		this.name = name;
		this.type = type;
	}

	public BundlePartType getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}

	abstract void publishContent(File workDir, Consumer<File> l) throws PublishException;

	abstract void writeDefine(OutputStream out) throws PublishException, IOException;
}
