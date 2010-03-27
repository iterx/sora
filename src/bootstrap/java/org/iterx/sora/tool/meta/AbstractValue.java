package org.iterx.sora.tool.meta;


public abstract class AbstractValue<T extends Value<T>> implements Value<T> {

    private final Type<?> type;

    protected AbstractValue(final Type<?> type) {
        this.type = type;
    }

    public Type<?> getType() {
        return type;
    }

    public boolean isConstant() {
        return false;
    }

    public boolean isVariable() {
        return false;
    }

    public boolean isInstruction() {
        return false;
    }
}