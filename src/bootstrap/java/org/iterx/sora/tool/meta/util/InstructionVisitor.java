package org.iterx.sora.tool.meta.util;

import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.Value;
import org.iterx.sora.tool.meta.value.Variable;

public interface InstructionVisitor {    

    void SUPER(String methodName, Type<?> returnType, Value<?>[] values);

    void invokeMethod(Type<?> target, String methodName, Type<?> returnType, Value<?>[] values);

    void RETURN(Value<?> value);

    void store(Variable variable, Value<?> value);
        
    void GETFIELD(Variable owner, String fieldName, Type<?> fieldType);

    void putField(Variable owner, String fieldName, Type<?> fieldType, Value<?> value);

}
