package org.iterx.sora.tool.meta.support.asm;

import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;
import org.iterx.sora.tool.meta.declaration.Declaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;
import org.iterx.sora.tool.meta.statement.AssignStatement;
import org.iterx.sora.tool.meta.statement.GetFieldStatement;
import org.iterx.sora.tool.meta.statement.InvokeInitStatement;
import org.iterx.sora.tool.meta.statement.PutFieldStatement;
import org.iterx.sora.tool.meta.statement.ReturnVariableStatement;
import org.iterx.sora.tool.meta.statement.Statement;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public abstract class AsmCompiler<V, C, T> {

    private static final Map<Class, Dispatcher> DISPATCHERS = new HashMap<Class, Dispatcher>();

    static
    {
        register(new ClassDeclaration.ClassDeclarationAsmCompiler());
        register(new ConstructorDeclaration.ConstructorDeclarationCompiler());
        register(new MethodDeclaration.MethodDeclarationAsmCompiler());
        register(new FieldDeclaration.FieldDeclarationAsmCompiler());
        register(new GetFieldStatement.GetFieldStatementAsmCompiler());
        register(new PutFieldStatement.PutFieldStatementAsmCompiler());
        register(new InvokeInitStatement.InvokeSuperStatementAsmCompiler());
        register(new AssignStatement.AssignStatementAsmCompiler());
        register(new ReturnVariableStatement.ReturnStatementAsmCompiler());
    }

    public static byte[] compile(final Declaration declaration) {
        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        loadDispatcher(declaration).compile(classWriter, new DeclarationContext<Declaration>(declaration));
        return classWriter.toByteArray();
    }

    private final Class contextClass;
    private final Class<V> visitorClass;
    private final Class<T> targetClass;

    protected AsmCompiler(final Class<V> visitorClass,
                          final Class<T> targetClass) {
        this.contextClass = (Statement.class.isAssignableFrom(targetClass))? 
                            StatementContext.class :
                            DeclarationContext.class;
        this.visitorClass = visitorClass;
        this.targetClass = targetClass;
    }

    public abstract void compile(final V visitor, final C context);

    public <S extends Declaration> void compile(final ClassVisitor visitor, final DeclarationContext<?> context, final S declaration)  {
        loadDispatcher(declaration).compile(visitor, new DeclarationContext<S>(context, declaration));
    }

    public  void compile(final MethodVisitor visitor,
                         final DeclarationContext<?> context,
                         final Collection<? extends Statement> statements)  {
        final StatementContext statementContext = new StatementContext(context, null);
        for(final Statement<?> statement : statements) compile(visitor, statementContext, statement);
        //loadDispatcher(statement).compile(visitor, new StatementContext<S>(context, statement));
    }

    public <S extends Statement> void compile(final MethodVisitor visitor, final StatementContext<?> context, final S statement)  {
        loadDispatcher(statement).compile(visitor, new StatementContext<S>(context, statement));
    }

    public static class Context<T> {
    }

    public static class StatementContext<T extends Statement> {

        private final DeclarationContext<? extends Declaration> declarationContext;
        private final T statement;
        private final Stack stack;

        private StatementContext(final StatementContext<? extends Statement> statementContext,
                                 final T statement) {
            this.stack = statementContext.stack;
            this.declarationContext = statementContext.declarationContext;
            this.statement = statement;
        }

        private StatementContext(final DeclarationContext<? extends Declaration> declarationContext,
                                 final T statement) {

            this.stack = newStack(declarationContext.getDeclaration());
            this.declarationContext = declarationContext;
            this.statement = statement;
        }

        public DeclarationContext<? extends Declaration> getDeclarationContext() {
            return declarationContext;
        }

        public T getStatement() {
            return statement;
        }

        public Stack getStack() {
            return stack;
        }

        private Stack newStack(final Declaration declaration) {
            final Type[] types = (declaration.getClass() == MethodDeclaration.class)?
                                 ((MethodDeclaration) declaration).getArgumentTypes() :
                                 (declaration.getClass() == ConstructorDeclaration.class)?
                                 ((ConstructorDeclaration) declaration).getConstructorTypes() :
                                 new Type[0];
            final Stack stack = new Stack();
            for(int i = 0, length = types.length; i != length; i++) stack.push("arg" + i, types[i]);
            return stack;
        }

    }

    public static class DeclarationContext<T extends Declaration> {

        private final DeclarationContext<?> parent;
        private final T declaration;

        private DeclarationContext(final T declaration) {
            this.parent = null;
            this.declaration = declaration;
        }

        private DeclarationContext(final DeclarationContext<?> parent, final T declaration) {
            this.parent = parent;
            this.declaration = declaration;
        }

        @SuppressWarnings("unchecked")
        public <T extends Declaration> DeclarationContext<T> getParent(final Class<T> declarationClass) {
            if(parent == null) throw new IllegalStateException();
            return (parent.getDeclaration().getClass() == declarationClass)?
                   (DeclarationContext<T>) parent :
                   getParent(declarationClass);
        }

        public T getDeclaration() {
            return declaration;
        }
    }


    private static class Dispatcher {

        private final AsmCompiler asmCompiler;
        private final Method compile;

        private Dispatcher(final AsmCompiler asmCompiler) {
            try {
                this.asmCompiler = asmCompiler;
                this.compile = asmCompiler.getClass().getDeclaredMethod("compile", asmCompiler.visitorClass, asmCompiler.contextClass);
            }
            catch(final NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        private void compile(final Object visitor, final Object context)  {
            try {
                compile.invoke(asmCompiler, visitor, context);
            }
            catch(final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Dispatcher loadDispatcher(final Declaration declaration) {
        final Dispatcher dispatcher = DISPATCHERS.get(declaration.getClass());
        if(dispatcher != null) return dispatcher;
        throw new IllegalArgumentException("Unsupported Declaration '" + declaration + "'");
    }

    private static void register(final AsmCompiler asmCompiler) {
        DISPATCHERS.put(asmCompiler.targetClass, new Dispatcher(asmCompiler));
    }

}
