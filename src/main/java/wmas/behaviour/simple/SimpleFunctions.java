package wmas.behaviour.simple;

import java.util.List;

import wmas.expression.DereferenceableObject;
import wmas.expression.functions.AbstractFunction;
import wmas.expression.functions.FunctionList;
import wmas.world.EntityInterface;

public class SimpleFunctions {
	static public class GetX extends AbstractFunction {
		public int getMinNbArg() {
			return 0;
		}

		public String getName() {
			return "x";
		}

		public Object getValue(EntityInterface e, List<Object> args) {
			if (e == null || e.getDrawingShape() == null)
				return 0;
			return e.getDrawingShape().getPosition()[0];
		}

		public boolean isStatic() {
			return false;
		}
	}

	static public class GetY extends AbstractFunction {
		public int getMinNbArg() {
			return 0;
		}

		public String getName() {
			return "y";
		}

		public Object getValue(EntityInterface e, List<Object> args) {
			if (e == null || e.getDrawingShape() == null)
				return 0;
			return e.getDrawingShape().getPosition()[1];
		}

		public boolean isStatic() {
			return false;
		}
	}

	static public class GetTime extends AbstractFunction {
		public int getMinNbArg() {
			return 0;
		}

		public String getName() {
			return "t";
		}

		public Object getValue(EntityInterface e, List<Object> args) {
			if (e == null || e.getWorld() == null)
				return 0;
			return e.getWorld().getTime();
		}

		public boolean isStatic() {
			return false;
		}
	}

	static public class GetExecutionRound extends AbstractFunction {
		public int getMinNbArg() {
			return 0;
		}

		public String getName() {
			return "run";
		}

		public Object getValue(EntityInterface e, List<Object> args) {
			if (e == null || e.getWorld() == null
					|| e.getWorld().getSimulator() == null)
				return 0;
			return e.getWorld().getSimulator().getRealRunNumber();
		}

		public boolean isStatic() {
			return false;
		}
	}

	static public class GetSize extends AbstractFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "sz";
		}

		public Object getValue(EntityInterface e, List<Object> args) {
			if (args.size() == 0 || args.get(0) == null)
				return 0;
			if (args.get(0) instanceof DereferenceableObject) {
				return ((DereferenceableObject) args.get(0)).getNbValues();
			} else if (args.get(0) instanceof Number) {
				return ((Number) args.get(0)).doubleValue() == 0 ? 0 : 1;
			}
			return 0;
		}

		public boolean isStatic() {
			return true;
		}
	}

	// And now the static function that register all the Functions
	static public void registerAll() {
		FunctionList.register(new GetX(),
				"Returns the x position of the entity");
		FunctionList.register(new GetY(),
				"Returns the y position of the entity");
		FunctionList.register(new GetTime(), "Returns the current time");
		FunctionList.register(new GetSize(),
				"Returns the number of elements of the parameter");
	}
}
