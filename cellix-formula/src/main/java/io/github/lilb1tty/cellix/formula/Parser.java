package io.github.lilb1tty.cellix.formula;

import io.github.lilb1tty.cellix.core.model.CellRef;
import io.github.lilb1tty.cellix.core.model.CellRange;
import io.github.lilb1tty.cellix.core.model.ErrorType;
import io.github.lilb1tty.cellix.formula.ast.*;
import io.github.lilb1tty.cellix.formula.exception.FormulaParseException;
import io.github.lilb1tty.cellix.formula.token.Token;
import io.github.lilb1tty.cellix.formula.token.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private List<Token> tokens;
    private int pos;

    public FormulaNode parse(String formula) {
        this.tokens = new Tokenizer().tokenize(formula);
        this.pos = 0;
        FormulaNode result = parseExpression();
        if (current().type() != TokenType.EOF) {
            throw new FormulaParseException("Unexpected token: " + current().text());
        }
        return result;
    }

    private FormulaNode parseExpression() {
        return parseComparison();
    }

    private FormulaNode parseComparison() {
        FormulaNode left = parseConcatenation();
        while (match(TokenType.EQ, TokenType.NE, TokenType.LT, TokenType.GT, TokenType.LE, TokenType.GE)) {
            Operator op = operatorFromToken(previous());
            FormulaNode right = parseConcatenation();
            left = new BinaryOp(op, left, right);
        }
        return left;
    }

    private FormulaNode parseConcatenation() {
        FormulaNode left = parseAddition();
        while (match(TokenType.CONCAT)) {
            FormulaNode right = parseAddition();
            left = new BinaryOp(Operator.CONCAT, left, right);
        }
        return left;
    }

    private FormulaNode parseAddition() {
        FormulaNode left = parseMultiplication();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Operator op = previous().type() == TokenType.PLUS ? Operator.ADD : Operator.SUBTRACT;
            FormulaNode right = parseMultiplication();
            left = new BinaryOp(op, left, right);
        }
        return left;
    }

    private FormulaNode parseMultiplication() {
        FormulaNode left = parseExponentiation();
        while (match(TokenType.MULTIPLY, TokenType.DIVIDE)) {
            Operator op = previous().type() == TokenType.MULTIPLY ? Operator.MULTIPLY : Operator.DIVIDE;
            FormulaNode right = parseExponentiation();
            left = new BinaryOp(op, left, right);
        }
        return left;
    }

    private FormulaNode parseExponentiation() {
        FormulaNode left = parseUnary();
        if (match(TokenType.POWER)) {
            FormulaNode right = parseExponentiation();
            left = new BinaryOp(Operator.POWER, left, right);
        }
        return left;
    }

    private FormulaNode parseUnary() {
        if (match(TokenType.MINUS)) {
            return new UnaryOp(UnaryOperator.NEGATE, parseUnary());
        }
        if (match(TokenType.PLUS)) {
            return parseUnary();
        }
        return parsePostfix();
    }

    private FormulaNode parsePostfix() {
        FormulaNode node = parsePrimary();
        if (match(TokenType.PERCENT)) {
            node = new UnaryOp(UnaryOperator.PERCENT, node);
        }
        return node;
    }

    private FormulaNode parsePrimary() {
        Token tok = current();
        switch (tok.type()) {
            case NUMBER -> {
                advance();
                return new NumberLiteral(Double.parseDouble(tok.text()));
            }
            case TEXT -> {
                advance();
                return new TextLiteral(tok.text());
            }
            case BOOL -> {
                advance();
                return new BoolLiteral("TRUE".equalsIgnoreCase(tok.text()));
            }
            case ERROR -> {
                advance();
                return new ErrorLiteral(ErrorType.fromExcelString(tok.text()));
            }
            case CELL_REF -> {
                advance();
                return parseCellRefOrRange(tok);
            }
            case NAME -> {
                advance();
                if (match(TokenType.LPAREN)) {
                    return parseFunctionCall(tok.text());
                }
                return new NameRef(tok.text());
            }
            case LPAREN -> {
                advance();
                FormulaNode node = parseExpression();
                consume(TokenType.RPAREN, "Expected ')'");
                return node;
            }
            case LBRACE -> {
                advance();
                List<List<FormulaNode>> rows = new ArrayList<>();
                List<FormulaNode> currentRow = new ArrayList<>();
                if (current().type() != TokenType.RBRACE) {
                    currentRow.add(parseExpression());
                    while (current().type() != TokenType.RBRACE) {
                        if (match(TokenType.COMMA)) {
                            currentRow.add(parseExpression());
                        } else if (match(TokenType.SEMICOLON)) {
                            rows.add(currentRow);
                            currentRow = new ArrayList<>();
                            if (current().type() != TokenType.RBRACE) {
                                currentRow.add(parseExpression());
                            }
                        } else {
                            throw new FormulaParseException("Expected ',', ';', or '}' in array literal");
                        }
                    }
                }
                if (!currentRow.isEmpty()) rows.add(currentRow);
                consume(TokenType.RBRACE, "Expected '}'");
                return new ArrayLiteral(rows);
            }
            default -> throw new FormulaParseException("Unexpected token: " + tok.text());
        }
    }

    private FormulaNode parseCellRefOrRange(Token firstRef) {
        CellRefNode first = parseCellRef(firstRef.text());
        if (match(TokenType.COLON)) {
            Token secondRef = consume(TokenType.CELL_REF, "Expected cell ref after ':'");
            CellRefNode second = parseCellRef(secondRef.text());
            return new CellRangeNode(new CellRange(first.ref(), second.ref()));
        }
        return first;
    }

    private CellRefNode parseCellRef(String text) {
        boolean colAbs = text.startsWith("$");
        String s = text.replace("$", "");
        int i = 0;
        while (i < s.length() && Character.isLetter(s.charAt(i))) i++;
        String colPart = s.substring(0, i);
        String rowPart = s.substring(i);
        int row = Integer.parseInt(rowPart);
        int col = 0;
        for (char c : colPart.toCharArray()) {
            col = col * 26 + (c - 'A' + 1);
        }
        boolean rowAbs = text.contains("$") && text.lastIndexOf("$") > 0;
        return new CellRefNode(new CellRef(row, col), rowAbs, colAbs);
    }

    private FormulaNode parseFunctionCall(String name) {
        List<FormulaNode> args = new ArrayList<>();
        if (current().type() != TokenType.RPAREN) {
            do {
                args.add(parseExpression());
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RPAREN, "Expected ')' after function arguments");
        return new FunctionCall(name.toUpperCase(), args);
    }

    private Operator operatorFromToken(Token tok) {
        return switch (tok.type()) {
            case EQ -> Operator.EQ;
            case NE -> Operator.NE;
            case LT -> Operator.LT;
            case GT -> Operator.GT;
            case LE -> Operator.LE;
            case GE -> Operator.GE;
            default -> throw new IllegalStateException();
        };
    }

    private Token current() {
        return tokens.get(pos);
    }

    private Token previous() {
        return tokens.get(pos - 1);
    }

    private boolean match(TokenType... types) {
        for (TokenType t : types) {
            if (current().type() == t) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token advance() {
        if (current().type() != TokenType.EOF) pos++;
        return previous();
    }

    private Token consume(TokenType type, String message) {
        if (current().type() == type) return advance();
        throw new FormulaParseException(message + " but got " + current().text());
    }
}
