package com.ibm.cics.bundle.deploy;

/*-
 * #%L
 * CICS Bundle Common Parent
 * %%
 * Copyright (C) 2019, 2023 IBM Corp.
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.matchers.CompareMatcher;

import com.ibm.cics.bundle.parts.AbstractJavaBundlePart;
import com.ibm.cics.bundle.parts.BundleResource;

public abstract class AbstractJavaBundlePartTestCase {

	@Rule
	public TemporaryFolder tf = new TemporaryFolder();
	
	private AbstractJavaBundlePart bundlePart;
	private Map<String, String> bundlePartAttributes = new HashMap<>();
	private File bin;

	@Before
	public void setUp() throws Exception {
		bin = tf.newFile();
		FileUtils.write(bin, "this is the content of my bin", StandardCharsets.UTF_8);
		
		bundlePart = createAbstractJavaBundlePart(bin);
		applyDefaults();
	}
	
	protected abstract AbstractJavaBundlePart createAbstractJavaBundlePart(File bin);
	
	protected abstract void applyDefaults();
	
	protected abstract String getRootElementName();
	
	protected abstract String getBinExtension();
	
	protected void setExpectedAttribute(String name, String value) {
		bundlePartAttributes.put(name, value);
	}
	
	protected void setExpectedSymbolicName(String name) {
		bundlePartAttributes.put("symbolicname", name);
	}
	
	protected void setExpectedJVMServer(String jvmServer) {
		bundlePartAttributes.put("jvmserver", jvmServer);
	}

	protected void setExpectedVersionRange(String versionrange) {
		bundlePartAttributes.put("versionRange", versionrange);
	}
	
	protected void assertBundlePart() throws Exception {
		try (InputStream content = bundlePart.getContent()) {
			String bundlePart = IOUtils.toString(content, StandardCharsets.UTF_8);
			Document expected = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = expected.createElement(getRootElementName());
			expected.appendChild(root);
			bundlePartAttributes.forEach(root::setAttribute);
			assertThat(
				bundlePart,
				CompareMatcher
					.isIdenticalTo(
						expected
					)
					.withDifferenceEvaluator(
						DifferenceEvaluators.chain(
							DifferenceEvaluators.ignorePrologDifferencesExceptDoctype(),
							DifferenceEvaluators.Default
						)
					)
			);
		}
		
		List<BundleResource> dynamicResources = bundlePart.getDynamicResources();
		
		assertThat(dynamicResources, hasSize(1));
		BundleResource resource = dynamicResources.get(0);
		
		assertThat(resource.getDynamicResources(), hasSize(0));
		
		try (InputStream actualIS = resource.getContent(); InputStream expectedIS = new FileInputStream(bin)) {
			byte[] actual = IOUtils.toByteArray(actualIS);
			byte[] expected = IOUtils.toByteArray(expectedIS);
			assertArrayEquals(expected, actual);
		}
	}
	
	@Test
	public void defaults() throws Exception {
		assertBundlePart();
	}

}
