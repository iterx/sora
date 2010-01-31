package org.iterx.sora.kernel.actor.sender;

public interface Sender<T> {

    void send(T object);

    void flush();
}
