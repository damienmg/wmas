package wmas.network;

import java.util.LinkedList;
import java.util.List;

import wmas.expression.functions.AbstractFunction;
import wmas.expression.functions.AbstractNumericFunction;
import wmas.expression.functions.FunctionList;
import wmas.world.EntityInterface;
import wmas.world.memory.Data;
import wmas.world.memory.Memory;

public class NetworkFunctions {
	private static int[] emptySet = new int[0];

	static class InComRange extends AbstractFunction {

		public int getMaxNbArg() {
			return 2;
		}

		public int getMinNbArg() {
			return 0;
		}

		public boolean isStatic() {
			return false;
		}

		public String getName() {
			return "inRange";
		}

		@Override
		public Object getValue(EntityInterface e, List<Object> args) {
			if (args.size() == 0)
				return ((Network) e.getWorld().getWorldBehaviour("network"))
						.isInComRange(e);
			if (args.size() == 1) {
				if (args.get(0) instanceof Number) {
					return ((Network) e.getWorld().getWorldBehaviour("network"))
							.isInComRange(((Number) args.get(0)).intValue(), e);
				}
				return ((Network) e.getWorld().getWorldBehaviour("network"))
						.isInComRange(e, args.get(0).toString());
			}
			int id = 0;
			String prefix = "";
			if (args.get(0) instanceof Number) {
				id = ((Number) args.get(0)).intValue();
				prefix = args.get(1).toString();
			} else {
				id = AbstractNumericFunction.getNumberFromArgument(args.get(1))
						.intValue();
				prefix = args.get(0).toString();
			}
			return ((Network) e.getWorld().getWorldBehaviour("network"))
					.isInComRange(id, e, prefix);
		}
	}

	static class Send extends AbstractFunction {

		public int getMaxNbArg() {
			return 3;
		}

		public int getMinNbArg() {
			return 1;
		}

		public boolean isStatic() {
			return false;
		}

		public String getName() {
			return "send";
		}

		@Override
		public Object getValue(EntityInterface e, List<Object> args) {
			if (e.getWorld() == null || !e.getWorld().hasAttribute("network"))
				return false;
			Network network = ((Network) e.getWorld().getWorldBehaviour(
					"network"));
			if (args.size() == 0)
				return false;
			if (!(args.get(0) instanceof NetworkMessage))
				return false;
			NetworkMessage msg = (NetworkMessage) args.get(0);
			String prefix = "";
			if (args.size() == 1) {
				return network.send(e, prefix, msg);
			}
			if (args.size() == 2) {
				if (args.get(1) instanceof Number) {
					return network.send(((Number) args.get(1)).intValue(), e,
							prefix, msg);
				}
				return network.send(e, args.get(1).toString(), msg);
			}
			int id = 0;
			if (args.get(1) instanceof Number) {
				id = ((Number) args.get(1)).intValue();
				prefix = args.get(2).toString();
			} else {
				id = AbstractNumericFunction.getNumberFromArgument(args.get(2))
						.intValue();
				prefix = args.get(1).toString();
			}
			return network.send(id, e, prefix, msg);
		}
	}

	static class Broadcast extends AbstractFunction {

		public int getMaxNbArg() {
			return 2;
		}

		public int getMinNbArg() {
			return 1;
		}

		public boolean isStatic() {
			return false;
		}

		public String getName() {
			return "broadcast";
		}

		@Override
		public Object getValue(EntityInterface e, List<Object> args) {
			if (e.getWorld() == null || !e.getWorld().hasAttribute("network"))
				return false;
			Network network = ((Network) e.getWorld().getWorldBehaviour(
					"network"));
			if (args.size() == 0)
				return false;
			if (!(args.get(0) instanceof NetworkMessage))
				return false;
			NetworkMessage msg = (NetworkMessage) args.get(0);
			if (args.size() == 1) {
				return network.broadcast(e, msg);
			}
			int id = ((Number) args.get(1)).intValue();
			return network.broadcast(id, e, msg);
		}
	}

	static class Read extends AbstractFunction {

		public int getMaxNbArg() {
			return Integer.MAX_VALUE;
		}

		public int getMinNbArg() {
			return 1;
		}

