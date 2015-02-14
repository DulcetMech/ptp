/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.sdm.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.internal.debug.sdm.core.SDMDebugCorePlugin;
import org.eclipse.ptp.internal.debug.sdm.core.SDMLaunchConfigurationConstants;
import org.eclipse.ptp.internal.debug.sdm.core.SDMPreferenceConstants;
import org.eclipse.ptp.internal.debug.sdm.ui.messages.Messages;
import org.eclipse.ptp.internal.ui.preferences.ScrolledPageContent;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemotePortForwardingService;
import org.eclipse.remote.core.launch.IRemoteLaunchConfigService;
import org.eclipse.remote.ui.IRemoteUIFileService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

/**
 * The dynamic tab for SDM-based debugger implementations.
 */
public class SDMPage extends AbstractLaunchConfigurationTab {
	protected static final String EXTENSION_POINT_ID = "defaultPath"; //$NON-NLS-1$
	protected static final String ATTR_PATH = "path"; //$NON-NLS-1$

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	protected static final String LOCALHOST = "localhost"; //$NON-NLS-1$

	private IRemoteConnection fRemoteConnection;

	protected Combo fSDMBackendCombo;
	protected Text fSDMPathText;
	protected Text fSessionAddressText;
	protected Text fBackendPathText;
	protected Button fSDMPathBrowseButton;
	protected Button fBackendPathBrowseButton;
	protected Button fDefaultSessionAddressButton;
	protected Button fUseBuiltinSDM;
	protected ExpandableComposite fAdvancedOptions;

