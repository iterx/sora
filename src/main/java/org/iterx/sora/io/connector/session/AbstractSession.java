package org.iterx.sora.io.connector.session;

import static org.iterx.sora.util.Exception.swallow;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractSession<C extends Channel<R, W>, R, W> implements Session<C, R, W> {

    private final Lock stateLock;
    private volatile State state;

    protected AbstractSession() {
        this.stateLock = new ReentrantLock();
        this.state = State.CLOSED;
    }

    public void open() {
        changeState(State.OPENING);
    }

    public void close() {
        changeState(State.CLOSING);
    }

    protected State onOpening() {
        return State.OPEN;
    }

    protected State onOpen() {
        return State.OPEN;
    }

    protected State onClosing() {
        return State.CLOSED;
    }

    protected State onClosed() {
        return State.DESTROYING;
    }

    protected State onAbort(final Throwable throwable) {
        return State.CLOSED;
    }

    protected void assertState(final State isState) {
        if(!state.isSame(isState)) throw new IllegalStateException("Invalid state '" + state + "'");
    }

    protected void changeState(final State newState, final Object... arguments)
    {
        stateLock.lock();
        try {
            if(state.allowState(newState)) {
                for(State nextState = newState; nextState != null && state != nextState; ) {
                    try {
                        state = nextState;
                        nextState = state.run(this, arguments);
                    }
                    catch(final Throwable throwable) {
                        changeState(State.ABORTING, throwable);
                        swallow(throwable);
                        break;
                    }
                }
            }
        }
        finally {
            stateLock.unlock();
        }
    }

    protected enum State {
        CLOSED {
            @Override
            public boolean allowState(final State newState) {
                return newState == OPENING || super.isAllowed(newState);
            }

            @Override
            public State run(final AbstractSession session, final Object... arguments) {
                return session.onClosed();
            }
        },
        OPENING {
            @Override
            public boolean isAllowed(final State newState) {
                return newState == OPEN || super.isAllowed(newState);
            }

            @Override
            public State run(final AbstractSession session, final Object... arguments) {
                return session.onOpening();
            }
        },
        OPEN {
            @Override
            public boolean allowState(final State newState) {
                return newState == CLOSING || super.isAllowed(newState);
            }

            @Override
            public State run(final AbstractSession session, final Object... arguments) {
                return session.onOpen();
            }
        },
        CLOSING {
            @Override
            public boolean allowState(final State newState) {
                return newState == DESTROYING || super.isAllowed(newState);
            }

            @Override
            public State run(final AbstractSession session, final Object... arguments) {
                return session.onClosing();
            }
        },
        ABORTING {
            @Override
            public boolean allowState(final State newState) {
                return newState == CLOSING || newState == DESTROYING;
            }

            @Override
            public State run(final AbstractSession session, final Object... arguments) {
                return session.onAbort((Throwable) arguments[0]);
            }

        },
        DESTROYING {
            @Override
            public boolean allowState(final State newState) {
                return false;
            }
        };

        public boolean allowState(final State newState) {
            if(isSame(newState)) return false;
            else if(isAllowed(newState)) return true;
            throw new IllegalStateException("Invalid state transition from '" + name() + "' to '" + newState.name() + "'");
        }

        protected State run(final AbstractSession session, final Object... arguments) {
            return this;
        }

        protected boolean isAllowed(final State newState) {
            return (newState == State.ABORTING || newState == State.DESTROYING);
        }

        protected boolean isSame(final State newState) {
            return (this == newState);
        }
    }
}
