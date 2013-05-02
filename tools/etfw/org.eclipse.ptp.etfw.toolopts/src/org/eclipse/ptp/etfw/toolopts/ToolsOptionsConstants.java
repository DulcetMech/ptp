/**********************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.etfw.toolopts;

/**
 * Constants used in the toolopts plugin
 * 
 * @author Beth Tibbitts
 * 
 */
public class ToolsOptionsConstants {

	public static final String TOOL_CONFIG_ARGUMENT_SUFFIX = "_ARGUMENT_SAVED"; //$NON-NLS-1$
	public static final String TOOL_CONFIG_STATE_SUFFIX = "_BUTTON_STATE"; //$NON-NLS-1$
	public static final String TOOL_CONFIG_DEFAULT_SUFFIX = "_ARGUMENT_DEFAULT"; //$NON-NLS-1$
	public static final String TOOL_PANE_ID_SUFFIX = ".performance.options.configuration_id_"; //$NON-NLS-1$
	public static final String TOOL_PANE_VAR_ID_SUFFIX = ".performance.options.environmentvariables.configuration_id_"; //$NON-NLS-1$
	/**
	 * This string will be swapped out for a stored project location if possible if found in an argument string. Used primarily for
	 * indicating location of
	 * selective instrumentation files generated by other plugins
	 */
	// public static final String PROJECT_LOCATION="%%REPLACE_WITH_LOCAL_DIR%%";

	public static final String PROJECT_BUILD = "%%REPLACE_WITH_BUILD_DIR%%"; //$NON-NLS-1$

	public static final String PROJECT_ROOT = "%%REPLACE_WITH_PROJECT_ROOT_DIR%%"; //$NON-NLS-1$

	public static final String CONF_VALUE = "%%REPLACE_WITH_CONFIGURATION_VALUE%%"; //$NON-NLS-1$

	public static final String PROJECT_BINARY = "%%REPLACE_WITH_PROJECT_BINARY%%"; //$NON-NLS-1$

	public static final String PROJECT_NAME = "%%REPLACE_WITH_PROJECT_NAME%%"; //$NON-NLS-1$

}
