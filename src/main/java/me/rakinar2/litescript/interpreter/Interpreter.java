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
import me.rakinar2.litescript.ast.nodes.BinaryExpressionNode;
import me.rakinar2.litescript.ast.nodes.BinaryOperator;
import me.rakinar2.litescript.ast.nodes.EmptyStatementNode;
import me.rakinar2.litescript.ast.nodes.ExpressionNode;
import me.rakinar2.litescript.ast.nodes.ExpressionStatementNode;
import me.rakinar2.litescript.ast.nodes.LiteralExpressionNode;
import me.rakinar2.litescript.ast.nodes.RootNode;
import me.rakinar2.litescript.ast.nodes.StatementNode;

/**
 * Main interpreter execution unit.
 * 
 * @author rakinar2
 */
public class Interpreter {
    public ExecutionContext createDefaultContext() {
        return ExecutionContext.create().setScope(Scope.createGlobal());
    }
    
    public RuntimeValue interpret(AbstractNode sourceNode) {
        ExecutionContext context = createDefaultContext();
        return interpret(sourceNode, context);
    }
    
    public RuntimeValue interpret(AbstractNode sourceNode, ExecutionContext context) {
        return switch (sourceNode) {
            case ExpressionNode expression ->
                interpretExpression(expression, context);
                
            case StatementNode statement ->
                 interpretStatement(statement, context);
                
            default ->
                throw new InterpreterRuntimeException("Unable to interpret node", 
                        sourceNode.getLocation());
        };
    }
    
    public RuntimeValue interpretStatement(StatementNode sourceNode, ExecutionContext context) {
        RuntimeValue value = switch (sourceNode) {                
            case ExpressionStatementNode expressionStatement ->
                interpretExpression(expressionStatement.expression, context);
                
            case RootNode rootNode ->
                interpretRootNode(rootNode, context);
                
            case EmptyStatementNode _ -> RuntimeValue.NullValue.getInstance();
            
            default ->
                throw new InterpreterRuntimeException("Unable to interpret node", 
                        sourceNode.getLocation());
        };
        
        return value;
    }
    
    public RuntimeValue interpretRootNode(RootNode sourceNode, ExecutionContext context) {
        RuntimeValue value = RuntimeValue.NullValue.getInstance();
        
        for (var childNode : sourceNode.statements) {
            value = interpret(childNode, context);
        }
        
        return value;
    }
    
    public RuntimeValue interpretExpression(ExpressionNode sourceNode, ExecutionContext context) {
        return switch (sourceNode) {
            case LiteralExpressionNode literal ->
                interpretLiteralExpression(literal, context);
                
            case BinaryExpressionNode expression ->
                interpretBinaryExpression(expression, context);
            
            default ->
                throw new InterpreterRuntimeException("Unable to interpret node", 
                        sourceNode.getLocation());
        };
    }
    
    private RuntimeValue.StringValue convertValueToString(RuntimeValue value) {
        return switch (value) {
            case RuntimeValue.IntValue intValue ->
                new RuntimeValue.StringValue(Long.toString(intValue.value));
                
            case RuntimeValue.FloatValue floatValue ->
                new RuntimeValue.StringValue(ValueFormatter.DECIMAL_FORMATTER.format(floatValue.value));
                
            case RuntimeValue.BooleanValue booleanValue ->
                new RuntimeValue.StringValue(booleanValue.value ? "true" : "false");
                
            case RuntimeValue.StringValue stringValue -> stringValue;
                
            case RuntimeValue.NullValue _ -> new RuntimeValue.StringValue("null");
                
            default ->
                throw new IllegalStateException("Invalid literal");
        };
    }
    
    private String getRuntimeTypeOf(RuntimeValue value) {
         return switch (value) {
            case RuntimeValue.IntValue _ -> "Int";
            case RuntimeValue.FloatValue _ -> "Float";
            case RuntimeValue.BooleanValue _ -> "Boolean";
            case RuntimeValue.StringValue _ -> "String";
            case RuntimeValue.NullValue _ -> "Null";
                
            default ->
                throw new IllegalStateException("Invalid literal");
        };
    }
    
    private RuntimeValue.FloatValue convertValueToFloat(RuntimeValue value) {
        return switch (value) {
            case RuntimeValue.FloatValue floatValue -> floatValue;
            case RuntimeValue.IntValue intValue -> new RuntimeValue.FloatValue((double) intValue.value);
            default -> throw new IllegalStateException(String.format("Cannot convert '%s' to Float", getRuntimeTypeOf(value)));
        };
    }
    
