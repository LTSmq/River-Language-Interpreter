public class Log {
    enum Source {
        SCANNER,
        PARSER,
        EXECUTOR,
    }

    static void standardOut(Source source, String message) {
        System.out.println(source + ":\t" + message);
    }

    public static void progress(Source source, String message) {
        standardOut(source, message);
    }

    public static void error(Source source, String message) {
        standardOut(source, message);
    }

    public static void operation(Source source, String message) {
        standardOut(source, message);
    }
}