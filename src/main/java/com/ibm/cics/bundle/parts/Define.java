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

class Define {
	
	private final String name;
	private final BundlePartType type;
	private final String path;
	
	public Define(String name, BundlePartType type, String path) {
		this.name = name;
		this.type = type;
		this.path = path;
	}
	
	public String getName() {
		return name;
	}
	
	public BundlePartType getType() {
		return type;
	}
	
	public String getPath() {
		return path;
	}
	
}
