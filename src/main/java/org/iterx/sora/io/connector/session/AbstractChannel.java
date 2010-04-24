package org.iterx.sora.io.connector.session;

import org.iterx.sora.util.state.States;

public abstract class AbstractChannel<R, W> implements Channel<R, W> {

    private final States<State, AbstractChannel> states;

    protected AbstractChannel() {
        states = new States<State, AbstractChannel>(this, State.CLOSED, State.ABORTED);
    }

    public Channel<R, W> open() {
        states.changeState(State.OPENED);
        return this;
    }

    public Channel<R, W> close() {
        states.changeState(State.CLOSED);
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

    protected enum State implements org.iterx.sora.util.state.State<State, AbstractChannel> {
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
            public State run(final AbstractChannel channel, final Object... arguments) {
                return channel.onClose();
            }
        },
        OPENING {
            @Override
            public boolean isAllowed(final State newState) {
                return newState == OPENED || super.isAllowed(newState);
            }

            @Override
            public State run(final AbstractChannel channel, final Object... arguments) {
                return channel.onOpening();
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
            public State run(final AbstractChannel channel, final Object... arguments) {
                return channel.onOpen();
            }
        },
        CLOSING {
            @Override
            public boolean isAllowed(final State newState) {
                return newState == CLOSED || super.isAllowed(newState);
            }

            @Override
            public State run(final AbstractChannel channel, final Object... arguments) {
                return channel.onClosing();
            }
        },
        ABORTED {
            @Override
            public boolean isAllowed(final State newState) {
                return newState == CLOSING || newState == DESTROYING;
            }

            @Override
            public State run(final AbstractChannel channel, final Object... arguments) {
                return channel.onAbort((Throwable) arguments[0]);
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

        public State run(final AbstractChannel channel, final Object... arguments) {
            return this;
        }
    }
}
