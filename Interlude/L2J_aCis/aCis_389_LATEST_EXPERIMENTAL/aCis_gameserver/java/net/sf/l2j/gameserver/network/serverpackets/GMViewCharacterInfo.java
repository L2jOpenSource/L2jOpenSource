package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;

public class GMViewCharacterInfo extends L2GameServerPacket
{
	private final Player _player;
	
	public GMViewCharacterInfo(Player player)
	{
		_player = player;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x8f);
		
		writeD(_player.getX());
		writeD(_player.getY());
		writeD(_player.getZ());
		writeD(_player.getHeading());
		writeD(_player.getObjectId());
		writeS(_player.getName());
		writeD(_player.getRace().ordinal());
		writeD(_player.getAppearance().getSex().ordinal());
		writeD(_player.getClassId().getId());
		writeD(_player.getLevel());
		writeQ(_player.getExp());
		writeD(_player.getSTR());
		writeD(_player.getDEX());
		writeD(_player.getCON());
		writeD(_player.getINT());
		writeD(_player.getWIT());
		writeD(_player.getMEN());
		writeD(_player.getMaxHp());
		writeD((int) _player.getCurrentHp());
		writeD(_player.getMaxMp());
		writeD((int) _player.getCurrentMp());
		writeD(_player.getSp());
		writeD(_player.getCurrentLoad());
		writeD(_player.getMaxLoad());
		writeD(0x28); // unknown
		
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIRALL));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FACE));
		
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIRALL));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_REAR));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_NECK));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FACE));
		
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
		
		writeD(_player.getPAtk(null));
		writeD(_player.getPAtkSpd());
		writeD(_player.getPDef(null));
		writeD(_player.getEvasionRate(null));
		writeD(_player.getAccuracy());
		writeD(_player.getCriticalHit(null, null));
		writeD(_player.getMAtk(null, null));
		
		writeD(_player.getMAtkSpd());
		writeD(_player.getPAtkSpd());
		
		writeD(_player.getMDef(null, null));
		
		writeD(_player.getPvpFlag()); // 0-non-pvp 1-pvp = violett name
		writeD(_player.getKarma());
		
		int _runSpd = _player.getStat().getBaseRunSpeed();
		int _walkSpd = _player.getStat().getBaseWalkSpeed();
		int _swimSpd = _player.getStat().getBaseSwimSpeed();
		writeD(_runSpd); // base run speed
		writeD(_walkSpd); // base walk speed
		writeD(_swimSpd); // swim run speed
		writeD(_swimSpd); // swim walk speed
		writeD(0);
		writeD(0);
		writeD(_player.isFlying() ? _runSpd : 0); // fly run speed
		writeD(_player.isFlying() ? _walkSpd : 0); // fly walk speed
		writeF(_player.getStat().getMovementSpeedMultiplier()); // run speed multiplier
		writeF(_player.getStat().getAttackSpeedMultiplier()); // attack speed multiplier
		
		writeF(_player.getCollisionRadius()); // scale
		writeF(_player.getCollisionHeight()); // y offset ??!? fem dwarf 4033
		writeD(_player.getAppearance().getHairStyle());
		writeD(_player.getAppearance().getHairColor());
		writeD(_player.getAppearance().getFace());
		writeD(_player.isGM() ? 0x01 : 0x00); // builder level
		
		writeS(_player.getTitle());
		writeD(_player.getClanId()); // pledge id
		writeD(_player.getClanCrestId()); // pledge crest id
		writeD(_player.getAllyId()); // ally id
		writeC(_player.getMountType()); // mount type
		writeC(_player.getOperateType().getId());
		writeC(_player.hasDwarvenCraft() ? 1 : 0);
		writeD(_player.getPkKills());
		writeD(_player.getPvpKills());
		
		writeH(_player.getRecomLeft());
		writeH(_player.getRecomHave()); // Blue value for name (0 = white, 255 = pure blue)
		writeD(_player.getClassId().getId());
		writeD(0x00); // special effects? circles around player...
		writeD(_player.getMaxCp());
		writeD((int) _player.getCurrentCp());
		
		writeC(_player.isRunning() ? 0x01 : 0x00); // changes the Speed display on Status Window
		
		writeC(321);
		
		writeD(_player.getPledgeClass()); // changes the text above CP on Status Window
		
		writeC(_player.isNoble() ? 0x01 : 0x00);
		writeC(_player.isHero() ? 0x01 : 0x00);
		
		writeD(_player.getAppearance().getNameColor());
		writeD(_player.getAppearance().getTitleColor());
	}
}