package org.iterx.sora.collection;

public interface Record<E> {

    Record<E> previous();
        
    Record<E> next();

    E get();

    void remove();
}
