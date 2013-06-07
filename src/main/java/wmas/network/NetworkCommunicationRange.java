package wmas.network;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import wmas.world.EntityInterface;
import wmas.world.World;
import wmas.world.WorldBehaviour;

public class NetworkCommunicationRange implements WorldBehaviour {

	private Set<EntityInterface> allNetworkedEntities = new HashSet<EntityInterface>();
	private Map<Integer, Set<EntityInterface>> networkedEntity = new HashMap<Integer, Set<EntityInterface>>();
	private Map<Integer, Map<EntityInterface, Set<EntityInterface>>> inComRangeEntities = new HashMap<Integer, Map<EntityInterface, Set<EntityInterface>>>();

	private NetworksDescription descr = null;

	private InternalNetworkListener listener = null;
	private World world;

	public void setListener(InternalNetworkListener listener) {
		this.listener = listener;
	}

	public void resetEntities() {
		networkedEntity.clear();
		allNetworkedEntities.clear();
		if (world.hasAttribute("network")) {
			descr = (NetworksDescription) world.getAttribute("network");
			for (EntityInterface e : world.listActiveEntities()) {
				if (e.hasAttribute("networked")) {
					NetworksAttribute attr = (NetworksAttribute) e
							.getAttribute("networked");
					for (int i = 0; i < descr.getNbNetworks(); i++) {
						if (attr.hasNetwork(i)) {
							if (!networkedEntity.containsKey(i)) {
								networkedEntity.put(i,
										new HashSet<EntityInterface>());
							}
							networkedEntity.get(i).add(e);
							allNetworkedEntities.add(e);
						}
					}
				}
			}
		}

	}

	private int checkStatus(int index, EntityInterface e1, EntityInterface e2) {
		if (!inComRangeEntities.containsKey(index))
			return 0;
		if (!inComRangeEntities.get(index).containsKey(e1))
			return 0;
		if (!inComRangeEntities.get(index).containsKey(e2))
			return 0;
		double[] p1 = e1.getPosition();
		double[] p2 = e2.getPosition();
		if (p1 == null || p2 == null) {
			if (inComRangeEntities.get(index).get(e1).contains(e2)) {
				inComRangeEntities.get(index).get(e1).remove(e2);
				inComRangeEntities.get(index).get(e2).remove(e1);
				return -1;
			}
			return 0;
		}
		double d = Point.distance(p1[0], p1[1], p2[0], p2[1]);
		NetworksAttribute attr1 = (NetworksAttribute) e1
				.getAttribute("networked");
		NetworksAttribute attr2 = (NetworksAttribute) e2
				.getAttribute("networked");
		boolean inComRange = descr.isInRange(index, d, attr1, attr2);
		boolean wasInComRange = inComRangeEntities.get(index).get(e1)
				.contains(e2);
		if (inComRange && !wasInComRange) {
			inComRangeEntities.get(index).get(e1).add(e2);
			inComRangeEntities.get(index).get(e2).add(e1);
			return 1;
		} else if (!inComRange && wasInComRange) {
			inComRangeEntities.get(index).get(e1).remove(e2);
			inComRangeEntities.get(index).get(e2).remove(e1);
			return -1;
		}
		return 0;
	}

