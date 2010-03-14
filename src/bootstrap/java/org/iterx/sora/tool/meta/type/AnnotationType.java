package org.iterx.sora.tool.meta.type;

import org.iterx.sora.tool.meta.AbstractType;
import org.iterx.sora.tool.meta.MetaClassLoader;

public final class AnnotationType extends AbstractType<AnnotationType> {

    private AnnotationType(final MetaClassLoader metaClassLoader, final String name) {
        super(metaClassLoader, name);
    }

    public static AnnotationType newType(final String name) {
        return newType(MetaClassLoader.getSystemMetaClassLoader(), name);
    }

    public static AnnotationType newType(final MetaClassLoader metaClassLoader, final String name) {
        return defineType(metaClassLoader, new AnnotationType(metaClassLoader, name));
    }

    @Override
    public boolean isAnnotation() {
        return true;
    }
}