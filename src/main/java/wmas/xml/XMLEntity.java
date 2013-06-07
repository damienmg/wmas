package wmas.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Interface for object that can be instancied from XML
 * <p>
 * All implementing class should have a constructor that takes either no
 * argument or one argument of type <code>TheMatrix</code>
 * 
 * @author dmartin
 */
public interface XMLEntity extends Copiable {
	/**
	 * Convert this class into an XML DOM tree
	 * 
	 * @param root
	 *            the root document of the DOM tree
	 * @return A DOM Element representing the current object in XML. This
	 *         element tag should be the java class name.
	 * @throws <code>Exception</code> on write error
	 */
	public Element toXML(Document root, XMLCrossRef refs) throws Exception;

	/**
	 * Initialise this class from an XML DOM node
	 * 
	 * @param e
	 *            the XML DOM node
	 * @throws <code>Exception</code> on parse error
	 */
	public void fromXML(Element e, XMLCrossRef refs) throws Exception;

	/**
	 * @return a copy of this entity
	 */
	public XMLEntity copy();
}
