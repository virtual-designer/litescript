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
package me.rakinar2.litescript.frontend.parser;

import java.util.LinkedList;
import java.util.List;
import me.rakinar2.litescript.ast.Location;
import me.rakinar2.litescript.ast.SourceLocatable;
import me.rakinar2.litescript.ast.nodes.AbstractNode;
import me.rakinar2.litescript.ast.nodes.AssignmentExpressionNode;
import me.rakinar2.litescript.ast.nodes.BinaryExpressionNode;
import me.rakinar2.litescript.ast.nodes.BinaryOperator;
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
import me.rakinar2.litescript.frontend.lexer.Token;
import me.rakinar2.litescript.frontend.lexer.TokenType;

/**
 *
 * @author rakinar2
 */
public class Parser {
    private static final List<Class<? extends AbstractNode>> NODES_WITHOUT_TRAILING_SEMICOLON = 
        List.of(
            EmptyStatementNode.class,
            FunctionDeclarationNode.class,
            IfStatementNode.class
        );
    
    private final List<Token> tokens;
    private boolean requireSemicolons = true;
    private int index = 0;
    private boolean insideFunctionDeclaration = false;
    
    public Parser(List<Token> tokens, boolean requireSemicolons) {
        this.tokens = tokens;
        this.requireSemicolons = requireSemicolons;
    }
    
    private boolean isEOF() {
        return index >= tokens.size() || tokens.get(index).type == TokenType.EOF;
    }
    
    private Token peek(int offset) {
        return offset + index >= tokens.size() ? null : tokens.get(offset + index);
    }
    
    private Token peek() {
        return peek(0);
    }
    
    private void consume(int count) {
        if (count == 0) {
            throw new IllegalArgumentException("Cannot consume 0 tokens");
        }
        
        index += count;
    }
    
    private void consume() {
        consume(1);
    }
    
    private Token expect(TokenType... types) {
        Token token = peek();
        return expect(String.format("Unexpected token: %s", token.value), types);
    }
    
    private Token expect(String errorMessage, TokenType... types) {
        Token token = peek();
        
        if (token != null) {
            for (TokenType type : types) {
                if (type == token.type) {
                    consume();
                    return token;
                }
            }
        }
        
        throw new ParserException(errorMessage, 
                token != null ? token.getLocation() : 
                tokens.get(tokens.size() - 1).getLocation());
    }
    
    private LiteralExpressionNode parseLiteralExpressionNode() {
        final Token token = expect(
            "Expected literal", 
            TokenType.INT_LITERAL, 
            TokenType.FLOAT_LITERAL, 
            TokenType.STRING_LITERAL,
            TokenType.BOOLEAN_TRUE,
            TokenType.BOOLEAN_FALSE,
            TokenType.NULL
        );
        
        final Location location = token.location;
        LiteralExpressionNode.LiteralValue value;
        
        switch (token.type) {
            case INT_LITERAL:
                value = new LiteralExpressionNode.LiteralValue.Int(Long.parseLong(token.value));
                break;
                
            case FLOAT_LITERAL:
                value = new LiteralExpressionNode.LiteralValue.Float(Double.parseDouble(token.value));
                break;
                
            case BOOLEAN_TRUE:
            case BOOLEAN_FALSE:
                value = new LiteralExpressionNode.LiteralValue.Boolean(token.type == TokenType.BOOLEAN_TRUE);
                break;
                
            case STRING_LITERAL:
                value = new LiteralExpressionNode.LiteralValue.String(token.value);
                break;
                
            case NULL:
                value = LiteralExpressionNode.LiteralValue.NULL_VALUE;
                break;
                
            default:
                throw new IllegalStateException("This should never be reached");
        }
        
        return new LiteralExpressionNode(value, location);
    }
    
    public ExpressionNode parseMultiplicativeBinaryExpression() {
        ExpressionNode left = parseCallExpressionNode();
        
        while (!isEOF() && 
                (peek().type == TokenType.TIMES || 
                peek().type == TokenType.SLASH || 
                peek().type == TokenType.MODULUS)) {
            final Token operatorToken = 
                    expect("Expected additive operator", 
                            TokenType.TIMES, TokenType.SLASH, TokenType.MODULUS);
        
            final BinaryOperator operator = operatorToken.type == TokenType.TIMES
                ? BinaryOperator.MULTIPLY
                : operatorToken.type == TokenType.SLASH
                ? BinaryOperator.DIVIDE
                : BinaryOperator.MODULUS;
            
            final ExpressionNode right = parseCallExpressionNode();
            left = new BinaryExpressionNode(left, right, operator, Location.combine(left, right));
        }
        
        return left;
    }
    
