/********************************************************************************
 * Copyright (c) 2008 - 2017 Profactor GmbH, TU Wien ACIN, fortiss GmbH, IBH Systems
 * 				 2018, 2020 Johannes Kepler University
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Gerhard Ebenhofer, Martijn Rooker, Alois Zoitl, Monika Wenger, Jens Reimann,
 *  Waldemar Eisenmenger, Gerd Kainz
 *    - initial API and implementation and/or initial documentation
 *  Martin Melik-Merkumians - adds convenience methods
 *  Alois Zoitl - Changed to a per project Type and Data TypeLibrary
 ********************************************************************************/
package org.eclipse.fordiac.ide.model.typelibrary;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.fordiac.ide.model.Activator;
import org.eclipse.fordiac.ide.model.Palette.DataTypePaletteEntry;
import org.eclipse.fordiac.ide.model.Palette.Palette;
import org.eclipse.fordiac.ide.model.Palette.PaletteEntry;
import org.eclipse.fordiac.ide.model.Palette.PaletteFactory;

public final class TypeLibrary implements TypeLibraryTags {

	// !> Holds type libraries of all open 4diac IDE projects
	private static Map<IProject, TypeLibrary> typeLibraryList = new HashMap<>();

	public static synchronized TypeLibrary getTypeLibrary(IProject proj) {
		return typeLibraryList.computeIfAbsent(proj, TypeLibrary::new);
	}

	public static TypeLibrary getTypeLibraryForPaletteEntry(PaletteEntry entry) {
		return getTypeLibrary(entry.getFile().getProject());
	}

	private final Palette blockTypeLib = PaletteFactory.eINSTANCE.createPalette();
	private final DataTypeLibrary dataTypeLib = new DataTypeLibrary();
	private final IProject project;

	/** An array of palette entry creators */
	private static IPaletteEntryCreator[] paletteCreators = null;

	public static String getTypeNameFromFile(IFile element) {
		return getTypeNameFromFileName(element.getName());
	}

	public static String getTypeNameFromFileName(final String fileName) {
		String name = fileName;
		int index = fileName.lastIndexOf('.');
		if (-1 != index) {
			name = fileName.substring(0, index);
		}
		return name;
	}

	public static PaletteEntry getPaletteEntryForFile(IFile typeFile) {
		TypeLibrary typeLib = TypeLibrary.getTypeLibrary(typeFile.getProject());
		return typeLib.getPaletteEntry(typeFile);
	}

	public PaletteEntry getPaletteEntry(IFile typeFile) {
		EMap<String, ? extends PaletteEntry> typeEntryList = getTypeList(typeFile);
		if (null != typeEntryList) {
			return typeEntryList.get(TypeLibrary.getTypeNameFromFile(typeFile));
		}
		return null;
	}

	public Palette getBlockTypeLib() {
		return blockTypeLib;
	}

	public DataTypeLibrary getDataTypeLibrary() {
		return dataTypeLib;
	}

	private EMap<String, ? extends PaletteEntry> getTypeList(IFile typeFile) {
		String extension = typeFile.getFileExtension();
		if (null != extension) {
			switch (extension.toUpperCase()) {
			case TypeLibraryTags.ADAPTER_TYPE_FILE_ENDING:
				return blockTypeLib.getAdapterTypes();
			case TypeLibraryTags.DEVICE_TYPE_FILE_ENDING:
				return blockTypeLib.getDeviceTypes();
			case TypeLibraryTags.FB_TYPE_FILE_ENDING:
				return blockTypeLib.getFbTypes();
			case TypeLibraryTags.RESOURCE_TYPE_FILE_ENDING:
				return blockTypeLib.getResourceTypes();
			case TypeLibraryTags.SEGMENT_TYPE_FILE_ENDING:
				return blockTypeLib.getSegmentTypes();
			case TypeLibraryTags.SUBAPP_TYPE_FILE_ENDING:
				return blockTypeLib.getSubAppTypes();
			default:
				break;
			}
		}
		return null;
	}

