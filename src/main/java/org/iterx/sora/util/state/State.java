package org.iterx.sora.util.state;

public interface State<S extends State<S, T>, T> {

    String name();

    S depends();

    boolean isAllowed(S newState);

    S run(T target, Object... arguments);
}
