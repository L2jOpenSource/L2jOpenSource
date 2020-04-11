package com.l2jfrozen.gameserver.ai.special;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.quest.Quest;

public class ZombieGatekeepers extends Quest implements Runnable
{
	public ZombieGatekeepers(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		super.addAttackId(22136);
		super.addAggroRangeEnterId(22136);
	}
	
	private final Map<Integer, List<L2Character>> attackersList = new HashMap<>();
	
	@Override
	public String onAttack(final L2NpcInstance npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		final int npcObjId = npc.getObjectId();
		
		final L2Character target = isPet ? attacker.getPet() : attacker;
		
		if (attackersList.get(npcObjId) == null)
		{
			List<L2Character> player = new ArrayList<>();
			player.add(target);
			attackersList.put(npcObjId, player);
		}
		else if (!attackersList.get(npcObjId).contains(target))
		{
			attackersList.get(npcObjId).add(target);
		}
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onAggroRangeEnter(final L2NpcInstance npc, final L2PcInstance player, final boolean isPet)
	{
		final int npcObjId = npc.getObjectId();
		
		final L2Character target = isPet ? player.getPet() : player;
		
		final L2ItemInstance VisitorsMark = player.getInventory().getItemByItemId(8064);
		final L2ItemInstance FadedVisitorsMark = player.getInventory().getItemByItemId(8065);
		final L2ItemInstance PagansMark = player.getInventory().getItemByItemId(8067);
		
		final long mark1 = VisitorsMark == null ? 0 : VisitorsMark.getCount();
		final long mark2 = FadedVisitorsMark == null ? 0 : FadedVisitorsMark.getCount();
		final long mark3 = PagansMark == null ? 0 : PagansMark.getCount();
		
		if (mark1 == 0 && mark2 == 0 && mark3 == 0)
		{
			((L2Attackable) npc).addDamageHate(target, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
		else
		{
			if (attackersList.get(npcObjId) == null || !attackersList.get(npcObjId).contains(target))
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
	public String onKill(final L2NpcInstance npc, final L2PcInstance killer, final boolean isPet)
	{
		final int npcObjId = npc.getObjectId();
		if (attackersList.get(npcObjId) != null)
		{
			attackersList.get(npcObjId).clear();
		}
		
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public void run()
	{
	}
}