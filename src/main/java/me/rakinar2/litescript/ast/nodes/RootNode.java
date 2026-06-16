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

import java.util.List;
import me.rakinar2.litescript.ast.Location;

/**
 * Represents the entire program.
 * 
 * @author rakinar2
 */
public class RootNode extends StatementNode {
    public final List<AbstractNode> statements;
    
    public RootNode(List<AbstractNode> statements, Location location) {
        super(location);
        this.statements = statements;
    }
    
    @Override
    public Iterable<AbstractNode> getBranches() {
        return statements;
    }
}
