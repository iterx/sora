package org.iterx.sora.tool.meta.util;

import org.iterx.sora.tool.meta.declaration.ClassTypeDeclaration;
import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceTypeDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;

public interface DeclarationVisitor {
    
    void startClass(ClassTypeDeclaration classTypeDeclaration);

    void startInterface(InterfaceTypeDeclaration interfaceTypeDeclaration);

    void field(FieldDeclaration fieldDeclaration);

    void constructor(ConstructorDeclaration constructorDeclaration);

    void method(MethodDeclaration methodDeclaration);

    void endClass();

    void endInterface();
}


