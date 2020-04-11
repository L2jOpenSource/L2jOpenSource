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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.instancemanager.ZoneManager;
import com.l2jserver.gameserver.model.actor.L2Attackable;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.zone.type.L2EffectZone;

import ai.npc.AbstractNpcAI;

public class PaganTemple extends AbstractNpcAI
{
	// Npcs
	private static final int DOORMAN_ZOMBIE_01 = 18343;
	private static final int DOORMAN_ZOMBIE_02 = 22136;
	private static final int CHAPEL_GUARD = 22138;
	private static final int PENANCE_GUARD_01 = 22137;
	private static final int PENANCE_GUARD_02 = 22194;
	private static final int TRIOL_REVELATION_01 = 32058;
	private static final int TRIOL_REVELATION_02 = 32059;
	private static final int TRIOL_REVELATION_03 = 32060;
	private static final int TRIOL_REVELATION_04 = 32061;
	private static final int TRIOL_REVELATION_05 = 32062;
	private static final int TRIOL_REVELATION_06 = 32063;
	private static final int TRIOL_REVELATION_07 = 32064;
	private static final int TRIOL_REVELATION_08 = 32065;
	private static final int TRIOL_REVELATION_09 = 32066;
	private static final int TRIOL_REVELATION_10 = 32067;
	private static final int TRIOL_REVELATION_11 = 32068;
	// Items
	private static final int VISITORS_MARK = 8064;
	private static final int FADED_VISITORS_MARK = 8065;
	private static final int PAGANS_MARK = 8067;
	
	private final Map<Integer, List<L2Character>> _attackersList = new ConcurrentHashMap<>();
	
	public PaganTemple()
	{
		super(PaganTemple.class.getSimpleName(), "ai/group_template");
		addAttackId(DOORMAN_ZOMBIE_01);
		addAggroRangeEnterId(DOORMAN_ZOMBIE_01);
		addSpawnId(DOORMAN_ZOMBIE_01, DOORMAN_ZOMBIE_02, CHAPEL_GUARD, PENANCE_GUARD_01, PENANCE_GUARD_02, TRIOL_REVELATION_01, TRIOL_REVELATION_02, TRIOL_REVELATION_03, TRIOL_REVELATION_04, TRIOL_REVELATION_05, TRIOL_REVELATION_06, TRIOL_REVELATION_07, TRIOL_REVELATION_08, TRIOL_REVELATION_09, TRIOL_REVELATION_10, TRIOL_REVELATION_11);
		addKillId(TRIOL_REVELATION_01, TRIOL_REVELATION_02, TRIOL_REVELATION_03, TRIOL_REVELATION_04, TRIOL_REVELATION_05, TRIOL_REVELATION_06, TRIOL_REVELATION_07, TRIOL_REVELATION_08, TRIOL_REVELATION_09, TRIOL_REVELATION_10, TRIOL_REVELATION_11);
	}
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, Skill skill)
	{
		int npcObjId = npc.getObjectId();
		L2Character target = isPet ? attacker.getSummon() : attacker;
		if (_attackersList.get(npcObjId) == null)
		{
			List<L2Character> player = new ArrayList<>();
			player.add(target);
			_attackersList.put(npcObjId, player);
		}
		else if (!_attackersList.get(npcObjId).contains(target))
		{
			_attackersList.get(npcObjId).add(target);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcObjId = npc.getObjectId();
		L2Character target = isPet ? player.getSummon() : player;
		L2ItemInstance VisitorsMark = player.getInventory().getItemByItemId(VISITORS_MARK);
		L2ItemInstance FadedVisitorsMark = player.getInventory().getItemByItemId(FADED_VISITORS_MARK);
		L2ItemInstance PagansMark = player.getInventory().getItemByItemId(PAGANS_MARK);
		long mark1 = VisitorsMark == null ? 0 : VisitorsMark.getCount();
		long mark2 = FadedVisitorsMark == null ? 0 : FadedVisitorsMark.getCount();
		long mark3 = PagansMark == null ? 0 : PagansMark.getCount();
		if ((mark1 == 0) && (mark2 == 0) && (mark3 == 0))
		{
			((L2Attackable) npc).addDamageHate(target, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
		else
		{
			if ((_attackersList.get(npcObjId) == null) || !_attackersList.get(npcObjId).contains(target))
			{
				((L2Attackable) npc).getAggroList().remove(target);
			}
			else
			{
				((L2Attackable) npc).addDamageHate(target, 0, 999);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		switch (npc.getId())
		{
			case DOORMAN_ZOMBIE_01:
			case DOORMAN_ZOMBIE_02:
			case CHAPEL_GUARD:
			case PENANCE_GUARD_01:
			case PENANCE_GUARD_02:
				npc.setIsImmobilized(true);
			case TRIOL_REVELATION_01:
			case TRIOL_REVELATION_02:
			case TRIOL_REVELATION_03:
			case TRIOL_REVELATION_04:
			case TRIOL_REVELATION_05:
			case TRIOL_REVELATION_06:
			case TRIOL_REVELATION_07:
			case TRIOL_REVELATION_08:
			case TRIOL_REVELATION_09:
			case TRIOL_REVELATION_10:
			case TRIOL_REVELATION_11:
				L2EffectZone zone = ZoneManager.getInstance().getZone(npc, L2EffectZone.class);
				if (zone != null)
				{
					zone.addSkill(4149, 9);
					zone.setEnabled(true);
				}
				npc.setIsParalyzed(true);
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcObjId = npc.getObjectId();
		if (_attackersList.get(npcObjId) != null)
		{
			_attackersList.get(npcObjId).clear();
		}
		switch (npc.getId())
		{
			case TRIOL_REVELATION_01:
			case TRIOL_REVELATION_02:
			case TRIOL_REVELATION_03:
			case TRIOL_REVELATION_04:
			case TRIOL_REVELATION_05:
			case TRIOL_REVELATION_06:
			case TRIOL_REVELATION_07:
			case TRIOL_REVELATION_08:
			case TRIOL_REVELATION_09:
			case TRIOL_REVELATION_10:
			case TRIOL_REVELATION_11:
				L2EffectZone zone = ZoneManager.getInstance().getZone(npc, L2EffectZone.class);
				zone.clearSkills();
				zone.setEnabled(false);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new PaganTemple();
	}
}