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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefineGraph {

	public Map<Object, Set<Object>> typesDependedOnByTypeGraph = new HashMap<>();

	private DefineGraph(Map<Object, Set<Object>> sourceMap) throws CycleDetectedException {
		// Clone to avoid subsequent manipulation via the builder
		sourceMap.forEach((type, typesDependedOnByType) -> {
			typesDependedOnByTypeGraph.put(type, new HashSet<>(typesDependedOnByType));
		});
		checkForCycles();
	}

	private void checkForCycles() throws CycleDetectedException {
		Set<Object> completedTypes = new HashSet<>(typesDependedOnByTypeGraph.size());
		for (Object type : typesDependedOnByTypeGraph.keySet()) {
			List<Object> alreadyVisited = new ArrayList<>();
			visitAndCheckPath(type, alreadyVisited, completedTypes);
			completedTypes.add(type);
		}
	}

	private void visitAndCheckPath(Object type, List<Object> alreadyVisited, Set<Object> completedTypes)
			throws CycleDetectedException {
		boolean newVisit = !alreadyVisited.contains(type);
		if (!newVisit) {
			throw new CycleDetectedException(
				"Cycle detected in define graph:\n\n" + 
				alreadyVisited
					.stream()
					.map(Object::toString)
					.collect(Collectors.joining(" depends on\n")) +
				" depends on\n" + type
			);
		}
		alreadyVisited.add(type);
		Set<Object> typesDependedOnByType = typesDependedOnByTypeGraph.get(type);
		if (typesDependedOnByType != null) {
			for (Object typeDependedOn : typesDependedOnByType) {
				if (!completedTypes.contains(typeDependedOn)) {
					visitAndCheckPath(typeDependedOn, new ArrayList<>(alreadyVisited), completedTypes);
				}
			}
		}

	}

	public boolean testDependsOn(Object type, Object dependsOnType) {
		Set<Object> typesDependedOnByType = typesDependedOnByTypeGraph.get(type);
		if (typesDependedOnByType != null) {
			for (Object typeDependedOnByType : typesDependedOnByType) {
				if (dependsOnType.equals(typeDependedOnByType)) {
					return true;
				}
				boolean indirectResult = testDependsOn(typeDependedOnByType, dependsOnType);
				if (indirectResult) {
					return true;
				}
			}
		}
		return false;

	}

	public static class Builder {

		private Map<Object, Set<Object>> typesDependedOnByTypeGraph = new HashMap<>();

		public Builder addDependsOn(Object type, Object... dependsOnTypes) {
			Arrays.asList(dependsOnTypes).forEach(t -> addDependency(type, t));
			return this;
		}

		public Builder addDependedOnBy(Object type, Object... dependedOnBy) {
			Arrays.asList(dependedOnBy).forEach(t -> addDependency(t, type));
			return this;
		}

		private Builder addDependency(Object type, Object dependsOnType) {
			Set<Object> typesDependedOnByThisType = typesDependedOnByTypeGraph.get(type);
			if (typesDependedOnByThisType == null) {
				typesDependedOnByTypeGraph.put(type, typesDependedOnByThisType = new HashSet<>());
			}
			typesDependedOnByThisType.add(dependsOnType);
			return this;
		}

		public DefineGraph build() throws CycleDetectedException {
			return new DefineGraph(typesDependedOnByTypeGraph);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((typesDependedOnByTypeGraph == null) ? 0 : typesDependedOnByTypeGraph.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefineGraph other = (DefineGraph) obj;
		if (typesDependedOnByTypeGraph == null) {
			if (other.typesDependedOnByTypeGraph != null)
				return false;
		} else if (!typesDependedOnByTypeGraph.equals(other.typesDependedOnByTypeGraph))
			return false;
		return true;
	}

	public class CycleDetectedException extends Exception {

		private static final long serialVersionUID = 1L;

		private CycleDetectedException(String message) {
			super(message);
		}

	}

}
