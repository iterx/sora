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

public class ClassDeclarationMatcher extends BaseMatcher<Declaration<?>> {

    private final MatchesDeclarationVisitor matchesDeclarationVisitor;
    private final Declaration declaration;

    public ClassDeclarationMatcher(final ClassTypeDeclaration classTypeDeclaration) {
        this.matchesDeclarationVisitor = new MatchesDeclarationVisitor(classTypeDeclaration);
        this.declaration = classTypeDeclaration;
    }

    public void describeTo(final Description description) {
        description.appendText("<" + declaration + ">");
    }

    public boolean matches(final Object object) {
        new DeclarationReader((Declaration<?>) object).accept(matchesDeclarationVisitor);
        return matchesDeclarationVisitor.matches();
    }

    private static class MatchesDeclarationVisitor implements DeclarationVisitor {

        private final ClassTypeDeclaration expectedClassTypeDeclaration;
        private boolean matches;

        private MatchesDeclarationVisitor(final ClassTypeDeclaration classTypeDeclaration) {
            this.expectedClassTypeDeclaration = classTypeDeclaration;
            this.matches = true;
        }

        public boolean matches() {
            return matches;
        }

        public void startClass(final ClassTypeDeclaration classTypeDeclaration) {
            set(expectedClassTypeDeclaration != null &&
                expectedClassTypeDeclaration.equals(classTypeDeclaration) &&
                expectedClassTypeDeclaration.getAccess().equals(classTypeDeclaration.getAccess()) &&
                expectedClassTypeDeclaration.getSuperType().equals(classTypeDeclaration.getSuperType())&&
                Arrays.equals(expectedClassTypeDeclaration.getModifiers(), classTypeDeclaration.getModifiers()) &&
                Arrays.equals(expectedClassTypeDeclaration.getInterfaceTypes(), classTypeDeclaration.getInterfaceTypes()) &&
                Arrays.equals(expectedClassTypeDeclaration.getFieldDeclarations(), classTypeDeclaration.getFieldDeclarations()) &&
                Arrays.equals(expectedClassTypeDeclaration.getConstructorDeclarations(), classTypeDeclaration.getConstructorDeclarations()) &&
                Arrays.equals(expectedClassTypeDeclaration.getMethodDeclarations(), classTypeDeclaration.getMethodDeclarations()));
        }

        public void startInterface(final InterfaceTypeDeclaration interfaceTypeDeclaration) {
            set(false);
        }

        public void field(final FieldDeclaration fieldDeclaration) {
            set(new FieldDeclarationMatcher(expectedClassTypeDeclaration.getFieldDeclaration(fieldDeclaration.getFieldName())).matches(fieldDeclaration));
        }

        public void constructor(final ConstructorDeclaration constructorDeclaration) {
            set(new ConstructorDeclarationMatcher(expectedClassTypeDeclaration.getConstructorDeclaration(constructorDeclaration.getConstructorTypes())).matches(constructorDeclaration));
        }

        public void method(final MethodDeclaration methodDeclaration) {
            set(new MethodDeclarationMatcher(expectedClassTypeDeclaration.getMethodDeclaration(methodDeclaration.getMethodName(), methodDeclaration.getArgumentTypes())).matches(methodDeclaration));
        }
        
        public void endClass() {}

        public void endInterface() {}

        private void set(final boolean value) {
            if(!value) matches = false;
        }
    }
}
