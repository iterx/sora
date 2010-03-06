package org.iterx.sora.tool.meta.type;

import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;

public final class ArrayMetaType extends Type<ArrayMetaType> {

    private final Type<?> type;

    private ArrayMetaType(final MetaClassLoader metaClassLoader, final Type<?> type) {
        super(metaClassLoader, toArrayName(type));
        this.type = type;
    }

    public static ArrayMetaType newType(final Type<?> type) {
        return newType(MetaClassLoader.getSystemMetaClassLoader(), type);
    }

    public static ArrayMetaType newType(final MetaClassLoader metaClassLoader, final Type<?> type) {
        return defineType(metaClassLoader, new ArrayMetaType(metaClassLoader, type));
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