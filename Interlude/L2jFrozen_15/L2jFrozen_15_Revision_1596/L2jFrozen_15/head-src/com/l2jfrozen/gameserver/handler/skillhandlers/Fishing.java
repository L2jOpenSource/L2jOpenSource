package com.l2jfrozen.gameserver.handler.skillhandlers;

import java.util.Arrays;
import java.util.List;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.managers.FishingZoneManager;
import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.zone.type.L2FishingZone;
import com.l2jfrozen.gameserver.model.zone.type.L2WaterZone;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.util.Util;
import com.l2jfrozen.util.random.Rnd;

public class Fishing implements ISkillHandler
{
	// private static Logger LOGGER = Logger.getLogger(SiegeFlag.class);
	// protected SkillType[] skillIds = {SkillType.FISHING};
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.FISHING
	};
	
	private static final List<Integer> FISHING_POLE_IDS = Arrays.asList(6529, // Baby Duck Rod - No grade
		6530, // Albatross Rod - D grade
		6531, // Pelican Rod - C grade
		6532, // KingFisher Rod - B grade
		6533, // Cygnus Pole - A grade
		6534 // Triton Pole - S grade
	);
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance player = (L2PcInstance) activeChar;
		
		if (!Config.ALLOWFISHING)
		{
			player.sendMessage("Fishing system is disabled");
			return;
		}
		
		if (player.isFishing())
		{
			if (player.getFishCombat() != null)
			{
				player.getFishCombat().doDie(false);
			}
			else
			{
				player.EndFishing(false);
			}
			// Cancels fishing
			player.sendPacket(new SystemMessage(SystemMessageId.FISHING_ATTEMPT_CANCELLED));
			return;
		}
		
		L2Weapon weaponItem = player.getActiveWeaponItem();
		if (weaponItem == null || !FISHING_POLE_IDS.contains(weaponItem.getItemId()))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.FISHING_POLE_NOT_EQUIPPED));
			return;
		}
		weaponItem = null;
		
		L2ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (lure == null)
		{
			// Bait not equiped.
			player.sendPacket(new SystemMessage(SystemMessageId.BAIT_ON_HOOK_BEFORE_FISHING));
			return;
		}
		
		player.setLure(lure);
		lure = null;
		L2ItemInstance lure2 = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		
		if (lure2 == null || lure2.getCount() < 1) // Not enough bait.
		{
			player.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_BAIT));
			return;
		}
		if (player.isInBoat())
		{
			// You can't fish while you are on boat
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_ON_BOAT));
			return;
		}
		
		if (player.isInCraftMode() || player.isInStoreMode())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_WHILE_USING_RECIPE_BOOK));
			// if(!player.isGM())
			return;
		}
		/*
		 * If fishing is enabled, here is the code that was striped from startFishing() in L2PcInstance. Decide now where will the hook be cast...
		 */
		final int rnd = Rnd.get(200) + 200;
		final double angle = Util.convertHeadingToDegree(player.getHeading());
		final double radian = Math.toRadians(angle);
		final double sin = Math.sin(radian);
		final double cos = Math.cos(radian);
		final int x1 = (int) (cos * rnd);
		final int y1 = (int) (sin * rnd);
		final int x = player.getX() + x1;
		final int y = player.getY() + y1;
		int z = player.getZ() - 30;
		/*
		 * ...and if the spot is in a fishing zone. If it is, it will then position the hook on the water surface. If not, you have to be GM to proceed past here... in that case, the hook will be positioned using the old Z lookup method.
		 */
		L2FishingZone aimingTo = FishingZoneManager.getInstance().isInsideFishingZone(x, y, z);
		L2WaterZone water = FishingZoneManager.getInstance().isInsideWaterZone(x, y, z);
		if (aimingTo != null && water != null && (GeoData.getInstance().canSeeTarget(player.getX(), player.getY(), player.getZ() + 50, x, y, water.getWaterZ() - 50)))
		{
			z = water.getWaterZ() + 10;
			// player.sendMessage("Hook x,y: " + x + "," + y + " - Water Z,
			// Player Z:" + z + ", " + player.getZ()); //debug line, shows hook
			// landing related coordinates. Uncoment if needed.
		}
		else if (aimingTo != null && water != null && GeoData.getInstance().canSeeTarget(player.getX(), player.getY(), player.getZ() + 50, x, y, water.getWaterZ() - 50))
		{
			z = aimingTo.getWaterZ() + 10;
		}
		else
		{
			// You can't fish here
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_HERE));
			return;
		}
		
		aimingTo = null;
		water = null;
		
		/*
		 * Of course since you can define fishing water volumes of any height, the function needs to be changed to cope with that. Still, this is assuming that fishing zones water surfaces, are always above "sea level".
		 */
		if (player.getZ() <= -3800 || player.getZ() < (z - 32))
		{
			// You can't fish in water
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_UNDER_WATER));
			// if(!player.isGM())
			return;
		}
		// Has enough bait, consume 1 and update inventory. Start fishing
		// follows.
		lure2 = player.getInventory().destroyItem("Consume", player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, player, null);
		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(lure2);
		player.sendPacket(iu);
		iu = null;
		// If everything else checks out, actually cast the hook and start
		// fishing... :P
		player.startFishing(x, y, z);
		
		// player = null;
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
	
}
