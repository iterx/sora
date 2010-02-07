package org.iterx.sora.io.connector.support.nio.multiplexor.selector;

import org.iterx.sora.collection.Arrays;
import org.iterx.sora.io.connector.multiplexor.Multiplexor;
import org.iterx.sora.io.connector.multiplexor.selector.Selector;
import org.iterx.sora.io.connector.multiplexor.selector.SelectorFactory;
import org.iterx.sora.io.connector.session.Channel;
import org.iterx.sora.io.connector.support.nio.session.NioChannel;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.iterx.sora.util.Exception.swallow;

public final class FileChannelNioSelectorFactory implements SelectorFactory<NioChannel<FileChannel>> {


    public Selector<NioChannel<FileChannel>> newSelector() {
        return new FileChannelNioSelector();
    }

    private static final class FileChannelNioSelector implements Selector<NioChannel<FileChannel>> {

        private final Lock lock;

        private volatile Multiplexor.Handler<? extends NioChannel<FileChannel>>[] readMultiplexorHandlers;
        private volatile Multiplexor.Handler<? extends NioChannel<FileChannel>>[] writeMultiplexorHandlers;

        public FileChannelNioSelector() {
            this.readMultiplexorHandlers = Arrays.newArray(Multiplexor.Handler.class, 0);
            this.writeMultiplexorHandlers = Arrays.newArray(Multiplexor.Handler.class, 0);
            this.lock = new ReentrantLock();
        }

        public boolean supports(final Multiplexor.Handler<?> multiplexorHandler) {
            final Channel channel = multiplexorHandler.getChannel();
            return channel instanceof NioChannel &&
                   ((NioChannel) channel).getChannel() instanceof FileChannel;
        }

        public boolean isReady() {
            if(writeMultiplexorHandlers.length == 0) {
                if(readMultiplexorHandlers.length != 0) {
                    for(final Multiplexor.Handler<? extends NioChannel<FileChannel>> readHandler : readMultiplexorHandlers) {
                        try {
                            final NioChannel<FileChannel> channel = readHandler.getChannel();
                            final FileChannel fileChannel = channel.getChannel();
                            if(fileChannel.position() < fileChannel.size()) return true;
                        }
                        catch(final IOException e) {
                            swallow(e);
                        }
                    }
                }
                return false;
            }
            return true;
        }

        public boolean poll(final long time, final TimeUnit timeUnit) {
            return doWrite() | doRead();
        }

        public boolean register(final Multiplexor.Handler<? extends NioChannel<FileChannel>> multiplexorHandler, final int ops) {
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

        public boolean deregister(final Multiplexor.Handler<? extends NioChannel<FileChannel>> multiplexorHandler, final int ops) {
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
