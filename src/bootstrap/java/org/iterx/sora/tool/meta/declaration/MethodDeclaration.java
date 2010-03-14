package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.tool.meta.AbstractDeclaration;
import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.Instruction;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.Instructions;
import org.iterx.sora.tool.meta.type.ClassType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MethodDeclaration extends AbstractDeclaration<MethodDeclaration> {

    public static final Type<?>[] EMPTY_ARGUMENT_TYPES = new Type[0];
    public static final Type<ClassType>[] EMPTY_EXCEPTION_TYPES = new ClassType[0];
    public static final Modifier[] EMPTY_MODIFIERS = new Modifier[0];

    public enum Access implements Declaration.Access {  PUBLIC, PROTECTED, PRIVATE, DEFAULT }
    public enum Modifier implements Declaration.Modifier { ABSTRACT, FINAL }

    private final List<Instruction<?>> instructions;
    private final String methodName;
    private final Type<?>[] argumentTypes;
    private Type<ClassType>[] exceptionTypes;
    private Type<?> returnType;
    private Access access;
    private Modifier[] modifiers;

    private MethodDeclaration(final String methodName,
                              final Type<?>... argumentTypes) {
        this.instructions = new ArrayList<Instruction<?>>();
        this.returnType = Type.VOID_TYPE;
        this.exceptionTypes = EMPTY_EXCEPTION_TYPES;
        this.access = Access.PUBLIC;
        this.modifiers = EMPTY_MODIFIERS;
        this.methodName = methodName;
        this.argumentTypes = argumentTypes;
    }

    public static MethodDeclaration newMethodDeclaration(final String methodName, final Type<?>... argumentTypes) {
        assertMethodName(methodName);
        assertType(argumentTypes);
        return new MethodDeclaration(methodName, argumentTypes);
    }

    @Override
    public boolean isMethodDeclaration() {
        return true;
    }

    public String getMethodName() {
        return methodName;
    }

    public Type<?>[] getArgumentTypes() {
        return argumentTypes;
    }

    public Type<?> getReturnType() {
        return returnType;
    }

    public MethodDeclaration setReturnType(final Type<?> returnType) {
        assertType(returnType);
        this.returnType = returnType;
        return this;
    }
    public Type<ClassType>[] getExceptionTypes() {
        return exceptionTypes;
    }

    public MethodDeclaration setExceptionTypes(final Type<ClassType>... exceptionTypes) {
        assertType(exceptionTypes);
        this.exceptionTypes = exceptionTypes;
        return this;
    }
    
    public Access getAccess() {
        return access;
    }

    public MethodDeclaration setAccess(final Access access) {
        assertAccess(access);
        this.access = access;
        return this;
    }

    public Modifier[] getModifiers() {
        return modifiers;
    }

    public MethodDeclaration setModifiers(final Modifier... modifiers) {
        assertModifiers(modifiers);
        this.modifiers = modifiers;
        return this;
    }

    public Instruction<?>[] getInstructions() {
        return instructions.toArray(new Instruction<?>[instructions.size()]);
    }

    public MethodDeclaration add(final Instructions instructions) {
        for(final Instruction instruction : instructions.getInstructions()) this.instructions.add(instruction);
        return this;
    }

    public MethodDeclaration add(final Instruction<?> instruction) {
        this.instructions.add(instruction);
        return this;
        }

    public MethodDeclaration remove(final Instruction<?> instruction) {
        this.instructions.remove(instruction);
        return this;
    }

    @Override
    public int hashCode() {
        return 31 * methodName.hashCode() +  Arrays.hashCode(argumentTypes);

    }

    @Override
    public boolean equals(final Object object) {
        return (this ==  object) ||
               (object != null && object.getClass() == getClass() &&
                methodName.equals(((MethodDeclaration) object).methodName) &&
                Arrays.equals(argumentTypes, ((MethodDeclaration) object).argumentTypes));
    }

    @Override
    public String toString() {
        return new StringBuilder().
                append("MethodDeclaration: ").
                append(methodName).
                append(Arrays.toString(argumentTypes)).
                toString();
    }


    private static void assertMethodName(final String methodName) {
        if(methodName == null) throw new IllegalArgumentException("methodName == null");
    }

    private static void assertType(final Type<?>... types) {
        if(types == null) throw new IllegalArgumentException("type == null");
        for(Type<?> type : types) if(type == null) throw new IllegalArgumentException("type == null");
    }

    private static void assertAccess(final Access access) {
        if(access == null) throw new IllegalArgumentException("access == null");
    }

    private static void assertModifiers(final Modifier... modifiers) {
        if(modifiers == null) throw new IllegalArgumentException("modifiers == null");
    }
}