    public ExpressionNode parseAdditiveBinaryExpression() {
        ExpressionNode left = parseMultiplicativeBinaryExpression();
        
        while (!isEOF() && (peek().type == TokenType.PLUS || peek().type == TokenType.MINUS)) {
            final Token operatorToken = expect("Expected additive operator", TokenType.PLUS, TokenType.MINUS);
            final BinaryOperator operator = operatorToken.type == TokenType.PLUS 
                ? BinaryOperator.ADD
                : BinaryOperator.SUBTRACT;
            
            final ExpressionNode right = parseMultiplicativeBinaryExpression();
            left = new BinaryExpressionNode(left, right, operator, Location.combine(left, right));
        }
        
        return left;
    }
    
    public IdentifierNode parseIdentifierNode() {
        final Token identifierToken = expect(TokenType.IDENTIFIER);
        return new IdentifierNode(identifierToken.value, identifierToken.location);
    }
    
    public ExpressionNode parseCallExpressionNode() {
        ExpressionNode callee = parsePrimaryExpression();
        
        if (!isEOF() && peek().type == TokenType.PAREN_OPEN) {
            expect(TokenType.PAREN_OPEN);
            
            List<ExpressionNode> arguments = new LinkedList<>();
            ExpressionNode lastArg = null;
            
            while (!isEOF() && peek().type != TokenType.PAREN_CLOSE) {
                lastArg = parseExpression();
                arguments.add(lastArg);
                
                if ((!isEOF() && peek().type != TokenType.PAREN_CLOSE)
                    || (peek(1) != null && peek().type == TokenType.COMMA 
                        && peek(1).type == TokenType.PAREN_CLOSE)) {
                    expect(TokenType.COMMA);
                }
            }
            
            expect(TokenType.PAREN_CLOSE);
            return new CallExpressionNode(callee, arguments, Location.combine(callee, lastArg));
        }
        
        return callee;
    }
    
    public ExpressionNode parsePrimaryExpression() {        
        return switch (peek().type) {
            case PAREN_OPEN -> {
                expect(TokenType.PAREN_OPEN);
                final var expression = parseExpression();
                expect(TokenType.PAREN_CLOSE);
                yield expression;
            }
            
            case INT_LITERAL, 
                 FLOAT_LITERAL, 
                 STRING_LITERAL,
                 BOOLEAN_TRUE,
                 BOOLEAN_FALSE,
                 NULL -> parseLiteralExpressionNode();
                
            case IDENTIFIER ->
                parseIdentifierNode();
        
            default -> throw new ParserException("Unexpected token: " + peek().value, peek().location);
        };
    }
    
    public ExpressionNode parseAssignmentExpression() {
        Token peekToken = peek(1);
        
        if (peekToken != null && peekToken.type == TokenType.EQUAL) {
            ExpressionNode left = parseIdentifierNode();
            expect(TokenType.EQUAL);
            ExpressionNode right = parseExpression();
            return new AssignmentExpressionNode(left, right, Location.combine(left, right));
        }
        
        return parseAdditiveBinaryExpression();
    }
    
    public ExpressionNode parseExpression() {
        return parseAssignmentExpression();
    }
    
    public VariableDeclarationNode parseVariableDeclaration() {
        final Token token = expect(TokenType.LET, TokenType.FINAL);
        final IdentifierNode identifier = parseIdentifierNode();
        ExpressionNode value = null;
        
        if (!isEOF() && peek().type == TokenType.EQUAL) {
            consume();
            value = parseExpression();
        }
        
        return new VariableDeclarationNode(
            token.type == TokenType.FINAL 
                    ? VariableDeclarationNode.Kind.FINAL 
                    : VariableDeclarationNode.Kind.LET, 
            identifier, 
            value,
            Location.combine(token, identifier, value)
        );
    }
    
