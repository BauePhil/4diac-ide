/**
 * *******************************************************************************
 * Copyright (c) 2022 Primetals Technologies GmbH,
 *               2022 Martin Erich Jobst
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *    Martin Jobst, Martin Melik Merkumians
 *      - initial API and implementation and/or initial documentation
 * *******************************************************************************
 */
package org.eclipse.fordiac.ide.structuredtextcore.stcore.impl;

import java.util.Collection;
import java.util.Map;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fordiac.ide.model.libraryElement.INamedElement;

import org.eclipse.fordiac.ide.structuredtextcore.stcore.STBuiltinFeature;
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STBuiltinFeatureExpression;
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STCallArgument;
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STCorePackage;
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STExpression;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>ST Builtin Feature Expression</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fordiac.ide.structuredtextcore.stcore.impl.STBuiltinFeatureExpressionImpl#getFeature <em>Feature</em>}</li>
 *   <li>{@link org.eclipse.fordiac.ide.structuredtextcore.stcore.impl.STBuiltinFeatureExpressionImpl#isCall <em>Call</em>}</li>
 *   <li>{@link org.eclipse.fordiac.ide.structuredtextcore.stcore.impl.STBuiltinFeatureExpressionImpl#getParameters <em>Parameters</em>}</li>
 * </ul>
 *
 * @generated
 */
public class STBuiltinFeatureExpressionImpl extends STExpressionImpl implements STBuiltinFeatureExpression {
	/**
	 * The default value of the '{@link #getFeature() <em>Feature</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeature()
	 * @generated
	 * @ordered
	 */
	protected static final STBuiltinFeature FEATURE_EDEFAULT = STBuiltinFeature.THIS;

	/**
	 * The cached value of the '{@link #getFeature() <em>Feature</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeature()
	 * @generated
	 * @ordered
	 */
	protected STBuiltinFeature feature = FEATURE_EDEFAULT;

	/**
	 * The default value of the '{@link #isCall() <em>Call</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isCall()
	 * @generated
	 * @ordered
	 */
	protected static final boolean CALL_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isCall() <em>Call</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isCall()
	 * @generated
	 * @ordered
	 */
	protected boolean call = CALL_EDEFAULT;

	/**
	 * The cached value of the '{@link #getParameters() <em>Parameters</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParameters()
	 * @generated
	 * @ordered
	 */
	protected EList<STCallArgument> parameters;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected STBuiltinFeatureExpressionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return STCorePackage.Literals.ST_BUILTIN_FEATURE_EXPRESSION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public STBuiltinFeature getFeature() {
		return feature;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFeature(STBuiltinFeature newFeature) {
		STBuiltinFeature oldFeature = feature;
		feature = newFeature == null ? FEATURE_EDEFAULT : newFeature;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, STCorePackage.ST_BUILTIN_FEATURE_EXPRESSION__FEATURE, oldFeature, feature));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isCall() {
		return call;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCall(boolean newCall) {
		boolean oldCall = call;
		call = newCall;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, STCorePackage.ST_BUILTIN_FEATURE_EXPRESSION__CALL, oldCall, call));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<STCallArgument> getParameters() {
		if (parameters == null) {
			parameters = new EObjectContainmentEList<STCallArgument>(STCallArgument.class, this, STCorePackage.ST_BUILTIN_FEATURE_EXPRESSION__PARAMETERS);
		}
		return parameters;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public INamedElement getResultType() {
		return org.eclipse.fordiac.ide.structuredtextcore.stcore.impl.ExpressionAnnotations.getResultType(this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public INamedElement getDeclaredResultType() {
		return org.eclipse.fordiac.ide.structuredtextcore.stcore.impl.ExpressionAnnotations.getDeclaredResultType(this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Map<INamedElement, STExpression> getMappedInputArguments() {
		return org.eclipse.fordiac.ide.structuredtextcore.stcore.impl.ExpressionAnnotations.getMappedInputArguments(this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Map<INamedElement, STExpression> getMappedOutputArguments() {
		return org.eclipse.fordiac.ide.structuredtextcore.stcore.impl.ExpressionAnnotations.getMappedOutputArguments(this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Map<INamedElement, STExpression> getMappedInOutArguments() {
		return org.eclipse.fordiac.ide.structuredtextcore.stcore.impl.ExpressionAnnotations.getMappedInOutArguments(this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case STCorePackage.ST_BUILTIN_FEATURE_EXPRESSION__PARAMETERS:
				return ((InternalEList<?>)getParameters()).basicRemove(otherEnd, msgs);
			default:
				return super.eInverseRemove(otherEnd, featureID, msgs);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case STCorePackage.ST_BUILTIN_FEATURE_EXPRESSION__FEATURE:
				return getFeature();
			case STCorePackage.ST_BUILTIN_FEATURE_EXPRESSION__CALL:
				return isCall();
			case STCorePackage.ST_BUILTIN_FEATURE_EXPRESSION__PARAMETERS:
				return getParameters();
			default:
				return super.eGet(featureID, resolve, coreType);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case STCorePackage.ST_BUILTIN_FEATURE_EXPRESSION__FEATURE:
				setFeature((STBuiltinFeature)newValue);
				return;
			case STCorePackage.ST_BUILTIN_FEATURE_EXPRESSION__CALL:
				setCall((Boolean)newValue);
				return;
			case STCorePackage.ST_BUILTIN_FEATURE_EXPRESSION__PARAMETERS:
				getParameters().clear();
				getParameters().addAll((Collection<? extends STCallArgument>)newValue);
				return;
			default:
				super.eSet(featureID, newValue);
				return;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case STCorePackage.ST_BUILTIN_FEATURE_EXPRESSION__FEATURE:
				setFeature(FEATURE_EDEFAULT);
				return;
			case STCorePackage.ST_BUILTIN_FEATURE_EXPRESSION__CALL:
				setCall(CALL_EDEFAULT);
				return;
			case STCorePackage.ST_BUILTIN_FEATURE_EXPRESSION__PARAMETERS:
				getParameters().clear();
				return;
			default:
				super.eUnset(featureID);
				return;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case STCorePackage.ST_BUILTIN_FEATURE_EXPRESSION__FEATURE:
				return feature != FEATURE_EDEFAULT;
			case STCorePackage.ST_BUILTIN_FEATURE_EXPRESSION__CALL:
				return call != CALL_EDEFAULT;
			case STCorePackage.ST_BUILTIN_FEATURE_EXPRESSION__PARAMETERS:
				return parameters != null && !parameters.isEmpty();
			default:
				return super.eIsSet(featureID);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (feature: "); //$NON-NLS-1$
		result.append(feature);
		result.append(", call: "); //$NON-NLS-1$
		result.append(call);
		result.append(')');
		return result.toString();
	}

} //STBuiltinFeatureExpressionImpl