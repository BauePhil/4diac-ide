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
 *   Fabio Gandolfi - initial implementation and/or documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.ant.ant;

import java.io.File;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.fordiac.ide.export.ExportException;
import org.eclipse.fordiac.ide.export.forte_ng.ForteNgExportFilter;

public class ExportFBs extends Task {

	protected static final String ANT_EXPORT_TASK_DIRECTORY_NAME = "exported_FBs"; //$NON-NLS-1$

	protected String projectNameString;
	protected boolean withSubfolders = false;
	protected IWorkspace workspace;
	protected IProject project;

	public void setProjectName(final String value) {
		projectNameString = value;
	}

	public void setWithSubfolders(final boolean value) {
		withSubfolders = value;
	}

	@Override
	public void execute() throws BuildException {

		if (projectNameString == null) {
			throw new BuildException("Project name not specified!"); //$NON-NLS-1$
		}

		workspace = ResourcesPlugin.getWorkspace();
		project = workspace.getRoot().getProject(projectNameString);
		if (project == null) {
			throw new BuildException("Project named '" + projectNameString + "' not in workspace in Workspace");//$NON-NLS-1$ //$NON-NLS-2$
		}

	}

	// recursively call directories and save fbt Files
	public List<File> getFBsFiles(final List<File> files, final File dir, final String singleFBName,
			final List<String> excludeSubfolder) {
		if (!dir.isDirectory() && dir.getName().endsWith(".fbt")
				&& (singleFBName == null || singleFBName.equals(dir.getName()))) {
			files.add(dir);
			return files;
		}

		if (dir.listFiles() != null) {
			for (final File file : dir.listFiles()) {
				if (!excludeSubfolder.contains(file.getName())) {
					getFBsFiles(files, file, singleFBName, excludeSubfolder);
				}
			}
		}
		return files;
	}

	protected void exportFiles(final List<File> files) {
		if (!files.isEmpty()) {

			final ForteNgExportFilter filter = new ForteNgExportFilter();

			final File folder = new File(ANT_EXPORT_TASK_DIRECTORY_NAME);
			if (!folder.exists()) {
				folder.mkdir();
			}

			for (final File file : files) {

				final IPath location = Path.fromOSString(file.getAbsolutePath());
				final IFile ifile = workspace.getRoot().getFileForLocation(location);
				System.out.println(ifile.getLocation().toString());

				try {
					filter.export(ifile, folder.getPath(), true, null);
				} catch (final ExportException e) {
					throw new BuildException("Could not export: " + e.getMessage());//$NON-NLS-1$
				}
			}
		}
	}

}