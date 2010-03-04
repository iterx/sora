package org.iterx.sora.tool.meta;

import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.Declaration;
import org.iterx.sora.tool.meta.declaration.InterfaceDeclaration;
import org.iterx.sora.tool.meta.support.asm.AsmCompiler;
import org.iterx.sora.tool.meta.support.asm.AsmExtractor;
import org.iterx.sora.tool.meta.type.ClassMetaType;
import org.iterx.sora.tool.meta.type.InterfaceMetaType;
import org.iterx.sora.tool.meta.type.Type;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//TODO: add option to save...
//TODO: issues -> how do we change type from class<->interface for a declaration (require separate class loaders)???
//TODO: Metatyping -> how do we resolve meta type???? -> require preloading of referenced declarations... and or lookup???

public final class MetaClassLoader extends SecureClassLoader implements Closeable {

    private static final MetaClassLoader DEFAULT_META_CLASS_LOADER = new MetaClassLoader();

    private final Map<String, Declaration<?>> declarationByNames;
    private final Map<String, Type<?>> typeByNames;

    public MetaClassLoader() {
        this.declarationByNames = new ConcurrentHashMap<String, Declaration<?>>();
        this.typeByNames = new ConcurrentHashMap<String, Type<?>>();
    }

    public MetaClassLoader(final ClassLoader classLoader) {
        super(classLoader);
        this.declarationByNames = new ConcurrentHashMap<String, Declaration<?>>();
        this.typeByNames = new ConcurrentHashMap<String, Type<?>>();
    }

    public static MetaClassLoader getMetaClassLoader() {
        //TODO: better define static lookup for default metaClassLoader and allow for hierarchy
        return DEFAULT_META_CLASS_LOADER;
    }

    //TODO: do we want this (and get decls to call back onto metaclassloader for new???) -> or do we register decl with metaclassloader?
    @Deprecated
    public ClassDeclaration newDeclaration(final ClassMetaType type) {
        //TODO: 1) ClassDeclaration.newClassDeclaration creates object
        //TODO: 2) calls metaclassloader.defineDeclaration(decl) -> registers object
        //TODO: 3) returns object
        return setDeclaration(type.getName(), ClassDeclaration.newClassDeclaration(type));
    }

    @Deprecated
    public InterfaceDeclaration newDeclaration(final InterfaceMetaType type) {
        return setDeclaration(type.getName(), InterfaceDeclaration.newInterfaceDeclaration(type));
    }

    public ClassDeclaration loadDeclaration(final ClassMetaType type) throws ClassNotFoundException {
        return (ClassDeclaration) loadDeclaration((Type<?>) type);
    }

    public InterfaceDeclaration loadDeclaration(final InterfaceMetaType type) throws ClassNotFoundException {
        return (InterfaceDeclaration) loadDeclaration((Type<?>) type);
    }

    public <T extends Declaration> T defineDeclaration(final T declaration) {
        //TODO: register & check if already defined....
        throw new UnsupportedOperationException();
    }
    public <T extends Type> T defineType(final T type) {
        //TODO: register & check if already defined....
        throw new UnsupportedOperationException();
    }

    //TODO: fix up...
    public Declaration<?> loadDeclaration(final byte[] bytes) {
        return defineDeclaration(bytes);
    }
    //TODO: fix up
    public byte[] storeDeclaration(final Declaration<?> declaration) {
        return compileDeclaration(declaration);
    }

    public Type<?> loadType(final String name) throws ClassNotFoundException {
        final Type<?> type  = getType(name);
        return (type != null)? type : findType(name);
    }

    public Class<?> loadClass(final Type<?> type) throws ClassNotFoundException {
        return loadClass(type.getName());
    }


    public void close() {
        //TODO:....
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        final Declaration<?> declaration = getDeclaration(name);
        return (declaration != null)? defineClass(name, declaration) : super.findClass(name);
    }

    private Declaration<?> loadDeclaration(final Type<?> type) throws ClassNotFoundException {
        final Declaration<?> declaration = getDeclaration(type.getName());
        return (declaration != null)? declaration : findDeclaration(type.getName());
    }

    private Declaration<?> findDeclaration(final String name) throws ClassNotFoundException {
        try {
            final URL resource = getResource(toResource(name));
            return setDeclaration(name, defineDeclaration(loadResource(resource)));
        }
        catch(final IOException e) {
            throw new ClassNotFoundException("Undefined declaration '" + name + "'", e);
        }
    }

    private Type<?> findType(final String name) throws ClassNotFoundException {
        try {
            final URL resource = getResource(toResource(name));
            return defineType(loadResource(resource));
        }
        catch(final IOException e) {
            throw new ClassNotFoundException("Undefined declaration '" + name + "'", e);
        }
    }

    private byte[] compileDeclaration(final Declaration<?> declaration) {
        return AsmCompiler.compile(declaration);
    }

    private Declaration<?> defineDeclaration(final byte[] bytes) {
        return AsmExtractor.extract(bytes);
    }

    private Type<?> defineType(final byte[] bytes) {
        return AsmExtractor.getType(bytes);
    }

    private Class<?> defineClass(final String name, final Declaration<?> declaration) {
        final byte[] bytes = AsmCompiler.compile(declaration);
        return super.defineClass(name, bytes, 0, bytes.length);
    }

    private byte[] loadResource(final URL resource) throws IOException {
        final InputStream inputStream = resource.openStream();
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for(int i = inputStream.read(); i != -1; i = inputStream.read()) byteArrayOutputStream.write(i);
        return byteArrayOutputStream.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private <T extends Type<?>> T getType(final String name) {
        return (T) typeByNames.get(name);
    }

    private <T extends Type<?>> T setType(final String name, final T type) {
        if(typeByNames.containsKey(name)) throw new IllegalStateException("Type already declared for '" + name + "'");
        typeByNames.put(name, type);
        return type;
    }

    @SuppressWarnings("unchecked")
    private <T extends Declaration<?>> T getDeclaration(final String name) {
        return (T) declarationByNames.get(name);
    }

    private <T extends Declaration<?>> T setDeclaration(final String name, final T declaration) {
        if(declarationByNames.containsKey(name)) throw new IllegalStateException("Declaration already declared for '" + name + "'");
        declarationByNames.put(name, declaration);
        return declaration;
    }

    private static String toResource(final String name) {
        return name.replace('.', '/') + ".class";
    }

}
