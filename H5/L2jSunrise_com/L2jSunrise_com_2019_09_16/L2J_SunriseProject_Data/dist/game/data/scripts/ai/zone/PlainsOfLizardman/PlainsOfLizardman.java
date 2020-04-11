package ai.zone.PlainsOfLizardman;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.serverpackets.MagicSkillUse;

import ai.npc.AbstractNpcAI;

/**
 * Plains of Lizardmen AI.
 * @author vGodFather
 */
public final class PlainsOfLizardman extends AbstractNpcAI
{
	// NPCs
	private static final int INVISIBLE_NPC = 18919;
	private static final int TANTA_GUARD = 18862;
	private static final int FANTASY_MUSHROOM = 18864;
	private static final int STICKY_MUSHROOM = 18865;
	private static final int RAINBOW_FROG = 18866;
	private static final int ABYSS_WEED = 18867;
	private static final int ENERGY_PLANT = 18868;
	private static final int TANTA_SCOUT = 22768;
	private static final int TANTA_MAGICIAN = 22773;
	private static final int[] TANTA_LIZARDMEN =
	{
		22768, // Tanta Lizardman Scout
		22769, // Tanta Lizardman Warrior
		22770, // Tanta Lizardman Soldier
		22771, // Tanta Lizardman Berserker
		22772, // Tanta Lizardman Archer
		22773, // Tanta Lizardman Magician
		22774, // Tanta Lizardman Summoner
	};
	// Skills
	private static final SkillHolder STUN_EFFECT = new SkillHolder(6622, 1);
	private static final SkillHolder FANTASY_MUSHROOM_SKILL = new SkillHolder(6427, 1);
	private static final SkillHolder RAINBOW_FROG_SKILL = new SkillHolder(6429, 1);
	private static final SkillHolder STICKY_MUSHROOM_SKILL = new SkillHolder(6428, 1);
	private static final SkillHolder ENERGY_PLANT_SKILL = new SkillHolder(6430, 1);
	// Buffs
	private static final SkillHolder[] BUFFS =
	{
		new SkillHolder(6625, 1), // Energy of Life
		new SkillHolder(6626, 2), // Energy of Life's Power
		new SkillHolder(6627, 3), // Energy of Life's Highest Power
		new SkillHolder(6628, 1), // Energy of Mana
		new SkillHolder(6629, 2), // Energy of Mana's Power
		new SkillHolder(6630, 3), // Energy of Mana's Highest Power
		new SkillHolder(6631, 1), // Energy of Power
		new SkillHolder(6633, 1), // Energy of Attack Speed
		new SkillHolder(6635, 1), // Energy of Crt Rate
		new SkillHolder(6636, 1), // Energy of Moving Speed
		new SkillHolder(6638, 1), // Aura of Mystery
		new SkillHolder(6639, 1), // Bane of Auras - Damage
		new SkillHolder(6640, 1), // Energizing Aura
		new SkillHolder(6674, 1), // Energy of Range Increment
	};
	// Misc
	// @formatter:off
	private static final int[] BUFF_LIST =
	{
		6, 7, 8, 11, 13
	};
	// @formatter:on
	
	public PlainsOfLizardman()
	{
		super(PlainsOfLizardman.class.getSimpleName(), "ai/zone/PlainsOfLizardman");
		addAttackId(FANTASY_MUSHROOM, RAINBOW_FROG, STICKY_MUSHROOM, ENERGY_PLANT);
		addKillId(TANTA_LIZARDMEN);
		addKillId(RAINBOW_FROG);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("fantasy_mushroom") && (npc != null) && (player != null))
		{
			npc.doCast(FANTASY_MUSHROOM_SKILL.getSkill());
			for (L2Character target : npc.getKnownList().getKnownCharactersInRadius(200))
			{
				if ((target != null) && target.isAttackable())
				{
					final L2Attackable monster = (L2Attackable) target;
					npc.setTarget(monster);
					npc.doCast(STUN_EFFECT.getSkill());
					attackPlayer(monster, player);
				}
			}
			npc.doDie(player);
		}
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		triggerNpcSkill(npc, attacker);
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		triggerNpcSkill(npc, killer);
		
		// Tanta Guard
		if (getRandom(1000) == 0)
		{
			final L2Npc guard = addSpawn(TANTA_GUARD, npc);
			attackPlayer((L2Attackable) guard, killer);
		}
		
