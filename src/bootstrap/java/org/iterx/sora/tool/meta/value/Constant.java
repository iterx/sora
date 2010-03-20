package org.iterx.sora.tool.meta.value;

import org.iterx.sora.tool.meta.AbstractValue;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.Value;

public final class Constant extends AbstractValue<Constant> {

    public final static Constant NULL = new Constant(Type.OBJECT_TYPE, null);
    public final static Constant VOID = new Constant(Type.VOID_TYPE, null);

    private final Object value;

    private Constant(final Type<?> type, final Object value) {
        super(type);
        this.value = value;
    }

    public static Constant newConstant(final boolean value) {
        return new Constant(Type.BOOLEAN_TYPE, value);
    }

    public static Constant newConstant(final byte value) {
        return new Constant(Type.BYTE_TYPE, value);
    }

    public static Constant newConstant(final char value) {
        return new Constant(Type.CHAR_TYPE, value);
    }

    public static Constant newConstant(final short value) {
        return new Constant(Type.SHORT_TYPE, value);
    }

    public static Constant newConstant(final int value) {
        return new Constant(Type.INT_TYPE, value);
    }

    public static Constant newConstant(final long value) {
        return new Constant(Type.LONG_TYPE, value);
    }

    public static Constant newConstant(final float value) {
        return new Constant(Type.FLOAT_TYPE, value);
    }

    public static Constant newConstant(final double value) {
        return new Constant(Type.DOUBLE_TYPE, value);
    }

    public static Constant newConstant(final String value) {
        return new Constant(Type.STRING_TYPE, value);
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public String toString() {
        return (value != null)? value.toString() : "null";
    }

    @Override
    public int hashCode() {
        return (value != null)? value.hashCode() : 0;
    }

    @Override
    public boolean equals(final Object object) {
        return ((this == object) ||
                (object != null && getClass().equals(object.getClass()) &&
                 (value != null)? value.equals(((Constant) object).getValue()) : value == ((Constant) object).getValue()));
    }
}
