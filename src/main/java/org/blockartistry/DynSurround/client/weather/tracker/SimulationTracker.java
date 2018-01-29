/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher, Abastro
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
package org.blockartistry.DynSurround.client.weather.tracker;

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.api.events.ThunderEvent;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.weather.Weather.Properties;
import org.blockartistry.DynSurround.data.DimensionEffectData;
import org.blockartistry.lib.PlayerUtils;
import org.blockartistry.lib.TimeUtils;
import org.blockartistry.lib.math.MathStuff;
import org.blockartistry.lib.random.XorShiftRandom;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SimulationTracker extends Tracker {

	protected float intensityLevel = 0.0F;
	protected float maxIntensityLevel = 0.0F;
	protected int nextThunderEvent = 0;
	protected Properties intensity = Properties.NONE;
	protected Random random;

	@Override
	protected String type() {
		return "SIMULATION";
	}

	@Override
	@Nonnull
	public Properties getWeatherProperties() {
		return this.intensity;
	}

	@Override
	public float getIntensityLevel() {
		return this.intensityLevel;
	}

	@Override
	public float getMaxIntensityLevel() {
		return this.maxIntensityLevel;
	}

	@Override
	public int getNextThunderEvent() {
		return this.nextThunderEvent;
	}

	@Override
	public boolean doVanilla() {
		return false;
	}

	@Override
	public boolean backgroundThunderPossible() {
		return this.isThundering() && this.getIntensityLevel() >= ModOptions.stormThunderThreshold;
	}

	@Override
	public void update() {
		updateRainState();
		doAmbientThunder();
	}

	private void updateRainState() {
		final float vanillaIntensity = super.getIntensityLevel();
		if (vanillaIntensity > 0 && this.intensityLevel == 0F) {
			// Starting to rain. Generate a max intensity to
			// be used in the simulation. Use the current MC day
			// as a seed to get some predictability across
			// clients on the server.
			this.random = new XorShiftRandom(generateSeed());
			this.maxIntensityLevel = MathStuff.clamp((this.random.nextFloat() + this.random.nextFloat()) / 2F, 0.01F, 1F);
		} else if (vanillaIntensity == 0F && this.intensityLevel > 0F) {
			// Stopped raining
			this.maxIntensityLevel = 0F;
			this.random = null;
		}

		float newIntensity = MathStuff.clamp(vanillaIntensity, 0.0F, this.maxIntensityLevel);

		setCurrentIntensity(newIntensity);
	}

	/**
	 * Sets the rainIntensity based on the intensityLevel level provided. This is
	 * called by the packet handler when the server wants to set the rainIntensity
	 * level on the client.
	 */
	protected void setCurrentIntensity(float level) {

		// If the level is Vanilla it means that
		// the rainfall in the dimension is to be
		// that of Vanilla.
		if (level == Properties.VANILLA.getLevel()) {
			this.intensity = Properties.VANILLA;
			this.intensityLevel = 0.0F;
		} else {

			level = MathStuff.clamp(level, DimensionEffectData.MIN_INTENSITY, DimensionEffectData.MAX_INTENSITY);

			if (this.intensityLevel != level) {
				this.intensityLevel = level;
				if (this.intensityLevel <= Properties.NONE.getLevel())
					this.intensity = Properties.NONE;
				else if (this.intensityLevel < Properties.CALM.getLevel())
					this.intensity = Properties.CALM;
				else if (this.intensityLevel < Properties.LIGHT.getLevel())
					this.intensity = Properties.LIGHT;
				else if (this.intensityLevel < Properties.NORMAL.getLevel())
					this.intensity = Properties.NORMAL;
				else
					this.intensity = Properties.HEAVY;
			}
		}
	}

	private static long generateSeed() {
		return TimeUtils.getGMTDaySeedBase() + EnvironState.getClock().getDay();
	}

	// Leveraged from WeatherGenerator
	private void doAmbientThunder() {

		// If not enabled, return
		if (!ModOptions.allowBackgroundThunder || this.random == null)
			return;

		// If it is thundering and the intensity exceeds our threshold...
		if (this.backgroundThunderPossible()) {
			final float intensity = this.getIntensityLevel();
			int time = this.nextThunderEvent - 1;
			if (time <= 0) {
				if (time == 0) {
					final EntityPlayer player = PlayerUtils.getRandomPlayer(EnvironState.getWorld());
					if (player != null) {
						final float theY = EnvironState.getDimensionInfo().getSkyHeight();
						final BlockPos pos = new BlockPos(player.posX, theY, player.posZ);
						MinecraftForge.EVENT_BUS
								.post(new ThunderEvent(EnvironState.getWorld(), doFlash(intensity), pos));
					}
				}

				// set new time
				time = nextThunderEvent(intensity);
			}
			this.nextThunderEvent = time;

		} else {
			// Clear out the timer data for the next storm
			this.nextThunderEvent = 0;
		}
	}

	private int nextThunderEvent(final float rainIntensity) {
		final float scale = 2.0F - rainIntensity;
		return this.random.nextInt((int) (450 * scale)) + 300;
	}

	protected boolean doFlash(final float rainIntensity) {
		final int randee = (int) (rainIntensity * 100.0F);
		return this.random.nextInt(150) <= randee;
	}

}
