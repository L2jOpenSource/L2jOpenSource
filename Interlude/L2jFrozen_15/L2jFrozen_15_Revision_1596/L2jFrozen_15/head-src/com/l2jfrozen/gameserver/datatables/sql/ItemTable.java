package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.Item;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance.ItemLocation;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfrozen.gameserver.skills.SkillsEngine;
import com.l2jfrozen.gameserver.templates.L2Armor;
import com.l2jfrozen.gameserver.templates.L2ArmorType;
import com.l2jfrozen.gameserver.templates.L2EtcItem;
import com.l2jfrozen.gameserver.templates.L2EtcItemType;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.templates.L2WeaponType;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * @version $Revision: 1.9.2.6.2.9 $ $Date: 2005/04/02 15:57:34 $
 */
public class ItemTable
{
	private final static Logger LOGGER = Logger.getLogger(ItemTable.class);
	private final static java.util.logging.Logger logItems = java.util.logging.Logger.getLogger("item");
	
	private static final Map<String, Integer> crystalTypes = new HashMap<>();
	private static final Map<String, L2WeaponType> weaponTypes = new HashMap<>();
	private static final Map<String, L2ArmorType> armorTypes = new HashMap<>();
	private static final Map<String, Integer> slots = new HashMap<>();
	
	private L2Item[] allTemplates;
	
	private final boolean initialized = true;
	
	static
	{
		crystalTypes.put("s", L2Item.CRYSTAL_S);
		crystalTypes.put("a", L2Item.CRYSTAL_A);
		crystalTypes.put("b", L2Item.CRYSTAL_B);
		crystalTypes.put("c", L2Item.CRYSTAL_C);
		crystalTypes.put("d", L2Item.CRYSTAL_D);
		crystalTypes.put("none", L2Item.CRYSTAL_NONE);
		
		weaponTypes.put("blunt", L2WeaponType.BLUNT);
		weaponTypes.put("bow", L2WeaponType.BOW);
		weaponTypes.put("dagger", L2WeaponType.DAGGER);
		weaponTypes.put("dual", L2WeaponType.DUAL);
		weaponTypes.put("dualfist", L2WeaponType.DUALFIST);
		weaponTypes.put("etc", L2WeaponType.ETC);
		weaponTypes.put("fist", L2WeaponType.FIST);
		weaponTypes.put("none", L2WeaponType.NONE); // these are shields !
		weaponTypes.put("pole", L2WeaponType.POLE);
		weaponTypes.put("sword", L2WeaponType.SWORD);
		weaponTypes.put("bigsword", L2WeaponType.BIGSWORD); // Two-Handed Swords
		weaponTypes.put("pet", L2WeaponType.PET); // Pet Weapon
		weaponTypes.put("rod", L2WeaponType.ROD); // Fishing Rods
		weaponTypes.put("bigblunt", L2WeaponType.BIGBLUNT); // Two handed blunt
		armorTypes.put("none", L2ArmorType.NONE);
		armorTypes.put("light", L2ArmorType.LIGHT);
		armorTypes.put("heavy", L2ArmorType.HEAVY);
		armorTypes.put("magic", L2ArmorType.MAGIC);
		armorTypes.put("pet", L2ArmorType.PET);
		
		slots.put("chest", L2Item.SLOT_CHEST);
		slots.put("fullarmor", L2Item.SLOT_FULL_ARMOR);
		slots.put("head", L2Item.SLOT_HEAD);
		slots.put("hair", L2Item.SLOT_HAIR);
		slots.put("face", L2Item.SLOT_FACE);
		slots.put("dhair", L2Item.SLOT_DHAIR);
		slots.put("underwear", L2Item.SLOT_UNDERWEAR);
		slots.put("back", L2Item.SLOT_BACK);
		slots.put("neck", L2Item.SLOT_NECK);
		slots.put("legs", L2Item.SLOT_LEGS);
		slots.put("feet", L2Item.SLOT_FEET);
		slots.put("gloves", L2Item.SLOT_GLOVES);
		slots.put("chest,legs", L2Item.SLOT_CHEST | L2Item.SLOT_LEGS);
		slots.put("rhand", L2Item.SLOT_R_HAND);
		slots.put("lhand", L2Item.SLOT_L_HAND);
		slots.put("lrhand", L2Item.SLOT_LR_HAND);
		slots.put("rear,lear", L2Item.SLOT_R_EAR | L2Item.SLOT_L_EAR);
		slots.put("rfinger,lfinger", L2Item.SLOT_R_FINGER | L2Item.SLOT_L_FINGER);
		slots.put("none", L2Item.SLOT_NONE);
		slots.put("wolf", L2Item.SLOT_WOLF); // for wolf
		slots.put("hatchling", L2Item.SLOT_HATCHLING); // for hatchling
		slots.put("strider", L2Item.SLOT_STRIDER); // for strider
		slots.put("babypet", L2Item.SLOT_BABYPET); // for babypet
	}
	
