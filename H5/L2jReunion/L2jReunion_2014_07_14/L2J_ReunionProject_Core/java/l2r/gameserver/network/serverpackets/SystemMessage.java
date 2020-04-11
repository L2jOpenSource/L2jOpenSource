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
package l2r.gameserver.network.serverpackets;

import java.io.PrintStream;
import java.util.Arrays;

import l2r.Config;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.datatables.xml.DoorData;
import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.Elementals;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2DoorInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.entity.Castle;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.SystemMessageId.SMLocalisation;

public final class SystemMessage extends L2GameServerPacket
{
	private static final SMParam[] EMPTY_PARAM_ARRAY = new SMParam[0];
	
	private static final class SMParam
	{
		private final byte _type;
		private final Object _value;
		
		public SMParam(final byte type, final Object value)
		{
			_type = type;
			_value = value;
		}
		
		public final byte getType()
		{
			return _type;
		}
		
		public final Object getValue()
		{
			return _value;
		}
		
		public final String getStringValue()
		{
			return (String) _value;
		}
		
		public final int getIntValue()
		{
			return ((Integer) _value).intValue();
		}
		
		public final long getLongValue()
		{
			return ((Long) _value).longValue();
		}
		
		public final int[] getIntArrayValue()
		{
			return (int[]) _value;
		}
	}
	
	// TODO: UnAfraid: Check/Implement id's: 14,15.
	// 15 exists in goddess of destruction but also may works in h5 needs to be verified!
	// private static final byte TYPE_CLASS_ID = 15;
	// id 14 unknown
	private static final byte TYPE_SYSTEM_STRING = 13;
	private static final byte TYPE_PLAYER_NAME = 12;
	private static final byte TYPE_DOOR_NAME = 11;
	private static final byte TYPE_INSTANCE_NAME = 10;
	private static final byte TYPE_ELEMENT_NAME = 9;
	// id 8 - same as 3
	private static final byte TYPE_ZONE_NAME = 7;
	private static final byte TYPE_LONG_NUMBER = 6;
	private static final byte TYPE_CASTLE_NAME = 5;
	private static final byte TYPE_SKILL_NAME = 4;
	private static final byte TYPE_ITEM_NAME = 3;
	private static final byte TYPE_NPC_NAME = 2;
	private static final byte TYPE_INT_NUMBER = 1;
	private static final byte TYPE_TEXT = 0;
	
	public static final SystemMessage sendString(final String text)
	{
		if (text == null)
		{
			throw new NullPointerException();
		}
		
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
		sm.addString(text);
		return sm;
	}
	
	public static final SystemMessage getSystemMessage(final SystemMessageId smId)
	{
		SystemMessage sm = smId.getStaticSystemMessage();
		if (sm != null)
		{
			return sm;
		}
		
		sm = new SystemMessage(smId);
		if (smId.getParamCount() == 0)
		{
			smId.setStaticSystemMessage(sm);
		}
		
		return sm;
	}
	
	/**
	 * Use {@link #getSystemMessage(SystemMessageId)} where possible instead
	 * @param id
	 * @return the system message associated to the given Id.
	 */
	public static SystemMessage getSystemMessage(int id)
	{
		return getSystemMessage(SystemMessageId.getSystemMessageId(id));
	}
	
	private final SystemMessageId _smId;
	private SMParam[] _params;
	private int _paramIndex;
	
	private SystemMessage(final SystemMessageId smId)
	{
		final int paramCount = smId.getParamCount();
		_smId = smId;
		_params = paramCount != 0 ? new SMParam[paramCount] : EMPTY_PARAM_ARRAY;
	}
	
	/**
	 * Use SystemMessage.getSystemMessage(SystemMessageId smId) where possible instead
	 * @param id
	 * @deprecated
	 */
	@Deprecated
	private SystemMessage(final int id)
	{
		this(SystemMessageId.getSystemMessageId(id));
	}
	
