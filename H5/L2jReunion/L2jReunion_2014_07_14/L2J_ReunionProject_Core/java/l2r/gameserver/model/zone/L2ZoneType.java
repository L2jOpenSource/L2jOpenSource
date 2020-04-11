/*
 * Copyright (C) 2004-2014 L2J Server
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
package l2r.gameserver.model.zone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.enums.QuestEventType;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for any zone type handles basic operations.
 * @author durgus
 */
public abstract class L2ZoneType
{
	protected static final Logger _log = LoggerFactory.getLogger(L2ZoneType.class);
	
	private final int _id;
	protected L2ZoneForm _zone;
	protected FastMap<Integer, L2Character> _characterList;
	
	/** Parameters to affect specific characters */
	private boolean _checkAffected = false;
	private String _name = null;
	private int _instanceId = -1;
	private String _instanceTemplate = "";
	private int _minLvl;
	private int _maxLvl;
	private int[] _race;
	private int[] _class;
	private char _classType;
	private Map<QuestEventType, List<Quest>> _questEvents;
	private InstanceType _target = InstanceType.L2Character; // default all chars
	private boolean _allowStore;
	private boolean _enabled;
	private AbstractZoneSettings _settings;
	
	protected L2ZoneType(int id)
	{
		_id = id;
		_characterList = new FastMap<>();
		_characterList.shared();
		
		_minLvl = 0;
		_maxLvl = 0xFF;
		
		_classType = 0;
		
		_race = null;
		_class = null;
		_allowStore = true;
		_enabled = true;
	}
	
	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return _id;
	}
	
	/**
	 * Setup new parameters for this zone
	 * @param name
	 * @param value
	 */
	public void setParameter(String name, String value)
	{
		_checkAffected = true;
		
		// Zone name
		if (name.equals("name"))
		{
			_name = value;
		}
		else if (name.equals("instanceId"))
		{
			_instanceId = Integer.parseInt(value);
		}
		else if (name.equals("instanceTemplate"))
		{
			_instanceTemplate = value;
			_instanceId = InstanceManager.getInstance().createDynamicInstance(value);
		}
		// Minimum level
		else if (name.equals("affectedLvlMin"))
		{
			_minLvl = Integer.parseInt(value);
		}
		// Maximum level
		else if (name.equals("affectedLvlMax"))
		{
			_maxLvl = Integer.parseInt(value);
		}
		// Affected Races
		else if (name.equals("affectedRace"))
		{
			// Create a new array holding the affected race
			if (_race == null)
			{
				_race = new int[1];
				_race[0] = Integer.parseInt(value);
			}
			else
			{
				int[] temp = new int[_race.length + 1];
				
				int i = 0;
				for (; i < _race.length; i++)
				{
					temp[i] = _race[i];
				}
				
				temp[i] = Integer.parseInt(value);
				
				_race = temp;
			}
		}
		// Affected classes
		else if (name.equals("affectedClassId"))
		{
			// Create a new array holding the affected classIds
			if (_class == null)
			{
				_class = new int[1];
				_class[0] = Integer.parseInt(value);
			}
			else
			{
				int[] temp = new int[_class.length + 1];
				
				int i = 0;
				for (; i < _class.length; i++)
				{
					temp[i] = _class[i];
				}
				
				temp[i] = Integer.parseInt(value);
				
				_class = temp;
			}
		}
		// Affected class type
		else if (name.equals("affectedClassType"))
		{
			if (value.equals("Fighter"))
			{
				_classType = 1;
			}
			else
			{
				_classType = 2;
			}
		}
		else if (name.equals("targetClass"))
		{
			_target = Enum.valueOf(InstanceType.class, value);
		}
		else if (name.equals("allowStore"))
		{
			_allowStore = Boolean.parseBoolean(value);
		}
		else if (name.equals("default_enabled"))
		{
			_enabled = Boolean.parseBoolean(value);
		}
		else
		{
			_log.info(getClass().getSimpleName() + ": Unknown parameter - " + name + " in zone: " + getId());
		}
	}
	
	/**
	 * @param character the player to verify.
	 * @return {@code true} if the given character is affected by this zone, {@code false} otherwise.
	 */
	private boolean isAffected(L2Character character)
	{
		// Check lvl
		if ((character.getLevel() < _minLvl) || (character.getLevel() > _maxLvl))
		{
			return false;
		}
		
		// check obj class
		if (!character.isInstanceTypes(_target))
		{
			return false;
		}
		
		if (character instanceof L2PcInstance)
		{
			// Check class type
			if (_classType != 0)
			{
				if (((L2PcInstance) character).isMageClass())
				{
					if (_classType == 1)
					{
						return false;
					}
				}
				else if (_classType == 2)
				{
					return false;
				}
			}
			
			// Check race
			if (_race != null)
			{
				boolean ok = false;
				
				for (int element : _race)
				{
					if (((L2PcInstance) character).getRace().ordinal() == element)
					{
						ok = true;
						break;
					}
				}
				
				if (!ok)
				{
					return false;
				}
			}
			
			// Check class
			if (_class != null)
			{
				boolean ok = false;
				
				for (int _clas : _class)
				{
					if (((L2PcInstance) character).getClassId().ordinal() == _clas)
					{
						ok = true;
						break;
					}
				}
				
				if (!ok)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Set the zone for this L2ZoneType Instance
	 * @param zone
	 */
	public void setZone(L2ZoneForm zone)
	{
		if (_zone != null)
		{
			throw new IllegalStateException("Zone already set");
		}
		_zone = zone;
	}
	
	/**
	 * Returns this zones zone form.
	 * @return {@link #_zone}
	 */
	public L2ZoneForm getZone()
	{
		return _zone;
	}
	
	/**
	 * Set the zone name.
	 * @param name
	 */
	public void setName(String name)
	{
		_name = name;
	}
	
	/**
	 * Returns zone name
	 * @return
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * Set the zone instanceId.
	 * @param instanceId
	 */
	public void setInstanceId(int instanceId)
	{
		_instanceId = instanceId;
	}
	
	/**
	 * Returns zone instanceId
	 * @return
	 */
	public int getInstanceId()
	{
		return _instanceId;
	}
	
	/**
	 * Returns zone instanceTemplate
	 * @return
	 */
	public String getInstanceTemplate()
	{
		return _instanceTemplate;
	}
	
	/**
	 * Checks if the given coordinates are within zone's plane
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isInsideZone(int x, int y)
	{
		return _zone.isInsideZone(x, y, _zone.getHighZ());
	}
	
	/**
	 * Checks if the given coordinates are within the zone, ignores instanceId check
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public boolean isInsideZone(int x, int y, int z)
	{
		return _zone.isInsideZone(x, y, z);
	}
	
	/**
	 * Checks if the given coordinates are within the zone and the instanceId used matched the zone's instanceId
	 * @param x
	 * @param y
	 * @param z
	 * @param instanceId
	 * @return
	 */
	public boolean isInsideZone(int x, int y, int z, int instanceId)
	{
		// It will check if coords are within the zone if the given instanceId or
		// the zone's _instanceId are in the multiverse or they match
		if ((_instanceId == -1) || (instanceId == -1) || (_instanceId == instanceId))
		{
			return _zone.isInsideZone(x, y, z);
		}
		
		return false;
	}
	
	/**
	 * Checks if the given object is inside the zone.
	 * @param object
	 * @return
	 */
	public boolean isInsideZone(L2Object object)
	{
		return isInsideZone(object.getX(), object.getY(), object.getZ(), object.getInstanceId());
	}
	
	public double getDistanceToZone(int x, int y)
	{
		return getZone().getDistanceToZone(x, y);
	}
	
	public double getDistanceToZone(L2Object object)
	{
		return getZone().getDistanceToZone(object.getX(), object.getY());
	}
	
	public void revalidateInZone(L2Character character)
	{
		// If the character can't be affected by this zone return
		if (_checkAffected)
		{
			if (!isAffected(character))
			{
				return;
			}
		}
		
		// If the object is inside the zone...
		if (isInsideZone(character))
		{
			// Was the character not yet inside this zone?
			if (!_characterList.containsKey(character.getObjectId()))
			{
				List<Quest> quests = getQuestByEvent(QuestEventType.ON_ENTER_ZONE);
				if (quests != null)
				{
					for (Quest quest : quests)
					{
						quest.notifyEnterZone(character, this);
					}
				}
				_characterList.put(character.getObjectId(), character);
				onEnter(character);
			}
		}
		else
		{
			// Was the character inside this zone?
			if (_characterList.containsKey(character.getObjectId()))
			{
				List<Quest> quests = getQuestByEvent(QuestEventType.ON_EXIT_ZONE);
				if (quests != null)
				{
					for (Quest quest : quests)
					{
						quest.notifyExitZone(character, this);
					}
				}
				_characterList.remove(character.getObjectId());
				onExit(character);
			}
		}
	}
	
	/**
	 * Force fully removes a character from the zone Should use during teleport / logoff
	 * @param character
	 */
	public void removeCharacter(L2Character character)
	{
		if (_characterList.containsKey(character.getObjectId()))
		{
			List<Quest> quests = getQuestByEvent(QuestEventType.ON_EXIT_ZONE);
			if (quests != null)
			{
				for (Quest quest : quests)
				{
					quest.notifyExitZone(character, this);
				}
			}
			_characterList.remove(character.getObjectId());
			onExit(character);
		}
	}
	
	/**
	 * Will scan the zones char list for the character
	 * @param character
	 * @return
	 */
	public boolean isCharacterInZone(L2Character character)
	{
		return _characterList.containsKey(character.getObjectId());
	}
	
	public AbstractZoneSettings getSettings()
	{
		return _settings;
	}
	
	public void setSettings(AbstractZoneSettings settings)
	{
		if (_settings != null)
		{
			_settings.clear();
		}
		_settings = settings;
	}
	
	protected abstract void onEnter(L2Character character);
	
	protected abstract void onExit(L2Character character);
	
	public abstract void onDieInside(L2Character character);
	
	public abstract void onReviveInside(L2Character character);
	
	public Map<Integer, L2Character> getCharacters()
	{
		return _characterList;
	}
	
	public Collection<L2Character> getCharactersInside()
	{
		return _characterList.values();
	}
	
	public List<L2PcInstance> getPlayersInside()
	{
		List<L2PcInstance> players = new ArrayList<>();
		for (L2Character ch : _characterList.values())
		{
			if ((ch != null) && ch.isPlayer())
			{
				players.add(ch.getActingPlayer());
			}
		}
		
		return players;
	}
	
	public void addQuestEvent(QuestEventType EventType, Quest q)
	{
		if (_questEvents == null)
		{
			_questEvents = new HashMap<>();
		}
		List<Quest> questByEvents = _questEvents.get(EventType);
		if (questByEvents == null)
		{
			questByEvents = new ArrayList<>();
		}
		if (!questByEvents.contains(q))
		{
			questByEvents.add(q);
		}
		_questEvents.put(EventType, questByEvents);
	}
	
	public List<Quest> getQuestByEvent(QuestEventType EventType)
	{
		if (_questEvents == null)
		{
			return null;
		}
		return _questEvents.get(EventType);
	}
	
	/**
	 * Broadcasts packet to all players inside the zone
	 * @param packet
	 */
	public void broadcastPacket(L2GameServerPacket packet)
	{
		if (_characterList.isEmpty())
		{
			return;
		}
		
		for (L2Character character : _characterList.values())
		{
			if ((character != null) && character.isPlayer())
			{
				character.sendPacket(packet);
			}
		}
	}
	
	public InstanceType getTargetType()
	{
		return _target;
	}
	
	public void setTargetType(InstanceType type)
	{
		_target = type;
		_checkAffected = true;
	}
	
	public boolean getAllowStore()
	{
		return _allowStore;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + _id + "]";
	}
	
	public void visualizeZone(int z)
	{
		getZone().visualizeZone(z);
	}
	
	public void setEnabled(boolean state)
	{
		_enabled = state;
	}
	
	public boolean isEnabled()
	{
		return _enabled;
	}
}
