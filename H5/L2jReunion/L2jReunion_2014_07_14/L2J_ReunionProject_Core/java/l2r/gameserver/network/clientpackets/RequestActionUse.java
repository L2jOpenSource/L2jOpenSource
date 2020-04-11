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
package l2r.gameserver.network.clientpackets;

import java.util.Arrays;

import l2r.Config;
import l2r.gameserver.ai.L2SummonAI;
import l2r.gameserver.ai.NextAction;
import l2r.gameserver.ai.NextAction.NextActionCallback;
import l2r.gameserver.datatables.sql.BotReportTable;
import l2r.gameserver.datatables.sql.SummonSkillsTable;
import l2r.gameserver.datatables.xml.PetData;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.MountType;
import l2r.gameserver.enums.PrivateStoreType;
import l2r.gameserver.instancemanager.AirShipManager;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2BabyPetInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2PetInstance;
import l2r.gameserver.model.actor.instance.L2SiegeFlagInstance;
import l2r.gameserver.model.actor.instance.L2StaticObjectInstance;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.ChairSit;
import l2r.gameserver.network.serverpackets.ExAskCoupleAction;
import l2r.gameserver.network.serverpackets.ExBasicActionList;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.gameserver.network.serverpackets.RecipeShopManageList;
import l2r.gameserver.network.serverpackets.SocialAction;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.taskmanager.AttackStanceTaskManager;
import l2r.util.Rnd;

/**
 * This class manages the action use request packet.
 * @author Zoey76
 */
public final class RequestActionUse extends L2GameClientPacket
{
	private static final String _C__56_REQUESTACTIONUSE = "[C] 56 RequestActionUse";
	
	private static final int SIN_EATER_ID = 12564;
	private static final int SWITCH_STANCE_ID = 6054;
	private static final NpcStringId[] NPC_STRINGS =
	{
		NpcStringId.USING_A_SPECIAL_SKILL_HERE_COULD_TRIGGER_A_BLOODBATH,
		NpcStringId.HEY_WHAT_DO_YOU_EXPECT_OF_ME,
		NpcStringId.UGGGGGH_PUSH_ITS_NOT_COMING_OUT,
		NpcStringId.AH_I_MISSED_THE_MARK
	};
	
	private int _actionId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	@Override
	protected void readImpl()
	{
		_actionId = readD();
		_ctrlPressed = (readD() == 1);
		_shiftPressed = (readC() == 1);
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.info(activeChar + " requested action use Id: " + _actionId + " Ctrl pressed:" + _ctrlPressed + " Shift pressed:" + _shiftPressed);
		}
		
