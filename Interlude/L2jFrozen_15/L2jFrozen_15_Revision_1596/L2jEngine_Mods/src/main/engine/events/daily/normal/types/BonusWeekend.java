package main.engine.events.daily.normal.types;

import main.data.properties.ConfigData;
import main.engine.events.daily.AbstractEvent;
import main.enums.ExpSpType;
import main.enums.ItemDropType;
import main.holders.objects.NpcHolder;
import main.holders.objects.PlayerHolder;
import main.instances.NpcDropsInstance;
import main.instances.NpcExpInstance;

/**
 * @author fissban
 */
public class BonusWeekend extends AbstractEvent
{
	public BonusWeekend()
	{
		registerEvent(ConfigData.ENABLE_BonusWeekend, ConfigData.BONUS_WEEKEND_ENABLE_DAY);
	}
	
	@Override
	public void onModState()
	{
		//
	}
	
	@Override
	public void onNpcDrop(PlayerHolder killer, NpcHolder npc, NpcDropsInstance instance)
	{
		// DropBonusHolder (dropType, amountBonus, chanceBonus)
		// Example: 110 -> 110%
		// if you use 100% drop will be normal, to earn bonus use values greater than 100%.
		
		// increase normal drop amount and chance
		instance.increaseDrop(ItemDropType.NORMAL, ConfigData.BONUS_WEEKEND_DROP, ConfigData.BONUS_WEEKEND_DROP);
		// increase spoil drop amount and chance
		instance.increaseDrop(ItemDropType.SPOIL, ConfigData.BONUS_WEEKEND_SPOIL, ConfigData.BONUS_WEEKEND_SPOIL);
		// increase seed drop amount and chance
		instance.increaseDrop(ItemDropType.SEED, ConfigData.BONUS_WEEKEND_SEED, ConfigData.BONUS_WEEKEND_SEED);
	}
	
	@Override
	public void onNpcExpSp(PlayerHolder killer, NpcHolder npc, NpcExpInstance instance)
	{
		// ExpSpBonusHolder (bonusType, amountBonus)
		// Example: 1.1 -> 110%
		// if you use 100% exp will be normal, to earn bonus use values greater than 100%.
		// increase normal exp/sp amount
		instance.increaseRate(ExpSpType.EXP, ConfigData.BONUS_WEEKEND_RATE_EXP);
		instance.increaseRate(ExpSpType.SP, ConfigData.BONUS_WEEKEND_RATE_SP);
	}
}
