package wmas.expression;

import java.util.LinkedList;
import java.util.List;

public class ExpressionToken {
	static final int TOKEN_IDENT = 0;
	static final int TOKEN_LEFT_PAR = 1;
	static final int TOKEN_RIGHT_PAR = 2;
	static final int TOKEN_COMMA = 3;
	static final int TOKEN_NUMBER = 4;
	static final int TOKEN_SYMBOL = 5;
	static final int TOKEN_STRING = 6;
	static final int TOKEN_LEFT_BRACKET = 7;
	static final int TOKEN_RIGHT_BRACKET = 8;

	int type = -1;
	String value;
	int characterNb = 0;

	static String escapeString(String str) {
		String r = "";
		for (char c : str.toCharArray()) {
			switch (c) {
			case '\\':
				r += "\\\\";
				break;
			case '\n':
				r += "\\n";
				break;
			case '\r':
				r += "\\r";
				break;
			case '\t':
				r += "\\t";
				break;
			case '\b':
				r += "\\b";
				break;
			case '\f':
				r += "\\f";
				break;
			case '"':
				r += "\"";
				break;
			case '\'':
				r += "\'";
				break;
			default:
				r += c;
				break;
			}
		}
		return r;
	}

	static boolean isSymbolChar(char c) {
		return !Character.isWhitespace(c) && !Character.isDigit(c)
				&& !Character.isLetter(c) && c != '(' && c != ')' && c != '['
				&& c != ']' && c != '.' && c != '_' && c != ',' && c != '"'
				&& c != '\\';
	}

	static boolean isStartIdentifier(char c) {
		return Character.isLetter(c) || c == '_';
	}

	static boolean isIdentifier(char c) {
		return isStartIdentifier(c) || Character.isDigit(c);
	}

	static int isNumber(int numberType, char c) {
		if (Character.isDigit(c)) {
			return numberType == 0 ? 1 : numberType;
		}
		if (c == '.')
			return numberType == 1 ? 2 : 0;
		if (c == 'e' || c == 'E')
			return numberType == 1 || numberType == 2 ? 3 : 0;
		return 0;
	}

	private String eatToken(int cNumber, String toParse)
			throws ExpressionParseException {
		characterNb = cNumber;
		toParse = toParse.trim();
		int numberType = 0;
		if (toParse.isEmpty())
			return "";
		char c = toParse.charAt(0);
		if (c == '(') {
			type = TOKEN_LEFT_PAR;
			return toParse.substring(1);
		} else if (c == ')') {
			type = TOKEN_RIGHT_PAR;
			return toParse.substring(1);
		} else if (c == '[') {
			type = TOKEN_LEFT_BRACKET;
			return toParse.substring(1);
		} else if (c == ']') {
			type = TOKEN_RIGHT_BRACKET;
			return toParse.substring(1);
		} else if (c == ',') {
			type = TOKEN_COMMA;
			return toParse.substring(1);
		} else if (isStartIdentifier(c)) {
			type = TOKEN_IDENT;
			return eatIdent(toParse);
		} else if (isSymbolChar(c)) {
			type = TOKEN_SYMBOL;
			return eatSymbol(toParse);
		} else if (c == '"') {
			type = TOKEN_STRING;
			return eatString(toParse.substring(1));
		} else {
			numberType = isNumber(numberType, c);
			if (numberType > 0) {
				type = TOKEN_NUMBER;
				return eatNumber(toParse);
			} else
				throw new ExpressionParseException("Invalid character '" + c
						+ "' at position " + characterNb + "!");
		}
	}

