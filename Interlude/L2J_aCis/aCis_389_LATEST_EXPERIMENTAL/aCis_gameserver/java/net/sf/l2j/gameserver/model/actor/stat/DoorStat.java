package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.enums.SealType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.skills.L2Skill;

public class DoorStat extends CreatureStat
{
	private int _upgradeHpRatio = 1;
	
	public DoorStat(Door door)
	{
		super(door);
	}
	
	@Override
	public Door getActor()
	{
		return (Door) super.getActor();
	}
	
	@Override
	public int getMDef(Creature target, L2Skill skill)
	{
		double defense = getActor().getTemplate().getBaseMDef();
		
		switch (SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE))
		{
			case DAWN:
				defense *= 1.2;
				break;
			
			case DUSK:
				defense *= 0.3;
				break;
		}
		
		return (int) defense;
	}
	
	@Override
	public int getPDef(Creature target)
	{
		double defense = getActor().getTemplate().getBasePDef();
		
		switch (SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE))
		{
			case DAWN:
				defense *= 1.2;
				break;
			
			case DUSK:
				defense *= 0.3;
				break;
		}
		
		return (int) defense;
	}
	
	@Override
	public int getMaxHp()
	{
		return super.getMaxHp() * _upgradeHpRatio;
	}
	
	public final void setUpgradeHpRatio(int hpRatio)
	{
		_upgradeHpRatio = hpRatio;
	}
	
	public final int getUpgradeHpRatio()
	{
		return _upgradeHpRatio;
	}
}