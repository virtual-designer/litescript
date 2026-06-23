/*
 *  This file is part of The LiteScript Project.
 * 
 *  Copyright (C) 2026  Ar Rakin.
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */
package me.rakinar2.litescript.interpreter;

import java.util.function.BiFunction;
import me.rakinar2.litescript.ast.nodes.AbstractNode;
import me.rakinar2.litescript.ast.nodes.BinaryOperator;

/**
 *
 * @author rakinar2
 */
public class PrimitiveOperationUnit {    
    private RuntimeValue commonBinaryOperationHandler(RuntimeValue left, RuntimeValue right, 
            BiFunction<Double, Double, RuntimeValue> floatHandler, BiFunction<Long, Long, RuntimeValue> intHandler) {
        if ((left instanceof RuntimeValue.IntValue || left instanceof RuntimeValue.FloatValue) 
            && (right instanceof RuntimeValue.IntValue || right instanceof RuntimeValue.FloatValue)) {
            if (left instanceof RuntimeValue.FloatValue || right instanceof RuntimeValue.FloatValue) {
                RuntimeValue.FloatValue fLeft, fRight;
                
                if (left instanceof RuntimeValue.FloatValue f) {
                    fLeft = f;
                    fRight = RuntimeValue.convertValueToFloat(right);
                }
                else {
                    fLeft = RuntimeValue.convertValueToFloat(left);
                    fRight = (RuntimeValue.FloatValue) right;
                }
                
                return floatHandler.apply(fLeft.value, fRight.value);
            }
            
            final var intLeft = (RuntimeValue.IntValue) left;
            final var intRight = (RuntimeValue.IntValue) right;
            
            return intHandler.apply(intLeft.value, intRight.value);
        }
        
        return null;
    }
    
    public RuntimeValue computeBinaryAdd(AbstractNode sourceNode, RuntimeValue left, RuntimeValue right) {
        if (left instanceof RuntimeValue.StringValue || 
            right instanceof RuntimeValue.StringValue) {
            RuntimeValue.StringValue strLeft;
            RuntimeValue.StringValue strRight;
            
            if (left instanceof RuntimeValue.StringValue s) {
                strLeft = s;
                strRight = RuntimeValue.convertValueToString(right);
            }
            else {
                strLeft = RuntimeValue.convertValueToString(left);
                strRight = (RuntimeValue.StringValue) right;
            }
            
            final RuntimeValue.StringValue result = new RuntimeValue.StringValue(strLeft.value + strRight.value);
            return result;
        }
        
        final var result = commonBinaryOperationHandler(left, right, 
                (lval, rval) -> new RuntimeValue.FloatValue(lval + rval),
                (lval, rval) -> new RuntimeValue.IntValue(lval + rval));
        
        if (result != null) {
            return result;
        }
        
        throw new InterpreterRuntimeException(
                        String.format("Unsupported operator '%s' on operands of type '%s' and '%s'", 
                                BinaryOperator.ADD, RuntimeValue.getTypeOf(left), RuntimeValue.getTypeOf(right)), 
                        sourceNode.getLocation());
    }
    
    public RuntimeValue computeBinarySubtract(AbstractNode sourceNode, RuntimeValue left, RuntimeValue right) {
        final var result = commonBinaryOperationHandler(left, right, 
                (lval, rval) -> new RuntimeValue.FloatValue(lval - rval),
                (lval, rval) -> new RuntimeValue.IntValue(lval - rval));
        
        if (result != null) {
            return result;
        }
        
        throw new InterpreterRuntimeException(
                        String.format("Unsupported operator '%s' on operands of type '%s' and '%s'", 
                                BinaryOperator.SUBTRACT, RuntimeValue.getTypeOf(left), RuntimeValue.getTypeOf(right)), 
                        sourceNode.getLocation());
    }
    
    public RuntimeValue computeBinaryMultiply(AbstractNode sourceNode, RuntimeValue left, RuntimeValue right) {
        final var result = commonBinaryOperationHandler(left, right, 
                (lval, rval) -> new RuntimeValue.FloatValue(lval * rval),
                (lval, rval) -> new RuntimeValue.IntValue(lval * rval));

        if (result != null) {
            return result;
        }
        
        throw new InterpreterRuntimeException(
                        String.format("Unsupported operator '%s' on operands of type '%s' and '%s'", 
                                BinaryOperator.MULTIPLY, RuntimeValue.getTypeOf(left), RuntimeValue.getTypeOf(right)), 
                        sourceNode.getLocation());
    }
    
    public RuntimeValue computeBinaryDivide(AbstractNode sourceNode, RuntimeValue left, RuntimeValue right) {  
        try {
            final var result = commonBinaryOperationHandler(left, right, 
                (lval, rval) -> new RuntimeValue.FloatValue(lval / rval),
                (lval, rval) -> new RuntimeValue.IntValue(lval / rval));

            if (result != null) {
                return result;
            }
        }
        catch (ArithmeticException exception) {
            if (exception.getMessage().equals("/ by zero")) {
                throw new InterpreterRuntimeException("Division by zero", sourceNode.getLocation());
            }
            
            throw exception;
        }
        
        throw new InterpreterRuntimeException(
                        String.format("Unsupported operator '%s' on operands of type '%s' and '%s'", 
                                BinaryOperator.DIVIDE, RuntimeValue.getTypeOf(left), RuntimeValue.getTypeOf(right)), 
                        sourceNode.getLocation());
    }
    
    public RuntimeValue computeBinaryModulus(AbstractNode sourceNode, RuntimeValue left, RuntimeValue right) {
        try {
            final var result = commonBinaryOperationHandler(left, right, 
                    (lval, rval) -> new RuntimeValue.FloatValue(lval % rval),
                    (lval, rval) -> new RuntimeValue.IntValue(lval % rval));

            if (result != null) {
                return result;
            }
        }
        catch (ArithmeticException exception) {
            if (exception.getMessage().equals("/ by zero")) {
                throw new InterpreterRuntimeException("Division by zero", sourceNode.getLocation());
            }
            
            throw exception;
        }
        
        throw new InterpreterRuntimeException(
                        String.format("Unsupported operator '%s' on operands of type '%s' and '%s'", 
                                BinaryOperator.DIVIDE, RuntimeValue.getTypeOf(left), RuntimeValue.getTypeOf(right)), 
                        sourceNode.getLocation());
    }
}
