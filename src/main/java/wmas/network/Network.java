package wmas.network;

import java.awt.Point;

import wmas.network.NetworkQueue.ScheduledMessage;
import wmas.network.gui.NetworkDisplayPanel;
import wmas.util.Doublet;
import wmas.world.EntityInterface;
import wmas.world.World;
import wmas.world.WorldBehaviour;
import wmas.world.memory.Data;
import wmas.world.memory.Memory;

public class Network implements WorldBehaviour {

	private NetworkCommunicationRange ranges = new NetworkCommunicationRange();
	private NetworkListenerManager listenerManager = new NetworkListenerManager(
			this);
	private NetworkQueue queue = new NetworkQueue();
	private NetworksDescription desc;
	private World world;
	private NetworkDisplayPanel display = null;

	public Network() {
		super();
		this.ranges.setListener(listenerManager);
		this.queue.setListener(listenerManager);
	}

	void reportUsage(int nId) {
		if (world != null) {
			world.addReport(world.getTime(), "network", Integer.toString(nId),
					1);
			world.addReport(world.getTime(), "network", "", 1);
		}
	}

	void reportReceived(ScheduledMessage msg) {
		if (world != null) {
			world.addEvent(msg.scheduledTime, "received", msg.toString(),
					msg.recipient);
		}
	}

	void reportDropped(double t, ScheduledMessage msg) {
		if (world != null) {
			world.addEvent(msg.scheduledTime, "dropped", msg.toString(),
					msg.recipient);
		}
	}

	public boolean read(EntityInterface reader, EntityInterface tag,
			int[] dataSetDescription) {
		if (desc != null && reader.hasAttribute("networked")) {
			double t = world.getTime();
			long size = NetworkQueue.getSize(null, dataSetDescription);
			int i = ranges.getBestNetwork(size, true, reader, tag);
			if (i >= 0 && reader != tag) {
				queue.read(i, reader, tag, dataSetDescription, t);
				return true;
			}
		}
		return false;
	}

	public boolean read(EntityInterface reader, String tag,
			int[] dataSetDescription) {
		if (desc != null && reader.hasAttribute("networked")) {
			double t = world.getTime();
			long size = NetworkQueue.getSize(null, dataSetDescription);
			for (EntityInterface rec : ranges.getInComRange(reader, tag, true)) {
				int i = ranges.getBestNetwork(size, true, reader, rec);
				if (i >= 0 && reader != rec) {
					queue.read(i, reader, rec, dataSetDescription, t);
					return true;
				}
			}
		}
		return false;
	}

	public boolean write(EntityInterface reader, EntityInterface tag,
			int[] dataSetDescription, Data[] dataSet) {
		if (desc != null && reader.hasAttribute("networked")) {
			double t = world.getTime();
			long size = NetworkQueue.getSize(dataSet, dataSetDescription);
			int i = ranges.getBestNetwork(size, true, reader, tag);
			if (i >= 0 && tag != reader) {
				queue.write(i, reader, tag, dataSetDescription, dataSet, t);
				return true;
			}
		}
		return false;
	}

	public boolean write(EntityInterface reader, String tag,
			int[] dataSetDescription, Data[] dataSet) {
		if (desc != null && reader.hasAttribute("networked")) {
			double t = world.getTime();
			long size = NetworkQueue.getSize(dataSet, dataSetDescription);
			for (EntityInterface rec : ranges.getInComRange(reader, tag, true)) {
				int i = ranges.getBestNetwork(size, true, reader, rec);
				if (i >= 0 && rec != reader) {
					queue.write(i, reader, rec, dataSetDescription, dataSet, t);
					return true;
				}
			}
		}
		return false;
	}

	public boolean send(EntityInterface sender, EntityInterface recipient,
			NetworkMessage msg) {
		if (desc != null && sender.hasAttribute("networked")) {
			double t = world.getTime();
			int i = ranges.getBestNetwork(msg.getSize(), false, sender,
					recipient);
			if (i >= 0 && recipient != sender) {
				queue.send(i, sender, recipient, msg, t);
				return true;
			}
		}
		return false;
	}

	public boolean send(EntityInterface sender, String recipient,
			NetworkMessage msg) {
		if (desc != null && sender.hasAttribute("networked")) {
			double t = world.getTime();
			long size = msg.getSize();
			for (EntityInterface rec : ranges.getInComRange(sender, recipient,
					false)) {
				int i = ranges.getBestNetwork(size, false, sender, rec);
				if (i >= 0 && rec != sender) {
					queue.send(i, sender, rec, msg, t);
					return true;
				}
			}
		}
		return false;
	}

