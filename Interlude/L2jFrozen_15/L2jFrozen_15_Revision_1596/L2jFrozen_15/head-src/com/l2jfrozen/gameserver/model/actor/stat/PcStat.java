package com.l2jfrozen.gameserver.model.actor.stat;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.xml.ExperienceData;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2ClassMasterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.base.ClassLevel;
import com.l2jfrozen.gameserver.model.base.PlayerClass;
import com.l2jfrozen.gameserver.model.base.SubClass;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.model.quest.QuestState;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.UserInfo;

public class PcStat extends PlayableStat
{
	private static Logger LOGGER = Logger.getLogger(PcStat.class);
	
	private int oldMaxHp; // stats watch
	private int oldMaxMp; // stats watch
	private int oldMaxCp; // stats watch
	
	public PcStat(final L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean addExp(final long value)
	{
		L2PcInstance activeChar = getActiveChar();
		
		// Player is Gm and access level is below or equal to canGainExp and is in party, don't give Xp
		if (!getActiveChar().getAccessLevel().canGainExp() && getActiveChar().isInParty())
		{
			return false;
		}
		
		if (!super.addExp(value))
		{
			return false;
		}
		
		// Set new karma
		if (!activeChar.isCursedWeaponEquiped() && activeChar.getKarma() > 0 && (activeChar.isGM() || !activeChar.isInsideZone(L2Character.ZONE_PVP)))
		{
			final int karmaLost = activeChar.calculateKarmaLost(value);
			
			if (karmaLost > 0)
			{
				activeChar.setKarma(activeChar.getKarma() - karmaLost);
			}
		}
		
		/*
		 * Micht : Use of UserInfo for C5 StatusUpdate su = new StatusUpdate(activeChar.getObjectId()); su.addAttribute(StatusUpdate.EXP, getExp()); activeChar.sendPacket(su);
		 */
		activeChar.sendPacket(new UserInfo(activeChar));
		
		activeChar = null;
		
		return true;
	}
	
	/**
	 * Add Experience and SP rewards to the L2PcInstance, remove its Karma (if necessary) and Launch increase level task.<BR>
	 * <BR>
	 * <B><U> Actions </U> :</B><BR>
	 * <BR>
	 * <li>Remove Karma when the player kills L2MonsterInstance</li>
	 * <li>Send a Server->Client packet StatusUpdate to the L2PcInstance</li>
	 * <li>Send a Server->Client System Message to the L2PcInstance</li>
	 * <li>If the L2PcInstance increases it's level, send a Server->Client packet SocialAction (broadcast)</li>
	 * <li>If the L2PcInstance increases it's level, manage the increase level task (Max MP, Max MP, Recommendation, Expertise and beginner skills...)</li>
	 * <li>If the L2PcInstance increases it's level, send a Server->Client packet UserInfo to the L2PcInstance</li><BR>
	 * <BR>
	 * @param addToExp The Experience value to add
	 * @param addToSp  The SP value to add
	 */
	@Override
	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		float ratioTakenByPet = 0;
		
		// Player is Gm and acces level is below or equal to GM_DONT_TAKE_EXPSP and is in party, don't give Xp/Sp
		L2PcInstance activeChar = getActiveChar();
		if (!activeChar.getAccessLevel().canGainExp() && activeChar.isInParty())
		{
			return false;
		}
		
		// if this player has a pet that takes from the owner's Exp, give the pet Exp now
		
		if (activeChar.getPet() instanceof L2PetInstance)
		{
			L2PetInstance pet = (L2PetInstance) activeChar.getPet();
			ratioTakenByPet = pet.getPetData().getOwnerExpTaken();
			
			// only give exp/sp to the pet by taking from the owner if the pet has a non-zero, positive ratio
			// allow possible customizations that would have the pet earning more than 100% of the owner's exp/sp
			if (ratioTakenByPet > 0 && !pet.isDead())
			{
				pet.addExpAndSp((long) (addToExp * ratioTakenByPet), (int) (addToSp * ratioTakenByPet));
			}
			
			// now adjust the max ratio to avoid the owner earning negative exp/sp
			if (ratioTakenByPet > 1)
			{
				ratioTakenByPet = 1;
			}
			
			addToExp = (long) (addToExp * (1 - ratioTakenByPet));
			addToSp = (int) (addToSp * (1 - ratioTakenByPet));
			
			pet = null;
		}
		
		if (!super.addExpAndSp(addToExp, addToSp))
		{
			return false;
		}
		
		// Send a Server->Client System Message to the L2PcInstance
		SystemMessage sm = new SystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_AND_S2_SP);
		sm.addNumber((int) addToExp);
		sm.addNumber(addToSp);
		getActiveChar().sendPacket(sm);
		sm = null;
		
