package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author ProGramMoS
 */
public class AdminGeodata implements IAdminCommandHandler
{
	// private static Logger LOGGER = Logger.getLogger(AdminKill.class);
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_geo_z",
		"admin_geo_type",
		"admin_geo_nswe",
		"admin_geo_los",
		"admin_geo_position",
		"admin_geo_bug",
		"admin_geo_load",
		"admin_geo_unload"
	};
	
	private enum CommandEnum
	{
		admin_geo_z,
		admin_geo_type,
		admin_geo_nswe,
		admin_geo_los,
		admin_geo_position,
		admin_geo_bug,
		admin_geo_load,
		admin_geo_unload
	}
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		String[] wordList = command.split(" ");
		CommandEnum comm;
		
		try
		{
			comm = CommandEnum.valueOf(wordList[0]);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return false;
		}
		
		CommandEnum commandEnum = comm;
		switch (commandEnum)
		{
			case admin_geo_z:
			case admin_geo_type:
			case admin_geo_nswe:
			case admin_geo_los:
			case admin_geo_position:
			case admin_geo_bug:
			case admin_geo_unload:
				if (Config.GEODATA == 0)
				{
					activeChar.sendMessage("Geodata is not active!");
					return true;
				}
				break;
		}
		
		switch (commandEnum)
		{
			case admin_geo_z:
				activeChar.sendMessage("GeoEngine: Geo_Z = " + GeoData.getInstance().getHeight(activeChar.getX(), activeChar.getY(), activeChar.getZ()) + " Loc_Z = " + activeChar.getZ());
				break;
			
			case admin_geo_type:
				final short type = GeoData.getInstance().getType(activeChar.getX(), activeChar.getY());
				activeChar.sendMessage("GeoEngine: Geo_Type = " + type);
				
				final short height = GeoData.getInstance().getHeight(activeChar.getX(), activeChar.getY(), activeChar.getZ());
				activeChar.sendMessage("GeoEngine: height = " + height);
				break;
			
			case admin_geo_nswe:
				String result = "";
				
				final short nswe = GeoData.getInstance().getNSWE(activeChar.getX(), activeChar.getY(), activeChar.getZ());
				
				if ((nswe & 8) == 0)
				{
					result += " N";
				}
				
				if ((nswe & 4) == 0)
				{
					result += " S";
				}
				
				if ((nswe & 2) == 0)
				{
					result += " W";
				}
				
				if ((nswe & 1) == 0)
				{
					result += " E";
				}
				
				activeChar.sendMessage("GeoEngine: Geo_NSWE -> " + nswe + "->" + result);
				break;
			
			case admin_geo_los:
				if (activeChar.getTarget() != null)
				{
					if (GeoData.getInstance().canSeeTargetDebug(activeChar, activeChar.getTarget()))
					{
						activeChar.sendMessage("GeoEngine: Р¦РµР»СЊ РІРёРґРЅР°");
					}
					else
					{
						activeChar.sendMessage("GeoEngine: Р¦РµР»СЊ РЅРµ РІРёРґРЅР°");
					}
				}
				else
				{
					activeChar.sendMessage("None Target!");
				}
				break;
			
			case admin_geo_position:
				activeChar.sendMessage("GeoEngine: Р’Р°С€Р° РїРѕР·РёС†РёСЏ: ");
				activeChar.sendMessage(".... РјРёСЂРѕРІС‹Рµ РєРѕРѕСЂРґРёРЅР°С‚С‹: x: " + activeChar.getX() + " y: " + activeChar.getY() + " z: " + activeChar.getZ());
				activeChar.sendMessage(".... geo РєРѕРѕСЂРґРёРЅР°С‚С‹: " + GeoData.getInstance().geoPosition(activeChar.getX(), activeChar.getY()));
				break;
			
			case admin_geo_load:
				final String[] v = command.substring(15).split(" ");
				
				if (v.length != 2)
				{
					activeChar.sendMessage("РџСЂРёРјРµСЂ: //admin_geo_load <СЂРµРіРёРѕРЅ_X> <СЂРµРіРёРѕРЅ_Y>");
				}
				else
				{
					try
					{
						final byte rx = Byte.parseByte(v[0]);
						final byte ry = Byte.parseByte(v[1]);
						
						final boolean result2 = GeoData.getInstance().loadGeodataFile(rx, ry);
						
						if (result2)
						{
							activeChar.sendMessage("GeoEngine: СЂРµРіРёРѕРЅ [" + rx + "," + ry + "] Р·Р°РіСЂСѓР¶РµРЅ.");
						}
						else
						{
							activeChar.sendMessage("GeoEngine: СЂРµРіРёРѕРЅ [" + rx + "," + ry + "] РЅРµ СЃРјРѕРі Р·Р°РіСЂСѓР·РёС‚СЃСЏ.");
						}
					}
					catch (final Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
						
						activeChar.sendMessage("You have to write numbers of regions <regionX> <regionY>");
					}
				}
				break;
			
			case admin_geo_unload:
				final String[] v2 = command.substring(17).split(" ");
				
				if (v2.length != 2)
				{
					activeChar.sendMessage("РџСЂРёРјРµСЂ: //admin_geo_unload <СЂРµРіРёРѕРЅ_X> <СЂРµРіРёРѕРЅ_Y>");
				}
				else
				{
					try
					{
						final byte rx = Byte.parseByte(v2[0]);
						final byte ry = Byte.parseByte(v2[1]);
						
						GeoData.getInstance().unloadGeodata(rx, ry);
						activeChar.sendMessage("GeoEngine: СЂРµРіРёРѕРЅ [" + rx + "," + ry + "] РІС‹РіСЂСѓР¶РµРЅ.");
					}
					catch (final Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
						
						activeChar.sendMessage("You have to write numbers of regions <regionX> <regionY>");
					}
				}
				break;
			
			case admin_geo_bug:
				try
				{
					final String comment = command.substring(14);
					GeoData.getInstance().addGeoDataBug(activeChar, comment);
				}
				catch (final StringIndexOutOfBoundsException e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
					activeChar.sendMessage("РџСЂРёРјРµСЂ: //admin_geo_bug РІР°С€ РєРѕРјРјРµРЅС‚Р°СЂРёР№ С‚СѓС‚");
				}
				break;
		}
		
		wordList = null;
		comm = null;
		commandEnum = null;
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
