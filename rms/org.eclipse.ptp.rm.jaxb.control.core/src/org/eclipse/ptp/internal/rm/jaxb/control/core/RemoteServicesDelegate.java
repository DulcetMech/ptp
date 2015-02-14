/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ptp.internal.rm.jaxb.control.core.messages.Messages;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * A wrapper for holding initialized remote services information. <br>
 * 
 * Also contains convenience utilities associated with remote file operations.
 * 
 * @see org.eclipse.ptp.remote.core.IRemoteFileManager
 * @see org.eclipse.core.filesystem.IFileStore
 * 
 * @author arossi
 * @since 5.0
 * 
 */
public class RemoteServicesDelegate {
	private static final String COSP = ": ";//$NON-NLS-1$ 
	private static final int UNDEFINED = -1;
	private static final int COPY_BUFFER_SIZE = 64 * 1024;

	/**
	 * @param from
	 *            manager for source resource
	 * @param source
	 *            file to copy
	 * @param to
	 *            manager for target resource
	 * @param target
	 *            destination file
	 * @param mkParent
	 *            EFS.NONE = mkdir -p on the parent directory; EFS.SHALLOW = mkdir parent; UNDEFINED = no mkdir
	 * @param progress
	 * @throws CoreException
	 */
	public static void copy(IRemoteFileService from, String source, IRemoteFileService to, String target, int mkParent,
			IProgressMonitor progress) throws CoreException {
		if (from == null) {
			throw newException(Messages.RemoteServicesDelegate_Copy_Operation_NullSourceFileManager, null);
		}
		if (to == null) {
			throw newException(Messages.RemoteServicesDelegate_Copy_Operation_NullTargetFileManager, null);
		}
		if (source == null) {
			throw newException(Messages.RemoteServicesDelegate_Copy_Operation_NullSource, null);
		}
		if (target == null) {
			throw newException(Messages.RemoteServicesDelegate_Copy_Operation_NullTarget, null);
		}
		SubMonitor subProgress = SubMonitor.convert(progress, (15));
		IFileStore lres = from.getResource(source);
		if (!lres.fetchInfo(EFS.NONE, subProgress.newChild(5)).exists()) {
			throw newException(
					Messages.RemoteServicesDelegate_Copy_Operation_Local_resource_does_not_exist + COSP + lres.getName(), null);
		}
		if (mkParent != UNDEFINED) {
			to.getResource(target).getParent().mkdir(mkParent, subProgress.newChild(5));
		}
		if (subProgress.isCanceled()) {
			return;
		}
		IFileStore rres = to.getResource(target);
		lres.copy(rres, EFS.OVERWRITE, subProgress.newChild(5));
	}

	/**
	 * Replicated from core to avoid dependencies.
	 * 
	 * @param message
	 * @param t
	 * @return error status object
	 */
	private static IStatus getErrorStatus(String message, Throwable t) {
		if (t != null) {
			JAXBControlCorePlugin.log(t);
		}
		return new Status(Status.ERROR, JAXBControlCorePlugin.getUniqueIdentifier(), Status.ERROR, message, t);
	}

	/**
	 * Checks for existence of file. If it does exist, tests to see if it is stable by checking size after the given timeout.
	 * 
	 * @param manager
	 *            for resource where file is located
	 * @param path
	 *            of file
	 * @param intervalInSecs
	 *            time after which to check size of file again
	 * @param progress
	 * @return true if file exists and is not being written to over the given interval.
	 */
	public static boolean isStable(IRemoteFileService fileSvc, String path, int intervalInSecs, IProgressMonitor progress)
			throws CoreException {
		if (fileSvc == null) {
			throw newException(Messages.RemoteServicesDelegate_Read_Operation_NullSourceFileManager, null);
		}
		if (path == null) {
			throw newException(Messages.RemoteServicesDelegate_Read_Operation_NullPath, null);
		}

		IFileStore lres = fileSvc.getResource(path);
		SubMonitor subProgress = SubMonitor.convert(progress, (10));
		IFileInfo info = lres.fetchInfo(EFS.NONE, subProgress.newChild(5));
		if (subProgress.isCanceled()) {
			return false;
		}
		if (!info.exists()) {
			return false;
		}
		long l0 = info.getLength();
		try {
			Thread.sleep(1000 * intervalInSecs);
		} catch (InterruptedException ignored) {
			// Ignore
		}
		info = lres.fetchInfo(EFS.NONE, subProgress.newChild(5));
		if (subProgress.isCanceled()) {
			return false;
		}
		long l1 = info.getLength();
		return l0 == l1;
	}

