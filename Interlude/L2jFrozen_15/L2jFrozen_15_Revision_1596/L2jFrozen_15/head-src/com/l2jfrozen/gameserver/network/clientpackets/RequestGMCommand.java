package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.GMViewCharacterInfo;
import com.l2jfrozen.gameserver.network.serverpackets.GMViewItemList;
import com.l2jfrozen.gameserver.network.serverpackets.GMViewPledgeInfo;
import com.l2jfrozen.gameserver.network.serverpackets.GMViewQuestList;
import com.l2jfrozen.gameserver.network.serverpackets.GMViewSkillInfo;
import com.l2jfrozen.gameserver.network.serverpackets.GMViewWarehouseWithdrawList;

public final class RequestGMCommand extends L2GameClientPacket
{
	static Logger LOGGER = Logger.getLogger(RequestGMCommand.class);
	
	private String targetName;
	private int command;
	
	@Override
	protected void readImpl()
	{
		targetName = readS();
		command = readD();
		// unknown = readD();
	}
	
	@Override
	protected void runImpl()
	{
		
		final L2PcInstance player = L2World.getInstance().getPlayer(targetName);
		
		// prevent non gm or low level GMs from vieweing player stuff
		if (player == null || !getClient().getActiveChar().getAccessLevel().allowAltG())
		{
			return;
		}
		
		switch (command)
		{
			case 1: // player status
			{
				sendPacket(new GMViewCharacterInfo(player));
				break;
			}
			case 2: // player clan
			{
				if (player.getClan() != null)
				{
					sendPacket(new GMViewPledgeInfo(player.getClan(), player));
				}
				break;
			}
			case 3: // player skills
			{
				sendPacket(new GMViewSkillInfo(player));
				break;
			}
			case 4: // player quests
			{
				sendPacket(new GMViewQuestList(player));
				break;
			}
			case 5: // player inventory
			{
				sendPacket(new GMViewItemList(player));
				break;
			}
			case 6: // player warehouse
			{
				// gm warehouse view to be implemented
				sendPacket(new GMViewWarehouseWithdrawList(player));
				break;
			}
			
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 6e RequestGMCommand";
	}
}
