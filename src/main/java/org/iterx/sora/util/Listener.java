package org.iterx.sora.util;

public interface Listener<E extends Enum, S> {

    void onEvent(E event, S source, Object... values);
}
