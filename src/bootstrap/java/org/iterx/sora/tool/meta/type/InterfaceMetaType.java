package org.iterx.sora.tool.meta.type;

import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;

public final class InterfaceMetaType extends Type<InterfaceMetaType> {

    private InterfaceMetaType(final MetaClassLoader metaClassLoader, final String name) {
        super(metaClassLoader, name);
    }

    public static InterfaceMetaType newType(final String name) {
        return newType(MetaClassLoader.getSystemMetaClassLoader(), name);
    }

    public static InterfaceMetaType newType(final MetaClassLoader metaClassLoader, final String name) {
        return defineType(metaClassLoader, new InterfaceMetaType(metaClassLoader, name));
    }
    
    @Override
    public boolean isInterface() {
        return true;
    }
}
