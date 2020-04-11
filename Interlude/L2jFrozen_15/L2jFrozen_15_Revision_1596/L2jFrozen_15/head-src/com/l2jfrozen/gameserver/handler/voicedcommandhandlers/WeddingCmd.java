/* L2jFrozen Project 
 * 
 * This program is free software; you can redistribute it and/or modify */package com.l2jfrozen.gameserver.handler.voicedcommandhandlers;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.CoupleManager;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ConfirmDlg;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.SetupGauge;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.Broadcast;

/**
 * @author L2JFrozen
 */
public class WeddingCmd implements IVoicedCommandHandler
{
	protected static final Logger LOGGER = Logger.getLogger(WeddingCmd.class);
	
	private static String[] voicedCommands =
	{
		"divorce",
		"engage",
		"gotolove"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (activeChar.isInFunEvent() || activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("Sorry,you are in event now.");
			return false;
		}
		
		if (command.startsWith("engage"))
		{
			return engage(activeChar);
		}
		else if (command.startsWith("divorce"))
		{
			return divorce(activeChar);
		}
		else if (command.startsWith("gotolove"))
		{
			return goToLove(activeChar);
		}
		return false;
	}
	
	public boolean divorce(final L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (activeChar.getPartnerId() == 0)
		{
			return false;
		}
		
		final int partnerId = activeChar.getPartnerId();
		final int coupleId = activeChar.getCoupleId();
		int adenaAmount = 0;
		
		if (activeChar.isMarried())
		{
			activeChar.sendMessage("You are now divorced.");
			adenaAmount = (activeChar.getAdena() / 100) * Config.L2JMOD_WEDDING_DIVORCE_COSTS;
			activeChar.getInventory().reduceAdena("Wedding", adenaAmount, activeChar, null);
		}
		else
		{
			activeChar.sendMessage("You have broken up as a couple.");
		}
		
		L2PcInstance partner;
		partner = (L2PcInstance) L2World.getInstance().findObject(partnerId);
		
		if (partner != null)
		{
			partner.setPartnerId(0);
			if (partner.isMarried())
			{
				partner.sendMessage("Your spouse has decided to divorce you.");
			}
			else
			{
				partner.sendMessage("Your fiance has decided to break the engagement with you.");
			}
			
			// give adena
			if (adenaAmount > 0)
			{
				partner.addAdena("WEDDING", adenaAmount, null, false);
			}
		}
		
		partner = null;
		
		CoupleManager.getInstance().deleteCouple(coupleId);
		return true;
	}
	
	public boolean engage(final L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		// check target
		if (activeChar.getTarget() == null)
		{
			activeChar.sendMessage("You have no one targeted.");
			return false;
		}
		
		// check if target is a l2pcinstance
		if (!(activeChar.getTarget() instanceof L2PcInstance))
		{
			activeChar.sendMessage("You can only ask another player to engage you.");
			return false;
		}
		
		L2PcInstance ptarget = (L2PcInstance) activeChar.getTarget();
		
		// check if player is already engaged
		if (activeChar.getPartnerId() != 0)
		{
			activeChar.sendMessage("You are already engaged.");
			
			if (Config.L2JMOD_WEDDING_PUNISH_INFIDELITY)
			{
				activeChar.startAbnormalEffect((short) 0x2000); // give player a Big Head
				// lets recycle the sevensigns debuffs
				int skillId;
				int skillLevel = 1;
				
				if (activeChar.getLevel() > 40)
				{
					skillLevel = 2;
				}
				
				if (activeChar.isMageClass())
				{
					skillId = 4361;
				}
				else
				{
					skillId = 4362;
				}
				
				L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
				if (activeChar.getFirstEffect(skill) == null)
				{
					skill.getEffects(activeChar, activeChar, false, false, false);
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
					sm.addSkillName(skillId);
					activeChar.sendPacket(sm);
					sm = null;
				}
				skill = null;
			}
			return false;
		}
		
		// check if player target himself
		if (ptarget.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendMessage("Is there something wrong with you, are you trying to go out with yourself?");
			return false;
		}
		
		if (ptarget.isMarried())
		{
			activeChar.sendMessage("Player already married.");
			return false;
		}
		
		if (ptarget.isEngageRequest())
		{
			activeChar.sendMessage("Player already asked by someone else.");
			return false;
		}
		
		if (ptarget.getPartnerId() != 0)
		{
			activeChar.sendMessage("Player already engaged with someone else.");
			return false;
		}
		
		if (ptarget.getAppearance().getSex() == activeChar.getAppearance().getSex() && !Config.L2JMOD_WEDDING_SAMESEX)
		{
			activeChar.sendMessage("Gay marriage is not allowed on this server!");
			return false;
		}
		
		if (!activeChar.getFriendList().contains(ptarget.getName()))
		{
			activeChar.sendMessage("The player you want to ask is not on your friends list, you must first be on each others friends list before you choose to engage.");
			return false;
		}
		
		ptarget.setEngageRequest(true, activeChar.getObjectId());
		ConfirmDlg dlg = new ConfirmDlg(614);
		dlg.addString(activeChar.getName() + " asking you to engage. Do you want to start a new relationship?");
		ptarget.sendPacket(dlg);
		dlg = null;
		ptarget = null;
		
		return true;
	}
	
