package org.iterx.sora.tool.meta.declaration;

//TODO: make immutable & validate (problem: allows multiple instances???)
//TODO: => so make mutable state inner object???
public interface Declaration<T extends Declaration> {

    public interface Access {
        String name();
    }

    public interface Modifier {
        String name();
    }

    Access getAccess();

    Modifier[] getModifiers();
}