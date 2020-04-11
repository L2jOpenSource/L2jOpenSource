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
package l2r.gameserver.network.clientpackets;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import l2r.Config;
import l2r.gameserver.data.sql.CharNameTable;
import l2r.gameserver.data.xml.impl.InitialEquipmentData;
import l2r.gameserver.data.xml.impl.InitialShortcutData;
import l2r.gameserver.data.xml.impl.PlayerTemplateData;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.data.xml.impl.SkillTreesData;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.model.L2SkillLearn;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.appearance.PcAppearance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.stat.PcStat;
import l2r.gameserver.model.actor.templates.L2PcTemplate;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.events.Containers;
import l2r.gameserver.model.events.EventDispatcher;
import l2r.gameserver.model.events.impl.character.player.OnPlayerCreate;
import l2r.gameserver.model.items.PcItemTemplate;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.network.L2GameClient;
import l2r.gameserver.network.serverpackets.CharCreateFail;
import l2r.gameserver.network.serverpackets.CharCreateOk;
import l2r.gameserver.network.serverpackets.CharSelectionInfo;
import l2r.gameserver.util.Util;

import gr.sr.configsEngine.configs.impl.CustomServerConfigs;

@SuppressWarnings("unused")
public final class CharacterCreate extends L2GameClientPacket
{
	private static final String _C__0C_CHARACTERCREATE = "[C] 0C CharacterCreate";
	protected static final Logger _logAccounting = Logger.getLogger("accounting");
	
