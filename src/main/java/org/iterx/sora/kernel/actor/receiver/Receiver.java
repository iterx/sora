package org.iterx.sora.kernel.actor.receiver;

public interface Receiver<T> {

    void receive(T object);

    void flush();
}
