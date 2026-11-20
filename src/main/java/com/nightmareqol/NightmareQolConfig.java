package com.nightmareqol;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Notification;

@ConfigGroup(NightmareQolConfig.GROUP)
public interface NightmareQolConfig extends Config
{
	String GROUP = "nightmareqol";

	@ConfigItem(
		keyName = "showOverlay",
		name = "Show lobby overlay",
		description = "Show The Nightmare's lobby status and fight timer while you are near her",
		position = 1
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "notifyOnOpen",
		name = "Notify when she wakes",
		description = "Notify when someone wakes The Nightmare and the fight can be joined",
		position = 2
	)
	default Notification notifyOnOpen()
	{
		return Notification.ON;
	}

	@ConfigItem(
		keyName = "notifyOnReady",
		name = "Notify when fight ends",
		description = "Notify when the current fight ends and The Nightmare is asleep again",
		position = 3
	)
	default Notification notifyOnReady()
	{
		return Notification.ON;
	}

	@ConfigSection(
		name = "Shadows",
		description = "Recolor the black Grasping Claws shadows during the fight",
		position = 10
	)
	String shadowSection = "shadows";

	@ConfigItem(
		keyName = "highlightShadows",
		name = "Recolor shadows",
		description = "Fill the Grasping Claws shadow tiles with a color that is easier to see",
		position = 11,
		section = shadowSection
	)
	default boolean highlightShadows()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		keyName = "shadowColor",
		name = "Shadow color",
		description = "Fill color for the shadow tiles (lower alpha = more see-through)",
		position = 12,
		section = shadowSection
	)
	default Color shadowColor()
	{
		return new Color(255, 0, 0, 110);
	}
}
