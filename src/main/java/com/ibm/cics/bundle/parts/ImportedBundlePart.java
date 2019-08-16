package com.ibm.cics.bundle.parts;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

import com.ibm.cics.bundle.parts.BundlePublisher.PublishException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImportedBundlePart extends BundlePart {

	private static final Map<String, BundlePartType> FILE_EXTENSION_TO_TYPE = Stream
		.of(
			BundlePartType.PACKAGESET,
			BundlePartType.EPADAPTER,
			BundlePartType.EPADAPTERSET,
			BundlePartType.FILE,
			BundlePartType.LIBRARY,
			BundlePartType.POLICY,
			BundlePartType.PROGRAM,
			BundlePartType.TCPIPSERVICE,
			BundlePartType.URIMAP,
			BundlePartType.EVENTBINDING
		)
		.collect(Collectors.toMap(
			BundlePartType::getBundlePartExtension,
			Function.identity()
		));
	
	private File toImport;
	
	public ImportedBundlePart(File toImport) throws ImportFailedException {
		this(getBundlePartCoords(toImport), toImport);
	}
	
	private ImportedBundlePart(BundlePartCoords coords, File toImport) throws ImportFailedException {
		super(coords.name, coords.type);
		this.toImport = toImport;
	}
	
	/**
	 * Attempts to derive a name, bundle part extension and bundle part type for the supplied file.
	 *   
	 * @param toImport the target file
	 * @return A String[] array containing {name, extension, type}
	 * @throws ImportFailedException if some part of the coordinates can't be determined
	 */
	private static BundlePartCoords getBundlePartCoords(File toImport) throws ImportFailedException {
		// not supported - atom config (xml file)
		// not supported - JVMSERVER (jvmprofile has to be in EBCDIC...?)
		// not supported - jsonapp
		// not supported - web service (didn't yet test but wsbind is binary and WSDL should be transferred as-is)
		String fileName = toImport.getName();
		
		int dot = fileName.lastIndexOf(".");
		if (dot <= 0) {
			throw new ImportFailedException("Couldn't determine bundle part type for file name: " + fileName);
		}
		
		String extension = fileName.substring(dot + 1).toLowerCase();
		boolean contained = FILE_EXTENSION_TO_TYPE.containsKey(extension);
		if(contained) {
			return new BundlePartCoords(fileName, FILE_EXTENSION_TO_TYPE.get(extension));
		} else {
			String importableBundleParts = FILE_EXTENSION_TO_TYPE
				.keySet()
				.stream()
				.collect(Collectors.joining(", "));
			throw new ImportFailedException("Couldn't determine bundle part type by extension for file \"" + toImport.getName() + "\".  Importable bundle part types are: " + importableBundleParts);
		}
	}

	@Override
	void publishContent(File workDir, Consumer<File> l) throws PublishException {
		//no-op for these bundle part types
	}

	@Override
	void writeDefine(OutputStream out) throws PublishException, IOException {
		IOUtils.copy(new FileInputStream(toImport), out);
	}
	
	@SuppressWarnings("serial")
	public static class ImportFailedException extends Exception {
		public ImportFailedException(String message) {
			super(message);
		}
	}
	
	private static class BundlePartCoords {
		private final String name;
		private final BundlePartType type;
		
		public BundlePartCoords(String name, BundlePartType type) {
			this.name = name;
			this.type = type;
		}
	}

}
