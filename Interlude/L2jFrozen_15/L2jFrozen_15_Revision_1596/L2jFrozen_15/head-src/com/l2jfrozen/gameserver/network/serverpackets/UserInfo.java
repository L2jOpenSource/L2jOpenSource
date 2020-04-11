package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.Map;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.managers.CursedWeaponsManager;
import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

import main.data.memory.ObjectData;
import main.holders.objects.PlayerHolder;

/**
 * Send a Server->Client packet UserInfo to this L2PcInstance (Public and Private Data)<BR>
 * ______________________<BR>
 * 0000: 04 03 15 00 00 77 ff 00 00 80 f1 ff ff 00 00 00 .....w.......... 0010: 00 2a 89 00 4c 43 00 61 00 6c 00 61 00 64 00 6f .*..LC.a.l.a.d.o 0020: 00 6e 00 00 00 01 00 00 00 00 00 00 00 19 00 00 .n.............. 0030: 00 0d 00 00 00 ee 81 02 00 15 00 00 00 18 00 00 ................ 0040: 00 19
 * 00 00 00 25 00 00 00 17 00 00 00 28 00 00 .....%.......(.. 0050: 00 14 01 00 00 14 01 00 00 02 01 00 00 02 01 00 ................ 0060: 00 fa 09 00 00 81 06 00 00 26 34 00 00 2e 00 00 .........&4..... 0070: 00 00 00 00 00 db 9f a1 41 93 26 64 41 de c8 31 ........A.&dA..1 0080: 41 ca 73 c0 41 d5
 * 22 d0 41 83 bd 41 41 81 56 10 A.s.A.".A..AA.V. 0090: 41 00 00 00 00 27 7d 30 41 69 aa e0 40 b4 fb d3 A....'}0Ai..@... 00a0: 41 91 f9 63 41 00 00 00 00 81 56 10 41 00 00 00 A..cA.....V.A... 00b0: 00 71 00 00 00 71 00 00 00 76 00 00 00 74 00 00 .q...q...v...t.. 00c0: 00 74 00 00 00 2a 00 00 00 e8
 * 02 00 00 00 00 00 .t...*.......... 00d0: 00 5f 04 00 00 ac 01 00 00 cf 01 00 00 62 04 00 ._...........b.. 00e0: 00 00 00 00 00 e8 02 00 00 0b 00 00 00 52 01 00 .............R.. 00f0: 00 4d 00 00 00 2a 00 00 00 2f 00 00 00 29 00 00 .M...*.../...).. 0100: 00 12 00 00 00 82 01 00 00 52 01 00 00 53
 * 00 00 .........R...S.. 0110: 00 00 00 00 00 00 00 00 00 7a 00 00 00 55 00 00 .........z...U.. 0120: 00 32 00 00 00 32 00 00 00 00 00 00 00 00 00 00 .2...2.......... 0130: 00 00 00 00 00 00 00 00 00 a4 70 3d 0a d7 a3 f0 ..........p=.... 0140: 3f 64 5d dc 46 03 78 f3 3f 00 00 00 00 00 00 1e
 * ?d].F.x.?....... 0150: 40 00 00 00 00 00 00 38 40 02 00 00 00 01 00 00 @......8@....... 0160: 00 00 00 00 00 00 00 00 00 00 00 c1 0c 00 00 01 ................ 0170: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ................ 0180: 00 00 00 00 ....
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddcccdd (h) dddddSddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd ffffddddSdddddcccddh (h) c dc hhdh
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddddcccddh (h) c dc hhdh ddddc c dcc cddd d (from 654) but it actually reads dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddddcccddh (h) c dc *dddddddd* hhdh ddddc dcc
 * cddd d *...*: here i am not sure at least it looks like it reads that much data (32 bytes), not sure about the format inside because it is not read thanks to the ususal
 * parsingfunctiondddddSddddQddddddddddddddddddddddddddddddddddddddddddddddddhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhddddddddddddddddddddffffddddSdddddcccddh [h] c dc d hhdh ddddc c dcc cddd d c dd d d
 * @version $Revision: 1.14.2.4.2.12 $ $Date: 2005/04/11 10:05:55 $
 */
