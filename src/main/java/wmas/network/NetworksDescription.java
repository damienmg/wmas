package wmas.network;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import wmas.expression.functions.FunctionList;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;

public class NetworksDescription implements XMLEntity {

	public static class NetworkDescription {
		public double defaultRange = 0; // The default range when adding it to a
										// network
		public int defaultWindow = 10; // The default window size when adding it
										// to a network
		public long bandwidth = 0; // The bandwidth of the network
		public double delay = 0; // The delay of the network
		public boolean reader = false; // This is a reader/writer network

		public NetworkDescription() {
		}

		public NetworkDescription(NetworkDescription o) {
			defaultRange = o.defaultRange;
			bandwidth = o.bandwidth;
			delay = o.delay;
			reader = o.reader;
			defaultWindow = o.defaultWindow;
		}

		public NetworkDescription(double defaultRange, int defaultWindow,
				long bandwidth, double delay, boolean reader) {
			super();
			this.defaultRange = defaultRange;
			this.bandwidth = bandwidth;
			this.delay = delay;
			this.reader = reader;
			this.defaultWindow = defaultWindow;
		}
	}

	private Vector<NetworkDescription> networks = new Vector<NetworkDescription>();
	HashMap<String, NetworkMessage> availableMsg = new HashMap<String, NetworkMessage>(); // list
																							// of
																							// availables
																							// messages
																							// on
																							// the
																							// network

	public int getNbNetworks() {
		return networks.size();
	}

	public NetworkDescription getNetwork(int nId) {
		if (nId < 0 || nId >= networks.size()) {
			return null;
		}
		return networks.get(nId);
	}

	public double duration(int nId, long size) {
		if (nId < 0 || nId >= networks.size()) {
			return -1;
		}
		return networks.get(nId).delay + size
				/ (double) (networks.get(nId).bandwidth);
	}

	public double duration(Collection<Integer> availables, NetworkMessage m) {
		double s = -1;
		long size = m.getSize();
		for (int i : availables) {
			double s1 = duration(i, size);
			if (s1 == 0)
				return 0;
			if (s1 >= 0 && (s1 < s || s < 0))
				s = s1;
		}
		return s;
	}

	public boolean isReaderNetwork(int i) {
		if (i < 0 || i >= networks.size()) {
			return false;
		}
		return networks.get(i).reader;
	}

	public boolean isInRange(int i, double d, NetworksAttribute attr1,
			NetworksAttribute attr2) {
		if (i < 0 || i >= networks.size()) {
			return false;
		}
		if ((attr1.getRange(i) == 0 || attr2.getRange(i) == 0)
				&& !networks.get(i).reader) {
			// Global network
			return true;
		}
		return (attr1.getRange(i) >= d && attr2.getRange(i) >= d)
				&& isCompatible(i, attr1, attr2);
	}

	private boolean isCompatible(int nId, NetworksAttribute attr1,
			NetworksAttribute attr2) {
		if (nId < 0 || nId >= networks.size()) {
			return false;
		}
		return !networks.get(nId).reader
				|| (attr1.isReader(nId) && !attr2.isReader(nId))
				|| (attr2.isReader(nId) && !attr1.isReader(nId));
	}

	public int getDefaultWindow(int id) {
		if (id < 0 || id >= networks.size()) {
			return 0;
		}
		return networks.get(id).defaultWindow;
	}

	public double getDefaultRange(int id) {
		if (id < 0 || id >= networks.size()) {
			return 0;
		}
		return networks.get(id).defaultRange;
	}

	@Override
	public NetworksDescription copy() {
		NetworksDescription na = new NetworksDescription();
		for (String n : availableMsg.keySet()) {
			na.availableMsg.put(n, availableMsg.get(n));
			FunctionList.registerSession(availableMsg.get(n),
					"Network message '" + n + "'");
		}
		for (int i = 0; i < networks.size(); i++) {
			na.networks.add(new NetworkDescription(networks.get(i)));
		}
		return na;
	}

	public Collection<String> listMessageTypes() {
		return availableMsg.keySet();
	}

	public boolean addMessageType(String name) {
		if (name.matches(FunctionList.identOnlyRegex)) {
			if (!availableMsg.containsKey(name)
					&& !FunctionList.functionExists(name)) {
				availableMsg.put(name, NetworkMessage.getInstance(name));
				FunctionList.registerSession(availableMsg.get(name),
						"Network message '" + name + "'");
				return true;
			}
		}
		return false;
	}

	public void removeMessageType(String name) {
		if (availableMsg.containsKey(name)) {
			FunctionList.unregister(availableMsg.get(name));
			availableMsg.remove(name);
		}
	}

	public void removeAllMessageTypes() {
		for (String s : availableMsg.keySet()) {
			FunctionList.unregister(availableMsg.get(s));
		}
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		networks.clear();
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) n;
				if (el.getTagName().equals("network")) {
					networks.add(
							Integer.parseInt(el.getAttribute("index")),
							new NetworkDescription(
									Double.parseDouble(el.getAttribute("range")),
									Integer.parseInt(el.getAttribute("window")),
									Long.parseLong(el.getAttribute("bandwidth")),
									Double.parseDouble(el.getAttribute("delay")),
									el.hasAttribute("reader")
											&& el.getAttribute("reader")
													.equals("1")));
				} else if (el.getTagName().equals("message")) {
					addMessageType(el.getAttribute("name"));
				}
			}
		}
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		for (String name : availableMsg.keySet()) {
			Element el = root.createElement("message");
			el.setAttribute("name", name);
			e.appendChild(el);
		}
		for (int i = 0; i < networks.size(); i++) {
			Element el = root.createElement("network");
			NetworkDescription n = networks.get(i);
			el.setAttribute("index", Integer.toString(i));
			el.setAttribute("range", Double.toString(n.defaultRange));
			el.setAttribute("window", Integer.toString(n.defaultWindow));
			el.setAttribute("bandwidth", Long.toString(n.bandwidth));
			el.setAttribute("delay", Double.toString(n.delay));
			if (n.reader)
				el.setAttribute("reader", "1");
			e.appendChild(el);
		}
		return e;
	}

	public void addNetwork() {
		networks.add(new NetworkDescription());
	}

	public void deleteNetwork() {
		if (networks.size() > 0)
			networks.remove(networks.size() - 1);
	}
}
