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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

public class BundlePartTypeComparatorTest {
	
	private BundlePartTypeComparator comparator = BundlePartTypeComparator.INSTANCE;
	
	@Test
	public void uriMapAfterWARBundle() {
		assertSame(1, comparator.compare(BundlePartType.URIMAP, BundlePartType.WARBUNDLE));
	}
	
	@Test
	public void osgiBundleBeforeWarBundle() {
		assertSame(-1, comparator.compare(BundlePartType.OSGIBUNDLE, BundlePartType.WARBUNDLE));
	}
	@Test
	public void warBundleBeforeURImap() {
		assertSame(-1, comparator.compare(BundlePartType.WARBUNDLE, BundlePartType.URIMAP));
	}
	
	@Test
	public void sameEqual() {
		assertSame(0, comparator.compare(BundlePartType.OSGIBUNDLE, BundlePartType.OSGIBUNDLE));
	}
	
	@Test
	public void peersEqual() {
		assertSame(0, comparator.compare(BundlePartType.EARBUNDLE, BundlePartType.WARBUNDLE));
	}
	
	@Test
	public void sortOSGiBundleAndURIMap() {
		List<BundlePartType> defines = Stream.of(BundlePartType.URIMAP, BundlePartType.OSGIBUNDLE).collect(Collectors.toList());
		List<BundlePartType> expected = Stream.of(BundlePartType.OSGIBUNDLE, BundlePartType.URIMAP).collect(Collectors.toList());
		
		defines.sort(comparator);
		
		assertEquals(expected, defines);
	}
	
}
