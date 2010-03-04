package org.iterx.sora.tool.meta.type;

public final class InterfaceMetaType extends Type<InterfaceMetaType> {

    private InterfaceMetaType(final String name) {
        super(name);
    }

    public static InterfaceMetaType newType(final String name) {
        return new InterfaceMetaType(name);
    }

    @Override
    public boolean isInterface() {
        return true;
    }
}
