/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.terminal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.InvalidRegistryObjectException;

/**
 * This class exists only to supply a name to a terminal object.
 * 
 * @author Steven R. Brandt
 *
 */
public class TitleConfigurationElement implements IConfigurationElement {

	private final String title;

	public TitleConfigurationElement(String title) {
		this.title = title;
	}

	@Override
	public Object createExecutableExtension(String propertyName) throws CoreException {
		return null;
	}

	@Override
	public String getAttribute(String name) throws InvalidRegistryObjectException {
		if ("name".equals(name)) { //$NON-NLS-1$
			return title;
		}
		return null;
	}

	@Override
	public String getAttribute(String attrName, String locale) throws InvalidRegistryObjectException {
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getAttributeAsIs(String name) throws InvalidRegistryObjectException {
		return null;
	}

	@Override
	public String[] getAttributeNames() throws InvalidRegistryObjectException {
		return null;
	}

	@Override
	public IConfigurationElement[] getChildren() throws InvalidRegistryObjectException {
		return null;
	}

	@Override
	public IConfigurationElement[] getChildren(String name) throws InvalidRegistryObjectException {
		return null;
	}

	@Override
	public IExtension getDeclaringExtension() throws InvalidRegistryObjectException {
		return null;
	}

	@Override
	public String getName() throws InvalidRegistryObjectException {
		return null;
	}

	@Override
	public Object getParent() throws InvalidRegistryObjectException {
		return null;
	}

	@Override
	public String getValue() throws InvalidRegistryObjectException {
		return null;
	}

	@Override
	public String getValue(String locale) throws InvalidRegistryObjectException {
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getValueAsIs() throws InvalidRegistryObjectException {
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getNamespace() throws InvalidRegistryObjectException {
		return null;
	}

	@Override
	public String getNamespaceIdentifier() throws InvalidRegistryObjectException {
		return null;
	}

	@Override
	public IContributor getContributor() throws InvalidRegistryObjectException {
		return null;
	}

	@Override
	public boolean isValid() {
		return false;
	}

}
