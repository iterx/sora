package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.Instruction;
import org.iterx.sora.tool.meta.Value;

public final class InvokeSuperInstruction extends Instruction<InvokeSuperInstruction> {

    private static final Value[] EMPTY_VALUES = new Value[0];

    private final Value[] values;

    private InvokeSuperInstruction(final Value[] values) {
        this.values = values;
    }

    public static InvokeSuperInstruction invokeInitInstruction(final Value... values) {
        return new InvokeSuperInstruction((values != null)? values : EMPTY_VALUES);
    }
       
    public Value[] getValues() {
        return values;
    }
}
