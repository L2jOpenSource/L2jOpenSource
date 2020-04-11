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
package l2r.gameserver.datatables.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2r.gameserver.engines.DocumentParser;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.templates.L2PcTemplate;
import l2r.gameserver.model.base.ClassId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This will be reworked Soon(tm).
 * @author Unknown, Forsaiken, Zoey76, GKR
 */
public final class CharTemplateData extends DocumentParser
{
	private static final Logger _log = LoggerFactory.getLogger(CharTemplateData.class.getName());
	
	private static final Map<ClassId, L2PcTemplate> _charTemplates = new HashMap<>();
	
	private int _dataCount = 0;
	
	protected CharTemplateData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_charTemplates.clear();
		parseDirectory("data/xml/stats/chars/baseStats", false);
		_log.info(getClass().getSimpleName() + ": Loaded " + _charTemplates.size() + " character templates.");
		_log.info(getClass().getSimpleName() + ": Loaded " + _dataCount + " level up gain records.");
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		int classId = 0;
		
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("classId".equalsIgnoreCase(d.getNodeName()))
					{
						classId = Integer.parseInt(d.getTextContent());
					}
					else if ("staticData".equalsIgnoreCase(d.getNodeName()))
					{
						StatsSet set = new StatsSet();
						set.set("classId", classId);
						List<Location> creationPoints = new ArrayList<>();
						
						for (Node nd = d.getFirstChild(); nd != null; nd = nd.getNextSibling())
						{
							// Skip odd nodes
							if (nd.getNodeName().equals("#text"))
							{
								continue;
							}
							
							if (nd.getChildNodes().getLength() > 1)
							{
								for (Node cnd = nd.getFirstChild(); cnd != null; cnd = cnd.getNextSibling())
								{
									// use L2CharTemplate(superclass) fields for male collision height and collision radius
									if (nd.getNodeName().equalsIgnoreCase("collisionMale"))
									{
										if (cnd.getNodeName().equalsIgnoreCase("radius"))
										{
											set.set("collision_radius", cnd.getTextContent());
										}
										else if (cnd.getNodeName().equalsIgnoreCase("height"))
										{
											set.set("collision_height", cnd.getTextContent());
										}
									}
									if ("node".equalsIgnoreCase(cnd.getNodeName()))
									{
										attrs = cnd.getAttributes();
										creationPoints.add(new Location(parseInteger(attrs, "x"), parseInteger(attrs, "y"), parseInteger(attrs, "z")));
									}
									else if ("walk".equalsIgnoreCase(cnd.getNodeName()))
									{
										set.set("baseWalkSpd", cnd.getTextContent());
									}
									else if ("run".equalsIgnoreCase(cnd.getNodeName()))
									{
										set.set("baseRunSpd", cnd.getTextContent());
									}
									else if ("slowSwim".equals(cnd.getNodeName()))
									{
										set.set("baseSwimWalkSpd", cnd.getTextContent());
									}
									else if ("fastSwim".equals(cnd.getNodeName()))
									{
										set.set("baseSwimRunSpd", cnd.getTextContent());
									}
									else if (!cnd.getNodeName().equals("#text"))
									{
										set.set((nd.getNodeName() + cnd.getNodeName()), cnd.getTextContent());
									}
								}
							}
							else
							{
								set.set(nd.getNodeName(), nd.getTextContent());
							}
						}
						// calculate total pdef and mdef from parts
						set.set("basePDef", (set.getInteger("basePDefchest", 0) + set.getInteger("basePDeflegs", 0) + set.getInteger("basePDefhead", 0) + set.getInteger("basePDeffeet", 0) + set.getInteger("basePDefgloves", 0) + set.getInteger("basePDefunderwear", 0) + set.getInteger("basePDefcloak", 0)));
						set.set("baseMDef", (set.getInteger("baseMDefrear", 0) + set.getInteger("baseMDeflear", 0) + set.getInteger("baseMDefrfinger", 0) + set.getInteger("baseMDefrfinger", 0) + set.getInteger("baseMDefneck", 0)));
						
						final L2PcTemplate ct = new L2PcTemplate(set, creationPoints);
						_charTemplates.put(ClassId.getClassId(classId), ct);
					}
					else if ("lvlUpgainData".equalsIgnoreCase(d.getNodeName()))
					{
						for (Node lvlNode = d.getFirstChild(); lvlNode != null; lvlNode = lvlNode.getNextSibling())
						{
							if ("level".equalsIgnoreCase(lvlNode.getNodeName()))
							{
								attrs = lvlNode.getAttributes();
								int level = parseInteger(attrs, "val");
								
								for (Node valNode = lvlNode.getFirstChild(); valNode != null; valNode = valNode.getNextSibling())
								{
									String nodeName = valNode.getNodeName();
									
									if ((nodeName.startsWith("hp") || nodeName.startsWith("mp") || nodeName.startsWith("cp")) && _charTemplates.containsKey(ClassId.getClassId(classId)))
									{
										_charTemplates.get(ClassId.getClassId(classId)).setUpgainValue(nodeName, level, Double.parseDouble(valNode.getTextContent()));
										_dataCount++;
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public L2PcTemplate getTemplate(final ClassId classId)
	{
		return _charTemplates.get(classId);
	}
	
	public L2PcTemplate getTemplate(final int classId)
	{
		return _charTemplates.get(ClassId.getClassId(classId));
	}
	
	public static final CharTemplateData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CharTemplateData _instance = new CharTemplateData();
	}
}
