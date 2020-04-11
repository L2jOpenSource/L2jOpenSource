package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.managers.CursedWeaponsManager;
import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

import main.data.memory.ObjectData;
import main.holders.objects.PlayerHolder;

/**
 * Send a Server->Client packet CharInfo to all L2PcInstance in knownPlayers of the L2PcInstance (Public data only)<BR>
 * ___________________<BR>
 * 0000: 03 32 15 00 00 44 fe 00 00 80 f1 ff ff 00 00 00 .2...D..........
 * <p>
 * 0010: 00 6b b4 c0 4a 45 00 6c 00 6c 00 61 00 6d 00 69 .k..JE.l.l.a.m.i
 * <p>
 * 0020: 00 00 00 01 00 00 00 01 00 00 00 12 00 00 00 00 ................
 * <p>
 * 0030: 00 00 00 2a 00 00 00 42 00 00 00 71 02 00 00 31 ...*...B...q...1
 * <p>
 * 0040: 00 00 00 18 00 00 00 1f 00 00 00 25 00 00 00 00 ...........%....
 * <p>
 * 0050: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 f9 ................
 * <p>
 * 0060: 00 00 00 b3 01 00 00 00 00 00 00 00 00 00 00 7d ...............}
 * <p>
 * 0070: 00 00 00 5a 00 00 00 32 00 00 00 32 00 00 00 00 ...Z...2...2....
 * <p>
 * 0080: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 67 ...............g
 * <p>
 * 0090: 66 66 66 66 66 f2 3f 5f 63 97 a8 de 1a f9 3f 00 fffff.?_c.....?.
 * <p>
 * 00a0: 00 00 00 00 00 1e 40 00 00 00 00 00 00 37 40 01 .............7..
 * <p>
 * 00b0: 00 00 00 01 00 00 00 01 00 00 00 00 00 c1 0c 00 ................
 * <p>
 * 00c0: 00 00 00 00 00 00 00 00 00 01 01 00 00 00 00 00 ................
 * <p>
 * 00d0: 00 00
 * <p>
 * <p>
 * dddddSdddddddddddddddddddddddddddffffdddSdddccccccc (h)
 * <p>
 * dddddSdddddddddddddddddddddddddddffffdddSdddddccccccch dddddSddddddddddddddddddddddddddddffffdddSdddddccccccch (h) c (dchd) ddc dcc c cddd d dddddSdddddddddddddddhhhhhhhhhhhhhhhhhhhhhhhhddddddddddddddffffdddSdddddccccccch [h] c (ddhd) ddc c ddc cddd d d dd d d d
 * @version $Revision: 1.7.2.6.2.11 $ $Date: 2005/04/11 10:05:54 $
 */
public class CharInfo extends L2GameServerPacket
{
	private static final Logger LOGGER = Logger.getLogger(CharInfo.class);
	private final L2PcInstance activeChar;
	private final Inventory inv;
	private final int x, y, z;
	private final int mAtkSpd, pAtkSpd;
	private final int runSpd, walkSpd, swimRunSpd, swimWalkSpd;
	private int flRunSpd;
	private int flWalkSpd;
	private int flyRunSpd;
	private int flyWalkSpd;
	private final float moveMultiplier, attackSpeedMultiplier;
	private final int maxCp;
	
	/**
	 * @param cha
	 */
	public CharInfo(final L2PcInstance cha)
	{
		activeChar = cha;
		inv = cha.getInventory();
		x = activeChar.getX();
		y = activeChar.getY();
		z = activeChar.getZ();
		mAtkSpd = activeChar.getMAtkSpd();
		pAtkSpd = activeChar.getPAtkSpd();
		moveMultiplier = activeChar.getMovementSpeedMultiplier();
		attackSpeedMultiplier = activeChar.getAttackSpeedMultiplier();
		runSpd = (int) (activeChar.getRunSpeed() / moveMultiplier);
		walkSpd = (int) (activeChar.getWalkSpeed() / moveMultiplier);
		swimRunSpd = flRunSpd = flyRunSpd = runSpd;
		swimWalkSpd = flWalkSpd = flyWalkSpd = walkSpd;
		maxCp = activeChar.getMaxCp();
	}
	
