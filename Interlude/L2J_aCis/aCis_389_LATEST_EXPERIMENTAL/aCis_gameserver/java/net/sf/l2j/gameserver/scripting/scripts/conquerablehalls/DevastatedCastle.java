package net.sf.l2j.gameserver.scripting.scripts.conquerablehalls;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.entity.ClanHallSiege;
import net.sf.l2j.gameserver.model.holder.SkillUseHolder;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.skills.L2Skill;

/**
 * The Devastated Castle is an occupied clan hall for which ownership can be acquired by siege battle with NPCs. The siege is set up to take place with the NPCs every week, which is different from other sieges. The clan hall battle for the Devastated Castle is always defended by an NPC clan and the
 * ownership to the castle is maintained for one week by the PC clan that is able to defeat the defending NPC clan and take the castle.<br>
 * <br>
 * <b>Time of the Occupying Battle</b><br>
 * <br>
 * The time when the Devastated Castle occupying battle takes place is at the same time each week after the very first occupation battle.<br>
 * <br>
 * <b>Registration Participation</b><br>
 * <br>
 * Clan that wants to participate in the battle to occupy and acquire the clan hall must register to participate in advance. Participation in the battle to occupy takes place in clan units and only clan leaders having clans of level 4 or above can apply to register. Clans that already own another
 * clan hall cannot register. The clan that had owned the Devastated Castle previously is automatically registered for the siege and the registration for the battle to occupy must be done at least two hours before the start time. Clans that participate in the battle to occupy the Devastated Castle
 * cannot participate in another battle to occupy another clan hall or siege at the same time.<br>
 * <br>
 * <b>Start of the Siege Battle</b><br>
 * <br>
 * When the time set for the siege starts, the area inside and around the Devastated Castle is designated as the battleground and the players that are already inside the castle are all kicked to the outside. Leader of the clans that are registered in the battle to occupy the castle can build a
 * headquarters.The rules of battle during the battle to occupy the castle are the same as ordinary sieges except for the fact that everyone is on the attacking side and not on the defending side. However, even if the headquarters are destroyed or an unregistered character enters the battlefield and
 * is killed, players are not restarted at the second closest village like in a siege.<br>
 * <br>
 * <b>Conditions of Victory</b><br>
 * <br>
 * A battle to occupy lasts for one hour and the clan recorded as having contributed the most to killing the head NPC of the Devastated Castle becomes the owner of the clan hall. If the leader is killed, the battle to occupy ends immediately. If no clan is able to kill the leader within the time
 * period, the Devastated Castle continues to be occupied by the NPCs until the next battle to occupy.
 */
public final class DevastatedCastle extends ClanHallSiege
{
	private static final int GUSTAV = 35410;
	private static final int MIKHAIL = 35409;
	private static final int DIETRICH = 35408;
	
	private static final String MIKHAIL_SPAWN_SHOUT = "Glory to Aden, the Kingdom of the Lion! Glory to Sir Gustav, our immortal lord!";
	private static final String DIETRICH_SPAWN_SHOUT = "Soldiers of Gustav, go forth and destroy the invaders!";
	private static final String GUSTAV_ATTACK_SHOUT = "This is unbelievable! Have I really been defeated? I shall return and take your head!";
	
	private final Map<Integer, Integer> _damageToGustav = new ConcurrentHashMap<>();
	
	public DevastatedCastle()
	{
		super("conquerablehalls", DEVASTATED_CASTLE);
		
		addKillId(GUSTAV);
		addSpawnId(MIKHAIL, DIETRICH);
		addAttackId(GUSTAV);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		if (npc.getNpcId() == MIKHAIL)
			broadcastNpcSay(npc, SayType.SHOUT, MIKHAIL_SPAWN_SHOUT);
		else if (npc.getNpcId() == DIETRICH)
			broadcastNpcSay(npc, SayType.SHOUT, DIETRICH_SPAWN_SHOUT);
		
		return null;
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (!_hall.isInSiege() || !(attacker instanceof Playable))
			return null;
		
		final Clan clan = attacker.getActingPlayer().getClan();
		if (clan != null && getAttackerClans().contains(clan))
			_damageToGustav.merge(clan.getClanId(), damage, Integer::sum);
		
		if ((npc.getCurrentHp() < npc.getMaxHp() / 12) && npc.getAI().getCurrentIntention().getType() != IntentionType.CAST)
		{
			broadcastNpcSay(npc, SayType.ALL, GUSTAV_ATTACK_SHOUT);
			npc.getAI().tryTo(IntentionType.CAST, new SkillUseHolder(SkillTable.getInstance().getInfo(4235, 1), npc, false, false), null);
		}
		return super.onAttack(npc, attacker, damage, skill);
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		if (!_hall.isInSiege())
			return null;
		
		_missionAccomplished = true;
		
		if (npc.getNpcId() == GUSTAV)
		{
			cancelSiegeTask();
			endSiege();
		}
		
		return super.onKill(npc, killer);
	}
	
	@Override
	public Clan getWinner()
	{
		// If none did damages, simply return null.
		if (_damageToGustav.isEmpty())
			return null;
		
		// Retrieve clanId who did the biggest amount of damage.
		final int clanId = Collections.max(_damageToGustav.entrySet(), Map.Entry.comparingByValue()).getKey();
		
		// Clear the Map for future usage.
		_damageToGustav.clear();
		
		// Return the Clan winner.
		return ClanTable.getInstance().getClan(clanId);
	}
}