package net.sf.l2j.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.handler.targethandlers.TargetAlly;
import net.sf.l2j.gameserver.handler.targethandlers.TargetArea;
import net.sf.l2j.gameserver.handler.targethandlers.TargetAreaCorpseMob;
import net.sf.l2j.gameserver.handler.targethandlers.TargetAreaSummon;
import net.sf.l2j.gameserver.handler.targethandlers.TargetAura;
import net.sf.l2j.gameserver.handler.targethandlers.TargetAuraUndead;
import net.sf.l2j.gameserver.handler.targethandlers.TargetBehindArea;
import net.sf.l2j.gameserver.handler.targethandlers.TargetBehindAura;
import net.sf.l2j.gameserver.handler.targethandlers.TargetClan;
import net.sf.l2j.gameserver.handler.targethandlers.TargetCorpseAlly;
import net.sf.l2j.gameserver.handler.targethandlers.TargetCorpseMob;
import net.sf.l2j.gameserver.handler.targethandlers.TargetCorpsePet;
import net.sf.l2j.gameserver.handler.targethandlers.TargetCorpsePlayer;
import net.sf.l2j.gameserver.handler.targethandlers.TargetEnemySummon;
import net.sf.l2j.gameserver.handler.targethandlers.TargetFrontArea;
import net.sf.l2j.gameserver.handler.targethandlers.TargetFrontAura;
import net.sf.l2j.gameserver.handler.targethandlers.TargetGround;
import net.sf.l2j.gameserver.handler.targethandlers.TargetHoly;
import net.sf.l2j.gameserver.handler.targethandlers.TargetOne;
import net.sf.l2j.gameserver.handler.targethandlers.TargetOwnerPet;
import net.sf.l2j.gameserver.handler.targethandlers.TargetParty;
import net.sf.l2j.gameserver.handler.targethandlers.TargetPartyMember;
import net.sf.l2j.gameserver.handler.targethandlers.TargetPartyOther;
import net.sf.l2j.gameserver.handler.targethandlers.TargetPet;
import net.sf.l2j.gameserver.handler.targethandlers.TargetSelf;
import net.sf.l2j.gameserver.handler.targethandlers.TargetSummon;
import net.sf.l2j.gameserver.handler.targethandlers.TargetUndead;
import net.sf.l2j.gameserver.handler.targethandlers.TargetUnlockable;

public class TargetHandler
{
	private final Map<SkillTargetType, ITargetHandler> _entries = new HashMap<>();
	
	protected TargetHandler()
	{
		registerHandler(new TargetAlly());
		registerHandler(new TargetArea());
		registerHandler(new TargetAreaCorpseMob());
		registerHandler(new TargetAreaSummon());
		registerHandler(new TargetAura());
		registerHandler(new TargetAuraUndead());
		registerHandler(new TargetBehindArea());
		registerHandler(new TargetBehindAura());
		registerHandler(new TargetClan());
		registerHandler(new TargetCorpseAlly());
		registerHandler(new TargetCorpseMob());
		registerHandler(new TargetCorpsePet());
		registerHandler(new TargetCorpsePlayer());
		registerHandler(new TargetEnemySummon());
		registerHandler(new TargetFrontArea());
		registerHandler(new TargetFrontAura());
		registerHandler(new TargetGround());
		registerHandler(new TargetHoly());
		registerHandler(new TargetOne());
		registerHandler(new TargetOwnerPet());
		registerHandler(new TargetParty());
		registerHandler(new TargetPartyMember());
		registerHandler(new TargetPartyOther());
		registerHandler(new TargetPet());
		registerHandler(new TargetSelf());
		registerHandler(new TargetSummon());
		registerHandler(new TargetUndead());
		registerHandler(new TargetUnlockable());
	}
	
	private void registerHandler(ITargetHandler handler)
	{
		_entries.put(handler.getTargetType(), handler);
	}
	
	public ITargetHandler getHandler(SkillTargetType targetType)
	{
		return _entries.get(targetType);
	}
	
	public int size()
	{
		return _entries.size();
	}
	
	public static TargetHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final TargetHandler INSTANCE = new TargetHandler();
	}
}