package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.managers.PacketsLoggerManager;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public class AdminCharSupervision implements IAdminCommandHandler
{
	protected static final Logger LOGGER = Logger.getLogger(AdminCharSupervision.class);
	
	private static String[] ADMIN_COMMANDS =
	{
		"admin_start_monitor_char",
		"admin_stop_monitor_char",
		"admin_block_char_packet",
		"admin_restore_char_packet"
	};
	
	private enum CommandEnum
	{
		admin_start_monitor_char,
		admin_stop_monitor_char,
		admin_block_char_packet,
		admin_restore_char_packet
	
	}
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		
		final CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case admin_block_char_packet:
			{
				
				String val = "";
				
				if (st.hasMoreTokens())
				{
					
					while (st.hasMoreTokens())
					{
						if (val.equals(""))
						{
							val = st.nextToken();
						}
						else
						{
							val += " " + st.nextToken();
						}
					}
					
				}
				else
				{
					activeChar.sendMessage("Usage: //admin_block_char_packet <char_name> <packet_op_code1>,<packet_op_code2>");
					return false;
				}
				
				final String[] charName_packet = val.split(" ");
				
				if (charName_packet.length < 2)
				{
					activeChar.sendMessage("Usage: //admin_block_char_packet <char_name> <packet_op_code1>,<packet_op_code2>");
					return false;
				}
				
				final L2PcInstance target = L2World.getInstance().getPlayer(charName_packet[0]);
				
				if (target != null)
				{
					PacketsLoggerManager.getInstance().blockCharacterPacket(target.getName(), charName_packet[1]);
					return true;
				}
				
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CHARACTER_DOES_NOT_EXIST));
				return false;
			}
			case admin_restore_char_packet:
			{
				
				String val = "";
				
				if (st.hasMoreTokens())
				{
					
					while (st.hasMoreTokens())
					{
						if (val.equals(""))
						{
							val = st.nextToken();
						}
						else
						{
							val += " " + st.nextToken();
						}
					}
					
				}
				else
				{
					activeChar.sendMessage("Usage: //admin_restore_char_packet <char_name> <packet_op_code1>,<packet_op_code2>");
					return false;
				}
				
				final String[] charName_packet = val.split(" ");
				
				if (charName_packet.length < 2)
				{
					activeChar.sendMessage("Usage: //admin_restore_char_packet <char_name> <packet_op_code1>,<packet_op_code2>");
					return false;
				}
				
				final L2PcInstance target = L2World.getInstance().getPlayer(charName_packet[0]);
				
				if (target != null)
				{
					PacketsLoggerManager.getInstance().restoreCharacterPacket(target.getName(), charName_packet[1]);
					return true;
				}
				
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CHARACTER_DOES_NOT_EXIST));
				return false;
			}
			case admin_start_monitor_char:
			{
				
				String val = "";
				
				if (st.hasMoreTokens())
				{
					
					while (st.hasMoreTokens())
					{
						if (val.equals(""))
						{
							val = st.nextToken();
						}
						else
						{
							val += " " + st.nextToken();
						}
					}
					
				}
				else
				{
					activeChar.sendMessage("Usage: //start_monitor_char <char_name>");
					return false;
				}
				
				final L2PcInstance target = L2World.getInstance().getPlayer(val.trim());
				
				if (target != null)
				{
					PacketsLoggerManager.getInstance().startCharacterPacketsMonitoring(target.getName());
					return true;
				}
				
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CHARACTER_DOES_NOT_EXIST));
				return false;
			}
			case admin_stop_monitor_char:
			{
				
				String val = "";
				
				if (st.hasMoreTokens())
				{
					
					while (st.hasMoreTokens())
					{
						if (val.equals(""))
						{
							val = st.nextToken();
						}
						else
						{
							val += " " + st.nextToken();
						}
					}
					
				}
				else
				{
					activeChar.sendMessage("Usage: //stop_monitor_char <char_name>");
					return false;
				}
				
				final L2PcInstance target = L2World.getInstance().getPlayer(val.trim());
				
				if (target != null)
				{
					PacketsLoggerManager.getInstance().stopCharacterPacketsMonitoring(target.getName());
					return true;
				}
				
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CHARACTER_DOES_NOT_EXIST));
				return false;
				
			}
			
		}
		
		return false;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
