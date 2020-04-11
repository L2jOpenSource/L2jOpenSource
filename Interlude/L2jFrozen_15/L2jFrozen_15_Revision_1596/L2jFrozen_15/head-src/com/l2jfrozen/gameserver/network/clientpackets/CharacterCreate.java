package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.GameServer;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.CharNameTable;
import com.l2jfrozen.gameserver.datatables.sql.CharTemplateTable;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.datatables.sql.SkillTreeTable;
import com.l2jfrozen.gameserver.datatables.xml.ExperienceData;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.managers.QuestManager;
import com.l2jfrozen.gameserver.model.L2ShortCut;
import com.l2jfrozen.gameserver.model.L2SkillLearn;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.model.quest.QuestState;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.network.serverpackets.CharCreateFail;
import com.l2jfrozen.gameserver.network.serverpackets.CharCreateOk;
import com.l2jfrozen.gameserver.network.serverpackets.CharSelectInfo;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2PcTemplate;
import com.l2jfrozen.gameserver.util.Util;

import main.EngineModsManager;

@SuppressWarnings("unused")
public final class CharacterCreate extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(CharacterCreate.class);
	
	private String name;
	private byte sex, hairStyle, hairColor, face;
	private int race, classId, intelligence, str, con, men, dex, wit;
	
	@Override
	protected void readImpl()
	{
		name = readS();
		race = readD();
		sex = (byte) readD();
		classId = readD();
		intelligence = readD(); // keyword int can't be used
		str = readD();
		con = readD();
		men = readD();
		dex = readD();
		wit = readD();
		hairStyle = (byte) readD();
		hairColor = (byte) readD();
		face = (byte) readD();
	}
	
	@Override
	protected void runImpl()
	{
		
		if (name.length() < 3 || name.length() > 16 || !Util.isAlphaNumeric(name) || !isValidName(name))
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("DEBUG " + getType() + ": charname: " + name + " is invalid. creation failed.");
			}
			
			sendPacket(new CharCreateFail(CharCreateFail.REASON_16_ENG_CHARS));
			return;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.debug("DEBUG " + getType() + ": charname: " + name + " classId: " + classId);
		}
		
		L2PcInstance newChar = null;
		L2PcTemplate template = null;
		
		// Since checks for duplicate names are done using SQL, lock must be held until data is written to DB as well.
		synchronized (CharNameTable.getInstance())
		{
			if (CharNameTable.getInstance().accountCharNumber(getClient().getAccountName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT && Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0)
			{
				if (Config.DEBUG)
				{
					LOGGER.debug("DEBUG " + getType() + ": Max number of characters reached. Creation failed.");
				}
				
				sendPacket(new CharCreateFail(CharCreateFail.REASON_TOO_MANY_CHARACTERS));
				return;
			}
			else if (CharNameTable.getInstance().doesCharNameExist(name))
			{
				if (Config.DEBUG)
				{
					LOGGER.debug("DEBUG " + getType() + ": charname: " + name + " already exists. creation failed.");
				}
				
				sendPacket(new CharCreateFail(CharCreateFail.REASON_NAME_ALREADY_EXISTS));
				return;
			}
			else if (CharNameTable.getInstance().ipCharNumber(getClient().getConnection().getInetAddress().getHostName()) >= Config.MAX_CHARACTERS_NUMBER_PER_IP && Config.MAX_CHARACTERS_NUMBER_PER_IP != 0)
			{
				if (Config.DEBUG)
				{
					LOGGER.debug("DEBUG " + getType() + ": Max number of characters reached for IP. Creation failed.");
				}
				
				sendPacket(new CharCreateFail(CharCreateFail.REASON_TOO_MANY_CHARACTERS));
				return;
			}
			
			template = CharTemplateTable.getInstance().getTemplate(classId);
			
			if (Config.DEBUG)
			{
				LOGGER.debug("DEBUG " + getType() + ": charname: " + name + " classId: " + classId + " template: " + template);
			}
			
			if (template == null || template.classBaseLevel > 1)
			{
				sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
				return;
			}
			
			final int objectId = IdFactory.getInstance().getNextId();
			newChar = L2PcInstance.create(objectId, template, getClient().getAccountName(), name, hairStyle, hairColor, face, sex != 0);
			
			newChar.setCurrentHp(newChar.getMaxHp());// L2Off like
			// newChar.setCurrentCp(template.baseCpMax);
			newChar.setCurrentCp(0); // L2Off like
			newChar.setCurrentMp(newChar.getMaxMp());// L2Off like
			// newChar.setMaxLoad(template.baseLoad);
			
			// send acknowledgement
			sendPacket(new CharCreateOk()); // Success
			initNewChar(getClient(), newChar);
		}
	}
	
	private boolean isValidName(final String text)
	{
		boolean result = true;
		final String test = text;
		Pattern pattern;
		
		try
		{
			pattern = Pattern.compile(Config.CNAME_TEMPLATE);
		}
		catch (final PatternSyntaxException e) // case of illegal pattern
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("ERROR " + getType() + ": Character name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		
		final Matcher regexp = pattern.matcher(test);
		if (!regexp.matches())
		{
			result = false;
		}
		
		return result;
	}
	
	private void initNewChar(final L2GameClient client, final L2PcInstance newChar)
	{
		if (Config.DEBUG)
		{
			LOGGER.debug("DEBUG " + getType() + ": Character init start");
		}
		
		L2World.getInstance().storeObject(newChar);
		final L2PcTemplate template = newChar.getTemplate();
		
		// Starting Items
		if (Config.STARTING_ADENA > 0)
		{
			newChar.addAdena("Init", Config.STARTING_ADENA, null, false);
		}
		
		if (Config.STARTING_AA > 0)
		{
			newChar.addAncientAdena("Init", Config.STARTING_AA, null, false);
		}
		
		if (Config.CUSTOM_STARTER_ITEMS_ENABLED)
		{
			if (newChar.isMageClass())
			{
				for (final int[] reward : Config.STARTING_CUSTOM_ITEMS_M)
				{
					if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
					{
						newChar.getInventory().addItem("Starter Items Mage", reward[0], reward[1], newChar, null);
					}
					else
					{
						for (int i = 0; i < reward[1]; ++i)
						{
							newChar.getInventory().addItem("Starter Items Mage", reward[0], 1, newChar, null);
						}
					}
				}
			}
			else
			{
				for (final int[] reward : Config.STARTING_CUSTOM_ITEMS_F)
				{
					if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
					{
						newChar.getInventory().addItem("Starter Items Fighter", reward[0], reward[1], newChar, null);
					}
					else
					{
						for (int i = 0; i < reward[1]; ++i)
						{
							newChar.getInventory().addItem("Starter Items Fighter", reward[0], 1, newChar, null);
						}
					}
				}
			}
		}
		
		if (Config.SPAWN_CHAR)
		{
			newChar.setXYZInvisible(Config.SPAWN_X, Config.SPAWN_Y, Config.SPAWN_Z);
		}
		else
		{
			newChar.setXYZInvisible(template.spawnX, template.spawnY, template.spawnZ);
		}
		
		if (Config.ALLOW_CREATE_LVL)
		{
			newChar.getStat().addExp(ExperienceData.getInstance().getExpForLevel(Config.CHAR_CREATE_LVL));
		}
		
		if (Config.CHAR_TITLE)
		{
			newChar.setTitle(Config.ADD_CHAR_TITLE);
		}
		else
		{
			newChar.setTitle("");
		}
		
		if (Config.PVP_PK_TITLE)
		{
			newChar.setTitle(Config.PVP_TITLE_PREFIX + "0" + Config.PK_TITLE_PREFIX + "0 ");
		}
		
		// Shortcuts
		newChar.registerShortCut(new L2ShortCut(0, 0, 3, 2, -1, 1)); // Attack
		newChar.registerShortCut(new L2ShortCut(3, 0, 3, 5, -1, 1)); // Take
		newChar.registerShortCut(new L2ShortCut(10, 0, 3, 0, -1, 1)); // Sit
		
		final L2Item[] items = template.getItems();
		
		for (final L2Item item2 : items)
		{
			final L2ItemInstance item = newChar.getInventory().addItem("Init", item2.getItemId(), 1, newChar, null);
			
			if (item.getItemId() == 5588)
			{
				newChar.registerShortCut(new L2ShortCut(11, 0, 1, item.getObjectId(), -1, 1)); // Tutorial Book shortcut
			}
			
			if (item.isEquipable())
			{
				if (newChar.getActiveWeaponItem() == null || !(item.getItem().getType2() != L2Item.TYPE2_WEAPON))
				{
					newChar.getInventory().equipItemAndRecord(item);
				}
			}
		}
		
		final L2SkillLearn[] startSkills = SkillTreeTable.getInstance().getAvailableSkills(newChar, newChar.getClassId());
		
		for (final L2SkillLearn startSkill : startSkills)
		{
			newChar.addSkill(SkillTable.getInstance().getInfo(startSkill.getId(), startSkill.getLevel()), true);
			
			if (startSkill.getId() == 1001 || startSkill.getId() == 1177)
			{
				newChar.registerShortCut(new L2ShortCut(1, 0, 2, startSkill.getId(), 1, 1));
			}
			
			if (startSkill.getId() == 1216)
			{
				newChar.registerShortCut(new L2ShortCut(10, 0, 2, startSkill.getId(), 1, 1));
			}
			
			if (Config.DEBUG)
			{
				LOGGER.debug("DEBUG " + getType() + ": Adding starter skill:" + startSkill.getId() + " / " + startSkill.getLevel());
			}
		}
		
		EngineModsManager.onCreateCharacter(newChar);
		
		startTutorialQuest(newChar);
		newChar.store();
		newChar.deleteMe(); // Release the world of this character and it's inventory
		
		// Before the char selection, check shutdown status
		if (GameServer.getSelectorThread().isShutdown())
		{
			client.closeNow();
			return;
		}
		
		// Send char list
		final CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1);
		client.getConnection().sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
		
		if (Config.DEBUG)
		{
			LOGGER.debug("DEBUG " + getType() + ": Character init end");
		}
	}
	
	public void startTutorialQuest(final L2PcInstance player)
	{
		final QuestState qs = player.getQuestState("255_Tutorial");
		Quest q = null;
		
		if (qs == null && !Config.ALT_DEV_NO_QUESTS)
		{
			q = QuestManager.getInstance().getQuest("255_Tutorial");
		}
		
		if (q != null)
		{
			q.newQuestState(player);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 0B CharacterCreate";
	}
}