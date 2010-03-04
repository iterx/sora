package org.iterx.sora.tool.meta.declaration;


//TODO: make immutable & validate (problem: allows multiple instances???)
//TODO: => so make mutable state inner object???
public abstract class Declaration<T extends Declaration<T>> {  //implements Meta.Type

    public interface Access {
        String name();
    }

    public interface Modifier {
        String name();
    }

    public abstract Access getAccess();

    public abstract Modifier[] getModifiers();

    //MetaClassLoader getMetaClassLoader(); //TODO: add
}