/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.instancemanager;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2r.Config;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.scripting.L2ScriptEngineManager;
import l2r.gameserver.scripting.ScriptManager;
import l2r.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quests and scripts manager.
 * @author Zoey76
 */
public final class QuestManager extends ScriptManager<Quest>
{
	protected static final Logger _log = LoggerFactory.getLogger(QuestManager.class);
	
	/** Map containing all the quests. */
	private final Map<String, Quest> _quests = new ConcurrentHashMap<>();
	/** Map containing all the scripts. */
	private final Map<String, Quest> _scripts = new ConcurrentHashMap<>();
	
	protected QuestManager()
	{
		// Prevent initialization.
	}
	
	public boolean reload(String questFolder)
	{
		final Quest q = getQuest(questFolder);
		if (q == null)
		{
			return false;
		}
		return q.reload();
	}
	
	/**
	 * Reloads a the quest by ID.
	 * @param questId the ID of the quest to be reloaded
	 * @return {@code true} if reload was successful, {@code false} otherwise
	 */
	public boolean reload(int questId)
	{
		final Quest q = getQuest(questId);
		if (q == null)
		{
			return false;
		}
		return q.reload();
	}
	
	/**
	 * Reload all quests and scripts.<br>
	 * Unload all quests and scripts and load scripts.ini.
	 */
	public void reloadAllScripts()
	{
		_log.info(getClass().getSimpleName() + ": Reloading all server scripts.");
		
		// Unload quests.
		for (Quest quest : _quests.values())
		{
			if (quest != null)
			{
				quest.unload(false);
			}
		}
		_quests.clear();
		// Unload scripts.
		for (Quest script : _scripts.values())
		{
			if (script != null)
			{
				script.unload(false);
			}
		}
		_scripts.clear();
		
		try
		{
			L2ScriptEngineManager.getInstance().executeScriptList(new File(Config.DATAPACK_ROOT, "data/scripts.ini"));
		}
		catch (IOException e)
		{
			_log.error(getClass().getSimpleName() + ": Failed loading scripts.ini, no script going to be loaded!", e);
		}
		
		report();
	}
	
	/**
	 * Logs how many quests and scripts are loaded.
	 */
	public void report()
	{
		_log.info(getClass().getSimpleName() + ": Loaded: " + _quests.size() + " quests.");
		_log.info(getClass().getSimpleName() + ": Loaded: " + _scripts.size() + " scripts.");
	}
	
	/**
	 * Calls {@link Quest#saveGlobalData()} in all quests and scripts.
	 */
	public void save()
	{
		// Save quests.
		for (Quest quest : _quests.values())
		{
			quest.saveGlobalData();
		}
		
		// Save scripts.
		for (Quest script : _scripts.values())
		{
			script.saveGlobalData();
		}
	}
	
	/**
	 * Gets a quest by name.<br>
	 * <i>For backwards compatibility, verifies scripts with the given name if the quest is not found.</i>
	 * @param name the quest name
	 * @return the quest
	 */
	public Quest getQuest(String name)
	{
		if (_quests.containsKey(name))
		{
			return _quests.get(name);
		}
		return _scripts.get(name);
	}
	
	/**
	 * Gets a quest by ID.
	 * @param questId the ID of the quest to get
	 * @return if found, the quest, {@code null} otherwise
	 */
	public Quest getQuest(int questId)
	{
		for (Quest q : _quests.values())
		{
			if (q.getId() == questId)
			{
				return q;
			}
		}
		return null;
	}
	
	/**
	 * Adds a new quest.
	 * @param quest the quest to be added
	 */
	public void addQuest(Quest quest)
	{
		// Quest does not exist, return.
		if (quest == null)
		{
			return;
		}
		
		// Quest already loaded, unload id.
		Quest old = getQuest(quest.getName());
		if (old != null)
		{
			old.unload();
		}
		
		// Add new quest.
		_quests.put(quest.getName(), quest);
		
		if (Config.ALT_DEV_SHOW_QUESTS_LOAD_IN_LOGS)
		{
			final String questName = quest.getName().contains("_") ? quest.getName().substring(quest.getName().indexOf('_') + 1) : quest.getName();
			_log.info(getClass().getSimpleName() + ": Loaded quest " + Util.splitWords(questName) + ".");
		}
	}
	
	/**
	 * Removes a script.
	 * @param script the script to remove
	 * @return {@code true} if the script was removed, {@code false} otherwise
	 */
	public boolean removeScript(Quest script)
	{
		if (_quests.containsKey(script.getName()))
		{
			_quests.remove(script.getName());
			return true;
		}
		else if (_scripts.containsKey(script.getName()))
		{
			_scripts.remove(script.getName());
			return true;
		}
		return false;
	}
	
	public Map<String, Quest> getQuests()
	{
		return _quests;
	}
	
	@Override
	public boolean unload(Quest ms)
	{
		ms.saveGlobalData();
		return removeScript(ms);
	}
	
	@Override
	public String getScriptManagerName()
	{
		return getClass().getSimpleName();
	}
	
	/**
	 * Gets all the registered scripts.
	 * @return all the scripts
	 */
	@Override
	public Map<String, Quest> getScripts()
	{
		return _scripts;
	}
	
	/**
	 * Adds a script.
	 * @param script the script to be added
	 */
	public void addScript(Quest script)
	{
		// Script already loaded, unload id.
		final Quest old = _scripts.put(script.getClass().getSimpleName(), script);
		if (old != null)
		{
			old.unload();
		}
		
		// Add new script.
		_scripts.put(script.getClass().getSimpleName(), script);
		
		if (Config.ALT_DEV_SHOW_SCRIPTS_LOAD_IN_LOGS)
		{
			_log.info(getClass().getSimpleName() + ": Loaded script " + Util.splitWords(script.getClass().getSimpleName()) + ".");
		}
	}
	
	/**
	 * Gets the single instance of {@code QuestManager}.
	 * @return single instance of {@code QuestManager}
	 */
	public static QuestManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final QuestManager _instance = new QuestManager();
	}
}
