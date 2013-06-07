package wmas.expression.functions;

import java.util.List;

import wmas.world.EntityInterface;

// Contains nearly all the functions of java.lang.Math
public class ArithmeticOperators {

	private interface ArithmeticOperator extends Function {
		double getValue(double v1, double v2);

		double getValue(long v1, double v2);

		double getValue(double v1, long v2);

		long getValue(long v1, long v2);
	}

	static private Number convert(Number n) {
		if (n instanceof Integer || n instanceof Long)
			return n;
		if (n.doubleValue() == n.longValue())
			return n.longValue();
		return n;
	}

	private static abstract class AbstractArithmeticOperator extends
			AbstractNumericFunction implements ArithmeticOperator {
		public int getMinNbArg() {
			return 2;
		}

		@Override
		public Number getNumberValue(EntityInterface e, List<Number> args) {
			Number n1 = args.get(0);
			Number n2 = args.get(1);
			if (n1 instanceof Double) {
				if (n2 instanceof Double)
					return convert(getValue(n1.doubleValue(), n2.doubleValue()));
				return convert(getValue(n1.doubleValue(), n2.longValue()));
			} else {
				if (n2 instanceof Double)
					return convert(getValue(n1.longValue(), n2.doubleValue()));
				return getValue(n1.longValue(), n2.longValue());
			}
		}
	}

	// Arithmetic operators
	static public class Addition extends AbstractArithmeticOperator {
		public String getName() {
			return "+";
		}

		public double getValue(double v1, double v2) {
			return v1 + v2;
		}

		public double getValue(long v1, double v2) {
			return v1 + v2;
		}

		public double getValue(double v1, long v2) {
			return v1 + v2;
		}

		public long getValue(long v1, long v2) {
			return v1 + v2;
		}

		public int getMinNbArg() {
			return 1;
		}

		public int getMaxNbArg() {
			return 2;
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			if (args.size() == 1) {
				return args.get(0);
			}
			return super.getNumberValue(e, args);
		}
	}

	static public class Substraction extends AbstractArithmeticOperator {
		public String getName() {
			return "-";
		}

		public double getValue(double v1, double v2) {
			return v1 - v2;
		}

		public double getValue(long v1, double v2) {
			return v1 - v2;
		}

		public double getValue(double v1, long v2) {
			return v1 - v2;
		}

		public long getValue(long v1, long v2) {
			return v1 - v2;
		}

		public int getMinNbArg() {
			return 1;
		}

		public int getMaxNbArg() {
			return 2;
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			if (args.size() == 1) {
				Number n = args.get(0);
				if (n.longValue() == n.doubleValue())
					return -n.longValue();
				else
					return -n.doubleValue();
			}
			return super.getNumberValue(e, args);
		}
	}

	static public class Multiplication extends AbstractArithmeticOperator {
		public String getName() {
			return "*";
		}

		public double getValue(double v1, double v2) {
			return v1 * v2;
		}

		public double getValue(long v1, double v2) {
			return v1 * v2;
		}

		public double getValue(double v1, long v2) {
			return v1 * v2;
		}

		public long getValue(long v1, long v2) {
			return v1 * v2;
		}
	}

	static public class Division extends AbstractArithmeticOperator {
		public String getName() {
			return "/";
		}

		public double getValue(double v1, double v2) {
			return v1 / v2;
		}

		public double getValue(long v1, double v2) {
			return v1 / v2;
		}

		public double getValue(double v1, long v2) {
			return v1 / v2;
		}

		public long getValue(long v1, long v2) {
			return v1 / v2;
		}
	}

	static public class Modulo extends AbstractArithmeticOperator {
		public String getName() {
			return "%";
		}

		public double getValue(double v1, double v2) {
			return v1 % v2;
		}

		public double getValue(long v1, double v2) {
			return v1 % v2;
		}

		public double getValue(double v1, long v2) {
			return v1 % v2;
		}

		public long getValue(long v1, long v2) {
			return v1 % v2;
		}
	}

	static public class Exponent extends AbstractArithmeticOperator {
		public String getName() {
			return "^";
		}

		public double getValue(double v1, double v2) {
			return Math.pow(v1, v2);
		}

		public double getValue(long v1, double v2) {
			return Math.pow(v1, v2);
		}

		public double getValue(double v1, long v2) {
			return Math.pow(v1, v2);
		}

		public long getValue(long v1, long v2) {
			return (long) Math.pow(v1, v2);
		}
	}

	static public class BitwiseOr extends AbstractNumericFunction {
		public String getName() {
			return "|";
		}

		public int getMinNbArg() {
			return 2;
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return args.get(0).longValue() | args.get(1).longValue();
		}
	}

	static public class BitwiseAnd extends AbstractNumericFunction {
		public String getName() {
			return "|";
		}

