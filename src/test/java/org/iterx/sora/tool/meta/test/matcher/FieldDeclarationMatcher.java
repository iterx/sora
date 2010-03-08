package org.iterx.sora.tool.meta.test.matcher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;

import java.util.Arrays;

public class FieldDeclarationMatcher extends BaseMatcher<FieldDeclaration> {

    private final FieldDeclaration expectedFieldDeclaration;

    public FieldDeclarationMatcher(final FieldDeclaration fieldDeclaration) {
        this.expectedFieldDeclaration = fieldDeclaration;
    }

    public void describeTo(final Description description) {
        description.appendText("<" + expectedFieldDeclaration + ">");
    }

    public boolean matches(final Object object) {
        final FieldDeclaration fieldDeclaration = (FieldDeclaration) object;
        return (expectedFieldDeclaration.equals(fieldDeclaration) &&
                expectedFieldDeclaration.getFieldType().equals(fieldDeclaration.getFieldType()) &&
                expectedFieldDeclaration.getAccess().equals(fieldDeclaration.getAccess()) &&
                Arrays.equals(expectedFieldDeclaration.getModifiers(), fieldDeclaration.getModifiers()));


    }
}
