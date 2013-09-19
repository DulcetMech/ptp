/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core;

import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.remote.core.IRemoteResource;

public class RemoteResource implements IRemoteResource {
	private IResource fResource;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteResource#getDefaultLocationURI(org.eclipse.core.resources.IResource)
	 */
	public URI getActiveLocationURI() {
			return fResource.getLocationURI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteResource#refresh(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void refresh(IProgressMonitor monitor) throws CoreException {
		fResource.refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteResource#getResource()
	 */
	public IResource getResource() {
		return fResource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteResource#setResource(org.eclipse.core.resources.IResource)
	 */
	public void setResource(IResource resource) {
		fResource = resource;
	}
}