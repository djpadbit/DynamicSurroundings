/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
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
 */

package org.orecruncher.dsurround.lib.scanner;

import javax.annotation.Nullable;

import org.orecruncher.lib.chunk.IBlockAccessEx;

import net.minecraft.util.math.BlockPos;

public abstract class ScanLocus {

	public ScanLocus() {

	}

	public abstract IBlockAccessEx getWorld();

	public abstract BlockPos getCenter();

	public int getReference() {
		return getWorld().worldReference();
	}

	@Override
	public boolean equals(@Nullable final Object o) {
		if (this == o)
			return true;
		final ScanLocus sl = (ScanLocus) o;
		return getWorld().getWorld() == sl.getWorld().getWorld() && getCenter().equals(sl.getCenter());
	}

}
