package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.tool.meta.AbstractDeclaration;
import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.value.Constant;


public final class FieldDeclaration extends AbstractDeclaration<FieldDeclaration> {

    public static final Modifier[] EMPTY_MODIFIERS = new Modifier[0];

    public enum Access implements Declaration.Access {  PUBLIC, PROTECTED, PRIVATE, DEFAULT }
    public enum Modifier implements Declaration.Modifier { VOLATILE, STATIC, FINAL }

    private final String fieldName;
    private Type<?> fieldType;
    private Access access;
    private Modifier[] modifiers;
    private Constant fieldValue;

    private FieldDeclaration(final Type<?> fieldType, final String fieldName) {
        this.access = Access.PRIVATE;
        this.modifiers = EMPTY_MODIFIERS;
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.fieldValue = Constant.VOID;
    }

    public static FieldDeclaration newFieldDeclaration(final Type<?> fieldType, final String fieldName) {
        assertFieldName(fieldName);
        return new FieldDeclaration(fieldType, fieldName);
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

    public Constant getFieldValue() {
        return fieldValue;
    }

    public FieldDeclaration setFieldValue(final Constant fieldValue) {
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
