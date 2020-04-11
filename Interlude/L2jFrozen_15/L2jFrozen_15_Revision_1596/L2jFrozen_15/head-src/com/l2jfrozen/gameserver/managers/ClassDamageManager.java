package com.l2jfrozen.gameserver.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.FService;
import com.l2jfrozen.gameserver.datatables.sql.CharTemplateTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Shyla
 */
public class ClassDamageManager
{
	private static final Logger LOGGER = Logger.getLogger(ClassDamageManager.class);
	
	private static Hashtable<Integer, Double> damage_to_mage = new Hashtable<>();
	private static Hashtable<Integer, Double> damage_to_fighter = new Hashtable<>();
	private static Hashtable<Integer, Double> damage_by_mage = new Hashtable<>();
	private static Hashtable<Integer, Double> damage_by_fighter = new Hashtable<>();
	
	private static Hashtable<Integer, String> id_to_name = new Hashtable<>();
	private static Hashtable<String, Integer> name_to_id = new Hashtable<>();
	
	public static void loadConfig()
	{
		final String SCRIPT = FService.CLASS_DAMAGES_FILE;
		InputStream is = null;
		File file = null;
		try
		{
			final Properties scriptSetting = new Properties();
			file = new File(SCRIPT);
			is = new FileInputStream(file);
			scriptSetting.load(is);
			
			final Set<Object> key_set = scriptSetting.keySet();
			
			for (final Object key : key_set)
			{
				
				final String key_string = (String) key;
				
				final String[] class_and_type = key_string.split("__");
				
				String class_name = class_and_type[0].replace("_", " ");
				
				if (class_name.equals("Eva s Saint"))
				{
					class_name = "Eva's Saint";
				}
				
				final String type = class_and_type[1];
				
				final Integer class_id = CharTemplateTable.getClassIdByName(class_name) - 1;
				
				id_to_name.put(class_id, class_name);
				name_to_id.put(class_name, class_id);
				
				if (type.equals("ToFighter"))
				{
					damage_to_fighter.put(class_id, Double.parseDouble(scriptSetting.getProperty(key_string)));
				}
				else if (type.equals("ToMage"))
				{
					damage_to_mage.put(class_id, Double.parseDouble(scriptSetting.getProperty(key_string)));
				}
				else if (type.equals("ByFighter"))
				{
					damage_by_fighter.put(class_id, Double.parseDouble(scriptSetting.getProperty(key_string)));
				}
				else if (type.equals("ByMage"))
				{
					damage_by_mage.put(class_id, Double.parseDouble(scriptSetting.getProperty(key_string)));
				}
				
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			
		}
		finally
		{
			
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public static double getClassDamageToMage(final int id)
	{
		
		final Double multiplier = damage_to_mage.get(id);
		
		if (multiplier != null)
		{
			return multiplier;
		}
		return 1;
	}
	
	public static double getClassDamageToFighter(final int id)
	{
		final Double multiplier = damage_to_fighter.get(id);
		if (multiplier != null)
		{
			return multiplier;
		}
		return 1;
	}
	
	public static double getClassDamageByMage(final int id)
	{
		final Double multiplier = damage_by_mage.get(id);
		if (multiplier != null)
		{
			return multiplier;
		}
		return 1;
	}
	
	public static double getClassDamageByFighter(final int id)
	{
		final Double multiplier = damage_by_fighter.get(id);
		if (multiplier != null)
		{
			return multiplier;
		}
		return 1;
	}
	
	public static int getIdByName(final String name)
	{
		
		final Integer id = name_to_id.get(name);
		if (id != null)
		{
			return id;
		}
		return 0;
	}
	
	public static String getNameById(final int id)
	{
		
		final String name = id_to_name.get(id);
		if (name != null)
		{
			return name;
		}
		return "";
	}
	
	/**
	 * return the product between the attackerMultiplier and attackedMultiplier configured into the classDamages.properties
	 * @param  attacker
	 * @param  attacked
	 * @return          output = attackerMulti*attackedMulti
	 */
	public static double getDamageMultiplier(final L2PcInstance attacker, final L2PcInstance attacked)
	{
		
		if (attacker == null || attacked == null)
		{
			return 1;
		}
		
		double attackerMulti = 1;
		
		if (attacked.isMageClass())
		{
			attackerMulti = getClassDamageToMage(attacker.getClassId().getId());
		}
		else
		{
			attackerMulti = getClassDamageToFighter(attacker.getClassId().getId());
		}
		
		double attackedMulti = 1;
		
		if (attacker.isMageClass())
		{
			attackedMulti = getClassDamageByMage(attacked.getClassId().getId());
		}
		else
		{
			attackedMulti = getClassDamageByFighter(attacked.getClassId().getId());
		}
		
		final double output = attackerMulti * attackedMulti;
		
		if (Config.ENABLE_CLASS_DAMAGES_LOGGER)
		{
			LOGGER.info("ClassDamageManager -");
			LOGGER.info("ClassDamageManager - Attacker: " + attacker.getName() + " Class: " + getNameById(attacker.getClassId().getId()) + " ClassId: " + attacker.getClassId().getId() + " isMage: " + attacker.isMageClass() + " mult: " + attackerMulti);
			LOGGER.info("ClassDamageManager - Attacked: " + attacked.getName() + " Class: " + getNameById(attacked.getClassId().getId()) + " ClassId: " + attacked.getClassId().getId() + " isMage: " + attacked.isMageClass() + " mult: " + attackedMulti);
			LOGGER.info("ClassDamageManager - FinalMultiplier: " + output);
			LOGGER.info("ClassDamageManager -");
		}
		
		return output;
		
	}
	
}
