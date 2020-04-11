package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * TODO Add support for Eval. Score dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSddd rev420 dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddcccddhh rev478
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddcccddhhddd rev551
 * @version $Revision: 1.2.2.2.2.8 $ $Date: 2005/03/27 15:29:39 $
 */
public class GMViewCharacterInfo extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	
	public GMViewCharacterInfo(final L2PcInstance character)
	{
		activeChar = character;
	}
	
	@Override
	protected final void writeImpl()
	{
		final float moveMultiplier = activeChar.getMovementSpeedMultiplier();
		final int runSpd = (int) (activeChar.getRunSpeed() / moveMultiplier);
		final int walkSpd = (int) (activeChar.getWalkSpeed() / moveMultiplier);
		
		writeC(0x8f);
		
		writeD(activeChar.getX());
		writeD(activeChar.getY());
		writeD(activeChar.getZ());
		writeD(activeChar.getHeading());
		writeD(activeChar.getObjectId());
		writeS(activeChar.getName());
		writeD(activeChar.getRace().ordinal());
		writeD(activeChar.getAppearance().getSex() ? 1 : 0);
		writeD(activeChar.getClassId().getId());
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
		writeD(0x28); // unknown
		
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
		
		// c6 new h's
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
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		// end of c6 new h's
		
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
		writeD(runSpd); // swimspeed
		writeD(walkSpd); // swimspeed
		writeD(runSpd);
		writeD(walkSpd);
		writeD(runSpd);
		writeD(walkSpd);
		writeF(moveMultiplier);
		writeF(activeChar.getAttackSpeedMultiplier()); // 2.9);//
		writeF(activeChar.getTemplate().collisionRadius); // scale
		writeF(activeChar.getTemplate().collisionHeight); // y offset ??!? fem dwarf 4033
		writeD(activeChar.getAppearance().getHairStyle());
		writeD(activeChar.getAppearance().getHairColor());
		writeD(activeChar.getAppearance().getFace());
		writeD(activeChar.isGM() ? 0x01 : 0x00); // builder level
		
		writeS(activeChar.getTitle());
		writeD(activeChar.getClanId()); // pledge id
		writeD(activeChar.getClanCrestId()); // pledge crest id
		writeD(activeChar.getAllyId()); // ally id
		writeC(activeChar.getMountType()); // mount type
		writeC(activeChar.getPrivateStoreType());
		writeC(activeChar.hasDwarvenCraft() ? 1 : 0);
		writeD(activeChar.getPkKills());
		writeD(activeChar.getPvpKills());
		
		writeH(activeChar.getRecomLeft());
		writeH(activeChar.getRecomHave()); // Blue value for name (0 = white, 255 = pure blue)
		writeD(activeChar.getClassId().getId());
		writeD(0x00); // special effects? circles around player...
		writeD(activeChar.getMaxCp());
		writeD((int) activeChar.getCurrentCp());
		
		writeC(activeChar.isRunning() ? 0x01 : 0x00); // changes the Speed display on Status Window
		
		writeC(321);
		
		writeD(activeChar.getPledgeClass()); // changes the text above CP on Status Window
		
		writeC(activeChar.isNoble() ? 0x01 : 0x00);
		writeC(activeChar.isHero() ? 0x01 : 0x00);
		
		writeD(activeChar.getAppearance().getNameColor());
		writeD(activeChar.getAppearance().getTitleColor());
	}
	
	@Override
	public String getType()
	{
		return "[S] 8F GMViewCharacterInfo";
	}
}
