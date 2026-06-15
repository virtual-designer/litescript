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

import me.rakinar2.litescript.ast.Location;

/**
 * Token representation.
 * 
 * @author rakinar2
 */
public final class Token {
    public final TokenType type;
    public final String value;
    public final Location location;
    private String str;
    
    public Token(TokenType type, String value, Location location) {
        this.type = type;
        this.value = value;
        this.location = location;
    }

    @Override
    public String toString() {
        if (str == null) {
            str = String.format("Token(type=%s, value=\"%s\")", type.name(), value);
        }
        
        return str;
    }
}
