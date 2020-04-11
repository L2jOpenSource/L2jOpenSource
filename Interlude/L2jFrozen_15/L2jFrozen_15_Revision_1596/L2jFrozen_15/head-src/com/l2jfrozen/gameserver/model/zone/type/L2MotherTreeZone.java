package com.l2jfrozen.gameserver.model.zone.type;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.base.Race;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author durgus
 */
public class L2MotherTreeZone extends L2ZoneType
{
	public L2MotherTreeZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;
			if (player.getRace() == Race.elf)
			{
				player.setInsideZone(L2Character.ZONE_MOTHERTREE, true);
				player.sendPacket(new SystemMessage(SystemMessageId.ENTER_SHADOW_MOTHER_TREE));
			}
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;
			if (player.getRace() == Race.elf)
			{
				player.setInsideZone(L2Character.ZONE_MOTHERTREE, false);
				player.sendPacket(new SystemMessage(SystemMessageId.EXIT_SHADOW_MOTHER_TREE));
			}
		}
	}
	
	@Override
	protected void onDieInside(L2Character character)
	{
	}
	
	@Override
	protected void onReviveInside(L2Character character)
	{
	}
}
