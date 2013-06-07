package wmas.network;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import wmas.network.NetworkQueue.ScheduledMessage;
import wmas.util.Doublet;
import wmas.world.memory.Data;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;

public class NetworksAttribute implements XMLEntity {
	public static class NetworkAttribute {
		public double range = 0; // The range of this entity in the network
		public boolean reader = false; // is a reader in a reader/writer network
		public int window = 10; // Receiving window size
	}

	private HashMap<Integer, NetworkAttribute> supportedNetwork = new HashMap<Integer, NetworkAttribute>();
	Queue<ScheduledMessage> receiveQueue = new LinkedList<ScheduledMessage>();
	private HashMap<Integer, Queue<ScheduledMessage>> receiveWindows = new HashMap<Integer, Queue<ScheduledMessage>>();

	public Set<Integer> getSupportedNetworks() {
		return supportedNetwork.keySet();
	}

	void receive(ScheduledMessage msg) {
		if (msg.freed)
			return;
		if (!supportedNetwork.containsKey(msg.networkIndex)) {
			return;
		}
		if (!receiveWindows.containsKey(msg.networkIndex)) {
			receiveWindows.put(msg.networkIndex,
					new LinkedList<ScheduledMessage>());
		}
		msg.networkAttributes = this;
		receiveQueue.offer(msg);
		receiveWindows.get(msg.networkIndex).offer(msg);
		if (receiveWindows.get(msg.networkIndex).size() > supportedNetwork
				.get(msg.networkIndex).window)
			receiveWindows.get(msg.networkIndex).poll().free();
	}

	boolean peek() {
		return !receiveQueue.isEmpty();
	}

	boolean peek(int nId) {
		return receiveWindows.containsKey(nId)
				&& !receiveWindows.get(nId).isEmpty();
	}

	private boolean match(ScheduledMessage msg, boolean read, String prefix,
			NetworkMessage m) {
		if (read) {
			return msg.isReadAnswer
					&& (prefix == null || msg.sender.getName().startsWith(
							prefix));
		}
		return (msg.msg != null & !msg.isReadAnswer)
				&& (prefix == null || prefix.isEmpty() || msg.sender.getName()
						.startsWith(prefix))
				&& (m == null || m.getType() == null || m.getType().equals(
						msg.msg.getType()));
	}

	boolean peek(int nId, String prefix, NetworkMessage m) {
		if (!receiveWindows.containsKey(nId)
				|| receiveWindows.get(nId).isEmpty())
			return false;
		if ((prefix == null || prefix.isEmpty())
				&& (m == null || m.getType() == null))
			return true;
		for (ScheduledMessage msg : receiveWindows.get(nId)) {
			if (match(msg, false, prefix, m))
				return true;
		}
		return false;
	}

	boolean peek(String prefix, NetworkMessage m) {
		if (receiveQueue.isEmpty())
			return false;
		if ((prefix == null || prefix.isEmpty())
				&& (m == null || m.getType() == null))
			return true;
		for (ScheduledMessage msg : receiveQueue) {
			if (match(msg, false, prefix, m))
				return true;
		}
		return false;
	}

	NetworkMessage poll(int nId, String prefix, NetworkMessage m) {
		if (!receiveWindows.containsKey(nId)
				|| receiveWindows.get(nId).isEmpty())
			return null;
		for (ScheduledMessage msg : receiveWindows.get(nId)) {
			if (match(msg, false, prefix, m)) {
				msg.free();
				return msg.msg;
			}
		}
		return null;
	}

	NetworkMessage poll(String prefix, NetworkMessage m) {
		if (receiveQueue.isEmpty())
			return null;
		for (ScheduledMessage msg : receiveQueue) {
			if (match(msg, false, prefix, m)) {
				msg.free();
				return msg.msg;
			}
		}
		return null;
	}

