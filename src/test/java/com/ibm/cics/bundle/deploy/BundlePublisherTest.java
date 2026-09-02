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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isIdenticalTo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import com.ibm.cics.bundle.parts.WarBundlePart;

public class BundlePublisherTest {
	
	private static final String EAR_CONTENTS = "contents";
	@Rule
	public TemporaryFolder tf = new TemporaryFolder();
	private Path bundleRoot;
	private File ear;
	
	@Before
	public void setUp() throws Exception {
		bundleRoot = tf.newFolder().toPath();
		ear = tf.newFile("my.ear");
		FileUtils.write(ear, EAR_CONTENTS, StandardCharsets.UTF_8);
	}

	@Test
	public void publish() throws Exception {
		BundlePublisher bundlePublisher = new BundlePublisher(bundleRoot, "foo", 1, 2, 3);
		bundlePublisher.addResource(new EarBundlePart("bar", "banana", ear));
		
		bundlePublisher.publishResources();
		bundlePublisher.publishDynamicResources();
		//verify before zip
		verifyContents(bundleRoot);
	}

	private void verifyContents(Path root) throws IOException {
		List<String> paths = Files
			.walk(bundleRoot)
			.sorted()
			.map(bundleRoot::relativize)
			.map(Path::toString)
			.collect(Collectors.toList());
		
		assertThat(paths, containsInAnyOrder("", "META-INF", "META-INF" + File.separator + "cics.xml", "bar.ear", "bar.earbundle"));
		
		String cicsXml = readBundleFile(root, "META-INF/cics.xml");
		
		assertThat(
			cicsXml,
			isIdenticalTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<manifest xmlns=\"http://www.ibm.com/xmlns/prod/cics/bundle\" bundleMajorVer=\"1\" bundleMicroVer=\"3\" bundleMinorVer=\"2\" bundleRelease=\"0\" bundleVersion=\"1\" id=\"foo\">\n" +
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

	@Test
	public void publishEarWithAppConfigFile() throws Exception {
		File appConfigFile = tf.newFile("ear-app-config.xml");
		FileUtils.write(appConfigFile, "<server></server>", StandardCharsets.UTF_8);
		
		BundlePublisher bundlePublisher = new BundlePublisher(bundleRoot, "foo", 1, 2, 3);
		bundlePublisher.addResource(new EarBundlePart("bar", "banana", true, appConfigFile, ear));
		
		bundlePublisher.publishResources();
		bundlePublisher.publishDynamicResources();
		
		List<String> paths = Files
			.walk(bundleRoot)
			.sorted()
			.map(bundleRoot::relativize)
			.map(Path::toString)
			.collect(Collectors.toList());
		
		assertThat(paths, containsInAnyOrder("", "META-INF", "META-INF" + File.separator + "cics.xml",
				"bar.ear", "bar.earbundle", "ear-app-config.xml"));
		
		assertThat(
			readBundleFile(bundleRoot, "bar.earbundle"),
			isIdenticalTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<earbundle appConfigFile=\"ear-app-config.xml\" jvmserver=\"banana\" symbolicname=\"bar\"/>"
			)
		);
	}

	@Test
	public void publishEarWithAddCICSAllAuthFalse() throws Exception {
		BundlePublisher bundlePublisher = new BundlePublisher(bundleRoot, "foo", 1, 2, 3);
		bundlePublisher.addResource(new EarBundlePart("bar", "banana", false, null, ear));
		
		bundlePublisher.publishResources();
		bundlePublisher.publishDynamicResources();
		
		assertThat(
			readBundleFile(bundleRoot, "bar.earbundle"),
			isIdenticalTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<earbundle addCICSAllAuth=\"false\" jvmserver=\"banana\" symbolicname=\"bar\"/>"
			)
		);
	}

