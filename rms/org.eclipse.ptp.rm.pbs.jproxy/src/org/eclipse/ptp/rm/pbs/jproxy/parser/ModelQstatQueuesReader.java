/*******************************************************************************
 * Copyright (c) 2010 Dieter Krachtus and The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dieter Krachtus (dieter.krachtus@gmail.com) and Roland Schulz - initial API and implementation
 *    Benjamin Lindner (ben@benlabs.net)

*******************************************************************************/

package org.eclipse.ptp.rm.pbs.jproxy.parser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ptp.proxy.jproxy.IParser;
import org.eclipse.ptp.proxy.jproxy.attributes.AttributeDefinition;
import org.eclipse.ptp.proxy.jproxy.element.IElement;

public class ModelQstatQueuesReader implements IParser {
	private Set<IElement> queues;
	
	
	public Set<org.eclipse.ptp.proxy.jproxy.element.IElement> parse(
			AttributeDefinition attrDef, InputStream in) {
		Set<IElement> queues = new HashSet<IElement>();
		try {
			//qstat -Q -f is not XML - specific Reader has to be used.
			_parse(in,attrDef);
			queues = getQueues();
		} catch(IOException e) {
			System.out.println(e);
		} catch(Exception e) {
			System.out.println(e);
		}
		
//		System.err.println("queues length -> " + queues.size());
		return queues;
	}

	
	public Set<IElement> getQueues() {
		return queues;
	}
	
	private void _parse(InputStream in, AttributeDefinition attrDef) throws Exception, IOException {
		queues = new HashSet<IElement>();
		
		BufferedReader reader = new BufferedReader( new InputStreamReader(in) );
		
		Pattern queuepattern = Pattern.compile("^[Qq]ueue:(.*)$");
		Pattern keyvalpattern = Pattern.compile("^([^=]+)=(.+)$");

		String line;
		ArrayList<HashMap<String,String> > qhashes = new ArrayList<HashMap<String,String>>();
		HashMap<String,String> thisqueue = new HashMap<String,String>();
		boolean firstqueue = true;
		// parse the input file and store the individual queues as Hashmaps in qhashes
		while (( line = reader.readLine()) != null) {
			Matcher mq = queuepattern.matcher(line);

			boolean newqueue = mq.find();
			if (newqueue) {
				if (!firstqueue) {
					qhashes.add(thisqueue);
				} else {
					firstqueue = false;
				}
				thisqueue = new HashMap<String,String>();
				thisqueue.put("name", mq.group(1).trim());
				continue;
			}
		
			Matcher mkv = keyvalpattern.matcher(line);
			if (!mkv.find()) {
				continue;
			}
			if (mkv.groupCount()!=2) {
				continue;
			}
			String skey = mkv.group(1).trim().toLowerCase();
			String svalue = mkv.group(2).trim();

			thisqueue.put(skey,svalue);
		}
		if (!firstqueue) {
			qhashes.add(thisqueue);
		}	
		
		// now convert the hashmap representation into beans
		for (HashMap<String,String> q : qhashes) {
			IElement e = attrDef.createElement();
			for (String attr : attrDef.getRequiredAttributes()) {
				e.setAttribute(attr, q.get(attr));
			}
//			for (Entry<String, String> entry : q.entrySet()) {
//				e.setAttribute(entry.getKey(), entry.getValue());
//			}
			queues.add( e );
		}
	}




	
}