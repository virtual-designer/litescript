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

package me.rakinar2.litescript.frontend.lexer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import me.rakinar2.litescript.ast.Location;

/**
 * Performs lexical analysis and generates tokens.
 * 
 * @version 1.0.0
 * @author rakinar2
 */
public class Lexer {
    private static Map<Character, TokenType> SINGLE_CHAR_TOKENS = 
        Map.ofEntries(
            Map.entry('+', TokenType.PLUS),
            Map.entry('-', TokenType.MINUS),
            Map.entry('*', TokenType.TIMES),
            Map.entry('/', TokenType.SLASH),
            Map.entry('%', TokenType.MODULUS),
            Map.entry('(', TokenType.PAREN_OPEN),
            Map.entry(')', TokenType.PAREN_CLOSE),
            Map.entry('{', TokenType.BRACE_OPEN),
            Map.entry('}', TokenType.BRACE_CLOSE),
            Map.entry('[', TokenType.BRACKET_OPEN),
            Map.entry(']', TokenType.BRACKET_CLOSE)
        );
    
    private String[] inputs;
    private int currentInputIndex = 0;
    private long line = 1;
    private long column = 1;
    private int index = 0;
    private long combinedLength;
    
    public Lexer(String[] inputs) {        
        this.inputs = Stream.of(inputs)
            .filter(input -> !input.isEmpty())
            .toArray(String[]::new);
        
        this.combinedLength = Stream.of(this.inputs)
            .mapToLong(str -> (long) str.length())
            .reduce(0L, (acc, len) -> acc + len);
    }
    
    private boolean isInputExhausted() {
        return index >= inputs[currentInputIndex].length() && 
               currentInputIndex >= (inputs.length - 1);
    }
    
    private boolean advance(int length) {   
        index += length;
        
        while (index >= inputs[currentInputIndex].length()) {            
            if (currentInputIndex >= (inputs.length - 1)) {
                return false;
            }
            
            index -= inputs[currentInputIndex].length();
            currentInputIndex++;
        }
        
        return true;
    }
    
    private String getCurrentInputString() {
        return inputs[currentInputIndex];
    }
    
    private char peek(int offset) {
        char c = getCurrentInputString().charAt(index + offset);
        return c;
    }
    
    private char peek() {
        return peek(0);
    }
    
    private void consume(int count) {
        advance(count);
    }
    
    private void consume() {
        consume(1);
    }
    
    public List<Token> lex() throws LexicalAnalysisException {
        List<Token> tokens = new LinkedList<>();

        while (!isInputExhausted()) {
            char c = peek();
            
            if (Character.isSpaceChar(c)) {
                if (c == '\n') {
                    column = 1;
                    line++;
                }
                else {
                    column++;
                }
                
                consume();
                continue;
            }
            
            if (SINGLE_CHAR_TOKENS.containsKey(c)) {
                tokens.add(new Token(SINGLE_CHAR_TOKENS.get(c), 
                           Character.toString(c), 
                           new Location(line, column, line, column + 1)));
                consume();
                column++;
                continue;
            }
            
            if (Character.isDigit(c)) {
                long lineStart = line, columnStart = column;
                StringBuilder builder = new StringBuilder();
                
                while (!isInputExhausted() && 
                       (Character.isDigit(c = peek()) || c == '_')) {
                    if (c != '_') {
                        builder.append(c);
                    }
                    
                    consume();
                    column++;
                }
                
                String str = builder.toString();
                Location location = new Location(lineStart, columnStart, line, column);
                
                try {
                    Long.parseLong(str, 10);
                }
                catch (NumberFormatException exception) {
                    throw new LexicalAnalysisException(
                            "Invalid integer literal: " + str, 
                            location);
                }
                
                tokens.add(new Token(TokenType.INT_LITERAL, str, location));
                continue;
            }
            
            throw new LexicalAnalysisException("Unexpected token: " + c, 
                    new Location(line, column, line, column));
        }
        
        return tokens;
    }
}
