package com.l2jfrozen.gameserver.managers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.scripting.L2ScriptEngineManager;
import com.l2jfrozen.gameserver.scripting.ScriptManager;

public class QuestManager extends ScriptManager<Quest>
{
	protected static final Logger LOGGER = Logger.getLogger(QuestManager.class);
	private Map<String, Quest> quests = new HashMap<>();
	private static QuestManager instance;
	
	public static QuestManager getInstance()
	{
		if (instance == null)
		{
			instance = new QuestManager();
		}
		return instance;
	}
	
	public QuestManager()
	{
		LOGGER.info("Initializing QuestManager");
	}
	
	public final boolean reload(final String questFolder)
	{
		final Quest q = getQuest(questFolder);
		if (q == null)
		{
			return false;
		}
		return q.reload();
	}
	
	/**
	 * Reloads a the quest given by questId.<BR>
	 * <B>NOTICE: Will only work if the quest name is equal the quest folder name</B>
	 * @param  questId The id of the quest to be reloaded
	 * @return         true if reload was succesful, false otherwise
	 */
	public final boolean reload(final int questId)
	{
		final Quest q = this.getQuest(questId);
		if (q == null)
		{
			return false;
		}
		return q.reload();
	}
	
	public final void reloadAllQuests()
	{
		LOGGER.info("Reloading Server Scripts");
		// unload all scripts
		for (final Quest quest : quests.values())
		{
			if (quest != null)
			{
				quest.unload();
			}
		}
		// now load all scripts
		final File scripts = new File(Config.DATAPACK_ROOT + "/data/scripts.cfg");
		L2ScriptEngineManager.getInstance().executeScriptsList(scripts);
		QuestManager.getInstance().report();
	}
	
	public final void report()
	{
		LOGGER.info("Loaded: " + quests.size() + " quests");
	}
	
	public final void save()
	{
		for (final Quest q : getQuests().values())
		{
			q.saveGlobalData();
		}
	}
	
	// =========================================================
	// Property - Public
	public final Quest getQuest(final String name)
	{
		return getQuests().get(name);
	}
	
	public final Quest getQuest(final int questId)
	{
		for (final Quest q : getQuests().values())
		{
			if (q.getQuestIntId() == questId)
			{
				return q;
			}
		}
		return null;
	}
	
	public final void addQuest(final Quest newQuest)
	{
		if (getQuests().containsKey(newQuest.getName()))
		{
			LOGGER.info("Replaced: " + newQuest.getName() + " with a new version");
		}
		
		// Note: HashMap will replace the old value if the key already exists
		// so there is no need to explicitly try to remove the old reference.
		getQuests().put(newQuest.getName(), newQuest);
	}
	
	public final HashMap<String, Quest> getQuests()
	{
		if (quests == null)
		{
			quests = new HashMap<>();
		}
		
		return (HashMap<String, Quest>) quests;
	}
	
	/**
	 * This will reload quests
	 */
	public static void reload()
	{
		instance = new QuestManager();
	}
	
	@Override
	public Iterable<Quest> getAllManagedScripts()
	{
		return quests.values();
	}
	
	@Override
	public boolean unload(final Quest ms)
	{
		ms.saveGlobalData();
		return removeQuest(ms);
	}
	
	@Override
	public String getScriptManagerName()
	{
		return "QuestManager";
	}
	
	public final boolean removeQuest(final Quest q)
	{
		return quests.remove(q.getName()) != null;
	}
	
	public final void unloadAllQuests()
	{
		LOGGER.info("Unloading Server Quests");
		// unload all scripts
		for (final Quest quest : quests.values())
		{
			if (quest != null)
			{
				quest.unload();
			}
		}
		QuestManager.getInstance().report();
	}
}