		public int getMinNbArg() {
			return 2;
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return args.get(0).longValue() & args.get(1).longValue();
		}
	}

	static public class BitwiseXor extends AbstractNumericFunction {
		public String getName() {
			return "|";
		}

		public int getMinNbArg() {
			return 2;
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return args.get(0).longValue() ^ args.get(1).longValue();
		}
	}

	static public class BitwiseNot extends AbstractNumericFunction {
		public String getName() {
			return "~";
		}

		public int getMinNbArg() {
			return 1;
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return ~args.get(0).longValue();
		}
	}

	// Tests
	static public class Equal extends AbstractFunction {
		public int getMinNbArg() {
			return 2;
		}

		public String getName() {
			return "=";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return args.get(0).doubleValue() == args.get(1).doubleValue() ? 1
					: 0;
		}

		@Override
		public Object getValue(EntityInterface e, List<Object> args) {
			if (args.size() != 2)
				return false;
			if (args.get(0) == null)
				return args.get(1) == null;
			if (args.get(1) == null)
				return false;
			if (args.get(0) instanceof Number && args.get(1) instanceof Number) {
				return (((Number) args.get(0)).doubleValue() == ((Number) args
						.get(1)).doubleValue());
			}
			return args.get(0).equals(args.get(1));
		}
	}

	static public class Different extends AbstractFunction {
		public int getMinNbArg() {
			return 2;
		}

		public String getName() {
			return "<>";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return args.get(0).doubleValue() == args.get(1).doubleValue() ? 1
					: 0;
		}

		@Override
		public Object getValue(EntityInterface e, List<Object> args) {
			if (args.size() != 2)
				return true;
			if (args.get(0) == null)
				return args.get(1) != null;
			if (args.get(1) == null)
				return true;
			if (args.get(0) instanceof Number && args.get(1) instanceof Number) {
				return (((Number) args.get(0)).doubleValue() != ((Number) args
						.get(1)).doubleValue());
			}
			return !args.get(0).equals(args.get(1));
		}
	}

