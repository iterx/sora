package org.iterx.sora.tool.meta.type;

import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;

public final class ClassMetaType extends Type<ClassMetaType> {

    private ClassMetaType(final MetaClassLoader metaClassLoader, final String name) {
        super(metaClassLoader, name);
    }

    public static ClassMetaType newType(final String name) {
        return newType(MetaClassLoader.getSystemMetaClassLoader(), name);
    }

    public static ClassMetaType newType(final MetaClassLoader metaClassLoader, final String name) {
        return defineType(metaClassLoader, new ClassMetaType(metaClassLoader, name));
    }

    @Override
    public boolean isClass() {
        return true;
    }
}
