package com.l2jfrozen.gameserver.handler.voicedcommandhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.managers.AwayManager;
import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Siege;

/**
 * @author Michiru
 */
public class AwayCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"away",
		"back"
	};
	
	@Override
	public boolean useVoicedCommand(final String command, final L2PcInstance activeChar, final String text)
	{
		if (command.startsWith("away"))
		{
			return away(activeChar, text);
		}
		else if (command.startsWith("back"))
		{
			return back(activeChar);
		}
		return false;
	}
	
	private boolean away(final L2PcInstance activeChar, String text)
	{
		Siege siege = SiegeManager.getInstance().getSiege(activeChar);
		
		// check char is all ready in away mode
		if (activeChar.isAway() || activeChar.isAwaying())
		{
			activeChar.sendMessage("You are already Away.");
			return false;
		}
		
		if (!activeChar.isInsideZone(L2Character.ZONE_PEACE) && Config.AWAY_PEACE_ZONE)
		{
			activeChar.sendMessage("You can only Away in peace zone.");
			return false;
		}
		
		// check player is death/fake death and movement disable
		if (activeChar.isMovementDisabled() || activeChar.isAlikeDead())
		{
			return false;
		}
		
		// Check if player is in Siege
		if (siege != null && siege.getIsInProgress())
		{
			activeChar.sendMessage("You are in siege, you can't go Afk.");
			return false;
		}
		
		// Check if player is a Cursed Weapon owner
		if (activeChar.isCursedWeaponEquiped())
		{
			activeChar.sendMessage("You can't go Afk! You are currently holding a cursed weapon.");
			return false;
		}
		
		// Check if player is in Duel
		if (activeChar.isInDuel())
		{
			activeChar.sendMessage("You can't go Afk! You are in a duel!");
			return false;
		}
		
		// check is in DimensionsRift
		if (activeChar.isInParty() && activeChar.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage("You can't go Afk! You are in the dimensional rift.");
			return false;
		}
		
		// Check to see if the player is in an event
		if (activeChar.isInFunEvent())
		{
			activeChar.sendMessage("You can't go Afk! You are in event now.");
			return false;
		}
		
		// check player is in Olympiade
		if (activeChar.isInOlympiadMode() || activeChar.getOlympiadGameId() != -1)
		{
			activeChar.sendMessage("You can't go Afk! Your are fighting in Olympiad!");
			return false;
		}
		
		// Check player is in observer mode
		if (activeChar.inObserverMode())
		{
			activeChar.sendMessage("You can't go Afk in Observer mode!");
			return false;
		}
		
		// check player have karma/pk/pvp status
		if (activeChar.getKarma() > 0 || activeChar.getPvpFlag() > 0)
		{
			activeChar.sendMessage("Player in PVP or with Karma can't use the Away command!");
			return false;
		}
		
		if (text == null)
		{
			text = "";
		}
		
		// check away text have not more then 10 letter
		if (text.length() > 10)
		{
			activeChar.sendMessage("You can't set your status Away with more then 10 letters.");
			return false;
		}
		
		// check if player have no one in target
		if (activeChar.getTarget() == null)
		{
			// set this Player status away in AwayManager
			AwayManager.getInstance().setAway(activeChar, text);
		}
		else
		{
			activeChar.sendMessage("You can't have any one in your target.");
			return false;
		}
		
		return true;
	}
	
	private boolean back(L2PcInstance activeChar)
	{
		if (!activeChar.isAway())
		{
			activeChar.sendMessage("You are not Away!");
			return false;
		}
		
		AwayManager.getInstance().setBack(activeChar);
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
