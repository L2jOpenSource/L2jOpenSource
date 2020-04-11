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
package com.l2jserver.gameserver.data.xml.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.Config;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.enums.AISkillScope;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.base.ClassId;
import com.l2jserver.gameserver.model.drops.DropListScope;
import com.l2jserver.gameserver.model.drops.GeneralDropItem;
import com.l2jserver.gameserver.model.drops.GroupedGeneralDropItem;
import com.l2jserver.gameserver.model.drops.IDropItem;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.holders.MinionHolder;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.util.Util;
import com.l2jserver.util.data.xml.IXmlReader;

/**
 * NPC data parser.
 * @author NosBit
 */
public class NpcData implements IXmlReader
{
	private final Map<Integer, L2NpcTemplate> _npcs = new ConcurrentHashMap<>();
	private final Map<String, Integer> _clans = new ConcurrentHashMap<>();
	private MinionData _minionData;
	
	protected NpcData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_minionData = new MinionData();
		
		parseDatapackDirectory("data/stats/npcs", false);
		LOG.info("{}: Loaded {} NPCs.", getClass().getSimpleName(), _npcs.size());
		
		if (Config.CUSTOM_NPC_DATA)
		{
			final int npcCount = _npcs.size();
			parseDatapackDirectory("data/stats/npcs/custom", true);
			LOG.info("{}: Loaded {} custom NPCs.", getClass().getSimpleName(), (_npcs.size() - npcCount));
		}
		
