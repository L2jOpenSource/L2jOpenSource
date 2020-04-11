package net.sf.l2j.gameserver.scripting.scripts.conquerablehalls;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.ClanHallSiege;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.spawn.Spawn;
import net.sf.l2j.gameserver.skills.L2Skill;

/**
 * Fortress of Resistance clan hall siege Script.
 */
public final class FortressOfResistance extends ClanHallSiege
{
	private static final int MESSENGER = 35382;
	private static final int BLOODY_LORD_NURKA = 35375;
	
	private static final SpawnLocation[] NURKA_COORDS =
	{
		new SpawnLocation(51952, 111060, -1970, 200), // Spawn location
		new SpawnLocation(45802, 109981, -1970, 0), // First event : move to Location
		new SpawnLocation(44525, 108867, -2020, 0) // Second event : move to Location
	};
	
	private final Map<Integer, Integer> _damageToNurka = new ConcurrentHashMap<>();
	
	private Spawn _nurka;
	
	public FortressOfResistance()
	{
		super("conquerablehalls", FORTRESS_OF_RESISTANCE);
		
		addFirstTalkId(MESSENGER);
		addKillId(BLOODY_LORD_NURKA);
		addAttackId(BLOODY_LORD_NURKA);
		
		try
		{
			_nurka = new Spawn(BLOODY_LORD_NURKA);
			_nurka.setRespawnDelay(10800);
			_nurka.setLoc(NURKA_COORDS[0]);
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to initialize a spawn.", e);
		}
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return getHtmlText("partisan_ordery_brakel001.htm").replace("%nextSiege%", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(_hall.getSiegeDate().getTime()));
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (!_hall.isInSiege() || !(attacker instanceof Playable))
			return null;
		
		final Clan clan = attacker.getActingPlayer().getClan();
		if (clan != null)
			_damageToNurka.merge(clan.getClanId(), damage, Integer::sum);
		
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		if (!_hall.isInSiege())
			return null;
		
		_missionAccomplished = true;
		
		npc.getSpawn().setRespawnState(false);
		npc.deleteMe();
		
		cancelSiegeTask();
		endSiege();
		return null;
	}
	
	@Override
	public Clan getWinner()
	{
		// If none did damages, simply return null.
		if (_damageToNurka.isEmpty())
			return null;
		
		// Retrieve clanId who did the biggest amount of damage.
		final int clanId = Collections.max(_damageToNurka.entrySet(), Map.Entry.comparingByValue()).getKey();
		
		// Clear the Map for future usage.
		_damageToNurka.clear();
		
		// Return the Clan winner.
		return ClanTable.getInstance().getClan(clanId);
	}
	
	@Override
	public void onSiegeStarts()
	{
		_nurka.doSpawn(false);
	}
}