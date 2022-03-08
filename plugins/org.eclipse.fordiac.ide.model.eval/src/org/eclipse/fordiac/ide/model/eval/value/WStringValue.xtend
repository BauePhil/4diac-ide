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
package org.eclipse.fordiac.ide.model.eval.value

import org.eclipse.fordiac.ide.model.data.WstringType
import org.eclipse.fordiac.ide.model.datatype.helper.IecTypes.ElementaryTypes

class WStringValue implements AnyStringValue {
	final String value;

	public static final WStringValue DEFAULT = new WStringValue("")

	private new(String value) {
		this.value = value;
	}

	def static toWStringValue(String value) { new WStringValue(value) }

	def static toWStringValue(AnyCharsValue value) { value.toString.toWStringValue }

	override WstringType getType() { ElementaryTypes.WSTRING }

	override equals(Object obj) { if(obj instanceof WStringValue) value == obj.value else false }

	override hashCode() { value.hashCode }

	override toString() { value }
}