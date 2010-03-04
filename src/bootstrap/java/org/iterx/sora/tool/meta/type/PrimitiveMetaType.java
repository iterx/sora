package org.iterx.sora.tool.meta.type;

public final class PrimitiveMetaType extends Type<PrimitiveMetaType> {

    private PrimitiveMetaType(final String name) {
        super(name);
    }

    public static PrimitiveMetaType newType(final String name) {
        return new PrimitiveMetaType(name);
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }
}
