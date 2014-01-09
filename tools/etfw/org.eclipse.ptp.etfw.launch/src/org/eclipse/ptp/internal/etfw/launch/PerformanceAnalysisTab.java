/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *   
 * Contributors: 
 * 		Chris Navarro (Illinois/NCSA) - Design and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.etfw.launch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.ptp.core.util.LaunchUtils;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.PreferenceConstants;
import org.eclipse.ptp.internal.etfw.jaxb.JAXBInitializationUtil;
import org.eclipse.ptp.internal.etfw.jaxb.data.BuildToolType;
import org.eclipse.ptp.internal.etfw.jaxb.data.EtfwToolProcessType;
import org.eclipse.ptp.internal.etfw.jaxb.data.ExecToolType;
import org.eclipse.ptp.internal.etfw.jaxb.data.ToolAppType;
import org.eclipse.ptp.internal.etfw.jaxb.data.ToolPaneType;
import org.eclipse.ptp.internal.etfw.jaxb.util.JAXBExtensionUtils;
import org.eclipse.ptp.internal.etfw.launch.messages.Messages;
import org.eclipse.ptp.internal.etfw.ui.ExternalToolSelectionTab;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.launch.IJAXBLaunchConfigurationTab;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationContentsChangedListener;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.JAXBDynamicLaunchConfigurationTab;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.core.LaunchControllerManager;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.CommandType;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Notes: This class is a re-implementation of ParallelToolSelectionTab using JAXB to dynamically build the UI.
 * TODO Review this class to see if some of what is done could be replaced with the JAXB provided classes or done as an extension of
 * the JAXB parent classes instead of what has been done with Some of the functionality in this class belongs in
 * ETFWParentLaunchConfiguration
 * 
 * @author Chris Navarro
 * 
 */
