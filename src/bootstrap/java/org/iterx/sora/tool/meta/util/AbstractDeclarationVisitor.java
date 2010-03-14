package org.iterx.sora.tool.meta.util;

import org.iterx.sora.tool.meta.declaration.ClassTypeDeclaration;
import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceTypeDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;

public abstract class AbstractDeclarationVisitor implements DeclarationVisitor {

    public void startClass(final ClassTypeDeclaration classTypeDeclaration) {}

    public void startInterface(final InterfaceTypeDeclaration interfaceTypeDeclaration) {}

    public void field(final FieldDeclaration fieldDeclaration) {}

    public void constructor(final ConstructorDeclaration constructorDeclaration) {}

    public void method(final MethodDeclaration methodDeclaration) {}

    public void endClass() {}

    public void endInterface() {}
}
