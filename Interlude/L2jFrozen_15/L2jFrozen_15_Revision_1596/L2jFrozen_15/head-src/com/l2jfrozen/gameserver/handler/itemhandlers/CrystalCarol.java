package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;

/**
 * This class ...
 * @version $Revision: 1.2.4.4 $ $Date: 2005/03/27 15:30:07 $
 */

public class CrystalCarol implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5562,
		5563,
		5564,
		5565,
		5566,
		5583,
		5584,
		5585,
		5586,
		5587,
		4411,
		4412,
		4413,
		4414,
		4415,
		4416,
		4417,
		5010,
		6903,
		7061,
		7062,
		8555
	};
	
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		final int itemId = item.getItemId();
		
		if (itemId == 5562)
		{ // crystal_carol_01
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2140, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_carol_01");
		}
		else if (itemId == 5563)
		{ // crystal_carol_02
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2141, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_carol_02");
		}
		else if (itemId == 5564)
		{ // crystal_carol_03
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2142, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_carol_03");
		}
		else if (itemId == 5565)
		{ // crystal_carol_04
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2143, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_carol_04");
		}
		else if (itemId == 5566)
		{ // crystal_carol_05
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2144, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_carol_05");
		}
		else if (itemId == 5583)
		{ // crystal_carol_06
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2145, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_carol_06");
		}
		else if (itemId == 5584)
		{ // crystal_carol_07
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2146, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_carol_07");
		}
		else if (itemId == 5585)
		{ // crystal_carol_08
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2147, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_carol_08");
		}
		else if (itemId == 5586)
		{ // crystal_carol_09
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2148, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_carol_09");
		}
		else if (itemId == 5587)
		{ // crystal_carol_10
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2149, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_carol_10");
		}
		else if (itemId == 4411)
		{ // crystal_journey
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2069, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_journey");
		}
		else if (itemId == 4412)
		{ // crystal_battle
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2068, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_battle");
		}
		else if (itemId == 4413)
		{ // crystal_love
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2070, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_love");
		}
		else if (itemId == 4414)
		{ // crystal_solitude
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2072, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_solitude");
		}
		else if (itemId == 4415)
		{ // crystal_festival
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2071, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_festival");
		}
		else if (itemId == 4416)
		{ // crystal_celebration
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2073, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_celebration");
		}
		else if (itemId == 4417)
		{ // crystal_comedy
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2067, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_comedy");
		}
		else if (itemId == 5010)
		{ // crystal_victory
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2066, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_victory");
		}
		else if (itemId == 6903)
		{ // music_box_m
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2187, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"EtcSound.battle");
		}
		else if (itemId == 7061)
		{ // crystal_birthday
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2073, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound2.crystal_celebration");
		}
		else if (itemId == 7062)
		{ // crystal_wedding
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2230, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"SkillSound5.wedding");
		}
		else if (itemId == 8555)
		{ // VVKorea
			final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2272, 1, 1, 0);
			activeChar.broadcastPacket(MSU);
			// playCrystalSound(activeChar,"EtcSound.VVKorea");
		}
		activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
		
		activeChar = null;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
