/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.network.serverpackets;

import l2r.Config;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.datatables.xml.ExperienceData;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.CursedWeaponsManager;
import l2r.gameserver.instancemanager.TerritoryWarManager;
import l2r.gameserver.model.Elementals;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.effects.AbnormalEffect;
import l2r.gameserver.model.itemcontainer.Inventory;

public final class UserInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private int _relation;
	private int _airShipHelm;
	
	private final int _runSpd, _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private final int _flRunSpd = 0;
	private final int _flWalkSpd = 0;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private final float _moveMultiplier;
	
	public UserInfo(L2PcInstance cha)
	{
		_activeChar = cha;
		
		int _territoryId = TerritoryWarManager.getInstance().getRegisteredTerritoryId(cha);
		_relation = _activeChar.isClanLeader() ? 0x40 : 0;
		if (_activeChar.getSiegeState() == 1)
		{
			if (_territoryId == 0)
			{
				_relation |= 0x180;
			}
			else
			{
				_relation |= 0x1000;
			}
		}
		if (_activeChar.getSiegeState() == 2)
		{
			_relation |= 0x80;
		}
		// _isDisguised = TerritoryWarManager.getInstance().isDisguised(character.getObjectId());
		if (_activeChar.isInAirShip() && _activeChar.getAirShip().isCaptain(_activeChar))
		{
			_airShipHelm = _activeChar.getAirShip().getHelmItemId();
		}
		else
		{
			_airShipHelm = 0;
		}
		
		_moveMultiplier = cha.getMovementSpeedMultiplier();
		_runSpd = Math.round(cha.getRunSpeed() / _moveMultiplier);
		_walkSpd = Math.round(cha.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = Math.round(cha.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = Math.round(cha.getSwimWalkSpeed() / _moveMultiplier);
		_flyRunSpd = cha.isFlying() ? _runSpd : 0;
		_flyWalkSpd = cha.isFlying() ? _walkSpd : 0;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x32);
		
		writeD(_activeChar.getX());
		writeD(_activeChar.getY());
		writeD(_activeChar.getZ());
		writeD(_activeChar.getVehicle() != null ? _activeChar.getVehicle().getObjectId() : 0);
		
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getName());
		writeD(_activeChar.getRace().ordinal());
		writeD(_activeChar.getAppearance().getSex() ? 1 : 0);
		
		writeD(_activeChar.getBaseClass());
		
		writeD(_activeChar.getLevel());
		writeQ(_activeChar.getExp());
		writeF((float) (_activeChar.getExp() - ExperienceData.getInstance().getExpForLevel(_activeChar.getLevel())) / (ExperienceData.getInstance().getExpForLevel(_activeChar.getLevel() + 1) - ExperienceData.getInstance().getExpForLevel(_activeChar.getLevel()))); // High Five exp %
		writeD(_activeChar.getSTR());
		writeD(_activeChar.getDEX());
		writeD(_activeChar.getCON());
		writeD(_activeChar.getINT());
		writeD(_activeChar.getWIT());
		writeD(_activeChar.getMEN());
		writeD(_activeChar.getMaxHp());
		writeD((int) _activeChar.getCurrentHp());
		writeD(_activeChar.getMaxMp());
		writeD((int) _activeChar.getCurrentMp());
		writeD(_activeChar.getSp());
		writeD(_activeChar.getCurrentLoad());
		writeD(_activeChar.getMaxLoad());
		
		writeD(_activeChar.getActiveWeaponItem() != null ? 40 : 20); // 20 no weapon, 40 weapon equipped
		
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_UNDER));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
		
		writeD(_airShipHelm == 0 ? _activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND) : _airShipHelm);
		writeD(_airShipHelm == 0 ? _activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND) : 0);
		
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CLOAK));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR2));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RBRACELET));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LBRACELET));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO1));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO2));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO3));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO4));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO5));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO6));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_BELT)); // CT2.3
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_UNDER));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_REAR));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_LEAR));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_NECK));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_RFINGER));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_LFINGER));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_HEAD));
		
		writeD(_airShipHelm == 0 ? _activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_RHAND) : _airShipHelm);
		writeD(_airShipHelm == 0 ? _activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_LHAND) : 0);
		
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_GLOVES));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_CHEST));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_LEGS));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_FEET));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_CLOAK));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_RHAND));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_HAIR));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_HAIR2));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_RBRACELET));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_LBRACELET));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_DECO1));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_DECO2));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_DECO3));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_DECO4));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_DECO5));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_DECO6));
		writeD(_activeChar.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_BELT)); // CT2.3
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_UNDER));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_REAR));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_LEAR));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_NECK));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RFINGER));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_LFINGER));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_HEAD));
		
		writeD(_airShipHelm == 0 ? _activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND) : _airShipHelm);
		writeD(_airShipHelm == 0 ? _activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_LHAND) : 0);
		
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_GLOVES));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_CHEST));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_LEGS));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_FEET));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_CLOAK));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_HAIR));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_HAIR2));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RBRACELET));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_LBRACELET));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO1));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO2));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO3));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO4));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO5));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO6));
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_BELT)); // CT2.3
		writeD(_activeChar.getInventory().getMaxTalismanCount()); // CT2.3
		writeD(_activeChar.getInventory().getCloakStatus()); // CT2.3
		writeD(_activeChar.getPAtk(null));
		writeD(_activeChar.getPAtkSpd());
		writeD(_activeChar.getPDef(null));
		writeD(_activeChar.getEvasionRate(null));
		writeD(_activeChar.getAccuracy());
		writeD(_activeChar.getCriticalHit(null, null));
		writeD(_activeChar.getMAtk(null, null));
		
		writeD(_activeChar.getMAtkSpd());
		writeD(_activeChar.getPAtkSpd());
		
		writeD(_activeChar.getMDef(null, null));
		
		writeD(_activeChar.getPvpFlag()); // 0-non-pvp 1-pvp = violet name
		writeD(_activeChar.getKarma());
		
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd);
		writeD(_swimWalkSpd);
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(_moveMultiplier);
		writeF(_activeChar.getAttackSpeedMultiplier());
		
		writeF(_activeChar.getCollisionRadius());
		writeF(_activeChar.getCollisionHeight());
		
		writeD(_activeChar.getAppearance().getHairStyle());
		writeD(_activeChar.getAppearance().getHairColor());
		writeD(_activeChar.getAppearance().getFace());
		writeD(_activeChar.isGM() ? 1 : 0); // builder level
		
		String title = _activeChar.getTitle();
		if (_activeChar.isInvisible())
		{
			title = "Invisible";
		}
		if (_activeChar.getPoly().isMorphed())
		{
			L2NpcTemplate polyObj = NpcTable.getInstance().getTemplate(_activeChar.getPoly().getPolyId());
			if (polyObj != null)
			{
				title += " - " + polyObj.getName();
			}
		}
		writeS(title);
		
		writeD(_activeChar.getClanId());
		writeD(_activeChar.getClanCrestId());
		writeD(_activeChar.getAllyId());
		writeD(_activeChar.getAllyCrestId()); // ally crest id
		// 0x40 leader rights
		// siege flags: attacker - 0x180 sword over name, defender - 0x80 shield, 0xC0 crown (|leader), 0x1C0 flag (|leader)
		writeD(_relation);
		writeC(_activeChar.getMountType().ordinal()); // mount type
		writeC(_activeChar.getPrivateStoreType().getId());
		writeC(_activeChar.hasDwarvenCraft() ? 1 : 0);
		writeD(_activeChar.getPkKills());
		writeD(_activeChar.getPvpKills());
		
		writeH(_activeChar.getCubics().size());
		for (int id : _activeChar.getCubics().keySet())
		{
			writeH(id);
		}
		
		writeC(_activeChar.isInPartyMatchRoom() ? 1 : 0);
		
		writeD(_activeChar.isInvisible() ? _activeChar.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask() : _activeChar.getAbnormalEffect());
		writeC(_activeChar.isInsideZone(ZoneIdType.WATER) ? 1 : _activeChar.isFlyingMounted() ? 2 : 0);
		
		writeD(_activeChar.getClanPrivileges().getBitmask());
		
		writeH(_activeChar.getRecomLeft()); // c2 recommendations remaining
		writeH(_activeChar.getRecomHave()); // c2 recommendations received
		writeD(_activeChar.getMountNpcId() > 0 ? _activeChar.getMountNpcId() + 1000000 : 0);
		writeH(_activeChar.getInventoryLimit());
		
		writeD(_activeChar.getClassId().getId());
		writeD(0x00); // special effects? circles around player...
		writeD(_activeChar.getMaxCp());
		writeD((int) _activeChar.getCurrentCp());
		writeC(_activeChar.isMounted() || (_airShipHelm != 0) ? 0 : _activeChar.getEnchantEffect());
		
		writeC(_activeChar.getTeam().getId());
		
		writeD(_activeChar.getClanCrestLargeId());
		writeC(_activeChar.isNoble() ? 1 : 0); // 0x01: symbol on char menu ctrl+I
		writeC(_activeChar.isHero() || (_activeChar.isGM() && Config.GM_HERO_AURA) ? 1 : 0); // 0x01: Hero Aura
		
		writeC(_activeChar.isFishing() ? 1 : 0); // Fishing Mode
		writeD(_activeChar.getFishx()); // fishing x
		writeD(_activeChar.getFishy()); // fishing y
		writeD(_activeChar.getFishz()); // fishing z
		writeD(_activeChar.getAppearance().getNameColor());
		
		// new c5
		writeC(_activeChar.isRunning() ? 0x01 : 0x00); // changes the Speed display on Status Window
		
		writeD(_activeChar.getPledgeClass()); // changes the text above CP on Status Window
		writeD(_activeChar.getPledgeType());
		
		writeD(_activeChar.getAppearance().getTitleColor());
		
		writeD(_activeChar.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(_activeChar.getCursedWeaponEquippedId()) : 0);
		
		// T1 Starts
		writeD(_activeChar.getTransformationId());
		
		byte attackAttribute = _activeChar.getAttackElement();
		writeH(attackAttribute);
		writeH(_activeChar.getAttackElementValue(attackAttribute));
		writeH(_activeChar.getDefenseElementValue(Elementals.FIRE));
		writeH(_activeChar.getDefenseElementValue(Elementals.WATER));
		writeH(_activeChar.getDefenseElementValue(Elementals.WIND));
		writeH(_activeChar.getDefenseElementValue(Elementals.EARTH));
		writeH(_activeChar.getDefenseElementValue(Elementals.HOLY));
		writeH(_activeChar.getDefenseElementValue(Elementals.DARK));
		
		writeD(_activeChar.getAgathionId());
		
		// T2 Starts
		writeD(_activeChar.getFame()); // Fame
		writeD(_activeChar.isMinimapAllowed() ? 1 : 0); // Minimap on Hellbound
		writeD(_activeChar.getVitalityPoints()); // Vitality Points
		writeD(_activeChar.getSpecialEffect());
		// writeD(_territoryId); // CT2.3
		// writeD((_isDisguised ? 0x01: 0x00)); // CT2.3
		// writeD(_territoryId); // CT2.3
	}
}
