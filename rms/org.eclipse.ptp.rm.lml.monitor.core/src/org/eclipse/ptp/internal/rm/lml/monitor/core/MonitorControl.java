/*******************************************************************************
 * Copyright (c) 2012-2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 * Carsten Karbach, FZ Juelich - add LML_da caching
 *******************************************************************************/
package org.eclipse.ptp.internal.rm.lml.monitor.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.jobs.IJobListener;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.JobManager;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.internal.rm.jaxb.control.core.RemoteServicesDelegate;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBInitializationUtils;
import org.eclipse.ptp.internal.rm.lml.core.JAXBUtil;
import org.eclipse.ptp.internal.rm.lml.monitor.core.messages.Messages;
import org.eclipse.ptp.remote.server.core.RemoteServerManager;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.core.LaunchControllerManager;
import org.eclipse.ptp.rm.jaxb.core.data.MonitorDriverType;
import org.eclipse.ptp.rm.jaxb.core.data.MonitorType;
import org.eclipse.ptp.rm.jaxb.core.data.SimpleCommandType;
import org.eclipse.ptp.rm.jaxb.core.data.lml.LayoutRoot;
import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.core.elements.CommandType;
import org.eclipse.ptp.rm.lml.core.elements.DriverType;
import org.eclipse.ptp.rm.lml.core.elements.ObjectFactory;
import org.eclipse.ptp.rm.lml.core.elements.RequestType;
import org.eclipse.ptp.rm.lml.da.server.core.LMLDAServer;
import org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl;
import org.eclipse.ptp.rm.lml.monitor.core.IMonitorCoreConstants;
import org.eclipse.ptp.rm.lml.monitor.core.MonitorControlManager;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

/**
 * LML JAXB resource manager monitor
 */
@SuppressWarnings("restriction")
public class MonitorControl implements IMonitorControl {
	private class JobListener implements IJobListener {
		@Override
		public void jobAdded(IJobStatus status) {
			addJob(status);
		}

		@Override
		public void jobChanged(IJobStatus status) {
			updateJob(status);
		}
	}

	/**
	 * Job for running the LML DA server. This job gets run periodically based
	 * on the JOB_SCHEDULE_FREQUENCY.
	 */
	private class MonitorJob extends Job {
		private final LMLDAServer fServer;
		private final IRemoteConnection fConnection;
		private int waitCount;
		private boolean stopReqested;

		public MonitorJob(IRemoteConnection conn) {
			super(Messages.LMLResourceManagerMonitor_LMLMonitorJob);
			setSystem(true);
			fConnection = conn;
			fServer = (LMLDAServer) RemoteServerManager.getServer(LMLDAServer.SERVER_ID, conn);
			IRemoteProcessService procSvc = conn.getService(IRemoteProcessService.class);
			fServer.setWorkDir(new Path(procSvc.getWorkingDirectory()).append(".eclipsesettings").toString()); //$NON-NLS-1$
		}

		/**
		 * Tell server to stop
		 */
		public void askForStop() {
			if (fServer.serverIsRunning()) {
				fServer.cancel();
			}
			stopReqested = true;
			wakeUp();
		}

