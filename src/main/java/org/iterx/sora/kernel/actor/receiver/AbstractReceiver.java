package org.iterx.sora.kernel.actor.receiver;

public abstract class AbstractReceiver<T> implements Receiver<T> {

    public abstract void receive(final T object);

    public void flush() {
    }
}
