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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rakinar2
 */
public class Scope {
    private static Scope globalScope = null;
    
    private Scope parent = null;
    private Map<String, Symbol> symbolTable = new HashMap<>();
    
    public Scope() {
        this.parent = null;
    }
    
    public Scope(Scope parent) {
        this.parent = parent;
    }
    
    /**
     * Gets a symbol from the scope, or its parents in chain.
     * 
     * @return The symbol if found, null otherwise.
     */
    public Symbol getSymbol(String name) {
        Symbol symbol = symbolTable.getOrDefault(name, null);
        
        if (symbol == null) {
            return parent == null ? null : parent.getSymbol(name);
        }
        
        return symbol;
    }
    
    public void setSymbol(Symbol symbol) {
        Symbol existing = symbolTable.getOrDefault(symbol.getName(), null);
        
        if (existing != null) {
            throw new InterpreterRuntimeException(
                    String.format("Identifier '%s' is already declared in this scope", 
                    existing.getName()), existing.getLocation());
        }
        
        symbolTable.put(symbol.getName(), symbol);
    }
    
    public static Scope createGlobal() {
        if (globalScope == null) {
            globalScope = new Scope();            
        }
        
        return globalScope;
    }
    
    public Scope createChild() {
        return new Scope(this);
    }
}