	private String eatString(String toParse) throws ExpressionParseException {
		int i = 0;
		boolean escaped = false;
		int escapeType = 0;
		String lastChar = "";
		String res = "";
		for (char c : toParse.toCharArray()) {
			if (c == '\\') {
				escaped = true;
			} else if (c == '"' && !escaped) {
				this.value = res;
				if (escapeType > 0)
					value += lastChar;
				i++;
				return toParse.substring(i);
			} else if (escaped) {
				switch (c) {
				case 'n':
					res += "\n";
					break;
				case 't':
					res += "\t";
					break;
				case 'r':
					res += "\r";
					break;
				case 'b':
					res += "\b";
					break;
				case '\\':
					res += "\\";
					break;
				case 'f':
					res += "\f";
					break;
				case '\'':
					res += "\'";
					break;
				case '"':
					res += "\"";
					break;
				case 'u':
					escapeType = 3;
					lastChar = "";
					break;
				case 'x':
					escapeType = 1;
					lastChar = "";
					break;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
					escapeType = 2;
					lastChar = Character.toString(c);
					break;
				default:
					res += c;
				}
				escaped = false;
			} else if (escapeType > 0) {
				try {
					switch (escapeType) {
					case 1: // Hexa
						if (Character.isDigit(c)
								|| (Character.toLowerCase(c) >= 'a' && Character
										.toLowerCase(c) <= 'f')) {
							lastChar += c;
						} else {
							if (lastChar.length() > 2 || lastChar.length() == 0) {
								throw new ExpressionParseException(
										"Invalid number format in string litteral at character "
												+ (characterNb + i));
							}
							char nc = (char) Integer.parseInt(lastChar, 16);
							res += nc;
							escapeType = 0;
						}
						break;
					case 2: // Octal
						if (Character.isDigit(c) && (c != '9') && (c != '8')) {
							lastChar += c;
						} else {
							if (lastChar.length() > 3 || lastChar.length() == 0) {
								throw new ExpressionParseException(
										"Invalid number format in string litteral at character "
												+ (characterNb + i));
							}
							char nc = (char) Integer.parseInt(lastChar, 8);
							res += nc;
							escapeType = 0;
						}
					case 3: // Unicode
						if (Character.isDigit(c)
								|| (Character.toLowerCase(c) >= 'a' && Character
										.toLowerCase(c) <= 'f')) {
							lastChar += c;
						} else {
							if (lastChar.length() > 4 || lastChar.length() == 0) {
								throw new ExpressionParseException(
										"Invalid number format in string litteral at character "
												+ (characterNb + i));
							}
							char nc = (char) Integer.parseInt(lastChar, 16);
							res += nc;
							escapeType = 0;
						}
						break;
					}
				} catch (NumberFormatException exn) {
					throw new ExpressionParseException(
							"Invalid number format in string litteral at character "
									+ (characterNb + i), exn);
				}
			} else {
				res += c;
			}
			i++;
		}
		throw new ExpressionParseException(
				"Unclosed string litteral started at position " + characterNb
						+ "!");
	}

	private String eatIdent(String toParse) {
		int i = 0;
		for (char c : toParse.toCharArray()) {
			if (isIdentifier(c)) {
				i++;
			} else {
				value = toParse.substring(0, i);
				return toParse.substring(i);
			}
		}
		value = toParse;
		return "";
	}

	private String eatSymbol(String toParse) {
		int i = 0;
		for (char c : toParse.toCharArray()) {
			if (isSymbolChar(c)) {
				i++;
			} else {
				value = toParse.substring(0, i);
				return toParse.substring(i);
			}
		}
		value = toParse;
		return "";
	}

	private String eatNumber(String toParse) {
		int i = 0;
		int numberType = 0;
		for (char c : toParse.toCharArray()) {
			numberType = isNumber(numberType, c);
			if (numberType > 0) {
				i++;
			} else {
				value = toParse.substring(0, i);
				return toParse.substring(i);
			}
		}
		value = toParse;
		return "";
	}

	static List<ExpressionToken> getTokenList(String toParse)
			throws ExpressionParseException {
		LinkedList<ExpressionToken> res = new LinkedList<ExpressionToken>();
		ExpressionToken t = null;
		String orig = toParse;
		while (!toParse.isEmpty()) {
			t = new ExpressionToken();
			toParse = t.eatToken(orig.length() - toParse.length(), toParse);
			if (t.type < 0) {
				return res;
			}
			res.add(t);
		}
		return res;
	}

	public String toString() {
		switch (type) {
		case TOKEN_COMMA:
			return ",";
		case TOKEN_IDENT:
			return "ident(" + value + ")";
		case TOKEN_LEFT_PAR:
			return "(";
		case TOKEN_NUMBER:
			return "number(" + value + ")";
		case TOKEN_RIGHT_PAR:
			return ")";
		case TOKEN_SYMBOL:
			return "symbol(" + value + ")";
		case TOKEN_STRING:
			return "string(\"" + escapeString(value) + "\")";
		}
		return "???";
	}
}
