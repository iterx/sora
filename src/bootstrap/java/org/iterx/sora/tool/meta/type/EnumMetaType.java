package org.iterx.sora.tool.meta.type;

public final class EnumMetaType extends Type<EnumMetaType> {

    private EnumMetaType(final String name) {
        super(name);
    }

    public static EnumMetaType newType(final String name) {
        return new EnumMetaType(name);
    }

    @Override
    public boolean isEnum() {
        return true;
    }
}
