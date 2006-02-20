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
package org.eclipse.ptp.debug.external.core;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.IDebugCommand;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIErrorEvent;

/**
 * @author Clement chu
 * 
 */
public class DebugCommandQueue extends Thread {
	private List queue = null;
	private boolean isTerminated = false;
	private IDebugCommand currentCommand = null;
	private IAbstractDebugger debugger = null;
	private int command_timeout;
	
	public DebugCommandQueue(IAbstractDebugger debugger, int timeout) {
		this.debugger = debugger;
		this.command_timeout = timeout;
		queue = Collections.synchronizedList(new LinkedList());
	}
	public void setTerminated() {
		isTerminated = true;
		cleanup();
	}
	
	public void run()  {
		while (!isTerminated) {
			if (!waitForCommand()) {
				break;
			}
			currentCommand = getCommand();
			try {
				System.out.println("***** CURRENT COMMAND: " + currentCommand);
				currentCommand.execCommand(debugger, command_timeout);
				currentCommand.waitForReturn();
			} catch (PCDIException e) {
				debugger.handleErrorEvent(currentCommand.getTasks(), "Executing " + currentCommand.getName() + " command problem - " + e.getMessage(), IPCDIErrorEvent.DBG_ERROR);
			} finally {
				currentCommand = null;
			}
		}
	}
	private boolean waitForCommand() {
		synchronized (queue) {
			try {
				while (currentCommand != null || queue.isEmpty()) {
					queue.wait();
				}
			} catch (InterruptedException e) {
				return false;
			}
			return true;
		}
	}

	public IDebugCommand getCommand() {
		synchronized (queue) {
			return (IDebugCommand)queue.remove(0);
		}
	}
	public void addCommand(IDebugCommand command) {
		synchronized (queue) {
			if (!contains(command)) {
				if (command.canInterrupt() && currentCommand != null) {
					setCommandReturn(null, null);
					try {
						//To make sure all events fired via AsbtractDebugger, so wait 0.5 sec here
						queue.wait(500);
					} catch (InterruptedException e) {}
				}
				queue.add(command);
				queue.notifyAll();
			}
			else {
				//debugger.handleErrorEvent(command.getTasks(), "Duplicate in " + command.getName() + " command", IPCDIErrorEvent.DBG_WARNING);
				System.err.println("************ ERROR in DebugCommandQueue -- duplicate, cmd: " + currentCommand);
			}
		}
	}
	private boolean contains(IDebugCommand command) {
		synchronized (queue) {
			int size = queue.size();
			if (size > 0) {
				return (((IDebugCommand)queue.get(size-1)).compareTo(command) == 0);
			}
			return false;
		}
	}
	public void flushCommands() {
		synchronized (queue) {
			IDebugCommand[] commands = (IDebugCommand[])queue.toArray(new IDebugCommand[0]);
			for (int i=commands.length-1; i>-1; i--) {
				commands[i].flush();
			}
			queue.clear();
		}
	}
	public void setCommandReturn(BitList tasks, Object result) {
		synchronized (queue) {
			if (currentCommand != null) {
				if (result == null) {
					flushCommands();
					currentCommand.flush();
				}
				else {
					currentCommand.setReturn(tasks, result);
				}
			}
		}
	}
	public void cleanup() {
		synchronized (queue) {
			//queue.clear();
			flushCommands();
		}
	}
}
