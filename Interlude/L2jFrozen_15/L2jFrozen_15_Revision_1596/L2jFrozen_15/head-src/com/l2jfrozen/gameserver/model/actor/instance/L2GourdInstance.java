package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.taskmanager.DecayTaskManager;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

public final class L2GourdInstance extends L2MonsterInstance
{
	// private static Logger LOGGER = Logger.getLogger(L2GourdInstance.class);
	
	private String name;
	private byte nectar = 0;
	private byte good = 0;
	
	public L2GourdInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		DecayTaskManager.getInstance().addDecayTask(this, 180000);
	}
	
	public void setOwner(final String name)
	{
		this.name = name;
	}
	
	public String getOwner()
	{
		return name;
	}
	
	public void addNectar()
	{
		nectar++;
	}
	
	public byte getNectar()
	{
		return nectar;
	}
	
	public void addGood()
	{
		good++;
	}
	
	public byte getGood()
	{
		return good;
	}
	
	@Override
	public void reduceCurrentHp(double damage, final L2Character attacker, final boolean awake)
	{
		if (!attacker.getName().equalsIgnoreCase(getOwner()))
		{
			damage = 0;
		}
		if (getTemplate().npcId == 12778 || getTemplate().npcId == 12779)
		{
			if (attacker.getActiveWeaponInstance().getItemId() == 4202 || attacker.getActiveWeaponInstance().getItemId() == 5133 || attacker.getActiveWeaponInstance().getItemId() == 5817 || attacker.getActiveWeaponInstance().getItemId() == 7058)
			{
				super.reduceCurrentHp(damage, attacker, awake);
			}
			else if (damage > 0)
			{
				damage = 0;
			}
		}
		super.reduceCurrentHp(damage, attacker, awake);
	}
}
