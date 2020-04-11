package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.data.xml.PlayerLevelData;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.PetDataEntry;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.L2Skill;

public class PetStat extends SummonStat
{
	public PetStat(Pet pet)
	{
		super(pet);
	}
	
	public boolean addExp(int value)
	{
		if (!super.addExp(value))
			return false;
		
		getActor().updateAndBroadcastStatus(1);
		return true;
	}
	
	@Override
	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		if (!super.addExpAndSp(addToExp, addToSp))
			return false;
		
		getActor().getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_EARNED_S1_EXP).addNumber((int) addToExp));
		return true;
	}
	
	@Override
	public final boolean addLevel(byte value)
	{
		if (getLevel() + value > PlayerLevelData.getInstance().getRealMaxLevel())
			return false;
		
		boolean levelIncreased = super.addLevel(value);
		if (levelIncreased)
			getActor().broadcastPacket(new SocialAction(getActor(), 15));
		
		return levelIncreased;
	}
	
	@Override
	public Pet getActor()
	{
		return (Pet) super.getActor();
	}
	
	@Override
	public void setLevel(byte value)
	{
		getActor().setPetData(value);
		
		super.setLevel(value); // Set level.
		
		// If a control item exists and its level is different of the new level.
		final ItemInstance controlItem = getActor().getControlItem();
		if (controlItem != null && controlItem.getEnchantLevel() != getLevel())
		{
			getActor().sendPetInfosToOwner();
			
			controlItem.setEnchantLevel(getLevel());
			
			// Update item
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(controlItem);
			getActor().getOwner().sendPacket(iu);
		}
	}
	
	@Override
	public int getMaxHp()
	{
		return (int) calcStat(Stats.MAX_HP, getActor().getPetData().getMaxHp(), null, null);
	}
	
	@Override
	public int getMaxMp()
	{
		return (int) calcStat(Stats.MAX_MP, getActor().getPetData().getMaxMp(), null, null);
	}
	
	@Override
	public int getMAtk(Creature target, L2Skill skill)
	{
		return (int) calcStat(Stats.MAGIC_ATTACK, getActor().getPetData().getMAtk(), target, skill);
	}
	
	@Override
	public int getMAtkSpd()
	{
		double base = 333;
		
		if (getActor().checkHungryState())
			base /= 2;
		
		return (int) calcStat(Stats.MAGIC_ATTACK_SPEED, base, null, null);
	}
	
	@Override
	public int getMDef(Creature target, L2Skill skill)
	{
		return (int) calcStat(Stats.MAGIC_DEFENCE, getActor().getPetData().getMDef(), target, skill);
	}
	
	@Override
	public int getPAtk(Creature target)
	{
		return (int) calcStat(Stats.POWER_ATTACK, getActor().getPetData().getPAtk(), target, null);
	}
	
	@Override
	public int getPAtkSpd()
	{
		double base = getActor().getTemplate().getBasePAtkSpd();
		
		if (getActor().checkHungryState())
			base /= 2;
		
		return (int) calcStat(Stats.POWER_ATTACK_SPEED, base, null, null);
	}
	
	@Override
	public int getPDef(Creature target)
	{
		return (int) calcStat(Stats.POWER_DEFENCE, getActor().getPetData().getPDef(), target, null);
	}
	
	@Override
	public long getExpForLevel(int level)
	{
		final PetDataEntry pde = getActor().getTemplate().getPetDataEntry(level);
		if (pde == null)
			return 0;
		
		return pde.getMaxExp();
	}
	
	@Override
	public long getExpForThisLevel()
	{
		final PetDataEntry pde = getActor().getTemplate().getPetDataEntry(getLevel());
		if (pde == null)
			return 0;
		
		return pde.getMaxExp();
	}
	
	@Override
	public long getExpForNextLevel()
	{
		final PetDataEntry pde = getActor().getTemplate().getPetDataEntry(getLevel() + 1);
		if (pde == null)
			return 0;
		
		return pde.getMaxExp();
	}
}