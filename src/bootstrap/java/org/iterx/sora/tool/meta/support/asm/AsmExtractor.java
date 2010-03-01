package org.iterx.sora.tool.meta.support.asm;

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
        final ExtractorClassVisitor extractorClassVisitor = new ExtractorClassVisitor();

        classReader.accept(extractorClassVisitor, ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        return extractorClassVisitor.getDeclaration();
    }

    private static class ExtractorClassVisitor extends AbstractExtractorClassVisitor {

        private AbstractExtractorClassVisitor classVisitor;

        public Declaration<?> getDeclaration() {
            return getClassVisitor().getDeclaration();
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

        private AbstractExtractorClassVisitor getClassVisitor() {
            if(classVisitor == null) throw new IllegalStateException();
            return classVisitor;
        }

        private AbstractExtractorClassVisitor newClassVisitor(final int access,
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

    private static class InterfaceDeclarationExtractorClassVisitor extends AbstractExtractorClassVisitor {

        private InterfaceDeclaration interfaceDeclaration;

        public InterfaceDeclarationExtractorClassVisitor(final int access,
                                                         final String name,
                                                         final String signature,
                                                         final String superName,
                                                         final String[] interfaceNames) {

            interfaceDeclaration = InterfaceDeclaration.newInterfaceDeclaration(toType(name)).
                    setInterfaceTypes(toTypes(interfaceNames)).
                    setAccess(toAccess(access, InterfaceDeclaration.Access.values()));
        }

        public Declaration<InterfaceDeclaration> getDeclaration() {
            return interfaceDeclaration;
        }

        @Override
        public FieldVisitor visitField(final int access, final String name, final String description, final String signature, final Object value) {
            //TODO: add support for value
            interfaceDeclaration.add(FieldDeclaration.newFieldDeclaration(name, toType(description)).
                    setAccess(toAccess(access, FieldDeclaration.Access.values())).
                    setModifiers(toModifiers(access, FieldDeclaration.Modifier.values())));
            return null;
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String description, final String signature, final String[] exceptions) {
            //TODO: add support for exceptions & generics
            assertMethod(name);
            interfaceDeclaration.add(MethodDeclaration.newMethodDeclaration(name, toTypes(org.objectweb.asm.Type.getArgumentTypes(description))).
                    setReturnType(toType(org.objectweb.asm.Type.getReturnType(description))).
                    setAccess(toAccess(access, MethodDeclaration.Access.values())).
                    setModifiers(toModifiers(access, MethodDeclaration.Modifier.values())));

            return null;
        }

        private void assertMethod(final String name) {
            if(isConstructor(name)) throw new IllegalArgumentException();
        }
    }

    private static class ClassDeclarationExtractorClassVisitor extends AbstractExtractorClassVisitor {

        private ClassDeclaration classDeclaration;

        public ClassDeclarationExtractorClassVisitor(final int access,
                                                     final String name,
                                                     final String signature,
                                                     final String superName,
                                                     final String[] interfaceNames) {
            classDeclaration = ClassDeclaration.newClassDeclaration(toType(name)).
                    setSuperType(toType(superName)).
                    setInterfaceTypes(toTypes(interfaceNames)).
                    setAccess(toAccess(access, ClassDeclaration.Access.values())).
                    setModifiers(toModifiers(access, ClassDeclaration.Modifier.values()));
        }

        public Declaration<ClassDeclaration> getDeclaration() {
            return classDeclaration;
        }

        @Override
        public FieldVisitor visitField(final int access, final String name, final String description, final String signature, final Object value) {
            //TODO: add support for value
            classDeclaration.add(FieldDeclaration.newFieldDeclaration(name, toType(description)).
                    setAccess(toAccess(access, FieldDeclaration.Access.values())).
                    setModifiers(toModifiers(access, FieldDeclaration.Modifier.values())));
            return null;
        }

        public MethodVisitor visitMethod(final int access, final String name, final String description, final String signature, final String[] exceptions) {
            if(isConstructor(name)) {
                classDeclaration.add(ConstructorDeclaration.newConstructorDeclaration(toTypes(org.objectweb.asm.Type.getArgumentTypes(description))).
                        setAccess(toAccess(access, ConstructorDeclaration.Access.values())).
                        setModifiers(toModifiers(access, ConstructorDeclaration.Modifier.values())));
            }
            else {
                classDeclaration.add(MethodDeclaration.newMethodDeclaration(name, toTypes(org.objectweb.asm.Type.getArgumentTypes(description))).
                        setReturnType(toType(org.objectweb.asm.Type.getReturnType(description))).
                        setAccess(toAccess(access, MethodDeclaration.Access.values())).
                        setModifiers(toModifiers(access, MethodDeclaration.Modifier.values())));
            }
            return null;
        }

    }

    private static abstract class AbstractExtractorClassVisitor implements ClassVisitor {

        public abstract Declaration<?> getDeclaration();

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

    private static Type toType(final org.objectweb.asm.Type type) {
        //TODO: fix this....
        return Type.getType(type.getClassName(), null);
    }

    private static Type toType(final String name) {
        return toType(org.objectweb.asm.Type.getObjectType(name));
    }

    private static Type[] toTypes(final org.objectweb.asm.Type... type) {
        final Type[] types = new Type[type.length];
        for(int i = types.length; i-- != 0; ) types[i] = toType(type[i]);
        return types;
    }

    private static Type[] toTypes(final String... names) {
        final Type[] types = new Type[names.length];
        for(int i = types.length; i-- != 0; ) types[i] = toType(names[i]);
        return types;
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

    private static boolean isConstructor(final String name) {
        return name.startsWith("<") && name.endsWith(">");
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
