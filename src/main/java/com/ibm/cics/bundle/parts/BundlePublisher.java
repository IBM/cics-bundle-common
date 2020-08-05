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

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
public class BundlePublisher {

	private static final Comparator<Define> DEFINE_COMPARATOR = Comparator
		.comparing(Define::getType, BundlePartTypeComparator.INSTANCE) 
		.thenComparing(d -> d.getType().getURI()) // Text-based URI comparison if no inherent dependency
		.thenComparing(Define::getName);
	
	private static final DocumentBuilder DOCUMENT_BUILDER;
	private static final Transformer TRANSFORMER;
	
	static {
		try {
			DOCUMENT_BUILDER = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			TRANSFORMER = TransformerFactory.newInstance().newTransformer();
			TRANSFORMER.setOutputProperty(OutputKeys.INDENT, "yes");
			TRANSFORMER.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
		} catch (ParserConfigurationException | TransformerConfigurationException | TransformerFactoryConfigurationError e) {
			throw new IllegalStateException(e);
		}
	}
	
	private final Path bundleRoot;
	private final String bundleId;
	private final int major;
	private final int minor;
	private final int micro;
	private final int release;
	
	private Consumer<Path> listener = f -> {};
	private Map<Path, BundleResource> bundleResources = new HashMap<>();
	private List<Define> defines = new ArrayList<>();

	public BundlePublisher(Path bundleRoot, String bundleId, int major, int minor, int micro, int release) {
		this.bundleRoot = bundleRoot;
		this.bundleId = bundleId;
		this.major = major;
		this.minor = minor;
		this.micro = micro;
		this.release = release;
	}
	
	public void addStaticResource(Path path, BundleResourceContentSupplier content) throws PublishException {
		addResource(new StaticBundleResource(path, content));
	}
	
	public void addResource(BundleResource resource) throws PublishException {
		Path destinationPath = getPathInBundle(resource);
		
		Define define = getDefine(destinationPath);
		if (define != null) {
			defines.add(define);
		}
		
		bundleResources.put(destinationPath, resource);
	}
	
	/**
	 * Attempts to derive a name, bundle part extension and bundle part type for the supplied file.
	 *   
	 * @param toImport relative path in the bundle to the target file
	 * @return a Define statement to add to cics.xml if the file was a bundle part, or null if not
	 */
	private static Define getDefine(Path toImport) {
		if (toImport != null) {
			final Path filePath = toImport.getFileName();
			if (filePath != null) {
				String fileName = filePath.toString();
				int dot = fileName.lastIndexOf(".");
				if (dot <= 0) {
					log.debug("Couldn't determine bundle part type for file name: " + fileName);
					return null;
				}
				String extension = fileName.substring(dot + 1).toLowerCase(Locale.getDefault());
				BundlePartType type = BundlePartType.getType(extension);
				if (type != null) {
					String name = fileName.substring(0, dot);
					if (name.length() > 0) {
						return new Define(name, type, toImport.toString());
					} else {
						log.debug("Couldn't determine bundle part name for file \"" + fileName + "\"");
						return null;
					}
				} else {
					log.debug("Couldn't determine bundle part type by extension for file \"" + fileName + "\"");
					return null;
				}
			}
			log.debug("Couldn't determine file name: (null) ");
			return null;
		}
		log.debug("Couldn't determine file name from import path: (null) ");
		return null;
	}

	private Path getPathInBundle(BundleResource resource) throws PublishException {
		Path pathInBundle = resource.getPath().normalize();
		if (pathInBundle.isAbsolute()) {
			throw new PublishException("Path " + pathInBundle.toString() + " was not relative");
		}
		
		if (!bundleRoot.resolve(pathInBundle).startsWith(bundleRoot)) {
			throw new PublishException("Path " + pathInBundle + " resolved to outside of bundle root");
		}
		
		if (bundleResources.containsKey(pathInBundle)) {
			throw new PublishException("A resource already exists at path " + pathInBundle);
		}
		return pathInBundle;
	}
	
	public void setFileChangeListener(Consumer<Path> listener) {
		this.listener = listener;
	}
	
