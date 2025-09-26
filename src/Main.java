import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.List;

public class Main {
    static final int maxFileSize = 0x100000;
    public static void main(String[] args) {
        String filename = "example.riverlang";
        String content = "";
        try {
            FileReader reader = new FileReader(filename);
            char[] buffer = new char[maxFileSize];
            try {
                reader.read(buffer);
                int lastIndex = buffer.length - 1;
                System.out.println(buffer[lastIndex]);
                while (buffer[lastIndex] == '\0') {
                    lastIndex--;
                }
                content = new String(buffer, 0, lastIndex+1);
                reader.close();
            }
            catch (IOException inOutException) {
                System.out.println("Error reading file...");
            }
            
        }
        catch (FileNotFoundException notFound) {
            System.out.println("File not found...");
        }

        content = Preprocessor.process(content);
        Scanner scanner = new Scanner(true);
        List<Token> tokens = scanner.getTokens(content);
        List<Statement> statements = Parser.parse(tokens);
        new Executor().execute(statements);

        for (String variableName : Variable.registry.keySet()) {
            Measure measure = Variable.registry.get(variableName).get(0);
            System.out.println("Variable:\t" + variableName + ",\t->\t" + measure.magnitude + "\t->\t" + measure.meter + "\t->\t" + measure.second + "\t-->\t" + measure);
        }

    }
}
