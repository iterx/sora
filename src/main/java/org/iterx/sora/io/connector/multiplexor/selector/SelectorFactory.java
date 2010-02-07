package org.iterx.sora.io.connector.multiplexor.selector;

import org.iterx.sora.io.connector.session.Channel;

public interface SelectorFactory<T extends Channel<?>> {

    Selector<T> newSelector();
}
