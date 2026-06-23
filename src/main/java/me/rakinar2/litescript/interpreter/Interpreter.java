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

import java.util.List;
import java.util.stream.Collectors;

import me.rakinar2.litescript.ast.Location;
import me.rakinar2.litescript.ast.nodes.AbstractNode;
import me.rakinar2.litescript.ast.nodes.AssignmentExpressionNode;
import me.rakinar2.litescript.ast.nodes.BinaryExpressionNode;
import me.rakinar2.litescript.ast.nodes.CallExpressionNode;
import me.rakinar2.litescript.ast.nodes.EmptyStatementNode;
import me.rakinar2.litescript.ast.nodes.ExpressionNode;
import me.rakinar2.litescript.ast.nodes.ExpressionStatementNode;
import me.rakinar2.litescript.ast.nodes.IdentifierNode;
import me.rakinar2.litescript.ast.nodes.LiteralExpressionNode;
import me.rakinar2.litescript.ast.nodes.RootNode;
import me.rakinar2.litescript.ast.nodes.StatementNode;
import me.rakinar2.litescript.ast.nodes.VariableDeclarationNode;
import me.rakinar2.litescript.stdlib.Loader;

/**
 * Main interpreter execution unit.
 * 
 * @author rakinar2
 */
public class Interpreter {
    private PrimitiveOperationUnit primitiveOperationUnit = new PrimitiveOperationUnit();
    private Loader loader = new Loader();
    
    public ExecutionContext createDefaultContext() {
        Scope scope = Scope.createGlobal();
        loader.load(scope);
        return ExecutionContext.create().setScope(scope);
    }
    
    public RuntimeValue interpret(AbstractNode sourceNode) {
        ExecutionContext context = createDefaultContext();
        return interpret(sourceNode, context);
    }
    
    public RuntimeValue interpret(AbstractNode sourceNode, ExecutionContext context) {
        if (sourceNode instanceof ExpressionNode expression) {
            return interpretExpression(expression, context);
        }
        
        if (sourceNode instanceof StatementNode statement) {
            return interpretStatement(statement, context);
        }
        
        throw new InterpreterRuntimeException("Unable to interpret node", 
                sourceNode.getLocation());
    }
    
    public RuntimeValue interpretStatement(StatementNode sourceNode, ExecutionContext context) {
        if (sourceNode instanceof ExpressionStatementNode expressionStatement) {
            return interpretExpression(expressionStatement.expression, context);
        }
        
        if (sourceNode instanceof RootNode rootNode) {
            return interpretRoot(rootNode, context);
        }
        
        if (sourceNode instanceof VariableDeclarationNode node) {
            return interpretVariableDeclaration(node, context);
        }
        
        if (sourceNode instanceof EmptyStatementNode) {
            return RuntimeValue.NULL;
        }
        
        throw new InterpreterRuntimeException("Unable to interpret node", 
                sourceNode.getLocation());
    }
    
    private RuntimeValue interpretVariableDeclaration(VariableDeclarationNode sourceNode, ExecutionContext context) {
        final Scope scope = context.getScope();
        
        if (scope.getSymbol(sourceNode.identifier.symbol) != null) {
            throw new InterpreterRuntimeException(
                    String.format("Identifier '%s' is already defined in this scope", sourceNode.identifier.symbol),
                    sourceNode.identifier.getLocation());
        }
        
        final RuntimeValue value = sourceNode.value.isPresent() 
                ? interpretExpression(sourceNode.value.get() , context) 
                : RuntimeValue.NULL;
        
        scope.setSymbol(new Symbol(sourceNode.identifier.symbol, sourceNode, value));
        return RuntimeValue.NULL;
    }
    
    public RuntimeValue interpretRoot(RootNode sourceNode, ExecutionContext context) {
        RuntimeValue value = RuntimeValue.NullValue.getInstance();
        
        for (final var childNode : sourceNode.statements) {
            if (childNode instanceof EmptyStatementNode) {
                continue;
            }
            
            value = interpret(childNode, context);
        }
        
        return value;
    }
    
    public RuntimeValue interpretExpression(ExpressionNode sourceNode, ExecutionContext context) {
        if (sourceNode instanceof LiteralExpressionNode literal) {
            return interpretLiteralExpression(literal, context);
        }
        
        if (sourceNode instanceof BinaryExpressionNode expression) {
            return interpretBinaryExpression(expression, context);
        }
        
        if (sourceNode instanceof IdentifierNode identifier) {
            return interpretIdentifier(identifier, context);
        }
        
        if (sourceNode instanceof AssignmentExpressionNode node) {
            return interpretAssignmentExpression(node, context);
        }
        
        if (sourceNode instanceof CallExpressionNode node) {
            return interpretCallExpression(node, context);
        }
        
        throw new InterpreterRuntimeException("Unable to interpret node", 
                sourceNode.getLocation());
    }
    
