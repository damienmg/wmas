package wmas.expression;

import java.util.LinkedList;
import java.util.List;

import wmas.expression.functions.AbstractNumericFunction;
import wmas.expression.functions.Function;
import wmas.world.Entity;
import wmas.world.EntityInterface;
import wmas.world.memory.Data;
import wmas.world.memory.NumberData;
import wmas.world.memory.StringData;

public class Expression {
	public static final Entity dummyEntity = new Entity();

	private FunctionList functionList = wmas.expression.functions.FunctionList
			.getInstance();

	private String unknownFunction = null;
	private Function f = null;
	private String var = null;
	private List<Expression> args = null;
	private List<Expression> deref = null;
	private List<Object> argsObject = null;
	private List<Object> derefObject = null;
	private Number number = null;
	private String str = null;
	private Object reducted = null;

	public Expression(Number n) {
		this.number = n;
	}

	public Expression(Number n, FunctionList funList) {
		this.number = n;
		this.functionList = funList;
	}

	public Expression(String toParse, FunctionList funList)
			throws ExpressionParseException {
		this.functionList = funList;
		parse(new ExpressionTokenTree(toParse));
		reduce();
	}

	public Expression(String toParse) throws ExpressionParseException {
		parse(new ExpressionTokenTree(toParse));
		reduce();
	}

	private Expression(ExpressionTokenTree toParse, FunctionList funList)
			throws ExpressionParseException {
		this.functionList = funList;
		parse(toParse);
		reduce();
	}

	public Expression(Expression e) {
		// Copy constructor;
		functionList = e.functionList;
		f = e.f;
		unknownFunction = e.unknownFunction;
		var = e.var;
		str = e.str;
		number = e.number;
		reducted = e.reducted;
		if (f != null || unknownFunction != null) {
			argsObject = new LinkedList<Object>();
			args = new LinkedList<Expression>();
			for (Expression ne : e.args) {
				args.add(new Expression(ne));
			}
		}
		if (e.deref != null) {
			deref = new LinkedList<Expression>();
			derefObject = new LinkedList<Object>();
			for (Expression ne : e.deref) {
				deref.add(new Expression(ne));
			}
		}
	}

	public boolean isLeftValue() {
		reduce();
		if (this.var != null)
			return true;
		if (this.f == null)
			return false;
		return this.f.isLeft();
	}

	private void getDerefObject(EntityInterface owner, Variables varSet) {
		if (derefObject == null) {
			derefObject = new LinkedList<Object>();
		}
		derefObject.clear();
		if (deref != null) {
			for (Expression e : deref) {
				derefObject.add(e.getObject(owner, varSet));
			}
		}
	}

	private void getDerefObject() {
		if (derefObject == null) {
			derefObject = new LinkedList<Object>();
		}
		derefObject.clear();
		if (deref != null) {
			for (Expression e : deref) {
				derefObject.add(e.getObject());
			}
		}
	}

	private Object getDerefValue(EntityInterface owner, Variables varSet,
			Object newValue) {
		return Variables.affectDeref(getUndeferencedObject(owner, varSet),
				newValue, derefObject);
	}

	public boolean affect(EntityInterface owner, Variables varSet, Object value) {
		reduce();
		if (this.var != null) {
			getDerefObject(owner, varSet);
			varSet.affect(var, value, derefObject);
			return true;
		} else if (this.f != null && this.f.isLeft()) {
			getDerefObject(owner, varSet);
			value = getDerefValue(owner, varSet, value);
			argsObject.clear();
			for (Expression e : args) {
				argsObject.add(e.getObject(owner, varSet));
			}
			this.f.affect(owner, argsObject, value);
		} else if (this.reducted instanceof DereferenceableObject
				&& deref != null) {
			getDerefObject(owner, varSet);
			this.reducted = getDerefValue(owner, varSet, value);
		}
		return false;
	}

