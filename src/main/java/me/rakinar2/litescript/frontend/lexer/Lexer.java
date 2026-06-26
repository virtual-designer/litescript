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
            Map.entry(']', TokenType.BRACKET_CLOSE),
            Map.entry(';', TokenType.SEMICOLON),
            Map.entry('.', TokenType.DOT),
            Map.entry(',', TokenType.COMMA),
            Map.entry('=', TokenType.EQUAL)
        );
    
    private static Map<String, TokenType> KEYWORD_TOKENS = 
        Map.ofEntries(
            Map.entry("true", TokenType.BOOLEAN_TRUE),
            Map.entry("false", TokenType.BOOLEAN_FALSE),
            Map.entry("null", TokenType.NULL),
            Map.entry("final", TokenType.FINAL),
            Map.entry("let", TokenType.LET),
            Map.entry("return", TokenType.RETURN),
            Map.entry("function", TokenType.FUNCTION),
            Map.entry("if", TokenType.IF),
            Map.entry("else", TokenType.ELSE)
        );
    
    private String fileName;
    private String[] inputs;
    private int currentInputIndex = 0;
    private long line = 1;
    private long column = 1;
    private int index = 0;
    
    @FunctionalInterface
    private static interface TokenConsumer {
        public boolean consume(List<Token> tokens) throws LexicalAnalysisException; 
    }
    
    public Lexer(String fileName, String[] inputs) {
        this.fileName = fileName;
        this.inputs = Stream.of(inputs)
            .filter(input -> !input.isEmpty())
            .toArray(String[]::new);
    }
    
    private boolean isInputExhausted() {
        return currentInputIndex >= (inputs.length - 1) && 
               (inputs.length == 0 || index >= inputs[currentInputIndex].length());
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
    
    private char peek(int offset) throws LexicalAnalysisException {
        try {
            char c = getCurrentInputString().charAt(index + offset);
            return c;
        }
        catch (IndexOutOfBoundsException exception) {
            throw new LexicalAnalysisException("Unexpected end of file", 
                    new Location(fileName, line, column, line, column), exception);
        }
    }
    
    private char peek() throws LexicalAnalysisException {
        return peek(0);
    }
    
    private void consume(int count) {
        advance(count);
    }
    
    private void consume() {
        consume(1);
    }
    
    private boolean lexSpace(@SuppressWarnings("unused") List<Token> _tokens) throws LexicalAnalysisException {
        boolean trimmed = false;
        
        while (!isInputExhausted()) {
            char c = peek();
            
            if (c == '\t' || c == ' ' || c == '\r' || c == '\n') {
                if (c == '\n') {
                    column = 1;
                    line++;
                }
                else {
                    column++;
                }

                consume();
                trimmed = true;
                continue;
            }
            
            break;
        }
        
        return trimmed;
    }
    
    private boolean lexStringLiteral(List<Token> tokens) throws LexicalAnalysisException {
        char c = peek();
        
        if (c == '"' || c == '\'') {
            char quote = c;
            long lineStart = line, columnStart = column;
            StringBuilder builder = new StringBuilder();

            consume();

            while (!isInputExhausted() && (c = peek()) != quote) {
                if (c == '\\') {
                    consume();

                    c = peek();
                    c = switch (c) {
                        case 'n' -> '\n';
                        case 'r' -> '\r';
                        case 'f' -> '\f';
                        case 't' -> '\t';
                        case 'b' -> '\b';
                        case 'v' -> '\u000b';
                        case '\\' -> '\\';
                        case '\'' -> '\'';
                        case '"' -> '"';
                        default -> 
                            throw new LexicalAnalysisException("Invalid escape sequence: \\" + c, 
                                    new Location(fileName, line, column, line, column + 1));
                    };
                }

                if (c == '\n') {
                    column = 1;
                    line++;
                }
                else {
                    column++;
                }

                builder.append(c);
                consume();
            }

            if (isInputExhausted() || peek() != quote) {
                throw new LexicalAnalysisException(
                        "Unterminated string literal: Expected " + quote, 
                        new Location(fileName, lineStart, columnStart, line, column));
            }

            consume();

            String str = builder.toString();
            Location location = new Location(fileName, lineStart, columnStart, line, column);

            tokens.add(new Token(TokenType.STRING_LITERAL, str, location));
            return true;
        }
    
        return false;
    }
    
    private boolean lexNumericLiteral(List<Token> tokens) throws LexicalAnalysisException {
        char c = peek();
        
        if (Character.isDigit(c)) {
            long lineStart = line, columnStart = column;
            boolean isFloat = false;
            StringBuilder builder = new StringBuilder();

            while (!isInputExhausted() && 
                   (Character.isDigit(c = peek()) || c == '_' || c == '.')) {
                if (c == '.') {
                    if (isFloat) {
                        break;
                    }
                    
                    isFloat = true;
                }
                
                if (c != '_') {
                    builder.append(c);
                }

                consume();
                column++;
            }

            String str = builder.toString();
            Location location = new Location(fileName, lineStart, columnStart, line, column);

            try {
                if (isFloat) {
                    Double.parseDouble(str);
                }
                else {
                    Long.parseLong(str, 10);
                }
            }
            catch (NumberFormatException exception) {
                throw new LexicalAnalysisException(
                        "Invalid integer literal: " + str, 
                        location);
            }

            tokens.add(new Token(isFloat ? TokenType.FLOAT_LITERAL : TokenType.INT_LITERAL, str, location));
            return true;
        }
            
        return false;
    }
    
    private boolean lexIdentifier(List<Token> tokens) throws LexicalAnalysisException {
        char c = peek();
        
        if (Character.isAlphabetic(c)) {
            long lineStart = line, columnStart = column;
            StringBuilder builder = new StringBuilder();

            while (!isInputExhausted() && 
                   (Character.isLetterOrDigit(c = peek()) || c == '_' || c == '$')) {
                builder.append(c);                    
                consume();
                column++;
            }

            String str = builder.toString();
            Location location = new Location(fileName, lineStart, columnStart, line, column);

            tokens.add(new Token(KEYWORD_TOKENS.getOrDefault(str, TokenType.IDENTIFIER), str, location));
            return true;
        }
        
        return false;
    }
    
    public List<Token> lex() throws LexicalAnalysisException {
        final List<Token> tokens = new LinkedList<>();
        final TokenConsumer[] tokenConsumers = {
            this::lexSpace,
            this::lexIdentifier,
            this::lexStringLiteral,
            this::lexNumericLiteral,
        };

        mainLoop:
        while (!isInputExhausted()) {            
            for (final TokenConsumer consumer : tokenConsumers) {
                if (consumer.consume(tokens)) {
                    continue mainLoop;
                }
            }
            
            final char c = peek();
            
            if (SINGLE_CHAR_TOKENS.containsKey(c)) {
                tokens.add(new Token(SINGLE_CHAR_TOKENS.get(c), 
                           Character.toString(c), 
                           new Location(fileName, line, column, line, column + 1)));
                consume();
                column++;
                continue mainLoop;
            }
            
            throw new LexicalAnalysisException("Unexpected token: " + c, 
                    new Location(fileName, line, column, line, column));
        }
        
        tokens.add(new Token(TokenType.EOF, "[EOF]", new Location(fileName, line, column, line, column)));
        return tokens;
    }
}
