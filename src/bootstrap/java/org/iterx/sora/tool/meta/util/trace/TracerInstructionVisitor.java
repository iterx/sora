package org.iterx.sora.tool.meta.util.trace;

import org.iterx.sora.tool.meta.Value;
import org.iterx.sora.tool.meta.util.InstructionVisitor;

import java.io.OutputStream;

public class TracerInstructionVisitor extends AbstractTracer<TracerInstructionVisitor> implements InstructionVisitor {

    private int indent;

    public TracerInstructionVisitor(final OutputStream outputStream) {
        this(outputStream, 0);
    }
    public TracerInstructionVisitor(final OutputStream outputStream, final int indent) {
        super(outputStream);
        this.indent = indent;
    }

    public void invokeSuper(final Value[] values) {
        print("invokeSuper(").print(values).print(");").newline();
    }

    public void returnValue(final Value value) {
        print("returnValue(").print(value).print(");").newline();
    }

    @Override
    protected int getIndent() {
        return indent;
    }

    private TracerInstructionVisitor print(final Value... values) {
        if(values.length > 0) {
            print(values[0].toString());
            for(int i = 1, length = values.length; i < length; i++) print(", ").print(values[i].toString());
        }
        return this;
    }

}
