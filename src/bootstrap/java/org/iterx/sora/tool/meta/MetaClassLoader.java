package org.iterx.sora.tool.meta;

import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.Declaration;
import org.iterx.sora.tool.meta.declaration.InterfaceDeclaration;
import org.iterx.sora.tool.meta.support.asm.AsmCompiler;
import org.iterx.sora.tool.meta.support.asm.AsmExtractor;
import org.iterx.sora.tool.meta.type.Type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//TODO: add option to save...
//TODO: issues -> how do we change type from class<->interface for a declaration (require separate class loaders)???
//TODO: Metatyping -> how do we resolve meta type???? -> require preloading of referenced declarations... and or lookup???

public final class MetaClassLoader extends SecureClassLoader {

    private final Map<String, Declaration<?>> declarationByNames;

    public MetaClassLoader() {
        super();
        this.declarationByNames = new ConcurrentHashMap<String, Declaration<?>>();
    }

    public MetaClassLoader(final ClassLoader classLoader) {
        super(classLoader);
        this.declarationByNames = new ConcurrentHashMap<String, Declaration<?>>();
    }

    @SuppressWarnings("unchecked")
    public <T extends Declaration<T>, S extends Type.MetaType<T>> T loadDeclaration(final Type<S> type) throws ClassNotFoundException {
        final String name = type.getName();
        final Declaration<?> declaration = getDeclaration(name);
        return (declaration != null)? (T) declaration : (T) findDeclaration(name);
    }

    @SuppressWarnings("unchecked")
    public <T extends Declaration<T>, S extends Type.MetaType<T>> T newDeclaration(final Type<S> type) {
        final S metaType = type.getMetaType();
        final String name = type.getName();
        if(Type.CLASS_META_TYPE.equals(metaType))
            return setDeclaration(name, (T) (Declaration<?>) ClassDeclaration.newClassDeclaration((Type<Type.ClassMetaType>) (Type<?>) type));
        else if(Type.INTERFACE_META_TYPE.equals(metaType))
            return setDeclaration(name, (T) (Declaration<?>) InterfaceDeclaration.newInterfaceDeclaration((Type<Type.InterfaceMetaType>) (Type<?>) type));
        throw new UnsupportedOperationException();
    }

    public Class<?> loadClass(final Type type) throws ClassNotFoundException {
        return loadClass(type.getName());
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        final Declaration<?> declaration = getDeclaration(name);
        return (declaration != null)? defineClass(name, declaration) : super.findClass(name);
    }

    private Declaration<?> findDeclaration(final String name) throws ClassNotFoundException {
        try {
            final URL resource = getResource(toResource(name));
            final Declaration<?> declaration = AsmExtractor.extract(loadResource(resource));
            return setDeclaration(name, declaration);
        }
        catch(final IOException e) {
            throw new ClassNotFoundException("Undefined declaration '" + name + "'", e);
        }
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

    private Declaration<?> getDeclaration(final String name) {
        return declarationByNames.get(name);
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
