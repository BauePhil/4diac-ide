/*******************************************************************************
 * Copyright (c) 2009 - 2014 Profactor GmbH, fortiss GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Gerhard Ebenhofer, Thomas Strasser, Alois Zoitl
 *     - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.export.compare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.Saveable;

/**
 * The Class Input.
 */
class Input implements IEditableContent, ITypedElement, IStreamContentAccessor, ISaveablesSource {

	/** The content. */
	private final File fContent;

	/**
	 * Instantiates a new input.
	 *
	 * @param f the f
	 */
	public Input(final File f) {
		fContent = f;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.compare.ITypedElement#getName()
	 */
	@Override
	public String getName() {
		return fContent != null ? fContent.getName() : ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.compare.ITypedElement#getImage()
	 */
	@Override
	public Image getImage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.compare.ITypedElement#getType()
	 */
	@Override
	public String getType() {
		return "cpp"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.compare.IStreamContentAccessor#getContents()
	 */
	@Override
	public InputStream getContents() throws CoreException {
		try {
			return new FileInputStream(fContent);
		} catch (final FileNotFoundException e) {
			Activator.getDefault().logError(e.getMessage(), e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.compare.IEditableContent#isEditable()
	 */
	@Override
	public boolean isEditable() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.compare.IEditableContent#replace(org.eclipse.compare.
	 * ITypedElement, org.eclipse.compare.ITypedElement)
	 */
	@Override
	public ITypedElement replace(final ITypedElement dest, final ITypedElement src) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.compare.IEditableContent#setContent(byte[])
	 */
	@Override
	public void setContent(final byte[] newContent) {
		try (FileOutputStream fo = new FileOutputStream(fContent);) {
			fo.write(newContent);
		} catch (final Exception e) {
			Activator.getDefault().logError(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.ISaveablesSource#getActiveSaveables()
	 */
	@Override
	public Saveable[] getActiveSaveables() {
		return new Saveable[0];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.ISaveablesSource#getSaveables()
	 */
	@Override
	public Saveable[] getSaveables() {
		return new Saveable[0];
	}

}