		public boolean isStatic() {
			return false;
		}

		public String getName() {
			return "read";
		}

		@Override
		public Object getValue(EntityInterface e, List<Object> args) {
			if (e.getWorld() == null || !e.getWorld().hasAttribute("network"))
				return false;
			Network network = ((Network) e.getWorld().getWorldBehaviour(
					"network"));
			if (args.size() == 0)
				return network.read(e, "", emptySet);
			String prefix = "";
			int id = -1;
			if (!(args.get(0) instanceof Number)) {
				prefix = args.remove(0).toString();
			} else if (args.size() >= 2 && !(args.get(1) instanceof Number)) {
				id = ((Number) args.remove(0)).intValue();
				prefix = args.remove(0).toString();
			}
			int[] res = new int[args.size()];
			int i = 0;
			for (Object o : args) {
				res[i] = AbstractNumericFunction.getNumberFromArgument(o)
						.intValue();
			}
			if (id < 0)
				return network.read(e, prefix, res);
			return network.read(id, e, prefix, res);
		}
	}

	static class Write extends AbstractFunction {

		public int getMaxNbArg() {
			return Integer.MAX_VALUE;
		}

		public int getMinNbArg() {
			return 1;
		}

		public boolean isStatic() {
			return false;
		}

		public String getName() {
			return "read";
		}

		private static boolean parseDescriptionAndWrite(Network network,
				EntityInterface e, List<Object> args, int id, String prefix) {
			List<Data> datas = new LinkedList<Data>();
			List<Integer> desc = new LinkedList<Integer>();
			boolean finished = false;
			boolean lastIsData = false;
			for (Object o : args) {
				if (lastIsData || finished) {
					desc.add(AbstractNumericFunction.getNumberFromArgument(o)
							.intValue());
					lastIsData = false;
				} else {
					if (o instanceof Data) {
						datas.add((Data) o);
						lastIsData = true;
					} else if (o instanceof Number) {
						desc.add(((Number) o).intValue());
						finished = true;
					} else {
						datas.add(Memory.createData(o));
						lastIsData = true;
					}
				}
			}
			Data[] resDatas = new Data[datas.size()];
			int[] resDesc = new int[desc.size()];
			int i = 0;
			for (Data d : datas) {
				resDatas[i] = d;
				i++;
			}
			i = 0;
			for (int k : desc) {
				resDesc[i] = k;
				i++;
			}
			if (id < 0)
				return network.write(e, prefix, resDesc, resDatas);
			return network.write(id, e, prefix, resDesc, resDatas);
		}

		@Override
		public Object getValue(EntityInterface e, List<Object> args) {
			if (e.getWorld() == null || !e.getWorld().hasAttribute("network"))
				return false;
			Network network = ((Network) e.getWorld().getWorldBehaviour(
					"network"));
			if (args.size() == 0)
				return network.read(e, "", emptySet);
			String prefix = "";
			int id = -1;
			if (!(args.get(0) instanceof Number)) {
				prefix = args.remove(0).toString();
			} else if (args.size() >= 2 && !(args.get(1) instanceof Number)) {
				id = ((Number) args.remove(0)).intValue();
				prefix = args.remove(0).toString();
			}
			return parseDescriptionAndWrite(network, e, args, id, prefix);
		}
	}

	static class ReadAnswer extends AbstractFunction {
		public int getMaxNbArg() {
			return 2;
		}

		public int getMinNbArg() {
			return 1;
		}

		public boolean isStatic() {
			return false;
		}

		public String getName() {
			return "readAnswer";
		}

		@Override
		public Object getValue(EntityInterface e, List<Object> args) {
			if (!e.hasAttribute("networked"))
				return 0;
			String prefix = null;
			NetworksAttribute attr = (NetworksAttribute) (e
					.getAttribute("networked"));
			if (args.size() == 0)
				return attr.getLastReadAnswer(prefix);
			if (args.size() == 1) {
				if (args.get(0) instanceof Number)
					return attr.getLastReadAnswer(
							((Number) args.get(0)).intValue(), prefix);
				return attr.getLastReadAnswer(args.get(0).toString());
			}
			int id = 0;
			if (args.get(0) instanceof Number) {
				id = ((Number) args.get(0)).intValue();
				prefix = args.get(1).toString();
			} else {
				id = AbstractNumericFunction.getNumberFromArgument(args.get(1))
						.intValue();
				prefix = args.get(0).toString();
			}
			return attr.getLastReadAnswer(id, prefix);
		}
	}

