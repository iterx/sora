package org.iterx.sora.tool.meta.support.asm.scope;

import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.Value;
import org.iterx.sora.tool.meta.support.asm.Scope;
import org.iterx.sora.tool.meta.value.Variable;

import java.util.Arrays;

public final class BlockScope implements Scope<BlockScope> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final String NULL = null;

    private String[] names;
    private Value<?>[] values;

    private int capacity;
    private int size;

    public BlockScope() {
        this.names = new String[DEFAULT_CAPACITY];
        this.values= new Value<?>[DEFAULT_CAPACITY];
        this.capacity = DEFAULT_CAPACITY;
    }

    public int getIndex(final Value<?> value) {
        if(value != null) for(int i = 0; i != size;  i++) if(value.equals(values[i])) return i;
        throw new IllegalArgumentException("Unresolved value '" + value + "'");
    }

    public Value<?> getValue(final int index) {
        if(values[index] != null) return values[index];
        throw new IllegalArgumentException("Unresolved index '" + index + "'");
    }

    public Value<?> resolveValue(final String name) {
        if(name != null) for(int i = size; i-- != 0;) if(name.equals(names[i])) return values[i];
        throw new IllegalArgumentException("Unresolved name '" + name + "'");
    }

    public Value<?> peek() {
        final int index = (values[size] == null)? size - 1 : size;
        return values[index];
    }

    public Value<?> pop() {
        final int index = (values[size] == null)? size - 1 : size;
        final Value<?> value = values[index];

        names[index] = null;
        values[index] = null;
        size -= sizeOf(value);
        return value;
    }

    public int push(final Value<?> value) {
        final int sizeOf = sizeOf(value);
        final String nameOf = nameOf(value);
        final int index = size;
        ensure(index + sizeOf);
        names[index] = nameOf;
        values[index] = value;
        size += sizeOf;
        return index;
    }

    public void clear() {
        Arrays.fill(names, null);
        Arrays.fill(values, null);
        size = 0;
    }

    public int size() {
        return size;
    }

    private String nameOf(final Value<?> value) {
        return (value.isVariable())? ((Variable) value).getName() : NULL;
    }

    private int sizeOf(final Value<?> value) {
        final Type<?> type = value.getType();
        return (type == Type.LONG_TYPE || type == Type.DOUBLE_TYPE)? 2 : 1;
    }

    private void ensure(final int size) {
        if(capacity < size) {
            capacity = capacity * 2;
            names = Arrays.copyOf(names, capacity);
            values = Arrays.copyOf(values, capacity);
        }
    }
}
