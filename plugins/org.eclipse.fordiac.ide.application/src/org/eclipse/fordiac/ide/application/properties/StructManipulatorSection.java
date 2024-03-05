/*******************************************************************************
 * Copyright (c) 2020, 2023 Johannes Kepler University Linz
 *                          Primetals Technologies Austria GmbH
 *                          Martin Erich Jobst
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bianca Wiesmayr - create dedicated section for StructManipulator to improve
 *                     the usability of the struct handling
 *   Lukas Wais - reworked tree structure
 *   Michael Oberlehner  - refactored tree structure
 *   Sebastian Hollersbacher - changed DropDownBox to autocompletion Textfield
 *                             for better usability
 *   Martin Erich Jobst - refactored type proposals and add support for FQN
 *******************************************************************************/
package org.eclipse.fordiac.ide.application.properties;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fordiac.ide.application.Messages;
import org.eclipse.fordiac.ide.application.editparts.AbstractStructManipulatorEditPart;
import org.eclipse.fordiac.ide.application.editparts.StructInterfaceEditPart;
import org.eclipse.fordiac.ide.gef.properties.AbstractSection;
import org.eclipse.fordiac.ide.gef.widgets.TypeSelectionWidget;
import org.eclipse.fordiac.ide.model.AbstractStructTreeNode;
import org.eclipse.fordiac.ide.model.CheckableStructTree;
import org.eclipse.fordiac.ide.model.StructTreeContentProvider;
import org.eclipse.fordiac.ide.model.StructTreeLabelProvider;
import org.eclipse.fordiac.ide.model.commands.change.ChangeStructCommand;
import org.eclipse.fordiac.ide.model.commands.create.AddNewImportCommand;
import org.eclipse.fordiac.ide.model.data.StructuredType;
import org.eclipse.fordiac.ide.model.datatype.helper.IecTypes.GenericTypes;
import org.eclipse.fordiac.ide.model.helpers.ImportHelper;
import org.eclipse.fordiac.ide.model.helpers.PackageNameHelper;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.LibraryElement;
import org.eclipse.fordiac.ide.model.libraryElement.StructManipulator;
import org.eclipse.fordiac.ide.model.libraryElement.VarDeclaration;
import org.eclipse.fordiac.ide.model.ui.nat.StructuredTypeSelectionTreeContentProvider;
import org.eclipse.fordiac.ide.model.ui.widgets.OpenStructMenu;
import org.eclipse.fordiac.ide.model.ui.widgets.StructuredTypeSelectionContentProvider;
import org.eclipse.fordiac.ide.ui.FordiacMessages;
import org.eclipse.fordiac.ide.ui.editors.EditorUtils;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class StructManipulatorSection extends AbstractSection implements CommandStackEventListener {
	protected TypeSelectionWidget typeSelectionWidget;

	protected CLabel muxLabel;
	protected TreeViewer memberVarViewer;
	protected boolean initTree = true;

	@Override
	protected FBNetworkElement getInputType(final Object input) {
		if (input instanceof final AbstractStructManipulatorEditPart structManEP) {
			return structManEP.getModel();
		}
		if (input instanceof final StructManipulator structMan) {
			return structMan;
		}
		if (input instanceof final StructInterfaceEditPart structIEEP) {
			return structIEEP.getModel().getFBNetworkElement();
		}
		return null;
	}

	@Override
	protected StructManipulator getType() {
		if (type instanceof final StructManipulator structMan) {
			return structMan;
		}
		return null;
	}

	private void createStructSelector(final Composite composite) {
		final Composite structComp = getWidgetFactory().createComposite(composite);
		structComp.setLayout(new GridLayout(2, false));
		structComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		muxLabel = getWidgetFactory().createCLabel(structComp, Messages.StructManipulatorSection_STRUCTURED_TYPE);

		typeSelectionWidget = new TypeSelectionWidget(getWidgetFactory(), this::handleStructSelectionChanged);
		typeSelectionWidget.createControls(structComp);
		typeSelectionWidget.setEditable(true);
	}

	protected void refreshStructTypeTable() {
		memberVarViewer.setInput(getType());
	}

	protected void handleStructSelectionChanged(final String newStructName) {
		if (null != getType() && newStructSelected(newStructName)) {
			final StructuredType packageStruct = ImportHelper
					.resolveImport(PackageNameHelper.extractPlainTypeName(newStructName), getType(), name -> {
						final StructuredType temp = getDataTypeLib().getStructuredType(name);
						return GenericTypes.isAnyType(temp) ? null : temp;
					}, name -> null);

			if (packageStruct == null
					&& EcoreUtil.getRootContainer(getType()) instanceof final LibraryElement libraryElement) {
				commandStack.execute(new AddNewImportCommand(libraryElement, newStructName));
			}

			final StructuredType newStruct = getDataTypeLib().getStructuredType(newStructName);
			final ChangeStructCommand cmd = new ChangeStructCommand(getType(), newStruct);
			commandStack.execute(cmd);
			updateStructManipulatorFB(cmd.getNewMux());
		}
	}

	public boolean newStructSelected(final String newStructName) {
		return !newStructName.equalsIgnoreCase(PackageNameHelper.getFullTypeName(getType().getStructType()))
				&& getDataTypeLib().getStructuredType(newStructName) != null;
	}

	protected static void updateStructManipulatorFB(final StructManipulator newMux) {
		final EditorPart activeEditor = (EditorPart) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActiveEditor();
		final GraphicalViewer viewer = activeEditor.getAdapter(GraphicalViewer.class);
		if (null != viewer) {
			viewer.flush();
			EditorUtils.refreshPropertySheetWithSelection(activeEditor, viewer,
					viewer.getEditPartRegistry().get(newMux));
		}
	}

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);
		createStructSelector(parent);
		final Group memberVarGroup = getWidgetFactory().createGroup(parent,
				Messages.StructManipulatorSection_Contained_variables);
		createMemberVariableViewer(memberVarGroup);
		memberVarGroup.setLayout(new GridLayout(1, true));
		memberVarGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	private void createMemberVariableViewer(final Composite parent) {
		memberVarViewer = createTreeViewer(parent);
		configureTreeLayout(memberVarViewer);
		memberVarViewer.setContentProvider(new StructTreeContentProvider());
		memberVarViewer.setLabelProvider(new StructTreeLabelProvider());
		GridLayoutFactory.fillDefaults().generateLayout(parent);

		createContextMenu(memberVarViewer.getControl());
	}

	@SuppressWarnings("static-method") // allow subclasses to provide different treeviewers
	protected TreeViewer createTreeViewer(final Composite parent) {
		return new TreeViewer(parent);
	}

	private void createContextMenu(final Control ctrl) {
		final Menu openEditorMenu = new Menu(memberVarViewer.getTree());
		final MenuItem openItem = new MenuItem(openEditorMenu, SWT.NONE);
		openItem.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final StructuredType sel = getSelectedStructuredType();
				if (sel != null) {
					OpenStructMenu.openStructEditor(sel.getTypeEntry().getFile());
				}
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				widgetSelected(e);
			}
		});
		openItem.setText(FordiacMessages.OPEN_TYPE_EDITOR_MESSAGE);

		openEditorMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuShown(final MenuEvent e) {
				final StructuredType type = getSelectedStructuredType();
				openItem.setEnabled((null != type) && !type.getName().contentEquals("ANY_STRUCT")); //$NON-NLS-1$
			}

			@Override
			public void menuHidden(final MenuEvent e) {
				// nothing to be done here
			}
		});
		ctrl.setMenu(openEditorMenu);
	}

	private StructuredType getSelectedStructuredType() {
		final ITreeSelection selection = memberVarViewer.getStructuredSelection();
		if (!selection.isEmpty()) {
			final AbstractStructTreeNode selected = (AbstractStructTreeNode) selection.getFirstElement();
			final VarDeclaration varDecl = selected.getVariable();
			if (varDecl.getType() instanceof final StructuredType structType) {
				return structType;
			}
		}
		return null;
	}

	private static void configureTreeLayout(final TreeViewer viewer) {
		final TreeViewerColumn variableName = new TreeViewerColumn(viewer, SWT.LEFT);
		final TreeViewerColumn variableType = new TreeViewerColumn(viewer, SWT.LEFT);
		final TreeViewerColumn comment = new TreeViewerColumn(viewer, SWT.LEFT);
		viewer.getTree().setHeaderVisible(true);
		variableName.getColumn().setResizable(true);
		variableType.getColumn().setResizable(true);

		variableName.getColumn().setText(Messages.StructManipulatorSection_MEMBERVAR_COLUMN_NAME);
		variableType.getColumn().setText(Messages.StructManipulatorSection_MEMBERVAR_COLUMN_TYPE);
		comment.getColumn().setText(Messages.StructManipulatorSection_MEMBERVAR_COLUMN_COMMENT);
		variableName.getColumn().setWidth(200);
		variableType.getColumn().setWidth(100);
		comment.getColumn().setWidth(800);
	}

	@Override
	public void refresh() {
		if ((null != getType()) && (null != getType().getFbNetwork()) && !blockRefresh) {
			refreshStructTypeTable();
		}
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		if (commandStack != null) {
			commandStack.removeCommandStackEventListener(this);
		}
		Assert.isTrue(selection instanceof IStructuredSelection);
		final Object input = ((IStructuredSelection) selection).getFirstElement();

		commandStack = getCommandStack(part, input);
		if (null == commandStack) { // disable all fields
			muxLabel.setEnabled(false);
			memberVarViewer.setInput(null);
		}

		setType(input);
		if (initTree) {
			initTree(getType(), memberVarViewer);
		}

		typeSelectionWidget.initialize(getType(), StructuredTypeSelectionContentProvider.INSTANCE,
				StructuredTypeSelectionTreeContentProvider.INSTANCE);

		if (commandStack != null) {
			commandStack.addCommandStackEventListener(this);
		}
	}

	public void initTree(final StructManipulator manipulator, final TreeViewer viewer) {
		final StructuredType struct = manipulator.getTypeEntry().getTypeLibrary().getDataTypeLibrary()
				.getStructuredType(PackageNameHelper.getFullTypeName(manipulator.getStructType()));

		final CheckableStructTree tree;
		if (viewer != null) {
			tree = new CheckableStructTree(manipulator, struct, viewer);
		} else {
			tree = new CheckableStructTree(manipulator, struct);
		}

		((StructTreeContentProvider) memberVarViewer.getContentProvider()).setRoot(tree.getRoot());
	}

	@Override
	public void dispose() {
		super.dispose();
		if (commandStack != null) {
			commandStack.removeCommandStackEventListener(this);
		}
	}

	@Override
	public void stackChanged(final CommandStackEvent event) {
		if (event.getDetail() == CommandStack.POST_UNDO || event.getDetail() == CommandStack.POST_REDO) {
			final Command command = event.getCommand();
			if (command instanceof final ChangeStructCommand cmd) {
				if (cmd.getOldMux() == getType() || cmd.getNewMux() == getType()) {
					if (event.getDetail() == CommandStack.POST_UNDO) {
						updateStructManipulatorFB(cmd.getOldMux());
					} else if (event.getDetail() == CommandStack.POST_REDO) {
						updateStructManipulatorFB(cmd.getNewMux());
					}
				}
				refreshStructTypeTable();
			}
		}
	}

	@Override
	protected void setInputCode() {
		// Currently nothing needs to be done here
	}

	@Override
	protected void setInputInit() {
		// Currently nothing needs to be done here
	}
}