	private final void append(final SMParam param)
	{
		if (_paramIndex >= _params.length)
		{
			_params = Arrays.copyOf(_params, _paramIndex + 1);
			_smId.setParamCount(_paramIndex + 1);
			_log.info("Wrong parameter count '" + (_paramIndex + 1) + "' for SystemMessageId: " + _smId);
		}
		
		_params[_paramIndex++] = param;
	}
	
	public final SystemMessage addString(final String text)
	{
		append(new SMParam(TYPE_TEXT, text));
		return this;
	}
	
	/**
	 * Appends a Castle name parameter type, the name will be read from CastleName-e.dat.<br>
	 * <ul>
	 * <li>1-9 Castle names</li>
	 * <li>21 Fortress of Resistance</li>
	 * <li>22-33 Clan Hall names</li>
	 * <li>34 Devastated Castle</li>
	 * <li>35 Bandit Stronghold</li>
	 * <li>36-61 Clan Hall names</li>
	 * <li>62 Rainbow Springs</li>
	 * <li>63 Wild Beast Reserve</li>
	 * <li>64 Fortress of the Dead</li>
	 * <li>81-89 Territory names</li>
	 * <li>90-100 null</li>
	 * <li>101-121 Fortress names</li>
	 * </ul>
	 * @param number the conquerable entity
	 * @return the system message with the proper parameter
	 */
	public final SystemMessage addCastleId(final int number)
	{
		append(new SMParam(TYPE_CASTLE_NAME, number));
		return this;
	}
	
	public final SystemMessage addInt(final int number)
	{
		append(new SMParam(TYPE_INT_NUMBER, number));
		return this;
	}
	
	public final SystemMessage addLong(final long number)
	{
		append(new SMParam(TYPE_LONG_NUMBER, number));
		return this;
	}
	
	public final SystemMessage addCharName(final L2Character cha)
	{
		if (cha.isNpc())
		{
			L2Npc npc = (L2Npc) cha;
			if (npc.getTemplate().isServerSideName())
			{
				return addString(npc.getTemplate().getName());
			}
			return addNpcName(npc);
		}
		else if (cha.isPlayer())
		{
			return addPcName(cha.getActingPlayer());
		}
		else if (cha.isSummon())
		{
			L2Summon summon = (L2Summon) cha;
			if (summon.getTemplate().isServerSideName())
			{
				return addString(summon.getTemplate().getName());
			}
			return addNpcName(summon);
		}
		else if (cha.isDoor())
		{
			L2DoorInstance door = (L2DoorInstance) cha;
			return addDoorName(door.getId());
		}
		return addString(cha.getName());
	}
	
	public final SystemMessage addPcName(final L2PcInstance pc)
	{
		append(new SMParam(TYPE_PLAYER_NAME, pc.getAppearance().getVisibleName()));
		return this;
	}
	
	/**
	 * ID from doorData.xml
	 * @param doorId
	 * @return
	 */
	public SystemMessage addDoorName(int doorId)
	{
		append(new SMParam(TYPE_DOOR_NAME, doorId));
		return this;
	}
	
	public final SystemMessage addNpcName(L2Npc npc)
	{
		return addNpcName(npc.getTemplate());
	}
	
	public final SystemMessage addNpcName(final L2Summon npc)
	{
		return addNpcName(npc.getId());
	}
	
	public final SystemMessage addNpcName(final L2NpcTemplate template)
	{
		if (template.isCustom())
		{
			return addString(template.getName());
		}
		return addNpcName(template.getId());
	}
	
	public final SystemMessage addNpcName(final int id)
	{
		append(new SMParam(TYPE_NPC_NAME, 1000000 + id));
		return this;
	}
	
	public final SystemMessage addItemName(final L2ItemInstance item)
	{
		return addItemName(item.getId());
	}
	
	public final SystemMessage addItemName(final L2Item item)
	{
		return addItemName(item.getId());
	}
	
