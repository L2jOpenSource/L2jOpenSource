package com.l2jfrozen.gameserver.skills.l2skills;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.xml.ExperienceData;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.PetInfo;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.random.Rnd;

public class L2SkillSummon extends L2Skill
{
	public static final int SKILL_CUBIC_MASTERY = 143;
	
	private final int npcId;
	private final float expPenalty;
	private final boolean isCubic;
	
	// cubic AI
	// Activation time for a cubic
	private final int activationtime;
	// Activation chance for a cubic.
	private final int activationchance;
	
	// What is the total lifetime of summons (in millisecs)
	private final int summonTotalLifeTime;
	
	public L2SkillSummon(final StatsSet set)
	{
		super(set);
		
		npcId = set.getInteger("npcId", 0); // default for undescribed skills
		expPenalty = set.getFloat("expPenalty", 0.f);
		isCubic = set.getBool("isCubic", false);
		
		activationtime = set.getInteger("activationtime", 8);
		activationchance = set.getInteger("activationchance", 30);
		
		summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 1200000); // 20 minutes default
		summonTimeLostIdle = set.getInteger("summonTimeLostIdle", 0);
		summonTimeLostActive = set.getInteger("summonTimeLostActive", 0);
		
		itemConsumeOT = set.getInteger("itemConsumeCountOT", 0);
		itemConsumeIdOT = set.getInteger("itemConsumeIdOT", 0);
		itemConsumeTime = set.getInteger("itemConsumeTime", 0);
		itemConsumeSteps = set.getInteger("itemConsumeSteps", 0);
	}
	
	public boolean checkCondition(final L2Character activeChar)
	{
		if (activeChar instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) activeChar;
			
			if (isCubic())
			{
				if (getTargetType() != L2Skill.SkillTargetType.TARGET_SELF)
				{
					return true; // Player is always able to cast mass cubic skill
				}
				int mastery = player.getSkillLevel(SKILL_CUBIC_MASTERY);
				if (mastery < 0)
				{
					mastery = 0;
				}
				final int count = player.getCubics().size();
				if (count > mastery)
				{
					player.sendMessage("You already have " + count + " cubic(s).");
					return false;
				}
			}
			else
			{
				if (player.inObserverMode())
				{
					return false;
				}
				if (player.getPet() != null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
					return false;
				}
			}
		}
		return super.checkCondition(activeChar, null, false);
	}
	
	@Override
	public void useSkill(final L2Character caster, final L2Object[] targets)
	{
		if (caster.isAlikeDead() || !(caster instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance activeChar = (L2PcInstance) caster;
		
		// Skill 2046 only used for animation
		if (getId() == 2046)
		{
			return;
		}
		
		if (npcId == 0)
		{
			activeChar.sendMessage("Summon skill " + getId() + " not described yet");
			return;
		}
		
		if (isCubic)
		{
			// Gnacik :
			// If skill is enchanted calculate cubic skill level based on enchant
			// 8 at 101 (+1 Power)
			// 12 at 130 (+30 Power)
			// Because 12 is max 5115-5117 skills
			// TODO: make better method of calculation, dunno how its calculated on offi
			int cubicSkillLevel = getLevel();
			if (cubicSkillLevel > 100)
			{
				cubicSkillLevel = ((getLevel() - 100) / 7) + 8;
			}
			
			if (targets.length > 1) // Mass cubic skill
			{
				for (final L2Object obj : targets)
				{
					if (!(obj instanceof L2PcInstance))
					{
						continue;
					}
					final L2PcInstance player = ((L2PcInstance) obj);
					int mastery = player.getSkillLevel(SKILL_CUBIC_MASTERY);
					if (mastery < 0)
					{
						mastery = 0;
					}
					if (mastery == 0 && !player.getCubics().isEmpty())
					{
						// Player can have only 1 cubic - we shuld replace old cubic with new one
						player.unsummonAllCubics();
					}
					// TODO: Should remove first cubic summoned and replace with new cubic
					if (player.getCubics().containsKey(npcId))
					{
						final L2CubicInstance cubic = player.getCubic(npcId);
						cubic.stopAction();
						cubic.cancelDisappear();
						player.delCubic(npcId);
					}
					if (player.getCubics().size() > mastery)
					{
						continue;
					}
					if (player == activeChar)
					{
						player.addCubic(npcId, cubicSkillLevel, getPower(), activationtime, activationchance, summonTotalLifeTime, false);
					}
					else
					{
						// given by other player
						player.addCubic(npcId, cubicSkillLevel, getPower(), activationtime, activationchance, summonTotalLifeTime, true);
					}
					player.broadcastUserInfo();
				}
				return;
			}
			
			int mastery = activeChar.getSkillLevel(SKILL_CUBIC_MASTERY);
			if (mastery < 0)
			{
				mastery = 0;
			}
			if (activeChar.getCubics().containsKey(npcId))
			{
				final L2CubicInstance cubic = activeChar.getCubic(npcId);
				cubic.stopAction();
				cubic.cancelDisappear();
				activeChar.delCubic(npcId);
			}
			if (activeChar.getCubics().size() > mastery)
			{
				if (Config.DEBUG)
				{
					LOGGER.debug("player can't summon any more cubics. ignore summon skill");
				}
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CUBIC_SUMMONING_FAILED));
				return;
			}
			activeChar.addCubic(npcId, cubicSkillLevel, getPower(), activationtime, activationchance, summonTotalLifeTime, false);
			activeChar.broadcastUserInfo();
			return;
		}
		
		if (activeChar.getPet() != null || activeChar.isMounted())
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("player has a pet already. ignore summon skill");
			}
			return;
		}
		
		L2SummonInstance summon;
		final L2NpcTemplate summonTemplate = NpcTable.getInstance().getTemplate(npcId);
		if (summonTemplate == null)
		{
			LOGGER.warn("Summon attempt for nonexisting NPC ID:" + npcId + ", skill ID:" + getId());
			return; // npcID doesn't exist
		}
		if (summonTemplate.type.equalsIgnoreCase("L2SiegeSummon"))
		{
			summon = new L2SiegeSummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, this);
		}
		else
		{
			summon = new L2SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, this);
		}
		
		summon.setName(summonTemplate.name);
		summon.setTitle(activeChar.getName());
		summon.setExpPenalty(expPenalty);
		if (summon.getLevel() >= ExperienceData.getInstance().getMaxLevel())
		{
			summon.getStat().setExp(ExperienceData.getInstance().getExpForLevel(ExperienceData.getInstance().getMaxPetLevel() - 1));
			LOGGER.warn("Summon (" + summon.getName() + ") NpcID: " + summon.getNpcId() + " has a level above 75. Please rectify.");
		}
		else
		{
			summon.getStat().setExp(ExperienceData.getInstance().getExpForLevel(summon.getLevel() % ExperienceData.getInstance().getMaxPetLevel()));
		}
		summon.setCurrentHp(summon.getMaxHp());
		summon.setCurrentMp(summon.getMaxMp());
		summon.setHeading(activeChar.getHeading());
		summon.setRunning();
		activeChar.setPet(summon);
		
		L2World.getInstance().storeObject(summon);
		
		// Check to see if we should do the decay right after the cast
		if (getTargetType() == SkillTargetType.TARGET_CORPSE_MOB)
		{
			final L2Character target = (L2Character) targets[0];
			if (target.isDead() && target instanceof L2NpcInstance)
			{
				summon.spawnMe(target.getX(), target.getY(), target.getZ() + 5);
				((L2NpcInstance) target).endDecayTask();
			}
		}
		else
		{
			summon.spawnMe(activeChar.getX() + Rnd.get(40) - 20, activeChar.getY() + Rnd.get(40) - 20, activeChar.getZ());
		}
		
		summon.setFollowStatus(true);
		summon.setShowSummonAnimation(false); // addVisibleObject created the info packets with summon animation
		// if someone comes into range now, the animation shouldnt show any more
		activeChar.sendPacket(new PetInfo(summon));
	}
	
	public final boolean isCubic()
	{
		return isCubic;
	}
	
}
