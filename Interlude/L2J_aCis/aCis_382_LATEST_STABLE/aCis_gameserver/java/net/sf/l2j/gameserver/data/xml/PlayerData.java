package net.sf.l2j.gameserver.data.xml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.util.StatsSet;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.template.PlayerTemplate;
import net.sf.l2j.gameserver.model.holder.ItemTemplateHolder;
import net.sf.l2j.gameserver.model.holder.skillnode.GeneralSkillNode;
import net.sf.l2j.gameserver.model.location.Location;

import org.w3c.dom.Document;

/**
 * This class loads and stores {@link PlayerTemplate}s. It also feed their skill trees.
 */
public class PlayerData implements IXmlReader
{
	private final Map<Integer, PlayerTemplate> _templates = new HashMap<>();
	
	protected PlayerData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/classes");
		LOGGER.info("Loaded {} player classes templates.", _templates.size());
		
		// We add parent skills, if existing.
		for (PlayerTemplate template : _templates.values())
		{
			final ClassId parentClassId = template.getClassId().getParent();
			if (parentClassId != null)
				template.getSkills().addAll(_templates.get(parentClassId.getId()).getSkills());
		}
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "class", classNode ->
		{
			final StatsSet set = new StatsSet();
			forEach(classNode, "set", setNode -> set.putAll(parseAttributes(setNode)));
			forEach(classNode, "items", itemsNode ->
			{
				final List<ItemTemplateHolder> items = new ArrayList<>();
				forEach(itemsNode, "item", itemNode -> items.add(new ItemTemplateHolder(parseAttributes(itemNode))));
				set.set("items", items);
			});
			forEach(classNode, "skills", skillsNode ->
			{
				final List<GeneralSkillNode> skills = new ArrayList<>();
				forEach(skillsNode, "skill", skillNode -> skills.add(new GeneralSkillNode(parseAttributes(skillNode))));
				set.set("skills", skills);
			});
			forEach(classNode, "spawns", spawnsNode ->
			{
				final List<Location> locs = new ArrayList<>();
				forEach(spawnsNode, "spawn", spawnNode -> locs.add(new Location(parseAttributes(spawnNode))));
				set.set("spawnLocations", locs);
			});
			_templates.put(set.getInteger("id"), new PlayerTemplate(set));
		}));
	}
	
	public PlayerTemplate getTemplate(ClassId classId)
	{
		return _templates.get(classId.getId());
	}
	
	public PlayerTemplate getTemplate(int classId)
	{
		return _templates.get(classId);
	}
	
	public final String getClassNameById(int classId)
	{
		final PlayerTemplate template = _templates.get(classId);
		return (template != null) ? template.getClassName() : "Invalid class";
	}
	
	public static PlayerData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final PlayerData INSTANCE = new PlayerData();
	}
}