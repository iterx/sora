package org.iterx.sora.io.connector.session;

import org.iterx.sora.util.state.States;

import static org.iterx.sora.util.Exception.swallow;

public abstract class AbstractSession<C extends Channel<R, W>, R, W> implements Session<C, R, W> {

    private final States<State, AbstractSession> states;

    protected AbstractSession() {
        states = new States<State, AbstractSession>(this, State.CLOSED, State.ABORTED);
    }

    public Session<C, R, W> open() {
        changeState(State.OPENED);
        return this;
    }

    public Session<C, R, W> close() {
        changeState(State.CLOSED);
        return this;
    }

    protected State onOpening() {
        return State.OPENED;
    }

    protected State onOpen() {
        return State.OPENED;
    }

    protected State onClosing() {
        return State.CLOSED;
    }

    protected State onClose() {
        return State.CLOSED;
    }

    protected State onAbort(final Throwable throwable) {
        return State.ABORTED;
    }

    protected boolean isState(final State isState) {
        return states.isState(isState);
    }
    
    protected void assertState(final State isState) {
        states.assertState(isState);
    }

    protected void changeState(final State newState, final Object... arguments) {
        states.changeState(newState, arguments);
    }

    protected enum State implements org.iterx.sora.util.state.State<State, AbstractSession> {
        CLOSED {
            @Override
            public State depends() {
                return State.CLOSING;
            }
            
            @Override
            public boolean isAllowed(final State newState) {
                return newState == OPENING || super.isAllowed(newState);
            }

            @Override
            public State run(final AbstractSession session, final Object... arguments) {
                return session.onClose();
            }
        },
        OPENING {
            @Override
            public boolean isAllowed(final State newState) {
                return newState == OPENED || super.isAllowed(newState);
            }

            @Override
            public State run(final AbstractSession session, final Object... arguments) {
                return session.onOpening();
            }
        },
        OPENED {
            @Override
            public State depends() {
                return State.OPENING;
            }

            @Override
            public boolean isAllowed(final State newState) {
                return newState == CLOSING || super.isAllowed(newState);
            }

            @Override
            public State run(final AbstractSession session, final Object... arguments) {
                return session.onOpen();
            }
        },
        CLOSING {
            @Override
            public boolean isAllowed(final State newState) {
                return newState == CLOSED || super.isAllowed(newState);
            }

            @Override
            public State run(final AbstractSession session, final Object... arguments) {
                return session.onClosing();
            }
        },
        ABORTED {
            @Override
            public boolean isAllowed(final State newState) {
                return newState == CLOSING || newState == DESTROYING;
            }

            @Override
            public State run(final AbstractSession session, final Object... arguments) {
                return session.onAbort((Throwable) arguments[0]);
            }
        },
        DESTROYING {
            @Override
            public boolean isAllowed(final State newState) {
                return false;
            }
        };


        public State depends() {
            return null;
        }

        public boolean isAllowed(final State newState) {
            return (newState == State.ABORTED || newState == State.DESTROYING);
        }

        public State run(final AbstractSession session, final Object... arguments) {
            return this;
        }
    }
}
