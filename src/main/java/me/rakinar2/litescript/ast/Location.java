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
package me.rakinar2.litescript.ast;

/**
 * Represents source input location.
 * 
 * @author rakinar2
 */
public record Location(String fileName, long lineStart, long columnStart, 
                       long lineEnd, long columnEnd) implements SourceLocatable {
    public static Location combine(SourceLocatable... locations) {
        long lineStart = Long.MAX_VALUE, columnStart = Long.MAX_VALUE;
        long lineEnd = Long.MIN_VALUE, columnEnd = Long.MIN_VALUE;
        String fileName = null;
        
        if (locations.length == 0) {
            throw new IllegalArgumentException("Must pass at least one Location object");
        }
        
        for (SourceLocatable locatable : locations) {
            final Location location = locatable.getLocation();
            
            if (location == null) {
                continue;
            }
            
            if (location.lineStart < lineStart || 
                (location.lineStart == lineStart && 
                location.columnStart < columnStart)) {
                lineStart = location.lineStart;
                columnStart = location.columnStart;
            }
            
            if (location.lineEnd > lineEnd || 
                (location.lineEnd == lineEnd && 
                location.columnEnd > columnEnd)) {
                lineEnd = location.lineEnd;
                columnEnd = location.columnEnd;
            }
            
            if (fileName == null) {
                fileName = location.fileName;
            }
        }
        
        return new Location(fileName, lineStart, columnStart, lineEnd, columnEnd);
    }
    
    @Override
    public Location getLocation() {
        return this;
    }
}
