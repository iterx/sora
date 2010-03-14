package org.iterx.sora.tool.meta.type;

import org.iterx.sora.tool.meta.AbstractType;
import org.iterx.sora.tool.meta.MetaClassLoader;

public final class PrimitiveType extends AbstractType<PrimitiveType> {

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
