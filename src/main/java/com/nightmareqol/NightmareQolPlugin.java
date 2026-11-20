package com.nightmareqol;

import com.google.inject.Provides;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Nightmare QOL",
	description = "Lobby alerts and a fight timer for The Nightmare",
	tags = {"nightmare", "ashihama", "lobby", "timer", "boss", "notification"}
)
public class NightmareQolPlugin extends Plugin
{
	/**
	 * If the lobby NPC despawns and respawns within this window (scene reload),
	 * keep treating state changes as live rather than as a fresh sighting.
	 */
	private static final Duration RESPAWN_GRACE = Duration.ofSeconds(5);

	@Inject
	private Notifier notifier;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NightmareQolOverlay overlay;

	@Inject
	private NightmareQolConfig config;

	@Getter
	private NPC lobbyNpc;

	@Getter
	private LobbyState state = LobbyState.UNKNOWN;

	/** When the current state started (or when we first saw it). */
	@Getter
	private Instant stateSince;

	/** When the current fight started, or the earliest we know it was running. */
	@Getter
	private Instant fightStart;

	/** True if we watched the fight start, so fightStart is exact. */
	@Getter
	private boolean fightStartExact;

	/** Length of the last fight we saw end. */
	@Getter
	private Duration lastFightDuration;

	/** True if lastFightDuration is exact (we saw both the start and the end). */
	@Getter
	private boolean lastFightExact;

	private Instant despawnedAt;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		reset();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		LobbyState spawned = LobbyState.fromNpcId(npc.getId());
		if (spawned == LobbyState.UNKNOWN)
		{
			return;
		}

		lobbyNpc = npc;
		boolean live = state != LobbyState.UNKNOWN
			&& despawnedAt != null
			&& Duration.between(despawnedAt, Instant.now()).compareTo(RESPAWN_GRACE) <= 0;
		despawnedAt = null;

		if (!live)
		{
			// Fresh sighting: we did not watch what happened before this.
			reset();
			lobbyNpc = npc;
		}
		setState(spawned, live);
	}

	@Subscribe
	public void onNpcChanged(NpcChanged event)
	{
		NPC npc = event.getNpc();
		LobbyState changed = LobbyState.fromNpcId(npc.getId());
		if (changed == LobbyState.UNKNOWN)
		{
			return;
		}

		lobbyNpc = npc;
		setState(changed, true);
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		if (event.getNpc() == lobbyNpc)
		{
			lobbyNpc = null;
			despawnedAt = Instant.now();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState gameState = event.getGameState();
		if (gameState == GameState.HOPPING || gameState == GameState.LOGIN_SCREEN)
		{
			reset();
		}
	}

	/**
	 * @param next the new lobby state
	 * @param live true if we watched this change happen, false if we just arrived and found her this way
	 */
	private void setState(LobbyState next, boolean live)
	{
		if (next == state)
		{
			return;
		}

		LobbyState prev = state;
		Instant now = Instant.now();
		state = next;
		stateSince = now;
		log.debug("Nightmare lobby {} -> {} (live={})", prev, next, live);

		if (next == LobbyState.OPEN)
		{
			fightStart = null;
			fightStartExact = false;
			if (live && prev == LobbyState.READY)
			{
				notifier.notify(config.notifyOnOpen(), "The Nightmare is awake. Get in now!");
			}
		}
		else if (next.isFight())
		{
			if (!prev.isFight())
			{
				// Fight just started (exact) or we just walked up mid-fight (lower bound only).
				fightStart = now;
				fightStartExact = live && prev == LobbyState.OPEN;
			}
		}
		else if (next == LobbyState.READY)
		{
			if (live && prev.isFight() && fightStart != null)
			{
				lastFightDuration = Duration.between(fightStart, now);
				lastFightExact = fightStartExact;
				notifier.notify(config.notifyOnReady(), "The Nightmare fight is over. She's asleep again.");
			}
			fightStart = null;
			fightStartExact = false;
		}
	}

	private void reset()
	{
		lobbyNpc = null;
		state = LobbyState.UNKNOWN;
		stateSince = null;
		fightStart = null;
		fightStartExact = false;
		lastFightDuration = null;
		lastFightExact = false;
		despawnedAt = null;
	}

	@Provides
	NightmareQolConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NightmareQolConfig.class);
	}
}
