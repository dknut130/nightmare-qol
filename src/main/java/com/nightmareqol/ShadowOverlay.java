package com.nightmareqol;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

/**
 * Fills the tiles of The Nightmare's Grasping Claws shadows with a user-chosen color.
 */
class ShadowOverlay extends Overlay
{
	private static final Stroke BORDER = new BasicStroke(1);

	private final Client client;
	private final NightmareQolPlugin plugin;
	private final NightmareQolConfig config;

	@Inject
	ShadowOverlay(Client client, NightmareQolPlugin plugin, NightmareQolConfig config)
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
		if (!config.highlightShadows() || plugin.getShadows().isEmpty())
		{
			return null;
		}

		Color fill = config.shadowColor();
		Color border = new Color(fill.getRed(), fill.getGreen(), fill.getBlue());

		for (GraphicsObject shadow : plugin.getShadows())
		{
			if (shadow.finished() || shadow.getLevel() != client.getTopLevelWorldView().getPlane())
			{
				continue;
			}

			LocalPoint location = shadow.getLocation();
			Polygon poly = Perspective.getCanvasTilePoly(client, location);
			if (poly != null)
			{
				OverlayUtil.renderPolygon(graphics, poly, border, fill, BORDER);
			}
		}

		return null;
	}
}
