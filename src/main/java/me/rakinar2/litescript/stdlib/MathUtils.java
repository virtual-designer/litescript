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

import me.rakinar2.litescript.interpreter.RuntimeValue;

/**
 *
 * @author rakinar2
 */
public class MathUtils {
    @Export
    @Function(varargs = true, minArgumentCount = 1)
    public RuntimeValue max(RuntimeValue... arguments) {
        long maxValue = Long.MIN_VALUE;
        
        for (final var value : arguments) {
            if (value instanceof RuntimeValue.IntValue i) {
                if (i.value > maxValue) {
                    maxValue = i.value;
                }
            }
        }
        
        return new RuntimeValue.IntValue(maxValue);
    }
    
    @Export
    @Function(varargs = true, minArgumentCount = 1)
    public RuntimeValue min(RuntimeValue... arguments) {
        long minValue = Long.MAX_VALUE;
        
        for (final var value : arguments) {
            if (value instanceof RuntimeValue.IntValue i) {
                if (i.value < minValue) {
                    minValue = i.value;
                }
            }
        }
        
        return new RuntimeValue.IntValue(minValue);
    }
    
}
