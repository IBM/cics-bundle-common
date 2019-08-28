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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import org.junit.Test;

import com.ibm.cics.bundle.parts.DefineGraph.CycleDetectedException;

public class DefineGraphTest {

	@Test(expected=CycleDetectedException.class)
	public void selfCycle() throws Exception {
		new DefineGraph.Builder()
				.addDependsOn("A", "A")
				.build();
	}

	@Test(expected=CycleDetectedException.class)
	public void directCycle() throws Exception {
		new DefineGraph.Builder()
				.addDependsOn("A", "B")
				.addDependsOn("B", "A")
				.build();
	}

	@Test(expected=CycleDetectedException.class)
	public void indirectCycle() throws Exception {
		new DefineGraph.Builder()
				.addDependsOn("A", "B")
				.addDependsOn("C", "C")
				.addDependsOn("C", "A")
				.build();
	}

	@Test(expected=CycleDetectedException.class)
	public void branchingIndirectCycle() throws Exception {
		new DefineGraph.Builder()
				.addDependsOn("A", "B")
				.addDependsOn("A", "C")
				.addDependsOn("C", "A")
				.build();
	}

	@Test
	public void equality() throws Exception {
		Callable<DefineGraph> graphSupplier = () -> new DefineGraph.Builder()
				.addDependsOn("A", "B")
				.addDependsOn("C", "D")
				.addDependsOn("B", "E")
				.build();
		
		DefineGraph g1 = graphSupplier.call();
		DefineGraph g2 = graphSupplier.call();
		
		assertEquals(g1, g2);
	}

	@Test
	public void inequality() throws Exception {
		DefineGraph g1 = new DefineGraph.Builder()
				.addDependsOn("A", "B")
				.addDependsOn("C", "D")
				.addDependsOn("B", "E")
				.build();
		DefineGraph g2 = new DefineGraph.Builder()
				.addDependsOn("A", "B")
				.addDependsOn("C", "F")
				.addDependsOn("B", "E")
				.build();
		
		assertNotEquals(g1, g2);
	}

	@Test
	public void multiAddTypes() throws Exception {
		DefineGraph g1 = new DefineGraph.Builder()
				.addDependedOnBy("C", "A", "B")
				.addDependsOn("C", "D")
				.build();
		DefineGraph g2 = new DefineGraph.Builder()
				.addDependsOn("A", "C")
				.addDependsOn("B", "C")
				.addDependsOn("C", "D")
				.build();
		
		assertEquals(g1, g2);
	}

	@Test
	public void multiAddDependsOnTypes() throws Exception {
		DefineGraph g1 = new DefineGraph.Builder()
				.addDependsOn("A", "B", "C")
				.addDependsOn("C", "D")
				.build();
		DefineGraph g2 = new DefineGraph.Builder()
				.addDependsOn("A", "B")
				.addDependsOn("A", "C")
				.addDependsOn("C", "D")
				.build();
		
		assertEquals(g1, g2);
	}

	@Test
	public void dependsOnDirect() throws Exception {
		DefineGraph g = new DefineGraph.Builder()
				.addDependsOn("A", "B")
				.addDependsOn("C", "D")
				.addDependsOn("B", "E")
				.build();
		
		assertTrue(g.testDependsOn("C", "D"));
	}

	@Test
	public void dependsOnIndirect() throws Exception {
		DefineGraph g = new DefineGraph.Builder()
				.addDependsOn("A", "B")
				.addDependsOn("C", "D")
				.addDependsOn("B", "E")
				.build();
		
		assertTrue(g.testDependsOn("A", "E"));
	}

	@Test
	public void notDependsOn() throws Exception {
		DefineGraph g = new DefineGraph.Builder()
				.addDependsOn("A", "B")
				.addDependsOn("C", "D")
				.addDependsOn("B", "E")
				.build();
		
		assertFalse(g.testDependsOn("C", "E"));
	}

	@Test
	public void notDependsOnReverseDependency() throws Exception {
		DefineGraph g = new DefineGraph.Builder()
				.addDependsOn("A", "B")
				.build();
		
		assertFalse(g.testDependsOn("B", "A"));
	}

}
