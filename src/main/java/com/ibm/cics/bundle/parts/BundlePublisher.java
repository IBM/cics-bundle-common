package com.ibm.cics.bundle.parts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lombok.extern.slf4j.Slf4j;

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
	
	private final File targetDir;
	private final String bundleId;
	private final int major;
	private final int minor;
	private final int micro;
	private final int release;
	
	private Consumer<File> listener = f -> {};
	private List<BundlePart> bundleParts = new ArrayList<>();

	public BundlePublisher(File targetDir, String bundleId, int major, int minor, int micro, int release) {
		this.targetDir = targetDir;
		this.bundleId = bundleId;
		this.major = major;
		this.minor = minor;
		this.micro = micro;
		this.release = release;
	}
	
	public void addBundlePart(BundlePart bundlePart) {
		bundleParts.add(bundlePart);
	}
	
	public void setFileChangeListener(Consumer<File> listener) {
		this.listener = listener;
	}
	
	public void publishMetadata() throws PublishException {
		List<Define> defines = new ArrayList<>(bundleParts.size());
		for (int i = 0 ;i < bundleParts.size(); i++) {
			defines.add(writeDefine(bundleParts.get(i)));
		}

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

		File metaInf = new File(targetDir, "META-INF");
		metaInf.mkdirs();
		File manifest = new File(metaInf, "cics.xml");
		
		try (OutputStream out = new FileOutputStream(manifest)) {
			writeDocument(d, out);
			listener.accept(manifest);
		} catch (TransformerException | IOException e) {
			throw new PublishException("Error writing cics.xml", e);
		}
	}
	
	private Define writeDefine(BundlePart bundlePart) throws PublishException {
		Define define = new Define(bundlePart.getName(), bundlePart.getType());
		
		File defineFile = new File(targetDir, define.getPath());
		try (FileOutputStream out = new FileOutputStream(defineFile)) {
			bundlePart.writeDefine(out);
			log.debug("Wrote bundlepart to " + defineFile.getAbsolutePath());
			listener.accept(defineFile);			
			return define;
		} catch (IOException e) {
			throw new PublishException("Error writing bundle part \"" + define.getPath() + "\"", e);
		}
	}

	public void publishContent() throws PublishException {
		for (BundlePart bundlePart : bundleParts) {
			bundlePart.publishContent(targetDir, listener);
		}
	}
	
	public void createArchive(File cicsBundleArchive) throws PublishException {
		try {
			ZipArchiver zipArchiver = new ZipArchiver();
			zipArchiver.addDirectory(targetDir);
			zipArchiver.setDestFile(cicsBundleArchive);
			zipArchiver.createArchive();
		} catch (ArchiverException | IOException e) {
			throw new PublishException("Failed to create cics bundle archive", e);
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
		
	}

}
