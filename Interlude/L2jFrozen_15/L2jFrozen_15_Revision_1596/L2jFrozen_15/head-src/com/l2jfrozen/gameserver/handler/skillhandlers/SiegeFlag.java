package com.l2jfrozen.gameserver.handler.skillhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.FortManager;
import com.l2jfrozen.gameserver.managers.FortSiegeManager;
import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.entity.siege.Fort;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author programmos TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class SiegeFlag implements ISkillHandler
{
	// private static Logger LOGGER = Logger.getLogger(SiegeFlag.class);
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.SIEGEFLAG
	};
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance player = (L2PcInstance) activeChar;
		if (player.getClan() == null || player.getClan().getLeaderId() != player.getObjectId())
		{
			return;
		}
		
		Castle castle = CastleManager.getInstance().getCastle(player);
		Fort fort = FortManager.getInstance().getFort(player);
		if ((castle == null) && (fort == null))
		{
			return;
		}
		
		if (castle != null)
		{
			if (!checkIfOkToPlaceFlag(player, castle, true))
			{
				return;
			}
		}
		else
		{
			if (!checkIfOkToPlaceFlag(player, fort, true))
			{
				return;
			}
		}
		
		try
		{
			// Spawn a new flag
			L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(35062));
			
			if (skill.is_advancedFlag())
			{
				flag.set_advanceFlag(true);
				flag.set_advanceMultiplier(skill.get_advancedMultiplier());
			}
			
			flag.setTitle(player.getClan().getName());
			flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
			flag.setHeading(player.getHeading());
			flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
			
			if (castle != null)
			{
				castle.getSiege().getFlag(player.getClan()).add(flag);
			}
			else
			{
				fort.getSiege().getFlag(player.getClan()).add(flag);
			}
			
			flag = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			player.sendMessage("Error placing flag:" + e);
		}
		
		player = null;
		castle = null;
		fort = null;
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
	
	/**
	 * Return true if character clan place a flag<BR>
	 * <BR>
	 * @param  activeChar  The L2Character of the character placing the flag
	 * @param  isCheckOnly if false, it will send a notification to the player telling him why it failed
	 * @return
	 */
	public static boolean checkIfOkToPlaceFlag(final L2Character activeChar, final boolean isCheckOnly)
	{
		final Castle castle = CastleManager.getInstance().getCastle(activeChar);
		final Fort fort = FortManager.getInstance().getFort(activeChar);
		if ((castle == null) && (fort == null))
		{
			return false;
		}
		
		if (castle != null)
		{
			return checkIfOkToPlaceFlag(activeChar, castle, isCheckOnly);
		}
		return checkIfOkToPlaceFlag(activeChar, fort, isCheckOnly);
	}
	
	public static boolean checkIfOkToPlaceFlag(final L2Character activeChar, final Castle castle, final boolean isCheckOnly)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
		{
			return false;
		}
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
		final L2PcInstance player = (L2PcInstance) activeChar;
		
		if (castle == null || castle.getCastleId() <= 0)
		{
			sm.addString("You must be on castle ground to place a flag");
		}
		else if (!castle.getSiege().getIsInProgress())
		{
			sm.addString("You can only place a flag during a siege.");
		}
		else if (castle.getSiege().getAttackerClan(player.getClan()) == null)
		{
			sm.addString("You must be an attacker to place a flag");
		}
		else if (player.getClan() == null || !player.isClanLeader())
		{
			sm.addString("You must be a clan leader to place a flag");
		}
		else if (castle.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= SiegeManager.getInstance().getFlagMaxCount())
		{
			sm.addString("You have already placed the maximum number of flags possible");
		}
		else if (player.isInsideZone(L2Character.ZONE_NOHQ))
		{
			sm.addString("You cannot place flag here.");
		}
		else
		{
			return true;
		}
		
		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}
		return false;
	}
	
	public static boolean checkIfOkToPlaceFlag(final L2Character activeChar, final Fort fort, final boolean isCheckOnly)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
		{
			return false;
		}
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
		final L2PcInstance player = (L2PcInstance) activeChar;
		
		if (fort == null || fort.getFortId() <= 0)
		{
			sm.addString("You must be on fort ground to place a flag");
		}
		else if (!fort.getSiege().getIsInProgress())
		{
			sm.addString("You can only place a flag during a siege.");
		}
		else if (fort.getSiege().getAttackerClan(player.getClan()) == null)
		{
			sm.addString("You must be an attacker to place a flag");
		}
		else if (player.getClan() == null || !player.isClanLeader())
		{
			sm.addString("You must be a clan leader to place a flag");
		}
		else if (fort.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= FortSiegeManager.getInstance().getFlagMaxCount())
		{
			sm.addString("You have already placed the maximum number of flags possible");
		}
		else if (player.isInsideZone(L2Character.ZONE_NOHQ))
		{
			sm.addString("You cannot place flag here.");
		}
		else
		{
			return true;
		}
		
		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}
		
		return false;
	}
}