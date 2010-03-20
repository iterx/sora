package org.iterx.sora.tool.meta.util.trace;

import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.Value;
import org.iterx.sora.tool.meta.util.InstructionVisitor;
import org.iterx.sora.tool.meta.util.TypeVisitor;
import org.iterx.sora.tool.meta.value.Constant;

import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;

public class TracerTypeVisitor extends AbstractTracer<TracerTypeVisitor> implements TypeVisitor {

    private final Deque<Type> stack;
    private final boolean traceInstructions;
    private int indent;

    public TracerTypeVisitor(final OutputStream outputStream) {
        this(outputStream, true);
    }

    public TracerTypeVisitor(final OutputStream outputStream, final boolean traceInstructions) {
        super(outputStream);
        this.stack = new ArrayDeque<Type>();
        this.indent = 4;
        this.traceInstructions = traceInstructions;
    }

    public void setIndent(final int indent) {
        this.indent = indent;
    }


    public void startClass(final Access access,
                           final Modifier[] modifiers,
                           final Type<?> type,
                           final Type<?> superType,
                           final Type<?>[] interfaceTypes) {
        print(access, modifiers).print(" ").print(type);
        if(superType != Type.OBJECT_TYPE) print(" extends ").print(superType);
        if(interfaceTypes.length != 0) print(" implements ").print(interfaceTypes);
        print(" {").newline().newline();
        stack.push(type);
    }

    public void startInterface(final Access access,
                               final Modifier[] modifiers,
                               final Type<?> type,
                               final Type<?>[] interfaceTypes) {
        print(access, modifiers).print(" ").print(type);
        if(interfaceTypes.length != 0) print(" extends ").print(interfaceTypes);
        print(" {").newline().newline();
        stack.push(type);
    }

    public void field(final Access access,
                      final Modifier[] modifiers,
                      final String fieldName,
                      final Type<?> fieldType,
                      final Constant fieldValue) {
        print(access, modifiers).print(" ").print(fieldType).print(" ").print(fieldName);
        if(fieldValue != Value.VOID) print(" = ").print(fieldValue);
        print(";").newline();
    }

    public InstructionVisitor startConstructor(final Access access,
                                               final Modifier[] modifiers,
                                               final Type<?>[] constructorTypes,
                                               final Type<?>[] exceptionTypes) {
        newline();
        print(access, modifiers).print(" ").print(Type.VOID_TYPE).print(" <init>").print("(").print(constructorTypes).print(")");
        if(exceptionTypes.length != 0) print(" throws ").print(exceptionTypes);
        print(";").newline();
        return (traceInstructions)? new TracerInstructionVisitor(getOutputStream(), indent + getIndent()) : null;
    }

    public void endConstructor() {
    }

    public InstructionVisitor startMethod(final Access access,
                                          final Modifier[] modifiers,
                                          final String methodName,
                                          final Type<?> returnType,
                                          final Type<?>[] argumentTypes,
                                          final Type<?>[] exceptionTypes) {
        newline();
        print(access, modifiers).print(" ").print(returnType).print(" ").print(methodName).print("(").print(argumentTypes).print(")");
        if(exceptionTypes.length != 0) print(" throws ").print(exceptionTypes);
        print(";").newline();
        return (traceInstructions)? new TracerInstructionVisitor(getOutputStream(), indent + getIndent()) : null;
    }


    public void endMethod() {
    }


    public void endClass() {
        stack.pop();
        newline().print("}").newline();
    }

    public void endInterface() {
        stack.pop();
        newline().print("}").newline();
    }

    protected int getIndent() {
        return indent * stack.size();
    }

    private TracerTypeVisitor print(final TypeVisitor.Access access, final TypeVisitor.Modifier... modifiers) {
        final boolean hasAccess = (access != TypeVisitor.Access.DEFAULT);
        if(hasAccess) print(access.name().toLowerCase());
        if(modifiers.length > 0) {
            if(hasAccess) print(" ");
            print(modifiers[0].name().toLowerCase());
            for(int i = 1, length = modifiers.length; i < length; i++)  print(" ").print(modifiers[i].name().toLowerCase());
        }
        return this;
    }

    private TracerTypeVisitor print(final Value... values) {
        if(values.length > 0) {
            print(values[0].toString());
            for(int i = 1, length = values.length; i < length; i++) print(", ").print(values[i].toString());
        }
        return this;
    }

    private TracerTypeVisitor print(final Type... types) {
        if(types.length > 0) {
            print(types[0].getName());
            for(int i = 1, length = types.length; i < length; i++) print(", ").print(types[i].getName());
        }
        return this;
    }
}
