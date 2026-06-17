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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 *
 * @author rakinar2
 */
public class ValueFormatter {
    public final static DecimalFormat DECIMAL_FORMATTER;
    
    static {
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.US);
        
        formatSymbols.setInfinity("Infinity");
        formatSymbols.setNaN("NaN");
        
        DECIMAL_FORMATTER = new DecimalFormat("0.0###############", formatSymbols);
    }
    
    private static boolean isColorSupported() {
        return System.console() != null;
    }
    
    public static String format(RuntimeValue runtimeValue) {        
        return switch (runtimeValue) {
            case RuntimeValue.IntValue intValue ->
                format(intValue.value);
            
            case RuntimeValue.BooleanValue booleanValue -> 
               format(booleanValue.value);
                
            case RuntimeValue.FloatValue floatValue ->
                format(floatValue.value);
                
            case RuntimeValue.StringValue stringValue ->
                format(stringValue.value);
                
            case RuntimeValue.NullValue _ -> 
                formatNull();
                
            default -> throw new IllegalStateException("Invalid value");
        };
    }
    
    private static String format(long value) {
        return isColorSupported() 
                ? String.format("\033[33m%d\033[0m", value) 
                : Long.toString(value);
    }
    
    private static String format(double value) {
        return isColorSupported() 
                ? String.format("\033[33m%s\033[0m", DECIMAL_FORMATTER.format(value)) 
                : DECIMAL_FORMATTER.format(value);
    }
    
    private static String format(boolean value) {
        return isColorSupported() 
                ? String.format("\033[34m%s\033[0m", value ? "true" : "false") 
                : value ? "true" : "false";
    }
    
    private static String format(String value) {
        return isColorSupported() 
                ? String.format("\033[32m\"%s\"\033[0m", value.replaceAll("\"", "\\\\\"")) 
                : String.format("\"%s\"", value.replaceAll("\"", "\\\\\"")) ;
    }
    
    private static String formatNull() {
        return isColorSupported() ? "\033[2mnull\033[0m" : "null";
    }
}