	@Override
	protected final void writeImpl()
	{
		boolean receiver_is_gm = false;
		
		final L2PcInstance tmp = getClient().getActiveChar();
		if (tmp != null && tmp.isGM())
		{
			receiver_is_gm = true;
		}
		
		if (!receiver_is_gm && activeChar.getAppearance().isInvisible())
		{
			return;
		}
		
		if (activeChar.getPoly().isMorphed())
		{
			final L2NpcTemplate template = NpcTable.getInstance().getTemplate(activeChar.getPoly().getPolyId());
			
			if (template != null)
			{
				writeC(0x16);
				writeD(activeChar.getObjectId());
				writeD(activeChar.getPoly().getPolyId() + 1000000); // npctype id
				writeD(activeChar.getKarma() > 0 ? 1 : 0);
				writeD(x);
				writeD(y);
				writeD(z);
				writeD(activeChar.getHeading());
				writeD(0x00);
				writeD(mAtkSpd);
				writeD(pAtkSpd);
				writeD(runSpd);
				writeD(walkSpd);
				writeD(swimRunSpd/* 0x32 */); // swimspeed
				writeD(swimWalkSpd/* 0x32 */); // swimspeed
				writeD(flRunSpd);
				writeD(flWalkSpd);
				writeD(flyRunSpd);
				writeD(flyWalkSpd);
				writeF(moveMultiplier);
				writeF(attackSpeedMultiplier);
				writeF(template.collisionRadius);
				writeF(template.collisionHeight);
				writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND)); // right hand weapon
				writeD(0);
				writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND)); // left hand weapon
				writeC(1); // name above char 1=true ... ??
				writeC(activeChar.isRunning() ? 1 : 0);
				writeC(activeChar.isInCombat() ? 1 : 0);
				writeC(activeChar.isAlikeDead() ? 1 : 0);
				
				writeC(0); // if the charinfo is written means receiver can see the char
				
				writeS(activeChar.getName());
				
				if (activeChar.getAppearance().isInvisible())
				// if(gmSeeInvis)
				{
					writeS("Invisible");
				}
				else
				{
					writeS(activeChar.getTitle());
				}
				
				writeD(0);
				writeD(0);
				writeD(0000); // hmm karma ??
				
				if (activeChar.getAppearance().isInvisible())
				// if(gmSeeInvis)
				{
					writeD(activeChar.getAbnormalEffect() | L2Character.ABNORMAL_EFFECT_STEALTH);
				}
				else
				{
					writeD(activeChar.getAbnormalEffect()); // C2
				}
				
				writeD(0); // C2
				writeD(0); // C2
				writeD(0); // C2
				writeD(0); // C2
				writeC(0); // C2
			}
			else
			{
				LOGGER.warn("Character " + activeChar.getName() + " (" + activeChar.getObjectId() + ") morphed in a Npc (" + activeChar.getPoly().getPolyId() + ") w/o template.");
			}
		}
		else
		{
			writeC(0x03);
			writeD(x);
			writeD(y);
			writeD(z);
			writeD(activeChar.getHeading()); // Why this is send it again later in line 382 of this file?
			writeD(activeChar.getObjectId());
			writeS(activeChar.getName());
			writeD(activeChar.getRace().ordinal());
			writeD(activeChar.getAppearance().getSex() ? 1 : 0);
			
			if (activeChar.getClassIndex() == 0)
			{
				writeD(activeChar.getClassId().getId());
			}
			else
			{
				writeD(activeChar.getBaseClass());
			}
			
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_DHAIR));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_FACE));
			
			// c6 new h's
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LRHAND));
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			
			writeD(activeChar.getPvpFlag());
			writeD(activeChar.getKarma());
			
			writeD(mAtkSpd);
			writeD(pAtkSpd);
			
			writeD(activeChar.getPvpFlag());
			writeD(activeChar.getKarma());
			
			writeD(runSpd);
			writeD(walkSpd);
			writeD(swimRunSpd/* 0x32 */); // swimspeed
			writeD(swimWalkSpd/* 0x32 */); // swimspeed
			writeD(flRunSpd);
			writeD(flWalkSpd);
			writeD(flyRunSpd);
			writeD(flyWalkSpd);
			writeF(activeChar.getMovementSpeedMultiplier()); // activeChar.getProperMultiplier()
			writeF(activeChar.getAttackSpeedMultiplier()); // activeChar.getAttackSpeedMultiplier()
			writeF(activeChar.getBaseTemplate().collisionRadius);
			writeF(activeChar.getBaseTemplate().collisionHeight);
			
			writeD(activeChar.getAppearance().getHairStyle());
			writeD(activeChar.getAppearance().getHairColor());
			writeD(activeChar.getAppearance().getFace());
			
			if (activeChar.getAppearance().isInvisible())
			// if(gmSeeInvis)
			{
				writeS("Invisible");
			}
			else
			{
				writeS(activeChar.getTitle());
			}
			
			writeD(activeChar.getClanId());
			writeD(activeChar.getClanCrestId());
			writeD(activeChar.getAllyId());
			writeD(activeChar.getAllyCrestId());
			// In UserInfo leader rights and siege flags, but here found nothing??
			// Therefore RelationChanged packet with that info is required
			writeD(0);
			
			writeC(activeChar.isSitting() ? 0 : 1); // standing = 1 sitting = 0
			writeC(activeChar.isRunning() ? 1 : 0); // running = 1 walking = 0
			writeC(activeChar.isInCombat() ? 1 : 0);
			writeC(activeChar.isAlikeDead() ? 1 : 0);
			
			writeC(0); // if the charinfo is written means receiver can see the char
			
			writeC(activeChar.getMountType()); // 1 on strider 2 on wyvern 0 no mount
			
			//writeC(activeChar.getPrivateStoreType()); // 1 - sellshop
			writeC(ObjectData.get(PlayerHolder.class, activeChar).isSellBuff() ? 1 : activeChar.getPrivateStoreType());
			
			final Map<Integer, L2CubicInstance> cubics = activeChar.getCubics();
			
			final Set<Integer> cubicsIds = cubics.keySet();
			
			writeH(cubicsIds.size());
			for (final Integer id : cubicsIds)
			{
				if (id != null)
				{
					writeH(id);
				}
			}
			
			writeC(activeChar.isInPartyMatchRoom() ? 1 : 0);
			// writeC(0x00); // find party members
			
			if (activeChar.getAppearance().isInvisible())
			// if(gmSeeInvis)
			{
				writeD(activeChar.getAbnormalEffect() | L2Character.ABNORMAL_EFFECT_STEALTH);
			}
			else
			{
				writeD(activeChar.getAbnormalEffect());
			}
			
			writeC(activeChar.getRecomLeft()); // Changed by Thorgrim
			writeH(activeChar.getRecomHave()); // Blue value for name (0 = white, 255 = pure blue)
			writeD(activeChar.getClassId().getId());
			
			writeD(maxCp);
			writeD((int) activeChar.getCurrentCp());
			writeC(activeChar.isMounted() ? 0 : activeChar.getEnchantEffect());
			
			writeC(ObjectData.get(PlayerHolder.class, activeChar).getTeam().ordinal());
			//if (activeChar.getTeam() == 1)
			//{
			//	writeC(0x01); // team circle around feet 1= Blue, 2 = red
			//}
			//else if (activeChar.getTeam() == 2)
			//{
			//	writeC(0x02); // team circle around feet 1= Blue, 2 = red
			//}
			//else
			//{
			//	writeC(0x00); // team circle around feet 1= Blue, 2 = red
			//}
			
			writeD(activeChar.getClanCrestLargeId());
			writeC(activeChar.isNoble() ? 1 : 0); // Symbol on char menu ctrl+I
			writeC(activeChar.isHero() || activeChar.isGM() && Config.GM_HERO_AURA || activeChar.getIsPVPHero() ? 1 : 0); // Hero Aura
			
			writeC(activeChar.isFishing() ? 1 : 0); // 0x01: Fishing Mode (Cant be undone by setting back to 0)
			writeD(activeChar.getFishx());
			writeD(activeChar.getFishy());
			writeD(activeChar.getFishz());
			
			writeD(activeChar.getAppearance().getNameColor());
			
			writeD(activeChar.getHeading());
			
			writeD(activeChar.getPledgeClass());
			writeD(0x00); // ??
			
			writeD(activeChar.getAppearance().getTitleColor());
			
			// writeD(0x00); // ??
			
			if (activeChar.isCursedWeaponEquiped())
			{
				writeD(CursedWeaponsManager.getInstance().getLevel(activeChar.getCursedWeaponEquipedId()));
			}
			else
			{
				writeD(0x00);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 03 CharInfo";
	}
}
