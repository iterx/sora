package org.iterx.sora.tool.meta;

public abstract class AbstractValueInstruction<T extends Instruction<T>> extends AbstractInstruction<T> implements ValueInstruction<T> {

    public boolean isConstant() {
        return false;
    }

    public boolean isVariable() {
        return false;
    }

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public String toString() {
        return getType().toString();
    }

    @Override
    public int hashCode() {
        return getType().hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        return ((this == object) ||
                (object != null &&
                 getClass().equals(object.getClass()) &&
                 getType().equals(((AbstractValueInstruction) object).getType())));
    }
}
