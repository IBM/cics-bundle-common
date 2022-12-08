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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
public abstract class AbstractJavaBundlePart extends BundlePartResource {

	private final String jvmServer;
	private final File bin;
	private final String binExtension;
	private final String symbolicName;
	private String versionRange;
	
	public AbstractJavaBundlePart(
			String name,
			BundlePartType type,
			String symbolicName,
			String jvmServer,
			File bin,
			String binExtension) {
		super(name, type);
		if (jvmServer == null || "".equals(jvmServer)) throw new IllegalStateException("JVM server was not supplied");
		this.jvmServer = jvmServer;
		this.symbolicName = symbolicName;
		this.bin = bin;
		this.binExtension = binExtension;
	}

	public AbstractJavaBundlePart(
			String name,
			BundlePartType type,
			String symbolicName,
			String jvmServer,
			File bin,
			String binExtension,
			String versionRange) {
		this(name, type, symbolicName, jvmServer, bin, binExtension);
		this.versionRange = versionRange;
	}

	@Override
	public InputStream getContent() throws IOException {
		Document bundlePart = BundlePublisher.createDocument();
		
		Element root = bundlePart.createElement(getType().getBundlePartExtension());
		bundlePart.appendChild(root);

		root.setAttribute("symbolicname", symbolicName);
		root.setAttribute("jvmserver", jvmServer);
		if (!versionRange.equals("") && !versionRange.isEmpty()) {
			root.setAttribute("versionrange", versionRange);
		}

		addAdditionalNodes(root);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		try {
			BundlePublisher.writeDocument(bundlePart, outputStream);
			return new ByteArrayInputStream(outputStream.toByteArray());
		} catch (TransformerException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public List<BundleResource> getDynamicResources() {
		return Collections.singletonList(new StaticBundleResource(Paths.get(getName() + "." + binExtension), () -> new FileInputStream(bin)));
	}
	
	protected void addAdditionalNodes(Element rootElement) {
		//no-op
	}
	
}
