package net.sf.l2j.gameserver.data.manager;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.commons.logging.CLogger;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.spawn.Spawn;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;

public class DayNightManager
{
	private static final CLogger LOGGER = new CLogger(DayNightManager.class.getName());
	
	private final List<Spawn> _dayCreatures = new ArrayList<>();
	private final List<Spawn> _nightCreatures = new ArrayList<>();
	
	protected DayNightManager()
	{
	}
	
	public void addDayCreature(Spawn spawnDat)
	{
		_dayCreatures.add(spawnDat);
	}
	
	public void addNightCreature(Spawn spawnDat)
	{
		_nightCreatures.add(spawnDat);
	}
	
	public void spawnCreatures(boolean isNight)
	{
		final List<Spawn> creaturesToUnspawn = (isNight) ? _dayCreatures : _nightCreatures;
		final List<Spawn> creaturesToSpawn = (isNight) ? _nightCreatures : _dayCreatures;
		
		for (Spawn spawn : creaturesToUnspawn)
		{
			spawn.setRespawnState(false);
			
			final Npc last = spawn.getNpc();
			if (last != null)
				last.deleteMe();
		}
		
		for (Spawn spawn : creaturesToSpawn)
		{
			spawn.setRespawnState(true);
			spawn.doSpawn(false);
		}
		
		LOGGER.info("Loaded {} creatures spawns.", ((isNight) ? "night" : "day"));
	}
	
	public void notifyChangeMode()
	{
		if (_nightCreatures.isEmpty() && _dayCreatures.isEmpty())
			return;
		
		spawnCreatures(GameTimeTaskManager.getInstance().isNight());
	}
	
	public void cleanUp()
	{
		_nightCreatures.clear();
		_dayCreatures.clear();
	}
	
	public static DayNightManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DayNightManager INSTANCE = new DayNightManager();
	}
}