	public final SystemMessage addItemName(final int id)
	{
		L2Item item = ItemData.getInstance().getTemplate(id);
		if (item.getDisplayId() != id)
		{
			return addString(item.getName());
		}
		
		append(new SMParam(TYPE_ITEM_NAME, id));
		return this;
	}
	
	public final SystemMessage addZoneName(final int x, final int y, final int z)
	{
		append(new SMParam(TYPE_ZONE_NAME, new int[]
		{
			x,
			y,
			z
		}));
		return this;
	}
	
	public final SystemMessage addSkillName(final L2Effect effect)
	{
		return addSkillName(effect.getSkill());
	}
	
	public final SystemMessage addSkillName(final L2Skill skill)
	{
		if (skill.getId() != skill.getDisplayId())
		{
			return addString(skill.getName());
		}
		return addSkillName(skill.getId(), skill.getLevel());
	}
	
	public final SystemMessage addSkillName(final int id)
	{
		return addSkillName(id, 1);
	}
	
	public final SystemMessage addSkillName(final int id, final int lvl)
	{
		append(new SMParam(TYPE_SKILL_NAME, new int[]
		{
			id,
			lvl
		}));
		return this;
	}
	
	/**
	 * Elemental name - 0(Fire) ...
	 * @param type
	 * @return
	 */
	public final SystemMessage addElemental(final int type)
	{
		append(new SMParam(TYPE_ELEMENT_NAME, type));
		return this;
	}
	
	/**
	 * ID from sysstring-e.dat
	 * @param type
	 * @return
	 */
	public final SystemMessage addSystemString(final int type)
	{
		append(new SMParam(TYPE_SYSTEM_STRING, type));
		return this;
	}
	
	/**
	 * Instance name from instantzonedata-e.dat
	 * @param type id of instance
	 * @return
	 */
	public final SystemMessage addInstanceName(final int type)
	{
		append(new SMParam(TYPE_INSTANCE_NAME, type));
		return this;
	}
	
	public final SystemMessageId getSystemMessageId()
	{
		return _smId;
	}
	
	public final SystemMessage getLocalizedMessage(final String lang)
	{
		if (!Config.L2JMOD_MULTILANG_SM_ENABLE || (_smId == SystemMessageId.S1))
		{
			return this;
		}
		
		final SMLocalisation sml = _smId.getLocalisation(lang);
		if (sml == null)
		{
			return this;
		}
		
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
		final Object[] params = new Object[_paramIndex];
		
		SMParam param;
		for (int i = 0; i < _paramIndex; i++)
		{
			param = _params[i];
			
			switch (param.getType())
			{
				case TYPE_TEXT:
				case TYPE_PLAYER_NAME:
				{
					params[i] = param.getValue();
					break;
				}
				
				case TYPE_LONG_NUMBER:
				{
					params[i] = param.getValue();
					break;
				}
				
				case TYPE_ITEM_NAME:
				{
					final L2Item item = ItemData.getInstance().getTemplate(param.getIntValue());
					params[i] = item == null ? "Unknown" : item.getName();
					break;
				}
				
				case TYPE_CASTLE_NAME:
				{
					final Castle castle = CastleManager.getInstance().getCastleById(param.getIntValue());
					params[i] = castle == null ? "Unknown" : castle.getName();
					break;
				}
				
				case TYPE_INT_NUMBER:
				{
					params[i] = param.getValue();
					break;
				}
				
				case TYPE_NPC_NAME:
				{
					final L2NpcTemplate template = NpcTable.getInstance().getTemplate(param.getIntValue());
					params[i] = template == null ? "Unknown" : template.getName();
					break;
				}
				
				case TYPE_ELEMENT_NAME:
				{
					params[i] = Elementals.getElementName((byte) param.getIntValue());
					break;
				}
				
				case TYPE_SYSTEM_STRING:
				{
					params[i] = "SYS-S-" + param.getIntValue(); // writeD(param.getIntValue());
					break;
				}
				
				case TYPE_INSTANCE_NAME:
				{
					final String instanceName = InstanceManager.getInstance().getInstanceIdName(param.getIntValue());
					params[i] = instanceName == null ? "Unknown" : instanceName;
					break;
				}
				
				case TYPE_DOOR_NAME:
				{
					final L2DoorInstance door = DoorData.getInstance().getDoor(param.getIntValue());
					params[i] = door == null ? "Unknown" : door.getName();
					break;
				}
				
				case TYPE_SKILL_NAME:
				{
					final int[] array = param.getIntArrayValue();
					final L2Skill skill = SkillData.getInstance().getInfo(array[0], array[1]);
					params[i] = skill == null ? "Unknown" : skill.getName();
					break;
				}
				
				case TYPE_ZONE_NAME:
				{
					final int[] array = param.getIntArrayValue();
					final L2ZoneType zone = ZoneManager.getInstance().getZone(array[0], array[1], array[2], L2ZoneType.class);
					params[i] = zone == null ? "Unknown ZONE-N-" + Arrays.toString(array) : zone.getName();
					break;
				}
			}
		}
		
		sm.addString(sml.getLocalisation(params));
		return sm;
	}
	
