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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.cics.bundle.parts.BundlePublisher.PublishException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractJavaBundlePart extends BundlePart {

	private final String jvmServer;
	private final File bin;
	private final String binExtension;
	private final String symbolicName;
	
	public AbstractJavaBundlePart(
			String name,
			BundlePartType type,
			String symbolicName,
			String jvmServer,
			File bin,
			String binExtension) {
		super(name, type);
		//TODO: make sure Maven plugin sets name to name-version
		//TODO: make sure we validate this properly in Maven (just have to validate that it has a value)
		//TODO: hook SLF4J logger statements up in Maven plugin (for instance): https://bitbucket.org/peachjean/slf4j-mojo/src/default/
		if (jvmServer == null || "".equals(jvmServer)) throw new IllegalStateException("JVM server was not supplied");
		this.jvmServer = jvmServer;
		this.symbolicName = symbolicName;
		this.bin = bin;
		this.binExtension = binExtension;
	}
	
	@Override
	void publishContent(File workDir, Consumer<File> l) throws PublishException {
		File targetFile = new File(workDir, getName() + "." + binExtension);
		try {
			FileUtils.copyFile(bin, targetFile);
			log.debug("Copied content " + bin + " to " + targetFile);
			l.accept(targetFile);
		} catch (IOException e) {
			throw new PublishException("Error copying "+ bin, e);
		}
	}
	
	@Override
	void writeDefine(OutputStream out) throws PublishException, IOException {
		Document bundlePart = BundlePublisher.createDocument();
		
		Element root = bundlePart.createElement(getType().getBundlePartExtension());
		bundlePart.appendChild(root);
		
		root.setAttribute("symbolicname", symbolicName);
		root.setAttribute("jvmserver", jvmServer);
		
		addAdditionalNodes(root);
		
		try {
			BundlePublisher.writeDocument(bundlePart, out);
		} catch (TransformerException e) {
			throw new IOException(e);
		}
	}
	
	protected void addAdditionalNodes(Element rootElement) {
		//no-op
	}
	
}
