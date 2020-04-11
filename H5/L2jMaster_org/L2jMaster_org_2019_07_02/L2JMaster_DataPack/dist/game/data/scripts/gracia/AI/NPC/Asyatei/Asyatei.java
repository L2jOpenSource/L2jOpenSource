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
package gracia.AI.NPC.Asyatei;

import ai.npc.AbstractNpcAI;

import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author MaGa
 */
public final class Asyatei extends AbstractNpcAI
{
	public static final int ASYATEI = 32546;
	
	public Asyatei()
	{
		super(Asyatei.class.getSimpleName(), "ai/group_template");
		addSpawnId(ASYATEI);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("move"))
		{
			final int locX = (npc.getLocation().getX() - 50) + getRandom(200);
			final int locY = (npc.getLocation().getY() - 50) + getRandom(200);
			npc.setWalking();
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(locX, locY, npc.getZ(), 0));
			startQuestTimer("move", getRandom(13000, 180000), npc, null);
		}
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		switch (npc.getId())
		{
			case ASYATEI:
				startQuestTimer("move", getRandom(13000, 180000), npc, null);
				break;
		}
		npc.setIsNoRndWalk(true);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Asyatei();
	}
}