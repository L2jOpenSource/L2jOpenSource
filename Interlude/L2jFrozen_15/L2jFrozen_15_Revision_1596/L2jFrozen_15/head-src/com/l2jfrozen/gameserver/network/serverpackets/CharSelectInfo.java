package com.l2jfrozen.gameserver.network.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.CharSelectInfoPackage;
import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * @version $Revision: 1.8.2.4.2.6 $ $Date: 2005/04/06 16:13:46 $
 */
public class CharSelectInfo extends L2GameServerPacket
{
	private static Logger LOGGER = Logger.getLogger(CharSelectInfo.class);
	private static final String SELECT_AUGMENTATION_ATTRIBUTE_BY_ITEM_OBJECT_ID = "SELECT attributes FROM augmentations WHERE item_object_id=?";
	private static final String SELECT_CHARACTER_BY_ACCOUNT_NAME = "SELECT account_name, obj_Id, char_name, level, maxHp, curHp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, runSpd, walkSpd, str, con, dex, _int, men, wit, face, hairStyle, hairColor, sex, heading, x, y, z, movement_multiplier, attack_speed_multiplier, colRad, colHeight, exp, sp, karma, pvpkills, pkkills, clanid, maxload, race, classid, deletetime, cancraft, title, rec_have, rec_left, accesslevel, online, char_slot, lastAccess, base_class FROM characters WHERE account_name=?";
	private static final String SELECT_CHARACTERS_SUBCLASSES_INFO = "SELECT exp, sp, level FROM character_subclasses WHERE char_obj_id=? AND class_id=? ORDER BY char_obj_id";
	
	private final String loginName;
	private final int sessionId;
	private int activeId;
	private final CharSelectInfoPackage[] characterPackages;
	
	/**
	 * @param loginName
	 * @param sessionId
	 */
	public CharSelectInfo(final String loginName, final int sessionId)
	{
		this.sessionId = sessionId;
		this.loginName = loginName;
		characterPackages = loadCharacterSelectInfo();
		activeId = -1;
	}
	
	public CharSelectInfo(final String loginName, final int sessionId, final int activeId)
	{
		this.sessionId = sessionId;
		this.loginName = loginName;
		characterPackages = loadCharacterSelectInfo();
		this.activeId = activeId;
	}
	
	public CharSelectInfoPackage[] getCharInfo()
	{
		return characterPackages;
	}
	
