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
package me.rakinar2.litescript.ast.nodes;

import java.util.LinkedList;
import java.util.List;
import me.rakinar2.litescript.ast.Location;

/**
 *
 * @author rakinar2
 */
public class FunctionDeclarationNode extends DeclarationNode {
    public final IdentifierNode name;
    public final List<IdentifierNode> parameterNames;
    public final List<AbstractNode> body;
    
    public FunctionDeclarationNode(IdentifierNode name, 
            List<IdentifierNode> parameterNames, 
            List<AbstractNode> body, Location location) {
        super(location);
        this.name = name;
        this.parameterNames = parameterNames;
        this.body = body;
    }

    @Override
    public Iterable<AbstractNode> getBranches() {
        final var list = new LinkedList<AbstractNode>();
        
        list.add(name);
        list.addAll(parameterNames);
        list.addAll(body);
        
        return list;
    }
}
