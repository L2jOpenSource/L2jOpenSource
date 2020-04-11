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

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.engines.DocumentParser;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.options.Options;
import l2r.gameserver.model.options.OptionsSkillHolder;
import l2r.gameserver.model.options.OptionsSkillType;
import l2r.gameserver.model.skills.funcs.FuncTemplate;
import l2r.gameserver.model.skills.funcs.LambdaConst;
import l2r.gameserver.model.stats.Stats;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author UnAfraid
 */
public class OptionsData extends DocumentParser
{
	private final Map<Integer, Options> _data = new HashMap<>();
	
	protected OptionsData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		parseDirectory("data/xml/stats/options");
		_log.info(getClass().getSimpleName() + ": Loaded: " + _data.size() + " Options.");
	}
	
	@Override
	protected void parseDocument()
	{
		int id;
		Options op;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("option".equalsIgnoreCase(d.getNodeName()))
					{
						id = parseInteger(d.getAttributes(), "id");
						op = new Options(id);
						
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							switch (cd.getNodeName())
							{
								case "for":
								{
									for (Node fd = cd.getFirstChild(); fd != null; fd = fd.getNextSibling())
									{
										switch (fd.getNodeName())
										{
											case "add":
											{
												parseFuncs(fd.getAttributes(), "Add", op);
												break;
											}
											case "mul":
											{
												parseFuncs(fd.getAttributes(), "Mul", op);
												break;
											}
											case "basemul":
											{
												parseFuncs(fd.getAttributes(), "BaseMul", op);
												break;
											}
											case "sub":
											{
												parseFuncs(fd.getAttributes(), "Sub", op);
												break;
											}
											case "div":
											{
												parseFuncs(fd.getAttributes(), "Div", op);
												break;
											}
											case "set":
											{
												parseFuncs(fd.getAttributes(), "Set", op);
												break;
											}
										}
									}
									break;
								}
								case "active_skill":
								{
									op.setActiveSkill(new SkillHolder(parseInteger(cd.getAttributes(), "id"), parseInteger(cd.getAttributes(), "level")));
									break;
								}
								case "passive_skill":
								{
									op.setPassiveSkill(new SkillHolder(parseInteger(cd.getAttributes(), "id"), parseInteger(cd.getAttributes(), "level")));
									break;
								}
								case "attack_skill":
								{
									op.addActivationSkill(new OptionsSkillHolder(parseInteger(cd.getAttributes(), "id"), parseInteger(cd.getAttributes(), "level"), parseDouble(cd.getAttributes(), "chance"), OptionsSkillType.ATTACK));
									break;
								}
								case "magic_skill":
								{
									op.addActivationSkill(new OptionsSkillHolder(parseInteger(cd.getAttributes(), "id"), parseInteger(cd.getAttributes(), "level"), parseDouble(cd.getAttributes(), "chance"), OptionsSkillType.MAGIC));
									break;
								}
								case "critical_skill":
								{
									op.addActivationSkill(new OptionsSkillHolder(parseInteger(cd.getAttributes(), "id"), parseInteger(cd.getAttributes(), "level"), parseDouble(cd.getAttributes(), "chance"), OptionsSkillType.CRITICAL));
									break;
								}
							}
						}
						_data.put(op.getId(), op);
					}
				}
			}
		}
	}
	
	private void parseFuncs(NamedNodeMap attrs, String func, Options op)
	{
		Stats stat = Stats.valueOfXml(parseString(attrs, "stat"));
		int ord = Integer.decode(parseString(attrs, "order"));
		double val = parseDouble(attrs, "val");
		op.addFunc(new FuncTemplate(null, null, func, stat, ord, new LambdaConst(val)));
	}
	
	public Options getOptions(int id)
	{
		return _data.get(id);
	}
	
	/**
	 * Gets the single instance of OptionsData.
	 * @return single instance of OptionsData
	 */
	public static final OptionsData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final OptionsData _instance = new OptionsData();
	}
}
