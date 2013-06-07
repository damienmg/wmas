package wmas.expression.functions;

import java.util.List;

import wmas.world.EntityInterface;

// Contains nearly all the functions of java.lang.Math
public class LogicalOperators {

	private interface LogicalOperator extends Function {
		boolean getValue(boolean v1, boolean v2);
	}

	private static abstract class AbstractLogicalOperator extends
			AbstractNumericFunction implements LogicalOperator {
		public int getMinNbArg() {
			return 2;
		}

		@Override
		public Number getNumberValue(EntityInterface e, List<Number> args) {
			Number n1 = args.get(0);
			Number n2 = args.get(1);
			boolean v1 = (n1.doubleValue() != 0);
			boolean v2 = (n2.doubleValue() != 0);
			return getValue(v1, v2) ? 1 : 0;
		}
	}

	// Unary not
	static public class UnaryNot extends AbstractNumericFunction {
		public Number getNumberValue(EntityInterface e, List<Number> args) {
			Number n1 = args.get(0);
			return (n1.doubleValue() != 0) ? 0 : 1;
		}

		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "!";
		}
	}

	// Binary logical operators
	static public class LogicalAnd extends AbstractLogicalOperator {
		public String getName() {
			return "&&";
		}

		public boolean getValue(boolean v1, boolean v2) {
			return (v1 && v2);
		}
	}

	static public class LogicalOr extends AbstractLogicalOperator {
		public String getName() {
			return "||";
		}

		public boolean getValue(boolean v1, boolean v2) {
			return (v1 || v2);
		}
	}

	static public class LogicalImplies extends AbstractLogicalOperator {
		public String getName() {
			return "->";
		}

		public boolean getValue(boolean v1, boolean v2) {
			return !(v1 && !v2);
		}
	}

	static public class LogicalImpliedBy extends AbstractLogicalOperator {
		public String getName() {
			return "<-";
		}

		public boolean getValue(boolean v1, boolean v2) {
			return !(v2 && !v1);
		}
	}

	static public class LogicalEquiv extends AbstractLogicalOperator {
		public String getName() {
			return "<->";
		}

		public boolean getValue(boolean v1, boolean v2) {
			return (v1 && v2) || (!v1 && !v2);
		}
	}

	// Existence operator

	// And now the static function that register all the Functions
	static public void registerAll() {
		FunctionList.register(new UnaryNot(), "Logical not");
		FunctionList.register(new LogicalAnd(), "Logical conjunction (^)");
		FunctionList.register(new LogicalOr(), "Logical disjunction (v)");
		FunctionList.register(new LogicalImplies(), "Logical implication (=>)");
		FunctionList
				.register(new LogicalImpliedBy(), "Logical implied by (<=)");
		FunctionList.register(new LogicalEquiv(), "Logical equivalence (<=>)");
	}
}
