package org.iterx.sora.tool.meta.support.asm;

import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.TypeDeclaration;
import org.iterx.sora.tool.meta.Value;
import org.iterx.sora.tool.meta.ValueInstruction;
import org.iterx.sora.tool.meta.support.asm.scope.ClassScope;
import org.iterx.sora.tool.meta.support.asm.scope.StackScope;
import org.iterx.sora.tool.meta.util.InstructionReader;
import org.iterx.sora.tool.meta.util.InstructionVisitor;
import org.iterx.sora.tool.meta.util.TypeReader;
import org.iterx.sora.tool.meta.util.TypeVisitor;
import org.iterx.sora.tool.meta.value.Constant;
import org.iterx.sora.tool.meta.value.Variable;
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

    public byte[] compile(final TypeDeclaration<?, ?> typeDeclaration) {
        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        new TypeReader(typeDeclaration).accept(new CompilerTypeVisitor(classWriter));
        return classWriter.toByteArray();
    }

    private static class CompilerTypeVisitor implements TypeVisitor, Opcodes {

        private final ClassWriter classWriter;
        private MethodVisitor methodVisitor;
        private ClassScope classScope;

        private CompilerTypeVisitor(final ClassWriter classWriter) {
            this.classWriter = classWriter;
        }

        public void startClass(final Access access,
                               final Modifier[] modifiers,
                               final Type<?> type,
                               final Type<?> superType,
                               final Type<?>[] interfaceTypes) {
            classWriter.visit(V1_7,
                              toAccess(access)|toModifier(modifiers),
                              toName(type),
                              null,
                              toName(superType),
                              toNames(interfaceTypes));
            classScope = new ClassScope(superType, type);
/*
           if(classTypeDeclaration.getOuterType() != Type.VOID_TYPE) {
               classWriter.visitOuterClass(toName(classTypeDeclaration.getOuterType()),
                                           null,
                                           null);
           }
           for(final Type<?> innerType : classTypeDeclaration.getInnerTypes()) {
               classWriter.visitInnerClass(toName(classTypeDeclaration.getClassType()),
                                           toName(innerType),
                                           toName(classTypeDeclaration.getClassType()), //TODO: make this short name
                                           toAccess(classTypeDeclaration.getAccess()));
           }
*/

        }

        public void startInterface(final Access access,
                                   final Modifier[] modifiers,
                                   final Type<?> type,
                                   final Type<?>[] interfaceTypes) {
            classWriter.visit(V1_7,
                              ACC_INTERFACE|toAccess(access)|toModifier(modifiers),
                              toName(type),
                              null,
                              toName(Type.OBJECT_TYPE),
                              toNames(interfaceTypes));
            classScope = new ClassScope(Type.OBJECT_TYPE, type);
        }

        public void field(final Access access,
                          final Modifier[] modifiers,
                          final String fieldName,
                          final Type<?> fieldType,
                          final Constant fieldValue) {
            final FieldVisitor fieldVisitor = classWriter.visitField(toAccess(access)|toModifier(modifiers),
                                                                     fieldName,
                                                                     toDescriptor(fieldType),
                                                                     null,
                                                                     fieldValue.getValue());
            fieldVisitor.visitEnd();
        }

        public InstructionVisitor startConstructor(final Access access,
                                                   final Modifier[] modifiers,
                                                   final Type<?>[] constructorTypes,
                                                   final Type<?>[] exceptionTypes) {
            methodVisitor = classWriter.visitMethod(toAccess(access)|toModifier(modifiers),
                                                    "<init>",
                                                    toMethodDescriptor(Type.VOID_TYPE, constructorTypes),
                                                    null,
                                                    toNames(exceptionTypes));

            methodVisitor.visitCode();
            return new CompilerInstructionVisitor(methodVisitor, newStackScope(constructorTypes));
        }

        public void endConstructor() {
            methodVisitor.visitMaxs(0, 0);
            methodVisitor.visitEnd();
            methodVisitor = null;
        }

        public InstructionVisitor startMethod(final Access access,
                                              final Modifier[] modifiers,
                                              final String methodName,
                                              final Type<?> returnType,
                                              final Type<?>[] argumentTypes,
                                              final Type<?>[] exceptionTypes) {
            methodVisitor = classWriter.visitMethod(toAccess(access)|toModifier(modifiers),
                                                    methodName,
                                                    toMethodDescriptor(returnType, argumentTypes),
                                                    null,
                                                    toNames(exceptionTypes));
            methodVisitor.visitCode();
            return new CompilerInstructionVisitor(methodVisitor, newStackScope(argumentTypes));
        }

        public void endMethod() {
            methodVisitor.visitMaxs(0, 0);
            methodVisitor.visitEnd();
            methodVisitor = null;
        }

        public void endClass() {
            classWriter.visitEnd();
        }

        public void endInterface() {
            classWriter.visitEnd();
        }

        private StackScope newStackScope(final Type<?>[] argumentTypes) {
            final StackScope stackScope = new StackScope(classScope);
            stackScope.push(Variable.THIS);
            for(int i = 0, size = argumentTypes.length; i != size; i++) stackScope.push(Variable.newVariable("arg" + i, argumentTypes[i]));
            return stackScope;
        }
    }

    private static class CompilerInstructionVisitor implements InstructionVisitor, Opcodes {

        private final MethodVisitor methodVisitor;
        private final StackScope stackScope;

        private CompilerInstructionVisitor(final MethodVisitor methodVisitor, final StackScope stackScope) {
            this.methodVisitor = methodVisitor;
            this.stackScope = stackScope;
        }

        public void invokeSuper(final Type<?> target,
                                final String methodName,
                                final Type<?> returnType,
                                final Value<?>[] values) {
            final Type<?>[] argumentTypes = getTypes(stackScope, values);
            loadValues(methodVisitor, stackScope, Variable.THIS);
            loadValues(methodVisitor, stackScope, values);
            methodVisitor.visitMethodInsn(INVOKESPECIAL,
                                          toName(target),
                                          methodName,
                                          toMethodDescriptor(returnType, argumentTypes));
        }

        public void invokeMethod(final Type<?> target,
                                 final String methodName,
                                 final Type<?> returnType,
                                 final Value<?>[] values) {
            final Type<?>[] argumentTypes = getTypes(stackScope, values);
            loadValues(methodVisitor, stackScope, Variable.THIS);
            loadValues(methodVisitor, stackScope, values);
            //TODO: need to resolve if target is private -> i.e INVOKESPECIAL
            methodVisitor.visitMethodInsn((target.isInterface())? INVOKEINTERFACE : INVOKEVIRTUAL,
                                          toName(target),
                                          methodName,
                                          toMethodDescriptor(returnType, argumentTypes));
        }

        public void returnValue(final Value<?> value) {
            final Type returnType = value.getType();

            loadValues(methodVisitor, stackScope, value);
            if(Type.VOID_TYPE != returnType) methodVisitor.visitInsn(toType(returnType).getOpcode(IRETURN));
            else methodVisitor.visitInsn(RETURN);
        }

        public void store(final Variable variable, final Value<?> value) {
            loadValues(methodVisitor, stackScope, value);
            methodVisitor.visitVarInsn(toType(variable.getType()).getOpcode(ISTORE),
                                       stackScope.push(variable));

        }

        public void getField(final Variable owner,
                             final String fieldName,
                             final Type<?> fieldType) {
            loadValues(methodVisitor, stackScope, owner);
            methodVisitor.visitFieldInsn(GETFIELD,
                                         toName(getType(stackScope, owner)),
                                         fieldName,
                                         toDescriptor(fieldType));
        }

        public void putField(final Variable owner,
                             final String fieldName,
                             final Type<?> fieldType,
                             final Value<?> value) {
            loadValues(methodVisitor, stackScope, owner);
            loadValues(methodVisitor, stackScope, value);
            methodVisitor.visitFieldInsn(PUTFIELD,
                                         toName(getType(stackScope, owner)),
                                         fieldName,
                                         toDescriptor(fieldType));
        }

        private static Type<?> getType(final StackScope stackScope, final Value value) {
            if(value == Variable.THIS) return stackScope.getClassScope().getThis();
            else if(value == Variable.SUPER) return stackScope.getClassScope().getSuper();
            else return value.getType();
        }

        private static Type<?>[] getTypes(final StackScope stackScope, final Value[] values) {
            final Type<?>[] types = new Type<?>[values.length];
            for(int i = values.length; i-- != 0;) types[i] = getType(stackScope, values[i]);
            return types;
        }

        private static void loadValues(final MethodVisitor methodVisitor, final StackScope stackScope, final Value... values) {
            for(final Value value : values) {
                if(value.isConstant()) loadConstant(methodVisitor, (Constant) value);
                else if(value.isVariable()) loadVariable(methodVisitor, stackScope, (Variable) value);
                else if(value.isInstruction()) loadInstruction(methodVisitor, stackScope, (ValueInstruction) value);
                else throw new IllegalArgumentException();
            }
        }

        private static void loadConstant(final MethodVisitor methodVisitor, final Constant constant) {
            final Type type = constant.getType();
            final Object value = constant.getValue();

            if(type == Type.OBJECT_TYPE) methodVisitor.visitInsn(ACONST_NULL);
            else if(type == Type.INT_TYPE) {
                final int i = ((Number) value).intValue();
                if(i == -1) methodVisitor.visitInsn(ICONST_M1);
                else if(i >= 0 && i <= 5) methodVisitor.visitInsn(getAsmOpcode("ICONST_" + i));
                else if(i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) methodVisitor.visitIntInsn(BIPUSH, i);
                else if(i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) methodVisitor.visitIntInsn(SIPUSH, i);
                else methodVisitor.visitLdcInsn(i);
            }
            else if(type == Type.FLOAT_TYPE) {
                final float f = ((Number) value).floatValue();
                if(f == 0f || f == 1f || f == 2f) methodVisitor.visitInsn(getAsmOpcode("FCONST_" + (int) f));
                else methodVisitor.visitLdcInsn(f);
            }
            else if(type == Type.LONG_TYPE) {
                final long l = ((Number) value).longValue();
                if(l == 0 || l == 1) methodVisitor.visitInsn(getAsmOpcode("LCONST_" + l));
                else methodVisitor.visitLdcInsn(l);
            }
            else if(type == Type.DOUBLE_TYPE) {
                final double d = ((Number) value).doubleValue();
                if(d == 0d || d == 1d) methodVisitor.visitInsn(getAsmOpcode("DCONST_" + (int) d ));
                else methodVisitor.visitLdcInsn(d);
            }
            else if(type != Type.VOID_TYPE) methodVisitor.visitLdcInsn(value);
        }

        private static void loadVariable(final MethodVisitor methodVisitor, final StackScope stackScope, final Variable variable) {
            //TODO: we shouldn't need to re-resolve values...
            final Value value = stackScope.resolveValue(variable.getName());
            methodVisitor.visitVarInsn(toType(value.getType()).getOpcode(ILOAD),
                                       stackScope.getIndex(value));
        }

        private static void loadInstruction(final MethodVisitor methodVisitor, final StackScope stackScope, final ValueInstruction valueInstruction) {
            new InstructionReader(valueInstruction).accept(new CompilerInstructionVisitor(methodVisitor, stackScope));
        }
    }

