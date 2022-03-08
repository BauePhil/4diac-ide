/*******************************************************************************
 * Copyright (c) 2022 Martin Erich Jobst
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Martin Jobst - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.model.eval.st

import org.eclipse.fordiac.ide.model.eval.Evaluator
import org.eclipse.fordiac.ide.model.eval.variable.Variable
import org.eclipse.fordiac.ide.model.libraryElement.STAlgorithm
import org.eclipse.fordiac.ide.structuredtextalgorithm.stalgorithm.STAlgorithmBody
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

import static extension org.eclipse.fordiac.ide.structuredtextalgorithm.util.StructuredTextParseUtil.*

@FinalFieldsConstructor
class STAlgorithmEvaluator extends StructuredTextEvaluator {
	final STAlgorithm algorithm

	STAlgorithmBody parseResult

	new(STAlgorithm algorithm, Iterable<Variable> variables, Evaluator parent) {
		super(algorithm.name, variables, parent)
		this.algorithm = algorithm
	}

	override prepare() {
		if (parseResult === null) {
			val errors = newArrayList
			parseResult = algorithm.parse(errors)
			if (parseResult === null) {
				errors.forEach[error("Parse error: " + it)]
				throw new Exception("Parse error: " + errors.join(", "))
			}
		}
	}

	override evaluate() {
		prepare();
		parseResult.evaluate
	}

	override STAlgorithm getSourceElement() {
		algorithm
	}
}