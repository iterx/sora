package org.iterx.sora.collection;


public interface List<E> extends java.util.Collection<E> {

    Record<E> head();

    Record<E> tail();
}
