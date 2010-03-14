package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.tool.meta.AbstractDeclaration;
import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.Instruction;
import org.iterx.sora.tool.meta.Instructions;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.type.ClassType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ConstructorDeclaration extends AbstractDeclaration<ConstructorDeclaration> {

    public static final Type<ClassType>[] EMPTY_EXCEPTION_TYPES = new ClassType[0];
    public static final Type<?>[] EMPTY_CONSTRUCTOR_TYPES = new Type[0];
    public static final Modifier[] EMPTY_MODIFIERS = new Modifier[0];

    public enum Access implements Declaration.Access {  PUBLIC, PROTECTED, PRIVATE, DEFAULT }
    public enum Modifier implements Declaration.Modifier { ABSTRACT, FINAL }

    private final List<Instruction<?>> instructions;
    private final Type<?>[] constructorTypes;
    private Type<ClassType>[] exceptionTypes;
    private Access access;
    private Modifier[] modifiers;

    private ConstructorDeclaration(final Type... constructorTypes) {
        this.instructions = new ArrayList<Instruction<?>>();
        this.access = Access.PUBLIC;
        this.modifiers = EMPTY_MODIFIERS;
        this.exceptionTypes = EMPTY_EXCEPTION_TYPES;
        this.constructorTypes = constructorTypes;
    }

    public static ConstructorDeclaration newConstructorDeclaration(final Type<?>... constructorTypes) {
        assertType(constructorTypes);
        return new ConstructorDeclaration(constructorTypes);
    }

    @Override
    public boolean isConstructorDeclaration() {
        return true;
    }

    public Type<?>[] getConstructorTypes() {
        return constructorTypes;
    }

    public Type<ClassType>[] getExceptionTypes() {
        return exceptionTypes;
    }

    public ConstructorDeclaration setExceptionTypes(final Type<ClassType>... exceptionTypes) {
        assertType(exceptionTypes);
        this.exceptionTypes = exceptionTypes;
        return this;
    }

    public Access getAccess() {
        return access;
    }

    public ConstructorDeclaration setAccess(final Access access) {
        assertAccess(access);
        this.access = access;
        return this;
    }

    public Modifier[] getModifiers() {
        return modifiers;
    }

    public ConstructorDeclaration setModifiers(final Modifier... modifiers) {
        assertModifiers(modifiers);
        this.modifiers = modifiers;
        return this;
    }

    public Instruction<?>[] getInstructions() {
        return instructions.toArray(new Instruction<?>[instructions.size()]);
    }

    public ConstructorDeclaration add(final Instructions instructions) {
        for(final Instruction instruction : instructions.getInstructions()) this.instructions.add(instruction);
        return this;
    }

    public ConstructorDeclaration add(final Instruction<?> instruction) {
        this.instructions.add(instruction);
        return this;
    }

    public ConstructorDeclaration remove(final Instruction<?> instruction) {
        this.instructions.remove(instruction);
        return this;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(constructorTypes);
    }

    @Override
    public boolean equals(final Object object) {
        return (this ==  object) ||
               (object != null && object.getClass() == getClass() && Arrays.equals(constructorTypes, ((ConstructorDeclaration) object).constructorTypes));
    }

    @Override
    public String toString() {
        return new StringBuilder().
                append("ConstructorDeclaration: ").
                append(Arrays.toString(constructorTypes)).
                toString();
    }


    private static void assertType(final Type<?>... types) {
        if(types == null) throw new IllegalArgumentException("type == null");
        for(Type type : types) if(type == null) throw new IllegalArgumentException("type == null");
    }

    private static void assertAccess(final Access access) {
        if(access == null) throw new IllegalArgumentException("access == null");
    }

    private static void assertModifiers(final Modifier... modifiers) {
        if(modifiers == null) throw new IllegalArgumentException("modifiers == null");
    }
}