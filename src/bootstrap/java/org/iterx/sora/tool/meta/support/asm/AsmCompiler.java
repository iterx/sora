package org.iterx.sora.tool.meta.support.asm;

import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.TypeDeclaration;
import org.iterx.sora.tool.meta.Value;
import org.iterx.sora.tool.meta.ValueInstruction;
import org.iterx.sora.tool.meta.declaration.ClassTypeDeclaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceTypeDeclaration;
import org.iterx.sora.tool.meta.support.asm.scope.ClassScope;
import org.iterx.sora.tool.meta.support.asm.scope.FrameScope;
import org.iterx.sora.tool.meta.support.asm.scope.MethodScope;
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

public final class AsmCompiler implements Opcodes {

    private final MetaClassLoader metaClassLoader;

    public AsmCompiler(final MetaClassLoader metaClassLoader) {
        this.metaClassLoader = metaClassLoader;
    }

    public byte[] compile(final TypeDeclaration<?, ?> typeDeclaration) {
        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        new TypeReader(typeDeclaration).accept(new CompilerTypeVisitor(metaClassLoader, classWriter));
        return classWriter.toByteArray();
    }

    private static class CompilerTypeVisitor implements TypeVisitor, Opcodes {

        private final ClassWriter classWriter;
        private final MetaClassLoader metaClassLoader;
        private MethodVisitor methodVisitor;
        private FrameScope frameScope;

        private CompilerTypeVisitor(final MetaClassLoader metaClassLoader, final ClassWriter classWriter) {
            this.metaClassLoader = metaClassLoader;
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
            frameScope = FrameScope.newFrameScope(metaClassLoader,
                                                  new ClassScope(access, modifiers, type, superType, interfaceTypes));
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
            frameScope = FrameScope.newFrameScope(metaClassLoader,
                                                  new ClassScope(access, modifiers, type, Type.OBJECT_TYPE, interfaceTypes));
        }

        public void field(final Access access,
                          final Modifier[] modifiers,
                          final String fieldName,
                          final Type<?> fieldType,
                          final Constant fieldValue) {
            final int fieldAccess = toAccess(access)|toModifier(modifiers);
            final FieldVisitor fieldVisitor = classWriter.visitField(fieldAccess,
                                                                     fieldName,
                                                                     toDescriptor(fieldType),
                                                                     null,
                                                                     ((fieldAccess & ACC_FINAL) != 0)? fieldValue.getValue() : null);
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
            //Setup non-final field states
            for(final FieldDeclaration fieldDeclaration : getFieldDeclarations(frameScope)) {
                if(!hasModifier(fieldDeclaration.getModifiers(), Modifier.FINAL) &&
                   fieldDeclaration.getFieldValue() != Value.VOID) {
                    methodVisitor.visitVarInsn(ALOAD, 0);
                    loadConstant(methodVisitor, fieldDeclaration.getFieldValue());
                    methodVisitor.visitFieldInsn(PUTFIELD,
                                                 toName(frameScope.getClassScope().getType()),
                                                 fieldDeclaration.getFieldName(),
                                                 toDescriptor(fieldDeclaration.getFieldType()));
                }
            }

            //Process constructor body
            return new CompilerInstructionVisitor(methodVisitor,
                                                  FrameScope.newFrameScope(frameScope.getMetaClassLoader(),
                                                                           frameScope.getClassScope(),
                                                                           new MethodScope(access,
                                                                                           modifiers,
                                                                                           "<init>",
                                                                                           Type.VOID_TYPE,
                                                                                           constructorTypes,
                                                                                           exceptionTypes)));
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
            return new CompilerInstructionVisitor(methodVisitor,
                                                  FrameScope.newFrameScope(frameScope.getMetaClassLoader(),
                                                                           frameScope.getClassScope(),
                                                                           new MethodScope(access,
                                                                                           modifiers,
                                                                                           methodName,
                                                                                           returnType,
                                                                                           argumentTypes,
                                                                                           exceptionTypes)));
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

        private static FieldDeclaration[] getFieldDeclarations(final FrameScope frameScope) {
            final Type<?> type = frameScope.getClassScope().getType();
            if(type.isInterface()) {
                final InterfaceTypeDeclaration interfaceTypeDeclaration = frameScope.getMetaClassLoader().loadDeclaration(type);
                return interfaceTypeDeclaration.getFieldDeclarations();
            }
            else if(type.isClass()) {
                final ClassTypeDeclaration classTypeDeclaration = frameScope.getMetaClassLoader().loadDeclaration(type);
                return classTypeDeclaration.getFieldDeclarations();
            }
            throw new UnsupportedOperationException();
        }

        private static boolean hasModifier(final Declaration.Modifier[] declarationModifiers, final Modifier modifier) {
            for(final Declaration.Modifier declarationModifier : declarationModifiers) if(declarationModifier.name().equals(modifier.name())) return true;
            return false;
        }

    }

    private static class CompilerInstructionVisitor implements InstructionVisitor, Opcodes {

        private final MethodVisitor methodVisitor;
        private final FrameScope frameScope;
        private final StackScope stackScope;

        private CompilerInstructionVisitor(final MethodVisitor methodVisitor, final FrameScope frameScope) {
            this.methodVisitor = methodVisitor;
            this.frameScope = frameScope;
            this.stackScope = frameScope.getStackScope();
        }