		activeChar = null;
		
		return true;
	}
	
	@Override
	public boolean removeExpAndSp(final long addToExp, final int addToSp)
	{
		if (!super.removeExpAndSp(addToExp, addToSp))
		{
			return false;
		}
		
		// Send a Server->Client System Message to the L2PcInstance
		SystemMessage sm = new SystemMessage(SystemMessageId.EXP_DECREASED_BY_S1);
		sm.addNumber((int) addToExp);
		getActiveChar().sendPacket(sm);
		sm = null;
		
		sm = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
		sm.addNumber(addToSp);
		getActiveChar().sendPacket(sm);
		sm = null;
		
		return true;
	}
	
	@Override
	public final boolean addLevel(final byte value)
	{
		// getActiveChar().setLocked(true);
		if (getLevel() + value > ExperienceData.getInstance().getMaxLevel() - 1)
		{
			return false;
		}
		
		final boolean levelIncreased = super.addLevel(value);
		
		if (Config.ALLOW_CLASS_MASTERS && Config.ALLOW_REMOTE_CLASS_MASTERS)
		{
			final L2ClassMasterInstance master_instance = L2ClassMasterInstance.getInstance();
			
			if (master_instance != null)
			{
				
				final ClassLevel lvlnow = PlayerClass.values()[getActiveChar().getClassId().getId()].getLevel();
				if (getLevel() >= 20 && lvlnow == ClassLevel.First)
				{
					L2ClassMasterInstance.getInstance().onAction(getActiveChar());
				}
				else if (getLevel() >= 40 && lvlnow == ClassLevel.Second)
				{
					L2ClassMasterInstance.getInstance().onAction(getActiveChar());
				}
				else if (getLevel() >= 76 && lvlnow == ClassLevel.Third)
				{
					L2ClassMasterInstance.getInstance().onAction(getActiveChar());
				}
				
			}
			else
			{
				
				LOGGER.info("Attention: Remote ClassMaster is Enabled, but not inserted into DataBase. Remember to install 31288 Custom_Npc ..");
				
			}
			
		}
		
		if (levelIncreased)
		{
			if (getActiveChar().getLevel() >= Config.MAX_LEVEL_NEWBIE_STATUS && getActiveChar().isNewbie())
			{
				getActiveChar().setNewbie(false);
				
				if (Config.DEBUG)
				{
					LOGGER.info("Newbie character ended: " + getActiveChar().getCharId());
				}
			}
			
			QuestState qs = getActiveChar().getQuestState("255_Tutorial");
			
			if (qs != null && qs.getQuest() != null)
			{
				qs.getQuest().notifyEvent("CE40", null, getActiveChar());
			}
			
			getActiveChar().setCurrentCp(getMaxCp());
			getActiveChar().broadcastPacket(new SocialAction(getActiveChar().getObjectId(), 15));
			getActiveChar().sendPacket(new SystemMessage(SystemMessageId.YOU_INCREASED_YOUR_LEVEL));
			
			qs = null;
		}
		
		if (getActiveChar().isInFunEvent())
		{
			if (getActiveChar().inEventTvT && TvT.getMaxlvl() == getLevel() && !TvT.isStarted())
			{
				TvT.removePlayer(getActiveChar());
			}
			getActiveChar().sendMessage("Your event sign up was canceled.");
		}
		
		getActiveChar().rewardSkills(); // Give Expertise skill of this level
		
		if (getActiveChar().getClan() != null)
		{
			getActiveChar().getClan().updateClanMember(getActiveChar());
			getActiveChar().getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(getActiveChar()));
		}
		
		if (getActiveChar().isInParty())
		{
			getActiveChar().getParty().recalculatePartyLevel(); // Recalculate the party level
		}
		
		StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		getActiveChar().sendPacket(su);
		su = null;
		
		// Update the overloaded status of the L2PcInstance
		getActiveChar().refreshOverloaded();
		// Update the expertise status of the L2PcInstance
		getActiveChar().refreshExpertisePenalty();
		// Send a Server->Client packet UserInfo to the L2PcInstance
		getActiveChar().sendPacket(new UserInfo(getActiveChar()));
		// getActiveChar().setLocked(false);
		return levelIncreased;
	}
	
	@Override
	public boolean addSp(final int value)
	{
		if (!super.addSp(value))
		{
			return false;
		}
		
		StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
		su.addAttribute(StatusUpdate.SP, getSp());
		getActiveChar().sendPacket(su);
		su = null;
		
		return true;
	}
	
	@Override
	public final long getExpForLevel(final int level)
	{
		return ExperienceData.getInstance().getExpForLevel(level);
	}
	
	@Override
	public final L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}
	
	@Override
	public final long getExp()
	{
		final L2PcInstance player = getActiveChar();
		if (player != null && player.isSubClassActive())
		{
			
			final int class_index = player.getClassIndex();
			
			SubClass player_subclass = null;
			if ((player_subclass = player.getSubClasses().get(class_index)) != null)
			{
				return player_subclass.getExp();
			}
			
		}
		
		return super.getExp();
	}
	
	@Override
	public final void setExp(final long value)
	{
		final L2PcInstance player = getActiveChar();
		
		if (player.isSubClassActive())
		{
			final int class_index = player.getClassIndex();
			
			SubClass player_subclass = null;
			if ((player_subclass = player.getSubClasses().get(class_index)) != null)
			{
				player_subclass.setExp(value);
			}
			
			// getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setExp(value);
		}
		else
		{
			super.setExp(value);
		}
	}
	
	@Override
	public final int getLevel()
	{
		try
		{
			final L2PcInstance player = getActiveChar();
			
			if (player.isSubClassActive())
			{
				final int class_index = player.getClassIndex();
				
				SubClass player_subclass = null;
				if ((player_subclass = player.getSubClasses().get(class_index)) != null)
				{
					return player_subclass.getLevel();
				}
				
				// getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setExp(value);
			}
			
			// if (getActiveChar().isSubClassActive())
			// return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getLevel();
			//
			return super.getLevel();
		}
		catch (final NullPointerException e)
		{
			return -1;
		}
	}
	
	@Override
	public final void setLevel(int value)
	{
		if (value > ExperienceData.getInstance().getMaxLevel() - 1)
		{
			value = ExperienceData.getInstance().getMaxLevel() - 1;
		}
		
		final L2PcInstance player = getActiveChar();
		
		if (player.isSubClassActive())
		{
			final int class_index = player.getClassIndex();
			
			SubClass player_subclass = null;
			if ((player_subclass = player.getSubClasses().get(class_index)) != null)
			{
				player_subclass.setLevel(value);
			}
			
			// if(getActiveChar().isSubClassActive())
			// {
			// getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setLevel(value);
		}
		else
		{
			super.setLevel(value);
		}
	}
	
	@Override
	public final int getMaxCp()
	{
		final int val = super.getMaxCp();
		
		if (val != oldMaxCp)
		{
			oldMaxCp = val;
			
			final L2PcInstance player = getActiveChar();
			
			if (player.getStatus().getCurrentCp() != val)
			{
				player.getStatus().setCurrentCp(getActiveChar().getStatus().getCurrentCp());
			}
		}
		return val;
	}
	
	@Override
	public final int getMaxHp()
	{
		// Get the Max HP (base+modifier) of the L2PcInstance
		final int val = super.getMaxHp();
		
		if (val != oldMaxHp)
		{
			oldMaxHp = val;
			
			final L2PcInstance player = getActiveChar();
			
			// Launch a regen task if the new Max HP is higher than the old one
			if (player.getStatus().getCurrentHp() != val)
			{
				player.getStatus().setCurrentHp(player.getStatus().getCurrentHp()); // trigger start of regeneration
			}
		}
		
		return val;
	}
	
	@Override
	public final int getMaxMp()
	{
		// Get the Max MP (base+modifier) of the L2PcInstance
		final int val = super.getMaxMp();
		
		if (val != oldMaxMp)
		{
			oldMaxMp = val;
			
			final L2PcInstance player = getActiveChar();
			
			// Launch a regen task if the new Max MP is higher than the old one
			if (player.getStatus().getCurrentMp() != val)
			{
				player.getStatus().setCurrentMp(player.getStatus().getCurrentMp()); // trigger start of regeneration
			}
		}
		
		return val;
	}
	
	@Override
	public final int getSp()
	{
		final L2PcInstance player = getActiveChar();
		
		if (player.isSubClassActive())
		{
			final int class_index = player.getClassIndex();
			
			SubClass player_subclass = null;
			if ((player_subclass = player.getSubClasses().get(class_index)) != null)
			{
				return player_subclass.getSp();
			}
		}
		// if(getActiveChar().isSubClassActive())
		// return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getSp();
		
		return super.getSp();
	}
	
	@Override
	public final void setSp(final int value)
	{
		
		final L2PcInstance player = getActiveChar();
		
		if (player.isSubClassActive())
		{
			final int class_index = player.getClassIndex();
			
			SubClass player_subclass = null;
			if ((player_subclass = player.getSubClasses().get(class_index)) != null)
			{
				player_subclass.setSp(value);
			}
		}
		
		// if(getActiveChar().isSubClassActive())
		// {
		// getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setSp(value);
		// }
		else
		{
			super.setSp(value);
		}
	}
}