	private static ItemTable instance;
	
	/** Table of SQL request in order to obtain items from tables [etcitem], [armor], [weapon] */
	private static final String[] SQL_ITEM_SELECTS =
	{
		"SELECT item_id, name, crystallizable, item_type, weight, consume_type, crystal_type, duration, price, crystal_count, sellable, dropable, destroyable, tradeable FROM etcitem",
		
		"SELECT item_id, name, bodypart, crystallizable, armor_type, weight, crystal_type, avoid_modify, duration, p_def, m_def, mp_bonus, price, crystal_count, sellable, dropable, destroyable, tradeable, item_skill_id, item_skill_lvl FROM armor",
		
		"SELECT item_id, name, bodypart, crystallizable, weight, soulshots, spiritshots, crystal_type, p_dam, rnd_dam, weaponType, critical, hit_modify, avoid_modify, shield_def, shield_def_rate, atk_speed, mp_consume, m_dam, duration, price, crystal_count, sellable, dropable, destroyable, tradeable, item_skill_id, item_skill_lvl,enchant4_skill_id,enchant4_skill_lvl, onCast_skill_id, onCast_skill_lvl, onCast_skill_chance, onCrit_skill_id, onCrit_skill_lvl, onCrit_skill_chance FROM weapon"
	};
	
	private static final String[] SQL_CUSTOM_ITEM_SELECTS =
	{
		"SELECT item_id, name, crystallizable, item_type, weight, consume_type, crystal_type, duration, price, crystal_count, sellable, dropable, destroyable, tradeable FROM custom_etcitem",
		
		"SELECT item_id, name, bodypart, crystallizable, armor_type, weight, crystal_type, avoid_modify, duration, p_def, m_def, mp_bonus, price, crystal_count, sellable, dropable, destroyable, tradeable, item_skill_id, item_skill_lvl FROM custom_armor",
		
		"SELECT item_id, name, bodypart, crystallizable, weight, soulshots, spiritshots, crystal_type, p_dam, rnd_dam, weaponType, critical, hit_modify, avoid_modify, shield_def, shield_def_rate, atk_speed, mp_consume, m_dam, duration, price, crystal_count, sellable, dropable, destroyable, tradeable, item_skill_id, item_skill_lvl,enchant4_skill_id,enchant4_skill_lvl, onCast_skill_id, onCast_skill_lvl, onCast_skill_chance, onCrit_skill_id, onCrit_skill_lvl, onCrit_skill_chance FROM custom_weapon"
	};
	
	private static final String DELETE_PET_BY_ITEM_OBJ_ID = "DELETE FROM pets WHERE item_obj_id=?";
	
	/** List of etcItem */
	private static final Map<Integer, Item> itemData = new HashMap<>();
	/** List of weapons */
	private static final Map<Integer, Item> weaponData = new HashMap<>();
	/** List of armor */
	private static final Map<Integer, Item> armorData = new HashMap<>();
	
