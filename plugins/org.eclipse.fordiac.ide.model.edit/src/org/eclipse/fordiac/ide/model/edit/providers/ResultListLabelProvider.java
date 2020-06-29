/*******************************************************************************
 * Copyright (c) 2020 Johannes Kepler University Linz
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Daniel Lindhuber - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.fordiac.ide.model.edit.providers;

import org.eclipse.fordiac.ide.model.Palette.PaletteEntry;
import org.eclipse.fordiac.ide.model.libraryElement.BasicFBType;
import org.eclipse.fordiac.ide.model.libraryElement.CompositeFBType;
import org.eclipse.fordiac.ide.model.libraryElement.SimpleFBType;
import org.eclipse.fordiac.ide.model.libraryElement.SubAppType;
import org.eclipse.fordiac.ide.ui.imageprovider.FordiacImage;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

public class ResultListLabelProvider extends LabelProvider implements IStyledLabelProvider {

	@Override
	public StyledString getStyledText(Object element) {
		StyledString styledString = null;
		if (element instanceof PaletteEntry) {
			PaletteEntry entry = (PaletteEntry) element;
			styledString = new StyledString(entry.getLabel());
			styledString.append(" - " + entry.getType().getComment(), //$NON-NLS-1$
					StyledString.QUALIFIER_STYLER);
		} else {
			styledString = new StyledString(element.toString());
		}
		return styledString;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof PaletteEntry) {
			PaletteEntry entry = (PaletteEntry) element;
			if (entry.getType() instanceof SubAppType) {
				return FordiacImage.ICON_SUB_APP.getImage();
			} else if (entry.getType() instanceof BasicFBType) {
				return FordiacImage.ICON_BASIC_FB.getImage();
			} else if (entry.getType() instanceof SimpleFBType) {
				return FordiacImage.ICON_SIMPLE_FB.getImage();
			} else if (entry.getType() instanceof CompositeFBType) {
				return FordiacImage.ICON_COMPOSITE_FB.getImage();
			} else {
				return FordiacImage.ICON_SIFB.getImage();
			}
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		return (element instanceof PaletteEntry)
				? String.format("%s - %s", getStyledText(element).toString(),
						((PaletteEntry) element).getFile().getFullPath())
				: "-";
	}

}
