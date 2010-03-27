package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.AbstractValueInstruction;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.Value;

public final class InvokeSuperInstruction extends AbstractValueInstruction<InvokeSuperInstruction> {

    private static final Value<?>[] EMPTY_VALUES = new Value<?>[0];

    private final Type<?> target; //TODO: change this to variable & rename to owner???
    private final Value<?>[] values;

    private String methodName;
    private Type<?> returnType;

    private InvokeSuperInstruction(final Type<?> target, final Value<?>[] values) {
        this.returnType = Type.VOID_TYPE;
        this.methodName = "<init>";
        this.target = target;
        this.values = values;
    }

    public static InvokeSuperInstruction newInvokeSuperInstruction(final Type<?> target,
                                                                final Value<?>... values) {
        return new InvokeSuperInstruction(target, (values != null)? values : EMPTY_VALUES);
    }

    public Type<?> getType() {
        return getReturnType();
    }

    public Type<?> getTarget() {
        return target;
    }

    public Value<?>[] getValues() {
        return values;
    }

    public String getMethodName() {
        return methodName;
    }

    public InvokeSuperInstruction setMethodName(final String methodName) {
        this.methodName = methodName;
        return this;
    }

    public Type<?> getReturnType() {
        return returnType;
    }

    public InvokeSuperInstruction setReturnType(final Type<?> returnType) {
        this.returnType = returnType;
        return this;
    }
}
