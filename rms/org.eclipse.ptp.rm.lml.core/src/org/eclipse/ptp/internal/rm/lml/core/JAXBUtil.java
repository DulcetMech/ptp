/**
 * Copyright (c) 2012-2014 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch, Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.internal.rm.lml.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.ptp.internal.rm.lml.core.model.LguiItem;
import org.eclipse.ptp.rm.lml.core.ILMLCoreConstants;
import org.eclipse.ptp.rm.lml.core.elements.AbslayoutType;
import org.eclipse.ptp.rm.lml.core.elements.ChartType;
import org.eclipse.ptp.rm.lml.core.elements.ChartgroupType;
import org.eclipse.ptp.rm.lml.core.elements.ComponentType;
import org.eclipse.ptp.rm.lml.core.elements.ComponentlayoutType;
import org.eclipse.ptp.rm.lml.core.elements.DataType;
import org.eclipse.ptp.rm.lml.core.elements.GobjectType;
import org.eclipse.ptp.rm.lml.core.elements.InfoboxType;
import org.eclipse.ptp.rm.lml.core.elements.JobType;
import org.eclipse.ptp.rm.lml.core.elements.LayoutRoot;
import org.eclipse.ptp.rm.lml.core.elements.LayoutType;
import org.eclipse.ptp.rm.lml.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.core.elements.NodedisplaylayoutType;
import org.eclipse.ptp.rm.lml.core.elements.ObjectFactory;
import org.eclipse.ptp.rm.lml.core.elements.PaneType;
import org.eclipse.ptp.rm.lml.core.elements.PatternMatchType;
import org.eclipse.ptp.rm.lml.core.elements.PatternType;
import org.eclipse.ptp.rm.lml.core.elements.SchemeType;
import org.eclipse.ptp.rm.lml.core.elements.SelectType;
import org.eclipse.ptp.rm.lml.core.elements.SplitlayoutType;
import org.eclipse.ptp.rm.lml.core.elements.TableType;
import org.eclipse.ptp.rm.lml.core.elements.TablelayoutType;
import org.eclipse.ptp.rm.lml.core.elements.TextboxType;
import org.eclipse.ptp.rm.lml.core.elements.UsageType;
import org.eclipse.ptp.rm.lml.core.elements.UsagebarType;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Encapsulates all operations working on JAXB classes.
 * Handles marshalling and unmarshalling of LML files.
 * It implements the Singleton pattern, so that only one marshaller/unmarshaller
 * instance per program is generated. It represents a collection
 * of helper functions for all LML handling classes.
 * 
 * @author Carsten Karbach
 * 
 */
public class JAXBUtil {

	private static JAXBUtil jaxbUtil;

	private static Unmarshaller unmarshaller;

	private static Marshaller marshaller;

	private static String JAXB_CLASSES = "org.eclipse.ptp.rm.lml.core.elements";//$NON-NLS-1$

	private static String SCHEMA_LOCATION = "jaxb.schemaLocation";//$NON-NLS-1$
	private static String SCHEMA_FILE = "http://eclipse.org/ptp/schemas/v1.1/lgui.xsd";//$NON-NLS-1$
	private static String SCHEMA_LGUI = "lgui";//$NON-NLS-1$
	private static String SCHEMA_LAYOUT = "layout";//$NON-NLS-1$
	private static String SCHEMA_LML = "lml";//$NON-NLS-1$
	private static String SCHEMA_NAMESPACE = "http://eclipse.org/ptp/lml";//$NON-NLS-1$

	private static Validator validator;

	/**
	 * Create an UsageType instance from given parameters.
	 * Set the total amount of cpus and add job-tags for each
	 * job to amount mapping within the jobMap.
	 * 
	 * @param totalCPUCount
	 *            total amount of cpus
	 * @param jobMap
	 *            mapping of jobnames to cpu amounts covered by these jobs
	 * @return usagetag to insert into a nodedisplay
	 */
	public static UsageType createUsageType(int totalCPUCount, HashMap<String, Integer> jobMap) {
		final ObjectFactory objectFactory = new ObjectFactory();
		final UsageType result = objectFactory.createUsageType();

		result.setCpucount(BigInteger.valueOf(totalCPUCount));

		for (final String jobName : jobMap.keySet()) {
			final JobType job = objectFactory.createJobType();
			job.setCpucount(BigInteger.valueOf(jobMap.get(jobName)));
			job.setOid(jobName);

			result.getJob().add(job);
		}
		return result;
	}