/*
    private static final class ClassTypeDeclarationCompilerDeclarationVisitor extends CompilerDeclarationVisitor {

        private final ClassTypeDeclaration classTypeDeclaration;
        private final ClassWriter classWriter;

        private ClassTypeDeclarationCompilerDeclarationVisitor(final ClassTypeDeclaration classTypeDeclaration) {
            this.classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            this.classTypeDeclaration = classTypeDeclaration;
            
            classWriter.visit(V1_7,
                              toAccess(classTypeDeclaration.getAccess()) | toModifier(classTypeDeclaration.getModifiers()),
                              toName(classTypeDeclaration.getClassType()),
                              null,
                              toName(classTypeDeclaration.getSuperType()),
                              toNames(classTypeDeclaration.getInterfaceTypes()));

           if(classTypeDeclaration.getOuterType() != Type.VOID_TYPE) {
               classWriter.visitOuterClass(toName(classTypeDeclaration.getOuterType()),
                                           null,
                                           null);
           }
           for(final Type<?> innerType : classTypeDeclaration.getInnerTypes()) {
               classWriter.visitInnerClass(toName(classTypeDeclaration.getClassType()),
                                           toName(innerType),
                                           toName(classTypeDeclaration.getClassType()), //TODO: make this short name
                                           toAccess(classTypeDeclaration.getAccess()));
           }
        }

        @Override
        public byte[] getBytes() {
            return classWriter.toByteArray();
        }

        @Override
        public void startClass(final ClassTypeDeclaration classTypeDeclaration) {
            throw new IllegalStateException();
        }

        @Override
        public void startInterface(final InterfaceTypeDeclaration interfaceTypeDeclaration) {
            throw new IllegalStateException();
        }

        @Override
        public void field(final FieldDeclaration fieldDeclaration) {
            final FieldVisitor fieldVisitor = classWriter.visitField(toAccess(fieldDeclaration.getAccess()),
                                                                          fieldDeclaration.getFieldName(),
                                                                          toDescriptor(fieldDeclaration.getFieldType()),
                                                                          null,
                                                                          fieldDeclaration.getFieldValue());
            fieldVisitor.visitEnd();
        }

        @Override
        public void startConstructor(final ConstructorDeclaration constructorDeclaration) {
            final MethodVisitor methodVisitor = classWriter.visitMethod(toAccess(constructorDeclaration.getAccess()) | toModifier(constructorDeclaration.getModifiers()),
                                                                        "<init>",
                                                                        toMethodDescriptor(Type.VOID_TYPE, constructorDeclaration.getConstructorTypes()),
                                                                        null,
                                                                        null);
            methodVisitor.visitCode();
            instructions(constructorDeclaration, methodVisitor);
            methodVisitor.visitMaxs(0, 0);
            methodVisitor.visitEnd();
        }

        @Override
        public void startMethod(final MethodDeclaration methodDeclaration) {
            final MethodVisitor methodVisitor = classWriter.visitMethod(toAccess(methodDeclaration.getAccess()) | toModifier(methodDeclaration.getModifiers()),
                                                                        methodDeclaration.getMethodName(),
                                                                             toMethodDescriptor(methodDeclaration.getReturnType(), methodDeclaration.getArgumentTypes()),
                                                                             null,
                                                                             null);
            methodVisitor.visitCode();
            instructions(methodDeclaration, methodVisitor);
            methodVisitor.visitMaxs(0, 0);
            methodVisitor.visitEnd();
        }

        @Override
        public void endClass() {
            classWriter.visitEnd();
        }

        @Override
        public void endInterface() {
            throw new IllegalStateException();
        }

        private void instructions(final ConstructorDeclaration constructorDeclaration, final MethodVisitor methodVisitor) {
            new InstructionReader(constructorDeclaration.getInstructions()).
                    accept(new CompilerInstructionVisitor(classTypeDeclaration, constructorDeclaration, methodVisitor));
        }

        private void instructions(final MethodDeclaration methodDeclaration, final MethodVisitor methodVisitor) {
            new InstructionReader(methodDeclaration.getInstructions()).
                    accept(new CompilerInstructionVisitor(classTypeDeclaration, methodDeclaration, methodVisitor));
        }
    }

    private static final class InterfaceTypeDeclarationCompilerDeclarationVisitor extends CompilerDeclarationVisitor {

        private final InterfaceTypeDeclaration interfaceTypeDeclaration;
        private final ClassWriter classWriter;

        private InterfaceTypeDeclarationCompilerDeclarationVisitor(final InterfaceTypeDeclaration interfaceTypeDeclaration) {
            this.classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            this.interfaceTypeDeclaration = interfaceTypeDeclaration;

            classWriter.visit(V1_7,
                              ACC_INTERFACE | toAccess(interfaceTypeDeclaration.getAccess()) | toModifier(interfaceTypeDeclaration.getModifiers()),
                              toName(interfaceTypeDeclaration.getInterfaceType()),
                              null,
                              toName(Type.OBJECT_TYPE),
                              toNames(interfaceTypeDeclaration.getInterfaceTypes()));
        }

        @Override
        public byte[] getBytes() {
            return classWriter.toByteArray();
        }

        @Override
        public void startClass(final ClassTypeDeclaration classTypeDeclaration) {
            throw new IllegalStateException();
        }

        @Override
        public void startInterface(final InterfaceTypeDeclaration interfaceTypeDeclaration) {
            throw new IllegalStateException();
        }

        @Override
        public void field(final FieldDeclaration fieldDeclaration) {
            final FieldVisitor fieldVisitor = classWriter.visitField(toAccess(fieldDeclaration.getAccess()),
                                                                          fieldDeclaration.getFieldName(),
                                                                          toDescriptor(fieldDeclaration.getFieldType()),
                                                                          null,
                                                                          fieldDeclaration.getFieldValue());
            fieldVisitor.visitEnd();
        }

        @Override
        public void startConstructor(final ConstructorDeclaration constructorDeclaration) {
            throw new IllegalStateException();
        }

        @Override
        public void startMethod(final MethodDeclaration methodDeclaration) {
            final MethodVisitor methodVisitor = classWriter.visitMethod(toAccess(methodDeclaration.getAccess()) | toModifier(methodDeclaration.getModifiers()),
                                                                             methodDeclaration.getMethodName(),
                                                                             toMethodDescriptor(methodDeclaration.getReturnType(), methodDeclaration.getArgumentTypes()),
                                                                             null,
                                                                             null);
            methodVisitor.visitCode();
            instructions(methodDeclaration, methodVisitor);
            methodVisitor.visitMaxs(0, 0);
            methodVisitor.visitEnd();
        }

        @Override
        public void endClass() {
            throw new IllegalStateException();
        }

        @Override
        public void endInterface() {
            classWriter.visitEnd();
        }

        private void instructions(final MethodDeclaration methodDeclaration, final MethodVisitor methodVisitor) {
            if(methodDeclaration.getInstructions().length != 0) throw new IllegalStateException();
        }
    }



    private static class CompilerDeclarationVisitor implements DeclarationVisitor, Opcodes {

        private CompilerDeclarationVisitor declarationVisitor;

        public byte[] getBytes() {
            return getDeclarationVisitor().getBytes();
        }

        public void startClass(final ClassTypeDeclaration classTypeDeclaration) {
            newDeclarationVisitor(classTypeDeclaration);
        }

        public void startInterface(final InterfaceTypeDeclaration interfaceTypeDeclaration) {
            newDeclarationVisitor(interfaceTypeDeclaration);
        }

        public void field(final FieldDeclaration fieldDeclaration) {
            getDeclarationVisitor().field(fieldDeclaration);
        }

        public void startConstructor(final ConstructorDeclaration constructorDeclaration) {
            getDeclarationVisitor().startConstructor(constructorDeclaration);
        }

        public void startMethod(final MethodDeclaration methodDeclaration) {
            getDeclarationVisitor().startMethod(methodDeclaration);
        }

        public void endClass() {
            getDeclarationVisitor().endClass();
        }

        public void endInterface() {
            getDeclarationVisitor().endInterface();
        }
        
        protected CompilerDeclarationVisitor getDeclarationVisitor() {
            if(declarationVisitor == null) throw new IllegalStateException();
            return declarationVisitor;
        }

        private DeclarationVisitor newDeclarationVisitor(final ClassTypeDeclaration classTypeDeclaration) {
            declarationVisitor = new ClassTypeDeclarationCompilerDeclarationVisitor(classTypeDeclaration);
            return declarationVisitor;
        }

        private DeclarationVisitor newDeclarationVisitor(final InterfaceTypeDeclaration interfaceTypeDeclaration) {
            declarationVisitor = new InterfaceTypeDeclarationCompilerDeclarationVisitor(interfaceTypeDeclaration);
            return declarationVisitor;
        }
    }

*/

