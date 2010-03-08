package org.iterx.sora.tool.meta.support.asm;

import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.Instruction;
import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.Value;
import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;
import org.iterx.sora.tool.meta.instruction.GetFieldInstruction;
import org.iterx.sora.tool.meta.instruction.InvokeSuperInstruction;
import org.iterx.sora.tool.meta.instruction.PutFieldInstruction;
import org.iterx.sora.tool.meta.instruction.ReturnValueInstruction;
import org.iterx.sora.tool.meta.instruction.StoreInstruction;
import org.iterx.sora.tool.meta.util.DeclarationReader;
import org.iterx.sora.tool.meta.util.DeclarationVisitor;
import org.iterx.sora.tool.meta.util.InstructionReader;
import org.iterx.sora.tool.meta.util.InstructionVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;

public final class AsmCompiler {

    private final MetaClassLoader metaClassLoader;

    public AsmCompiler(final MetaClassLoader metaClassLoader) {
        this.metaClassLoader = metaClassLoader;
    }

    public byte[] compile(final Declaration<?> declaration) {
        final DeclarationReader declarationReader = new DeclarationReader(declaration);
        final CompilerDeclarationVisitor compilerDeclarationVisitor = new CompilerDeclarationVisitor();

        declarationReader.accept(compilerDeclarationVisitor);
        return compilerDeclarationVisitor.getBytes();
    }

    //TODO: separate out interface & class visitors
    private static class CompilerDeclarationVisitor implements DeclarationVisitor, Opcodes {

        private final ClassWriter classWriter;
        private ClassDeclaration classDeclaration;

        private CompilerDeclarationVisitor() {
            this.classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        }

        public byte[] getBytes() {
            return classWriter.toByteArray();
        }

        public void startClass(final ClassDeclaration classDeclaration) {
            this.classDeclaration = classDeclaration;
            classWriter.visit(V1_7,
                              toAccess(classDeclaration.getAccess()) | toModifier(classDeclaration.getModifiers()),
                              toName(classDeclaration.getClassType()),
                              null,
                              toName(classDeclaration.getSuperType()),
                              toNames(classDeclaration.getInterfaceTypes()));
        }

        public void startInterface(final InterfaceDeclaration interfaceDeclaration) {
            classWriter.visit(V1_7,
                              ACC_INTERFACE | toAccess(interfaceDeclaration.getAccess()) | toModifier(interfaceDeclaration.getModifiers()),
                              toName(interfaceDeclaration.getInterfaceType()),
                              null,
                              toName(Type.OBJECT_TYPE),
                              toNames(interfaceDeclaration.getInterfaceTypes()));
        }

        public void field(final FieldDeclaration fieldDeclaration) {
            final FieldVisitor fieldVisitor =  classWriter.visitField(toAccess(fieldDeclaration.getAccess()),
                                                                      fieldDeclaration.getFieldName(),
                                                                      toDescriptor(fieldDeclaration.getFieldType()),
                                                                      null,
                                                                      null);
            fieldVisitor.visitEnd();
        }

        public void constructor(final ConstructorDeclaration constructorDeclaration) {
            final MethodVisitor methodVisitor = classWriter.visitMethod(toAccess(constructorDeclaration.getAccess()) | toModifier(constructorDeclaration.getModifiers()),
                                                                        "<init>",
                                                                        toMethodDescriptor(Type.VOID_TYPE, constructorDeclaration.getConstructorTypes()),
                                                                        null,
                                                                        null);
            methodVisitor.visitCode();
            new InstructionReader(constructorDeclaration.getInstructions()).accept(new CompilerInstructionVisitor(classDeclaration, constructorDeclaration, methodVisitor));
            //TODO: remove - should always force instruction....
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(0, 0);
            methodVisitor.visitEnd();
        }

        public void method(final MethodDeclaration methodDeclaration) {
            final MethodVisitor methodVisitor = classWriter.visitMethod(toAccess(methodDeclaration.getAccess()) | toModifier(methodDeclaration.getModifiers()),
                                                                        methodDeclaration.getMethodName(),
                                                                        toMethodDescriptor(methodDeclaration.getReturnType(), methodDeclaration.getArgumentTypes()),
                                                                        null,
                                                                        null);
            methodVisitor.visitCode();
            new InstructionReader(methodDeclaration.getInstructions()).accept(new CompilerInstructionVisitor(classDeclaration, methodDeclaration, methodVisitor));
            //TODO: remove - should always force instruction....
            if(methodDeclaration.getReturnType() == Type.VOID_TYPE) methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(0, 0);
            methodVisitor.visitEnd();
        }

        public void endClass() {
            this.classDeclaration = null;
            classWriter.visitEnd();
        }

        public void endInterface() {
            classWriter.visitEnd();
        }
    }

    private static class CompilerInstructionVisitor implements InstructionVisitor, Opcodes {

        private final ClassDeclaration classDeclaration;
        private final MethodDeclaration methodDeclaration;
        private final ConstructorDeclaration constructorDeclaration;
        private final MethodVisitor methodVisitor;
        private final AsmScope asmScope;