	static class Receive extends AbstractFunction {
		public int getMaxNbArg() {
			return 2;
		}

		public int getMinNbArg() {
			return 1;
		}

		public boolean isStatic() {
			return false;
		}

		public String getName() {
			return "recv";
		}

		@Override
		public Object getValue(EntityInterface e, List<Object> args) {
			if (!e.hasAttribute("networked"))
				return 0;
			String prefix = null;
			NetworkMessage nm = null;
			int id = -1;
			NetworksAttribute attr = (NetworksAttribute) (e
					.getAttribute("networked"));
			if (args.size() == 0)
				return attr.poll(null, null);
			for (Object o : args) {
				if (o instanceof Number)
					id = ((Number) o).intValue();
				else if (o instanceof NetworkMessage)
					nm = (NetworkMessage) o;
				else
					prefix = o.toString();
			}
			if (id < 0)
				return attr.poll(prefix, nm);
			return attr.poll(id, prefix, nm);
		}
	}

	static class HasReceived extends AbstractFunction {
		public int getMaxNbArg() {
			return 2;
		}

		public int getMinNbArg() {
			return 1;
		}

		public boolean isStatic() {
			return false;
		}

		public String getName() {
			return "received";
		}

		@Override
		public Object getValue(EntityInterface e, List<Object> args) {
			if (!e.hasAttribute("networked"))
				return 0;
			String prefix = null;
			NetworkMessage nm = null;
			int id = -1;
			NetworksAttribute attr = (NetworksAttribute) (e
					.getAttribute("networked"));
			if (args.size() == 0)
				return attr.peek();
			for (Object o : args) {
				if (o instanceof Number)
					id = ((Number) o).intValue();
				else if (o instanceof NetworkMessage)
					nm = (NetworkMessage) o;
				else
					prefix = o.toString();
			}
			if (id < 0)
				return attr.peek(prefix, nm);
			return attr.peek(id, prefix, nm);
		}
	}

	static class MessageType extends AbstractFunction {
		public int getMaxNbArg() {
			return 1;
		}

		public int getMinNbArg() {
			return 1;
		}

		public boolean isStatic() {
			return true;
		}

		public String getName() {
			return "msgType";
		}

		@Override
		public Object getValue(EntityInterface e, List<Object> args) {
			if (args.size() == 0)
				return null;
			if (args.get(0) instanceof NetworkMessage) {
				return ((NetworkMessage) args.get(0)).getType();
			}
			return null;
		}
	}

	public static void registerAll() {
		FunctionList
				.register(
						new Broadcast(),
						"Broadcast a message given in argument, optional parameter gives the network number. Returns the number of message sent.");
		FunctionList
				.register(
						new Send(),
						"Sends the message given in argument, optional parameters give the network number and a prefix for determining the entity to send the message to");
		FunctionList
				.register(
						new InComRange(),
						"Determines wether there is an entity in communication range, optional parameters give the network number and a prefix to match entity names");
		FunctionList
				.register(
						new Read(),
						"Read data from a tag. Parameter list is in the given form : [[network id] entity]? [index of data to read]*");
		FunctionList
				.register(
						new Write(),
						"Write data on a tag. Parameter list is in the given form : [[network id] entity]? [index, value]* where value can be null");
		FunctionList
				.register(
						new ReadAnswer(),
						"Get the answer of the last read. 0 on no answer, Array of id => data if answer. Optional parameters are the network id and the entity name prefix");
		FunctionList
				.register(
						new Receive(),
						"Pop the last received message. 0 on no message. Parameters can be any combinaison of the id of the network, a message to match the message type and an entity name prefix.");
		FunctionList
				.register(
						new HasReceived(),
						"Returns true if there was a last received message. Parameters can be any combinaison of the id of the network, a message to match the message type and an entity name prefix.");
		FunctionList.register(new MessageType(),
				"Returns the type of a message");
	}
}
