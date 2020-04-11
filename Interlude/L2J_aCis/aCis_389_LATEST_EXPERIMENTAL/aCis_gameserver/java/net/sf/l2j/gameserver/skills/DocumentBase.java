package net.sf.l2j.gameserver.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.l2j.commons.util.StatsSet;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.enums.items.ArmorType;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.enums.skills.AbnormalEffect;
import net.sf.l2j.gameserver.enums.skills.PlayerState;
import net.sf.l2j.gameserver.enums.skills.SkillType;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.skills.ChanceCondition;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.skills.basefuncs.FuncTemplate;
import net.sf.l2j.gameserver.skills.conditions.Condition;
import net.sf.l2j.gameserver.skills.conditions.ConditionElementSeed;
import net.sf.l2j.gameserver.skills.conditions.ConditionForceBuff;
import net.sf.l2j.gameserver.skills.conditions.ConditionGameTime;
import net.sf.l2j.gameserver.skills.conditions.ConditionLogicAnd;
import net.sf.l2j.gameserver.skills.conditions.ConditionLogicNot;
import net.sf.l2j.gameserver.skills.conditions.ConditionLogicOr;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerActiveEffectId;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerActiveSkillId;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerCharges;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerHasCastle;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerHasClanHall;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerHp;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerHpPercentage;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerInvSize;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerIsHero;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerLevel;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerMp;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerPkCount;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerPledgeClass;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerRace;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerSex;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerState;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerWeight;
import net.sf.l2j.gameserver.skills.conditions.ConditionSkillStats;
import net.sf.l2j.gameserver.skills.conditions.ConditionTargetActiveSkillId;
import net.sf.l2j.gameserver.skills.conditions.ConditionTargetHpMinMax;
import net.sf.l2j.gameserver.skills.conditions.ConditionTargetNpcId;
import net.sf.l2j.gameserver.skills.conditions.ConditionTargetRaceId;
import net.sf.l2j.gameserver.skills.conditions.ConditionUsingItemType;
import net.sf.l2j.gameserver.skills.effects.EffectChanceSkillTrigger;
import net.sf.l2j.gameserver.skills.effects.EffectTemplate;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

abstract class DocumentBase
{
	static Logger _log = Logger.getLogger(DocumentBase.class.getName());
	
	private final File _file;
	protected Map<String, String[]> _tables;
	
	DocumentBase(File pFile)
	{
		_file = pFile;
		_tables = new HashMap<>();
	}
	