	private void parse(ExpressionTokenTree toParse)
			throws ExpressionParseException {
		switch (toParse.rootToken.type) {
		case ExpressionToken.TOKEN_IDENT:
			this.deref = new LinkedList<Expression>();
			if (toParse.argTokens == null) {
				this.var = toParse.rootToken.value;
			} else {
				this.unknownFunction = toParse.rootToken.value;
				this.args = new LinkedList<Expression>();
				this.argsObject = new LinkedList<Object>();
				for (ExpressionTokenTree t : toParse.argTokens) {
					this.args.add(new Expression(t, functionList));
				}
				String ex = _reduceUnknownFunction();
				if (ex != null)
					throw new ExpressionParseException(ex + " at character "
							+ toParse.rootToken.characterNb);
			}
			for (ExpressionTokenTree t : toParse.derefTokens) {
				deref.add(new Expression(t, functionList));
			}
			break;
		case ExpressionToken.TOKEN_NUMBER:
			try {
				this.number = Long.parseLong(toParse.rootToken.value);
			} catch (NumberFormatException exn) {
				try {
					this.number = Double.parseDouble(toParse.rootToken.value);
				} catch (NumberFormatException ex) {
					throw new ExpressionParseException(
							"Invalid number at character "
									+ toParse.rootToken.characterNb);
				}
			}
			break;
		case ExpressionToken.TOKEN_STRING:
			this.str = toParse.rootToken.value;
			break;
		case ExpressionToken.TOKEN_SYMBOL:
			if (toParse.argTokens.size() == 1) {
				if (functionList.getPrefix(toParse.rootToken.value) == null)
					throw new ExpressionParseException(
							"Unknown prefix operator '"
									+ toParse.rootToken.value
									+ "' at character "
									+ toParse.rootToken.characterNb);
				this.f = functionList.getPrefix(toParse.rootToken.value);
				this.args = new LinkedList<Expression>();
				this.argsObject = new LinkedList<Object>();
				for (ExpressionTokenTree t : toParse.argTokens) {
					this.args.add(new Expression(t, functionList));
				}
			} else if (toParse.argTokens.size() == 2) {
				if (functionList.getInfix(toParse.rootToken.value) == null)
					throw new ExpressionParseException(
							"Unknown infix operator '"
									+ toParse.rootToken.value
									+ "' at character "
									+ toParse.rootToken.characterNb);
				this.f = functionList.getInfix(toParse.rootToken.value);
				this.args = new LinkedList<Expression>();
				this.argsObject = new LinkedList<Object>();
				for (ExpressionTokenTree t : toParse.argTokens) {
					this.args.add(new Expression(t, functionList));
				}
			} else
				throw new ExpressionParseException(
						"Internal error at character "
								+ toParse.rootToken.characterNb);
			break;
		default:
			throw new ExpressionParseException("Internal error at character "
					+ toParse.rootToken.characterNb);
		}
	}

	public Number getNumber(EntityInterface entity, Variables varSet) {
		return AbstractNumericFunction.getNumberFromArgument(getObject(entity,
				varSet));
	}

	public Number getNumber() {
		return AbstractNumericFunction.getNumberFromArgument(getObject());
	}

	public boolean isStatic() {
		reduce();
		return reducted != null || number != null || str != null;
	}

	private String _reduceUnknownFunction() {
		if (unknownFunction != null) {
			if (functionList.getFunction(unknownFunction) != null) {
				this.f = functionList.getFunction(unknownFunction);
				unknownFunction = null;
				int max = f.getMaxNbArg();
				int min = f.getMinNbArg();
				int s = args.size();
				if (s > max || s < min) {
					String ex = "Function '" + f.getName() + "' takes ";
					if (min != max)
						ex += "between " + min + " and " + max;
					else
						ex += min;
					ex += " argument";
					if (max > 1)
						ex += "s";
					return (ex + " but " + s + " provided");
				}
			}
		}
		return null;
	}

	private void reduceUnknownFunction() {
		String r = _reduceUnknownFunction();
		if (r != null)
			System.err.println(r);
	}

