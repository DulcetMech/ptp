/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ptp.internal.rdt.ui.scannerinfo;

import org.eclipse.cdt.ui.newui.SymbolTab;
import org.eclipse.swt.widgets.Composite;


/**
 * Reuse the CDT symbol tab but add the buttons for import/export.
 *
 */
public class RemoteSymbolTab extends SymbolTab {
	
	@Override
	public void createControls(final Composite parent) {
		super.createControls(parent);
		ImportExportWizardButtons.createControls(usercomp, page.getElement());
	}
	
}