	public Document parse()
	{
		Document doc = null;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			doc = factory.newDocumentBuilder().parse(_file);
			
			parseDocument(doc);
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error loading file " + _file, e);
		}
		return doc;
	}
	
	protected abstract void parseDocument(Document doc);
	
	protected abstract StatsSet getStatsSet();
	
	protected abstract String getTableValue(String name);
	
	protected abstract String getTableValue(String name, int idx);
	
	protected void resetTable()
	{
		_tables = new HashMap<>();
	}
	
	protected void setTable(String name, String[] table)
	{
		_tables.put(name, table);
	}
	
	protected void parseTemplate(Node n, Object template)
	{
		n = n.getFirstChild();
		if (n == null)
			return;
		
		Condition condition = null;
		if ("cond".equalsIgnoreCase(n.getNodeName()))
		{
			condition = parseCondition(n.getFirstChild(), template);
			Node msg = n.getAttributes().getNamedItem("msg");
			Node msgId = n.getAttributes().getNamedItem("msgId");
			if (condition != null && msg != null)
				condition.setMessage(msg.getNodeValue());
			else if (condition != null && msgId != null)
			{
				condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
				Node addName = n.getAttributes().getNamedItem("addName");
				if (addName != null && Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)
					condition.addName();
			}
			n = n.getNextSibling();
		}
		
		for (; n != null; n = n.getNextSibling())
		{
			if ("add".equalsIgnoreCase(n.getNodeName()))
				attachFunc(n, template, "Add", condition);
			else if ("addMul".equalsIgnoreCase(n.getNodeName()))
				attachFunc(n, template, "AddMul", condition);
			else if ("sub".equalsIgnoreCase(n.getNodeName()))
				attachFunc(n, template, "Sub", condition);
			else if ("subDiv".equalsIgnoreCase(n.getNodeName()))
				attachFunc(n, template, "SubDiv", condition);
			else if ("mul".equalsIgnoreCase(n.getNodeName()))
				attachFunc(n, template, "Mul", condition);
			else if ("basemul".equalsIgnoreCase(n.getNodeName()))
				attachFunc(n, template, "BaseMul", condition);
			else if ("div".equalsIgnoreCase(n.getNodeName()))
				attachFunc(n, template, "Div", condition);
			else if ("set".equalsIgnoreCase(n.getNodeName()))
				attachFunc(n, template, "Set", condition);
			else if ("enchant".equalsIgnoreCase(n.getNodeName()))
				attachFunc(n, template, "Enchant", condition);
			else if ("baseadd".equalsIgnoreCase(n.getNodeName()))
				attachFunc(n, template, "BaseAdd", condition);
			else if ("effect".equalsIgnoreCase(n.getNodeName()))
			{
				if (template instanceof EffectTemplate)
					throw new RuntimeException("Nested effects");
				
				attachEffect(n, template, condition);
			}
		}
	}
	
	protected void attachFunc(Node n, Object template, String function, Condition attachCond)
	{
		Stats stat = Stats.valueOfXml(n.getAttributes().getNamedItem("stat").getNodeValue());
		
		String valueString = n.getAttributes().getNamedItem("val").getNodeValue();
		double value;
		if (valueString.charAt(0) == '#')
			value = Double.parseDouble(getTableValue(valueString));
		else
			value = Double.parseDouble(valueString);
		
		final Condition applyCond = parseCondition(n.getFirstChild(), template);
		final FuncTemplate ft = new FuncTemplate(attachCond, applyCond, function, stat, value);
		if (template instanceof Item)
			((Item) template).attach(ft);
		else if (template instanceof L2Skill)
			((L2Skill) template).attach(ft);
		else if (template instanceof EffectTemplate)
			((EffectTemplate) template).attach(ft);
		else
			throw new RuntimeException("Attaching stat to a non-effect template!!!");
	}
	
	protected void attachEffect(Node n, Object template, Condition attachCond)
	{
		NamedNodeMap attrs = n.getAttributes();
		String name = getValue(attrs.getNamedItem("name").getNodeValue().intern(), template);
		
		// Keep this values as default ones, DP needs it
		int time = 1;
		int count = 1;
		
		if (attrs.getNamedItem("count") != null)
			count = Integer.decode(getValue(attrs.getNamedItem("count").getNodeValue(), template));
		
		if (attrs.getNamedItem("time") != null)
			time = Integer.decode(getValue(attrs.getNamedItem("time").getNodeValue(), template));
		else if (((L2Skill) template).getBuffDuration() > 0)
			time = ((L2Skill) template).getBuffDuration() / 1000 / count;
		
		boolean self = false;
		if (attrs.getNamedItem("self") != null)
		{
			if (Integer.decode(getValue(attrs.getNamedItem("self").getNodeValue(), template)) == 1)
				self = true;
		}
		
		boolean icon = true;
		if (attrs.getNamedItem("noicon") != null)
		{
			if (Integer.decode(getValue(attrs.getNamedItem("noicon").getNodeValue(), template)) == 1)
				icon = false;
		}
		
		String valueString = n.getAttributes().getNamedItem("val").getNodeValue();
		double value;
		if (valueString.charAt(0) == '#')
			value = Double.parseDouble(getTableValue(valueString));
		else
			value = Double.parseDouble(valueString);
		
		AbnormalEffect abnormal = AbnormalEffect.NULL;
		if (attrs.getNamedItem("abnormal") != null)
		{
			String abn = attrs.getNamedItem("abnormal").getNodeValue();
			abnormal = AbnormalEffect.getByName(abn);
		}
		
		String stackType = "none";
		if (attrs.getNamedItem("stackType") != null)
			stackType = attrs.getNamedItem("stackType").getNodeValue();
		
		float stackOrder = 0;
		if (attrs.getNamedItem("stackOrder") != null)
			stackOrder = Float.parseFloat(getValue(attrs.getNamedItem("stackOrder").getNodeValue(), template));
		
		double effectPower = -1;
		if (attrs.getNamedItem("effectPower") != null)
			effectPower = Double.parseDouble(getValue(attrs.getNamedItem("effectPower").getNodeValue(), template));
		
		SkillType type = null;
		if (attrs.getNamedItem("effectType") != null)
		{
			String typeName = getValue(attrs.getNamedItem("effectType").getNodeValue(), template);
			
			try
			{
				type = Enum.valueOf(SkillType.class, typeName);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Not skilltype found for: " + typeName);
			}
		}
		
		EffectTemplate lt;
		
		final boolean isChanceSkillTrigger = (name == EffectChanceSkillTrigger.class.getName());
		int trigId = 0;
		if (attrs.getNamedItem("triggeredId") != null)
			trigId = Integer.parseInt(getValue(attrs.getNamedItem("triggeredId").getNodeValue(), template));
		else if (isChanceSkillTrigger)
			throw new NoSuchElementException(name + " requires triggerId");
		
		int trigLvl = 1;
		if (attrs.getNamedItem("triggeredLevel") != null)
			trigLvl = Integer.parseInt(getValue(attrs.getNamedItem("triggeredLevel").getNodeValue(), template));
		
		String chanceCond = null;
		if (attrs.getNamedItem("chanceType") != null)
			chanceCond = getValue(attrs.getNamedItem("chanceType").getNodeValue(), template);
		else if (isChanceSkillTrigger)
			throw new NoSuchElementException(name + " requires chanceType");
		
		int activationChance = -1;
		if (attrs.getNamedItem("activationChance") != null)
			activationChance = Integer.parseInt(getValue(attrs.getNamedItem("activationChance").getNodeValue(), template));
		
		ChanceCondition chance = ChanceCondition.parse(chanceCond, activationChance);
		
		if (chance == null && isChanceSkillTrigger)
			throw new NoSuchElementException("Invalid chance condition: " + chanceCond + " " + activationChance);
		
		lt = new EffectTemplate(attachCond, name, value, count, time, abnormal, stackType, stackOrder, icon, effectPower, type, trigId, trigLvl, chance);
		
		parseTemplate(n, lt);
		if (template instanceof L2Skill)
		{
			if (self)
				((L2Skill) template).attachSelf(lt);
			else
				((L2Skill) template).attach(lt);
		}
	}
	
	protected Condition parseCondition(Node n, Object template)
	{
		while (n != null && n.getNodeType() != Node.ELEMENT_NODE)
			n = n.getNextSibling();
		
		if (n == null)
			return null;
		
		if ("and".equalsIgnoreCase(n.getNodeName()))
			return parseLogicAnd(n, template);
		
		if ("or".equalsIgnoreCase(n.getNodeName()))
			return parseLogicOr(n, template);
		
		if ("not".equalsIgnoreCase(n.getNodeName()))
			return parseLogicNot(n, template);
		
		if ("player".equalsIgnoreCase(n.getNodeName()))
			return parsePlayerCondition(n, template);
		
		if ("target".equalsIgnoreCase(n.getNodeName()))
			return parseTargetCondition(n, template);
		
		if ("skill".equalsIgnoreCase(n.getNodeName()))
			return parseSkillCondition(n);
		
		if ("using".equalsIgnoreCase(n.getNodeName()))
			return parseUsingCondition(n);
		
		if ("game".equalsIgnoreCase(n.getNodeName()))
			return parseGameCondition(n);
		
		return null;
	}
	
	protected Condition parseLogicAnd(Node n, Object template)
	{
		ConditionLogicAnd cond = new ConditionLogicAnd();
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
			if (n.getNodeType() == Node.ELEMENT_NODE)
				cond.add(parseCondition(n, template));
			
		if (cond.conditions == null || cond.conditions.length == 0)
			_log.severe("Empty <and> condition in " + _file);
		
		return cond;
	}
	
	protected Condition parseLogicOr(Node n, Object template)
	{
		ConditionLogicOr cond = new ConditionLogicOr();
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
			if (n.getNodeType() == Node.ELEMENT_NODE)
				cond.add(parseCondition(n, template));
			
		if (cond.conditions == null || cond.conditions.length == 0)
			_log.severe("Empty <or> condition in " + _file);
		
		return cond;
	}
	
	protected Condition parseLogicNot(Node n, Object template)
	{
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
			if (n.getNodeType() == Node.ELEMENT_NODE)
				return new ConditionLogicNot(parseCondition(n, template));
			
		_log.severe("Empty <not> condition in " + _file);
		return null;
	}
	
	protected Condition parsePlayerCondition(Node n, Object template)
	{
		Condition cond = null;
		int[] ElementSeeds = new int[5];
		byte[] forces = new byte[2];
		NamedNodeMap attrs = n.getAttributes();
		
		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			if ("race".equalsIgnoreCase(a.getNodeName()))
			{
				ClassRace race = ClassRace.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerRace(race));
			}
			else if ("level".equalsIgnoreCase(a.getNodeName()))
			{
				int lvl = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionPlayerLevel(lvl));
			}
			else if ("resting".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.RESTING, val));
			}
			else if ("riding".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.RIDING, val));
			}
			else if ("flying".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.FLYING, val));
			}
			else if ("moving".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.MOVING, val));
			}
			else if ("running".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.RUNNING, val));
			}
			else if ("behind".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.BEHIND, val));
			}
			else if ("front".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.FRONT, val));
			}
			else if ("olympiad".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.OLYMPIAD, val));
			}
			else if ("ishero".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerIsHero(val));
			}
			else if ("hp".equalsIgnoreCase(a.getNodeName()))
			{
				int hp = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerHp(hp));
			}
			else if ("hprate".equalsIgnoreCase(a.getNodeName()))
			{
				double rate = Double.parseDouble(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerHpPercentage(rate));
			}
			else if ("mp".equalsIgnoreCase(a.getNodeName()))
			{
				int hp = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerMp(hp));
			}
			else if ("pkCount".equalsIgnoreCase(a.getNodeName()))
			{
				int expIndex = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionPlayerPkCount(expIndex));
			}
			else if ("battle_force".equalsIgnoreCase(a.getNodeName()))
			{
				forces[0] = Byte.decode(getValue(a.getNodeValue(), null));
			}
			else if ("spell_force".equalsIgnoreCase(a.getNodeName()))
			{
				forces[1] = Byte.decode(getValue(a.getNodeValue(), null));
			}
			else if ("charges".equalsIgnoreCase(a.getNodeName()))
			{
				int value = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionPlayerCharges(value));
			}
			else if ("weight".equalsIgnoreCase(a.getNodeName()))
			{
				int weight = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerWeight(weight));
			}
			else if ("invSize".equalsIgnoreCase(a.getNodeName()))
			{
				int size = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerInvSize(size));
			}
			else if ("pledgeClass".equalsIgnoreCase(a.getNodeName()))
			{
				int pledgeClass = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerPledgeClass(pledgeClass));
			}
			else if ("clanHall".equalsIgnoreCase(a.getNodeName()))
			{
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item, null)));
				}
				cond = joinAnd(cond, new ConditionPlayerHasClanHall(array));
			}
			else if ("castle".equalsIgnoreCase(a.getNodeName()))
			{
				int castle = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerHasCastle(castle));
			}
			else if ("sex".equalsIgnoreCase(a.getNodeName()))
			{
				int sex = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerSex(sex));
			}
			else if ("active_effect_id".equalsIgnoreCase(a.getNodeName()))
			{
				int effect_id = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionPlayerActiveEffectId(effect_id));
			}
			else if ("active_effect_id_lvl".equalsIgnoreCase(a.getNodeName()))
			{
				String val = getValue(a.getNodeValue(), template);
				int effect_id = Integer.decode(getValue(val.split(",")[0], template));
				int effect_lvl = Integer.decode(getValue(val.split(",")[1], template));
				cond = joinAnd(cond, new ConditionPlayerActiveEffectId(effect_id, effect_lvl));
			}
			else if ("active_skill_id".equalsIgnoreCase(a.getNodeName()))
			{
				int skill_id = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id));
			}
			else if ("active_skill_id_lvl".equalsIgnoreCase(a.getNodeName()))
			{
				String val = getValue(a.getNodeValue(), template);
				int skill_id = Integer.decode(getValue(val.split(",")[0], template));
				int skill_lvl = Integer.decode(getValue(val.split(",")[1], template));
				cond = joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id, skill_lvl));
			}
			else if ("seed_fire".equalsIgnoreCase(a.getNodeName()))
			{
				ElementSeeds[0] = Integer.decode(getValue(a.getNodeValue(), null));
			}
			else if ("seed_water".equalsIgnoreCase(a.getNodeName()))
			{
				ElementSeeds[1] = Integer.decode(getValue(a.getNodeValue(), null));
			}
			else if ("seed_wind".equalsIgnoreCase(a.getNodeName()))
			{
				ElementSeeds[2] = Integer.decode(getValue(a.getNodeValue(), null));
			}
			else if ("seed_various".equalsIgnoreCase(a.getNodeName()))
			{
				ElementSeeds[3] = Integer.decode(getValue(a.getNodeValue(), null));
			}
			else if ("seed_any".equalsIgnoreCase(a.getNodeName()))
			{
				ElementSeeds[4] = Integer.decode(getValue(a.getNodeValue(), null));
			}
		}
		
		// Elemental seed condition processing
		for (int elementSeed : ElementSeeds)
		{
			if (elementSeed > 0)
			{
				cond = joinAnd(cond, new ConditionElementSeed(ElementSeeds));
				break;
			}
		}
		
		if (forces[0] + forces[1] > 0)
			cond = joinAnd(cond, new ConditionForceBuff(forces));
		
		if (cond == null)
			_log.severe("Unrecognized <player> condition in " + _file);
		
		return cond;
	}
	
	protected Condition parseTargetCondition(Node n, Object template)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			if ("hp_min_max".equalsIgnoreCase(a.getNodeName()))
			{
				String val = getValue(a.getNodeValue(), template);
				int hpMin = Integer.decode(getValue(val.split(",")[0], template));
				int hpMax = Integer.decode(getValue(val.split(",")[1], template));
				cond = joinAnd(cond, new ConditionTargetHpMinMax(hpMin, hpMax));
			}
			else if ("active_skill_id".equalsIgnoreCase(a.getNodeName()))
			{
				int skill_id = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionTargetActiveSkillId(skill_id));
			}
			else if ("race_id".equalsIgnoreCase(a.getNodeName()))
			{
				List<Integer> array = new ArrayList<>();
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item, null)));
				}
				cond = joinAnd(cond, new ConditionTargetRaceId(array));
			}
			else if ("npcId".equalsIgnoreCase(a.getNodeName()))
			{
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item, null)));
				}
				cond = joinAnd(cond, new ConditionTargetNpcId(array));
			}
		}
		
		if (cond == null)
			_log.severe("Unrecognized <target> condition in " + _file);
		
		return cond;
	}
	
	protected Condition parseSkillCondition(Node n)
	{
		NamedNodeMap attrs = n.getAttributes();
		Stats stat = Stats.valueOfXml(attrs.getNamedItem("stat").getNodeValue());
		return new ConditionSkillStats(stat);
	}
	
	protected Condition parseUsingCondition(Node n)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			if ("kind".equalsIgnoreCase(a.getNodeName()))
			{
				int mask = 0;
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				while (st.hasMoreTokens())
				{
					int old = mask;
					String item = st.nextToken();
					for (WeaponType wt : WeaponType.VALUES)
					{
						if (wt.name().equals(item))
						{
							mask |= wt.mask();
							break;
						}
					}
					
					for (ArmorType at : ArmorType.VALUES)
					{
						if (at.name().equals(item))
						{
							mask |= at.mask();
							break;
						}
					}
					
					if (old == mask)
						_log.info("[parseUsingCondition=\"kind\"] Unknown item type name: " + item);
				}
				cond = joinAnd(cond, new ConditionUsingItemType(mask));
			}
		}
		
		if (cond == null)
			_log.severe("Unrecognized <using> condition in " + _file);
		
		return cond;
	}
	
	protected Condition parseGameCondition(Node n)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			if ("night".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionGameTime(val));
			}
		}
		
		if (cond == null)
			_log.severe("Unrecognized <game> condition in " + _file);
		
		return cond;
	}
	
	protected void parseTable(Node n)
	{
		NamedNodeMap attrs = n.getAttributes();
		String name = attrs.getNamedItem("name").getNodeValue();
		
		if (name.charAt(0) != '#')
			throw new IllegalArgumentException("Table name must start with #");
		
		StringTokenizer data = new StringTokenizer(n.getFirstChild().getNodeValue());
		List<String> array = new ArrayList<>(data.countTokens());
		
		while (data.hasMoreTokens())
			array.add(data.nextToken());
		
		setTable(name, array.toArray(new String[array.size()]));
	}
	
	protected void parseBeanSet(Node n, StatsSet set, Integer level)
	{
		String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
		String value = n.getAttributes().getNamedItem("val").getNodeValue().trim();
		char ch = value.length() == 0 ? ' ' : value.charAt(0);
		
		if (ch == '#' || ch == '-' || Character.isDigit(ch))
			set.set(name, String.valueOf(getValue(value, level)));
		else
			set.set(name, value);
	}
	
	protected String getValue(String value, Object template)
	{
		// is it a table?
		if (value.charAt(0) == '#')
		{
			if (template instanceof L2Skill)
				return getTableValue(value);
			else if (template instanceof Integer)
				return getTableValue(value, ((Integer) template).intValue());
			else
				throw new IllegalStateException();
		}
		return value;
	}
	
	protected Condition joinAnd(Condition cond, Condition c)
	{
		if (cond == null)
			return c;
		
		if (cond instanceof ConditionLogicAnd)
		{
			((ConditionLogicAnd) cond).add(c);
			return cond;
		}
		ConditionLogicAnd and = new ConditionLogicAnd();
		and.add(cond);
		and.add(c);
		return and;
	}
}