package org.iterx.sora.tool.meta.type;

import org.iterx.sora.tool.meta.AbstractType;
import org.iterx.sora.tool.meta.MetaClassLoader;

public final class EnumType extends AbstractType<EnumType> {

    private EnumType(final MetaClassLoader metaClassLoader, final String name) {
        super(metaClassLoader, name);
    }

    public static EnumType newType(final String name) {
        return newType(MetaClassLoader.getSystemMetaClassLoader(), name);
    }

    public static EnumType newType(final MetaClassLoader metaClassLoader, final String name) {
        return defineType(metaClassLoader, new EnumType(metaClassLoader, name));
    }
        
    @Override
    public boolean isEnum() {
        return true;
    }
}
