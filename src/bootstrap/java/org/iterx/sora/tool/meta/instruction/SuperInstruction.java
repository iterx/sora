package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.AbstractValueInstruction;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.Value;

public final class SuperInstruction extends AbstractValueInstruction<SuperInstruction> {

    private static final Value<?>[] EMPTY_VALUES = new Value<?>[0];

    private final Value<?>[] values;

    private String methodName;
    private Type<?> returnType;

    private SuperInstruction(final Value<?>[] values) {
        this.returnType = Type.VOID_TYPE;
        this.methodName = "<init>";
        this.values = values;
    }

    public static SuperInstruction newSuperInstruction(final Value<?>... values) {
        return new SuperInstruction((values != null)? values : EMPTY_VALUES);
    }

    public Type<?> getType() {
        return getReturnType();
    }

    public Value<?>[] getValues() {
        return values;
    }

    public String getMethodName() {
        return methodName;
    }

    public SuperInstruction setMethodName(final String methodName) {
        this.methodName = methodName;
        return this;
    }

    public Type<?> getReturnType() {
        return returnType;
    }

    public SuperInstruction setReturnType(final Type<?> returnType) {
        this.returnType = returnType;
        return this;
    }
}
