package lox.scanner.exceptions;

public class LexicalAnalysisException extends RuntimeException {

    public LexicalAnalysisException(String errorMessage) {
        System.out.println(errorMessage);
    }
}
