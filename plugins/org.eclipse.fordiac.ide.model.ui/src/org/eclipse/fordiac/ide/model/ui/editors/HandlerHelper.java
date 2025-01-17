/*******************************************************************************
 * Copyright (c) 2021 Primetals Technologies Austria GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bianca Wiesmayr - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.fordiac.ide.model.ui.editors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetwork;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.SubApp;
import org.eclipse.fordiac.ide.model.libraryElement.SubAppType;
import org.eclipse.fordiac.ide.model.ui.actions.OpenListenerManager;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.ui.IEditorPart;

public final class HandlerHelper {
	private HandlerHelper() {
		// do not instantiate this class
	}

	public static GraphicalViewer getViewer(final IEditorPart editor) {
		return editor.getAdapter(GraphicalViewer.class);
	}

	public static CommandStack getCommandStack(final IEditorPart editor) {
		return editor.getAdapter(CommandStack.class);
	}

	public static FBNetwork getFBNetwork(final IEditorPart editor) {
		return editor.getAdapter(FBNetwork.class);
	}

	public static void selectElement(final Object element, final IEditorPart editor) {
		if (null != editor) {
			final GraphicalViewer viewer = getViewer(editor);
			if (null != viewer) {
				selectElement(element, viewer);
			} else {
				// TODO how other editor may want to handle selection
			}
		}
	}

	public static void selectElement(final Object element, final GraphicalViewer viewer) {
		if (viewer != null) {
			final EditPart editPart = (EditPart) viewer.getEditPartRegistry().get(element);
			if (null != editPart) {
				viewer.flush(); // ensure that the viewer is ready
				if (viewer instanceof AdvancedScrollingGraphicalViewer) {
					((AdvancedScrollingGraphicalViewer) viewer).selectAndRevealEditPart(editPart);
				} else {
					viewer.select(editPart);
					viewer.reveal(editPart);
				}
			}
		}
	}

	public static IEditorPart openEditor(final EObject model) {
		return OpenListenerManager.openEditor(model);
	}

	public static IEditorPart openParentEditor(final FBNetworkElement model) {
		final EObject parentModel = model.eContainer().eContainer();  // use eContainer here so that it also works for
		// types
		return OpenListenerManager.openEditor(parentModel);
	}

	public static boolean isEditableSubApp(final SubApp subApp) {
		if ((null == subApp) || (subApp.isTyped())) {
			return false;
		}

		EObject obj = subApp;
		while (obj.eContainer() != null) {
			obj = obj.eContainer();
			if (obj instanceof SubAppType) {
				return false;
			}
		}
		return true;
	}
}
