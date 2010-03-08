package org.iterx.sora.tool.meta.test.matcher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;

import java.util.Arrays;

public class ConstructorDeclarationMatcher extends BaseMatcher<ConstructorDeclaration> {

    private final ConstructorDeclaration expectedConstructorDeclaration;

    public ConstructorDeclarationMatcher(final ConstructorDeclaration constructorDeclaration) {
        this.expectedConstructorDeclaration = constructorDeclaration;
    }

    public void describeTo(final Description description) {
        description.appendText("<" + expectedConstructorDeclaration + ">");
    }

    public boolean matches(final Object object) {
        final ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) object;
        return (expectedConstructorDeclaration.equals(constructorDeclaration) &&
                expectedConstructorDeclaration.getAccess().equals(constructorDeclaration.getAccess()) &&
                Arrays.equals(expectedConstructorDeclaration.getModifiers(), constructorDeclaration.getModifiers()) &&
                Arrays.equals(expectedConstructorDeclaration.getExceptionTypes(), constructorDeclaration.getExceptionTypes()));
    }
}