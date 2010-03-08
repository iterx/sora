package org.iterx.sora.tool.meta;

public abstract class Instruction<T extends Instruction<T>> {

    public static Instruction<?> NO_OP = new Instruction() {}; 

    //TODO: can we work this out???
    //public abstract Type<?> getReturnType();
}
