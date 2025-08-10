package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import lox.domain.Token;
import lox.scanner.Scanner;

public class Lox {

    private static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox <.lox file path>");
            System.exit(64);
        }

        String file = "src/main/resources/lox_samples/simple_var_declaring.lox";

        if (true) {
//            runFile(args[0]);
            runFile(file);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        String codeAsString = new String(bytes, Charset.defaultCharset());
        run(codeAsString);
        if (hadError) { // todo: need to improve the error handling to use java exceptions instead of a global boolean flag
            System.exit(65);
        }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader inputReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputReader);

        for (; ; ) {
            System.out.print("> ");
            String line = bufferedReader.readLine();
            if (line == null) {
                break;
            }

            run(line);
            hadError = false; // fix me
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();

        for (Token token : tokens) {
            System.out.println(token.toString());
        }
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where,
                               String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}