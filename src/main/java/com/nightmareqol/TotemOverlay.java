package com.nightmareqol;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Draws a square over each totem that can be hit, colored by the health shown on the game's own
 * totem health bars: green with lots of health left, fading through yellow to red as it runs out.
 * The plugin hides the game's corner totem display while this is on, so the health is moved onto
 * the totems rather than shown twice.
 */
class TotemOverlay extends Overlay
{
	private static final BasicStroke STROKE = new BasicStroke(2);
	private static final int FILL_ALPHA = 70;

	private final Client client;
	private final NightmareQolPlugin plugin;
	private final NightmareQolConfig config;

	@Inject
	TotemOverlay(Client client, NightmareQolPlugin plugin, NightmareQolConfig config)
	{
		super(plugin);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Set<NPC> totems = plugin.getTotems();
		if (!config.highlightTotems() || totems.isEmpty())
		{
			return null;
		}

		// Center of all totems, used to work out which corner (NW/NE/SW/SE) each one is in.
		double cx = 0;
		double cy = 0;
		for (NPC totem : totems)
		{
			WorldPoint wp = totem.getWorldLocation();
			cx += wp.getX();
			cy += wp.getY();
		}
		cx /= totems.size();
		cy /= totems.size();

		for (NPC totem : totems)
		{
			if (!isReady(totem.getId()))
			{
				continue;
			}

			WorldPoint wp = totem.getWorldLocation();
			double health = healthFor(wp.getY() >= cy, wp.getX() <= cx);
			LocalPoint lp = totem.getLocalLocation();
			if (health < 0 || lp == null)
			{
				continue;
			}

			NPCComposition comp = totem.getTransformedComposition();
			int size = comp != null ? comp.getSize() : 1;
			Polygon poly = Perspective.getCanvasTileAreaPoly(client, lp, size);
			if (poly == null)
			{
				continue;
			}

			Color color = blend(health);
			graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), FILL_ALPHA));
			graphics.fill(poly);
			graphics.setColor(color);
			graphics.setStroke(STROKE);
			graphics.draw(poly);
		}

		return null;
	}

	/**
	 * Reads the game's own health bar for one corner. Returns 0..1, or -1 if it can't be read.
	 */
	private double healthFor(boolean north, boolean west)
	{
		int remainingId;
		int backId;
		if (north && west)
		{
			remainingId = InterfaceID.NightmareTotems.TOTEM_NW_BAR_REMAINING;
			backId = InterfaceID.NightmareTotems.TOTEM_NW_BAR_BACK;
		}
		else if (north)
		{
			remainingId = InterfaceID.NightmareTotems.TOTEM_NE_BAR_REMAINING;
			backId = InterfaceID.NightmareTotems.TOTEM_NE_BAR_BACK;
		}
		else if (west)
		{
			remainingId = InterfaceID.NightmareTotems.TOTEM_SW_BAR_REMAINING;
			backId = InterfaceID.NightmareTotems.TOTEM_SW_BAR_BACK;
		}
		else
		{
			remainingId = InterfaceID.NightmareTotems.TOTEM_SE_BAR_REMAINING;
			backId = InterfaceID.NightmareTotems.TOTEM_SE_BAR_BACK;
		}

		Widget remaining = client.getWidget(remainingId);
		Widget back = client.getWidget(backId);
		if (remaining == null || back == null)
		{
			return -1;
		}

		int full = width(back);
		if (full <= 0)
		{
			return -1;
		}
		return Math.max(0, Math.min(1, (double) width(remaining) / full));
	}

	private static int width(Widget w)
	{
		int width = w.getWidth();
		return width > 0 ? width : w.getOriginalWidth();
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
		int red = (int) Math.round(255 * Math.min(1, 2 * (1 - health)));
		int green = (int) Math.round(255 * Math.min(1, 2 * health));
		return new Color(red, green, 0);
	}
}