    public FunctionDeclarationNode parseFunctionDeclaration() {
        insideFunctionDeclaration = true;
        
        final Token token = expect(TokenType.FUNCTION);
        final IdentifierNode identifier = parseIdentifierNode();
        final List<IdentifierNode> parameters = new LinkedList<>();
        final List<AbstractNode> body = new LinkedList<>();
        
        expect(TokenType.PAREN_OPEN);
        
        while (!isEOF() && peek().type != TokenType.PAREN_CLOSE) {
            parameters.add(parseIdentifierNode());

            if ((!isEOF() && peek().type != TokenType.PAREN_CLOSE)
                || (peek(1) != null && peek().type == TokenType.COMMA 
                    && peek(1).type == TokenType.PAREN_CLOSE)) {
                expect(TokenType.COMMA);
            }
        }
        
        expect(TokenType.PAREN_CLOSE);
        expect(TokenType.BRACE_OPEN);
        
        while (!isEOF() && peek().type != TokenType.BRACE_CLOSE) {
            body.add(parseStatement());
        }
        
        final var lastToken = expect(TokenType.BRACE_CLOSE);
        
        insideFunctionDeclaration = false;
        
        return new FunctionDeclarationNode(
            identifier,
            parameters,
            body,
            Location.combine(token, identifier, lastToken)
        );
    }
    
    public ReturnStatementNode parseReturnStatement() {
        if (!insideFunctionDeclaration) {
            throw new ParserException("'return' cannot be used outside function declaration", peek().location);
        }
        
        final var token = expect(TokenType.RETURN);
        ExpressionNode value = null;
        
        if (!isEOF() && peek().type != TokenType.SEMICOLON) {
            value = parseExpression();   
        }
        
        return new ReturnStatementNode(value, Location.combine(token, value));
    }
    
    public BlockStatementNode parseBlockStatement() {
         final var startToken = expect(TokenType.BRACE_OPEN);
         final var children = new LinkedList<AbstractNode>();
         
         while (!isEOF() && peek().type != TokenType.BRACE_CLOSE) {
             children.add(parseStatement());
         }
         
         final var endToken = expect(TokenType.BRACE_CLOSE);
         return new BlockStatementNode(children, Location.combine(startToken, endToken));
    }
    
    public IfStatementNode parseIfStatement() {
        final var ifToken = expect(TokenType.IF);
        
        expect(TokenType.PAREN_OPEN);
        final var condition = parseExpression();
        expect(TokenType.PAREN_CLOSE);
        
        final var then = parseStatement();
        StatementNode alternate = null;
        
        if (!isEOF() && peek().type == TokenType.ELSE) {
            consume();
            alternate = parseStatement();
        }
        
        return new IfStatementNode(condition, then, alternate, Location.combine(ifToken, then, alternate));
    }
    
    public StatementNode parseStatement() {
        StatementNode statement = switch (peek().type) {
            case SEMICOLON -> new EmptyStatementNode(expect(TokenType.SEMICOLON).location);
            case LET, FINAL -> parseVariableDeclaration();
            case FUNCTION -> parseFunctionDeclaration();
            case RETURN -> parseReturnStatement();
            case BRACE_OPEN -> parseBlockStatement();
            case IF -> parseIfStatement();
                
            default -> {
                final ExpressionNode expression = parseExpression();
                yield new ExpressionStatementNode(expression, expression.location);
            }
        };
        
        if (requireSemicolons && !NODES_WITHOUT_TRAILING_SEMICOLON.contains(statement.getClass())) {
            expect("Expected ';' at the end of statement", TokenType.SEMICOLON);
        }
        
        while (!isEOF() && peek().type == TokenType.SEMICOLON) {
            consume();
        }

        return statement;
    }
    
    public RootNode parseAll() {
        final List<AbstractNode> statements = new LinkedList<>();
        final Location combinedLocation = tokens.size() >= 2 
                ? Location.combine(tokens.get(0), tokens.get(1)) 
                : Location.combine(tokens.toArray(SourceLocatable[]::new));
        
        while (!isEOF()) {
            statements.add(parseStatement());
        }
        
        return new RootNode(statements, combinedLocation);
    }
}
