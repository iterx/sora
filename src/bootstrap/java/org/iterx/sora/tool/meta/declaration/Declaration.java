package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.tool.meta.MetaClassLoader;

//TODO: make immutable & validate (problem: allows multiple instances???)
//TODO: => so make mutable state inner object???
public interface Declaration<T extends Declaration<T>> {  //implements Meta.Type

    public interface Access {
        String name();
    }

    public interface Modifier {
        String name();
    }

    Access getAccess();

    Modifier[] getModifiers();

    //MetaClassLoader getMetaClassLoader(); //TODO: add
}