package abnf;

import abnf.analysis.Analysis;
import abnf.analysis.ObjectClassWriter;
import abnf.lexer.Lexer;
import abnf.lexer.LexerException;
import abnf.node.Start;
import abnf.parser.Parser;
import abnf.parser.ParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.Writer;

//TODO: look into JAS???
public final class Compiler {

    private static final int BUFFER_SIZE = 1024;

    public static void main(final String[] arguments) {
        try {
            compile(new InputStreamReader(new FileInputStream(arguments[0])),
                    null);
        }
        catch(final Throwable throwable) {
            throwable.printStackTrace();
        }
    }


    private static void compile(final Reader reader, final Writer writer) throws LexerException, IOException, ParserException {
        final Lexer lexer = new Lexer(new PushbackReader(reader, BUFFER_SIZE));
        final Parser parser = new Parser(lexer);
        final Analysis analysis = new ObjectClassWriter();
        parser.parse().apply(analysis);
    }
}
