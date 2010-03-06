package org.iterx.sora.tool.meta.type;

import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;

public final class EnumMetaType extends Type<EnumMetaType> {

    private EnumMetaType(final MetaClassLoader metaClassLoader, final String name) {
        super(metaClassLoader, name);
    }

    public static EnumMetaType newType(final String name) {
        return newType(MetaClassLoader.getSystemMetaClassLoader(), name);
    }

    public static EnumMetaType newType(final MetaClassLoader metaClassLoader, final String name) {
        return defineType(metaClassLoader, new EnumMetaType(metaClassLoader, name));
    }
        
    @Override
    public boolean isEnum() {
        return true;
    }
}
