package org.iterx.sora.tool.meta;


public abstract class Value<T extends Value<T>> {
    
    private final Type<?> type;

    protected Value(final Type<?> type) {
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
}
