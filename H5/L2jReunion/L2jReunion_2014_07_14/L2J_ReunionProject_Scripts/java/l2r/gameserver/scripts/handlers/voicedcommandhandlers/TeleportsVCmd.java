package l2r.gameserver.scripts.handlers.voicedcommandhandlers;

import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.olympiad.OlympiadManager;

/**
 * @author -=DoctorNo=-
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
		Location loc = null;
		
		if (command.equalsIgnoreCase("giran"))
			loc = new Location(83473, 148554, -3400);
		if (command.equalsIgnoreCase("dion"))
			loc = new Location(15619, 143132, -2705);
		if (command.equalsIgnoreCase("aden"))
			loc = new Location(147974, 26883, -2200);
		if (command.equalsIgnoreCase("gludio"))
			loc = new Location(-14413, 123044, -3112);
		if (command.equalsIgnoreCase("rune"))
			loc = new Location(43759, -48122, -792);
		if (command.equalsIgnoreCase("heine"))
			loc = new Location(111381, 218981, -3538);
		if (command.equalsIgnoreCase("goddard"))
			loc = new Location(147732, -56554, -2776);
		if (command.equalsIgnoreCase("schuttgart"))
			loc = new Location(87355, -142095, -1336);
		if (command.equalsIgnoreCase("oren"))
			loc = new Location(82760, 53578, -1491);
		
		if (!activeChar.isInsideZone(ZoneIdType.PEACE))
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
			if (activeChar.isInCombat())
			{
				activeChar.sendMessage("Cannot use while in combat.");
				return false;
			}
			if (activeChar.isInOlympiadMode() || activeChar.inObserverMode() || OlympiadManager.getInstance().isRegistered(activeChar))
			{
				activeChar.sendMessage("Cannot use while in Olympiad.");
				return false;
			}
		}
		
		if (loc != null)
			activeChar.teleToLocation(loc, false);
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}