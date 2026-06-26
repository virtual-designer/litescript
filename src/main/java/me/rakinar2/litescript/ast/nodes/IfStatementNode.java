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
import java.util.Optional;
import me.rakinar2.litescript.ast.Location;

/**
 *
 * @author rakinar2
 */
public class IfStatementNode extends StatementNode {
    public final ExpressionNode condition;
    public final AbstractNode then;
    public final Optional<AbstractNode> alternate;
    
    public IfStatementNode(ExpressionNode condition, AbstractNode then, Location location) {
        super(location);
        this.condition = condition;
        this.then = then;
        this.alternate = Optional.empty();
    }
    
    public IfStatementNode(ExpressionNode condition, AbstractNode then, AbstractNode alternate, Location location) {
        super(location);
        this.condition = condition;
        this.then = then;
        this.alternate = Optional.ofNullable(alternate);
    }

    @Override
    public Iterable<AbstractNode> getBranches() {
        return alternate.isEmpty() ? List.of(condition, then) : List.of(condition, then, alternate.get());
    }
}
