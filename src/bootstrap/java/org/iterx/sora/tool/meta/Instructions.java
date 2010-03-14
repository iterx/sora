package org.iterx.sora.tool.meta;

import org.iterx.sora.tool.meta.instruction.GetFieldInstruction;
import org.iterx.sora.tool.meta.instruction.InvokeSuperInstruction;
import org.iterx.sora.tool.meta.instruction.PutFieldInstruction;
import org.iterx.sora.tool.meta.instruction.ReturnInstruction;
import org.iterx.sora.tool.meta.instruction.StoreInstruction;
import org.iterx.sora.tool.meta.value.Constant;
import org.iterx.sora.tool.meta.value.Variable;

import java.util.ArrayList;
import java.util.List;

public abstract class Instructions {

    public static final Constant NULL = Constant.NULL;

    private final List<Instruction<?>> instructions;

    public Instructions() {
        this.instructions = new ArrayList<Instruction<?>>();
    }

    public Instruction<?>[] getInstructions() {
        return instructions.toArray(new Instruction<?>[instructions.size()]);
    }

    protected Constant constant(final boolean value) {
        return Constant.newConstant(value);
    }

    protected Constant constant(final byte value) {
        return Constant.newConstant(value);
    }

    protected Constant constant(final char value) {
        return Constant.newConstant(value);
    }

    protected Constant constant(final short value) {
        return Constant.newConstant(value);
    }

    protected Constant constant(final int value) {
        return Constant.newConstant(value);
    }

    protected Constant constant(final long value) {
        return Constant.newConstant(value);
    }

    protected Constant constant(final float value) {
        return Constant.newConstant(value);
    }

    protected Constant constant(final double value) {
        return Constant.newConstant(value);
    }

    protected Constant constant(final String value) {
        return Constant.newConstant(value);
    }
    
    protected Variable variable(final String name, final Type<?> type) {
        return Variable.newVariable(name, type);
    }

    protected GetFieldInstruction getField(final String fieldName) {
        return store(instructions, GetFieldInstruction.newGetFieldInstruction(fieldName));
    }

    protected PutFieldInstruction putField(final String fieldName, final Variable variable) {
        return store(instructions, PutFieldInstruction.newPutFieldInstruction(fieldName, variable));
    }

    protected StoreInstruction store(final Variable variable, final Instruction instruction) {
        return store(instructions, StoreInstruction.newStoreInstruction(variable, instruction));
    }

    protected InvokeSuperInstruction invokeSuper(final Variable... variables) {
        return store(instructions, InvokeSuperInstruction.invokeInitInstruction(variables));
    }

    protected ReturnInstruction returnVoid() {
        return store(instructions, ReturnInstruction.newReturnInstruction());
    }

    protected ReturnInstruction returnInstruction(final Instruction instruction) {
        instructions.remove(instruction);
        return store(instructions, ReturnInstruction.newReturnInstruction(instruction));
    }

    protected ReturnInstruction returnValue(final Value value) {
        return store(instructions, ReturnInstruction.newReturnInstruction(value));
    }

    private static <T> T store(final List<? super T> values, final T value) {
        values.add(value);
        return value;
    }
}
