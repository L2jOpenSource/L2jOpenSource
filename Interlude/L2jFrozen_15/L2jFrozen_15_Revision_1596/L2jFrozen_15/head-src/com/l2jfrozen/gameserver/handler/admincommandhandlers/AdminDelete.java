package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.managers.RaidBossSpawnManager;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - delete = deletes target
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/04/11 10:05:56 $
 */
public class AdminDelete implements IAdminCommandHandler
{
	// private static Logger LOGGER = Logger.getLogger(AdminDelete.class);
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_delete"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.equals("admin_delete"))
		{
			handleDelete(activeChar);
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleDelete(final L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		
		// Admin can't delete Players --> L2PcInstances
		if (obj instanceof L2PcInstance)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		if (obj != null && obj instanceof L2NpcInstance)
		{
			final L2NpcInstance target = (L2NpcInstance) obj;
			target.deleteMe();
			
			L2Spawn spawn = target.getSpawn();
			if (spawn != null)
			{
				spawn.stopRespawn();
				
				if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcid()) && !spawn.isCustomRaidBoss())
				{
					RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
				}
				else
				{
					boolean update_db = true;
					
					// if custom grandboss instance, it's not saved on database
					if (GrandBossManager.getInstance().isDefined(spawn.getNpcid()) && spawn.isCustomRaidBoss())
					{
						update_db = false;
					}
					
					SpawnTable.getInstance().deleteSpawn(spawn, update_db);
				}
			}
			
			spawn = null;
			obj = null;
			
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Deleted " + target.getName() + " from " + target.getObjectId() + ".");
			activeChar.sendPacket(sm);
			sm = null;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
	}
}
