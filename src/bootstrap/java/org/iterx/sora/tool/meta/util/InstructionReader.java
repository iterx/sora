package org.iterx.sora.tool.meta.util;

import org.iterx.sora.collection.Map;
import org.iterx.sora.collection.map.HashMap;
import org.iterx.sora.tool.meta.Instruction;
import org.iterx.sora.tool.meta.instruction.GetFieldInstruction;
import org.iterx.sora.tool.meta.instruction.InvokeMethodInstruction;
import org.iterx.sora.tool.meta.instruction.InvokeSuperInstruction;
import org.iterx.sora.tool.meta.instruction.PutFieldInstruction;
import org.iterx.sora.tool.meta.instruction.ReturnValueInstruction;
import org.iterx.sora.tool.meta.instruction.StoreInstruction;

import java.lang.reflect.Method;

public final class InstructionReader {

    private static final Map<Class, Method> INSTRUCTION_DISPATCH_TABLE = newInstructionDispatchTable();

    private final Instruction<?>[] instructions;
    
    public InstructionReader(final Instruction<?>... instructions) {
        this.instructions = instructions;
    }

    public void accept(final InstructionVisitor instructionVisitor) {
        for(final Instruction instruction : instructions) {
            try {
                final Method method = INSTRUCTION_DISPATCH_TABLE.get(instruction.getClass());
                if(method != null) method.invoke(this, instructionVisitor, instruction);
                //INSTRUCTION_DISPATCH_TABLE.get(instruction.getClass()).invoke(this, instructionVisitor, instruction);
            }
            catch(final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void accept(final InstructionVisitor instructionVisitor, final InvokeSuperInstruction invokeSuperInstruction) {
        instructionVisitor.invokeSuper(invokeSuperInstruction.getTarget(),
                                       invokeSuperInstruction.getMethodName(),
                                       invokeSuperInstruction.getReturnType(),
                                       invokeSuperInstruction.getValues());
    }

    private void accept(final InstructionVisitor instructionVisitor, final InvokeMethodInstruction invokeMethodInstruction) {
        instructionVisitor.invokeMethod(invokeMethodInstruction.getTarget(),
                                        invokeMethodInstruction.getMethodName(),
                                        invokeMethodInstruction.getReturnType(),
                                        invokeMethodInstruction.getValues());
    }

    private void accept(final InstructionVisitor instructionVisitor, final ReturnValueInstruction returnValueInstruction) {
        instructionVisitor.returnValue(returnValueInstruction.getValue());
    }
    
    private void accept(final InstructionVisitor instructionVisitor, final StoreInstruction storeInstruction) {
        instructionVisitor.store(storeInstruction.getVariable(), storeInstruction.getValue());
    }

    private void accept(final InstructionVisitor instructionVisitor, final GetFieldInstruction getFieldInstruction) {
        instructionVisitor.getField(getFieldInstruction.getOwner(),
                                    getFieldInstruction.getFieldName(),
                                    getFieldInstruction.getFieldType());
    }

    private void accept(final InstructionVisitor instructionVisitor, final PutFieldInstruction putFieldInstruction) {
        instructionVisitor.putField(putFieldInstruction.getOwner(),
                                    putFieldInstruction.getFieldName(),
                                    putFieldInstruction.getFieldType(),
                                    putFieldInstruction.getValue());
    }

    private static Map<Class, Method> newInstructionDispatchTable() {
        final Map<Class, Method> dispatchTable = new HashMap<Class, Method>();
        for(final Method method : InstructionReader.class.getDeclaredMethods()) {
            final Class[] parameterTypes = method.getParameterTypes();
            if("accept".equals(method.getName()) &&
               parameterTypes.length == 2 &&
               InstructionVisitor.class.equals(method.getParameterTypes()[0])) dispatchTable.put(method.getParameterTypes()[1], method);
        }
        return dispatchTable;
    }

}
