package wmas.behaviour.physical;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import wmas.util.Util;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;

public class SpeedAttribute implements XMLEntity {
	private HashMap<Color, Double> speeds = new HashMap<Color, Double>();

	public SpeedAttribute() {
		speeds.put(null, 1.0);
	}

	@Override
	public XMLEntity copy() {
		SpeedAttribute sa = new SpeedAttribute();
		sa.speeds.putAll(speeds);
		return sa;
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		speeds.clear();
		for (int i = 0; i < e.getChildNodes().getLength(); i++) {
			Node n = e.getChildNodes().item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) n;
				if (el.getTagName().equals("speed")) {
					Color c = Util.colorFromString(el.getAttribute("color"));
					double s = Double.parseDouble(el.getAttribute("value"));
					speeds.put(c, s);
				}
			}
		}
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		for (Map.Entry<Color, Double> c : speeds.entrySet()) {
			Element el = root.createElement("speed");
			el.setAttribute("value", c.getValue().toString());
			if (c.getKey() != null) {
				el.setAttribute("color", Util.colorToString(c.getKey()));
			}
			e.appendChild(el);
		}
		return e;
	}

	public boolean hasSpeed(Color key) {
		return speeds.containsKey(key);
	}

	public double getSpeed() {
		return getSpeed(null);
	}

	public double getSpeed(Color key) {
		if (!speeds.containsKey(key)) {
			return speeds.containsKey(null) ? speeds.get(null) : 0;
		}
		return speeds.get(key);
	}

	public Set<Color> listSupportedColors() {
		return speeds.keySet();
	}

	public void addSpeed(Color key, double value) {
		speeds.put(key, value);
	}

	public void removeSpeed(Color key) {
		if (key != null)
			speeds.remove(key);
	}
}