	public int broadcast(EntityInterface sender, NetworkMessage msg) {
		int k = 0;
		if (desc != null && sender.hasAttribute("networked")) {
			double t = world.getTime();
			long size = msg.getSize();
			for (EntityInterface recipient : ranges
					.getInComRange(sender, false)) {
				int i = ranges.getBestNetwork(size, false, sender, recipient);
				if (i >= 0 && recipient != sender) {
					queue.send(i, sender, recipient, msg, t);
					k++;
				}
			}
		}
		return k;
	}

	public boolean read(int nId, EntityInterface reader, EntityInterface tag,
			int[] dataSetDescription) {
		if (desc != null && desc.isReaderNetwork(nId)
				&& reader.hasAttribute("networked")
				&& tag.hasAttribute("networked")
				&& reader.getPosition() != null && tag.getPosition() != null) {
			double t = world.getTime();
			double[] p1 = reader.getPosition();
			double[] p2 = tag.getPosition();
			double d = Point.distance(p1[0], p1[1], p2[0], p2[1]);
			NetworksAttribute attr1 = (NetworksAttribute) reader
					.getAttribute("networked");
			NetworksAttribute attr2 = (NetworksAttribute) tag
					.getAttribute("networked");
			if (attr1.hasNetwork(nId) && attr2.hasNetwork(nId)
					&& desc.isInRange(nId, d, attr1, attr2)
					&& attr1.isReader(nId)) {
				queue.read(nId, reader, tag, dataSetDescription, t);
				return true;
			}
		}
		return false;
	}

	public boolean read(int nId, EntityInterface reader, String tag,
			int[] dataSetDescription) {
		if (desc != null && !desc.isReaderNetwork(nId)
				&& reader.hasAttribute("networked")) {
			double t = world.getTime();
			NetworksAttribute attr1 = (NetworksAttribute) reader
					.getAttribute("networked");
			if (attr1.isReader(nId)) {
				for (EntityInterface rec : ranges.getInComRange(nId, reader,
						tag)) {
					queue.read(nId, reader, rec, dataSetDescription, t);
					return true;
				}
			}
		}
		return false;
	}

	public boolean write(int nId, EntityInterface reader, EntityInterface tag,
			int[] dataSetDescription, Data[] dataSet) {
		if (desc != null && desc.isReaderNetwork(nId)
				&& reader.hasAttribute("networked")
				&& tag.hasAttribute("networked")
				&& reader.getPosition() != null && tag.getPosition() != null) {
			double t = world.getTime();
			double[] p1 = reader.getPosition();
			double[] p2 = tag.getPosition();
			double d = Point.distance(p1[0], p1[1], p2[0], p2[1]);
			NetworksAttribute attr1 = (NetworksAttribute) reader
					.getAttribute("networked");
			NetworksAttribute attr2 = (NetworksAttribute) tag
					.getAttribute("networked");
			if (attr1.hasNetwork(nId) && attr2.hasNetwork(nId)
					&& desc.isInRange(nId, d, attr1, attr2)
					&& attr1.isReader(nId)) {
				queue.write(nId, reader, tag, dataSetDescription, dataSet, t);
				return true;
			}
		}
		return false;
	}

	public boolean write(int nId, EntityInterface reader, String tag,
			int[] dataSetDescription, Data[] dataSet) {
		if (desc != null && !desc.isReaderNetwork(nId)
				&& reader.hasAttribute("networked")) {
			NetworksAttribute attr1 = (NetworksAttribute) reader
					.getAttribute("networked");
			double t = world.getTime();
			if (attr1.isReader(nId)) {
				for (EntityInterface rec : ranges.getInComRange(nId, reader,
						tag)) {
					queue.write(nId, reader, rec, dataSetDescription, dataSet,
							t);
					return true;
				}
			}
		}
		return false;
	}

