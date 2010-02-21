package org.iterx.sora.tool.meta.statement;


import org.objectweb.asm.Type;

import java.util.Collection;
import java.util.LinkedList;

public class Statements {

    public final Collection<Statement> statements;

    public Statements() {
        this.statements = new LinkedList<Statement>();
    }

    public InvokeInitStatement invokeInit(final Type initType, final String... variableNames) {
        return store(new InvokeInitStatement(initType, variableNames));
    }

    public AssignStatement assign(final String variableName, final Type variableType, final Statement statement) {
        return store(new AssignStatement(variableName, variableType, statement));
    }

    public ReturnVariableStatement returnVariable(final String variableName) {
        return store(new ReturnVariableStatement(variableName));
    }

    public GetFieldStatement getField(final String fieldName) {
        return store(new GetFieldStatement(fieldName));
    }

    public PutFieldStatement putField(final String fieldName, final String variableName) {
        return store(new PutFieldStatement(fieldName, variableName));
    }

    private <T extends Statement> T store(final T statement) {
        statements.add(statement);
        return statement;
    }
}