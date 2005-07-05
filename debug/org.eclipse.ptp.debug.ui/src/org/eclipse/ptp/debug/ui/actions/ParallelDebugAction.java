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
package org.eclipse.ptp.debug.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.debug.ui.model.Element;
import org.eclipse.ptp.debug.ui.views.AbstractDebugParallelView;
import org.eclipse.swt.widgets.Shell;

/**
 * @author clement chu
 *
 */
public abstract class ParallelDebugAction extends Action {
	protected AbstractDebugParallelView debugView = null;
	
	public ParallelDebugAction(String text, AbstractDebugParallelView debugView) {
		this(text, IAction.AS_PUSH_BUTTON, debugView);
	}
	
	public ParallelDebugAction(String text, int style, AbstractDebugParallelView debugView) {
		super(text, style);
		this.debugView = debugView;
	    this.setToolTipText(text);
	    this.setEnabled(false);
	}
	
	public AbstractDebugParallelView getViewPart() {
        return debugView;
    }
    
    public Shell getShell() {
        return debugView.getViewSite().getShell();
    }
    
    public abstract void run(Element[] elements);
    
    public void run() {
    	ISelection selection = debugView.getSelection();
    	if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
        	Object[] objs = ((IStructuredSelection)selection).toArray();
        	Element[] elements = new Element[objs.length];
        	System.arraycopy(objs, 0, elements, 0, objs.length);
        	run(elements);
    	}
    	else
    		run(new Element[0]);
    }
}
