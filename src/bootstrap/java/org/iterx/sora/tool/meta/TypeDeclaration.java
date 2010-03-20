package org.iterx.sora.tool.meta;

public interface TypeDeclaration<T extends Type<T>, S extends Declaration<S>> extends Declaration<S>, Type<T> {
}