	/**
	 * Replicated from core to avoid dependencies.
	 * 
	 * @param message
	 * @param t
	 * @return exception
	 */
	private static CoreException newException(String message, Throwable t) {
		return new CoreException(getErrorStatus(message, t));
	}

	/**
	 * @param manager
	 *            for resource where file is located
	 * @param path
	 *            of file
	 * @param progress
	 * @return contents of file
	 * @throws CoreException
	 */
	public static String read(IRemoteFileService fileSvc, String path, IProgressMonitor progress) throws CoreException {
		if (fileSvc == null) {
			throw newException(Messages.RemoteServicesDelegate_Read_Operation_NullSourceFileManager, null);
		}
		if (path == null) {
			throw newException(Messages.RemoteServicesDelegate_Read_Operation_NullPath, null);
		}

		IFileStore lres = fileSvc.getResource(path);
		SubMonitor subProgress = SubMonitor.convert(progress, (100));
		if (!lres.fetchInfo(EFS.NONE, subProgress.newChild(5)).exists()) {
			throw newException(Messages.RemoteServicesDelegate_Read_Operation_resource_does_not_exist + COSP + lres.getName(), null);
		}
		BufferedInputStream is = new BufferedInputStream(lres.openInputStream(EFS.NONE, progress));
		StringBuffer sb = new StringBuffer();
		byte[] buffer = new byte[COPY_BUFFER_SIZE];
		int rcvd = 0;

		try {
			while (true) {
				try {
					rcvd = is.read(buffer, 0, COPY_BUFFER_SIZE);
				} catch (EOFException eof) {
					break;
				}

				if (rcvd == UNDEFINED) {
					break;
				}

				sb.append(new String(buffer, 0, rcvd));

				if (subProgress.isCanceled()) {
					break;
				}
			}
		} catch (IOException ioe) {
			throw newException(Messages.RemoteServicesDelegate_Read_OperationFailed + path, null);
		} finally {
			try {
				is.close();
			} catch (IOException ioe) {
				JAXBControlCorePlugin.log(ioe);
			}
		}
		return sb.toString();
	}

	/**
	 * @param manager
	 *            for resource where file is to be written
	 * @param path
	 *            of file to write
	 * @param contents
	 *            to write to file
	 * @param progress
	 * @throws CoreException
	 */
	public static void write(IRemoteFileService fileSvc, String path, String contents, IProgressMonitor progress)
			throws CoreException {
		if (contents == null) {
			return;
		}
		if (fileSvc == null) {
			throw newException(Messages.RemoteServicesDelegate_Write_Operation_NullSourceFileManager, null);
		}
		if (path == null) {
			throw newException(Messages.RemoteServicesDelegate_Write_Operation_NullPath, null);
		}

		IFileStore lres = fileSvc.getResource(path);
		BufferedOutputStream os = new BufferedOutputStream(lres.openOutputStream(EFS.NONE, progress));
		if (!progress.isCanceled()) {
			try {
				os.write(contents.getBytes());
				os.flush();
			} catch (IOException ioe) {
				throw newException(Messages.RemoteServicesDelegate_Write_OperationFailed + path, ioe);
			} finally {
				try {
					os.close();
				} catch (IOException ioe) {
					JAXBControlCorePlugin.log(ioe);
				}
			}
		}
	}

	private final String remoteConnectionTypeId;
	private final String remoteConnectionName;
	private IRemoteConnectionType remoteConnectionType;
	private IRemoteConnectionType localConnectionType;
	private IRemoteConnection remoteConnection;
	private IRemoteConnection localConnection;

	private static Map<String, RemoteServicesDelegate> fDelegates = Collections
			.synchronizedMap(new HashMap<String, RemoteServicesDelegate>());

	/**
	 * @since 6.0
	 */
	public static RemoteServicesDelegate getDelegate(String remoteConectionTypeId, String remoteConnectionName,
			IProgressMonitor monitor) throws CoreException {
		RemoteServicesDelegate delegate = fDelegates.get(remoteConectionTypeId + "." + remoteConnectionName); //$NON-NLS-1$
		if (delegate == null) {
			delegate = new RemoteServicesDelegate(remoteConectionTypeId, remoteConnectionName);
			fDelegates.put(remoteConectionTypeId + "." + remoteConnectionName, delegate); //$NON-NLS-1$
		}
		delegate.initialize(monitor);
		return delegate;
	}

