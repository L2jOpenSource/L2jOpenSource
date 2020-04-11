package net.sf.l2j.gameserver.scripting.scripts.conquerablehalls;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.entity.ClanHallSiege;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;

/**
 * The Fortress of the Dead is located southeast of the Rune Township and is a contested hideout similar to the siege style of the Devastated Castle clan hall. It is of the highest grade among all contested clan halls.<br>
 * <br>
 * Only a clan level 4 or higher may participate.<br>
 * <br>
 * Siege registration is open up to two hours before a war and is scheduled through the messenger NPC outside of the clan hall.<br>
 * <br>
 * The siege war follows the same rules as Devastated Castle. The siege war goes on for one hour, and the clan that contributes the most to killing Lidia von Hellmann takes possession of the clan hall. If the followers of Lidia von Hellmann, Alfred von Hellmann, and Giselle von Hellmann are killed,
 * the clan hall war will be a lot easier.<br>
 * <br>
 * The siege war ends upon the death of Lidia von Hellmann; just as in Devastated Castle, and if there is no clan that has killed the applicable NPC, the clan hall is under the NPC's possession until the next siege war.<br>
 * <br>
 * The clan that owned the clan hall previously will be automatically registered in the next clan hall war.<br>
 * <br>
 * The possessing clan hall leader can ride on a wyvern.
 */
public final class FortressOfTheDead extends ClanHallSiege
{
	private static final int LIDIA = 35629;
	private static final int ALFRED = 35630;
	private static final int GISELLE = 35631;
	
	private static final String LIDIA_SPAWN_SHOUT = "Hmm, those who are not of the bloodline are coming this way to take over the castle?!  Humph!  The bitter grudges of the dead.  You must not make light of their power!";
	private static final String ALFRED_SPAWN_SHOUT = "Heh Heh... I see that the feast has begun! Be wary! The curse of the Hellmann family has poisoned this land!";
	private static final String GISELLE_SPAWN_SHOUT = "Arise, my faithful servants! You, my people who have inherited the blood.  It is the calling of my daughter.  The feast of blood will now begin!";
	
	private static final String ALFRED_GISELLE_DEATH_SHOUT = "Aargh...!  If I die, then the magic force field of blood will...!";
	private static final String LIDIA_DEATH_SHOUT = "Grarr! For the next 2 minutes or so, the game arena are will be cleaned. Throw any items you don't need to the floor now.";
	
	private final Map<Integer, Integer> _damageToLidia = new ConcurrentHashMap<>();
	
	public FortressOfTheDead()
	{
		super("conquerablehalls", FORTRESS_OF_DEAD);
		
		addAttackId(LIDIA);
		addKillId(LIDIA, ALFRED, GISELLE);
		addSpawnId(LIDIA, ALFRED, GISELLE);
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (!_hall.isInSiege() || !(attacker instanceof Playable))
			return null;
		
		final Clan clan = attacker.getActingPlayer().getClan();
		if (clan != null && getAttackerClans().contains(clan))
			_damageToLidia.merge(clan.getClanId(), damage, Integer::sum);
		
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		if (!_hall.isInSiege())
			return null;
		
		final int npcId = npc.getNpcId();
		
		if (npcId == ALFRED || npcId == GISELLE)
			broadcastNpcSay(npc, SayType.SHOUT, ALFRED_GISELLE_DEATH_SHOUT);
		else if (npcId == LIDIA)
		{
			broadcastNpcSay(npc, SayType.SHOUT, LIDIA_DEATH_SHOUT);
			
			_missionAccomplished = true;
			
			cancelSiegeTask();
			endSiege();
		}
		
		return null;
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		if (npc.getNpcId() == LIDIA)
			broadcastNpcSay(npc, SayType.SHOUT, LIDIA_SPAWN_SHOUT);
		else if (npc.getNpcId() == ALFRED)
			broadcastNpcSay(npc, SayType.SHOUT, ALFRED_SPAWN_SHOUT);
		else if (npc.getNpcId() == GISELLE)
			broadcastNpcSay(npc, SayType.SHOUT, GISELLE_SPAWN_SHOUT);
		
		return null;
	}
	
	@Override
	public Clan getWinner()
	{
		// If none did damages, simply return null.
		if (_damageToLidia.isEmpty())
			return null;
		
		// Retrieve clanId who did the biggest amount of damage.
		final int clanId = Collections.max(_damageToLidia.entrySet(), Map.Entry.comparingByValue()).getKey();
		
		// Clear the Map for future usage.
		_damageToLidia.clear();
		
		// Return the Clan winner.
		return ClanTable.getInstance().getClan(clanId);
	}
	
	@Override
	public void startSiege()
	{
		// Siege must start at night
		final int hoursLeft = (GameTimeTaskManager.getInstance().getGameTime() / 60) % 24;
		if (hoursLeft < 0 || hoursLeft > 6)
		{
			cancelSiegeTask();
			
			long scheduleTime = (24 - hoursLeft) * 10 * 60000;
			_siegeTask = ThreadPool.schedule(() -> startSiege(), scheduleTime);
		}
		else
			super.startSiege();
	}
}
