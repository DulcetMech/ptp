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
/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.external.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariableDescriptor;
import org.eclipse.ptp.debug.external.IDebugger;
import org.eclipse.ptp.debug.external.PTPDebugExternalPlugin;
import org.eclipse.ptp.debug.external.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.Thread;
import org.eclipse.ptp.debug.external.cdi.model.variable.Argument;
import org.eclipse.ptp.debug.external.cdi.model.variable.ArgumentDescriptor;
import org.eclipse.ptp.debug.external.cdi.model.variable.GlobalVariable;
import org.eclipse.ptp.debug.external.cdi.model.variable.GlobalVariableDescriptor;
import org.eclipse.ptp.debug.external.cdi.model.variable.LocalVariable;
import org.eclipse.ptp.debug.external.cdi.model.variable.LocalVariableDescriptor;
import org.eclipse.ptp.debug.external.cdi.model.variable.Variable;
import org.eclipse.ptp.debug.external.cdi.model.variable.VariableDescriptor;

/**
 */
public class VariableManager extends Manager {

	public VariableManager(Session session) {
		super(session, true);
	}

	protected void update(Target target) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");	}
	
	public GlobalVariableDescriptor getGlobalVariableDescriptor(Target target, String filename, String function, String name) throws CDIException {
		if (filename == null) {
			filename = new String();
		}
		if (function == null) {
			function = new String();
		}
		if (name == null) {
			name = new String();
		}
		StringBuffer buffer = new StringBuffer();
		if (filename.length() > 0) {
			buffer.append('\'').append(filename).append('\'').append("::"); //$NON-NLS-1$
		}
		if (function.length() > 0) {
			buffer.append(function).append("::"); //$NON-NLS-1$
		}
		buffer.append(name);
		return new GlobalVariableDescriptor(target, null, null, buffer.toString(), null, 0, 0);
	}

	public ICDIArgumentDescriptor[] getArgumentDescriptors(StackFrame frame) throws CDIException {
		List argObjects = new ArrayList();
		Target target = (Target)frame.getTarget();
		Session session = (Session) target.getSession();
		IDebugger debugger = session.getDebugger();
		ICDIArgument[] args = debugger.listArguments(session.createBitList(target.getTargetId()), frame);
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				Thread thread = (Thread) ((VariableDescriptor) args[i]).getThread();
				String name = args[i].getName();
				String fName = args[i].getQualifiedName();
				int pos = ((VariableDescriptor) args[i]).getPosition();
				int depth = ((VariableDescriptor) args[i]).getStackDepth();
				ArgumentDescriptor arg = new ArgumentDescriptor(target, thread, frame, name, fName, pos, depth);
				argObjects.add(arg);
			}
		}
		return (ICDIArgumentDescriptor[]) argObjects.toArray(new ICDIArgumentDescriptor[0]);
	}
	
	public ICDILocalVariableDescriptor[] getLocalVariableDescriptors(StackFrame frame) throws CDIException {
		List argObjects = new ArrayList();
		Target target = (Target)frame.getTarget();
		Session session = (Session) target.getSession();
		IDebugger debugger = session.getDebugger();
		ICDILocalVariable[] args = debugger.listLocalVariables(session.createBitList(target.getTargetId()), frame);
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				Thread thread = (Thread) ((VariableDescriptor) args[i]).getThread();
				String name = args[i].getName();
				String fName = args[i].getQualifiedName();
				int pos = ((VariableDescriptor) args[i]).getPosition();
				int depth = ((VariableDescriptor) args[i]).getStackDepth();
				LocalVariableDescriptor arg = new LocalVariableDescriptor(target, thread, frame, name, fName, pos, depth);
				argObjects.add(arg);
			}
		}
		return (ICDILocalVariableDescriptor[]) argObjects.toArray(new ICDILocalVariableDescriptor[0]);
	}
	
	public Variable createVariable(VariableDescriptor varDesc) throws CDIException {
		if (varDesc instanceof ArgumentDescriptor) {
			return createArgument((ArgumentDescriptor)varDesc);
		} else if (varDesc instanceof LocalVariableDescriptor) {
			return createLocalVariable((LocalVariableDescriptor)varDesc);
		} else if (varDesc instanceof GlobalVariableDescriptor) {
			return createGlobalVariable((GlobalVariableDescriptor)varDesc);
		}
		throw new CDIException(CDIResources.getString("cdi.VariableManager.Unknown_variable_object")); //$NON-NLS-1$			
	}
	
	public LocalVariable createLocalVariable(LocalVariableDescriptor varDesc) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		StackFrame frame = (StackFrame) varDesc.getStackFrame();
		Target target = (Target)frame.getTarget();
		Session session = (Session) target.getSession();
		IDebugger debugger = session.getDebugger();
		ICDILocalVariable[] vars = debugger.listLocalVariables(session.createBitList(target.getTargetId()), frame);
		for (int i = 0; i < vars.length; i++) {
			if (varDesc.getName().equals(vars[i].getName()))
				return new LocalVariable(varDesc);
		}
		throw new CDIException(CDIResources.getString("cdi.VariableManager.Unknown_variable_object")); //$NON-NLS-1$
	}
	
	public GlobalVariable createGlobalVariable(GlobalVariableDescriptor varDesc) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		StackFrame frame = (StackFrame) varDesc.getStackFrame();
		Target target = (Target)frame.getTarget();
		Session session = (Session) target.getSession();
		IDebugger debugger = session.getDebugger();
		ICDIGlobalVariable[] vars = debugger.listGlobalVariables(session.createBitList(target.getTargetId()));
		for (int i = 0; i < vars.length; i++) {
			if (varDesc.getName().equals(vars[i].getName()))
				return new GlobalVariable(varDesc);
		}
		throw new CDIException(CDIResources.getString("cdi.VariableManager.Unknown_variable_object")); //$NON-NLS-1$
	}

	public Argument createArgument(ArgumentDescriptor argDesc) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		StackFrame frame = (StackFrame) argDesc.getStackFrame();
		Target target = (Target)frame.getTarget();
		Session session = (Session) target.getSession();
		IDebugger debugger = session.getDebugger();
		ICDIArgument[] vars = debugger.listArguments(session.createBitList(target.getTargetId()), frame);
		for (int i = 0; i < vars.length; i++) {
			if (argDesc.getName().equals(vars[i].getName()))
				return new Argument(argDesc);
		}
		throw new CDIException(CDIResources.getString("cdi.VariableManager.Unknown_variable_object")); //$NON-NLS-1$
	}

}
