package org.iterx.sora.tool.meta.support.asm;

import org.iterx.sora.tool.meta.type.Type;
import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;
import org.iterx.sora.tool.meta.declaration.Declaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;
import org.iterx.sora.tool.meta.util.DeclarationReader;
import org.iterx.sora.tool.meta.util.DeclarationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Field;

import static org.objectweb.asm.Opcodes.V1_7;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.RETURN;

public final class AsmCompiler {

    private AsmCompiler() {}

    public static byte[] compile(final Declaration<?> declaration) {
        final DeclarationReader declarationReader = new DeclarationReader(declaration);
        final CompilerDeclarationVisitor compilerDeclarationVisitor = new CompilerDeclarationVisitor();

        declarationReader.accept(compilerDeclarationVisitor);
        return compilerDeclarationVisitor.getBytes();
    }

    private static class CompilerDeclarationVisitor implements DeclarationVisitor {

        private final ClassWriter classWriter;

        private CompilerDeclarationVisitor() {
            this.classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        }

        public byte[] getBytes() {
            return classWriter.toByteArray();
        }

        public void startClassDeclaration(final ClassDeclaration classDeclaration) {
            classWriter.visit(V1_7,
                              toAccess(classDeclaration.getAccess()) | toModifier(classDeclaration.getModifiers()),
                              toName(classDeclaration.getType()),
                              null,
                              toName(classDeclaration.getSuperType()),
                              toNames(classDeclaration.getInterfaceTypes()));
        }

        public void startInterfaceDeclaration(final InterfaceDeclaration interfaceDeclaration) {
            classWriter.visit(V1_7,
                              ACC_INTERFACE | toAccess(interfaceDeclaration.getAccess()) | toModifier(interfaceDeclaration.getModifiers()),
                              toName(interfaceDeclaration.getType()),
                              null,
                              toName(Type.OBJECT_TYPE),
                              toNames(interfaceDeclaration.getInterfaceTypes()));
        }

        public void fieldDeclaration(final FieldDeclaration fieldDeclaration) {

        }

        public void constructorDeclaration(final ConstructorDeclaration constructorDeclaration) {
            final MethodVisitor methodVisitor = classWriter.visitMethod(toAccess(constructorDeclaration.getAccess()) | toModifier(constructorDeclaration.getModifiers()),
                                                                        "<init>",
                                                                        toMethodDescription(Type.VOID_TYPE, constructorDeclaration.getConstructorTypes()),
                                                                        null,
                                                                        null);
            methodVisitor.visitCode();
            //compile(methodVisitor, context, constructorDeclaration.getStatements());
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(0, 0);
            methodVisitor.visitEnd();
        }

        public void methodDeclaration(final MethodDeclaration methodDeclaration) {
            final MethodVisitor methodVisitor = classWriter.visitMethod(toAccess(methodDeclaration.getAccess()) | toModifier(methodDeclaration.getModifiers()),
                                                                        methodDeclaration.getMethodName(),
                                                                        toMethodDescription(methodDeclaration.getReturnType(), methodDeclaration.getArgumentTypes()),
                                                                        null,
                                                                        null);
            methodVisitor.visitCode();
            // compile(methodVisitor, context, methodDeclaration.getStatements());
            if(methodDeclaration.getReturnType() == Type.VOID_TYPE) methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(0, 0);
            methodVisitor.visitEnd();
        }

        public void endClassDeclaration() {
            classWriter.visitEnd();
        }

        public void endInterfaceDeclaration() {
            classWriter.visitEnd();
        }
    }

    private static int toAccess(final Declaration.Access access) {
        return getAsmOpcode("ACC_" + access.name());
    }

    private static int toModifier(final Declaration.Modifier... modifiers) {
        int access = 0;
        for(final Declaration.Modifier modifier : modifiers) access |= getAsmOpcode("ACC_" + modifier.name());
        return access;
    }

    private static String toMethodDescription(final Type type, final Type... types) {
        final org.objectweb.asm.Type returnType = getAsmType(type);
        if(types != null) {
            final org.objectweb.asm.Type[] argumentTypes = new org.objectweb.asm.Type[types.length];
            for(int i = argumentTypes.length; i-- != 0;) argumentTypes[i] = getAsmType(types[i]);
            return org.objectweb.asm.Type.getMethodDescriptor(returnType, argumentTypes);
        }
        return org.objectweb.asm.Type.getMethodDescriptor(returnType, new org.objectweb.asm.Type[0]);
    }

    private static String toName(final Type type) {
        return getAsmType(type).getInternalName();
    }

    private static String[] toNames(final Type... types) {
        if(types != null) {
            final String[] names = new String[types.length];
            for(int i = names.length; i-- != 0;) names[i] = toName(types[i]);
            return names;
        }
        return null;
    }

    private static int getAsmOpcode(final String name) {
        try {
            final Field field = org.objectweb.asm.Opcodes.class.getField(name);
            return (Integer) field.get(org.objectweb.asm.Opcodes.class);
        }
        catch(final Exception e) {
            return 0;
        }
    }

    private static org.objectweb.asm.Type getAsmType(final Type type) {
        final String name = type.getName();
        if(name.equals("void")) return org.objectweb.asm.Type.VOID_TYPE;
        else if(name.equals("boolean")) return org.objectweb.asm.Type.BOOLEAN_TYPE;
        else if(name.equals("byte")) return org.objectweb.asm.Type.BYTE_TYPE;
        else if(name.equals("char")) return org.objectweb.asm.Type.CHAR_TYPE;
        else if(name.equals("short")) return org.objectweb.asm.Type.SHORT_TYPE;
        else if(name.equals("int")) return org.objectweb.asm.Type.INT_TYPE;
        else if(name.equals("long")) return org.objectweb.asm.Type.LONG_TYPE;
        else if(name.equals("float")) return org.objectweb.asm.Type.FLOAT_TYPE;
        else if(name.equals("double")) return org.objectweb.asm.Type.DOUBLE_TYPE;
        else return org.objectweb.asm.Type.getObjectType(name.replace('.', '/'));
    }   
}


