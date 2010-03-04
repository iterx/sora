package org.iterx.sora.tool.meta.support.asm;

import org.iterx.sora.tool.meta.type.ClassMetaType;
import org.iterx.sora.tool.meta.type.InterfaceMetaType;
import org.iterx.sora.tool.meta.type.Type;
import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;
import org.iterx.sora.tool.meta.declaration.Declaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.objectweb.asm.Opcodes.ACC_INTERFACE;

public final class AsmExtractor {

    private AsmExtractor() {}

    public static Declaration<?> extract(final byte[] bytes) {
        final ClassReader classReader = new ClassReader(bytes);
        final DeclarationExtractorClassVisitor declarationExtractorClassVisitor = new DeclarationExtractorClassVisitor();

        classReader.accept(declarationExtractorClassVisitor, ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        return declarationExtractorClassVisitor.getDeclaration();
    }

    public static Type<?> getType(final byte[] bytes) {
        final ClassReader classReader = new ClassReader(bytes);
        final MetaTypeClassVisitor metaTypeClassVisitor = new MetaTypeClassVisitor();
        classReader.accept(metaTypeClassVisitor,  ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES|ClassReader.SKIP_CODE);
        return metaTypeClassVisitor.getMetaType();
    }

    private static class MetaTypeClassVisitor extends DefaultClassVisitor {

        private Type<?> metaType;

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
            metaType = hasAsmOpcodes(access, ACC_INTERFACE)? InterfaceMetaType.newType(className) :
                       ClassMetaType.newType(className);
        }
    }

    private static class DeclarationExtractorClassVisitor extends DefaultClassVisitor {

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

    private static class InterfaceDeclarationExtractorClassVisitor extends DeclarationExtractorClassVisitor {
        private InterfaceDeclaration interfaceDeclaration;

        public InterfaceDeclarationExtractorClassVisitor(final int access,
                                                         final String name,
                                                         final String signature,
                                                         final String superName,
                                                         final String[] interfaceNames) {

            interfaceDeclaration = InterfaceDeclaration.newInterfaceDeclaration(toInterfaceType(org.objectweb.asm.Type.getObjectType(name))).
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
            //TODO: add support for exceptions & generics
            if("<clinit>".equals(name)) {
                System.err.println("<clinit> not supported for " + interfaceDeclaration.getType());
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

    private static class ClassDeclarationExtractorClassVisitor extends DeclarationExtractorClassVisitor {

        private ClassDeclaration classDeclaration;

        public ClassDeclarationExtractorClassVisitor(final int access,
                                                     final String name,
                                                     final String signature,
                                                     final String superName,
                                                     final String[] interfaceNames) {
            classDeclaration = ClassDeclaration.newClassDeclaration(toClassType(org.objectweb.asm.Type.getObjectType(name))).
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
            if("<clinit>".equals(name)) {
                System.err.println("<clinit> not supported for " + classDeclaration.getType());
            }
            else if("<init>".equals(name)) {
                classDeclaration.add(ConstructorDeclaration.newConstructorDeclaration(toTypes(org.objectweb.asm.Type.getArgumentTypes(description))).
                        setExceptionTypes(toTypes(toObjectTypes(exceptions))).
                        setAccess(toAccess(access, ConstructorDeclaration.Access.values())).
                        setModifiers(toModifiers(access, ConstructorDeclaration.Modifier.values())));
            }
            else {
                classDeclaration.add(MethodDeclaration.newMethodDeclaration(name, toTypes(org.objectweb.asm.Type.getArgumentTypes(description))).
                        setReturnType(toType(org.objectweb.asm.Type.getReturnType(description))).
                        setExceptionTypes(toTypes(toObjectTypes(exceptions))).
                        setAccess(toAccess(access, MethodDeclaration.Access.values())).
                        setModifiers(toModifiers(access, MethodDeclaration.Modifier.values())));
            }
            return null;
        }

    }

    private static class DefaultClassVisitor implements ClassVisitor {

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

    private static org.objectweb.asm.Type[] toObjectTypes(final String... names) {
        final org.objectweb.asm.Type[] types = (names != null)? new org.objectweb.asm.Type[names.length] : new org.objectweb.asm.Type[0];
        for(int i = types.length; i-- != 0; ) types[i] = org.objectweb.asm.Type.getObjectType(names[i]);
        return types;
    }


    private static Type toType(final org.objectweb.asm.Type type) {
        try {
            return Type.getType(type.getClassName());
        }
        catch(final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Type[] toTypes(final org.objectweb.asm.Type... types) {
        final Type[] instanceTypes = new Type[types.length];
        for(int i = instanceTypes.length; i-- != 0; ) instanceTypes[i] = toType(types[i]);
        return instanceTypes;
    }

    private static InterfaceMetaType toInterfaceType(final org.objectweb.asm.Type type) {
        return InterfaceMetaType.newType(type.getClassName());
    }

    public static InterfaceMetaType[] toInterfaceTypes(final org.objectweb.asm.Type... types) {
        final InterfaceMetaType[] interfaceTypes = new InterfaceMetaType[types.length];
        for(int i = interfaceTypes.length; i-- != 0; ) interfaceTypes[i] = toInterfaceType(types[i]);
        return interfaceTypes;
    }

    public static ClassMetaType toClassType(final org.objectweb.asm.Type type) {
        return ClassMetaType.newType(type.getClassName());
    }

    public static ClassMetaType[] toClassTypes(final org.objectweb.asm.Type... types) {
        final ClassMetaType[] classTypes = new ClassMetaType[types.length];
        for(int i = classTypes.length; i-- != 0; ) classTypes[i] = toClassType(types[i]);
        return classTypes;
    }

    private static <T extends Declaration.Access> T toAccess(final int value, final T... accesses) {
        for(final T access : accesses) if((value & getAsmOpcode("ACC_" + access)) != 0) return access;
        return accesses[accesses.length - 1];
    }

    @SuppressWarnings("unchecked")
    private static <T extends Declaration.Modifier> T[] toModifiers(final int value, final T... modifiers) {
        int matches = 0;

        final T[] values = Arrays.copyOf(modifiers, modifiers.length);
        for(final T modifier : modifiers) if((value & getAsmOpcode("ACC_" + modifier)) != 0) values[matches++] = modifier;
        return Arrays.copyOf(values, matches);
    }

    private static boolean hasAsmOpcodes(final int access, final int opcodes) {
        return ((access & opcodes) != 0);
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
}
