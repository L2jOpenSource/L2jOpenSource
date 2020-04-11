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

import java.text.DecimalFormat;

import l2r.Config;
import l2r.gameserver.datatables.sql.ClanTable;
import l2r.gameserver.datatables.xml.CharTemplateData;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.TownManager;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.actor.FakePc;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2GuardInstance;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2NpcInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2TrapInstance;
import l2r.gameserver.model.actor.templates.L2PcTemplate;
import l2r.gameserver.model.effects.AbnormalEffect;
import gr.reunion.datatables.FakePcsTable;

public abstract class AbstractNpcInfo extends L2GameServerPacket
{
	protected int _x, _y, _z, _heading;
	protected int _idTemplate;
	protected boolean _isAttackable, _isSummoned;
	protected int _mAtkSpd, _pAtkSpd;
	protected int _runSpd;
	protected int _walkSpd;
	protected final int _swimRunSpd;
	protected final int _swimWalkSpd;
	protected final int _flRunSpd = 0;
	protected final int _flWalkSpd = 0;
	protected final int _flyRunSpd;
	protected final int _flyWalkSpd;
	protected float _moveMultiplier;
	
	protected int _rhand, _lhand, _chest, _enchantEffect;
	protected double _collisionHeight, _collisionRadius;
	protected String _name = "";
	protected String _title = "";
	
