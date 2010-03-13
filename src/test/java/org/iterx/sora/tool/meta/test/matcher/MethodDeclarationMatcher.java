package org.iterx.sora.tool.meta.test.matcher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;

import java.util.Arrays;

public class MethodDeclarationMatcher extends BaseMatcher<MethodDeclaration> {

    private final MethodDeclaration expectedMethodDeclaration;

    public MethodDeclarationMatcher(final MethodDeclaration methodDeclaration) {
        this.expectedMethodDeclaration = methodDeclaration;
    }

    public void describeTo(final Description description) {
        description.appendText("<" + expectedMethodDeclaration + ">");
    }

    public boolean matches(final Object object) {
        final MethodDeclaration methodDeclaration = (MethodDeclaration) object;
        return (expectedMethodDeclaration.equals(methodDeclaration) &&
                expectedMethodDeclaration.getReturnType().equals(methodDeclaration.getReturnType()) &&
                expectedMethodDeclaration.getAccess().equals(methodDeclaration.getAccess()) &&
                Arrays.equals(expectedMethodDeclaration.getModifiers(), methodDeclaration.getModifiers()) &&
                Arrays.equals(expectedMethodDeclaration.getExceptionTypes(), methodDeclaration.getExceptionTypes())) &&
               new InstructionsMatcher(expectedMethodDeclaration.getInstructions()).matches(expectedMethodDeclaration.getInstructions());
    }
}
