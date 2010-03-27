package org.iterx.sora.tool.meta;

import org.iterx.sora.tool.meta.instruction.GetFieldInstruction;
import org.iterx.sora.tool.meta.instruction.InvokeMethodInstruction;
import org.iterx.sora.tool.meta.instruction.InvokeSuperInstruction;
import org.iterx.sora.tool.meta.instruction.PutFieldInstruction;
import org.iterx.sora.tool.meta.instruction.ReturnValueInstruction;
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
    
    protected Variable variable(final Type<?> type, final String name) {
        return Variable.newVariable(name, type);
    }

    protected GetFieldInstruction getField(final Type<?> fieldType, final String fieldName) {
        return store(instructions, GetFieldInstruction.newGetFieldInstruction(fieldType, fieldName));
    }

    protected PutFieldInstruction putField(final Type<?> fieldType, final String fieldName, final Value<?> value) {
        return store(instructions, PutFieldInstruction.newPutFieldInstruction(fieldType, fieldName, remove(instructions, value)));
    }

    protected StoreInstruction store(final Variable variable, final Value<?> value) {
        return store(instructions, StoreInstruction.newStoreInstruction(variable, remove(instructions, value)));
    }    

    protected InvokeSuperInstruction invokeSuper(final Type<?> target, final Value<?>... values) {
        return store(instructions, InvokeSuperInstruction.newInvokeSuperInstruction(target, values));
    }

    protected Object invokeStatic(final Type<?> target, final String methodName, final Value<?>... values) {
        throw new UnsupportedOperationException();
    }
    
    protected InvokeMethodInstruction invokeMethod(final Type<?> target, final String methodName, final Value<?>... values) {
        return store(instructions, InvokeMethodInstruction.newInvokeMethodInstruction(target, methodName, values));
    }

    protected ReturnValueInstruction returnValue(final Value<?> value) {
        return store(instructions, ReturnValueInstruction.newReturnInstruction(remove(instructions, value)));
    }

    private static <T> T store(final List<? super T> values, final T value) {
        values.add(value);
        return value;
    }

    private static Value remove(final List<Instruction<?>> values, final Value<?> value) {
        if(value.isInstruction()) values.remove(value);
        return value;
    }
}