	/**
	 * Browse for a file.
	 * 
	 * @return path to file selected in browser
	 */
	private String browseFile() {
		if (fRemoteConnection != null) {
			IRemoteUIFileService fileService = fRemoteConnection.getConnectionType().getService(IRemoteUIFileService.class);
			if (fileService != null) {
				fileService.setConnection(fRemoteConnection);
				return fileService.browseFile(getShell(), Messages.SDMPage_Select_Debugger_Executable, fSDMPathText.getText(), 0);
			}
		}

		FileDialog dialog = new FileDialog(getShell());
		dialog.setText(Messages.SDMPage_Select_Debugger_Executable);
		dialog.setFileName(fSDMPathText.getText());
		return dialog.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
	 */
	@Override
	public boolean canSave() {
		setErrorMessage(null);
		if (getFieldContent(fSessionAddressText.getText()) == null) {
			setErrorMessage(Messages.SDMPage_Debugger_host_must_be_specified);
		} else if (getFieldContent(fSDMPathText.getText()) == null) {
			setErrorMessage(Messages.SDMPage_Executable_path_must_be_specified);
		}
		// setErrorMessage(errMsg);
		return (getErrorMessage() == null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse .swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		ScrolledPageContent pageContent = new ScrolledPageContent(parent);
		pageContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite comp = pageContent.getBody();
		comp.setLayout(new GridLayout(3, false));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.SDMPage_Debugger_backend);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		fSDMBackendCombo = new Combo(comp, SWT.READ_ONLY);
		fSDMBackendCombo.setItems(SDMDebugCorePlugin.getDefault().getDebuggerBackends());
		fSDMBackendCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateBackend();
				updateLaunchConfigurationDialog();
			}
		});
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.horizontalSpan = 2;
		fSDMBackendCombo.setLayoutData(gd);

		fUseBuiltinSDM = new Button(comp, SWT.CHECK);
		fUseBuiltinSDM.setText(Messages.SDMPage_Use_builtin_SDM);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.horizontalSpan = 3;
		fUseBuiltinSDM.setLayoutData(gd);

		label = new Label(comp, SWT.NONE);
		label.setText(Messages.SDMPage_Path_to_SDM);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		fSDMPathText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		fSDMPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fSDMPathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		fSDMPathBrowseButton = createPushButton(comp, Messages.SDMPage_Browse, null);
		fSDMPathBrowseButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fSDMPathBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String file = browseFile();
				if (file != null) {
					fSDMPathText.setText(file);
				}
			}
		});

		/*
		 * Advanced options
		 */
		fAdvancedOptions = new ExpandableComposite(comp, SWT.NONE, ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
		fAdvancedOptions.setText(Messages.SDMPage_Advanced_Options);
		fAdvancedOptions.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				ScrolledPageContent parent = getParentScrolledComposite((ExpandableComposite) e.getSource());
				if (parent != null) {
					parent.reflow(true);
				}
			}
		});
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 3;
		fAdvancedOptions.setLayoutData(gd);

		Composite advComp = new Composite(fAdvancedOptions, SWT.NONE);
		fAdvancedOptions.setClient(advComp);
		advComp.setLayout(new GridLayout(3, false));
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 2;
		advComp.setLayoutData(gd);

		Group sessGroup = new Group(advComp, SWT.SHADOW_ETCHED_IN);
		sessGroup.setLayout(new GridLayout(2, false));
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 3;
		sessGroup.setLayoutData(gd);

		fDefaultSessionAddressButton = createCheckButton(sessGroup, Messages.SDMPage_Use_default_session_address);
		fDefaultSessionAddressButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}
		});
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 2;
		fDefaultSessionAddressButton.setLayoutData(gd);
		fDefaultSessionAddressButton.setSelection(true);

		label = new Label(sessGroup, SWT.NONE);
		label.setText(Messages.SDMPage_Session_address);
		gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
		label.setLayoutData(gd);

		fSessionAddressText = new Text(sessGroup, SWT.SINGLE | SWT.BORDER);
		fSessionAddressText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		fSessionAddressText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		fSessionAddressText.setLayoutData(gd);

		label = new Label(advComp, SWT.NONE);
		label.setText(Messages.SDMPage_Path_to_gdb_executable);
		gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		label.setLayoutData(gd);

		fBackendPathText = new Text(advComp, SWT.SINGLE | SWT.BORDER);
		fBackendPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fBackendPathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		fBackendPathBrowseButton = createPushButton(advComp, Messages.SDMPage_Browse, null);
		fBackendPathBrowseButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fBackendPathBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String file = browseFile();
				if (file != null) {
					fBackendPathText.setText(file);
				}
			}
		});

		setControl(parent);
	}

	/**
	 * Get and clean content of a Text field
	 * 
	 * @param text
	 *            text obtained from a Text field
	 * @return cleaned up content
	 */
	protected String getFieldContent(String text) {
		if (text.trim().length() == 0 || text.equals(EMPTY_STRING)) {
			return null;
		}
		return text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return Messages.SDMPage_Debugger;
	}

	private ScrolledPageContent getParentScrolledComposite(Control control) {
		Control parent = control.getParent();
		while (!(parent instanceof ScrolledPageContent) && parent != null) {
			parent = parent.getParent();
		}
		if (parent instanceof ScrolledPageContent) {
			return (ScrolledPageContent) parent;
		}
		return null;
	}

	/**
	 * Helper method to locate the remote connection used by the launch configuration
	 * 
	 * @param configuration
	 *            launch configuration
	 * @throws CoreException
	 */
	private IRemoteConnection getRemoteConnection(ILaunchConfiguration configuration) {
		IRemoteLaunchConfigService launchConfigService = SDMDebugCorePlugin.getService(IRemoteLaunchConfigService.class);
		return launchConfigService.getActiveConnection(configuration);
	}

	private void initializeBackend(ILaunchConfiguration configuration) throws CoreException {
		String backend = configuration.getAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND, EMPTY_STRING);
		int selected = 0;
		for (int i = 0; i < fSDMBackendCombo.getItemCount(); i++) {
			if (fSDMBackendCombo.getItem(i).equals(backend)) {
				selected = i;
				break;
			}
		}
		fSDMBackendCombo.select(selected);
		backend = fSDMBackendCombo.getItem(selected);

		if (!backend.equals(EMPTY_STRING)) {
			boolean useBuiltin = configuration.getAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_USE_BUILTIN_SDM, false);
			fUseBuiltinSDM.setSelection(useBuiltin);
		}

		updateBackend();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse .debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		/*
		 * Launch configuration is selected or we have just selected SDM as the debugger...
		 */
		try {
			initializeBackend(configuration);
			fSessionAddressText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, LOCALHOST));
			fRemoteConnection = getRemoteConnection(configuration);
			fDefaultSessionAddressButton.setSelection(fRemoteConnection == null
					|| (fRemoteConnection.hasService(IRemotePortForwardingService.class) && fSessionAddressText.getText().equals(
							LOCALHOST)));
			updateEnablement();
		} catch (CoreException e) {
			// Ignore
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse .debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		if (getFieldContent(fSessionAddressText.getText()) == null) {
			setErrorMessage(Messages.SDMPage_Debugger_host_must_be_specified);
		} else if (getFieldContent(fSDMPathText.getText()) == null) {
			setErrorMessage(Messages.SDMPage_Executable_path_must_be_specified);
		}
		return (getErrorMessage() == null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse .debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		/*
		 * Note: performApply gets called when either text is modified via updateLaunchConfigurationDialog(). Only update the
		 * configuration if things are valid.
		 */
		if (isValid(configuration)) {
			configuration.setAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND,
					getFieldContent(fSDMBackendCombo.getText()));
			configuration.setAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND_PATH,
					getFieldContent(fBackendPathText.getText()));
			configuration
					.setAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_USE_BUILTIN_SDM, fUseBuiltinSDM.getSelection());
			configuration.setAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_EXECUTABLE,
					getFieldContent(fSDMPathText.getText()));
			configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST,
					getFieldContent(fSessionAddressText.getText()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse. debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		/*
		 * We have just selected SDM as the debugger...
		 */
		String backend = Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.PREFS_SDM_BACKEND);
		configuration.setAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND, backend);
		configuration.setAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_EXECUTABLE,
				Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.PREFS_SDM_PATH + backend));
		configuration.setAttribute(
				SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND_PATH,
				Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.PREFS_SDM_BACKEND_PATH
						+ backend));
		configuration.setAttribute(
				SDMLaunchConfigurationConstants.ATTR_DEBUGGER_USE_BUILTIN_SDM,
				Preferences.getBoolean(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.PREFS_USE_BUILTIN_SDM
						+ backend));
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, LOCALHOST);
	}

	private void updateBackend() {
		String backend = getFieldContent(fSDMBackendCombo.getText());
		if (backend != null) {
			fSDMPathText.setText(Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(),
					SDMPreferenceConstants.PREFS_SDM_PATH + backend));
			fBackendPathText.setText(Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(),
					SDMPreferenceConstants.PREFS_SDM_BACKEND_PATH + backend));
		}
	}

	private void updateEnablement() {
		fSessionAddressText.setEnabled(!fDefaultSessionAddressButton.getSelection());
	}
}
