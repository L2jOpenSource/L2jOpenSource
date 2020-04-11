/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.network.serverpackets;

import com.l2jserver.Config;
import com.l2jserver.gameserver.data.xml.impl.ExperienceData;
import com.l2jserver.gameserver.data.xml.impl.NpcData;
import com.l2jserver.gameserver.data.xml.impl.PlayerTemplateData;
import com.l2jserver.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jserver.gameserver.instancemanager.TerritoryWarManager;
import com.l2jserver.gameserver.model.Elementals;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.base.ClassId;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;
import com.l2jserver.gameserver.model.skills.AbnormalVisualEffect;
import com.l2jserver.gameserver.model.zone.ZoneId;

public final class UserInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private int _relation;
	private int _airShipHelm;
	
	private final int _runSpd, _walkSpd;
	private final int _swimRunSpd, _swimWalkSpd;
	private final int _flyRunSpd, _flyWalkSpd;
	private final double _moveMultiplier;
	
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
		_runSpd = (int) Math.round(cha.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) Math.round(cha.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = (int) Math.round(cha.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = (int) Math.round(cha.getSwimWalkSpeed() / _moveMultiplier);
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
		writeS(_activeChar.getAppearance().getVisibleName());
		if (_activeChar.hasAlternativeClass())
		{
			writeD(_activeChar.getAlternativeClassId());
		}
		else
		{
			writeD(_activeChar.getRace().ordinal());
		}
		writeD(_activeChar.getAppearance().getSex() ? 1 : 0);
		
		if (_activeChar.hasAlternativeClass() && (_activeChar.getAlternativeClassId() >= 0))
		{
			switch (_activeChar.getAlternativeClassId())
			{
				case 0:
					if (!_activeChar.getClassId().childOf(ClassId.fighter) && !_activeChar.getClassId().childOf(ClassId.mage))
					{
						_activeChar.setCollisionHeight(PlayerTemplateData.getInstance().getTemplate(ClassId.treasureHunter.ordinal()).getfCollisionHeight());
						writeD(ClassId.treasureHunter.ordinal());
						break;
					}
					if (_activeChar.getClassId().childOf(ClassId.fighter))
					{
						_activeChar.setCollisionHeight(_activeChar.getCollisionHeight());
						writeD(ClassId.treasureHunter.ordinal());
					}
					else
					{
						_activeChar.setCollisionHeight(_activeChar.getCollisionHeight());
						writeD(ClassId.bishop.ordinal());
					}
					break;
				case 1:
					if (!_activeChar.getClassId().childOf(ClassId.elvenFighter) && !_activeChar.getClassId().childOf(ClassId.elvenMage))
					{
						_activeChar.setCollisionHeight(PlayerTemplateData.getInstance().getTemplate(ClassId.elvenFighter.ordinal()).getfCollisionHeight());
						writeD(ClassId.elvenMage.ordinal());
						break;
					}
					
					if (_activeChar.getClassId().childOf(ClassId.elvenFighter))
					{
						_activeChar.setCollisionHeight(_activeChar.getCollisionHeight());
						writeD(ClassId.elvenFighter.ordinal());
					}
					else
					{
						_activeChar.setCollisionHeight(_activeChar.getCollisionHeight());
						writeD(ClassId.mysticMuse.ordinal());
					}
					break;
				case 2:
					if (!_activeChar.getClassId().childOf(ClassId.darkFighter) && !_activeChar.getClassId().childOf(ClassId.darkMage))
					{
						_activeChar.setCollisionHeight(PlayerTemplateData.getInstance().getTemplate(ClassId.darkFighter.ordinal()).getCollisionHeight());
						writeD(ClassId.darkFighter.ordinal());
						break;
					}
					if (_activeChar.getClassId().childOf(ClassId.darkFighter))
					{
						_activeChar.setCollisionHeight(_activeChar.getCollisionHeight());
						writeD(ClassId.palusKnight.ordinal());
					}
					else
					{
						_activeChar.setCollisionHeight(_activeChar.getCollisionHeight());
						writeD(ClassId.stormScreamer.ordinal());
					}
					break;
				case 3:
					if (!_activeChar.getClassId().childOf(ClassId.orcFighter) && !_activeChar.getClassId().childOf(ClassId.mage))
					{
						_activeChar.setCollisionHeight(PlayerTemplateData.getInstance().getTemplate(ClassId.orcFighter.ordinal()).getCollisionHeight());
						writeD(ClassId.orcFighter.ordinal());
						break;
					}
					if (_activeChar.getClassId().childOf(ClassId.orcFighter))
					{
						_activeChar.setCollisionHeight(_activeChar.getCollisionHeight());
						writeD(ClassId.orcFighter.ordinal());
					}
					else
					{
						_activeChar.setCollisionHeight(_activeChar.getCollisionHeight());
						writeD(ClassId.dominator.ordinal());
					}
					break;
				case 4:
					if (!_activeChar.getClassId().childOf(ClassId.dwarvenFighter))
					{
						_activeChar.setCollisionHeight(PlayerTemplateData.getInstance().getTemplate(ClassId.warsmith.ordinal()).getfCollisionHeight());
						writeD(ClassId.warsmith.ordinal());
					}
					else
					{
						_activeChar.setCollisionHeight(_activeChar.getCollisionHeight());
						writeD(_activeChar.getBaseClass());
					}
					break;
				case 5:
					if (!_activeChar.getClassId().childOf(ClassId.femaleSoldier) && !_activeChar.getClassId().childOf(ClassId.maleSoldier))
					{
						_activeChar.setCollisionHeight(PlayerTemplateData.getInstance().getTemplate(ClassId.arbalester.ordinal()).getfCollisionHeight());
						writeD(ClassId.arbalester.ordinal());
					}
					else
					{
						_activeChar.setCollisionHeight(_activeChar.getCollisionHeight());
						writeD(ClassId.arbalester.ordinal());
					}
					break;
			}
		}
		else
		{
			writeD(_activeChar.getBaseClass());
		}
		
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
		writeD((int) Math.round(_activeChar.getCurrentHp()));
		writeD(_activeChar.getMaxMp());
		writeD((int) Math.round(_activeChar.getCurrentMp()));
		writeD(_activeChar.getSp());
		writeD(_activeChar.getCurrentLoad());
		writeD(_activeChar.getMaxLoad());
		
		writeD(_activeChar.getActiveWeaponItem() != null ? 40 : 20); // 20 no weapon, 40 weapon equipped
		
		for (int slot : getPaperdollOrder())
		{
			writeD(_activeChar.getInventory().getPaperdollObjectId(slot));
		}
		
		for (int slot : getPaperdollOrder())
		{
			if (_activeChar.hasAlternativeEquip())
			{
				if ((Inventory.PAPERDOLL_CHEST == slot) && (_activeChar.getAlternativeArmor() >= 0))// chest
				{
					writeD(armorIds[_activeChar.getAlternativeArmor()][0]);
				}
				else if ((Inventory.PAPERDOLL_LEGS == slot) && (_activeChar.getAlternativeArmor() >= 0)) // legs
				{
					writeD(armorIds[_activeChar.getAlternativeArmor()][1]);
				}
				else if ((Inventory.PAPERDOLL_GLOVES == slot) && (_activeChar.getAlternativeArmor() >= 0))// gloves
				{
					writeD(armorIds[_activeChar.getAlternativeArmor()][2]);
				}
				else if ((Inventory.PAPERDOLL_FEET == slot) && (_activeChar.getAlternativeArmor() >= 0))// feet
				{
					writeD(armorIds[_activeChar.getAlternativeArmor()][3]);
				}
				else if (Inventory.PAPERDOLL_LHAND == slot)
				{
					if (Config.ALLOW_SHIELD_ON_VISUALEQUIP && (_activeChar.getAlternativeShield() >= 0))
					{
						writeD(shieldIds[_activeChar.getAlternativeShield()]);
					}
					else
					{
						writeD(_activeChar.getInventory().getPaperdollItemDisplayId(slot));
					}
				}
				else if ((Inventory.PAPERDOLL_RHAND == slot) && (_activeChar.getAlternativeWeapon() >= 0))
				{
					if (Config.ALLOW_WEAPON_ON_VISUALEQUIP)
					{
						writeD(weaponIds[_activeChar.getAlternativeWeapon()]);
					}
					else
					{
						writeD(_activeChar.getInventory().getPaperdollItemDisplayId(slot));
					}
				}
				else
				{
					writeD(_activeChar.getInventory().getPaperdollItemDisplayId(slot));
				}
			}
			else
			{
				writeD(_activeChar.getInventory().getPaperdollItemDisplayId(slot));
			}
		}
		
		for (int slot : getPaperdollOrder())
		{
			writeD(_activeChar.getInventory().getPaperdollAugmentationId(slot));
		}
		
		writeD(_activeChar.getInventory().getTalismanSlots());
		writeD(_activeChar.getInventory().canEquipCloak() ? 1 : 0);
		writeD((int) _activeChar.getPAtk(null));
		writeD((int) _activeChar.getPAtkSpd());
		writeD((int) _activeChar.getPDef(null));
		writeD(_activeChar.getEvasionRate(null));
		writeD(_activeChar.getAccuracy());
		writeD(_activeChar.getCriticalHit(null, null));
		writeD((int) _activeChar.getMAtk(null, null));
		
		writeD(_activeChar.getMAtkSpd());
		writeD((int) _activeChar.getPAtkSpd());
		
		writeD((int) _activeChar.getMDef(null, null));
		
		writeD(_activeChar.getPvpFlag());
		writeD(_activeChar.getKarma());
		
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd);
		writeD(_swimWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(_moveMultiplier);
		writeF(_activeChar.getAttackSpeedMultiplier());
		
		writeF(_activeChar.getCollisionRadius());
		if (_activeChar.hasAlternativeClass() && (_activeChar.getAlternativeClassId() >= 0))
		{
			writeF(_activeChar.getCollisionHeightCustom());
			_activeChar.setCollisionHeight(_activeChar.getCollisionHeight());
		}
		else
		{
			writeF(_activeChar.getCollisionHeight());
		}
		
		writeD(_activeChar.getAppearance().getHairStyle());
		writeD(_activeChar.getAppearance().getHairColor());
		writeD(_activeChar.getAppearance().getFace());
		writeD(_activeChar.isGM() ? 1 : 0); // builder level
		
		String title = _activeChar.getTitle();
		if (_activeChar.isGM() && _activeChar.isInvisible())
		{
			title = "Invisible";
		}
		if (_activeChar.getPoly().isMorphed())
		{
			final L2NpcTemplate polyObj = NpcData.getInstance().getTemplate(_activeChar.getPoly().getPolyId());
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
		for (int cubicId : _activeChar.getCubics().keySet())
		{
			writeH(cubicId);
		}
		
		writeC(_activeChar.isInPartyMatchRoom() ? 1 : 0);
		
		writeD(_activeChar.isInvisible() ? _activeChar.getAbnormalVisualEffects() | AbnormalVisualEffect.STEALTH.getMask() : _activeChar.getAbnormalVisualEffects());
		writeC(_activeChar.isInsideZone(ZoneId.WATER) ? 1 : _activeChar.isFlyingMounted() ? 2 : 0);
		
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
		writeD(_activeChar.getTransformationDisplayId());
		
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
		writeD(_activeChar.getAbnormalVisualEffectSpecial());
		// writeD(_territoryId); // CT2.3
		// writeD((_isDisguised ? 0x01: 0x00)); // CT2.3
		// writeD(_territoryId); // CT2.3
	}
	
	public static int[][] armorIds =
	{
		// Elegia Heavy
		{
			15575,
			15578,
			15581,
			15584
		},
		// Elegia Light
		{
			15576,
			15579,
			15582,
			15585
		},
		// Elegia Robe
		{
			15577,
			15580,
			15583,
			15586
		},
		// Vorpal Heavy - 3
		{
			15592,
			15595,
			15598,
			15601
		},
		// Vorpal Light
		{
			15593,
			15596,
			15599,
			15602
		},
		// Vorpal Robe
		{
			15594,
			15596,
			15600,
			15603
		},
		// Vesper Noble Heavy - 6
		{
			13435,
			13448,
			13449,
			13450
		},
		// Vesper Noble Light
		{
			13436,
			13451,
			13452,
			13453
		},
		// vesper Noble Robe
		{
			13437,
			13454,
			13455,
			13456
		},
		// Vesper Heavy - 9
		{
			13432,
			13438,
			13439,
			13440
		},
		// Vesper Light
		{
			13433,
			13441,
			13442,
			13443
		},
		// Vesper Robe
		{
			13434,
			13444,
			13445,
			13446
		},
		
		// Moirai Heavy - 12
		{
			15609,
			15612,
			15615,
			15618
		},
		// Moirai Light
		{
			15610,
			15613,
			15616,
			15619
		},
		// Moirai Robe
		{
			15611,
			15614,
			15617,
			15620
		},
		// Dynasty Heavy - 15
		{
			9416,
			9421,
			9423,
			9424
		},
		// Dynasty Light
		{
			9425,
			9428,
			9430,
			9431
		},
		// Dynasty Robe
		{
			9432,
			9437,
			9439,
			9440
		},
		// Imperial - 18
		{
			6373,
			6374,
			6375,
			6376
		},
		// Draconic
		{
			6379,
			6379,
			6380,
			6381
		},
		// Arcana
		{
			6383,
			6383,
			6384,
			6385
		},
		// Majestic Heavy - 21
		{
			2383,
			2383,
			5774,
			5786
		},
		// Majestic Light
		{
			2395,
			2395,
			5775,
			5787
		},
		// Majestic Robe
		{
			2409,
			2409,
			5776,
			5788
		},
		// Tallum Heavy - 24
		{
			2382,
			2382,
			5768,
			5780
		},
		// Tallum Light
		{
			2393,
			2393,
			5769,
			5781
		},
		// Tallum Robe
		{
			2400,
			2405,
			5770,
			5782
		},
		// Nightmare Heavy - 27
		{
			374,
			374,
			11472,
			5783
		},
		// Nightmare Light
		{
			2394,
			2394,
			11473,
			5784
		},
		// Nightmare Robe
		{
			2408,
			2408,
			11474,
			5785
		},
		// Dark Crystal Heavy - 30
		{
			365,
			388,
			5765,
			5777
		},
		// Dark Crystal Light
		{
			2385,
			2389,
			5766,
			5778
		},
		// Dark Crystal Robe
		{
			2407,
			2407,
			5767,
			5779
		},
		// Blue Wolf Heavy - 33
		{
			358,
			2380,
			5718,
			5734
		},
		// Blue Wolf Light
		{
			2391,
			2391,
			5719,
			5736
		},
		// Blue Wolf Robe
		{
			2398,
			2403,
			5720,
			5736
		},
		// Doom Heavy - 36
		{
			2381,
			2381,
			5722,
			5738
		},
		// Doom Light
		{
			2392,
			2392,
			5723,
			5739
		},
		// Doom Robe
		{
			2399,
			2404,
			5724,
			5740
		},
		// Full Plate - 39
		{
			356,
			356,
			2462,
			2438
		},
		// Chain
		{
			354,
			381,
			2453,
			2429
		},
		// Composite Armor
		{
			60,
			60,
			63,
			64
		},
		// Karmian Tunic - 42
		{
			439,
			471,
			2454,
			2430
		},
		// Demon's Tunic
		{
			441,
			472,
			2459,
			2435
		},
		// Divine Tunic
		{
			442,
			473,
			2463,
			603
		},
		// Theca - 45
		{
			400,
			420,
			2460,
			2436
		},
		// Drake
		{
			401,
			401,
			2461,
			2437
		},
		// Plated
		{
			398,
			418,
			2455,
			2431
		},
		// School Uniform A - 48
		{
			25000,
			25000,
			25000,
			25000
		},
		// School Uniform B
		{
			25001,
			25001,
			25001,
			25001
		},
		// Cowboy Outfit
		{
			25002,
			25002,
			25002,
			25002
		},
		// Archer Red Armor - 51
		{
			25004,
			25004,
			25004,
			25004
		},
		// Royal White Suit
		{
			25006,
			25006,
			25006,
			25006
		},
		// Green Wizard Suit
		{
			25008,
			25008,
			25008,
			25008
		},
		// Musketeer - 54
		{
			25010,
			25010,
			25010,
			25010
		},
		// Dark Assassin Suit
		{
			25012,
			25012,
			25012,
			25012
		},
		// White Assassin Suit
		{
			25014,
			25014,
			25014,
			25014
		},
		// Santa Suit - 57
		{
			25016,
			25016,
			25016,
			25016
		},
		// Vampire Hunter Suit
		{
			25017,
			25017,
			25017,
			25017
		},
		// Military Suit
		{
			25018,
			25018,
			25018,
			25018
		},
		// Maid Service Costume- 60
		{
			25019,
			25019,
			25019,
			25019
		},
		// Ninja Costume
		{
			25020,
			25020,
			25020,
			25020
		},
		// Chinese Attire 1
		{
			25021,
			25021,
			25021,
			25021
		},
		// Chinese Attire 2 - 63
		{
			25022,
			25022,
			25022,
			25022
		},
		// Chinese Attire 3
		{
			25023,
			25023,
			25023,
			25023
		},
		// Chinese Attire 4
		{
			25024,
			25024,
			25024,
			25024
		},
		// Beach Swimsuit - 66
		{
			25025,
			25025,
			25025,
			25025
		},
		// Alluring Swimsuit
		{
			25026,
			25026,
			25026,
			25026
		},
		// Seductive Swimsuit
		{
			25027,
			25027,
			25027,
			25027
		},
		// Summer Swimsuit 1
		{
			25028,
			25028,
			25028,
			25028
		},
		// Summer Swimsuit 2 - 70
		{
			25029,
			25029,
			25029,
			25029
		},
		// Summer Swimsuit 3
		{
			25030,
			25030,
			25030,
			25030
		},
		// Body Tattoos A
		{
			25031,
			25031,
			25031,
			25031
		},
		// Body Tattoos B
		{
			25032,
			25032,
			25032,
			25032
		},
		// Body Tattoos C
		{
			25033,
			25033,
			25033,
			25033
		},
		// Metal Suit - 75
		{
			25039,
			25039,
			25039,
			25039
		},
		// Cat Suit
		{
			25040,
			25040,
			25040,
			25040
		},
		// Samurai Outfit
		{
			25042,
			25042,
			25042,
			25042
		},
		// Halloween outfit - 78
		{
			25044,
			25044,
			25044,
			25044
		},
		// Pirate Suit
		{
			25046,
			25046,
			25046,
			25046
		},
		// Pirate Captain Suit
		{
			25048,
			25048,
			25048,
			25048
		},
		// Beleth Suit - 81
		{
			25050,
			25050,
			25050,
			25050
		},
		// Anakim Suit
		{
			25052,
			25052,
			25052,
			25052
		},
		// Tauti Heavy Suit
		{
			25053,
			25053,
			25053,
			25053
		},
		// Tauti Light Suit - 84
		{
			25054,
			25054,
			25054,
			25054
		},
		// Tauti Robe Suit
		{
			25055,
			25055,
			25055,
			25055
		},
		// Navy Suit
		{
			25056,
			25056,
			25056,
			25056
		},
		// Freya Suit - 87
		{
			25057,
			25057,
			25057,
			25057
		},
		// Single Mesh Armor - 88
		{
			25058,
			25058,
			25058,
			25058
		},
	};
	
	public static int[] shieldIds =
	{
		15587, // Elegia Shield
		15604, // Vorpal Shield
		13471, // Vesper Shield
		15621, // Moirai Shield
		9441, // Dynasty Shield
		6377, // Imperial Shield
		2498, // Nightmare Shield
		641, // Dark Crystal Shield
		633, // Zubei Shield
		673, // Avadon Shield
		110, // Doom Shield
		4222, // Dream Shield
		4223, // Ubiquitous Shield
		2497, // Full Plate Shield
		2496, // Dwarven Chain Shield
		2495, // Chain Shield
		2494, // Plate Shield
		2493, // Brigandine Shield
		1332, // Knight Shield
		1329, // Shield of Victory
		1328, // Shield of Grace
		674, // Divine Shield
		672, // Absolute Shield
		671, // Blood Shield
		670, // Shield of Bravery
		669, // Flame Shield
		668, // Shield of blessing
		667, // Shield of Aid
		666, // Cerberus Shield
		665, // Phoenix Shield
		664, // Shield of Holy Spirit
		662, // Shield of Phatom
		661, // Elemental Shield
		660, // Otherworldy Shield
		659, // Shieldof Summoning
		658, // Shield of Blackore
		657, // Inferno Shield
		656, // Paradia Shield
		655, // Sage's Shield
		654, // Shield of Mana
		653, // Marksman Shield
		652, // Guardian's Shield
		651, // Ace's Shield
		650, // Shield of Concentration
		649, // Shield of Underworld
		648, // Prairie Shield
		647, // Gust Shield
		646, // Shield of silence
		645, // Art of Shield
		644, // Hell Shield
		643, // Dark Vagian Shield
		642, // Elven Vagian Shield
		640, // Elven Crystal Shield
		639, // Red flame Shield
		638, // Glorious Shield
		637, // Shield of Valor
		636, // Shining Dragon Shield
		635, // Wolf Shield
		634, // Dragon Shield
		632, // Knight's Shield
		630, // Square Shield
		629, // Kite Shield
		626, // Bronze Shield
		625, // Bone Shield
		111, // Shield of pledge
		109, // Shield of Solar Eclipse
		108, // Masterpiece Shield
		107, // Composite Shield
		106, // Dark Dragon Shield
		105, // Implosion Shield
		103, // Tower Shield
		102, // Round Shield
		19, // Small Shield
		18, // Leather Shield
		26001 // Halloween Shield
	};
	
	public static int[] weaponIds =
	{
		15560, // Vigwik Axe
		15561, // Devilish Maul
		15562, // Feather Eye Blade
		15563, // Octo Claw
		15564, // Doubletop Spear
		15565, // Rising Star - 5
		15566, // Black Visage
		15567, // Veniplant Sword
		15568, // Skull Carnium Bow
		15558, // Periel Sword
		15559, // Skull Edge - 10
		10226, // Icarus Shooter
		10220, // Icarus Hammer
		10215, // Icarus Sawsword
		9376, // Dynasty Rapier
		9384, // Dynasty Crossbow - 15
		10253, // Dynasty Crusher
		10252, // Dynasty Staff
		9448, // Dynasty Cudgel
		52, // Vesper Dual Sword
		13458, // Vesper Slasher - 20
		13457, // Vesper Cutter
		11256, // Dynasty Mace
		11268, // Dynasty Blade
		26002, // Halloween Cutter
		26003, // Halloween Shapper - 25
		26004, // Halloween Slasher
		26005, // Halloween Fighter
		26006, // Halloween Stormer
		26007, // Halloween Bow
		26008, // Halloween Axe - 30
		26009, // Halloween Crusher
		26010, // Halloween Buster
		26011, // Halloween Caster
		26012, // Halloween Retributer
		26013, // Halloween Dualsword - 35
		26014, // Halloween Dual Dagger
		26015, // Halloween Crossbow
		26016, // Fafurion Avenger - Health
		26017, // Fafurion Cutter - Focus
		26018, // Fafurion Fighter - Focus - 40
		26019, // Fafurion Singer - Empower
		26020, // Fafurion Shaper - Haste
		26021, // Fafurion Stormer - Haste
		26022, // Fafurion Dual Sword
		26023, // Fafurion Dual Daggers - 45
		100030, // Antharas Avenger-Top-grade
		100031, // Antharas Buster-Top-grade
		100032, // Antharas Cutter-Top-grade
		100033, // Antharas Dual Blunt Weapon-Top-grade
		100034, // Antharas Dualsword-Top-grade - 50
		100035, // Antharas Fighter-Top-grade
		100036, // Antharas Shaper-Top-grade
		100037, // Antharas Slasher-Top-grade
		100038, // Antharas Stormer-Top-grade
		100039, // Antharas Thrower-Top-grade - 55
		101030, // Valakas Buster-Top-grade
		101031, // Valakas Caster-Top-grade
		101032, // Valakas Cutter-Top-grade
		101033, // Valakas Retributer-Top-grade
		101034, // Valakas Shaper-Top-grade - 60
		101035, // Valakas Slasher-Top-grade
		101036, // Valakas Thrower-Top-grade
		102030, // Lindvior Caster-Top-grade
		102031, // Lindvior Cutter-Top-grade
		102032, // Lindvior Caster-Top-grade - 65
		102033, // Lindvior Caster-Top-grade
		102034, // Lindvior Caster-Top-grade
		102035, // Lindvior Caster-Top-grade
		102036 // Lindvior Caster-Top-grade
	};
}