	public AbstractNpcInfo(L2Character cha)
	{
		_isSummoned = cha.isShowSummonAnimation();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_heading = cha.getHeading();
		_mAtkSpd = cha.getMAtkSpd();
		_pAtkSpd = cha.getPAtkSpd();
		_moveMultiplier = cha.getMovementSpeedMultiplier();
		_runSpd = Math.round(cha.getRunSpeed() / _moveMultiplier);
		_walkSpd = Math.round(cha.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = Math.round(cha.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = Math.round(cha.getSwimWalkSpeed() / _moveMultiplier);
		_flyRunSpd = cha.isFlying() ? _runSpd : 0;
		_flyWalkSpd = cha.isFlying() ? _walkSpd : 0;
	}
	
	/**
	 * Packet for Npcs
	 */
	public static class NpcInfo extends AbstractNpcInfo
	{
		private final L2Npc _npc;
		private int _clanCrest = 0;
		private int _allyCrest = 0;
		private int _allyId = 0;
		private int _clanId = 0;
		private int _displayEffect = 0;
		
		public NpcInfo(L2Npc cha, L2Character attacker)
		{
			super(cha);
			_npc = cha;
			_idTemplate = cha.getTemplate().getIdTemplate(); // On every subclass
			_rhand = cha.getRightHandItem(); // On every subclass
			_lhand = cha.getLeftHandItem(); // On every subclass
			_enchantEffect = cha.getEnchantEffect();
			_collisionHeight = cha.getCollisionHeight();// On every subclass
			_collisionRadius = cha.getCollisionRadius();// On every subclass
			_isAttackable = cha.isAutoAttackable(attacker);
			if (cha.getTemplate().isServerSideName())
			{
				_name = cha.getName();// On every subclass
			}
			
			if (_npc.isInvisible())
			{
				_title = "Invisible";
			}
			else if (Config.L2JMOD_CHAMPION_ENABLE && cha.isChampion())
			{
				_title = (Config.L2JMOD_CHAMP_TITLE); // On every subclass
			}
			else if (cha.getTemplate().isServerSideTitle())
			{
				_title = cha.getTemplate().getTitle(); // On every subclass
			}
			else
			{
				_title = cha.getTitle(); // On every subclass
			}
			
			if (Config.SHOW_NPC_LVL && (_npc instanceof L2MonsterInstance) && ((L2Attackable) _npc).canShowLevelInTitle())
			{
				String t = "Lv " + cha.getLevel() + (cha.isAggressive() ? "*" : "");
				if (_title != null)
				{
					t += " " + _title;
				}
				
				_title = t;
			}
			
			// npc crest of owning clan/ally of castle
			if ((cha instanceof L2NpcInstance) && cha.isInsideZone(ZoneIdType.TOWN) && (Config.SHOW_CREST_WITHOUT_QUEST || cha.getCastle().getShowNpcCrest()) && (cha.getCastle().getOwnerId() != 0))
			{
				int townId = TownManager.getTown(_x, _y, _z).getTownId();
				if ((townId != 33) && (townId != 22))
				{
					L2Clan clan = ClanTable.getInstance().getClan(cha.getCastle().getOwnerId());
					_clanCrest = clan.getCrestId();
					_clanId = clan.getId();
					_allyCrest = clan.getAllyCrestId();
					_allyId = clan.getAllyId();
				}
			}
			
			_displayEffect = cha.getDisplayEffect();
		}
		
		@Override
		protected void writeImpl()
		{
			FakePc fpc = FakePcsTable.getInstance().getFakePc(_npc.getId());
			if (fpc != null)
			{
				writeC(0x31);
				writeD(_x);
				writeD(_y);
				writeD(_z);
				writeD(0x00); // vehicle id
				writeD(_npc.getObjectId());
				writeS(fpc.name); // visible name
				writeD(fpc.race);
				writeD(fpc.sex);
				writeD(fpc.clazz);
				
				writeD(fpc.pdUnder);
				writeD(fpc.pdHead);
				writeD(fpc.pdRHand);
				writeD(fpc.pdLHand);
				writeD(fpc.pdGloves);
				writeD(fpc.pdChest);
				writeD(fpc.pdLegs);
				writeD(fpc.pdFeet);
				writeD(fpc.pdBack);
				writeD(fpc.pdLRHand);
				writeD(fpc.pdHair);
				writeD(fpc.pdHair2);
				writeD(fpc.pdRBracelet);
				writeD(fpc.pdLBracelet);
				writeD(fpc.pdDeco1);
				writeD(fpc.pdDeco2);
				writeD(fpc.pdDeco3);
				writeD(fpc.pdDeco4);
				writeD(fpc.pdDeco5);
				writeD(fpc.pdDeco6);
				writeD(0x00); // belt
				
				writeD(fpc.pdUnderAug);
				writeD(fpc.pdHeadAug);
				writeD(fpc.pdRHandAug);
				writeD(fpc.pdLHandAug);
				writeD(fpc.pdGlovesAug);
				writeD(fpc.pdChestAug);
				writeD(fpc.pdLegsAug);
				writeD(fpc.pdFeetAug);
				writeD(fpc.pdBackAug);
				writeD(fpc.pdLRHandAug);
				writeD(fpc.pdHairAug);
				writeD(fpc.pdHair2Aug);
				writeD(fpc.pdRBraceletAug);
				writeD(fpc.pdLBraceletAug);
				writeD(fpc.pdDeco1Aug);
				writeD(fpc.pdDeco2Aug);
				writeD(fpc.pdDeco3Aug);
				writeD(fpc.pdDeco4Aug);
				writeD(fpc.pdDeco5Aug);
				writeD(fpc.pdDeco6Aug);
				writeD(0x00); // belt aug
				writeD(0x00);
				writeD(0x01);
				
				writeD(fpc.pvpFlag);
				writeD(fpc.karma);
				
				writeD(_mAtkSpd);
				writeD(_pAtkSpd);
				
				writeD(0x00);
				
				writeD(_runSpd);
				writeD(_walkSpd);
				writeD(_runSpd); // swim run speed
				writeD(_walkSpd); // swim walk speed
				writeD(_runSpd); // fly run speed
				writeD(_walkSpd); // fly walk speed
				writeD(_runSpd);
				writeD(_walkSpd);
				writeF(_npc.getMovementSpeedMultiplier()); // _activeChar.getProperMultiplier()
				writeF(_npc.getAttackSpeedMultiplier()); // _activeChar.getAttackSpeedMultiplier()
				
				// TODO: add handling of mount collision
				L2PcTemplate pctmpl = CharTemplateData.getInstance().getTemplate(fpc.clazz);
				writeF(fpc.sex == 0 ? pctmpl.getfCollisionRadius() : pctmpl._fCollisionRadiusFemale);
				writeF(fpc.sex == 0 ? pctmpl.getfCollisionHeight() : pctmpl._fCollisionHeightFemale);
				
				writeD(fpc.hairStyle);
				writeD(fpc.hairColor);
				writeD(fpc.face);
				
				if ((_npc instanceof L2MonsterInstance) || (_npc instanceof L2GuardInstance))
				{
					writeS(fpc.title + (fpc.title.isEmpty() ? "" : " - ") + "HP " + new DecimalFormat("#").format((100.0 * _npc.getCurrentHp()) / _npc.getMaxHp()) + "%");
				}
				else
				{
					writeS(fpc.title);
				}
				
				writeD(0x00); // clan id
				writeD(0x00); // clan crest id
				writeD(0x00); // ally id
				writeD(0x00); // ally crest id
				
				writeC(0x01); // standing = 1 sitting = 0
				writeC(_npc.isRunning() ? 1 : 0); // running = 1 walking = 0
				writeC(_npc.isInCombat() ? 1 : 0);
				writeC(_npc.isAlikeDead() ? 1 : 0);
				
				writeC(fpc.invisible); // invisible = 1 visible =0
				
				writeC(fpc.mount); // 1 on strider 2 on wyvern 3 on Great Wolf 0 no mount
				writeC(0x00); // 1 - sellshop
				writeH(0x00); // cubic count
				// for (int id : allCubics)
				// writeH(id);
				writeC(0x00); // find party members
				writeD(0x00); // abnormal effect
				writeC(0x00); // isFlying() ? 2 : 0
				writeH(0x00); // getRecomHave(): Blue value for name (0 = white, 255 = pure blue)
				writeD(1000000); // getMountNpcId() + 1000000
				writeD(fpc.clazz);
				writeD(0x00); // ?
				writeC(fpc.enchantEffect);
				writeC(fpc.team); // team circle around feet 1= Blue, 2 = red
				writeD(0x00); // getClanCrestLargeId()
				writeC(0x00); // isNoble(): Symbol on char menu ctrl+I
				writeC(fpc.hero); // Hero Aura
				writeC(fpc.fishing); // 0x01: Fishing Mode (Cant be undone by setting back to 0)
				writeD(fpc.fishingX);
				writeD(fpc.fishingY);
				writeD(fpc.fishingZ);
				
				writeD(fpc.nameColor);
				writeD(_heading);
				writeD(0x00); // pledge class
				writeD(0x00); // pledge type
				
				if (_npc instanceof L2MonsterInstance)
				{
					if (((100 * _npc.getCurrentHp()) / _npc.getMaxHp()) >= 75)
					{
						writeD(Integer.decode("0x" + "00ff00"));
					}
					if ((((100 * _npc.getCurrentHp()) / _npc.getMaxHp()) < 75) && (((100 * _npc.getCurrentHp()) / _npc.getMaxHp()) >= 40))
					{
						writeD(Integer.decode("0x" + "0080ff"));
					}
					if (((100 * _npc.getCurrentHp()) / _npc.getMaxHp()) < 40)
					{
						writeD(Integer.decode("0x" + "3737ff"));
					}
				}
				else
				{
					writeD(fpc.titleColor);
				}
				
				writeD(0x00); // cursed weapon level
				writeD(0x00); // reputation score
				writeD(0x00); // transformation id
				writeD(0x00); // agathion id
				writeD(0x01); // T2 ?
				writeD(0x00); // special effect
				// writeD(0x00); // territory Id
				// writeD(0x00); // is Disguised
				// writeD(0x00); // territory Id
			}
			else
			{
				writeC(0x0c);
				writeD(_npc.getObjectId());
				writeD(_idTemplate + 1000000); // npctype id
				writeD(_isAttackable ? 1 : 0);
				writeD(_x);
				writeD(_y);
				writeD(_z);
				writeD(_heading);
				writeD(0x00);
				writeD(_mAtkSpd);
				writeD(_pAtkSpd);
				writeD(_runSpd);
				writeD(_walkSpd);
				writeD(_runSpd); // swim run speed
				writeD(_walkSpd); // swim walk speed
				writeD(_runSpd); // swim run speed
				writeD(_walkSpd); // swim walk speed
				writeD(_runSpd); // fly run speed
				writeD(_walkSpd); // fly run speed
				writeF(_npc.getMovementSpeedMultiplier());
				writeF(_npc.getAttackSpeedMultiplier());
				writeF(_collisionRadius);
				writeF(_collisionHeight);
				writeD(_rhand); // right hand weapon
				writeD(_chest);
				writeD(_lhand); // left hand weapon
				writeC(1); // name above char 1=true ... ??
				writeC(_npc.isRunning() ? 1 : 0);
				writeC(_npc.isInCombat() ? 1 : 0);
				writeC(_npc.isAlikeDead() ? 1 : 0);
				writeC(_isSummoned ? 2 : 0); // 0=teleported 1=default 2=summoned
				writeD(-1); // High Five NPCString ID
				writeS(_name);
				writeD(-1); // High Five NPCString ID
				writeS(_title);
				writeD(0x00); // Title color 0=client default
				writeD(0x00); // pvp flag
				writeD(0x00); // karma
				
				writeD(_npc.isInvisible() ? _npc.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask() : _npc.getAbnormalEffect());
				writeD(_clanId); // clan id
				writeD(_clanCrest); // crest id
				writeD(_allyId); // ally id
				writeD(_allyCrest); // all crest
				writeC(_npc.isInsideZone(ZoneIdType.WATER) ? 1 : _npc.isFlying() ? 2 : 0); // C2
				writeC(0x00); // title color 0=client
				
				writeF(_collisionRadius);
				writeF(_collisionHeight);
				writeD(_enchantEffect); // C4
				writeD(_npc.isFlying() ? 1 : 0); // C6
				writeD(0x00);
				writeD(_npc.getColorEffect());// CT1.5 Pet form and skills, Color effect
				writeC(_npc.isShowName() ? 0x01 : 0x00);
				writeC(_npc.isTargetable() ? 0x01 : 0x00);
				writeD(_npc.getSpecialEffect());
				writeD(_displayEffect);
			}
		}
	}
	
	public static class TrapInfo extends AbstractNpcInfo
	{
		private final L2TrapInstance _trap;
		
		public TrapInfo(L2TrapInstance cha, L2Character attacker)
		{
			super(cha);
			
			_trap = cha;
			_idTemplate = cha.getTemplate().getIdTemplate();
			_isAttackable = cha.isAutoAttackable(attacker);
			_rhand = 0;
			_lhand = 0;
			_collisionHeight = _trap.getTemplate().getfCollisionHeight();
			_collisionRadius = _trap.getTemplate().getfCollisionRadius();
			if (cha.getTemplate().isServerSideName())
			{
				_name = cha.getName();
			}
			_title = cha.getOwner() != null ? cha.getOwner().getName() : "";
		}
		
		@Override
		protected void writeImpl()
		{
			writeC(0x0c);
			writeD(_trap.getObjectId());
			writeD(_idTemplate + 1000000); // npctype id
			writeD(_isAttackable ? 1 : 0);
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(_heading);
			writeD(0x00);
			writeD(_mAtkSpd);
			writeD(_pAtkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_swimRunSpd);
			writeD(_swimWalkSpd);
			writeD(_flRunSpd);
			writeD(_flWalkSpd);
			writeD(_flyRunSpd);
			writeD(_flyWalkSpd);
			writeF(_moveMultiplier);
			writeF(_trap.getAttackSpeedMultiplier());
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(_rhand); // right hand weapon
			writeD(_chest);
			writeD(_lhand); // left hand weapon
			writeC(1); // name above char 1=true ... ??
			writeC(1);
			writeC(_trap.isInCombat() ? 1 : 0);
			writeC(_trap.isAlikeDead() ? 1 : 0);
			writeC(_isSummoned ? 2 : 0); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
			writeD(-1); // High Five NPCString ID
			writeS(_name);
			writeD(-1); // High Five NPCString ID
			writeS(_title);
			writeD(0x00); // title color 0 = client default
			
			writeD(_trap.getPvpFlag());
			writeD(_trap.getKarma());
			
			writeD(_trap.isInvisible() ? _trap.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask() : _trap.getAbnormalEffect());
			writeD(0x00); // clan id
			writeD(0x00); // crest id
			writeD(0000); // C2
			writeD(0000); // C2
			writeC(0000); // C2
			
			writeC(_trap.getTeam().getId());
			
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(0x00); // C4
			writeD(0x00); // C6
			writeD(0x00);
			writeD(0);// CT1.5 Pet form and skills
			writeC(0x01);
			writeC(0x01);
			writeD(0x00);
		}
	}
	
	/**
	 * Packet for summons
	 */
	public static class SummonInfo extends AbstractNpcInfo
	{
		private final L2Summon _summon;
		private final int _form;
		private final int _val;
		
		public SummonInfo(L2Summon cha, L2Character attacker, int val)
		{
			super(cha);
			_summon = cha;
			_val = val;
			_form = cha.getFormId();
			_isAttackable = cha.isAutoAttackable(attacker);
			_rhand = cha.getWeapon();
			_lhand = 0;
			_chest = cha.getArmor();
			_enchantEffect = cha.getTemplate().getEnchantEffect();
			_name = cha.getName();
			_title = (cha.getOwner() != null) && cha.getOwner().isOnline() ? cha.getOwner().getName() : "";
			_idTemplate = cha.getTemplate().getIdTemplate();
			_collisionHeight = cha.getTemplate().getfCollisionHeight();
			_collisionRadius = cha.getTemplate().getfCollisionRadius();
			_invisible = cha.isInvisible();
		}
		
		@Override
		protected void writeImpl()
		{
			boolean gmSeeInvis = false;
			if (_invisible)
			{
				final L2PcInstance activeChar = getClient().getActiveChar();
				if ((activeChar != null) && activeChar.canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS))
				{
					gmSeeInvis = true;
				}
			}
			
			writeC(0x0c);
			writeD(_summon.getObjectId());
			writeD(_idTemplate + 1000000); // npctype id
			writeD(_isAttackable ? 1 : 0);
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(_heading);
			writeD(0x00);
			writeD(_mAtkSpd);
			writeD(_pAtkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd); // swim run speed
			writeD(_walkSpd); // swim walk speed
			writeD(_runSpd); // fly run speed
			writeD(_walkSpd); // fly walk speed
			writeD(_runSpd); // fly run speed
			writeD(_walkSpd); // fly walk speed
			writeF(_summon.getMovementSpeedMultiplier());
			writeF(_summon.getAttackSpeedMultiplier());
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(_rhand); // right hand weapon
			writeD(_chest);
			writeD(_lhand); // left hand weapon
			writeC(0x01); // name above char 1=true ... ??
			writeC(0x01); // always running 1=running 0=walking
			writeC(_summon.isInCombat() ? 1 : 0);
			writeC(_summon.isAlikeDead() ? 1 : 0);
			writeC(_val); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
			writeD(-1); // High Five NPCString ID
			writeS(_name);
			writeD(-1); // High Five NPCString ID
			writeS(_title);
			writeD(0x01);// Title color 0=client default
			
			writeD(_summon.getPvpFlag());
			writeD(_summon.getKarma());
			
			writeD(gmSeeInvis ? _summon.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask() : _summon.getAbnormalEffect());
			
			writeD(0x00); // clan id
			writeD(0x00); // crest id
			writeD(0x00); // C2
			writeD(0x00); // C2
			writeC(_summon.isInsideZone(ZoneIdType.WATER) ? 1 : _summon.isFlying() ? 2 : 0); // C2
			
			writeC(_summon.getTeam().getId());
			
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(_enchantEffect); // C4
			writeD(0x00); // C6
			writeD(0x00);
			writeD(_form); // CT1.5 Pet form and skills
			writeC(0x01);
			writeC(0x01);
			writeD(_summon.getSpecialEffect());
		}
	}
}
