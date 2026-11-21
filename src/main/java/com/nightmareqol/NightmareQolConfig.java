package com.nightmareqol;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
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

	@ConfigItem(
		keyName = "highlightTotems",
		name = "Highlight totems",
		description = "Outline attackable totems, green with lots of health left fading to red as they run out",
		position = 20
	)
	default boolean highlightTotems()
	{
		return false;
	}
}