	@Override
	protected final void writeImpl()
	{
		final int size = characterPackages.length;
		
		writeC(0x13);
		writeD(size);
		
		long lastAccess = 0L;
		
		if (activeId == -1)
		{
			for (int i = 0; i < size; i++)
			{
				if (lastAccess < characterPackages[i].getLastAccess())
				{
					lastAccess = characterPackages[i].getLastAccess();
					activeId = i;
				}
			}
		}
		
		for (int i = 0; i < size; i++)
		{
			final CharSelectInfoPackage charInfoPackage = characterPackages[i];
			
			writeS(charInfoPackage.getName());
			writeD(charInfoPackage.getCharId());
			writeS(loginName);
			writeD(sessionId);
			writeD(charInfoPackage.getClanId());
			writeD(0x00); // ??
			
			writeD(charInfoPackage.getSex());
			writeD(charInfoPackage.getRace());
			
			if (charInfoPackage.getClassId() == charInfoPackage.getBaseClassId())
			{
				writeD(charInfoPackage.getClassId());
			}
			else
			{
				writeD(charInfoPackage.getBaseClassId());
			}
			
			writeD(0x01); // active ??
			
			writeD(0x00); // x
			writeD(0x00); // y
			writeD(0x00); // z
			
			writeF(charInfoPackage.getCurrentHp()); // hp cur
			writeF(charInfoPackage.getCurrentMp()); // mp cur
			
			writeD(charInfoPackage.getSp());
			writeQ(charInfoPackage.getExp());
			writeD(charInfoPackage.getLevel());
			
			writeD(charInfoPackage.getKarma()); // karma
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_DHAIR));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_FACE));
			
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_DHAIR));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_REAR));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_NECK));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_FACE));
			
			writeD(charInfoPackage.getHairStyle());
			writeD(charInfoPackage.getHairColor());
			writeD(charInfoPackage.getFace());
			
			writeF(charInfoPackage.getMaxHp()); // hp max
			writeF(charInfoPackage.getMaxMp()); // mp max
			
			final long deleteTime = charInfoPackage.getDeleteTimer();
			final int accesslevels = charInfoPackage.getAccessLevel();
			int deletedays = 0;
			
			if (deleteTime > 0)
			{
				deletedays = (int) ((deleteTime - System.currentTimeMillis()) / 1000);
			}
			else if (accesslevels < 0)
			{
				deletedays = -1; // like L2OFF player looks dead if he is banned.
			}
			
			writeD(deletedays); // days left before
			// delete .. if != 0
			// then char is inactive
			writeD(charInfoPackage.getClassId());
			
			if (i == activeId)
			{
				writeD(0x01);
			}
			else
			{
				writeD(0x00); // c3 auto-select char
			}
			
			writeC(charInfoPackage.getEnchantEffect() > 127 ? 127 : charInfoPackage.getEnchantEffect());
			
			writeD(charInfoPackage.getAugmentationId());
		}
	}
	
	private CharSelectInfoPackage[] loadCharacterSelectInfo()
	{
		CharSelectInfoPackage charInfopackage;
		final List<CharSelectInfoPackage> characterList = new ArrayList<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHARACTER_BY_ACCOUNT_NAME))
		{
			statement.setString(1, loginName);
			
			try (ResultSet charList = statement.executeQuery())
			{
				while (charList.next())// fills the package
				{
					charInfopackage = restoreChar(charList);
					if (charInfopackage != null)
					{
						characterList.add(charInfopackage);
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("CharacterSelecInfo.CharSelectInfoPackage : Could not select character by account name from characters table", e);
		}
		
		return characterList.toArray(new CharSelectInfoPackage[characterList.size()]);
	}
	
	private void loadCharacterSubclassInfo(final CharSelectInfoPackage charInfopackage, final int ObjectId, final int activeClassId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement(SELECT_CHARACTERS_SUBCLASSES_INFO))
		{
			statement.setInt(1, ObjectId);
			statement.setInt(2, activeClassId);
			ResultSet charList = statement.executeQuery();
			
			if (charList.next())
			{
				charInfopackage.setExp(charList.getLong("exp"));
				charInfopackage.setSp(charList.getInt("sp"));
				charInfopackage.setLevel(charList.getInt("level"));
			}
			
			charList.close();
		}
		catch (Exception e)
		{
			LOGGER.error("CharSelectInfo.loadCharacterSubclassInfo : Could not select character subclass info", e);
		}
	}
	
	private CharSelectInfoPackage restoreChar(final ResultSet chardata) throws Exception
	{
		final int objectId = chardata.getInt("obj_id");
		
		// See if the char must be deleted
		final long deletetime = chardata.getLong("deletetime");
		if (deletetime > 0)
		{
			if (System.currentTimeMillis() > deletetime)
			{
				final L2PcInstance cha = L2PcInstance.load(objectId);
				final L2Clan clan = cha.getClan();
				if (clan != null)
				{
					clan.removeClanMember(cha.getName(), 0);
				}
				
				L2GameClient.deleteCharByObjId(objectId);
				return null;
			}
		}
		
		final String name = chardata.getString("char_name");
		
		final CharSelectInfoPackage charInfopackage = new CharSelectInfoPackage(objectId, name);
		charInfopackage.setLevel(chardata.getInt("level"));
		charInfopackage.setMaxHp(chardata.getInt("maxhp"));
		charInfopackage.setCurrentHp(chardata.getDouble("curhp"));
		charInfopackage.setMaxMp(chardata.getInt("maxmp"));
		charInfopackage.setCurrentMp(chardata.getDouble("curmp"));
		charInfopackage.setKarma(chardata.getInt("karma"));
		
		charInfopackage.setFace(chardata.getInt("face"));
		charInfopackage.setHairStyle(chardata.getInt("hairstyle"));
		charInfopackage.setHairColor(chardata.getInt("haircolor"));
		charInfopackage.setSex(chardata.getInt("sex"));
		
		charInfopackage.setExp(chardata.getLong("exp"));
		charInfopackage.setSp(chardata.getInt("sp"));
		charInfopackage.setClanId(chardata.getInt("clanid"));
		
		charInfopackage.setRace(chardata.getInt("race"));
		
		charInfopackage.setAccessLevel(Config.GM_PLAYERS.getOrDefault(objectId, 0));
		
		final int baseClassId = chardata.getInt("base_class");
		final int activeClassId = chardata.getInt("classid");
		
		// if is in subclass, load subclass exp, sp, lvl info
		if (baseClassId != activeClassId)
		{
			loadCharacterSubclassInfo(charInfopackage, objectId, activeClassId);
		}
		
		charInfopackage.setClassId(activeClassId);
		
		// Get the augmentation id for equipped weapon
		int weaponObjId = charInfopackage.getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND);
		if (weaponObjId < 1)
		{
			weaponObjId = charInfopackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND);
		}
		
		if (weaponObjId > 0)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(SELECT_AUGMENTATION_ATTRIBUTE_BY_ITEM_OBJECT_ID);)
			{
				statement.setInt(1, weaponObjId);
				ResultSet result = statement.executeQuery();
				
				if (result.next())
				{
					charInfopackage.setAugmentationId(result.getInt("attributes"));
				}
				
				result.close();
			}
			catch (final Exception e)
			{
				LOGGER.error("CharacterSelectInfo : Could not select augmentation info", e);
			}
		}
		
		/*
		 * Check if the base class is set to zero and alse doesn't match with the current active class, otherwise send the base class ID. This prevents chars created before base class was introduced from being displayed incorrectly.
		 */
		if (baseClassId == 0 && activeClassId > 0)
		{
			charInfopackage.setBaseClassId(activeClassId);
		}
		else
		{
			charInfopackage.setBaseClassId(baseClassId);
		}
		
		charInfopackage.setDeleteTimer(deletetime);
		charInfopackage.setLastAccess(chardata.getLong("lastAccess"));
		
		return charInfopackage;
	}
	
	@Override
	public String getType()
	{
		return "[S] 1F CharSelectInfo";
	}
}