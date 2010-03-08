package org.iterx.sora.tool.meta.util;

import org.iterx.sora.collection.map.HashMap;
import org.iterx.sora.tool.meta.Instruction;

import java.lang.reflect.Method;
import java.util.Map;

public final class InstructionReader {

    private static final Map<Class, Method> DISPATCH_TABLE = newDispatchTable();

    private final Instruction[] instructions;

    public InstructionReader(final Instruction... instructions) {
        this.instructions = instructions;
    }

    public void accept(final InstructionVisitor instructionVisitor) {
        try {
            for(final Instruction instruction : instructions) {
                DISPATCH_TABLE.get(instruction.getClass()).invoke(instructionVisitor, instruction);
            }
        }
        catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<Class, Method> newDispatchTable() {
        final Map<Class, Method> dispatcher = new HashMap<Class, Method>();
        for(final Method method : InstructionVisitor.class.getMethods()) dispatcher.put(method.getParameterTypes()[0], method);
        return dispatcher;
    }
}
