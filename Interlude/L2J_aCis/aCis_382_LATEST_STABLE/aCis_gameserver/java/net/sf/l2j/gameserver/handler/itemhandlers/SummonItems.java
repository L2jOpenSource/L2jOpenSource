package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.data.xml.SummonItemData;
import net.sf.l2j.gameserver.enums.GaugeColor;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.ChristmasTree;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class SummonItems implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final Player player = (Player) playable;
		
		if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return;
		}
		
		if (player.isInObserverMode())
			return;
		
		if (player.isAllSkillsDisabled() || player.isCastingNow())
			return;
		
		final IntIntHolder sitem = SummonItemData.getInstance().getSummonItem(item.getItemId());
		
		if ((player.getSummon() != null || player.isMounted()) && sitem.getValue() > 0)
		{
			player.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
			return;
		}
		
		if (player.isAttackingNow())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT);
			return;
		}
		
		final int npcId = sitem.getId();
		if (npcId == 0)
			return;
		
		final NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(npcId);
		if (npcTemplate == null)
			return;
		
		player.stopMove(null);
		
		switch (sitem.getValue())
		{
			case 0: // static summons (like Christmas tree)
				try
				{
					for (ChristmasTree ch : player.getKnownTypeInRadius(ChristmasTree.class, 1200))
					{
						if (npcTemplate.getNpcId() == ChristmasTree.SPECIAL_TREE_ID)
						{
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_SUMMON_S1_AGAIN).addCharName(ch));
							return;
						}
					}
					
					if (player.destroyItem("Summon", item.getObjectId(), 1, null, false))
					{
						final L2Spawn spawn = new L2Spawn(npcTemplate);
						spawn.setLoc(player.getPosition());
						spawn.setRespawnState(false);
						
						final Npc npc = spawn.doSpawn(true);
						npc.setTitle(player.getName());
						npc.setIsRunning(false); // broadcast info
					}
				}
				catch (Exception e)
				{
					player.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
				}
				break;
			case 1: // pet summons
				final WorldObject oldTarget = player.getTarget();
				player.setTarget(player);
				player.broadcastPacket(new MagicSkillUse(player, 2046, 1, 5000, 0));
				player.setTarget(oldTarget);
				player.sendPacket(new SetupGauge(GaugeColor.BLUE, 5000));
				player.sendPacket(SystemMessageId.SUMMON_A_PET);
				player.setIsCastingNow(true);
				
				ThreadPool.schedule(new PetSummonFinalizer(player, npcTemplate, item), 5000);
				break;
			case 2: // wyvern
				player.mount(sitem.getId(), item.getObjectId());
				break;
		}
	}
	
	// TODO: this should be inside skill handler
	static class PetSummonFinalizer implements Runnable
	{
		private final Player _player;
		private final ItemInstance _item;
		private final NpcTemplate _template;
		
		PetSummonFinalizer(Player player, NpcTemplate template, ItemInstance item)
		{
			_player = player;
			_template = template;
			_item = item;
		}
		
		@Override
		public void run()
		{
			_player.sendPacket(new MagicSkillLaunched(_player, 2046, 1));
			_player.setIsCastingNow(false);
			
			// check for summon item validity
			if (_item == null || _item.getOwnerId() != _player.getObjectId() || _item.getLocation() != ItemInstance.ItemLocation.INVENTORY)
				return;
			
			// Owner has a pet listed in world.
			if (World.getInstance().getPet(_player.getObjectId()) != null)
				return;
			
			// Add the pet instance to world.
			final Pet pet = Pet.restore(_item, _template, _player);
			if (pet == null)
				return;
			
			World.getInstance().addPet(_player.getObjectId(), pet);
			
			_player.setSummon(pet);
			
			pet.setRunning();
			pet.setTitle(_player.getName());
			pet.spawnMe();
			pet.startFeed();
			pet.setFollowStatus(true);
		}
	}
}