/*
    private static class CompilerInstructionVisitor implements InstructionVisitor, Opcodes {

        private final ClassTypeDeclaration classTypeDeclaration;
        private final Type<?>[] argumentTypes;
        private final Type<?> returnType;
        private final MethodVisitor methodVisitor;
        private final StackScope stackScope;

        private CompilerInstructionVisitor(final ClassTypeDeclaration classTypeDeclaration,
                                           final ConstructorDeclaration constructorDeclaration,
                                           final MethodVisitor methodVisitor) {
            this.stackScope = newBlockScope(constructorDeclaration.getConstructorTypes());
            this.classTypeDeclaration = classTypeDeclaration;
            this.argumentTypes = constructorDeclaration.getConstructorTypes();
            this.returnType = Type.VOID_TYPE;
            this.methodVisitor = methodVisitor;
        }



        private CompilerInstructionVisitor(final ClassTypeDeclaration classTypeDeclaration,
                                           final MethodDeclaration methodDeclaration,
                                           final MethodVisitor methodVisitor) {
            this.stackScope = newBlockScope(methodDeclaration.getArgumentTypes());
            this.classTypeDeclaration = classTypeDeclaration;
            this.argumentTypes = methodDeclaration.getArgumentTypes();
            this.returnType = methodDeclaration.getReturnType();
            this.methodVisitor = methodVisitor;
        }

        public void getField(final GetFieldInstruction getFieldInstruction) {
            //TODO: need to lookup declaration based on target...
            try {
            final Variable fieldOwner = getFieldInstruction.getFieldOwner();
            final ClassTypeDeclaration classTypeDeclaration = (fieldOwner == Variable.THIS)?
                                                              this.classTypeDeclaration :
                                                              this.classTypeDeclaration.getMetaClassLoader().<ClassTypeDeclaration>loadDeclaration(fieldOwner.getType());
            final FieldDeclaration fieldDeclaration = classTypeDeclaration.getFieldDeclaration(getFieldInstruction.getFieldName());
            //methodVisitor.visitVarInsn(ALOAD, 0);
            loadValues(methodVisitor, stackScope, fieldOwner);
            methodVisitor.visitFieldInsn(GETFIELD,
                                         toName(classTypeDeclaration.getClassType()),
                                         fieldDeclaration.getFieldName(),
                                         toDescriptor(fieldDeclaration.getFieldType()));
            }
            catch(final Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void putField(final PutFieldInstruction putFieldInstruction) {
            final FieldDeclaration fieldDeclaration = classTypeDeclaration.getFieldDeclaration(putFieldInstruction.getFieldName());
            methodVisitor.visitVarInsn(ALOAD, 0);
            loadValues(methodVisitor, stackScope, putFieldInstruction.getValue());
            methodVisitor.visitFieldInsn(PUTFIELD,
                                         toName(classTypeDeclaration.getClassType()),
                                         fieldDeclaration.getFieldName(),
                                         toDescriptor(fieldDeclaration.getFieldType()));
        }

        public void store(final StoreInstruction storeInstruction) {
            final Variable variable = storeInstruction.getVariable();
            final int index = stackScope.push(variable);
            instructions(storeInstruction.getInstruction());
            //TODO: abstract out as store values???
            methodVisitor.visitVarInsn(toType(variable.getType()).getOpcode(ISTORE),
                                       index);
        }

        public void invokeSuper(final InvokeSuperInstruction newInvokeSuperInstruction) {
            methodVisitor.visitVarInsn(ALOAD, 0);
            loadValues(methodVisitor, stackScope, newInvokeSuperInstruction.getValues());
            methodVisitor.visitMethodInsn(INVOKESPECIAL,
                                          toName(classTypeDeclaration.getSuperType()),
                                          "<init>",
                                          toMethodDescriptor(Type.VOID_TYPE, argumentTypes));
        }

        public void returnValue(final ReturnValueInstruction returnInstruction) {
            if(returnType == Type.VOID_TYPE) {
                methodVisitor.visitInsn(RETURN);
            }
            else {
                if(returnInstruction.getInstruction() != null) instructions(returnInstruction.getInstruction());
                if(returnInstruction.getValue() != null) loadValues(methodVisitor, stackScope, returnInstruction.getValue());
                methodVisitor.visitInsn(toType(returnType).getOpcode(IRETURN));
            }
        }

        private void instructions(final Instruction... instructions) {
            new InstructionReader(instructions).accept(this);
        }

        private static void loadValues(final MethodVisitor methodVisitor, final StackScope stackScope, final Value... values) {
            for(final Value value : values) {
                if(value.isConstant()) loadConstant(methodVisitor, (Constant) value);
                else if(value.isVariable()) loadVariable(methodVisitor, stackScope, (Variable) value);
                else throw new IllegalArgumentException();
            }
        }

        private static void loadConstant(final MethodVisitor methodVisitor, final Constant constant) {
            final Type type = constant.getType();
            final Object value = constant.getValue();

            if(type == Type.OBJECT_TYPE) methodVisitor.visitInsn(ACONST_NULL);
            else if(type == Type.BOOLEAN_TYPE) methodVisitor.visitInsn((((Boolean) value))? ICONST_1 : ICONST_0);
            else if(type == Type.BYTE_TYPE || type == Type.SHORT_TYPE || type == Type.INT_TYPE) {
                final int i = ((Number) value).intValue();
                if(i == -1) methodVisitor.visitInsn(ICONST_M1);
                else if(i >= 0 && i <= 5) methodVisitor.visitInsn(getAsmOpcode("ICONST_" + i));
                else if(i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) methodVisitor.visitIntInsn(BIPUSH, i);
                else if(i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) methodVisitor.visitIntInsn(SIPUSH, i);
                else methodVisitor.visitLdcInsn(i);
            }
            else if(type == Type.FLOAT_TYPE) {
                final float f = ((Number) value).floatValue();
                if(f == 0f || f == 1f || f == 2f) methodVisitor.visitInsn(getAsmOpcode("FCONST_" + (int) f));
                else methodVisitor.visitLdcInsn(f);
            }
            else if(type == Type.LONG_TYPE) {
                final long l = ((Number) value).longValue();
                if(l == 0 || l == 1) methodVisitor.visitInsn(getAsmOpcode("LCONST_" + l));
                else methodVisitor.visitLdcInsn(l);
            }
            else if(type == Type.DOUBLE_TYPE) {
                final double d = ((Number) value).doubleValue();
                if(d == 0d || d == 1d) methodVisitor.visitInsn(getAsmOpcode("DCONST_" + (int) d ));
                else methodVisitor.visitLdcInsn(d);
            }
            else if(type != Type.VOID_TYPE) methodVisitor.visitLdcInsn(value);
        }

        private static void loadVariable(final MethodVisitor methodVisitor, final StackScope stackScope, final Variable variable) {
            //TODO: we shouldn't need to re-resolve values...
            final Value value = stackScope.resolveValue(variable.getName());
            methodVisitor.visitVarInsn(toType(value.getType()).getOpcode(ILOAD),
                                       stackScope.getIndex(value));
        }


        private static StackScope newBlockScope(final Type<?>[] argumentTypes) {
            final StackScope stackScope = new StackScope();
            stackScope.push(Variable.THIS);
            for(int i = 0, size = argumentTypes.length; i != size; i++) stackScope.push(Variable.newVariable("arg" + i, argumentTypes[i]));
            return stackScope;
        }
    }
*/


    private static int toAccess(final TypeVisitor.Access access) {
        return getAsmOpcode("ACC_" + access.name());
    }

    private static int toModifier(final TypeVisitor.Modifier... modifiers) {
        int access = 0;
        for(final TypeVisitor.Modifier modifier : modifiers) access |= getAsmOpcode("ACC_" + modifier.name());
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


