/*
 * Copyright (c) 2019-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.zoom;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.MathHelper;

public enum WiZoom
{
	INSTANCE;
	
	public static final MinecraftClient MC = MinecraftClient.getInstance();
	
	private KeyBinding zoomKey;
	private final double defaultLevel = 3;
	private Double currentLevel;
	double actualLevel;
	double lerpSpeed = 8f;
	private Double defaultMouseSensitivity;
	
	public void initialize()
	{
		FabricLoader fabricLoader = FabricLoader.getInstance();
		ModContainer modContainer =
			fabricLoader.getModContainer("wi_zoom").get();
		Version version = modContainer.getMetadata().getVersion();
		System.out.println("Starting WI Zoom v" + version.getFriendlyString());
		
		zoomKey = new KeyBinding("key.wi_zoom.zoom", InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_V, "WI Zoom");
		KeyBindingHelper.registerKeyBinding(zoomKey);
	}
	public double lerp(double a, double b, double f)
	{
	    return a * (1.0 - f) + (b * f);
	}
	boolean hasZoomed = false;
	long lastFrameTime = System.nanoTime();
	public double changeFovBasedOnZoom(double fov)
	{
		SimpleOption<Double> mouseSensitivitySetting =
			MC.options.getMouseSensitivity();
		if(defaultMouseSensitivity == null)
			defaultMouseSensitivity = mouseSensitivitySetting.getValue();
		
		if(currentLevel == null)
			currentLevel = defaultLevel;
		
		if(!zoomKey.isPressed())
		{
			if(mouseSensitivitySetting.getValue() != defaultMouseSensitivity)
				mouseSensitivitySetting
					.setValue(defaultMouseSensitivity);
			else
				defaultMouseSensitivity = null;

			currentLevel = 1.0;
			hasZoomed = false;
		}
		else if(!hasZoomed) {
			currentLevel = defaultLevel;
			hasZoomed = true;
		}
		else {
			// Adjust mouse sensitivity in relation to zoom level.
			mouseSensitivitySetting
				.setValue(defaultMouseSensitivity * (1.0 / actualLevel));
		}
		
		
		long thisFrameTime = System.nanoTime();
	    double deltaTime = (thisFrameTime - lastFrameTime) / 1_000_000_000.0;  // Convert to seconds
	    lastFrameTime = thisFrameTime;
		double realLerpSpeed = lerpSpeed;
	    if(actualLevel > currentLevel)
			realLerpSpeed *= 2;
		double blend = Math.pow(.5, deltaTime * realLerpSpeed);
		actualLevel = lerp(currentLevel,actualLevel, blend);
		
		return fov / actualLevel;
	}
	
	public void onMouseScroll(double amount)
	{
		if(!zoomKey.isPressed())
			return;
		
		if(currentLevel == null)
			currentLevel = defaultLevel;
		
		if(amount > 0)
			currentLevel *= 1.1;
		else if(amount < 0)
			currentLevel *= 0.9;
		
		currentLevel = MathHelper.clamp(currentLevel, 1, 50);
	}
	
	public KeyBinding getZoomKey()
	{
		return zoomKey;
	}
}
