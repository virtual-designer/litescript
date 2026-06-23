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

/**
 *
 * @author rakinar2
 */
public abstract sealed class RuntimeValue {
    public static final NullValue NULL = NullValue.getInstance();
    
    public abstract String toPrettyString();
    
    public static final class IntValue extends RuntimeValue {
        public final long value;
        
        public IntValue(long value) {
            this.value = value;
        }

        @Override
        public String toPrettyString() {
            return String.format("[RuntimeValue (Int): %d]", value);
        }
    }
    
    public static final class FloatValue extends RuntimeValue {
        public final double value;
        
        public FloatValue(double value) {
            this.value = value;
        }

        @Override
        public String toPrettyString() {
            return String.format("[RuntimeValue (Float): %f]", value);
        }
    }
    
    public static final class StringValue extends RuntimeValue {
        public final String value;
        
        public StringValue(String value) {
            this.value = value;
        }

        @Override
        public String toPrettyString() {
            return String.format("[RuntimeValue (String): |%s|]", value);
        }
    }
    
    public static final class BooleanValue extends RuntimeValue {
        public final boolean value;
        
        public BooleanValue(boolean value) {
            this.value = value;
        }

        @Override
        public String toPrettyString() {
            return String.format("[RuntimeValue (Boolean): %s]", value ? "true" : "false");
        }
    }
    
    public static final class NullValue extends RuntimeValue {
        private static NullValue instance = null;
        
        private NullValue() {
            
        }
        
        @Override
        public String toPrettyString() {
            return String.format("[RuntimeValue (Null)]");
        }
        
        public static NullValue getInstance() {
            if (instance == null) {
                instance = new NullValue();
            }
            
            return instance;
        }
    }
    
    public static String getTypeOf(RuntimeValue value) {
        if (value instanceof IntValue) {
            return "Int";
        }
        
        if (value instanceof FloatValue) {
            return "Float";
        }
        
        if (value instanceof StringValue) {
            return "String";
        }
        
        if (value instanceof BooleanValue) {
            return "Boolean";
        }
        
        if (value instanceof NullValue) {
            return "Null";
        }
        
        throw new IllegalStateException("Invalid literal");
    }
    
    public static FloatValue convertValueToFloat(RuntimeValue value) {
        if (value instanceof FloatValue floatValue) {
            return floatValue;
        }
        
        if (value instanceof IntValue intValue) {
            return new FloatValue((double) intValue.value);
        }
        
        throw new IllegalStateException(String.format("Cannot convert '%s' to Float", getTypeOf(value)));
    }
    
    public static StringValue convertValueToString(RuntimeValue value) {
        if (value instanceof IntValue intValue) {
            return new StringValue(Long.toString(intValue.value));
        }
        
        if (value instanceof FloatValue floatValue) {
            return new StringValue(ValueFormatter.DECIMAL_FORMATTER.format(floatValue.value));
        }
        
        if (value instanceof StringValue stringValue) {
            return stringValue;
        }
        
        if (value instanceof BooleanValue booleanValue) {
            return new StringValue(booleanValue.value ? "true" : "false");
        }
        
        if (value instanceof NullValue) {
            return new StringValue("null");
        }
        
        throw new IllegalStateException("Invalid literal");
    }
}
