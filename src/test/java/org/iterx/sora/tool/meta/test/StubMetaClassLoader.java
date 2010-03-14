package org.iterx.sora.tool.meta.test;

import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.declaration.ClassTypeDeclaration;
import org.iterx.sora.tool.meta.support.asm.AsmCompiler;
import org.iterx.sora.tool.meta.type.ClassType;
import org.iterx.sora.tool.meta.type.InterfaceType;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;

public class StubMetaClassLoader extends MetaClassLoader {

    private final AsmCompiler asmCompiler;
    private final Map<String, byte[]> classes;
    private final boolean debug;

    public StubMetaClassLoader() {
        this(false);
    }

    public StubMetaClassLoader(final boolean debug) {
        this.classes = new HashMap<String, byte[]>();
        this.asmCompiler = new AsmCompiler(this);
        this.debug = debug;
    }

    @Override
    public Class<?> loadClass(final Type<?> type) throws ClassNotFoundException {
        if(debug) debug(asmCompiler.compile(loadDeclaration(type)));
        return super.loadClass(type);
    }

    public URL getResource(final String name) {
        try {
            return (classes.containsKey(name))?
                   new URL("vm", null, -1, name, new ClassURLStreamHandler(classes.get(name))) :
                   super.getResource(name);
        }
        catch(final MalformedURLException e) {
            return null;
        }
    }


    public void defineClass(final ClassTypeDeclaration classTypeDeclaration) {
        classes.put(toResource(classTypeDeclaration.getClassType().getName()),
                    asmCompiler.compile(classTypeDeclaration));
    }

    @Override
    protected Class<?> defineClass(final String name, final Declaration<?> declaration) {
        if(declaration.isClassDeclaration()) defineClass((ClassTypeDeclaration) declaration);
        return super.defineClass(name, declaration);
    }

    private class ClassURLStreamHandler extends URLStreamHandler {

        private final byte[] bytes;

        private ClassURLStreamHandler(final byte[] bytes) {
            this.bytes = bytes;
        }

        protected URLConnection openConnection(final URL url) throws IOException {
            return new URLConnection(url) {
                @Override
                public void connect() throws IOException {}

                @Override
                public InputStream getInputStream() throws IOException {
                    return new ByteArrayInputStream(bytes);
                }
            };
        }
    }

    private static void debug(final byte[] bytes) {
        new ClassReader(bytes).accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
    }

    private static String toResource(final String name) {
        return name.replace('.', '/') + ".class";
    }
}
