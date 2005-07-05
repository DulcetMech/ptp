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
package org.eclipse.ptp.debug.ui.model;


/**
 * @author clement chu
 *
 */
public class Element implements Cloneable, Comparable {
	protected int id = 0;
	protected boolean selected = false; 
	
	public Element(int id, boolean selected) {
		this.id = id;
	}
	public Element(int id) {
		this(id, false);
	}
	public int getID() {
		return id;
	}
	public boolean isSelected() {
		return selected;
	}	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public Element cloneElement() {
		try {
			return (Element)clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	/**
	 * @param obj compare to
	 * @return -1 if smaller than obj, 1 if bigger than obj, otherwise they equal
	 */
	public int compareTo(Object obj) {
		if (obj instanceof Element) {
			int my_rank = id;
			int his_rank = ((Element) obj).getID();
			if (my_rank < his_rank)
				return -1;
			if (my_rank == his_rank)
				return 0;
			if (my_rank > his_rank)
				return 1;
		}
		return 0;
	}	
}
