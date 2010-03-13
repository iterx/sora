package org.iterx.sora.tool.meta.value;

import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.Value;

public final class Variable extends Value<Variable> {
    private final String name;

    private Variable(final String name, final Type<?> type) {
        super(type);
        this.name = name;
    }

    public static Variable newVariable(final String name) {
        return newVariable(name, Type.OBJECT_TYPE);
    }
    
    public static Variable newVariable(final String name, final Type<?> type) {
        return new Variable(name, type);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean isVariable() {
        return true;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        return ((this == object) ||
                (object != null &&
                 getClass().equals(object.getClass()) &&
                 getName().equals(((Variable) object).getName()) &&
                 getType().equals(((Variable) object).getType())));
    }
}