	/**
	 * @param remoteConectionTypeId
	 *            e.g., "local", "remotetools", "rse"
	 * @param remoteConnectionName
	 *            e.g., "ember.ncsa.illinois.edu"
	 */
	private RemoteServicesDelegate(String remoteConectionTypeId, String remoteConnectionName) {
		this.remoteConnectionTypeId = remoteConectionTypeId;
		this.remoteConnectionName = remoteConnectionName;
	}

	public IRemoteConnection getLocalConnection() {
		return localConnection;
	}

	public IRemoteFileService getLocalFileService() {
		return localConnection.getService(IRemoteFileService.class);
	}

	public URI getLocalHome() {
		IRemoteFileService fileSvc = getLocalFileService();
		return fileSvc.toURI(fileSvc.getBaseDirectory());
	}

	public IRemoteConnection getRemoteConnection() {
		return remoteConnection;
	}

	public IRemoteFileService getRemoteFileService() {
		return remoteConnection.getService(IRemoteFileService.class);
	}

	public URI getRemoteHome() {
		IRemoteFileService fileSvc = getRemoteFileService();
		return fileSvc.toURI(fileSvc.getBaseDirectory());
	}

	public IRemoteProcessBuilder getLocalProcessBuilder(List<String> command) {
		return localConnection.getService(IRemoteProcessService.class).getProcessBuilder(command);
	}

	public IRemoteProcessBuilder getRemoteProcessBuilder(List<String> command) {
		return remoteConnection.getService(IRemoteProcessService.class).getProcessBuilder(command);
	}

	/**
	 * On the basis of the passed in identifiers, constructs the local and remote services, connection manager, connection, file
	 * manager and home URIs.
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	public void initialize(IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 2);

		/*
		 * could happen on shutdown
		 */
		if (JAXBControlCorePlugin.getDefault() == null) {
			return;
		}
		IRemoteServicesManager svcsManager = getService(IRemoteServicesManager.class);

		localConnectionType = svcsManager.getLocalConnectionType();
		localConnection = localConnectionType.getConnections().get(0);

		if (remoteConnectionTypeId != null) {
			remoteConnectionType = svcsManager.getConnectionType(remoteConnectionTypeId);
			if (remoteConnectionType != null) {
				remoteConnection = remoteConnectionType.getConnection(remoteConnectionName);
				if (remoteConnection != null) {
					if (!remoteConnection.hasService(IRemoteFileService.class)) {
						throw new CoreException(new Status(IStatus.ERROR, JAXBControlCorePlugin.getUniqueIdentifier(),
								Messages.RemoteServicesDelegate_0));
					}
					if (!remoteConnection.hasService(IRemoteProcessService.class)) {
						throw new CoreException(new Status(IStatus.ERROR, JAXBControlCorePlugin.getUniqueIdentifier(),
								Messages.RemoteServicesDelegate_1));
					}
				} else {
					throw new CoreException(new Status(IStatus.ERROR, JAXBControlCorePlugin.getUniqueIdentifier(),
							Messages.RemoteServicesDelegate_2));
				}
			} else {
				throw new CoreException(new Status(IStatus.ERROR, JAXBControlCorePlugin.getUniqueIdentifier(),
						Messages.RemoteServicesDelegate_3));
			}
		} else {
			remoteConnectionType = localConnectionType;
			remoteConnection = localConnection;
		}
		/*
		 * Bug 370775 - need to open connection before obtaining working directory otherwise root ("/") will always be returned.
		 * This might cause problems if the connection to the target machine can't be opened, however there is a progress
		 * monitor that the user can cancel if this happens.
		 */
		if (!remoteConnection.isOpen()) {
			remoteConnection.open(progress.newChild(1));
		}
	}

	/**
	 * Return the OSGi service with the given service interface.
	 *
	 * @param service
	 *            service interface
	 * @return the specified service or null if it's not registered
	 */
	public static <T> T getService(Class<T> service) {
		final BundleContext context = JAXBControlCorePlugin.getDefault().getBundle().getBundleContext();
		final ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}
}
