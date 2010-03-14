package org.iterx.sora.tool.meta;

import org.iterx.sora.tool.meta.declaration.ClassTypeDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceTypeDeclaration;
import org.iterx.sora.tool.meta.support.asm.AsmCompiler;
import org.iterx.sora.tool.meta.support.asm.AsmExtractor;
import org.iterx.sora.tool.meta.type.AnnotationType;
import org.iterx.sora.tool.meta.type.ArrayType;
import org.iterx.sora.tool.meta.type.ClassType;
import org.iterx.sora.tool.meta.type.EnumType;
import org.iterx.sora.tool.meta.type.InterfaceType;
import org.iterx.sora.tool.meta.type.PrimitiveType;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

//TODO: add option to save... -> MetaClassStorer
//TODO: issues -> how do we change type from class<->interface for a declaration (require separate class loaders)???
//TODO:           or do we implement a redefine call???
public class MetaClassLoader extends SecureClassLoader implements Closeable {

    //TODO: ClassDecl & InterfaceDecl -> should be class/interface meta types as well



    private static final MetaClassLoader SYSTEM_META_CLASS_LOADER = new MetaClassLoader();

    private final ConcurrentMap<String, Declaration<?>> declarationByNames;
    private final ConcurrentMap<String, Type<?>> typeByNames;
    private final AsmExtractor asmExtractor;
    private final AsmCompiler asmCompiler;
    private final MetaClassLoader parent;

    public MetaClassLoader() {
        this(getSystemClassLoader());
    }

    public MetaClassLoader(final ClassLoader classLoader) {
        this(MetaClassLoader.getSystemMetaClassLoader(), classLoader);
    }

    public MetaClassLoader(final MetaClassLoader metaClassLoader) {
        this(metaClassLoader, getSystemClassLoader());
    }

    public MetaClassLoader(final MetaClassLoader metaClassLoader, final ClassLoader classLoader) {
        super(classLoader);
        this.declarationByNames = new ConcurrentHashMap<String, Declaration<?>>();
        this.typeByNames = new ConcurrentHashMap<String, Type<?>>();
        this.asmExtractor = new AsmExtractor(this);
        this.asmCompiler = new AsmCompiler(this);
        this.parent = metaClassLoader;
    }

    public static MetaClassLoader getSystemMetaClassLoader() {
        return SYSTEM_META_CLASS_LOADER;
    }

    @SuppressWarnings("unchecked")
    public <S extends AbstractTypeDeclaration<?, S>> S loadDeclaration(final String name) throws ClassNotFoundException {
        final Declaration<?> declaration = getDeclaration(name);
        return (declaration != null)? (S) declaration : (S) findDeclaration(name);
    } 

    @SuppressWarnings("unchecked")
    //TODO: Fix generics
    public <S extends AbstractTypeDeclaration> S loadDeclaration(final Type<?> type) throws ClassNotFoundException {
        final Declaration<?> declaration = getDeclaration(type.getName());
        return (declaration != null)? (S) declaration : (S) findDeclaration(type.getName());
    }

/*
    public ClassTypeDeclaration loadDeclaration(final ClassType type) throws ClassNotFoundException {
        return (ClassTypeDeclaration) loadDeclaration((Type<?>) type);
    }

    public InterfaceTypeDeclaration loadDeclaration(final InterfaceType type) throws ClassNotFoundException {
        return (InterfaceTypeDeclaration) loadDeclaration((Type<?>) type);
    }
*/

    @SuppressWarnings("unchecked")
    public <T extends Type> T loadType(final String name) throws ClassNotFoundException {
        final T type = getType(name);
        return (type != null)? type : (T) findType(name);
    }

    @SuppressWarnings("unchecked")
    public <T extends Type> T loadType(final Class<?> cls) throws ClassNotFoundException {
        final T type = getType(cls.getName());
        return (type != null)? type : (T) defineType(cls);
    }

    public Class<?> loadClass(final Type<?> type) throws ClassNotFoundException {
        return loadClass(type.getName());
    }

