package net.sf.l2j.gameserver.data.xml;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.util.StatsSet;

import net.sf.l2j.gameserver.model.PlayerLevel;

import org.w3c.dom.Document;

/**
 * This class loads and stores Player level related variables.
 */
public class PlayerLevelData implements IXmlReader
{
	private final Map<Integer, PlayerLevel> _levels = new HashMap<>();
	
	private int _maxLevel;
	
	protected PlayerLevelData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/playerLevels.xml");
		LOGGER.info("Loaded {} player levels.", _levels.size());
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "playerLevel", levelNode ->
		{
			final StatsSet set = parseAttributes(levelNode);
			final int level = set.getInteger("level");
			
			_levels.put(level, new PlayerLevel(set));
			
			// Check current max level.
			if (level > _maxLevel)
				_maxLevel = level;
		}));
	}
	
	/**
	 * @param level : The level to check.
	 * @return the xp death penalty related to a level.
	 */
	public PlayerLevel getPlayerLevel(int level)
	{
		return _levels.get(level);
	}
	
	public long getRequiredExpForHighestLevel()
	{
		return _levels.get(_maxLevel).getRequiredExpToLevelUp();
	}
	
	/**
	 * If you want a max at 80 & 99.99%, you have to put 81.
	 * @return the first UNREACHABLE level.
	 */
	public int getMaxLevel()
	{
		return _maxLevel;
	}
	
	public int getRealMaxLevel()
	{
		return _maxLevel - 1;
	}
	
	public static PlayerLevelData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final PlayerLevelData INSTANCE = new PlayerLevelData();
	}
}