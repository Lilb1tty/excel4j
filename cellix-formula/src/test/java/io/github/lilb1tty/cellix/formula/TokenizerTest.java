package io.github.lilb1tty.cellix.formula;

import io.github.lilb1tty.cellix.formula.token.Token;
import io.github.lilb1tty.cellix.formula.token.TokenType;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class TokenizerTest {

    @Test
    void tokenizeSimpleSum() {
        List<Token> tokens = new Tokenizer().tokenize("SUM(A1,B2)");
        assertThat(tokens).extracting(Token::type)
            .containsExactly(TokenType.NAME, TokenType.LPAREN,
                TokenType.CELL_REF, TokenType.COMMA,
                TokenType.CELL_REF, TokenType.RPAREN, TokenType.EOF);
    }

    @Test
    void tokenizeArithmetic() {
        List<Token> tokens = new Tokenizer().tokenize("1+2*3");
        assertThat(tokens).extracting(Token::type)
            .containsExactly(TokenType.NUMBER, TokenType.PLUS,
                TokenType.NUMBER, TokenType.MULTIPLY, TokenType.NUMBER, TokenType.EOF);
    }

    @Test
    void tokenizeCellRefWithAbsoluteness() {
        List<Token> tokens = new Tokenizer().tokenize("$A$1");
        assertThat(tokens.get(0).text()).isEqualTo("$A$1");
        assertThat(tokens.get(0).type()).isEqualTo(TokenType.CELL_REF);
    }

    @Test
    void tokenizeStringLiteral() {
        List<Token> tokens = new Tokenizer().tokenize("\"hello\"");
        assertThat(tokens.get(0).text()).isEqualTo("hello");
        assertThat(tokens.get(0).type()).isEqualTo(TokenType.TEXT);
    }

    @Test
    void tokenizeBooleanAndError() {
        List<Token> t1 = new Tokenizer().tokenize("TRUE");
        assertThat(t1.get(0).type()).isEqualTo(TokenType.BOOL);
        List<Token> t2 = new Tokenizer().tokenize("#DIV/0!");
        assertThat(t2.get(0).type()).isEqualTo(TokenType.ERROR);
    }
}