        public void SUPER(final String methodName,
                          final Type<?> returnType,
                          final Value<?>[] values) {
            final Type<?>[] argumentTypes = getTypes(frameScope, values);
            loadValues(methodVisitor, frameScope, Variable.THIS);
            loadValues(methodVisitor, frameScope, values);
            methodVisitor.visitMethodInsn(INVOKESPECIAL,
                                          toName(getMethodOwner(frameScope, Variable.SUPER, methodName, argumentTypes)),
                                          methodName,
                                          toMethodDescriptor(returnType, argumentTypes));
        }

        public void invokeMethod(final Type<?> target,
                                 final String methodName,
                                 final Type<?> returnType,
                                 final Value<?>[] values) {
            final Type<?>[] argumentTypes = getTypes(frameScope, values);
            loadValues(methodVisitor, frameScope, Variable.THIS);
            loadValues(methodVisitor, frameScope, values);
            //TODO: need to resolve if target is private -> i.e INVOKESPECIAL
            methodVisitor.visitMethodInsn((target.isInterface())? INVOKEINTERFACE : INVOKEVIRTUAL,
                                          toName(target),
                                          methodName,
                                          toMethodDescriptor(returnType, argumentTypes));
        }

        public void RETURN(final Value<?> value) {
            final Type returnType = value.getType();

            loadValues(methodVisitor, frameScope, value);
            if(Type.VOID_TYPE != returnType) methodVisitor.visitInsn(toType(returnType).getOpcode(IRETURN));
            else methodVisitor.visitInsn(RETURN);
        }

        public void store(final Variable variable, final Value<?> value) {
            loadValues(methodVisitor, frameScope, value);
            methodVisitor.visitVarInsn(toType(variable.getType()).getOpcode(ISTORE),
                                       stackScope.push(variable));

        }

        public void GETFIELD(final Variable owner,
                             final String fieldName,
                             final Type<?> fieldType) {
            final FieldDeclaration fieldDeclaration = getFieldDeclaration(frameScope, owner, fieldName);
            
            loadValues(methodVisitor, frameScope, owner);
            methodVisitor.visitFieldInsn(GETFIELD,
                                         toName(getType(frameScope, owner)),
                                         fieldName,
                                         toDescriptor(fieldType));
        }

        public void putField(final Variable owner,
                             final String fieldName,
                             final Type<?> fieldType,
                             final Value<?> value) {
            loadValues(methodVisitor, frameScope, owner);
            loadValues(methodVisitor, frameScope, value);
            methodVisitor.visitFieldInsn(PUTFIELD,
                                         toName(getType(frameScope, owner)),
                                         fieldName,
                                         toDescriptor(fieldType));
        }

        private static Type<?> getMethodOwner(final FrameScope frameScope, final Variable variable, final String methodName, final Type<?>[] argumentTypes) {
            if(variable == Variable.THIS) return frameScope.getClassScope().getType();
            else if(variable == Variable.SUPER) return frameScope.getClassScope().getSuperType();
/*
            final MethodScope methodScope = frameScope.getMethodScope();
            if(methodScope.getMethodName().equals(methodName) && Arrays.equals(methodScope.getArgumentTypes(), argumentTypes)) {
                return frameScope.getClassScope().getType();
            }
*/
            //TODO: need to resolve method owner by walking tree
            throw new UnsupportedOperationException();
        }

        
        private static FieldDeclaration getFieldDeclaration(final FrameScope frameScope, final Variable variable, final String fieldName) {
            final Type<?> type = getType(frameScope, variable);
            if(type.isInterface()) {
                final InterfaceTypeDeclaration interfaceTypeDeclaration = frameScope.getMetaClassLoader().loadDeclaration(type);
                return interfaceTypeDeclaration.getFieldDeclaration(fieldName);
            }
            else if(type.isClass()) {
                final ClassTypeDeclaration classTypeDeclaration = frameScope.getMetaClassLoader().loadDeclaration(type);
                return classTypeDeclaration.getFieldDeclaration(fieldName);
            }
            throw new UnsupportedOperationException();
        }


        private static Type<?> getType(final FrameScope frameScope, final Value value) {
            if(value == Variable.THIS) return frameScope.getClassScope().getType();
            else if(value == Variable.SUPER) return frameScope.getClassScope().getSuperType();
            else return value.getType();
        }

        private static Type<?>[] getTypes(final FrameScope frameScope, final Value[] values) {
            final Type<?>[] types = new Type<?>[values.length];
            for(int i = values.length; i-- != 0;) types[i] = getType(frameScope, values[i]);
            return types;
        }

        private static void loadValues(final MethodVisitor methodVisitor, final FrameScope stackScope, final Value... values) {
            for(final Value value : values) {
                if(value.isConstant()) loadConstant(methodVisitor, (Constant) value);
                else if(value.isVariable()) loadVariable(methodVisitor, stackScope, (Variable) value);
                else if(value.isInstruction()) loadInstruction(methodVisitor, stackScope, (ValueInstruction) value);
                else throw new IllegalArgumentException();
            }
        }


        private static void loadVariable(final MethodVisitor methodVisitor, final FrameScope frameScope, final Variable variable) {
            //TODO: we shouldn't need to re-resolve values...
            final StackScope stackScope = frameScope.getStackScope();
            final Value value = stackScope.resolveValue(variable.getName());
            methodVisitor.visitVarInsn(toType(value.getType()).getOpcode(ILOAD),
                                       stackScope.getIndex(value));
        }

        private static void loadInstruction(final MethodVisitor methodVisitor, final FrameScope frameScope, final ValueInstruction valueInstruction) {
            new InstructionReader(valueInstruction).accept(new CompilerInstructionVisitor(methodVisitor, frameScope));
        }
    }

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
}


