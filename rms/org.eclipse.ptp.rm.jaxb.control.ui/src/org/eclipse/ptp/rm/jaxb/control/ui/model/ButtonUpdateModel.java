/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.ui.model;

import java.util.List;

import org.eclipse.ptp.rm.jaxb.control.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;

/**
 * Update Model for Button widgets.
 * 
 * @author arossi
 * 
 */
public class ButtonUpdateModel extends AbstractUpdateModel implements SelectionListener {

	private final Button button;
	private final String[] valueMapping;

	/**
	 * @param name
	 *            name of the model, which will correspond to the name of a
	 *            Property or Attribute if the widget value is to be saved.
	 * @param linkUpdateTo
	 *            if a change in this property or attribute value overwrites
	 *            other property or attribute values
	 * @param handler
	 *            the handler for notifying other widgets to refresh their
	 *            values
	 * @param button
	 *            the widget to which this model corresponds
	 * @param translateBooleanAs
	 *            a comma-delimite pair of strings corresponding to T,F to use
	 *            as the value instead of the boolean (can be <code>null</code>)
	 */
	public ButtonUpdateModel(String name, List<String> linkUpdateTo, ValueUpdateHandler handler, Button button,
			String translateBooleanAs) {
		super(name, linkUpdateTo, handler);
		this.button = button;
		button.addSelectionListener(this);
		if (translateBooleanAs == null) {
			valueMapping = null;
		} else {
			String[] pair = translateBooleanAs.split(JAXBUIConstants.CM);
			if (pair.length == 2) {
				valueMapping = pair;
			} else {
				valueMapping = null;
			}
		}
	}

	@Override
	public Object getControl() {
		return button;
	}

	/*
	 * @return value of the selection; boolean or string (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.IUpdateModel#getValueFromControl()
	 */
	public Object getValueFromControl() {
		Boolean b = button.getSelection();
		if (valueMapping != null) {
			return b ? valueMapping[0] : valueMapping[1];
		}
		return b;
	}

	/*
	 * Sets the selection on the button after translating the value from the
	 * current environment. Turns on the refreshing flag so as not to trigger
	 * further updates from the listener. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.IUpdateModel#refreshValueFromMap()
	 */
	public void refreshValueFromMap() {
		refreshing = true;
		mapValue = lcMap.get(name);
		boolean b = false;
		if (mapValue != null) {
			if (mapValue instanceof String) {
				if (valueMapping != null) {
					b = mapValue.equals(valueMapping[0]);
				} else {
					b = Boolean.parseBoolean((String) mapValue);
				}
			} else {
				b = (Boolean) mapValue;
			}
		}
		button.setSelection(b);
		refreshing = false;
	}

	/*
	 * Model serves as widget selection listener: calls {@link #storeValue()}
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse
	 * .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/*
	 * Model serves as widget selection listener: calls {@link #storeValue()}
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt
	 * .events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		if (refreshing) {
			return;
		}
		Object value = storeValue();
		handleUpdate(value);
	}
}