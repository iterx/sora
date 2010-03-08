package org.iterx.sora.tool.meta.test.matcher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;
import org.iterx.sora.tool.meta.util.DeclarationReader;
import org.iterx.sora.tool.meta.util.DeclarationVisitor;

import java.util.Arrays;

public class InterfaceDeclarationMatcher extends BaseMatcher<Declaration<?>> {

    private final MatchesDeclarationVisitor matchesDeclarationVisitor;
    private final Declaration declaration;

    public InterfaceDeclarationMatcher(final InterfaceDeclaration interfaceDeclaration) {
        this.matchesDeclarationVisitor = new MatchesDeclarationVisitor(interfaceDeclaration);
        this.declaration = interfaceDeclaration;
    }

    public void describeTo(final Description description) {
        description.appendText("<" + declaration + ">");
    }

    public boolean matches(final Object object) {
        new DeclarationReader((Declaration<?>) object).accept(matchesDeclarationVisitor);
        return matchesDeclarationVisitor.matches();
    }

    private static class MatchesDeclarationVisitor implements DeclarationVisitor {

        private final InterfaceDeclaration expectedInterfaceDeclaration;
        private boolean matches;

        private MatchesDeclarationVisitor(final InterfaceDeclaration interfaceDeclaration) {
            this.expectedInterfaceDeclaration = interfaceDeclaration;
            this.matches = false;
        }

        public boolean matches() {
            return matches;
        }

        public void startClass(final ClassDeclaration classDeclaration) {}

        public void startInterface(final InterfaceDeclaration interfaceDeclaration) {
            matches = (expectedInterfaceDeclaration != null &&
                       expectedInterfaceDeclaration.equals(interfaceDeclaration) &&
                       expectedInterfaceDeclaration.getAccess().equals(interfaceDeclaration.getAccess()) &&
                       Arrays.equals(expectedInterfaceDeclaration.getModifiers(), interfaceDeclaration.getModifiers()) &&
                       Arrays.equals(expectedInterfaceDeclaration.getInterfaceTypes(), interfaceDeclaration.getInterfaceTypes()) &&
                       Arrays.equals(expectedInterfaceDeclaration.getFieldDeclarations(), interfaceDeclaration.getFieldDeclarations()) &&
                       Arrays.equals(expectedInterfaceDeclaration.getMethodDeclarations(), interfaceDeclaration.getMethodDeclarations()));
        }

        public void field(final FieldDeclaration fieldDeclaration) {
            if(matches) matches &= new FieldDeclarationMatcher(expectedInterfaceDeclaration.getFieldDeclaration(fieldDeclaration.getFieldName())).matches(fieldDeclaration);
        }

        public void constructor(final ConstructorDeclaration constructorDeclaration) {
            matches = false;
        }

        public void method(final MethodDeclaration methodDeclaration) {
            if(matches) matches &= new MethodDeclarationMatcher(expectedInterfaceDeclaration.getMethodDeclaration(methodDeclaration.getMethodName(), methodDeclaration.getArgumentTypes())).matches(methodDeclaration);
        }
        
        public void endClass() {}

        public void endInterface() {}
    }
}