
package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.csv.SummonItemsData;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2SummonItem;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jfrozen.gameserver.network.serverpackets.PetInfo;
import com.l2jfrozen.gameserver.network.serverpackets.Ride;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

public class SummonItems implements IItemHandler
{
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		
		if (!activeChar.getFloodProtectors().getItemPetSummon().tryPerformAction("summon pet"))
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// if(activeChar._inEventTvT && TvT._started && !Config.TVT_ALLOW_SUMMON)
		if (activeChar.inEventTvT && TvT.isStarted() && !Config.TVT_ALLOW_SUMMON)
		{
			final ActionFailed af = ActionFailed.STATIC_PACKET;
			activeChar.sendPacket(af);
			return;
		}
		
		// if(activeChar._inEventDM && DM._started && !Config.DM_ALLOW_SUMMON)
		if (activeChar.inEventDM && DM.isStarted() && !Config.DM_ALLOW_SUMMON)
		{
			final ActionFailed af = ActionFailed.STATIC_PACKET;
			activeChar.sendPacket(af);
			return;
		}
		
		// if(activeChar._inEventCTF && CTF._started && !Config.CTF_ALLOW_SUMMON)
		if (activeChar.inEventCTF && CTF.isStarted() && !Config.CTF_ALLOW_SUMMON)
		{
			final ActionFailed af = ActionFailed.STATIC_PACKET;
			activeChar.sendPacket(af);
			return;
		}
		
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
			return;
		}
		
		if (activeChar.isParalyzed())
		{
			activeChar.sendMessage("You Cannot Use This While You Are Paralyzed");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.inObserverMode())
		{
			return;
		}
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}
		
		L2SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());
		
		if ((activeChar.getPet() != null || activeChar.isMounted()) && sitem.isPetSummon())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
			return;
		}
		
		// Like L2OFF you can't summon pet in combat
		if (activeChar.isAttackingNow() || activeChar.isInCombat())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT));
			return;
		}
		
		if (activeChar.isCursedWeaponEquiped() && sitem.isPetSummon())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE));
			return;
		}
		
		final int npcID = sitem.getNpcId();
		
		if (npcID == 0)
		{
			return;
		}
		
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcID);
		
		if (npcTemplate == null)
		{
			return;
		}
		
		switch (sitem.getType())
		{
			case 0: // static summons (like christmas tree)
				try
				{
					L2Spawn spawn = new L2Spawn(npcTemplate);
					
					// if(spawn == null)
					// return;
					
					spawn.setId(IdFactory.getInstance().getNextId());
					spawn.setLocx(activeChar.getX());
					spawn.setLocy(activeChar.getY());
					spawn.setLocz(activeChar.getZ());
					L2World.getInstance().storeObject(spawn.spawnOne());
					activeChar.destroyItem("Summon", item.getObjectId(), 1, null, false);
					activeChar.sendMessage("Created " + npcTemplate.name + " at x: " + spawn.getLocx() + " y: " + spawn.getLocy() + " z: " + spawn.getLocz());
					spawn = null;
				}
				catch (final Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
					activeChar.sendMessage("Target is not ingame.");
				}
				
				break;
			case 1: // pet summons
				activeChar.setTarget(activeChar);
				// Skill 2046 used only for animation
				final L2Skill skill = SkillTable.getInstance().getInfo(2046, 1);
				activeChar.useMagic(skill, true, true);
				activeChar.sendPacket(new SystemMessage(SystemMessageId.SUMMON_A_PET));
				ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFinalizer(activeChar, npcTemplate, item), 4800);
				
				break;
			case 2: // wyvern
				if (!activeChar.disarmWeapons())
				{
					return;
				}
				
				final Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, sitem.getNpcId());
				activeChar.sendPacket(mount);
				activeChar.broadcastPacket(mount);
				activeChar.setMountType(mount.getMountType());
				activeChar.setMountObjectID(item.getObjectId());
		}
		
		activeChar = null;
		sitem = null;
		npcTemplate = null;
	}
	
	static class PetSummonFeedWait implements Runnable
	{
		private final L2PcInstance activeChar;
		private final L2PetInstance petSummon;
		
		PetSummonFeedWait(final L2PcInstance activeChar, final L2PetInstance petSummon)
		{
			this.activeChar = activeChar;
			this.petSummon = petSummon;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (petSummon.getCurrentFed() <= 0)
				{
					petSummon.unSummon(activeChar);
				}
				else
				{
					petSummon.startFeed(false);
				}
			}
			catch (final Throwable e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	static class PetSummonFinalizer implements Runnable
	{
		private final L2PcInstance activeChar;
		private final L2ItemInstance item;
		private final L2NpcTemplate npcTemplate;
		
		PetSummonFinalizer(final L2PcInstance activeChar, final L2NpcTemplate npcTemplate, final L2ItemInstance item)
		{
			this.activeChar = activeChar;
			this.npcTemplate = npcTemplate;
			this.item = item;
		}
		
		@Override
		public void run()
		{
			try
			{
				final SkillDat skilldat = activeChar.getCurrentSkill();
				
				if (!activeChar.isCastingNow() || (skilldat != null && skilldat.getSkillId() != 2046))
				{
					return;
				}
				
				activeChar.sendPacket(new MagicSkillLaunched(activeChar, 2046, 1));
				
				// check for summon item validity
				if (item == null || item.getOwnerId() != activeChar.getObjectId() || item.getLocation() != L2ItemInstance.ItemLocation.INVENTORY)
				{
					return;
				}
				
				final L2PetInstance petSummon = L2PetInstance.spawnPet(npcTemplate, activeChar, item);
				
				if (petSummon == null)
				{
					return;
				}
				
				petSummon.setTitle(activeChar.getName());
				
				if (!petSummon.isRespawned())
				{
					petSummon.setCurrentHp(petSummon.getMaxHp());
					petSummon.setCurrentMp(petSummon.getMaxMp());
					petSummon.getStat().setExp(petSummon.getExpForThisLevel());
					petSummon.setCurrentFed(petSummon.getMaxFed());
				}
				
				petSummon.setRunning();
				
				if (!petSummon.isRespawned())
				{
					petSummon.store();
				}
				
				activeChar.setPet(petSummon);
				
				L2World.getInstance().storeObject(petSummon);
				petSummon.spawnMe(activeChar.getX() + 50, activeChar.getY() + 100, activeChar.getZ());
				activeChar.sendPacket(new PetInfo(petSummon));
				petSummon.startFeed(false);
				item.setEnchantLevel(petSummon.getLevel());
				
				if (petSummon.getCurrentFed() <= 0)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFeedWait(activeChar, petSummon), 60000);
				}
				else
				{
					petSummon.startFeed(false);
				}
				
				petSummon.setFollowStatus(true);
				petSummon.setShowSummonAnimation(false);
				petSummon.broadcastStatusUpdate();
			}
			catch (final Throwable e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return SummonItemsData.getInstance().itemIDs();
	}
}