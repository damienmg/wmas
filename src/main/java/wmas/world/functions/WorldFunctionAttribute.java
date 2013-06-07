package wmas.world.functions;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;
import wmas.xml.XMLInterpretor;

public class WorldFunctionAttribute implements XMLEntity {

	private List<ExpressionFunction> functions = new LinkedList<ExpressionFunction>();

	public WorldFunctionAttribute() {
	}

	@Override
	public XMLEntity copy() {
		WorldFunctionAttribute n = new WorldFunctionAttribute();
		for (ExpressionFunction ef : functions)
			n.functions.add(ef.copy());
		return n;
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		functions.clear();
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				XMLEntity xe = XMLInterpretor.convert((Element) nl, refs);
				if (xe instanceof ExpressionFunction) {
					functions.add((ExpressionFunction) xe);
				}
			}
		}
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		for (ExpressionFunction ef : functions) {
			e.appendChild(ef.toXML(root, refs));
		}
		return e;
	}

	public List<ExpressionFunction> getFunctions() {
		return functions;
	}
}
