package org.iterx.sora.tool.meta.type;

import org.iterx.sora.tool.meta.AbstractType;
import org.iterx.sora.tool.meta.MetaClassLoader;

public final class ClassType extends AbstractType<ClassType> {

    public static final ClassType OBJECT_TYPE = ClassType.newType("java.lang.Object");
    public static final ClassType STRING_TYPE = ClassType.newType("java.lang.String");

    private ClassType(final MetaClassLoader metaClassLoader, final String name) {
        super(metaClassLoader, name);
    }

    public static ClassType newType(final String name) {
        return newType(MetaClassLoader.getSystemMetaClassLoader(), name);
    }

    public static ClassType newType(final MetaClassLoader metaClassLoader, final String name) {
        return defineType(metaClassLoader, new ClassType(metaClassLoader, name));
    }

    @Override
    public boolean isClass() {
        return true;
    }
}
