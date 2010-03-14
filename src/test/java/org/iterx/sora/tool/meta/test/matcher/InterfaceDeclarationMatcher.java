package org.iterx.sora.tool.meta.test.matcher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.declaration.ClassTypeDeclaration;
import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceTypeDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;
import org.iterx.sora.tool.meta.util.DeclarationReader;
import org.iterx.sora.tool.meta.util.DeclarationVisitor;

import java.util.Arrays;

public class InterfaceDeclarationMatcher extends BaseMatcher<Declaration<?>> {

    private final MatchesDeclarationVisitor matchesDeclarationVisitor;
    private final Declaration declaration;

    public InterfaceDeclarationMatcher(final InterfaceTypeDeclaration interfaceTypeDeclaration) {
        this.matchesDeclarationVisitor = new MatchesDeclarationVisitor(interfaceTypeDeclaration);
        this.declaration = interfaceTypeDeclaration;
    }

    public void describeTo(final Description description) {
        description.appendText("<" + declaration + ">");
    }

    public boolean matches(final Object object) {
        new DeclarationReader((Declaration<?>) object).accept(matchesDeclarationVisitor);
        return matchesDeclarationVisitor.matches();
    }

    private static class MatchesDeclarationVisitor implements DeclarationVisitor {

        private final InterfaceTypeDeclaration expectedInterfaceTypeDeclaration;
        private boolean matches;

        private MatchesDeclarationVisitor(final InterfaceTypeDeclaration interfaceTypeDeclaration) {
            this.expectedInterfaceTypeDeclaration = interfaceTypeDeclaration;
            this.matches = false;
        }

        public boolean matches() {
            return matches;
        }

        public void startClass(final ClassTypeDeclaration classTypeDeclaration) {}

        public void startInterface(final InterfaceTypeDeclaration interfaceTypeDeclaration) {
            matches = (expectedInterfaceTypeDeclaration != null &&
                       expectedInterfaceTypeDeclaration.equals(interfaceTypeDeclaration) &&
                       expectedInterfaceTypeDeclaration.getAccess().equals(interfaceTypeDeclaration.getAccess()) &&
                       Arrays.equals(expectedInterfaceTypeDeclaration.getModifiers(), interfaceTypeDeclaration.getModifiers()) &&
                       Arrays.equals(expectedInterfaceTypeDeclaration.getInterfaceTypes(), interfaceTypeDeclaration.getInterfaceTypes()) &&
                       Arrays.equals(expectedInterfaceTypeDeclaration.getFieldDeclarations(), interfaceTypeDeclaration.getFieldDeclarations()) &&
                       Arrays.equals(expectedInterfaceTypeDeclaration.getMethodDeclarations(), interfaceTypeDeclaration.getMethodDeclarations()));
        }

        public void field(final FieldDeclaration fieldDeclaration) {
            if(matches) matches &= new FieldDeclarationMatcher(expectedInterfaceTypeDeclaration.getFieldDeclaration(fieldDeclaration.getFieldName())).matches(fieldDeclaration);
        }

        public void constructor(final ConstructorDeclaration constructorDeclaration) {
            matches = false;
        }

        public void method(final MethodDeclaration methodDeclaration) {
            if(matches) matches &= new MethodDeclarationMatcher(expectedInterfaceTypeDeclaration.getMethodDeclaration(methodDeclaration.getMethodName(), methodDeclaration.getArgumentTypes())).matches(methodDeclaration);
        }
        
        public void endClass() {}

        public void endInterface() {}
    }
}