package org.iterx.sora.tool.meta.type;

public final class ClassMetaType extends Type<ClassMetaType> {

    private ClassMetaType(final String name) {
        super(name);
    }

    public static ClassMetaType newType(final String name) {
        return new ClassMetaType(name);
    }

    @Override
    public boolean isClass() {
        return true;
    }
}
