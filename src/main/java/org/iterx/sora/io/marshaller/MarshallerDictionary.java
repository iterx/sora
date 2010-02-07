package org.iterx.sora.io.marshaller;

public interface MarshallerDictionary<T> {

    <S> Marshaller<S, T> loadMarshaller(Class<?> cls);

    void register(Marshaller<?, T> marshaller);

    void unregister(Marshaller<?, T> marshaller);
}
