package net.sf.l2j.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminAdmin;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminAnnouncements;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminBan;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminBookmark;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminBuffs;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminCamera;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminClanHall;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminCreateItem;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminCursedWeapons;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminDelete;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminDoorControl;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEditChar;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEditNpc;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEffects;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEnchant;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminExpSp;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminGeoEngine;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminGm;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminGmChat;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminHeal;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminHelpPage;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminKick;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminKnownlist;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminLevel;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminMaintenance;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminMammon;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminManor;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminMenu;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminMovieMaker;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminOlympiad;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminPForge;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminPetition;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminPledge;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminPolymorph;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminRes;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminRideWyvern;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminShop;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminSiege;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminSkill;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminSpawn;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminTarget;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminTeleport;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminZone;

public class AdminCommandHandler
{
	private final Map<Integer, IAdminCommandHandler> _entries = new HashMap<>();
	
	protected AdminCommandHandler()
	{
		registerHandler(new AdminAdmin());
		registerHandler(new AdminAnnouncements());
		registerHandler(new AdminBan());
		registerHandler(new AdminBookmark());
		registerHandler(new AdminBuffs());
		registerHandler(new AdminCamera());
		registerHandler(new AdminClanHall());
		registerHandler(new AdminCreateItem());
		registerHandler(new AdminCursedWeapons());
		registerHandler(new AdminDelete());
		registerHandler(new AdminDoorControl());
		registerHandler(new AdminEditChar());
		registerHandler(new AdminEditNpc());
		registerHandler(new AdminEffects());
		registerHandler(new AdminEnchant());
		registerHandler(new AdminExpSp());
		registerHandler(new AdminGeoEngine());
		registerHandler(new AdminGm());
		registerHandler(new AdminGmChat());
		registerHandler(new AdminHeal());
		registerHandler(new AdminHelpPage());
		registerHandler(new AdminKick());
		registerHandler(new AdminKnownlist());
		registerHandler(new AdminLevel());
		registerHandler(new AdminMaintenance());
		registerHandler(new AdminMammon());
		registerHandler(new AdminManor());
		registerHandler(new AdminMenu());
		registerHandler(new AdminMovieMaker());
		registerHandler(new AdminOlympiad());
		registerHandler(new AdminPetition());
		registerHandler(new AdminPForge());
		registerHandler(new AdminPledge());
		registerHandler(new AdminPolymorph());
		registerHandler(new AdminRes());
		registerHandler(new AdminRideWyvern());
		registerHandler(new AdminShop());
		registerHandler(new AdminSiege());
		registerHandler(new AdminSkill());
		registerHandler(new AdminSpawn());
		registerHandler(new AdminTarget());
		registerHandler(new AdminTeleport());
		registerHandler(new AdminZone());
	}
	
	private void registerHandler(IAdminCommandHandler handler)
	{
		for (String id : handler.getAdminCommandList())
			_entries.put(id.hashCode(), handler);
	}
	
	public IAdminCommandHandler getHandler(String adminCommand)
	{
		String command = adminCommand;
		
		if (adminCommand.indexOf(" ") != -1)
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		
		return _entries.get(command.hashCode());
	}
	
	public int size()
	{
		return _entries.size();
	}
	
	public static AdminCommandHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AdminCommandHandler INSTANCE = new AdminCommandHandler();
	}
}