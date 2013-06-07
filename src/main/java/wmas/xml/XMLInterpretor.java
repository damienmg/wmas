package wmas.xml;

import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

/**
 * Singleton class to convert from and to XML
 * 
 * @author dmartin
 * 
 */
@SuppressWarnings("restriction")
public class XMLInterpretor {
	static private XMLInterpretor singleton = null;
	private DocumentBuilder builder;
	private Transformer transformer;
	private DOMParser parser;

	private XMLInterpretor() throws Exception {
		builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty("indent", "yes");
		transformer.setOutputProperty("method", "xml");
		parser = new DOMParser();
	}

	/**
	 * Singleton getInstance. An constructor for this class
	 * 
	 * @return An instance of {@code XMLInterpretor}
	 * @throws Exception
	 */
	static public XMLInterpretor getInstance() throws Exception {
		if (singleton == null)
			singleton = new XMLInterpretor();
		return singleton;
	}

	public Document toXML(XMLEntity e, XMLCrossRef refs) throws Exception {
		Document root = builder.newDocument();
		root.appendChild(e.toXML(root, refs));
		return root;
	}

	public void toXML(XMLEntity e, XMLCrossRef refs, OutputStream stream)
			throws Exception {
		transformer.transform(new DOMSource(toXML(e, refs)), new StreamResult(
				stream));
	}

	public void toXML(XMLEntity e, XMLCrossRef refs, String filename)
			throws Exception {
		toXML(e, refs, new FileOutputStream(filename));
	}

	public static Document convert(XMLEntity e, XMLCrossRef refs)
			throws Exception {
		return getInstance().toXML(e, refs);
	}

	public static void convert(XMLEntity e, XMLCrossRef refs,
			OutputStream stream) throws Exception {
		getInstance().toXML(e, refs, stream);
	}

	public static void convert(XMLEntity e, XMLCrossRef refs, String filename)
			throws Exception {
		getInstance().toXML(e, refs, filename);
	}

	private XMLEntity instanciate(String className) throws Exception {
		Class<?> a = Class.forName(className);
		return (XMLEntity) a.newInstance();
	}

	public XMLEntity fromXML(Element e, XMLCrossRef refs) throws Exception {
		if (e.getTagName().equals("crossref") && refs != null) {
			Object r = refs.getObject(e.getAttribute("xref"));
			if (r != null && r instanceof XMLEntity)
				return (XMLEntity) r;
		}
		if (refs == null) {
			refs = new XMLCrossRef();
		}
		XMLEntity xe = instanciate(e.getTagName());
		xe.fromXML(e, refs);
		if (xe != null && e.hasAttribute("xid"))
			refs.setObject(e, xe);
		return xe;
	}

	public static XMLEntity convert(Element e, XMLCrossRef refs)
			throws Exception {
		return getInstance().fromXML(e, refs);
	}

	public XMLEntity fromXML(String filename, XMLCrossRef refs)
			throws Exception {
		parser.reset();
		parser.parse(filename);
		Document d = parser.getDocument();
		return fromXML(d.getDocumentElement(), refs);
	}

	public static XMLEntity convert(String filename, XMLCrossRef refs)
			throws Exception {
		return getInstance().fromXML(filename, refs);
	}

	public static Element makeCrossReferencedElement(Document root,
			XMLEntity entity, XMLCrossRef refs) throws Exception {
		Element n;
		if (refs != null) {
			if (refs.hasObject(entity)) {
				n = root.createElement("crossref");
				n.setAttribute("xref", refs.getRef(entity));
				return n;
			}
		}
		n = entity.toXML(root, refs);
		if (refs != null) {
			refs.makeRef(entity, n);
		}
		return n;
	}
}
