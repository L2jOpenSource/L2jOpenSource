/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.l2jserver.Config;
import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.cache.HtmCache;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class BuffBBSManager extends BaseBBSManager
{
	public static final Logger _log = Logger.getLogger(BuffBBSManager.class.getName());
	private static String _bbscommand = "bufferCB";
	
	// private static final boolean DEBUG = false;
	
	private static final boolean ENABLE_HEAL = true;
	private static boolean ENABLE_HEAL_IN_COMBAT = true;
	private static final boolean ENABLE_BUFF_REMOVE = true;
	private static final boolean BUFF_WITH_KARMA = true;
	private static final boolean FREE_BUFFS = false;
	private static final int BUFF_REMOVE_PRICE = 10000;
	private static final int HEAL_PRICE = 10000;
	private static final int BUFF_PRICE = 1000;
	private static final int BUFF_SET_PRICE = 15000;
	private static final int SCHEME_BUFF_PRICE = 20000;
	// private static final int SCHEMES_PER_PLAYER = 4;
	
	private static boolean init = true;
	
	private static Map<Integer, Map<String, ArrayList<Integer>>> _schemes;
	private static Map<String, ArrayList<Integer>> _eachScheme;
	private static Map<Integer, Boolean> _summonOption;
	
	// @formatter:off
	private static int[] _fighterBuffListPreset =
	{
		1036,2, // Magic Barrier
		1040,3, // Shield
		1045,6, // Bless The Body
		1048,6, // Bless The Soul
		1068,3, // Might
		1077,3, // Focus
		1086,2, // Haste
		1240,3, // Guidance
		1242,3, // Death Whisper
		1388,3, // Greater Might
		1062,2, // Berserker Spirit
		1363,1, // Chant of Victory
		1035,4, // Mental Shield
		1352,1, // Elemental Protection
		1353,1, // Divine Protection
		264,1, // Song of Earth
		267,1, // Song of Warding
		268,1, // Song of Wind
		269,1, // Song of Hunter
		304,1, // Song of Vitality
		271,1, // Dance of Warrior
		274,1, // Dance of Fire
		275,1 // Dance of Fury
	};
	
	private static int[] _mageBuffListPreset =
	{
		1036,2, // Magic Barrier
		1040,3, // Shield
		1045,6, // Bless The Body
		1048,6, // Bless The Soul
		1059,3, // Empower
		1085,3, // Acumen
		1204,2, // Wind Walk
		1303,2, // Wild Magic
		1389,3, // Greater Shield
		1062,2, // Berserker Spirit
		1413,1, // Magnus' Chant
		1035,4, // Mental Shield
		1352,1, // Elemental Protection
		1353,1, // Divine Protection
		264,1, // Song of Earth
		267,1, // Song of Warding
		268,1, // Song of Wind
		304,1, // Song of Vitality
		273,1, // Dance of the Mystic
		276,1 // Dance of Concentration
		
	};
	
	private static int[] _resistBuffListPreset =
	{
		1033,3, // Resist Poison
		1182,3, // Resist Aqua
		1189,3, // Resist Wind
		1191,3, // Resist Fire
		1259,4, // Resist Shock
		1392,3, // Resist Holy
		1393,3, // Resist Dark
		1548,3 // Resist Earth
	};
	
	private static int[] _improvedBuffListPreset =
	{
		1499,1, // Improved Combat
		1500,1, // Improved Magic
		1501,1, // Improved Condition
		1502,1, // Improved Critical Attack
		1503,1, // Improved Shield Defense
		1504,1, // Improved Movement
		1519,1 // Chant of Blood Awakening
	};
	
	
	private static int[] _songBuffList =
	{
		264,1, // Song of Earth
		265,1, // Song of Life
		266,1, // Song of Water
		267,1, // Song of Warding
		268,1, // Song of Wind
		269,1, // Song of Hunter
		270,1, // Song of Invocation
		304,1, // Song of Vitality
		305,1, // Song of Vengeance
		306,1, // Song of Flame Guard
		308,1, // Song of Storm Guard
		349,1, // Song of Renewal
		363,1, // Song of Meditation
		364,1, // Song of Champion
		529,1, // Song of Elemental
		914,1 // Song of Purification
	};
	
	private static int[] _danceBuffList =
	{
		271,1, // Dance of Warrior
		272,1, // Dance of Inspiration
		273,1, // Dance of the Mystic
		274,1, // Dance of Fire
		275,1, // Dance of Fury
		276,1, // Dance of Concentration
		277,1, // Dance of Light
		307,1, // Dance of Aqua Guard
		309,1, // Dance of Earth Guard
		310,1, // Dance of Vampire
		311,1, // Dance of Protection
		365,1, // Dance of Siren
		366,1, // Dance of Shadow
		530,1, // Dance of Aligment
		915,1 // Dance of Berserker
	};
	
	private static int[] _orcBuffList =
	{
		1003,3, // Pa'agrian Gift
		1004,3, // Wisdom of Pa'agrio
		1005,3, // Blessin. of Pa'agrio
		1008,3, // Glory of Pa'agrio
		1249,3, // Vision of Pa'agrio
		1250,3, // Protect. of Pa'agrio
		1260,2, // Tact of Pa'agrio
		1261,2, // Pa'agrio Rage
		1282,2, // Pa'agrian Haste
		1364,1, // Eye of Pa'agrio
		1365,1, // Soul of Pa'agrio
		1414,1, // Pa'agrio Victo
		1415,1, // Pa'agrio's Emblem
		1416,1, // Pa'agrio's Fist
		//1536,1, // Combat of Pa'agrio
		//1537,1, // Critical of Pa'agrio
		//1538,1, // Condition of Pa'agrio
		1007,3, // Chant of Battle
		1009,3, // Chant of Shielding
		1002,3, // Flame Chant
		1006,3, // Chant of Fire
		1251,2, // Chant of Fury
		1252,3, // Chant of Evasion
		1253,3, // Chant of Rage
		1284,3, // Chant of Revange
		1308,3, // Chant of Predator
		1309,3, // Chant of Eagle
		1310,4, // Chant of Vampire
		1362,1, // Chant of Spirit
		1363,1, // Chant of Victory
		1390,3, // War Chant
		1391,3, // Earth Chant
		1413,1, // Magnus' Chant
		1461,1, // Chant of Protection
		//1535,1, // Chant of Movement
		1562,2, // Chant of Berserker
		//1519,1, // Chant of Blood Awakening
		1549,1 // Chant of Elements
	};
	
	private static int[] _humanBuffList =
	{
		1040,3, // Shield
		1068,3, // Might
		1035,4, // Mental Shield
		1043,1, // Holy Weapon
		1044,3, // Regeneration
		1542,1, // Counter Critical
		1077,3, // Focus
		1078,6, // Concentration
		1085,3, // Acumen
		1204,2, // Wind Walk
		1032,3, // Invigor
		1036,2, // Magic Barrier
		1045,6, // Bless The Body
		1048,6, // Bless The Soul
		1086,2, // Haste
		1240,3, // Guidance
		1242,3, // Death Whisper
		1243,3, // Bless Shield
		1388,3, // Greater Might
		1389,3, // Greater Shield
		1356,1, // Prophecy of Fire
		1062,2, // Berserker Spirit
		1191,3, // Resist Fire
		1033,3, // Resist Poison
		1182,3, // Resist Aqua
		1189,3, // Resist Wind
		1548,3, // Earth Resistance
		1392,3, // Holy Resistance
		1393,3, // Unholy Resistance
		1352,1, // Elemental Protection
		1501,1, // Improved Condition
		1499,1 // Improve Combat		
	};
	
	private static int[] _elfBuffList =
	{
		1087,3, // Agility
		1397,3, // Clarity
		1035,4, // Mental Shield
		1304,3, // Advance Block
		1259,4, // Resist Shock
		1353,1, // Divine Protection
		1354,1, // Arcane Protection
		1355,1, // Prophecy of Water
		1460,1, // Mana Gain
		1503,1, // Improved Shield Defense
		1504,1 // Improved Movement
	};
	
	private static int[] _darkElfBuffList =
	{
		1500,1, // Improved Magic
		1502,1, // Improved Critical Attack
		1303,2, // Wild Magic
		1357,1, // Prophecy of Wind
		1268,4, // Vampiric Rage
		1035,4, // Mental Shield
		1059,3 // Empower	
	};
	
	private static int[] _dwarfBuffList =
	{
		825,1, // Sharp Edge
		826,1, // Spike
		827,1, // Restring
		828,1, // Case Harden
		829,1, // Hard Training
		830,1 // Embroider
	};
	
	private static int[] _kamaelBuffList =
	{
		834,1, // Blodd Pact
		1442,1, // Protection from Darkness
		1443,1, // Dark Weapon
		1444,1 // Pride of Kamael
	};
	
	private static int[] _othersBuffList =
	{
		4699,3, // Bleesing of Queen
		4700,3, // Gift of Queen
		4702,3, // Blessing of Seraphim
		4703,3, // Gift of Seraphim
		1232,3, // Blazing Skin
		1238,3, // Freezing Skin
		1323,1, // Noblesse Blessing
		1307,3, // Prayer
		1311,6, // Body of Avatar
		982,3 // Combat Aura
	};
	
	private static int[] _resistBuffList =
	{
		1033,3, // Resist Poison
		1182,3, // Resist Aqua
		1189,3, // Resist Wind
		1191,3, // Resist Fire
		1259,4, // Resist Shock
		1392,3, // Resist Holy
		1393,3, // Resist Dark
		1548,3 // Resist Earth		
	};
	
	private static int[] _improvedBuffList =
	{
		1499,1, // Improved Combat
		1500,1, // Improved Magic
		1501,1, // Improved Condition
		1502,1, // Improved Critical Attack
		1503,1, // Improved Shield Defense
		1504,1 // Improved Movement	
	};
	// @formatter:on
	
	private static Map<Integer, Integer> _fighterPreset;
	private static Map<Integer, Integer> _magePreset;
	private static Map<Integer, Integer> _resistPreset;
	private static Map<Integer, Integer> _improvedPreset;
	
	private static Map<Integer, Integer> _songList;
	private static Map<Integer, Integer> _danceList;
	private static Map<Integer, Integer> _orcList;
	private static Map<Integer, Integer> _humanList;
	private static Map<Integer, Integer> _elfList;
	private static Map<Integer, Integer> _darkElfList;
	private static Map<Integer, Integer> _dwarfList;
	private static Map<Integer, Integer> _kamaelList;
	private static Map<Integer, Integer> _otherList;
	private static Map<Integer, Integer> _resistList;
	private static Map<Integer, Integer> _improvedList;
	
	public BuffBBSManager()
	{
		if (init)
		{
			_fighterPreset = new HashMap<>();
			_magePreset = new HashMap<>();
			_resistPreset = new HashMap<>();
			_improvedPreset = new HashMap<>();
			
			_songList = new HashMap<>();
			_danceList = new HashMap<>();
			_orcList = new HashMap<>();
			_humanList = new HashMap<>();
			_elfList = new HashMap<>();
			_darkElfList = new HashMap<>();
			_dwarfList = new HashMap<>();
			_kamaelList = new HashMap<>();
			_otherList = new HashMap<>();
			_resistList = new HashMap<>();
			_improvedList = new HashMap<>();
			_schemes = new ConcurrentHashMap<>();
			_eachScheme = new ConcurrentHashMap<>();
			_summonOption = new ConcurrentHashMap<>();
			loadBuffs();
			loadSchemes();
		}
	}
	
	/**
	 * Initialize buff lists & presets
	 */
	private void loadBuffs()
	{
		for (int i = 0; i < _fighterBuffListPreset.length; i++)
		{
			_fighterPreset.put(_fighterBuffListPreset[i], _fighterBuffListPreset[++i]);
		}
		for (int i = 0; i < _mageBuffListPreset.length; i++)
		{
			_magePreset.put(_mageBuffListPreset[i], _mageBuffListPreset[++i]);
		}
		for (int i = 0; i < _improvedBuffListPreset.length; i++)
		{
			_improvedPreset.put(_improvedBuffListPreset[i], _improvedBuffListPreset[++i]);
		}
		for (int i = 0; i < _resistBuffListPreset.length; i++)
		{
			_resistPreset.put(_resistBuffListPreset[i], _resistBuffListPreset[++i]);
		}
		
		for (int i = 0; i < _songBuffList.length; i++)
		{
			_songList.put(_songBuffList[i], _songBuffList[++i]);
		}
		for (int i = 0; i < _danceBuffList.length; i++)
		{
			_danceList.put(_danceBuffList[i], _danceBuffList[++i]);
		}
		for (int i = 0; i < _orcBuffList.length; i++)
		{
			_orcList.put(_orcBuffList[i], _orcBuffList[++i]);
		}
		for (int i = 0; i < _humanBuffList.length; i++)
		{
			_humanList.put(_humanBuffList[i], _humanBuffList[++i]);
		}
		for (int i = 0; i < _elfBuffList.length; i++)
		{
			_elfList.put(_elfBuffList[i], _elfBuffList[++i]);
		}
		for (int i = 0; i < _darkElfBuffList.length; i++)
		{
			_darkElfList.put(_darkElfBuffList[i], _darkElfBuffList[++i]);
		}
		for (int i = 0; i < _dwarfBuffList.length; i++)
		{
			_dwarfList.put(_dwarfBuffList[i], _dwarfBuffList[++i]);
		}
		for (int i = 0; i < _kamaelBuffList.length; i++)
		{
			_kamaelList.put(_kamaelBuffList[i], _kamaelBuffList[++i]);
		}
		for (int i = 0; i < _othersBuffList.length; i++)
		{
			_otherList.put(_othersBuffList[i], _othersBuffList[++i]);
		}
		for (int i = 0; i < _resistBuffList.length; i++)
		{
			_resistList.put(_resistBuffList[i], _resistBuffList[++i]);
		}
		for (int i = 0; i < _improvedBuffList.length; i++)
		{
			_improvedList.put(_improvedBuffList[i], _improvedBuffList[++i]);
		}
		init = false;
	}
	
	public static BuffBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final BuffBBSManager _instance = new BuffBBSManager();
	}
	
	@Override
	public void parsecmd(String event, L2PcInstance player)
	{
		StringTokenizer st = new StringTokenizer(event, "; ");
		st.nextToken(); // _bbs_buff
		String commandB = st.nextToken(); // bufferCB
		
		if (commandB.startsWith(_bbscommand))
		{
			if (!st.hasMoreTokens())
			{
				
				if (!_summonOption.containsKey(player.getObjectId()))
				{
					_summonOption.put(player.getObjectId(), false);
				}
				if (_summonOption.get(player.getObjectId()))
				{
					sendHtm(player, "data/html/CommunityBoard/custom/buffer/main2.htm");
				}
				else
				{
					sendHtm(player, "data/html/CommunityBoard/custom/buffer/main.htm");
				}
				return;
			}
			
			if (st.hasMoreTokens())
			{
				String nextToken = st.nextToken();
				
				if (nextToken.contains("Preset"))
				{
					castPreset(nextToken, player);
					return;
				}
				
				if (nextToken.equalsIgnoreCase("pageLink"))
				{
					sendHtm(player, "data/html/CommunityBoard/custom/buffer/" + st.nextToken());
					return;
				}
				
				if (nextToken.equalsIgnoreCase("pageBuild"))
				{
					if (st.hasMoreTokens())
					{
						String htmlBuilt = buildHtmlBuffPage(st.nextToken(), player);
						if (htmlBuilt == null)
						{
							if (_summonOption.get(player.getObjectId()))
							{
								sendHtm(player, "data/html/CommunityBoard/custom/buffer/main2.htm");
							}
							else
							{
								sendHtm(player, "data/html/CommunityBoard/custom/buffer/main.htm");
							}
						}
						else
						{
							separateAndSend(htmlBuilt, player);
							
						}
						return;
					}
				}
				
				if (nextToken.equalsIgnoreCase("heal"))
				{
					if (ENABLE_HEAL)
					{
						if (!ENABLE_HEAL_IN_COMBAT && player.isInCombat())
						{
							player.sendMessage("Can not buff on combat");
							return;
						}
						if (!BUFF_WITH_KARMA)
						{
							player.sendMessage("Can not buff on combat");
							return;
						}
						
						if (!FREE_BUFFS)
						{
							if (HEAL_PRICE > 0)
							{
								if ((player.getInventory().getItemByItemId(Config.BUFF_CONSUMABLE_ID) == null) || (player.getInventory().getItemByItemId(Config.BUFF_CONSUMABLE_ID).getCount() < HEAL_PRICE))
								{
									player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
									return;
								}
								else
								{
									player.getInventory().destroyItemByItemId("CbBuff", Config.BUFF_CONSUMABLE_ID, HEAL_PRICE, player, true);
								}
							}
						}
					}
					player.setCurrentCp(player.getMaxCp());
					player.setCurrentHp(player.getMaxHp());
					player.setCurrentMp(player.getMaxMp());
					if (player.hasSummon())
					{
						player.getSummon().setCurrentHp(player.getSummon().getMaxHp());
						player.getSummon().setCurrentMp(player.getSummon().getMaxMp());
					}
				}
				
				if (nextToken.equalsIgnoreCase("removeBuff"))
				{
					if (ENABLE_BUFF_REMOVE)
					{
						if (!FREE_BUFFS)
						{
							if (BUFF_REMOVE_PRICE > 0)
							{
								if ((player.getInventory().getItemByItemId(Config.BUFF_CONSUMABLE_ID) == null) || (player.getInventory().getItemByItemId(Config.BUFF_CONSUMABLE_ID).getCount() < BUFF_REMOVE_PRICE))
								{
									player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
									return;
								}
								else
								{
									player.getInventory().destroyItemByItemId("CbBuff", Config.BUFF_CONSUMABLE_ID, HEAL_PRICE, player, player);
								}
							}
						}
						
						player.stopAllEffects();
						if (player.hasSummon())
						{
							player.getSummon().stopAllEffects();
						}
					}
				}
				
				if (nextToken.equalsIgnoreCase("buff"))
				{
					if (st.hasMoreTokens())
					{
						int skillId = Integer.parseInt(st.nextToken());
						if (st.hasMoreTokens())
						{
							int skillLvl = Integer.parseInt(st.nextToken());
							
							if (!FREE_BUFFS)
							{
								if (BUFF_PRICE > 0)
								{
									if ((player.getInventory().getItemByItemId(Config.BUFF_CONSUMABLE_ID) == null) || (player.getInventory().getItemByItemId(Config.BUFF_CONSUMABLE_ID).getCount() < BUFF_PRICE))
									{
										player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
										return;
									}
									else
									{
										player.getInventory().destroyItemByItemId("CbBuff", Config.BUFF_CONSUMABLE_ID, HEAL_PRICE, player, player);
									}
								}
							}
							
							if (player.hasSummon() && _summonOption.get(player.getObjectId()))
							{
								SkillData.getInstance().getSkill(skillId, skillLvl).applyEffects(player, player.getSummon());
							}
							else
							{
								SkillData.getInstance().getSkill(skillId, skillLvl).applyEffects(player, player);
							}
						}
					}
				}
				
				if (nextToken.equalsIgnoreCase("scheme"))
				{
					if (st.hasMoreTokens())
					{
						String opcion = st.nextToken();
						if (!st.hasMoreTokens())
						{
							return;
						}
						String schemeName = st.nextToken().toLowerCase().trim();
						switch (opcion)
						{
							case "add":
								if (_schemes.containsKey(player.getObjectId()))
								{
									if (_schemes.get(player.getObjectId()).containsKey(schemeName))
									{
										player.sendMessage("This scheme name is used");
										return;
									}
									
									ArrayList<Integer> buffList = new ArrayList<>();
									// Map<String, ArrayList<Integer>> mapInterno = new HashMap<>();
									
									for (BuffInfo buff : player.getEffectList().getEffects())
									{
										if (_danceList.containsKey(buff.getSkill().getId()) //
											|| _darkElfList.containsKey(buff.getSkill().getId()) //
											|| _dwarfList.containsKey(buff.getSkill().getId()) //
											|| _elfList.containsKey(buff.getSkill().getId()) //
											|| _humanList.containsKey(buff.getSkill().getId()) //
											|| _improvedList.containsKey(buff.getSkill().getId()) //
											|| _kamaelList.containsKey(buff.getSkill().getId()) //
											|| _orcList.containsKey(buff.getSkill().getId()) //
											|| _fighterPreset.containsKey(buff.getSkill().getId()) //
											|| _magePreset.containsKey(buff.getSkill().getId()) //
											|| _improvedPreset.containsKey(buff.getSkill().getId()) //
											|| _resistPreset.containsKey(buff.getSkill().getId()) //
											|| _otherList.containsKey(buff.getSkill().getId()) //
											|| _resistList.containsKey(buff.getSkill().getId()) //
											|| _songList.containsKey(buff.getSkill().getId()) //
										)
										{
											buffList.add(buff.getSkill().getId());
											buffList.add(SkillData.getInstance().getMaxLevel(buff.getSkill().getId()));
										}
									}
									_schemes.get(player.getObjectId()).put(schemeName, buffList);
									// _schemes.put(player.getObjectId(), mapInterno);
									
									for (Entry<String, ArrayList<Integer>> l : _schemes.get(player.getObjectId()).entrySet())
									{
										if (l.getKey().equalsIgnoreCase(schemeName))
										{
											for (int i = 0; i < l.getValue().size(); i++)
											{
												storeScheme(player, schemeName, l.getValue().get(i), l.getValue().get(++i));
											}
											break;
										}
										
									}
									
								}
								else
								{
									ArrayList<Integer> buffList = new ArrayList<>();
									Map<String, ArrayList<Integer>> mapInterno = new HashMap<>();
									for (BuffInfo buff : player.getEffectList().getEffects())
									{
										if (_danceList.containsKey(buff.getSkill().getId()) //
											|| _darkElfList.containsKey(buff.getSkill().getId()) //
											|| _dwarfList.containsKey(buff.getSkill().getId()) //
											|| _elfList.containsKey(buff.getSkill().getId()) //
											|| _humanList.containsKey(buff.getSkill().getId()) //
											|| _improvedList.containsKey(buff.getSkill().getId()) //
											|| _kamaelList.containsKey(buff.getSkill().getId()) //
											|| _orcList.containsKey(buff.getSkill().getId()) //
											|| _fighterPreset.containsKey(buff.getSkill().getId()) //
											|| _magePreset.containsKey(buff.getSkill().getId()) //
											|| _improvedPreset.containsKey(buff.getSkill().getId()) //
											|| _resistPreset.containsKey(buff.getSkill().getId()) //
											|| _otherList.containsKey(buff.getSkill().getId()) //
											|| _resistList.containsKey(buff.getSkill().getId()) //
											|| _songList.containsKey(buff.getSkill().getId()) //
										)
										{
											buffList.add(buff.getSkill().getId());
											buffList.add(SkillData.getInstance().getMaxLevel(buff.getSkill().getId()));
										}
									}
									mapInterno.put(schemeName, buffList);
									_schemes.put(player.getObjectId(), mapInterno);
									
									for (String schemeN : _schemes.get(player.getObjectId()).keySet())
									{
										
										for (ArrayList<Integer> l : _schemes.get(player.getObjectId()).values())
										{
											for (int i = 0; i < l.size(); i++)
											{
												storeScheme(player, schemeN, l.get(i), l.get(++i));
											}
										}
									}
									
								}
								break;
							case "remove":
								if (_schemes.containsKey(player.getObjectId()))
								{
									if (_schemes.get(player.getObjectId()).containsKey(schemeName))
									{
										removeScheme(player, schemeName);
										if (_schemes.get(player.getObjectId()).containsKey(schemeName))
										{
											Map<String, ArrayList<Integer>> mapBuff = new HashMap<>();
											mapBuff = _schemes.get(player.getObjectId());
											mapBuff.remove(schemeName);
											_schemes.put(player.getObjectId(), mapBuff);
										}
										player.sendMessage("Scheme " + schemeName + " was removed");
									}
								}
								else
								{
									player.sendMessage("Scheme " + schemeName + " can't be found");
								}
								break;
							
							default:
								break;
						}
					}
					if (_summonOption.get(player.getObjectId()))
					{
						sendHtm(player, "data/html/CommunityBoard/custom/buffer/main2.htm");
					}
					else
					{
						sendHtm(player, "data/html/CommunityBoard/custom/buffer/main.htm");
					}
					return;
				}
				
				if (nextToken.equalsIgnoreCase("schemeSet"))
				{
					if (st.hasMoreTokens())
					{
						if (!FREE_BUFFS)
						{
							if (SCHEME_BUFF_PRICE > 0)
							{
								if ((player.getInventory().getItemByItemId(Config.BUFF_CONSUMABLE_ID) == null) || (player.getInventory().getItemByItemId(Config.BUFF_CONSUMABLE_ID).getCount() < SCHEME_BUFF_PRICE))
								{
									player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
									return;
								}
								else
								{
									player.getInventory().destroyItemByItemId("CbBuff", Config.BUFF_CONSUMABLE_ID, SCHEME_BUFF_PRICE, player, player);
								}
							}
						}
						
						String schemeName = st.nextToken();
						for (Entry<String, ArrayList<Integer>> bl : _schemes.get(player.getObjectId()).entrySet())
						{
							if (bl.getKey().equalsIgnoreCase(schemeName))
							{
								for (int i = 0; i < bl.getValue().size(); i++)
								{
									if (player.hasSummon() && _summonOption.get(player.getObjectId()))
									{
										
										SkillData.getInstance().getSkill(bl.getValue().get(i), bl.getValue().get(++i)).applyEffects(player, player.getSummon());
									}
									else
									{
										
										SkillData.getInstance().getSkill(bl.getValue().get(i), bl.getValue().get(++i)).applyEffects(player, player);
									}
								}
							}
						}
					}
				}
				
				if (nextToken.equalsIgnoreCase("pet"))
				{
					if (_summonOption.get(player.getObjectId()))
					{
						_summonOption.put(player.getObjectId(), false);
					}
					else
					{
						_summonOption.put(player.getObjectId(), true);
					}
				}
			}
		}
	}
	
	/**
	 * Load all schemes from BBDD to _schemes
	 */
	@SuppressWarnings("unlikely-arg-type")
	private void loadSchemes()
	{
		final String LOAD_SCHEMES = "SELECT DISTINCT charId,scheme_name FROM buff_cb_schemes order by charId";
		final String LOAD_EACH_SCHEME = "SELECT * FROM buff_cb_schemes where charId = ? and scheme_name = ?";
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement load = con.prepareStatement(LOAD_SCHEMES))
		{
			con.setAutoCommit(false);
			try (ResultSet rset = load.executeQuery())
			{
				while (rset.next())
				{
					int charId = rset.getInt("charId");
					String schemeName = rset.getString("scheme_name");
					try (Connection con2 = ConnectionFactory.getInstance().getConnection();
						PreparedStatement load_each = con.prepareStatement(LOAD_EACH_SCHEME))
					{
						load_each.setInt(1, charId);
						load_each.setString(2, schemeName);
						con.setAutoCommit(false);
						try (ResultSet rs = load_each.executeQuery())
						{
							ArrayList<Integer> listaBuff = new ArrayList<>();
							while (rs.next())
							{
								listaBuff.add(rs.getInt("buff_id"));
								listaBuff.add(rs.getInt("buff_lvl"));
								// load.executeUpdate();
								con.commit(); // flush
							}
							
							_eachScheme.put(schemeName, listaBuff);
							
							if (_schemes.containsKey(charId))
							{
								_schemes.get(charId).remove(_eachScheme);
								_schemes.get(charId).put(schemeName, listaBuff);
								// _schemes.put(charId, eachScheme);
							}
							else
							{
								_schemes.put(charId, _eachScheme);
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					// load.executeUpdate();
					load.clearParameters();
					con.commit(); // flush
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @param player
	 * @param skill_lvl
	 * @param skillId
	 * @param schemeN
	 */
	private void storeScheme(L2PcInstance player, String schemeN, Integer skillId, Integer skill_lvl)
	{
		final String ADD_SCHEME = "INSERT INTO buff_cb_schemes (charId,scheme_name,buff_id,buff_lvl) VALUES (?,?,?,?) ";
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement store = con.prepareStatement(ADD_SCHEME))
		{
			con.setAutoCommit(false);
			
			store.setInt(1, player.getObjectId());
			store.setString(2, schemeN);
			store.setInt(3, skillId);
			store.setInt(4, skill_lvl);
			
			store.execute();
			store.clearParameters();
			con.commit(); // flush
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param player for remove your scheme
	 * @param schemeName scheme name
	 */
	private void removeScheme(L2PcInstance player, String schemeName)
	{
		final String REMOVE_SCHEME = "DELETE FROM buff_cb_schemes  WHERE charId = ? AND scheme_name=? ";
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement remove = con.prepareStatement(REMOVE_SCHEME))
		{
			remove.setInt(1, player.getCharId());
			remove.setString(2, schemeName);
			con.setAutoCommit(false);
			
			remove.execute();
			
			remove.clearParameters();
			con.commit(); // flush
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param nextToken html file name, example: dances.htm
	 * @param player player that receive html
	 * @return
	 */
	private String buildHtmlBuffPage(String nextToken, L2PcInstance player)
	{
		String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/custom/buffer/" + nextToken);
		if (content != null)
		{
			String buffList = "";
			switch (nextToken)
			{
				case "dances.htm":
					int contDance = 2;
					buffList += "<table>";
					
					for (int skillId : _danceList.keySet())
					{
						if ((contDance % 2) == 0)
						{
							buffList += "<tr>";
						}
						
						buffList += "<td width=40><button value=\"\"" + " action=\"bypass -h _bbs_buff;bufferCB buff " + skillId + " " + _danceList.get(skillId) + "\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue\"></td>";
						buffList += "<td width=40><img src=\"" + SkillData.getInstance().getSkill(skillId, 1).getIcon() + "\" width=32 height=32></td>";
						buffList += "<td width=220>" + SkillData.getInstance().getSkill(skillId, 1).getName() + "<font color=a1a1a1>Lv</font> <font color=ae9977>1</font></td>";
						if ((contDance % 2) == 0)
						{
							
							buffList += "</tr>";
						}
						contDance++;
					}
					buffList += "</table>";
					
					break;
				case "songs.htm":
					int contSong = 2;
					buffList += "<table>";
					
					for (int skillId : _songList.keySet())
					{
						if ((contSong % 2) == 0)
						{
							buffList += "<tr>";
						}
						
						buffList += "<td width=40><button value=\"\"" + " action=\"bypass -h _bbs_buff;bufferCB buff " + skillId + " " + _songList.get(skillId) + "\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue\"></td>";
						buffList += "<td width=40><img src=\"" + SkillData.getInstance().getSkill(skillId, 1).getIcon() + "\" width=32 height=32></td>";
						buffList += "<td width=220>" + SkillData.getInstance().getSkill(skillId, 1).getName() + "<font color=a1a1a1>Lv</font> <font color=ae9977>1</font></td>";
						if ((contSong % 2) == 0)
						{
							
							buffList += "</tr>";
						}
						contSong++;
					}
					buffList += "</table>";
					
					break;
				case "human.htm":
					int contHuman = 2;
					buffList += "<table>";
					
					for (int skillId : _humanList.keySet())
					{
						if ((contHuman % 2) == 0)
						{
							buffList += "<tr>";
						}
						
						buffList += "<td width=40><button value=\"\"" + " action=\"bypass -h _bbs_buff;bufferCB buff " + skillId + " " + _humanList.get(skillId) + "\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue\"></td>";
						buffList += "<td width=40><img src=\"" + SkillData.getInstance().getSkill(skillId, 1).getIcon() + "\" width=32 height=32></td>";
						buffList += "<td width=220>" + SkillData.getInstance().getSkill(skillId, 1).getName() + "<font color=a1a1a1>Lv</font> <font color=ae9977>1</font></td>";
						if ((contHuman % 2) == 0)
						{
							
							buffList += "</tr>";
						}
						contHuman++;
					}
					buffList += "</table>";
					
					break;
				case "elf.htm":
					int contElf = 2;
					buffList += "<table>";
					
					for (int skillId : _elfList.keySet())
					{
						if ((contElf % 2) == 0)
						{
							buffList += "<tr>";
						}
						
						buffList += "<td width=40><button value=\"\"" + " action=\"bypass -h _bbs_buff;bufferCB buff " + skillId + " " + _elfList.get(skillId) + "\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue\"></td>";
						buffList += "<td width=40><img src=\"" + SkillData.getInstance().getSkill(skillId, 1).getIcon() + "\" width=32 height=32></td>";
						buffList += "<td width=220>" + SkillData.getInstance().getSkill(skillId, 1).getName() + "<font color=a1a1a1>Lv</font> <font color=ae9977>1</font></td>";
						if ((contElf % 2) == 0)
						{
							
							buffList += "</tr>";
						}
						contElf++;
					}
					buffList += "</table>";
					
					break;
				case "darkelf.htm":
					int contDark = 2;
					buffList += "<table>";
					
					for (int skillId : _darkElfList.keySet())
					{
						if ((contDark % 2) == 0)
						{
							buffList += "<tr>";
						}
						
						buffList += "<td width=40><button value=\"\"" + " action=\"bypass -h _bbs_buff;bufferCB buff " + skillId + " " + _darkElfList.get(skillId) + "\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue\"></td>";
						buffList += "<td width=40><img src=\"" + SkillData.getInstance().getSkill(skillId, 1).getIcon() + "\" width=32 height=32></td>";
						buffList += "<td width=220>" + SkillData.getInstance().getSkill(skillId, 1).getName() + "<font color=a1a1a1>Lv</font> <font color=ae9977>1</font></td>";
						if ((contDark % 2) == 0)
						{
							
							buffList += "</tr>";
						}
						contDark++;
					}
					buffList += "</table>";
					
					break;
				case "dwarf.htm":
					int contDwarf = 2;
					buffList += "<table>";
					
					for (int skillId : _dwarfList.keySet())
					{
						if ((contDwarf % 2) == 0)
						{
							buffList += "<tr>";
						}
						
						buffList += "<td width=40><button value=\"\"" + " action=\"bypass -h _bbs_buff;bufferCB buff " + skillId + " " + _dwarfList.get(skillId) + "\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue\"></td>";
						buffList += "<td width=40><img src=\"" + SkillData.getInstance().getSkill(skillId, 1).getIcon() + "\" width=32 height=32></td>";
						buffList += "<td width=220>" + SkillData.getInstance().getSkill(skillId, 1).getName() + "<font color=a1a1a1>Lv</font> <font color=ae9977>1</font></td>";
						if ((contDwarf % 2) == 0)
						{
							
							buffList += "</tr>";
						}
						contDwarf++;
					}
					buffList += "</table>";
					
					break;
				case "orc.htm":
					int contOrc = 2;
					buffList += "<table>";
					
					for (int skillId : _orcList.keySet())
					{
						if ((contOrc % 2) == 0)
						{
							buffList += "<tr>";
						}
						
						buffList += "<td width=40><button value=\"\"" + " action=\"bypass -h _bbs_buff;bufferCB buff " + skillId + " " + _orcList.get(skillId) + "\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue\"></td>";
						buffList += "<td width=40><img src=\"" + SkillData.getInstance().getSkill(skillId, 1).getIcon() + "\" width=32 height=32></td>";
						buffList += "<td width=220>" + SkillData.getInstance().getSkill(skillId, 1).getName() + "<font color=a1a1a1>Lv</font> <font color=ae9977>1</font></td>";
						if ((contOrc % 2) == 0)
						{
							
							buffList += "</tr>";
						}
						contOrc++;
					}
					buffList += "</table>";
					
					break;
				case "kamael.htm":
					int contKamael = 2;
					buffList += "<table>";
					
					for (int skillId : _kamaelList.keySet())
					{
						if ((contKamael % 2) == 0)
						{
							buffList += "<tr>";
						}
						
						buffList += "<td width=40><button value=\"\"" + " action=\"bypass -h _bbs_buff;bufferCB buff " + skillId + " " + _kamaelList.get(skillId) + "\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue\"></td>";
						buffList += "<td width=40><img src=\"" + SkillData.getInstance().getSkill(skillId, 1).getIcon() + "\" width=32 height=32></td>";
						buffList += "<td width=220>" + SkillData.getInstance().getSkill(skillId, 1).getName() + "<font color=a1a1a1>Lv</font> <font color=ae9977>1</font></td>";
						if ((contKamael % 2) == 0)
						{
							
							buffList += "</tr>";
						}
						contKamael++;
					}
					buffList += "</table>";
					
					break;
				case "others.htm":
					int contOther = 2;
					buffList += "<table>";
					
					for (int skillId : _otherList.keySet())
					{
						if ((contOther % 2) == 0)
						{
							buffList += "<tr>";
						}
						
						buffList += "<td width=40><button value=\"\"" + " action=\"bypass -h _bbs_buff;bufferCB buff " + skillId + " " + _otherList.get(skillId) + "\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue\"></td>";
						buffList += "<td width=40><img src=\"" + SkillData.getInstance().getSkill(skillId, 1).getIcon() + "\" width=32 height=32></td>";
						buffList += "<td width=220>" + SkillData.getInstance().getSkill(skillId, 1).getName() + "<font color=a1a1a1>Lv</font> <font color=ae9977>1</font></td>";
						if ((contOther % 2) == 0)
						{
							
							buffList += "</tr>";
						}
						contOther++;
					}
					buffList += "</table>";
					
					break;
				case "resist.htm":
					int contResist = 2;
					buffList += "<table>";
					
					for (int skillId : _resistList.keySet())
					{
						if ((contResist % 2) == 0)
						{
							buffList += "<tr>";
						}
						
						buffList += "<td width=40><button value=\"\"" + " action=\"bypass -h _bbs_buff;bufferCB buff " + skillId + " " + _resistList.get(skillId) + "\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue\"></td>";
						buffList += "<td width=40><img src=\"" + SkillData.getInstance().getSkill(skillId, 1).getIcon() + "\" width=32 height=32></td>";
						buffList += "<td width=220>" + SkillData.getInstance().getSkill(skillId, 1).getName() + "<font color=a1a1a1>Lv</font> <font color=ae9977>1</font></td>";
						if ((contResist % 2) == 0)
						{
							
							buffList += "</tr>";
						}
						contResist++;
					}
					buffList += "</table>";
					
					break;
				case "improved.htm":
					int contImproved = 2;
					buffList += "<table>";
					
					for (int skillId : _improvedList.keySet())
					{
						if ((contImproved % 2) == 0)
						{
							buffList += "<tr>";
						}
						
						buffList += "<td width=40><button value=\"\"" + " action=\"bypass -h _bbs_buff;bufferCB buff " + skillId + " " + _improvedList.get(skillId) + "\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue\"></td>";
						buffList += "<td width=40><img src=\"" + SkillData.getInstance().getSkill(skillId, 1).getIcon() + "\" width=32 height=32></td>";
						buffList += "<td width=220>" + SkillData.getInstance().getSkill(skillId, 1).getName() + "<font color=a1a1a1>Lv</font> <font color=ae9977>1</font></td>";
						if ((contImproved % 2) == 0)
						{
							
							buffList += "</tr>";
						}
						contImproved++;
					}
					buffList += "</table>";
					
					break;
				
				case "scheme.htm":
					int contScheme = 2;
					buffList += "<table>";
					if (_schemes.containsKey(player.getObjectId()))
					{
						for (String name : _schemes.get(player.getObjectId()).keySet())
						{
							if ((contScheme % 2) == 0)
							{
								buffList += "<tr>";
							}
							
							buffList += "<td width=200><button value=\"" + name + "\" action=\"bypass -h _bbs_buff;bufferCB schemeSet " + name + "\" width=200 height=32 back=\"L2UI_CT1.OlympiadWnd_DF_HeroConfirm_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_HeroConfirm\"></td>";
							if ((contScheme % 2) == 0)
							{
								
								buffList += "</tr>";
							}
							contScheme++;
						}
					}
					else
					{
						
					}
					buffList += "</table>";
					break;
				default:
					break;
			}
			
			return content.replaceAll("%buffList%", buffList);
		}
		return null;
	}
	
	/**
	 * @param preset preset choosen
	 * @param player player for buff
	 */
	private void castPreset(String preset, L2PcInstance player)
	{
		if (!FREE_BUFFS)
		{
			if (BUFF_SET_PRICE > 0)
			{
				if ((player.getInventory().getItemByItemId(Config.BUFF_CONSUMABLE_ID) == null) || (player.getInventory().getItemByItemId(Config.BUFF_CONSUMABLE_ID).getCount() < BUFF_SET_PRICE))
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				else
				{
					player.getInventory().destroyItemByItemId("CbBuff", Config.BUFF_CONSUMABLE_ID, BUFF_SET_PRICE, player, player);
				}
			}
		}
		
		switch (preset)
		{
			case "fighterPreset":
				for (int skill : _fighterPreset.keySet())
				{
					player.broadcastPacket(new MagicSkillUse(player, skill, _fighterPreset.get(skill), 0, 1));
					if (player.hasSummon() && _summonOption.get(player.getObjectId()))
					{
						SkillData.getInstance().getSkill(skill, _fighterPreset.get(skill)).applyEffects(player, player.getSummon());
					}
					else
					{
						SkillData.getInstance().getSkill(skill, _fighterPreset.get(skill)).applyEffects(player, player);
					}
				}
				if (_summonOption.get(player.getObjectId()))
				{
					sendHtm(player, "data/html/CommunityBoard/custom/buffer/main2.htm");
				}
				else
				{
					sendHtm(player, "data/html/CommunityBoard/custom/buffer/main.htm");
				}
				break;
			
			case "magePreset":
				for (int skill : _magePreset.keySet())
				{
					player.broadcastPacket(new MagicSkillUse(player, skill, _magePreset.get(skill), 0, 1));
					if (player.hasSummon() && _summonOption.get(player.getObjectId()))
					{
						SkillData.getInstance().getSkill(skill, _magePreset.get(skill)).applyEffects(player, player.getSummon());
					}
					else
					{
						SkillData.getInstance().getSkill(skill, _magePreset.get(skill)).applyEffects(player, player);
					}
				}
				
				if (_summonOption.get(player.getObjectId()))
				{
					sendHtm(player, "data/html/CommunityBoard/custom/buffer/main2.htm");
				}
				else
				{
					sendHtm(player, "data/html/CommunityBoard/custom/buffer/main.htm");
				}
				break;
			case "resistPreset":
				for (int skill : _resistPreset.keySet())
				{
					player.broadcastPacket(new MagicSkillUse(player, skill, _resistPreset.get(skill), 0, 1));
					if (player.hasSummon() && _summonOption.get(player.getObjectId()))
					{
						SkillData.getInstance().getSkill(skill, _resistPreset.get(skill)).applyEffects(player, player.getSummon());
					}
					else
					{
						SkillData.getInstance().getSkill(skill, _resistPreset.get(skill)).applyEffects(player, player);
					}
				}
				
				if (_summonOption.get(player.getObjectId()))
				{
					sendHtm(player, "data/html/CommunityBoard/custom/buffer/main2.htm");
				}
				else
				{
					sendHtm(player, "data/html/CommunityBoard/custom/buffer/main.htm");
				}
				break;
			case "improvedPreset":
				for (int skill : _improvedPreset.keySet())
				{
					player.broadcastPacket(new MagicSkillUse(player, skill, _improvedPreset.get(skill), 0, 1));
					if (player.hasSummon() && _summonOption.get(player.getObjectId()))
					{
						SkillData.getInstance().getSkill(skill, _improvedPreset.get(skill)).applyEffects(player, player.getSummon());
					}
					else
					{
						SkillData.getInstance().getSkill(skill, _improvedPreset.get(skill)).applyEffects(player, player);
					}
				}
				
				if (_summonOption.get(player.getObjectId()))
				{
					sendHtm(player, "data/html/CommunityBoard/custom/buffer/main2.htm");
				}
				else
				{
					sendHtm(player, "data/html/CommunityBoard/custom/buffer/main.htm");
				}
				break;
			default:
				break;
		}
	}
	
	private boolean sendHtm(L2PcInstance player, String path)
	{
		String oriPath = path;
		if ((player.getLang() != null) && !player.getLang().equalsIgnoreCase("en"))
		{
			if (path.contains("html/"))
			{
				path = path.replace("html/", "html-" + player.getLang() + "/");
			}
		}
		String content = HtmCache.getInstance().getHtm(path);
		if ((content == null) && !oriPath.equals(path))
		{
			content = HtmCache.getInstance().getHtm(oriPath);
		}
		if (content == null)
		{
			return false;
		}
		
		separateAndSend(content, player);
		return true;
	}
	
	@Override
	public void parsewrite(String s, String s1, String s2, String s3, String s4, L2PcInstance l2pcinstance)
	{
	}
}