package l2r.gameserver.data.xml.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import l2r.util.data.xml.IXmlReader.IXmlReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author vGodFather
 */
public class SkillIconData implements IXmlReader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SkillIconData.class);
	
	private final Map<Integer, String> _skillIcons = new HashMap<>();
	
	protected SkillIconData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_skillIcons.clear();
		parseDatapackFile("data/xml/other/skillIcons.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _skillIcons.size() + " skill icons.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		final Node table = doc.getFirstChild();
		
		NamedNodeMap attrs;
		for (Node n = table.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("skill".equals(n.getNodeName()))
			{
				attrs = n.getAttributes();
				_skillIcons.put(parseInteger(attrs, "id"), parseString(attrs, "icon"));
			}
		}
	}
	
	public boolean hasIcon(int id)
	{
		return _skillIcons.containsKey(id);
	}
	
	public String getIcon(int id)
	{
		return hasIcon(id) ? _skillIcons.get(id) : "";
	}
	
	/**
	 * Gets the single instance of ActionData.
	 * @return single instance of ActionData
	 */
	public static final SkillIconData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillIconData _instance = new SkillIconData();
	}
}
