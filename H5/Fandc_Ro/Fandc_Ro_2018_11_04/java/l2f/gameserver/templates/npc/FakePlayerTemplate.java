package l2f.gameserver.templates.npc;

import java.util.HashMap;
import java.util.Map;

import l2f.gameserver.model.Player;
import l2f.gameserver.model.base.Race;
import l2f.gameserver.model.base.TeamType;
import l2f.gameserver.model.items.Inventory;
import l2f.gameserver.model.items.ItemInstance;
import l2f.gameserver.templates.StatsSet;

public class FakePlayerTemplate
{
	private final int templateId;
	private final String name;
	private final String title;
	private final boolean hasPvpFlag;
	private final int karma;
	private final int recommends;
	private final int nameColor;
	private final int titleColor;
	private final Race race;
	private final int sex;
	private final int classId;
	private final int hairStyle;
	private final int hairColor;
	private final int face;
	private final boolean isSitting;
	private final boolean isRunning;
	private final boolean inCombat;
	private final boolean isDead;
	private final boolean isStore;
	private final int weaponEnchant;
	private final boolean weaponGlow;
	private final int abnormal;
	private final int abnormal2;
	private final boolean isHero;
	private final int agathion;
	private final int[] cubics;
	private final TeamType team;
	private final Map<Integer, Integer> inventory;

	public FakePlayerTemplate(int templateId, String name, String title, StatsSet parameters, Map<Integer, Integer> inventory)
	{
		this.templateId = templateId;
		this.name = name;
		this.title = title;
		this.inventory = inventory;
		this.hasPvpFlag = parameters.getBool("hasPvpFlag");
		this.karma = parameters.getInteger("karma");
		this.recommends = parameters.getInteger("recommends");
		this.nameColor = parameters.getInteger("nameColor");
		this.titleColor = parameters.getInteger("titleColor");
		this.race = Race.valueOf(parameters.getString("race"));
		this.sex = parameters.getInteger("sex");
		this.classId = parameters.getInteger("classId");
		this.hairStyle = parameters.getInteger("hairStyle");
		this.hairColor = parameters.getInteger("hairColor");
		this.face = parameters.getInteger("face");
		this.isSitting = parameters.getBool("isSitting");
		this.isRunning = parameters.getBool("isRunning");
		this.inCombat = parameters.getBool("inCombat");
		this.isDead = parameters.getBool("isDead");
		this.isStore = parameters.getBool("isStore");
		this.weaponEnchant = parameters.getInteger("weaponEnchant");
		this.weaponGlow = parameters.getBool("weaponGlow");
		this.abnormal = parameters.getInteger("abnormal");
		this.abnormal2 = parameters.getInteger("abnormal2");
		this.isHero = parameters.getBool("isHero");
		this.agathion = parameters.getInteger("agathion");
		this.cubics = parameters.getIntegerArray("cubics");
		this.team = TeamType.valueOf(parameters.getString("team"));
	}

	public FakePlayerTemplate(int templateId, String name, String title, Player player)
	{
		this.templateId = templateId;
		this.name = name;
		this.title = title;
		this.hasPvpFlag = (player.getPvpFlag() > 0);
		this.karma = player.getKarma();
		this.recommends = player.getRecomHave();
		this.nameColor = player.getNameColor();
		this.titleColor = player.getTitleColor();
		this.race = player.getRace();
		this.sex = player.getSex();
		this.classId = player.getBaseClassId();
		this.hairStyle = player.getHairStyle();
		this.hairColor = player.getHairColor();
		this.face = player.getFace();
		this.isSitting = player.isSitting();
		this.isRunning = player.isRunning();
		this.inCombat = player.isInCombat();
		this.isDead = player.isDead();
		this.isStore = player.isInStoreMode();
		this.weaponEnchant = player.getEnchantEffect();
		this.weaponGlow = false;
		this.abnormal = player.getAbnormalEffect();
		this.abnormal2 = player.getAbnormalEffect2();
		this.isHero = player.isHero();
		this.agathion = 0;
		this.cubics = new int[0];
		this.team = player.getTeam();
		this.inventory = new HashMap<Integer, Integer>(26);
		for (int slot : Inventory.PAPERDOLL_ORDER)
		{
			final ItemInstance item = player.getInventory().getPaperdollItem(slot);
			if (item != null)
			{
				if (item.getVisualItemId() > 0)
				{
					this.inventory.put(slot, item.getVisualItemId());
				}
				else
				{
					this.inventory.put(slot, item.getItemId());
				}
			}
		}
	}

	public int getTemplateId()
	{
		return this.templateId;
	}

	public String getName()
	{
		return this.name;
	}

	public String getTitle()
	{
		return this.title;
	}

	public boolean isHasPvpFlag()
	{
		return this.hasPvpFlag;
	}

	public int getKarma()
	{
		return this.karma;
	}

	public int getRecommends()
	{
		return this.recommends;
	}

	public int getNameColor()
	{
		return this.nameColor;
	}

	public int getTitleColor()
	{
		return this.titleColor;
	}

	public Race getRace()
	{
		return this.race;
	}

	public int getSex()
	{
		return this.sex;
	}

	public int getClassId()
	{
		return this.classId;
	}

	public int getHairStyle()
	{
		return this.hairStyle;
	}

	public int getHairColor()
	{
		return this.hairColor;
	}

	public int getFace()
	{
		return this.face;
	}

	public boolean isSitting()
	{
		return this.isSitting;
	}

	public boolean isRunning()
	{
		return this.isRunning;
	}

	public boolean isInCombat()
	{
		return this.inCombat;
	}

	public boolean isDead()
	{
		return this.isDead;
	}

	public boolean isStore()
	{
		return this.isStore;
	}

	public int getWeaponEnchant()
	{
		return this.weaponEnchant;
	}

	public boolean getWeaponGlow()
	{
		return this.weaponGlow;
	}

	public int getAbnormal()
	{
		return this.abnormal;
	}

	public int getAbnormal2()
	{
		return this.abnormal2;
	}

	public boolean isHero()
	{
		return this.isHero;
	}

	public int getAgathion()
	{
		return this.agathion;
	}

	public int[] getCubics()
	{
		return this.cubics;
	}

	public TeamType getTeam()
	{
		return this.team;
	}

	public Map<Integer, Integer> getInventory()
	{
		return this.inventory;
	}
}
