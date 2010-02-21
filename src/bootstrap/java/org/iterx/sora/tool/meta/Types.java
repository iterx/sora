package org.iterx.sora.tool.meta;

import org.objectweb.asm.Type;

public interface Types {

    public static final Type VOID_TYPE = Type.VOID_TYPE;
    public static final Type INT_TYPE = Type.INT_TYPE;
    public static final Type LONG_TYPE = Type.LONG_TYPE;

    public static final Type OBJECT_TYPE = Type.getType(Object.class);
    public static final Type STRING_TYPE = Type.getType(String.class);

}
