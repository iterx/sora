package org.iterx.sora.io.connector.support.nio.multiplexor.selector;

import org.iterx.sora.io.IoException;
import org.iterx.sora.io.connector.multiplexor.Multiplexor;
import org.iterx.sora.io.connector.multiplexor.selector.Selector;
import org.iterx.sora.io.connector.multiplexor.selector.SelectorFactory;
import org.iterx.sora.io.connector.session.Channel;
import org.iterx.sora.io.connector.support.nio.session.NioChannel;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class SelectableChannelNioSelectorFactory implements SelectorFactory<NioChannel<? extends SelectableChannel>> {

    public Selector<NioChannel<? extends SelectableChannel>> newSelector() {
        return new SelectableChannelNioSelector();
    }

    private static final class SelectableChannelNioSelector implements Selector<NioChannel<? extends SelectableChannel>> {

        private static final int OP_CLOSE = 2;

        private final java.nio.channels.Selector selector;

        private final int readPollSize = 4096; //TODO: read in via properties.... or self tune???
        private final int writePollSize = 4096;

        private final AtomicInteger pendingRegister;

        public SelectableChannelNioSelector() {
            try {
                this.pendingRegister = new AtomicInteger();
                this.selector = java.nio.channels.Selector.open();
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
        }

        public boolean supports(final Multiplexor.Handler<?> multiplexorHandler) {
            final Channel channel = multiplexorHandler.getChannel();
            return channel instanceof NioChannel &&
                   ((NioChannel) channel).getChannel() instanceof SelectableChannel;
        }

        public boolean isReady() {
            return !selector.keys().isEmpty();
        }

        @SuppressWarnings("unchecked")
        public boolean poll(final long time, final TimeUnit timeUnit) {
            try {
                try {
                    if(selector.select(timeUnit.toMillis(time)) != 0) {
                        for(final Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                            selectionKeyIterator.hasNext();) {
                            try {
                                final SelectionKey selectionKey = selectionKeyIterator.next();
                                final Multiplexor.Handler<? extends SelectableChannel> handler = (Multiplexor.Handler<? extends SelectableChannel>) selectionKey.attachment();
                                final SelectableChannel selectableChannel = selectionKey.channel();
                                if(selectableChannel.isOpen()) {
                                    final int readyOps = selectionKey.readyOps();
                                    if((readyOps & SelectionKey.OP_CONNECT) != 0) handler.doOpen();
                                    if((readyOps & SelectionKey.OP_ACCEPT) != 0) handler.doOpen();
                                    if((readyOps & SelectionKey.OP_READ) != 0) handler.doRead(readPollSize);
                                    if((readyOps & SelectionKey.OP_WRITE) != 0) handler.doWrite(writePollSize);
                                }
                                else  {
                                    handler.doClose();
                                    selectionKey.cancel();
                                }
                            }
                            finally {
                                selectionKeyIterator.remove();
                            }
                        }
                        return true;
                    }
                }
                finally {
                    while(pendingRegister.get() != 0) Thread.yield();
                }
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
            return false;
        }

        public boolean register(final Multiplexor.Handler<? extends NioChannel<? extends SelectableChannel>> multiplexorHandler, final int ops) {
            try {
                final NioChannel<? extends SelectableChannel> channel = multiplexorHandler.getChannel();
                final SelectableChannel selectableChannel = channel.getChannel();
                final int interestOps = toSelectorOps(ops) & selectableChannel.validOps();
                if(interestOps != 0) {
                    final SelectionKey selectionKey = selectableChannel.keyFor(selector);
                    if(selectionKey != null && selectionKey.isValid()) {
                        selectionKey.interestOps(selectionKey.interestOps() | interestOps);
                    }
                    else {
                        pendingRegister.incrementAndGet();
                        try {
                            selector.wakeup();
                            selectableChannel.register(selector, interestOps, multiplexorHandler);
                        }
                        finally {
                            pendingRegister.decrementAndGet();
                        }
                    }
                    return true;
                }
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
            return false;
        }

        public boolean deregister(final Multiplexor.Handler<? extends NioChannel<? extends SelectableChannel>> multiplexorHandler, final int ops) {
            final NioChannel<? extends SelectableChannel> channel = multiplexorHandler.getChannel();
            final SelectableChannel selectableChannel = channel.getChannel();
            final SelectionKey selectionKey = selectableChannel.keyFor(selector);
            if(selectionKey != null && selectionKey.isValid()) {
                final int interestOps = (toSelectorOps(ops) & selectableChannel.validOps()) ^ selectionKey.interestOps();
                selectionKey.interestOps(interestOps);
            }
            return false;
        }

        public void destroy() {
            try {
                selector.close();
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
        }

        private static int toSelectorOps(final int ops) {
            int selectorOps = 0;
            if((ops & Multiplexor.OPEN_OP) != 0) selectorOps |= (SelectionKey.OP_ACCEPT|SelectionKey.OP_CONNECT);
            if((ops & Multiplexor.READ_OP) != 0) selectorOps |= SelectionKey.OP_READ;
            if((ops & Multiplexor.WRITE_OP) != 0) selectorOps |= SelectionKey.OP_WRITE;
            if((ops & Multiplexor.CLOSE_OP) != 0) selectorOps |= OP_CLOSE;
            return selectorOps;
        }
    }
}