		// Invisible buff npc
		final int random = getRandom(100);
		final L2Npc buffer = addSpawn(INVISIBLE_NPC, npc.getLocation(), false, 6000);
		buffer.setTarget(killer);
		
		if (random <= 42)
		{
			castRandomBuff(buffer, 7, 45, BUFFS[0], BUFFS[1], BUFFS[2]);
		}
		if (random <= 11)
		{
			castRandomBuff(buffer, 8, 60, BUFFS[3], BUFFS[4], BUFFS[5]);
			castRandomBuff(buffer, 3, 6, BUFFS[9], BUFFS[10], BUFFS[12]);
		}
		if (random <= 25)
		{
			buffer.doCast(BUFFS[BUFF_LIST[getRandom(BUFF_LIST.length)]].getSkill());
		}
		if (random <= 10)
		{
			buffer.doCast(BUFFS[13].getSkill());
		}
		if (random <= 1)
		{
			final int i = getRandom(100);
			if (i <= 34)
			{
				buffer.doCast(BUFFS[6].getSkill());
				buffer.doCast(BUFFS[7].getSkill());
				buffer.doCast(BUFFS[8].getSkill());
			}
			else if (i < 67)
			{
				buffer.doCast(BUFFS[13].getSkill());
			}
			else
			{
				buffer.doCast(BUFFS[2].getSkill());
				buffer.doCast(BUFFS[5].getSkill());
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	private void castRandomBuff(L2Npc npc, int chance1, int chance2, SkillHolder... buffs)
	{
		final int rand = getRandom(100);
		if (rand <= chance1)
		{
			npc.doCast(buffs[2].getSkill());
		}
		else if (rand <= chance2)
		{
			npc.doCast(buffs[1].getSkill());
		}
		else
		{
			npc.doCast(buffs[0].getSkill());
		}
	}
	
	private void triggerNpcSkill(L2Npc npc, L2PcInstance attacker)
	{
		switch (npc.getId())
		{
			case RAINBOW_FROG:
				ThreadPoolManager.getInstance().scheduleEffect(new TaskAfterDead(attacker, RAINBOW_FROG_SKILL), 3000);
				// castSkill(npc, attacker, RAINBOW_FROG_SKILL);
				npc.doDie(attacker);
				break;
			case STICKY_MUSHROOM:
				ThreadPoolManager.getInstance().scheduleEffect(new TaskAfterDead(attacker, STICKY_MUSHROOM_SKILL), 3000);
				// castSkill(npc, attacker, STICKY_MUSHROOM_SKILL);
				npc.doDie(attacker);
				break;
			case ENERGY_PLANT:
				ThreadPoolManager.getInstance().scheduleEffect(new TaskAfterDead(attacker, ENERGY_PLANT_SKILL), 3000);
				// castSkill(npc, attacker, ENERGY_PLANT_SKILL);
				npc.doDie(attacker);
				break;
			case ABYSS_WEED:
				npc.doDie(attacker);
				break;
			case FANTASY_MUSHROOM:
				if (npc.isScriptValue(0))
				{
					npc.setScriptValue(1);
					npc.setIsInvul(true);
					for (L2Character target : npc.getKnownList().getKnownCharactersInRadius(1000))
					{
						if ((target != null) && target.isAttackable())
						{
							final L2Attackable monster = (L2Attackable) target;
							if ((monster.getId() == TANTA_MAGICIAN) || (monster.getId() == TANTA_SCOUT))
							{
								target.setIsRunning(true);
								target.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(npc.getX(), npc.getY(), npc.getZ(), 0));
							}
						}
					}
					startQuestTimer("fantasy_mushroom", 4000, npc, attacker);
				}
				break;
		}
	}
	
	public static class TaskAfterDead implements Runnable
	{
		private final L2Character _killer;
		private final L2Skill _skill;
		
		public TaskAfterDead(L2Character killer, SkillHolder skill)
		{
			_killer = killer;
			_skill = skill.getSkill();
		}
		
		@Override
		public void run()
		{
			if ((_killer != null) && _killer.isPlayer() && !_killer.isDead())
			{
				_killer.broadcastPacket(new MagicSkillUse(_killer, _killer, _skill.getId(), _skill.getLevel(), 0, 0));
				_skill.applyEffects(_killer, _killer);
			}
		}
	}
}