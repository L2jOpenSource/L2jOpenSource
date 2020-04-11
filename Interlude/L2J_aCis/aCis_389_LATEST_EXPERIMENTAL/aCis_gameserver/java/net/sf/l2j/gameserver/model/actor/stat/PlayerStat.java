package net.sf.l2j.gameserver.model.actor.stat;

import java.util.Map;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.data.xml.PlayerLevelData;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.PlayerLevel;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.container.npc.RewardInfo;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.model.zone.type.SwampZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.skills.L2Skill;

public class PlayerStat extends PlayableStat
{
	private int _oldMaxHp;
	private int _oldMaxMp;
	private int _oldMaxCp;
	
	public PlayerStat(Player player)
	{
		super(player);
	}
	
	@Override
	public final int getSTR()
	{
		return (int) calcStat(Stats.STAT_STR, getActor().getTemplate().getBaseSTR(), null, null);
	}
	
	@Override
	public final int getDEX()
	{
		return (int) calcStat(Stats.STAT_DEX, getActor().getTemplate().getBaseDEX(), null, null);
	}
	
	@Override
	public final int getCON()
	{
		return (int) calcStat(Stats.STAT_CON, getActor().getTemplate().getBaseCON(), null, null);
	}
	
	@Override
	public int getINT()
	{
		return (int) calcStat(Stats.STAT_INT, getActor().getTemplate().getBaseINT(), null, null);
	}
	
	@Override
	public final int getMEN()
	{
		return (int) calcStat(Stats.STAT_MEN, getActor().getTemplate().getBaseMEN(), null, null);
	}
	
	@Override
	public final int getWIT()
	{
		return (int) calcStat(Stats.STAT_WIT, getActor().getTemplate().getBaseWIT(), null, null);
	}
	
	@Override
	public boolean addExp(long value)
	{
		// Allowed to gain exp?
		if (!getActor().getAccessLevel().canGainExp())
			return false;
		
		if (!super.addExp(value))
			return false;
		
		getActor().sendPacket(new UserInfo(getActor()));
		return true;
	}
	
	/**
	 * Add Experience and SP rewards to the Player, remove its Karma (if necessary) and Launch increase level task.
	 * <ul>
	 * <li>Remove Karma when the player kills Monster</li>
	 * <li>Send StatusUpdate to the Player</li>
	 * <li>Send a Server->Client System Message to the Player</li>
	 * <li>If the Player increases its level, send SocialAction (broadcast)</li>
	 * <li>If the Player increases its level, manage the increase level task (Max MP, Max MP, Recommandation, Expertise and beginner skills...)</li>
	 * <li>If the Player increases its level, send UserInfo to the Player</li>
	 * </ul>
	 * @param addToExp The Experience value to add
	 * @param addToSp The SP value to add
	 */
	@Override
	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		if (!super.addExpAndSp(addToExp, addToSp))
			return false;
		
		SystemMessage sm;
		
