package org.iterx.sora.tool.meta.support.asm;

import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AsmClassLoader extends ClassLoader {

    private static final AsmClassLoader ASM_CLASS_LOADER = new AsmClassLoader();

    private final ConcurrentMap<Type, ClassDeclaration> asmClassByType;

    public AsmClassLoader() {
        asmClassByType = new ConcurrentHashMap<Type, ClassDeclaration>();
    }

    public static AsmClassLoader getAsmClassLoader() {
        return ASM_CLASS_LOADER;
    }

    public static ClassDeclaration defineClass(final Type type, final Type superType, final Type... interfaceTypes) {
        return getAsmClassLoader().defineAsmClass(new ClassDeclaration(type, superType, interfaceTypes));
    }

    ClassDeclaration findAsmClass(final Type type) throws ClassNotFoundException {
        final ClassDeclaration classDeclaration = asmClassByType.get(type);
        if(classDeclaration != null) return classDeclaration;

        throw new ClassNotFoundException("Undefined ClassDeclaration for Type '" + type + "'");
    }

    ClassDeclaration defineAsmClass(final ClassDeclaration classDeclaration) {
        final Type type = classDeclaration.getType();
        if(asmClassByType.putIfAbsent(type, classDeclaration) == null) return classDeclaration;

        throw new IllegalArgumentException("Redefining ClassDeclaration for Type '" + type + "'");
    }

    @Override
    protected Class<?> findClass(final String className) throws ClassNotFoundException {
        final ClassDeclaration classDeclaration = asmClassByType.get(Type.getObjectType(className));
        return (classDeclaration != null) ? defineClass(classDeclaration) : super.findClass(className);
    }
        
    private Class<?> defineClass(final ClassDeclaration classDeclaration) {
        final byte[] bytes = AsmCompiler.compile(classDeclaration);
        debug(bytes);
        return defineClass(null, bytes, 0, bytes.length);
    }

    private void debug(final byte[] bytes) {
        new ClassReader(bytes).accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
    }

}