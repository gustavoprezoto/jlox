package lox.scanner;

import lox.Lox;
import lox.domain.Token;
import lox.domain.TokenType;

import static lox.domain.TokenType.*;

import java.util.ArrayList;
import java.util.List;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int currentCharOffset = 0;
    private int line = 1;

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = currentCharOffset; // hmmmm i dont thinks thats correct
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return currentCharOffset >= source.length();
    }

    private void scanToken() {
        char currentChar = source.charAt(currentCharOffset);
        switch (currentChar) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            case '!':
                addToken(nextCharMatchesWith('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(nextCharMatchesWith('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(nextCharMatchesWith('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(nextCharMatchesWith('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (nextCharMatchesWith('/')) {
                    advanceCurrentCharOffset(); // Needed to ignore the second / in the comment
                    while (lookAheadToNextChar() != '\n' && !isAtEnd()) { // We want to skip the last '\n', so it can go to it own (switch) case
                        advanceCurrentCharOffset();
                    }
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\"':
                addStringLiteralToken();
                break;
            case '\n':
                line++;
                break;
            default:
                String errorMsg = "Unexpected character \"" + currentChar + "\"";
                Lox.error(line, errorMsg);
        }

        advanceCurrentCharOffset();
    }

    private void advanceCurrentCharOffset() {
        currentCharOffset++;
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, currentCharOffset);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean nextCharMatchesWith(char expected) {
        int nextCharOffset = currentCharOffset + 1;

        // Checks if next char is not the EOF
        if (nextCharOffset >= source.length()) {
            return false;
        }

        if (source.charAt(nextCharOffset) == expected) {
            advanceCurrentCharOffset(); // Current should consider this char as one
            return true;
        }

        return false;
    }

    private char getCurrentChar() {
        if (isAtEnd()) {
            return '\0';
        }

        return source.charAt(currentCharOffset);
    }

    private char lookAheadToNextChar() {
        int nextCharOffset = currentCharOffset + 1;

        // Checks if next char is not the EOF
        if (nextCharOffset >= source.length()) {
            return '\0';
        }

        return source.charAt(currentCharOffset + 1);
    }

    private void addStringLiteralToken() {

    }
}
