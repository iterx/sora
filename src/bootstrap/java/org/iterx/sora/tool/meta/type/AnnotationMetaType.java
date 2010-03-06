package org.iterx.sora.tool.meta.type;

import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;

public final class AnnotationMetaType extends Type<AnnotationMetaType> {

    private AnnotationMetaType(final MetaClassLoader metaClassLoader, final String name) {
        super(metaClassLoader, name);
    }

    public static AnnotationMetaType newType(final String name) {
        return newType(MetaClassLoader.getSystemMetaClassLoader(), name);
    }

    public static AnnotationMetaType newType(final MetaClassLoader metaClassLoader, final String name) {
        return defineType(metaClassLoader, new AnnotationMetaType(metaClassLoader, name));
    }

    @Override
    public boolean isAnnotation() {
        return true;
    }
}