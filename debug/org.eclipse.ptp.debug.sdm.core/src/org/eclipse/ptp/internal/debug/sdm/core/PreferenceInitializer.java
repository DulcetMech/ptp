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
package org.eclipse.ptp.internal.debug.sdm.core;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ptp.core.Preferences;

/**
 * @author Clement chu
 * 
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer# initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		Preferences.setDefaultString(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.PREFS_SDM_BACKEND, "gdb-mi"); //$NON-NLS-1$
		for (String backend : SDMDebugCorePlugin.getDefault().getDebuggerBackends()) {
			Preferences.setDefaultString(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.PREFS_SDM_BACKEND_PATH
					+ backend, SDMDebugCorePlugin.getDefault().getDebuggerBackendPath(backend));
			Preferences.setDefaultString(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.PREFS_SDM_PATH + backend,
					SDMDebugCorePlugin.getDefault().getDebuggerSDMPath(backend));
			Preferences.setDefaultBoolean(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.PREFS_USE_BUILTIN_SDM
					+ backend, SDMDebugCorePlugin.getDefault().getDebuggerUseBuiltin(backend));
		}
	}
}