	public void publishResources() throws PublishException {
		//Sort defines so dependencies always get installed first
		defines.sort(DEFINE_COMPARATOR);

		writeManifest(
			defines,
			bundleId,
			major,
			minor,
			micro,
			release
		);
		
		for (Map.Entry<Path, BundleResource> bundleResourceE : bundleResources.entrySet()) {
			writeBundleResource(
				bundleResourceE.getKey(),
				bundleResourceE.getValue()
			);
		}
	}

	private void writeBundleResource(Path pathInBundle, BundleResource bundleResource) throws PublishException {
		Path absolutePath = bundleRoot.resolve(pathInBundle);
		
		try {
			final Path parent = absolutePath.getParent();
			if (parent == null) {
				throw new PublishException("Error getting parent for bundle resource \"" + pathInBundle + "\"");
			}
			Files.createDirectories(parent);
		} catch (IOException e1) {
			throw new PublishException("Error creating directories for bundle resource \"" + pathInBundle + "\"");
		}
		
		try (InputStream is = new BufferedInputStream(bundleResource.getContent())) {
			Files.copy(is, absolutePath);
			log.debug("Wrote resource to " + pathInBundle);
			listener.accept(absolutePath);
		} catch (IOException e) {
			throw new PublishException("Error writing bundle resource \"" + pathInBundle + "\"", e);
		}
	}
	
	public void publishDynamicResources() throws PublishException {
		for (BundleResource bundleResource : bundleResources.values()) {
			for (BundleResource dynamicResource : bundleResource.getDynamicResources()) {
				Path pathInBundle = getPathInBundle(dynamicResource);
				writeBundleResource(pathInBundle, dynamicResource);
			}
		}
	}
	
	private void writeManifest(List<Define> defines, String id, int major, int minor, int micro, int release) throws PublishException {
		Document d = DOCUMENT_BUILDER.newDocument();
		Element root = d.createElementNS(BundlePartType.NS, "manifest");
		root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", BundlePartType.NS);
		root.setAttribute("id", id);
		
		root.setAttribute("bundleMajorVer", String.valueOf(major));
		root.setAttribute("bundleMinorVer", String.valueOf(minor));
		root.setAttribute("bundleMicroVer", String.valueOf(micro));
		root.setAttribute("bundleRelease", String.valueOf(release));
		root.setAttribute("bundleVersion", "1");
		
		d.appendChild(root);
		
		Element metaDirectives = d.createElementNS(BundlePartType.NS, "meta_directives");
		root.appendChild(metaDirectives);
		
		Element timestamp = d.createElementNS(BundlePartType.NS, "timestamp");
		metaDirectives.appendChild(timestamp);
		timestamp.setTextContent(ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
		
		defines.sort(DEFINE_COMPARATOR);
		
		for (Define define : defines) {
			Element defineElement = d.createElementNS(BundlePartType.NS, "define");
			defineElement.setAttribute("name", define.getName());
			defineElement.setAttribute("type", define.getType().getURI());
			defineElement.setAttribute("path", define.getPath());
			root.appendChild(defineElement);
		}

		Path metaInf = bundleRoot.resolve("META-INF");
		try {
			Files.createDirectories(metaInf);
		} catch (IOException e) {
			throw new PublishException("Couldn't create META-INF directory", e);
		}
		Path manifest = metaInf.resolve("cics.xml");
		
		try (OutputStream out = Files.newOutputStream(manifest)) {
			writeDocument(d, out);
			listener.accept(manifest);
		} catch (TransformerException | IOException e) {
			throw new PublishException("Error writing cics.xml", e);
		}
	}
	
	static Document createDocument() {
		return BundlePublisher.DOCUMENT_BUILDER.newDocument();
	}
	
	static void writeDocument(Document d, OutputStream out) throws TransformerException {
		TRANSFORMER.transform(
			new DOMSource(d),
			new StreamResult(out)
		);
	}
	
	@SuppressWarnings("serial")
	public static class PublishException extends Exception {
		
		PublishException(String message, Throwable cause) {
			super(message, cause);
		}
		
		PublishException(String message) {
			super(message);
		}
		
	}

}
