/*******************************************************************************
 * Copyright (c) 2023 Primetals Technologies Austria GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alois Zoitl - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.application.editparts;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.fordiac.ide.application.figures.InstanceCommentFigure;
import org.eclipse.fordiac.ide.gef.editparts.AbstractPositionableElementEditPart;
import org.eclipse.fordiac.ide.gef.editparts.FigureCellEditorLocator;
import org.eclipse.fordiac.ide.gef.editparts.TextDirectEditManager;
import org.eclipse.fordiac.ide.gef.figures.BorderedRoundedRectangle;
import org.eclipse.fordiac.ide.gef.figures.RoundedRectangleShadowBorder;
import org.eclipse.fordiac.ide.gef.policies.AbstractViewRenameEditPolicy;
import org.eclipse.fordiac.ide.gef.preferences.DiagramPreferences;
import org.eclipse.fordiac.ide.model.commands.change.ChangeCommentCommand;
import org.eclipse.fordiac.ide.model.commands.delete.DeleteFBNetworkElementCommand;
import org.eclipse.fordiac.ide.model.libraryElement.Comment;
import org.eclipse.fordiac.ide.model.libraryElement.INamedElement;
import org.eclipse.fordiac.ide.model.libraryElement.LibraryElementPackage;
import org.eclipse.fordiac.ide.model.libraryElement.Position;
import org.eclipse.fordiac.ide.model.libraryElement.PositionableElement;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class CommentEditPart extends AbstractPositionableElementEditPart {

	public static final class CommentFigure extends BorderedRoundedRectangle {

		final InstanceCommentFigure comment;

		public CommentFigure() {
			setupFigure();
			setupRootLayout();
			comment = new InstanceCommentFigure();
			comment.setCursor(Cursors.SIZEALL);
			add(comment, new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		}

		public InstanceCommentFigure getComment() {
			return comment;
		}

		private void setupFigure() {
			setOutline(false);
			setCornerDimensions(new Dimension(DiagramPreferences.CORNER_DIM, DiagramPreferences.CORNER_DIM));
			setFillXOR(false);
			setOpaque(false);
			setBorder(new RoundedRectangleShadowBorder());
		}

		private void setupRootLayout() {
			final GridLayout mainLayout = new GridLayout(1, true);
			mainLayout.verticalSpacing = 0;
			mainLayout.horizontalSpacing = 0;
			setLayoutManager(mainLayout);
		}

		public void setCommentText(final String newCommentTest) {
			comment.setText(newCommentTest);
		}

	}

	private class CommentRenameEditPolicy extends AbstractViewRenameEditPolicy {
		@Override
		protected Command getDirectEditCommand(final DirectEditRequest request) {
			if (getHost() instanceof final CommentEditPart ep) {
				final String str = (String) request.getCellEditor().getValue();
				if (!InstanceCommentFigure.EMPTY_COMMENT.equals(str)) {
					return new ChangeCommentCommand(ep.getModel(), str);
				}
			}
			return null;
		}

		@Override
		protected void showCurrentEditValue(final DirectEditRequest request) {
			final String value = (String) request.getCellEditor().getValue();
			getFigure().setCommentText(value);
		}

		@Override
		protected void revertOldEditValue(final DirectEditRequest request) {
			refreshComment();
		}
	}

	@Override
	public Comment getModel() {
		return (Comment) super.getModel();
	}

	@Override
	protected PositionableElement getPositionableElement() {
		return getModel();
	}

	@Override
	public INamedElement getINamedElement() {
		return getModel();
	}

	@Override
	public Label getNameLabel() {
		// we don't have a name label in groups
		return null;
	}

	@Override
	protected void refreshName() {
		// as we don't have a name label we don't want to do anything here
	}

	@Override
	protected IPropertyChangeListener getPreferenceChangeListener() {
		// for now we don't need a preference change listener
		return null;
	}

	@Override
	protected IFigure createFigureForModel() {
		final CommentFigure mainFigure = new CommentFigure();
		mainFigure.setCommentText(getModel().getComment());
		return mainFigure;
	}

	@Override
	public CommentFigure getFigure() {
		return (CommentFigure) super.getFigure();
	}

	@Override
	protected void performDirectEdit() {
		new TextDirectEditManager(this, new FigureCellEditorLocator(getFigure().getComment())) {
			@Override
			protected CellEditor createCellEditorOn(final Composite composite) {
				return new TextCellEditor(composite, SWT.MULTI | SWT.WRAP);
			}

			@Override
			protected void initCellEditor() {
				super.initCellEditor();
				getCellEditor().setValue(getModel().getComment());
			}
		}.show();
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentEditPolicy() {
			@Override
			protected Command createDeleteCommand(final GroupRequest request) {
				return new DeleteFBNetworkElementCommand((Comment) getHost().getModel());
			}
		});
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new CommentRenameEditPolicy());
	}

	@Override
	protected void refreshPosition() {
		if (getParent() != null) {
			final Position position = getModel().getPosition();
			final Point asPoint = position.asPoint();
			final Rectangle bounds = new Rectangle(asPoint, getCommentSize());
			((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), bounds);
		}
	}

	@Override
	protected void refreshComment() {
		getFigure().setCommentText(getModel().getComment());
	}

	@Override
	public <T> T getAdapter(final Class<T> key) {
		if (key == Comment.class) {
			return key.cast(getModel());
		}
		return super.getAdapter(key);
	}

	private Dimension getCommentSize() {
		return new Dimension(getModel().getWidth(), getModel().getHeight());
	}

	@Override
	protected Adapter createContentAdapter() {
		return new AdapterImpl() {
			@Override
			public void notifyChanged(final Notification notification) {
				super.notifyChanged(notification);
				final Object feature = notification.getFeature();
				if (LibraryElementPackage.eINSTANCE.getComment_Width().equals(feature)
						|| LibraryElementPackage.eINSTANCE.getComment_Height().equals(feature)) {
					refreshPosition();
				}
			}
		};
	}

}