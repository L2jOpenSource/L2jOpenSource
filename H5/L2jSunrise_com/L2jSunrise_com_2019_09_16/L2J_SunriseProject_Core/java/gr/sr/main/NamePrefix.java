package gr.sr.main;

import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class NamePrefix
{
	private NamePrefix()
	{
		// Dummy default
	}
	
	public static void namePrefixCategories(L2PcInstance player, int prefixId)
	{
		switch (prefixId)
		{
			case 0:
				player.setVar("namePrefix", "");
				break;
			case 1:
				player.setVar("namePrefix", "[GM]");
				break;
			case 2:
				player.setVar("namePrefix", "[HGM]");
				break;
			case 3:
				player.setVar("namePrefix", "[ADM]");
				break;
			case 4:
				player.setVar("namePrefix", "[DEV]");
				break;
		}
	}
}