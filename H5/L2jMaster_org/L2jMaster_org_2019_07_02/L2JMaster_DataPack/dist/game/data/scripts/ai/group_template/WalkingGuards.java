/*
 * Copyright (C) 2004-2019 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.group_template;

import ai.npc.AbstractNpcAI;

import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Attackable;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.zone.ZoneId;

/**
 * @author MaGa
 */
public final class WalkingGuards extends AbstractNpcAI
{
	public static final int MOVEGUARD_HUMAN = 31032; // Guard
	public static final int MOVEGUARD_ELF = 31033; // Sentinel
	public static final int MOVEGUARD_DARKELF = 31034; // Sentry
	public static final int MOVEGUARD_DWARF = 31035; // Defender
	public static final int MOVEGUARD_ORC = 31036; // Centurion
	public static final int MOVEGUARD_KAMAEL = 32335; // Marksman
	
	private WalkingGuards()
	{
		super(WalkingGuards.class.getSimpleName(), "ai/group_template");
		addSpawnId(MOVEGUARD_HUMAN, MOVEGUARD_ELF, MOVEGUARD_DARKELF, MOVEGUARD_DWARF, MOVEGUARD_ORC, MOVEGUARD_KAMAEL);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (npc.isInsideZone(ZoneId.WATER))
		{
			((L2Attackable) npc).returnHome();
			startQuestTimer("move", getRandom(1000), npc, null);
		}
		
		if (event.equalsIgnoreCase("move"))
		{
			final int locX = (npc.getSpawn().getX() - 50) + getRandom(500);
			final int locY = (npc.getSpawn().getY() - 50) + getRandom(300);
			npc.setWalking();
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(locX, locY, npc.getZ(), 0));
			startQuestTimer("move", getRandom(3000, 42000), npc, null);
		}
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		switch (npc.getId())
		{
			case MOVEGUARD_HUMAN:
			case MOVEGUARD_ELF:
			case MOVEGUARD_DARKELF:
			case MOVEGUARD_DWARF:
			case MOVEGUARD_ORC:
			case MOVEGUARD_KAMAEL:
				((L2Attackable) npc).setCanReturnToSpawnPoint(false);
				startQuestTimer("move", getRandom(3000, 42000), npc, null);
				break;
		}
		npc.setIsNoRndWalk(true);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new WalkingGuards();
	}
}