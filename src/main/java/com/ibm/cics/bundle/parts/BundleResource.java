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

import java.nio.file.Path;
import java.util.List;

/**
 * Defines a resource within a CICS bundle.
 */
public interface BundleResource extends BundleResourceContentSupplier {
	/**
	 * @return The path to the CICS bundle part descriptor.
	 */
	public Path getPath();

	/**
	 * @return A list of the resource in this CICS bundle part that are pointed to
	 *         by the bundle part descriptor.
	 */
	public List<BundleResource> getDynamicResources();

}