    private RuntimeValue commonBinaryOperationHandler(RuntimeValue left, RuntimeValue right, 
            BiFunction<Double, Double, RuntimeValue> floatHandler, BiFunction<Long, Long, RuntimeValue> intHandler) {
        if ((left instanceof RuntimeValue.IntValue || left instanceof RuntimeValue.FloatValue) 
            && (right instanceof RuntimeValue.IntValue || right instanceof RuntimeValue.FloatValue)) {
            if (left instanceof RuntimeValue.FloatValue || right instanceof RuntimeValue.FloatValue) {
                RuntimeValue.FloatValue fLeft, fRight;
                
                if (left instanceof RuntimeValue.FloatValue f) {
                    fLeft = f;
                    fRight = convertValueToFloat(right);
                }
                else {
                    fLeft = convertValueToFloat(left);
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
    
    private RuntimeValue computeBinaryAdd(AbstractNode sourceNode, RuntimeValue left, RuntimeValue right) {
        if (left instanceof RuntimeValue.StringValue || 
            right instanceof RuntimeValue.StringValue) {
            RuntimeValue.StringValue strLeft;
            RuntimeValue.StringValue strRight;
            
            if (left instanceof RuntimeValue.StringValue s) {
                strLeft = s;
                strRight = convertValueToString(right);
            }
            else {
                strLeft = convertValueToString(left);
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
                        String.format("Unsupported operator '%s' on operands of type '%s' amd '%s'", 
                                BinaryOperator.ADD, getRuntimeTypeOf(left), getRuntimeTypeOf(right)), 
                        sourceNode.getLocation());
    }
    
    private RuntimeValue computeBinarySubtract(AbstractNode sourceNode, RuntimeValue left, RuntimeValue right) {
        final var result = commonBinaryOperationHandler(left, right, 
                (lval, rval) -> new RuntimeValue.FloatValue(lval - rval),
                (lval, rval) -> new RuntimeValue.IntValue(lval - rval));
        
        if (result != null) {
            return result;
        }
        
        throw new InterpreterRuntimeException(
                        String.format("Unsupported operator '%s' on operands of type '%s' amd '%s'", 
                                BinaryOperator.SUBTRACT, getRuntimeTypeOf(left), getRuntimeTypeOf(right)), 
                        sourceNode.getLocation());
    }
    
    private RuntimeValue computeBinaryMultiply(AbstractNode sourceNode, RuntimeValue left, RuntimeValue right) {
        final var result = commonBinaryOperationHandler(left, right, 
                (lval, rval) -> new RuntimeValue.FloatValue(lval * rval),
                (lval, rval) -> new RuntimeValue.IntValue(lval * rval));

        if (result != null) {
            return result;
        }
        
        throw new InterpreterRuntimeException(
                        String.format("Unsupported operator '%s' on operands of type '%s' amd '%s'", 
                                BinaryOperator.MULTIPLY, getRuntimeTypeOf(left), getRuntimeTypeOf(right)), 
                        sourceNode.getLocation());
    }
    
    private RuntimeValue computeBinaryDivide(AbstractNode sourceNode, RuntimeValue left, RuntimeValue right) {  
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
                        String.format("Unsupported operator '%s' on operands of type '%s' amd '%s'", 
                                BinaryOperator.DIVIDE, getRuntimeTypeOf(left), getRuntimeTypeOf(right)), 
                        sourceNode.getLocation());
    }
    
    private RuntimeValue computeBinaryModulus(AbstractNode sourceNode, RuntimeValue left, RuntimeValue right) {
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
                        String.format("Unsupported operator '%s' on operands of type '%s' amd '%s'", 
                                BinaryOperator.DIVIDE, getRuntimeTypeOf(left), getRuntimeTypeOf(right)), 
                        sourceNode.getLocation());
    }
    
    private RuntimeValue interpretBinaryExpression(BinaryExpressionNode sourceNode, ExecutionContext context) {
        RuntimeValue left = interpretExpression(sourceNode.left, context);
        RuntimeValue right = interpretExpression(sourceNode.right, context);
        
        return switch (sourceNode.operator) {
            case BinaryOperator.ADD -> computeBinaryAdd(sourceNode, left, right);
            case BinaryOperator.SUBTRACT -> computeBinarySubtract(sourceNode, left, right);
            case BinaryOperator.MULTIPLY -> computeBinaryMultiply(sourceNode, left, right);
            case BinaryOperator.DIVIDE -> computeBinaryDivide(sourceNode, left, right);
            case BinaryOperator.MODULUS -> computeBinaryModulus(sourceNode, left, right);
            
            default ->
                throw new InterpreterRuntimeException(
                        "Unsupported operator: " + sourceNode.operator.toString(), 
                        sourceNode.getLocation());
        };
    }
    
    private RuntimeValue interpretLiteralExpression(LiteralExpressionNode sourceNode, ExecutionContext context) {
        return switch (sourceNode.value) {
            case LiteralExpressionNode.LiteralValue.Int intLiteral ->
                new RuntimeValue.IntValue(intLiteral.value);
                
            case LiteralExpressionNode.LiteralValue.Float floatLiteral ->
                new RuntimeValue.FloatValue(floatLiteral.value);
                
            case LiteralExpressionNode.LiteralValue.Boolean booleanLiteral ->
                new RuntimeValue.BooleanValue(booleanLiteral.value);
                
            case LiteralExpressionNode.LiteralValue.String stringLiteral ->
                new RuntimeValue.StringValue(stringLiteral.value);
                
            case LiteralExpressionNode.LiteralValue.Null _ ->
                RuntimeValue.NullValue.getInstance();
                
            default ->
                throw new IllegalStateException("Invalid literal");
        };
    }
}