public class UserInfo extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	private final int runSpd, walkSpd, swimRunSpd, swimWalkSpd;
	private int flRunSpd;
	private int flWalkSpd;
	private int flyRunSpd;
	private int flyWalkSpd;
	private int relation;
	private final float moveMultiplier;
	
	public UserInfo(final L2PcInstance character)
	{
		activeChar = character;
		moveMultiplier = activeChar.getMovementSpeedMultiplier();
		runSpd = (int) (activeChar.getRunSpeed() / moveMultiplier);
		walkSpd = (int) (activeChar.getWalkSpeed() / moveMultiplier);
		swimRunSpd = flRunSpd = flyRunSpd = runSpd;
		swimWalkSpd = flWalkSpd = flyWalkSpd = walkSpd;
		relation = activeChar.isClanLeader() ? 0x40 : 0;
		
		if (activeChar.getSiegeState() == 1)
		{
			relation |= 0x180;
		}
		
		if (activeChar.getSiegeState() == 2)
		{
			relation |= 0x80;
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x04);
		writeD(activeChar.getX());
		writeD(activeChar.getY());
		writeD(activeChar.getZ());
		writeD(activeChar.getHeading());
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
		
		writeD(activeChar.getLevel());
		writeQ(activeChar.getExp());
		writeD(activeChar.getSTR());
		writeD(activeChar.getDEX());
		writeD(activeChar.getCON());
		writeD(activeChar.getINT());
		writeD(activeChar.getWIT());
		writeD(activeChar.getMEN());
		writeD(activeChar.getMaxHp());
		writeD((int) activeChar.getCurrentHp());
		writeD(activeChar.getMaxMp());
		writeD((int) activeChar.getCurrentMp());
		writeD(activeChar.getSp());
		writeD(activeChar.getCurrentLoad());
		writeD(activeChar.getMaxLoad());
		
		writeD(activeChar.getActiveWeaponItem() != null ? 40 : 20); // 20 no weapon, 40 weapon equippe
		
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DHAIR));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FACE));
		
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DHAIR));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_REAR));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_NECK));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FACE));
		
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
		writeH(0x00);
		writeH(0x00);
		writeD(activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
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
		writeD(activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_LRHAND));
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		
		writeD(activeChar.getPAtk(null));
		writeD(activeChar.getPAtkSpd());
		writeD(activeChar.getPDef(null));
		writeD(activeChar.getEvasionRate(null));
		writeD(activeChar.getAccuracy());
		writeD(activeChar.getCriticalHit(null, null));
		writeD(activeChar.getMAtk(null, null));
		
		writeD(activeChar.getMAtkSpd());
		writeD(activeChar.getPAtkSpd());
		
		writeD(activeChar.getMDef(null, null));
		
		writeD(activeChar.getPvpFlag()); // 0-non-pvp 1-pvp = violett name
		writeD(activeChar.getKarma());
		
		writeD(runSpd);
		writeD(walkSpd);
		writeD(swimRunSpd); // swimspeed
		writeD(swimWalkSpd); // swimspeed
		writeD(flRunSpd);
		writeD(flWalkSpd);
		writeD(flyRunSpd);
		writeD(flyWalkSpd);
		writeF(moveMultiplier);
		writeF(activeChar.getAttackSpeedMultiplier());
		
		final L2Summon pet = activeChar.getPet();
		if (activeChar.getMountType() != 0 && pet != null)
		{
			writeF(pet.getTemplate().collisionRadius);
			writeF(pet.getTemplate().collisionHeight);
		}
		else
		{
			writeF(activeChar.getBaseTemplate().collisionRadius);
			writeF(activeChar.getBaseTemplate().collisionHeight);
		}
		
		writeD(activeChar.getAppearance().getHairStyle());
		writeD(activeChar.getAppearance().getHairColor());
		writeD(activeChar.getAppearance().getFace());
		writeD(activeChar.isGM() ? 1 : 0); // builder level
		
		String title = activeChar.getTitle();
		if (activeChar.getAppearance().isInvisible() && activeChar.isGM())
		{
			title = "Invisible";
		}
		if (activeChar.getPoly().isMorphed())
		{
			final L2NpcTemplate polyObj = NpcTable.getInstance().getTemplate(activeChar.getPoly().getPolyId());
			if (polyObj != null)
			{
				title += " - " + polyObj.name;
			}
		}
		writeS(title);
		
		writeD(activeChar.getClanId());
		writeD(activeChar.getClanCrestId());
		writeD(activeChar.getAllyId());
		writeD(activeChar.getAllyCrestId()); // ally crest id
		// 0x40 leader rights
		// siege flags: attacker - 0x180 sword over name, defender - 0x80 shield, 0xC0 crown (|leader), 0x1C0 flag (|leader)
		writeD(relation);
		writeC(activeChar.getMountType()); // mount type
		writeC(ObjectData.get(PlayerHolder.class, activeChar).isSellBuff() ? 1 : activeChar.getPrivateStoreType());
		//writeC(activeChar.getPrivateStoreType());
		writeC(activeChar.hasDwarvenCraft() ? 1 : 0);
		writeD(activeChar.getPkKills());
		writeD(activeChar.getPvpKills());
		
		final Map<Integer, L2CubicInstance> cubics = activeChar.getCubics();
		writeH(cubics.size());
		for (final Integer id : cubics.keySet())
		{
			writeH(id);
		}
		
		writeC(activeChar.isInPartyMatchRoom() ? 1 : 0);
		
		writeD(activeChar.getAbnormalEffect());
		writeC(0x00); // unk
		
		writeD(activeChar.getClanPrivileges());
		
		writeH(activeChar.getRecomLeft()); // c2 recommendations remaining
		writeH(activeChar.getRecomHave()); // c2 recommendations received
		writeD(0x00); // FIXME: MOUNT NPC ID
		writeH(activeChar.getInventoryLimit());
		
		writeD(activeChar.getClassId().getId());
		writeD(0x00); // FIXME: special effects? circles around player...
		writeD(activeChar.getMaxCp());
		writeD((int) activeChar.getCurrentCp());
		writeC(activeChar.isMounted() ? 0 : activeChar.getEnchantEffect());
		
		if (activeChar.getTeam() == 1)
		{
			writeC(0x01); // team circle around feet 1= Blue, 2 = red
		}
		else if (activeChar.getTeam() == 2)
		{
			writeC(0x02); // team circle around feet 1= Blue, 2 = red
		}
		else
		{
			writeC(0x00); // team circle around feet 1= Blue, 2 = red
		}
		
		writeD(activeChar.getClanCrestLargeId());
		writeC(activeChar.isNoble() ? 1 : 0); // 0x01: symbol on char menu ctrl+I
		writeC(activeChar.isHero() || activeChar.isGM() && Config.GM_HERO_AURA || activeChar.getIsPVPHero() ? 1 : 0); // 0x01: Hero Aura
		
		writeC(activeChar.isFishing() ? 1 : 0); // Fishing Mode
		writeD(activeChar.getFishx()); // fishing x
		writeD(activeChar.getFishy()); // fishing y
		writeD(activeChar.getFishz()); // fishing z
		writeD(activeChar.getAppearance().getNameColor());
		
		writeC(activeChar.isRunning() ? 0x01 : 0x00); // changes the Speed display on Status Window
		
		writeD(activeChar.getPledgeClass()); // changes the text above CP on Status Window
		writeD(activeChar.getPledgeType()); // TODO: PLEDGE TYPE
		
		writeD(activeChar.getAppearance().getTitleColor());
		
		if (activeChar.isCursedWeaponEquiped())
		{
			writeD(CursedWeaponsManager.getInstance().getLevel(activeChar.getCursedWeaponEquipedId()));
		}
		else
		{
			writeD(0x00);
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 04 UserInfo";
	}
}
