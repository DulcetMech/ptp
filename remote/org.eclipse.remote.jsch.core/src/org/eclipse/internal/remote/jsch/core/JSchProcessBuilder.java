/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.internal.remote.jsch.core;

import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.internal.remote.jsch.core.messages.Messages;
import org.eclipse.remote.core.AbstractRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.exception.RemoteConnectionException;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

public class JSchProcessBuilder extends AbstractRemoteProcessBuilder {
	private final JSchConnection fConnection;
	private final Map<String, String> fRemoteEnv = new HashMap<String, String>();
	private final Set<Character> charSet = new HashSet<Character>();

	private Map<String, String> fNewRemoteEnv = null;

	/**
	 * @since 4.0
	 */
	public JSchProcessBuilder(JSchConnection connection, List<String> command) {
		super(command);
		fConnection = connection;
		fRemoteEnv.putAll(fConnection.getEnv());

		// Create set of characters not to escape
		String trustedChars = null;
		trustedChars = "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; //$NON-NLS-1$ //$NON-NLS-2$
		trustedChars += "0123456789" + "/._-"; //$NON-NLS-1$ //$NON-NLS-2$
		CharacterIterator it = new StringCharacterIterator(trustedChars);
		for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
			charSet.add(c);
		}
	}

	/**
	 * @since 4.0
	 */
	public JSchProcessBuilder(JSchConnection connection, String... command) {
		this(connection, Arrays.asList(command));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.AbstractRemoteProcessBuilder#directory()
	 */
	@Override
	public IFileStore directory() {
		IFileStore dir = super.directory();
		IRemoteFileManager fileMgr = fConnection.getFileManager();
		if (dir == null && fileMgr != null) {
			dir = fileMgr.getResource(fConnection.getWorkingDirectory());
			directory(dir);
		}
		return dir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.AbstractRemoteProcessBuilder#environment()
	 */
	@Override
	public Map<String, String> environment() {
		if (fNewRemoteEnv == null) {
			fNewRemoteEnv = new HashMap<String, String>();
			fNewRemoteEnv.putAll(fRemoteEnv);
		}
		return fNewRemoteEnv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.AbstractRemoteProcessBuilder#getSupportedFlags ()
	 */
	@Override
	public int getSupportedFlags() {
		return ALLOCATE_PTY | FORWARD_X11;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteProcessBuilder#start(int)
	 */
	@Override
	public IRemoteProcess start(int flags) throws IOException {
		if (!fConnection.isOpen()) {
			throw new IOException(Messages.JSchProcessBuilder_Connection_is_not_open);
		}

		List<String> cmdArgs = command();
		if (cmdArgs.size() < 1) {
			throw new IndexOutOfBoundsException();
		}

		String remoteCmd = ""; //$NON-NLS-1$

		for (int i = 0; i < cmdArgs.size(); i++) {
			if (i > 0) {
				remoteCmd += " "; //$NON-NLS-1$
			}
			remoteCmd += charEscapify(cmdArgs.get(i), charSet);
		}

		/*
		 * There are two possibilities:
		 * 
		 * 1. Some environment variables have changed values, or new variables have been added. In this case we just want to
		 * send send the changed values rather than all the variables.
		 * 
		 * 2. Some of the existing variables have been removed. In this case, we need to resend the entire environment variable
		 * list.
		 */

		final List<String> env = new ArrayList<String>();
		boolean clearEnv = false;

		if (fNewRemoteEnv != null) {

			/*
			 * See if any of the existing variables have been removed
			 */
			for (String key : fRemoteEnv.keySet()) {
				if (fNewRemoteEnv.get(key) == null) {
					clearEnv = true;
					break;
				}
			}

			if (clearEnv) {
				/*
				 * Add new/changed variables
				 */
				for (Entry<String, String> entry : fNewRemoteEnv.entrySet()) {
					env.add(entry.getKey() + "=" + entry.getValue()); //$NON-NLS-1$
				}
			} else {
				/*
				 * Just add new or changed environment variables.
				 */
				for (Entry<String, String> entry : fNewRemoteEnv.entrySet()) {
					String oldValue = fRemoteEnv.get(entry.getKey());
					if (oldValue == null || !oldValue.equals(entry.getValue())) {
						env.add(entry.getKey() + "=" + entry.getValue()); //$NON-NLS-1$
					}
				}
			}
		}

		try {
			ChannelExec exec = fConnection.getExecChannel();
			String command = buildCommand(remoteCmd, env, clearEnv);
			exec.setCommand(command);
			exec.setPty((flags & ALLOCATE_PTY) == ALLOCATE_PTY);
			exec.setXForwarding((flags & FORWARD_X11) == FORWARD_X11);
			exec.connect();
			return new JSchProcess(exec, redirectErrorStream());
		} catch (RemoteConnectionException e) {
			throw new IOException(e.getMessage());
		} catch (JSchException e) {
			throw new IOException(e.getMessage());
		}
	}

	private String buildCommand(String cmd, List<String> environment, boolean clearEnv) {
		StringBuffer sb = new StringBuffer();
		if (directory() != null) {
			sb.append("cd " + charEscapify(directory().toURI().getPath(), charSet) + "; "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (clearEnv) {
			sb.append("env -i"); //$NON-NLS-1$
			for (String env : environment) {
				sb.append(" \"" + env + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			sb.append(" "); //$NON-NLS-1$
		} else {
			for (String env : environment) {
				sb.append("export \"" + env + "\"; "); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		sb.append(cmd);
		return sb.toString();
	}

	private String charEscapify(String inputString, Set<Character> charSet) {
		if (inputString == null) {
			return null;
		}
		StringBuffer newString = new StringBuffer(""); //$NON-NLS-1$
		CharacterIterator it = new StringCharacterIterator(inputString);

		for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
			if (c == '\'') {
				newString.append("'\\\\\\''"); //$NON-NLS-1$
			} else if (c > 127 || charSet.contains(c)) { // Do not escape non-ASCII characters (> 127)
				newString.append(c);
			} else {
				newString.append("\\" + c); //$NON-NLS-1$
			}
		}
		inputString = newString.toString();
		return inputString;
	}
}