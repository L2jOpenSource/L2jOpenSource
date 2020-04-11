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

import l2r.gameserver.model.skills.targets.L2TargetType;
import l2r.gameserver.scripts.handlers.targethandlers.Area;
import l2r.gameserver.scripts.handlers.targethandlers.AreaCorpseMob;
import l2r.gameserver.scripts.handlers.targethandlers.AreaFriendly;
import l2r.gameserver.scripts.handlers.targethandlers.AreaSummon;
import l2r.gameserver.scripts.handlers.targethandlers.Aura;
import l2r.gameserver.scripts.handlers.targethandlers.AuraCorpseMob;
import l2r.gameserver.scripts.handlers.targethandlers.BehindArea;
import l2r.gameserver.scripts.handlers.targethandlers.BehindAura;
import l2r.gameserver.scripts.handlers.targethandlers.Clan;
import l2r.gameserver.scripts.handlers.targethandlers.ClanMember;
import l2r.gameserver.scripts.handlers.targethandlers.CommandChannel;
import l2r.gameserver.scripts.handlers.targethandlers.CorpseClan;
import l2r.gameserver.scripts.handlers.targethandlers.CorpseMob;
import l2r.gameserver.scripts.handlers.targethandlers.CorpsePet;
import l2r.gameserver.scripts.handlers.targethandlers.CorpsePlayer;
import l2r.gameserver.scripts.handlers.targethandlers.EnemySummon;
import l2r.gameserver.scripts.handlers.targethandlers.FlagPole;
import l2r.gameserver.scripts.handlers.targethandlers.FrontArea;
import l2r.gameserver.scripts.handlers.targethandlers.FrontAura;
import l2r.gameserver.scripts.handlers.targethandlers.Ground;
import l2r.gameserver.scripts.handlers.targethandlers.Holy;
import l2r.gameserver.scripts.handlers.targethandlers.One;
import l2r.gameserver.scripts.handlers.targethandlers.OwnerPet;
import l2r.gameserver.scripts.handlers.targethandlers.Party;
import l2r.gameserver.scripts.handlers.targethandlers.PartyClan;
import l2r.gameserver.scripts.handlers.targethandlers.PartyMember;
import l2r.gameserver.scripts.handlers.targethandlers.PartyNotMe;
import l2r.gameserver.scripts.handlers.targethandlers.PartyOther;
import l2r.gameserver.scripts.handlers.targethandlers.Pet;
import l2r.gameserver.scripts.handlers.targethandlers.Self;
import l2r.gameserver.scripts.handlers.targethandlers.Summon;
import l2r.gameserver.scripts.handlers.targethandlers.TargetParty;
import l2r.gameserver.scripts.handlers.targethandlers.Unlockable;

/**
 * @author UnAfraid
 */
public class TargetHandler implements IHandler<ITargetTypeHandler, Enum<L2TargetType>>
{
	private final Map<Enum<L2TargetType>, ITargetTypeHandler> _datatable;
	
	protected TargetHandler()
	{
		_datatable = new HashMap<>();
		
		registerHandler(new Area());
		registerHandler(new AreaCorpseMob());
		registerHandler(new AreaFriendly());
		registerHandler(new AreaSummon());
		registerHandler(new Aura());
		registerHandler(new AuraCorpseMob());
		registerHandler(new BehindArea());
		registerHandler(new BehindAura());
		registerHandler(new Clan());
		registerHandler(new ClanMember());
		registerHandler(new CommandChannel());
		registerHandler(new CorpseClan());
		registerHandler(new CorpseMob());
		registerHandler(new CorpsePet());
		registerHandler(new CorpsePlayer());
		registerHandler(new EnemySummon());
		registerHandler(new FlagPole());
		registerHandler(new FrontArea());
		registerHandler(new FrontAura());
		registerHandler(new Ground());
		registerHandler(new Holy());
		registerHandler(new One());
		registerHandler(new OwnerPet());
		registerHandler(new Party());
		registerHandler(new PartyClan());
		registerHandler(new PartyMember());
		registerHandler(new PartyNotMe());
		registerHandler(new PartyOther());
		registerHandler(new Pet());
		registerHandler(new Self());
		registerHandler(new Summon());
		registerHandler(new TargetParty());
		registerHandler(new Unlockable());
	}
	
	@Override
	public void registerHandler(ITargetTypeHandler handler)
	{
		_datatable.put(handler.getTargetType(), handler);
	}
	
	@Override
	public synchronized void removeHandler(ITargetTypeHandler handler)
	{
		_datatable.remove(handler.getTargetType());
	}
	
	@Override
	public ITargetTypeHandler getHandler(Enum<L2TargetType> targetType)
	{
		return _datatable.get(targetType);
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	public static TargetHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TargetHandler _instance = new TargetHandler();
	}
}
