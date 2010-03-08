package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.Instruction;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.Value;

import java.util.ArrayList;
import java.util.List;

public abstract class Instructions {

    public static final Value VOID = Value.VOID;

    public final List<Instruction<?>> instructions;

    public Instructions() {
        this.instructions = new ArrayList<Instruction<?>>();
    }

    public Value value(final String name, final Type<?> type) {
        return Value.newValue(name, type);
    }

    public GetFieldInstruction getField(final String fieldName) {
        return store(instructions, GetFieldInstruction.newGetFieldInstruction(fieldName));
    }

    public PutFieldInstruction putField(final String fieldName, final Value value) {
        return store(instructions, PutFieldInstruction.newPutFieldInstruction(fieldName, value));
    }

    public StoreInstruction store(final Value value, final Instruction instruction) {
        return store(instructions, StoreInstruction.newStoreInstruction(value, instruction));
    }

    public InvokeSuperInstruction invokeSuper(final Value... values) {
        return store(instructions, InvokeSuperInstruction.invokeInitInstruction(values));
    }

    public ReturnValueInstruction returnValue(final Instruction instruction) {
        instructions.remove(instruction);
        return store(instructions, ReturnValueInstruction.newReturnInstruction(instruction));
    }

    public ReturnValueInstruction returnValue(final Value value) {
        return store(instructions, ReturnValueInstruction.newReturnInstruction(value));
    }

    private static <T> T store(final List<? super T> values, final T value) {
        values.add(value);
        return value;
    }
}
