package wmas.expression.functions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import wmas.util.Doublet;

public class FunctionList implements wmas.expression.FunctionList {
	public static final String identRegex = "[_a-zA-Z][_a-zA-Z0-9]*";
	public static final String identOnlyRegex = "^" + identRegex + "$";
	public static final String notSymbolRegex = "[\\(\\)A-Za-z0-9\\.,_]";

	public static final String intRegex = "[+-]?[0-9]*";
	public static final String numberRegex = "[+-]?[0-9]+(\\.[0-9]+)?([eE][+-]?[0-9]+)?";
	public static final String intOnlyRegex = "^" + intRegex + "$";
	public static final String numberOnlyRegex = "^" + numberRegex + "$";

	private static Map<String, String> descriptions = new HashMap<String, String>(); // Description
																						// of
																						// the
																						// functions
	private static Map<String, Function> infixFunctions = new HashMap<String, Function>(); // functions
																							// of
																							// the
																							// form
																							// A
																							// =
																							// B
	private static Map<String, Function> prefixFunctions = new HashMap<String, Function>(); // functions
																							// of
																							// the
																							// form
																							// !A
	private static Map<String, Function> functions = new HashMap<String, Function>(); // functions
																						// of
																						// the
																						// form
																						// f(A,B)
	private static HashSet<String> sessionFunctions = new HashSet<String>();

	public static Set<String> listFunctions() {
		Set<String> f = functions.keySet();
		f.removeAll(sessionFunctions);
		return f;
	}

	public static Set<String> listInfix() {
		Set<String> f = infixFunctions.keySet();
		f.removeAll(sessionFunctions);
		return f;
	}

	public static Set<String> listPrefix() {
		Set<String> f = prefixFunctions.keySet();
		f.removeAll(sessionFunctions);
		return f;
	}

	public static String getDescription(String name) {
		return descriptions.get(name);
	}

	public static Doublet<Integer, Integer> getArgsNumber(String name) {
		if (functions.containsKey(name)) {
			Function f = functions.get(name);
			return new Doublet<Integer, Integer>(f.getMinNbArg(),
					f.getMaxNbArg());
		}
		return null;
	}

	public static boolean functionExists(String name) {
		return (functions.containsKey(name) || infixFunctions.containsKey(name) || prefixFunctions
				.containsKey(name));
	}

	public static void clearSession() {
		for (String s : sessionFunctions) {
			infixFunctions.remove(s);
			prefixFunctions.remove(s);
			functions.remove(s);
			descriptions.remove(s);
		}
		sessionFunctions.clear();
	}

	public static void register(Function f, String desc) {
		sessionFunctions.remove(_register(f, desc));
	}

	public static void registerSession(Function f, String desc) {
		sessionFunctions.add(_register(f, desc));
	}

	private static String _register(Function f, String desc) {
		String s = f.getName();
		if (s.matches("\\s") || s.length() == 0)
			throw new RuntimeException("Invalid name '" + s
					+ "' for a function (is empty or contains whitespaces)!");
		if (s.matches(identOnlyRegex)) {
			functions.put(s, f);
			descriptions.put(s, desc);
		} else if (s.matches(notSymbolRegex)) {
			throw new RuntimeException("Invalid name '" + s
					+ "' for a function!");
		} else {
			switch (f.getMinNbArg()) {
			case 1:
				prefixFunctions.put(s, f);
				descriptions.put(s, desc);
				break;
			case 2:
				infixFunctions.put(s, f);
				descriptions.put(s, desc);
				break;
			default:
				throw new RuntimeException(
						"Symbol functions must have 1 or 2 args. Function '"
								+ s + "' has " + f.getMinNbArg() + "!");
			}
			switch (f.getMaxNbArg()) {
			case 1:
				prefixFunctions.put(s, f);
				descriptions.put(s, desc);
				break;
			case 2:
				infixFunctions.put(s, f);
				descriptions.put(s, desc);
				break;
			default:
				throw new RuntimeException(
						"Symbol functions must have 1 or 2 args. Function '"
								+ s + "' has " + f.getMaxNbArg() + "!");
			}
		}
		return s;
	}

	private static void remove(Function f, Map<String, Function> list) {
		if (list.containsKey(f.getName()) && list.get(f.getName()) == f) {
			list.remove(f.getName());
			descriptions.remove(f.getName());
			sessionFunctions.remove(f.getName());
		}
	}

	public static void unregister(Function f) {
		remove(f, infixFunctions);
		remove(f, prefixFunctions);
		remove(f, functions);
	}

	// Pattern singleton
	private FunctionList() {
	}

	static private FunctionList instance = new FunctionList();

	public static FunctionList getInstance() {
		return instance;
	}

	@Override
	public Function getFunction(String s) {
		if (!functions.containsKey(s))
			return null;
		return functions.get(s);
	}

	@Override
	public Function getInfix(String s) {
		if (!infixFunctions.containsKey(s))
			return null;
		return infixFunctions.get(s);
	}

	@Override
	public Function getPrefix(String s) {
		if (!prefixFunctions.containsKey(s))
			return null;
		return prefixFunctions.get(s);
	}

}