public class PerformanceAnalysisTab extends AbstractLaunchConfigurationTab implements IToolLaunchConfigurationConstants,
		IExecutableExtension {

	private static final String TAB_ID = "org.eclipse.ptp.internal.etfw.launch.PerformanceAnalysisTab"; //$NON-NLS-1$
	/**
	 * Determines if the launch configuration associated with this tab has
	 * access to the PTP
	 */
	protected boolean noPTP = false;
	private EtfwToolProcessType etfwTool;
	private ETFWParentLaunchConfigurationTab launchTabParent;
	private IVariableMap vmap;
	private ILaunchConfiguration launchConfiguration = null;
	private ILaunchController controller;
	private final WidgetListener listener = new WidgetListener();
	private List<ToolPaneType> toolTabs;

	private Composite topComposite;
	private Composite toolComposite;
	private Composite bottomComposite;
	private Combo toolCombo;
	private Label selectToolLbl;
	private Button buildOnlyCheck;
	private Button analyzeonlyCheck;

	// This should be part of the launchTabParent, but there is RM specifics that must be removed
	private final LinkedList<IJAXBLaunchConfigurationTab> tabControllers = new LinkedList<IJAXBLaunchConfigurationTab>();
	private final ContentsChangedListener launchContentsChangedListener = new ContentsChangedListener();

	// Sax Parser ETFW
	private ExternalToolSelectionTab saxETFWTab;

	public PerformanceAnalysisTab() {
		this(false);
	}

	public PerformanceAnalysisTab(boolean noPar) {
		noPTP = noPar;
	}

	/**
	 * Generates the UI for the analysis tab, consisting of sub-tabs which may be
	 * dynamically generated
	 * 
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		setControl(content);

		GridLayout layout = new GridLayout();
		content.setLayout(layout);

		topComposite = new Composite(content, SWT.NONE);
		topComposite.setLayout(new GridLayout(2, false));
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolComposite = new Composite(content, SWT.NONE);
		toolComposite.setLayout(new FillLayout());
		toolComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		bottomComposite = new Composite(content, SWT.NONE);
		bottomComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		bottomComposite.setLayout(new GridLayout(1, false));

		buildOnlyCheck = new Button(bottomComposite, SWT.CHECK);
		buildOnlyCheck.setText(Messages.PerformanceAnalysisTab_BuildInstrumentedExecutable);

		analyzeonlyCheck = new Button(bottomComposite, SWT.CHECK);
		analyzeonlyCheck.setText(Messages.PerformanceAnalysisTab_SelectExistingPerfData);

		if (noPTP) {
			buildSAXParserUI();
		} else {
			String parser = PreferenceConstants.getVersion();
			if (parser.equals(IToolLaunchConfigurationConstants.USE_SAX_PARSER)) {
				buildSAXParserUI();
			} else {
				buildJAXBParserUI();
			}
		}
	}

	private void buildSAXParserUI() {
		bottomComposite.setVisible(false);

		saxETFWTab = new ExternalToolSelectionTab(noPTP);
		saxETFWTab.createControl(toolComposite);
		saxETFWTab.setLaunchConfigurationDialog(this.getLaunchConfigurationDialog());

		toolComposite.getParent().layout();
		toolComposite.layout();
	}

	private void buildJAXBParserUI() {
		selectToolLbl = new Label(topComposite, SWT.NONE);
		selectToolLbl.setText(Messages.PerformanceAnalysisTab_SelectTool);

		toolCombo = new Combo(topComposite, SWT.READ_ONLY);
		toolCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		String[] toolNames = JAXBExtensionUtils.getToolNames();
		toolCombo.add(Messages.PerformanceAnalysisTab_PleaseSelectWorkflow);

		for (String name : toolNames) {
			toolCombo.add(name);
		}

		toolCombo.addSelectionListener(listener);

		toolComposite.getParent().layout();
		topComposite.layout();
		toolComposite.layout();
	}

	/**
	 * Helper to add a read-only attribute
	 * 
	 * @param name
	 * @param value
	 */
	private void addAttribute(String name, String value) {
		AttributeType attr = vmap.get(name);
		if (attr == null) {
			attr = new AttributeType();
			attr.setName(name);
			attr.setVisible(true);
			attr.setReadOnly(true);
			vmap.put(name, attr);
		}
		attr.setValue(value);
	}

	/**
	 * Add connection properties to the attribute map.
	 * 
	 * @param conn
	 */
	private void setConnectionPropertyAttributes(IRemoteConnection conn) {
		String property = conn.getProperty(IRemoteConnection.OS_ARCH_PROPERTY);
		if (property != null) {
			addAttribute(IRemoteConnection.OS_ARCH_PROPERTY, property);
		}
		property = conn.getProperty(IRemoteConnection.OS_NAME_PROPERTY);
		if (property != null) {
			addAttribute(IRemoteConnection.OS_NAME_PROPERTY, property);
		}
		property = conn.getProperty(IRemoteConnection.OS_VERSION_PROPERTY);
		if (property != null) {
			addAttribute(IRemoteConnection.OS_VERSION_PROPERTY, property);
		}
	}

	private void rebuildTab(String toolName) {
		clearOldWidgets();

		if (toolName.equals(Messages.PerformanceAnalysisTab_PleaseSelectWorkflow)) {
			return;
		}

		etfwTool = JAXBExtensionUtils.getTool(toolName);

		if (controller != null) {
			vmap = controller.getEnvironment();

			JAXBInitializationUtil.initializeMap(etfwTool, vmap);

			// Add in connection property attributes to ETFW variable map
			final IRemoteServices services = RemoteServices.getRemoteServices(controller.getRemoteServicesId());
			if (services != null) {
				final IRemoteConnectionManager connMgr = services.getConnectionManager();
				IRemoteConnection remoteConnection = connMgr.getConnection(controller.getConnectionName());
				if (remoteConnection != null) {
					setConnectionPropertyAttributes(remoteConnection);
				}
			}

			try {
				launchTabParent = new ETFWParentLaunchConfigurationTab(controller, getLaunchConfigurationDialog(),
						new NullProgressMonitor(), vmap);
			} catch (Throwable e1) {
				e1.printStackTrace();
			}
			launchTabParent.addContentsChangedListener(launchContentsChangedListener);

			for (IJAXBLaunchConfigurationTab tabControl : tabControllers) {
				tabControl.getLocalWidgets().clear();
			}

			tabControllers.clear();
			for (Control control : toolComposite.getChildren()) {
				control.dispose();
			}

			if (etfwTool.getControlData() != null) {
				for (CommandType command : etfwTool.getControlData().getInitializeCommand()) {
					if (command != null) {
						try {
							controller.runCommand(command, vmap);
						} catch (CoreException e) {
							e.printStackTrace();
						}
					}
				}
			}

			toolTabs = findTabControllers();

			for (ToolPaneType toolTab : toolTabs) {
				tabControllers.add(new JAXBDynamicLaunchConfigurationTab(controller, toolTab.getOptionPane(), launchTabParent));
			}

			launchTabParent.addDynamicContent(tabControllers);

			try {
				launchTabParent.createControl(toolComposite, controller.getControlId());
			} catch (CoreException e) {
				e.printStackTrace();
			}

			handleUpdate();
		} else {
			Composite comp = new Composite(toolComposite, SWT.NONE);
			GridLayout layout = new GridLayout();
			comp.setLayout(layout);
			Text message = new Text(comp, SWT.NONE);
			message.setText(Messages.PerformanceAnalysisTab_Please_select_a_target_configuration_first);
			message.setBackground(toolComposite.getBackground());
			message.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		}
	}

	private void handleUpdate() {
		launchTabParent.initializeFrom(this.launchConfiguration);

		for (IJAXBLaunchConfigurationTab tabControl : tabControllers) {
			for (IUpdateModel m : tabControl.getLocalWidgets().values()) {
				m.initialize(this.launchConfiguration, vmap, launchTabParent.getVariableMap());
			}

			((JAXBDynamicLaunchConfigurationTab) tabControl).initializeFrom(this.launchConfiguration);
		}
	}

	private List<ToolPaneType> findTabControllers() {
		List<ToolPaneType> subTabs = new ArrayList<ToolPaneType>();
		for (Object tool : etfwTool.getExecToolOrAnalysisToolOrBuildTool()) {
			if (tool instanceof BuildToolType) {
				BuildToolType buildTool = (BuildToolType) tool;
				if (buildTool.getGlobal() != null) {
					for (ToolPaneType toolPane : buildTool.getGlobal().getToolPanes()) {
						if (!toolPane.isVirtual() && toolPane.getOptionPane() != null) {
							subTabs.add(toolPane);
						}
					}
				}

				if (buildTool.getAllCompilers() != null) {
					for (ToolPaneType toolPane : buildTool.getAllCompilers().getToolPanes()) {
						if (!toolPane.isVirtual() && toolPane.getOptionPane() != null) {
							subTabs.add(toolPane);
						}
					}
				}
			} else if (tool instanceof ExecToolType) {
				ExecToolType execTool = (ExecToolType) tool;
				if (execTool.getGlobal() != null) {
					for (ToolPaneType toolPane : execTool.getGlobal().getToolPanes()) {
						if (!toolPane.isVirtual() && toolPane.getOptionPane() != null) {
							subTabs.add(toolPane);
						}
					}
				}

				for (ToolAppType toolApp : execTool.getExecUtils()) {
					for (ToolPaneType toolPane : toolApp.getToolPanes()) {
						if (!toolPane.isVirtual() && toolPane.getOptionPane() != null) {
							subTabs.add(toolPane);
						}
					}
				}
			}
		}
		return subTabs;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// Do nothing
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		launchConfiguration = configuration;

		if (noPTP) {
			saxETFWTab.initializeFrom(configuration);
		} else {

			final String rmType = LaunchUtils.getTemplateName(configuration);
			final String remId = LaunchUtils.getRemoteServicesId(configuration);
			final String remName = LaunchUtils.getConnectionName(configuration);
			try {
				controller = LaunchControllerManager.getInstance().getLaunchController(remId, remName, rmType);
				if (controller != null) {
					String parser = PreferenceConstants.getVersion();
					if (parser.equals(IToolLaunchConfigurationConstants.USE_SAX_PARSER)) {
						saxETFWTab.initializeFrom(configuration);
					} else {
						String toolName = configuration.getAttribute(IToolLaunchConfigurationConstants.SELECTED_TOOL,
								IToolLaunchConfigurationConstants.EMPTY_STRING);
						for (int index = 0; index < toolCombo.getItemCount(); index++) {
							if (toolCombo.getItem(index).equals(toolName)) {
								toolCombo.select(index);
								toolCombo.notifyListeners(SWT.Selection, null);
								break;
							}
						}

						if (toolName.equals(IToolLaunchConfigurationConstants.EMPTY_STRING)) {
							// When switching between launch configurations, clear out old widgets
							toolCombo.select(0);
							clearOldWidgets();
						}
					}
				}

			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (noPTP) {
			configuration.setAttribute(IToolLaunchConfigurationConstants.ETFW_VERSION,
					IToolLaunchConfigurationConstants.USE_SAX_PARSER);
			saxETFWTab.performApply(configuration);
		} else {
			String parser = PreferenceConstants.getVersion();
			configuration.setAttribute(IToolLaunchConfigurationConstants.ETFW_VERSION, parser);
			if (parser.equals(IToolLaunchConfigurationConstants.USE_SAX_PARSER)) {
				saxETFWTab.performApply(configuration);
			} else {
				if (toolCombo.getSelectionIndex() > 0) {
					String selectedtool = toolCombo.getItem(toolCombo.getSelectionIndex());
					configuration.setAttribute(SELECTED_TOOL, selectedtool);

					configuration.setAttribute(BUILDONLY, buildOnlyCheck.getSelection());
					configuration.setAttribute(ANALYZEONLY, analyzeonlyCheck.getSelection());

					if (launchTabParent != null) {
						launchTabParent.performApply(configuration);
					}
				}
			}
		}
	}

	protected class WidgetListener extends SelectionAdapter {
		private String prevToolName;
		private String prevLaunchConfig;

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (getLaunchConfigurationDialog().getActiveTab().getName().equals(getName())) {
				int selection = toolCombo.getSelectionIndex();
				String toolName = toolCombo.getItem(selection);

				if (!toolName.equals(prevToolName) || !launchConfiguration.getName().equals(prevLaunchConfig)) {
					prevToolName = toolName;
					prevLaunchConfig = launchConfiguration.getName();
					rebuildTab(toolName);
				}
			}

			updateLaunchConfigurationDialog();
		}
	}

	private void clearOldWidgets() {
		for (Control child : toolComposite.getChildren()) {
			child.dispose();
		}
	}

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	@Override
	public String getName() {
		return Messages.PerformanceAnalysisTab_Tab_Name;
	}

	@Override
	public String getId() {
		return TAB_ID;
	}

	/**
	 * @see ILaunchConfigurationTab#setLaunchConfigurationDialog (ILaunchConfigurationDialog)
	 */
	@Override
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
	}

	@Override
	public boolean isValid(ILaunchConfiguration configuration) {
		setErrorMessage(null);
		if (toolCombo != null) {
			try {
				String toolName = configuration.getAttribute(IToolLaunchConfigurationConstants.SELECTED_TOOL,
						IToolLaunchConfigurationConstants.EMPTY_STRING);
				if (toolCombo.getSelectionIndex() != -1) {
					if (toolCombo.getItem(toolCombo.getSelectionIndex()).equals(toolName)) {
						return true;
					}
				}
			} catch (CoreException e) {
				// Ignore
			}
			setErrorMessage(Messages.PerformanceAnalysisTab_NoWorkflowSelected);
			return false;
		} else if (launchTabParent != null) {
			String error = launchTabParent.getUpdateHandler().getFirstError();
			if (error != null) {
				setErrorMessage(error);
				return false;
			}
		}

		return true;
	}

	/**
	 * Sets whether it is possible to initiate a parallel launch from
	 * this tab
	 * 
	 * @param noPar
	 *            Availability of the PTP to this tab's launch configuration
	 *            delegate
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		if (data != null) {
			Map<String, String> parameters = (Map<String, String>) data;
			noPTP = Boolean.valueOf(parameters.get("noPTP")); //$NON-NLS-1$
		}
	}

	private final class ContentsChangedListener implements IRMLaunchConfigurationContentsChangedListener {

		@Override
		public void handleContentsChanged(IRMLaunchConfigurationDynamicTab rmDynamicTab) {
			// The buttons and messages have to be updated based on anything
			// that has changed in the dynamic portion of the launch tab.
			updateLaunchConfigurationDialog();
		}
	}

}
