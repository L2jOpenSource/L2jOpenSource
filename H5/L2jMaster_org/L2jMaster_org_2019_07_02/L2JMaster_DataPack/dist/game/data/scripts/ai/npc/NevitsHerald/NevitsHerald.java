/*
 * Copyright (C) 2004-2019 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J Datapack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Datapack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.npc.NevitsHerald;

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jserver.gameserver.network.serverpackets.NpcSay;
import com.l2jserver.util.Rnd;

import ai.npc.AbstractNpcAI;

/**
 * Nevit's Herald AI.
 * @author Sacrifice
 */
public final class NevitsHerald extends AbstractNpcAI
{
	private static final int NEVITS_HERALD = 4326;
	private static final List<L2Npc> SPAWNS = new ArrayList<>();
	private static final Location[] NEVITS_HERALD_LOC =
	{
		new Location(86971, -142772, -1336, 20480), // Town of Schuttgart
		new Location(44165, -48494, -792, 32768), // Rune Township
		new Location(148017, -55264, -2728, 49152), // Town of Goddard
		new Location(147919, 26631, -2200, 16384), // Town of Aden
		new Location(82325, 53278, -1488, 16384), // Town of Oren
		new Location(81925, 148302, -3464, 49152), // Town of Giran
		new Location(111678, 219197, -3536, 49152), // Heine
		new Location(16254, 142808, -2696, 16384), // Town of Dion
		new Location(-13865, 122081, -2984, 32768), // Town of Gludio
		new Location(-83248, 150832, -3136, 32768), // Gludin Village
		new Location(116899, 77256, -2688, 49152) // Hunters Village
	};
	private static final int ANTHARAS = 29068; // Antharas Strong (85)
	private static final int VALAKAS = 29028; // Valakas (85)
	private static final NpcStringId[] SPAM =
	{
		NpcStringId.SHOW_RESPECT_TO_THE_HEROES_WHO_DEFEATED_THE_EVIL_DRAGON_AND_PROTECTED_THIS_ADEN_WORLD,
		NpcStringId.SHOUT_TO_CELEBRATE_THE_VICTORY_OF_THE_HEROES,
		NpcStringId.PRAISE_THE_ACHIEVEMENT_OF_THE_HEROES_AND_RECEIVE_NEVITS_BLESSING
	};
	private static boolean isActive = false;
	// Skill
	private static final SkillHolder FALL_OF_THE_DRAGON = new SkillHolder(23312);
	
	private NevitsHerald()
	{
		super(NevitsHerald.class.getSimpleName(), "ai/npc");
		addFirstTalkId(NEVITS_HERALD);
		addStartNpc(NEVITS_HERALD);
		addTalkId(NEVITS_HERALD);
		addKillId(ANTHARAS, VALAKAS);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "4326.htm";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		if (npc.getId() == NEVITS_HERALD)
		{
			if (event.equalsIgnoreCase("buff"))
			{
				if (player.getEffectList().getFirstEffect(L2EffectType.NEVIT_HOURGLASS) != null)
				{
					return "4326-1.htm";
				}
				npc.setTarget(player);
				npc.doCast(FALL_OF_THE_DRAGON);
				return null;
			}
		}
		else if (event.equalsIgnoreCase("text_spam"))
		{
			cancelQuestTimer("text_spam", npc, player);
			npc.broadcastPacket(new NpcSay(NEVITS_HERALD, Say2.SHOUT, NEVITS_HERALD, SPAM[Rnd.get(0, SPAM.length - 1)]));
			startQuestTimer("text_spam", 60000, npc, player);
			return null;
		}
		else if (event.equalsIgnoreCase("despawn"))
		{
			despawnHeralds();
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		ExShowScreenMessage message = null;
		
		if (npc.getId() == VALAKAS)
		{
			message = new ExShowScreenMessage(NpcStringId.THE_EVIL_FIRE_DRAGON_VALAKAS_HAS_BEEN_DEFEATED, 2, 10000);
		}
		else
		{
			message = new ExShowScreenMessage(NpcStringId.THE_EVIL_LAND_DRAGON_ANTHARAS_HAS_BEEN_DEFEATED, 2, 10000);
		}
		
		for (L2PcInstance onlinePlayer : L2World.getInstance().getPlayers())
		{
			if (onlinePlayer == null)
			{
				continue;
			}
			onlinePlayer.sendPacket(message);
		}
		
		if (!isActive)
		{
			isActive = true;
			
			SPAWNS.clear();
			
			for (Location loc : NEVITS_HERALD_LOC)
			{
				L2Npc herald = addSpawn(NEVITS_HERALD, loc, false, 0);
				if (herald != null)
				{
					SPAWNS.add(herald);
				}
			}
			startQuestTimer("despawn", 14400000, npc, killer);
			startQuestTimer("text_spam", 3000, npc, killer);
		}
		return null;
	}
	
	private void despawnHeralds()
	{
		if (!SPAWNS.isEmpty())
		{
			for (L2Npc npc : SPAWNS)
			{
				npc.deleteMe();
			}
		}
		SPAWNS.clear();
	}
	
	public static void main(String[] args)
	{
		new NevitsHerald();
	}
}