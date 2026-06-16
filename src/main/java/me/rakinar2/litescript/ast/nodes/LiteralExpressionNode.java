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
public class LiteralExpressionNode extends ExpressionNode {
    public static sealed class LiteralValue {        
        public static final class Int extends LiteralValue {
            public final long value;
            
            public Int(long value) {
                this.value = value;
            }

            @Override
            public java.lang.String toString() {
                return java.lang.String.format("[Int %d]", value);
            }
        }
        
        public static final class Float extends LiteralValue {
            public final double value;
            
            public Float(double value) {
                this.value = value;
            }

            @Override
            public java.lang.String toString() {
                return java.lang.String.format("[Float %f]", value);
            }
        }
        
        public static final class String extends LiteralValue {
            public final java.lang.String value;
            
            public String(java.lang.String value) {
                this.value = value;
            }

            @Override
            public java.lang.String toString() {
                return java.lang.String.format("[String |%s|]", value);
            }
        }
        
        public static final class Boolean extends LiteralValue {
            public final boolean value;
            
            public Boolean(boolean value) {
                this.value = value;
            }
            
            @Override
            public java.lang.String toString() {
                return java.lang.String.format("[Boolean %s]", value ? "true" : "false");
            }
        }
        
        public static final class Null extends LiteralValue {
            @Override
            public java.lang.String toString() {
                return "[Null]";
            }
        }
        
        public static final Null NULL_VALUE = new Null();
    }
    
    public final LiteralValue value;
    
    public LiteralExpressionNode(LiteralValue value, Location location) {
        super(location);
        this.value = value;
    }
    
    public LiteralExpressionNode(long value, Location location) {
        this(new LiteralValue.Int(value), location);
    }
    
    public LiteralExpressionNode(double value, Location location) {
        this(new LiteralValue.Float(value), location);
    }
    
    public LiteralExpressionNode(boolean value, Location location) {
        this(new LiteralValue.Boolean(value), location);
    }
    
    public LiteralExpressionNode(String value, Location location) {
        this(new LiteralValue.String(value), location);
    }
    
    public static LiteralExpressionNode newNull(Location location) {
        return new LiteralExpressionNode(LiteralValue.NULL_VALUE, location);
    }

    @Override
    public Iterable<AbstractNode> getBranches() {
        return List.of();
    }
}