        private CompilerInstructionVisitor(final ClassDeclaration classDeclaration,
                                           final ConstructorDeclaration constructorDeclaration,
                                           final MethodVisitor methodVisitor) {
            this.asmScope = newAsmStack(constructorDeclaration.getConstructorTypes());
            this.classDeclaration = classDeclaration;
            this.methodDeclaration = null;
            this.constructorDeclaration = constructorDeclaration;
            this.methodVisitor = methodVisitor;
        }

        private CompilerInstructionVisitor(final ClassDeclaration classDeclaration,
                                           final MethodDeclaration methodDeclaration,
                                           final MethodVisitor methodVisitor) {
            this.asmScope = newAsmStack(methodDeclaration.getArgumentTypes());
            this.classDeclaration = classDeclaration;
            this.methodDeclaration = methodDeclaration;
            this.constructorDeclaration = null;
            this.methodVisitor = methodVisitor;
        }

        public void getField(final GetFieldInstruction getFieldInstruction) {
            final FieldDeclaration fieldDeclaration = classDeclaration.getFieldDeclaration(getFieldInstruction.getFieldName());

            loadValues(methodVisitor, asmScope);
            methodVisitor.visitFieldInsn(GETFIELD,
                                         toName(classDeclaration.getClassType()),
                                         fieldDeclaration.getFieldName(),
                                         toDescriptor(fieldDeclaration.getFieldType()));
        }

        public void putField(final PutFieldInstruction putFieldInstruction) {
            final FieldDeclaration fieldDeclaration = classDeclaration.getFieldDeclaration(putFieldInstruction.getFieldName());
            loadValues(methodVisitor, asmScope, putFieldInstruction.getValue());
            methodVisitor.visitFieldInsn(PUTFIELD,
                                         toName(classDeclaration.getClassType()),
                                         fieldDeclaration.getFieldName(),
                                         toDescriptor(fieldDeclaration.getFieldType()));
        }

        public void store(final StoreInstruction storeInstruction) {
            final Value value = storeInstruction.getValue();
            final String valueName = value.getName();
            final Type valueType = value.getType();

            instructions(storeInstruction.getInstruction());
            //TODO: abstract out as store values???
            asmScope.push(valueName, valueType);
            methodVisitor.visitVarInsn(toType(valueType).getOpcode(ISTORE),
                                       asmScope.getIndex(valueName));
        }

        public void invokeSuper(final InvokeSuperInstruction invokeSuperInstruction) {
            loadValues(methodVisitor, asmScope, invokeSuperInstruction.getValues());
            methodVisitor.visitMethodInsn(INVOKESPECIAL,
                                          toName(classDeclaration.getSuperType()),
                                          "<init>",
                                          toMethodDescriptor(Type.VOID_TYPE, constructorDeclaration.getConstructorTypes()));
        }

        public void returnValue(final ReturnValueInstruction returnValueInstruction) {
            if(returnValueInstruction.getInstruction() != null) instructions(returnValueInstruction.getInstruction());
            if(returnValueInstruction.getValue() != null) loadValues(methodVisitor, asmScope, returnValueInstruction.getValue());
            methodVisitor.visitInsn(toType(methodDeclaration.getReturnType()).getOpcode(IRETURN));
        }

        private void instructions(final Instruction... instructions) {
            new InstructionReader(instructions).accept(this);
        }

        private static void loadValues(final MethodVisitor methodVisitor, final AsmScope asmScope, final Value... values) {
            methodVisitor.visitVarInsn(ALOAD, 0);
            for(final Value value : values) {
                final String valueName = value.getName();
                methodVisitor.visitVarInsn(toType(asmScope.getType(valueName)).getOpcode(ILOAD),
                                           asmScope.getIndex(valueName));
            }
        }


        private static AsmScope newAsmStack(final Type<?>[] argumentTypes) {
            final AsmScope asmScope = new AsmScope();

            asmScope.push("this", Type.OBJECT_TYPE);
            for(int i = 0, size = argumentTypes.length; i != size; i++) asmScope.push("arg" + i, argumentTypes[i]);
            return asmScope;
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

    public static String toDescriptor(final Type type) {
        return toType(type).getDescriptor();
    }

    private static String toMethodDescriptor(final Type type, final Type... types) {
        final org.objectweb.asm.Type returnType = toType(type);
        if(types != null) {
            final org.objectweb.asm.Type[] argumentTypes = new org.objectweb.asm.Type[types.length];
            for(int i = argumentTypes.length; i-- != 0;) argumentTypes[i] = toType(types[i]);
            return org.objectweb.asm.Type.getMethodDescriptor(returnType, argumentTypes);
        }
        return org.objectweb.asm.Type.getMethodDescriptor(returnType, new org.objectweb.asm.Type[0]);
    }

    private static String toName(final Type type) {
        return toType(type).getInternalName();
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

    private static org.objectweb.asm.Type toType(final Type type) {
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


