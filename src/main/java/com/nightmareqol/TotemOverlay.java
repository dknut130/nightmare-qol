package com.nightmareqol;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.NPC;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Highlights attackable totems: green while they have lots of health left, fading to red
 * as their health bar runs out.
 */
class TotemOverlay extends Overlay
{
	private static final BasicStroke STROKE = new BasicStroke(2);

	private final NightmareQolPlugin plugin;
	private final NightmareQolConfig config;

	/** Last known health fraction per totem, since the health bar is only sent while it is being hit. */
	private final Map<NPC, Double> lastHealth = new HashMap<>();

	@Inject
	TotemOverlay(NightmareQolPlugin plugin, NightmareQolConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.highlightTotems() || plugin.getTotems().isEmpty())
		{
			lastHealth.clear();
			return null;
		}

		lastHealth.keySet().retainAll(plugin.getTotems());

		for (NPC totem : plugin.getTotems())
		{
			if (!isReady(totem.getId()))
			{
				continue;
			}

			double health = lastHealth.getOrDefault(totem, 1.0);
			int ratio = totem.getHealthRatio();
			int scale = totem.getHealthScale();
			if (ratio >= 0 && scale > 0)
			{
				health = (double) ratio / scale;
				lastHealth.put(totem, health);
			}

			Shape hull = totem.getConvexHull();
			if (hull == null)
			{
				continue;
			}

			Color color = blend(health);
			graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
			graphics.fill(hull);
			graphics.setColor(color);
			graphics.setStroke(STROKE);
			graphics.draw(hull);
		}

		return null;
	}

	private static boolean isReady(int id)
	{
		return id == NpcID.NIGHTMARE_TOTEM_1_READY
			|| id == NpcID.NIGHTMARE_TOTEM_2_READY
			|| id == NpcID.NIGHTMARE_TOTEM_3_READY
			|| id == NpcID.NIGHTMARE_TOTEM_4_READY;
	}

	/** 1.0 health = green, 0.5 = yellow, 0.0 = red. */
	private static Color blend(double health)
	{
		double h = Math.max(0, Math.min(1, health));
		int red = (int) Math.round(255 * Math.min(1, 2 * (1 - h)));
		int green = (int) Math.round(255 * Math.min(1, 2 * h));
		return new Color(red, green, 0);
	}
}