	// cSdddddddddddd
	private String _name;
	private int _race;
	private byte _sex;
	private int _classId;
	private int _int;
	private int _str;
	private int _con;
	private int _men;
	private int _dex;
	private int _wit;
	private byte _hairStyle;
	private byte _hairColor;
	private byte _face;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
		_race = readD();
		_sex = (byte) readD();
		_classId = readD();
		_int = readD();
		_str = readD();
		_con = readD();
		_men = readD();
		_dex = readD();
		_wit = readD();
		_hairStyle = (byte) readD();
		_hairColor = (byte) readD();
		_face = (byte) readD();
	}
	
	@Override
	protected void runImpl()
	{
		// Last Verified: May 30, 2009 - Gracia Final - Players are able to create characters with names consisting of as little as 1,2,3 letter/number combinations.
		if ((_name.length() < 1) || (_name.length() > 16))
		{
			if (Config.DEBUG)
			{
				_log.info("Character Creation Failure: Character name " + _name + " is invalid. Message generated: Your title cannot exceed 16 characters in length. Please try again.");
			}
			
			sendPacket(new CharCreateFail(CharCreateFail.REASON_16_ENG_CHARS));
			return;
		}
		
		if (Config.FORBIDDEN_NAMES.length > 1)
		{
			for (String st : Config.FORBIDDEN_NAMES)
			{
				if (_name.toLowerCase().contains(st.toLowerCase()))
				{
					sendPacket(new CharCreateFail(CharCreateFail.REASON_INCORRECT_NAME));
					return;
				}
			}
		}
		
		// Last Verified: May 30, 2009 - Gracia Final
		if (!Util.isAlphaNumeric(_name) || !isValidName(_name))
		{
			if (Config.DEBUG)
			{
				_log.info("Character Creation Failure: Character name " + _name + " is invalid. Message generated: Incorrect name. Please try again.");
			}
			
			sendPacket(new CharCreateFail(CharCreateFail.REASON_INCORRECT_NAME));
			return;
		}
		
		if ((_face > 2) || (_face < 0))
		{
			_log.warn("Character Creation Failure: Character face " + _face + " is invalid. Possible client hack. " + getClient());
			
			sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
			return;
		}
		
		if ((_hairStyle < 0) || ((_sex == 0) && (_hairStyle > 4)) || ((_sex != 0) && (_hairStyle > 6)))
		{
			_log.warn("Character Creation Failure: Character hair style " + _hairStyle + " is invalid. Possible client hack. " + getClient());
			
			sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
			return;
		}
		
		if ((_hairColor > 3) || (_hairColor < 0))
		{
			_log.warn("Character Creation Failure: Character hair color " + _hairColor + " is invalid. Possible client hack. " + getClient());
			
			sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
			return;
		}
		
		L2PcInstance newChar = null;
		L2PcTemplate template = null;
		
		/*
		 * DrHouse: Since checks for duplicate names are done using SQL, lock must be held until data is written to DB as well.
		 */
		synchronized (CharNameTable.getInstance())
		{
			if ((CharNameTable.getInstance().getAccountCharacterCount(getClient().getAccountName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT) && (Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0))
			{
				if (Config.DEBUG)
				{
					_log.info("Max number of characters reached. Creation failed.");
				}
				
				sendPacket(new CharCreateFail(CharCreateFail.REASON_TOO_MANY_CHARACTERS));
				return;
			}
			else if (CharNameTable.getInstance().doesCharNameExist(_name))
			{
				if (Config.DEBUG)
				{
					_log.info("Character Creation Failure: Message generated: You cannot create another character. Please delete the existing character and try again.");
				}
				
				sendPacket(new CharCreateFail(CharCreateFail.REASON_NAME_ALREADY_EXISTS));
				return;
			}
			
			template = PlayerTemplateData.getInstance().getTemplate(_classId);
			if ((template == null) || (ClassId.getClassId(_classId).level() > 0))
			{
				if (Config.DEBUG)
				{
					_log.info("Character Creation Failure: " + _name + " classId: " + _classId + " Template: " + template + " Message generated: Your character creation has failed.");
				}
				
				sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
				return;
			}
			final PcAppearance app = new PcAppearance(_face, _hairColor, _hairStyle, _sex != 0);
			newChar = L2PcInstance.create(template, getClient().getAccountName(), _name, app);
		}
		
		getClient().setCharCreation(true);
		
		// HP and MP are at maximum and CP is zero by default.
		newChar.setCurrentHp(newChar.getMaxHp());
		newChar.setCurrentMp(newChar.getMaxMp());
		// newChar.setMaxLoad(template.getBaseLoad());
		
		sendPacket(new CharCreateOk());
		initNewChar(getClient(), newChar);
		
		LogRecord record = new LogRecord(Level.INFO, "Created new character");
		record.setParameters(new Object[]
		{
			newChar,
			getClient()
		});
		_logAccounting.log(record);
	}
	
	private boolean isValidName(String text)
	{
		boolean result = true;
		String test = text;
		Pattern pattern;
		// UnAfraid: TODO: Move that into Config
		try
		{
			pattern = Pattern.compile(Config.CNAME_TEMPLATE);
		}
		catch (PatternSyntaxException e) // case of illegal pattern
		{
			_log.warn("ERROR : Character name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		Matcher regexp = pattern.matcher(test);
		if (!regexp.matches())
		{
			result = false;
		}
		return result;
	}
	
	private void initNewChar(L2GameClient client, L2PcInstance newChar)
	{
		if (Config.DEBUG)
		{
			_log.info("Character init start");
		}
		
		L2World.getInstance().storeObject(newChar);
		
		if (Config.STARTING_ADENA > 0)
		{
			newChar.addAdena("Init", Config.STARTING_ADENA, null, false);
		}
		
		final L2PcTemplate template = newChar.getTemplate();
		Location createLoc = template.getCreationPoint();
		newChar.setXYZInvisible(createLoc.getX(), createLoc.getY(), createLoc.getZ());
		if (CustomServerConfigs.ENABLE_STARTING_TITLE)
		{
			newChar.setTitle(CustomServerConfigs.STARTING_TITLE);
		}
		else
		{
			newChar.setTitle("");
		}
		
		if (Config.ENABLE_VITALITY)
		{
			newChar.setVitalityPoints(Math.min(Config.STARTING_VITALITY_POINTS, PcStat.MAX_VITALITY_POINTS), true);
		}
		if (Config.STARTING_LEVEL > 1)
		{
			newChar.getStat().addLevel((byte) (Config.STARTING_LEVEL - 1));
		}
		if (Config.STARTING_SP > 0)
		{
			newChar.getStat().addSp(Config.STARTING_SP);
		}
		
		final List<PcItemTemplate> initialItems = InitialEquipmentData.getInstance().getEquipmentList(newChar.getClassId());
		if (initialItems != null)
		{
			for (PcItemTemplate ie : initialItems)
			{
				final L2ItemInstance item = newChar.getInventory().addItem("Init", ie.getId(), ie.getCount(), newChar, null);
				if (item == null)
				{
					_log.warn("Could not create item during char creation: itemId " + ie.getId() + ", amount " + ie.getCount() + ".");
					continue;
				}
				
				if (item.isEquipable() && ie.isEquipped())
				{
					newChar.getInventory().equipItem(item);
				}
			}
		}
		
		for (L2SkillLearn skill : SkillTreesData.getInstance().getAvailableSkills(newChar, newChar.getClassId(), false, true))
		{
			if (Config.DEBUG)
			{
				_log.info("Adding starter skill:" + skill.getSkillId() + " / " + skill.getSkillLevel());
			}
			
			newChar.addSkill(SkillData.getInstance().getSkill(skill.getSkillId(), skill.getSkillLevel()), true);
		}
		
		// Register all shortcuts for actions, skills and items for this new character.
		InitialShortcutData.getInstance().registerAllShortcuts(newChar);
		
		if (!Config.DISABLE_TUTORIAL)
		{
			startTutorialQuest(newChar);
		}
		
		EventDispatcher.getInstance().notifyEvent(new OnPlayerCreate(newChar, newChar.getObjectId(), newChar.getName(), client), Containers.Players());
		
		newChar.setOnlineStatus(true, false);
		newChar.deleteMe();
		
		final CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		client.sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
		client.setCharCreation(false);
		
		if (Config.DEBUG)
		{
			_log.info("Character init end");
		}
	}
	
	/**
	 * TODO: Unhardcode it using the new listeners.
	 * @param player
	 */
	public void startTutorialQuest(L2PcInstance player)
	{
		if (player.getQuestState(Quest.TUTORIAL) == null)
		{
			final Quest q = QuestManager.getInstance().getQuest(Quest.TUTORIAL);
			if (q != null)
			{
				q.newQuestState(player).setState(State.STARTED);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__0C_CHARACTERCREATE;
	}
}
