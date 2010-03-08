package org.iterx.sora.tool.meta.support.asm;

import org.iterx.sora.tool.meta.Type;

import java.util.Arrays;

//TODO: Rename to scope -> add block scoping & hierarchy 
public final class AsmScope {


    private static final int DEFAULT_CAPACITY = 16;

    private String[] names;
    private Type<?>[] types;

    private int size;
    private int capacity;

    public AsmScope() {
        this.names = new String[DEFAULT_CAPACITY];
        this.types = new Type<?>[DEFAULT_CAPACITY];
        this.capacity = DEFAULT_CAPACITY;
    }

    public int getIndex(final String name) {
        for(int i = 0, index = 0; i != size; index += sizeOf(types[i]), i++) if(names[i].equals(name)) return index;
        throw new IllegalArgumentException("Invalid name '" + name + "'");
    }

    public String getName(final int index) {
        for(int i = 0, ptr = 0; i != size; ptr += sizeOf(types[i]), i++) if(ptr == index) return names[i];
        throw new IllegalArgumentException("Invalid index '" + index + "'");
    }

    public Type<?> getType(final String name) {
        for(int i = 0; i != size; i++) if(names[i].equals(name)) return types[i];
        throw new IllegalArgumentException("Invalid name '" + name + "'");
    }

    public Type<?> getType(final int index) {
        return getType(getName(index));
    }

    public String peek() {
        return names[size - 1];
    }

    public void pop() {
        --size;
        names[size] = null;
        types[size] = null;
    }

    public String push(final String name, final Type type) {
        if(capacity == size) resize();
        names[size] = name;
        types[size] = type;
        size++;
        return name;
    }

    public void clear() {
        Arrays.fill(names, null);
        Arrays.fill(types, null);
        size = 0;
    }

    public int size() {
        return size;
    }

    private int sizeOf(final Type<?> type) {
        return (type == Type.LONG_TYPE || type == Type.DOUBLE_TYPE)? 2 : 1;
    }

    private void resize() {
        capacity = capacity * 2;
        names = Arrays.copyOf(names, capacity);
        types = Arrays.copyOf(types, capacity);
    }
}
