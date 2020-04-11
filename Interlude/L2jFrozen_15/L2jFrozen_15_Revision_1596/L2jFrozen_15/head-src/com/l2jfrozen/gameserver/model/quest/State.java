package com.l2jfrozen.gameserver.model.quest;

import com.l2jfrozen.gameserver.managers.QuestManager;

/**
 * @author Luis Arias Functions in this class are used in python files
 */
public class State
{
	private final String questName;
	private final String name;
	
	/**
	 * Constructor for the state of the quest.
	 * @param name  : String pointing out the name of the quest
	 * @param quest : Quest
	 */
	public State(final String name, final Quest quest)
	{
		this.name = name;
		questName = quest.getName();
		quest.addState(this);
	}
	
	/**
	 * Add drop for the quest at this state of the quest
	 * @param npcId  : int designating the ID of the NPC
	 * @param itemId : int designating the ID of the item dropped
	 * @param chance : int designating the chance the item dropped DEPRECATING THIS...only the itemId is really needed, and even that is only here for backwards compatibility
	 */
	public void addQuestDrop(final int npcId, final int itemId, final int chance)
	{
		QuestManager.getInstance().getQuest(questName).registerItem(itemId);
	}
	
	/**
	 * @return name of the state
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @return name of the quest
	 */
	public String getQuestName()
	{
		return questName;
	}
}