	public boolean send(int nId, EntityInterface sender,
			EntityInterface recipient, NetworkMessage msg) {
		if (desc != null && !desc.isReaderNetwork(nId)
				&& sender.hasAttribute("networked")
				&& recipient.hasAttribute("networked")
				&& sender.getPosition() != null
				&& recipient.getPosition() != null) {
			double t = world.getTime();
			double[] p1 = sender.getPosition();
			double[] p2 = recipient.getPosition();
			double d = Point.distance(p1[0], p1[1], p2[0], p2[1]);
			NetworksAttribute attr1 = (NetworksAttribute) sender
					.getAttribute("networked");
			NetworksAttribute attr2 = (NetworksAttribute) recipient
					.getAttribute("networked");
			if (attr1.hasNetwork(nId) && attr2.hasNetwork(nId)
					&& desc.isInRange(nId, d, attr1, attr2)) {
				queue.send(nId, sender, recipient, msg, t);
				return true;
			}
		}
		return false;
	}

	public boolean send(int nId, EntityInterface sender, String recipient,
			NetworkMessage msg) {
		if (desc != null && !desc.isReaderNetwork(nId)
				&& sender.hasAttribute("networked")) {
			double t = world.getTime();
			for (EntityInterface rec : ranges.getInComRange(nId, sender,
					recipient)) {
				queue.send(nId, sender, rec, msg, t);
				return true;
			}
		}
		return false;
	}

	public int broadcast(int nId, EntityInterface sender, NetworkMessage msg) {
		int i = 0;
		if (desc != null && !desc.isReaderNetwork(nId)
				&& sender.hasAttribute("networked")) {
			double t = world.getTime();
			for (EntityInterface recipient : ranges.getInComRange(nId, sender)) {
				i++;
				queue.send(nId, sender, recipient, msg, t);
			}
		}
		return i;
	}

	void noMoreInComRange(int network, EntityInterface e1, EntityInterface e2) {
		if (world != null) {
			double t = world.getTime();
			queue.dropAll(t, network, e1, e2);
		}
	}

	Doublet<Data[], int[]> readData(int network, EntityInterface tag,
			int[] dataSetDescription) {
		if (tag.hasAttribute("networked")) {
			NetworksAttribute tagAttr = (NetworksAttribute) tag
					.getAttribute("networked");
			if (!(tagAttr.isReader(network))) {
				if (tag.hasAttribute("memory")) {
					return ((Memory) tag.getAttribute("memory"))
							.getData(dataSetDescription);
				}
			}
		}
		return null;
	}

	void writed(int network, EntityInterface tag, int[] dataSetDescription,
			Data[] dataSet) {
		if (tag.hasAttribute("networked")) {
			NetworksAttribute tagAttr = (NetworksAttribute) tag
					.getAttribute("networked");
			if (!(tagAttr.isReader(network))) {
				if (tag.hasAttribute("memory")) {
					((Memory) tag.getAttribute("memory")).setData(
							dataSetDescription, dataSet);
				}
			}
		}
	}

	@Override
	public WorldBehaviour copy() {
		return new Network();
	}

	@Override
	public void init() {
		queue.init();
		ranges.init();
		ranges.update(0); // Needed at initialization
		if (world != null) {
			if (world.getDisplay() != null) {
				if (display == null)
					display = new NetworkDisplayPanel();
				display.setWorld(world, this);
				display.init();
			}
		}
	}

	@Override
	public void setWorld(World world) {
		if (world == null)
			desc = null;
		else
			desc = world.hasAttribute("network") ? (NetworksDescription) world
					.getAttribute("network") : null;
		ranges.setWorld(world);
		queue.setWorld(world);
		if (display != null)
			display.setWorld(world, this);
		this.world = world;
	}

	@Override
	public void update(double t) {
		ranges.update(t);
		queue.update(t);
		if (display != null)
			display.update(t);
	}

	public boolean isInComRange(EntityInterface e1, EntityInterface e2) {
		return ranges.isInComRange(e1, e2);
	}

	public boolean isInComRange(EntityInterface e1, String prefix) {
		return ranges.isInComRange(e1, prefix);
	}

	public boolean isInComRange(int i, EntityInterface e1, EntityInterface e2) {
		return ranges.isInComRange(i, e1, e2);
	}

	public boolean isInComRange(int i, EntityInterface e1, String prefix) {
		return ranges.isInComRange(i, e1, prefix);
	}

	public boolean isInComRange(int i, EntityInterface e1) {
		return ranges.isInComRange(i, e1);
	}

	public boolean isInComRange(EntityInterface e) {
		return ranges.isInComRange(e);
	}

	public void addListener(InternalNetworkListener o) {
		listenerManager.addListener(o);
	}

	public void removeListener(InternalNetworkListener o) {
		listenerManager.removeListener(o);
	}

	public NetworkDisplayPanel getDisplay() {
		return display;
	}

}
