package wmas.network;

import java.util.HashSet;

import wmas.network.NetworkQueue.ScheduledMessage;
import wmas.util.Doublet;
import wmas.world.EntityInterface;
import wmas.world.memory.Data;

public class NetworkListenerManager implements InternalNetworkListener {

	private HashSet<InternalNetworkListener> listeners = new HashSet<InternalNetworkListener>();
	private Network parent;

	public NetworkListenerManager(Network network) {
		parent = network;
	}

	@Override
	public void inComRange(double t, int network, EntityInterface e1,
			EntityInterface e2) {
		for (InternalNetworkListener nl : listeners)
			nl.inComRange(t, network, e1, e2);
	}

	@Override
	public void noMoreInComRange(double t, int network, EntityInterface e1,
			EntityInterface e2) {
		parent.noMoreInComRange(network, e1, e2);
		for (InternalNetworkListener nl : listeners)
			nl.noMoreInComRange(t, network, e1, e2);
	}

	@Override
	public void arrived(ScheduledMessage msg) {
		parent.reportReceived(msg);
		Doublet<Data[], int[]> r = null;
		if (msg.isRead) {
			r = parent.readData(msg.networkIndex, msg.recipient,
					msg.dataSetDescription);
		} else if (msg.isWrite) {
			parent.writed(msg.networkIndex, msg.recipient,
					msg.dataSetDescription, msg.dataSet);
		} else {
			((NetworksAttribute) msg.recipient.getAttribute("networked"))
					.receive(msg);
		}
		for (InternalNetworkListener nl : listeners)
			nl.arrived(msg);
		if (r != null) {
			msg.dataSet = r.getFirst();
			msg.dataSetDescription = r.getSecond();
			msg.isReadAnswer = true;
		} else {
			msg.dataSet = null;
			msg.dataSetDescription = null;
		}
	}

	@Override
	public void dropped(double t, ScheduledMessage msg) {
		parent.reportDropped(t, msg);
		for (InternalNetworkListener nl : listeners)
			nl.dropped(t, msg);
	}

	@Override
	public void scheduled(ScheduledMessage msg) {
		parent.reportUsage(msg.networkIndex);
		for (InternalNetworkListener nl : listeners)
			nl.scheduled(msg);
	}

	public void addListener(InternalNetworkListener o) {
		listeners.add(o);
	}

	public void removeListener(InternalNetworkListener o) {
		listeners.remove(o);
	}
}