    private RuntimeValue interpretCallExpression(CallExpressionNode sourceNode, ExecutionContext context) {
        final RuntimeValue callee = interpretExpression(sourceNode.callee, context);
        final List<RuntimeValue> arguments = 
            sourceNode.arguments
                .stream()
                .map(arg -> interpretExpression(arg, context))
                .collect(Collectors.toList());
        
        
        if (callee instanceof RuntimeValue.FunctionValue fn) {
            int minArgumentCount = Math.max(fn.parameters.size() == 0 ? Integer.MIN_VALUE : fn.parameters.size(), fn.getMinArgumentCount());
            int maxArgumentCount = Math.min(fn.parameters.size() == 0 ? Integer.MAX_VALUE : fn.parameters.size(), fn.getMaxArgumentCount());

            if (arguments.size() < minArgumentCount) {
                throw new InterpreterRuntimeException(
                    String.format("Cannot call this function without at least %d arguments", minArgumentCount), 
                    sourceNode.getLocation());
            }

            if (arguments.size() > maxArgumentCount) {
                throw new InterpreterRuntimeException(
                    String.format("Cannot call this function with more than %d arguments", maxArgumentCount), 
                    sourceNode.getLocation());
            }

            if (fn.isBuiltin()) {
                try {
                    Object[] argv = arguments.toArray(new RuntimeValue[0]);
                    Object ret = fn.isVariadic() ? fn.method.invoke(fn.instance, (Object) argv) : fn.method.invoke(fn.instance, argv);
                    
                    if (ret instanceof RuntimeValue v) {
                        return v;
                    }
                    
                    throw new RuntimeException("Built-in function did not return a RuntimeValue instance");
                }
                catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }
            
            throw new InterpreterRuntimeException("User-defined function calls are not supported yet", 
                sourceNode.getLocation());
        }
        
        throw new InterpreterRuntimeException("Cannot call a non-function value", 
                sourceNode.getLocation());
    }
    
    private RuntimeValue interpretAssignmentExpression(AssignmentExpressionNode sourceNode, ExecutionContext context) {
        final ExpressionNode left = sourceNode.left;
        
        if (left instanceof IdentifierNode identifierLeft) {
            final Symbol symbol = getIdentifier(context, identifierLeft);
            
            if (symbol.getSourceNode() instanceof VariableDeclarationNode decl 
                    && decl.kind == VariableDeclarationNode.Kind.FINAL) {
                throw new InterpreterRuntimeException(
                    String.format("Cannot assign to final identifier '%s'", symbol.getName()),
                    sourceNode.getLocation());    
            }
            
            final RuntimeValue value = interpretExpression(sourceNode.right, context);
            symbol.setValue(value);
            return value;
        }
        
        throw new InterpreterRuntimeException(
                    "Invalid left-side in assignment",
                    sourceNode.getLocation());        
    }
    
    private Symbol getIdentifier(ExecutionContext context, IdentifierNode identifierNode) {
        return getIdentifier(context.getScope(), identifierNode.symbol, identifierNode.location);
    }
    
    private Symbol getIdentifier(Scope scope, String name, Location location) {
        final Symbol symbol = scope.getSymbol(name);

        if (symbol == null) {
            throw new InterpreterRuntimeException(
                String.format("Identifier '%s' is not defined", name),
                location);
        }
        
        return symbol;
    }
    
    private RuntimeValue interpretIdentifier(IdentifierNode sourceNode, ExecutionContext context) {
        return getIdentifier(context, sourceNode).getValue();
    }
    
    private RuntimeValue interpretBinaryExpression(BinaryExpressionNode sourceNode, ExecutionContext context) {
        RuntimeValue left = interpretExpression(sourceNode.left, context);
        RuntimeValue right = interpretExpression(sourceNode.right, context);
        
        switch (sourceNode.operator) {
            case ADD:
                return primitiveOperationUnit.computeBinaryAdd(sourceNode, left, right);
                
            case SUBTRACT:
                return primitiveOperationUnit.computeBinarySubtract(sourceNode, left, right);
            
            case MULTIPLY:
                return primitiveOperationUnit.computeBinaryMultiply(sourceNode, left, right);
                
            case DIVIDE:
                return primitiveOperationUnit.computeBinaryDivide(sourceNode, left, right);
                
            case MODULUS:
                return primitiveOperationUnit.computeBinaryModulus(sourceNode, left, right);
            
            default:
                throw new InterpreterRuntimeException(
                        "Unsupported operator: " + sourceNode.operator.toString(), 
                        sourceNode.getLocation());
        }
    }
    
    private RuntimeValue interpretLiteralExpression(LiteralExpressionNode sourceNode, ExecutionContext context) {
        if (sourceNode.value instanceof LiteralExpressionNode.LiteralValue.Int intLiteral) {
            return new RuntimeValue.IntValue(intLiteral.value);
        }
        
        if (sourceNode.value instanceof LiteralExpressionNode.LiteralValue.Float floatLiteral) {
            return new RuntimeValue.FloatValue(floatLiteral.value);
        }
        
        if (sourceNode.value instanceof LiteralExpressionNode.LiteralValue.Boolean booleanLiteral) {
            return new RuntimeValue.BooleanValue(booleanLiteral.value);
        }
        
        if (sourceNode.value instanceof LiteralExpressionNode.LiteralValue.String stringLiteral) {
            return new RuntimeValue.StringValue(stringLiteral.value);
        }
        
        if (sourceNode.value instanceof LiteralExpressionNode.LiteralValue.Null) {
            return RuntimeValue.NULL;
        }
        
        throw new IllegalStateException("Invalid literal");
    }
}
