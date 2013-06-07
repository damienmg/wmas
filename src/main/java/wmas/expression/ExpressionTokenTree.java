package wmas.expression;

import java.util.LinkedList;
import java.util.List;

public class ExpressionTokenTree {
	ExpressionToken rootToken = null;
	List<ExpressionTokenTree> argTokens = null;
	List<ExpressionTokenTree> derefTokens = null;

	ExpressionTokenTree(String toParse) throws ExpressionParseException {
		List<ExpressionToken> toks = ExpressionToken.getTokenList(toParse);
		parse(toks);
		if (toks.size() > 0)
			throw new ExpressionParseException("Extra symbols at character "
					+ toks.get(0).characterNb);
	}

	private ExpressionTokenTree(List<ExpressionToken> toks)
			throws ExpressionParseException {
		parse(toks);
	}

	public ExpressionTokenTree(ExpressionTokenTree expressionTokenTree) {
		this.rootToken = expressionTokenTree.rootToken;
		this.argTokens = expressionTokenTree.argTokens;
		this.derefTokens = expressionTokenTree.derefTokens;
	}

	private void parse(List<ExpressionToken> toks)
			throws ExpressionParseException {
		if (toks.size() == 0)
			throw new ExpressionParseException("Empty expression");
		ExpressionToken t, t2;
		this.argTokens = null;
		switch (toks.get(0).type) {
		case ExpressionToken.TOKEN_SYMBOL: // unary operator
			this.rootToken = toks.remove(0);
			this.argTokens = new LinkedList<ExpressionTokenTree>();
			this.argTokens.add(new ExpressionTokenTree(toks));
			break;
		case ExpressionToken.TOKEN_IDENT:
			this.rootToken = toks.remove(0);
			derefTokens = new LinkedList<ExpressionTokenTree>();
			if (toks.size() > 0) {
				if (toks.get(0).type == ExpressionToken.TOKEN_LEFT_PAR) {
					// Parameter list
					toks.remove(0);
					parseParameterList(toks);
				}
				while (toks.size() > 0
						&& toks.get(0).type == ExpressionToken.TOKEN_LEFT_BRACKET) {
					// Dereferencing
					t = toks.remove(0);
					derefTokens.add(new ExpressionTokenTree(toks));
					if (toks.size() == 0) {
						throw new ExpressionParseException(
								"Unmatched '[' bracket at character "
										+ t.characterNb + " (end of input)");
					}
					t2 = toks.remove(0);
					if (t2.type != ExpressionToken.TOKEN_RIGHT_BRACKET) {
						throw new ExpressionParseException(
								"Unmatched '[' bracket at character "
										+ t.characterNb
										+ " error at character "
										+ t2.characterNb);
					}
				}
			}
			break;
		case ExpressionToken.TOKEN_LEFT_PAR:
			t = toks.remove(0);
			parse(toks);
			if (toks.size() == 0)
				throw new ExpressionParseException(
						"Unmatched '(' parenthesis at character "
								+ t.characterNb);
			t2 = toks.remove(0);
			if (t2.type != ExpressionToken.TOKEN_RIGHT_PAR)
				throw new ExpressionParseException(
						"Unmatched '(' parenthesis at character "
								+ t.characterNb + " error at character "
								+ t2.characterNb);
			if (rootToken == null)
				throw new ExpressionParseException("Empty '()' at character "
						+ t.characterNb);
			break;
		case ExpressionToken.TOKEN_RIGHT_PAR:
			return;
		case ExpressionToken.TOKEN_LEFT_BRACKET:
		case ExpressionToken.TOKEN_RIGHT_BRACKET:
			throw new ExpressionParseException(
					"Syntax error, bracket at invalid position at character "
							+ toks.get(0).characterNb);
		case ExpressionToken.TOKEN_COMMA:
			throw new ExpressionParseException(
					"Syntax error, comma at invalid position at character "
							+ toks.get(0).characterNb);
		case ExpressionToken.TOKEN_NUMBER:
		case ExpressionToken.TOKEN_STRING:
			this.rootToken = toks.remove(0);
			break;
		}
		while (toks.size() > 0
				&& toks.get(0).type == ExpressionToken.TOKEN_SYMBOL) {
			ExpressionTokenTree a1 = new ExpressionTokenTree(this);
			this.rootToken = toks.remove(0);
			this.argTokens = new LinkedList<ExpressionTokenTree>();
			this.argTokens.add(a1);
			if (toks.size() == 0)
				throw new ExpressionParseException(
						"Syntax error, no right value for operator '"
								+ rootToken.value + "' at character "
								+ rootToken.characterNb);
			this.argTokens.add(new ExpressionTokenTree(toks));
		}
	}

	private void parseParameterList(List<ExpressionToken> toks)
			throws ExpressionParseException {
		argTokens = new LinkedList<ExpressionTokenTree>();
		if (toks.size() > 0
				&& toks.get(0).type == ExpressionToken.TOKEN_RIGHT_PAR) {
			toks.remove(0);
			return;
		}
		while (toks.size() > 0) {
			argTokens.add(new ExpressionTokenTree(toks));
			if (toks.size() > 0
					&& toks.get(0).type == ExpressionToken.TOKEN_COMMA) {
				toks.remove(0);
			} else if (toks.size() > 0
					&& toks.get(0).type == ExpressionToken.TOKEN_RIGHT_PAR) {
				toks.remove(0);
				return;
			}
		}
		throw new ExpressionParseException("Unmatched '(' parenthesis");
	}

	public String toString() {
		if (argTokens != null) {
			return rootToken + "(" + argTokens + ")";
		} else {
			return rootToken.toString();
		}
	}
}
