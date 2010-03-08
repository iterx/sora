package org.iterx.sora.tool.meta;

public final class Value {

    public static Value VOID = newValue(null);
    //public static Value LAST_VALUE = newValue("<last>");
    //public static Value DEFAULT_VALUE = newValue("<default>");

    //TODO: do we need to support types???
    private final Type<?> type;
    private final String name;

    private Value(final String name, final Type<?> type) {
        this.name = name;
        this.type = type;
    }

    //TODO: abstract out as local variable, class variable, constant, etc???
    //TODO: allow setting of defaults...
    public static Value newValue(final String name) {
        return newValue(name, Type.OBJECT_TYPE);
    }
    public static Value newValue(final String name, final Type<?> type) {
        return new Value(name, type);
    }

    public String getName() {
        return name;
    }

    public Type<?> getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        return ((this == object) ||
                (object != null && getClass().equals(object.getClass()) && getName().equals(((Value) object).getName())));
    }
}