	/**
	 * Returns instance of ItemTable
	 * @return ItemTable
	 */
	public static ItemTable getInstance()
	{
		if (instance == null)
		{
			instance = new ItemTable();
		}
		return instance;
	}
	
	/**
	 * Returns a new object Item
	 * @return
	 */
	public Item newItem()
	{
		return new Item();
	}
	
	public ItemTable()
	{
		Map<Integer, L2EtcItem> etcItems = new HashMap<>();
		Map<Integer, L2Armor> armors = new HashMap<>();
		Map<Integer, L2Weapon> weapons = new HashMap<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			for (String selectQuery : SQL_ITEM_SELECTS)
			{
				try (PreparedStatement statement = con.prepareStatement(selectQuery);
					ResultSet rset = statement.executeQuery())
				{
					// Add item in correct HashMap
					while (rset.next())
					{
						if (selectQuery.endsWith("etcitem"))
						{
							Item newItem = readItem(rset);
							itemData.put(newItem.id, newItem);
						}
						else if (selectQuery.endsWith("armor"))
						{
							Item newItem = readArmor(rset);
							armorData.put(newItem.id, newItem);
						}
						else if (selectQuery.endsWith("weapon"))
						{
							Item newItem = readWeapon(rset);
							weaponData.put(newItem.id, newItem);
						}
					}
				}
				catch (Exception e)
				{
					LOGGER.error("ItemTable.CustomTables : Something happened while etcitem, armor and weapon table loading data. ", e);
				}
			}
			
			if (Config.CUSTOM_ITEM_TABLES)
			{
				for (String selectQuery : SQL_CUSTOM_ITEM_SELECTS)
				{
					try (PreparedStatement statement = con.prepareStatement(selectQuery);
						ResultSet rset = statement.executeQuery())
					{
						// Add item in correct HashMap
						while (rset.next())
						{
							if (selectQuery.endsWith("etcitem"))
							{
								Item newItem = readItem(rset);
								
								if (itemData.containsKey(newItem.id))
								{
									LOGGER.warn("ItemTable.custom_etcitem: Item with ID: " + newItem.id + " already exist in etcitem table. ");
								}
								
								itemData.put(newItem.id, newItem);
							}
							else if (selectQuery.endsWith("armor"))
							{
								Item newItem = readArmor(rset);
								
								if (armorData.containsKey(newItem.id))
								{
									LOGGER.warn("ItemTable.custom_armor: Armor with ID: " + newItem.id + " already exist in armor table.");
								}
								
								armorData.put(newItem.id, newItem);
							}
							else if (selectQuery.endsWith("weapon"))
							{
								Item newItem = readWeapon(rset);
								
								if (weaponData.containsKey(newItem.id))
								{
									LOGGER.warn("ItemTable.custom_weapon: Weapon with ID: " + newItem.id + " already exist in weapon table.");
								}
								
								weaponData.put(newItem.id, newItem);
							}
						}
					}
					catch (Exception e)
					{
						LOGGER.error("ItemTable.CustomTables : Something happened while custom_etcitem, custom_armor and custom_weapon table loading data. ", e);
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("ItemTable.ItemTable : Can not connect to database. ", e);
		}
		
		for (L2Armor armor : SkillsEngine.getInstance().loadArmors(armorData))
		{
			armors.put(armor.getItemId(), armor);
		}
		
		LOGGER.info("ItemTable: Loaded " + armors.size() + " Armors.");
		
		for (L2EtcItem item : SkillsEngine.getInstance().loadItems(itemData))
		{
			etcItems.put(item.getItemId(), item);
		}
		
		LOGGER.info("ItemTable: Loaded " + etcItems.size() + " Items.");
		
		for (L2Weapon weapon : SkillsEngine.getInstance().loadWeapons(weaponData))
		{
			weapons.put(weapon.getItemId(), weapon);
		}
		
		LOGGER.info("ItemTable: Loaded " + weapons.size() + " Weapons.");
		
		buildFastLookupTable(armors, weapons, etcItems);
	}
	
	/**
	 * Returns object Item from the record of the database
	 * @param  rset         : ResultSet designating a record of the [weapon] table of database
	 * @return              Item : object created from the database record
	 * @throws SQLException
	 */
	private Item readWeapon(final ResultSet rset) throws SQLException
	{
		final Item item = new Item();
		item.set = new StatsSet();
		item.type = weaponTypes.get(rset.getString("weaponType"));
		item.id = rset.getInt("item_id");
		item.name = rset.getString("name");
		
		item.set.set("item_id", item.id);
		item.set.set("name", item.name);
		
		// lets see if this is a shield
		if (item.type == L2WeaponType.NONE)
		{
			item.set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);
			item.set.set("type2", L2Item.TYPE2_SHIELD_ARMOR);
		}
		else
		{
			item.set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
			item.set.set("type2", L2Item.TYPE2_WEAPON);
		}
		
		item.set.set("bodypart", slots.get(rset.getString("bodypart")));
		item.set.set("crystal_type", crystalTypes.get(rset.getString("crystal_type")));
		item.set.set("crystallizable", Boolean.valueOf(rset.getString("crystallizable")).booleanValue());
		item.set.set("weight", rset.getInt("weight"));
		item.set.set("soulshots", rset.getInt("soulshots"));
		item.set.set("spiritshots", rset.getInt("spiritshots"));
		item.set.set("p_dam", rset.getInt("p_dam"));
		item.set.set("rnd_dam", rset.getInt("rnd_dam"));
		item.set.set("critical", rset.getInt("critical"));
		item.set.set("hit_modify", rset.getDouble("hit_modify"));
		item.set.set("avoid_modify", rset.getInt("avoid_modify"));
		item.set.set("shield_def", rset.getInt("shield_def"));
		item.set.set("shield_def_rate", rset.getInt("shield_def_rate"));
		item.set.set("atk_speed", rset.getInt("atk_speed"));
		item.set.set("mp_consume", rset.getInt("mp_consume"));
		item.set.set("m_dam", rset.getInt("m_dam"));
		item.set.set("duration", rset.getInt("duration"));
		item.set.set("price", rset.getInt("price"));
		item.set.set("crystal_count", rset.getInt("crystal_count"));
		item.set.set("sellable", Boolean.valueOf(rset.getString("sellable")));
		item.set.set("dropable", Boolean.valueOf(rset.getString("dropable")));
		item.set.set("destroyable", Boolean.valueOf(rset.getString("destroyable")));
		item.set.set("tradeable", Boolean.valueOf(rset.getString("tradeable")));
		
		item.set.set("item_skill_id", rset.getInt("item_skill_id"));
		item.set.set("item_skill_lvl", rset.getInt("item_skill_lvl"));
		
		item.set.set("enchant4_skill_id", rset.getInt("enchant4_skill_id"));
		item.set.set("enchant4_skill_lvl", rset.getInt("enchant4_skill_lvl"));
		
		item.set.set("onCast_skill_id", rset.getInt("onCast_skill_id"));
		item.set.set("onCast_skill_lvl", rset.getInt("onCast_skill_lvl"));
		item.set.set("onCast_skill_chance", rset.getInt("onCast_skill_chance"));
		
		item.set.set("onCrit_skill_id", rset.getInt("onCrit_skill_id"));
		item.set.set("onCrit_skill_lvl", rset.getInt("onCrit_skill_lvl"));
		item.set.set("onCrit_skill_chance", rset.getInt("onCrit_skill_chance"));
		
		if (item.type == L2WeaponType.PET)
		{
			item.set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
			
			if (item.set.getInteger("bodypart") == L2Item.SLOT_WOLF)
			{
				item.set.set("type2", L2Item.TYPE2_PET_WOLF);
			}
			else if (item.set.getInteger("bodypart") == L2Item.SLOT_HATCHLING)
			{
				item.set.set("type2", L2Item.TYPE2_PET_HATCHLING);
			}
			else if (item.set.getInteger("bodypart") == L2Item.SLOT_BABYPET)
			{
				item.set.set("type2", L2Item.TYPE2_PET_BABY);
			}
			else
			{
				item.set.set("type2", L2Item.TYPE2_PET_STRIDER);
			}
			
			item.set.set("bodypart", L2Item.SLOT_R_HAND);
		}
		
		return item;
	}
	
	/**
	 * Returns object Item from the record of the database
	 * @param  rset         : ResultSet designating a record of the [armor] table of database
	 * @return              Item : object created from the database record
	 * @throws SQLException
	 */
	private Item readArmor(final ResultSet rset) throws SQLException
	{
		final Item item = new Item();
		item.set = new StatsSet();
		item.type = armorTypes.get(rset.getString("armor_type"));
		item.id = rset.getInt("item_id");
		item.name = rset.getString("name");
		
		item.set.set("item_id", item.id);
		item.set.set("name", item.name);
		final int bodypart = slots.get(rset.getString("bodypart"));
		item.set.set("bodypart", bodypart);
		item.set.set("crystallizable", Boolean.valueOf(rset.getString("crystallizable")));
		item.set.set("crystal_count", rset.getInt("crystal_count"));
		item.set.set("sellable", Boolean.valueOf(rset.getString("sellable")));
		item.set.set("dropable", Boolean.valueOf(rset.getString("dropable")));
		item.set.set("destroyable", Boolean.valueOf(rset.getString("destroyable")));
		item.set.set("tradeable", Boolean.valueOf(rset.getString("tradeable")));
		item.set.set("item_skill_id", rset.getInt("item_skill_id"));
		item.set.set("item_skill_lvl", rset.getInt("item_skill_lvl"));
		
		if (bodypart == L2Item.SLOT_NECK || bodypart == L2Item.SLOT_HAIR || bodypart == L2Item.SLOT_FACE || bodypart == L2Item.SLOT_DHAIR || (bodypart & L2Item.SLOT_L_EAR) != 0 || (bodypart & L2Item.SLOT_L_FINGER) != 0)
		{
			item.set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
			item.set.set("type2", L2Item.TYPE2_ACCESSORY);
		}
		else
		{
			item.set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);
			item.set.set("type2", L2Item.TYPE2_SHIELD_ARMOR);
		}
		
		item.set.set("weight", rset.getInt("weight"));
		item.set.set("crystal_type", crystalTypes.get(rset.getString("crystal_type")));
		item.set.set("avoid_modify", rset.getInt("avoid_modify"));
		item.set.set("duration", rset.getInt("duration"));
		item.set.set("p_def", rset.getInt("p_def"));
		item.set.set("m_def", rset.getInt("m_def"));
		item.set.set("mp_bonus", rset.getInt("mp_bonus"));
		item.set.set("price", rset.getInt("price"));
		
		if (item.type == L2ArmorType.PET)
		{
			item.set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);
			
			if (item.set.getInteger("bodypart") == L2Item.SLOT_WOLF)
			{
				item.set.set("type2", L2Item.TYPE2_PET_WOLF);
			}
			else if (item.set.getInteger("bodypart") == L2Item.SLOT_HATCHLING)
			{
				item.set.set("type2", L2Item.TYPE2_PET_HATCHLING);
			}
			else if (item.set.getInteger("bodypart") == L2Item.SLOT_BABYPET)
			{
				item.set.set("type2", L2Item.TYPE2_PET_BABY);
			}
			else
			{
				item.set.set("type2", L2Item.TYPE2_PET_STRIDER);
			}
			
			item.set.set("bodypart", L2Item.SLOT_CHEST);
		}
		