		if (addToExp == 0 && addToSp > 0)
			sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_SP).addNumber(addToSp);
		else if (addToExp > 0 && addToSp == 0)
			sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_EXPERIENCE).addNumber((int) addToExp);
		else
			sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_AND_S2_SP).addNumber((int) addToExp).addNumber(addToSp);
		
		getActor().sendPacket(sm);
		
		return true;
	}
	
	/**
	 * Add Experience and SP rewards to the Player, remove its Karma (if necessary) and Launch increase level task.
	 * <ul>
	 * <li>Remove Karma when the player kills Monster</li>
	 * <li>Send StatusUpdate to the Player</li>
	 * <li>Send a Server->Client System Message to the Player</li>
	 * <li>If the Player increases its level, send SocialAction (broadcast)</li>
	 * <li>If the Player increases its level, manage the increase level task (Max MP, Max MP, Recommandation, Expertise and beginner skills...)</li>
	 * <li>If the Player increases its level, send UserInfo to the Player</li>
	 * </ul>
	 * @param addToExp The Experience value to add
	 * @param addToSp The SP value to add
	 * @param rewards The list of players and summons, who done damage
	 * @return
	 */
	public boolean addExpAndSp(long addToExp, int addToSp, Map<Creature, RewardInfo> rewards)
	{
		// GM check concerning canGainExp().
		if (!getActor().getAccessLevel().canGainExp())
			return false;
		
		// If this player has a pet, give the xp to the pet now (if any).
		if (getActor().hasPet())
		{
			final Pet pet = (Pet) getActor().getSummon();
			if (pet.getStat().getExp() <= (pet.getTemplate().getPetDataEntry(81).getMaxExp() + 10000) && !pet.isDead() && pet.isIn3DRadius(getActor(), Config.PARTY_RANGE))
			{
				long petExp = 0;
				int petSp = 0;
				
				int ratio = pet.getPetData().getExpType();
				if (ratio == -1)
				{
					RewardInfo r = rewards.get(pet);
					RewardInfo reward = rewards.get(getActor());
					if (r != null && reward != null)
					{
						double damageDoneByPet = ((double) (r.getDamage())) / reward.getDamage();
						petExp = (long) (addToExp * damageDoneByPet);
						petSp = (int) (addToSp * damageDoneByPet);
					}
				}
				else
				{
					// now adjust the max ratio to avoid the owner earning negative exp/sp
					if (ratio > 100)
						ratio = 100;
					
					petExp = Math.round(addToExp * (1 - (ratio / 100.0)));
					petSp = (int) Math.round(addToSp * (1 - (ratio / 100.0)));
				}
				
				addToExp -= petExp;
				addToSp -= petSp;
				pet.addExpAndSp(petExp, petSp);
			}
		}
		return addExpAndSp(addToExp, addToSp);
	}
	
	@Override
	public boolean removeExpAndSp(long removeExp, int removeSp)
	{
		return removeExpAndSp(removeExp, removeSp, true);
	}
	
	public boolean removeExpAndSp(long removeExp, int removeSp, boolean sendMessage)
	{
		final int oldLevel = getLevel();
		
		if (!super.removeExpAndSp(removeExp, removeSp))
			return false;
		
		// Send messages.
		if (sendMessage)
		{
			if (removeExp > 0)
				getActor().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EXP_DECREASED_BY_S1).addNumber((int) removeExp));
			
			if (removeSp > 0)
				getActor().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1).addNumber(removeSp));
			
			if (getLevel() < oldLevel)
				getActor().broadcastStatusUpdate();
		}
		return true;
	}
	
	@Override
	public final boolean addLevel(byte value)
	{
		if (getLevel() + value > PlayerLevelData.getInstance().getRealMaxLevel())
			return false;
		
		boolean levelIncreased = super.addLevel(value);
		
		if (levelIncreased)
		{
			if (!Config.DISABLE_TUTORIAL)
			{
				QuestState qs = getActor().getQuestState("Tutorial");
				if (qs != null)
					qs.getQuest().notifyEvent("CE40", null, getActor());
			}
			
			getActor().setCurrentCp(getMaxCp());
			getActor().broadcastPacket(new SocialAction(getActor(), 15));
			getActor().sendPacket(SystemMessageId.YOU_INCREASED_YOUR_LEVEL);
		}
		
		// Refresh player skills (autoGet skills or all available skills if Config.AUTO_LEARN_SKILLS is activated).
		getActor().giveSkills();
		
		final Clan clan = getActor().getClan();
		if (clan != null)
		{
			final ClanMember member = clan.getClanMember(getActor().getObjectId());
			if (member != null)
				member.refreshLevel();
			
			clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(getActor()));
		}
		
		// Recalculate the party level
		final Party party = getActor().getParty();
		if (party != null)
			party.recalculateLevel();
		
		// Update the overloaded status of the player
		getActor().refreshOverloaded();
		// Update the expertise status of the player
		getActor().refreshExpertisePenalty();
		// Send UserInfo to the player
		getActor().sendPacket(new UserInfo(getActor()));
		
		return levelIncreased;
	}
	
	@Override
	public final Player getActor()
	{
		return (Player) super.getActor();
	}
	
	@Override
	public final long getExp()
	{
		if (getActor().isSubClassActive())
			return getActor().getSubClasses().get(getActor().getClassIndex()).getExp();
		
		return super.getExp();
	}
	
	@Override
	public final void setExp(long value)
	{
		if (getActor().isSubClassActive())
			getActor().getSubClasses().get(getActor().getClassIndex()).setExp(value);
		else
			super.setExp(value);
	}
	
	@Override
	public final byte getLevel()
	{
		if (getActor().isSubClassActive())
			return getActor().getSubClasses().get(getActor().getClassIndex()).getLevel();
		
		return super.getLevel();
	}
	
	@Override
	public final void setLevel(byte value)
	{
		value = (byte) Math.min(value, PlayerLevelData.getInstance().getRealMaxLevel());
		
		if (getActor().isSubClassActive())
			getActor().getSubClasses().get(getActor().getClassIndex()).setLevel(value);
		else
			super.setLevel(value);
	}
	
	@Override
	public final int getMaxCp()
	{
		// Get the Max CP (base+modifier) of the player
		int val = (int) calcStat(Stats.MAX_CP, getActor().getTemplate().getBaseCpMax(getActor().getLevel()), null, null);
		if (val != _oldMaxCp)
		{
			_oldMaxCp = val;
			
			// Launch a regen task if the new Max CP is higher than the old one
			if (getActor().getStatus().getCurrentCp() != val)
				getActor().getStatus().setCurrentCp(getActor().getStatus().getCurrentCp()); // trigger start of regeneration
		}
		return val;
	}
	
	@Override
	public final int getMaxHp()
	{
		// Get the Max HP (base+modifier) of the player
		int val = super.getMaxHp();
		if (val != _oldMaxHp)
		{
			_oldMaxHp = val;
			
			// Launch a regen task if the new Max HP is higher than the old one
			if (getActor().getStatus().getCurrentHp() != val)
				getActor().getStatus().setCurrentHp(getActor().getStatus().getCurrentHp()); // trigger start of regeneration
		}
		
		return val;
	}
	
	@Override
	public final int getMaxMp()
	{
		// Get the Max MP (base+modifier) of the player
		int val = super.getMaxMp();
		
		if (val != _oldMaxMp)
		{
			_oldMaxMp = val;
			
			// Launch a regen task if the new Max MP is higher than the old one
			if (getActor().getStatus().getCurrentMp() != val)
				getActor().getStatus().setCurrentMp(getActor().getStatus().getCurrentMp()); // trigger start of regeneration
		}
		
		return val;
	}
	
	@Override
	public final int getSp()
	{
		if (getActor().isSubClassActive())
			return getActor().getSubClasses().get(getActor().getClassIndex()).getSp();
		
		return super.getSp();
	}
	
	@Override
	public final void setSp(int value)
	{
		if (getActor().isSubClassActive())
			getActor().getSubClasses().get(getActor().getClassIndex()).setSp(value);
		else
			super.setSp(value);
		
		StatusUpdate su = new StatusUpdate(getActor());
		su.addAttribute(StatusUpdate.SP, getSp());
		getActor().sendPacket(su);
	}
	
	@Override
	public int getBaseRunSpeed()
	{
		if (getActor().isMounted())
		{
			int base = (getActor().isFlying()) ? getActor().getPetDataEntry().getMountFlySpeed() : getActor().getPetDataEntry().getMountBaseSpeed();
			
			if (getActor().getLevel() < getActor().getMountLevel())
				base /= 2;
			
			if (getActor().checkFoodState(getActor().getPetTemplate().getHungryLimit()))
				base /= 2;
			
			return base;
		}
		
		return super.getBaseRunSpeed();
	}
	
	public int getBaseSwimSpeed()
	{
		if (getActor().isMounted())
		{
			int base = getActor().getPetDataEntry().getMountSwimSpeed();
			
			if (getActor().getLevel() < getActor().getMountLevel())
				base /= 2;
			
			if (getActor().checkFoodState(getActor().getPetTemplate().getHungryLimit()))
				base /= 2;
			
			return base;
		}
		
		return getActor().getTemplate().getBaseSwimSpeed();
	}
	
	@Override
	public float getMoveSpeed()
	{
		// get base value, use swimming speed in water
		float baseValue = (getActor().isInWater()) ? getBaseSwimSpeed() : getBaseMoveSpeed();
		
		// apply zone modifier before final calculation
		if (getActor().isInsideZone(ZoneId.SWAMP))
		{
			final SwampZone zone = ZoneManager.getInstance().getZone(getActor(), SwampZone.class);
			if (zone != null)
				baseValue *= (100 + zone.getMoveBonus()) / 100.0;
		}
		
		// apply armor grade penalty before final calculation
		final int penalty = getActor().getExpertiseArmorPenalty();
		if (penalty > 0)
			baseValue *= Math.pow(0.84, penalty);
		
		// calculate speed
		return (float) calcStat(Stats.RUN_SPEED, baseValue, null, null);
	}
	
	@Override
	public float getRealMoveSpeed(boolean isStillWalking)
	{
		// get base value, use swimming speed in water
		float baseValue = (getActor().isInWater()) ? getBaseSwimSpeed() : ((isStillWalking || !getActor().isRunning()) ? getBaseWalkSpeed() : getBaseRunSpeed());
		
		// apply zone modifier before final calculation
		if (getActor().isInsideZone(ZoneId.SWAMP))
		{
			final SwampZone zone = ZoneManager.getInstance().getZone(getActor(), SwampZone.class);
			if (zone != null)
				baseValue *= (100 + zone.getMoveBonus()) / 100.0;
		}
		
		// apply armor grade penalty before final calculation
		final int penalty = getActor().getExpertiseArmorPenalty();
		if (penalty > 0)
			baseValue *= Math.pow(0.84, penalty);
		
		// calculate speed
		return (float) calcStat(Stats.RUN_SPEED, baseValue, null, null);
	}
	
	@Override
	public int getMAtk(Creature target, L2Skill skill)
	{
		if (getActor().isMounted())
		{
			double base = getActor().getPetDataEntry().getMountMAtk();
			
			if (getActor().getLevel() < getActor().getMountLevel())
				base /= 2;
			
			return (int) calcStat(Stats.MAGIC_ATTACK, base, null, null);
		}
		
		return super.getMAtk(target, skill);
	}
	
	@Override
	public int getMAtkSpd()
	{
		double base = 333;
		
		if (getActor().isMounted())
		{
			if (getActor().checkFoodState(getActor().getPetTemplate().getHungryLimit()))
				base /= 2;
		}
		
		final int penalty = getActor().getExpertiseArmorPenalty();
		if (penalty > 0)
			base *= Math.pow(0.84, penalty);
		
		return (int) calcStat(Stats.MAGIC_ATTACK_SPEED, base, null, null);
	}
	
	@Override
	public int getPAtk(Creature target)
	{
		if (getActor().isMounted())
		{
			double base = getActor().getPetDataEntry().getMountPAtk();
			
			if (getActor().getLevel() < getActor().getMountLevel())
				base /= 2;
			
			return (int) calcStat(Stats.POWER_ATTACK, base, null, null);
		}
		
		return super.getPAtk(target);
	}
	
	@Override
	public int getPAtkSpd()
	{
		if (getActor().isFlying())
			return (getActor().checkFoodState(getActor().getPetTemplate().getHungryLimit())) ? 150 : 300;
		
		if (getActor().isRiding())
		{
			int base = getActor().getPetDataEntry().getMountAtkSpd();
			
			if (getActor().checkFoodState(getActor().getPetTemplate().getHungryLimit()))
				base /= 2;
			
			return (int) calcStat(Stats.POWER_ATTACK_SPEED, base, null, null);
		}
		
		return super.getPAtkSpd();
	}
	
	@Override
	public int getEvasionRate(Creature target)
	{
		int val = super.getEvasionRate(target);
		
		final int penalty = getActor().getExpertiseArmorPenalty();
		if (penalty > 0)
			val -= (2 * penalty);
		
		return val;
	}
	
	@Override
	public int getAccuracy()
	{
		int val = super.getAccuracy();
		
		if (getActor().getExpertiseWeaponPenalty())
			val -= 20;
		
		return val;
	}
	
	@Override
	public int getPhysicalAttackRange()
	{
		return (int) calcStat(Stats.POWER_ATTACK_RANGE, getActor().getAttackType().getRange(), null, null);
	}
	
	@Override
	public long getExpForLevel(int level)
	{
		final PlayerLevel pl = PlayerLevelData.getInstance().getPlayerLevel(level);
		if (pl == null)
			return 0;
		
		return pl.getRequiredExpToLevelUp();
	}
	
	@Override
	public long getExpForThisLevel()
	{
		final PlayerLevel pl = PlayerLevelData.getInstance().getPlayerLevel(getLevel());
		if (pl == null)
			return 0;
		
		return pl.getRequiredExpToLevelUp();
	}
	
	@Override
	public long getExpForNextLevel()
	{
		final PlayerLevel pl = PlayerLevelData.getInstance().getPlayerLevel(getLevel() + 1);
		if (pl == null)
			return 0;
		
		return pl.getRequiredExpToLevelUp();
	}
}