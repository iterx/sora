package org.iterx.sora.tool.meta.type;

import org.iterx.sora.tool.meta.AbstractType;
import org.iterx.sora.tool.meta.MetaClassLoader;

public final class InterfaceType extends AbstractType<InterfaceType> {

    private InterfaceType(final MetaClassLoader metaClassLoader, final String name) {
        super(metaClassLoader, name);
    }

    public static InterfaceType newType(final String name) {
        return newType(MetaClassLoader.getSystemMetaClassLoader(), name);
    }

    public static InterfaceType newType(final MetaClassLoader metaClassLoader, final String name) {
        return defineType(metaClassLoader, new InterfaceType(metaClassLoader, name));
    }
    
    @Override
    public boolean isInterface() {
        return true;
    }
}