	Doublet<int[], Data[]> getLastReadAnswer(String prefix) {
		if (receiveQueue.isEmpty())
			return null;
		for (ScheduledMessage msg : receiveQueue) {
			if (match(msg, true, prefix, null)) {
				msg.free();
				return new Doublet<int[], Data[]>(msg.dataSetDescription,
						msg.dataSet);
			}
		}
		return null;
	}

	Doublet<int[], Data[]> getLastReadAnswer(int nId, String prefix) {
		if (!receiveWindows.containsKey(nId)
				|| receiveWindows.get(nId).isEmpty())
			return null;
		for (ScheduledMessage msg : receiveWindows.get(nId)) {
			if (match(msg, true, prefix, null)) {
				msg.free();
				return new Doublet<int[], Data[]>(msg.dataSetDescription,
						msg.dataSet);
			}
		}
		return null;
	}

	double getRange(int id) {
		return supportedNetwork.containsKey(id) ? supportedNetwork.get(id).range
				: -1;
	}

	boolean isReader(int id) {
		return supportedNetwork.containsKey(id) ? supportedNetwork.get(id).reader
				: false;
	}

	public void addNetworkAttribute(int id, double range, int window,
			boolean reader) {
		if (!supportedNetwork.containsKey(id))
			supportedNetwork.put(id, new NetworkAttribute());
		supportedNetwork.get(id).range = range;
		supportedNetwork.get(id).reader = reader;
		supportedNetwork.get(id).window = window;
	}

	public void addNetworkAttribute(int id, double range, int window) {
		if (!supportedNetwork.containsKey(id))
			supportedNetwork.put(id, new NetworkAttribute());
		supportedNetwork.get(id).range = range;
		supportedNetwork.get(id).window = window;
	}

	public void addNetworkAttribute(int id, NetworksDescription desc) {
		if (!supportedNetwork.containsKey(id))
			supportedNetwork.put(id, new NetworkAttribute());
		supportedNetwork.get(id).range = desc.getDefaultRange(id);
		supportedNetwork.get(id).window = desc.getDefaultWindow(id);
	}

	public void delNetworkAttribute(int id) {
		supportedNetwork.remove(id);
	}

	public Collection<Integer> listSupportedNetwork() {
		return supportedNetwork.keySet();
	}

	public NetworkAttribute getSupportedNetwork(int id) {
		return supportedNetwork.get(id);
	}

	@Override
	public NetworksAttribute copy() {
		NetworksAttribute na = new NetworksAttribute();
		for (Map.Entry<Integer, NetworkAttribute> entry : supportedNetwork
				.entrySet()) {
			na.addNetworkAttribute(entry.getKey(), entry.getValue().range,
					entry.getValue().window, entry.getValue().reader);
		}
		return na;
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		supportedNetwork.clear();
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) n;
				if (el.getTagName().equals("network")) {
					addNetworkAttribute(
							Integer.parseInt(el.getAttribute("index")),
							Double.parseDouble(el.getAttribute("range")),
							Integer.parseInt(el.getAttribute("window")),
							el.hasAttribute("reader")
									&& el.getAttribute("reader").equals("1"));
				}
			}
		}
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		for (Map.Entry<Integer, NetworkAttribute> entry : supportedNetwork
				.entrySet()) {
			Element el = root.createElement("network");
			el.setAttribute("index", Integer.toString(entry.getKey()));
			el.setAttribute("range", Double.toString(entry.getValue().range));
			el.setAttribute("window", Integer.toString(entry.getValue().window));
			if (entry.getValue().reader)
				el.setAttribute("reader", "1");
			e.appendChild(el);
		}
		return e;
	}

	public boolean hasNetwork(int i) {
		return supportedNetwork.containsKey(i);
	}

	void remove(ScheduledMessage msg) {
		if (receiveWindows.get(msg.networkIndex) == null) {
			System.err.println(msg.networkIndex + " : " + msg.msg.toString());
		}
		receiveWindows.get(msg.networkIndex).remove(msg);
		receiveQueue.remove(msg);
	}
}
