package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.Ride;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author l2jfrozen.
 */
public class AdminRideWyvern implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_ride_wyvern",
		"admin_ride_strider",
		"admin_unride_wyvern",
		"admin_unride_strider",
		"admin_unride",
	};
	private int petRideId;
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.startsWith("admin_ride"))
		{
			if (activeChar.isMounted() || activeChar.getPet() != null)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Already Have a Pet or Mounted.");
				activeChar.sendPacket(sm);
				sm = null;
				
				return false;
			}
			
			if (command.startsWith("admin_ride_wyvern"))
			{
				petRideId = 12621;
				
				// Add skill Wyvern Breath
				activeChar.addSkill(SkillTable.getInstance().getInfo(4289, 1));
				activeChar.sendSkillList();
			}
			else if (command.startsWith("admin_ride_strider"))
			{
				petRideId = 12526;
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Command '" + command + "' not recognized");
				activeChar.sendPacket(sm);
				sm = null;
				
				return false;
			}
			
			if (!activeChar.disarmWeapons())
			{
				return false;
			}
			
			Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, petRideId);
			activeChar.sendPacket(mount);
			activeChar.broadcastPacket(mount);
			activeChar.setMountType(mount.getMountType());
			mount = null;
		}
		else if (command.startsWith("admin_unride"))
		{
			if (activeChar.isFlying())
			{
				// Remove skill Wyvern Breath
				activeChar.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
				activeChar.sendSkillList();
			}
			
			if (activeChar.setMountType(0))
			{
				Ride dismount = new Ride(activeChar.getObjectId(), Ride.ACTION_DISMOUNT, 0);
				activeChar.broadcastPacket(dismount);
				dismount = null;
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
}