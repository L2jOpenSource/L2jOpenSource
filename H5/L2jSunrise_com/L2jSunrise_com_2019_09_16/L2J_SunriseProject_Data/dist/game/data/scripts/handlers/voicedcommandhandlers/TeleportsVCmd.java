package handlers.voicedcommandhandlers;

import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.olympiad.OlympiadManager;

import gr.sr.interf.SunriseEvents;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class TeleportsVCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"giran",
		"dion",
		"aden",
		"goddard",
		"gludio",
		"rune",
		"heine",
		"schuttgart",
		"oren"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (activeChar.getKarma() > 0)
		{
			activeChar.sendMessage("Cannot use while have karma.");
			return false;
		}
		
		if (activeChar.getPvpFlag() > 0)
		{
			activeChar.sendMessage("Cannot use while have pvp flag.");
			return false;
		}
		
		if (activeChar.isInOlympiadMode() || activeChar.inObserverMode() || OlympiadManager.getInstance().isRegistered(activeChar))
		{
			activeChar.sendMessage("Cannot use while in Olympiad.");
			return false;
		}
		
		if (SunriseEvents.isInEvent(activeChar) || SunriseEvents.isRegistered(activeChar))
		{
			activeChar.sendMessage("Cannot use while in event.");
			return false;
		}
		
		if (activeChar.isJailed())
		{
			activeChar.sendMessage("Cannot use while in jail.");
			return false;
		}
		
		if (activeChar.isAlikeDead())
		{
			activeChar.sendMessage("Cannot use while in fake death mode.");
			return false;
		}
		
		if (!activeChar.isInsideZone(ZoneIdType.PEACE) && activeChar.isInCombat())
		{
			activeChar.sendMessage("Cannot use while in combat outside of peace zone.");
			return false;
		}
		
		Location loc = null;
		switch (command)
		{
			case "giran":
				loc = new Location(83473, 148554, -3400);
				break;
			case "dion":
				loc = new Location(15619, 143132, -2705);
				break;
			case "aden":
				loc = new Location(147974, 26883, -2200);
				break;
			case "gludio":
				loc = new Location(-14413, 123044, -3112);
				break;
			case "rune":
				loc = new Location(43759, -48122, -792);
				break;
			case "heine":
				loc = new Location(111381, 218981, -3538);
				break;
			case "goddard":
				loc = new Location(147732, -56554, -2776);
				break;
			case "schuttgart":
				loc = new Location(87355, -142095, -1336);
				break;
			case "oren":
				loc = new Location(82760, 53578, -1491);
				break;
		}
		
		activeChar.teleToLocation(loc, false);
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}