	public static JAXBUtil getInstance() {
		if (jaxbUtil == null) {
			jaxbUtil = new JAXBUtil();
		}
		return jaxbUtil;
	}

	/**
	 * Take a graphical object and minimize the data so that this instance is
	 * valid against the LML-Schema but at the same time as small as possible.
	 * 
	 * @param gobject
	 * @return a copy of gobj with minimal size, only attributes in GobjectType
	 *         are copied and lower special elements which are needed to make
	 *         lml-model valid
	 */
	@SuppressWarnings("unchecked")
	public static JAXBElement<GobjectType> minimizeGobjectType(GobjectType gobject, ObjectFactory objectFactory) {

		String qName = null;
		GobjectType value = null;

		if (gobject instanceof TableType) {
			value = objectFactory.createTableType();
			final TableType origin = (TableType) gobject;
			((TableType) value).setContenttype(origin.getContenttype());

			qName = ILMLCoreConstants.TABLE_ELEMENT;
		} else if (gobject instanceof UsagebarType) {
			value = objectFactory.createUsagebarType();

			((UsagebarType) value).setCpucount(BigInteger.valueOf(0));

			qName = ILMLCoreConstants.USAGEBAR_ELEMENT;
		} else if (gobject instanceof TextboxType) {
			value = objectFactory.createTextboxType();
			((TextboxType) value).setText(ILMLCoreConstants.ZEROSTR);

			qName = ILMLCoreConstants.TEXT_ELEMENT;
		} else if (gobject instanceof InfoboxType) {
			value = objectFactory.createInfoboxType();

			qName = ILMLCoreConstants.INFOBOX_ELEMENT;
		} else if (gobject instanceof Nodedisplay) {
			value = objectFactory.createNodedisplay();
			final SchemeType scheme = objectFactory.createSchemeType();
			scheme.getEl1().add(objectFactory.createSchemeElement1());
			((Nodedisplay) value).setScheme(scheme);

			final DataType dat = objectFactory.createDataType();
			dat.getEl1().add(objectFactory.createDataElement1());
			((Nodedisplay) value).setData(dat);

			qName = ILMLCoreConstants.NODEDISPLAY_ELEMENT;
		} else if (gobject instanceof ChartType) {
			value = objectFactory.createChartType();

			qName = ILMLCoreConstants.CHART_ELEMENT;
		} else if (gobject instanceof ChartgroupType) {
			final ChartgroupType chartgroupType = objectFactory.createChartgroupType();
			// Add lower chart-elements to the minimized chart-group
			// Go through all charts minimize them and add them to ut
			for (final ChartType chart : ((ChartgroupType) gobject).getChart()) {
				final ChartType minimal = (ChartType) (minimizeGobjectType(chart, objectFactory).getValue());
				chartgroupType.getChart().add(minimal);
			}

			value = chartgroupType;

			qName = ILMLCoreConstants.CHARTGROUP_ELEMENT;
		}

		if (value != null) {
			value.setDescription(gobject.getDescription());
			value.setId(gobject.getId());
			value.setTitle(gobject.getTitle());
		}

		return new JAXBElement<GobjectType>(new QName(qName), (Class<GobjectType>) gobject.getClass(), value);
	}

	/**
	 * Replace a global layout within the lml-model by a new one
	 * 
	 * @param newLayout
	 *            new layout, which replaces the old in model with the same id
	 * @param lgui
	 *            lgui-instance, which is changed
	 */
	@SuppressWarnings("unchecked")
	public static void replaceGlobalLayout(LguiType newLayout, LguiType lgui) {

		for (final JAXBElement<?> layout : newLayout.getObjectsAndRelationsAndInformation()) {
			if (layout.getValue() instanceof LayoutType) {
				boolean replaced = false;
				for (final JAXBElement<?> object : lgui.getObjectsAndRelationsAndInformation()) {
					if (object.getValue() instanceof LayoutType) {
						if (((LayoutType) object.getValue()).getId().equals(((LayoutType) layout.getValue()).getId())) {
							((JAXBElement<LayoutType>) object).setValue((LayoutType) layout.getValue());
							replaced = true;
							break;
						}
					}
				}
				if (!replaced) {
					lgui.getObjectsAndRelationsAndInformation().add(layout);
				}
			}
		}
	}

