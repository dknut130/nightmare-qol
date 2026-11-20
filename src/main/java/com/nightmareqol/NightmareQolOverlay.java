package com.nightmareqol;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class NightmareQolOverlay extends OverlayPanel
{
	private static final Color OPEN_COLOR = new Color(0, 230, 0);
	private static final Color FIGHT_COLOR = new Color(255, 170, 0);

	private final NightmareQolPlugin plugin;
	private final NightmareQolConfig config;

	@Inject
	NightmareQolOverlay(NightmareQolPlugin plugin, NightmareQolConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showOverlay() || plugin.getLobbyNpc() == null)
		{
			return null;
		}

		LobbyState state = plugin.getState();
		if (state == LobbyState.UNKNOWN || plugin.getStateSince() == null)
		{
			return null;
		}
		Instant now = Instant.now();

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("The Nightmare")
			.build());

		switch (state)
		{
			case READY:
				addLine("Status", "Asleep", Color.WHITE);
				if (plugin.getLastFightDuration() != null)
				{
					addLine("Last fight", format(plugin.getLastFightDuration(), plugin.isLastFightExact()), Color.WHITE);
				}
				break;
			case OPEN:
				addLine("Status", "OPEN, GO IN", OPEN_COLOR);
				addLine("Open for", format(Duration.between(plugin.getStateSince(), now), true), OPEN_COLOR);
				break;
			case PHASE_1:
			case PHASE_2:
			case PHASE_3:
				addLine("Status", "Fight running", FIGHT_COLOR);
				addLine("Phase " + state.getPhase(), format(Duration.between(plugin.getStateSince(), now), true), Color.WHITE);
				if (plugin.getFightStart() != null)
				{
					addLine("Fight time", format(Duration.between(plugin.getFightStart(), now), plugin.isFightStartExact()), Color.WHITE);
					if (!plugin.isFightStartExact())
					{
						addLine("Started before you got here", "", Color.GRAY);
					}
				}
				break;
			default:
				break;
		}

		return super.render(graphics);
	}

	private void addLine(String left, String right, Color rightColor)
	{
		panelComponent.getChildren().add(LineComponent.builder()
			.left(left)
			.right(right)
			.rightColor(rightColor)
			.build());
	}

	/**
	 * Formats as m:ss. Adds a "+" when the true time is at least this long but could be longer.
	 */
	private static String format(Duration duration, boolean exact)
	{
		long seconds = Math.max(0, duration.getSeconds());
		String text = String.format("%d:%02d", seconds / 60, seconds % 60);
		return exact ? text : text + "+";
	}
}
