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
 *
 * @author rakinar2
 */
public class BinaryExpressionNode extends ExpressionNode {
    public final ExpressionNode left;
    public final ExpressionNode right;
    public final BinaryOperator operator;
    
    public BinaryExpressionNode(ExpressionNode left, ExpressionNode right, 
            BinaryOperator operator, Location location) {
        super(location);
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public Iterable<AbstractNode> getBranches() {
        return List.of(left, right);
    }    
}
