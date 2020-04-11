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

import l2r.gameserver.scripts.handlers.bypasshandlers.ArenaBuff;
import l2r.gameserver.scripts.handlers.bypasshandlers.Augment;
import l2r.gameserver.scripts.handlers.bypasshandlers.Buy;
import l2r.gameserver.scripts.handlers.bypasshandlers.BuyShadowItem;
import l2r.gameserver.scripts.handlers.bypasshandlers.ChatLink;
import l2r.gameserver.scripts.handlers.bypasshandlers.ClanWarehouse;
import l2r.gameserver.scripts.handlers.bypasshandlers.ElcardiaBuff;
import l2r.gameserver.scripts.handlers.bypasshandlers.Festival;
import l2r.gameserver.scripts.handlers.bypasshandlers.Freight;
import l2r.gameserver.scripts.handlers.bypasshandlers.ItemAuctionLink;
import l2r.gameserver.scripts.handlers.bypasshandlers.Link;
import l2r.gameserver.scripts.handlers.bypasshandlers.Loto;
import l2r.gameserver.scripts.handlers.bypasshandlers.ManorManager;
import l2r.gameserver.scripts.handlers.bypasshandlers.Multisell;
import l2r.gameserver.scripts.handlers.bypasshandlers.Observation;
import l2r.gameserver.scripts.handlers.bypasshandlers.OlympiadManagerLink;
import l2r.gameserver.scripts.handlers.bypasshandlers.OlympiadObservation;
import l2r.gameserver.scripts.handlers.bypasshandlers.PlayerHelp;
import l2r.gameserver.scripts.handlers.bypasshandlers.PrivateWarehouse;
import l2r.gameserver.scripts.handlers.bypasshandlers.QuestLink;
import l2r.gameserver.scripts.handlers.bypasshandlers.QuestList;
import l2r.gameserver.scripts.handlers.bypasshandlers.ReceivePremium;
import l2r.gameserver.scripts.handlers.bypasshandlers.ReleaseAttribute;
import l2r.gameserver.scripts.handlers.bypasshandlers.RemoveDeathPenalty;
import l2r.gameserver.scripts.handlers.bypasshandlers.RentPet;
import l2r.gameserver.scripts.handlers.bypasshandlers.Rift;
import l2r.gameserver.scripts.handlers.bypasshandlers.SkillList;
import l2r.gameserver.scripts.handlers.bypasshandlers.SupportBlessing;
import l2r.gameserver.scripts.handlers.bypasshandlers.SupportMagic;
import l2r.gameserver.scripts.handlers.bypasshandlers.TerritoryStatus;
import l2r.gameserver.scripts.handlers.bypasshandlers.VoiceCommand;
import l2r.gameserver.scripts.handlers.bypasshandlers.Wear;

/**
 * @author nBd, UnAfraid
 */
public class BypassHandler implements IHandler<IBypassHandler, String>
{
	private final Map<String, IBypassHandler> _datatable;
	
	protected BypassHandler()
	{
		_datatable = new HashMap<>();
		
		registerHandler(new ArenaBuff());
		registerHandler(new Augment());
		registerHandler(new Buy());
		registerHandler(new BuyShadowItem());
		registerHandler(new ChatLink());
		registerHandler(new ClanWarehouse());
		registerHandler(new ElcardiaBuff());
		registerHandler(new Festival());
		registerHandler(new Freight());
		registerHandler(new ItemAuctionLink());
		registerHandler(new Link());
		registerHandler(new Loto());
		registerHandler(new ManorManager());
		registerHandler(new Multisell());
		registerHandler(new Observation());
		registerHandler(new OlympiadManagerLink());
		registerHandler(new OlympiadObservation());
		registerHandler(new PlayerHelp());
		registerHandler(new PrivateWarehouse());
		registerHandler(new QuestLink());
		registerHandler(new QuestList());
		registerHandler(new ReceivePremium());
		registerHandler(new ReleaseAttribute());
		registerHandler(new RemoveDeathPenalty());
		registerHandler(new RentPet());
		registerHandler(new Rift());
		registerHandler(new SkillList());
		registerHandler(new SupportBlessing());
		registerHandler(new SupportMagic());
		registerHandler(new TerritoryStatus());
		registerHandler(new VoiceCommand());
		registerHandler(new Wear());
	}
	
	@Override
	public void registerHandler(IBypassHandler handler)
	{
		for (String element : handler.getBypassList())
		{
			_datatable.put(element.toLowerCase(), handler);
		}
	}
	
	@Override
	public synchronized void removeHandler(IBypassHandler handler)
	{
		for (String element : handler.getBypassList())
		{
			_datatable.remove(element.toLowerCase());
		}
	}
	
	@Override
	public IBypassHandler getHandler(String command)
	{
		if (command.contains(" "))
		{
			command = command.substring(0, command.indexOf(" "));
		}
		return _datatable.get(command.toLowerCase());
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	public static BypassHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final BypassHandler _instance = new BypassHandler();
	}
}