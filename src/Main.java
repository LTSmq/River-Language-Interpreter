import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.List;

import nodes.tokens.*;
import processors.*;

public class Main {
    public static void main(String[] args) {
         String tokensPath = "lexicon/tokens.txt";
         String grammarPath = "lexicon/testgrammar.txt";
         String programPath = "examples/test1.txt";

         Scanner scanner = new Scanner(readFile(tokensPath));
         String source = readFile(programPath);

         List<Token> tokens = scanner.scan(source);
         Parser parser = new Parser(readFile(grammarPath));

    }

    public static String readFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        }

        catch (IOException e) {
            System.out.println("Bad read of file " + filePath + ":\t" + e);
            return "";
        }
    }

}