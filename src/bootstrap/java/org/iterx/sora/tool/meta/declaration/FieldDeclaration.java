package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.Type;


public final class FieldDeclaration extends Declaration<FieldDeclaration> {

    public static final Modifier[] EMPTY_MODIFIERS = new Modifier[0];

    public enum Access implements Declaration.Access {  PUBLIC, PROTECTED, PRIVATE, DEFAULT }
    public enum Modifier implements Declaration.Modifier { VOLATILE, STATIC, FINAL }

    private final String fieldName;
    private Type<?> fieldType;
    private Access access;
    private Modifier[] modifiers;
    private Object fieldValue;

    private FieldDeclaration(final String fieldName) {
        this.access = Access.PRIVATE;
        this.modifiers = EMPTY_MODIFIERS;
        this.fieldName = fieldName;
    }

    public static FieldDeclaration newFieldDeclaration(final String fieldName, final Type<?> fieldType) {
        assertFieldName(fieldName);
        return new FieldDeclaration(fieldName).setFieldType(fieldType);
    }

    @Override
    public boolean isFieldDeclaration() {
        return true;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Type<?> getFieldType() {
        return fieldType;
    }

    public FieldDeclaration setFieldType(final Type<?> fieldType) {
        assertType(fieldType);
        this.fieldType = fieldType;
        return this;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public FieldDeclaration setFieldValue(final Integer fieldValue) {
        this.fieldValue = fieldValue;
        return this;
    }
    public FieldDeclaration setFieldValue(final Long fieldValue) {
        this.fieldValue = fieldValue;
        return this;
    }

    public FieldDeclaration setFieldValue(final Float fieldValue) {
        this.fieldValue = fieldValue;
        return this;
    }

    public FieldDeclaration setFieldValue(final Double fieldValue) {
        this.fieldValue = fieldValue;
        return this;
    }

    public FieldDeclaration setFieldValue(final String fieldValue) {
        this.fieldValue = fieldValue;
        return this;
    }

    public Access getAccess() {
        return access;
    }

    public FieldDeclaration setAccess(final Access access) {
        assertAccess(access);
        this.access = access;
        return this;
    }

    public Modifier[] getModifiers() {
        return modifiers;
    }

    public FieldDeclaration setModifiers(final Modifier... modifiers) {
        assertModifiers(modifiers);
        this.modifiers = modifiers;
        return this;
    }

    @Override
    public int hashCode() {
        return fieldName.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        return (this ==  object) ||
               (object != null && object.getClass() == getClass() && fieldName.equals(((FieldDeclaration) object).fieldName));
    }

    @Override
    public String toString() {
        return new StringBuilder().
                append("FieldDeclaration: ").
                append(fieldName).
                append(" ").
                append(fieldType).
                toString();
    }

    private static void assertFieldName(final String fieldName) {
        if(fieldName == null) throw new IllegalArgumentException("fieldName == null");
    }

    private static void assertType(final Type<?>... types) {
        if(types == null) throw new IllegalArgumentException("type == null");
        for(Type type : types) if(type == null) throw new IllegalArgumentException("type == null");
    }

    private static void assertAccess(final Access access) {
        if(access == null) throw new IllegalArgumentException("access == null");
    }

    private static void assertModifiers(final Modifier... modifiers) {
        if(modifiers == null) throw new IllegalArgumentException("modifiers == null");
    }
}
