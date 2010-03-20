package org.iterx.sora.tool.meta.test.matcher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.Value;
import org.iterx.sora.tool.meta.util.InstructionVisitor;
import org.iterx.sora.tool.meta.util.TypeReader;
import org.iterx.sora.tool.meta.util.TypeVisitor;
import org.iterx.sora.tool.meta.value.Constant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public final class TypeMatcher<T> extends BaseMatcher {

    private final Collection<Matcher<?>> matchers;
    private final Object expected;

    private TypeMatcher(final Object expected, final Collection<Matcher<?>> matchers) {
        this.expected = expected;
        this.matchers = matchers;
    }

    public static <T extends Type<T>> TypeMatcher<T> newTypeMatcher(final T type) {
        return new TypeMatcher<T>(type, CollectorVisitor.newCollectorTypeVisitor(type).getMatchers());
    }

    public static <T extends Class<?>> TypeMatcher<T> newTypeMatcher(final T cls) {
        return new TypeMatcher<T>(cls, CollectorVisitor.newCollectorTypeVisitor(cls).getMatchers());
    }

    public void describeTo(final Description description) {
        description.appendText((expected != null)? expected.toString() : "null");
    }

    public boolean matches(final Object object) {
        if(object instanceof Type) return MatcherVisitor.newMatcherVisitor(matchers, (Type) object).isMatch();
        else if (object instanceof Class) return MatcherVisitor.newMatcherVisitor(matchers, (Class) object).isMatch();
        return false;
    }

    private static class MatcherVisitor implements TypeVisitor, InstructionVisitor {

        private final Iterator<Matcher<?>> matcherIterator;
        private boolean matches;

        private MatcherVisitor(final Iterator<Matcher<?>> matcherIterator) {
            this.matcherIterator = matcherIterator;
            this.matches = true;
        }

        public static MatcherVisitor newMatcherVisitor(final Collection<Matcher<?>> matchers, final Type<?> type) {
            final MatcherVisitor matcherVisitor = new MatcherVisitor(matchers.iterator());
            new TypeReader(type).accept(matcherVisitor);
            return matcherVisitor;
        }

        public static MatcherVisitor newMatcherVisitor(final Collection<Matcher<?>> matchers, final Class<?> cls) {
            final MatcherVisitor matcherVisitor = new MatcherVisitor(matchers.iterator());
            new TypeReader(cls).accept(matcherVisitor);
            return matcherVisitor;
        }


        public boolean isMatch() {
            return (matches && !matcherIterator.hasNext());
        }

        public void startClass(final Access access,
                               final Modifier[] modifiers,
                               final Type<?> type,
                               final Type<?> superType,
                               final Type<?>[] interfaceTypes) {
            matches(access);
            matches(modifiers);
            matches(type);
            matches(superType);
            matches(interfaceTypes);
        }

        public void startInterface(final Access access,
                                   final Modifier[] modifiers,
                                   final Type<?> type,
                                   final Type<?>[] interfaceTypes) {
            matches(access);
            matches(modifiers);
            matches(type);
            matches(interfaceTypes);
        }

        public void field(final Access access,
                          final Modifier[] modifiers,
                          final String fieldName,
                          final Type<?> fieldType,
                          final Constant fieldValue) {
            matches(access);
            matches(modifiers);
            matches(fieldName);
            matches(fieldType);
            matches(fieldValue);
        }

        public InstructionVisitor startConstructor(final Access access,
                                                   final Modifier[] modifiers,
                                                   final Type<?>[] constructorTypes,
                                                   final Type<?>[] exceptionTypes) {
            matches(access);
            matches(modifiers);
            matches(constructorTypes);
            matches(exceptionTypes);
            return this;
        }


        public void endConstructor() {
        }

        public InstructionVisitor startMethod(final Access access,
                                              final Modifier[] modifiers,
                                              final String methodName,
                                              final Type<?> returnType,
                                              final Type<?>[] argumentTypes,
                                              final Type<?>[] exceptionTypes) {
            matches(access);
            matches(modifiers);
            matches(methodName);
            matches(returnType);
            matches(argumentTypes);
            matches(exceptionTypes);
            return this;
        }

        public void endMethod() {
        }

        public void endClass() {
        }

        public void endInterface() {
        }


        public void invokeSuper(final Value[] values) {
            matches(values);
        }

        public void returnValue(final Value value) {
            matches(value);
        }

        private void matches(final Object object) {
            if(matcherIterator.hasNext() && matcherIterator.next().matches(object)) return;
            matches = false;
        }
    }

    private static class CollectorVisitor implements TypeVisitor, InstructionVisitor {

        private final Collection<Matcher<?>> matchers;

        private CollectorVisitor() {
            this.matchers = new ArrayList<Matcher<?>>();
        }

        public Collection<Matcher<?>> getMatchers() {
            return matchers;
        }

        public static CollectorVisitor newCollectorTypeVisitor(final Type<?> type) {
            final CollectorVisitor collectorVisitor = new CollectorVisitor();
            new TypeReader(type).accept(collectorVisitor);
            return collectorVisitor;
        }

        public static CollectorVisitor newCollectorTypeVisitor(final Class<?> cls) {
            final CollectorVisitor collectorVisitor = new CollectorVisitor();
            new TypeReader(cls).accept(collectorVisitor);
            return collectorVisitor;
        }

        public void startClass(final Access access,
                               final Modifier[] modifiers,
                               final Type<?> type,
                               final Type<?> superType,
                               final Type<?>[] interfaceTypes) {
            matches(Matchers.equalTo(access));
            matches(Matchers.equalTo(modifiers));
            matches(Matchers.equalTo(type));
            matches(Matchers.equalTo(superType));
            matches(Matchers.equalTo(interfaceTypes));
        }

        public void startInterface(final Access access,
                                   final Modifier[] modifiers,
                                   final Type<?> type,
                                   final Type<?>[] interfaceTypes) {
            matches(Matchers.equalTo(access));
            matches(Matchers.equalTo(modifiers));
            matches(Matchers.equalTo(type));
            matches(Matchers.equalTo(interfaceTypes));
        }

        public void field(final Access access,
                          final Modifier[] modifiers,
                          final String fieldName,
                          final Type<?> fieldType,
                          final Constant fieldValue) {
            matches(Matchers.equalTo(access));
            matches(Matchers.equalTo(modifiers));
            matches(Matchers.equalTo(fieldName));
            matches(Matchers.equalTo(fieldType));
            matches(Matchers.equalTo(fieldValue));
        }

        public InstructionVisitor startConstructor(final Access access,
                                                   final Modifier[] modifiers,
                                                   final Type<?>[] constructorTypes,
                                                   final Type<?>[] exceptionTypes) {
            matches(Matchers.equalTo(access));
            matches(Matchers.equalTo(modifiers));
            matches(Matchers.equalTo(constructorTypes));
            matches(Matchers.equalTo(exceptionTypes));
            return this;
        }

        public void endConstructor() {
        }

        public InstructionVisitor startMethod(final Access access,
                                              final Modifier[] modifiers,
                                              final String methodName,
                                              final Type<?> returnType,
                                              final Type<?>[] argumentTypes,
                                              final Type<?>[] exceptionTypes) {
            matches(Matchers.equalTo(access));
            matches(Matchers.equalTo(modifiers));
            matches(Matchers.equalTo(methodName));
            matches(Matchers.equalTo(returnType));
            matches(Matchers.equalTo(argumentTypes));
            matches(Matchers.equalTo(exceptionTypes));
            return this;
        }

        public void endMethod() {
        }
        
        public void endClass() {
        }

        public void endInterface() {
        }

        public void invokeSuper(final Value[] values) {
            matches(Matchers.equalTo(values));
        }

        public void returnValue(final Value value) {
            matches(Matchers.equalTo(value));
        }

        private void matches(final Matcher<?> matcher) {
            matchers.add(matcher);
        }
    }
}