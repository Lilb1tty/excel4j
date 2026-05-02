package io.excel4j.formula;

import io.excel4j.formula.token.Token;
import io.excel4j.formula.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Tokenizer {

    private static final Set<String> BOOLEAN_LITERALS = Set.of("TRUE", "FALSE");

    public List<Token> tokenize(String formula) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < formula.length()) {
            char c = formula.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (c == '"') {
                i = readString(formula, i, tokens);
            } else if (Character.isDigit(c) || c == '.') {
                i = readNumber(formula, i, tokens);
            } else if (c == '#') {
                i = readError(formula, i, tokens);
            } else if (Character.isLetter(c) || c == '$') {
                i = readNameOrRef(formula, i, tokens);
            } else if (c == '<' || c == '>') {
                i = readComparison(formula, i, tokens);
            } else {
                i = readOperator(formula, i, tokens);
            }
        }
        tokens.add(new Token(TokenType.EOF, "", formula.length()));
        return tokens;
    }

    private int readString(String formula, int start, List<Token> tokens) {
        int i = start + 1;
        StringBuilder sb = new StringBuilder();
        while (i < formula.length()) {
            char c = formula.charAt(i);
            if (c == '"') {
                if (i + 1 < formula.length() && formula.charAt(i + 1) == '"') {
                    sb.append('"');
                    i += 2;
                } else {
                    tokens.add(new Token(TokenType.TEXT, sb.toString(), start));
                    return i + 1;
                }
            } else {
                sb.append(c);
                i++;
            }
        }
        throw new IllegalArgumentException("Unterminated string at " + start);
    }

    private int readNumber(String formula, int start, List<Token> tokens) {
        int i = start;
        while (i < formula.length() && (Character.isDigit(formula.charAt(i)) || formula.charAt(i) == '.')) {
            i++;
        }
        if (i < formula.length() && (formula.charAt(i) == 'e' || formula.charAt(i) == 'E')) {
            i++;
            if (i < formula.length() && (formula.charAt(i) == '+' || formula.charAt(i) == '-')) {
                i++;
            }
            while (i < formula.length() && Character.isDigit(formula.charAt(i))) {
                i++;
            }
        }
        tokens.add(new Token(TokenType.NUMBER, formula.substring(start, i), start));
        return i;
    }

    private int readError(String formula, int start, List<Token> tokens) {
        int i = start + 1;
        while (i < formula.length() && formula.charAt(i) != '!') {
            i++;
        }
        if (i >= formula.length()) {
            throw new IllegalArgumentException("Invalid error literal at " + start);
        }
        tokens.add(new Token(TokenType.ERROR, formula.substring(start, i + 1), start));
        return i + 1;
    }

    private int readNameOrRef(String formula, int start, List<Token> tokens) {
        int i = start;
        while (i < formula.length()) {
            char c = formula.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '$' || c == '_' || c == '.') {
                i++;
            } else {
                break;
            }
        }
        String text = formula.substring(start, i);
        if (isCellRef(text)) {
            tokens.add(new Token(TokenType.CELL_REF, text, start));
        } else if (BOOLEAN_LITERALS.contains(text.toUpperCase())) {
            tokens.add(new Token(TokenType.BOOL, text.toUpperCase(), start));
        } else {
            tokens.add(new Token(TokenType.NAME, text, start));
        }
        return i;
    }

    private boolean isCellRef(String text) {
        int dollarCount = 0;
        int letterCount = 0;
        int digitCount = 0;
        for (char c : text.toCharArray()) {
            if (c == '$') {
                dollarCount++;
            } else if (Character.isLetter(c)) {
                letterCount++;
            } else if (Character.isDigit(c)) {
                digitCount++;
            } else {
                return false;
            }
        }
        if (dollarCount > 2 || letterCount == 0 || digitCount == 0) {
            return false;
        }
        // letters must come before digits (allowing for $ prefixes)
        String stripped = text.replace("$", "");
        int digitStart = 0;
        while (digitStart < stripped.length() && Character.isLetter(stripped.charAt(digitStart))) {
            digitStart++;
        }
        return digitStart > 0 && digitStart < stripped.length();
    }

    private int readComparison(String formula, int start, List<Token> tokens) {
        char c1 = formula.charAt(start);
        char c2 = (start + 1 < formula.length()) ? formula.charAt(start + 1) : '\0';
        if (c1 == '<' && c2 == '=') {
            tokens.add(new Token(TokenType.LE, "<=", start));
            return start + 2;
        } else if (c1 == '>' && c2 == '=') {
            tokens.add(new Token(TokenType.GE, ">=", start));
            return start + 2;
        } else if (c1 == '<' && c2 == '>') {
            tokens.add(new Token(TokenType.NE, "<>", start));
            return start + 2;
        } else if (c1 == '<') {
            tokens.add(new Token(TokenType.LT, "<", start));
            return start + 1;
        } else {
            tokens.add(new Token(TokenType.GT, ">", start));
            return start + 1;
        }
    }

    private int readOperator(String formula, int start, List<Token> tokens) {
        char c = formula.charAt(start);
        TokenType type = switch (c) {
            case '+' -> TokenType.PLUS;
            case '-' -> TokenType.MINUS;
            case '*' -> TokenType.MULTIPLY;
            case '/' -> TokenType.DIVIDE;
            case '^' -> TokenType.POWER;
            case '&' -> TokenType.CONCAT;
            case '=' -> TokenType.EQ;
            case '%' -> TokenType.PERCENT;
            case '(' -> TokenType.LPAREN;
            case ')' -> TokenType.RPAREN;
            case ',' -> TokenType.COMMA;
            case ':' -> TokenType.COLON;
            case '{' -> TokenType.LBRACE;
            case '}' -> TokenType.RBRACE;
            case ';' -> TokenType.SEMICOLON;
            default -> throw new IllegalArgumentException("Unexpected char: " + c + " at " + start);
        };
        tokens.add(new Token(type, String.valueOf(c), start));
        return start + 1;
    }
}
