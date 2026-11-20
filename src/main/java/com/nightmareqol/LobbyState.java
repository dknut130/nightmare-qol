package com.nightmareqol;

import net.runelite.api.gameval.NpcID;

/**
 * State of The Nightmare as seen from the lobby (outside the fight instance).
 */
enum LobbyState
{
	UNKNOWN(0),
	/** Asleep. Anyone can disturb her to start a new fight. */
	READY(0),
	/** Woken up, countdown running. Players can still join. */
	OPEN(0),
	/** Fight in progress, phase 1. Not joinable. */
	PHASE_1(1),
	/** Fight in progress, phase 2. Not joinable. */
	PHASE_2(2),
	/** Fight in progress, phase 3. Not joinable. */
	PHASE_3(3);

	private final int phase;

	LobbyState(int phase)
	{
		this.phase = phase;
	}

	int getPhase()
	{
		return phase;
	}

	boolean isFight()
	{
		return phase > 0;
	}

	static LobbyState fromNpcId(int id)
	{
		switch (id)
		{
			case NpcID.NIGHTMARE_ENTRY_READY:
				return READY;
			case NpcID.NIGHTMARE_ENTRY_OPEN:
				return OPEN;
			case NpcID.NIGHTMARE_ENTRY_CLOSED_01:
				return PHASE_1;
			case NpcID.NIGHTMARE_ENTRY_CLOSED_02:
				return PHASE_2;
			case NpcID.NIGHTMARE_ENTRY_CLOSED_03:
				return PHASE_3;
			default:
				return UNKNOWN;
		}
	}
}
