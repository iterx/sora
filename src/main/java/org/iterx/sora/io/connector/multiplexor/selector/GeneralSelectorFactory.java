package org.iterx.sora.io.connector.multiplexor.selector;

import org.iterx.sora.collection.Arrays;
import org.iterx.sora.io.connector.multiplexor.Multiplexor;
import org.iterx.sora.io.connector.session.Channel;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class GeneralSelectorFactory implements SelectorFactory<Channel<ByteBuffer, ByteBuffer>> {

    public Selector<Channel<ByteBuffer, ByteBuffer>> newSelector() {
        return new GeneralSelector();
    }

    private static final class GeneralSelector implements Selector<Channel<ByteBuffer, ByteBuffer>> {

        private final Lock lock;

        private volatile Multiplexor.Handler<? extends Channel<ByteBuffer, ByteBuffer>>[] readMultiplexorHandlers;
        private volatile Multiplexor.Handler<? extends Channel<ByteBuffer, ByteBuffer>>[] writeMultiplexorHandlers;

        private GeneralSelector() {
            this.readMultiplexorHandlers = Arrays.newArray(Multiplexor.Handler.class, 0);
            this.writeMultiplexorHandlers = Arrays.newArray(Multiplexor.Handler.class, 0);
            this.lock = new ReentrantLock();
        }

        public boolean supports(final Multiplexor.Handler<?> multiplexorHandler) {
            return true;
        }

        public boolean isReady() {
            return (writeMultiplexorHandlers.length != 0 || readMultiplexorHandlers.length != 0);
        }

        public boolean poll(final long time, final TimeUnit timeUnit) {
            return doWrite() | doRead();
        }

        public boolean register(final Multiplexor.Handler<? extends Channel<ByteBuffer, ByteBuffer>> multiplexorHandler, final int ops) {
            lock.lock();
            try {
                if((ops & Multiplexor.READ_OP) != 0) readMultiplexorHandlers = Arrays.add(readMultiplexorHandlers, multiplexorHandler);
                if((ops & Multiplexor.WRITE_OP) != 0) writeMultiplexorHandlers = Arrays.add(writeMultiplexorHandlers, multiplexorHandler);
                return true;
            }
            finally {
                lock.unlock();
            }
        }

        public boolean deregister(final Multiplexor.Handler<? extends Channel<ByteBuffer, ByteBuffer>> multiplexorHandler, final int ops) {
            lock.lock();
            try {
                if((ops & Multiplexor.READ_OP) != 0) readMultiplexorHandlers = Arrays.remove(readMultiplexorHandlers, multiplexorHandler);
                if((ops & Multiplexor.WRITE_OP) != 0) writeMultiplexorHandlers = Arrays.remove(writeMultiplexorHandlers, multiplexorHandler);
                return true;
            }
            finally {
                lock.unlock();
            }
        }

        public void destroy() {
            lock.lock();
            try {
                readMultiplexorHandlers = Arrays.newArray(Multiplexor.Handler.class, 0);
                writeMultiplexorHandlers = Arrays.newArray(Multiplexor.Handler.class, 0);
            }
            finally {
                lock.unlock();
            }
        }

        private boolean doRead() {
            final Multiplexor.Handler[] multiplexorHandlers = readMultiplexorHandlers;
            int read = 0;
            for(final Multiplexor.Handler multiplexorHandler : multiplexorHandlers) read += multiplexorHandler.doRead(1024);
            return (read != 0);
        }

        private boolean doWrite() {
            final Multiplexor.Handler[] multiplexorHandlers = writeMultiplexorHandlers;
            int written = 0;
            for(final Multiplexor.Handler multiplexorHandler : multiplexorHandlers) written += multiplexorHandler.doWrite(1024);
            return (written != 0);
        }
    }
}
