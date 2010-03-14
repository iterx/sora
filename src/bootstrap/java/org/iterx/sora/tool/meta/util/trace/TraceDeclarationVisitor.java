package org.iterx.sora.tool.meta.util.trace;

import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.declaration.ClassTypeDeclaration;
import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceTypeDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;
import org.iterx.sora.tool.meta.util.DeclarationVisitor;
import org.iterx.sora.tool.meta.util.InstructionReader;

import java.io.OutputStream;
import java.io.PrintStream;

public class TraceDeclarationVisitor implements DeclarationVisitor {

    private final PrintStream printStream;

    public TraceDeclarationVisitor(final OutputStream outputStream) {
        printStream = (outputStream instanceof PrintStream)? (PrintStream) outputStream : new PrintStream(outputStream);
    }

    public void startClass(final ClassTypeDeclaration classTypeDeclaration) {
        print(classTypeDeclaration.getAccess(), classTypeDeclaration.getModifiers());
        print(" ");
        print(classTypeDeclaration.getClassType());
        print(" extends ");
        print(classTypeDeclaration.getSuperType());
        if(classTypeDeclaration.getInterfaceTypes().length != 0) {
            print(" implements ");
            print(classTypeDeclaration.getInterfaceTypes());
        }
        print(" {\n");
    }

    public void startInterface(final InterfaceTypeDeclaration interfaceTypeDeclaration) {
    }

    public void field(final FieldDeclaration fieldDeclaration) {
        print(fieldDeclaration.getAccess(), fieldDeclaration.getModifiers());
        print(" ");
        print(fieldDeclaration.getFieldType());
        print(" ");
        print(fieldDeclaration.getFieldName());
        print("\n");
    }

    public void constructor(final ConstructorDeclaration constructorDeclaration) {
        print(constructorDeclaration.getAccess(), constructorDeclaration.getModifiers());
        print(" <init>(");
        print(constructorDeclaration.getConstructorTypes());
        print(")");
        //TODO: throws clause
        print("{\n");
        new InstructionReader(constructorDeclaration.getInstructions()).accept(new TraceInstructionVisitor(printStream));
        print("}\n");
    }

    public void method(final MethodDeclaration methodDeclaration) {
        print(methodDeclaration.getAccess(), methodDeclaration.getModifiers());
        print(" ");
        print(methodDeclaration.getMethodName());
        print("(");
        print(methodDeclaration.getArgumentTypes());
        print(")");
        //TODO: throws clause
        print("{\n");
        new InstructionReader(methodDeclaration.getInstructions()).accept(new TraceInstructionVisitor(printStream));
        print("}\n");
    }

    public void endClass() {
        print("}\n");
    }

    public void endInterface() {
    }

    private void print(final Declaration.Access access, final Declaration.Modifier... modifiers) {
        printStream.print(access.name().toLowerCase());
        if(modifiers.length > 0) {
            printStream.print(" ");
            printStream.print(modifiers[0]);
            for(int i = 1, length = modifiers.length; i < length; i++) {
                printStream.print(" ");
                printStream.print(modifiers[i].name().toLowerCase());
            }
        }
    }

    private void print(final Type... types) {
        if(types.length > 0) {
            printStream.print(types[0].getName());
            for(int i = 1, length = types.length; i < length; i++) {
                printStream.print(" ,");
                printStream.print(types[i].getName());
            }
        }
    }

    private void print(final String... strings) {
        if(strings.length > 0) {
            printStream.print(strings[0]);
            for(int i = 1, length = strings.length; i < length; i++) {
                printStream.print(" ");
                printStream.print(strings[i]);
            }
        }
    }

    private void print(int padding) {
        for(int i = 0; i < padding; i++) printStream.print(" ");
    }
}