		return item;
	}
	
	/**
	 * Returns object Item from the record of the database
	 * @param  rset         : ResultSet designating a record of the [etcitem] table of database
	 * @return              Item : object created from the database record
	 * @throws SQLException
	 */
	private Item readItem(final ResultSet rset) throws SQLException
	{
		final Item item = new Item();
		item.set = new StatsSet();
		item.id = rset.getInt("item_id");
		
		item.set.set("item_id", item.id);
		item.set.set("crystallizable", Boolean.valueOf(rset.getString("crystallizable")));
		item.set.set("type1", L2Item.TYPE1_ITEM_QUESTITEM_ADENA);
		item.set.set("type2", L2Item.TYPE2_OTHER);
		item.set.set("bodypart", 0);
		item.set.set("crystal_count", rset.getInt("crystal_count"));
		item.set.set("sellable", Boolean.valueOf(rset.getString("sellable")));
		item.set.set("dropable", Boolean.valueOf(rset.getString("dropable")));
		item.set.set("destroyable", Boolean.valueOf(rset.getString("destroyable")));
		item.set.set("tradeable", Boolean.valueOf(rset.getString("tradeable")));
		String itemType = rset.getString("item_type");
		
		switch (itemType)
		{
			case "none":
				item.type = L2EtcItemType.OTHER; // only for default
				
				break;
			case "castle_guard":
				item.type = L2EtcItemType.SCROLL; // dummy
				
				break;
			case "pet_collar":
				item.type = L2EtcItemType.PET_COLLAR;
				break;
			case "potion":
				item.type = L2EtcItemType.POTION;
				break;
			case "recipe":
				item.type = L2EtcItemType.RECEIPE;
				break;
			case "scroll":
				item.type = L2EtcItemType.SCROLL;
				break;
			case "seed":
				item.type = L2EtcItemType.SEED;
				break;
			case "shot":
				item.type = L2EtcItemType.SHOT;
				break;
			case "spellbook":
				item.type = L2EtcItemType.SPELLBOOK; // Spellbook, Amulet, Blueprint
				
				break;
			case "herb":
				item.type = L2EtcItemType.HERB;
				break;
			case "arrow":
				item.type = L2EtcItemType.ARROW;
				item.set.set("bodypart", L2Item.SLOT_L_HAND);
				break;
			case "quest":
				item.type = L2EtcItemType.QUEST;
				item.set.set("type2", L2Item.TYPE2_QUEST);
				break;
			case "lure":
				item.type = L2EtcItemType.OTHER;
				item.set.set("bodypart", L2Item.SLOT_L_HAND);
				break;
			default:
				if (Config.DEBUG)
				{
					LOGGER.info("Unknown etcitem type:" + itemType);
				}
				item.type = L2EtcItemType.OTHER;
				break;
		}
		itemType = null;
		
		final String consume = rset.getString("consume_type");
		switch (consume)
		{
			case "asset":
				item.type = L2EtcItemType.MONEY;
				item.set.set("stackable", true);
				item.set.set("type2", L2Item.TYPE2_MONEY);
				break;
			case "stackable":
				item.set.set("stackable", true);
				break;
			default:
				item.set.set("stackable", false);
				break;
		}
		
		final int crystal = crystalTypes.get(rset.getString("crystal_type"));
		item.set.set("crystal_type", crystal);
		
		final int weight = rset.getInt("weight");
		item.set.set("weight", weight);
		item.name = rset.getString("name");
		item.set.set("name", item.name);
		
		item.set.set("duration", rset.getInt("duration"));
		item.set.set("price", rset.getInt("price"));
		
		return item;
	}
	
	/**
	 * Returns if ItemTable initialized
	 * @return boolean
	 */
	public boolean isInitialized()
	{
		return initialized;
	}
	
	/**
	 * Builds a variable in which all items are putting in in function of their ID.
	 * @param armors
	 * @param weapons
	 * @param etcItems
	 */
	private void buildFastLookupTable(final Map<Integer, L2Armor> armors, final Map<Integer, L2Weapon> weapons, final Map<Integer, L2EtcItem> etcItems)
	{
		int highestId = 0;
		
		// Get highest ID of item in armor HashMap, then in weapon HashMap, and finally in etcitem HashMap
		for (final L2Armor item : armors.values())
		{
			if (item.getItemId() > highestId)
			{
				highestId = item.getItemId();
			}
		}
		
		for (final L2Weapon item : weapons.values())
		{
			if (item.getItemId() > highestId)
			{
				highestId = item.getItemId();
			}
		}
		
		for (final L2EtcItem item : etcItems.values())
		{
			if (item.getItemId() > highestId)
			{
				highestId = item.getItemId();
			}
		}
		
		LOGGER.info("Highest item id used: " + highestId);
		
		allTemplates = new L2Item[highestId + 1];
		
		// Insert armor item in Fast Look Up Table
		for (final int id : armors.keySet())
		{
			allTemplates[id] = armors.get(id);
		}
		
		// Insert weapon item in Fast Look Up Table
		for (final int id : weapons.keySet())
		{
			allTemplates[id] = weapons.get(id);
		}
		
		// Insert etcItem item in Fast Look Up Table
		for (final int id : etcItems.keySet())
		{
			allTemplates[id] = etcItems.get(id);
		}
	}
	
	/**
	 * Returns the item corresponding to the item ID
	 * @param  id : int designating the item
	 * @return    L2Item
	 */
	public L2Item getTemplate(final int id)
	{
		if (id > allTemplates.length)
		{
			return null;
		}
		return allTemplates[id];
	}
	
	public List<L2Item> getAllTemplatesByText(String text)
	{
		List<L2Item> list = new ArrayList<>();
		
		if (text.equals("") || text.equals(" "))
		{
			return list;
		}
		
		for (L2Item item : allTemplates)
		{
			if (item == null)
			{
				continue;
			}
			
			if (item.getName().toLowerCase().contains(text.trim().toLowerCase()))
			{
				list.add(item);
			}
		}
		
		return list;
	}
	
	/**
	 * Create the L2ItemInstance corresponding to the Item Identifier and quantitiy add logs the activity.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Create and Init the L2ItemInstance corresponding to the Item Identifier and quantity</li>
	 * <li>Add the L2ItemInstance object to allObjects of L2world</li>
	 * <li>Logs Item creation according to LOGGER settings</li><BR>
	 * <BR>
	 * @param  process   : String Identifier of process triggering this action
	 * @param  itemId    : int Item Identifier of the item to be created
	 * @param  count     : int Quantity of items to be created for stackable items
	 * @param  actor     : L2PcInstance Player requesting the item creation
	 * @param  reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return           L2ItemInstance corresponding to the new item
	 */
	public L2ItemInstance createItem(final String process, final int itemId, final int count, final L2PcInstance actor, final L2Object reference)
	{
		// Create and Init the L2ItemInstance corresponding to the Item Identifier
		final L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		
		// create loot schedule also if autoloot is enabled
		if (process.equalsIgnoreCase("loot")/* && !Config.AUTO_LOOT */)
		{
			ScheduledFuture<?> itemLootShedule;
			long delay = 0;
			// if in CommandChannel and was killing a World/RaidBoss
			if (reference instanceof L2GrandBossInstance || reference instanceof L2RaidBossInstance)
			{
				if (((L2Attackable) reference).getFirstCommandChannelAttacked() != null && ((L2Attackable) reference).getFirstCommandChannelAttacked().meetRaidWarCondition(reference))
				{
					item.setOwnerId(((L2Attackable) reference).getFirstCommandChannelAttacked().getChannelLeader().getObjectId());
					delay = 300000;
				}
				else
				{
					delay = 15000;
					item.setOwnerId(actor.getObjectId());
				}
			}
			else
			{
				item.setOwnerId(actor.getObjectId());
				delay = 15000;
			}
			itemLootShedule = ThreadPoolManager.getInstance().scheduleGeneral(new resetOwner(item), delay);
			item.setItemLootShedule(itemLootShedule);
		}
		
		if (Config.DEBUG)
		{
			LOGGER.info("ItemTable: Item created  oid: {} itemid: {}" + " " + item.getObjectId() + " " + itemId);
		}
		
		// Add the L2ItemInstance object to allObjects of L2world
		L2World.getInstance().storeObject(item);
		
		// Set Item parameters
		if (item.isStackable() && count > 1)
		{
			item.setCount(count);
		}
		
		if (Config.LOG_ITEMS)
		{
			final LogRecord record = new LogRecord(Level.INFO, "CREATE:" + process);
			record.setLoggerName("item");
			record.setParameters(new Object[]
			{
				item,
				actor,
				reference
			});
			logItems.log(record);
		}
		return item;
	}
	
	public L2ItemInstance createItem(final String process, final int itemId, final int count, final L2PcInstance actor)
	{
		return createItem(process, itemId, count, actor, null);
	}
	
	/**
	 * Returns a dummy (fr = factice) item.<BR>
	 * <BR>
	 * <U><I>Concept :</I></U><BR>
	 * Dummy item is created by setting the ID of the object in the world at null value
	 * @param  itemId : int designating the item
	 * @return        L2ItemInstance designating the dummy item created
	 */
	public L2ItemInstance createDummyItem(final int itemId)
	{
		final L2Item item = getTemplate(itemId);
		
		if (item == null)
		{
			return null;
		}
		
		L2ItemInstance temp = new L2ItemInstance(0, item);
		
		try
		{
			temp = new L2ItemInstance(0, itemId);
		}
		catch (final ArrayIndexOutOfBoundsException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			// this can happen if the item templates were not initialized
		}
		
		if (temp.getItem() == null)
		{
			LOGGER.warn("ItemTable: Item Template missing for Id: {}" + " " + itemId);
		}
		
		return temp;
	}
	
	/**
	 * Destroys the L2ItemInstance.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Sets L2ItemInstance parameters to be unusable</li>
	 * <li>Removes the L2ItemInstance object to allObjects of L2world</li>
	 * <li>Logs Item delettion according to LOGGER settings</li><BR>
	 * <BR>
	 * @param process   : String Identifier of process triggering this action
	 * @param item
	 * @param actor     : L2PcInstance Player requesting the item destroy
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void destroyItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
	{
		synchronized (item)
		{
			item.setCount(0);
			item.setOwnerId(0);
			item.setLocation(ItemLocation.VOID);
			item.setLastChange(L2ItemInstance.REMOVED);
			
			L2World.getInstance().removeObject(item);
			IdFactory.getInstance().releaseId(item.getObjectId());
			/*
			 * if(Config.LOG_ITEMS) { LogRecord record = new LogRecord(Level.INFO, "DELETE:" + process); record.setLoggerName("item"); record.setParameters(new Object[] { item, actor, reference }); logItems.LOGGER(record); }
			 */
			// if it's a pet control item, delete the pet as well
			if (L2PetDataTable.isPetItem(item.getItemId()))
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement(DELETE_PET_BY_ITEM_OBJ_ID))
				{
					statement.setInt(1, item.getObjectId());
					statement.executeUpdate();
				}
				catch (Exception e)
				{
					LOGGER.error("ItemTable.destroyItem : Could not delete pet objectid", e);
				}
			}
		}
	}
	
	public void reload()
	{
		synchronized (instance)
		{
			instance = null;
			instance = new ItemTable();
		}
	}
	
	protected class resetOwner implements Runnable
	{
		L2ItemInstance item;
		
		public resetOwner(final L2ItemInstance item)
		{
			this.item = item;
		}
		
		@Override
		public void run()
		{
			item.setOwnerId(0);
			item.setItemLootShedule(null);
		}
	}
	
}
