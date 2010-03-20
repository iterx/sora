package org.iterx.sora.tool.meta.util;

import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.value.Constant;


//TODO: inner classes & outer classes -> this is based on context -> i.e by walking tree -> opening inner classes!

public interface TypeVisitor {

    public enum Access {  PUBLIC, PROTECTED, PRIVATE, DEFAULT }
    public enum Modifier { ABSTRACT, FINAL, STATIC, VOLATILE }

    void startClass(Access access,
                    Modifier[] modifiers,
                    Type<?> type,
                    Type<?> superType,
                    Type<?>[] interfaceTypes);

    void startInterface(Access access,
                        Modifier[] modifiers,
                        Type<?> type,
                        Type<?>[] interfaceTypes);

    void field(Access access,
               Modifier[] modifiers,
               String fieldName,
               Type<?> fieldType,
               Constant fieldValue);

    InstructionVisitor startConstructor(Access access,
                                        Modifier[] modifiers,
                                        Type<?>[] constructorTypes,
                                        Type<?>[] exceptionTypes);

    void endConstructor();

    InstructionVisitor startMethod(Access access,
                                   Modifier[] modifiers,
                                   String methodName,
                                   Type<?> returnType,
                                   Type<?>[] argumentTypes,
                                   Type<?>[] exceptionTypes);

    void endMethod();

    void endClass();

    void endInterface();
}
