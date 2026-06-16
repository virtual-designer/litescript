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

/**
 * All possible token types.
 * 
 * @author rakinar2
 */
public enum TokenType {
    EOF,
    IDENTIFIER,
    INT_LITERAL,
    FLOAT_LITERAL,
    STRING_LITERAL,
    BOOLEAN_TRUE,
    BOOLEAN_FALSE,
    NULL,
    PLUS,
    MINUS,
    TIMES,
    SLASH,
    MODULUS,
    PAREN_OPEN,
    PAREN_CLOSE,
    BRACE_OPEN,
    BRACE_CLOSE,
    BRACKET_OPEN,
    BRACKET_CLOSE,
}
