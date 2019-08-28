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
import java.util.Collections;
import java.util.List;

public class StaticBundleResource implements BundleResource {
	private final Path path;
	private final BundleResourceContentSupplier contentSupplier;

	public StaticBundleResource(Path path, BundleResourceContentSupplier contentsSupplier) {
		this.path = path;
		this.contentSupplier = contentsSupplier;
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public List<BundleResource> getDynamicResources() {
		return Collections.emptyList();
	}

	@Override
	public InputStream getContent() throws IOException {
		return contentSupplier.getContent();
	}
}