	/**
	 * Instantiates a new fB type library.
	 */
	private TypeLibrary(IProject project) {
		this.project = project;
		loadPaletteFolderMembers(project);
	}

	public static synchronized void loadToolLibrary() {
		IProject toolLibProject = getToolLibProject();
		typeLibraryList.computeIfAbsent(toolLibProject, TypeLibrary::createToolLibrary);
	}

	private static TypeLibrary createToolLibrary(IProject toolLibProject) {
		if (toolLibProject.exists()) {
			// clean-up old links
			try {
				toolLibProject.delete(true, new NullProgressMonitor());
			} catch (CoreException e) {
				Activator.getDefault().logError(e.getMessage(), e);
			}
		}

		createToolLibProject(toolLibProject);

		return new TypeLibrary(toolLibProject);
	}

	private void loadPaletteFolderMembers(IContainer container) {
		IResource[] members;
		try {
			if (!ResourcesPlugin.getWorkspace().isTreeLocked()) {
				container.refreshLocal(IResource.DEPTH_ONE, null);
			}
			members = container.members();

			for (IResource iResource : members) {
				if (iResource instanceof IFolder) {
					loadPaletteFolderMembers((IFolder) iResource);
				}
				if (iResource instanceof IFile) {
					createPaletteEntry((IFile) iResource);
				}
			}
		} catch (CoreException e) {
			Activator.getDefault().logError(e.getMessage(), e);
		}
	}

	/**
	 *
	 * @param palette
	 * @param file
	 * @return
	 */
	public PaletteEntry createPaletteEntry(IFile file) {
		PaletteEntry entry = null;
		for (IPaletteEntryCreator in : getPaletteCreators()) {
			if (in.canHandle(file)) {
				entry = in.createPaletteEntry();
				configurePaletteEntry(entry, file);
				addPaletteEntry(entry);
			}
		}
		return entry;
	}

	private void addPaletteEntry(PaletteEntry entry) {
		if (entry instanceof DataTypePaletteEntry) {
			dataTypeLib.addPaletteEntry((DataTypePaletteEntry) entry);
		} else {
			blockTypeLib.addPaletteEntry(entry);
		}

	}

	/**
	 *
	 */
	private static void setPaletteCreators() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elems = registry
				.getConfigurationElementsFor(org.eclipse.fordiac.ide.model.Activator.PLUGIN_ID, "PaletteEntryCreator"); //$NON-NLS-1$
		int countPaletteCreater = 0;
		paletteCreators = new IPaletteEntryCreator[elems.length];

