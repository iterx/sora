package org.iterx.sora.tool.meta;


public interface Value<T extends Value<T>> {
    
    Type<?> getType();

    boolean isConstant();

    boolean isVariable();
}