	@Test
	public void publishEarWithBothAppConfigFileAndAddCICSAllAuthFalse() throws Exception {
		File appConfigFile = tf.newFile("ear-app-config.xml");
		FileUtils.write(appConfigFile, "<server></server>", StandardCharsets.UTF_8);
		
		BundlePublisher bundlePublisher = new BundlePublisher(bundleRoot, "foo", 1, 2, 3);
		bundlePublisher.addResource(new EarBundlePart("bar", "banana", false, appConfigFile, ear));
		
		bundlePublisher.publishResources();
		bundlePublisher.publishDynamicResources();
		
		assertThat(
			readBundleFile(bundleRoot, "bar.earbundle"),
			isIdenticalTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<earbundle addCICSAllAuth=\"false\" appConfigFile=\"ear-app-config.xml\" jvmserver=\"banana\" symbolicname=\"bar\"/>"
			)
		);
	}

	@Test
	public void publishWarWithAppConfigFile() throws Exception {
		File war = tf.newFile("my.war");
		FileUtils.write(war, "war contents", StandardCharsets.UTF_8);
		File appConfigFile = tf.newFile("war-app-config.xml");
		FileUtils.write(appConfigFile, "<server></server>", StandardCharsets.UTF_8);
		
		BundlePublisher bundlePublisher = new BundlePublisher(bundleRoot, "foo", 1, 2, 3);
		bundlePublisher.addResource(new WarBundlePart("webapp", "banana", true, appConfigFile, war));
		
		bundlePublisher.publishResources();
		bundlePublisher.publishDynamicResources();
		
		List<String> paths = Files
			.walk(bundleRoot)
			.sorted()
			.map(bundleRoot::relativize)
			.map(Path::toString)
			.collect(Collectors.toList());
		
		assertThat(paths, containsInAnyOrder("", "META-INF", "META-INF" + File.separator + "cics.xml",
				"webapp.war", "webapp.warbundle", "war-app-config.xml"));
		
		assertThat(
			readBundleFile(bundleRoot, "webapp.warbundle"),
			isIdenticalTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<warbundle appConfigFile=\"war-app-config.xml\" jvmserver=\"banana\" symbolicname=\"webapp\"/>"
			)
		);
	}

	@Test
	public void publishWarWithAddCICSAllAuthFalse() throws Exception {
		File war = tf.newFile("my.war");
		FileUtils.write(war, "war contents", StandardCharsets.UTF_8);
		
		BundlePublisher bundlePublisher = new BundlePublisher(bundleRoot, "foo", 1, 2, 3);
		bundlePublisher.addResource(new WarBundlePart("webapp", "banana", false, null, war));
		
		bundlePublisher.publishResources();
		bundlePublisher.publishDynamicResources();
		
		assertThat(
			readBundleFile(bundleRoot, "webapp.warbundle"),
			isIdenticalTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<warbundle addCICSAllAuth=\"false\" jvmserver=\"banana\" symbolicname=\"webapp\"/>"
			)
		);
	}

	@Test
	public void publishWarWithBothAppConfigFileAndAddCICSAllAuthFalse() throws Exception {
		File war = tf.newFile("my.war");
		FileUtils.write(war, "war contents", StandardCharsets.UTF_8);
		File appConfigFile = tf.newFile("war-app-config.xml");
		FileUtils.write(appConfigFile, "<server></server>", StandardCharsets.UTF_8);
		
		BundlePublisher bundlePublisher = new BundlePublisher(bundleRoot, "foo", 1, 2, 3);
		bundlePublisher.addResource(new WarBundlePart("webapp", "banana", false, appConfigFile, war));
		
		bundlePublisher.publishResources();
		bundlePublisher.publishDynamicResources();
		
		assertThat(
			readBundleFile(bundleRoot, "webapp.warbundle"),
			isIdenticalTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<warbundle addCICSAllAuth=\"false\" appConfigFile=\"war-app-config.xml\" jvmserver=\"banana\" symbolicname=\"webapp\"/>"
			)
		);
	}

	private String readBundleFile(Path root, String p) throws IOException {
		return new String(Files.readAllBytes(bundleRoot.resolve(p)), StandardCharsets.UTF_8);
	}
	
}
