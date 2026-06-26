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
import me.rakinar2.litescript.ast.Location;
import me.rakinar2.litescript.ast.nodes.EmptyStatementNode;
import me.rakinar2.litescript.interpreter.RuntimeValue;
import me.rakinar2.litescript.interpreter.Scope;
import me.rakinar2.litescript.interpreter.Symbol;

/**
 *
 * @author rakinar2
 */
public class Loader {
    private static final List<Class<?>> LIBRARY_CLASSES = List.of(
        IO.class,
        MathUtils.class
    );
    
    public void load(Scope scope) {
        final var sourceLocation = new Location("<builtin>", 1, 1, 1, 1);
        
        try {
            for (final var clazz : LIBRARY_CLASSES) {
                final var instance = clazz.getConstructor().newInstance();
                
                for (final var method : clazz.getMethods()) {
                    if (!method.isAnnotationPresent(Export.class)) {
                        continue;
                    }
                    
                    if (method.isAnnotationPresent(Function.class)) {
                        final Function function = method.getAnnotation(Function.class);
                        final String name = function.name().isEmpty() ? method.getName() : function.name();

                        final var value = new RuntimeValue.FunctionValue(
                            name, 
                            function != null ? List.of(function.parameterNames()) : List.of(), 
                            instance, method
                        );
                        
                        value.setMaxArgumentCount(function.maxArgumentCount());
                        value.setMinArgumentCount(function.minArgumentCount());                        
                        value.setBuiltin(true);
                        value.setVariadic(function.varargs());
                        
                        scope.setSymbol(new Symbol(name, new EmptyStatementNode(sourceLocation)).setValue(value));
                        System.out.format("Loaded global function: %s\n", name);
                    }
                }
            }
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
