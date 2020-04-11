
package com.l2jfrozen.gameserver.handler.usercommandhandlers;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.handler.IUserCommandHandler;
import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.Ride;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.Broadcast;

/**
 * Support for /mount command.
 * @author Tempy
 */
public class Mount implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		61
	};
	
	@Override
	public synchronized boolean useUserCommand(final int id, final L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0])
		{
			return false;
		}
		
		L2Summon pet = activeChar.getPet();
		
		if (pet != null && pet.isMountable() && !activeChar.isMounted())
		{
			if (activeChar.isDead())
			{
				// A strider cannot be ridden when player is dead.
				SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
				activeChar.sendPacket(msg);
				msg = null;
			}
			else if (pet.isDead())
			{
				// A dead strider cannot be ridden.
				SystemMessage msg = new SystemMessage(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
				activeChar.sendPacket(msg);
				msg = null;
			}
			else if (pet.isInCombat())
			{
				// A strider in battle cannot be ridden.
				SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
				activeChar.sendPacket(msg);
				msg = null;
			}
			else if (activeChar.isInCombat())
			{
				// A pet cannot be ridden while player is in battle.
				SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
				activeChar.sendPacket(msg);
				msg = null;
			}
			else if (!activeChar.isInsideRadius(pet, 60, true, false))
			{
				activeChar.sendMessage("Too far away from strider to mount.");
				return false;
			}
			else if (!GeoData.getInstance().canSeeTarget(activeChar, pet))
			{
				final SystemMessage msg = new SystemMessage(SystemMessageId.CANT_SEE_TARGET);
				activeChar.sendPacket(msg);
				return false;
			}
			else if (activeChar.isSitting() || activeChar.isMoving())
			{
				// A strider can be ridden only when player is standing.
				SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
				activeChar.sendPacket(msg);
				msg = null;
			}
			else if (!pet.isDead() && !activeChar.isMounted())
			{
				if (!activeChar.disarmWeapons())
				{
					return false;
				}
				
				Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, pet.getTemplate().npcId);
				Broadcast.toSelfAndKnownPlayersInRadius(activeChar, mount, 810000/* 900 */);
				activeChar.setMountType(mount.getMountType());
				activeChar.setMountObjectID(pet.getControlItemId());
				pet.unSummon(activeChar);
				mount = null;
				
				if (activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND) != null || activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND) != null)
				{
					if (activeChar.setMountType(0))
					{
						if (activeChar.isFlying())
						{
							activeChar.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
						}
						
						Ride dismount = new Ride(activeChar.getObjectId(), Ride.ACTION_DISMOUNT, 0);
						Broadcast.toSelfAndKnownPlayers(activeChar, dismount);
						activeChar.setMountObjectID(0);
						dismount = null;
					}
				}
			}
		}
		else if (activeChar.isRentedPet())
		{
			activeChar.stopRentPet();
		}
		else if (activeChar.isMounted())
		{
			// Dismount
			if (activeChar.setMountType(0))
			{
				if (activeChar.isFlying())
				{
					activeChar.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
				}
				
				Ride dismount = new Ride(activeChar.getObjectId(), Ride.ACTION_DISMOUNT, 0);
				Broadcast.toSelfAndKnownPlayers(activeChar, dismount);
				activeChar.setMountObjectID(0);
				dismount = null;
			}
		}
		
		pet = null;
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
