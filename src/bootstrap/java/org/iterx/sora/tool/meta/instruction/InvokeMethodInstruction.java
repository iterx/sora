package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.AbstractValueInstruction;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.Value;

public final class InvokeMethodInstruction extends AbstractValueInstruction<InvokeMethodInstruction> {

    private static final Value<?>[] EMPTY_VALUES = new Value<?>[0];

    private final String methodName;
    private final Value<?>[] values;
    private Type<?> target;
    private Type<?> returnType;

    public InvokeMethodInstruction(final Type<?> target, final String methodName, final Value<?>[] values) {
        this.returnType = Type.VOID_TYPE;
        this.target = target;
        this.methodName = methodName;
        this.values = values;
    }

    public static InvokeMethodInstruction newInvokeMethodInstruction(final Type<?> target,
                                                                     final String methodName,
                                                                     final Value<?>... values) {
        return new InvokeMethodInstruction(target, methodName, (values != null)? values : EMPTY_VALUES);
    }

    public Type<?> getType() {
        return returnType;
    }

    public Type<?> getTarget() {
        return target;
    }

    public String getMethodName() {
        return methodName;
    }

    public Value<?>[] getValues() {
        return values;
    }

    public Type<?> getReturnType() {
        return returnType;
    }

    public InvokeMethodInstruction setReturnType(final Type<?> returnType) {
        this.returnType = returnType;
        return this;
    }
}
