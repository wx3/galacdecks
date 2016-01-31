/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Kevin Lin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
/**
 * 
 */
package com.wx3.galacdecks.game;

import java.io.Serializable;

import javax.persistence.Embeddable;

/**
 * Struct for hexagonal entity coordinates.
 * 
 * @author Kevin
 */
@Embeddable
public class EntityCoordinates implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public final int x, y;
	
	// Internal cubic coordinates:
	private final int cubeX, cubeY, cubeZ;
	
	public static int distance(EntityCoordinates a, EntityCoordinates b) {
		return (Math.abs(a.cubeX - b.cubeX) + Math.abs(a.cubeY - b.cubeY) + Math.abs(a.cubeZ - b.cubeZ) ) /2;
	}
	
	public EntityCoordinates(int x, int y) {
		this.x = x;
		this.y = y;
		
		cubeX = x;
		cubeZ = y - (x - (x & 1)) / 2;
		cubeY = -cubeX-cubeZ;
	}
	
	// Constructor for cubic coordinates
	public EntityCoordinates(int cubeX, int cubeY, int cubeZ) {
		this.cubeX = cubeX;
		this.cubeY = cubeY;
		this.cubeZ = cubeZ;
		
		this.x = cubeX;
		this.y = cubeZ + (cubeX - (cubeX % 2)) / 2;
	}
	
	public EntityCoordinates add(HexDirection dir) {
		return new EntityCoordinates(cubeX + dir.x, cubeY + dir.y, cubeZ + dir.z);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof EntityCoordinates)) return false;
		EntityCoordinates coord = (EntityCoordinates) obj;
		if(coord.x == x && coord.y == y) return true;
		return false;
	}
	
	@Override
	public int hashCode() {
		int A = (x >= 0 ? 2 * x : -2 * x - 1);
	    int B = (y >= 0 ? 2 * y : -2 * y - 1);
	    int C = (int)((A >= B ? A * A + A + B : A + B * B) / 2);
	    return x < 0 && y < 0 || x >= 0 && y >= 0 ? C : -C - 1;
	}
	
	@Override
	public String toString() {
		return "EntityCoordinates " + x + "x" + y;
	}
}
