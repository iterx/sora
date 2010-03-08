package org.iterx.sora.tool.meta.util;

import org.iterx.sora.tool.meta.instruction.GetFieldInstruction;
import org.iterx.sora.tool.meta.instruction.InvokeSuperInstruction;
import org.iterx.sora.tool.meta.instruction.PutFieldInstruction;
import org.iterx.sora.tool.meta.instruction.ReturnValueInstruction;
import org.iterx.sora.tool.meta.instruction.StoreInstruction;

public interface InstructionVisitor {    

    void getField(GetFieldInstruction getFieldInstruction);

    void putField(PutFieldInstruction putFieldInstruction);

    void store(StoreInstruction storeInstruction);

    void invokeSuper(InvokeSuperInstruction invokeSuperInstruction);

    void returnValue(ReturnValueInstruction returnValueInstruction);
}
