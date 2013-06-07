package wmas.world.functions;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import wmas.expression.Expression;
import wmas.expression.Variables;
import wmas.expression.functions.AbstractFunction;
import wmas.expression.functions.FunctionList;
import wmas.world.EntityInterface;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;

public class ExpressionFunction extends AbstractFunction implements XMLEntity {
	private static Variables varSet = new Variables();
	private Expression expr;
	private List<String> parameters;
	private String name;

	public static String listToString(List<String> l) {
		String r = "";
		if (l.isEmpty())
			return r;
		for (Object o : l) {
			r += o.toString() + ", ";
		}
		return r.substring(0, r.length() - 2);
	}

	public static void stringToList(String s, List<String> result)
			throws Exception {
		result.clear();
		s = s.trim();
		if (s.length() > 0) {
			for (String r : s.split(",")) {
				if (r.trim().isEmpty())
					throw new Exception("Invalid parameter list format!");
				result.add(r.trim());

			}
		}
	}

	public ExpressionFunction(String name, Expression expr,
			List<String> parameters) {
		this.expr = expr;
		this.parameters = parameters;
		this.name = name;
		FunctionList.registerSession(this, "World attribute function");
	}

	public boolean isStatic() {
		return false;
	}

	@Override
	public int getMinNbArg() {
		return parameters.size();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getValue(EntityInterface e, List<Object> args) {
		varSet.clear();
		int index = 0;
		for (String s : parameters)
			varSet.affect(s, args.get(index++), null);
		return expr.getObject(e, varSet);
	}

	public String toString() {
		return name + "(" + parameters + ") = " + expr.toString();
	}

	@Override
	public ExpressionFunction copy() {
		return new ExpressionFunction(name, new Expression(expr),
				new LinkedList<String>(parameters));
	}

	public void unregister() {
		FunctionList.unregister(this);
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		name = e.getAttribute("name");
		expr = new Expression(e.getAttribute("expr"));
		parameters = new LinkedList<String>();
		stringToList(e.getAttribute("parameters"), parameters);
		FunctionList.registerSession(this, "World attribute function");
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		e.setAttribute("name", name);
		e.setAttribute("parameters", listToString(parameters));
		e.setAttribute("expr", expr.toString());
		return e;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public Expression getExpression() {
		return expr;
	}

	public void setName(String name) {
		FunctionList.unregister(this);
		this.name = name;
		FunctionList.registerSession(this, "World attribute function");
	}

	public void setParameters(List<String> parameters) {
		FunctionList.unregister(this);
		this.parameters = parameters;
		FunctionList.registerSession(this, "World attribute function");
	}

	public void setExpression(Expression expr) {
		this.expr = expr;
	}

	public void setFunction(String name, String parameters) throws Exception {
		FunctionList.unregister(this);
		this.name = name;
		this.parameters = new LinkedList<String>();
		stringToList(parameters, this.parameters);
		FunctionList.registerSession(this, "World attribute function");
	}
}
