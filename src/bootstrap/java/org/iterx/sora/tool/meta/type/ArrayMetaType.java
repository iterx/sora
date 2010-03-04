package org.iterx.sora.tool.meta.type;

public final class ArrayMetaType extends Type<ArrayMetaType> {

    private ArrayMetaType(final String name) {
        super(name);
    }

    public static ArrayMetaType newType(final String name) {
        return new ArrayMetaType(name);
    }

    @Override
    public boolean isArray() {
        return true;
    }
}