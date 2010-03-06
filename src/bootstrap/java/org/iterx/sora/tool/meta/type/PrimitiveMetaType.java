package org.iterx.sora.tool.meta.type;

import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;

public final class PrimitiveMetaType extends Type<PrimitiveMetaType> {

    private PrimitiveMetaType(final MetaClassLoader metaClassLoader, final String name) {
        super(metaClassLoader, name);
    }

    public static PrimitiveMetaType newType(final String name) {
        return newType(MetaClassLoader.getSystemMetaClassLoader(), name);
    }

    public static PrimitiveMetaType newType(final MetaClassLoader metaClassLoader, final String name) {
        return defineType(metaClassLoader, new PrimitiveMetaType(metaClassLoader, name));
    }
    
    @Override
    public boolean isPrimitive() {
        return true;
    }
}
