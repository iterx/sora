package org.iterx.sora.collection;

import java.lang.reflect.Array;

public final class Arrays {

    private Arrays() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(final Class<?> type, final int length) {
        return (type == Object[].class)?
               (T[]) new Object[length] :
               (T[]) Array.newInstance((type.isArray())? type.getComponentType() : type, length);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(final Class<?> type, final Object... values) {
        final int length = values.length;
        final T[] array = newArray(type, length);
        for(int i = 0; i != length; i++) array[i] = (T) values[i];
        return array;
    }

    public static <T> T[] add(final T[] array, T value) {
        final T[] copy = newArray(array.getClass(), array.length + 1);
        System.arraycopy(array, 0, copy, 0, array.length);
        copy[array.length] = value;
        return copy;
    }

    public static <T> T[] remove(final T[] array, T value) {
        final int index = indexOf(array, value);
        if(index != -1) {
            final T[] copy = newArray(array.getClass(), array.length - 1);
            if(index > 0 && copy.length > 0) System.arraycopy(array, 0, copy, index, index);
            if(index < copy.length) System.arraycopy(array, index + 1, copy, index, copy.length - index);
            return copy;
        }
        return array;
    }

    public static <T> T[] clear(final T[] array) {
        java.util.Arrays.fill(array, null);
        return array; 
    }

    public static <T> int indexOf(final T[] array, final T value){
        for(int i = 0, length = array.length; i != length; i++) if(array[i] == value) return i;
        return -1;
    }
}
