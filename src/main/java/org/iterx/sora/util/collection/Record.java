package org.iterx.sora.util.collection;

public interface Record<E> {

    Record<E> previous();
        
    Record<E> next();

    E get();

    void remove();
}
