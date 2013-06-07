package wmas.world;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import wmas.behaviour.Behaviour;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;
import wmas.xml.XMLInterpretor;

public class Model implements XMLEntity {
	private List<EntityInterface> entities = new LinkedList<EntityInterface>();
	private List<Behaviour> availableBehaviours = new LinkedList<Behaviour>();
	private Map<String, XMLEntity> attributes = new HashMap<String, XMLEntity>();

	public void addAttribute(String name, XMLEntity attribute) {
		this.attributes.put(name, attribute);
	}

	public void removeAttribute(String name) {
		this.attributes.remove(name);
	}

	public boolean hasAttribute(String name) {
		return this.attributes.containsKey(name);
	}

	public XMLEntity getAttribute(String name) {
		return this.attributes.get(name);
	}

	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		if (refs == null)
			refs = new XMLCrossRef();
		for (int i = 0; i < e.getChildNodes().getLength(); i++) {
			Node n = e.getChildNodes().item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) n;
				if (el.getTagName().equals("attribute")) {
					String attrName = el.getAttribute("name");
					XMLEntity c = null;
					for (int j = 0; j < el.getChildNodes().getLength()
							&& c == null; j++) {
						Node n1 = el.getChildNodes().item(i);
						if (n1.getNodeType() == Node.ELEMENT_NODE) {
							c = XMLInterpretor.convert((Element) n1, refs);
						}
					}
					attributes.put(attrName, c);
				} else {
					XMLEntity o = XMLInterpretor.convert(el, refs);
					if (o != null) {
						if (o instanceof EntityInterface) {
							entities.add((EntityInterface) o);
							((EntityInterface) o).setParent(this);
						} else if (o instanceof Behaviour) {
							availableBehaviours.add((Behaviour) o);
						}
					}
				}
			}
		}
	}

	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		if (refs == null)
			refs = new XMLCrossRef();
		Element e = root.createElement(this.getClass().getName());
		for (Map.Entry<String, XMLEntity> me : attributes.entrySet()) {
			Element attr = root.createElement("attribute");
			attr.setAttribute("name", me.getKey());
			if (me.getValue() != null)
				attr.appendChild(me.getValue().toXML(root, refs));
			e.appendChild(attr);
		}
		for (Behaviour b : availableBehaviours) {
			e.appendChild(XMLInterpretor.makeCrossReferencedElement(root, b,
					refs));
		}
		for (EntityInterface ent : entities) {
			e.appendChild(XMLInterpretor.makeCrossReferencedElement(root, ent,
					refs));
		}
		return e;
	}

	public XMLEntity copy() {
		Model r = new Model();
		HashMap<Behaviour, Behaviour> bMap = new HashMap<Behaviour, Behaviour>();
		for (Behaviour b : availableBehaviours) {
			Behaviour c = b.copy();
			r.availableBehaviours.add(c);
			bMap.put(b, c);
		}
		for (EntityInterface e : entities) {
			EntityInterface copy = e.copy(bMap);
			copy.setParent(r);
			r.entities.add(copy);
		}
		for (Map.Entry<String, XMLEntity> me : attributes.entrySet()) {
			r.attributes.put(me.getKey(), me.getValue().copy());
		}
		return r;
	}

	public List<EntityInterface> getEntities() {
		return entities;
	}

	public List<Behaviour> getAvailableBehaviours() {
		return availableBehaviours;
	}

	public Collection<String> getAttributes() {
		return attributes.keySet();
	}
}
