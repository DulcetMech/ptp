/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.internal.etfw.parallel;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.internal.etfw.ILaunchFactory;
import org.eclipse.ptp.internal.etfw.IToolLaunchConfigurationDelegate;
import org.eclipse.ptp.internal.etfw.RemoteBuildLaunchUtils;
import org.eclipse.ptp.internal.etfw.ToolLaunchManager;
import org.eclipse.ptp.internal.etfw.parallel.messages.Messages;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.launch.ParallelLaunchConfigurationDelegate;
import org.eclipse.remote.core.launch.IRemoteLaunchConfigService;

/**
 * Launches parallel C/C++ (or Fortran) applications after rebuilding them with performance instrumentation
 */
public class ParallelToolLaunchConfigurationDelegate extends ParallelLaunchConfigurationDelegate implements
		IToolLaunchConfigurationConstants, IToolLaunchConfigurationDelegate {

	private boolean initialized = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.etfw.IToolLaunchConfigurationDelegate#getInitialized()
	 */
	public boolean getInitialized() {
		return initialized;
	}

	/**
	 * The primary launch command of this launch configuration delegate. The operations in this function are divided into three
	 * jobs: Buildig, Running and Data collection
	 */
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launchIn, IProgressMonitor monitor)
			throws CoreException {
		if (initialized) {// TODO: This can break if the launch fails. Fix it.
			initialized = false;
			super.launch(configuration, mode, launchIn, monitor);

			return;
		}

		try {
			final SubMonitor progress = SubMonitor.convert(monitor, 110);
			progress.setTaskName(NLS.bind(Messages.ParallelToolLaunchConfigurationDelegate_Launching, configuration.getName()));
			progress.setWorkRemaining(90);
			// save the executable location so we can access it in the postprocessing
			final ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
			final String progName = wc.getAttribute(IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME, "defaultValue"); //$NON-NLS-1$
			final String progPath = wc.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, "defaultValue"); //$NON-NLS-1$
			final String projName = wc.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, "defaultValue"); //$NON-NLS-1$

			final IFileStore pdir = EFS.getLocalFileSystem().getStore(new Path(progPath));
			final IFileStore prog = pdir.getChild(progName);

			verifyProject(configuration);

			wc.setAttribute(EXTOOL_EXECUTABLE_NAME, prog.toURI().getPath());// Path+File.separator+progName
			wc.setAttribute(EXTOOL_PROJECT_NAME, projName);
			wc.setAttribute(EXTOOL_ATTR_ARGUMENTS_TAG, IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS);
			wc.setAttribute(EXTOOL_PROJECT_NAME_TAG, IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME);
			wc.setAttribute(EXTOOL_EXECUTABLE_NAME_TAG, IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME);
			wc.setAttribute(EXTOOL_EXECUTABLE_PATH_TAG, IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH);
			IRemoteLaunchConfigService launchConfigService = Activator.getService(IRemoteLaunchConfigService.class);
			String rmId = launchConfigService.getActiveConnection(configuration).getConnectionType().getId();
			rmId += DOT;
			wc.setAttribute(EXTOOL_JAXB_ATTR_ARGUMENTS_TAG, rmId + JAXBControlConstants.PROG_ARGS);
			wc.setAttribute(EXTOOL_JAXB_EXECUTABLE_PATH_TAG, rmId + JAXBControlConstants.EXEC_PATH);
			wc.setAttribute(EXTOOL_JAXB_EXECUTABLE_DIRECTORY_TAG, rmId + JAXBControlConstants.EXEC_DIR);

			// put(JAXBControlConstants.DIRECTORY, configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR,
			// dir));

			// String testJaxb=configuration.getAttribute(JAXBControlConstants.EXEC_PATH, EMPTY_STRING);
			// if(testJaxb!=null&&testJaxb.length()>0 &&test)

			wc.doSave();

			final ILaunchFactory lf = new ParallelLaunchFactory();

			{
				// initialized = true;
				progress.worked(80);
				if (launchIn instanceof IPLaunch) {
					if (!verifyLaunchAttributes(configuration, mode, progress.newChild(10)) || progress.isCanceled()) {
						return;
					}

					final ToolLaunchManager plaunch = new ToolLaunchManager(this, lf, new RemoteBuildLaunchUtils(configuration));
					plaunch.launch(configuration, mode, launchIn, monitor);
				}
			}
			initialized = false;
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.etfw.IToolLaunchConfigurationDelegate#setInitialized(boolean)
	 */
	public void setInitialized(boolean init) {
		initialized = init;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.AbstractParallelLaunchConfigurationDelegate#verifyLaunchAttributes(org.eclipse.debug.core.
	 * ILaunchConfiguration, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected boolean verifyLaunchAttributes(final ILaunchConfiguration configuration, String mode, final IProgressMonitor monitor)
			throws CoreException {
		try {
			boolean value = super.verifyLaunchAttributes(configuration, mode, monitor);
			if (value) {
				value = verifyProfilingTool(configuration);
			}

			return value;
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}

	}

	/**
	 * @param configuration
	 * @return If there is an ETFw version (JAXB or SAX) and ETFw tool selected return true. Otherwise throw an exception.
	 * @throws CoreException
	 */
	protected boolean verifyProfilingTool(ILaunchConfiguration configuration) throws CoreException {
		final String whichParser = configuration.getAttribute(IToolLaunchConfigurationConstants.ETFW_VERSION,
				IToolLaunchConfigurationConstants.EMPTY_STRING);
		final String whichTool = configuration.getAttribute(IToolLaunchConfigurationConstants.SELECTED_TOOL,
				IToolLaunchConfigurationConstants.EMPTY_STRING);

		if (whichParser.isEmpty() || whichTool.isEmpty()) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					Messages.ParallelToolLaunchConfigurationDelegate_ProfilingToolNotSpecified));
		}
		return true;
	}
}
