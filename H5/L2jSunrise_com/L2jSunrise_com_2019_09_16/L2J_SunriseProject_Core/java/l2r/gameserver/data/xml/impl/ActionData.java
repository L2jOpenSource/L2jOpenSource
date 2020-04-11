/*
 * This file is part of the L2J Sunrise project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.data.xml.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.holders.ActionDataHolder;
import l2r.util.data.xml.IXmlReader.IXmlReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * @author UnAfraid
 */
public class ActionData implements IXmlReader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ActionData.class);
	
	private final Map<Integer, ActionDataHolder> _actionData = new HashMap<>();
	private final Map<Integer, Integer> _actionSkillsData = new HashMap<>(); // skillId, actionId
	
	protected ActionData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_actionData.clear();
		_actionSkillsData.clear();
		parseDatapackFile("data/xml/other/ActionData.xml");
		_actionData.values().stream().filter(h -> h.getHandler().equals("PetSkillUse") || h.getHandler().equals("ServitorSkillUse")).forEach(h -> _actionSkillsData.put(h.getOptionId(), h.getId()));
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _actionData.size() + " player actions.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "action", actionNode ->
		{
			final ActionDataHolder holder = new ActionDataHolder(new StatsSet(parseAttributes(actionNode)));
			_actionData.put(holder.getId(), holder);
		}));
	}
	
	/**
	 * @param id
	 * @return the ActionDataHolder for specified id
	 */
	public ActionDataHolder getActionData(int id)
	{
		return _actionData.get(id);
	}
	
	/**
	 * @param skillId
	 * @return the actionId corresponding to the skillId or -1 if no actionId is found for the specified skill.
	 */
	public int getSkillActionId(int skillId)
	{
		return _actionSkillsData.getOrDefault(skillId, -1);
	}
	
	/**
	 * Gets the single instance of ActionData.
	 * @return single instance of ActionData
	 */
	public static final ActionData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ActionData _instance = new ActionData();
	}
}
