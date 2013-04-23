/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizePropertiesDescriptor;

public class SynchronizePropertiesRegistry {
	public static final String SYNCHRONIZE_PROPERTIES_EXTENSION = "synchronizeProperties"; //$NON-NLS-1$

	private static List<ISynchronizePropertiesDescriptor> fAllDescriptors;

	public static ISynchronizePropertiesDescriptor[] getDescriptors() {
		loadDescriptors();
		if (fAllDescriptors != null) {
			return fAllDescriptors.toArray(new ISynchronizePropertiesDescriptor[fAllDescriptors.size()]);
		}
		return new ISynchronizePropertiesDescriptor[0];
	}

	public static ISynchronizeProperties getSynchronizePropertiesForProject(IProject project) {
		for (ISynchronizePropertiesDescriptor descriptor : getDescriptors()) {
			try {
				if (project.hasNature(descriptor.getNature())) {
					return descriptor.getProperties();
				}
			} catch (CoreException e) {
				// Ignore
			}
		}
		return null;
	}

	private static void loadDescriptors() {
		if (fAllDescriptors == null) {
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(RDTSyncUIPlugin.PLUGIN_ID,
					SYNCHRONIZE_PROPERTIES_EXTENSION);
			if (point != null) {
				fAllDescriptors = new ArrayList<ISynchronizePropertiesDescriptor>();
				for (IExtension extension : point.getExtensions()) {
					for (IConfigurationElement configElement : extension.getConfigurationElements()) {
						fAllDescriptors.add(new SynchronizePropertiesDescriptor(configElement));
					}
				}
			}
		}
	}
}
