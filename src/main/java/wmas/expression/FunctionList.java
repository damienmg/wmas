package wmas.expression;

import wmas.expression.functions.Function;

public interface FunctionList {
	public Function getInfix(String s); // functions of the form A = B

	public Function getPrefix(String s); // functions of the form !A

	public Function getFunction(String s); // functions of the form f(A, B)
}
