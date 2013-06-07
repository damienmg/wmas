package wmas.network;

import wmas.world.EntityInterface;

public interface InternalNetworkListener {
	public void inComRange(double t, int network, EntityInterface e1,
			EntityInterface e2);

	public void noMoreInComRange(double t, int network, EntityInterface e1,
			EntityInterface e2);

	public void scheduled(NetworkQueue.ScheduledMessage msg);

	public void arrived(NetworkQueue.ScheduledMessage msg);

	public void dropped(double t, NetworkQueue.ScheduledMessage msg);
}
