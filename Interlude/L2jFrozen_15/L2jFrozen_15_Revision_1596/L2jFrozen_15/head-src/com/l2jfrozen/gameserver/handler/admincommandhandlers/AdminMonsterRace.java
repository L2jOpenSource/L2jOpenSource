package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.MonsterRace;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.DeleteObject;
import com.l2jfrozen.gameserver.network.serverpackets.MonRaceInfo;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * This class handles following admin commands: - invul = turns invulnerability on/off
 * @version $Revision: 1.1.6.4 $ $Date: 2007/07/31 10:06:00 $
 */
public class AdminMonsterRace implements IAdminCommandHandler
{
	// private static Logger LOGGER = Logger.getLogger(AdminMonsterRace.class);
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_mons"
	};
	
	protected static int state = -1;
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.equalsIgnoreCase("admin_mons"))
		{
			handleSendPacket(activeChar);
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleSendPacket(final L2PcInstance activeChar)
	{
		/*
		 * -1 0 to initialize the race 0 15322 to start race 13765 -1 in middle of race -1 0 to end the race 8003 to 8027
		 */
		
		final int[][] codes =
		{
			{
				-1,
				0
			},
			{
				0,
				15322
			},
			{
				13765,
				-1
			},
			{
				-1,
				0
			}
		};
		MonsterRace race = MonsterRace.getInstance();
		
		if (state == -1)
		{
			state++;
			race.newRace();
			race.newSpeeds();
			MonRaceInfo spk = new MonRaceInfo(codes[state][0], codes[state][1], race.getMonsters(), race.getSpeeds());
			activeChar.sendPacket(spk);
			activeChar.broadcastPacket(spk);
			spk = null;
		}
		else if (state == 0)
		{
			state++;
			
			SystemMessage sm = new SystemMessage(SystemMessageId.MONSRACE_RACE_START);
			sm.addNumber(0);
			activeChar.sendPacket(sm);
			sm = null;
			
			PlaySound SRace = new PlaySound(1, "S_Race", 0, 0, 0, 0, 0);
			activeChar.sendPacket(SRace);
			activeChar.broadcastPacket(SRace);
			SRace = null;
			
			PlaySound SRace2 = new PlaySound(0, "ItemSound2.race_start", 1, 121209259, 12125, 182487, -3559);
			activeChar.sendPacket(SRace2);
			activeChar.broadcastPacket(SRace2);
			SRace2 = null;
			
			MonRaceInfo spk = new MonRaceInfo(codes[state][0], codes[state][1], race.getMonsters(), race.getSpeeds());
			activeChar.sendPacket(spk);
			activeChar.broadcastPacket(spk);
			spk = null;
			
			ThreadPoolManager.getInstance().scheduleGeneral(new RunRace(codes, activeChar), 5000);
		}
		
		race = null;
	}
	
	class RunRace implements Runnable
	{
		
		private final int[][] codes;
		private final L2PcInstance activeChar;
		
		public RunRace(final int[][] pCodes, final L2PcInstance pActiveChar)
		{
			codes = pCodes;
			activeChar = pActiveChar;
		}
		
		@Override
		public void run()
		{
			// int[][] speeds1 = MonsterRace.getInstance().getSpeeds();
			// MonsterRace.getInstance().newSpeeds();
			// int[][] speeds2 = MonsterRace.getInstance().getSpeeds();
			/*
			 * int[] speed = new int[8]; for (int i=0; i<8; i++) { for (int j=0; j<20; j++) { //LOGGER.info("Adding "+speeds1[i][j] +" and "+ speeds2[i][j]); speed[i] += (speeds1[i][j]*1);// + (speeds2[i][j]*1); } LOGGER.info("Total speed for "+(i+1)+" = "+speed[i]); }
			 */
			
			MonRaceInfo spk = new MonRaceInfo(codes[2][0], codes[2][1], MonsterRace.getInstance().getMonsters(), MonsterRace.getInstance().getSpeeds());
			activeChar.sendPacket(spk);
			activeChar.broadcastPacket(spk);
			spk = null;
			ThreadPoolManager.getInstance().scheduleGeneral(new RunEnd(activeChar), 30000);
		}
	}
	
	class RunEnd implements Runnable
	{
		private final L2PcInstance activeChar;
		
		public RunEnd(final L2PcInstance pActiveChar)
		{
			activeChar = pActiveChar;
		}
		
		@Override
		public void run()
		{
			DeleteObject obj = null;
			
			for (int i = 0; i < 8; i++)
			{
				obj = new DeleteObject(MonsterRace.getInstance().getMonsters()[i]);
				activeChar.sendPacket(obj);
				activeChar.broadcastPacket(obj);
				obj = null;
			}
			state = -1;
		}
	}
}
