package com.ibm.cics.bundle.parts;

import java.nio.file.Path;
import java.util.List;

public interface BundleResource extends BundleResourceContentSupplier {
	
	public Path getPath();
	
	public List<BundleResource> getDynamicResources();
	
}