		/**
		 * Schedule an immediate refresh
		 */
		public void refresh() {
			waitCount = 0;
			wakeUp();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final SubMonitor subMon = SubMonitor.convert(monitor, 100);

			fConnection.addConnectionChangeListener(fConnectionListener);
			JobManager.getInstance().addListener(fJobListener);

			try {
				while (!stopReqested && !subMon.isCanceled()) {
					fServer.startServer(subMon.newChild(20));
					if (!subMon.isCanceled()) {
						fServer.waitForServerStart(subMon.newChild(20));
						if (!subMon.isCanceled() && fServer.serverIsRunning()) {
							LMLManager.getInstance().update(getId(), fServer.getInputStream(), fServer.getOutputStream());
						}
					}

					if (!stopReqested) {
						IStatus status = fServer.waitForServerFinish(subMon.newChild(40));

						if (status == Status.OK_STATUS) {
							waitCount = getUpdateInterval() * 1000;

							while (!stopReqested && !subMon.isCanceled() && waitCount > 0) {
								synchronized (this) {
									try {
										wait(100);
									} catch (InterruptedException e) {
										// Ignore
									}
								}
								waitCount -= 100;
							}
						}
					}
				}
			} catch (final Exception e) {
				return new Status(IStatus.ERROR, LMLMonitorCorePlugin.PLUGIN_ID, e.getLocalizedMessage(), e);
			} finally {
				save();
				fLMLManager.closeLgui(getId());

				fConnection.removeConnectionChangeListener(fConnectionListener);
				JobManager.getInstance().removeListener(fJobListener);

				setActive(false);
			}

			return Status.OK_STATUS;
		}
	}

	private void setActive(boolean active) {
		fActive = active;
		MonitorControlManager.getInstance().fireMonitorUpdated(new IMonitorControl[] { MonitorControl.this });
	}

	private class ConnectionChangeListener implements IRemoteConnectionChangeListener {
		@Override
		public void connectionChanged(RemoteConnectionChangeEvent event) {
			int type = event.getType();
			MonitorJob job = fMonitorJob;
			if (job != null
					&& (type == RemoteConnectionChangeEvent.CONNECTION_CLOSED || type == RemoteConnectionChangeEvent.CONNECTION_ABORTED)) {
				job.askForStop();
			}
		}
	}

	public static void checkForOutputFile(JobStatusData data, boolean isError, ILaunchController controller,
			IProgressMonitor monitor) {
		String path;
		if (isError) {
			path = data.getString(JobStatusData.STDERR_REMOTE_FILE_ATTR);
		} else {
			path = data.getString(JobStatusData.STDOUT_REMOTE_FILE_ATTR);
		}
		if (path != null) {
			SubMonitor progress = SubMonitor.convert(monitor, 20);
			try {
				IRemoteConnection conn = getRemoteConnection(controller);
				if (conn != null) {
					IRemoteFileService fileSvc = conn.getService(IRemoteFileService.class);
					IFileStore store = fileSvc.getResource(path);
					if (store.fetchInfo(EFS.NONE, progress.newChild(10)).exists()) {
						if (isError) {
							data.setErrReady(true);
						} else {
							data.setOutReady(true);
						}
					}
				}
			} catch (CoreException e) {
				// Ignore, output will remain unavailable
			}
		}
	}

	private static void checkOutputFile(JobStatusData jobData, final ILaunchController controller) {
		checkOutputFiles(new JobStatusData[] { jobData }, controller);
	}

