package com.ibm.cics.bundle.parts;

import static com.ibm.cics.bundle.parts.BundlePartType.EARBUNDLE;
import static com.ibm.cics.bundle.parts.BundlePartType.EPADAPTER;
import static com.ibm.cics.bundle.parts.BundlePartType.EPADAPTERSET;
import static com.ibm.cics.bundle.parts.BundlePartType.EVENTBINDING;
import static com.ibm.cics.bundle.parts.BundlePartType.FILE;
import static com.ibm.cics.bundle.parts.BundlePartType.LIBRARY;
import static com.ibm.cics.bundle.parts.BundlePartType.OSGIBUNDLE;
import static com.ibm.cics.bundle.parts.BundlePartType.POLICY;
import static com.ibm.cics.bundle.parts.BundlePartType.PROGRAM;
import static com.ibm.cics.bundle.parts.BundlePartType.TRANSACTION;
import static com.ibm.cics.bundle.parts.BundlePartType.URIMAP;
import static com.ibm.cics.bundle.parts.BundlePartType.WARBUNDLE;

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


import java.util.Comparator;

import com.ibm.cics.bundle.parts.DefineGraph.CycleDetectedException;

public class BundlePartTypeComparator implements Comparator<BundlePartType> {

	private static final DefineGraph DEPENDENCY_GRAPH;
	
	public static final BundlePartTypeComparator INSTANCE = new BundlePartTypeComparator();
	
	static {
		try {
			DEPENDENCY_GRAPH = new DefineGraph.Builder()
				.addDependedOnBy(PROGRAM, /* <- */ EARBUNDLE, WARBUNDLE, URIMAP, EPADAPTER, TRANSACTION )
				.addDependedOnBy(TRANSACTION, /* <- */ EPADAPTER)
				.addDependedOnBy(EPADAPTER, /* <- */ EPADAPTERSET)
				.addDependsOn(EVENTBINDING, /* -> */ EPADAPTER, EPADAPTERSET)
				.addDependsOn(PROGRAM, /* -> */ OSGIBUNDLE, LIBRARY)
				.addDependsOn(URIMAP, /* -> */ /* PIPELINE, not supported yet */ OSGIBUNDLE, EARBUNDLE, WARBUNDLE)
				.addDependsOn(POLICY, /* -> */ EPADAPTER, EPADAPTERSET)
				.addDependsOn(PROGRAM, /* -> */ FILE)
				.build();
		} catch (CycleDetectedException e) {
			throw new RuntimeException("Cycles detected in define graph", e);
		}
	}
	
	private BundlePartTypeComparator() {
	}
	
	@Override
	public int compare(BundlePartType o1, BundlePartType o2) {
		// -1 when o1 < o2
		if (DEPENDENCY_GRAPH.testDependsOn(o2, o1)) {
			return -1;
		}
		// +1 when o1 > o2
		if (DEPENDENCY_GRAPH.testDependsOn(o1, o2)) {
			return 1;
		}
		// Otherwise 0
		return 0;
	}

}
