package wmas.xml;

import java.util.HashMap;

import org.w3c.dom.Element;

public class XMLCrossRef {
	private HashMap<Object, Integer> ids = new HashMap<Object, Integer>();
	private HashMap<Integer, Object> refs = new HashMap<Integer, Object>();

	private void constructRef(Object o) {
		if (!ids.containsKey(o)) {
			int i = refs.size();
			while (refs.containsKey(i))
				i++;
			ids.put(o, i);
			refs.put(i, o);
		}
	}

	public void makeRef(Object o, Element e) {
		constructRef(o);
		e.setAttribute("xid", ids.get(o).toString());
	}

	public boolean hasObject(Object o) {
		return ids.containsKey(o);
	}

	public String getRef(Object o) {
		constructRef(o);
		if (ids.containsKey(o))
			return ids.get(o).toString();
		return "";
	}

	public void setObject(Element ref, Object o) {
		try {
			Integer i = new Integer(ref.getAttribute("xid"));
			refs.put(i, o);
			ids.put(o, i);
		} catch (NumberFormatException e) {
		}
	}

	public void setObject(String ref, Object o) {
		try {
			Integer i = new Integer(ref);
			refs.put(i, o);
			ids.put(o, i);
		} catch (NumberFormatException e) {
		}
	}

	public Object getObject(String ref) {
		try {
			Integer i = new Integer(ref);
			if (!refs.containsKey(i)) {
				refs.put(i, null);
			}
			return refs.get(i);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
