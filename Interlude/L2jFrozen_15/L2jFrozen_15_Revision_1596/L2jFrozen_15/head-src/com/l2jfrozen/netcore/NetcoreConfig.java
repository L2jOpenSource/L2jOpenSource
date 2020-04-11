package com.l2jfrozen.netcore;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class NetcoreConfig
{
	public boolean PACKET_HANDLER_DEBUG;
	
	/** MMO settings */
	public int MMO_SELECTOR_SLEEP_TIME = 20; // default 20
	public int MMO_MAX_SEND_PER_PASS = 12; // default 12
	public int MMO_MAX_READ_PER_PASS = 12; // default 12
	public int MMO_HELPER_BUFFER_COUNT = 20; // default 20
	
	public boolean ENABLE_MMOCORE_DEBUG = false;
	
	/** Client Packets Queue settings */
	public static boolean ENABLE_CLIENT_FLOOD_PROTECTION;
	public int CLIENT_PACKET_QUEUE_SIZE; // default MMO_MAX_READ_PER_PASS + 2
	public int CLIENT_PACKET_QUEUE_MAX_BURST_SIZE; // default MMO_MAX_READ_PER_PASS + 1
	public int CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND; // default 80
	public int CLIENT_PACKET_QUEUE_MEASURE_INTERVAL; // default 5
	public int CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND; // default 40
	public int CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN; // default 2
	public int CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN; // default 1
	public int CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN; // default 1
	public int CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN; // default 5
	
	// Packets flooding Config
	public boolean DISABLE_FULL_PACKETS_FLOOD_PROTECTOR;
	public int FLOOD_PACKET_PROTECTION_INTERVAL;
	public boolean LOG_PACKET_FLOODING;
	public int PACKET_FLOODING_PUNISHMENT_LIMIT;
	public String PACKET_FLOODING_PUNISHMENT_TYPE;
	
	public String PROTECTED_OPCODES;
	public List<Integer> GS_LIST_PROTECTED_OPCODES = new ArrayList<>();
	public List<Integer> GS_LIST_PROTECTED_OPCODES2 = new ArrayList<>();
	public List<Integer> LS_LIST_PROTECTED_OPCODES = new ArrayList<>();
	
	public String ALLOWED_OFFLINE_OPCODES;
	public List<Integer> LIST_ALLOWED_OFFLINE_OPCODES = new ArrayList<>();
	public List<Integer> LIST_ALLOWED_OFFLINE_OPCODES2 = new ArrayList<>();
	
	public boolean DUMP_CLOSE_CONNECTIONS;
	
	// ============================================================
	private static NetcoreConfig instance;
	
	public static NetcoreConfig getInstance()
	{
		if (instance == null)
		{
			instance = new NetcoreConfig();
		}
		return instance;
	}
	
	private NetcoreConfig()
	{
		final String MMO_CONFIG = "./config/protected/mmocore.properties";
		
		try
		{
			final Properties mmoSetting = new Properties();
			final InputStream is = new FileInputStream(new File(MMO_CONFIG));
			mmoSetting.load(is);
			is.close();
			
			ENABLE_MMOCORE_DEBUG = Boolean.parseBoolean(mmoSetting.getProperty("EnableMMOCoreDebug", "False"));
			PACKET_HANDLER_DEBUG = Boolean.parseBoolean(mmoSetting.getProperty("PacketHandlerDebug", "False"));
			
			// flooding protection
			DISABLE_FULL_PACKETS_FLOOD_PROTECTOR = Boolean.parseBoolean(mmoSetting.getProperty("DisableOpCodesFloodProtector", "false"));
			FLOOD_PACKET_PROTECTION_INTERVAL = Integer.parseInt(mmoSetting.getProperty("FloodPacketProtectionInterval", "1"));
			LOG_PACKET_FLOODING = Boolean.parseBoolean(mmoSetting.getProperty("LogPacketFlooding", "false"));
			PACKET_FLOODING_PUNISHMENT_LIMIT = Integer.parseInt(mmoSetting.getProperty("PacketFloodingPunishmentLimit", "15"));
			PACKET_FLOODING_PUNISHMENT_TYPE = mmoSetting.getProperty("PacketFloodingPunishmentType", "kick");
			
			// CLIENT-QUEUE-SETTINGS
			ENABLE_CLIENT_FLOOD_PROTECTION = Boolean.parseBoolean(mmoSetting.getProperty("EnableClientFloodProtection", "true"));
			CLIENT_PACKET_QUEUE_SIZE = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueSize", "14")); // default MMO_MAX_READ_PER_PASS + 2
			CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueMaxBurstSize", "13")); // default MMO_MAX_READ_PER_PASS + 1
			CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueMaxPacketsPerSecond", "80")); // default 80
			CLIENT_PACKET_QUEUE_MEASURE_INTERVAL = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueMeasureInterval", "5")); // default 5
			CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueMaxAveragePacketsPerSecond", "40")); // default 40
			CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueMaxFloodPerMin", "6")); // default 6
			CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueOverflowsPerMin", "3")); // default 3
			CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueUnderflowsPerMin", "3")); // default 3
			CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueUnknownPerMin", "5")); // default 5
			
			// OPCODES Flood Protector
			PROTECTED_OPCODES = mmoSetting.getProperty("ListOfProtectedOpCodes");
			
			LS_LIST_PROTECTED_OPCODES = new ArrayList<>();
			GS_LIST_PROTECTED_OPCODES = new ArrayList<>();
			GS_LIST_PROTECTED_OPCODES2 = new ArrayList<>();
			
			if (PROTECTED_OPCODES != null && !PROTECTED_OPCODES.equals(""))
			{
				
				final StringTokenizer st = new StringTokenizer(PROTECTED_OPCODES, ";");
				
				while (st.hasMoreTokens())
				{
					
					final String token = st.nextToken();
					
					String[] token_splitted = null;
					
					if (token != null && !token.equals(""))
					{
						token_splitted = token.split(",");
					}
					else
					{
						continue;
					}
					
					if (token_splitted == null || token_splitted.length < 2)
					{
						continue;
					}
					
					final String server = token_splitted[0];
					
					if (server.equalsIgnoreCase("g"))
					{ // gameserver opcode
						
						final String opcode1 = token_splitted[1].substring(2);
						String opcode2 = "";
						
						if (token_splitted.length == 3 && opcode1.equals("0xd0"))
						{
							opcode2 = token_splitted[2].substring(2);
						}
						
						if (opcode1 != null && !opcode1.equals(""))
						{
							GS_LIST_PROTECTED_OPCODES.add(Integer.parseInt(opcode1, 16));
						}
						
						if (opcode2 != null && !opcode2.equals(""))
						{
							GS_LIST_PROTECTED_OPCODES2.add(Integer.parseInt(opcode2, 16));
						}
						
					}
					else if (server.equalsIgnoreCase("l"))
					{ // login opcode
						
						LS_LIST_PROTECTED_OPCODES.add(Integer.parseInt(token_splitted[1].substring(2), 16));
						
					}
					
				}
				
			}
			
			// OPCODES Offline Protection
			ALLOWED_OFFLINE_OPCODES = mmoSetting.getProperty("ListOfAllowedOfflineOpCodes", "0x03;0x9d;0xd0,0x08;0x13;0x81;");
			
			LIST_ALLOWED_OFFLINE_OPCODES = new ArrayList<>();
			LIST_ALLOWED_OFFLINE_OPCODES2 = new ArrayList<>();
			
			if (ALLOWED_OFFLINE_OPCODES != null && !ALLOWED_OFFLINE_OPCODES.equals(""))
			{
				
				final StringTokenizer st = new StringTokenizer(ALLOWED_OFFLINE_OPCODES, ";");
				
				while (st.hasMoreTokens())
				{
					
					final String token = st.nextToken();
					
					String[] token_splitted = null;
					
					if (token != null && !token.equals(""))
					{
						token_splitted = token.split(",");
					}
					else
					{
						continue;
					}
					
					if (token_splitted == null || token_splitted.length == 0 || token_splitted[0].length() <= 3)
					{
						continue;
					}
					
					final String opcode1 = token_splitted[0].substring(2);
					
					if (opcode1 != null && !opcode1.equals(""))
					{
						LIST_ALLOWED_OFFLINE_OPCODES.add(Integer.parseInt(opcode1, 16));
						if (token_splitted.length > 1 && opcode1.equals("d0"))
						{
							for (int i = 1; i < token_splitted.length; i++)
							{
								if (token_splitted[i].length() <= 3)
								{
									break;
								}
								
								final String opcode2 = token_splitted[i].substring(2);
								if (opcode2 != null && !opcode2.equals(""))
								{
									LIST_ALLOWED_OFFLINE_OPCODES2.add(Integer.parseInt(opcode2, 16));
								}
							}
						}
					}
				}
				
			}
			
			DUMP_CLOSE_CONNECTIONS = Boolean.parseBoolean(mmoSetting.getProperty("DumpCloseConnectionLogs", "false"));
			
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + MMO_CONFIG + " File.");
		}
	}
}