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

import me.rakinar2.litescript.ast.Location;
import me.rakinar2.litescript.ast.nodes.AbstractNode;
import me.rakinar2.litescript.ast.nodes.AssignmentExpressionNode;
import me.rakinar2.litescript.ast.nodes.BinaryExpressionNode;
import me.rakinar2.litescript.ast.nodes.BinaryOperator;
import me.rakinar2.litescript.ast.nodes.EmptyStatementNode;
import me.rakinar2.litescript.ast.nodes.ExpressionNode;
import me.rakinar2.litescript.ast.nodes.ExpressionStatementNode;
import me.rakinar2.litescript.ast.nodes.IdentifierNode;
import me.rakinar2.litescript.ast.nodes.LiteralExpressionNode;
import me.rakinar2.litescript.ast.nodes.RootNode;
import me.rakinar2.litescript.ast.nodes.StatementNode;
import me.rakinar2.litescript.ast.nodes.VariableDeclarationNode;

/**
 * Main interpreter execution unit.
 * 
 * @author rakinar2
 */
public class Interpreter {
    private PrimitiveOperationUnit primitiveOperationUnit = new PrimitiveOperationUnit();
    
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
                interpretRoot(rootNode, context);
                
            case VariableDeclarationNode node ->
                interpretVariableDeclaration(node, context);
                
            case EmptyStatementNode _ -> RuntimeValue.NullValue.getInstance();
            
            default ->
                throw new InterpreterRuntimeException("Unable to interpret node", 
                        sourceNode.getLocation());
        };
        
        return value;
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
        
        for (var childNode : sourceNode.statements) {
            if (childNode instanceof EmptyStatementNode) {
                continue;
            }
            
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
                
            case IdentifierNode identifier ->
                interpretIdentifier(identifier, context);
                
            case AssignmentExpressionNode node ->
                interpretAssignmentExpression(node, context);
            
            default ->
                throw new InterpreterRuntimeException("Unable to interpret node", 
                        sourceNode.getLocation());
        };
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
        
        return switch (sourceNode.operator) {
            case BinaryOperator.ADD -> 
                primitiveOperationUnit.computeBinaryAdd(sourceNode, left, right);
            case BinaryOperator.SUBTRACT -> 
                primitiveOperationUnit.computeBinarySubtract(sourceNode, left, right);
            case BinaryOperator.MULTIPLY -> 
                primitiveOperationUnit.computeBinaryMultiply(sourceNode, left, right);
            case BinaryOperator.DIVIDE -> 
                primitiveOperationUnit.computeBinaryDivide(sourceNode, left, right);
            case BinaryOperator.MODULUS -> 
                primitiveOperationUnit.computeBinaryModulus(sourceNode, left, right);
            
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
