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
package org.eclipse.fordiac.ide.debug.basic;

import java.util.List;
import java.util.Queue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.fordiac.ide.debug.fb.FBLaunchConfigurationDelegate;
import org.eclipse.fordiac.ide.model.eval.fb.BasicFBEvaluator;
import org.eclipse.fordiac.ide.model.eval.fb.FBEvaluator;
import org.eclipse.fordiac.ide.model.eval.variable.Variable;
import org.eclipse.fordiac.ide.model.libraryElement.BasicFBType;
import org.eclipse.fordiac.ide.model.libraryElement.Event;
import org.eclipse.fordiac.ide.model.libraryElement.FBType;

public class BasicFBLaunchConfigurationDelegate extends FBLaunchConfigurationDelegate {

	@Override
	public FBEvaluator<? extends FBType> createEvaluator(final FBType type, final Queue<Event> queue, final List<Variable> variables)
			throws CoreException {
		return new BasicFBEvaluator((BasicFBType) type, null, variables, queue, null);
	}
}