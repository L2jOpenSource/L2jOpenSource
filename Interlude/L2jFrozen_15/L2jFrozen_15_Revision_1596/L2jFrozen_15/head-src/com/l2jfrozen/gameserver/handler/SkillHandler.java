package com.l2jfrozen.gameserver.handler;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.GameServer;
import com.l2jfrozen.gameserver.handler.skillhandlers.BalanceLife;
import com.l2jfrozen.gameserver.handler.skillhandlers.BeastFeed;
import com.l2jfrozen.gameserver.handler.skillhandlers.Blow;
import com.l2jfrozen.gameserver.handler.skillhandlers.Charge;
import com.l2jfrozen.gameserver.handler.skillhandlers.ClanGate;
import com.l2jfrozen.gameserver.handler.skillhandlers.CombatPointHeal;
import com.l2jfrozen.gameserver.handler.skillhandlers.Continuous;
import com.l2jfrozen.gameserver.handler.skillhandlers.CpDam;
import com.l2jfrozen.gameserver.handler.skillhandlers.Craft;
import com.l2jfrozen.gameserver.handler.skillhandlers.DeluxeKey;
import com.l2jfrozen.gameserver.handler.skillhandlers.Disablers;
import com.l2jfrozen.gameserver.handler.skillhandlers.DrainSoul;
import com.l2jfrozen.gameserver.handler.skillhandlers.Fishing;
import com.l2jfrozen.gameserver.handler.skillhandlers.FishingSkill;
import com.l2jfrozen.gameserver.handler.skillhandlers.GetPlayer;
import com.l2jfrozen.gameserver.handler.skillhandlers.Harvest;
import com.l2jfrozen.gameserver.handler.skillhandlers.Heal;
import com.l2jfrozen.gameserver.handler.skillhandlers.ManaHeal;
import com.l2jfrozen.gameserver.handler.skillhandlers.Manadam;
import com.l2jfrozen.gameserver.handler.skillhandlers.Mdam;
import com.l2jfrozen.gameserver.handler.skillhandlers.Pdam;
import com.l2jfrozen.gameserver.handler.skillhandlers.Recall;
import com.l2jfrozen.gameserver.handler.skillhandlers.Resurrect;
import com.l2jfrozen.gameserver.handler.skillhandlers.SiegeFlag;
import com.l2jfrozen.gameserver.handler.skillhandlers.Sow;
import com.l2jfrozen.gameserver.handler.skillhandlers.Spoil;
import com.l2jfrozen.gameserver.handler.skillhandlers.StrSiegeAssault;
import com.l2jfrozen.gameserver.handler.skillhandlers.SummonFriend;
import com.l2jfrozen.gameserver.handler.skillhandlers.SummonTreasureKey;
import com.l2jfrozen.gameserver.handler.skillhandlers.Sweep;
import com.l2jfrozen.gameserver.handler.skillhandlers.TakeCastle;
import com.l2jfrozen.gameserver.handler.skillhandlers.Unlock;
import com.l2jfrozen.gameserver.handler.skillhandlers.ZakenPlayer;
import com.l2jfrozen.gameserver.handler.skillhandlers.ZakenSelf;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;

/**
 * This class ...
 * @version $Revision: 1.1.4.4 $ $Date: 2005/04/03 15:55:06 $
 */
public class SkillHandler
{
	private static final Logger LOGGER = Logger.getLogger(GameServer.class);
	
	private static SkillHandler instance;
	
	private final Map<SkillType, ISkillHandler> dataTable;
	
	public static SkillHandler getInstance()
	{
		if (instance == null)
		{
			instance = new SkillHandler();
		}
		
		return instance;
	}
	
	private SkillHandler()
	{
		dataTable = new TreeMap<>();
		registerSkillHandler(new Blow());
		registerSkillHandler(new Pdam());
		registerSkillHandler(new Mdam());
		registerSkillHandler(new CpDam());
		registerSkillHandler(new Manadam());
		registerSkillHandler(new Heal());
		registerSkillHandler(new CombatPointHeal());
		registerSkillHandler(new ManaHeal());
		registerSkillHandler(new BalanceLife());
		registerSkillHandler(new Charge());
		registerSkillHandler(new ClanGate());
		registerSkillHandler(new Continuous());
		registerSkillHandler(new Resurrect());
		registerSkillHandler(new Spoil());
		registerSkillHandler(new Sweep());
		registerSkillHandler(new StrSiegeAssault());
		registerSkillHandler(new SummonFriend());
		registerSkillHandler(new SummonTreasureKey());
		registerSkillHandler(new Disablers());
		registerSkillHandler(new Recall());
		registerSkillHandler(new SiegeFlag());
		registerSkillHandler(new TakeCastle());
		registerSkillHandler(new Unlock());
		registerSkillHandler(new DrainSoul());
		registerSkillHandler(new Craft());
		registerSkillHandler(new Fishing());
		registerSkillHandler(new FishingSkill());
		registerSkillHandler(new BeastFeed());
		registerSkillHandler(new DeluxeKey());
		registerSkillHandler(new Sow());
		registerSkillHandler(new Harvest());
		registerSkillHandler(new GetPlayer());
		registerSkillHandler(new ZakenPlayer());
		registerSkillHandler(new ZakenSelf());
		LOGGER.info("SkillHandler: Loaded " + dataTable.size() + " handlers.");
		
	}
	
	public void registerSkillHandler(final ISkillHandler handler)
	{
		SkillType[] types = handler.getSkillIds();
		
		for (final SkillType t : types)
		{
			dataTable.put(t, handler);
		}
		types = null;
	}
	
	public ISkillHandler getSkillHandler(final SkillType skillType)
	{
		return dataTable.get(skillType);
	}
	
	/**
	 * @return
	 */
	public int size()
	{
		return dataTable.size();
	}
}