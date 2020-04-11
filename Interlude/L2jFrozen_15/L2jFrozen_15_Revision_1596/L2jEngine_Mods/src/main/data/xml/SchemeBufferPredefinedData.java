package main.data.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jfrozen.gameserver.util.StringUtil;

import main.data.XmlParser;
import main.enums.BuffType;
import main.holders.BuffHolder;

/**
 * @author fissban
 */
public class SchemeBufferPredefinedData extends XmlParser
{
	private enum BuffLoad
	{
		GENERAL,
		MAGE,
		WARRIOR,
	}
	
	private BuffLoad type = BuffLoad.GENERAL;
	/** All buffs can use un schemeBufferNpc */
	private final static List<BuffHolder> generalBuffs = new ArrayList<>();
	/** List of buffs preset for warriors */
	private final static List<BuffHolder> warriorBuffs = new ArrayList<>();
	/** List of buffs preset for mages */
	private final static List<BuffHolder> mageBuffs = new ArrayList<>();
	
	@Override
	public void load()
	{
		// Duplicate data is prevented if this method is reloaded
		generalBuffs.clear();
		warriorBuffs.clear();
		mageBuffs.clear();
		
		type = BuffLoad.GENERAL;
		loadFile("./data/xml/engine/scheme_buffer/generalBuffs.xml");
		type = BuffLoad.MAGE;
		loadFile("./data/xml/engine/scheme_buffer/setMageBuffs.xml");
		type = BuffLoad.WARRIOR;
		loadFile("./data/xml/engine/scheme_buffer/setWarriorBuffs.xml");
		
		StringUtil.printSection("SchemeBufferPredefinedData: Loaded buffs " + generalBuffs.size());
		StringUtil.printSection("SchemeBufferPredefinedData: Loaded warrior buffs " + warriorBuffs.size());
		StringUtil.printSection("SchemeBufferPredefinedData: Loaded mage buffs " + mageBuffs.size());
	}
	
	@Override
	protected void parseFile()
	{
		for (Node n : getNodes("buff"))
		{
			NamedNodeMap attrs = n.getAttributes();
			switch (type)
			{
				case GENERAL:
				{
					BuffType type = BuffType.valueOf(parseString(attrs, "type"));
					int id = parseInt(attrs, "id");
					int lvl = parseInt(attrs, "lvl");
					generalBuffs.add(new BuffHolder(type, id, lvl));
					break;
				}
				case MAGE:
				{
					int id = parseInt(attrs, "id");
					int lvl = parseInt(attrs, "lvl");
					mageBuffs.add(new BuffHolder(id, lvl));
					break;
				}
				case WARRIOR:
				{
					int id = parseInt(attrs, "id");
					int lvl = parseInt(attrs, "lvl");
					warriorBuffs.add(new BuffHolder(id, lvl));
					break;
				}
			}
		}
	}
	
	/**
	 * Get all preset mage buffs
	 * @return
	 */
	public static List<BuffHolder> getAllMageBuffs()
	{
		return mageBuffs;
	}
	
	/**
	 * Get all preset warrior buffs
	 * @return
	 */
	public static List<BuffHolder> getAllWarriorBuffs()
	{
		return warriorBuffs;
	}
	
	/**
	 * Get all general buffs
	 * @return
	 */
	public static List<BuffHolder> getAllGeneralBuffs()
	{
		return generalBuffs;
	}
	
	public static SchemeBufferPredefinedData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SchemeBufferPredefinedData INSTANCE = new SchemeBufferPredefinedData();
	}
}
