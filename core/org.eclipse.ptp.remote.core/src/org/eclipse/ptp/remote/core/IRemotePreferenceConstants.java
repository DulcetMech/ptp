/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.core;

/**
 * Preference constants that can be used to check the default remote service provider preference.
 * 
 * @since 6.0
 */
public interface IRemotePreferenceConstants {
	/**
	 * Preference setting for the default remote services provider. Clients can check this preference to see if a default provider
	 * has been set, and if so, what the provider ID is.
	 */
	public static final String PREF_REMOTE_SERVICES_ID = "remoteServicesId"; //$NON-NLS-1$

	/**
	 * ID of the Remote Tools provider
	 * 
	 * @since 6.0
	 */
	public static final String REMOTE_TOOLS_REMOTE_SERVICES_ID = "org.eclipse.ptp.remote.RemoteTools"; //$NON-NLS-1$

	/**
	 * ID of the RSE provider
	 * 
	 * @since 6.0
	 */
	public static final String RSE_REMOTE_SERVICES_ID = "org.eclipse.ptp.remote.RSERemoteServices"; //$NON-NLS-1$
}
