package org.iterx.sora.tool.meta.util;

import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;

public abstract class AbstractDeclarationVisitor implements DeclarationVisitor {

    public void startClass(final ClassDeclaration classDeclaration) {}

    public void startInterface(final InterfaceDeclaration interfaceDeclaration) {}

    public void field(final FieldDeclaration fieldDeclaration) {}

    public void constructor(final ConstructorDeclaration constructorDeclaration) {}

    public void method(final MethodDeclaration methodDeclaration) {}

    public void endClass() {}

    public void endInterface() {}
}
