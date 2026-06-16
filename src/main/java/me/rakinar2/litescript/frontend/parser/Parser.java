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
import me.rakinar2.litescript.ast.nodes.ExpressionNode;
import me.rakinar2.litescript.ast.nodes.ExpressionStatementNode;
import me.rakinar2.litescript.ast.nodes.LiteralExpressionNode;
import me.rakinar2.litescript.ast.nodes.RootNode;
import me.rakinar2.litescript.ast.nodes.StatementNode;
import me.rakinar2.litescript.frontend.lexer.Token;
import me.rakinar2.litescript.frontend.lexer.TokenType;

/**
 *
 * @author rakinar2
 */
public class Parser {
    private List<Token> tokens;
    private int index = 0;
    
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
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
            case TokenType.INT_LITERAL:
                value = new LiteralExpressionNode.LiteralValue.Int(Long.parseLong(token.value));
                break;
                
            case TokenType.FLOAT_LITERAL:
                value = new LiteralExpressionNode.LiteralValue.Float(Double.parseDouble(token.value));
                break;
                
            case TokenType.BOOLEAN_TRUE:
            case TokenType.BOOLEAN_FALSE:
                value = new LiteralExpressionNode.LiteralValue.Boolean(token.type == TokenType.BOOLEAN_TRUE);
                break;
                
            case TokenType.STRING_LITERAL:
                value = new LiteralExpressionNode.LiteralValue.String(token.value);
                break;
                
            case TokenType.NULL:
                value = LiteralExpressionNode.LiteralValue.NULL_VALUE;
                break;
                
            default:
                throw new IllegalStateException("This should never be reached");
        }
        
        return new LiteralExpressionNode(value, location);
    }
    
    public ExpressionNode parseExpression() {
        return parseLiteralExpressionNode();
    }
    
    public StatementNode parseStatement() {
        final ExpressionNode expression = parseExpression();
        return new ExpressionStatementNode(expression, expression.location);
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
