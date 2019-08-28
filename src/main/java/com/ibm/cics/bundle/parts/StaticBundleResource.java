package com.ibm.cics.bundle.parts;

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