		// Don't do anything if player is dead
		if (activeChar.isAlikeDead() || activeChar.isDead())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Don't do anything if player is confused
		if (activeChar.isOutOfControl())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		L2Effect ef = null;
		if (((ef = activeChar.getFirstEffect(L2EffectType.ACTION_BLOCK)) != null) && !ef.checkCondition(_actionId))
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_SO_ACTIONS_NOT_ALLOWED);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Don't allow to do some action if player is transformed
		if (activeChar.isTransformed())
		{
			int[] allowedActions = activeChar.isTransformed() ? ExBasicActionList.ACTIONS_ON_TRANSFORM : ExBasicActionList.DEFAULT_ACTION_LIST;
			if (!(Arrays.binarySearch(allowedActions, _actionId) >= 0))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				_log.warn("Player " + activeChar + " used action which he does not have! Id = " + _actionId + " transform: " + activeChar.getTransformation());
				return;
			}
		}
		
		final L2Summon summon = activeChar.getSummon();
		final L2Object target = activeChar.getTarget();
		switch (_actionId)
		{
			case 0: // Sit/Stand
				if (activeChar.isSitting() || !activeChar.isMoving())
				{
					useSit(activeChar, target);
				}
				else
				{
					// Sit when arrive using next action.
					// Creating next action class.
					final NextAction nextAction = new NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.AI_INTENTION_MOVE_TO, (NextActionCallback) () -> useSit(activeChar, target));
					
					// Binding next action to AI.
					activeChar.getAI().setNextAction(nextAction);
				}
				break;
			case 1: // Walk/Run
				if (activeChar.isRunning())
				{
					activeChar.setWalking();
				}
				else
				{
					activeChar.setRunning();
				}
				break;
			case 10: // Private Store - Sell
				activeChar.tryOpenPrivateSellStore(false);
				break;
			case 15: // Change Movement Mode (Pets)
				if (validateSummon(summon, true))
				{
					((L2SummonAI) summon.getAI()).notifyFollowStatusChange();
				}
				break;
			case 16: // Attack (Pets)
				if (validateSummon(summon, true))
				{
					if (summon.canAttack(_ctrlPressed))
					{
						summon.doAttack();
					}
				}
				break;
			case 17: // Stop (Pets)
				if (validateSummon(summon, true))
				{
					summon.cancelAction();
				}
				break;
			case 19: // Unsummon Pet
				if (summon.isDead())
				{
					sendPacket(SystemMessageId.DEAD_PET_CANNOT_BE_RETURNED);
					break;
				}
				
				if (summon.isAttackingNow() || summon.isInCombat() || summon.isMovementDisabled())
				{
					sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
					break;
				}
				
				if (summon.isHungry())
				{
					if (summon.isPet() && !((L2PetInstance) summon).getPetData().getFood().isEmpty())
					{
						sendPacket(SystemMessageId.YOU_CANNOT_RESTORE_HUNGRY_PETS);
					}
					else
					{
						sendPacket(SystemMessageId.THE_HELPER_PET_CANNOT_BE_RETURNED);
					}
					break;
				}
				
				if (!validateSummon(summon, true))
				{
					break;
				}
				
				summon.unSummon(activeChar);
				break;
			case 21: // Change Movement Mode (Servitors)
				if (validateSummon(summon, false))
				{
					((L2SummonAI) summon.getAI()).notifyFollowStatusChange();
				}
				break;
			case 22: // Attack (Servitors)
				if (validateSummon(summon, false))
				{
					if (summon.canAttack(_ctrlPressed))
					{
						summon.doAttack();
					}
				}
				break;
			case 23: // Stop (Servitors)
				if (validateSummon(summon, false))
				{
					summon.cancelAction();
				}
				break;
			case 28: // Private Store - Buy
				activeChar.tryOpenPrivateBuyStore();
				break;
			case 32: // Wild Hog Cannon - Wild Cannon
				useSkill(4230, false);
				break;
			case 36: // Soulless - Toxic Smoke
				useSkill(4259, false);
				break;
			case 37: // Dwarven Manufacture
				if (activeChar.isAlikeDead())
				{
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (activeChar.getPrivateStoreType() != PrivateStoreType.NONE)
				{
					activeChar.setPrivateStoreType(PrivateStoreType.NONE);
					activeChar.broadcastUserInfo();
				}
				if (activeChar.isSitting())
				{
					activeChar.standUp();
				}
				
				sendPacket(new RecipeShopManageList(activeChar, true));
				break;
			case 38: // Mount/Dismount
				activeChar.mountPlayer(summon);
				break;
			case 39: // Soulless - Parasite Burst
				useSkill(4138, false);
				break;
			case 41: // Wild Hog Cannon - Attack
				if (validateSummon(summon, false))
				{
					if ((target != null) && (target.isDoor() || (target instanceof L2SiegeFlagInstance)))
					{
						useSkill(4230, false);
					}
					else
					{
						sendPacket(SystemMessageId.INCORRECT_TARGET);
					}
				}
				break;
			case 42: // Kai the Cat - Self Damage Shield
				useSkill(4378, activeChar, false);
				break;
			case 43: // Unicorn Merrow - Hydro Screw
				useSkill(4137, false);
				break;
			case 44: // Big Boom - Boom Attack
				useSkill(4139, false);
				break;
			case 45: // Unicorn Boxer - Master Recharge
				useSkill(4025, activeChar, false);
				break;
			case 46: // Mew the Cat - Mega Storm Strike
				useSkill(4261, false);
				break;
			case 47: // Silhouette - Steal Blood
				useSkill(4260, false);
				break;
			case 48: // Mechanic Golem - Mech. Cannon
				useSkill(4068, false);
				break;
			case 51: // General Manufacture
				// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
				if (activeChar.isAlikeDead())
				{
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (activeChar.getPrivateStoreType() != PrivateStoreType.NONE)
				{
					activeChar.setPrivateStoreType(PrivateStoreType.NONE);
					activeChar.broadcastUserInfo();
				}
				if (activeChar.isSitting())
				{
					activeChar.standUp();
				}
				
				sendPacket(new RecipeShopManageList(activeChar, false));
				break;
			case 52: // Unsummon Servitor
				if (validateSummon(summon, false))
				{
					if (summon.isAttackingNow() || summon.isInCombat())
					{
						sendPacket(SystemMessageId.SERVITOR_NOT_RETURN_IN_BATTLE);
						break;
					}
					summon.unSummon(activeChar);
				}
				break;
			case 53: // Move to target (Servitors)
				if (validateSummon(summon, false))
				{
					if ((target != null) && (summon != target) && !summon.isMovementDisabled())
					{
						summon.setFollowStatus(false);
						summon.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(target.getX(), target.getY(), target.getZ(), 0));
					}
				}
				break;
			case 54: // Move to target (Pets)
				if (validateSummon(summon, true))
				{
					if ((target != null) && (summon != target) && !summon.isMovementDisabled())
					{
						summon.setFollowStatus(false);
						summon.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(target.getX(), target.getY(), target.getZ(), 0));
					}
				}
				break;
			case 61: // Private Store Package Sell
				activeChar.tryOpenPrivateSellStore(true);
				break;
			case 65: // Bot Report Button
				if (Config.BOTREPORT_ENABLE)
				{
					BotReportTable.getInstance().reportBot(activeChar);
				}
				else
				{
					activeChar.sendMessage("This feature is disabled.");
				}
				break;
			case 67: // Steer
				if (activeChar.isInAirShip())
				{
					if (activeChar.getAirShip().setCaptain(activeChar))
					{
						activeChar.broadcastUserInfo();
					}
				}
				break;
			case 68: // Cancel Control
				if (activeChar.isInAirShip() && activeChar.getAirShip().isCaptain(activeChar))
				{
					if (activeChar.getAirShip().setCaptain(null))
					{
						activeChar.broadcastUserInfo();
					}
				}
				break;
			case 69: // Destination Map
				AirShipManager.getInstance().sendAirShipTeleportList(activeChar);
				break;
			case 70: // Exit Airship
				if (activeChar.isInAirShip())
				{
					if (activeChar.getAirShip().isCaptain(activeChar))
					{
						if (activeChar.getAirShip().setCaptain(null))
						{
							activeChar.broadcastUserInfo();
						}
					}
					else if (activeChar.getAirShip().isInDock())
					{
						activeChar.getAirShip().oustPlayer(activeChar);
					}
				}
				break;
			case 71:
			case 72:
			case 73:
				useCoupleSocial(_actionId - 55);
				break;
			case 1000: // Siege Golem - Siege Hammer
				if ((target != null) && target.isDoor())
				{
					useSkill(4079, false);
				}
				break;
			case 1001: // Sin Eater - Ultimate Bombastic Buster
				if (validateSummon(summon, true) && (summon.getId() == SIN_EATER_ID))
				{
					summon.broadcastPacket(new NpcSay(summon.getObjectId(), Say2.NPC_ALL, summon.getId(), NPC_STRINGS[Rnd.get(NPC_STRINGS.length)]));
				}
				break;
			case 1003: // Wind Hatchling/Strider - Wild Stun
				useSkill(4710, true);
				break;
			case 1004: // Wind Hatchling/Strider - Wild Defense
				useSkill(4711, activeChar, true);
				break;
			case 1005: // Star Hatchling/Strider - Bright Burst
				useSkill(4712, true);
				break;
			case 1006: // Star Hatchling/Strider - Bright Heal
				useSkill(4713, activeChar, true);
				break;
			case 1007: // Cat Queen - Blessing of Queen
				useSkill(4699, activeChar, false);
				break;
			case 1008: // Cat Queen - Gift of Queen
				useSkill(4700, activeChar, false);
				break;
			case 1009: // Cat Queen - Cure of Queen
				useSkill(4701, false);
				break;
			case 1010: // Unicorn Seraphim - Blessing of Seraphim
				useSkill(4702, activeChar, false);
				break;
			case 1011: // Unicorn Seraphim - Gift of Seraphim
				useSkill(4703, activeChar, false);
				break;
			case 1012: // Unicorn Seraphim - Cure of Seraphim
				useSkill(4704, false);
				break;
			case 1013: // Nightshade - Curse of Shade
				useSkill(4705, false);
				break;
			case 1014: // Nightshade - Mass Curse of Shade
				useSkill(4706, false);
				break;
			case 1015: // Nightshade - Shade Sacrifice
				useSkill(4707, false);
				break;
			case 1016: // Cursed Man - Cursed Blow
				useSkill(4709, false);
				break;
			case 1017: // Cursed Man - Cursed Strike/Stun
				useSkill(4708, false);
				break;
			case 1031: // Feline King - Slash
				useSkill(5135, false);
				break;
			case 1032: // Feline King - Spinning Slash
				useSkill(5136, false);
				break;
			case 1033: // Feline King - Grip of the Cat
				useSkill(5137, false);
				break;
			case 1034: // Magnus the Unicorn - Whiplash
				useSkill(5138, false);
				break;
			case 1035: // Magnus the Unicorn - Tridal Wave
				useSkill(5139, false);
				break;
			case 1036: // Spectral Lord - Corpse Kaboom
				useSkill(5142, false);
				break;
			case 1037: // Spectral Lord - Dicing Death
				useSkill(5141, false);
				break;
			case 1038: // Spectral Lord - Force Curse
				useSkill(5140, false);
				break;
			case 1039: // Swoop Cannon - Cannon Fodder
				if ((target != null) && target.isDoor())
				{
					useSkill(5110, false);
				}
				break;
			case 1040: // Swoop Cannon - Big Bang
				if ((target != null) && target.isDoor())
				{
					useSkill(5111, false);
				}
				break;
			case 1041: // Great Wolf - Bite Attack
				useSkill(5442, true);
				break;
			case 1042: // Great Wolf - Maul
				useSkill(5444, true);
				break;
			case 1043: // Great Wolf - Cry of the Wolf
				useSkill(5443, true);
				break;
			case 1044: // Great Wolf - Awakening
				useSkill(5445, true);
				break;
			case 1045: // Great Wolf - Howl
				useSkill(5584, true);
				break;
			case 1046: // Strider - Roar
				useSkill(5585, true);
				break;
			case 1047: // Divine Beast - Bite
				useSkill(5580, false);
				break;
			case 1048: // Divine Beast - Stun Attack
				useSkill(5581, false);
				break;
			case 1049: // Divine Beast - Fire Breath
				useSkill(5582, false);
				break;
			case 1050: // Divine Beast - Roar
				useSkill(5583, false);
				break;
			case 1051: // Feline Queen - Bless The Body
				useSkill(5638, false);
				break;
			case 1052: // Feline Queen - Bless The Soul
				useSkill(5639, false);
				break;
			case 1053: // Feline Queen - Haste
				useSkill(5640, false);
				break;
			case 1054: // Unicorn Seraphim - Acumen
				useSkill(5643, false);
				break;
			case 1055: // Unicorn Seraphim - Clarity
				useSkill(5647, false);
				break;
			case 1056: // Unicorn Seraphim - Empower
				useSkill(5648, false);
				break;
			case 1057: // Unicorn Seraphim - Wild Magic
				useSkill(5646, false);
				break;
			case 1058: // Nightshade - Death Whisper
				useSkill(5652, false);
				break;
			case 1059: // Nightshade - Focus
				useSkill(5653, false);
				break;
			case 1060: // Nightshade - Guidance
				useSkill(5654, false);
				break;
			case 1061: // Wild Beast Fighter, White Weasel - Death blow
				useSkill(5745, true);
				break;
			case 1062: // Wild Beast Fighter - Double attack
				useSkill(5746, true);
				break;
			case 1063: // Wild Beast Fighter - Spin attack
				useSkill(5747, true);
				break;
			case 1064: // Wild Beast Fighter - Meteor Shower
				useSkill(5748, true);
				break;
			case 1065: // Fox Shaman, Wild Beast Fighter, White Weasel, Fairy Princess - Awakening
				useSkill(5753, true);
				break;
			case 1066: // Fox Shaman, Spirit Shaman - Thunder Bolt
				useSkill(5749, true);
				break;
			case 1067: // Fox Shaman, Spirit Shaman - Flash
				useSkill(5750, true);
				break;
			case 1068: // Fox Shaman, Spirit Shaman - Lightning Wave
				useSkill(5751, true);
				break;
			case 1069: // Fox Shaman, Fairy Princess - Flare
				useSkill(5752, true);
				break;
			case 1070: // White Weasel, Fairy Princess, Improved Baby Buffalo, Improved Baby Kookaburra, Improved Baby Cougar, Spirit Shaman, Toy Knight, Turtle Ascetic - Buff control
				useSkill(5771, true);
				break;
			case 1071: // Tigress - Power Strike
				useSkill(5761, true);
				break;
			case 1072: // Toy Knight - Piercing attack
				useSkill(6046, true);
				break;
			case 1073: // Toy Knight - Whirlwind
				useSkill(6047, true);
				break;
			case 1074: // Toy Knight - Lance Smash
				useSkill(6048, true);
				break;
			case 1075: // Toy Knight - Battle Cry
				useSkill(6049, true);
				break;
			case 1076: // Turtle Ascetic - Power Smash
				useSkill(6050, true);
				break;
			case 1077: // Turtle Ascetic - Energy Burst
				useSkill(6051, true);
				break;
			case 1078: // Turtle Ascetic - Shockwave
				useSkill(6052, true);
				break;
			case 1079: // Turtle Ascetic - Howl
				useSkill(6053, true);
				break;
			case 1080: // Phoenix Rush
				useSkill(6041, false);
				break;
			case 1081: // Phoenix Cleanse
				useSkill(6042, false);
				break;
			case 1082: // Phoenix Flame Feather
				useSkill(6043, false);
				break;
			case 1083: // Phoenix Flame Beak
				useSkill(6044, false);
				break;
			case 1084: // Switch State
				if (summon instanceof L2BabyPetInstance)
				{
					useSkill(6054, true);
				}
				break;
			case 1086: // Panther Cancel
				useSkill(6094, false);
				break;
			case 1087: // Panther Dark Claw
				useSkill(6095, false);
				break;
			case 1088: // Panther Fatal Claw
				useSkill(6096, false);
				break;
			case 1089: // Deinonychus - Tail Strike
				useSkill(6199, true);
				break;
			case 1090: // Guardian's Strider - Strider Bite
				useSkill(6205, true);
				break;
			case 1091: // Guardian's Strider - Strider Fear
				useSkill(6206, true);
				break;
			case 1092: // Guardian's Strider - Strider Dash
				useSkill(6207, true);
				break;
			case 1093: // Maguen - Maguen Strike
				useSkill(6618, true);
				break;
			case 1094: // Maguen - Maguen Wind Walk
				useSkill(6681, true);
				break;
			case 1095: // Elite Maguen - Maguen Power Strike
				useSkill(6619, true);
				break;
			case 1096: // Elite Maguen - Elite Maguen Wind Walk
				useSkill(6682, true);
				break;
			case 1097: // Maguen - Maguen Return
				useSkill(6683, true);
				break;
			case 1098: // Elite Maguen - Maguen Party Return
				useSkill(6684, true);
				break;
			case 5000: // Baby Rudolph - Reindeer Scratch
				useSkill(23155, true);
				break;
			case 5001: // Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum - Rosy Seduction
				useSkill(23167, true);
				break;
			case 5002: // Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum - Critical Seduction
				useSkill(23168, true);
				break;
			case 5003: // Hyum, Lapham, Hyum, Lapham - Thunder Bolt
				useSkill(5749, true);
				break;
			case 5004: // Hyum, Lapham, Hyum, Lapham - Flash
				useSkill(5750, true);
				break;
			case 5005: // Hyum, Lapham, Hyum, Lapham - Lightning Wave
				useSkill(5751, true);
				break;
			case 5006: // Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum, Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum - Buff Control
				useSkill(5771, true);
				break;
			case 5007: // Deseloph, Lilias, Deseloph, Lilias - Piercing Attack
				useSkill(6046, true);
				break;
			case 5008: // Deseloph, Lilias, Deseloph, Lilias - Spin Attack
				useSkill(6047, true);
				break;
			case 5009: // Deseloph, Lilias, Deseloph, Lilias - Smash
				useSkill(6048, true);
				break;
			case 5010: // Deseloph, Lilias, Deseloph, Lilias - Ignite
				useSkill(6049, true);
				break;
			case 5011: // Rekang, Mafum, Rekang, Mafum - Power Smash
				useSkill(6050, true);
				break;
			case 5012: // Rekang, Mafum, Rekang, Mafum - Energy Burst
				useSkill(6051, true);
				break;
			case 5013: // Rekang, Mafum, Rekang, Mafum - Shockwave
				useSkill(6052, true);
				break;
			case 5014: // Rekang, Mafum, Rekang, Mafum - Ignite
				useSkill(6053, true);
				break;
			case 5015: // Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum, Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum - Switch Stance
				useSkill(6054, true);
				break;
			// Social Packets
			case 12: // Greeting
				tryBroadcastSocial(2);
				break;
			case 13: // Victory
				tryBroadcastSocial(3);
				break;
			case 14: // Advance
				tryBroadcastSocial(4);
				break;
			case 24: // Yes
				tryBroadcastSocial(6);
				break;
			case 25: // No
				tryBroadcastSocial(5);
				break;
			case 26: // Bow
				tryBroadcastSocial(7);
				break;
			case 29: // Unaware
				tryBroadcastSocial(8);
				break;
			case 30: // Social Waiting
				tryBroadcastSocial(9);
				break;
			case 31: // Laugh
				tryBroadcastSocial(10);
				break;
			case 33: // Applaud
				tryBroadcastSocial(11);
				break;
			case 34: // Dance
				tryBroadcastSocial(12);
				break;
			case 35: // Sorrow
				tryBroadcastSocial(13);
				break;
			case 62: // Charm
				tryBroadcastSocial(14);
				break;
			case 66: // Shyness
				tryBroadcastSocial(15);
				break;
			default:
				_log.warn(activeChar.getName() + ": unhandled action type " + _actionId);
				break;
		}
	}
	
	/**
	 * Use the sit action.
	 * @param activeChar the player trying to sit
	 * @param target the target to sit, throne, bench or chair
	 * @return {@code true} if the player can sit, {@code false} otherwise
	 */
	protected boolean useSit(L2PcInstance activeChar, L2Object target)
	{
		if (activeChar.getMountType() != MountType.NONE)
		{
			return false;
		}
		
		if (!activeChar.isSitting() && (target instanceof L2StaticObjectInstance) && (((L2StaticObjectInstance) target).getType() == 1) && activeChar.isInsideRadius(target, L2StaticObjectInstance.INTERACTION_DISTANCE, false, false))
		{
			final ChairSit cs = new ChairSit(activeChar, ((L2StaticObjectInstance) target).getId());
			sendPacket(cs);
			activeChar.sitDown();
			activeChar.broadcastPacket(cs);
			return true;
		}
		
		if (activeChar.isSitting())
		{
			activeChar.standUp();
		}
		else
		{
			activeChar.sitDown();
		}
		return true;
	}
	
	/**
	 * Cast a skill for active summon.<br>
	 * Target is specified as a parameter but can be overwrited or ignored depending on skill type.
	 * @param skillId the skill Id to be casted by the summon
	 * @param target the target to cast the skill on, overwritten or ignored depending on skill type
	 * @param pet if {@code true} it'll validate a pet, if {@code false} it will validate a servitor
	 */
	private void useSkill(int skillId, L2Object target, boolean pet)
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final L2Summon summon = activeChar.getSummon();
		if (!validateSummon(summon, pet))
		{
			return;
		}
		
		if (summon instanceof L2BabyPetInstance)
		{
			if (!((L2BabyPetInstance) summon).isInSupportMode())
			{
				sendPacket(SystemMessageId.PET_AUXILIARY_MODE_CANNOT_USE_SKILLS);
				return;
			}
		}
		
		int lvl = 0;
		if (summon.isPet())
		{
			if ((summon.getLevel() - activeChar.getLevel()) > 20)
			{
				sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL);
				return;
			}
			lvl = PetData.getInstance().getPetData(summon.getId()).getAvailableLevel(skillId, summon.getLevel());
		}
		else
		{
			lvl = SummonSkillsTable.getInstance().getAvailableLevel(summon, skillId);
		}
		
		if (lvl > 0)
		{
			summon.setTarget(target);
			summon.useMagic(SkillData.getInstance().getInfo(skillId, lvl), _ctrlPressed, _shiftPressed);
		}
		
		if (skillId == SWITCH_STANCE_ID)
		{
			summon.switchMode();
		}
	}
	
	/**
	 * Cast a skill for active summon.<br>
	 * Target is retrieved from owner's target, then validated by overloaded method useSkill(int, L2Character).
	 * @param skillId the skill Id to use
	 * @param pet if {@code true} it'll validate a pet, if {@code false} it will validate a servitor
	 */
	private void useSkill(int skillId, boolean pet)
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		useSkill(skillId, activeChar.getTarget(), pet);
	}
	
	/**
	 * Validates the given summon and sends a system message to the master.
	 * @param summon the summon to validate
	 * @param checkPet if {@code true} it'll validate a pet, if {@code false} it will validate a servitor
	 * @return {@code true} if the summon is not null and whether is a pet or a servitor depending on {@code checkPet} value, {@code false} otherwise
	 */
	private boolean validateSummon(L2Summon summon, boolean checkPet)
	{
		if ((summon != null) && ((checkPet && summon.isPet()) || summon.isServitor()))
		{
			if (summon.isPet() && ((L2PetInstance) summon).isUncontrollable())
			{
				sendPacket(SystemMessageId.WHEN_YOUR_PETS_HUNGER_GAUGE_IS_AT_0_YOU_CANNOT_USE_YOUR_PET);
				return false;
			}
			
			if (summon.isBetrayed())
			{
				sendPacket(SystemMessageId.PET_REFUSING_ORDER);
				return false;
			}
			return true;
		}
		
		if (checkPet)
		{
			sendPacket(SystemMessageId.DONT_HAVE_PET);
		}
		else
		{
			sendPacket(SystemMessageId.DONT_HAVE_SERVITOR);
		}
		return false;
	}
	
	/**
	 * Try to broadcast SocialAction packet.
	 * @param id the social action Id to broadcast
	 */
	private void tryBroadcastSocial(int id)
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		if (activeChar.isFishing())
		{
			sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}
		
		if (activeChar.canMakeSocialAction())
		{
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), id));
		}
	}
	
	/**
	 * Perform a couple social action.
	 * @param id the couple social action Id
	 */
	private void useCoupleSocial(final int id)
	{
		final L2PcInstance requester = getActiveChar();
		if (requester == null)
		{
			return;
		}
		
		final L2Object target = requester.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		final int distance = (int) Math.sqrt(requester.getPlanDistanceSq(target));
		if ((distance > 125) || (distance < 15) || (requester.getObjectId() == target.getObjectId()))
		{
			sendPacket(SystemMessageId.TARGET_DO_NOT_MEET_LOC_REQUIREMENTS);
			return;
		}
		
		SystemMessage sm;
		if (requester.isInStoreMode() || requester.isInCraftMode())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_PRIVATE_SHOP_MODE_OR_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isInCombat() || requester.isInDuel() || AttackStanceTaskManager.getInstance().hasAttackStanceTask(requester))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isFishing())
		{
			sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}
		
		if (requester.getKarma() > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CHAOTIC_STATE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isInOlympiadMode())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isInSiege())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CASTLE_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isInHideoutSiege())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_A_HIDEOUT_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
		}
		
		if (requester.isMounted() || requester.isFlyingMounted() || requester.isInBoat() || requester.isInAirShip())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_RIDING_A_SHIP_STEED_OR_STRIDER_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isTransformed())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TRANSFORMING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isAlikeDead())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_DEAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		// Checks for partner.
		final L2PcInstance partner = target.getActingPlayer();
		if (partner.isInStoreMode() || partner.isInCraftMode())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_PRIVATE_SHOP_MODE_OR_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isInCombat() || partner.isInDuel() || AttackStanceTaskManager.getInstance().hasAttackStanceTask(partner))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.getMultiSociaAction() > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_PARTICIPATING_IN_A_COUPLE_ACTION_AND_CANNOT_BE_REQUESTED_FOR_ANOTHER_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isFishing())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_FISHING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.getKarma() > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CHAOTIC_STATE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isInOlympiadMode())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isInHideoutSiege())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_A_HIDEOUT_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isInSiege())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CASTLE_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isMounted() || partner.isFlyingMounted() || partner.isInBoat() || partner.isInAirShip())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_RIDING_A_SHIP_STEED_OR_STRIDER_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isTeleporting())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TELEPORTING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isTransformed())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TRANSFORMING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isAlikeDead())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_DEAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (requester.isAllSkillsDisabled() || partner.isAllSkillsDisabled())
		{
			sendPacket(SystemMessageId.COUPLE_ACTION_CANCELED);
			return;
		}
		
		requester.setMultiSocialAction(id, partner.getObjectId());
		sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_REQUESTED_COUPLE_ACTION_C1);
		sm.addPcName(partner);
		sendPacket(sm);
		
		if ((requester.getAI().getIntention() != CtrlIntention.AI_INTENTION_IDLE) || (partner.getAI().getIntention() != CtrlIntention.AI_INTENTION_IDLE))
		{
			final NextAction nextAction = new NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.AI_INTENTION_MOVE_TO, (NextActionCallback) () -> partner.sendPacket(new ExAskCoupleAction(requester.getObjectId(), id)));
			requester.getAI().setNextAction(nextAction);
			return;
		}
		
		if (requester.isCastingNow() || requester.isCastingSimultaneouslyNow())
		{
			final NextAction nextAction = new NextAction(CtrlEvent.EVT_FINISH_CASTING, CtrlIntention.AI_INTENTION_CAST, (NextActionCallback) () -> partner.sendPacket(new ExAskCoupleAction(requester.getObjectId(), id)));
			requester.getAI().setNextAction(nextAction);
			return;
		}
		
		partner.sendPacket(new ExAskCoupleAction(requester.getObjectId(), id));
	}
	
	@Override
	public String getType()
	{
		return _C__56_REQUESTACTIONUSE;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return (_actionId != 10) && (_actionId != 28);
	}
}
