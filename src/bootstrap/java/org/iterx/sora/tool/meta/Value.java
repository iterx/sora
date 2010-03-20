package org.iterx.sora.tool.meta;

import org.iterx.sora.tool.meta.value.Constant;


public interface Value<T extends Value<T>> {

    public final static Constant NULL = Constant.NULL;
    public final static Constant VOID = Constant.VOID;
        
    Type<?> getType();

    boolean isConstant();

    boolean isVariable();
}