	/**
	 * Search for gid-attributes of a pane and put it into neededComponents
	 * Recursively search all graphical objects referenced by this pane
	 * 
	 * @param pane
	 *            part of SplitLayout, which is scanned for gid-attributes
	 * @param components
	 *            resulting Hashset
	 */
	private static void collectComponents(PaneType pane, HashSet<String> components) {

		if (pane.getGid() != null) {
			components.add(pane.getGid());
		} else {
			// top and bottom components?
			if (pane.getBottom() != null) {
				collectComponents(pane.getBottom(), components);
				collectComponents(pane.getTop(), components);
			} else {// Left and right
				collectComponents(pane.getLeft(), components);
				collectComponents(pane.getRight(), components);
			}
		}

	}

	/**
	 * Generates the jaxbelement corresponding to the layout instance.
	 * This element can be marshalled to XML by the marshaller.
	 * 
	 * @param rootValue
	 *            layout instance
	 * @return jaxbelement, which can be marshalled
	 */
	private static JAXBElement<LayoutRoot> createRootLayout(LayoutRoot rootValue) {
		final QName qname = new QName(SCHEMA_NAMESPACE, SCHEMA_LAYOUT, SCHEMA_LML);
		return new JAXBElement<LayoutRoot>(qname, LayoutRoot.class, rootValue);
	}

	/**
	 * Generates the jaxbelement corresponding to the lgui instance.
	 * This element can be marshalled to XML by the marshaller.
	 * 
	 * @param rootValue
	 *            lgui instance
	 * @return jaxbelement, which can be marshalled
	 */
	private static JAXBElement<LguiType> createRootLgui(LguiType rootValue) {
		final QName qname = new QName(SCHEMA_NAMESPACE, SCHEMA_LGUI, SCHEMA_LML);
		return new JAXBElement<LguiType>(qname, LguiType.class, rootValue);
	}

	/**
	 * * Uses the ResourceManagerData schema.
	 * 
	 * @return static singleton
	 * @throws IOException
	 * @throws SAXException
	 */
	private synchronized static Validator getValidator() throws IOException, SAXException {
		if (validator == null) {

			final URL xsd = LMLCorePlugin.getResource(ILMLCoreConstants.RM_XSD);
			final SchemaFactory factory = SchemaFactory.newInstance(ILMLCoreConstants.XMLSchema);
			final Schema schema = factory.newSchema(xsd);
			validator = schema.newValidator();
		}
		return validator;
	}

	private synchronized static void validate(Source source) throws SAXException, IOException, URISyntaxException {
		try {
			getValidator().validate(source);
		} catch (final SAXParseException sax) {
			throw sax;
		}
	}

