/*******************************************************************************
 * Copyright (c) 2012 IBM and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.terminal;

import org.eclipse.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.internal.terminal.provisional.api.AbstractSettingsPage;

public class RemoteSettingsPage extends AbstractSettingsPage {
	private final RemoteSettings fTerminalSettings;
	private RemoteConnectionWidget fRemoteConnectionWidget;

	public RemoteSettingsPage(RemoteSettings settings) {
		fTerminalSettings = settings;
	}

	public void saveSettings() {
		fTerminalSettings.setRemoteServices(fRemoteConnectionWidget.getConnection().getRemoteServices().getId());
		fTerminalSettings.setConnectionName(fRemoteConnectionWidget.getConnection().getName());
	}

	public void loadSettings() {
		if (fTerminalSettings != null) {
			fRemoteConnectionWidget.setConnection(fTerminalSettings.getRemoteServices(), fTerminalSettings.getConnectionName());
		}
	}

	String get(String value, String def) {
		if (value == null || value.length() == 0) {
			return def;
		}
		return value;
	}

	public boolean validateSettings() {
		if (fRemoteConnectionWidget.getConnection() == null) {
			return false;
		}
		return true;
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);

		composite.setLayout(gridLayout);
		composite.setLayoutData(gridData);

		fRemoteConnectionWidget = new RemoteConnectionWidget(composite, SWT.NONE, null, 0, null);
		loadSettings();
	}
}
