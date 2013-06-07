package wmas.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import wmas.world.EntityInterface;
import wmas.world.World;
import wmas.world.WorldBehaviour;
import wmas.world.memory.Data;

public class NetworkQueue implements WorldBehaviour {

	private InternalNetworkListener listener = null;
	private World world = null;
	private NetworksDescription descr = null;
	private Stack<ScheduledMessage> pool = new Stack<ScheduledMessage>();
	private PriorityQueue<ScheduledMessage> messages = new PriorityQueue<ScheduledMessage>();;
	private Map<EntityInterface, Set<ScheduledMessage>> scheduledMessagesByEntity = new HashMap<EntityInterface, Set<ScheduledMessage>>();

	static long getSize(Data[] dataSet, int[] descr) {
		int s = descr == null ? 1 : 4 * descr.length;
		if (dataSet != null) {
			for (int i = 0; i < dataSet.length; i++) {
				s += dataSet[i] == null ? 0 : dataSet[i].getSize();
			}
		}
		return s;
	}

	public class ScheduledMessage implements Comparable<ScheduledMessage> {
		boolean freed = false;
		EntityInterface sender;
		EntityInterface recipient;
		NetworkMessage msg = null;
		double scheduledTime;
		double departureTime;
		int networkIndex;
		boolean isWrite = false;
		boolean isRead = false;
		boolean isReadAnswer = false;
		int[] dataSetDescription = null;
		Data[] dataSet = null;
		NetworksAttribute networkAttributes = null;

		private String dataSetDescription() {
			if (dataSetDescription == null || dataSetDescription.length == 0)
				return "()";
			else if (dataSetDescription.length == 1
					&& dataSetDescription[0] < 0)
				return "(-1)";
			String s = "(";
			boolean first = true;
			for (int i : dataSetDescription) {
				if (first)
					first = false;
				else
					s += ", ";
				s += i;
			}
			return s + ")";
		}

		private String dataSet() {
			if (dataSetDescription == null || dataSet == null)
				return dataSetDescription();
			boolean first = true;
			String s = "(";
			int i;
			for (i = 0; i < Math.min(dataSet.length, dataSetDescription.length); i++) {
				if (first)
					first = false;
				else
					s += ", ";
				s += dataSetDescription[i] + "   " + dataSet[i].toString();
			}
			while (i < dataSetDescription.length) {
				i++;
				if (first)
					first = false;
				else
					s += ", ";
				s += dataSetDescription[i];
			}
			return s;
		}

		public EntityInterface getSender() {
			return sender;
		}

		public EntityInterface getRecipient() {
			return recipient;
		}

		public double getDeparture() {
			return departureTime;
		}

		public double getArrival() {
			return scheduledTime;
		}

		public String getDescription() {
			if (isWrite || isReadAnswer) {
				return (isWrite ? "write" : "readAnswer") + dataSet();
			} else if (isRead) {
				if (dataSetDescription == null
						|| dataSetDescription.length == 0)
					return "readIds()";
				else if (dataSetDescription.length == 1
						&& dataSetDescription[0] < 0)
					return "readAll()";
				return "read" + dataSetDescription();
			}
			return msg.toString();
		}

		private ScheduledMessage(int index, EntityInterface sender,
				EntityInterface receiver, NetworkMessage msg, double t) {
			super();
			this.networkIndex = index;
			this.sender = sender;
			this.recipient = receiver;
			this.msg = msg;
			schedule(t);
		}

		private ScheduledMessage(int index, EntityInterface reader,
				EntityInterface tag, int[] dataSetDescription, double t) {
			super();
			this.networkIndex = index;
			this.sender = reader;
			this.recipient = tag;
			this.dataSetDescription = dataSetDescription;
			this.isRead = true;
			schedule(t);
		}

		private ScheduledMessage(int index, EntityInterface reader,
				EntityInterface tag, int[] dataSetDescription, Data[] dataSet,
				double t) {
			super();
			this.networkIndex = index;
			this.sender = reader;
			this.recipient = tag;
			this.dataSetDescription = dataSetDescription;
			this.dataSet = dataSet;
			this.isWrite = true;
			schedule(t);
		}

		private void drop(double t) {
			if (listener != null) {
				listener.dropped(t, this);
			}
			free();
		}

		public void drop(double t, int index, EntityInterface e1,
				EntityInterface e2) {
			if (this.networkIndex == index
					&& ((e1 == sender) || (e2 == sender))
					&& ((e2 == recipient) || (e1 == recipient))) {
				drop(t);
			}
		}

		public int compareTo(ScheduledMessage o) {
			return Double.compare(scheduledTime, o.scheduledTime);
		}

		long getSize() {
			if (isRead) {
				return NetworkQueue.getSize(null, dataSetDescription);
			} else if (isReadAnswer || isWrite) {
				return NetworkQueue.getSize(dataSet, dataSetDescription);
			} else
				return msg.getSize();
		}

		void schedule(double t) {
			departureTime = t;
			freed = false;
			scheduledTime = descr.duration(this.networkIndex, getSize()) + t;
			messages.offer(this);
			if (!scheduledMessagesByEntity.containsKey(sender)) {
				scheduledMessagesByEntity.put(sender,
						new HashSet<ScheduledMessage>());
			}
			if (!scheduledMessagesByEntity.containsKey(recipient)) {
				scheduledMessagesByEntity.put(recipient,
						new HashSet<ScheduledMessage>());
			}
			scheduledMessagesByEntity.get(sender).add(this);
			scheduledMessagesByEntity.get(recipient).add(this);
			if (listener != null)
				listener.scheduled(this);
		}

