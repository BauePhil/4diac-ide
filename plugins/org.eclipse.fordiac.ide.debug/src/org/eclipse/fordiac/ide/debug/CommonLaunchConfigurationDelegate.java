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
package org.eclipse.fordiac.ide.debug;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.fordiac.ide.model.eval.Evaluator;

public abstract class CommonLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	@SuppressWarnings("static-method")
	protected void launch(final Evaluator evaluator, final ILaunchConfiguration configuration, final String mode,
			final ILaunch launch, final IProgressMonitor monitor) throws CoreException {
		if (ILaunchManager.RUN_MODE.equals(mode)) {
			final EvaluatorProcess process = new EvaluatorProcess(configuration.getName(), evaluator, launch);
			process.start();
		} else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
			final EvaluatorDebugTarget debugTarget = new EvaluatorDebugTarget(configuration.getName(), evaluator,
					launch);
			if (LaunchConfigurationAttributes.isStopOnFirstLine(configuration)) {
				debugTarget.getDebugger().getThread(debugTarget.getProcess().getMainThread()).suspend();
			}
			debugTarget.start();
		} else {
			throw new CoreException(Status.error("Illegal launch mode: " + mode)); //$NON-NLS-1$
		}
	}
}