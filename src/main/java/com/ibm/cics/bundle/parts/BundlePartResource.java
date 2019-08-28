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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;


abstract class BundlePartResource implements BundleResource {

	private final String name;
	private final BundlePartType type;
	private Path path;
	
	protected BundlePartResource(String name, BundlePartType type) {
		this(name, type, Paths.get(""));
	}
	
	protected BundlePartResource(String name, BundlePartType type, Path parentPath) {
		this.name = name;
		this.type = type;
		this.path = parentPath.resolve(name + "." + type.getBundlePartExtension());
	}

	public BundlePartType getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public Path getPath() {
		return path;
	}
	
	@Override
	public abstract InputStream getContent() throws IOException;
}
