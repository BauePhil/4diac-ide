/*******************************************************************************
 * Copyright (c) 2014 - 2017 fortiss GmbH
 *               2019 - 2020 Johannes Kepler University Linz
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Monika Wenger, Alois Zoitl
 *     - initial API and implementation and/or initial documentation
 *   Bianca Wiesmayr
 *     - extract table viewer creation, add initialvalue/arraysize columns
 *******************************************************************************/
package org.eclipse.fordiac.ide.fbtypeeditor.properties;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.fordiac.ide.fbtypeeditor.contentprovider.EventContentProvider;
import org.eclipse.fordiac.ide.fbtypeeditor.contentprovider.EventLabelProvider;
import org.eclipse.fordiac.ide.model.commands.change.ChangeArraySizeCommand;
import org.eclipse.fordiac.ide.model.commands.change.ChangeDataTypeCommand;
import org.eclipse.fordiac.ide.model.commands.change.ChangeValueCommand;
import org.eclipse.fordiac.ide.model.commands.create.WithCreateCommand;
import org.eclipse.fordiac.ide.model.commands.delete.DeleteWithCommand;
import org.eclipse.fordiac.ide.model.data.DataType;
import org.eclipse.fordiac.ide.model.libraryElement.Event;
import org.eclipse.fordiac.ide.model.libraryElement.FBType;
import org.eclipse.fordiac.ide.model.libraryElement.SubAppType;
import org.eclipse.fordiac.ide.model.libraryElement.VarDeclaration;
import org.eclipse.fordiac.ide.model.libraryElement.With;
import org.eclipse.fordiac.ide.model.ui.widgets.ITypeSelectionContentProvider;
import org.eclipse.fordiac.ide.ui.FordiacMessages;
import org.eclipse.fordiac.ide.ui.widget.TableWidgetFactory;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class DataInterfaceElementSection extends AdapterInterfaceElementSection {
	private Text arraySizeText;
	private Text initValueText;
	private TableViewer withEventsViewer;
	private Group eventComposite;

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);
		createDataSection(getLeftComposite());
		createEventSection(getRightComposite());
	}

	private void createDataSection(final Composite parent) {
		getWidgetFactory().createCLabel(parent, FordiacMessages.ArraySize + ":"); //$NON-NLS-1$
		arraySizeText = createGroupText(parent, true);
		arraySizeText.addModifyListener(e -> {
			removeContentAdapter();
			executeCommand(new ChangeArraySizeCommand((VarDeclaration) type, arraySizeText.getText()));
			addContentAdapter();
		});
		getWidgetFactory().createCLabel(parent, FordiacMessages.InitialValue + ":"); //$NON-NLS-1$
		initValueText = createGroupText(parent, true);
		initValueText.addModifyListener(e -> {
			removeContentAdapter();
			executeCommand(new ChangeValueCommand((VarDeclaration) type, initValueText.getText()));
			addContentAdapter();
		});
	}

	private void createEventSection(final Composite parent) {
		eventComposite = getWidgetFactory().createGroup(parent, FordiacMessages.With);
		eventComposite.setLayout(new GridLayout(1, false));
		eventComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		withEventsViewer = TableWidgetFactory.createPropertyTableViewer(eventComposite, SWT.CHECK);
		withEventsViewer.setContentProvider(new EventContentProvider());
		withEventsViewer.setLabelProvider(new EventLabelProvider());

		final Table tableWith = withEventsViewer.getTable();
		configureTableLayout(tableWith);
		tableWith.addListener(SWT.Selection, event -> {
			if (event.detail == SWT.CHECK) {
				final TableItem checkedItem = (TableItem) event.item;
				final Event e = (Event) checkedItem.getData();
				final With with = e.getWith().stream().filter(w -> w.getVariables().equals(getType())).findFirst()
						.orElse(null);
				if (checkedItem.getChecked()) {
					if (null == with) {
						executeCommand(new WithCreateCommand(e, getType()));
					}
				} else if (null != with) {
					executeCommand(new DeleteWithCommand(with));
				}
			}
		});
	}

	private static void configureTableLayout(final Table tableWith) {
		final TableColumn column1 = new TableColumn(tableWith, SWT.LEFT);
		column1.setText(FordiacMessages.Event);
		final TableColumn column2 = new TableColumn(tableWith, SWT.LEFT);
		column2.setText(FordiacMessages.DataType);
		final TableColumn column3 = new TableColumn(tableWith, SWT.LEFT);
		column3.setText(FordiacMessages.Comment);
		final TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(20, 100));
		layout.addColumnData(new ColumnWeightData(20, 70));
		layout.addColumnData(new ColumnWeightData(20, 100));
		tableWith.setLayout(layout);
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		Assert.isTrue(selection instanceof IStructuredSelection);
		// hide with part for sub app type events
		eventComposite.setVisible(!(getType().eContainer().eContainer() instanceof SubAppType));
		if (null == commandStack) { // disable all field
			arraySizeText.setEnabled(false);
			initValueText.setEnabled(false);
			withEventsViewer.setInput(null);
			Arrays.stream(withEventsViewer.getTable().getItems()).forEach(item -> item.setGrayed(true));
		}
	}

	@Override
	protected VarDeclaration getType() {
		return (VarDeclaration) super.getType();
	}

	@Override
	public void refresh() {
		super.refresh();
		final CommandStack commandStackBuffer = commandStack;
		commandStack = null;
		if (null != type) {
			arraySizeText.setText(0 >= getType().getArraySize() ? "" : (Integer.toString((getType()).getArraySize()))); //$NON-NLS-1$
			initValueText.setText(null == getType().getValue() ? "" : getType().getValue().getValue()); //$NON-NLS-1$
			if (getType().eContainer().eContainer() instanceof FBType) {
				eventComposite.setVisible(true);
				withEventsViewer.setInput(getType());
				Arrays.stream(withEventsViewer.getTable().getItems()).forEach(item -> item.setChecked(false));
				getType().getWiths().stream().map(with -> withEventsViewer.testFindItem(with.eContainer()))
						.filter(TableItem.class::isInstance).forEach(item -> ((TableItem) item).setChecked(true));
			} else {
				eventComposite.setVisible(false);
			}
		}
		commandStack = commandStackBuffer;
	}

	@Override
	protected void handleDataSelectionChanged(String dataName) {
		final DataType newType = getDataTypeLib().getTypeIfExists(dataName);
		if (newType != null) {
			commandStack.execute(new ChangeDataTypeCommand((VarDeclaration) getType(), newType));
		}
	}

	@Override
	protected ITypeSelectionContentProvider getTypeSelectionContentProvider() {
		return new ITypeSelectionContentProvider() {
			@Override
			public List<DataType> getTypes() {
				return getDataTypeLib().getDataTypesSorted();
			}
		};
	}
}
