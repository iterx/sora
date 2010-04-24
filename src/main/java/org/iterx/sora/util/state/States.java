package org.iterx.sora.util.state;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.iterx.sora.util.Exception.swallow;

public class States<S extends State<S, T>, T> {

    private final Lock stateLock;
    private final T target;
    private final S abortState;

    private volatile S state;

    public States(final T target,
                  final S initialState,
                  final S abortState) {
        this.stateLock = new ReentrantLock();
        this.abortState = abortState;
        this.state = initialState;
        this.target = target;
    }

    public S state() {
        return state;
    }

    public boolean isState(final S isState) {
        return (state.equals(isState));
    }

    public void assertState(final S isState) {
        if(!isState(state)) throw new IllegalStateException("Invalid state '" + state + "'");
    }

    public void changeState(final S newState, final Object... arguments) {
        stateLock.lock();
        try {
            if(!state.equals(newState)) {
                S nextState = resolveState(newState);
                if(state.isAllowed(nextState)) {
                    while(nextState != null && state != nextState) {
                        try {
                            state = nextState;
                            nextState = state.run(target, arguments);
                        }
                        catch(final Throwable throwable) {
                            changeState(abortState, throwable);
                            swallow(throwable);
                            break;
                        }
                    }
                    return;
                }
                throw new IllegalStateException("Invalid state transition from '" + state.name() + "' to '" + newState.name() + "'");
            }
        }
        finally {
            stateLock.unlock();
        }
    }

    private S resolveState(final S newState) {
        final S dependsState = newState.depends();
        return (dependsState == null || isState(dependsState))? newState : resolveState(dependsState);
    }
}