	private JAXBUtil() {
		jaxbUtil = this;
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(JAXB_CLASSES);
			marshaller = jaxbContext.createMarshaller();
			try {
				marshaller.setProperty(SCHEMA_LOCATION, SCHEMA_NAMESPACE + " " + SCHEMA_FILE); //$NON-NLS-1$
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			} catch (final PropertyException exc) {
				// Dont fail just because of missing property
			}

			unmarshaller = jaxbContext.createUnmarshaller();

		} catch (final JAXBException e) {
			marshaller = null;
			unmarshaller = null;
			// TODO Error Message?
		}
	}

	public void addComponentLayoutElement(LguiType lgui, ComponentlayoutType component) {
		if (component instanceof TablelayoutType) {
			lgui.getObjectsAndRelationsAndInformation().add(
					new JAXBElement<TablelayoutType>(new QName(ILMLCoreConstants.TABLELAYOUT_ELEMENT), TablelayoutType.class,
							(TablelayoutType) component));
		} else if (component instanceof NodedisplaylayoutType) {
			lgui.getObjectsAndRelationsAndInformation().add(
					new JAXBElement<NodedisplaylayoutType>(new QName(ILMLCoreConstants.NODEDISPLAYLAYOUT_ELEMENT),
							NodedisplaylayoutType.class, (NodedisplaylayoutType) component));
		} else {
			lgui.getObjectsAndRelationsAndInformation().add(
					new JAXBElement<ComponentlayoutType>(new QName(ILMLCoreConstants.COMPONENTLAYOUT_ELEMENT),
							ComponentlayoutType.class, component));
		}
	}

	public void addLayoutElement(LguiType lgui, Object object) {
		if (object instanceof SplitlayoutType) {
			lgui.getObjectsAndRelationsAndInformation().add(
					new JAXBElement<SplitlayoutType>(new QName(ILMLCoreConstants.SPLITLAYOUT_ELEMENT), SplitlayoutType.class,
							(SplitlayoutType) object));
		} else if (object instanceof AbslayoutType) {
			lgui.getObjectsAndRelationsAndInformation().add(
					new JAXBElement<AbslayoutType>(new QName(ILMLCoreConstants.ABSLAYOUT_ELEMENT), AbslayoutType.class,
							(AbslayoutType) object));
		}

	}

	/**
	 * Add a new created layout to the model
	 * 
	 * @param layout
	 *            absolute or splitlayout
	 */
	public void addLayoutTag(LguiType lgui, ILguiItem lguiItem, LayoutType layout) {

		if (layout.getId() == null) {
			layout.setId(ILMLCoreConstants.ZEROSTR);
		}

		JAXBElement<? extends LayoutType> jaxbElement = null;
		// Create jaxbelement corresponding to the class-type
		if (layout instanceof AbslayoutType) {

			final AbslayoutType absLayout = (AbslayoutType) layout;

			jaxbElement = new JAXBElement<AbslayoutType>(new QName(ILMLCoreConstants.ABSLAYOUT_ELEMENT), AbslayoutType.class,
					absLayout);

		} else if (layout instanceof SplitlayoutType) {

			final SplitlayoutType splitLayout = (SplitlayoutType) layout;

			jaxbElement = new JAXBElement<SplitlayoutType>(new QName(ILMLCoreConstants.SPLITLAYOUT_ELEMENT), SplitlayoutType.class,
					splitLayout);

		} else {
			return;
		}

		lgui.getObjectsAndRelationsAndInformation().add(jaxbElement);

		lguiItem.notifyListeners();

	}

	/**
	 * Adds all layout definitions (splitlayout/abslayout) and all component layout elements
	 * from the data object to the request.
	 * 
	 * @param request
	 *            LML layout request, which has to be filled
	 * @param data
	 *            LML data model
	 */
	public void addLayoutTagsToRequest(LayoutRoot request, LguiType data) {

		for (final JAXBElement<?> child : data.getObjectsAndRelationsAndInformation()) {
			if (child.getValue() instanceof ComponentlayoutType || child.getValue() instanceof SplitlayoutType
					|| child.getValue() instanceof AbslayoutType) {
				request.getTextlayoutAndInfoboxlayoutAndTablelayout().add(child);

				if (child.getValue() instanceof ComponentlayoutType) {
					((ComponentlayoutType) child.getValue()).setActive(true);
				}
			}
		}

		if (data.getRequest() != null) {
			request.setRequest(data.getRequest());
		}
	}

	/**
	 * The request is already filled with nodedisplaylayout elements.
	 * Now the scheme tags of the corresponding nodedisplay tags should be
	 * transferred to the nodedisplay layout as scheme hints.
	 * This function adjusts only the nodedisplay layout tags in the request.
	 * 
	 * @param request
	 *            new LML layout
	 * @param data
	 *            last LML data
	 */
	public void addNodedisplaySchemeHints(LayoutRoot request, LguiType data) {
		// Find nodedisplays identified by their IDs
		final Map<String, Nodedisplay> gidToNodedisplay = new HashMap<String, Nodedisplay>();
		for (final JAXBElement<?> child : data.getObjectsAndRelationsAndInformation()) {
			if (child.getValue() instanceof Nodedisplay) {
				final Nodedisplay display = (Nodedisplay) child.getValue();
				gidToNodedisplay.put(display.getId(), display);
			}
		}
		// Feed nodedisplay layouts with scheme hints
		for (final JAXBElement<?> child : request.getTextlayoutAndInfoboxlayoutAndTablelayout()) {
			if (child.getValue() instanceof NodedisplaylayoutType) {
				final NodedisplaylayoutType layout = (NodedisplaylayoutType) child.getValue();
				final String gid = layout.getGid();
				final Nodedisplay display = gidToNodedisplay.get(gid);

				if (display != null) {
					layout.setSchemehint(display.getScheme());
				}
			}
		}
	}

	public void addPatternInclude(PatternType pattern, PatternMatchType patternMatch) {
		pattern.getIncludeAndExcludeAndSelect().add(
				new JAXBElement<PatternMatchType>(new QName(ILMLCoreConstants.INCLUDE_ELEMENT), PatternMatchType.class,
						patternMatch));
	}

	public void addPatternSelect(PatternType pattern, SelectType select) {
		pattern.getIncludeAndExcludeAndSelect().add(
				new JAXBElement<SelectType>(new QName(ILMLCoreConstants.SELECT_ELEMENT), SelectType.class, select));
	}

	public void addTable(LguiType lgui, TableType table) {
		lgui.getObjectsAndRelationsAndInformation().add(
				new JAXBElement<TableType>(new QName(ILMLCoreConstants.TABLE_ELEMENT), TableType.class, table));
	}

	/**
	 * The request is already filled with tablelayout elements.
	 * Now the table layouts have to be filled with contenttype hints.
	 * These hints are simply copied from the table tags.
	 * 
	 * @param request
	 *            new LML layout
	 * @param data
	 *            last LML data
	 */
	public void addTableContenttypeHints(LayoutRoot request, LguiType data) {
		// Find tables identified by their IDs
		final Map<String, TableType> gidToTable = new HashMap<String, TableType>();
		for (final JAXBElement<?> child : data.getObjectsAndRelationsAndInformation()) {
			if (child.getValue() instanceof TableType) {
				final TableType table = (TableType) child.getValue();
				gidToTable.put(table.getId(), table);
			}
		}
		// Feed nodedisplay layouts with scheme hints
		for (final JAXBElement<?> child : request.getTextlayoutAndInfoboxlayoutAndTablelayout()) {
			if (child.getValue() instanceof TablelayoutType) {
				final TablelayoutType layout = (TablelayoutType) child.getValue();
				final String gid = layout.getGid();
				final TableType table = gidToTable.get(gid);

				if (table != null) {
					layout.setContenthint(table.getContenttype());
				}
			}
		}
	}

	/**
	 * Convert layout object into lgui instance. Each LML layout is
	 * a subset of an entire LML instance with data. Thus, lml:lgui
	 * could more or less inherit from lml:layout.
	 * 
	 * @param request
	 *            LML layout request, which has to be filled
	 * @param data
	 *            LML data model
	 */
	public LguiType convertLayoutToLgui(LayoutRoot request) {

		final LguiType result = new ObjectFactory().createLguiType();
		result.setLayout(true);
		result.setVersion(request.getVersion());
		// Copy all global and specific component layouts
		for (final JAXBElement<?> child : request.getTextlayoutAndInfoboxlayoutAndTablelayout()) {
			result.getObjectsAndRelationsAndInformation().add(child);
		}
		// Copy request
		if (request.getRequest() != null) {
			result.setRequest(request.getRequest());
		}

		return result;
	}

	public void getLayoutComponents(LguiType result, LguiType lgui, HashSet<String> components) {

		for (final JAXBElement<?> element : lgui.getObjectsAndRelationsAndInformation()) {

			final Object object = element.getValue();

			if (object instanceof LayoutType) {
				if (object instanceof SplitlayoutType) {
					result.getObjectsAndRelationsAndInformation().add(element);
				} else if (object instanceof AbslayoutType) {
					result.getObjectsAndRelationsAndInformation().add(element);
				}

				if (object instanceof SplitlayoutType) {
					if (((SplitlayoutType) object).getLeft() != null) {
						collectComponents(((SplitlayoutType) object).getLeft(), components);
						collectComponents(((SplitlayoutType) object).getRight(), components);
					}
				} else if (object instanceof AbslayoutType) {
					for (final ComponentType component : ((AbslayoutType) object).getComp()) {
						components.add(component.getGid());
					}
				}
			} else if (object instanceof ComponentlayoutType) {
				if (((ComponentlayoutType) object).isActive()) {
					result.getObjectsAndRelationsAndInformation().add(element);
					components.add(((ComponentlayoutType) object).getGid());
				}
			}
		}
	}

	/**
	 * @param lgui
	 *            LML instance, whose children have to be extracted
	 * @return list of value objects of the direct children of the passed lgui instance
	 */
	public List<Object> getObjects(LguiType lgui) {
		final List<Object> list = new ArrayList<Object>();
		if (lgui == null) {
			return list;
		}
		for (final JAXBElement<?> element : lgui.getObjectsAndRelationsAndInformation()) {
			list.add(element.getValue());
		}
		return list;
	}

	public List<SelectType> getSelects(List<JAXBElement<?>> listOfPatternElements) {
		final LinkedList<SelectType> selectsList = new LinkedList<SelectType>();
		for (final JAXBElement<?> patternElement : listOfPatternElements) {
			if (patternElement.getValue() instanceof SelectType) {
				selectsList.add((SelectType) patternElement.getValue());
			}
		}
		return selectsList;
	}

	/**
	 * Converts layout root element into XML.
	 * Writes the XML into the output stream.
	 * 
	 * @param layout
	 *            the jaxb instance, which has to be converted into XML
	 * @param output
	 *            the output stream, into which the layout is printed
	 */
	public void marshal(LayoutRoot layout, OutputStream output) {
		final StringWriter sw = new StringWriter();
		marshal(layout, sw);
		final PrintWriter pw = new PrintWriter(output);
		pw.print(sw.toString());
		pw.close();
	}

	/**
	 * Converts layout root element into XML.
	 * Writes the layout into the writer instance.
	 * 
	 * @param layout
	 *            the jaxb instance, which has to be converted into XML
	 * @param writer
	 *            the output writer, into which the layout is printed
	 */
	public void marshal(LayoutRoot layout, StringWriter writer) {
		synchronized (LguiItem.class) {
			try {
				marshaller.marshal(createRootLayout(layout), writer);
			} catch (final JAXBException e) {
			}
		}
	}

	public void marshal(LguiType lgui, OutputStream output) {
		try {
			/*
			 * Synchronize to avoid the dreaded
			 * "FWK005 parse may not be called while parsing" message
			 */
			synchronized (LguiItem.class) {
				marshaller.marshal(createRootLgui(lgui), output);
			}
			output.close(); // Must close to flush stream
		} catch (final IOException e) {
			// TODO ErrorMessage
		} catch (final JAXBException e) {
			// TODO ErrorMessage
		}
	}

	public void marshal(LguiType lgui, StringWriter writer) {
		try {
			/*
			 * Synchronize to avoid the dreaded
			 * "FWK005 parse may not be called while parsing" message
			 */
			synchronized (LguiItem.class) {
				marshaller.marshal(createRootLgui(lgui), writer);
			}

		} catch (final JAXBException e) {
			// TODO ErrorMessage
		}
	}

	/**
	 * Replace all componentlayouts for a graphical object with given gid
	 * through newlayout.getGid() with newlayout
	 * 
	 * @param newLayout
	 *            new layout, which is placed into the positions of old layouts
	 */
	@SuppressWarnings("unchecked")
	public void replaceComponentLayout(LguiType lgui, ILguiItem lguiItem, ComponentlayoutType newLayout) {
		if (newLayout == null) {
			return;
		}
		final List<JAXBElement<?>> objects = lgui.getObjectsAndRelationsAndInformation();

		boolean replaced = false;

		// Over all objects in lml-file
		for (int i = 0; i < objects.size(); i++) {
			final JAXBElement<?> object = objects.get(i);

			// Over all Componentlayouts
			if (object.getValue() instanceof ComponentlayoutType) {

				if (((ComponentlayoutType) object.getValue()).getGid() != null
						&& ((ComponentlayoutType) object.getValue()).getGid().equals(newLayout.getGid())) {

					if (!replaced) {

						((JAXBElement<ComponentlayoutType>) object).setValue(newLayout);
						lguiItem.notifyListeners();
						replaced = true;
					} else {// Delete this object
						objects.remove(object);
						// One step back
						i--;
					}
				}

			}
		}

		if (!replaced) {
			// Insert new layout, if there was nothing to replace
			// Takes any componentlayout
			JAXBElement<?> newElement = null;

			// Differ between several layouts, create different JAXBElements
			if (newLayout instanceof TablelayoutType) {
				newElement = new JAXBElement<TablelayoutType>(new QName(ILMLCoreConstants.TABLELAYOUT_ELEMENT),
						TablelayoutType.class, (TablelayoutType) newLayout);
			} else if (newLayout instanceof NodedisplaylayoutType) {
				newElement = new JAXBElement<NodedisplaylayoutType>(new QName(ILMLCoreConstants.NODEDISPLAYLAYOUT_ELEMENT),
						NodedisplaylayoutType.class, (NodedisplaylayoutType) newLayout);
			}

			if (newElement != null) {
				lgui.getObjectsAndRelationsAndInformation().add(newElement);
				lguiItem.notifyListeners();
			}
		}

	}

	/**
	 * Parsing an XML file. The method generates from an XML file an instance of
	 * LguiType.
	 * 
	 * @param string
	 *            String containing the information from the server side
	 * @return the generated LguiType
	 */
	@SuppressWarnings("unchecked")
	public LguiType unmarshal(String string) {
		/*
		 * Synchronize to avoid the dreaded
		 * "FWK005 parse may not be called while parsing" message
		 */
		Source source = new StreamSource(new StringReader(string));
		try {
			validate(source);
		} catch (final SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (final IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (final URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		source = new StreamSource(new StringReader(string));
		JAXBElement<LguiType> jaxb = null;
		try {
			synchronized (LguiItem.class) {
				jaxb = (JAXBElement<LguiType>) unmarshaller.unmarshal(source);
			}
		} catch (final JAXBException e) {
			// TODO ErrorMessage
		}
		if (jaxb != null) {
			return jaxb.getValue();
		}
		else {
			return null;
		}
	}

	/**
	 * Parsing an LML layout file (<lml:layout>...</lml:layout>).
	 * Validates the input string and returns LayoutRoot instance.
	 * Tries to create the layout instance even, if validation fails.
	 * 
	 * @param string
	 *            contains entire LML layout file
	 * @return the generated layout instance
	 */
	public LayoutRoot unmarshalLayout(String string) {
		// Validate input string at first
		Source source = new StreamSource(new StringReader(string));
		try {
			validate(source);
		} catch (final SAXException e1) {
			e1.printStackTrace();
		} catch (final IOException e1) {
			e1.printStackTrace();
		} catch (final URISyntaxException e1) {
			e1.printStackTrace();
		}

		source = new StreamSource(new StringReader(string));
		JAXBElement<LayoutRoot> jaxb = null;
		try {
			synchronized (LguiItem.class) {
				jaxb = unmarshaller.unmarshal(source, LayoutRoot.class);
			}
		} catch (final JAXBException e) {
		}
		if (jaxb == null) {
			return null;
		} else {
			return jaxb.getValue();
		}
	}

	/**
	 * Unmarshal any complete subtree of an LML file.
	 * E.g. <request><driver name="TORQUE">...</request>
	 * 
	 * @param string
	 *            contains entire XML for the request tag
	 * @param parsedClass
	 *            the class instance of the type T, this cannot be called within the generic function
	 * @return the unmarshalled object or null, if unmarshalling failed
	 */
	public <T> T unmarshalFragment(String string, Class<T> parsedClass) {
		// No validation is done here, as this would not allow to unmarshal any non-root elements
		Source source = new StreamSource(new StringReader(string));
		JAXBElement<T> jaxb = null;
		try {
			synchronized (LguiItem.class) {
				jaxb = unmarshaller.unmarshal(source, parsedClass);
			}
		} catch (final JAXBException e) {
		}
		if (jaxb == null) {
			return null;
		} else {
			return jaxb.getValue();
		}
	}
}
