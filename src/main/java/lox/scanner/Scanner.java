package lox.scanner;

import lox.Lox;
import lox.domain.Token;
import lox.domain.TokenType;

import static lox.domain.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private static final Map<String, TokenType> keywords;
    private int currentTokenStartOffset = 0;
    private int currentCharOffset = 0;
    private int line = 1;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            currentTokenStartOffset = currentCharOffset;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return currentCharOffset >= source.length();
    }

    private void scanToken() {
        char currentChar = getCurrentChar();
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
                // Ignore whitespaces
                break;
            case '\"':
                addStringLiteralToken();
                break;
            case '\n':
                line++;
                break;
            default:
                if (charIsDigit(currentChar)) {
                    addNumericLiteralToken();
                    break;
                }

                if (charIsAlphaOrUnderscore(currentChar)) {
                    addIdentifierToken();
                    break;
                }

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
        String text;
        int lexemeEndOffset = currentCharOffset;
        if (type.equals(STRING)) {
            lexemeEndOffset++;
        }
        text = source.substring(currentTokenStartOffset, lexemeEndOffset);
        Token token = new Token(type, text, literal, line);
        tokens.add(token);
    }

    private boolean nextCharMatchesWith(char expected) {
        if (lookAheadToNextChar() == expected) {
            advanceCurrentCharOffset(); // If the next char is equal we expect, we consider that is a double char unique character (like != or ==) so we skip it
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
        char nextChar = lookAheadToNextChar();

        // This while iterate between the start of string (considering the first quote)
        // until the last char before the closing quote.
        while (nextChar != '\"' && !isAtEnd()) {
            if (nextChar == '\n') {
                line++;
            }

            advanceCurrentCharOffset();
            nextChar = lookAheadToNextChar();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
        }

        // Advance through the closing quote
        advanceCurrentCharOffset();

        // Note: `currentTokenStartOffset + 1` is to ignore the initial quote from the string and trim the string literal correctly
        // This is a fucking flaw in my design of the lexer
        String literalString = source.substring(currentTokenStartOffset + 1, currentCharOffset);
        addToken(STRING, literalString);
    }

    private void addIdentifierToken() {
        while (charIsAlphanumericOrUnderscore(getCurrentChar())) {
            advanceCurrentCharOffset();
        }

        String identifierLiteral = source.substring(currentTokenStartOffset, currentCharOffset);
        TokenType tokenType = keywords.get(identifierLiteral);
        if (tokenType == null) {
            tokenType = IDENTIFIER;
        }
        addToken(tokenType, identifierLiteral);
    }

    private void addNumericLiteralToken() {
        // Consume the integer part
        while (charIsDigit(getCurrentChar())) {
            advanceCurrentCharOffset();
        }

        // Validate if contains a fractional part
        if (getCurrentChar() == '.' && charIsDigit(lookAheadToNextChar())) {
            advanceCurrentCharOffset(); // necessary to consume the '.'

            // Consume the fractional part
            while (charIsDigit(getCurrentChar())) {
                advanceCurrentCharOffset();
            }
        }

        Double literalNumber = Double.valueOf(source.substring(currentTokenStartOffset, currentCharOffset));
        addToken(NUMBER, literalNumber);
    }

    private boolean charIsDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    private boolean charIsAlphaOrUnderscore(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_');
    }

    private boolean charIsAlphanumericOrUnderscore(char c) {
        return charIsDigit(c) || charIsAlphaOrUnderscore(c);
    }
}