	static public class Lower extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 2;
		}

		public String getName() {
			return "<";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return args.get(0).doubleValue() < args.get(1).doubleValue() ? 1
					: 0;
		}
	}

	static public class LowerEqual extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 2;
		}

		public String getName() {
			return "<=";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return args.get(0).doubleValue() <= args.get(1).doubleValue() ? 1
					: 0;
		}
	}

	static public class Greater extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 2;
		}

		public String getName() {
			return ">";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return args.get(0).doubleValue() > args.get(1).doubleValue() ? 1
					: 0;
		}
	}

	static public class GreaterEqual extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 2;
		}

		public String getName() {
			return ">=";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return args.get(0).doubleValue() >= args.get(1).doubleValue() ? 1
					: 0;
		}
	}

	// Functions
	static public class Sine extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "sin";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return Math.sin(args.get(0).doubleValue());
		}
	}

	static public class Cosine extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "cos";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return Math.cos(args.get(0).doubleValue());
		}
	}

	static public class Tangent extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "tan";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return Math.tan(args.get(0).doubleValue());
		}
	}

	static public class Arcsine extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "asin";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return Math.asin(args.get(0).doubleValue());
		}
	}

	static public class Arccosine extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "acos";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return Math.acos(args.get(0).doubleValue());
		}
	}

	static public class Arctangent extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "atan";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return Math.atan(args.get(0).doubleValue());
		}
	}

	static public class SineH extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "sinh";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return Math.sinh(args.get(0).doubleValue());
		}
	}

	static public class CosineH extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "cosh";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return Math.cosh(args.get(0).doubleValue());
		}
	}

	static public class TangentH extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "tanh";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return Math.tanh(args.get(0).doubleValue());
		}
	}

	static public class Exponential extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "exp";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return Math.exp(args.get(0).doubleValue());
		}
	}

	static public class Logarithm extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "log";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return Math.log(args.get(0).doubleValue());
		}
	}

	static public class DecimalLogarithm extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "log10";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return Math.log10(args.get(0).doubleValue());
		}
	}

	static public class SquareRoot extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "sqrt";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return Math.sqrt(args.get(0).doubleValue());
		}
	}

	static public class CubicRoot extends AbstractNumericFunction {
		public int getMaxNbArg() {
			return 1;
		}

		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "cbrt";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return Math.cbrt(args.get(0).doubleValue());
		}
	}

	static public class RandomNumber extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 0;
		}

		public String getName() {
			return "rnd";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return Math.random();
		}

		public boolean isStatic() {
			return false;
		}
	}

	static public class Abs extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "abs";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return convert(Math.abs(args.get(0).doubleValue()));
		}
	}

	static public class Ceil extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "ceil";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return (long) (Math.ceil(args.get(0).doubleValue()));
		}
	}

	static public class Floor extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "floor";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return (long) (Math.floor(args.get(0).doubleValue()));
		}
	}

	static public class Round extends AbstractNumericFunction {
		public int getMinNbArg() {
			return 1;
		}

		public String getName() {
			return "round";
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return (long) (Math.round(args.get(0).doubleValue()));
		}
	}

	static public class Maximum extends AbstractArithmeticOperator {
		public String getName() {
			return "max";
		}

		public double getValue(double v1, double v2) {
			return Math.max(v1, v2);
		}

		public double getValue(long v1, double v2) {
			return Math.max(v1, v2);
		}

		public double getValue(double v1, long v2) {
			return Math.max(v1, v2);
		}

		public long getValue(long v1, long v2) {
			return Math.max(v1, v2);
		}
	}

	static public class Minimum extends AbstractArithmeticOperator {
		public String getName() {
			return "min";
		}

		public double getValue(double v1, double v2) {
			return Math.min(v1, v2);
		}

		public double getValue(long v1, double v2) {
			return Math.min(v1, v2);
		}

		public double getValue(double v1, long v2) {
			return Math.min(v1, v2);
		}

		public long getValue(long v1, long v2) {
			return Math.min(v1, v2);
		}
	}

	static public class Sign extends AbstractNumericFunction {
		public String getName() {
			return "sign";
		}

		public int getMaxNbArg() {
			return 1;
		}

		public int getMinNbArg() {
			return 1;
		}

		public Number getNumberValue(EntityInterface e, List<Number> args) {
			return (long) (Math.signum(args.get(0).doubleValue()));
		}
	}

	// And now the static function that register all the Functions
	static public void registerAll() {
		FunctionList.register(new Addition(), "Arithmetic addition");
		FunctionList.register(new Substraction(), "Arithmetic substraction");
		FunctionList
				.register(new Multiplication(), "Arithmetic multiplication");
		FunctionList.register(new Division(), "Arithmetic division");
		FunctionList.register(new Modulo(), "Euclidian division remainder");
		FunctionList.register(new Exponent(), "Arithmetic exponentiation");
		FunctionList.register(new BitwiseOr(), "Bitwise or");
		FunctionList.register(new BitwiseAnd(), "Bitwise and");
		FunctionList.register(new BitwiseXor(), "Bitwise exclusive-or");
		FunctionList.register(new BitwiseNot(), "Bitwise not");

		// Tests
		FunctionList.register(new Equal(), "Equality test");
		FunctionList.register(new Different(), "Inequality test");
		FunctionList.register(new Lower(), "Lower than test");
		FunctionList.register(new LowerEqual(), "Lower or equalt test");
		FunctionList.register(new Greater(), "Greater than test");
		FunctionList.register(new GreaterEqual(), "Greater or equal test");

		// Functions
		FunctionList.register(new Sine(), "Trigonometric sine function");
		FunctionList.register(new Cosine(), "Trigonometric cosine function");
		FunctionList.register(new Tangent(), "Trigonometric tangent function");
		FunctionList.register(new Arcsine(), "Trigonometric arcsine function");
		FunctionList.register(new Arccosine(),
				"Trigonometric arccosine function");
		FunctionList.register(new Arctangent(),
				"Trigonometric arctangent function");
		FunctionList.register(new SineH(), "Hyperbolic sine function");
		FunctionList.register(new CosineH(), "Hyperbolic cossine function");
		FunctionList.register(new TangentH(), "Hyperbolic tangent function");
		FunctionList.register(new Exponential(), "Exponential function");
		FunctionList.register(new Logarithm(), "Logarithm function");
		FunctionList.register(new DecimalLogarithm(),
				"Decimal logarithm function");
		FunctionList.register(new SquareRoot(), "Square root");
		FunctionList.register(new CubicRoot(), "Cubic root");
		FunctionList
				.register(new RandomNumber(),
						"Returns a random number between 0 (included) and 1 (excluded)");
		FunctionList.register(new Abs(),
				"Returns the absolute value of the parameter");
		FunctionList
				.register(
						new Ceil(),
						"Returns the smallest (closest to negative infinity) integer value that is greater than or equal to the argument");
		FunctionList
				.register(
						new Floor(),
						"Returns the greatest (closest to positive infinity) integer value that is lower than or equal to the argument");
		FunctionList.register(new Round(),
				"Returns the argument rounded to the nearest integer value");
		FunctionList.register(new Maximum(),
				"Returns the greatest number of the two arguments");
		FunctionList.register(new Minimum(),
				"Returns the lowest number of the two arguments");
		FunctionList
				.register(
						new Sign(),
						"Returns -1 if the argument is negative, 0 if the argument is 0, 1 if the argument is positive");
	}
}
