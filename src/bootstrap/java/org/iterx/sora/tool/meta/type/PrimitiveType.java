package org.iterx.sora.tool.meta.type;

import org.iterx.sora.tool.meta.AbstractType;
import org.iterx.sora.tool.meta.MetaClassLoader;

public final class PrimitiveType extends AbstractType<PrimitiveType> {

    public static final PrimitiveType VOID_TYPE = PrimitiveType.newType("void");
    public static final PrimitiveType BYTE_TYPE = PrimitiveType.newType("byte");
    public static final PrimitiveType BOOLEAN_TYPE = PrimitiveType.newType("boolean");
    public static final PrimitiveType CHAR_TYPE = PrimitiveType.newType("char");
    public static final PrimitiveType SHORT_TYPE = PrimitiveType.newType("short");
    public static final PrimitiveType INT_TYPE = PrimitiveType.newType("int");
    public static final PrimitiveType LONG_TYPE = PrimitiveType.newType("long");
    public static final PrimitiveType FLOAT_TYPE = PrimitiveType.newType("float");
    public static final PrimitiveType DOUBLE_TYPE = PrimitiveType.newType("double");
    

    private PrimitiveType(final MetaClassLoader metaClassLoader, final String name) {
        super(metaClassLoader, name);
    }

    public static PrimitiveType newType(final String name) {
        return newType(MetaClassLoader.getSystemMetaClassLoader(), name);
    }

    public static PrimitiveType newType(final MetaClassLoader metaClassLoader, final String name) {
        return defineType(metaClassLoader, new PrimitiveType(metaClassLoader, name));
    }
    
    @Override
    public boolean isPrimitive() {
        return true;
    }
}
