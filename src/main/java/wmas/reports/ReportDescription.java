package wmas.reports;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;

public class ReportDescription implements XMLEntity {

	static Set<String> possibleEvents = new HashSet<String>();
	static Map<String, Boolean> possibleReport = new HashMap<String, Boolean>();

	public static void registerEventReport(String s) {
		possibleEvents.add(s);
	}

	public static void registerDataReport(String s, boolean isCumulative) {
		possibleReport.put(s, isCumulative);
	}

	public static void registerDataReport(String s) {
		possibleReport.put(s, false);
	}

	public static void unregisterEventReport(String s) {
		possibleEvents.remove(s);
	}

	public static void unregisterDataReport(String s) {
		possibleReport.remove(s);
	}

	Set<String> storedEvent = new HashSet<String>();
	Map<String, HashMap<String, Boolean>> storedReport = new HashMap<String, HashMap<String, Boolean>>();

	@Override
	public ReportDescription copy() {
		ReportDescription r = new ReportDescription();
		for (String s : storedReport.keySet()) {
			r.storedReport.put(s, new HashMap<String, Boolean>());
			r.storedReport.get(s).putAll(storedReport.get(s));
		}
		r.storedEvent.addAll(storedEvent);
		return r;
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) n;
				if (el.getTagName().equals("data")) {
					String name = el.getAttribute("name");
					storedReport.put(name, new HashMap<String, Boolean>());
					NodeList nl2 = el.getChildNodes();
					for (int j = 0; j < nl2.getLength(); j++) {
						if (nl2.item(j).getNodeType() == Node.ELEMENT_NODE) {
							Element el2 = (Element) nl2.item(j);
							if (el2.getTagName().equals("param")) {
								storedReport
										.get(name)
										.put(el2.hasAttribute("name") ? el2.getAttribute("name")
												: null,
												el2.hasAttribute("multiple")
														&& el2.getAttribute(
																"multiple")
																.equals("1"));
							}
						}
					}
				} else if (el.getTagName().equals("event")) {
					String name = el.getAttribute("name");
					storedEvent.add(name);
				}
			}
		}
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		for (String s : storedReport.keySet()) {
			Element el = root.createElement("data");
			el.setAttribute("name", s);
			for (String s2 : storedReport.get(s).keySet()) {
				Element el2 = root.createElement("param");
				if (s2 != null) {
					el2.setAttribute("name", s2);
				}
				el2.setAttribute("multiple", storedReport.get(s).get(s2) ? "1"
						: "0");
				el.appendChild(el2);
			}
			e.appendChild(el);
		}
		for (String s : storedEvent) {
			Element el = root.createElement("event");
			el.setAttribute("name", s);
			e.appendChild(el);
		}
		return e;
	}

	public boolean isMultiple(String name, String param) {
		if (!storedReport.containsKey(name)
				|| !storedReport.get(name).containsKey(param))
			return false;
		return storedReport.get(name).get(param);
	}

	public static boolean isCumulative(String name) {
		if (!possibleReport.containsKey(name))
			return false;
		return possibleReport.get(name);
	}

	public Set<String> listEvent() {
		return storedEvent;
	}

	public static Set<String> listPossibleEvents() {
		return possibleEvents;
	}
}
