package net.sf.l2j.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.skillhandlers.BalanceLife;
import net.sf.l2j.gameserver.handler.skillhandlers.Blow;
import net.sf.l2j.gameserver.handler.skillhandlers.Cancel;
import net.sf.l2j.gameserver.handler.skillhandlers.CombatPointHeal;
import net.sf.l2j.gameserver.handler.skillhandlers.Continuous;
import net.sf.l2j.gameserver.handler.skillhandlers.CpDamPercent;
import net.sf.l2j.gameserver.handler.skillhandlers.Craft;
import net.sf.l2j.gameserver.handler.skillhandlers.Disablers;
import net.sf.l2j.gameserver.handler.skillhandlers.DrainSoul;
import net.sf.l2j.gameserver.handler.skillhandlers.Dummy;
import net.sf.l2j.gameserver.handler.skillhandlers.Extractable;
import net.sf.l2j.gameserver.handler.skillhandlers.Fishing;
import net.sf.l2j.gameserver.handler.skillhandlers.FishingSkill;
import net.sf.l2j.gameserver.handler.skillhandlers.GetPlayer;
import net.sf.l2j.gameserver.handler.skillhandlers.GiveSp;
import net.sf.l2j.gameserver.handler.skillhandlers.Harvest;
import net.sf.l2j.gameserver.handler.skillhandlers.Heal;
import net.sf.l2j.gameserver.handler.skillhandlers.HealPercent;
import net.sf.l2j.gameserver.handler.skillhandlers.InstantJump;
import net.sf.l2j.gameserver.handler.skillhandlers.ManaHeal;
import net.sf.l2j.gameserver.handler.skillhandlers.Manadam;
import net.sf.l2j.gameserver.handler.skillhandlers.Mdam;
import net.sf.l2j.gameserver.handler.skillhandlers.Pdam;
import net.sf.l2j.gameserver.handler.skillhandlers.Resurrect;
import net.sf.l2j.gameserver.handler.skillhandlers.Sow;
import net.sf.l2j.gameserver.handler.skillhandlers.Spoil;
import net.sf.l2j.gameserver.handler.skillhandlers.StrSiegeAssault;
import net.sf.l2j.gameserver.handler.skillhandlers.SummonFriend;
import net.sf.l2j.gameserver.handler.skillhandlers.Sweep;
import net.sf.l2j.gameserver.handler.skillhandlers.TakeCastle;
import net.sf.l2j.gameserver.handler.skillhandlers.Unlock;

public class SkillHandler
{
	private final Map<Integer, ISkillHandler> _entries = new HashMap<>();
	
	protected SkillHandler()
	{
		registerHandler(new BalanceLife());
		registerHandler(new Blow());
		registerHandler(new Cancel());
		registerHandler(new CombatPointHeal());
		registerHandler(new Continuous());
		registerHandler(new CpDamPercent());
		registerHandler(new Craft());
		registerHandler(new Disablers());
		registerHandler(new DrainSoul());
		registerHandler(new Dummy());
		registerHandler(new Extractable());
		registerHandler(new Fishing());
		registerHandler(new FishingSkill());
		registerHandler(new GetPlayer());
		registerHandler(new GiveSp());
		registerHandler(new Harvest());
		registerHandler(new Heal());
		registerHandler(new HealPercent());
		registerHandler(new InstantJump());
		registerHandler(new Manadam());
		registerHandler(new ManaHeal());
		registerHandler(new Mdam());
		registerHandler(new Pdam());
		registerHandler(new Resurrect());
		registerHandler(new Sow());
		registerHandler(new Spoil());
		registerHandler(new StrSiegeAssault());
		registerHandler(new SummonFriend());
		registerHandler(new Sweep());
		registerHandler(new TakeCastle());
		registerHandler(new Unlock());
	}
	
	private void registerHandler(ISkillHandler handler)
	{
		for (L2SkillType t : handler.getSkillIds())
			_entries.put(t.ordinal(), handler);
	}
	
	public ISkillHandler getHandler(L2SkillType skillType)
	{
		return _entries.get(skillType.ordinal());
	}
	
	public int size()
	{
		return _entries.size();
	}
	
	public static SkillHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillHandler INSTANCE = new SkillHandler();
	}
}