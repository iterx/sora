package org.iterx.sora.tool.meta.util.trace;

import org.iterx.sora.tool.meta.Value;
import org.iterx.sora.tool.meta.instruction.GetFieldInstruction;
import org.iterx.sora.tool.meta.instruction.InvokeSuperInstruction;
import org.iterx.sora.tool.meta.instruction.PutFieldInstruction;
import org.iterx.sora.tool.meta.instruction.ReturnInstruction;
import org.iterx.sora.tool.meta.instruction.StoreInstruction;
import org.iterx.sora.tool.meta.util.InstructionVisitor;
import org.iterx.sora.tool.meta.value.Constant;
import org.iterx.sora.tool.meta.value.Variable;

import java.io.PrintStream;

public class TraceInstructionVisitor implements InstructionVisitor {

    private final PrintStream printStream;

    public TraceInstructionVisitor(final PrintStream printStream) {
        this.printStream = printStream;
    }

    public void getField(final GetFieldInstruction getFieldInstruction) {
    }

    public void putField(final PutFieldInstruction putFieldInstruction) {
    }

    public void store(final StoreInstruction storeInstruction) {
    }

    public void invokeSuper(final InvokeSuperInstruction invokeSuperInstruction) {
        print("invokeSuper(");
        print(invokeSuperInstruction.getValues());
        print(")\n");
    }

    public void returnValue(final ReturnInstruction returnInstruction) {
        final Value value = returnInstruction.getValue();
        if(value == Constant.VOID) print("returnVoid(");
        else if(value != null) {
            print("returnValue(");
            print(returnInstruction.getValue());
        }
        else print("returnInstruction(");
        print(")\n");
    }

    private void print(final Value... values) {
        if(values.length > 0) {
            print(values[0]);
            for(int i = 1, length = values.length; i < length; i++) {
                print(" ,");
                print(values[i]);
            }
        }
    }

    private void print(final Value value) {
        if(value.isConstant()) printStream.print(((Constant) value).getValue());
        else if(value.isVariable()) printStream.print(((Variable) value).getName());
    }


    private void print(final String... strings) {
        if(strings.length > 0) {
            printStream.print(strings[0]);
            for(int i = 1, length = strings.length; i < length; i++) {
                printStream.print(" ");
                printStream.print(strings[i]);
            }
        }
    }

}
