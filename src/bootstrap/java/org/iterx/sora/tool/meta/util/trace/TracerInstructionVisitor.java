package org.iterx.sora.tool.meta.util.trace;

import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.Value;
import org.iterx.sora.tool.meta.ValueInstruction;
import org.iterx.sora.tool.meta.util.InstructionReader;
import org.iterx.sora.tool.meta.util.InstructionVisitor;
import org.iterx.sora.tool.meta.value.Variable;

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

    public void SUPER(
            final String methodName,
            final Type<?> returnType,
            final Value<?>[] values) {
        print("SUPER").print(".").print(methodName);
        print("(").print(values).print(")").print(returnType).newline();
    }

    public void invokeMethod(final Type<?> target,
                            final String methodName,
                            final Type<?> returnType,
                            final Value<?>[] values) {
        print("invokeMethod").print(".").print(methodName);
        print("(").print(values).print(")").print(returnType).newline();
    }

    public void RETURN(final Value<?> value) {
        //TODO: need to redispatch if value => value instruction....
        print("RETURN(");
        if(value.isInstruction()) {
            new InstructionReader((ValueInstruction) value).accept(this);
        }
        else print(value);
        print(")").newline();
    }

    public void store(final Variable variable, final Value<?> value) {
        print("store(").print(variable.getName()).print(", ").print(value).print(")").newline();
    }

    public void GETFIELD(final Variable owner,
                         final String fieldName,
                         final Type<?> fieldType) {
        print(owner).print(".GETFIELD(").print(fieldName).print(")").print(fieldType).newline();
    }


    public void putField(final Variable owner,
                         final String fieldName,
                         final Type<?> fieldType,
                         final Value<?> value) {
        print(owner).print(".putField(").print(fieldName).print(", ").print(value).print(")").
                print(fieldType).newline();

    }

    @Override
    protected int getIndent() {
        return indent;
    }


}