		public void free() {
			if (freed)
				System.err.println("double-free");
			if (networkAttributes != null) {
				networkAttributes.remove(this);
				networkAttributes = null;
			}
			messages.remove(this);
			freed = true;
			if (scheduledMessagesByEntity.containsKey(sender)) {
				scheduledMessagesByEntity.get(sender).remove(this);
			}
			if (scheduledMessagesByEntity.containsKey(recipient)) {
				scheduledMessagesByEntity.get(recipient).remove(this);
			}
			pool.add(this);
		}

		public int getNetworkId() {
			return networkIndex;
		}
	}

	private ScheduledMessage getInstance(int index, EntityInterface sender,
			EntityInterface receiver, NetworkMessage msg, double t) {
		if (pool.isEmpty())
			return new ScheduledMessage(index, sender, receiver, msg, t);
		else {
			ScheduledMessage m = pool.pop();
			if (m.networkAttributes != null) {
				System.err.println("err");
			}
			m.isRead = false;
			m.freed = false;
			m.isReadAnswer = false;
			m.isWrite = false;
			m.dataSet = null;
			m.dataSetDescription = null;
			m.msg = msg;
			m.sender = sender;
			m.recipient = receiver;
			m.networkIndex = index;
			m.schedule(t);
			return m;
		}
	}

	private ScheduledMessage getInstance(int index, EntityInterface sender,
			EntityInterface receiver, int[] dataSetDescription, Data[] dataSet,
			double t) {
		if (pool.isEmpty())
			return new ScheduledMessage(index, sender, receiver,
					dataSetDescription, dataSet, t);
		else {
			ScheduledMessage m = pool.pop();
			m.dataSet = dataSet;
			m.freed = false;
			m.dataSetDescription = dataSetDescription;
			m.msg = null;
			m.freed = false;
			m.isRead = false;
			m.isReadAnswer = false;
			m.isWrite = true;
			m.sender = sender;
			m.recipient = receiver;
			m.networkIndex = index;
			m.schedule(t);
			return m;
		}
	}

	private ScheduledMessage getInstance(int index, EntityInterface sender,
			EntityInterface receiver, int[] dataSetDescription, double t) {
		if (pool.isEmpty())
			return new ScheduledMessage(index, sender, receiver,
					dataSetDescription, t);
		else {
			ScheduledMessage m = pool.pop();
			m.freed = false;
			m.dataSet = null;
			m.dataSetDescription = dataSetDescription;
			m.msg = null;
			m.freed = false;
			m.isRead = true;
			m.isReadAnswer = false;
			m.isWrite = false;
			m.sender = sender;
			m.recipient = receiver;
			m.networkIndex = index;
			m.schedule(t);
			return m;
		}
	}

	public void send(int network, EntityInterface sender,
			EntityInterface recipient, NetworkMessage msg, double t) {
		if (msg != null) {
			getInstance(network, sender, recipient, msg, t);
		}
	}

	public void read(int network, EntityInterface reader, EntityInterface tag,
			int[] dataSetDescription, double t) {
		getInstance(network, reader, tag, dataSetDescription, t);
	}

	public void write(int network, EntityInterface reader, EntityInterface tag,
			int[] dataSetDescription, Data[] dataSet, double t) {
		if (dataSetDescription != null && dataSet != null) {
			getInstance(network, reader, tag, dataSetDescription, dataSet, t);
		}
	}

	public void setListener(InternalNetworkListener listener) {
		this.listener = listener;
	}

	@Override
	public WorldBehaviour copy() {
		return new NetworkQueue();
	}

	@Override
	public void init() {
		scheduledMessagesByEntity.clear();
		descr = (NetworksDescription) this.world.getAttribute("network");
		for (Object msg : messages.toArray())
			((ScheduledMessage) msg).free();
		messages.clear();
	}

	public void setWorld(World world) {
		this.world = world;
	}

	@Override
	public void update(double t) {
		while (!messages.isEmpty() && (messages.peek().scheduledTime < t)) {
			ScheduledMessage msg = messages.poll();
			if (msg.isRead) {
				if (listener != null) {
					listener.arrived(msg);
					if ((msg.dataSet != null || msg.dataSetDescription != null)
							&& msg.isReadAnswer) {
						msg.isRead = false;
						EntityInterface res = msg.sender;
						msg.sender = msg.recipient;
						msg.recipient = res;
						msg.schedule(msg.scheduledTime);
					}
				} else {
					msg.free();
				}
			} else {
				if (listener != null)
					listener.arrived(msg);
				if (msg.isWrite)
					msg.free();
			}
		}
	}

	Set<ScheduledMessage> toDrop = new HashSet<ScheduledMessage>();

	public void dropAll(double t, int index, EntityInterface e1,
			EntityInterface e2) {
		toDrop.clear();
		if (scheduledMessagesByEntity.containsKey(e1))
			toDrop.addAll(scheduledMessagesByEntity.get(e1));
		if (scheduledMessagesByEntity.containsKey(e2))
			toDrop.addAll(scheduledMessagesByEntity.get(e2));
		for (ScheduledMessage m : toDrop) {
			m.drop(t, index, e1, e2);
		}
	}

}
