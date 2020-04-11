package com.l2jfrozen.gameserver.handler.skillhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.FortManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2ArtefactInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.entity.siege.Fort;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.Util;

/**
 * @author drunk
 */
public class TakeCastle implements ISkillHandler
{
	// private static Logger LOGGER = Logger.getLogger(TakeCastle.class);
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.TAKECASTLE
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
		
		if (castle != null && fort == null)
		{
			if (!checkIfOkToCastSealOfRule(player, castle, true))
			{
				return;
			}
			
		}
		else if (fort != null && castle == null)
		{
			if (!checkIfOkToCastFlagDisplay(player, fort, true))
			{
				return;
			}
		}
		
		if (castle == null && fort == null)
		{
			return;
		}
		
		try
		{
			if ((castle != null) && (targets[0] instanceof L2ArtefactInstance))
			{
				castle.Engrave(player.getClan(), targets[0].getObjectId());
			}
			else if (fort != null)
			{
				fort.EndOfSiege(player.getClan());
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
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
	 * @param  isCheckOnly
	 * @return
	 */
	public static boolean checkIfOkToCastSealOfRule(final L2Character activeChar, final boolean isCheckOnly)
	{
		final Castle castle = CastleManager.getInstance().getCastle(activeChar);
		final Fort fort = FortManager.getInstance().getFort(activeChar);
		
		if (fort != null && castle == null)
		{
			return checkIfOkToCastFlagDisplay(activeChar, fort, isCheckOnly);
		}
		return checkIfOkToCastSealOfRule(activeChar, castle, isCheckOnly);
	}
	
	public static boolean checkIfOkToCastSealOfRule(final L2Character activeChar, final Castle castle, final boolean isCheckOnly)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
		{
			return false;
		}
		
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
		L2PcInstance player = (L2PcInstance) activeChar;
		
		if (castle == null || castle.getCastleId() <= 0)
		{
			sm.addString("You must be on castle ground to use this skill");
		}
		else if (player.getTarget() == null || !(player.getTarget() instanceof L2ArtefactInstance))
		{
			sm.addString("You can only use this skill on an artifact");
		}
		else if (!castle.getSiege().getIsInProgress())
		{
			sm.addString("You can only use this skill during a siege.");
		}
		else if (!Util.checkIfInRange(200, player, player.getTarget(), true))
		{
			sm.addString("You are not in range of the artifact.");
		}
		else if (castle.getSiege().getAttackerClan(player.getClan()) == null)
		{
			sm.addString("You must be an attacker to use this skill");
		}
		else
		{
			if (!isCheckOnly)
			{
				castle.getSiege().announceToPlayer("Clan " + player.getClan().getName() + " has begun to engrave the ruler.", true);
			}
			sm = null;
			player = null;
			return true;
		}
		
		if (!isCheckOnly)
		{
			player.sendPacket(sm);
			sm = null;
			player = null;
		}
		
		return false;
	}
	
	public static boolean checkIfOkToCastFlagDisplay(final L2Character activeChar, final boolean isCheckOnly)
	{
		return checkIfOkToCastFlagDisplay(activeChar, FortManager.getInstance().getFort(activeChar), isCheckOnly);
	}
	
	public static boolean checkIfOkToCastFlagDisplay(final L2Character activeChar, final Fort fort, final boolean isCheckOnly)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
		{
			return false;
		}
		
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
		L2PcInstance player = (L2PcInstance) activeChar;
		
		if (fort == null || fort.getFortId() <= 0)
		{
			sm.addString("You must be on fort ground to use this skill");
		}
		else if (player.getTarget() == null && !(player.getTarget() instanceof L2ArtefactInstance))
		{
			sm.addString("You can only use this skill on an flagpole");
		}
		else if (!fort.getSiege().getIsInProgress())
		{
			sm.addString("You can only use this skill during a siege.");
		}
		else if (!Util.checkIfInRange(200, player, player.getTarget(), true))
		{
			sm.addString("You are not in range of the flagpole.");
		}
		else if (fort.getSiege().getAttackerClan(player.getClan()) == null)
		{
			sm.addString("You must be an attacker to use this skill");
		}
		else
		{
			if (!isCheckOnly)
			{
				fort.getSiege().announceToPlayer("Clan " + player.getClan().getName() + " has begun to raise flag.", true);
			}
			sm = null;
			player = null;
			return true;
		}
		
		if (!isCheckOnly)
		{
			player.sendPacket(sm);
			sm = null;
			player = null;
		}
		
		return false;
	}
}