	private void checkEntity(double t, EntityInterface e) {
		for (int i : networkedEntity.keySet()) {
			if (networkedEntity.get(i).contains(e)) {
				for (EntityInterface e2 : networkedEntity.get(i)) {
					if (e2 != e) {
						int r = checkStatus(i, e, e2);
						if (listener != null) {
							if (r < 0) {
								listener.noMoreInComRange(t, i, e, e2);
							} else if (r > 0) {
								listener.inComRange(t, i, e, e2);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public WorldBehaviour copy() {
		return new NetworkCommunicationRange();
	}

	@Override
	public void init() {
		inComRangeEntities.clear();
		this.resetEntities();
		for (int i : networkedEntity.keySet()) {
			inComRangeEntities.put(i,
					new HashMap<EntityInterface, Set<EntityInterface>>());
			for (EntityInterface e : networkedEntity.get(i)) {
				inComRangeEntities.get(i)
						.put(e, new HashSet<EntityInterface>());
			}
		}
	}

	@Override
	public void setWorld(World world) {
		this.world = world;
	}

	@Override
	public void update(double t) {
		for (EntityInterface e : allNetworkedEntities) {
			checkEntity(t, e);
		}
	}

	private static Set<EntityInterface> emptySet = new HashSet<EntityInterface>();

	public int getBestNetwork(long size, boolean reader, EntityInterface e1,
			EntityInterface e2) {
		if (descr == null || !e1.hasAttribute("networked")
				|| !e2.hasAttribute("networked"))
			return -1;
		NetworksAttribute attr1 = (NetworksAttribute) e1
				.getAttribute("networked");
		NetworksAttribute attr2 = (NetworksAttribute) e1
				.getAttribute("networked");
		int selected = -1;
		double duration = Double.MAX_VALUE;

		for (int i : inComRangeEntities.keySet()) {
			if (descr.isReaderNetwork(i) && reader
					|| (!descr.isReaderNetwork(i) && !reader)) {
				if (inComRangeEntities.get(i).containsKey(e1)) {
					if (inComRangeEntities.get(i).get(e1).contains(e2)) {
						if (!reader || attr1.isReader(i) && !attr2.isReader(i)) {
							double d = descr.duration(i, size);
							if (d < duration) {
								selected = i;
								duration = d;
							}
						}
					}
				}
			}
		}
		return selected;
	}

	public Set<EntityInterface> getInComRange(EntityInterface e) {
		HashSet<EntityInterface> result = new HashSet<EntityInterface>();
		for (int i : inComRangeEntities.keySet()) {
			if (inComRangeEntities.get(i).containsKey(e)) {
				result.addAll(inComRangeEntities.get(i).get(e));
			}
		}
		return result;
	}

	public Set<EntityInterface> getInComRange(EntityInterface e, boolean reader) {
		HashSet<EntityInterface> result = new HashSet<EntityInterface>();
		if (descr == null)
			return result;
		for (int i : inComRangeEntities.keySet()) {
			if (descr.isReaderNetwork(i) && reader
					|| (!descr.isReaderNetwork(i) && !reader)) {
				if (inComRangeEntities.get(i).containsKey(e)) {
					result.addAll(inComRangeEntities.get(i).get(e));
				}
			}
		}
		return result;
	}

	public Set<EntityInterface> getInComRange(int index, EntityInterface e) {
		if (inComRangeEntities.containsKey(index)) {
			if (inComRangeEntities.get(index).containsKey(e)) {
				return (inComRangeEntities.get(index).get(e));
			}
		}
		return emptySet;
	}

	public Set<EntityInterface> getInComRange(int index, EntityInterface e,
			String prefix) {
		if (prefix == null || prefix.isEmpty())
			return getInComRange(index, e);
		HashSet<EntityInterface> result = new HashSet<EntityInterface>();
		if (inComRangeEntities.containsKey(index)) {
			if (inComRangeEntities.get(index).containsKey(e)) {
				for (EntityInterface e2 : inComRangeEntities.get(index).get(e)) {
					if (e2.getName() != null && e2.getName().startsWith(prefix))
						result.add(e2);
				}
			}
		}
		return result;
	}

	public Set<EntityInterface> getInComRange(EntityInterface e, String prefix) {
		if (prefix == null || prefix.isEmpty())
			return getInComRange(e);
		HashSet<EntityInterface> result = new HashSet<EntityInterface>();
		for (int index : inComRangeEntities.keySet()) {
			if (inComRangeEntities.get(index).containsKey(e)) {
				for (EntityInterface e2 : inComRangeEntities.get(index).get(e)) {
					if (e2.getName() != null && e2.getName().startsWith(prefix))
						result.add(e2);
				}
			}
		}
		return result;
	}

	public Set<EntityInterface> getInComRange(EntityInterface e, String prefix,
			boolean reader) {
		if (prefix == null || prefix.isEmpty())
			return getInComRange(e);
		HashSet<EntityInterface> result = new HashSet<EntityInterface>();
		if (descr == null)
			return result;
		for (int index : inComRangeEntities.keySet()) {
			if (descr.isReaderNetwork(index) && reader
					|| (!descr.isReaderNetwork(index) && !reader)) {
				if (inComRangeEntities.get(index).containsKey(e)) {
					for (EntityInterface e2 : inComRangeEntities.get(index)
							.get(e)) {
						if (e2.getName() != null
								&& e2.getName().startsWith(prefix))
							result.add(e2);
					}
				}
			}
		}
		return result;
	}

	public boolean isInComRange(EntityInterface e1, EntityInterface e2) {
		for (int i : inComRangeEntities.keySet()) {
			if (inComRangeEntities.get(i).containsKey(e1)) {
				if (inComRangeEntities.get(i).get(e1).contains(e2))
					return true;
			}
		}
		return false;
	}

	public boolean isInComRange(EntityInterface e1, String prefix) {
		for (int i : inComRangeEntities.keySet()) {
			if (inComRangeEntities.get(i).containsKey(e1)) {
				for (EntityInterface e2 : inComRangeEntities.get(i).get(e1)) {
					if (e2.getName() != null && e2.getName().startsWith(prefix))
						return true;
				}
			}
		}
		return false;
	}

	public boolean isInComRange(int i, EntityInterface e1) {
		if (inComRangeEntities.containsKey(i)) {
			if (inComRangeEntities.get(i).containsKey(e1)) {
				return !inComRangeEntities.get(i).get(e1).isEmpty();
			}
		}
		return false;
	}

	public boolean isInComRange(int i, EntityInterface e1, String prefix) {
		if (inComRangeEntities.containsKey(i)) {
			if (inComRangeEntities.get(i).containsKey(e1)) {
				for (EntityInterface e2 : inComRangeEntities.get(i).get(e1)) {
					if (e2.getName() != null && e2.getName().startsWith(prefix))
						return true;
				}
			}
		}
		return false;
	}

	public boolean isInComRange(int i, EntityInterface e1, EntityInterface e2) {
		if (inComRangeEntities.containsKey(i)) {
			if (inComRangeEntities.get(i).containsKey(e1)) {
				return (inComRangeEntities.get(i).get(e1).contains(e2));
			}
		}
		return false;
	}

	public boolean isInComRange(EntityInterface e) {
		for (int i : inComRangeEntities.keySet()) {
			if (inComRangeEntities.get(i).containsKey(e)) {
				if (!inComRangeEntities.get(i).get(e).isEmpty())
					return true;
			}
		}
		return false;
	}
}
