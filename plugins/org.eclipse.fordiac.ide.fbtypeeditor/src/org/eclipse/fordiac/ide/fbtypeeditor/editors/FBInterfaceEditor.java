/*******************************************************************************
 * Copyright (c) 2011 - 2017 Profactor GmbH, TU Wien ACIN, fortiss GmbH
 * 				 2019 Johannes Kepler University
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Gerhard Ebenhofer, Alois Zoitl, Monika Wenger
 *     - initial API and implementation and/or initial documentation
 *   Alois Zoitl - inherited FBInterface editor from the common diagram editor to
 *   				to reduce code duplication and more common look and feel
 *******************************************************************************/
package org.eclipse.fordiac.ide.fbtypeeditor.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fordiac.ide.fbtypeeditor.FBInterfacePaletteFactory;
import org.eclipse.fordiac.ide.fbtypeeditor.FBTypeEditDomain;
import org.eclipse.fordiac.ide.fbtypeeditor.contentprovider.InterfaceContextMenuProvider;
import org.eclipse.fordiac.ide.fbtypeeditor.editparts.FBInterfaceEditPartFactory;
import org.eclipse.fordiac.ide.gef.DiagramEditorWithFlyoutPalette;
import org.eclipse.fordiac.ide.model.Palette.Palette;
import org.eclipse.fordiac.ide.model.libraryElement.AutomationSystem;
import org.eclipse.fordiac.ide.model.libraryElement.FBType;
import org.eclipse.fordiac.ide.model.libraryElement.InterfaceList;
import org.eclipse.fordiac.ide.model.typelibrary.TypeLibrary;
import org.eclipse.fordiac.ide.typemanagement.FBTypeEditorInput;
import org.eclipse.fordiac.ide.ui.imageprovider.FordiacImage;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

public class FBInterfaceEditor extends DiagramEditorWithFlyoutPalette implements IFBTEditorPart {

	private CommandStack commandStack;
	private FBType fbType;

	private PaletteRoot paletteRoot;
	private Palette palette;

	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
		setInput(input);
		if (input instanceof FBTypeEditorInput) {
			FBTypeEditorInput untypedInput = (FBTypeEditorInput) input;
			fbType = untypedInput.getContent();
			EObject group = untypedInput.getPaletteEntry().getGroup();
			while (group.eContainer() != null) {
				group = group.eContainer();
			}
			if (group instanceof Palette) {
				palette = (Palette) group;
			}
			if (null == palette) {
				palette = TypeLibrary.getInstance().getPalette();
			}
		}
		super.init(site, input);
		setPartName("Interface");
		setTitleImage(FordiacImage.ICON_INTERFACE_EDITOR.getImage());
	}

	@Override
	protected void setModel(IEditorInput input) {
		super.setModel(input);
		setEditDomain(new FBTypeEditDomain(this, commandStack));
	}

	@Override
	protected void createActions() {
		ActionRegistry registry = getActionRegistry();
		InterfaceContextMenuProvider.createInterfaceEditingActions(this, registry, getModel());
		super.createActions();
	}

	@Override
	protected EditPartFactory getEditPartFactory() {
		return new FBInterfaceEditPartFactory(this, palette, getZoomManger());
	}

	@Override
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
		super.selectionChanged(part, selection);
		updateActions(getSelectionActions());
	}

	@Override
	protected PaletteRoot getPaletteRoot() {
		if (null == paletteRoot) {
			paletteRoot = FBInterfacePaletteFactory.createPalette(palette);
		}
		return paletteRoot;
	}

	protected Palette getPalette() {
		return palette;
	}

	@Override
	public void doSave(final IProgressMonitor monitor) {
		// currently nothing needs to be done here
	}

	@Override
	public boolean outlineSelectionChanged(Object selectedElement) {
		Object editpart = getGraphicalViewer().getEditPartRegistry().get(selectedElement);
		getGraphicalViewer().flush();
		if (editpart instanceof EditPart && ((EditPart) editpart).isSelectable()) {
			getGraphicalViewer().select((EditPart) editpart);
			return true;
		}
		return (selectedElement instanceof InterfaceList);
	}

	@Override
	public void setCommonCommandStack(CommandStack commandStack) {
		this.commandStack = commandStack;
	}

	@Override
	protected FlyoutPreferences getPalettePreferences() {
		return FBInterfacePaletteFactory.PALETTE_PREFERENCES;
	}

	/**
	 * Override so that we can add a template transferdragsourcelistener for drag
	 * and drop
	 */
	@Override
	protected PaletteViewerProvider createPaletteViewerProvider() {
		return new PaletteViewerProvider(getEditDomain()) {
			@Override
			protected void configurePaletteViewer(final PaletteViewer viewer) {
				super.configurePaletteViewer(viewer);
				viewer.addDragSourceListener(new TemplateTransferDragSourceListener(viewer));
			}
		};
	}

	@Override
	public FBType getModel() {
		return fbType;
	}

	@Override
	protected ContextMenuProvider getContextMenuProvider(ScrollingGraphicalViewer viewer, ZoomManager zoomManager) {
		return new InterfaceContextMenuProvider(viewer, zoomManager, getActionRegistry());
	}

	@Override
	protected TransferDropTargetListener createTransferDropTargetListener() {
		// we don't need an additional transferdroptarget listener
		return null;
	}

	@Override
	public AutomationSystem getSystem() {
		return null; // this is currently needed as the base class is targeted for system editors
	}

	@Override
	public void doSaveAs() {
		// nothing to do here
	}

}
