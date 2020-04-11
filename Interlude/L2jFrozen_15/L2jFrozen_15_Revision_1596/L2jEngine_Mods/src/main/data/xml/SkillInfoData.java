package main.data.xml;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jfrozen.gameserver.util.StringUtil;

import main.data.XmlParser;

/**
 * @author fissban
 */
public class SkillInfoData extends XmlParser
{
	private static final Map<String, String> skills = new HashMap<>();
	
	@Override
	public void load()
	{
		// Duplicate data is prevented if this method is reloaded
		skills.clear();
		
		loadFile("./data/xml/engine/modsSkill.xml");
		StringUtil.printSection("SkillInfoData: Loaded skill info " + skills.size());
	}
	
	@Override
	protected void parseFile()
	{
		int id = 0;
		int level = 0;
		String description = "";
		for (Node n : getNodes("skill"))
		{
			NamedNodeMap attrs = n.getAttributes();
			
			try
			{
				id = parseInt(attrs, "id");
				level = parseInt(attrs, "level");
				description = parseString(attrs, "description");
				skills.put(id + " " + level, description);
			}
			catch (Exception e)
			{
				LOG.severe("Error en: " + id);
			}
			
		}
	}
	
	public static String getDescription(int id, int lvl)
	{
		return skills.get(id + " " + lvl);
	}
	
	public static String getSkillIcon(int id)
	{
		String formato;
		if (id <= 9)
		{
			formato = "000" + id;
		}
		else if ((id > 9) && (id < 100))
		{
			formato = "00" + id;
		}
		else if ((id > 99) && (id < 1000))
		{
			formato = "0" + id;
		}
		else if (id == 1517)
		{
			formato = "1536";
		}
		else if (id == 1518)
		{
			formato = "1537";
		}
		else if (id == 1547)
		{
			formato = "0065";
		}
		else if (id == 2076)
		{
			formato = "0195";
		}
		else if ((id > 4550) && (id < 4555))
		{
			formato = "5739";
		}
		else if ((id > 4698) && (id < 4701))
		{
			formato = "1331";
		}
		else if ((id > 4701) && (id < 4704))
		{
			formato = "1332";
		}
		else if (id == 6049)
		{
			formato = "0094";
		}
		else
		{
			formato = String.valueOf(id);
		}
		return "Icon.skill" + formato;
	}
	
	public static SkillInfoData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillInfoData INSTANCE = new SkillInfoData();
	}
}