	public final void printMe(PrintStream out)
	{
		out.println(0x62);
		
		out.println(_smId.getId());
		out.println(_paramIndex);
		
		SMParam param;
		for (int i = 0; i < _paramIndex; i++)
		{
			param = _params[i];
			out.println(param.getType());
			
			switch (param.getType())
			{
				case TYPE_TEXT:
				case TYPE_PLAYER_NAME:
				{
					out.println(param.getStringValue());
					break;
				}
				
				case TYPE_LONG_NUMBER:
				{
					out.println(param.getLongValue());
					break;
				}
				
				case TYPE_ITEM_NAME:
				case TYPE_CASTLE_NAME:
				case TYPE_INT_NUMBER:
				case TYPE_NPC_NAME:
				case TYPE_ELEMENT_NAME:
				case TYPE_SYSTEM_STRING:
				case TYPE_INSTANCE_NAME:
				case TYPE_DOOR_NAME:
				{
					out.println(param.getIntValue());
					break;
				}
				
				case TYPE_SKILL_NAME:
				{
					final int[] array = param.getIntArrayValue();
					out.println(array[0]); // SkillId
					out.println(array[1]); // SkillLevel
					break;
				}
				
				case TYPE_ZONE_NAME:
				{
					final int[] array = param.getIntArrayValue();
					out.println(array[0]); // x
					out.println(array[1]); // y
					out.println(array[2]); // z
					break;
				}
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x62);
		
		writeD(_smId.getId());
		writeD(_paramIndex);
		
		SMParam param;
		for (int i = 0; i < _paramIndex; i++)
		{
			param = _params[i];
			writeD(param.getType());
			
			switch (param.getType())
			{
				case TYPE_TEXT:
				case TYPE_PLAYER_NAME:
				{
					writeS(param.getStringValue());
					break;
				}
				
				case TYPE_LONG_NUMBER:
				{
					writeQ(param.getLongValue());
					break;
				}
				
				case TYPE_ITEM_NAME:
				case TYPE_CASTLE_NAME:
				case TYPE_INT_NUMBER:
				case TYPE_NPC_NAME:
				case TYPE_ELEMENT_NAME:
				case TYPE_SYSTEM_STRING:
				case TYPE_INSTANCE_NAME:
				case TYPE_DOOR_NAME:
				{
					writeD(param.getIntValue());
					break;
				}
				
				case TYPE_SKILL_NAME:
				{
					final int[] array = param.getIntArrayValue();
					writeD(array[0]); // SkillId
					writeD(array[1]); // SkillLevel
					break;
				}
				
				case TYPE_ZONE_NAME:
				{
					final int[] array = param.getIntArrayValue();
					writeD(array[0]); // x
					writeD(array[1]); // y
					writeD(array[2]); // z
					break;
				}
			}
		}
	}
}