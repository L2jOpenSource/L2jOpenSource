package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

public class ChristmasTree implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5560, /* x-mas tree */
		5561
		/* Special x-mas tree */
	};
	
	private static final int[] NPC_IDS =
	{
		13006, /* Christmas tree w. flashing lights and snow */
		13007
	};
	
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		L2PcInstance activeChar = (L2PcInstance) playable;
		
		L2NpcTemplate template1 = null;
		
		final int itemId = item.getItemId();
		for (int i = 0; i < ITEM_IDS.length; i++)
		{
			if (ITEM_IDS[i] == itemId)
			{
				template1 = NpcTable.getInstance().getTemplate(NPC_IDS[i]);
				break;
			}
		}
		
		if (template1 == null)
		{
			return;
		}
		
		L2Object target = activeChar.getTarget();
		if (target == null)
		{
			target = activeChar;
		}
		
		try
		{
			L2Spawn spawn = new L2Spawn(template1);
			spawn.setId(IdFactory.getInstance().getNextId());
			spawn.setLocx(target.getX());
			spawn.setLocy(target.getY());
			spawn.setLocz(target.getZ());
			final L2NpcInstance result = spawn.spawnOne();
			
			activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
			
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			activeChar.sendPacket(sm);
			
			ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawn(result), 3600000);
			
			sm = null;
			spawn = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Target is not ingame.");
			activeChar.sendPacket(sm);
			sm = null;
		}
		
		activeChar = null;
		template1 = null;
		target = null;
	}
	
	public class DeSpawn implements Runnable
	{
		L2NpcInstance npc = null;
		
		public DeSpawn(final L2NpcInstance npc)
		{
			this.npc = npc;
		}
		
		@Override
		public void run()
		{
			npc.onDecay();
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
