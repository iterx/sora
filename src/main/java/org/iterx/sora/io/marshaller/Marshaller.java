package org.iterx.sora.io.marshaller;

public interface Marshaller<S, T> {
    
    boolean supports(Class<?> cls);
    
    T encode(T target, S source);

    S decode(S target, T source);
}
