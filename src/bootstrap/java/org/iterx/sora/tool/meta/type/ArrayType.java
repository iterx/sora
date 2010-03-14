package org.iterx.sora.tool.meta.type;

import org.iterx.sora.tool.meta.AbstractType;
import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;

public final class ArrayType extends AbstractType<ArrayType> {

    private final Type<?> type;

    private ArrayType(final MetaClassLoader metaClassLoader, final Type<?> type) {
        super(metaClassLoader, toArrayName(type));
        this.type = type;
    }

    public static ArrayType newType(final Type<?> type) {
        return newType(MetaClassLoader.getSystemMetaClassLoader(), type);
    }

    public static ArrayType newType(final MetaClassLoader metaClassLoader, final Type<?> type) {
        return defineType(metaClassLoader, new ArrayType(metaClassLoader, type));
    }

    public Type<?> getType() {
        return type;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    private static String toArrayName(final Type<?> type) {
        return type.getName() + "[]";
    }
}