	/**
	 * Check the stdout and stderr output files to see if they exist. If so, the ready flag will be set in the JobStatusData object.
	 * 
	 * @param jobData
	 *            Array of JobStatusData object
	 * @param controller
	 *            launch controller
	 */
	private static void checkOutputFiles(final JobStatusData[] jobData, final ILaunchController controller) {
		Job job = new Job(Messages.MonitorControl_Check_job_output) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				SubMonitor progress = SubMonitor.convert(monitor, 20);
				for (JobStatusData data : jobData) {
					checkForOutputFile(data, true, controller, progress.newChild(10));
					checkForOutputFile(data, false, controller, progress.newChild(10));
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private final String fMonitorId;
	private final LMLManager fLMLManager = LMLManager.getInstance();
	private final JobListener fJobListener = new JobListener();
	private final ConnectionChangeListener fConnectionListener = new ConnectionChangeListener();
	private final List<JobStatusData> fSavedJobs = new ArrayList<JobStatusData>();

	private String fSavedLayout;
	private MonitorJob fMonitorJob;
	private String fConfigurationName;
	private boolean fActive;
	private boolean cacheActive;// If true, remote caching is activated, otherwise an update is enforced
	private String fRemoteServicesId;
	private String fConnectionName;

	private static final String XML = "xml";//$NON-NLS-1$ 
	private static final String JOBS_ATTR = "jobs";//$NON-NLS-1$ 
	private static final String JOB_ATTR = "job";//$NON-NLS-1$ 
	private static final String LAYOUT_ATTR = "layout";//$NON-NLS-1$
	private static final String LAYOUT_STRING_ATTR = "layoutString";//$NON-NLS-1$
	private static final String MONITOR_STATE = "monitorState";//$NON-NLS-1$;
	private static final String MONITOR_ATTR = "monitor";//$NON-NLS-1$
	private static final String REMOTE_SERVICES_ID_ATTR = "remoteServicesId";//$NON-NLS-1$;
	private static final String CONNECTION_NAME_ATTR = "connectionName";//$NON-NLS-1$;
	private static final String CONFIGURATION_NAME_ATTR = "configurationName";//$NON-NLS-1$

	public MonitorControl(String monitorId) {
		fMonitorId = monitorId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#dispose()
	 */
	@Override
	public void dispose() {
		try {
			getSaveLocation().delete();
		} catch (Exception e) {
			LMLMonitorCorePlugin.log(e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#getConfigurationName()
	 */
	@Override
	public String getConfigurationName() {
		return fConfigurationName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#getConnectionName()
	 */
	@Override
	public String getConnectionName() {
		return fConnectionName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#getId()
	 */
	@Override
	public String getId() {
		return fMonitorId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#isCacheActive()
	 */
	@Override
	public boolean isCacheActive() {
		return cacheActive;
	}

	/**
	 * Retrieve configured mode for the caching mechanism of LML_da.
	 * This value is only used at the start-up of a monitoring
	 * connection. If the user triggers an update manually, the used
	 * update mode will be used for all subsequent updates.
	 * 
	 * @return true, if cache should be disabled per default, false otherwise
	 */
	public boolean getDefaultForceUpdate() {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(LMLMonitorCorePlugin.PLUGIN_ID);

		boolean result = preferences.getBoolean(IMonitorCoreConstants.PREF_FORCE_UPDATE,
				IMonitorCoreConstants.PREF_FORCE_UPDATE_DEFAULT);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#getRemoteServicesId()
	 */
	@Override
	public String getRemoteServicesId() {
		return fRemoteServicesId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#getSystemType()
	 */
	@Override
	public String getSystemType() {
		return MonitorControlManager.getSystemType(fConfigurationName);
	}

	/**
	 * Retrieve configured update interval from the eclipse preferences.
	 * 
	 * @return the update interval in seconds, at which the job run is repeated
	 */
	public int getUpdateInterval() {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(LMLMonitorCorePlugin.PLUGIN_ID);

		int prefUpdateInterval = preferences.getInt(IMonitorCoreConstants.PREF_UPDATE_INTERVAL,
				IMonitorCoreConstants.PREF_UPDATE_INTERVAL_DEFAULT);
		return prefUpdateInterval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#isActive()
	 */
	@Override
	public boolean isActive() {
		return fActive;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#load()
	 */
	@Override
	public boolean load() throws CoreException {
		fSavedJobs.clear();

		FileReader reader;
		try {
			reader = new FileReader(getSaveLocation());
		} catch (FileNotFoundException e) {
			throw CoreExceptionUtils.newException(e.getMessage(), e);
		}
		IMemento memento = XMLMemento.createReadRoot(reader);

		boolean active = loadState(memento);

		IMemento layout = memento.getChild(LAYOUT_ATTR);
		if (layout != null) {
			fSavedLayout = layout.getString(LAYOUT_STRING_ATTR);
		}

		IMemento jobs = memento.getChild(JOBS_ATTR);
		loadJobs(jobs, fSavedJobs);

		return active;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#refresh()
	 */
	@Override
	public void refresh() {
		if (fMonitorJob != null) {
			fMonitorJob.refresh();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#save()
	 */
	@Override
	public void save() {
		final XMLMemento memento = XMLMemento.createWriteRoot(MONITOR_ATTR);

		final String layout = fLMLManager.getCurrentLayout(getId());
		final JobStatusData[] jobs = fLMLManager.getUserJobs(getId());

		saveState(memento);

		if (layout != null) {
			final IMemento layoutMemento = memento.createChild(LAYOUT_ATTR);
			layoutMemento.putString(LAYOUT_STRING_ATTR, layout);
		}

		if (jobs != null && jobs.length > 0) {
			final IMemento jobsMemento = memento.createChild(JOBS_ATTR);
			for (final JobStatusData status : jobs) {
				saveJob(status, jobsMemento);
			}
		}

		try {
			FileWriter writer = new FileWriter(getSaveLocation());
			memento.save(writer);
		} catch (final IOException e) {
			LMLMonitorCorePlugin.log(e.getLocalizedMessage());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#setConfigurationName(java.lang.String)
	 */
	@Override
	public void setConfigurationName(String name) {
		fConfigurationName = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#setConnectionName
	 * (java.lang.String)
	 */
	@Override
	public void setConnectionName(String connName) {
		fConnectionName = connName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#setRemoteServicesId
	 * (java.lang.String)
	 */
	@Override
	public void setRemoteServicesId(String id) {
		fRemoteServicesId = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.monitors.IMonitorControl#start(org.eclipse.core.
	 * runtime.IProgressMonitor)
	 */
	@Override
	public void start(IProgressMonitor monitor) throws CoreException {
		if (!isActive()) {
			SubMonitor progress = SubMonitor.convert(monitor, 20);
			ILaunchController controller = LaunchControllerManager.getInstance().getLaunchController(getRemoteServicesId(),
					getConnectionName(), getConfigurationName());

			if (controller == null) {
				throw new CoreException(new Status(IStatus.ERROR, LMLMonitorCorePlugin.getUniqueIdentifier(),
						Messages.MonitorControl_UnableToLocateLaunchController));
			}

			try {
				load();
			} catch (CoreException e) {
				/*
				 * Can't find monitor data for some reason, so just log a
				 * message but allow the monitor to be started anyway
				 */
				LMLMonitorCorePlugin.log(e.getLocalizedMessage());
			}

			final IRemoteConnection conn = getRemoteConnection(controller);

			if (conn == null) {
				throw new CoreException(new Status(IStatus.ERROR, LMLMonitorCorePlugin.getUniqueIdentifier(), NLS.bind(
						Messages.MonitorControl_UnableToLocateConnection, getConnectionName())));

			}

			if (conn == null || progress.isCanceled()) {
				return;
			}

			if (!conn.isOpen()) {
				try {
					conn.open(progress.newChild(10));
				} catch (final RemoteConnectionException e) {
					throw new CoreException(new Status(IStatus.ERROR, LMLMonitorCorePlugin.getUniqueIdentifier(), e.getMessage()));
				}
				if (!conn.isOpen()) {
					throw new CoreException(new Status(IStatus.ERROR, LMLMonitorCorePlugin.getUniqueIdentifier(),
							Messages.LMLResourceManagerMonitor_unableToOpenConnection));
				}
			}

			if (fSavedLayout == null) {
				// Load default layout from RMS configuration
				fSavedLayout = MonitorControlManager.getSystemLayout(fConfigurationName);
				// This might still be null or could contain the default layout configured in the RMS configuration
			}

			/*
			 * Check status of output files for jobs
			 */
			checkOutputFiles(fSavedJobs.toArray(new JobStatusData[0]), controller);

			/*
			 * Initialize LML classes
			 */
			IRemoteConnectionHostService hostSvc = conn.getService(IRemoteConnectionHostService.class);
			fLMLManager.openLgui(getId(), hostSvc.getUsername(), getMonitorConfigurationRequestType(controller), fSavedLayout,
					fSavedJobs.toArray(new JobStatusData[0]));
			setCacheActive(!getDefaultForceUpdate());

			/*
			 * Start monitoring job. Note that the monitoring job can fail,
			 * in which case the monitor is considered to be stopped and the
			 * active flag set appropriately.
			 */
			synchronized (this) {
				fMonitorJob = new MonitorJob(conn);
				fMonitorJob.schedule();
			}
			setActive(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.monitors.IMonitorControl#stop()
	 */
	@Override
	public void stop() throws CoreException {
		if (fMonitorJob != null) {
			synchronized (this) {
				fMonitorJob.askForStop();
				try {
					fMonitorJob.join();
				} catch (InterruptedException e) {
					// Ignore
				}
				fMonitorJob = null;
			}
		}
	}

	private void addJob(IJobStatus status) {
		ILaunchController controller = LaunchControllerManager.getInstance().getLaunchController(status.getControlId());
		if (controller != null) {
			String[][] attrs = { { JobStatusData.CONTROL_ID_ATTR, status.getControlId() },
					{ JobStatusData.MONITOR_ID_ATTR, getId() }, { JobStatusData.QUEUE_NAME_ATTR, status.getQueueName() },
					{ JobStatusData.OWNER_ATTR, status.getOwner() },
					{ JobStatusData.STDOUT_REMOTE_FILE_ATTR, status.getOutputPath() },
					{ JobStatusData.STDERR_REMOTE_FILE_ATTR, status.getErrorPath() },
					{ JobStatusData.INTERACTIVE_ATTR, Boolean.toString(status.isInteractive()) } };
			final JobStatusData data = new JobStatusData(status.getJobId(), attrs);
			data.setState(status.getState());
			data.setStateDetail(status.getStateDetail());
			if (IJobStatus.JOB_OUTERR_READY.equals(status.getStateDetail())) {
				checkOutputFile(data, controller);
			}
			fLMLManager.addUserJob(getId(), status.getJobId(), data);
		}
	}

	/**
	 * Extract the request sent to LML_DA from the target system configuration.
	 * The request can either be placed in the driver's layout or in the main driver
	 * configuration. Both configuration places are searched by this function.
	 * 
	 * @param controller
	 *            launch controller holding the parsed target system configuration
	 * @return the request, which can be forwarded to LML_DA for configuration
	 */
	private RequestType getMonitorConfigurationRequestType(ILaunchController controller) {
		// Check, if there is an LML request within the LML layout of the rm data
		MonitorType monitor = controller.getConfiguration().getMonitorData();
		if (monitor != null) {
			LayoutRoot lmlLayout = monitor.getLayout();
			if (lmlLayout != null) {
				org.eclipse.ptp.rm.jaxb.core.data.lml.RequestType rmRequest = lmlLayout.getRequest();
				// Convert lml request from rm config to lml.core request, which is return type of this function
				// Therefore marshall it to a string with the rm JAXBContext and unmarshall it from the string with the lml.core
				// JAXBContext
				Class<org.eclipse.ptp.rm.jaxb.core.data.lml.RequestType> rmRequestClass = org.eclipse.ptp.rm.jaxb.core.data.lml.RequestType.class;

				String rmRequestString = JAXBInitializationUtils.marshalData(rmRequest, rmRequestClass, "request"); //$NON-NLS-1$

				RequestType lmlCoreRequest = JAXBUtil.getInstance().unmarshalFragment(rmRequestString, RequestType.class);

				if (lmlCoreRequest != null) {
					return lmlCoreRequest;
				}
				// Otherwise, parsing failed => try the old way of getting the request
			}
		}
		// Parse at least the scheduler type
		ObjectFactory lmlFactory = new ObjectFactory();
		RequestType request = lmlFactory.createRequestType();
		final DriverType driver = lmlFactory.createDriverType();
		driver.setName(getSystemType());
		if (monitor != null) {
			List<CommandType> driverCommands = driver.getCommand();
			for (MonitorDriverType monitorDriver : monitor.getDriver()) {
				for (SimpleCommandType monitorCmd : monitorDriver.getCmd()) {
					CommandType cmd = new CommandType();
					cmd.setName(monitorCmd.getName());
					cmd.setExec(monitorCmd.getExec());
					driverCommands.add(cmd);
				}
			}
		}
		request.getDriver().add(driver);
		return request;
	}

	/**
	 * Get the remote connection specified by the controller configuration.
	 * 
	 * @param controller
	 *            controller
	 * @return connection for the monitor
	 */
	private static IRemoteConnection getRemoteConnection(ILaunchController controller) throws CoreException {
		IRemoteServicesManager svcMgr = RemoteServicesDelegate.getService(IRemoteServicesManager.class);
		IRemoteConnectionType connType = svcMgr.getConnectionType(controller.getRemoteServicesId());
		if (connType != null) {
			return connType.getConnection(controller.getConnectionName());
		}
		throw CoreExceptionUtils.newException(Messages.MonitorControl_unableToOpenRemoteConnection, null);
	}

	private File getSaveLocation() {
		return LMLMonitorCorePlugin.getDefault().getStateLocation().append(getId()).addFileExtension(XML).toFile();
	}

	private void loadJobs(IMemento memento, List<JobStatusData> jobs) {
		if (memento != null) {
			final IMemento[] children = memento.getChildren(JOB_ATTR);
			for (final IMemento child : children) {
				String[] keys = child.getAttributeKeys();
				String[][] attrs = new String[keys.length][2];
				for (int i = 0; i < keys.length; i++) {
					attrs[i][0] = keys[i];
					attrs[i][1] = child.getString(keys[i]);
				}

				final JobStatusData jobData = new JobStatusData(child.getID(), attrs);
				jobs.add(jobData);
			}
		}
	}

	private boolean loadState(IMemento memento) {
		setRemoteServicesId(memento.getString(REMOTE_SERVICES_ID_ATTR));
		setConnectionName(memento.getString(CONNECTION_NAME_ATTR));
		setConfigurationName(memento.getString(CONFIGURATION_NAME_ATTR));
		return memento.getBoolean(MONITOR_STATE);
	}

	private void saveJob(JobStatusData job, IMemento memento) {
		final IMemento jobMemento = memento.createChild(JOB_ATTR, job.getJobId());

		for (final String key : job.getKeys()) {
			jobMemento.putString(key, job.getString(key));
		}
	}

	private void saveState(IMemento memento) {
		memento.putString(REMOTE_SERVICES_ID_ATTR, getRemoteServicesId());
		memento.putString(CONNECTION_NAME_ATTR, getConnectionName());
		memento.putString(CONFIGURATION_NAME_ATTR, getConfigurationName());
		memento.putBoolean(MONITOR_STATE, isActive());
	}

	private void updateJob(IJobStatus status) {
		JobStatusData data = fLMLManager.getUserJob(getId(), status.getJobId());
		if (data != null && !data.isRemoved() && status.getControlId().equals(data.getString(JobStatusData.CONTROL_ID_ATTR))) {
			ILaunchController controller = LaunchControllerManager.getInstance().getLaunchController(status.getControlId());
			if (controller != null && IJobStatus.JOB_OUTERR_READY.equals(status.getStateDetail())) {
				checkOutputFile(data, controller);
			}
			fLMLManager.updateUserJob(getId(), status.getJobId(), status.getState(), status.getStateDetail());
		}
	}

	@Override
	public void setCacheActive(boolean active) {
		cacheActive = active;
		fLMLManager.setCachingMode(getId(), cacheActive);
		// Notify listeners about a possible change here
		MonitorControlManager.getInstance().fireMonitorUpdated(new IMonitorControl[] { this });
	}
}
