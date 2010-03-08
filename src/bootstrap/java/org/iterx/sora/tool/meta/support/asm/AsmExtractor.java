package org.iterx.sora.tool.meta.support.asm;

import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.Value;
import org.iterx.sora.tool.meta.instruction.GetFieldInstruction;
import org.iterx.sora.tool.meta.instruction.PutFieldInstruction;
import org.iterx.sora.tool.meta.type.ClassMetaType;
import org.iterx.sora.tool.meta.type.InterfaceMetaType;
import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.util.Arrays;

public final class AsmExtractor {

    private final MetaClassLoader metaClassLoader;

    public AsmExtractor(final MetaClassLoader metaClassLoader) {
        this.metaClassLoader = metaClassLoader;
    }

    public Declaration<?> extract(final byte[] bytes) {
        final ClassReader classReader = new ClassReader(bytes);
        final DeclarationExtractorClassVisitor declarationExtractorClassVisitor = new DeclarationExtractorClassVisitor();

        classReader.accept(declarationExtractorClassVisitor, ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        return declarationExtractorClassVisitor.getDeclaration();
    }

    public Type<?> getType(final byte[] bytes) {
        final ClassReader classReader = new ClassReader(bytes);
        final MetaTypeClassVisitor metaTypeClassVisitor = new MetaTypeClassVisitor();
        classReader.accept(metaTypeClassVisitor,  ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES|ClassReader.SKIP_CODE);
        return metaTypeClassVisitor.getMetaType();
    }

    private class MetaTypeClassVisitor extends DefaultClassVisitor {

        private Type<?> metaType;

        @SuppressWarnings("unchecked")
        public Type<?> getMetaType() {
            if(metaType == null) throw new IllegalStateException();
            return metaType;
        }

        public void visit(final int version,
                          final int access,
                          final String name,
                          final String signature,
                          final String superName,
                          final String[] interfaceNames) {
            final String className = org.objectweb.asm.Type.getObjectType(name).getClassName();
            metaType = hasAsmOpcodes(access, ACC_INTERFACE)?
                       InterfaceMetaType.newType(metaClassLoader, className) :
                       ClassMetaType.newType(metaClassLoader, className);
        }
    }

    private class DeclarationExtractorClassVisitor extends DefaultClassVisitor {

        private DeclarationExtractorClassVisitor classVisitor;

        public Declaration<?> getDeclaration() {
            return classVisitor.getDeclaration();
        }

        public void visit(final int version,
                          final int access,
                          final String name,
                          final String signature,
                          final String superName,
                          final String[] interfaceNames) {
            newClassVisitor(access, name, signature, superName, interfaceNames);
        }

        public void visitSource(final String source, final String debug) {
            getClassVisitor().visitSource(source, debug);
        }

        public void visitOuterClass(final String owner,
                                    final String name,
                                    final String description) {
            getClassVisitor().visitOuterClass(owner, name, description);
        }

        public void visitAttribute(final Attribute attribute) {
            getClassVisitor().visitAttribute(attribute);
        }

        public AnnotationVisitor visitAnnotation(final String description, final boolean visible) {
            return getClassVisitor().visitAnnotation(description, visible);
        }

        public void visitInnerClass(final String name,
                                    final String innerName,
                                    final String outerName,
                                    final int access) {
            getClassVisitor().visitInnerClass(name, innerName, outerName, access);
        }

        public FieldVisitor visitField(final int access,
                                       final String name,
                                       final String description,
                                       final String signature,
                                       final Object value) {
            return getClassVisitor().visitField(access, name, description, signature, value);
        }

        public MethodVisitor visitMethod(final int access,
                                         final String name,
                                         final String description,
                                         final String signature,
                                         final String[] exceptions) {
            return getClassVisitor().visitMethod(access, name, description, signature, exceptions);
        }

        public void visitEnd() {
            getClassVisitor().visitEnd();
        }

        protected ClassVisitor getClassVisitor() {
            if(classVisitor == null) throw new IllegalStateException();
            return classVisitor;
        }

        private DeclarationExtractorClassVisitor newClassVisitor(final int access,
                                                                 final String name,
                                                                 final String signature,
                                                                 final String superName,
                                                                 final String[] interfaceNames) {
            classVisitor = hasAsmOpcodes(access, ACC_INTERFACE)?
                           new InterfaceDeclarationExtractorClassVisitor(access, name, signature, superName, interfaceNames) :
                           new ClassDeclarationExtractorClassVisitor(access, name, signature, superName, interfaceNames);
            return classVisitor;
        }
    }

    private class InterfaceDeclarationExtractorClassVisitor extends DeclarationExtractorClassVisitor {

        private InterfaceDeclaration interfaceDeclaration;

        public InterfaceDeclarationExtractorClassVisitor(final int access,
                                                         final String name,
                                                         final String signature,
                                                         final String superName,
                                                         final String[] interfaceNames) {

            interfaceDeclaration = InterfaceDeclaration.newInterfaceDeclaration(metaClassLoader, toInterfaceType(org.objectweb.asm.Type.getObjectType(name))).
                    setAccess(toAccess(access, InterfaceDeclaration.Access.values()));
            if(interfaceNames != null) interfaceDeclaration.setInterfaceTypes(toInterfaceTypes(toObjectTypes(interfaceNames)));
        }

        public Declaration<InterfaceDeclaration> getDeclaration() {
            return interfaceDeclaration;
        }

        @Override
        protected ClassVisitor getClassVisitor() {
            return DefaultClassVisitor.INSTANCE;
        }

        @Override
        public FieldVisitor visitField(final int access,
                                       final String name,
                                       final String description,
                                       final String signature,
                                       final Object value) {
            //TODO: add support for value
            interfaceDeclaration.add(FieldDeclaration.newFieldDeclaration(name, toType(org.objectweb.asm.Type.getType(description))).
                    setAccess(toAccess(access, FieldDeclaration.Access.values())).
                    setModifiers(toModifiers(access, FieldDeclaration.Modifier.values())));
            return null;
        }

        @Override
        public MethodVisitor visitMethod(final int access,
                                         final String name,
                                         final String description,
                                         final String signature,
                                         final String[] exceptions) {
            //TODO: add support for statics & generics
            if("<clinit>".equals(name)) {
                System.err.println("<clinit> not supported for " + interfaceDeclaration.getInterfaceType());
            }
            else if("<init>".equals(name)) {
                throw new IllegalArgumentException();
            }
            else {
                interfaceDeclaration.add(MethodDeclaration.newMethodDeclaration(name, toTypes(org.objectweb.asm.Type.getArgumentTypes(description))).
                        setReturnType(toType(org.objectweb.asm.Type.getReturnType(description))).
                        setExceptionTypes(toTypes(toObjectTypes(exceptions))).
                        setAccess(toAccess(access, MethodDeclaration.Access.values())).
                        setModifiers(toModifiers(access, MethodDeclaration.Modifier.values())));
            }
            return null;
        }
    }

    private class ClassDeclarationExtractorClassVisitor extends DeclarationExtractorClassVisitor {

        private ClassDeclaration classDeclaration;

        public ClassDeclarationExtractorClassVisitor(final int access,
                                                     final String name,
                                                     final String signature,
                                                     final String superName,
                                                     final String[] interfaceNames) {
            classDeclaration = ClassDeclaration.newClassDeclaration(metaClassLoader, toClassType(org.objectweb.asm.Type.getObjectType(name))).
                    setAccess(toAccess(access, ClassDeclaration.Access.values())).
                    setModifiers(toModifiers(access, ClassDeclaration.Modifier.values()));
            if(superName != null) classDeclaration.setSuperType(toClassType(org.objectweb.asm.Type.getObjectType(superName)));
            if(interfaceNames != null) classDeclaration.setInterfaceTypes(toInterfaceTypes(toObjectTypes(interfaceNames)));
        }

        public Declaration<ClassDeclaration> getDeclaration() {
            return classDeclaration;
        }
        
        @Override
        protected ClassVisitor getClassVisitor() {
            return DefaultClassVisitor.INSTANCE;
        }

        @Override
        public FieldVisitor visitField(final int access,
                                       final String name,
                                       final String description,
                                       final String signature,
                                       final Object value) {
            //TODO: add support for value
            classDeclaration.add(FieldDeclaration.newFieldDeclaration(name, toType(org.objectweb.asm.Type.getType(description))).
                    setAccess(toAccess(access, FieldDeclaration.Access.values())).
                    setModifiers(toModifiers(access, FieldDeclaration.Modifier.values())));
            return null;
        }

        public MethodVisitor visitMethod(final int access,
                                         final String name,
                                         final String description,
                                         final String signature,
                                         final String[] exceptions) {
            // add support for statics and generics
            if("<clinit>".equals(name)) {
                System.err.println("<clinit> not supported for " + classDeclaration.getClassType());
            }
            else if("<init>".equals(name)) {
                classDeclaration.add(ConstructorDeclaration.newConstructorDeclaration(toTypes(org.objectweb.asm.Type.getArgumentTypes(description))).
                        setExceptionTypes(toTypes(toObjectTypes(exceptions))).
                        setAccess(toAccess(access, ConstructorDeclaration.Access.values())).
                        setModifiers(toModifiers(access, ConstructorDeclaration.Modifier.values())));
            }
            else {
                final MethodDeclaration methodDeclaration = MethodDeclaration.newMethodDeclaration(name, toTypes(org.objectweb.asm.Type.getArgumentTypes(description))).
                        setReturnType(toType(org.objectweb.asm.Type.getReturnType(description))).
                        setExceptionTypes(toTypes(toObjectTypes(exceptions))).
                        setAccess(toAccess(access, MethodDeclaration.Access.values())).
                        setModifiers(toModifiers(access, MethodDeclaration.Modifier.values()));
                classDeclaration.add(methodDeclaration);
                return new InstructionExtractorMethodVisitor(methodDeclaration);
            }
            return null;
        }
    }

    private static class DefaultClassVisitor implements ClassVisitor, Opcodes {

        private static final DefaultClassVisitor INSTANCE = new DefaultClassVisitor(){};

        public void visit(final int version,
                          final int access,
                          final String name,
                          final String signature,
                          final String superName,
                          final String[] interfaceNames) {}

        public void visitSource(final String source, final String debug) {}

        public void visitOuterClass(final String owner,
                                    final String name,
                                    final String description) {}

        public void visitAttribute(final Attribute attribute) {}

        public AnnotationVisitor visitAnnotation(final String description, final boolean visible) {
            return null;
        }

        public void visitInnerClass(final String name,
                                    final String innerName,
                                    final String outerName,
                                    final int access) {}

        public FieldVisitor visitField(final int access,
                                       final String name,
                                       final String description,
                                       final String signature,
                                       final Object value) {
            return null;
        }

        public MethodVisitor visitMethod(final int access,
                                         final String name,
                                         final String description,
                                         final String signature,
                                         final String[] exceptions) {
            return null;
        }

        public void visitEnd() {}
    }

    private class InstructionExtractorMethodVisitor implements MethodVisitor, Opcodes {

        private final MethodDeclaration methodDeclaration;
        private final AsmScope asmScope;

        private InstructionExtractorMethodVisitor(final MethodDeclaration methodDeclaration) {
            this.asmScope = newAsmStack(methodDeclaration.getArgumentTypes());
            this.methodDeclaration = methodDeclaration;
        }

        public AnnotationVisitor visitAnnotationDefault() {
            return null;
        }

        public AnnotationVisitor visitAnnotation(final String s, final boolean b) {
            return null;
        }

        public AnnotationVisitor visitParameterAnnotation(final int i, final String s, final boolean b) {
            return null;
        }

        public void visitAttribute(final Attribute attribute) {
        }

        public void visitCode() {
        }

        public void visitFrame(final int i, final int i1, final Object[] objects, final int i2, final Object[] objects1) {
        }

        public void visitInsn(final int i) {
        }

        public void visitIntInsn(final int i, final int i1) {
        }

        public void visitVarInsn(final int opcode, final int variable) {
            if(hasAsmOpcodes(opcode, ILOAD|LLOAD|FLOAD|DLOAD|ALOAD))
                asmScope.push(asmScope.getName(variable), asmScope.getType(variable));
            else if(hasAsmOpcodes(opcode, ISTORE|LSTORE|FSTORE|DSTORE|ASTORE))
                asmScope.push("var" + variable, asmScope.getType(variable));
            //TODO: RET
        }


        public void visitTypeInsn(final int i, final String s) {
        }

        public void visitFieldInsn(final int opcode, final String owner, final String name, final String description) {

            switch(opcode){
                case GETFIELD:
                    methodDeclaration.add(GetFieldInstruction.newGetFieldInstruction(name));
                    //method.add(GetFieldInstruction.newGetFieldInstruction(name, Value.newValue(name)));
                    //asmScope.push(name, );
                    break;
                case PUTFIELD:
                    final String valueName = asmScope.peek();
                    //TODO: do we need to support Value types???
                    methodDeclaration.add(PutFieldInstruction.newPutFieldInstruction(name, Value.newValue(valueName)));
                    asmScope.pop();
                    asmScope.pop();
                    break;
                default:
                    //throw new UnsupportedOperationException();
            }
        }

        public void visitMethodInsn(final int i, final String s, final String s1, final String s2) {
        }

        public void visitJumpInsn(final int i, final Label label) {
        }

        public void visitLabel(final Label label) {
        }

        public void visitLdcInsn(final Object o) {
        }

        public void visitIincInsn(final int i, final int i1) {
        }

        public void visitTableSwitchInsn(final int i, final int i1, final Label label, final Label[] labels) {
        }

        public void visitLookupSwitchInsn(final Label label, final int[] ints, final Label[] labels) {
        }

        public void visitMultiANewArrayInsn(final String s, final int i) {
        }

        public void visitTryCatchBlock(final Label label, final Label label1, final Label label2, final String s) {
        }

        public void visitLocalVariable(final String s, final String s1, final String s2, final Label label, final Label label1, final int i) {
        }

        public void visitLineNumber(final int i, final Label label) {
        }

        public void visitMaxs(final int i, final int i1) {
        }

        public void visitEnd() {
        }

        private Type<?> toType(final int opcode) {
            if(hasAsmOpcodes(opcode, ILOAD)) return Type.INT_TYPE;
            else if(hasAsmOpcodes(opcode, ALOAD))  return Type.OBJECT_TYPE;
            else if(hasAsmOpcodes(opcode, LLOAD))  return Type.LONG_TYPE;
            else if(hasAsmOpcodes(opcode, FLOAD))  return Type.FLOAT_TYPE;
            else if(hasAsmOpcodes(opcode, DLOAD))  return Type.DOUBLE_TYPE;
            throw new IllegalArgumentException();
        }

        private AsmScope newAsmStack(final Type<?>[] argumentTypes) {
            final AsmScope asmScope = new AsmScope();

            asmScope.push("this", Type.OBJECT_TYPE);
            for(int i = 0, size = argumentTypes.length; i != size; i++) asmScope.push("arg" + i, argumentTypes[i]);
            return asmScope;
        }
    }

    private static org.objectweb.asm.Type[] toObjectTypes(final String... names) {
        final org.objectweb.asm.Type[] types = (names != null)? new org.objectweb.asm.Type[names.length] : new org.objectweb.asm.Type[0];
        for(int i = types.length; i-- != 0; ) types[i] = org.objectweb.asm.Type.getObjectType(names[i]);
        return types;
    }

    @SuppressWarnings("unchecked")
    private <T extends Type> T toType(final org.objectweb.asm.Type type) {
        try {
            return (T) metaClassLoader.loadType(type.getClassName());
        }
        catch(final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Type> T[] toTypes(final org.objectweb.asm.Type... types) {
        final T[] instanceTypes = (T[]) new Type[types.length];
        for(int i = instanceTypes.length; i-- != 0; ) instanceTypes[i] = toType(types[i]);
        return instanceTypes;
    }

    private InterfaceMetaType toInterfaceType(final org.objectweb.asm.Type type) {
        try {
            return toType(type);
        }
        catch(final RuntimeException e) {
            return InterfaceMetaType.newType(type.getClassName());
        }

    }

    private InterfaceMetaType[] toInterfaceTypes(final org.objectweb.asm.Type... types) {
        final InterfaceMetaType[] interfaceTypes = new InterfaceMetaType[types.length];
        for(int i = interfaceTypes.length; i-- != 0; ) interfaceTypes[i] = toInterfaceType(types[i]);
        return interfaceTypes;
    }

    private ClassMetaType toClassType(final org.objectweb.asm.Type type) {
        try {
            return toType(type);
        }
        catch(final RuntimeException e) {
            return ClassMetaType.newType(type.getClassName());
        }
    }

    private ClassMetaType[] toClassTypes(final org.objectweb.asm.Type... types) {
        final ClassMetaType[] classTypes = new ClassMetaType[types.length];
        for(int i = classTypes.length; i-- != 0; ) classTypes[i] = toClassType(types[i]);
        return classTypes;
    }

    private <T extends Declaration.Access> T toAccess(final int value, final T... accesses) {
        for(final T access : accesses) if((value & getAsmOpcode("ACC_" + access)) != 0) return access;
        return accesses[accesses.length - 1];
    }

    @SuppressWarnings("unchecked")
    private <T extends Declaration.Modifier> T[] toModifiers(final int value, final T... modifiers) {
        int matches = 0;

        final T[] values = Arrays.copyOf(modifiers, modifiers.length);
        for(final T modifier : modifiers) if((value & getAsmOpcode("ACC_" + modifier)) != 0) values[matches++] = modifier;
        return Arrays.copyOf(values, matches);
    }

    private boolean hasAsmOpcodes(final int opcodes, final int values) {
        return ((opcodes & values) != 0);
    }

    private int getAsmOpcode(final String name) {
        try {
            final Field field = org.objectweb.asm.Opcodes.class.getField(name);
            return (Integer) field.get(org.objectweb.asm.Opcodes.class);
        }
        catch(final Exception e) {
            return 0;
        }
    }
}
