package gr.sr.spreeEngine;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.util.Broadcast;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class SpreeHandler
{
	public SpreeHandler()
	{
		// Dummy default
	}
	
	public void spreeSystem(L2PcInstance player, int spreeKills)
	{
		ExShowScreenMessage msgCase = null;
		String announceMessage = null;
		
		switch (spreeKills)
		{
			case 1:
				break;
			case 2:
				break;
			case 3:
				msgCase = new ExShowScreenMessage("You've reached 3 killing spree!", 4000);
				announceMessage = "just got a Triple Kill!";
				break;
			case 4:
				msgCase = new ExShowScreenMessage("You've reached 4 killing spree!", 4000);
				announceMessage = "just got a Mega Kill!";
				break;
			case 5:
				msgCase = new ExShowScreenMessage("You've reached 5 killing spree!", 4000);
				announceMessage = "just got an Ultra Kill!";
				break;
			case 8:
				msgCase = new ExShowScreenMessage("You've reached 8 killing spree!", 4000);
				announceMessage = "is Unstoppable!";
				break;
			case 10:
				msgCase = new ExShowScreenMessage("You've reached 10 killing spree!", 4000);
				announceMessage = "just got a Monster Kill!";
				break;
			case 13:
				msgCase = new ExShowScreenMessage("You've reached 13 killing spree!", 4000);
				announceMessage = "is WhickedSick!";
				break;
			case 15:
				msgCase = new ExShowScreenMessage("You've reached 15 killing spree!", 4000);
				announceMessage = "is on a KILLING SPREE!!!";
				break;
			case 20:
				msgCase = new ExShowScreenMessage("You've reached 20 killing spree!", 4000);
				announceMessage = "is !!Dominating!!";
				break;
			case 25:
				msgCase = new ExShowScreenMessage("You've reached 25 killing spree!", 4000);
				announceMessage = "is !!!GODLIKE!!!";
				break;
			case 30:
				msgCase = new ExShowScreenMessage("You've reached MAX killing spree!", 4000);
				announceMessage = "is Beyond GODLIKE!!!";
				break;
			default:
		}
		
		if ((msgCase != null) && (announceMessage != null))
		{
			player.sendPacket(msgCase);
			Broadcast.toAllOnlinePlayers(new CreatureSay(1, Say2.CRITICAL_ANNOUNCE, "", "PvP Manager: " + player.getName() + " " + announceMessage));
		}
	}
	
	public static SpreeHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SpreeHandler _instance = new SpreeHandler();
	}
}