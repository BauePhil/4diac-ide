/**
 * *******************************************************************************
 * Copyright (c) 2008 - 2018 Profactor GmbH, TU Wien ACIN, fortiss GmbH
 *               2022-2023 Martin Erich Jobst
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *    Gerhard Ebenhofer, Alois Zoitl, Ingo Hegny, Monika Wenger, Martin Jobst
 *      - initial API and implementation and/or initial documentation
 * *******************************************************************************
 */
package org.eclipse.fordiac.ide.model.data.impl;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.fordiac.ide.model.data.AnySCharsType;
import org.eclipse.fordiac.ide.model.data.DataPackage;
import org.eclipse.fordiac.ide.model.data.DataType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Any SChars Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * @generated
 */
public class AnySCharsTypeImpl extends AnyCharsTypeImpl implements AnySCharsType {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected AnySCharsTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DataPackage.Literals.ANY_SCHARS_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isAssignableFrom(final DataType other) {
		return org.eclipse.fordiac.ide.model.data.impl.DataTypeAnnotations.isAssignableFrom(this, other);
	}

} //AnySCharsTypeImpl