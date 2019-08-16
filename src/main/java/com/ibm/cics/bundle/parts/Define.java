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

public class Define {
	
	private final String name;
	private final BundlePartType type;
	
	public Define(String name, BundlePartType type) {
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public BundlePartType getType() {
		return type;
	}
	
	public String getPath() {
		return name + "." + type.getBundlePartExtension();
	}
	
}