    public void close() {
        throw new UnsupportedOperationException();
    }

    <T extends Declaration> T defineDeclaration(final Type<?> type, final T declaration) {
        return setDeclaration(type.getName(), declaration);
    }

    <T extends Type> T defineType(final T type) {
        return setType(type.getName(), type);
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        final Declaration<?> declaration = getDeclaration(name);
        return (declaration != null)? defineClass(name, declaration) : super.findClass(name);
    }

    protected Declaration<?> findDeclaration(final String name) throws ClassNotFoundException {
        try {
            final URL resource = getResource(toResource(name));
            return defineDeclaration(loadResource(resource));
        }
        catch(final Exception e) {
            throw new ClassNotFoundException("Undefined declaration '" + name + "'", e);
        }
    }

    protected Type<?> findType(final String name) throws ClassNotFoundException {
        try {
            if(isArray(name))
                return ArrayType.newType(loadType(toArrayType(name)));
            else {
                final URL resource = getResource(toResource(name));
                return defineType(loadResource(resource));
            }
        }
        catch(final Exception e) {
            throw new ClassNotFoundException("Undefined declaration '" + name + "'", e);
        }
    }

    protected Class<?> defineClass(final String name, final Declaration<?> declaration) {
        final byte[] bytes = asmCompiler.compile(declaration);
        return super.defineClass(name, bytes, 0, bytes.length);
    }

/*
    private Declaration<?> loadDeclaration(final Type<?> type) throws ClassNotFoundException {
        final Declaration<?> declaration = getDeclaration(type.getName());
        return (declaration != null)? declaration : findDeclaration(type.getName());
    }
*/

    private byte[] compileDeclaration(final Declaration<?> declaration) {
        return asmCompiler.compile(declaration);
    }

    private Declaration<?> defineDeclaration(final byte[] bytes) {
        return asmExtractor.extract(bytes);
    }

    private Type<?> defineType(final byte[] bytes) {
        return asmExtractor.getType(bytes);
    }

    private Type<?> defineType(final Class cls) throws ClassNotFoundException {
        final String name = cls.getName();
        return (cls.isInterface())? InterfaceType.newType(this, name) :
               (cls.isPrimitive())? PrimitiveType.newType(this, name) :
               (cls.isAnnotation())? AnnotationType.newType(this, name) :
               (cls.isEnum())? EnumType.newType(this, name) :
               (cls.isArray())? ArrayType.newType(this, loadType(toArrayType(name))) :
               ClassType.newType(this, name);
    }

    private byte[] loadResource(final URL resource) throws IOException {
        final InputStream inputStream = resource.openStream();
        try {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            for(int i = inputStream.read(); i != -1; i = inputStream.read()) byteArrayOutputStream.write(i);
            return byteArrayOutputStream.toByteArray();
        }
        finally {
            inputStream.close();
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Type<?>> T getType(final String name) {
        final T type = (T) typeByNames.get(name);
        return (type != null)? type :
               (parent != null)? parent.<T>getType(name) :
               null;
    }

    private <T extends Type<?>> T setType(final String name, final T type) {
        if(null != typeByNames.putIfAbsent(name, type))
            throw new IllegalStateException("Type already declared for '" + name + "'");
        return type;
    }

    @SuppressWarnings("unchecked")
    private <T extends Declaration<?>> T getDeclaration(final String name) {
        final T declaration = (T) declarationByNames.get(name);
        return (declaration != null)? declaration :
               (parent != null)? parent.<T>getDeclaration(name) :
               null;
    }

    private <T extends Declaration<?>> T setDeclaration(final String name, final T declaration) {
        if(null != declarationByNames.putIfAbsent(name, declaration))
            throw new IllegalStateException("Declaration already declared for '" + name + "'");
        return declaration;
    }

    private static String toResource(final String name) {
        return name.replace('.', '/') + ".class";
    }

    private static boolean isArray(final String name) {
        return name.endsWith("[]");
    }

    private static String toArrayType(final String name) {
        return name.substring(0, name.length() - 2);
    }
}
