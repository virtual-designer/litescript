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
package me.rakinar2.litescript.stdlib;

import java.util.List;
import me.rakinar2.litescript.interpreter.RuntimeValue;

/**
 *
 * @author rakinar2
 */
public class IO {
    @Export
    @Function(varargs = true)
    public RuntimeValue println(RuntimeValue... arguments) {
        final StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < arguments.length; i++) {
            sb.append(RuntimeValue.convertValueToString(arguments[i]).value);
            
            if (i < arguments.length - 1) {
                sb.append(" ");
            }
        }
        
        System.out.println(sb.toString());
        return RuntimeValue.NULL;
    }
    
    @Export
    @Function(minArgumentCount = 1, maxArgumentCount = 1)
    public RuntimeValue print(RuntimeValue argument) {
        System.out.print(RuntimeValue.convertValueToString(argument).value);
        return RuntimeValue.NULL;
    }
}