	private void reduce() {
		if (deref != null) {
			for (Expression e : deref)
				e.reduce();
		}
		if (number != null || var != null)
			return;
		reduceUnknownFunction();
		if (f != null && args != null) {
			boolean canReduce = f.isStatic();
			argsObject.clear();
			for (Expression e : args) {
				canReduce = canReduce && e.isStatic();
				if (canReduce) {
					argsObject.add(e.getObject());
				}
			}
			if (canReduce) {
				reducted = f.getValue(dummyEntity, argsObject);
				if (reducted == null)
					reducted = 0;
				f = null;
				args = null;
			}
		}
	}

	private String derefString() {
		String r = "";
		if (deref != null) {
			for (Expression e : deref) {
				r += "[" + e.toString() + "]";
			}
		}
		return r;
	}

	public String toString() {
		return corpusToString(this.functionList) + derefString();
	}

	public String corpusToString(FunctionList functionList) {
		if (number != null)
			return number.toString();
		else if (var != null)
			return var;
		else if (str != null)
			return "\"" + ExpressionToken.escapeString(str) + "\"";
		else if (reducted != null)
			return reducted.toString();
		else if ((f != null || unknownFunction != null) && args != null) {
			String s = unknownFunction != null ? unknownFunction : f.getName();
			if (functionList.getPrefix(s) != null && args.size() == 1) {
				String p = "???";
				if (args.size() > 0)
					p = args.get(0).toString();
				return s + " " + p;
			} else if (functionList.getInfix(s) != null && args.size() == 2) {
				String p1 = "???";
				String p2 = "???";
				if (args.size() > 0)
					p1 = args.get(0).toString();
				if (args.size() > 0)
					p2 = args.get(1).toString();
				return "(" + p1 + " " + s + " " + p2 + ")";
			} else {
				String r = "";
				for (Expression e : args) {
					r += e.toString() + ", ";
				}
				if (r.length() > 0)
					r = r.substring(0, r.length() - 2);
				return s + "(" + r + ")";
			}
		}
		return "???";
	}

	public Object getObject(EntityInterface entity, Variables varSet) {
		Object v = getUndeferencedObject(entity, varSet);
		if (deref != null) {
			getDerefObject(entity, varSet);
			return Variables.dereferences(v, derefObject);
		}
		return v;
	}

	Object getUndeferencedObject(EntityInterface entity, Variables varSet) {
		reduceUnknownFunction();
		if (var != null) {
			return varSet.getValue(var);
		} else if (str != null) {
			return str;
		} else if (number != null) {
			return number;
		} else if (reducted != null) {
			return reducted;
		} else if (f != null) {
			argsObject.clear();
			for (Expression e : args) {
				argsObject.add(e.getObject(entity, varSet));
			}
			Object r = f.getValue(entity, argsObject);
			return r == null ? 0 : r;
		}
		return 0;
	}

	public Object getObject() {
		Object v = getUndeferencedObject();
		if (deref != null) {
			getDerefObject();
			return Variables.dereferences(v, derefObject);
		}
		return v;
	}

	Object getUndeferencedObject() {
		reduceUnknownFunction();
		if (var != null) {
			return 0;
		} else if (str != null) {
			return str;
		} else if (number != null) {
			return number;
		} else if (reducted != null) {
			return reducted;
		} else if (f != null) {
			argsObject.clear();
			for (Expression e : args) {
				argsObject.add(e.getObject());
			}
			Object r = f.getValue(dummyEntity, argsObject);
			return r == null ? 0 : 1;
		}
		return 0;
	}

	public boolean verified(EntityInterface e, Variables varSet) {
		return getNumber(e, varSet).doubleValue() != 0;
	}

	public long getSize() {
		if (var != null || number != null) {
			return NumberData.NUMBER_SIZE;
		} else if (str != null) {
			return StringData.stringSize(str);
		} else if (reducted != null) {
			if (reducted instanceof Data) {
				return ((Data) reducted).getSize();
			}
			return NumberData.NUMBER_SIZE;
		} else if (f != null || unknownFunction != null) {
			long r = NumberData.NUMBER_SIZE;
			for (Expression e : args) {
				r += e.getSize();
			}
			return r;
		}
		return 0;
	}
}
