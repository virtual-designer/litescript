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
public class VariableDeclarationNode extends DeclarationNode {
    public static enum Kind {
        LET,
        FINAL
    }
    
    public final Kind kind;
    public final IdentifierNode identifier;
    public final Optional<ExpressionNode> value;
    
    public VariableDeclarationNode(Kind kind, IdentifierNode identifier, 
            Location location) {
        super(location);
        this.kind = kind;
        this.identifier = identifier;
        this.value = Optional.empty();
    }
    
    public VariableDeclarationNode(Kind kind, IdentifierNode identifier, 
            ExpressionNode value, Location location) {
        super(location);
        this.kind = kind;
        this.identifier = identifier;
        this.value = Optional.ofNullable(value);
    }
    
    @Override
    public Iterable<AbstractNode> getBranches() {
        return value.isPresent() ? List.of(identifier, value.get()) : List.of(identifier);
    }
}
