/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007, 2010 Los Alamos National Security, LLC and others.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy.  The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     LANS - Initial Implementation
 *     Dieter Krachtus, University of Heidelberg
 *     Roland Schulz, University of Tennessee
 *     Greg Watson, IBM
 *******************************************************************************/

package org.eclipse.ptp.proxy.event;

import org.eclipse.ptp.proxy.packet.ProxyPacket;

public interface IProxyEventFactory {
	/**
	 * Create a new error event.
	 * 
	 * @param transID
	 *            transaction id
	 * @return new error event
	 * @since 4.0
	 */
	public IProxyErrorEvent newErrorEvent(int transID, int code, String message);

	/**
	 * Create a new error event.
	 * 
	 * @param transID
	 *            transaction id
	 * @param args
	 *            array of arguments for the event
	 * @return new error event
	 * @since 4.0
	 */
	public IProxyErrorEvent newErrorEvent(int transID, String[] args);

	/**
	 * Create a new ok event.
	 * 
	 * @param transID
	 *            transaction id
	 * @return new ok event
	 * @since 4.0
	 */
	public IProxyOKEvent newOKEvent(int transID);

	/**
	 * Create a new shutdown event.
	 * 
	 * @param transID
	 *            transaction id
	 * @return new shutdown event
	 * @since 4.0
	 */
	public IProxyShutdownEvent newShutdownEvent(int transID);

	/**
	 * Decode event packet into a proxy event.
	 * 
	 * @param packet
	 *            packet received from proxy
	 * @return decoded event packet or null if the packet couldn't be decoded
	 */
	public IProxyEvent toEvent(ProxyPacket packet);
}
