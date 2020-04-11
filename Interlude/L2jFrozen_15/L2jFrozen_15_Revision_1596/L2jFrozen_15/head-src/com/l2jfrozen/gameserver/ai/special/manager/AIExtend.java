package com.l2jfrozen.gameserver.ai.special.manager;

import java.util.HashMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * @author programmos
 */
public class AIExtend implements Runnable
{
	private static HashMap<Integer, AIExtend> ai = new HashMap<>();
	private int idCharacter;
	
	/**
	 * @param id
	 */
	public void addAI(final int id)
	{
		if (ai.get(id) == null)
		{
			idCharacter = id;
			ai.put(id, this);
		}
	}
	
	public static enum Action
	{
		/** on spell finished action when npc finish casting skill */
		ON_SPELL_FINISHED(true),
		
		/** a person came within the Npc/Mob's range */
		ON_AGGRO_RANGE_ENTER(true),
		
		/** OnSpawn */
		ON_SPAWN(true),
		
		/** OnSkillUse (MOB_TARGETED_BY_SKILL) */
		ON_SKILL_USE(true),
		
		/** OnKill (MOBKILLED) */
		ON_KILL(true),
		
		/** OnAttack (MOBGOTATTACKED) */
		ON_ATTACK(true);
		
		private final boolean isRegistred;
		
		Action(final boolean reg)
		{
			isRegistred = reg;
		}
		
		public boolean isRegistred()
		{
			return isRegistred;
		}
	}
	
	public static void clearAllAI()
	{
		ai.clear();
		L2NpcTemplate.clearAI();
	}
	
	/**
	 * @return idCharacter
	 */
	public int getID()
	{
		return idCharacter;
	}
	
	public L2NpcTemplate addActionId(final int npcId, final Action actionType)
	{
		try
		{
			final L2NpcTemplate t = NpcTable.getInstance().getTemplate(npcId);
			
			if (t != null)
			{
				t.addAIEvent(actionType, this);
			}
			
			return t;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public String onAttack(final L2NpcInstance npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		return null;
	}
	
	public String onKill(final L2NpcInstance npc, final L2PcInstance killer, final boolean isPet)
	{
		return null;
	}
	
	public String onSkillUse(final L2NpcInstance npc, final L2PcInstance caster, final L2Skill skill)
	{
		return null;
	}
	
	public String onSpellFinished(final L2NpcInstance npc, final L2PcInstance player, final L2Skill skill)
	{
		return null;
	}
	
	public String onSpawn(final L2NpcInstance npc)
	{
		return null;
	}
	
	public String onAggroRangeEnter(final L2NpcInstance npc, final L2PcInstance player, final boolean isPet)
	{
		return null;
	}
	
	/*
	 * public String onDeath (L2Character killer, L2Character victim, QuestState qs) { if (killer instanceof L2NpcInstance) return onAdvEvent("", (L2NpcInstance)killer,qs.getPlayer()); else return onAdvEvent("", null,qs.getPlayer()); }
	 */
	
	public final boolean notifyAggroRangeEnter(final L2NpcInstance npc, final L2PcInstance player, final boolean isPet)
	{
		try
		{
			onAggroRangeEnter(npc, player, isPet);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return false;
		}
		return true;
	}
	
	public final boolean notifySpawn(final L2NpcInstance npc)
	{
		try
		{
			onSpawn(npc);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
	
	public final boolean notifySkillUse(final L2NpcInstance npc, final L2PcInstance caster, final L2Skill skill)
	{
		try
		{
			onSkillUse(npc, caster, skill);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			return false;
		}
		
		return true;
	}
	
	public final boolean notifySpellFinished(final L2NpcInstance npc, final L2PcInstance player, final L2Skill skill)
	{
		try
		{
			onSpellFinished(npc, player, skill);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
	
	public final boolean notifyKill(final L2NpcInstance npc, final L2PcInstance killer, final boolean isPet)
	{
		try
		{
			onKill(npc, killer, isPet);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return false;
		}
		
		return true;
	}
	
	public final boolean notifyAttack(final L2NpcInstance npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		try
		{
			onAttack(npc, attacker, damage, isPet);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return false;
		}
		
		return true;
	}
	
	/*
	 * public final boolean notifyDeath(L2Character killer, L2Character victim, QuestState qs) { try { onDeath(killer, victim, qs); } catch (Exception e) { return false; } return true; }
	 */
	
	@Override
	public void run()
	{
	}
	
}