		for (int i = 0; i < elems.length; i++) {
			IConfigurationElement elem = elems[i];
			try {
				Object object = elem.createExecutableExtension("class"); //$NON-NLS-1$
				if (object instanceof IPaletteEntryCreator) {
					paletteCreators[countPaletteCreater] = (IPaletteEntryCreator) object;
					countPaletteCreater++;
				}
			} catch (CoreException e) {
				Activator.getDefault().logError(e.getMessage(), e);
			}
		}
	}

	private static IPaletteEntryCreator[] getPaletteCreators() {
		if (null == paletteCreators) {
			setPaletteCreators();
		}
		return paletteCreators;
	}

	private static void configurePaletteEntry(PaletteEntry entry, IFile file) {
		entry.setType(null);
		entry.setLabel(TypeLibrary.getTypeNameFromFile(file));
		entry.setFile(file);
	}

	public static void refreshTypeLib(IFile file) {
		TypeLibrary typeLib = TypeLibrary.getTypeLibrary(file.getProject());
		typeLib.refresh();
	}

	private void refresh() {
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
			Activator.getDefault().logError(e.getMessage(), e);
		}

		checkDeletions();
		checkAdditions(project);
	}

	private void checkDeletions() {
		checkDeletionsForTypeGroup(blockTypeLib.getAdapterTypes());
		checkDeletionsForTypeGroup(blockTypeLib.getDeviceTypes());
		checkDeletionsForTypeGroup(blockTypeLib.getFbTypes());
		checkDeletionsForTypeGroup(blockTypeLib.getResourceTypes());
		checkDeletionsForTypeGroup(blockTypeLib.getSegmentTypes());
		checkDeletionsForTypeGroup(blockTypeLib.getSubAppTypes());
	}

	private static void checkDeletionsForTypeGroup(EMap<String, ? extends PaletteEntry> types) {
		types.entrySet().removeIf(e -> (!e.getValue().getFile().exists()));
	}

	private void checkAdditions(IContainer container) {
		try {
			IResource[] members = container.members();

			for (IResource iResource : members) {
				if (iResource instanceof IFolder) {
					checkAdditions((IFolder) iResource);
				}
				if ((iResource instanceof IFile) && (!paletteContainsType((IFile) iResource))) {
					// only add new entry if it does not exist
					createPaletteEntry((IFile) iResource);
				}
			}
		} catch (CoreException e) {
			Activator.getDefault().logError(e.getMessage(), e);
		}

	}

	public boolean paletteContainsType(IFile file) {
		String typeName = getTypeNameFromFile(file);
		return ((null != blockTypeLib.getAdapterTypeEntry(typeName))
				|| (null != blockTypeLib.getDeviceTypeEntry(typeName))
				|| (null != blockTypeLib.getFBTypeEntry(typeName))
				|| (null != blockTypeLib.getResourceTypeEntry(typeName))
				|| (null != blockTypeLib.getSegmentTypeEntry(typeName))
				|| (null != blockTypeLib.getSubAppTypeEntry(typeName)));
	}

	/**
	 * Returns the tool library project.
	 *
	 * @return the tool library project of the 4diac-ide instance
	 */
	private static IProject getToolLibProject() {
		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		return myWorkspaceRoot.getProject(TOOL_LIBRARY_PROJECT_NAME);
	}

	public static IFolder getToolLibFolder() {

		IProject toolLibProject = getToolLibProject();

		if (!toolLibProject.exists()) {
			createToolLibProject(toolLibProject);
		}

		IFolder toolLibFolder = toolLibProject.getFolder(TOOL_LIBRARY_PROJECT_NAME);
		if (!toolLibFolder.exists()) {
			createToolLibLink(toolLibProject);
			toolLibFolder = toolLibProject.getFolder(TOOL_LIBRARY_PROJECT_NAME);
		}

		return toolLibFolder;
	}

	private static void createToolLibProject(IProject toolLibProject) {
		IProgressMonitor progressMonitor = new NullProgressMonitor();

		try {
			toolLibProject.create(progressMonitor);
			toolLibProject.open(progressMonitor);
		} catch (Exception e) {
			Activator.getDefault().logError(e.getMessage(), e);
		}

		createToolLibLink(toolLibProject);
	}

	private static void createToolLibLink(IProject toolLibProject) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		IFolder link = toolLibProject.getFolder(TOOL_LIBRARY_PROJECT_NAME);

		final String typeLibPath = System.getProperty("4diac.typelib.path"); //$NON-NLS-1$

		final IPath location;

		if (typeLibPath != null && !typeLibPath.isEmpty()) {
			location = new Path(typeLibPath);
		} else {
			location = new Path(Platform.getInstallLocation().getURL().getFile() + TypeLibraryTags.TYPE_LIBRARY);
		}
		if (workspace.validateLinkLocation(link, location).isOK()) {
			try {
				link.createLink(location, IResource.NONE, null);
			} catch (Exception e) {
				Activator.getDefault().logError(e.getMessage(), e);
			}
		} else {
			// invalid location, throw an exception or warn user
		}
	}

}
