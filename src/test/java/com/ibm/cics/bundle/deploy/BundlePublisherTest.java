package com.ibm.cics.bundle.deploy;

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

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isIdenticalTo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ibm.cics.bundle.parts.BundlePublisher;
import com.ibm.cics.bundle.parts.EarBundlePart;

public class BundlePublisherTest {
	
	private static final String EAR_CONTENTS = "contents";
	@Rule
	public TemporaryFolder tf = new TemporaryFolder();
	private Path bundleRoot;
	private File ear;
	private Path bundleArchive;
	
	
	@Before
	public void setUp() throws Exception {
		bundleRoot = tf.newFolder().toPath();
		ear = tf.newFile("my.ear");
		FileUtils.write(ear, EAR_CONTENTS, StandardCharsets.UTF_8);
		bundleArchive = tf.newFile("bundle.zip").toPath();
	}

	@Test
	public void publish() throws Exception {
		BundlePublisher bundlePublisher = new BundlePublisher(bundleRoot, "foo", 1, 2, 3, 4);
		bundlePublisher.addResource(new EarBundlePart("bar", "banana", ear));
		
		bundlePublisher.publishResources();
		bundlePublisher.publishDynamicResources();
		//verify before zip
		verifyContents(bundleRoot);
		bundlePublisher.createArchive(bundleArchive);
		
		FileSystem bundleArchiveFs = FileSystems.newFileSystem(bundleArchive, null);
		//verify after zip
		verifyContents(bundleArchiveFs.getRootDirectories().iterator().next());
	}

	private void verifyContents(Path root) throws IOException {
		List<String> paths = Files
			.walk(bundleRoot)
			.sorted()
			.map(bundleRoot::relativize)
			.map(Path::toString)
			.collect(Collectors.toList());
		
		assertThat(paths, contains("", "META-INF", "META-INF/cics.xml", "bar.ear", "bar.earbundle"));
		
		String cicsXml = readBundleFile(root, "META-INF/cics.xml");
		
		assertThat(
			cicsXml,
			isIdenticalTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<manifest xmlns=\"http://www.ibm.com/xmlns/prod/cics/bundle\" bundleMajorVer=\"1\" bundleMicroVer=\"3\" bundleMinorVer=\"2\" bundleRelease=\"4\" bundleVersion=\"1\" id=\"foo\">\n" + 
				"  <meta_directives>\n" + 
				"    <timestamp>2019-08-28T08:26:05.076Z</timestamp>\n" + 
				"  </meta_directives>\n" + 
				"  <define name=\"bar\" path=\"bar.earbundle\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/EARBUNDLE\"/>\n" + 
				"</manifest>"
			).withNodeFilter(
				//Ignore timestamps as they'll vary between test runs
				node -> !"timestamp".equals(node.getNodeName())
			)
		);
		
		assertThat(
			EAR_CONTENTS,
			Matchers.equalTo(readBundleFile(root, "bar.ear"))
		);
		
		assertThat(
			readBundleFile(root, "bar.earbundle"),
			isIdenticalTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<earbundle jvmserver=\"banana\" symbolicname=\"bar\"/>"
			)
		);
	}

	private String readBundleFile(Path root, String p) throws IOException {
		return new String(Files.readAllBytes(bundleRoot.resolve(p)), StandardCharsets.UTF_8);
	}
	
}
