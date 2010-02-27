package org.iterx.sora.tool.meta;

import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.Declaration;
import org.iterx.sora.tool.meta.support.asm.AsmCompiler;
import org.iterx.sora.tool.meta.support.asm.AsmExtractor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public final class MetaClassLoader {

    //TODO -> enable multiple compiler back ends....
    //TODO -> this should implement a class loader -> extended to support lookup of meta information
    //TODO -> should enable extracting of bytes[] from just a method???-> or do we just load entire class & extract...

    //public byte[] saveClass()

    public Declaration<?> loadClass(final Type type) throws ClassNotFoundException {
        try {
            //TODO: check if already loaded...
            final byte[] bytes = load(type.getName().replace('.', '/') + ".class");
            return AsmExtractor.extract(bytes);
        }
        catch(final IOException e) {
            throw new ClassNotFoundException();
        }
    }


    public ClassDeclaration defineClass(final Type type, final Type superType, final Type... interfaceTypes) {
        //return getAsmClassLoader().defineAsmClass(new LegacyClassDeclaration(type, superType, interfaceTypes));
        return null;
    }

    private byte[] load(final String resource) throws IOException {
        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resource);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for(int i = inputStream.read(); i != -1; i = inputStream.read()) byteArrayOutputStream.write(i);
        return byteArrayOutputStream.toByteArray();
    }

    public static void main(String[] args) throws Throwable {
        //dump(Type.class);
        debug(new ClassReader(TestClass.class.getName()));
        dump(TestClass.class);
    }

    public static abstract class TestClass {

        public abstract void method();
        
    }

    private static void dump(final Class cls) throws Throwable {
        final Declaration declaration = new MetaClassLoader().loadClass(Type.getType(cls));
        final byte[] bytes = AsmCompiler.compile(declaration);
        debug(new ClassReader(bytes));
        load(bytes);
    }


    private static void load(final byte[] bytes) {
        new DebugClassLoader().defineClass(bytes);
    }

    private static void debug(final ClassReader classReader) {
        classReader.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
    }

    private static class DebugClassLoader extends ClassLoader {

        public Class<?> defineClass(final byte[] bytes) {
            return super.defineClass(null, bytes, 0, bytes.length);
        }
    }

}
