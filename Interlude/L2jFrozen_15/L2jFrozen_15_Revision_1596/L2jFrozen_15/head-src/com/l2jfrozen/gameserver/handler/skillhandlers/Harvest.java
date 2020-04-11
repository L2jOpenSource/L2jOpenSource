package com.l2jfrozen.gameserver.handler.skillhandlers;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author l3x
 */
public class Harvest implements ISkillHandler
{
	protected static final Logger LOGGER = Logger.getLogger(Harvest.class);
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.HARVEST
	};
	
	private L2PcInstance activeCharHarvest;
	private L2MonsterInstance targetHarvest;
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
		{
			return;
		}
		
		activeCharHarvest = (L2PcInstance) activeChar;
		
		L2Object[] targetList = skill.getTargetList(activeChar);
		
		InventoryUpdate iu = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		
		if (targetList == null)
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.info("Casting harvest");
		}
		
		for (final L2Object aTargetList : targetList)
		{
			if (!(aTargetList instanceof L2MonsterInstance))
			{
				continue;
			}
			
			targetHarvest = (L2MonsterInstance) aTargetList;
			
			if (activeCharHarvest != targetHarvest.getSeeder())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_HARVEST);
				activeCharHarvest.sendPacket(sm);
				sm = null;
				continue;
			}
			
			boolean send = false;
			int total = 0;
			int cropId = 0;
			
			// TODO: check items and amount of items player harvest
			if (targetHarvest.isSeeded())
			{
				if (calcSuccess())
				{
					L2Attackable.RewardItem[] items = targetHarvest.takeHarvest();
					if (items != null && items.length > 0)
					{
						for (final L2Attackable.RewardItem ritem : items)
						{
							cropId = ritem.getItemId(); // always got 1 type of crop as reward
							if (activeCharHarvest.isInParty())
							{
								activeCharHarvest.getParty().distributeItem(activeCharHarvest, ritem, true, targetHarvest);
							}
							else
							{
								L2ItemInstance item = activeCharHarvest.getInventory().addItem("Manor", ritem.getItemId(), ritem.getCount(), activeCharHarvest, targetHarvest);
								if (iu != null)
								{
									iu.addItem(item);
								}
								send = true;
								total += ritem.getCount();
								item = null;
							}
						}
						if (send)
						{
							SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
							smsg.addNumber(total);
							smsg.addItemName(cropId);
							activeCharHarvest.sendPacket(smsg);
							smsg = null;
							
							if (activeCharHarvest.getParty() != null)
							{
								smsg = new SystemMessage(SystemMessageId.S1_HARVESTED_S3_S2S);
								smsg.addString(activeCharHarvest.getName());
								smsg.addNumber(total);
								smsg.addItemName(cropId);
								activeCharHarvest.getParty().broadcastToPartyMembers(activeCharHarvest, smsg);
								smsg = null;
							}
							
							if (iu != null)
							{
								activeCharHarvest.sendPacket(iu);
							}
							else
							{
								activeCharHarvest.sendPacket(new ItemList(activeCharHarvest, false));
							}
						}
					}
					items = null;
				}
				else
				{
					activeCharHarvest.sendPacket(new SystemMessage(SystemMessageId.THE_HARVEST_HAS_FAILED));
				}
			}
			else
			{
				activeCharHarvest.sendPacket(new SystemMessage(SystemMessageId.THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN));
			}
		}
		targetList = null;
		iu = null;
	}
	
	private boolean calcSuccess()
	{
		int basicSuccess = 100;
		final int levelPlayer = activeCharHarvest.getLevel();
		final int levelTarget = targetHarvest.getLevel();
		
		int diff = (levelPlayer - levelTarget);
		if (diff < 0)
		{
			diff = -diff;
		}
		
		// apply penalty, target <=> player levels
		// 5% penalty for each level
		if (diff > 5)
		{
			basicSuccess -= (diff - 5) * 5;
		}
		
		// success rate cant be less than 1%
		if (basicSuccess < 1)
		{
			basicSuccess = 1;
		}
		
		final int rate = Rnd.nextInt(99);
		
		if (rate < basicSuccess)
		{
			return true;
		}
		return false;
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
