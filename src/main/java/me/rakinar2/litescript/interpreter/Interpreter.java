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
import me.rakinar2.litescript.ast.nodes.BlockStatementNode;
import me.rakinar2.litescript.ast.nodes.CallExpressionNode;
import me.rakinar2.litescript.ast.nodes.EmptyStatementNode;
import me.rakinar2.litescript.ast.nodes.ExpressionNode;
import me.rakinar2.litescript.ast.nodes.ExpressionStatementNode;
import me.rakinar2.litescript.ast.nodes.FunctionDeclarationNode;
import me.rakinar2.litescript.ast.nodes.IdentifierNode;
import me.rakinar2.litescript.ast.nodes.IfStatementNode;
import me.rakinar2.litescript.ast.nodes.LiteralExpressionNode;
import me.rakinar2.litescript.ast.nodes.ReturnStatementNode;
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
        
        if (sourceNode instanceof FunctionDeclarationNode node) {
            return interpretFunctionDeclaration(node, context);
        }
        
        if (sourceNode instanceof EmptyStatementNode) {
            return RuntimeValue.NULL;
        }
        
        if (sourceNode instanceof ReturnStatementNode node) {
            return interpretReturnStatement(node, context);
        }
        
        if (sourceNode instanceof BlockStatementNode node) {
            return interpretBlockStatement(node, context);
        }
        
        if (sourceNode instanceof IfStatementNode node) {
            return interpretIfStatement(node, context);
        }
        
        throw new InterpreterRuntimeException("Unable to interpret node", 
                sourceNode.getLocation());
    }
    
    private RuntimeValue interpretIfStatement(IfStatementNode sourceNode, ExecutionContext context) {
        RuntimeValue.BooleanValue value = RuntimeValue.convertValueToBoolean(interpretExpression(sourceNode.condition, context));
        
        if (value == RuntimeValue.BooleanValue.TRUE) {
            interpret(sourceNode.then, context);
        }
        else if (sourceNode.alternate.isPresent()) {
            interpret(sourceNode.alternate.get(), context);
        }
        
        return RuntimeValue.NULL;
    }
    
    private RuntimeValue interpretBlockStatement(BlockStatementNode sourceNode, ExecutionContext context) {
        final Scope scope = context.getScope().createChild();
        final ExecutionContext childContext = ExecutionContext.create(scope);
        
        for (final var childNode : sourceNode.children) {
            interpret(childNode, childContext);
        }
            
        return RuntimeValue.NULL;
    }
    
    private RuntimeValue interpretReturnStatement(ReturnStatementNode sourceNode, ExecutionContext context) {
        if (!context.isInsideFunction()) {
            throw new InterpreterRuntimeException("'return' cannot be used outside function declaration", 
                sourceNode.getLocation());
        }
        
        throw new FunctionReturnedException(
            sourceNode.value
                .map(v -> interpretExpression(v, context))
                .orElse(RuntimeValue.NULL)
        );
    }
    
    private RuntimeValue interpretFunctionDeclaration(FunctionDeclarationNode sourceNode, ExecutionContext context) {
        final Scope scope = context.getScope();
        
        if (scope.getImmediateSymbol(sourceNode.name.symbol) != null) {
            throw new InterpreterRuntimeException(
                    String.format("Identifier '%s' is already defined in this scope", sourceNode.name.symbol),
                    sourceNode.name.getLocation());
        }
        
        final RuntimeValue value = new RuntimeValue.FunctionValue(sourceNode.name.symbol, sourceNode);
        scope.setSymbol(new Symbol(sourceNode.name.symbol, sourceNode, value));        
        return value;
    }
    
    private RuntimeValue interpretVariableDeclaration(VariableDeclarationNode sourceNode, ExecutionContext context) {
        final Scope scope = context.getScope();
        
        if (scope.getImmediateSymbol(sourceNode.identifier.symbol) != null) {
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
            int paramCount = fn.declaration != null ? fn.declaration.parameterNames.size() : 0;
            int minArgumentCount = Math.max(paramCount == 0 && fn.isBuiltin() ? Integer.MIN_VALUE : paramCount, fn.getMinArgumentCount());
            int maxArgumentCount = Math.min(paramCount == 0 && fn.isBuiltin() ? Integer.MAX_VALUE : paramCount, fn.getMaxArgumentCount());

            if (arguments.size() < minArgumentCount) {
                throw new InterpreterRuntimeException(
                    String.format("Cannot call function '%s' without at least %d arguments", fn.name, minArgumentCount), 
                    sourceNode.getLocation());
            }

            if (arguments.size() > maxArgumentCount) {
                throw new InterpreterRuntimeException(
                    String.format("Cannot call function '%s' with more than %d arguments", fn.name, maxArgumentCount), 
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
            else {
                final Scope parentScope = context.getScope();
                final Scope scope = parentScope.createChild();
                final ExecutionContext childContext = ExecutionContext.create(scope);
                
                for (int i = 0; i < fn.declaration.parameterNames.size(); i++) {
                    final var parameterName = fn.declaration.parameterNames.get(i);
                    final var argument = i >= arguments.size() ? RuntimeValue.NULL : arguments.get(i);
                    scope.setSymbol(new Symbol(parameterName.symbol, parameterName, argument));
                }
                
                childContext.setInsideFunction(true);
                
                for (final var childNode : fn.declaration.body) {
                    try {
                        interpret(childNode, childContext);
                    }
                    catch (FunctionReturnedException exception) {
                        return exception.value;
                    }
                }
                
                return RuntimeValue.NULL;
            }
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
            return RuntimeValue.BooleanValue.from(booleanLiteral.value);
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