	public boolean goToLove(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!activeChar.isMarried())
		{
			activeChar.sendMessage("You're not married.");
			return false;
		}
		else if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("You can not teleport to your partner while participating in Olympiad.");
			return false;
		}
		else if (activeChar.inObserverMode())
		{
			activeChar.sendMessage("You can not teleport to your partner while observe.");
			return false;
		}
		else if (activeChar.isInFunEvent())
		{
			activeChar.sendMessage("You're partner is in a Fun Event.");
			return false;
		}
		else if (GrandBossManager.getInstance().getZone(activeChar) != null)
		{
			activeChar.sendMessage("You're partner is in a Grand boss zone.");
			return false;
		}
		else if (activeChar.isInJail())
		{
			activeChar.sendMessage("You are in Jail!");
			return false;
		}
		else if (activeChar.isDead())
		{
			activeChar.sendMessage("You are dead.");
			return false;
		}
		else if (activeChar.isAlikeDead())
		{
			activeChar.sendMessage("You looks like dead.");
			return false;
		}
		else if (activeChar.isInDuel())
		{
			activeChar.sendMessage("You are in a duel!");
			return false;
		}
		else if (activeChar.getClan() != null && CastleManager.getInstance().getCastleByOwner(activeChar.getClan()) != null && CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).getSiege().getIsInProgress())
		{
			activeChar.sendMessage("You are in siege, you can't go to your partner.");
			return false;
		}
		else if (activeChar.isFestivalParticipant())
		{
			activeChar.sendMessage("You are in a festival.");
			return false;
		}
		else if (activeChar.isInParty() && activeChar.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage("You are in the dimensional rift.");
			return false;
		}
		else if (activeChar.isCursedWeaponEquiped())
		{
			activeChar.sendMessage("You have a cursed weapon, you can't go to your partner.");
			return false;
		}
		else if (activeChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
		{
			activeChar.sendMessage("You are in area which blocks summoning.");
			return false;
		}
		else if (activeChar.getPartnerId() == 0)
		{
			activeChar.sendMessage("Something went wrong, please report to Game Master, error code: 4004");
			LOGGER.error("Error 4004: Married but couldn't find parter for " + activeChar.getName());
			return false;
		}
		
		L2PcInstance partner;
		partner = (L2PcInstance) L2World.getInstance().findObject(activeChar.getPartnerId());
		
		if (partner == null)
		{
			return false;
		}
		
		if (!partner.isMarried())
		{
			activeChar.sendMessage("Your partner is not married.");
			return false;
		}
		else if (partner.isInOlympiadMode())
		{
			activeChar.sendMessage("You can not teleport to your partner while participating in Olympiad.");
			return false;
		}
		else if (partner.inObserverMode())
		{
			activeChar.sendMessage("You can not teleport to your partner while observe.");
			return false;
		}
		else if (partner.isInFunEvent())
		{
			activeChar.sendMessage("Your partner is in a Fun Event.");
			return false;
		}
		else if (GrandBossManager.getInstance().getZone(partner) != null)
		{
			activeChar.sendMessage("Your partner is in a Grand boss zone.");
			return false;
		}
		else if (partner.isInJail())
		{
			activeChar.sendMessage("Your partner is in Jail!");
			return false;
		}
		else if (partner.isDead())
		{
			activeChar.sendMessage("Your partner is dead.");
			return false;
		}
		else if (partner.isAlikeDead())
		{
			activeChar.sendMessage("Your partner looks like dead.");
			return false;
		}
		else if (partner.isInDuel())
		{
			activeChar.sendMessage("Your partner is in a duel!");
			return false;
		}
		else if (partner.getClan() != null && CastleManager.getInstance().getCastleByOwner(partner.getClan()) != null && CastleManager.getInstance().getCastleByOwner(partner.getClan()).getSiege().getIsInProgress())
		{
			activeChar.sendMessage("Your partner is in siege.");
			return false;
		}
		else if (partner.isFestivalParticipant())
		{
			activeChar.sendMessage("Your partner in a festival.");
			return false;
		}
		else if (partner.isInParty() && partner.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage("Your partner is in the dimensional rift.");
			return false;
		}
		else if (partner.isCursedWeaponEquiped())
		{
			activeChar.sendMessage("Your partner have a cursed weapon.");
			return false;
		}
		else if (partner.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
		{
			activeChar.sendMessage("Your partner is in area which blocks summoning.");
			return false;
		}
		
		final int teleportTimer = Config.L2JMOD_WEDDING_TELEPORT_DURATION * 1000;
		
		activeChar.sendMessage("After " + teleportTimer / 60000 + " min. you will be teleported to your fiance.");
		activeChar.getInventory().reduceAdena("Wedding", Config.L2JMOD_WEDDING_TELEPORT_PRICE, activeChar, null);
		
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		// SoE Animation section
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills();
		
		MagicSkillUser msk = new MagicSkillUser(activeChar, 1050, 1, teleportTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 810000/* 900 */);
		SetupGauge sg = new SetupGauge(0, teleportTimer);
		activeChar.sendPacket(sg);
		// End SoE Animation section
		
		EscapeFinalizer ef = new EscapeFinalizer(activeChar, partner.getX(), partner.getY(), partner.getZ(), partner.isIn7sDungeon());
		// continue execution later
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, teleportTimer));
		activeChar.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + teleportTimer / GameTimeController.MILLIS_IN_TICK);
		
		return true;
	}
	
	public static class EscapeFinalizer implements Runnable
	{
		private final L2PcInstance activeChar;
		private final int partnerx;
		private final int partnery;
		private final int partnerz;
		private final boolean to7sDungeon;
		
		EscapeFinalizer(final L2PcInstance activeChar, final int x, final int y, final int z, final boolean to7sDungeon)
		{
			this.activeChar = activeChar;
			partnerx = x;
			partnery = y;
			partnerz = z;
			this.to7sDungeon = to7sDungeon;
		}
		
		@Override
		public void run()
		{
			if (activeChar.isDead())
			{
				return;
			}
			
			activeChar.setIsIn7sDungeon(to7sDungeon);
			activeChar.enableAllSkills();
			
			try
			{
				activeChar.teleToLocation(partnerx, partnery, partnerz);
			}
			catch (Throwable e)
			{
				LOGGER.error("WeddingCmd.EscapeFinalizer.run : Error in Thread run process. ", e);
			}
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return voicedCommands;
	}
}
