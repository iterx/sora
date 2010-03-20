package org.iterx.sora.tool.meta.util.trace;

import java.io.OutputStream;
import java.io.PrintStream;

public abstract class AbstractTracer<T extends AbstractTracer<T>> {

    private final PrintStream printStream;
    private boolean newline;

    protected AbstractTracer(final OutputStream outputStream) {
        this.printStream = (outputStream instanceof PrintStream)? (PrintStream) outputStream : new PrintStream(outputStream);
        this.newline = true;
    }

    protected OutputStream getOutputStream() {
        return printStream;
    }

    protected abstract int getIndent();

    @SuppressWarnings("unchecked")
    protected T print(final String... strings) {
        if(strings.length > 0) {
            if(newline) {
                for(int i = getIndent(); i-- != 0;) printStream.print(" ");
                newline = false;
            }
            printStream.print(strings[0]);
            for(int i = 1, length = strings.length; i < length; i++) {
                printStream.print(" ");
                printStream.print(strings[i]);
            }
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    protected T newline() {
        print("\n");
        newline = true;
        return (T) this;
    }

}
