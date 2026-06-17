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

import me.rakinar2.litescript.ast.Location;
import me.rakinar2.litescript.ast.nodes.AbstractNode;

/**
 *
 * @author rakinar2
 */
public class Symbol {
    private String name;
    private AbstractNode sourceNode;
    
    public Symbol(String name, AbstractNode sourceNode) {
        this.name = name;
        this.sourceNode = sourceNode;
    }
    
    public AbstractNode getSourceNode() {
        return sourceNode;
    }
    
    public Location getLocation() {
        return sourceNode.location;
    }
    
    public String getName() {
        return name;
    }
}