		_minionData = null;
		loadNpcsSkillLearn();
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node node = doc.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if ("list".equalsIgnoreCase(node.getNodeName()))
			{
				for (Node listNode = node.getFirstChild(); listNode != null; listNode = listNode.getNextSibling())
				{
					if ("npc".equalsIgnoreCase(listNode.getNodeName()))
					{
						NamedNodeMap attrs = listNode.getAttributes();
						final StatsSet set = new StatsSet();
						final int npcId = parseInteger(attrs, "id");
						Map<String, Object> parameters = null;
						Map<Integer, Skill> skills = null;
						Set<Integer> clans = null;
						Set<Integer> ignoreClanNpcIds = null;
						Map<DropListScope, List<IDropItem>> dropLists = null;
						set.set("id", npcId);
						set.set("displayId", parseInteger(attrs, "displayId"));
						set.set("level", parseByte(attrs, "level"));
						set.set("type", parseString(attrs, "type"));
						set.set("name", parseString(attrs, "name"));
						set.set("usingServerSideName", parseBoolean(attrs, "usingServerSideName"));
						set.set("title", parseString(attrs, "title"));
						set.set("usingServerSideTitle", parseBoolean(attrs, "usingServerSideTitle"));
						for (Node npcNode = listNode.getFirstChild(); npcNode != null; npcNode = npcNode.getNextSibling())
						{
							attrs = npcNode.getAttributes();
							switch (npcNode.getNodeName().toLowerCase())
							{
								case "parameters":
								{
									if (parameters == null)
									{
										parameters = new HashMap<>();
									}
									
									for (Node parametersNode = npcNode.getFirstChild(); parametersNode != null; parametersNode = parametersNode.getNextSibling())
									{
										attrs = parametersNode.getAttributes();
										switch (parametersNode.getNodeName().toLowerCase())
										{
											case "param":
											{
												parameters.put(parseString(attrs, "name"), parseString(attrs, "value"));
												break;
											}
											case "skill":
											{
												parameters.put(parseString(attrs, "name"), new SkillHolder(parseInteger(attrs, "id"), parseInteger(attrs, "level")));
												break;
											}
											case "minions":
											{
												final List<MinionHolder> minions = new ArrayList<>(1);
												for (Node minionsNode = parametersNode.getFirstChild(); minionsNode != null; minionsNode = minionsNode.getNextSibling())
												{
													if (minionsNode.getNodeName().equalsIgnoreCase("npc"))
													{
														attrs = minionsNode.getAttributes();
														minions.add(new MinionHolder(parseInteger(attrs, "id"), parseInteger(attrs, "count"), parseInteger(attrs, "respawnTime"), parseInteger(attrs, "weightPoint")));
													}
												}
												
												if (!minions.isEmpty())
												{
													parameters.put(parseString(parametersNode.getAttributes(), "name"), minions);
												}
												
												break;
											}
										}
									}
									break;
								}
								case "race":
								case "sex":
									set.set(npcNode.getNodeName(), npcNode.getTextContent().toUpperCase());
									break;
								case "equipment":
								{
									set.set("chestId", parseInteger(attrs, "chest"));
									set.set("rhandId", parseInteger(attrs, "rhand"));
									set.set("lhandId", parseInteger(attrs, "lhand"));
									set.set("weaponEnchant", parseInteger(attrs, "weaponEnchant"));
									break;
								}
								case "acquire":
								{
									set.set("expRate", parseDouble(attrs, "expRate"));
									set.set("sp", parseDouble(attrs, "sp"));
									set.set("raidPoints", parseDouble(attrs, "raidPoints"));
									break;
								}
								case "stats":
								{
									set.set("baseSTR", parseInteger(attrs, "str"));
									set.set("baseINT", parseInteger(attrs, "int"));
									set.set("baseDEX", parseInteger(attrs, "dex"));
									set.set("baseWIT", parseInteger(attrs, "wit"));
									set.set("baseCON", parseInteger(attrs, "con"));
									set.set("baseMEN", parseInteger(attrs, "men"));
									for (Node statsNode = npcNode.getFirstChild(); statsNode != null; statsNode = statsNode.getNextSibling())
									{
										attrs = statsNode.getAttributes();
										switch (statsNode.getNodeName().toLowerCase())
										{
											case "vitals":
											{
												set.set("baseHpMax", parseDouble(attrs, "hp"));
												set.set("baseHpReg", parseDouble(attrs, "hpRegen"));
												set.set("baseMpMax", parseDouble(attrs, "mp"));
												set.set("baseMpReg", parseDouble(attrs, "mpRegen"));
												break;
											}
											case "attack":
											{
												set.set("basePAtk", parseDouble(attrs, "physical"));
												set.set("baseMAtk", parseDouble(attrs, "magical"));
												set.set("baseRndDam", parseInteger(attrs, "random"));
												set.set("baseCritRate", parseInteger(attrs, "critical"));
												set.set("accuracy", parseDouble(attrs, "accuracy"));// TODO: Implement me
												set.set("basePAtkSpd", parseInteger(attrs, "attackSpeed"));
												set.set("reuseDelay", parseInteger(attrs, "reuseDelay"));// TODO: Implement me
												set.set("baseAtkType", parseString(attrs, "type"));
												set.set("baseAtkRange", parseInteger(attrs, "range"));
												set.set("distance", parseInteger(attrs, "distance"));// TODO: Implement me
												set.set("width", parseInteger(attrs, "width"));// TODO: Implement me
												break;
											}
											case "defence":
											{
												set.set("basePDef", parseDouble(attrs, "physical"));
												set.set("baseMDef", parseDouble(attrs, "magical"));
												set.set("evasion", parseInteger(attrs, "evasion"));// TODO: Implement me
												set.set("baseShldDef", parseInteger(attrs, "shield"));
												set.set("baseShldRate", parseInteger(attrs, "shieldRate"));
												break;
											}
											case "attribute":
											{
												for (Node attributeNode = statsNode.getFirstChild(); attributeNode != null; attributeNode = attributeNode.getNextSibling())
												{
													attrs = attributeNode.getAttributes();
													switch (attributeNode.getNodeName().toLowerCase())
													{
														case "attack":
														{
															String attackAttributeType = parseString(attrs, "type");
															switch (attackAttributeType.toUpperCase())
															{
																case "FIRE":
																	set.set("baseFire", parseInteger(attrs, "value"));
																	break;
																case "WATER":
																	set.set("baseWater", parseInteger(attrs, "value"));
																	break;
																case "WIND":
																	set.set("baseWind", parseInteger(attrs, "value"));
																	break;
																case "EARTH":
																	set.set("baseEarth", parseInteger(attrs, "value"));
																	break;
																case "DARK":
																	set.set("baseDark", parseInteger(attrs, "value"));
																	break;
																case "HOLY":
																	set.set("baseHoly", parseInteger(attrs, "value"));
																	break;
															}
															break;
														}
														case "defence":
														{
															set.set("baseFireRes", parseInteger(attrs, "fire"));
															set.set("baseWaterRes", parseInteger(attrs, "water"));
															set.set("baseWindRes", parseInteger(attrs, "wind"));
															set.set("baseEarthRes", parseInteger(attrs, "earth"));
															set.set("baseHolyRes", parseInteger(attrs, "holy"));
															set.set("baseDarkRes", parseInteger(attrs, "dark"));
															set.set("baseElementRes", parseInteger(attrs, "default"));
															break;
														}
													}
												}
												break;
											}
											case "speed":
											{
												for (Node speedNode = statsNode.getFirstChild(); speedNode != null; speedNode = speedNode.getNextSibling())
												{
													attrs = speedNode.getAttributes();
													switch (speedNode.getNodeName().toLowerCase())
													{
														case "walk":
														{
															set.set("baseWalkSpd", parseDouble(attrs, "ground"));
															set.set("baseSwimWalkSpd", parseDouble(attrs, "swim"));
															set.set("baseFlyWalkSpd", parseDouble(attrs, "fly"));
															break;
														}
														case "run":
														{
															set.set("baseRunSpd", parseDouble(attrs, "ground"));
															set.set("baseSwimRunSpd", parseDouble(attrs, "swim"));
															set.set("baseFlyRunSpd", parseDouble(attrs, "fly"));
															break;
														}
													}
												}
												break;
											}
											case "hittime":
												set.set("hitTime", npcNode.getTextContent());// TODO: Implement me default 600 (value in ms)
												break;
										}
									}
									break;
								}
								case "status":
								{
									set.set("unique", parseBoolean(attrs, "unique"));
									set.set("attackable", parseBoolean(attrs, "attackable"));
									set.set("targetable", parseBoolean(attrs, "targetable"));
									set.set("undying", parseBoolean(attrs, "undying"));
									set.set("showName", parseBoolean(attrs, "showName"));
									set.set("flying", parseBoolean(attrs, "flying"));
									set.set("canMove", parseBoolean(attrs, "canMove"));
									set.set("noSleepMode", parseBoolean(attrs, "noSleepMode"));
									set.set("passableDoor", parseBoolean(attrs, "passableDoor"));
									set.set("hasSummoner", parseBoolean(attrs, "hasSummoner"));
									set.set("canBeSown", parseBoolean(attrs, "canBeSown"));
									break;
								}
								case "skilllist":
								{
									skills = new HashMap<>();
									for (Node skillListNode = npcNode.getFirstChild(); skillListNode != null; skillListNode = skillListNode.getNextSibling())
									{
										if ("skill".equalsIgnoreCase(skillListNode.getNodeName()))
										{
											attrs = skillListNode.getAttributes();
											final int skillId = parseInteger(attrs, "id");
											final int skillLevel = parseInteger(attrs, "level");
											final Skill skill = SkillData.getInstance().getSkill(skillId, skillLevel);
											if (skill != null)
											{
												skills.put(skill.getId(), skill);
											}
											else
											{
												LOG.warn("[{}] skill not found. NPC ID: {} Skill ID: {} Skill Level: {}!", f.getName(), npcId, skillId, skillLevel);
											}
										}
									}
									break;
								}
								case "shots":
								{
									set.set("soulShot", parseInteger(attrs, "soul"));
									set.set("spiritShot", parseInteger(attrs, "spirit"));
									set.set("shotShotChance", parseInteger(attrs, "shotChance"));
									set.set("spiritShotChance", parseInteger(attrs, "spiritChance"));
									break;
								}
								case "corpsetime":
									set.set("corpseTime", npcNode.getTextContent());
									break;
								case "excrteffect":
									set.set("exCrtEffect", npcNode.getTextContent()); // TODO: Implement me default ? type boolean
									break;
								case "snpcprophprate":
									set.set("sNpcPropHpRate", npcNode.getTextContent()); // TODO: Implement me default 1 type double
									break;
								case "ai":
								{
									set.set("aiType", parseString(attrs, "type"));
									set.set("aggroRange", parseInteger(attrs, "aggroRange"));
									set.set("clanHelpRange", parseInteger(attrs, "clanHelpRange"));
									set.set("dodge", parseInteger(attrs, "dodge"));
									set.set("isChaos", parseBoolean(attrs, "isChaos"));
									set.set("isAggressive", parseBoolean(attrs, "isAggressive"));
									for (Node aiNode = npcNode.getFirstChild(); aiNode != null; aiNode = aiNode.getNextSibling())
									{
										attrs = aiNode.getAttributes();
										switch (aiNode.getNodeName().toLowerCase())
										{
											case "skill":
											{
												set.set("minSkillChance", parseInteger(attrs, "minChance"));
												set.set("maxSkillChance", parseInteger(attrs, "maxChance"));
												set.set("primarySkillId", parseInteger(attrs, "primaryId"));
												set.set("shortRangeSkillId", parseInteger(attrs, "shortRangeId"));
												set.set("shortRangeSkillChance", parseInteger(attrs, "shortRangeChance"));
												set.set("longRangeSkillId", parseInteger(attrs, "longRangeId"));
												set.set("longRangeSkillChance", parseInteger(attrs, "longRangeChance"));
												break;
											}
											case "clanlist":
											{
												for (Node clanListNode = aiNode.getFirstChild(); clanListNode != null; clanListNode = clanListNode.getNextSibling())
												{
													switch (clanListNode.getNodeName().toLowerCase())
													{
														case "clan":
														{
															if (clans == null)
															{
																clans = new HashSet<>(1);
															}
															clans.add(getOrCreateClanId(clanListNode.getTextContent()));
															break;
														}
														case "ignorenpcid":
														{
															if (ignoreClanNpcIds == null)
															{
																ignoreClanNpcIds = new HashSet<>(1);
															}
															ignoreClanNpcIds.add(Integer.parseInt(clanListNode.getTextContent()));
															break;
														}
													}
												}
												break;
											}
										}
									}
									break;
								}
								case "droplists":
								{
									for (Node dropListsNode = npcNode.getFirstChild(); dropListsNode != null; dropListsNode = dropListsNode.getNextSibling())
									{
										DropListScope dropListScope = null;
										
										try
										{
											dropListScope = Enum.valueOf(DropListScope.class, dropListsNode.getNodeName().toUpperCase());
										}
										catch (Exception e)
										{
										}
										
										if (dropListScope != null)
										{
											if (dropLists == null)
											{
												dropLists = new EnumMap<>(DropListScope.class);
											}
											
											List<IDropItem> dropList = new ArrayList<>();
											parseDropList(f, dropListsNode, dropListScope, dropList);
											dropLists.put(dropListScope, Collections.unmodifiableList(dropList));
										}
									}
									break;
								}
								case "collision":
								{
									for (Node collisionNode = npcNode.getFirstChild(); collisionNode != null; collisionNode = collisionNode.getNextSibling())
									{
										attrs = collisionNode.getAttributes();
										switch (collisionNode.getNodeName().toLowerCase())
										{
											case "radius":
											{
												set.set("collisionRadius", parseDouble(attrs, "normal"));
												set.set("collisionRadiusGrown", parseDouble(attrs, "grown"));
												break;
											}
											case "height":
											{
												set.set("collisionHeight", parseDouble(attrs, "normal"));
												set.set("collisionHeightGrown", parseDouble(attrs, "grown"));
												break;
											}
										}
									}
									break;
								}
							}
						}
						
						L2NpcTemplate template = _npcs.get(npcId);
						if (template == null)
						{
							template = new L2NpcTemplate(set);
							_npcs.put(template.getId(), template);
						}
						else
						{
							template.set(set);
						}
						
						if (_minionData._tempMinions.containsKey(npcId))
						{
							if (parameters == null)
							{
								parameters = new HashMap<>();
							}
							parameters.putIfAbsent("Privates", _minionData._tempMinions.get(npcId));
						}
						
						if (parameters != null)
						{
							// Using unmodifiable map parameters of template are not meant to be changed at runtime.
							template.setParameters(new StatsSet(Collections.unmodifiableMap(parameters)));
						}
						else
						{
							template.setParameters(StatsSet.EMPTY_STATSET);
						}
						
						if (skills != null)
						{
							Map<AISkillScope, List<Skill>> aiSkillLists = null;
							for (Skill skill : skills.values())
							{
								if (skill.isPassive())
								{
									continue;
								}
								
								if (aiSkillLists == null)
								{
									aiSkillLists = new EnumMap<>(AISkillScope.class);
								}
								
								final List<AISkillScope> aiSkillScopes = new ArrayList<>();
								final AISkillScope shortOrLongRangeScope = skill.getCastRange() <= 150 ? AISkillScope.SHORT_RANGE : AISkillScope.LONG_RANGE;
								if (skill.isSuicideAttack())
								{
									aiSkillScopes.add(AISkillScope.SUICIDE);
								}
								else
								{
									aiSkillScopes.add(AISkillScope.GENERAL);
									
									if (skill.isContinuous())
									{
										if (!skill.isDebuff())
										{
											aiSkillScopes.add(AISkillScope.BUFF);
										}
										else
										{
											aiSkillScopes.add(AISkillScope.DEBUFF);
											aiSkillScopes.add(AISkillScope.COT);
											aiSkillScopes.add(shortOrLongRangeScope);
										}
									}
									else
									{
										if (skill.hasEffectType(L2EffectType.DISPEL))
										{
											aiSkillScopes.add(AISkillScope.NEGATIVE);
											aiSkillScopes.add(shortOrLongRangeScope);
										}
										else if (skill.hasEffectType(L2EffectType.HP))
										{
											aiSkillScopes.add(AISkillScope.HEAL);
										}
										else if (skill.hasEffectType(L2EffectType.PHYSICAL_ATTACK, L2EffectType.MAGICAL_ATTACK, L2EffectType.HP_DRAIN))
										{
											aiSkillScopes.add(AISkillScope.ATTACK);
											aiSkillScopes.add(AISkillScope.UNIVERSAL);
											aiSkillScopes.add(shortOrLongRangeScope);
										}
										else if (skill.hasEffectType(L2EffectType.SLEEP))
										{
											aiSkillScopes.add(AISkillScope.IMMOBILIZE);
										}
										else if (skill.hasEffectType(L2EffectType.STUN, L2EffectType.ROOT))
										{
											aiSkillScopes.add(AISkillScope.IMMOBILIZE);
											aiSkillScopes.add(shortOrLongRangeScope);
										}
										else if (skill.hasEffectType(L2EffectType.MUTE, L2EffectType.FEAR))
										{
											aiSkillScopes.add(AISkillScope.COT);
											aiSkillScopes.add(shortOrLongRangeScope);
										}
										else if (skill.hasEffectType(L2EffectType.PARALYZE))
										{
											aiSkillScopes.add(AISkillScope.IMMOBILIZE);
											aiSkillScopes.add(shortOrLongRangeScope);
										}
										else if (skill.hasEffectType(L2EffectType.DMG_OVER_TIME))
										{
											aiSkillScopes.add(shortOrLongRangeScope);
										}
										else if (skill.hasEffectType(L2EffectType.RESURRECTION))
										{
											aiSkillScopes.add(AISkillScope.RES);
										}
										else
										{
											aiSkillScopes.add(AISkillScope.UNIVERSAL);
										}
									}
								}
								
								for (AISkillScope aiSkillScope : aiSkillScopes)
								{
									List<Skill> aiSkills = aiSkillLists.get(aiSkillScope);
									if (aiSkills == null)
									{
										aiSkills = new ArrayList<>();
										aiSkillLists.put(aiSkillScope, aiSkills);
									}
									
									aiSkills.add(skill);
								}
							}
							
							template.setSkills(skills);
							template.setAISkillLists(aiSkillLists);
						}
						else
						{
							template.setSkills(null);
							template.setAISkillLists(null);
						}
						
						template.setClans(clans);
						template.setIgnoreClanNpcIds(ignoreClanNpcIds);
						
						template.setDropLists(dropLists);
					}
				}
			}
		}
	}
	
	private void parseDropList(File f, Node dropListNode, DropListScope dropListScope, List<IDropItem> drops)
	{
		for (Node dropNode = dropListNode.getFirstChild(); dropNode != null; dropNode = dropNode.getNextSibling())
		{
			NamedNodeMap attrs = dropNode.getAttributes();
			switch (dropNode.getNodeName().toLowerCase())
			{
				case "group":
				{
					GroupedGeneralDropItem dropItem = dropListScope.newGroupedDropItem(parseDouble(attrs, "chance"));
					List<IDropItem> groupedDropList = new ArrayList<>(2);
					for (Node groupNode = dropNode.getFirstChild(); groupNode != null; groupNode = groupNode.getNextSibling())
					{
						parseDropListItem(groupNode, dropListScope, groupedDropList);
					}
					
					List<GeneralDropItem> items = new ArrayList<>(groupedDropList.size());
					for (IDropItem item : groupedDropList)
					{
						if (item instanceof GeneralDropItem)
						{
							items.add((GeneralDropItem) item);
						}
						else
						{
							LOG.warn("[{}] grouped general drop item supports only general drop item.", f);
						}
					}
					dropItem.setItems(items);
					
					drops.add(dropItem);
					break;
				}
				default:
				{
					parseDropListItem(dropNode, dropListScope, drops);
					break;
				}
			}
		}
	}
	
	private void parseDropListItem(Node dropListItem, DropListScope dropListScope, List<IDropItem> drops)
	{
		NamedNodeMap attrs = dropListItem.getAttributes();
		switch (dropListItem.getNodeName().toLowerCase())
		{
			case "item":
			{
				final IDropItem dropItem = dropListScope.newDropItem(parseInteger(attrs, "id"), parseLong(attrs, "min"), parseLong(attrs, "max"), parseDouble(attrs, "chance"));
				if (dropItem != null)
				{
					drops.add(dropItem);
				}
				break;
			}
		}
	}
	
	/**
	 * Gets or creates a clan id if it doesnt exists.
	 * @param clanName the clan name to get or create its id
	 * @return the clan id for the given clan name
	 */
	private int getOrCreateClanId(String clanName)
	{
		Integer id = _clans.get(clanName.toUpperCase());
		if (id == null)
		{
			id = _clans.size();
			_clans.put(clanName.toUpperCase(), id);
		}
		return id;
	}
	
	/**
	 * Gets the clan id
	 * @param clanName the clan name to get its id
	 * @return the clan id for the given clan name if it exists, -1 otherwise
	 */
	public int getClanId(String clanName)
	{
		Integer id = _clans.get(clanName.toUpperCase());
		return id != null ? id : -1;
	}
	
	/**
	 * Gets the template.
	 * @param id the template Id to get.
	 * @return the template for the given id.
	 */
	public L2NpcTemplate getTemplate(int id)
	{
		return _npcs.get(id);
	}
	
	/**
	 * Gets the template by name.
	 * @param name of the template to get.
	 * @return the template for the given name.
	 */
	public L2NpcTemplate getTemplateByName(String name)
	{
		for (L2NpcTemplate npcTemplate : _npcs.values())
		{
			if (npcTemplate.getName().equalsIgnoreCase(name))
			{
				return npcTemplate;
			}
		}
		return null;
	}
	
	/**
	 * Gets all templates matching the filter.
	 * @param filter
	 * @return the template list for the given filter
	 */
	public List<L2NpcTemplate> getTemplates(Predicate<L2NpcTemplate> filter)
	{
		//@formatter:off
			return _npcs.values().stream()
			.filter(filter)
			.collect(Collectors.toList());
		//@formatter:on
	}
	
	/**
	 * Gets the all of level.
	 * @param lvls of all the templates to get.
	 * @return the template list for the given level.
	 */
	public List<L2NpcTemplate> getAllOfLevel(int... lvls)
	{
		return getTemplates(template -> Util.contains(lvls, template.getLevel()));
	}
	
	/**
	 * Gets the all monsters of level.
	 * @param lvls of all the monster templates to get.
	 * @return the template list for the given level.
	 */
	public List<L2NpcTemplate> getAllMonstersOfLevel(int... lvls)
	{
		return getTemplates(template -> Util.contains(lvls, template.getLevel()) && template.isType("L2Monster"));
	}
	
	/**
	 * Gets the all npc starting with.
	 * @param text of all the NPC templates which its name start with.
	 * @return the template list for the given letter.
	 */
	public List<L2NpcTemplate> getAllNpcStartingWith(String text)
	{
		return getTemplates(template -> template.isType("L2Npc") && template.getName().startsWith(text));
	}
	
	/**
	 * Gets the all npc of class type.
	 * @param classTypes of all the templates to get.
	 * @return the template list for the given class type.
	 */
	public List<L2NpcTemplate> getAllNpcOfClassType(String... classTypes)
	{
		return getTemplates(template -> Util.contains(classTypes, template.getType(), true));
	}
	
	public void loadNpcsSkillLearn()
	{
		_npcs.values().forEach(template ->
		{
			final List<ClassId> teachInfo = SkillLearnData.getInstance().getSkillLearnData(template.getId());
			if (teachInfo != null)
			{
				template.addTeachInfo(teachInfo);
			}
		});
	}
	
	/**
	 * This class handles minions from Spawn System<br>
	 * Once Spawn System gets reworked delete this class<br>
	 * @author Zealar
	 */
	private final class MinionData implements IXmlReader
	{
		public final Map<Integer, List<MinionHolder>> _tempMinions = new HashMap<>();
		
		protected MinionData()
		{
			load();
		}
		
		@Override
		public void load()
		{
			_tempMinions.clear();
			parseDatapackFile("data/minionData.xml");
			LOG.info("{}: Loaded {} minions data.", getClass().getSimpleName(), _tempMinions.size());
		}
		
		@Override
		public void parseDocument(Document doc)
		{
			for (Node node = doc.getFirstChild(); node != null; node = node.getNextSibling())
			{
				if ("list".equals(node.getNodeName()))
				{
					for (Node listNode = node.getFirstChild(); listNode != null; listNode = listNode.getNextSibling())
					{
						if ("npc".equals(listNode.getNodeName()))
						{
							final List<MinionHolder> minions = new ArrayList<>(1);
							NamedNodeMap attrs = listNode.getAttributes();
							int id = parseInteger(attrs, "id");
							for (Node npcNode = listNode.getFirstChild(); npcNode != null; npcNode = npcNode.getNextSibling())
							{
								if ("minion".equals(npcNode.getNodeName()))
								{
									attrs = npcNode.getAttributes();
									minions.add(new MinionHolder(parseInteger(attrs, "id"), parseInteger(attrs, "count"), parseInteger(attrs, "respawnTime"), 0));
								}
							}
							_tempMinions.put(id, minions);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Gets the single instance of NpcData.
	 * @return single instance of NpcData
	 */
	public static NpcData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final NpcData _instance = new NpcData();
	}
}
