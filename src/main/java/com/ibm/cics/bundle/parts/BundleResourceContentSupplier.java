package com.ibm.cics.bundle.parts;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface BundleResourceContentSupplier {

	public InputStream getContent() throws IOException;
	
}
