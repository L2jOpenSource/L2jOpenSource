/*
 * Copyright (C) 2004-2013 L2J DataPack
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.NpcSay;

import ai.npc.AbstractNpcAI;

/**
 * Prison Guards AI.
 * @author Gigiikun
 */
public class PrisonGuards extends AbstractNpcAI
{
	// NPCs
	private static final int GUARD_HEAD = 18367; // Prison Guard
	private static final int GUARD = 18368; // Prison Guard
	// Item
	private static final int STAMP = 10013; // Race Stamp
	// Skills
	private static final SkillHolder STONE = new SkillHolder(4578, 1); // Petrification
	private static final SkillHolder SILENCE = new SkillHolder(4098, 9); // Silence
	private final static int SKILL_SILENCE = 4098;
	private final static int SKILL_PERTIFICATION = 4578;
	private final static int SKILL_EVENT_TIMER = 5239;
	
	private final Map<L2Npc, Integer> _guards = new ConcurrentHashMap<>();
	
	public PrisonGuards()
	{
		super(PrisonGuards.class.getSimpleName(), "ai/group_template");
		addAttackId(GUARD_HEAD, GUARD);
		addSpawnId(GUARD_HEAD, GUARD);
		addNpcHateId(GUARD);
		addSkillSeeId(GUARD);
		addSpellFinishedId(GUARD_HEAD, GUARD);
		addAggroRangeEnterId(GUARD);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("Respawn"))
		{
			L2Npc newGuard = addSpawn(npc.getId(), npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(), npc.getSpawn().getHeading(), false, 0);
			newGuard.setIsNoRndWalk(true);
			newGuard.setIsImmobilized(true);
			if (npc.getId() == GUARD_HEAD)
			{
				newGuard.setIsInvul(true);
				newGuard.disableCoreAI(true);
			}
			
			int place = _guards.get(npc);
			_guards.remove(npc);
			_guards.put(newGuard, place);
		}
		else if (event.equals("attackEnd") && (npc.getId() == GUARD))
		{
			if ((npc.getX() != npc.getSpawn().getX()) || (npc.getY() != npc.getSpawn().getY()))
			{
				npc.teleToLocation(npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(), npc.getSpawn().getHeading(), false);
				npc.setIsImmobilized(true);
			}
			((L2Attackable) npc).getAggroList().clear();
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		else if (event.equals("CLEAR_STATUS"))
		{
			npc.setScriptValue(0);
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance player, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		L2Character caster = isSummon ? player.getSummon() : player;
		
		if ((caster.getFirstEffect(SKILL_EVENT_TIMER) == null))
		{
			if (caster.getFirstEffect(SKILL_SILENCE) == null)
			{
				npc.setTarget(caster);
				npc.doCast(SILENCE.getSkill());
			}
		}
		
		return super.onSkillSee(npc, player, skill, targets, isSummon);
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if ((skill == SILENCE.getSkill()) || (skill == STONE.getSkill()))
		{
			((L2Attackable) npc).clearAggroList();
			npc.setTarget(npc);
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		L2Character target = isSummon ? player.getSummon() : player;
		
		if (npc.getId() == GUARD)
		{
			if (target.getFirstEffect(SKILL_EVENT_TIMER) != null)
			{
				cancelQuestTimer("attackEnd", null, null);
				startQuestTimer("attackEnd", 180000, npc, null);
				
				npc.setIsImmobilized(false);
				npc.setTarget(target);
				npc.setRunning();
				((L2Attackable) npc).addDamageHate(target, 0, 999);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			else
			{
				if ((npc.getX() != npc.getSpawn().getX()) || (npc.getY() != npc.getSpawn().getY()))
				{
					npc.teleToLocation(npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(), npc.getSpawn().getHeading(), false);
					npc.setIsImmobilized(true);
				}
				((L2Attackable) npc).getAggroList().remove(target);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				return null;
			}
		}
		
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon)
	{
		L2Character attacker = isSummon ? player.getSummon() : player;
		
		if (attacker.getFirstEffect(SKILL_EVENT_TIMER) == null)
		{
			if (attacker.getFirstEffect(SKILL_PERTIFICATION) == null)
			{
				castDebuff(npc, attacker, SKILL_PERTIFICATION, isSummon, true, false);
			}
			
			npc.setTarget(null);
			((L2Attackable) npc).getAggroList().remove(attacker);
			((L2Attackable) npc).stopHating(attacker);
			((L2Attackable) npc).abortAttack();
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			return null;
		}
		
		if (npc.getId() == GUARD)
		{
			cancelQuestTimer("attackEnd", null, null);
			startQuestTimer("attackEnd", 180000, npc, null);
			
			npc.setIsImmobilized(false);
			npc.setTarget(attacker);
			npc.setRunning();
			((L2Attackable) npc).addDamageHate(attacker, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
		else if ((npc.getId() == GUARD_HEAD) && (getRandom(100) < 10) && (npc.calculateDistance(player, true, false) < 100))
		{
			if ((getRandom(100) < 10) && (npc.calculateDistance(player, true, false) < 100))
			{
				if ((getQuestItemsCount(player, STAMP) <= 3) && npc.isScriptValue(0))
				{
					giveItems(player, STAMP, 1);
					npc.setScriptValue(1);
					startQuestTimer("CLEAR_STATUS", 600000, npc, null);
				}
			}
		}
		
		return super.onAttack(npc, player, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if (_guards.containsKey(npc))
		{
			startQuestTimer("Respawn", 20000, npc, null);
		}
		
		return super.onKill(npc, player, isSummon);
	}
	
	private void castDebuff(L2Npc npc, L2Character player, int effectId, boolean isSummon, boolean fromAttack, boolean isSpell)
	{
		if (fromAttack)
		{
			NpcStringId npcString = (npc.getId() == GUARD_HEAD ? NpcStringId.ITS_NOT_EASY_TO_OBTAIN : NpcStringId.YOURE_OUT_OF_YOUR_MIND_COMING_HERE);
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), npcString));
		}
		
		L2Skill skill = SkillData.getInstance().getInfo(effectId, isSpell ? 9 : 1);
		if (skill != null)
		{
			npc.setTarget(isSummon ? player.getSummon() : player);
			npc.doCast(skill);
		}
	}
}
