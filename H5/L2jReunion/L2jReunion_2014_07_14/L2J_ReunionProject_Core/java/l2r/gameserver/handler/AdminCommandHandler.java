/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminAdmin;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminAnnouncements;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminBBS;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminBuffs;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminCHSiege;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminCamera;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminChangeAccessLevel;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminCheckBots;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminClan;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminCreateItem;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminCursedWeapons;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminCustomCreateItem;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminDebug;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminDelete;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminDisconnect;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminDoorControl;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminEditChar;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminEditNpc;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminEffects;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminElement;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminEnchant;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminExpSp;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminFightCalculator;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminFortSiege;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminGeoEditor;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminGeodata;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminGm;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminGmChat;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminGraciaSeeds;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminGrandBoss;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminHWIDBan;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminHeal;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminHellbound;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminHtml;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminInstance;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminInstanceZone;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminInvul;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminKick;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminKill;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminLevel;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminLogin;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminMammon;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminManor;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminMenu;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminMessages;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminMobGroup;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminMonsterRace;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminPForge;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminPathNode;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminPcCondOverride;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminPetition;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminPledge;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminPolymorph;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminPremium;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminPunishment;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminQuest;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminReload;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminRepairChar;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminRes;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminRide;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminScan;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminShop;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminShowQuests;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminShutdown;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminSiege;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminSkill;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminSpawn;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminSummon;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminTarget;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminTargetSay;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminTeleport;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminTerritoryWar;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminTest;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminUnblockIp;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminVitality;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminZone;

/**
 * @author UnAfraid
 */
public class AdminCommandHandler implements IHandler<IAdminCommandHandler, String>
{
	private final Map<String, IAdminCommandHandler> _datatable;
	
	protected AdminCommandHandler()
	{
		_datatable = new HashMap<>();
		
		registerHandler(new AdminAdmin());
		registerHandler(new AdminAnnouncements());
		registerHandler(new AdminBBS());
		registerHandler(new AdminBuffs());
		registerHandler(new AdminCamera());
		registerHandler(new AdminChangeAccessLevel());
		registerHandler(new AdminCheckBots());
		registerHandler(new AdminCHSiege());
		registerHandler(new AdminClan());
		registerHandler(new AdminCreateItem());
		registerHandler(new AdminCursedWeapons());
		registerHandler(new AdminCustomCreateItem());
		registerHandler(new AdminDebug());
		registerHandler(new AdminDelete());
		registerHandler(new AdminDisconnect());
		registerHandler(new AdminDoorControl());
		registerHandler(new AdminEditChar());
		registerHandler(new AdminEditNpc());
		registerHandler(new AdminEffects());
		registerHandler(new AdminElement());
		registerHandler(new AdminEnchant());
		registerHandler(new AdminExpSp());
		registerHandler(new AdminFightCalculator());
		registerHandler(new AdminFortSiege());
		registerHandler(new AdminHellbound());
		registerHandler(new AdminGeodata());
		registerHandler(new AdminGeoEditor());
		registerHandler(new AdminGm());
		registerHandler(new AdminGmChat());
		registerHandler(new AdminGraciaSeeds());
		registerHandler(new AdminGrandBoss());
		registerHandler(new AdminHeal());
		registerHandler(new AdminHtml());
		registerHandler(new AdminHWIDBan());
		registerHandler(new AdminInstance());
		registerHandler(new AdminInstanceZone());
		registerHandler(new AdminInvul());
		registerHandler(new AdminKick());
		registerHandler(new AdminKill());
		registerHandler(new AdminLevel());
		registerHandler(new AdminLogin());
		registerHandler(new AdminMammon());
		registerHandler(new AdminManor());
		registerHandler(new AdminMenu());
		registerHandler(new AdminMessages());
		registerHandler(new AdminMobGroup());
		registerHandler(new AdminMonsterRace());
		registerHandler(new AdminPathNode());
		registerHandler(new AdminPcCondOverride());
		registerHandler(new AdminPetition());
		registerHandler(new AdminPForge());
		registerHandler(new AdminPledge());
		registerHandler(new AdminPolymorph());
		registerHandler(new AdminPremium());
		registerHandler(new AdminPunishment());
		registerHandler(new AdminQuest());
		registerHandler(new AdminReload());
		registerHandler(new AdminRepairChar());
		registerHandler(new AdminRes());
		registerHandler(new AdminRide());
		registerHandler(new AdminScan());
		registerHandler(new AdminShop());
		registerHandler(new AdminShowQuests());
		registerHandler(new AdminShutdown());
		registerHandler(new AdminSiege());
		registerHandler(new AdminSkill());
		registerHandler(new AdminSpawn());
		registerHandler(new AdminSummon());
		registerHandler(new AdminTarget());
		registerHandler(new AdminTargetSay());
		registerHandler(new AdminTeleport());
		registerHandler(new AdminTerritoryWar());
		registerHandler(new AdminTest());
		registerHandler(new AdminUnblockIp());
		registerHandler(new AdminVitality());
		registerHandler(new AdminZone());
	}
	
	@Override
	public void registerHandler(IAdminCommandHandler handler)
	{
		String[] ids = handler.getAdminCommandList();
		for (String id : ids)
		{
			_datatable.put(id, handler);
		}
	}
	
	@Override
	public synchronized void removeHandler(IAdminCommandHandler handler)
	{
		String[] ids = handler.getAdminCommandList();
		for (String id : ids)
		{
			_datatable.remove(id);
		}
	}
	
	@Override
	public IAdminCommandHandler getHandler(String adminCommand)
	{
		String command = adminCommand;
		if (adminCommand.contains(" "))
		{
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		}
		return _datatable.get(command);
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	public static AdminCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AdminCommandHandler _instance = new AdminCommandHandler();
	}
}
