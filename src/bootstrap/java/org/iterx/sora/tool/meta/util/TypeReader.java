package org.iterx.sora.tool.meta.util;

import org.iterx.sora.collection.Map;
import org.iterx.sora.collection.map.HashMap;
import org.iterx.sora.tool.meta.Instruction;
import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.TypeDeclaration;
import org.iterx.sora.tool.meta.declaration.ClassTypeDeclaration;
import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceTypeDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;

import java.lang.reflect.Method;

public final class TypeReader {

    private static final Map<Class, Method> TYPE_DECLARATION_DISPATCH_TABLE = newTypeDeclarationDispatchTable();

    private final TypeDeclaration<?, ?> typeDeclaration;

    public TypeReader(final Class<?> cls) {
        this(MetaClassLoader.getSystemMetaClassLoader().loadType(cls));
    }

    public TypeReader(final Type<?> type) {
        this(type.getMetaClassLoader(), type.getMetaClassLoader().loadDeclaration(type));
    }

    public TypeReader(final TypeDeclaration<?, ?> typeDeclaration) {
        this(MetaClassLoader.getSystemMetaClassLoader(), typeDeclaration);
    }

    public TypeReader(final MetaClassLoader metaClassLoader, final TypeDeclaration<?, ?> typeDeclaration) {
        this.typeDeclaration = typeDeclaration;
    }

    public void accept(final TypeVisitor typeVisitor) {
        try {
            TYPE_DECLARATION_DISPATCH_TABLE.get(typeDeclaration.getClass()).invoke(this, typeVisitor, typeDeclaration);
        }
        catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void accept(final TypeVisitor typeVisitor, final ClassTypeDeclaration classTypeDeclaration) {
        typeVisitor.startClass(toAccess(classTypeDeclaration.getAccess()),
                               toModifiers(classTypeDeclaration.getModifiers()),
                               classTypeDeclaration.getClassType(),
                               classTypeDeclaration.getSuperType(),
                               classTypeDeclaration.getInterfaceTypes());
        for(final FieldDeclaration fieldDeclaration : classTypeDeclaration.getFieldDeclarations()) accept(typeVisitor, fieldDeclaration);
        for(final ConstructorDeclaration constructorDeclaration : classTypeDeclaration.getConstructorDeclarations()) accept(typeVisitor, constructorDeclaration);
        for(final MethodDeclaration methodDeclaration : classTypeDeclaration.getMethodDeclarations()) accept(typeVisitor, methodDeclaration);
        typeVisitor.endClass();
    }

    private void accept(final TypeVisitor typeVisitor, final InterfaceTypeDeclaration interfaceTypeDeclaration) {
        typeVisitor.startInterface(toAccess(interfaceTypeDeclaration.getAccess()),
                                   toModifiers(interfaceTypeDeclaration.getModifiers()),
                                   interfaceTypeDeclaration.getInterfaceType(),
                                   interfaceTypeDeclaration.getInterfaceTypes());
        for(final FieldDeclaration fieldDeclaration : interfaceTypeDeclaration.getFieldDeclarations()) accept(typeVisitor, fieldDeclaration);
        for(final MethodDeclaration methodDeclaration : interfaceTypeDeclaration.getMethodDeclarations()) accept(typeVisitor, methodDeclaration);
        typeVisitor.endInterface();
    }

    private void accept(final TypeVisitor typeVisitor, final FieldDeclaration fieldDeclaration) {
        typeVisitor.field(toAccess(fieldDeclaration.getAccess()),
                          toModifiers(fieldDeclaration.getModifiers()),
                          fieldDeclaration.getFieldName(),
                          fieldDeclaration.getFieldType(),
                          fieldDeclaration.getFieldValue());
    }

    private void accept(final TypeVisitor typeVisitor, final ConstructorDeclaration constructorDeclaration) {
        final InstructionVisitor instructionVisitor = typeVisitor.startConstructor(toAccess(constructorDeclaration.getAccess()),
                                                                              toModifiers(constructorDeclaration.getModifiers()),
                                                                              constructorDeclaration.getConstructorTypes(),
                                                                              constructorDeclaration.getExceptionTypes());
        if(instructionVisitor != null) accept(instructionVisitor, constructorDeclaration.getInstructions());
        typeVisitor.endConstructor();
    }

    private void accept(final TypeVisitor typeVisitor, final MethodDeclaration methodDeclaration) {
        final InstructionVisitor instructionVisitor = typeVisitor.startMethod(toAccess(methodDeclaration.getAccess()),
                                                                              toModifiers(methodDeclaration.getModifiers()),
                                                                              methodDeclaration.getMethodName(),
                                                                              methodDeclaration.getReturnType(),
                                                                              methodDeclaration.getArgumentTypes(),
                                                                              methodDeclaration.getExceptionTypes());
        if(instructionVisitor != null) accept(instructionVisitor, methodDeclaration.getInstructions());
        typeVisitor.endMethod();
    }

    private void accept(final InstructionVisitor instructionVisitor, final Instruction... instructions) {
        new InstructionReader(instructions).accept(instructionVisitor);
    }

    private TypeVisitor.Access toAccess(final TypeDeclaration.Access value) {
        return TypeVisitor.Access.valueOf(value.name());
    }

    private TypeVisitor.Modifier[] toModifiers(final TypeDeclaration.Modifier[] values) {
        final TypeVisitor.Modifier[] modifiers = new TypeVisitor.Modifier[values.length];
        for(int i = values.length; i-- != 0;) modifiers[i] = TypeVisitor.Modifier.valueOf(values[i].name());
        return modifiers;
    }

    private static Map<Class, Method> newTypeDeclarationDispatchTable() {
        final Map<Class, Method> dispatchTable = new HashMap<Class, Method>();
        for(final Method method : TypeReader.class.getDeclaredMethods()) {
            final Class[] parameterTypes = method.getParameterTypes();
            if("accept".equals(method.getName()) &&
               parameterTypes.length == 2 &&
               TypeVisitor.class.equals(method.getParameterTypes()[0])) dispatchTable.put(method.getParameterTypes()[1], method);
        }
        return dispatchTable;
    }

}
