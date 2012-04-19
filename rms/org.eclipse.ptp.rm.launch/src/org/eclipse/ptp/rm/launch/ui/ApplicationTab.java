/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California.
 * This material was produced under U.S. Government contract W-7405-ENG-36
 * for Los Alamos National Laboratory, which is operated by the University
 * of California for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.rm.launch.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.ui.LaunchConfigurationTab;
import org.eclipse.ptp.launch.ui.LaunchImages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteResource;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.launch.RMLaunchPlugin;
import org.eclipse.ptp.rm.launch.RMLaunchUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The Main tab is used to specify the resource manager for the launch, select the project and executable to launch, and specify the
 * location of the executable if it is a remote launch.
 */
public class ApplicationTab extends LaunchConfigurationTab {
	protected class WidgetListener extends SelectionAdapter implements ModifyListener {
		@Override
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == projButton) {
				handleProjectButtonSelected();
			} else if (source == appButton) {
				handleApplicationButtonSelected();
			} else if (source == consoleButton) {
				updateLaunchConfigurationDialog();
			} else if (source == localAppButton) {
				handleLocalApplicationButtonSelected();
				updateLaunchConfigurationDialog();
			} else if (source == browseAppButton) {
				handleBrowseLocalApplicationButtonSelected();
			}
		}
	}

	/**
	 * @since 4.0
	 */
	public static final String TAB_ID = "org.eclipse.ptp.rm.launch.applicationLaunch.applicationTab"; //$NON-NLS-1$

	protected Text projText = null;

	protected Text appText = null;
	protected Text localAppText = null;
	protected Button projButton = null;
	protected Button appButton = null;
	protected Button browseAppButton = null;
	protected Button localAppButton = null;
	protected Button consoleButton = null;
	protected WidgetListener listener = new WidgetListener();
	protected final boolean combinedOutputDefault = true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse .swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		comp.setLayout(new GridLayout());

		Composite mainComp = new Composite(comp, SWT.NONE);
		mainComp.setLayout(createGridLayout(2, false, 0, 0));
		mainComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label projLabel = new Label(mainComp, SWT.NONE);
		projLabel.setText("Project:");
		projLabel.setLayoutData(spanGridData(-1, 2));

		projText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		projText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projText.addModifyListener(listener);

		projButton = createPushButton(mainComp, "&Browse:", null);
		projButton.addSelectionListener(listener);

		createVerticalSpacer(comp, 1);

		Label appLabel = new Label(mainComp, SWT.NONE);
		appLabel.setText("&Application program:");
		appLabel.setLayoutData(spanGridData(-1, 2));

		appText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		appText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		appText.addModifyListener(listener);

		appButton = createPushButton(mainComp, "B&rowse", null);
		appButton.addSelectionListener(listener);

		createVerticalSpacer(mainComp, 2);

		localAppButton = createCheckButton(mainComp, "Copy executable from local filesystem");
		localAppButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		localAppButton.addSelectionListener(listener);

		Label localAppLabel = new Label(mainComp, SWT.NONE);
		localAppLabel.setText("Path to local executable:");
		localAppLabel.setLayoutData(spanGridData(-1, 2));

		localAppText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		localAppText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		localAppText.addModifyListener(listener);

		browseAppButton = createPushButton(mainComp, "Browse", null);
		browseAppButton.addSelectionListener(listener);

		createVerticalSpacer(mainComp, 2);

		consoleButton = createCheckButton(mainComp, "Display output from all processes in a console view");
		consoleButton.setSelection(combinedOutputDefault);
		consoleButton.addSelectionListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 */
	@Override
	public String getId() {
		return TAB_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return LaunchImages.getImage(LaunchImages.IMG_MAIN_TAB);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	@Override
	public String getName() {
		return "Application";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse .debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);

		try {
			projText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING));
			appText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, EMPTY_STRING));
			localAppText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_LOCAL_EXECUTABLE_PATH,
					EMPTY_STRING));
			localAppButton.setSelection(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_COPY_EXECUTABLE, false));
			consoleButton.setSelection(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_CONSOLE, false));
		} catch (CoreException e) {
			setErrorMessage("Cannot read configuration");
		}
		handleLocalApplicationButtonSelected(); // Refreshes the local path
		// textbox enable state.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse .debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);

		String name = getFieldContent(projText.getText());
		if (name != null) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IStatus status = workspace.validateName(name, IResource.PROJECT);
			if (status.isOK()) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
				if (!project.exists()) {
					setErrorMessage(NLS.bind("Project {0} does not exist", new Object[] { name }));
					return false;
				}
				if (!project.isOpen()) {
					setErrorMessage(NLS.bind("Project {0} is closed", new Object[] { name }));
					return false;
				}
			} else {
				setErrorMessage(NLS.bind("Invalid project name: {0}", new Object[] { status.getMessage() }));
				return false;
			}
		}

		name = getFieldContent(appText.getText());
		if (name == null) {
			setErrorMessage("Application program not specified");
			return false;
		}

		if (localAppButton.getSelection()) {
			name = getFieldContent(localAppText.getText());
			if (name == null) {
				setErrorMessage("Local file not specified");
			}
			File file = new File(name);
			if (!file.isAbsolute()) {
				setErrorMessage("Local file path must be absolute");
			}
			if (!file.exists() || !file.isFile()) {
				setErrorMessage("Local file must exist and be valid");
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse .debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, getFieldContent(projText.getText()));
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, getFieldContent(appText.getText()));
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_COPY_EXECUTABLE, localAppButton.getSelection());
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_LOCAL_EXECUTABLE_PATH,
				getFieldContent(localAppText.getText()));
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_CONSOLE, consoleButton.getSelection());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse. debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		IProject project = getDefaultProject(configuration);
		String projectName = null;
		if (project != null) {
			projectName = project.getName();
			String name = getLaunchConfigurationDialog().generateName(projectName);
			configuration.rename(name);
		}

		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, (String) null);
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_COPY_EXECUTABLE, (String) null);
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_LOCAL_EXECUTABLE_PATH, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.debug.ui.AbstractLaunchConfigurationTab# setLaunchConfigurationDialog
	 * (org.eclipse.debug.ui.ILaunchConfigurationDialog)
	 */
	@Override
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
	}

	/**
	 * Create a dialog that allows the user to select a file in the current project.
	 * 
	 * @return selected file
	 */
	protected IResource chooseFile() {
		final IProject project = getProject();
		if (project == null) {
			MessageDialog.openInformation(getShell(), "Please select the project first",
					"Enter project before browsing for program");
			return null;
		}

		WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();
		BaseWorkbenchContentProvider contentProvider = new BaseWorkbenchContentProvider();
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), labelProvider, contentProvider);
		dialog.setTitle("Program selection");
		dialog.setMessage("Choose program to run");
		dialog.setBlockOnOpen(true);
		dialog.setAllowMultiple(false);
		dialog.setInput(project);
		dialog.setValidator(new ISelectionStatusValidator() {
			@Override
			public IStatus validate(Object[] selection) {
				if (selection.length == 0 || !(selection[0] instanceof IFile)) {
					return new Status(IStatus.ERROR, RMLaunchPlugin.getUniqueIdentifier(), IStatus.INFO,
							"Selection must be a file", null);
				}
				try {
					IResource resource = project.findMember(((IFile) selection[0]).getProjectRelativePath());
					if (resource == null || resource.getType() != IResource.FILE) {
						return new Status(IStatus.ERROR, RMLaunchPlugin.getUniqueIdentifier(), IStatus.INFO,
								"Selection must be a file", null);
					}

					return new Status(IStatus.OK, RMLaunchPlugin.getUniqueIdentifier(), IStatus.OK, resource.getName(), null);
				} catch (Exception ex) {
					return new Status(IStatus.ERROR, RMLaunchPlugin.PLUGIN_ID, IStatus.INFO, "Selection must be a file", null);
				}
			}
		});
		if (dialog.open() == Window.OK) {
			return (IResource) dialog.getFirstResult();
		}
		return null;
	}

	/**
	 * Create a dialog that allows the user to choose a project.
	 * 
	 * @return selected project
	 */
	protected IProject chooseProject() {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(),
				WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		dialog.setTitle("Select project");
		dialog.setMessage("Choose the project containing the program you wish to execute");
		dialog.setElements(getWorkspaceRoot().getProjects());

		IProject project = getProject();
		if (project != null) {
			dialog.setInitialSelections(new Object[] { project });
		}
		if (dialog.open() == Window.OK) {
			return (IProject) dialog.getFirstResult();
		}
		return null;
	}

	/**
	 * Get a default project. This is either the project name that has been previously selected, or the project that is currently
	 * selected in the workspace.
	 * 
	 * @param configuration
	 * @return default project
	 */
	protected IProject getDefaultProject(ILaunchConfiguration configuration) {
		String projectName = null;
		try {
			projectName = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
		} catch (CoreException e) {
			return null;
		}
		if (projectName != null && !projectName.equals("")) { //$NON-NLS-1$
			IProject project = getWorkspaceRoot().getProject(projectName);
			if (project != null && project.exists()) {
				return project;
			}
		}

		IWorkbenchPage page = PTPLaunchPlugin.getActivePage();
		if (page != null) {
			ISelection selection = page.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (!ss.isEmpty()) {
					Object obj = ss.getFirstElement();
					if (obj instanceof IAdaptable) {
						Object o = ((IAdaptable) obj).getAdapter(IResource.class);
						if (o instanceof IResource) {
							return ((IResource) o).getProject();
						}
					}
				}
			}

			IEditorPart part = page.getActiveEditor();
			if (part != null) {
				IEditorInput input = part.getEditorInput();
				IFile file = (IFile) input.getAdapter(IFile.class);
				if (file != null) {
					return file.getProject();
				}
			}
		}
		return null;
	}

	/**
	 * Get the IProject the corresponds to the project name that is displayed in the projText control
	 * 
	 * @return project
	 */
	protected IProject getProject() {
		String projectName = projText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		return getWorkspaceRoot().getProject(projectName);
	}

	/**
	 * Allow the user to choose the application to execute
	 * 
	 * Initial path does not work on MacOS X: see bug #153365
	 */
	protected void handleApplicationButtonSelected() {
		String initPath = appText.getText();
		if (initPath.equals(EMPTY_STRING)) {
			final IProject project = getProject();
			if (project == null) {
				MessageDialog.openInformation(getShell(), "Please select the project first",
						"Enter project before browsing for program");
				return;
			}
			IRemoteResource remoteProject = (IRemoteResource) project.getAdapter(IRemoteResource.class);
			if (remoteProject != null) {
				URI location = remoteProject.getActiveLocationURI();
				if (location != null) {
					initPath = location.getPath();
				}
			}
		}

		final IRemoteConnection[] conn = new IRemoteConnection[1];
		try {
			getLaunchConfigurationDialog().run(false, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						conn[0] = RMLaunchUtils.getRemoteConnection(getLaunchConfiguration(), monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e.getCause());
					}
				}
			});
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		}
		if (conn[0] != null) {
			IRemoteUIServices remoteUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(conn[0].getRemoteServices());
			if (remoteUIServices != null) {
				IRemoteUIFileManager fileManager = remoteUIServices.getUIFileManager();
				if (fileManager != null) {
					fileManager.setConnection(conn[0]);
					fileManager.showConnections(false);
					String path = fileManager.browseFile(getShell(), "Select application to execute", initPath, 0);
					if (path != null) {
						appText.setText(path.toString());
					}
				}
			}
		} else {
			MessageDialog.openInformation(getShell(), "Unable to open connection", "Please specify remote connection first");
		}
	}

	protected void handleBrowseLocalApplicationButtonSelected() {
		String initPath = localAppText.getText();
		if (initPath.equals(EMPTY_STRING)) {
			final IProject project = getProject();
			if (project == null || project.getLocationURI() == null) {
				MessageDialog.openInformation(getShell(), "Please select the project first",
						"Enter the project before browsing the program");
				return;
			}
			initPath = project.getLocationURI().getPath();
		}
		IRemoteServices localServices = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		IRemoteUIServices localUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(localServices);
		if (localServices != null && localUIServices != null) {
			IRemoteConnectionManager lconnMgr = localServices.getConnectionManager();
			IRemoteConnection lconn = lconnMgr.getConnection(IRemoteConnectionManager.DEFAULT_CONNECTION_NAME);
			IRemoteUIFileManager localUIFileMgr = localUIServices.getUIFileManager();
			localUIFileMgr.setConnection(lconn);
			String path = localUIFileMgr.browseFile(getShell(), "Select the executable file to be copied", initPath, 0);
			if (path != null) {
				localAppText.setText(path);
			}
		}
	}

	/**
	 * Disables copy of executable from local machine.
	 */
	protected void handleLocalApplicationButtonSelected() {
		localAppText.setEnabled(localAppButton.getSelection());
		browseAppButton.setEnabled(localAppButton.getSelection());
	}

	/**
	 * Allow the user to choose a project
	 */
	protected void handleProjectButtonSelected() {
		IProject project = chooseProject();
		if (project == null) {
			return;
		}

		String projectName = project.getName();
		projText.setText(projectName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.debug.ui.AbstractLaunchConfigurationTab# updateLaunchConfigurationDialog()
	 */
	@Override
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}
}
