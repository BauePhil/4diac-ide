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
package org.eclipse.fordiac.ide.model.eval.variable

import java.util.List
import java.util.regex.Pattern
import org.eclipse.fordiac.ide.model.data.ArrayType
import org.eclipse.fordiac.ide.model.data.DataFactory
import org.eclipse.fordiac.ide.model.data.DataType
import org.eclipse.fordiac.ide.model.data.Subrange
import org.eclipse.fordiac.ide.model.eval.value.ArrayValue
import org.eclipse.fordiac.ide.model.eval.value.Value
import org.eclipse.xtend.lib.annotations.Accessors

import static org.eclipse.fordiac.ide.model.eval.variable.VariableOperations.*

import static extension org.eclipse.emf.ecore.util.EcoreUtil.*

class ArrayVariable extends AbstractVariable {
	static final Pattern ARRAY_PATTERN = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")

	@Accessors final DataType elementType
	@Accessors final List<Variable> elements
	@Accessors final ArrayValue value

	new(String name, ArrayType type) {
		super(name, type)
		elementType = if (type.subranges.size > 1)
			newArrayType(type.baseType, type.subranges.tail.map[copy])
		else
			type.baseType
		elements = (type.subranges.head.lowerLimit .. type.subranges.head.upperLimit).map [
			newVariable(it.toString, elementType)
		].toList.immutableCopy
		value = new ArrayValue(type, elements)
	}

	override setValue(Value value) {
		if (value instanceof ArrayValue) {
			if (value.elements.size != elements.size) {
				throw new IllegalArgumentException('''Cannot assign array with different size «value.elements.size» to array of size «elements.size»''')
			}
			elements.forEach[variable, index|variable.value = value.get(index).value]
		} else
			throw new ClassCastException('''Cannot assign value with incompatible type «value.type.name» as «type.name»''')
	}

	override setValue(String value) {
		val trimmed = value.trim
		if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
			throw new IllegalArgumentException("Not a valid array value")
		}
		val inner = trimmed.substring(1, trimmed.length - 1)
		ARRAY_PATTERN.split(inner).forEach[elem, index|
			elements.get(index).value = elem.trim
		]
	}

	override validateValue(String value) {
		val trimmed = value.trim
		if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
			return false
		}
		val inner = trimmed.substring(1, trimmed.length - 1)
		val elementStrings = ARRAY_PATTERN.split(inner)
		for (i : 0 ..< elementStrings.size) {
			if (!elements.get(i).validateValue(elementStrings.get(i))) {
				return false
			}
		}
		return true
	}

	def static ArrayType newArrayType(DataType arrayBaseType, Subrange... arraySubranges) {
		arrayBaseType.newArrayType(arraySubranges as Iterable<Subrange>)
	}

	def static ArrayType newArrayType(DataType arrayBaseType, Iterable<Subrange> arraySubranges) {
		DataFactory.eINSTANCE.createArrayType => [
			baseType = arrayBaseType
			subranges.addAll(arraySubranges)
		]
	}

	def static newSubrange(int lower, int upper) {
		DataFactory.eINSTANCE.createSubrange => [
			lowerLimit = lower
			upperLimit = upper
		]
	}
}