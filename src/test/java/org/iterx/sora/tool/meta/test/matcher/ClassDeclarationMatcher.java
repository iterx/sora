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

public class ClassDeclarationMatcher extends BaseMatcher<Declaration<?>> {

    private final MatchesDeclarationVisitor matchesDeclarationVisitor;
    private final Declaration declaration;

    public ClassDeclarationMatcher(final ClassDeclaration classDeclaration) {
        this.matchesDeclarationVisitor = new MatchesDeclarationVisitor(classDeclaration);
        this.declaration = classDeclaration;
    }

    public void describeTo(final Description description) {
        description.appendText("<" + declaration + ">");
    }

    public boolean matches(final Object object) {
        new DeclarationReader((Declaration<?>) object).accept(matchesDeclarationVisitor);
        return matchesDeclarationVisitor.matches();
    }

    private static class MatchesDeclarationVisitor implements DeclarationVisitor {

        private final ClassDeclaration expectedClassDeclaration;
        private boolean matches;

        private MatchesDeclarationVisitor(final ClassDeclaration classDeclaration) {
            this.expectedClassDeclaration = classDeclaration;
            this.matches = false;
        }

        public boolean matches() {
            return matches;
        }

        public void startClass(final ClassDeclaration classDeclaration) {
            matches = (expectedClassDeclaration != null &&
                       expectedClassDeclaration.equals(classDeclaration) &&
                       expectedClassDeclaration.getAccess().equals(classDeclaration.getAccess()) &&
                       expectedClassDeclaration.getSuperType().equals(classDeclaration.getSuperType())&&
                       Arrays.equals(expectedClassDeclaration.getModifiers(), classDeclaration.getModifiers()) &&
                       Arrays.equals(expectedClassDeclaration.getInterfaceTypes(), classDeclaration.getInterfaceTypes()) &&
                       Arrays.equals(expectedClassDeclaration.getFieldDeclarations(), classDeclaration.getFieldDeclarations()) &&
                       Arrays.equals(expectedClassDeclaration.getConstructorDeclarations(), classDeclaration.getConstructorDeclarations()) &&
                       Arrays.equals(expectedClassDeclaration.getMethodDeclarations(), classDeclaration.getMethodDeclarations()));
        }

        public void startInterface(final InterfaceDeclaration interfaceDeclaration) {}

        public void field(final FieldDeclaration fieldDeclaration) {
            if(matches) matches &= new FieldDeclarationMatcher(expectedClassDeclaration.getFieldDeclaration(fieldDeclaration.getFieldName())).matches(fieldDeclaration);
        }

        public void constructor(final ConstructorDeclaration constructorDeclaration) {
            if(matches) matches &= new ConstructorDeclarationMatcher(expectedClassDeclaration.getConstructorDeclaration(constructorDeclaration.getConstructorTypes())).matches(constructorDeclaration);
        }

        public void method(final MethodDeclaration methodDeclaration) {
            if(matches) matches &= new MethodDeclarationMatcher(expectedClassDeclaration.getMethodDeclaration(methodDeclaration.getMethodName(), methodDeclaration.getArgumentTypes())).matches(methodDeclaration);
        }
        
        public void endClass() {}

        public void endInterface() {}
    }
}
