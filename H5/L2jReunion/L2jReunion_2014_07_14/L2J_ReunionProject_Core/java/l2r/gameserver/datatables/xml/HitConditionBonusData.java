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

import l2r.Config;
import l2r.gameserver.GameTimeController;
import l2r.gameserver.engines.DocumentParser;
import l2r.gameserver.model.actor.L2Character;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class load, holds and calculates the hit condition bonuses.
 * @author Nik
 */
public final class HitConditionBonusData extends DocumentParser
{
	private int frontBonus = 0;
	private int sideBonus = 0;
	private int backBonus = 0;
	private int highBonus = 0;
	private int lowBonus = 0;
	private int darkBonus = 0;
	private int rainBonus = 0;
	
	/**
	 * Instantiates a new hit condition bonus.
	 */
	protected HitConditionBonusData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("data/xml/stats/hitConditionBonus.xml");
		_log.info(getClass().getSimpleName() + ": Loaded Hit Condition bonuses.");
		if (Config.DEBUG)
		{
			_log.info(getClass().getSimpleName() + ": Front bonus: " + frontBonus);
			_log.info(getClass().getSimpleName() + ": Side bonus: " + sideBonus);
			_log.info(getClass().getSimpleName() + ": Back bonus: " + backBonus);
			_log.info(getClass().getSimpleName() + ": High bonus: " + highBonus);
			_log.info(getClass().getSimpleName() + ": Low bonus: " + lowBonus);
			_log.info(getClass().getSimpleName() + ": Dark bonus: " + darkBonus);
			_log.info(getClass().getSimpleName() + ": Rain bonus: " + rainBonus);
		}
	}
	
	@Override
	protected void parseDocument()
	{
		final Node n = getCurrentDocument().getFirstChild();
		NamedNodeMap attrs;
		for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
		{
			attrs = d.getAttributes();
			switch (d.getNodeName())
			{
				case "front":
					frontBonus = parseInteger(attrs, "val");
					break;
				case "side":
					sideBonus = parseInteger(attrs, "val");
					break;
				case "back":
					backBonus = parseInteger(attrs, "val");
					break;
				case "high":
					highBonus = parseInteger(attrs, "val");
					break;
				case "low":
					lowBonus = parseInteger(attrs, "val");
					break;
				case "dark":
					darkBonus = parseInteger(attrs, "val");
					break;
				case "rain":
					rainBonus = parseInteger(attrs, "val");
					break;
			}
		}
	}
	
	/**
	 * Gets the condition bonus.
	 * @param attacker the attacking character.
	 * @param target the attacked character.
	 * @return the bonus of the attacker against the target.
	 */
	public double getConditionBonus(L2Character attacker, L2Character target)
	{
		double mod = 100;
		// Get high or low bonus
		if ((attacker.getZ() - target.getZ()) > 50)
		{
			mod += highBonus;
		}
		else if ((attacker.getZ() - target.getZ()) < -50)
		{
			mod += lowBonus;
		}
		
		// Get weather bonus
		if (GameTimeController.getInstance().isNight())
		{
			mod += darkBonus;
			// else if () No rain support yet.
			// chance += hitConditionBonus.rainBonus;
		}
		
		// Get side bonus
		if (attacker.isBehindTarget())
		{
			mod += backBonus;
		}
		else if (attacker.isInFrontOfTarget())
		{
			mod += frontBonus;
		}
		else
		{
			mod += sideBonus;
		}
		
		// If (mod / 100) is less than 0, return 0, because we can't lower more than 100%.
		return Math.max(mod / 100, 0);
	}
	
	/**
	 * Gets the single instance of HitConditionBonus.
	 * @return single instance of HitConditionBonus
	 */
	public static HitConditionBonusData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final HitConditionBonusData _instance = new HitConditionBonusData();
	}
}