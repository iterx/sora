package org.iterx.sora.tool.meta.type;

public final class AnnotationMetaType extends Type<AnnotationMetaType> {

    private AnnotationMetaType(final String name) {
        super(name);
    }

    public static AnnotationMetaType newType(final String name) {
        return new AnnotationMetaType(name);
    }

    @Override
    public boolean isAnnotation() {
        return true;
    }
}