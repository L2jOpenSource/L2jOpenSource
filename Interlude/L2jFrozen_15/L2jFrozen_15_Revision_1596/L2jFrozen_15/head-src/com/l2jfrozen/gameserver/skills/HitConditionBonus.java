package com.l2jfrozen.gameserver.skills;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.model.L2Character;

/**
 * @author Nik
 */
public class HitConditionBonus
{
	protected static final Logger LOGGER = Logger.getLogger(HitConditionBonus.class);
	
	private static int frontBonus = 0;
	private static int sideBonus = 0;
	private static int backBonus = 0;
	private static int highBonus = 0;
	private static int lowBonus = 0;
	private static int darkBonus = 0;
	
	public HitConditionBonus()
	{
		loadData();
	}
	
	public static double getConditionBonus(final L2Character attacker, final L2Character target)
	{
		double mod = 100;
		
		// Get high or low bonus
		if (attacker.getZ() - target.getZ() > 50)
		{
			mod += HitConditionBonus.highBonus;
		}
		else if (attacker.getZ() - target.getZ() < -50)
		{
			mod += HitConditionBonus.lowBonus;
		}
		
		// Get weather bonus
		if (GameTimeController.getInstance().isNowNight())
		{
			mod += HitConditionBonus.darkBonus;
		}
		
		// Get side bonus
		if (attacker.isBehindTarget())
		{
			mod += HitConditionBonus.backBonus;
		}
		else if (attacker.isFrontTarget())
		{
			mod += HitConditionBonus.frontBonus;
		}
		else
		{
			mod += HitConditionBonus.sideBonus;
		}
		
		// If (mod / 10) is less than 0, return 0, because we cant lower more than 100%.
		return Math.max(mod / 100, 0);
	}
	
	public void loadData()
	{
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		factory.setIgnoringComments(true);
		final File file = new File(Config.DATAPACK_ROOT, "data/xml/hitConditionBonus.xml");
		Document doc = null;
		
		if (file.exists())
		{
			try
			{
				doc = factory.newDocumentBuilder().parse(file);
			}
			catch (Exception e)
			{
				LOGGER.warn("[HitConditionBonus] Could not parse file hitConditionBonus. ", e);
				return;
			}
			
			String name;
			for (Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
			{
				if ("hitConditionBonus".equalsIgnoreCase(list.getNodeName()) || "list".equalsIgnoreCase(list.getNodeName()))
				{
					for (Node cond = list.getFirstChild(); cond != null; cond = cond.getNextSibling())
					{
						int bonus = 0;
						name = cond.getNodeName();
						try
						{
							if (cond.hasAttributes())
							{
								bonus = Integer.parseInt(cond.getAttributes().getNamedItem("val").getNodeValue());
							}
						}
						catch (final Exception e)
						{
							LOGGER.warn("[HitConditionBonus] Could not parse condition: " + e.getMessage(), e);
						}
						finally
						{
							switch (name)
							{
								case "front":
									frontBonus = bonus;
									break;
								case "side":
									sideBonus = bonus;
									break;
								case "back":
									backBonus = bonus;
									break;
								case "high":
									highBonus = bonus;
									break;
								case "low":
									lowBonus = bonus;
									break;
								case "dark":
									darkBonus = bonus;
									break;
							}
						}
					}
				}
			}
		}
		else
		{
			LOGGER.error("[HitConditionBonus] File not found " + file.getName());
		}
	}
	
	public static HitConditionBonus getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		protected static final HitConditionBonus instance = new HitConditionBonus();
	}
}