/*******************************************************************************
 * Copyright (c) 2022 Primetals Technologies Austria GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Dunja Životin - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.application.properties;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fordiac.ide.gef.properties.AbstractSection;
import org.eclipse.fordiac.ide.gef.widgets.ConnectionDisplayWidget;
import org.eclipse.fordiac.ide.gef.widgets.InternalConnectionsViewer;
import org.eclipse.fordiac.ide.gef.widgets.PinInfoBasicWidget;
import org.eclipse.fordiac.ide.model.Palette.AdapterTypePaletteEntry;
import org.eclipse.fordiac.ide.model.Palette.SubApplicationTypePaletteEntry;
import org.eclipse.fordiac.ide.model.commands.change.ChangeDataTypeCommand;
import org.eclipse.fordiac.ide.model.data.DataType;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.IInterfaceElement;
import org.eclipse.fordiac.ide.model.libraryElement.VarDeclaration;
import org.eclipse.fordiac.ide.model.ui.widgets.ITypeSelectionContentProvider;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public class PinEventInfoSection extends AbstractSection {

	protected PinInfoBasicWidget pinInfo;
	private ConnectionDisplayWidget inConnections;
	private InternalConnectionsViewer outConnections;

	private static final int NUM_OF_CONN_DISPLAYS = 2;
	private static final int PARTS = 2;

	protected Composite leftComposite;
	private Composite middleComposite;
	private Composite rightComposite;

	protected IInterfaceElement type;
	protected TabbedPropertySheetWidgetFactory widgetFactory;

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);
		parent.setLayout(new GridLayout(PARTS, true));
		parent.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		widgetFactory = tabbedPropertySheetPage.getWidgetFactory();

		// Enforcing the layout so the connections would be side by side
		getRightComposite().setLayout(new GridLayout(NUM_OF_CONN_DISPLAYS, true));

		leftComposite = getLeftComposite();
		middleComposite = createSmallComposite(getRightComposite());
		rightComposite = createSmallComposite(getRightComposite());

		pinInfoCreation();
		inConnections = new ConnectionDisplayWidget(widgetFactory, middleComposite, this);
		outConnections = new InternalConnectionsViewer(widgetFactory, rightComposite, this);

	}

	protected void pinInfoCreation() {
		pinInfo = new PinInfoBasicWidget(leftComposite, widgetFactory);
	}

	@Override
	protected EObject getType() {
		return type;
	}

	@Override
	protected Object getInputType(final Object input) {
		if (input instanceof EditPart) {
			type = (IInterfaceElement) ((EditPart) input).getModel(); // Changed from UntypedSubAppEditPart
			return type;
		}
		return null;
	}

	@Override
	public void refresh() {
		final CommandStack commandStackBuffer = commandStack;
		commandStack = null;
		if (null != pinInfo && null != inConnections && null != outConnections && null != type) {
			pinInfo.refresh();
			inConnections.refreshConnectionsViewer(type);
			outConnections.refreshConnectionsViewer(type);
			final FBNetworkElement fb = type.getFBNetworkElement();
			if (fb != null) {
				inConnections.setEditable(true);
				outConnections.setEditable(true);
			}
		}
		commandStack = commandStackBuffer;
	}

	@Override
	protected void setInputCode() {
		pinInfo.disableAllFields();
		inConnections.setEditable(false);
		outConnections.setEditable(false);
	}

	@Override
	protected void setInputInit() {
		if (pinInfo != null) {
			pinInfo.initialize(type, this::executeCommand);
			pinInfo.getTypeSelectionWidget().initialize(type, getTypeSelectionContentProvider(),
					this::handleDataSelectionChanged);
		}
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			final Object input = ((IStructuredSelection) selection).getFirstElement();
			commandStack = getCommandStack(part, input);
			if (null == commandStack) {
				setInputCode();
			}
			setType(input);
			setInputInit();
		}

	}

	protected void handleDataSelectionChanged(final String dataName) {
		final SubApplicationTypePaletteEntry subAppEntry = getTypeLibrary().getBlockTypeLib()
				.getSubAppTypeEntry(dataName);
		final DataType newType = subAppEntry == null ? null : (DataType) subAppEntry.getType();
		if (newType != null) {
			commandStack.execute(new ChangeDataTypeCommand((VarDeclaration) getType(), newType));
		}
	}


	private Composite createSmallComposite(final Composite parent) {
		final Composite composite = widgetFactory.createComposite(parent);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		return composite;
	}

	protected ITypeSelectionContentProvider getTypeSelectionContentProvider() {
		return new TypeSelectionWidgetContentProvider();
	}


	private class TypeSelectionWidgetContentProvider implements ITypeSelectionContentProvider {
		@Override
		public List<DataType> getTypes() {
			return getTypeLibrary().getBlockTypeLib().getAdapterTypesSorted().stream()
					.map(AdapterTypePaletteEntry::getType).collect(Collectors.toList());
		}
	}

}