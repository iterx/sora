package org.iterx.sora.tool.meta.support.asm;

import org.objectweb.asm.Type;

import java.util.Arrays;

public class Stack {

    private static final int DEFAULT_SIZE = 8;

    private Type[] types;
    private String[] names;
    private int index;
    private int size;

    public Stack() {
        index = 1;
        types = new Type[DEFAULT_SIZE];
        names = new String[DEFAULT_SIZE];
    }

    public int push(final String name, final Type type) {
        final int start = index;
        ensureCapacity();
        types[size] = type;
        names[size] = name;
        index += type.getSize();
        size += 1;
        return start;
    }

    public String peek() {
        return names[size - 1];
    }

    public Type getType(final String name) {
        for(int i = size; i-- != 0; ) if(name.equals(names[i])) return types[i];
        throw new IllegalArgumentException("Invalid name '" + name + "'");
    }

    public Type[] getTypes(final String[] names) {
        final Type[] types = new Type[names.length];
        for(int i = types.length; i-- != 0;) types[i] = getType(names[i]);
        return types;
    }

    public int getIndex(final String name)
    {
        for(int i = 0, index = 1; i < size; i++) {
            if(names[i].equals(name)) return index;
            index += types[i].getSize();
        }
        throw new IllegalArgumentException("Invalid name '" + name + "'");
    }

    private void ensureCapacity() {
        if(size + 1 >= types.length) {
            final int newLength = types.length * 2;
            types = Arrays.copyOf(types, newLength);
            names = Arrays.copyOf(names, newLength);
        }
    }
}
