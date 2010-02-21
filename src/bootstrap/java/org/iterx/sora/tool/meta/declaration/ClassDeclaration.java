package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.tool.meta.support.asm.AsmCompiler;
import org.iterx.sora.tool.meta.Types;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Set;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.V1_7;

public class ClassDeclaration extends AbstractDeclaration<ClassDeclaration> implements Types {

    private final Declarations declarations;

    private final Type type;
    private final Type superType;
    private final Type[] interfaceTypes;

    public ClassDeclaration(final Type type, final Type superType, final Type... interfaceTypes) {
        super(ACC_PUBLIC|ACC_PROTECTED|ACC_PRIVATE|ACC_FINAL|ACC_SUPER, ACC_PUBLIC|ACC_FINAL|ACC_SUPER);
        this.declarations = new Declarations();
        this.type = type;
        this.superType = superType;
        this.interfaceTypes = interfaceTypes;
    }

    public Type getType() {
        return type;
    }

    public Type getSuperType() {
        return superType;
    }

    public Type[] getInterfaceTypes() {
        return interfaceTypes;
    }


    public Set<FieldDeclaration> getFieldDeclarations() {
        return declarations.fieldDeclarations;
    }

    public FieldDeclaration getFieldDeclaration(final String fieldName) {
        for(final FieldDeclaration fieldDeclaration : declarations.fieldDeclarations) {
            if(fieldDeclaration.getFieldName().equals(fieldName)) return fieldDeclaration;
        }
        throw new RuntimeException(new NoSuchFieldException());
    }

    public Set<ConstructorDeclaration> getConstructorDeclarations() {
        return declarations.constructorDeclarations;
    }

    public ConstructorDeclaration getConstructorDeclaration(final Type... constructorTypes) {
        for(final ConstructorDeclaration constructorDeclaration : declarations.constructorDeclarations) {
            if(Arrays.equals(constructorDeclaration.getConstructorTypes(), constructorTypes)) return constructorDeclaration;
        }
        throw new RuntimeException(new NoSuchMethodException());
    }


    public Set<MethodDeclaration> getMethodDeclarations() {
        return declarations.methodDeclarations;
    }

    public MethodDeclaration getMethodDeclaration(final String methodName, final Type... argumentTypes) {
        for(final MethodDeclaration methodDeclaration : declarations.methodDeclarations) {
            if(methodDeclaration.getMethodName().equals(methodName) &&
               Arrays.equals(methodDeclaration.getArgumentTypes(), argumentTypes)) return methodDeclaration;
        }
        throw new RuntimeException(new NoSuchMethodException());
    }

    public ClassDeclaration declare(final Declarations declarations) {
        this.declarations.union(declarations);
        return this;
    }

    public static class ClassDeclarationAsmCompiler extends AsmCompiler<ClassVisitor, AsmCompiler.DeclarationContext<ClassDeclaration>, ClassDeclaration> {

        public ClassDeclarationAsmCompiler() {
            super(ClassVisitor.class, ClassDeclaration.class);
        }

        public void compile(final ClassVisitor classVisitor, final DeclarationContext<ClassDeclaration> context) {
            final ClassDeclaration classDeclaration = context.getDeclaration();
            classVisitor.visit(V1_7,
                              classDeclaration.getAccess(),
                              toInternalName(classDeclaration.getType()),
                              null,
                              toInternalName(classDeclaration.getSuperType()),
                              toInternalNames(classDeclaration.getInterfaceTypes()));
            compileAll(classVisitor, context, classDeclaration.getFieldDeclarations());
            compileAll(classVisitor, context, classDeclaration.getConstructorDeclarations());
            compileAll(classVisitor, context, classDeclaration.getMethodDeclarations());
            classVisitor.visitEnd();
        }


        private <T extends Declaration> void compileAll(final ClassVisitor classVisitor,
                                                        final DeclarationContext<ClassDeclaration> context,
                                                        final Set<T> declarations) {
            for(final T declaration : declarations) compile(classVisitor, context, declaration);
        }

        private static String toInternalName(final Type type) {
            return type.getInternalName();
        }

        private static String[] toInternalNames(final Type... types) {
            if(types != null) {
                final String[] internalNames = new String[types.length];
                for(int i = internalNames.length; i-- != 0;) internalNames[i] = types[i].getInternalName();
                return internalNames;
            }
            return null;
        }
    }
}
