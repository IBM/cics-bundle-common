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

/**
 * Supplies the content of a CICS bundle resource.
 */
@FunctionalInterface
public interface BundleResourceContentSupplier {

	/**
	 * @return The contents of the resource in a stream.
	 * @throws IOException If the content cannot be read.
	 */
	public InputStream getContent() throws IOException;
	
}
