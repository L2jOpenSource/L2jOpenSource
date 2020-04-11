package com.l2jfrozen.gameserver.util;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author luisantonioa
 */
public final class IllegalPlayerAction implements Runnable
{
	private static Logger logAudit = Logger.getLogger("audit");
	
	private final String message;
	private final int punishment;
	private final L2PcInstance playerActor;
	
	public static final int PUNISH_BROADCAST = 1;
	public static final int PUNISH_KICK = 2;
	public static final int PUNISH_KICKBAN = 3;
	public static final int PUNISH_JAIL = 4;
	
	public IllegalPlayerAction(final L2PcInstance actor, final String message, final int punishment)
	{
		this.message = message;
		this.punishment = punishment;
		playerActor = actor;
		
		switch (punishment)
		{
			case PUNISH_KICK:
				playerActor.sendMessage("You will be kicked for illegal action, GM informed.");
				break;
			case PUNISH_KICKBAN:
				playerActor.setAccessLevel(-100);
				playerActor.setAccountAccesslevel(-100);
				playerActor.sendMessage("You are banned for illegal action, GM informed.");
				break;
			case PUNISH_JAIL:
				playerActor.sendMessage("Illegal action performed!");
				playerActor.sendMessage("You will be teleported to GM Consultation Service area and jailed.");
				break;
		}
	}
	
	@Override
	public void run()
	{
		final LogRecord record = new LogRecord(Level.INFO, "AUDIT:" + message);
		record.setLoggerName("audit");
		record.setParameters(new Object[]
		{
			playerActor,
			punishment
		});
		logAudit.log(record);
		
		GmListTable.broadcastMessageToGMs(message);
		
		switch (punishment)
		{
			case PUNISH_BROADCAST:
				return;
			case PUNISH_KICK:
				playerActor.logout(true);
				break;
			case PUNISH_KICKBAN:
				playerActor.logout(true);
				break;
			case PUNISH_JAIL:
				playerActor.setPunishLevel(L2PcInstance.PunishLevel.JAIL, Config.DEFAULT_PUNISH_PARAM);
				break;
		}
	}
}
