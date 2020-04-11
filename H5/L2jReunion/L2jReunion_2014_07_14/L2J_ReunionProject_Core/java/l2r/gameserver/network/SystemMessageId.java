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
package l2r.gameserver.network;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import l2r.Config;
import l2r.gameserver.model.clientstrings.Builder;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Noctarius, Nille02, crion, Forsaiken
 */
public final class SystemMessageId
{
	private static final Logger _log = LoggerFactory.getLogger(SystemMessageId.class);
	private static final SMLocalisation[] EMPTY_SML_ARRAY = new SMLocalisation[0];
	public static final SystemMessageId[] EMPTY_ARRAY = new SystemMessageId[0];
	
	/**
	 * ID: 0<br>
	 * Message: You have been disconnected from the server.
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_DISCONNECTED;
	
	/**
	 * ID: 1<br>
	 * Message: The server will be coming down in $1 seconds. Please find a safe place to log out.
	 */
	public static final SystemMessageId THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS;
	
	/**
	 * ID: 2<br>
	 * Message: $s1 does not exist.
	 */
	public static final SystemMessageId S1_DOES_NOT_EXIST;
	
	/**
	 * ID: 3<br>
	 * Message: $s1 is not currently logged in.
	 */
	public static final SystemMessageId S1_IS_NOT_ONLINE;
	
	/**
	 * ID: 4<br>
	 * Message: You cannot ask yourself to apply to a clan.
	 */
	public static final SystemMessageId CANNOT_INVITE_YOURSELF;
	
	/**
	 * ID: 5<br>
	 * Message: $s1 already exists.
	 */
	public static final SystemMessageId S1_ALREADY_EXISTS;
	
	/**
	 * ID: 6<br>
	 * Message: $s1 does not exist
	 */
	public static final SystemMessageId S1_DOES_NOT_EXIST2;
	
	/**
	 * ID: 7<br>
	 * Message: You are already a member of $s1.
	 */
	public static final SystemMessageId ALREADY_MEMBER_OF_S1;
	
	/**
	 * ID: 8<br>
	 * Message: You are working with another clan.
	 */
	public static final SystemMessageId YOU_ARE_WORKING_WITH_ANOTHER_CLAN;
	
	/**
	 * ID: 9<br>
	 * Message: $s1 is not a clan leader.
	 */
	public static final SystemMessageId S1_IS_NOT_A_CLAN_LEADER;
	
	/**
	 * ID: 10<br>
	 * Message: $s1 is working with another clan.
	 */
	public static final SystemMessageId S1_WORKING_WITH_ANOTHER_CLAN;
	
	/**
	 * ID: 11<br>
	 * Message: There are no applicants for this clan.
	 */
	public static final SystemMessageId NO_APPLICANTS_FOR_THIS_CLAN;
	
	/**
	 * ID: 12<br>
	 * Message: The applicant information is incorrect.
	 */
	public static final SystemMessageId APPLICANT_INFORMATION_INCORRECT;
	
	/**
	 * ID: 13<br>
	 * Message: Unable to disperse: your clan has requested to participate in a castle siege.
	 */
	public static final SystemMessageId CANNOT_DISSOLVE_CAUSE_CLAN_WILL_PARTICIPATE_IN_CASTLE_SIEGE;
	
	/**
	 * ID: 14<br>
	 * Message: Unable to disperse: your clan owns one or more castles or hideouts.
	 */
	public static final SystemMessageId CANNOT_DISSOLVE_CAUSE_CLAN_OWNS_CASTLES_HIDEOUTS;
	
	/**
	 * ID: 15<br>
	 * Message: You are in siege.
	 */
	public static final SystemMessageId YOU_ARE_IN_SIEGE;
	
	/**
	 * ID: 16<br>
	 * Message: You are not in siege.
	 */
	public static final SystemMessageId YOU_ARE_NOT_IN_SIEGE;
	
	/**
	 * ID: 17<br>
	 * Message: The castle siege has begun.
	 */
	public static final SystemMessageId CASTLE_SIEGE_HAS_BEGUN;
	
	/**
	 * ID: 18<br>
	 * Message: The castle siege has ended.
	 */
	public static final SystemMessageId CASTLE_SIEGE_HAS_ENDED;
	
	/**
	 * ID: 19<br>
	 * Message: There is a new Lord of the castle!
	 */
	public static final SystemMessageId NEW_CASTLE_LORD;
	
	/**
	 * ID: 20<br>
	 * Message: The gate is being opened.
	 */
	public static final SystemMessageId GATE_IS_OPENING;
	
	/**
	 * ID: 21<br>
	 * Message: The gate is being destroyed.
	 */
	public static final SystemMessageId GATE_IS_DESTROYED;
	
	/**
	 * ID: 22<br>
	 * Message: Your target is out of range.
	 */
	public static final SystemMessageId TARGET_TOO_FAR;
	
	/**
	 * ID: 23<br>
	 * Message: Not enough HP.
	 */
	public static final SystemMessageId NOT_ENOUGH_HP;
	
	/**
	 * ID: 24<br>
	 * Message: Not enough MP.
	 */
	public static final SystemMessageId NOT_ENOUGH_MP;
	
	/**
	 * ID: 25<br>
	 * Message: Rejuvenating HP.
	 */
	public static final SystemMessageId REJUVENATING_HP;
	
	/**
	 * ID: 26<br>
	 * Message: Rejuvenating MP.
	 */
	public static final SystemMessageId REJUVENATING_MP;
	
	/**
	 * ID: 27<br>
	 * Message: Your casting has been interrupted.
	 */
	public static final SystemMessageId CASTING_INTERRUPTED;
	
	/**
	 * ID: 28<br>
	 * Message: You have obtained $s1 adena.
	 */
	public static final SystemMessageId YOU_PICKED_UP_S1_ADENA;
	
	/**
	 * ID: 29<br>
	 * Message: You have obtained $s2 $s1.
	 */
	public static final SystemMessageId YOU_PICKED_UP_S1_S2;
	
	/**
	 * ID: 30<br>
	 * Message: You have obtained $s1.
	 */
	public static final SystemMessageId YOU_PICKED_UP_S1;
	
	/**
	 * ID: 31<br>
	 * Message: You cannot move while sitting.
	 */
	public static final SystemMessageId CANT_MOVE_SITTING;
	
	/**
	 * ID: 32<br>
	 * Message: You are unable to engage in combat. Please go to the nearest restart point.
	 */
	public static final SystemMessageId UNABLE_COMBAT_PLEASE_GO_RESTART;
	
	/**
	 * ID: 33<br>
	 * Message: You cannot move while casting.
	 */
	public static final SystemMessageId CANT_MOVE_CASTING;
	
	/**
	 * ID: 34<br>
	 * Message: Welcome to the World of Lineage II.
	 */
	public static final SystemMessageId WELCOME_TO_LINEAGE;
	
	/**
	 * ID: 35<br>
	 * Message: You hit for $s1 damage
	 */
	public static final SystemMessageId YOU_DID_S1_DMG;
	
	/**
	 * ID: 36<br>
	 * Message: $c1 hit you for $s2 damage.
	 */
	public static final SystemMessageId C1_GAVE_YOU_S2_DMG;
	
	/**
	 * ID: 37<br>
	 * Message: $c1 hit you for $s2 damage.
	 */
	public static final SystemMessageId C1_GAVE_YOU_S2_DMG2;
	
	/**
	 * ID: 41<br>
	 * Message: You carefully nock an arrow.
	 */
	public static final SystemMessageId GETTING_READY_TO_SHOOT_AN_ARROW;
	
	/**
	 * ID: 42<br>
	 * Message: You have avoided $c1's attack.
	 */
	public static final SystemMessageId AVOIDED_C1_ATTACK;
	
	/**
	 * ID: 43<br>
	 * Message: You have missed.
	 */
	public static final SystemMessageId MISSED_TARGET;
	
	/**
	 * ID: 44<br>
	 * Message: Critical hit!
	 */
	public static final SystemMessageId CRITICAL_HIT;
	
	/**
	 * ID: 45<br>
	 * Message: You have earned $s1 experience.
	 */
	public static final SystemMessageId EARNED_S1_EXPERIENCE;
	
	/**
	 * ID: 46<br>
	 * Message: You use $s1.
	 */
	public static final SystemMessageId USE_S1;
	
	/**
	 * ID: 47<br>
	 * Message: You begin to use a(n) $s1.
	 */
	public static final SystemMessageId BEGIN_TO_USE_S1;
	
	/**
	 * ID: 48<br>
	 * Message: $s1 is not available at this time: being prepared for reuse.
	 */
	public static final SystemMessageId S1_PREPARED_FOR_REUSE;
	
	/**
	 * ID: 49<br>
	 * Message: You have equipped your $s1.
	 */
	public static final SystemMessageId S1_EQUIPPED;
	
	/**
	 * ID: 50<br>
	 * Message: Your target cannot be found.
	 */
	public static final SystemMessageId TARGET_CANT_FOUND;
	
	/**
	 * ID: 51<br>
	 * Message: You cannot use this on yourself.
	 */
	public static final SystemMessageId CANNOT_USE_ON_YOURSELF;
	
	/**
	 * ID: 52<br>
	 * Message: You have earned $s1 adena.
	 */
	public static final SystemMessageId EARNED_S1_ADENA;
	
	/**
	 * ID: 53<br>
	 * Message: You have earned $s2 $s1(s).
	 */
	public static final SystemMessageId EARNED_S2_S1_S;
	
	/**
	 * ID: 54<br>
	 * Message: You have earned $s1.
	 */
	public static final SystemMessageId EARNED_ITEM_S1;
	
	/**
	 * ID: 55<br>
	 * Message: You have failed to pick up $s1 adena.
	 */
	public static final SystemMessageId FAILED_TO_PICKUP_S1_ADENA;
	
	/**
	 * ID: 56<br>
	 * Message: You have failed to pick up $s1.
	 */
	public static final SystemMessageId FAILED_TO_PICKUP_S1;
	
	/**
	 * ID: 57<br>
	 * Message: You have failed to pick up $s2 $s1(s).
	 */
	public static final SystemMessageId FAILED_TO_PICKUP_S2_S1_S;
	
	/**
	 * ID: 58<br>
	 * Message: You have failed to earn $s1 adena.
	 */
	public static final SystemMessageId FAILED_TO_EARN_S1_ADENA;
	
	/**
	 * ID: 59<br>
	 * Message: You have failed to earn $s1.
	 */
	public static final SystemMessageId FAILED_TO_EARN_S1;
	
	/**
	 * ID: 60<br>
	 * Message: You have failed to earn $s2 $s1(s).
	 */
	public static final SystemMessageId FAILED_TO_EARN_S2_S1_S;
	
	/**
	 * ID: 61<br>
	 * Message: Nothing happened.
	 */
	public static final SystemMessageId NOTHING_HAPPENED;
	
	/**
	 * ID: 62<br>
	 * Message: Your $s1 has been successfully enchanted.
	 */
	public static final SystemMessageId S1_SUCCESSFULLY_ENCHANTED;
	
	/**
	 * ID: 63<br>
	 * Message: Your +$S1 $S2 has been successfully enchanted.
	 */
	public static final SystemMessageId S1_S2_SUCCESSFULLY_ENCHANTED;
	
	/**
	 * ID: 64<br>
	 * Message: The enchantment has failed! Your $s1 has been crystallized.
	 */
	public static final SystemMessageId ENCHANTMENT_FAILED_S1_EVAPORATED;
	
	/**
	 * ID: 65<br>
	 * Message: The enchantment has failed! Your +$s1 $s2 has been crystallized.
	 */
	public static final SystemMessageId ENCHANTMENT_FAILED_S1_S2_EVAPORATED;
	
	/**
	 * ID: 66<br>
	 * Message: $c1 is inviting you to join a party. Do you accept?
	 */
	public static final SystemMessageId C1_INVITED_YOU_TO_PARTY;
	
	/**
	 * ID: 67<br>
	 * Message: $s1 has invited you to the join the clan, $s2. Do you wish to join?
	 */
	public static final SystemMessageId S1_HAS_INVITED_YOU_TO_JOIN_THE_CLAN_S2;
	
	/**
	 * ID: 68<br>
	 * Message: Would you like to withdraw from the $s1 clan? If you leave, you will have to wait at least a day before joining another clan.
	 */
	public static final SystemMessageId WOULD_YOU_LIKE_TO_WITHDRAW_FROM_THE_S1_CLAN;
	
	/**
	 * ID: 69<br>
	 * Message: Would you like to dismiss $s1 from the clan? If you do so, you will have to wait at least a day before accepting a new member.
	 */
	public static final SystemMessageId WOULD_YOU_LIKE_TO_DISMISS_S1_FROM_THE_CLAN;
	
	/**
	 * ID: 70<br>
	 * Message: Do you wish to disperse the clan, $s1?
	 */
	public static final SystemMessageId DO_YOU_WISH_TO_DISPERSE_THE_CLAN_S1;
	
	/**
	 * ID: 71<br>
	 * Message: How many of your $s1(s) do you wish to discard?
	 */
	public static final SystemMessageId HOW_MANY_S1_DISCARD;
	
	/**
	 * ID: 72<br>
	 * Message: How many of your $s1(s) do you wish to move?
	 */
	public static final SystemMessageId HOW_MANY_S1_MOVE;
	
	/**
	 * ID: 73<br>
	 * Message: How many of your $s1(s) do you wish to destroy?
	 */
	public static final SystemMessageId HOW_MANY_S1_DESTROY;
	
	/**
	 * ID: 74<br>
	 * Message: Do you wish to destroy your $s1?
	 */
	public static final SystemMessageId WISH_DESTROY_S1;
	
	/**
	 * ID: 75<br>
	 * Message: ID does not exist.
	 */
	public static final SystemMessageId ID_NOT_EXIST;
	
	/**
	 * ID: 76<br>
	 * Message: Incorrect password.
	 */
	public static final SystemMessageId INCORRECT_PASSWORD;
	
	/**
	 * ID: 77<br>
	 * Message: You cannot create another character. Please delete the existing character and try again.
	 */
	public static final SystemMessageId CANNOT_CREATE_CHARACTER;
	
	/**
	 * ID: 78<br>
	 * Message: When you delete a character, any items in his/her possession will also be deleted. Do you really wish to delete $s1%?
	 */
	public static final SystemMessageId WISH_DELETE_S1;
	
	/**
	 * ID: 79<br>
	 * Message: This name already exists.
	 */
	public static final SystemMessageId NAMING_NAME_ALREADY_EXISTS;
	
	/**
	 * ID: 80<br>
	 * Message: Names must be between 1-16 characters, excluding spaces or special characters.
	 */
	public static final SystemMessageId NAMING_CHARNAME_UP_TO_16CHARS;
	
	/**
	 * ID: 81<br>
	 * Message: Please select your race.
	 */
	public static final SystemMessageId PLEASE_SELECT_RACE;
	
	/**
	 * ID: 82<br>
	 * Message: Please select your occupation.
	 */
	public static final SystemMessageId PLEASE_SELECT_OCCUPATION;
	
	/**
	 * ID: 83<br>
	 * Message: Please select your gender.
	 */
	public static final SystemMessageId PLEASE_SELECT_GENDER;
	
	/**
	 * ID: 84<br>
	 * Message: You may not attack in a peaceful zone.
	 */
	public static final SystemMessageId CANT_ATK_PEACEZONE;
	
	/**
	 * ID: 85<br>
	 * Message: You may not attack this target in a peaceful zone.
	 */
	public static final SystemMessageId TARGET_IN_PEACEZONE;
	
	/**
	 * ID: 86<br>
	 * Message: Please enter your ID.
	 */
	public static final SystemMessageId PLEASE_ENTER_ID;
	
	/**
	 * ID: 87<br>
	 * Message: Please enter your password.
	 */
	public static final SystemMessageId PLEASE_ENTER_PASSWORD;
	
	/**
	 * ID: 88<br>
	 * Message: Your protocol version is different, please restart your client and run a full check.
	 */
	public static final SystemMessageId WRONG_PROTOCOL_CHECK;
	
	/**
	 * ID: 89<br>
	 * Message: Your protocol version is different, please continue.
	 */
	public static final SystemMessageId WRONG_PROTOCOL_CONTINUE;
	
	/**
	 * ID: 90<br>
	 * Message: You are unable to connect to the server.
	 */
	public static final SystemMessageId UNABLE_TO_CONNECT;
	
	/**
	 * ID: 91<br>
	 * Message: Please select your hairstyle.
	 */
	public static final SystemMessageId PLEASE_SELECT_HAIRSTYLE;
	
	/**
	 * ID: 92<br>
	 * Message: $s1 has worn off.
	 */
	public static final SystemMessageId S1_HAS_WORN_OFF;
	
	/**
	 * ID: 93<br>
	 * Message: You do not have enough SP for this.
	 */
	public static final SystemMessageId NOT_ENOUGH_SP;
	
	/**
	 * ID: 94<br>
	 * Message: 2004-2011 (c) NC Interactive, Inc. All Rights Reserved.
	 */
	public static final SystemMessageId COPYRIGHT;
	
	/**
	 * ID: 95<br>
	 * Message: You have earned $s1 experience and $s2 SP.
	 */
	public static final SystemMessageId YOU_EARNED_S1_EXP_AND_S2_SP;
	
	/**
	 * ID: 96<br>
	 * Message: Your level has increased!
	 */
	public static final SystemMessageId YOU_INCREASED_YOUR_LEVEL;
	
	/**
	 * ID: 97<br>
	 * Message: This item cannot be moved.
	 */
	public static final SystemMessageId CANNOT_MOVE_THIS_ITEM;
	
	/**
	 * ID: 98<br>
	 * Message: This item cannot be discarded.
	 */
	public static final SystemMessageId CANNOT_DISCARD_THIS_ITEM;
	
	/**
	 * ID: 99<br>
	 * Message: This item cannot be traded or sold.
	 */
	public static final SystemMessageId CANNOT_TRADE_THIS_ITEM;
	
	/**
	 * ID: 100<br>
	 * Message: $c1 is requesting to trade. Do you wish to continue?
	 */
	public static final SystemMessageId C1_REQUESTS_TRADE;
	
	/**
	 * ID: 101<br>
	 * Message: You cannot exit while in combat.
	 */
	public static final SystemMessageId CANT_LOGOUT_WHILE_FIGHTING;
	
	/**
	 * ID: 102<br>
	 * Message: You cannot restart while in combat.
	 */
	public static final SystemMessageId CANT_RESTART_WHILE_FIGHTING;
	
	/**
	 * ID: 103<br>
	 * Message: This ID is currently logged in.
	 */
	public static final SystemMessageId ID_LOGGED_IN;
	
	/**
	 * ID: 104<br>
	 * Message: You cannot change weapons during an attack.
	 */
	public static final SystemMessageId CANNOT_CHANGE_WEAPON_DURING_AN_ATTACK;
	
	/**
	 * ID: 105<br>
	 * Message: $c1 has been invited to the party.
	 */
	public static final SystemMessageId C1_INVITED_TO_PARTY;
	
	/**
	 * ID: 106<br>
	 * Message: You have joined $s1's party.
	 */
	public static final SystemMessageId YOU_JOINED_S1_PARTY;
	
	/**
	 * ID: 107<br>
	 * Message: $c1 has joined the party.
	 */
	public static final SystemMessageId C1_JOINED_PARTY;
	
	/**
	 * ID: 108<br>
	 * Message: $c1 has left the party.
	 */
	public static final SystemMessageId C1_LEFT_PARTY;
	
	/**
	 * ID: 109<br>
	 * Message: Invalid target.
	 */
	public static final SystemMessageId INCORRECT_TARGET;
	
	/**
	 * ID: 110<br>
	 * Message: $s1 $s2's effect can be felt.
	 */
	public static final SystemMessageId YOU_FEEL_S1_EFFECT;
	
	/**
	 * ID: 111<br>
	 * Message: Your shield defense has succeeded.
	 */
	public static final SystemMessageId SHIELD_DEFENCE_SUCCESSFULL;
	
	/**
	 * ID: 112<br>
	 * Message: You have run out of arrows.
	 */
	public static final SystemMessageId NOT_ENOUGH_ARROWS;
	
	/**
	 * ID: 113<br>
	 * Message: $s1 cannot be used due to unsuitable terms.
	 */
	public static final SystemMessageId S1_CANNOT_BE_USED;
	
	/**
	 * ID: 114<br>
	 * Message: You have entered the shadow of the Mother Tree.
	 */
	public static final SystemMessageId ENTER_SHADOW_MOTHER_TREE;
	
	/**
	 * ID: 115<br>
	 * Message: You have left the shadow of the Mother Tree.
	 */
	public static final SystemMessageId EXIT_SHADOW_MOTHER_TREE;
	
	/**
	 * ID: 116<br>
	 * Message: You have entered a peaceful zone.
	 */
	public static final SystemMessageId ENTER_PEACEFUL_ZONE;
	
	/**
	 * ID: 117<br>
	 * Message: You have left the peaceful zone.
	 */
	public static final SystemMessageId EXIT_PEACEFUL_ZONE;
	
	/**
	 * ID: 118<br>
	 * Message: You have requested a trade with $c1.
	 */
	public static final SystemMessageId REQUEST_C1_FOR_TRADE;
	
	/**
	 * ID: 119<br>
	 * Message: $c1 has denied your request to trade.
	 */
	public static final SystemMessageId C1_DENIED_TRADE_REQUEST;
	
	/**
	 * ID: 120<br>
	 * Message: You begin trading with $c1.
	 */
	public static final SystemMessageId BEGIN_TRADE_WITH_C1;
	
	/**
	 * ID: 121<br>
	 * Message: $c1 has confirmed the trade.
	 */
	public static final SystemMessageId C1_CONFIRMED_TRADE;
	
	/**
	 * ID: 122<br>
	 * Message: You may no longer adjust items in the trade because the trade has been confirmed.
	 */
	public static final SystemMessageId CANNOT_ADJUST_ITEMS_AFTER_TRADE_CONFIRMED;
	
	/**
	 * ID: 123<br>
	 * Message: Your trade is successful.
	 */
	public static final SystemMessageId TRADE_SUCCESSFUL;
	
	/**
	 * ID: 124<br>
	 * Message: $c1 has cancelled the trade.
	 */
	public static final SystemMessageId C1_CANCELED_TRADE;
	
	/**
	 * ID: 125<br>
	 * Message: Do you wish to exit the game?
	 */
	public static final SystemMessageId WISH_EXIT_GAME;
	
	/**
	 * ID: 126<br>
	 * Message: Do you wish to return to the character select screen?
	 */
	public static final SystemMessageId WISH_RESTART_GAME;
	
	/**
	 * ID: 127<br>
	 * Message: You have been disconnected from the server. Please login again.
	 */
	public static final SystemMessageId DISCONNECTED_FROM_SERVER;
	
	/**
	 * ID: 128<br>
	 * Message: Your character creation has failed.
	 */
	public static final SystemMessageId CHARACTER_CREATION_FAILED;
	
	/**
	 * ID: 129<br>
	 * Message: Your inventory is full.
	 */
	public static final SystemMessageId SLOTS_FULL;
	
	/**
	 * ID: 130<br>
	 * Message: Your warehouse is full.
	 */
	public static final SystemMessageId WAREHOUSE_FULL;
	
	/**
	 * ID: 131<br>
	 * Message: $s1 has logged in.
	 */
	public static final SystemMessageId S1_LOGGED_IN;
	
	/**
	 * ID: 132<br>
	 * Message: $s1 has been added to your friends list.
	 */
	public static final SystemMessageId S1_ADDED_TO_FRIENDS;
	
	/**
	 * ID: 133<br>
	 * Message: $s1 has been removed from your friends list.
	 */
	public static final SystemMessageId S1_REMOVED_FROM_YOUR_FRIENDS_LIST;
	
	/**
	 * ID: 134<br>
	 * Message: Please check your friends list again.
	 */
	public static final SystemMessageId PLEACE_CHECK_YOUR_FRIEND_LIST_AGAIN;
	
	/**
	 * ID: 135<br>
	 * Message: $c1 did not reply to your invitation. Your invitation has been cancelled.
	 */
	public static final SystemMessageId C1_DID_NOT_REPLY_TO_YOUR_INVITE;
	
	/**
	 * ID: 136<br>
	 * Message: You have not replied to $c1's invitation. The offer has been cancelled.
	 */
	public static final SystemMessageId YOU_DID_NOT_REPLY_TO_C1_INVITE;
	
	/**
	 * ID: 137<br>
	 * Message: There are no more items in the shortcut.
	 */
	public static final SystemMessageId NO_MORE_ITEMS_SHORTCUT;
	
	/**
	 * ID: 138<br>
	 * Message: Designate shortcut.
	 */
	public static final SystemMessageId DESIGNATE_SHORTCUT;
	
	/**
	 * ID: 139<br>
	 * Message: $c1 has resisted your $s2.
	 */
	public static final SystemMessageId C1_RESISTED_YOUR_S2;
	
	/**
	 * ID: 140<br>
	 * Message: Your skill was removed due to a lack of MP.
	 */
	public static final SystemMessageId SKILL_REMOVED_DUE_LACK_MP;
	
	/**
	 * ID: 141<br>
	 * Message: Once the trade is confirmed, the item cannot be moved again.
	 */
	public static final SystemMessageId ONCE_THE_TRADE_IS_CONFIRMED_THE_ITEM_CANNOT_BE_MOVED_AGAIN;
	
	/**
	 * ID: 142<br>
	 * Message: You are already trading with someone.
	 */
	public static final SystemMessageId ALREADY_TRADING;
	
	/**
	 * ID: 143<br>
	 * Message: $c1 is already trading with another person. Please try again later.
	 */
	public static final SystemMessageId C1_ALREADY_TRADING;
	
	/**
	 * ID: 144<br>
	 * Message: That is the incorrect target.
	 */
	public static final SystemMessageId TARGET_IS_INCORRECT;
	
	/**
	 * ID: 145<br>
	 * Message: That player is not online.
	 */
	public static final SystemMessageId TARGET_IS_NOT_FOUND_IN_THE_GAME;
	
	/**
	 * ID: 146<br>
	 * Message: Chatting is now permitted.
	 */
	public static final SystemMessageId CHATTING_PERMITTED;
	
	/**
	 * ID: 147<br>
	 * Message: Chatting is currently prohibited.
	 */
	public static final SystemMessageId CHATTING_PROHIBITED;
	
	/**
	 * ID: 148<br>
	 * Message: You cannot use quest items.
	 */
	public static final SystemMessageId CANNOT_USE_QUEST_ITEMS;
	
	/**
	 * ID: 149<br>
	 * Message: You cannot pick up or use items while trading.
	 */
	public static final SystemMessageId CANNOT_USE_ITEM_WHILE_TRADING;
	
	/**
	 * ID: 150<br>
	 * Message: You cannot discard or destroy an item while trading at a private store.
	 */
	public static final SystemMessageId CANNOT_DISCARD_OR_DESTROY_ITEM_WHILE_TRADING;
	
	/**
	 * ID: 151<br>
	 * Message: That is too far from you to discard.
	 */
	public static final SystemMessageId CANNOT_DISCARD_DISTANCE_TOO_FAR;
	
	/**
	 * ID: 152<br>
	 * Message: You have invited the wrong target.
	 */
	public static final SystemMessageId YOU_HAVE_INVITED_THE_WRONG_TARGET;
	
	/**
	 * ID: 153<br>
	 * Message: $c1 is on another task. Please try again later.
	 */
	public static final SystemMessageId C1_IS_BUSY_TRY_LATER;
	
	/**
	 * ID: 154<br>
	 * Message: Only the leader can give out invitations.
	 */
	public static final SystemMessageId ONLY_LEADER_CAN_INVITE;
	
	/**
	 * ID: 155<br>
	 * Message: The party is full.
	 */
	public static final SystemMessageId PARTY_FULL;
	
	/**
	 * ID: 156<br>
	 * Message: Drain was only 50 percent successful.
	 */
	public static final SystemMessageId DRAIN_HALF_SUCCESFUL;
	
	/**
	 * ID: 157<br>
	 * Message: You resisted $c1's drain.
	 */
	public static final SystemMessageId RESISTED_C1_DRAIN;
	
	/**
	 * ID: 158<br>
	 * Message: Your attack has failed.
	 */
	public static final SystemMessageId ATTACK_FAILED;
	
	/**
	 * ID: 159<br>
	 * Message: You resisted $c1's magic.
	 */
	public static final SystemMessageId RESISTED_C1_MAGIC;
	
	/**
	 * ID: 160<br>
	 * Message: $c1 is a member of another party and cannot be invited.
	 */
	public static final SystemMessageId C1_IS_ALREADY_IN_PARTY;
	
	/**
	 * ID: 161<br>
	 * Message: That player is not currently online.
	 */
	public static final SystemMessageId INVITED_USER_NOT_ONLINE;
	
	/**
	 * ID: 162<br>
	 * Message: Warehouse is too far.
	 */
	public static final SystemMessageId WAREHOUSE_TOO_FAR;
	
	/**
	 * ID: 163<br>
	 * Message: You cannot destroy it because the number is incorrect.
	 */
	public static final SystemMessageId CANNOT_DESTROY_NUMBER_INCORRECT;
	
	/**
	 * ID: 164<br>
	 * Message: Waiting for another reply.
	 */
	public static final SystemMessageId WAITING_FOR_ANOTHER_REPLY;
	
	/**
	 * ID: 165<br>
	 * Message: You cannot add yourself to your own friend list.
	 */
	public static final SystemMessageId YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST;
	
	/**
	 * ID: 166<br>
	 * Message: Friend list is not ready yet. Please register again later.
	 */
	public static final SystemMessageId FRIEND_LIST_NOT_READY_YET_REGISTER_LATER;
	
	/**
	 * ID: 167<br>
	 * Message: $c1 is already on your friend list.
	 */
	public static final SystemMessageId C1_ALREADY_ON_FRIEND_LIST;
	
	/**
	 * ID: 168<br>
	 * Message: $c1 has sent a friend request.
	 */
	public static final SystemMessageId C1_REQUESTED_TO_BECOME_FRIENDS;
	
	/**
	 * ID: 169<br>
	 * Message: Accept friendship 0/1 (1 to accept, 0 to deny)
	 */
	public static final SystemMessageId ACCEPT_THE_FRIENDSHIP;
	
	/**
	 * ID: 170<br>
	 * Message: The user who requested to become friends is not found in the game.
	 */
	public static final SystemMessageId THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME;
	
	/**
	 * ID: 171<br>
	 * Message: $c1 is not on your friend list.
	 */
	public static final SystemMessageId C1_NOT_ON_YOUR_FRIENDS_LIST;
	
	/**
	 * ID: 172<br>
	 * Message: You lack the funds needed to pay for this transaction.
	 */
	public static final SystemMessageId LACK_FUNDS_FOR_TRANSACTION1;
	
	/**
	 * ID: 173<br>
	 * Message: You lack the funds needed to pay for this transaction.
	 */
	public static final SystemMessageId LACK_FUNDS_FOR_TRANSACTION2;
	
	/**
	 * ID: 174<br>
	 * Message: That person's inventory is full.
	 */
	public static final SystemMessageId OTHER_INVENTORY_FULL;
	
	/**
	 * ID: 175<br>
	 * Message: That skill has been de-activated as HP was fully recovered.
	 */
	public static final SystemMessageId SKILL_DEACTIVATED_HP_FULL;
	
	/**
	 * ID: 176<br>
	 * Message: That person is in message refusal mode.
	 */
	public static final SystemMessageId THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE;
	
	/**
	 * ID: 177<br>
	 * Message: Message refusal mode.
	 */
	public static final SystemMessageId MESSAGE_REFUSAL_MODE;
	
	/**
	 * ID: 178<br>
	 * Message: Message acceptance mode.
	 */
	public static final SystemMessageId MESSAGE_ACCEPTANCE_MODE;
	
	/**
	 * ID: 179<br>
	 * Message: You cannot discard those items here.
	 */
	public static final SystemMessageId CANT_DISCARD_HERE;
	
	/**
	 * ID: 180<br>
	 * Message: You have $s1 day(s) left until deletion. Do you wish to cancel this action?
	 */
	public static final SystemMessageId S1_DAYS_LEFT_CANCEL_ACTION;
	
	/**
	 * ID: 181<br>
	 * Message: Cannot see target.
	 */
	public static final SystemMessageId CANT_SEE_TARGET;
	
	/**
	 * ID: 182<br>
	 * Message: Do you want to quit the current quest?
	 */
	public static final SystemMessageId WANT_QUIT_CURRENT_QUEST;
	
	/**
	 * ID: 183<br>
	 * Message: There are too many users on the server. Please try again later
	 */
	public static final SystemMessageId TOO_MANY_USERS;
	
	/**
	 * ID: 184<br>
	 * Message: Please try again later.
	 */
	public static final SystemMessageId TRY_AGAIN_LATER;
	
	/**
	 * ID: 185<br>
	 * Message: You must first select a user to invite to your party.
	 */
	public static final SystemMessageId FIRST_SELECT_USER_TO_INVITE_TO_PARTY;
	
	/**
	 * ID: 186<br>
	 * Message: You must first select a user to invite to your clan.
	 */
	public static final SystemMessageId FIRST_SELECT_USER_TO_INVITE_TO_CLAN;
	
	/**
	 * ID: 187<br>
	 * Message: Select user to expel.
	 */
	public static final SystemMessageId SELECT_USER_TO_EXPEL;
	
	/**
	 * ID: 188<br>
	 * Message: Please create your clan name.
	 */
	public static final SystemMessageId PLEASE_CREATE_CLAN_NAME;
	
	/**
	 * ID: 189<br>
	 * Message: Your clan has been created.
	 */
	public static final SystemMessageId CLAN_CREATED;
	
	/**
	 * ID: 190<br>
	 * Message: You have failed to create a clan.
	 */
	public static final SystemMessageId FAILED_TO_CREATE_CLAN;
	
	/**
	 * ID: 191<br>
	 * Message: Clan member $s1 has been expelled.
	 */
	public static final SystemMessageId CLAN_MEMBER_S1_EXPELLED;
	
	/**
	 * ID: 192<br>
	 * Message: You have failed to expel $s1 from the clan.
	 */
	public static final SystemMessageId FAILED_EXPEL_S1;
	
	/**
	 * ID: 193<br>
	 * Message: Clan has dispersed.
	 */
	public static final SystemMessageId CLAN_HAS_DISPERSED;
	
	/**
	 * ID: 194<br>
	 * Message: You have failed to disperse the clan.
	 */
	public static final SystemMessageId FAILED_TO_DISPERSE_CLAN;
	
	/**
	 * ID: 195<br>
	 * Message: Entered the clan.
	 */
	public static final SystemMessageId ENTERED_THE_CLAN;
	
	/**
	 * ID: 196<br>
	 * Message: $s1 declined your clan invitation.
	 */
	public static final SystemMessageId S1_REFUSED_TO_JOIN_CLAN;
	
	/**
	 * ID: 197<br>
	 * Message: You have withdrawn from the clan.
	 */
	public static final SystemMessageId YOU_HAVE_WITHDRAWN_FROM_CLAN;
	
	/**
	 * ID: 198<br>
	 * Message: You have failed to withdraw from the $s1 clan.
	 */
	public static final SystemMessageId FAILED_TO_WITHDRAW_FROM_S1_CLAN;
	
	/**
	 * ID: 199<br>
	 * Message: You have recently been dismissed from a clan. You are not allowed to join another clan for 24-hours.
	 */
	public static final SystemMessageId CLAN_MEMBERSHIP_TERMINATED;
	
	/**
	 * ID: 200<br>
	 * Message: You have withdrawn from the party.
	 */
	public static final SystemMessageId YOU_LEFT_PARTY;
	
	/**
	 * ID: 201<br>
	 * Message: $c1 was expelled from the party.
	 */
	public static final SystemMessageId C1_WAS_EXPELLED_FROM_PARTY;
	
	/**
	 * ID: 202<br>
	 * Message: You have been expelled from the party.
	 */
	public static final SystemMessageId HAVE_BEEN_EXPELLED_FROM_PARTY;
	
	/**
	 * ID: 203<br>
	 * Message: The party has dispersed.
	 */
	public static final SystemMessageId PARTY_DISPERSED;
	
	/**
	 * ID: 204<br>
	 * Message: Incorrect name. Please try again.
	 */
	public static final SystemMessageId INCORRECT_NAME_TRY_AGAIN;
	
	/**
	 * ID: 205<br>
	 * Message: Incorrect character name. Please try again.
	 */
	public static final SystemMessageId INCORRECT_CHARACTER_NAME_TRY_AGAIN;
	
	/**
	 * ID: 206<br>
	 * Message: Please enter the name of the clan you wish to declare war on.
	 */
	public static final SystemMessageId ENTER_CLAN_NAME_TO_DECLARE_WAR;
	
	/**
	 * ID: 207<br>
	 * Message: $s2 of the clan $s1 requests declaration of war. Do you accept?
	 */
	public static final SystemMessageId S2_OF_THE_CLAN_S1_REQUESTS_WAR;
	
	/**
	 * ID: 208<br>
	 * Message: Please include file type when entering file path.
	 */
	
	/**
	 * ID: 209<br>
	 * Message: The size of the image file is inappropriate. Please adjust to 16x12 pixels.
	 */
	public static final SystemMessageId THE_SIZE_OF_THE_IMAGE_FILE_IS_INAPPROPRIATE;
	
	/**
	 * ID: 210<br>
	 * Message: Cannot find file. Please enter precise path.
	 */
	
	/**
	 * ID: 211<br>
	 * Message: You can only register 16x12 pixel 256 color bmp files.
	 */
	
	/**
	 * ID: 212<br>
	 * Message: You are not a clan member and cannot perform this action.
	 */
	public static final SystemMessageId YOU_ARE_NOT_A_CLAN_MEMBER;
	
	/**
	 * ID: 213<br>
	 * Message: Not working. Please try again later.
	 */
	public static final SystemMessageId NOT_WORKING_PLEASE_TRY_AGAIN_LATER;
	
	/**
	 * ID: 214<br>
	 * Message: Your title has been changed.
	 */
	public static final SystemMessageId TITLE_CHANGED;
	
	/**
	 * ID: 215<br>
	 * Message: War with the $s1 clan has begun.
	 */
	public static final SystemMessageId WAR_WITH_THE_S1_CLAN_HAS_BEGUN;
	
	/**
	 * ID: 216<br>
	 * Message: War with the $s1 clan has ended.
	 */
	public static final SystemMessageId WAR_WITH_THE_S1_CLAN_HAS_ENDED;
	
	/**
	 * ID: 217<br>
	 * Message: You have won the war over the $s1 clan!
	 */
	public static final SystemMessageId YOU_HAVE_WON_THE_WAR_OVER_THE_S1_CLAN;
	
	/**
	 * ID: 218<br>
	 * Message: You have surrendered to the $s1 clan.
	 */
	public static final SystemMessageId YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN;
	
	/**
	 * ID: 219<br>
	 * Message: Your clan leader has died. You have been defeated by the $s1 clan.
	 */
	public static final SystemMessageId YOU_WERE_DEFEATED_BY_S1_CLAN;
	
	/**
	 * ID: 220<br>
	 * Message: You have $s1 minutes left until the clan war ends.
	 */
	public static final SystemMessageId S1_MINUTES_LEFT_UNTIL_CLAN_WAR_ENDS;
	
	/**
	 * ID: 221<br>
	 * Message: The time limit for the clan war is up. War with the $s1 clan is over.
	 */
	public static final SystemMessageId CLAN_WAR_WITH_S1_CLAN_HAS_ENDED;
	
	/**
	 * ID: 222<br>
	 * Message: $s1 has joined the clan.
	 */
	public static final SystemMessageId S1_HAS_JOINED_CLAN;
	
	/**
	 * ID: 223<br>
	 * Message: $s1 has withdrawn from the clan.
	 */
	public static final SystemMessageId S1_HAS_WITHDRAWN_FROM_THE_CLAN;
	
	/**
	 * ID: 224<br>
	 * Message: $s1 did not respond: Invitation to the clan has been cancelled.
	 */
	public static final SystemMessageId S1_DID_NOT_RESPOND_TO_CLAN_INVITATION;
	
	/**
	 * ID: 225<br>
	 * Message: You didn't respond to $s1's invitation: joining has been cancelled.
	 */
	public static final SystemMessageId YOU_DID_NOT_RESPOND_TO_S1_CLAN_INVITATION;
	
	/**
	 * ID: 226<br>
	 * Message: The $s1 clan did not respond: war proclamation has been refused.
	 */
	public static final SystemMessageId S1_CLAN_DID_NOT_RESPOND;
	
	/**
	 * ID: 227<br>
	 * Message: Clan war has been refused because you did not respond to $s1 clan's war proclamation.
	 */
	public static final SystemMessageId CLAN_WAR_REFUSED_YOU_DID_NOT_RESPOND_TO_S1;
	
	/**
	 * ID: 228<br>
	 * Message: Request to end war has been denied.
	 */
	public static final SystemMessageId REQUEST_TO_END_WAR_HAS_BEEN_DENIED;
	
	/**
	 * ID: 229<br>
	 * Message: You do not meet the criteria in order to create a clan.
	 */
	public static final SystemMessageId YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN;
	
	/**
	 * ID: 230<br>
	 * Message: You must wait 10 days before creating a new clan.
	 */
	public static final SystemMessageId YOU_MUST_WAIT_XX_DAYS_BEFORE_CREATING_A_NEW_CLAN;
	
	/**
	 * ID: 231<br>
	 * Message: After a clan member is dismissed from a clan, the clan must wait at least a day before accepting a new member.
	 */
	public static final SystemMessageId YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER;
	
	/**
	 * ID: 232<br>
	 * Message: After leaving or having been dismissed from a clan, you must wait at least a day before joining another clan.
	 */
	public static final SystemMessageId YOU_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN;
	
	/**
	 * ID: 233<br>
	 * Message: The Academy/Royal Guard/Order of Knights is full and cannot accept new members at this time.
	 */
	public static final SystemMessageId SUBCLAN_IS_FULL;
	
	/**
	 * ID: 234<br>
	 * Message: The target must be a clan member.
	 */
	public static final SystemMessageId TARGET_MUST_BE_IN_CLAN;
	
	/**
	 * ID: 235<br>
	 * Message: You are not authorized to bestow these rights.
	 */
	public static final SystemMessageId NOT_AUTHORIZED_TO_BESTOW_RIGHTS;
	
	/**
	 * ID: 236<br>
	 * Message: Only the clan leader is enabled.
	 */
	public static final SystemMessageId ONLY_THE_CLAN_LEADER_IS_ENABLED;
	
	/**
	 * ID: 237<br>
	 * Message: The clan leader could not be found.
	 */
	public static final SystemMessageId CLAN_LEADER_NOT_FOUND;
	
	/**
	 * ID: 238<br>
	 * Message: Not joined in any clan.
	 */
	public static final SystemMessageId NOT_JOINED_IN_ANY_CLAN;
	
	/**
	 * ID: 239<br>
	 * Message: The clan leader cannot withdraw.
	 */
	public static final SystemMessageId CLAN_LEADER_CANNOT_WITHDRAW;
	
	/**
	 * ID: 240<br>
	 * Message: Currently involved in clan war.
	 */
	public static final SystemMessageId CURRENTLY_INVOLVED_IN_CLAN_WAR;
	
	/**
	 * ID: 241<br>
	 * Message: Leader of the $s1 Clan is not logged in.
	 */
	public static final SystemMessageId LEADER_OF_S1_CLAN_NOT_FOUND;
	
	/**
	 * ID: 242<br>
	 * Message: Select target.
	 */
	public static final SystemMessageId SELECT_TARGET;
	
	/**
	 * ID: 243<br>
	 * Message: You cannot declare war on an allied clan.
	 */
	public static final SystemMessageId CANNOT_DECLARE_WAR_ON_ALLIED_CLAN;
	
	/**
	 * ID: 244<br>
	 * Message: You are not allowed to issue this challenge.
	 */
	public static final SystemMessageId NOT_ALLOWED_TO_CHALLENGE;
	
	/**
	 * ID: 245<br>
	 * Message: 5 days has not passed since you were refused war. Do you wish to continue?
	 */
	public static final SystemMessageId FIVE_DAYS_NOT_PASSED_SINCE_REFUSED_WAR;
	
	/**
	 * ID: 246<br>
	 * Message: That clan is currently at war.
	 */
	public static final SystemMessageId CLAN_CURRENTLY_AT_WAR;
	
	/**
	 * ID: 247<br>
	 * Message: You have already been at war with the $s1 clan: 5 days must pass before you can challenge this clan again
	 */
	public static final SystemMessageId FIVE_DAYS_MUST_PASS_BEFORE_CHALLENGE_AGAIN;
	
	/**
	 * ID: 248<br>
	 * Message: You cannot proclaim war: the $s1 clan does not have enough members.
	 */
	public static final SystemMessageId S1_CLAN_NOT_ENOUGH_MEMBERS_FOR_WAR;
	
	/**
	 * ID: 249<br>
	 * Message: Do you wish to surrender to the $s1 clan?
	 */
	public static final SystemMessageId WISH_SURRENDER_TO_S1_CLAN;
	
	/**
	 * ID: 250<br>
	 * Message: You have personally surrendered to the $s1 clan. You are no longer participating in this clan war.
	 */
	public static final SystemMessageId YOU_HAVE_PERSONALLY_SURRENDERED_TO_THE_S1_CLAN;
	
	/**
	 * ID: 251<br>
	 * Message: You cannot proclaim war: you are at war with another clan.
	 */
	public static final SystemMessageId ALREADY_AT_WAR_WITH_ANOTHER_CLAN;
	
	/**
	 * ID: 252<br>
	 * Message: Enter the clan name to surrender to.
	 */
	public static final SystemMessageId ENTER_CLAN_NAME_TO_SURRENDER_TO;
	
	/**
	 * ID: 253<br>
	 * Message: Enter the name of the clan you wish to end the war with.
	 */
	public static final SystemMessageId ENTER_CLAN_NAME_TO_END_WAR;
	
	/**
	 * ID: 254<br>
	 * Message: A clan leader cannot personally surrender.
	 */
	public static final SystemMessageId LEADER_CANT_PERSONALLY_SURRENDER;
	
	/**
	 * ID: 255<br>
	 * Message: The $s1 clan has requested to end war. Do you agree?
	 */
	public static final SystemMessageId S1_CLAN_REQUESTED_END_WAR;
	
	/**
	 * ID: 256<br>
	 * Message: Enter title
	 */
	public static final SystemMessageId ENTER_TITLE;
	
	/**
	 * ID: 257<br>
	 * Message: Do you offer the $s1 clan a proposal to end the war?
	 */
	public static final SystemMessageId DO_YOU_OFFER_S1_CLAN_END_WAR;
	
	/**
	 * ID: 258<br>
	 * Message: You are not involved in a clan war.
	 */
	public static final SystemMessageId NOT_INVOLVED_CLAN_WAR;
	
	/**
	 * ID: 259<br>
	 * Message: Select clan members from list.
	 */
	public static final SystemMessageId SELECT_MEMBERS_FROM_LIST;
	
	/**
	 * ID: 260<br>
	 * Message: Fame level has decreased: 5 days have not passed since you were refused war
	 */
	public static final SystemMessageId FIVE_DAYS_NOT_PASSED_SINCE_YOU_WERE_REFUSED_WAR;
	
	/**
	 * ID: 261<br>
	 * Message: Clan name is invalid.
	 */
	public static final SystemMessageId CLAN_NAME_INCORRECT;
	
	/**
	 * ID: 262<br>
	 * Message: Clan name's length is incorrect.
	 */
	public static final SystemMessageId CLAN_NAME_TOO_LONG;
	
	/**
	 * ID: 263<br>
	 * Message: You have already requested the dissolution of your clan.
	 */
	public static final SystemMessageId DISSOLUTION_IN_PROGRESS;
	
	/**
	 * ID: 264<br>
	 * Message: You cannot dissolve a clan while engaged in a war.
	 */
	public static final SystemMessageId CANNOT_DISSOLVE_WHILE_IN_WAR;
	
	/**
	 * ID: 265<br>
	 * Message: You cannot dissolve a clan during a siege or while protecting a castle.
	 */
	public static final SystemMessageId CANNOT_DISSOLVE_WHILE_IN_SIEGE;
	
	/**
	 * ID: 266<br>
	 * Message: You cannot dissolve a clan while owning a clan hall or castle.
	 */
	public static final SystemMessageId CANNOT_DISSOLVE_WHILE_OWNING_CLAN_HALL_OR_CASTLE;
	
	/**
	 * ID: 267<br>
	 * Message: There are no requests to disperse.
	 */
	public static final SystemMessageId NO_REQUESTS_TO_DISPERSE;
	
	/**
	 * ID: 268<br>
	 * Message: That player already belongs to another clan.
	 */
	public static final SystemMessageId PLAYER_ALREADY_ANOTHER_CLAN;
	
	/**
	 * ID: 269<br>
	 * Message: You cannot dismiss yourself.
	 */
	public static final SystemMessageId YOU_CANNOT_DISMISS_YOURSELF;
	
	/**
	 * ID: 270<br>
	 * Message: You have already surrendered.
	 */
	public static final SystemMessageId YOU_HAVE_ALREADY_SURRENDERED;
	
	/**
	 * ID: 271<br>
	 * Message: A player can only be granted a title if the clan is level 3 or above
	 */
	public static final SystemMessageId CLAN_LVL_3_NEEDED_TO_ENDOWE_TITLE;
	
	/**
	 * ID: 272<br>
	 * Message: A clan crest can only be registered when the clan's skill level is 3 or above.
	 */
	public static final SystemMessageId CLAN_LVL_3_NEEDED_TO_SET_CREST;
	
	/**
	 * ID: 273<br>
	 * Message: A clan war can only be declared when a clan's skill level is 3 or above.
	 */
	public static final SystemMessageId CLAN_LVL_3_NEEDED_TO_DECLARE_WAR;
	
	/**
	 * ID: 274<br>
	 * Message: Your clan's skill level has increased.
	 */
	public static final SystemMessageId CLAN_LEVEL_INCREASED;
	
	/**
	 * ID: 275<br>
	 * Message: Clan has failed to increase skill level.
	 */
	public static final SystemMessageId CLAN_LEVEL_INCREASE_FAILED;
	
	/**
	 * ID: 276<br>
	 * Message: You do not have the necessary materials or prerequisites to learn this skill.
	 */
	public static final SystemMessageId ITEM_OR_PREREQUISITES_MISSING_TO_LEARN_SKILL;
	
	/**
	 * ID: 277<br>
	 * Message: You have earned $s1.
	 */
	public static final SystemMessageId LEARNED_SKILL_S1;
	
	/**
	 * ID: 278<br>
	 * Message: You do not have enough SP to learn this skill.
	 */
	public static final SystemMessageId NOT_ENOUGH_SP_TO_LEARN_SKILL;
	
	/**
	 * ID: 279<br>
	 * Message: You do not have enough adena.
	 */
	public static final SystemMessageId YOU_NOT_ENOUGH_ADENA;
	
	/**
	 * ID: 280<br>
	 * Message: You do not have any items to sell.
	 */
	public static final SystemMessageId NO_ITEMS_TO_SELL;
	
	/**
	 * ID: 281<br>
	 * Message: You do not have enough adena to pay the fee.
	 */
	public static final SystemMessageId YOU_NOT_ENOUGH_ADENA_PAY_FEE;
	
	/**
	 * ID: 282<br>
	 * Message: You have not deposited any items in your warehouse.
	 */
	public static final SystemMessageId NO_ITEM_DEPOSITED_IN_WH;
	
	/**
	 * ID: 283<br>
	 * Message: You have entered a combat zone.
	 */
	public static final SystemMessageId ENTERED_COMBAT_ZONE;
	
	/**
	 * ID: 284<br>
	 * Message: You have left a combat zone.
	 */
	public static final SystemMessageId LEFT_COMBAT_ZONE;
	
	/**
	 * ID: 285<br>
	 * Message: Clan $s1 has succeeded in engraving the ruler!
	 */
	public static final SystemMessageId CLAN_S1_ENGRAVED_RULER;
	
	/**
	 * ID: 286<br>
	 * Message: Your base is being attacked.
	 */
	public static final SystemMessageId BASE_UNDER_ATTACK;
	
	/**
	 * ID: 287<br>
	 * Message: The opposing clan has stared to engrave to monument!
	 */
	public static final SystemMessageId OPPONENT_STARTED_ENGRAVING;
	
	/**
	 * ID: 288<br>
	 * Message: The castle gate has been broken down.
	 */
	public static final SystemMessageId CASTLE_GATE_BROKEN_DOWN;
	
	/**
	 * ID: 289<br>
	 * Message: An outpost or headquarters cannot be built because at least one already exists.
	 */
	public static final SystemMessageId NOT_ANOTHER_HEADQUARTERS;
	
	/**
	 * ID: 290<br>
	 * Message: You cannot set up a base here.
	 */
	public static final SystemMessageId NOT_SET_UP_BASE_HERE;
	
	/**
	 * ID: 291<br>
	 * Message: Clan $s1 is victorious over $s2's castle siege!
	 */
	public static final SystemMessageId CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE;
	
	/**
	 * ID: 292<br>
	 * Message: $s1 has announced the castle siege time.
	 */
	public static final SystemMessageId S1_ANNOUNCED_SIEGE_TIME;
	
	/**
	 * ID: 293<br>
	 * Message: The registration term for $s1 has ended.
	 */
	public static final SystemMessageId REGISTRATION_TERM_FOR_S1_ENDED;
	
	/**
	 * ID: 294<br>
	 * Message: Because your clan is not currently on the offensive in a Clan Hall siege war, it cannot summon its base camp.
	 */
	public static final SystemMessageId BECAUSE_YOUR_CLAN_IS_NOT_CURRENTLY_ON_THE_OFFENSIVE_IN_A_CLAN_HALL_SIEGE_WAR_IT_CANNOT_SUMMON_ITS_BASE_CAMP;
	
	/**
	 * ID: 295<br>
	 * Message: $s1's siege was canceled because there were no clans that participated.
	 */
	public static final SystemMessageId S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED;
	
	/**
	 * ID: 296<br>
	 * Message: You received $s1 damage from taking a high fall.
	 */
	public static final SystemMessageId FALL_DAMAGE_S1;
	
	/**
	 * ID: 297<br>
	 * Message: You have taken $s1 damage because you were unable to breathe.
	 */
	public static final SystemMessageId DROWN_DAMAGE_S1;
	
	/**
	 * ID: 298<br>
	 * Message: You have dropped $s1.
	 */
	public static final SystemMessageId YOU_DROPPED_S1;
	
	/**
	 * ID: 299<br>
	 * Message: $c1 has obtained $s3 $s2.
	 */
	public static final SystemMessageId C1_OBTAINED_S3_S2;
	
	/**
	 * ID: 300<br>
	 * Message: $c1 has obtained $s2.
	 */
	public static final SystemMessageId C1_OBTAINED_S2;
	
	/**
	 * ID: 301<br>
	 * Message: $s2 $s1 has disappeared.
	 */
	public static final SystemMessageId S2_S1_DISAPPEARED;
	
	/**
	 * ID: 302<br>
	 * Message: $s1 has disappeared.
	 */
	public static final SystemMessageId S1_DISAPPEARED;
	
	/**
	 * ID: 303<br>
	 * Message: Select item to enchant.
	 */
	public static final SystemMessageId SELECT_ITEM_TO_ENCHANT;
	
	/**
	 * ID: 304<br>
	 * Message: Clan member $s1 has logged into game.
	 */
	public static final SystemMessageId CLAN_MEMBER_S1_LOGGED_IN;
	
	/**
	 * ID: 305<br>
	 * Message: The player declined to join your party.
	 */
	public static final SystemMessageId PLAYER_DECLINED;
	
	/**
	 * ID: 306<br>
	 * Message: You have failed to delete the character.
	 */
	public static final SystemMessageId FAILED_TO_DELETE_CHAR;
	
	/**
	 * ID: 307<br>
	 * Message: You cannot trade with a warehouse keeper.
	 */
	public static final SystemMessageId CANNOT_TRADE_WAREHOUSE_KEEPER;
	
	/**
	 * ID: 308<br>
	 * Message: The player declined your clan invitation.
	 */
	public static final SystemMessageId PLAYER_DECLINED_CLAN_INVITATION;
	
	/**
	 * ID: 309<br>
	 * Message: You have succeeded in expelling the clan member.
	 */
	public static final SystemMessageId YOU_HAVE_SUCCEEDED_IN_EXPELLING_CLAN_MEMBER;
	
	/**
	 * ID: 310<br>
	 * Message: You have failed to expel the clan member.
	 */
	public static final SystemMessageId FAILED_TO_EXPEL_CLAN_MEMBER;
	
	/**
	 * ID: 311<br>
	 * Message: The clan war declaration has been accepted.
	 */
	public static final SystemMessageId CLAN_WAR_DECLARATION_ACCEPTED;
	
	/**
	 * ID: 312<br>
	 * Message: The clan war declaration has been refused.
	 */
	public static final SystemMessageId CLAN_WAR_DECLARATION_REFUSED;
	
	/**
	 * ID: 313<br>
	 * Message: The cease war request has been accepted.
	 */
	public static final SystemMessageId CEASE_WAR_REQUEST_ACCEPTED;
	
	/**
	 * ID: 314<br>
	 * Message: You have failed to surrender.
	 */
	public static final SystemMessageId FAILED_TO_SURRENDER;
	
	/**
	 * ID: 315<br>
	 * Message: You have failed to personally surrender.
	 */
	public static final SystemMessageId FAILED_TO_PERSONALLY_SURRENDER;
	
	/**
	 * ID: 316<br>
	 * Message: You have failed to withdraw from the party.
	 */
	public static final SystemMessageId FAILED_TO_WITHDRAW_FROM_THE_PARTY;
	
	/**
	 * ID: 317<br>
	 * Message: You have failed to expel the party member.
	 */
	public static final SystemMessageId FAILED_TO_EXPEL_THE_PARTY_MEMBER;
	
	/**
	 * ID: 318<br>
	 * Message: You have failed to disperse the party.
	 */
	public static final SystemMessageId FAILED_TO_DISPERSE_THE_PARTY;
	
	/**
	 * ID: 319<br>
	 * Message: This door cannot be unlocked.
	 */
	public static final SystemMessageId UNABLE_TO_UNLOCK_DOOR;
	
	/**
	 * ID: 320<br>
	 * Message: You have failed to unlock the door.
	 */
	public static final SystemMessageId FAILED_TO_UNLOCK_DOOR;
	
	/**
	 * ID: 321<br>
	 * Message: It is not locked.
	 */
	public static final SystemMessageId ITS_NOT_LOCKED;
	
	/**
	 * ID: 322<br>
	 * Message: Please decide on the sales price.
	 */
	public static final SystemMessageId DECIDE_SALES_PRICE;
	
	/**
	 * ID: 323<br>
	 * Message: Your force has increased to $s1 level.
	 */
	public static final SystemMessageId FORCE_INCREASED_TO_S1;
	
	/**
	 * ID: 324<br>
	 * Message: Your force has reached maximum capacity.
	 */
	public static final SystemMessageId FORCE_MAXLEVEL_REACHED;
	
	/**
	 * ID: 325<br>
	 * Message: The corpse has already disappeared.
	 */
	public static final SystemMessageId CORPSE_ALREADY_DISAPPEARED;
	
	/**
	 * ID: 326<br>
	 * Message: Select target from list.
	 */
	public static final SystemMessageId SELECT_TARGET_FROM_LIST;
	
	/**
	 * ID: 327<br>
	 * Message: You cannot exceed 80 characters.
	 */
	public static final SystemMessageId CANNOT_EXCEED_80_CHARACTERS;
	
	/**
	 * ID: 328<br>
	 * Message: Please input title using less than 128 characters.
	 */
	public static final SystemMessageId PLEASE_INPUT_TITLE_LESS_128_CHARACTERS;
	
	/**
	 * ID: 329<br>
	 * Message: Please input content using less than 3000 characters.
	 */
	public static final SystemMessageId PLEASE_INPUT_CONTENT_LESS_3000_CHARACTERS;
	
	/**
	 * ID: 330<br>
	 * Message: A one-line response may not exceed 128 characters.
	 */
	public static final SystemMessageId ONE_LINE_RESPONSE_NOT_EXCEED_128_CHARACTERS;
	
	/**
	 * ID: 331<br>
	 * Message: You have acquired $s1 SP.
	 */
	public static final SystemMessageId ACQUIRED_S1_SP;
	
	/**
	 * ID: 332<br>
	 * Message: Do you want to be restored?
	 */
	public static final SystemMessageId DO_YOU_WANT_TO_BE_RESTORED;
	
	/**
	 * ID: 333<br>
	 * Message: You have received $s1 damage by Core's barrier.
	 */
	public static final SystemMessageId S1_DAMAGE_BY_CORE_BARRIER;
	
	/**
	 * ID: 334<br>
	 * Message: Please enter your private store display message.
	 */
	public static final SystemMessageId ENTER_PRIVATE_STORE_MESSAGE;
	
	/**
	 * ID: 335<br>
	 * Message: $s1 has been aborted.
	 */
	public static final SystemMessageId S1_HAS_BEEN_ABORTED;
	
	/**
	 * ID: 336<br>
	 * Message: You are attempting to crystallize $s1. Do you wish to continue?
	 */
	public static final SystemMessageId WISH_TO_CRYSTALLIZE_S1;
	
	/**
	 * ID: 337<br>
	 * Message: The soulshot you are attempting to use does not match the grade of your equipped weapon.
	 */
	public static final SystemMessageId SOULSHOTS_GRADE_MISMATCH;
	
	/**
	 * ID: 338<br>
	 * Message: You do not have enough soulshots for that.
	 */
	public static final SystemMessageId NOT_ENOUGH_SOULSHOTS;
	
	/**
	 * ID: 339<br>
	 * Message: Cannot use soulshots.
	 */
	public static final SystemMessageId CANNOT_USE_SOULSHOTS;
	
	/**
	 * ID: 340<br>
	 * Message: Your private store is now open for business.
	 */
	public static final SystemMessageId PRIVATE_STORE_UNDER_WAY;
	
	/**
	 * ID: 341<br>
	 * Message: You do not have enough materials to perform that action.
	 */
	public static final SystemMessageId NOT_ENOUGH_MATERIALS;
	
	/**
	 * ID: 342<br>
	 * Message: Power of the spirits enabled.
	 */
	public static final SystemMessageId ENABLED_SOULSHOT;
	
	/**
	 * ID: 343<br>
	 * Message: Sweeper failed, target not spoiled.
	 */
	public static final SystemMessageId SWEEPER_FAILED_TARGET_NOT_SPOILED;
	
	/**
	 * ID: 344<br>
	 * Message: Power of the spirits disabled.
	 */
	public static final SystemMessageId SOULSHOTS_DISABLED;
	
	/**
	 * ID: 345<br>
	 * Message: Chat enabled.
	 */
	public static final SystemMessageId CHAT_ENABLED;
	
	/**
	 * ID: 346<br>
	 * Message: Chat disabled.
	 */
	public static final SystemMessageId CHAT_DISABLED;
	
	/**
	 * ID: 347<br>
	 * Message: Incorrect item count.
	 */
	public static final SystemMessageId INCORRECT_ITEM_COUNT;
	
	/**
	 * ID: 348<br>
	 * Message: Incorrect item price.
	 */
	public static final SystemMessageId INCORRECT_ITEM_PRICE;
	
	/**
	 * ID: 349<br>
	 * Message: Private store already closed.
	 */
	public static final SystemMessageId PRIVATE_STORE_ALREADY_CLOSED;
	
	/**
	 * ID: 350<br>
	 * Message: Item out of stock.
	 */
	public static final SystemMessageId ITEM_OUT_OF_STOCK;
	
	/**
	 * ID: 351<br>
	 * Message: Incorrect item count.
	 */
	public static final SystemMessageId NOT_ENOUGH_ITEMS;
	
	/**
	 * ID: 352<br>
	 * Message: Incorrect item.
	 */
	public static final SystemMessageId INCORRECT_ITEM;
	
	/**
	 * ID: 353<br>
	 * Message: Cannot purchase.
	 */
	public static final SystemMessageId CANNOT_PURCHASE;
	
	/**
	 * ID: 354<br>
	 * Message: Cancel enchant.
	 */
	public static final SystemMessageId CANCEL_ENCHANT;
	
	/**
	 * ID: 355<br>
	 * Message: Inappropriate enchant conditions.
	 */
	public static final SystemMessageId INAPPROPRIATE_ENCHANT_CONDITION;
	
	/**
	 * ID: 356<br>
	 * Message: Reject resurrection.
	 */
	public static final SystemMessageId REJECT_RESURRECTION;
	
	/**
	 * ID: 357<br>
	 * Message: It has already been spoiled.
	 */
	public static final SystemMessageId ALREADY_SPOILED;
	
	/**
	 * ID: 358<br>
	 * Message: $s1 hour(s) until castle siege conclusion.
	 */
	public static final SystemMessageId S1_HOURS_UNTIL_SIEGE_CONCLUSION;
	
	/**
	 * ID: 359<br>
	 * Message: $s1 minute(s) until castle siege conclusion.
	 */
	public static final SystemMessageId S1_MINUTES_UNTIL_SIEGE_CONCLUSION;
	
	/**
	 * ID: 360<br>
	 * Message: Castle siege $s1 second(s) left!
	 */
	public static final SystemMessageId CASTLE_SIEGE_S1_SECONDS_LEFT;
	
	/**
	 * ID: 361<br>
	 * Message: Over-hit!
	 */
	public static final SystemMessageId OVER_HIT;
	
	/**
	 * ID: 362<br>
	 * Message: You have acquired $s1 bonus experience from a successful over-hit.
	 */
	public static final SystemMessageId ACQUIRED_BONUS_EXPERIENCE_THROUGH_OVER_HIT;
	
	/**
	 * ID: 363<br>
	 * Message: Chat available time: $s1 minute.
	 */
	public static final SystemMessageId CHAT_AVAILABLE_S1_MINUTE;
	
	/**
	 * ID: 364<br>
	 * Message: Enter user's name to search
	 */
	public static final SystemMessageId ENTER_USER_NAME_TO_SEARCH;
	
	/**
	 * ID: 365<br>
	 * Message: Are you sure?
	 */
	public static final SystemMessageId ARE_YOU_SURE;
	
	/**
	 * ID: 366<br>
	 * Message: Please select your hair color.
	 */
	public static final SystemMessageId PLEASE_SELECT_HAIR_COLOR;
	
	/**
	 * ID: 367<br>
	 * Message: You cannot remove that clan character at this time.
	 */
	public static final SystemMessageId CANNOT_REMOVE_CLAN_CHARACTER;
	
	/**
	 * ID: 368<br>
	 * Message: Equipped +$s1 $s2.
	 */
	public static final SystemMessageId S1_S2_EQUIPPED;
	
	/**
	 * ID: 369<br>
	 * Message: You have obtained a +$s1 $s2.
	 */
	public static final SystemMessageId YOU_PICKED_UP_A_S1_S2;
	
	/**
	 * ID: 370<br>
	 * Message: Failed to pickup $s1.
	 */
	public static final SystemMessageId FAILED_PICKUP_S1;
	
	/**
	 * ID: 371<br>
	 * Message: Acquired +$s1 $s2.
	 */
	public static final SystemMessageId ACQUIRED_S1_S2;
	
	/**
	 * ID: 372<br>
	 * Message: Failed to earn $s1.
	 */
	public static final SystemMessageId FAILED_EARN_S1;
	
	/**
	 * ID: 373<br>
	 * Message: You are trying to destroy +$s1 $s2. Do you wish to continue?
	 */
	public static final SystemMessageId WISH_DESTROY_S1_S2;
	
	/**
	 * ID: 374<br>
	 * Message: You are attempting to crystallize +$s1 $s2. Do you wish to continue?
	 */
	public static final SystemMessageId WISH_CRYSTALLIZE_S1_S2;
	
	/**
	 * ID: 375<br>
	 * Message: You have dropped +$s1 $s2 .
	 */
	public static final SystemMessageId DROPPED_S1_S2;
	
	/**
	 * ID: 376<br>
	 * Message: $c1 has obtained +$s2$s3.
	 */
	public static final SystemMessageId C1_OBTAINED_S2_S3;
	
	/**
	 * ID: 377<br>
	 * Message: $S1 $S2 disappeared.
	 */
	public static final SystemMessageId S1_S2_DISAPPEARED;
	
	/**
	 * ID: 378<br>
	 * Message: $c1 purchased $s2.
	 */
	public static final SystemMessageId C1_PURCHASED_S2;
	
	/**
	 * ID: 379<br>
	 * Message: $c1 purchased +$s2$s3.
	 */
	public static final SystemMessageId C1_PURCHASED_S2_S3;
	
	/**
	 * ID: 380<br>
	 * Message: $c1 purchased $s3 $s2(s).
	 */
	public static final SystemMessageId C1_PURCHASED_S3_S2_S;
	
	/**
	 * ID: 381<br>
	 * Message: The game client encountered an error and was unable to connect to the petition server.
	 */
	public static final SystemMessageId GAME_CLIENT_UNABLE_TO_CONNECT_TO_PETITION_SERVER;
	
	/**
	 * ID: 382<br>
	 * Message: Currently there are no users that have checked out a GM ID.
	 */
	public static final SystemMessageId NO_USERS_CHECKED_OUT_GM_ID;
	
	/**
	 * ID: 383<br>
	 * Message: Request confirmed to end consultation at petition server.
	 */
	public static final SystemMessageId REQUEST_CONFIRMED_TO_END_CONSULTATION;
	
	/**
	 * ID: 384<br>
	 * Message: The client is not logged onto the game server.
	 */
	public static final SystemMessageId CLIENT_NOT_LOGGED_ONTO_GAME_SERVER;
	
	/**
	 * ID: 385<br>
	 * Message: Request confirmed to begin consultation at petition server.
	 */
	public static final SystemMessageId REQUEST_CONFIRMED_TO_BEGIN_CONSULTATION;
	
	/**
	 * ID: 386<br>
	 * Message: The body of your petition must be more than five characters in length.
	 */
	public static final SystemMessageId PETITION_MORE_THAN_FIVE_CHARACTERS;
	
	/**
	 * ID: 387<br>
	 * Message: This ends the GM petition consultation. Please take a moment to provide feedback about this service.
	 */
	public static final SystemMessageId THIS_END_THE_PETITION_PLEASE_PROVIDE_FEEDBACK;
	
	/**
	 * ID: 388<br>
	 * Message: Not under petition consultation.
	 */
	public static final SystemMessageId NOT_UNDER_PETITION_CONSULTATION;
	
	/**
	 * ID: 389<br>
	 * Message: our petition application has been accepted. - Receipt No. is $s1.
	 */
	public static final SystemMessageId PETITION_ACCEPTED_RECENT_NO_S1;
	
	/**
	 * ID: 390<br>
	 * Message: You may only submit one petition (active) at a time.
	 */
	public static final SystemMessageId ONLY_ONE_ACTIVE_PETITION_AT_TIME;
	
	/**
	 * ID: 391<br>
	 * Message: Receipt No. $s1, petition cancelled.
	 */
	public static final SystemMessageId RECENT_NO_S1_CANCELED;
	
	/**
	 * ID: 392<br>
	 * Message: Under petition advice.
	 */
	public static final SystemMessageId UNDER_PETITION_ADVICE;
	
	/**
	 * ID: 393<br>
	 * Message: Failed to cancel petition. Please try again later.
	 */
	public static final SystemMessageId FAILED_CANCEL_PETITION_TRY_LATER;
	
	/**
	 * ID: 394<br>
	 * Message: Starting petition consultation with $c1.
	 */
	public static final SystemMessageId STARTING_PETITION_WITH_C1;
	
	/**
	 * ID: 395<br>
	 * Message: Ending petition consultation with $c1.
	 */
	public static final SystemMessageId PETITION_ENDED_WITH_C1;
	
	/**
	 * ID: 396<br>
	 * Message: Please login after changing your temporary password.
	 */
	public static final SystemMessageId TRY_AGAIN_AFTER_CHANGING_PASSWORD;
	
	/**
	 * ID: 397<br>
	 * Message: Not a paid account.
	 */
	public static final SystemMessageId NO_PAID_ACCOUNT;
	
	/**
	 * ID: 398<br>
	 * Message: There is no time left on this account.
	 */
	public static final SystemMessageId NO_TIME_LEFT_ON_ACCOUNT;
	
	/**
	 * ID: 399<br>
	 * Message: System error.
	 */
	public static final SystemMessageId SYSTEM_ERROR;
	
	/**
	 * ID: 400<br>
	 * Message: You are attempting to drop $s1. Dou you wish to continue?
	 */
	public static final SystemMessageId WISH_TO_DROP_S1;
	
	/**
	 * ID: 401<br>
	 * Message: You have to many ongoing quests.
	 */
	public static final SystemMessageId TOO_MANY_QUESTS;
	
	/**
	 * ID: 402<br>
	 * Message: You do not possess the correct ticket to board the boat.
	 */
	public static final SystemMessageId NOT_CORRECT_BOAT_TICKET;
	
	/**
	 * ID: 403<br>
	 * Message: You have exceeded your out-of-pocket adena limit.
	 */
	public static final SystemMessageId EXCEECED_POCKET_ADENA_LIMIT;
	
	/**
	 * ID: 404<br>
	 * Message: Your Create Item level is too low to register this recipe.
	 */
	public static final SystemMessageId CREATE_LVL_TOO_LOW_TO_REGISTER;
	
	/**
	 * ID: 405<br>
	 * Message: The total price of the product is too high.
	 */
	public static final SystemMessageId TOTAL_PRICE_TOO_HIGH;
	
	/**
	 * ID: 406<br>
	 * Message: Petition application accepted.
	 */
	public static final SystemMessageId PETITION_APP_ACCEPTED;
	
	/**
	 * ID: 407<br>
	 * Message: Petition under process.
	 */
	public static final SystemMessageId PETITION_UNDER_PROCESS;
	
	/**
	 * ID: 408<br>
	 * Message: Set Period
	 */
	public static final SystemMessageId SET_PERIOD;
	
	/**
	 * ID: 409<br>
	 * Message: Set Time-$s1:$s2:$s3
	 */
	public static final SystemMessageId SET_TIME_S1_S2_S3;
	
	/**
	 * ID: 410<br>
	 * Message: Registration Period
	 */
	public static final SystemMessageId REGISTRATION_PERIOD;
	
	/**
	 * ID: 411<br>
	 * Message: Registration Time-$s1:$s2:$s3
	 */
	public static final SystemMessageId REGISTRATION_TIME_S1_S2_S3;
	
	/**
	 * ID: 412<br>
	 * Message: Battle begins in $s1:$s2:$s3
	 */
	public static final SystemMessageId BATTLE_BEGINS_S1_S2_S3;
	
	/**
	 * ID: 413<br>
	 * Message: Battle ends in $s1:$s2:$s3
	 */
	public static final SystemMessageId BATTLE_ENDS_S1_S2_S3;
	
	/**
	 * ID: 414<br>
	 * Message: Standby
	 */
	public static final SystemMessageId STANDBY;
	
	/**
	 * ID: 415<br>
	 * Message: Under Siege
	 */
	public static final SystemMessageId UNDER_SIEGE;
	
	/**
	 * ID: 416<br>
	 * Message: This item cannot be exchanged.
	 */
	public static final SystemMessageId ITEM_CANNOT_EXCHANGE;
	
	/**
	 * ID: 417<br>
	 * Message: $s1 has been disarmed.
	 */
	public static final SystemMessageId S1_DISARMED;
	
	/**
	 * ID: 419<br>
	 * Message: $s1 minute(s) of usage time left.
	 */
	public static final SystemMessageId S1_MINUTES_USAGE_LEFT;
	
	/**
	 * ID: 420<br>
	 * Message: Time expired.
	 */
	public static final SystemMessageId TIME_EXPIRED;
	
	/**
	 * ID: 421<br>
	 * Message: Another person has logged in with the same account.
	 */
	public static final SystemMessageId ANOTHER_LOGIN_WITH_ACCOUNT;
	
	/**
	 * ID: 422<br>
	 * Message: You have exceeded the weight limit.
	 */
	public static final SystemMessageId WEIGHT_LIMIT_EXCEEDED;
	
	/**
	 * ID: 423<br>
	 * Message: You have cancelled the enchanting process.
	 */
	public static final SystemMessageId ENCHANT_SCROLL_CANCELLED;
	
	/**
	 * ID: 424<br>
	 * Message: Does not fit strengthening conditions of the scroll.
	 */
	public static final SystemMessageId DOES_NOT_FIT_SCROLL_CONDITIONS;
	
	/**
	 * ID: 425<br>
	 * Message: Your Create Item level is too low to register this recipe.
	 */
	public static final SystemMessageId CREATE_LVL_TOO_LOW_TO_REGISTER2;
	
	/**
	 * ID: 445<br>
	 * Message: (Reference Number Regarding Membership Withdrawal Request: $s1)
	 */
	public static final SystemMessageId REFERENCE_MEMBERSHIP_WITHDRAWAL_S1;
	
	/**
	 * ID: 447<br>
	 * Message: .
	 */
	public static final SystemMessageId DOT;
	
	/**
	 * ID: 448<br>
	 * Message: There is a system error. Please log in again later.
	 */
	public static final SystemMessageId SYSTEM_ERROR_LOGIN_LATER;
	
	/**
	 * ID: 449<br>
	 * Message: The password you have entered is incorrect.
	 */
	public static final SystemMessageId PASSWORD_ENTERED_INCORRECT1;
	
	/**
	 * ID: 450<br>
	 * Message: Confirm your account information and log in later.
	 */
	public static final SystemMessageId CONFIRM_ACCOUNT_LOGIN_LATER;
	
	/**
	 * ID: 451<br>
	 * Message: The password you have entered is incorrect.
	 */
	public static final SystemMessageId PASSWORD_ENTERED_INCORRECT2;
	
	/**
	 * ID: 452<br>
	 * Message: Please confirm your account information and try logging in later.
	 */
	public static final SystemMessageId PLEASE_CONFIRM_ACCOUNT_LOGIN_LATER;
	
	/**
	 * ID: 453<br>
	 * Message: Your account information is incorrect.
	 */
	public static final SystemMessageId ACCOUNT_INFORMATION_INCORRECT;
	
	/**
	 * ID: 455<br>
	 * Message: Account is already in use. Unable to log in.
	 */
	public static final SystemMessageId ACCOUNT_IN_USE;
	
	/**
	 * ID: 456<br>
	 * Message: Lineage II game services may be used by individuals 15 years of age or older except for PvP servers,which may only be used by adults 18 years of age and older (Korea Only)
	 */
	public static final SystemMessageId LINAGE_MINIMUM_AGE;
	
	/**
	 * ID: 457<br>
	 * Message: Currently undergoing game server maintenance. Please log in again later.
	 */
	public static final SystemMessageId SERVER_MAINTENANCE;
	
	/**
	 * ID: 458<br>
	 * Message: Your usage term has expired.
	 */
	public static final SystemMessageId USAGE_TERM_EXPIRED;
	
	/**
	 * ID: 460<br>
	 * Message: to reactivate your account.
	 */
	public static final SystemMessageId TO_REACTIVATE_YOUR_ACCOUNT;
	
	/**
	 * ID: 461<br>
	 * Message: Access failed.
	 */
	public static final SystemMessageId ACCESS_FAILED;
	
	/**
	 * ID: 462<br>
	 * Message: Please try again later.
	 */
	public static final SystemMessageId PLEASE_TRY_AGAIN_LATER;
	
	/**
	 * ID: 464<br>
	 * Message: This feature is only available alliance leaders.
	 */
	public static final SystemMessageId FEATURE_ONLY_FOR_ALLIANCE_LEADER;
	
	/**
	 * ID: 465<br>
	 * Message: You are not currently allied with any clans.
	 */
	public static final SystemMessageId NO_CURRENT_ALLIANCES;
	
	/**
	 * ID: 466<br>
	 * Message: You have exceeded the limit.
	 */
	public static final SystemMessageId YOU_HAVE_EXCEEDED_THE_LIMIT;
	
	/**
	 * ID: 467<br>
	 * Message: You may not accept any clan within a day after expelling another clan.
	 */
	public static final SystemMessageId CANT_INVITE_CLAN_WITHIN_1_DAY;
	
	/**
	 * ID: 468<br>
	 * Message: A clan that has withdrawn or been expelled cannot enter into an alliance within one day of withdrawal or expulsion.
	 */
	public static final SystemMessageId CANT_ENTER_ALLIANCE_WITHIN_1_DAY;
	
	/**
	 * ID: 469<br>
	 * Message: You may not ally with a clan you are currently at war with. That would be diabolical and treacherous.
	 */
	public static final SystemMessageId MAY_NOT_ALLY_CLAN_BATTLE;
	
	/**
	 * ID: 470<br>
	 * Message: Only the clan leader may apply for withdrawal from the alliance.
	 */
	public static final SystemMessageId ONLY_CLAN_LEADER_WITHDRAW_ALLY;
	
	/**
	 * ID: 471<br>
	 * Message: Alliance leaders cannot withdraw.
	 */
	public static final SystemMessageId ALLIANCE_LEADER_CANT_WITHDRAW;
	
	/**
	 * ID: 472<br>
	 * Message: You cannot expel yourself from the clan.
	 */
	public static final SystemMessageId CANNOT_EXPEL_YOURSELF;
	
	/**
	 * ID: 473<br>
	 * Message: Different alliance.
	 */
	public static final SystemMessageId DIFFERENT_ALLIANCE;
	
	/**
	 * ID: 474<br>
	 * Message: That clan does not exist.
	 */
	public static final SystemMessageId CLAN_DOESNT_EXISTS;
	
	/**
	 * ID: 475<br>
	 * Message: Different alliance.
	 */
	public static final SystemMessageId DIFFERENT_ALLIANCE2;
	
	/**
	 * ID: 476<br>
	 * Message: Please adjust the image size to 8x12.
	 */
	public static final SystemMessageId ADJUST_IMAGE_8_12;
	
	/**
	 * ID: 477<br>
	 * Message: No response. Invitation to join an alliance has been cancelled.
	 */
	public static final SystemMessageId NO_RESPONSE_TO_ALLY_INVITATION;
	
	/**
	 * ID: 478<br>
	 * Message: No response. Your entrance to the alliance has been cancelled.
	 */
	public static final SystemMessageId YOU_DID_NOT_RESPOND_TO_ALLY_INVITATION;
	
	/**
	 * ID: 479<br>
	 * Message: $s1 has joined as a friend.
	 */
	public static final SystemMessageId S1_JOINED_AS_FRIEND;
	
	/**
	 * ID: 480<br>
	 * Message: Please check your friend list.
	 */
	public static final SystemMessageId PLEASE_CHECK_YOUR_FRIENDS_LIST;
	
	/**
	 * ID: 481<br>
	 * Message: $s1 has been deleted from your friends list.
	 */
	public static final SystemMessageId S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST;
	
	/**
	 * ID: 482<br>
	 * Message: You cannot add yourself to your own friend list.
	 */
	public static final SystemMessageId YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIENDS_LIST;
	
	/**
	 * ID: 483<br>
	 * Message: This function is inaccessible right now. Please try again later.
	 */
	public static final SystemMessageId FUNCTION_INACCESSIBLE_NOW;
	
	/**
	 * ID: 484<br>
	 * Message: This player is already registered in your friends list.
	 */
	public static final SystemMessageId S1_ALREADY_IN_FRIENDS_LIST;
	
	/**
	 * ID: 485<br>
	 * Message: No new friend invitations may be accepted.
	 */
	public static final SystemMessageId NO_NEW_INVITATIONS_ACCEPTED;
	
	/**
	 * ID: 486<br>
	 * Message: The following user is not in your friends list.
	 */
	public static final SystemMessageId THE_USER_NOT_IN_FRIENDS_LIST;
	
	/**
	 * ID: 487<br>
	 * Message: ======<Friends List>======
	 */
	public static final SystemMessageId FRIEND_LIST_HEADER;
	
	/**
	 * ID: 488<br>
	 * Message: $s1 (Currently: Online)
	 */
	public static final SystemMessageId S1_ONLINE;
	
	/**
	 * ID: 489<br>
	 * Message: $s1 (Currently: Offline)
	 */
	public static final SystemMessageId S1_OFFLINE;
	
	/**
	 * ID: 490<br>
	 * Message: ========================
	 */
	public static final SystemMessageId FRIEND_LIST_FOOTER;
	
	/**
	 * ID: 491<br>
	 * Message: =======<Alliance Information>=======
	 */
	public static final SystemMessageId ALLIANCE_INFO_HEAD;
	
	/**
	 * ID: 492<br>
	 * Message: Alliance Name: $s1
	 */
	public static final SystemMessageId ALLIANCE_NAME_S1;
	
	/**
	 * ID: 493<br>
	 * Message: Connection: $s1 / Total $s2
	 */
	public static final SystemMessageId CONNECTION_S1_TOTAL_S2;
	
	/**
	 * ID: 494<br>
	 * Message: Alliance Leader: $s2 of $s1
	 */
	public static final SystemMessageId ALLIANCE_LEADER_S2_OF_S1;
	
	/**
	 * ID: 495<br>
	 * Message: Affiliated clans: Total $s1 clan(s)
	 */
	public static final SystemMessageId ALLIANCE_CLAN_TOTAL_S1;
	
	/**
	 * ID: 496<br>
	 * Message: =====<Clan Information>=====
	 */
	public static final SystemMessageId CLAN_INFO_HEAD;
	
	/**
	 * ID: 497<br>
	 * Message: Clan Name: $s1
	 */
	public static final SystemMessageId CLAN_INFO_NAME_S1;
	
	/**
	 * ID: 498<br>
	 * Message: Clan Leader: $s1
	 */
	public static final SystemMessageId CLAN_INFO_LEADER_S1;
	
	/**
	 * ID: 499<br>
	 * Message: Clan Level: $s1
	 */
	public static final SystemMessageId CLAN_INFO_LEVEL_S1;
	
	/**
	 * ID: 500<br>
	 * Message: ------------------------
	 */
	public static final SystemMessageId CLAN_INFO_SEPARATOR;
	
	/**
	 * ID: 501<br>
	 * Message: ========================
	 */
	public static final SystemMessageId CLAN_INFO_FOOT;
	
	/**
	 * ID: 502<br>
	 * Message: You already belong to another alliance.
	 */
	public static final SystemMessageId ALREADY_JOINED_ALLIANCE;
	
	/**
	 * ID: 503<br>
	 * Message: $s1 (Friend) has logged in.
	 */
	public static final SystemMessageId FRIEND_S1_HAS_LOGGED_IN;
	
	/**
	 * ID: 504<br>
	 * Message: Only clan leaders may create alliances.
	 */
	public static final SystemMessageId ONLY_CLAN_LEADER_CREATE_ALLIANCE;
	
	/**
	 * ID: 505<br>
	 * Message: You cannot create a new alliance within 10 days after dissolution.
	 */
	public static final SystemMessageId CANT_CREATE_ALLIANCE_10_DAYS_DISOLUTION;
	
	/**
	 * ID: 506<br>
	 * Message: Incorrect alliance name. Please try again.
	 */
	public static final SystemMessageId INCORRECT_ALLIANCE_NAME;
	
	/**
	 * ID: 507<br>
	 * Message: Incorrect length for an alliance name.
	 */
	public static final SystemMessageId INCORRECT_ALLIANCE_NAME_LENGTH;
	
	/**
	 * ID: 508<br>
	 * Message: This alliance name already exists.
	 */
	public static final SystemMessageId ALLIANCE_ALREADY_EXISTS;
	
	/**
	 * ID: 509<br>
	 * Message: Cannot accept. clan ally is registered as an enemy during siege battle.
	 */
	public static final SystemMessageId CANT_ACCEPT_ALLY_ENEMY_FOR_SIEGE;
	
	/**
	 * ID: 510<br>
	 * Message: You have invited someone to your alliance.
	 */
	public static final SystemMessageId YOU_INVITED_FOR_ALLIANCE;
	
	/**
	 * ID: 511<br>
	 * Message: You must first select a user to invite.
	 */
	public static final SystemMessageId SELECT_USER_TO_INVITE;
	
	/**
	 * ID: 512<br>
	 * Message: Do you really wish to withdraw from the alliance?
	 */
	public static final SystemMessageId DO_YOU_WISH_TO_WITHDRW;
	
	/**
	 * ID: 513<br>
	 * Message: Enter the name of the clan you wish to expel.
	 */
	public static final SystemMessageId ENTER_NAME_CLAN_TO_EXPEL;
	
	/**
	 * ID: 514<br>
	 * Message: Do you really wish to dissolve the alliance?
	 */
	public static final SystemMessageId DO_YOU_WISH_TO_DISOLVE;
	
	/**
	 * ID: 516<br>
	 * Message: $s1 has invited you to be their friend.
	 */
	public static final SystemMessageId SI_INVITED_YOU_AS_FRIEND;
	
	/**
	 * ID: 517<br>
	 * Message: You have accepted the alliance.
	 */
	public static final SystemMessageId YOU_ACCEPTED_ALLIANCE;
	
	/**
	 * ID: 518<br>
	 * Message: You have failed to invite a clan into the alliance.
	 */
	public static final SystemMessageId FAILED_TO_INVITE_CLAN_IN_ALLIANCE;
	
	/**
	 * ID: 519<br>
	 * Message: You have withdrawn from the alliance.
	 */
	public static final SystemMessageId YOU_HAVE_WITHDRAWN_FROM_ALLIANCE;
	
	/**
	 * ID: 520<br>
	 * Message: You have failed to withdraw from the alliance.
	 */
	public static final SystemMessageId YOU_HAVE_FAILED_TO_WITHDRAWN_FROM_ALLIANCE;
	
	/**
	 * ID: 521<br>
	 * Message: You have succeeded in expelling a clan.
	 */
	public static final SystemMessageId YOU_HAVE_EXPELED_A_CLAN;
	
	/**
	 * ID: 522<br>
	 * Message: You have failed to expel a clan.
	 */
	public static final SystemMessageId FAILED_TO_EXPELED_A_CLAN;
	
	/**
	 * ID: 523<br>
	 * Message: The alliance has been dissolved.
	 */
	public static final SystemMessageId ALLIANCE_DISOLVED;
	
	/**
	 * ID: 524<br>
	 * Message: You have failed to dissolve the alliance.
	 */
	public static final SystemMessageId FAILED_TO_DISOLVE_ALLIANCE;
	
	/**
	 * ID: 525<br>
	 * Message: You have succeeded in inviting a friend to your friends list.
	 */
	public static final SystemMessageId YOU_HAVE_SUCCEEDED_INVITING_FRIEND;
	
	/**
	 * ID: 526<br>
	 * Message: You have failed to add a friend to your friends list.
	 */
	public static final SystemMessageId FAILED_TO_INVITE_A_FRIEND;
	
	/**
	 * ID: 527<br>
	 * Message: $s1 leader, $s2, has requested an alliance.
	 */
	public static final SystemMessageId S2_ALLIANCE_LEADER_OF_S1_REQUESTED_ALLIANCE;
	
	/**
	 * ID: 530<br>
	 * Message: The Spiritshot does not match the weapon's grade.
	 */
	public static final SystemMessageId SPIRITSHOTS_GRADE_MISMATCH;
	
	/**
	 * ID: 531<br>
	 * Message: You do not have enough Spiritshots for that.
	 */
	public static final SystemMessageId NOT_ENOUGH_SPIRITSHOTS;
	
	/**
	 * ID: 532<br>
	 * Message: You may not use Spiritshots.
	 */
	public static final SystemMessageId CANNOT_USE_SPIRITSHOTS;
	
	/**
	 * ID: 533<br>
	 * Message: Power of Mana enabled.
	 */
	public static final SystemMessageId ENABLED_SPIRITSHOT;
	
	/**
	 * ID: 534<br>
	 * Message: Power of Mana disabled.
	 */
	public static final SystemMessageId DISABLED_SPIRITSHOT;
	
	/**
	 * ID: 535<br>
	 * Message: Enter a name for your pet.
	 */
	
	/**
	 * ID: 536<br>
	 * Message: How much adena do you wish to transfer to your Inventory?
	 */
	public static final SystemMessageId HOW_MUCH_ADENA_TRANSFER;
	
	/**
	 * ID: 537<br>
	 * Message: How much will you transfer?
	 */
	public static final SystemMessageId HOW_MUCH_TRANSFER;
	
	/**
	 * ID: 538<br>
	 * Message: Your SP has decreased by $s1.
	 */
	public static final SystemMessageId SP_DECREASED_S1;
	
	/**
	 * ID: 539<br>
	 * Message: Your Experience has decreased by $s1.
	 */
	public static final SystemMessageId EXP_DECREASED_BY_S1;
	
	/**
	 * ID: 540<br>
	 * Message: Clan leaders may not be deleted. Dissolve the clan first and try again.
	 */
	public static final SystemMessageId CLAN_LEADERS_MAY_NOT_BE_DELETED;
	
	/**
	 * ID: 541<br>
	 * Message: You may not delete a clan member. Withdraw from the clan first and try again.
	 */
	public static final SystemMessageId CLAN_MEMBER_MAY_NOT_BE_DELETED;
	
	/**
	 * ID: 542<br>
	 * Message: The NPC server is currently down. Pets and servitors cannot be summoned at this time.
	 */
	public static final SystemMessageId THE_NPC_SERVER_IS_CURRENTLY_DOWN;
	
	/**
	 * ID: 543<br>
	 * Message: You already have a pet.
	 */
	public static final SystemMessageId YOU_ALREADY_HAVE_A_PET;
	
	/**
	 * ID: 544<br>
	 * Message: Your pet cannot carry this item.
	 */
	public static final SystemMessageId ITEM_NOT_FOR_PETS;
	
	/**
	 * ID: 545<br>
	 * Message: Your pet cannot carry any more items. Remove some, then try again.
	 */
	public static final SystemMessageId YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS;
	
	/**
	 * ID: 546<br>
	 * Message: Unable to place item, your pet is too encumbered.
	 */
	public static final SystemMessageId UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED;
	
	/**
	 * ID: 547<br>
	 * Message: Summoning your pet.
	 */
	public static final SystemMessageId SUMMON_A_PET;
	
	/**
	 * ID: 548<br>
	 * Message: Your pet's name can be up to 8 characters in length.
	 */
	public static final SystemMessageId NAMING_PETNAME_UP_TO_8CHARS;
	
	/**
	 * ID: 549<br>
	 * Message: To create an alliance, your clan must be Level 5 or higher.
	 */
	public static final SystemMessageId TO_CREATE_AN_ALLY_YOU_CLAN_MUST_BE_LEVEL_5_OR_HIGHER;
	
	/**
	 * ID: 550<br>
	 * Message: You may not create an alliance during the term of dissolution postponement.
	 */
	public static final SystemMessageId YOU_MAY_NOT_CREATE_ALLY_WHILE_DISSOLVING;
	
	/**
	 * ID: 551<br>
	 * Message: You cannot raise your clan level during the term of dispersion postponement.
	 */
	public static final SystemMessageId CANNOT_RISE_LEVEL_WHILE_DISSOLUTION_IN_PROGRESS;
	
	/**
	 * ID: 552<br>
	 * Message: During the grace period for dissolving a clan, the registration or deletion of a clan's crest is not allowed.
	 */
	public static final SystemMessageId CANNOT_SET_CREST_WHILE_DISSOLUTION_IN_PROGRESS;
	
	/**
	 * ID: 553<br>
	 * Message: The opposing clan has applied for dispersion.
	 */
	public static final SystemMessageId OPPOSING_CLAN_APPLIED_DISPERSION;
	
	/**
	 * ID: 554<br>
	 * Message: You cannot disperse the clans in your alliance.
	 */
	public static final SystemMessageId CANNOT_DISPERSE_THE_CLANS_IN_ALLY;
	
	/**
	 * ID: 555<br>
	 * Message: You cannot move - you are too encumbered
	 */
	public static final SystemMessageId CANT_MOVE_TOO_ENCUMBERED;
	
	/**
	 * ID: 556<br>
	 * Message: You cannot move in this state
	 */
	public static final SystemMessageId CANT_MOVE_IN_THIS_STATE;
	
	/**
	 * ID: 557<br>
	 * Message: Your pet has been summoned and may not be destroyed
	 */
	public static final SystemMessageId PET_SUMMONED_MAY_NOT_DESTROYED;
	
	/**
	 * ID: 558<br>
	 * Message: Your pet has been summoned and may not be let go.
	 */
	public static final SystemMessageId PET_SUMMONED_MAY_NOT_LET_GO;
	
	/**
	 * ID: 559<br>
	 * Message: You have purchased $s2 from $c1.
	 */
	public static final SystemMessageId PURCHASED_S2_FROM_C1;
	
	/**
	 * ID: 560<br>
	 * Message: You have purchased +$s2 $s3 from $c1.
	 */
	public static final SystemMessageId PURCHASED_S2_S3_FROM_C1;
	
	/**
	 * ID: 561<br>
	 * Message: You have purchased $s3 $s2(s) from $c1.
	 */
	public static final SystemMessageId PURCHASED_S3_S2_S_FROM_C1;
	
	/**
	 * ID: 562<br>
	 * Message: You may not crystallize this item. Your crystallization skill level is too low.
	 */
	public static final SystemMessageId CRYSTALLIZE_LEVEL_TOO_LOW;
	
	/**
	 * ID: 563<br>
	 * Message: Failed to disable attack target.
	 */
	public static final SystemMessageId FAILED_DISABLE_TARGET;
	
	/**
	 * ID: 564<br>
	 * Message: Failed to change attack target.
	 */
	public static final SystemMessageId FAILED_CHANGE_TARGET;
	
	/**
	 * ID: 565<br>
	 * Message: Not enough luck.
	 */
	public static final SystemMessageId NOT_ENOUGH_LUCK;
	
	/**
	 * ID: 566<br>
	 * Message: Your confusion spell failed.
	 */
	public static final SystemMessageId CONFUSION_FAILED;
	
	/**
	 * ID: 567<br>
	 * Message: Your fear spell failed.
	 */
	public static final SystemMessageId FEAR_FAILED;
	
	/**
	 * ID: 568<br>
	 * Message: Cubic Summoning failed.
	 */
	public static final SystemMessageId CUBIC_SUMMONING_FAILED;
	
	/**
	 * ID: 569<br>
	 * Message: Caution -- this item's price greatly differs from non-player run shops. Do you wish to continue?
	 */
	
	/**
	 * ID: 570<br>
	 * ID: 571<br>
	 * Message: How many $s1(s) do you want to purchase?
	 */
	
	/**
	 * ID: 572<br>
	 * Message: Do you accept $c1's party invitation? (Item Distribution: Finders Keepers.)
	 */
	public static final SystemMessageId C1_INVITED_YOU_TO_PARTY_FINDERS_KEEPERS;
	
	/**
	 * ID: 573<br>
	 * Message: Do you accept $c1's party invitation? (Item Distribution: Random.)
	 */
	public static final SystemMessageId C1_INVITED_YOU_TO_PARTY_RANDOM;
	
	/**
	 * ID: 574<br>
	 * Message: Pets and Servitors are not available at this time.
	 */
	public static final SystemMessageId PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME;
	
	/**
	 * ID: 575<br>
	 * Message: How much adena do you wish to transfer to your pet?
	 */
	public static final SystemMessageId HOW_MUCH_ADENA_TRANSFER_TO_PET;
	
	/**
	 * ID: 576<br>
	 * Message: How much do you wish to transfer?
	 */
	public static final SystemMessageId HOW_MUCH_TRANSFER2;
	
	/**
	 * ID: 577<br>
	 * Message: You cannot summon during a trade or while using the private shops.
	 */
	public static final SystemMessageId CANNOT_SUMMON_DURING_TRADE_SHOP;
	
	/**
	 * ID: 578<br>
	 * Message: You cannot summon during combat.
	 */
	public static final SystemMessageId YOU_CANNOT_SUMMON_IN_COMBAT;
	
	/**
	 * ID: 579<br>
	 * Message: A pet cannot be sent back during battle.
	 */
	public static final SystemMessageId PET_CANNOT_SENT_BACK_DURING_BATTLE;
	
	/**
	 * ID: 580<br>
	 * Message: You may not use multiple pets or servitors at the same time.
	 */
	public static final SystemMessageId SUMMON_ONLY_ONE;
	
	/**
	 * ID: 581<br>
	 * Message: There is a space in the name.
	 */
	public static final SystemMessageId NAMING_THERE_IS_A_SPACE;
	
	/**
	 * ID: 582<br>
	 * Message: Inappropriate character name.
	 */
	public static final SystemMessageId NAMING_INAPPROPRIATE_CHARACTER_NAME;
	
	/**
	 * ID: 583<br>
	 * Message: Name includes forbidden words.
	 */
	public static final SystemMessageId NAMING_INCLUDES_FORBIDDEN_WORDS;
	
	/**
	 * ID: 584<br>
	 * Message: This is already in use by another pet.
	 */
	public static final SystemMessageId NAMING_ALREADY_IN_USE_BY_ANOTHER_PET;
	
	/**
	 * ID: 585<br>
	 * Message: Please decide on the price.
	 */
	public static final SystemMessageId DECIDE_ON_PRICE;
	
	/**
	 * ID: 586<br>
	 * Message: Pet items cannot be registered as shortcuts.
	 */
	public static final SystemMessageId PET_NO_SHORTCUT;
	
	/**
	 * ID: 588<br>
	 * Message: Your pet's inventory is full.
	 */
	public static final SystemMessageId PET_INVENTORY_FULL;
	
	/**
	 * ID: 589<br>
	 * Message: A dead pet cannot be sent back.
	 */
	public static final SystemMessageId DEAD_PET_CANNOT_BE_RETURNED;
	
	/**
	 * ID: 590<br>
	 * Message: Your pet is motionless and any attempt you make to give it something goes unrecognized.
	 */
	public static final SystemMessageId CANNOT_GIVE_ITEMS_TO_DEAD_PET;
	
	/**
	 * ID: 591<br>
	 * Message: An invalid character is included in the pet's name.
	 */
	public static final SystemMessageId NAMING_PETNAME_CONTAINS_INVALID_CHARS;
	
	/**
	 * ID: 592<br>
	 * Message: Do you wish to dismiss your pet? Dismissing your pet will cause the pet necklace to disappear
	 */
	public static final SystemMessageId WISH_TO_DISMISS_PET;
	
	/**
	 * ID: 593<br>
	 * Message: Starving, grumpy and fed up, your pet has left.
	 */
	public static final SystemMessageId STARVING_GRUMPY_AND_FED_UP_YOUR_PET_HAS_LEFT;
	
	/**
	 * ID: 594<br>
	 * Message: You may not restore a hungry pet.
	 */
	public static final SystemMessageId YOU_CANNOT_RESTORE_HUNGRY_PETS;
	
	/**
	 * ID: 595<br>
	 * Message: Your pet is very hungry.
	 */
	public static final SystemMessageId YOUR_PET_IS_VERY_HUNGRY;
	
	/**
	 * ID: 596<br>
	 * Message: Your pet ate a little, but is still hungry.
	 */
	public static final SystemMessageId YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY;
	
	/**
	 * ID: 597<br>
	 * Message: Your pet is very hungry. Please be careful.
	 */
	public static final SystemMessageId YOUR_PET_IS_VERY_HUNGRY_PLEASE_BE_CAREFULL;
	
	/**
	 * ID: 598<br>
	 * Message: You may not chat while you are invisible.
	 */
	public static final SystemMessageId NOT_CHAT_WHILE_INVISIBLE;
	
	/**
	 * ID: 599<br>
	 * Message: The GM has an important notice. Chat has been temporarily disabled.
	 */
	public static final SystemMessageId GM_NOTICE_CHAT_DISABLED;
	
	/**
	 * ID: 600<br>
	 * Message: You may not equip a pet item.
	 */
	public static final SystemMessageId CANNOT_EQUIP_PET_ITEM;
	
	/**
	 * ID: 601<br>
	 * Message: There are $S1 petitions currently on the waiting list.
	 */
	public static final SystemMessageId S1_PETITION_ON_WAITING_LIST;
	
	/**
	 * ID: 602<br>
	 * Message: The petition system is currently unavailable. Please try again later.
	 */
	public static final SystemMessageId PETITION_SYSTEM_CURRENT_UNAVAILABLE;
	
	/**
	 * ID: 603<br>
	 * Message: That item cannot be discarded or exchanged.
	 */
	public static final SystemMessageId CANNOT_DISCARD_EXCHANGE_ITEM;
	
	/**
	 * ID: 604<br>
	 * Message: You may not call forth a pet or summoned creature from this location
	 */
	public static final SystemMessageId NOT_CALL_PET_FROM_THIS_LOCATION;
	
	/**
	 * ID: 605<br>
	 * Message: You may register up to 64 people on your list.
	 */
	public static final SystemMessageId MAY_REGISTER_UP_TO_64_PEOPLE;
	
	/**
	 * ID: 606<br>
	 * Message: You cannot be registered because the other person has already registered 64 people on his/her list.
	 */
	public static final SystemMessageId OTHER_PERSON_ALREADY_64_PEOPLE;
	
	/**
	 * ID: 607<br>
	 * Message: You do not have any further skills to learn. Come back when you have reached Level $s1.
	 */
	public static final SystemMessageId DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1;
	
	/**
	 * ID: 608<br>
	 * Message: $c1 has obtained $s3 $s2 by using Sweeper.
	 */
	public static final SystemMessageId C1_SWEEPED_UP_S3_S2;
	
	/**
	 * ID: 609<br>
	 * Message: $c1 has obtained $s2 by using Sweeper.
	 */
	public static final SystemMessageId C1_SWEEPED_UP_S2;
	
	/**
	 * ID: 610<br>
	 * Message: Your skill has been canceled due to lack of HP.
	 */
	public static final SystemMessageId SKILL_REMOVED_DUE_LACK_HP;
	
	/**
	 * ID: 611<br>
	 * Message: You have succeeded in Confusing the enemy.
	 */
	public static final SystemMessageId CONFUSING_SUCCEEDED;
	
	/**
	 * ID: 612<br>
	 * Message: The Spoil condition has been activated.
	 */
	public static final SystemMessageId SPOIL_SUCCESS;
	
	/**
	 * ID: 613<br>
	 * Message: ======<Ignore List>======
	 */
	public static final SystemMessageId BLOCK_LIST_HEADER;
	
	/**
	 * ID: 614<br>
	 * Message: $c1 : $c2
	 */
	public static final SystemMessageId C1_D_C2;
	
	/**
	 * ID: 615<br>
	 * Message: You have failed to register the user to your Ignore List.
	 */
	public static final SystemMessageId FAILED_TO_REGISTER_TO_IGNORE_LIST;
	
	/**
	 * ID: 616<br>
	 * Message: You have failed to delete the character.
	 */
	public static final SystemMessageId FAILED_TO_DELETE_CHARACTER;
	
	/**
	 * ID: 617<br>
	 * Message: $s1 has been added to your Ignore List.
	 */
	public static final SystemMessageId S1_WAS_ADDED_TO_YOUR_IGNORE_LIST;
	
	/**
	 * ID: 618<br>
	 * Message: $s1 has been removed from your Ignore List.
	 */
	public static final SystemMessageId S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST;
	
	/**
	 * ID: 619<br>
	 * Message: $s1 has placed you on his/her Ignore List.
	 */
	public static final SystemMessageId S1_HAS_ADDED_YOU_TO_IGNORE_LIST;
	
	/**
	 * ID: 620<br>
	 * Message: $s1 has placed you on his/her Ignore List.
	 */
	public static final SystemMessageId S1_HAS_ADDED_YOU_TO_IGNORE_LIST2;
	
	/**
	 * ID: 621<br>
	 * Message: Game connection attempted through a restricted IP.
	 */
	public static final SystemMessageId CONNECTION_RESTRICTED_IP;
	
	/**
	 * ID: 622<br>
	 * Message: You may not make a declaration of war during an alliance battle.
	 */
	public static final SystemMessageId NO_WAR_DURING_ALLY_BATTLE;
	
	/**
	 * ID: 623<br>
	 * Message: Your opponent has exceeded the number of simultaneous alliance battles alllowed.
	 */
	public static final SystemMessageId OPPONENT_TOO_MUCH_ALLY_BATTLES1;
	
	/**
	 * ID: 624<br>
	 * Message: $s1 Clan leader is not currently connected to the game server.
	 */
	public static final SystemMessageId S1_LEADER_NOT_CONNECTED;
	
	/**
	 * ID: 625<br>
	 * Message: Your request for Alliance Battle truce has been denied.
	 */
	public static final SystemMessageId ALLY_BATTLE_TRUCE_DENIED;
	
	/**
	 * ID: 626<br>
	 * Message: The $s1 clan did not respond: war proclamation has been refused.
	 */
	public static final SystemMessageId WAR_PROCLAMATION_HAS_BEEN_REFUSED;
	
	/**
	 * ID: 627<br>
	 * Message: Clan battle has been refused because you did not respond to $s1 clan's war proclamation.
	 */
	public static final SystemMessageId YOU_REFUSED_CLAN_WAR_PROCLAMATION;
	
	/**
	 * ID: 628<br>
	 * Message: You have already been at war with the $s1 clan: 5 days must pass before you can declare war again.
	 */
	public static final SystemMessageId ALREADY_AT_WAR_WITH_S1_WAIT_5_DAYS;
	
	/**
	 * ID: 629<br>
	 * Message: Your opponent has exceeded the number of simultaneous alliance battles alllowed.
	 */
	public static final SystemMessageId OPPONENT_TOO_MUCH_ALLY_BATTLES2;
	
	/**
	 * ID: 630<br>
	 * Message: War with the clan has begun.
	 */
	public static final SystemMessageId WAR_WITH_CLAN_BEGUN;
	
	/**
	 * ID: 631<br>
	 * Message: War with the clan is over.
	 */
	public static final SystemMessageId WAR_WITH_CLAN_ENDED;
	
	/**
	 * ID: 632<br>
	 * Message: You have won the war over the clan!
	 */
	public static final SystemMessageId WON_WAR_OVER_CLAN;
	
	/**
	 * ID: 633<br>
	 * Message: You have surrendered to the clan.
	 */
	public static final SystemMessageId SURRENDERED_TO_CLAN;
	
	/**
	 * ID: 634<br>
	 * Message: Your alliance leader has been slain. You have been defeated by the clan.
	 */
	public static final SystemMessageId DEFEATED_BY_CLAN;
	
	/**
	 * ID: 635<br>
	 * Message: The time limit for the clan war has been exceeded. War with the clan is over.
	 */
	public static final SystemMessageId TIME_UP_WAR_OVER;
	
	/**
	 * ID: 636<br>
	 * Message: You are not involved in a clan war.
	 */
	public static final SystemMessageId NOT_INVOLVED_IN_WAR;
	
	/**
	 * ID: 637<br>
	 * Message: A clan ally has registered itself to the opponent.
	 */
	public static final SystemMessageId ALLY_REGISTERED_SELF_TO_OPPONENT;
	
	/**
	 * ID: 638<br>
	 * Message: You have already requested a Siege Battle.
	 */
	public static final SystemMessageId ALREADY_REQUESTED_SIEGE_BATTLE;
	
	/**
	 * ID: 639<br>
	 * Message: Your application has been denied because you have already submitted a request for another Siege Battle.
	 */
	public static final SystemMessageId APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE;
	
	/**
	 * ID: 640<br>
	 * Message: You have failed to refuse castle defense aid.
	 */
	public static final SystemMessageId FAILED_TO_REFUSE_CASTLE_DEFENSE_AID;
	
	/**
	 * ID: 641<br>
	 * Message: You have failed to approve castle defense aid.
	 */
	public static final SystemMessageId FAILED_TO_APPROVE_CASTLE_DEFENSE_AID;
	
	/**
	 * ID: 642<br>
	 * Message: You are already registered to the attacker side and must cancel your registration before submitting your request.
	 */
	public static final SystemMessageId ALREADY_ATTACKER_NOT_CANCEL;
	
	/**
	 * ID: 643<br>
	 * Message: You have already registered to the defender side and must cancel your registration before submitting your request.
	 */
	public static final SystemMessageId ALREADY_DEFENDER_NOT_CANCEL;
	
	/**
	 * ID: 644<br>
	 * Message: You are not yet registered for the castle siege.
	 */
	public static final SystemMessageId NOT_REGISTERED_FOR_SIEGE;
	
	/**
	 * ID: 645<br>
	 * Message: Only clans of level 5 or higher may register for a castle siege.
	 */
	public static final SystemMessageId ONLY_CLAN_LEVEL_5_ABOVE_MAY_SIEGE;
	
	/**
	 * ID: 646<br>
	 * Message: You do not have the authority to modify the castle defender list.
	 */
	public static final SystemMessageId DO_NOT_HAVE_AUTHORITY_TO_MODIFY_CASTLE_DEFENDER_LIST;
	
	/**
	 * ID: 647<br>
	 * Message: You do not have the authority to modify the siege time.
	 */
	public static final SystemMessageId DO_NOT_HAVE_AUTHORITY_TO_MODIFY_SIEGE_TIME;
	
	/**
	 * ID: 648<br>
	 * Message: No more registrations may be accepted for the attacker side.
	 */
	public static final SystemMessageId ATTACKER_SIDE_FULL;
	
	/**
	 * ID: 649<br>
	 * Message: No more registrations may be accepted for the defender side.
	 */
	public static final SystemMessageId DEFENDER_SIDE_FULL;
	
	/**
	 * ID: 650<br>
	 * Message: You may not summon from your current location.
	 */
	public static final SystemMessageId YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION;
	
	/**
	 * ID: 651<br>
	 * Message: Place in the current location and direction. Do you wish to continue?
	 */
	public static final SystemMessageId PLACE_CURRENT_LOCATION_DIRECTION;
	
	/**
	 * ID: 652<br>
	 * Message: The target of the summoned monster is wrong.
	 */
	public static final SystemMessageId TARGET_OF_SUMMON_WRONG;
	
	/**
	 * ID: 653<br>
	 * Message: You do not have the authority to position mercenaries.
	 */
	public static final SystemMessageId YOU_DO_NOT_HAVE_AUTHORITY_TO_POSITION_MERCENARIES;
	
	/**
	 * ID: 654<br>
	 * Message: You do not have the authority to cancel mercenary positioning.
	 */
	public static final SystemMessageId YOU_DO_NOT_HAVE_AUTHORITY_TO_CANCEL_MERCENARY_POSITIONING;
	
	/**
	 * ID: 655<br>
	 * Message: Mercenaries cannot be positioned here.
	 */
	public static final SystemMessageId MERCENARIES_CANNOT_BE_POSITIONED_HERE;
	
	/**
	 * ID: 656<br>
	 * Message: This mercenary cannot be positioned anymore.
	 */
	public static final SystemMessageId THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE;
	
	/**
	 * ID: 657<br>
	 * Message: Positioning cannot be done here because the distance between mercenaries is too short.
	 */
	public static final SystemMessageId POSITIONING_CANNOT_BE_DONE_BECAUSE_DISTANCE_BETWEEN_MERCENARIES_TOO_SHORT;
	
	/**
	 * ID: 658<br>
	 * Message: This is not a mercenary of a castle that you own and so you cannot cancel its positioning.
	 */
	public static final SystemMessageId THIS_IS_NOT_A_MERCENARY_OF_A_CASTLE_THAT_YOU_OWN_AND_SO_CANNOT_CANCEL_POSITIONING;
	
	/**
	 * ID: 659<br>
	 * Message: This is not the time for siege registration and so registrations cannot be accepted or rejected.
	 */
	public static final SystemMessageId NOT_SIEGE_REGISTRATION_TIME1;
	
	/**
	 * ID: 660<br>
	 * Message: This is not the time for siege registration and so registration and cancellation cannot be done.
	 */
	public static final SystemMessageId NOT_SIEGE_REGISTRATION_TIME2;
	
	/**
	 * ID: 661<br>
	 * Message: This character cannot be spoiled.
	 */
	public static final SystemMessageId SPOIL_CANNOT_USE;
	
	/**
	 * ID: 662<br>
	 * Message: The other player is rejecting friend invitations.
	 */
	public static final SystemMessageId THE_PLAYER_IS_REJECTING_FRIEND_INVITATIONS;
	
	/**
	 * ID: 663<br>
	 * Message: The siege time has been declared for $s. It is not possible to change the time after a siege time has been declared. Do you want to continue?
	 */
	public static final SystemMessageId SIEGE_TIME_DECLARED_FOR_S1;
	
	/**
	 * ID: 664<br>
	 * Message: Please choose a person to receive.
	 */
	public static final SystemMessageId CHOOSE_PERSON_TO_RECEIVE;
	
	/**
	 * ID: 665<br>
	 * Message: of alliance is applying for alliance war. Do you want to accept the challenge?
	 */
	public static final SystemMessageId APPLYING_ALLIANCE_WAR;
	
	/**
	 * ID: 666<br>
	 * Message: A request for ceasefire has been received from alliance. Do you agree?
	 */
	public static final SystemMessageId REQUEST_FOR_CEASEFIRE;
	
	/**
	 * ID: 667<br>
	 * Message: You are registering on the attacking side of the siege. Do you want to continue?
	 */
	public static final SystemMessageId REGISTERING_ON_ATTACKING_SIDE;
	
	/**
	 * ID: 668<br>
	 * Message: You are registering on the defending side of the siege. Do you want to continue?
	 */
	public static final SystemMessageId REGISTERING_ON_DEFENDING_SIDE;
	
	/**
	 * ID: 669<br>
	 * Message: You are canceling your application to participate in the siege battle. Do you want to continue?
	 */
	public static final SystemMessageId CANCELING_REGISTRATION;
	
	/**
	 * ID: 670<br>
	 * Message: You are refusing the registration of clan on the defending side. Do you want to continue?
	 */
	public static final SystemMessageId REFUSING_REGISTRATION;
	
	/**
	 * ID: 671<br>
	 * Message: You are agreeing to the registration of clan on the defending side. Do you want to continue?
	 */
	public static final SystemMessageId AGREEING_REGISTRATION;
	
	/**
	 * ID: 672<br>
	 * Message: $s1 adena disappeared.
	 */
	public static final SystemMessageId S1_DISAPPEARED_ADENA;
	
	/**
	 * ID: 673<br>
	 * Message: Only a clan leader whose clan is of level 2 or higher is allowed to participate in a clan hall auction.
	 */
	public static final SystemMessageId AUCTION_ONLY_CLAN_LEVEL_2_HIGHER;
	
	/**
	 * ID: 674<br>
	 * Message: I has not yet been seven days since canceling an auction.
	 */
	public static final SystemMessageId NOT_SEVEN_DAYS_SINCE_CANCELING_AUCTION;
	
	/**
	 * ID: 675<br>
	 * Message: There are no clan halls up for auction.
	 */
	public static final SystemMessageId NO_CLAN_HALLS_UP_FOR_AUCTION;
	
	/**
	 * ID: 676<br>
	 * Message: Since you have already submitted a bid, you are not allowed to participate in another auction at this time.
	 */
	public static final SystemMessageId ALREADY_SUBMITTED_BID;
	
	/**
	 * ID: 677<br>
	 * Message: Your bid price must be higher than the minimum price that can be bid.
	 */
	public static final SystemMessageId BID_PRICE_MUST_BE_HIGHER;
	
	/**
	 * ID: 678<br>
	 * Message: You have submitted a bid for the auction of $s1.
	 */
	public static final SystemMessageId SUBMITTED_A_BID_OF_S1;
	
	/**
	 * ID: 679<br>
	 * Message: You have canceled your bid.
	 */
	public static final SystemMessageId CANCELED_BID;
	
	/**
	 * ID: 680<br>
	 * Message: You cannot participate in an auction.
	 */
	public static final SystemMessageId CANNOT_PARTICIPATE_IN_AN_AUCTION;
	
	/**
	 * ID: 681<br>
	 * Message: The clan does not own a clan hall.
	 */
	public static final SystemMessageId CLAN_HAS_NO_CLAN_HALL;
	
	/**
	 * ID: 682<br>
	 * Message: You are moving to another village. Do you want to continue?
	 */
	public static final SystemMessageId MOVING_TO_ANOTHER_VILLAGE;
	
	/**
	 * ID: 683<br>
	 * Message: There are no priority rights on a sweeper.
	 */
	public static final SystemMessageId SWEEP_NOT_ALLOWED;
	
	/**
	 * ID: 684<br>
	 * Message: You cannot position mercenaries during a siege.
	 */
	public static final SystemMessageId CANNOT_POSITION_MERCS_DURING_SIEGE;
	
	/**
	 * ID: 685<br>
	 * Message: You cannot apply for clan war with a clan that belongs to the same alliance
	 */
	public static final SystemMessageId CANNOT_DECLARE_WAR_ON_ALLY;
	
	/**
	 * ID: 686<br>
	 * Message: You have received $s1 damage from the fire of magic.
	 */
	public static final SystemMessageId S1_DAMAGE_FROM_FIRE_MAGIC;
	
	/**
	 * ID: 687<br>
	 * Message: You cannot move while frozen. Please wait.
	 */
	public static final SystemMessageId CANNOT_MOVE_FROZEN;
	
	/**
	 * ID: 688<br>
	 * Message: The clan that owns the castle is automatically registered on the defending side.
	 */
	public static final SystemMessageId CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING;
	
	/**
	 * ID: 689<br>
	 * Message: A clan that owns a castle cannot participate in another siege.
	 */
	public static final SystemMessageId CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE;
	
	/**
	 * ID: 690<br>
	 * Message: You cannot register on the attacking side because you are part of an alliance with the clan that owns the castle.
	 */
	public static final SystemMessageId CANNOT_ATTACK_ALLIANCE_CASTLE;
	
	/**
	 * ID: 691<br>
	 * Message: $s1 clan is already a member of $s2 alliance.
	 */
	public static final SystemMessageId S1_CLAN_ALREADY_MEMBER_OF_S2_ALLIANCE;
	
	/**
	 * ID: 692<br>
	 * Message: The other party is frozen. Please wait a moment.
	 */
	public static final SystemMessageId OTHER_PARTY_IS_FROZEN;
	
	/**
	 * ID: 693<br>
	 * Message: The package that arrived is in another warehouse.
	 */
	public static final SystemMessageId PACKAGE_IN_ANOTHER_WAREHOUSE;
	
	/**
	 * ID: 694<br>
	 * Message: No packages have arrived.
	 */
	public static final SystemMessageId NO_PACKAGES_ARRIVED;
	
	/**
	 * ID: 695<br>
	 * Message: You cannot set the name of the pet.
	 */
	public static final SystemMessageId NAMING_YOU_CANNOT_SET_NAME_OF_THE_PET;
	
	/**
	 * ID: 697<br>
	 * Message: The item enchant value is strange
	 */
	public static final SystemMessageId ITEM_ENCHANT_VALUE_STRANGE;
	
	/**
	 * ID: 698<br>
	 * Message: The price is different than the same item on the sales list.
	 */
	public static final SystemMessageId PRICE_DIFFERENT_FROM_SALES_LIST;
	
	/**
	 * ID: 699<br>
	 * Message: Currently not purchasing.
	 */
	public static final SystemMessageId CURRENTLY_NOT_PURCHASING;
	
	/**
	 * ID: 700<br>
	 * Message: The purchase is complete.
	 */
	public static final SystemMessageId THE_PURCHASE_IS_COMPLETE;
	
	/**
	 * ID: 701<br>
	 * Message: You do not have enough required items.
	 */
	public static final SystemMessageId NOT_ENOUGH_REQUIRED_ITEMS;
	
	/**
	 * ID: 702 <br>
	 * Message: There are no GMs currently visible in the public list as they may be performing other functions at the moment.
	 */
	public static final SystemMessageId NO_GM_PROVIDING_SERVICE_NOW;
	
	/**
	 * ID: 703<br>
	 * Message: ======<GM List>======
	 */
	public static final SystemMessageId GM_LIST;
	
	/**
	 * ID: 704<br>
	 * Message: GM : $c1
	 */
	public static final SystemMessageId GM_C1;
	
	/**
	 * ID: 705<br>
	 * Message: You cannot exclude yourself.
	 */
	public static final SystemMessageId CANNOT_EXCLUDE_SELF;
	
	/**
	 * ID: 706<br>
	 * Message: You can only register up to 64 names on your exclude list.
	 */
	public static final SystemMessageId ONLY_64_NAMES_ON_EXCLUDE_LIST;
	
	/**
	 * ID: 707<br>
	 * Message: You cannot teleport to a village that is in a siege.
	 */
	public static final SystemMessageId NO_PORT_THAT_IS_IN_SIGE;
	
	/**
	 * ID: 708<br>
	 * Message: You do not have the right to use the castle warehouse.
	 */
	public static final SystemMessageId YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CASTLE_WAREHOUSE;
	
	/**
	 * ID: 709<br>
	 * Message: You do not have the right to use the clan warehouse.
	 */
	public static final SystemMessageId YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE;
	
	/**
	 * ID: 710<br>
	 * Message: Only clans of clan level 1 or higher can use a clan warehouse.
	 */
	public static final SystemMessageId ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE;
	
	/**
	 * ID: 711<br>
	 * Message: The siege of $s1 has started.
	 */
	public static final SystemMessageId SIEGE_OF_S1_HAS_STARTED;
	
	/**
	 * ID: 712<br>
	 * Message: The siege of $s1 has finished.
	 */
	public static final SystemMessageId SIEGE_OF_S1_HAS_ENDED;
	
	/**
	 * ID: 713<br>
	 * Message: $s1/$s2/$s3 :
	 */
	public static final SystemMessageId S1_S2_S3_D;
	
	/**
	 * ID: 714<br>
	 * Message: A trap device has been tripped.
	 */
	public static final SystemMessageId A_TRAP_DEVICE_HAS_BEEN_TRIPPED;
	
	/**
	 * ID: 715<br>
	 * Message: A trap device has been stopped.
	 */
	public static final SystemMessageId A_TRAP_DEVICE_HAS_BEEN_STOPPED;
	
	/**
	 * ID: 716<br>
	 * Message: If a base camp does not exist, resurrection is not possible.
	 */
	public static final SystemMessageId NO_RESURRECTION_WITHOUT_BASE_CAMP;
	
	/**
	 * ID: 717<br>
	 * Message: The guardian tower has been destroyed and resurrection is not possible
	 */
	public static final SystemMessageId TOWER_DESTROYED_NO_RESURRECTION;
	
	/**
	 * ID: 718<br>
	 * Message: The castle gates cannot be opened and closed during a siege.
	 */
	public static final SystemMessageId GATES_NOT_OPENED_CLOSED_DURING_SIEGE;
	
	/**
	 * ID: 719<br>
	 * Message: You failed at mixing the item.
	 */
	public static final SystemMessageId ITEM_MIXING_FAILED;
	
	/**
	 * ID: 720<br>
	 * Message: The purchase price is higher than the amount of money that you have and so you cannot open a personal store.
	 */
	public static final SystemMessageId THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY;
	
	/**
	 * ID: 721<br>
	 * Message: You cannot create an alliance while participating in a siege.
	 */
	public static final SystemMessageId NO_ALLY_CREATION_WHILE_SIEGE;
	
	/**
	 * ID: 722<br>
	 * Message: You cannot dissolve an alliance while an affiliated clan is participating in a siege battle.
	 */
	public static final SystemMessageId CANNOT_DISSOLVE_ALLY_WHILE_IN_SIEGE;
	
	/**
	 * ID: 723<br>
	 * Message: The opposing clan is participating in a siege battle.
	 */
	public static final SystemMessageId OPPOSING_CLAN_IS_PARTICIPATING_IN_SIEGE;
	
	/**
	 * ID: 724<br>
	 * Message: You cannot leave while participating in a siege battle.
	 */
	public static final SystemMessageId CANNOT_LEAVE_WHILE_SIEGE;
	
	/**
	 * ID: 725<br>
	 * Message: You cannot banish a clan from an alliance while the clan is participating in a siege
	 */
	public static final SystemMessageId CANNOT_DISMISS_WHILE_SIEGE;
	
	/**
	 * ID: 726<br>
	 * Message: Frozen condition has started. Please wait a moment.
	 */
	public static final SystemMessageId FROZEN_CONDITION_STARTED;
	
	/**
	 * ID: 727<br>
	 * Message: The frozen condition was removed.
	 */
	public static final SystemMessageId FROZEN_CONDITION_REMOVED;
	
	/**
	 * ID: 728<br>
	 * Message: You cannot apply for dissolution again within seven days after a previous application for dissolution.
	 */
	public static final SystemMessageId CANNOT_APPLY_DISSOLUTION_AGAIN;
	
	/**
	 * ID: 729<br>
	 * Message: That item cannot be discarded.
	 */
	public static final SystemMessageId ITEM_NOT_DISCARDED;
	
	/**
	 * ID: 730<br>
	 * Message: You have submitted $s1 petition(s). - You may submit $s2 more petition(s) today.
	 */
	public static final SystemMessageId SUBMITTED_YOU_S1_TH_PETITION_S2_LEFT;
	
	/**
	 * ID: 731<br>
	 * Message: A petition has been received by the GM on behalf of $s1. The petition code is $s2.
	 */
	public static final SystemMessageId PETITION_S1_RECEIVED_CODE_IS_S2;
	
	/**
	 * ID: 732<br>
	 * Message: $c1 has received a request for a consultation with the GM.
	 */
	public static final SystemMessageId C1_RECEIVED_CONSULTATION_REQUEST;
	
	/**
	 * ID: 733<br>
	 * Message: We have received $s1 petitions from you today and that is the maximum that you can submit in one day. You cannot submit any more petitions.
	 */
	public static final SystemMessageId WE_HAVE_RECEIVED_S1_PETITIONS_TODAY;
	
	/**
	 * ID: 734<br>
	 * Message: You have failed at submitting a petition on behalf of someone else. $c1 already submitted a petition.
	 */
	public static final SystemMessageId PETITION_FAILED_C1_ALREADY_SUBMITTED;
	
	/**
	 * ID: 735<br>
	 * Message: You have failed at submitting a petition on behalf of $c1. The error number is $s2.
	 */
	public static final SystemMessageId PETITION_FAILED_FOR_C1_ERROR_NUMBER_S2;
	
	/**
	 * ID: 736<br>
	 * Message: The petition was canceled. You may submit $s1 more petition(s) today.
	 */
	public static final SystemMessageId PETITION_CANCELED_SUBMIT_S1_MORE_TODAY;
	
	/**
	 * ID: 737<br>
	 * Message: You have cancelled submitting a petition on behalf of $s1.
	 */
	public static final SystemMessageId CANCELED_PETITION_ON_S1;
	
	/**
	 * ID: 738<br>
	 * Message: You have not submitted a petition.
	 */
	public static final SystemMessageId PETITION_NOT_SUBMITTED;
	
	/**
	 * ID: 739<br>
	 * Message: You have failed at cancelling a petition on behalf of $c1. The error number is $s2.
	 */
	public static final SystemMessageId PETITION_CANCEL_FAILED_FOR_C1_ERROR_NUMBER_S2;
	
	/**
	 * ID: 740<br>
	 * Message: $c1 participated in a petition chat at the request of the GM.
	 */
	public static final SystemMessageId C1_PARTICIPATE_PETITION;
	
	/**
	 * ID: 741<br>
	 * Message: You have failed at adding $c1 to the petition chat. Petition has already been submitted.
	 */
	public static final SystemMessageId FAILED_ADDING_C1_TO_PETITION;
	
	/**
	 * ID: 742<br>
	 * Message: You have failed at adding $c1 to the petition chat. The error code is $s2.
	 */
	public static final SystemMessageId PETITION_ADDING_C1_FAILED_ERROR_NUMBER_S2;
	
	/**
	 * ID: 743<br>
	 * Message: $c1 left the petition chat.
	 */
	public static final SystemMessageId C1_LEFT_PETITION_CHAT;
	
	/**
	 * ID: 744<br>
	 * Message: You have failed at removing $s1 from the petition chat. The error code is $s2.
	 */
	public static final SystemMessageId PETITION_REMOVING_S1_FAILED_ERROR_NUMBER_S2;
	
	/**
	 * ID: 745<br>
	 * Message: You are currently not in a petition chat.
	 */
	public static final SystemMessageId YOU_ARE_NOT_IN_PETITION_CHAT;
	
	/**
	 * ID: 746<br>
	 * Message: It is not currently a petition.
	 */
	public static final SystemMessageId CURRENTLY_NO_PETITION;
	
	/**
	 * ID: 748<br>
	 * Message: The distance is too far and so the casting has been stopped.
	 */
	public static final SystemMessageId DIST_TOO_FAR_CASTING_STOPPED;
	
	/**
	 * ID: 749<br>
	 * Message: The effect of $s1 has been removed.
	 */
	public static final SystemMessageId EFFECT_S1_DISAPPEARED;
	
	/**
	 * ID: 750<br>
	 * Message: There are no other skills to learn.
	 */
	public static final SystemMessageId NO_MORE_SKILLS_TO_LEARN;
	
	/**
	 * ID: 751<br>
	 * Message: As there is a conflict in the siege relationship with a clan in the alliance, you cannot invite that clan to the alliance.
	 */
	public static final SystemMessageId CANNOT_INVITE_CONFLICT_CLAN;
	
	/**
	 * ID: 752<br>
	 * Message: That name cannot be used.
	 */
	public static final SystemMessageId CANNOT_USE_NAME;
	
	/**
	 * ID: 753<br>
	 * Message: You cannot position mercenaries here.
	 */
	public static final SystemMessageId NO_MERCS_HERE;
	
	/**
	 * ID: 754<br>
	 * Message: There are $s1 hours and $s2 minutes left in this week's usage time.
	 */
	public static final SystemMessageId S1_HOURS_S2_MINUTES_LEFT_THIS_WEEK;
	
	/**
	 * ID: 755<br>
	 * Message: There are $s1 minutes left in this week's usage time.
	 */
	public static final SystemMessageId S1_MINUTES_LEFT_THIS_WEEK;
	
	/**
	 * ID: 756<br>
	 * Message: This week's usage time has finished.
	 */
	public static final SystemMessageId WEEKS_USAGE_TIME_FINISHED;
	
	/**
	 * ID: 757<br>
	 * Message: There are $s1 hours and $s2 minutes left in the fixed use time.
	 */
	public static final SystemMessageId S1_HOURS_S2_MINUTES_LEFT_IN_TIME;
	
	/**
	 * ID: 758<br>
	 * Message: There are $s1 hours and $s2 minutes left in this week's play time.
	 */
	public static final SystemMessageId S1_HOURS_S2_MINUTES_LEFT_THIS_WEEKS_PLAY_TIME;
	
	/**
	 * ID: 759<br>
	 * Message: There are $s1 minutes left in this week's play time.
	 */
	public static final SystemMessageId S1_MINUTES_LEFT_THIS_WEEKS_PLAY_TIME;
	
	/**
	 * ID: 760<br>
	 * Message: $c1 cannot join the clan because one day has not yet passed since he/she left another clan.
	 */
	public static final SystemMessageId C1_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN;
	
	/**
	 * ID: 761<br>
	 * Message: $s1 clan cannot join the alliance because one day has not yet passed since it left another alliance.
	 */
	public static final SystemMessageId S1_CANT_ENTER_ALLIANCE_WITHIN_1_DAY;
	
	/**
	 * ID: 762<br>
	 * Message: $c1 rolled $s2 and $s3's eye came out.
	 */
	public static final SystemMessageId C1_ROLLED_S2_S3_EYE_CAME_OUT;
	
	/**
	 * ID: 763<br>
	 * Message: You failed at sending the package because you are too far from the warehouse.
	 */
	public static final SystemMessageId FAILED_SENDING_PACKAGE_TOO_FAR;
	
	/**
	 * ID: 764<br>
	 * Message: You have been playing for an extended period of time. Please consider taking a break.
	 */
	public static final SystemMessageId PLAYING_FOR_LONG_TIME;
	
	/**
	 * ID: 769<br>
	 * Message: A hacking tool has been discovered. Please try again after closing unnecessary programs.
	 */
	public static final SystemMessageId HACKING_TOOL;
	
	/**
	 * ID: 774<br>
	 * Message: Play time is no longer accumulating.
	 */
	public static final SystemMessageId PLAY_TIME_NO_LONGER_ACCUMULATING;
	
	/**
	 * ID: 775<br>
	 * Message: From here on, play time will be expended.
	 */
	public static final SystemMessageId PLAY_TIME_EXPENDED;
	
	/**
	 * ID: 776<br>
	 * Message: The clan hall which was put up for auction has been awarded to clan.
	 */
	public static final SystemMessageId CLANHALL_AWARDED_TO_CLAN;
	
	/**
	 * ID: 777<br>
	 * Message: The clan hall which was put up for auction was not sold and therefore has been re-listed.
	 */
	public static final SystemMessageId CLANHALL_NOT_SOLD;
	
	/**
	 * ID: 778<br>
	 * Message: You may not log out from this location.
	 */
	public static final SystemMessageId NO_LOGOUT_HERE;
	
	/**
	 * ID: 779<br>
	 * Message: You may not restart in this location.
	 */
	public static final SystemMessageId NO_RESTART_HERE;
	
	/**
	 * ID: 780<br>
	 * Message: Observation is only possible during a siege.
	 */
	public static final SystemMessageId ONLY_VIEW_SIEGE;
	
	/**
	 * ID: 781<br>
	 * Message: Observers cannot participate.
	 */
	public static final SystemMessageId OBSERVERS_CANNOT_PARTICIPATE;
	
	/**
	 * ID: 782<br>
	 * Message: You may not observe a siege with a pet or servitor summoned.
	 */
	public static final SystemMessageId NO_OBSERVE_WITH_PET;
	
	/**
	 * ID: 783<br>
	 * Message: Lottery ticket sales have been temporarily suspended.
	 */
	public static final SystemMessageId LOTTERY_TICKET_SALES_TEMP_SUSPENDED;
	
	/**
	 * ID: 784<br>
	 * Message: Tickets for the current lottery are no longer available.
	 */
	public static final SystemMessageId NO_LOTTERY_TICKETS_AVAILABLE;
	
	/**
	 * ID: 785<br>
	 * Message: The results of lottery number $s1 have not yet been published.
	 */
	public static final SystemMessageId LOTTERY_S1_RESULT_NOT_PUBLISHED;
	
	/**
	 * ID: 786<br>
	 * Message: Incorrect syntax.
	 */
	public static final SystemMessageId INCORRECT_SYNTAX;
	
	/**
	 * ID: 787<br>
	 * Message: The tryouts are finished.
	 */
	public static final SystemMessageId CLANHALL_SIEGE_TRYOUTS_FINISHED;
	
	/**
	 * ID: 788<br>
	 * Message: The finals are finished.
	 */
	public static final SystemMessageId CLANHALL_SIEGE_FINALS_FINISHED;
	
	/**
	 * ID: 789<br>
	 * Message: The tryouts have begun.
	 */
	public static final SystemMessageId CLANHALL_SIEGE_TRYOUTS_BEGUN;
	
	/**
	 * ID: 790<br>
	 * Message: The finals are finished.
	 */
	public static final SystemMessageId CLANHALL_SIEGE_FINALS_BEGUN;
	
	/**
	 * ID: 791<br>
	 * Message: The final match is about to begin. Line up!
	 */
	public static final SystemMessageId FINAL_MATCH_BEGIN;
	
	/**
	 * ID: 792<br>
	 * Message: The siege of the clan hall is finished.
	 */
	public static final SystemMessageId CLANHALL_SIEGE_ENDED;
	
	/**
	 * ID: 793<br>
	 * Message: The siege of the clan hall has begun.
	 */
	public static final SystemMessageId CLANHALL_SIEGE_BEGUN;
	
	/**
	 * ID: 794<br>
	 * Message: You are not authorized to do that.
	 */
	public static final SystemMessageId YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT;
	
	/**
	 * ID: 795<br>
	 * Message: Only clan leaders are authorized to set rights.
	 */
	public static final SystemMessageId ONLY_LEADERS_CAN_SET_RIGHTS;
	
	/**
	 * ID: 796<br>
	 * Message: Your remaining observation time is minutes.
	 */
	public static final SystemMessageId REMAINING_OBSERVATION_TIME;
	
	/**
	 * ID: 797<br>
	 * Message: You may create up to 48 macros.
	 */
	public static final SystemMessageId YOU_MAY_CREATE_UP_TO_48_MACROS;
	
	/**
	 * ID: 798<br>
	 * Message: Item registration is irreversible. Do you wish to continue?
	 */
	public static final SystemMessageId ITEM_REGISTRATION_IRREVERSIBLE;
	
	/**
	 * ID: 799<br>
	 * Message: The observation time has expired.
	 */
	public static final SystemMessageId OBSERVATION_TIME_EXPIRED;
	
	/**
	 * ID: 800<br>
	 * Message: You are too late. The registration period is over.
	 */
	public static final SystemMessageId REGISTRATION_PERIOD_OVER;
	
	/**
	 * ID: 801<br>
	 * Message: Registration for the clan hall siege is closed.
	 */
	public static final SystemMessageId REGISTRATION_CLOSED;
	
	/**
	 * ID: 802<br>
	 * Message: Petitions are not being accepted at this time. You may submit your petition after a.m./p.m.
	 */
	public static final SystemMessageId PETITION_NOT_ACCEPTED_NOW;
	
	/**
	 * ID: 803<br>
	 * Message: Enter the specifics of your petition.
	 */
	public static final SystemMessageId PETITION_NOT_SPECIFIED;
	
	/**
	 * ID: 804<br>
	 * Message: Select a type.
	 */
	public static final SystemMessageId SELECT_TYPE;
	
	/**
	 * ID: 805<br>
	 * Message: Petitions are not being accepted at this time. You may submit your petition after $s1 a.m./p.m.
	 */
	public static final SystemMessageId PETITION_NOT_ACCEPTED_SUBMIT_AT_S1;
	
	/**
	 * ID: 806<br>
	 * Message: If you are trapped, try typing "/unstuck".
	 */
	public static final SystemMessageId TRY_UNSTUCK_WHEN_TRAPPED;
	
	/**
	 * ID: 807<br>
	 * Message: This terrain is navigable. Prepare for transport to the nearest village.
	 */
	public static final SystemMessageId STUCK_PREPARE_FOR_TRANSPORT;
	
	/**
	 * ID: 808<br>
	 * Message: You are stuck. You may submit a petition by typing "/gm".
	 */
	public static final SystemMessageId STUCK_SUBMIT_PETITION;
	
	/**
	 * ID: 809<br>
	 * Message: You are stuck. You will be transported to the nearest village in five minutes.
	 */
	public static final SystemMessageId STUCK_TRANSPORT_IN_FIVE_MINUTES;
	
	/**
	 * ID: 810<br>
	 * Message: Invalid macro. Refer to the Help file for instructions.
	 */
	public static final SystemMessageId INVALID_MACRO;
	
	/**
	 * ID: 811<br>
	 * Message: You will be moved to (). Do you wish to continue?
	 */
	public static final SystemMessageId WILL_BE_MOVED;
	
	/**
	 * ID: 812<br>
	 * Message: The secret trap has inflicted $s1 damage on you.
	 */
	public static final SystemMessageId TRAP_DID_S1_DAMAGE;
	
	/**
	 * ID: 813<br>
	 * Message: You have been poisoned by a Secret Trap.
	 */
	public static final SystemMessageId POISONED_BY_TRAP;
	
	/**
	 * ID: 814<br>
	 * Message: Your speed has been decreased by a Secret Trap.
	 */
	public static final SystemMessageId SLOWED_BY_TRAP;
	
	/**
	 * ID: 815<br>
	 * Message: The tryouts are about to begin. Line up!
	 */
	public static final SystemMessageId TRYOUTS_ABOUT_TO_BEGIN;
	
	/**
	 * ID: 816<br>
	 * Message: Tickets are now available for Monster Race $s1!
	 */
	public static final SystemMessageId MONSRACE_TICKETS_AVAILABLE_FOR_S1_RACE;
	
	/**
	 * ID: 817<br>
	 * Message: Now selling tickets for Monster Race $s1!
	 */
	public static final SystemMessageId MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE;
	
	/**
	 * ID: 818<br>
	 * Message: Ticket sales for the Monster Race will end in $s1 minute(s).
	 */
	public static final SystemMessageId MONSRACE_TICKETS_STOP_IN_S1_MINUTES;
	
	/**
	 * ID: 819<br>
	 * Message: Tickets sales are closed for Monster Race $s1. Odds are posted.
	 */
	public static final SystemMessageId MONSRACE_S1_TICKET_SALES_CLOSED;
	
	/**
	 * ID: 820<br>
	 * Message: Monster Race $s2 will begin in $s1 minute(s)!
	 */
	public static final SystemMessageId MONSRACE_S2_BEGINS_IN_S1_MINUTES;
	
	/**
	 * ID: 821<br>
	 * Message: Monster Race $s1 will begin in 30 seconds!
	 */
	public static final SystemMessageId MONSRACE_S1_BEGINS_IN_30_SECONDS;
	
	/**
	 * ID: 822<br>
	 * Message: Monster Race $s1 is about to begin! Countdown in five seconds!
	 */
	public static final SystemMessageId MONSRACE_S1_COUNTDOWN_IN_FIVE_SECONDS;
	
	/**
	 * ID: 823<br>
	 * Message: The race will begin in $s1 second(s)!
	 */
	public static final SystemMessageId MONSRACE_BEGINS_IN_S1_SECONDS;
	
	/**
	 * ID: 824<br>
	 * Message: They're off!
	 */
	public static final SystemMessageId MONSRACE_RACE_START;
	
	/**
	 * ID: 825<br>
	 * Message: Monster Race $s1 is finished!
	 */
	public static final SystemMessageId MONSRACE_S1_RACE_END;
	
	/**
	 * ID: 826<br>
	 * Message: First prize goes to the player in lane $s1. Second prize goes to the player in lane $s2.
	 */
	public static final SystemMessageId MONSRACE_FIRST_PLACE_S1_SECOND_S2;
	
	/**
	 * ID: 827<br>
	 * Message: You may not impose a block on a GM.
	 */
	public static final SystemMessageId YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_GM;
	
	/**
	 * ID: 828<br>
	 * Message: Are you sure you wish to delete the $s1 macro?
	 */
	public static final SystemMessageId WISH_TO_DELETE_S1_MACRO;
	
	/**
	 * ID: 829<br>
	 * Message: You cannot recommend yourself.
	 */
	public static final SystemMessageId YOU_CANNOT_RECOMMEND_YOURSELF;
	
	/**
	 * ID: 830<br>
	 * Message: You have recommended $c1. You have $s2 recommendations left.
	 */
	public static final SystemMessageId YOU_HAVE_RECOMMENDED_C1_YOU_HAVE_S2_RECOMMENDATIONS_LEFT;
	
	/**
	 * ID: 831<br>
	 * Message: You have been recommended by $c1.
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_RECOMMENDED_BY_C1;
	
	/**
	 * ID: 832<br>
	 * Message: That character has already been recommended.
	 */
	public static final SystemMessageId THAT_CHARACTER_IS_RECOMMENDED;
	
	/**
	 * ID: 833<br>
	 * Message: You are not authorized to make further recommendations at this time. You will receive more recommendation credits each day at 1 p.m.
	 */
	public static final SystemMessageId NO_MORE_RECOMMENDATIONS_TO_HAVE;
	
	/**
	 * ID: 834<br>
	 * Message: $c1 has rolled $s2.
	 */
	public static final SystemMessageId C1_ROLLED_S2;
	
	/**
	 * ID: 835<br>
	 * Message: You may not throw the dice at this time. Try again later.
	 */
	public static final SystemMessageId YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER;
	
	/**
	 * ID: 836<br>
	 * Message: You have exceeded your inventory volume limit and cannot take this item.
	 */
	public static final SystemMessageId YOU_HAVE_EXCEEDED_YOUR_INVENTORY_VOLUME_LIMIT_AND_CANNOT_TAKE_THIS_ITEM;
	
	/**
	 * ID: 837<br>
	 * Message: Macro descriptions may contain up to 32 characters.
	 */
	public static final SystemMessageId MACRO_DESCRIPTION_MAX_32_CHARS;
	
	/**
	 * ID: 838<br>
	 * Message: Enter the name of the macro.
	 */
	public static final SystemMessageId ENTER_THE_MACRO_NAME;
	
	/**
	 * ID: 839<br>
	 * Message: That name is already assigned to another macro.
	 */
	public static final SystemMessageId MACRO_NAME_ALREADY_USED;
	
	/**
	 * ID: 840<br>
	 * Message: That recipe is already registered.
	 */
	public static final SystemMessageId RECIPE_ALREADY_REGISTERED;
	
	/**
	 * ID: 841<br>
	 * Message: No further recipes may be registered.
	 */
	public static final SystemMessageId NO_FUTHER_RECIPES_CAN_BE_ADDED;
	
	/**
	 * ID: 842<br>
	 * Message: You are not authorized to register a recipe.
	 */
	public static final SystemMessageId NOT_AUTHORIZED_REGISTER_RECIPE;
	
	/**
	 * ID: 843<br>
	 * Message: The siege of $s1 is finished.
	 */
	public static final SystemMessageId SIEGE_OF_S1_FINISHED;
	
	/**
	 * ID: 844<br>
	 * Message: The siege to conquer $s1 has begun.
	 */
	public static final SystemMessageId SIEGE_OF_S1_BEGUN;
	
	/**
	 * ID: 845<br>
	 * Message: The deadlineto register for the siege of $s1 has passed.
	 */
	public static final SystemMessageId DEADLINE_FOR_SIEGE_S1_PASSED;
	
	/**
	 * ID: 846<br>
	 * Message: The siege of $s1 has been canceled due to lack of interest.
	 */
	public static final SystemMessageId SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST;
	
	/**
	 * ID: 847<br>
	 * Message: A clan that owns a clan hall may not participate in a clan hall siege.
	 */
	public static final SystemMessageId CLAN_OWNING_CLANHALL_MAY_NOT_SIEGE_CLANHALL;
	
	/**
	 * ID: 848<br>
	 * Message: $s1 has been deleted.
	 */
	public static final SystemMessageId S1_HAS_BEEN_DELETED;
	
	/**
	 * ID: 849<br>
	 * Message: $s1 cannot be found.
	 */
	public static final SystemMessageId S1_NOT_FOUND;
	
	/**
	 * ID: 850<br>
	 * Message: $s1 already exists.
	 */
	public static final SystemMessageId S1_ALREADY_EXISTS2;
	
	/**
	 * ID: 851<br>
	 * Message: $s1 has been added.
	 */
	public static final SystemMessageId S1_ADDED;
	
	/**
	 * ID: 852<br>
	 * Message: The recipe is incorrect.
	 */
	public static final SystemMessageId RECIPE_INCORRECT;
	
	/**
	 * ID: 853<br>
	 * Message: You may not alter your recipe book while engaged in manufacturing.
	 */
	public static final SystemMessageId CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING;
	
	/**
	 * ID: 854<br>
	 * Message: You are missing $s2 $s1 required to create that.
	 */
	public static final SystemMessageId MISSING_S2_S1_TO_CREATE;
	
	/**
	 * ID: 855<br>
	 * Message: $s1 clan has defeated $s2.
	 */
	public static final SystemMessageId S1_CLAN_DEFEATED_S2;
	
	/**
	 * ID: 856<br>
	 * Message: The siege of $s1 has ended in a draw.
	 */
	public static final SystemMessageId SIEGE_S1_DRAW;
	
	/**
	 * ID: 857<br>
	 * Message: $s1 clan has won in the preliminary match of $s2.
	 */
	public static final SystemMessageId S1_CLAN_WON_MATCH_S2;
	
	/**
	 * ID: 858<br>
	 * Message: The preliminary match of $s1 has ended in a draw.
	 */
	public static final SystemMessageId MATCH_OF_S1_DRAW;
	
	/**
	 * ID: 859<br>
	 * Message: Please register a recipe.
	 */
	public static final SystemMessageId PLEASE_REGISTER_RECIPE;
	
	/**
	 * ID: 860<br>
	 * Message: You may not buld your headquarters in close proximity to another headquarters.
	 */
	public static final SystemMessageId HEADQUARTERS_TOO_CLOSE;
	
	/**
	 * ID: 861<br>
	 * Message: You have exceeded the maximum number of memos.
	 */
	public static final SystemMessageId TOO_MANY_MEMOS;
	
	/**
	 * ID: 862<br>
	 * Message: Odds are not posted until ticket sales have closed.
	 */
	public static final SystemMessageId ODDS_NOT_POSTED;
	
	/**
	 * ID: 863<br>
	 * Message: You feel the energy of fire.
	 */
	public static final SystemMessageId FEEL_ENERGY_FIRE;
	
	/**
	 * ID: 864<br>
	 * Message: You feel the energy of water.
	 */
	public static final SystemMessageId FEEL_ENERGY_WATER;
	
	/**
	 * ID: 865<br>
	 * Message: You feel the energy of wind.
	 */
	public static final SystemMessageId FEEL_ENERGY_WIND;
	
	/**
	 * ID: 866<br>
	 * Message: You may no longer gather energy.
	 */
	public static final SystemMessageId NO_LONGER_ENERGY;
	
	/**
	 * ID: 867<br>
	 * Message: The energy is depleted.
	 */
	public static final SystemMessageId ENERGY_DEPLETED;
	
	/**
	 * ID: 868<br>
	 * Message: The energy of fire has been delivered.
	 */
	public static final SystemMessageId ENERGY_FIRE_DELIVERED;
	
	/**
	 * ID: 869<br>
	 * Message: The energy of water has been delivered.
	 */
	public static final SystemMessageId ENERGY_WATER_DELIVERED;
	
	/**
	 * ID: 870<br>
	 * Message: The energy of wind has been delivered.
	 */
	public static final SystemMessageId ENERGY_WIND_DELIVERED;
	
	/**
	 * ID: 871<br>
	 * Message: The seed has been sown.
	 */
	public static final SystemMessageId THE_SEED_HAS_BEEN_SOWN;
	
	/**
	 * ID: 872<br>
	 * Message: This seed may not be sown here.
	 */
	public static final SystemMessageId THIS_SEED_MAY_NOT_BE_SOWN_HERE;
	
	/**
	 * ID: 873<br>
	 * Message: That character does not exist.
	 */
	public static final SystemMessageId CHARACTER_DOES_NOT_EXIST;
	
	/**
	 * ID: 874<br>
	 * Message: The capacity of the warehouse has been exceeded.
	 */
	public static final SystemMessageId WAREHOUSE_CAPACITY_EXCEEDED;
	
	/**
	 * ID: 875<br>
	 * Message: The transport of the cargo has been canceled.
	 */
	public static final SystemMessageId CARGO_CANCELED;
	
	/**
	 * ID: 876<br>
	 * Message: The cargo was not delivered.
	 */
	public static final SystemMessageId CARGO_NOT_DELIVERED;
	
	/**
	 * ID: 877<br>
	 * Message: The symbol has been added.
	 */
	public static final SystemMessageId SYMBOL_ADDED;
	
	/**
	 * ID: 878<br>
	 * Message: The symbol has been deleted.
	 */
	public static final SystemMessageId SYMBOL_DELETED;
	
	/**
	 * ID: 879<br>
	 * Message: The manor system is currently under maintenance.
	 */
	public static final SystemMessageId THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE;
	
	/**
	 * ID: 880<br>
	 * Message: The transaction is complete.
	 */
	public static final SystemMessageId THE_TRANSACTION_IS_COMPLETE;
	
	/**
	 * ID: 881<br>
	 * Message: There is a discrepancy on the invoice.
	 */
	public static final SystemMessageId THERE_IS_A_DISCREPANCY_ON_THE_INVOICE;
	
	/**
	 * ID: 882<br>
	 * Message: The seed quantity is incorrect.
	 */
	public static final SystemMessageId THE_SEED_QUANTITY_IS_INCORRECT;
	
	/**
	 * ID: 883<br>
	 * Message: The seed information is incorrect.
	 */
	public static final SystemMessageId THE_SEED_INFORMATION_IS_INCORRECT;
	
	/**
	 * ID: 884<br>
	 * Message: The manor information has been updated.
	 */
	public static final SystemMessageId THE_MANOR_INFORMATION_HAS_BEEN_UPDATED;
	
	/**
	 * ID: 885<br>
	 * Message: The number of crops is incorrect.
	 */
	public static final SystemMessageId THE_NUMBER_OF_CROPS_IS_INCORRECT;
	
	/**
	 * ID: 886<br>
	 * Message: The crops are priced incorrectly.
	 */
	public static final SystemMessageId THE_CROPS_ARE_PRICED_INCORRECTLY;
	
	/**
	 * ID: 887<br>
	 * Message: The type is incorrect.
	 */
	public static final SystemMessageId THE_TYPE_IS_INCORRECT;
	
	/**
	 * ID: 888<br>
	 * Message: No crops can be purchased at this time.
	 */
	public static final SystemMessageId NO_CROPS_CAN_BE_PURCHASED_AT_THIS_TIME;
	
	/**
	 * ID: 889<br>
	 * Message: The seed was successfully sown.
	 */
	public static final SystemMessageId THE_SEED_WAS_SUCCESSFULLY_SOWN;
	
	/**
	 * ID: 890<br>
	 * Message: The seed was not sown.
	 */
	public static final SystemMessageId THE_SEED_WAS_NOT_SOWN;
	
	/**
	 * ID: 891<br>
	 * Message: You are not authorized to harvest.
	 */
	public static final SystemMessageId YOU_ARE_NOT_AUTHORIZED_TO_HARVEST;
	
	/**
	 * ID: 892<br>
	 * Message: The harvest has failed.
	 */
	public static final SystemMessageId THE_HARVEST_HAS_FAILED;
	
	/**
	 * ID: 893<br>
	 * Message: The harvest failed because the seed was not sown.
	 */
	public static final SystemMessageId THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN;
	
	/**
	 * ID: 894<br>
	 * Message: Up to $s1 recipes can be registered.
	 */
	public static final SystemMessageId UP_TO_S1_RECIPES_CAN_REGISTER;
	
	/**
	 * ID: 895<br>
	 * Message: No recipes have been registered.
	 */
	public static final SystemMessageId NO_RECIPES_REGISTERED;
	
	/**
	 * ID: 896<br>
	 * Message: The ferry has arrived at Gludin Harbor.
	 */
	public static final SystemMessageId FERRY_AT_GLUDIN;
	
	/**
	 * ID: 897<br>
	 * Message: The ferry will leave for Talking Island Harbor after anchoring for ten minutes.
	 */
	public static final SystemMessageId FERRY_LEAVE_TALKING;
	
	/**
	 * ID: 898<br>
	 * Message: Only characters of level 10 or above are authorized to make recommendations.
	 */
	public static final SystemMessageId ONLY_LEVEL_SUP_10_CAN_RECOMMEND;
	
	/**
	 * ID: 899<br>
	 * Message: The symbol cannot be drawn.
	 */
	public static final SystemMessageId CANT_DRAW_SYMBOL;
	
	/**
	 * ID: 900<br>
	 * Message: No slot exists to draw the symbol
	 */
	public static final SystemMessageId SYMBOLS_FULL;
	
	/**
	 * ID: 901<br>
	 * Message: The symbol information cannot be found.
	 */
	public static final SystemMessageId SYMBOL_NOT_FOUND;
	
	/**
	 * ID: 902<br>
	 * Message: The number of items is incorrect.
	 */
	public static final SystemMessageId NUMBER_INCORRECT;
	
	/**
	 * ID: 903<br>
	 * Message: You may not submit a petition while frozen. Be patient.
	 */
	public static final SystemMessageId NO_PETITION_WHILE_FROZEN;
	
	/**
	 * ID: 904<br>
	 * Message: Items cannot be discarded while in private store status.
	 */
	public static final SystemMessageId NO_DISCARD_WHILE_PRIVATE_STORE;
	
	/**
	 * ID: 905<br>
	 * Message: The current score for the Humans is $s1.
	 */
	public static final SystemMessageId HUMAN_SCORE_S1;
	
	/**
	 * ID: 906<br>
	 * Message: The current score for the Elves is $s1.
	 */
	public static final SystemMessageId ELVES_SCORE_S1;
	
	/**
	 * ID: 907<br>
	 * Message: The current score for the Dark Elves is $s1.
	 */
	public static final SystemMessageId DARK_ELVES_SCORE_S1;
	
	/**
	 * ID: 908<br>
	 * Message: The current score for the Orcs is $s1.
	 */
	public static final SystemMessageId ORCS_SCORE_S1;
	
	/**
	 * ID: 909<br>
	 * Message: The current score for the Dwarves is $s1.
	 */
	public static final SystemMessageId DWARVEN_SCORE_S1;
	
	/**
	 * ID: 910<br>
	 * Message: Current location : $s1, $s2, $s3 (Near Talking Island Village)
	 */
	public static final SystemMessageId LOC_TI_S1_S2_S3;
	
	/**
	 * ID: 911<br>
	 * Message: Current location : $s1, $s2, $s3 (Near Gludin Village)
	 */
	public static final SystemMessageId LOC_GLUDIN_S1_S2_S3;
	
	/**
	 * ID: 912<br>
	 * Message: Current location : $s1, $s2, $s3 (Near the Town of Gludio)
	 */
	public static final SystemMessageId LOC_GLUDIO_S1_S2_S3;
	
	/**
	 * ID: 913<br>
	 * Message: Current location : $s1, $s2, $s3 (Near the Neutral Zone)
	 */
	public static final SystemMessageId LOC_NEUTRAL_ZONE_S1_S2_S3;
	
	/**
	 * ID: 914<br>
	 * Message: Current location : $s1, $s2, $s3 (Near the Elven Village)
	 */
	public static final SystemMessageId LOC_ELVEN_S1_S2_S3;
	
	/**
	 * ID: 915<br>
	 * Message: Current location : $s1, $s2, $s3 (Near the Dark Elf Village)
	 */
	public static final SystemMessageId LOC_DARK_ELVEN_S1_S2_S3;
	
	/**
	 * ID: 916<br>
	 * Message: Current location : $s1, $s2, $s3 (Near the Town of Dion)
	 */
	public static final SystemMessageId LOC_DION_S1_S2_S3;
	
	/**
	 * ID: 917<br>
	 * Message: Current location : $s1, $s2, $s3 (Near the Floran Village)
	 */
	public static final SystemMessageId LOC_FLORAN_S1_S2_S3;
	
	/**
	 * ID: 918<br>
	 * Message: Current location : $s1, $s2, $s3 (Near the Town of Giran)
	 */
	public static final SystemMessageId LOC_GIRAN_S1_S2_S3;
	
	/**
	 * ID: 919<br>
	 * Message: Current location : $s1, $s2, $s3 (Near Giran Harbor)
	 */
	public static final SystemMessageId LOC_GIRAN_HARBOR_S1_S2_S3;
	
	/**
	 * ID: 920<br>
	 * Message: Current location : $s1, $s2, $s3 (Near the Orc Village)
	 */
	public static final SystemMessageId LOC_ORC_S1_S2_S3;
	
	/**
	 * ID: 921<br>
	 * Message: Current location : $s1, $s2, $s3 (Near the Dwarven Village)
	 */
	public static final SystemMessageId LOC_DWARVEN_S1_S2_S3;
	
	/**
	 * ID: 922<br>
	 * Message: Current location : $s1, $s2, $s3 (Near the Town of Oren)
	 */
	public static final SystemMessageId LOC_OREN_S1_S2_S3;
	
	/**
	 * ID: 923<br>
	 * Message: Current location : $s1, $s2, $s3 (Near Hunters Village)
	 */
	public static final SystemMessageId LOC_HUNTER_S1_S2_S3;
	
	/**
	 * ID: 924<br>
	 * Message: Current location : $s1, $s2, $s3 (Near Aden Castle Town)
	 */
	public static final SystemMessageId LOC_ADEN_S1_S2_S3;
	
	/**
	 * ID: 925<br>
	 * Message: Current location : $s1, $s2, $s3 (Near the Coliseum)
	 */
	public static final SystemMessageId LOC_COLISEUM_S1_S2_S3;
	
	/**
	 * ID: 926<br>
	 * Message: Current location : $s1, $s2, $s3 (Near Heine)
	 */
	public static final SystemMessageId LOC_HEINE_S1_S2_S3;
	
	/**
	 * ID: 927<br>
	 * Message: The current time is $s1:$s2.
	 */
	public static final SystemMessageId TIME_S1_S2_IN_THE_DAY;
	
	/**
	 * ID: 928<br>
	 * Message: The current time is $s1:$s2.
	 */
	public static final SystemMessageId TIME_S1_S2_IN_THE_NIGHT;
	
	/**
	 * ID: 929<br>
	 * Message: No compensation was given for the farm products.
	 */
	public static final SystemMessageId NO_COMPENSATION_FOR_FARM_PRODUCTS;
	
	/**
	 * ID: 930<br>
	 * Message: Lottery tickets are not currently being sold.
	 */
	public static final SystemMessageId NO_LOTTERY_TICKETS_CURRENT_SOLD;
	
	/**
	 * ID: 931<br>
	 * Message: The winning lottery ticket numbers has not yet been anonunced.
	 */
	public static final SystemMessageId LOTTERY_WINNERS_NOT_ANNOUNCED_YET;
	
	/**
	 * ID: 932<br>
	 * Message: You cannot chat locally while observing.
	 */
	public static final SystemMessageId NO_ALLCHAT_WHILE_OBSERVING;
	
	/**
	 * ID: 933<br>
	 * Message: The seed pricing greatly differs from standard seed prices.
	 */
	public static final SystemMessageId THE_SEED_PRICING_GREATLY_DIFFERS_FROM_STANDARD_SEED_PRICES;
	
	/**
	 * ID: 934<br>
	 * Message: It is a deleted recipe.
	 */
	public static final SystemMessageId A_DELETED_RECIPE;
	
	/**
	 * ID: 935<br>
	 * Message: The amount is not sufficient and so the manor is not in operation.
	 */
	public static final SystemMessageId THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION;
	
	/**
	 * ID: 936<br>
	 * Message: Use $s1.
	 */
	public static final SystemMessageId USE_S1_;
	
	/**
	 * ID: 937<br>
	 * Message: Currently preparing for private workshop.
	 */
	public static final SystemMessageId PREPARING_PRIVATE_WORKSHOP;
	
	/**
	 * ID: 938<br>
	 * Message: The community server is currently offline.
	 */
	public static final SystemMessageId CB_OFFLINE;
	
	/**
	 * ID: 939<br>
	 * Message: You cannot exchange while blocking everything.
	 */
	public static final SystemMessageId NO_EXCHANGE_WHILE_BLOCKING;
	
	/**
	 * ID: 940<br>
	 * Message: $s1 is blocked everything.
	 */
	public static final SystemMessageId S1_BLOCKED_EVERYTHING;
	
	/**
	 * ID: 941<br>
	 * Message: Restart at Talking Island Village.
	 */
	public static final SystemMessageId RESTART_AT_TI;
	
	/**
	 * ID: 942<br>
	 * Message: Restart at Gludin Village.
	 */
	public static final SystemMessageId RESTART_AT_GLUDIN;
	
	/**
	 * ID: 943<br>
	 * Message: Restart at the Town of Gludin. || guess should be Gludio ;)
	 */
	public static final SystemMessageId RESTART_AT_GLUDIO;
	
	/**
	 * ID: 944<br>
	 * Message: Restart at the Neutral Zone.
	 */
	public static final SystemMessageId RESTART_AT_NEUTRAL_ZONE;
	
	/**
	 * ID: 945<br>
	 * Message: Restart at the Elven Village.
	 */
	public static final SystemMessageId RESTART_AT_ELFEN_VILLAGE;
	
	/**
	 * ID: 946<br>
	 * Message: Restart at the Dark Elf Village.
	 */
	public static final SystemMessageId RESTART_AT_DARKELF_VILLAGE;
	
	/**
	 * ID: 947<br>
	 * Message: Restart at the Town of Dion.
	 */
	public static final SystemMessageId RESTART_AT_DION;
	
	/**
	 * ID: 948<br>
	 * Message: Restart at Floran Village.
	 */
	public static final SystemMessageId RESTART_AT_FLORAN;
	
	/**
	 * ID: 949<br>
	 * Message: Restart at the Town of Giran.
	 */
	public static final SystemMessageId RESTART_AT_GIRAN;
	
	/**
	 * ID: 950<br>
	 * Message: Restart at Giran Harbor.
	 */
	public static final SystemMessageId RESTART_AT_GIRAN_HARBOR;
	
	/**
	 * ID: 951<br>
	 * Message: Restart at the Orc Village.
	 */
	public static final SystemMessageId RESTART_AT_ORC_VILLAGE;
	
	/**
	 * ID: 952<br>
	 * Message: Restart at the Dwarven Village.
	 */
	public static final SystemMessageId RESTART_AT_DWARFEN_VILLAGE;
	
	/**
	 * ID: 953<br>
	 * Message: Restart at the Town of Oren.
	 */
	public static final SystemMessageId RESTART_AT_OREN;
	
	/**
	 * ID: 954<br>
	 * Message: Restart at Hunters Village.
	 */
	public static final SystemMessageId RESTART_AT_HUNTERS_VILLAGE;
	
	/**
	 * ID: 955<br>
	 * Message: Restart at the Town of Aden.
	 */
	public static final SystemMessageId RESTART_AT_ADEN;
	
	/**
	 * ID: 956<br>
	 * Message: Restart at the Coliseum.
	 */
	public static final SystemMessageId RESTART_AT_COLISEUM;
	
	/**
	 * ID: 957<br>
	 * Message: Restart at Heine.
	 */
	public static final SystemMessageId RESTART_AT_HEINE;
	
	/**
	 * ID: 958<br>
	 * Message: Items cannot be discarded or destroyed while operating a private store or workshop.
	 */
	public static final SystemMessageId ITEMS_CANNOT_BE_DISCARDED_OR_DESTROYED_WHILE_OPERATING_PRIVATE_STORE_OR_WORKSHOP;
	
	/**
	 * ID: 959<br>
	 * Message: $s1 (*$s2) manufactured successfully.
	 */
	public static final SystemMessageId S1_S2_MANUFACTURED_SUCCESSFULLY;
	
	/**
	 * ID: 960<br>
	 * Message: $s1 manufacturing failure.
	 */
	public static final SystemMessageId S1_MANUFACTURE_FAILURE;
	
	/**
	 * ID: 961<br>
	 * Message: You are now blocking everything.
	 */
	public static final SystemMessageId BLOCKING_ALL;
	
	/**
	 * ID: 962<br>
	 * Message: You are no longer blocking everything.
	 */
	public static final SystemMessageId NOT_BLOCKING_ALL;
	
	/**
	 * ID: 963<br>
	 * Message: Please determine the manufacturing price.
	 */
	public static final SystemMessageId DETERMINE_MANUFACTURE_PRICE;
	
	/**
	 * ID: 964<br>
	 * Message: Chatting is prohibited for one minute.
	 */
	public static final SystemMessageId CHATBAN_FOR_1_MINUTE;
	
	/**
	 * ID: 965<br>
	 * Message: The chatting prohibition has been removed.
	 */
	public static final SystemMessageId CHATBAN_REMOVED;
	
	/**
	 * ID: 966<br>
	 * Message: Chatting is currently prohibited. If you try to chat before the prohibition is removed, the prohibition time will become even longer.
	 */
	public static final SystemMessageId CHATTING_IS_CURRENTLY_PROHIBITED;
	
	/**
	 * ID: 967<br>
	 * Message: Do you accept $c1's party invitation? (Item Distribution: Random including spoil.)
	 */
	public static final SystemMessageId C1_PARTY_INVITE_RANDOM_INCLUDING_SPOIL;
	
	/**
	 * ID: 968<br>
	 * Message: Do you accept $c1's party invitation? (Item Distribution: By Turn.)
	 */
	public static final SystemMessageId C1_PARTY_INVITE_BY_TURN;
	
	/**
	 * ID: 969<br>
	 * Message: Do you accept $c1's party invitation? (Item Distribution: By Turn including spoil.)
	 */
	public static final SystemMessageId C1_PARTY_INVITE_BY_TURN_INCLUDING_SPOIL;
	
	/**
	 * ID: 970<br>
	 * Message: $s2's MP has been drained by $c1.
	 */
	public static final SystemMessageId S2_MP_HAS_BEEN_DRAINED_BY_C1;
	
	/**
	 * ID: 971<br>
	 * Message: Petitions cannot exceed 255 characters.
	 */
	public static final SystemMessageId PETITION_MAX_CHARS_255;
	
	/**
	 * ID: 972<br>
	 * Message: This pet cannot use this item.
	 */
	public static final SystemMessageId PET_CANNOT_USE_ITEM;
	
	/**
	 * ID: 973<br>
	 * Message: Please input no more than the number you have.
	 */
	public static final SystemMessageId INPUT_NO_MORE_YOU_HAVE;
	
	/**
	 * ID: 974<br>
	 * Message: The soul crystal succeeded in absorbing a soul.
	 */
	public static final SystemMessageId SOUL_CRYSTAL_ABSORBING_SUCCEEDED;
	
	/**
	 * ID: 975<br>
	 * Message: The soul crystal was not able to absorb a soul.
	 */
	public static final SystemMessageId SOUL_CRYSTAL_ABSORBING_FAILED;
	
	/**
	 * ID: 976<br>
	 * Message: The soul crystal broke because it was not able to endure the soul energy.
	 */
	public static final SystemMessageId SOUL_CRYSTAL_BROKE;
	
	/**
	 * ID: 977<br>
	 * Message: The soul crystals caused resonation and failed at absorbing a soul.
	 */
	public static final SystemMessageId SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION;
	
	/**
	 * ID: 978<br>
	 * Message: The soul crystal is refusing to absorb a soul.
	 */
	public static final SystemMessageId SOUL_CRYSTAL_ABSORBING_REFUSED;
	
	/**
	 * ID: 979<br>
	 * Message: The ferry arrived at Talking Island Harbor.
	 */
	public static final SystemMessageId FERRY_ARRIVED_AT_TALKING;
	
	/**
	 * ID: 980<br>
	 * Message: The ferry will leave for Gludin Harbor after anchoring for ten minutes.
	 */
	public static final SystemMessageId FERRY_LEAVE_FOR_GLUDIN_AFTER_10_MINUTES;
	
	/**
	 * ID: 981<br>
	 * Message: The ferry will leave for Gludin Harbor in five minutes.
	 */
	public static final SystemMessageId FERRY_LEAVE_FOR_GLUDIN_IN_5_MINUTES;
	
	/**
	 * ID: 982<br>
	 * Message: The ferry will leave for Gludin Harbor in one minute.
	 */
	public static final SystemMessageId FERRY_LEAVE_FOR_GLUDIN_IN_1_MINUTE;
	
	/**
	 * ID: 983<br>
	 * Message: Those wishing to ride should make haste to get on.
	 */
	public static final SystemMessageId MAKE_HASTE_GET_ON_BOAT;
	
	/**
	 * ID: 984<br>
	 * Message: The ferry will be leaving soon for Gludin Harbor.
	 */
	public static final SystemMessageId FERRY_LEAVE_SOON_FOR_GLUDIN;
	
	/**
	 * ID: 985<br>
	 * Message: The ferry is leaving for Gludin Harbor.
	 */
	public static final SystemMessageId FERRY_LEAVING_FOR_GLUDIN;
	
	/**
	 * ID: 986<br>
	 * Message: The ferry has arrived at Gludin Harbor.
	 */
	public static final SystemMessageId FERRY_ARRIVED_AT_GLUDIN;
	
	/**
	 * ID: 987<br>
	 * Message: The ferry will leave for Talking Island Harbor after anchoring for ten minutes.
	 */
	public static final SystemMessageId FERRY_LEAVE_FOR_TALKING_AFTER_10_MINUTES;
	
	/**
	 * ID: 988<br>
	 * Message: The ferry will leave for Talking Island Harbor in five minutes.
	 */
	public static final SystemMessageId FERRY_LEAVE_FOR_TALKING_IN_5_MINUTES;
	
	/**
	 * ID: 989<br>
	 * Message: The ferry will leave for Talking Island Harbor in one minute.
	 */
	public static final SystemMessageId FERRY_LEAVE_FOR_TALKING_IN_1_MINUTE;
	
	/**
	 * ID: 990<br>
	 * Message: The ferry will be leaving soon for Talking Island Harbor.
	 */
	public static final SystemMessageId FERRY_LEAVE_SOON_FOR_TALKING;
	
	/**
	 * ID: 991<br>
	 * Message: The ferry is leaving for Talking Island Harbor.
	 */
	public static final SystemMessageId FERRY_LEAVING_FOR_TALKING;
	
	/**
	 * ID: 992<br>
	 * Message: The ferry has arrived at Giran Harbor.
	 */
	public static final SystemMessageId FERRY_ARRIVED_AT_GIRAN;
	
	/**
	 * ID: 993<br>
	 * Message: The ferry will leave for Giran Harbor after anchoring for ten minutes.
	 */
	public static final SystemMessageId FERRY_LEAVE_FOR_GIRAN_AFTER_10_MINUTES;
	
	/**
	 * ID: 994<br>
	 * Message: The ferry will leave for Giran Harbor in five minutes.
	 */
	public static final SystemMessageId FERRY_LEAVE_FOR_GIRAN_IN_5_MINUTES;
	
	/**
	 * ID: 995<br>
	 * Message: The ferry will leave for Giran Harbor in one minute.
	 */
	public static final SystemMessageId FERRY_LEAVE_FOR_GIRAN_IN_1_MINUTE;
	
	/**
	 * ID: 996<br>
	 * Message: The ferry will be leaving soon for Giran Harbor.
	 */
	public static final SystemMessageId FERRY_LEAVE_SOON_FOR_GIRAN;
	
	/**
	 * ID: 997<br>
	 * Message: The ferry is leaving for Giran Harbor.
	 */
	public static final SystemMessageId FERRY_LEAVING_FOR_GIRAN;
	
	/**
	 * ID: 998<br>
	 * Message: The Innadril pleasure boat has arrived. It will anchor for ten minutes.
	 */
	public static final SystemMessageId INNADRIL_BOAT_ANCHOR_10_MINUTES;
	
	/**
	 * ID: 999<br>
	 * Message: The Innadril pleasure boat will leave in five minutes.
	 */
	public static final SystemMessageId INNADRIL_BOAT_LEAVE_IN_5_MINUTES;
	
	/**
	 * ID: 1000<br>
	 * Message: The Innadril pleasure boat will leave in one minute.
	 */
	public static final SystemMessageId INNADRIL_BOAT_LEAVE_IN_1_MINUTE;
	
	/**
	 * ID: 1001<br>
	 * Message: The Innadril pleasure boat will be leaving soon.
	 */
	public static final SystemMessageId INNADRIL_BOAT_LEAVE_SOON;
	
	/**
	 * ID: 1002<br>
	 * Message: The Innadril pleasure boat is leaving.
	 */
	public static final SystemMessageId INNADRIL_BOAT_LEAVING;
	
	/**
	 * ID: 1003<br>
	 * Message: Cannot possess a monster race ticket.
	 */
	public static final SystemMessageId CANNOT_POSSES_MONS_TICKET;
	
	/**
	 * ID: 1004<br>
	 * Message: You have registered for a clan hall auction.
	 */
	public static final SystemMessageId REGISTERED_FOR_CLANHALL;
	
	/**
	 * ID: 1005<br>
	 * Message: There is not enough adena in the clan hall warehouse.
	 */
	public static final SystemMessageId NOT_ENOUGH_ADENA_IN_CWH;
	
	/**
	 * ID: 1006<br>
	 * Message: You have bid in a clan hall auction.
	 */
	public static final SystemMessageId BID_IN_CLANHALL_AUCTION;
	
	/**
	 * ID: 1007<br>
	 * Message: The preliminary match registration of $s1 has finished.
	 */
	public static final SystemMessageId PRELIMINARY_REGISTRATION_OF_S1_FINISHED;
	
	/**
	 * ID: 1008<br>
	 * Message: A hungry strider cannot be mounted or dismounted.
	 */
	public static final SystemMessageId HUNGRY_STRIDER_NOT_MOUNT;
	
	/**
	 * ID: 1009<br>
	 * Message: A strider cannot be ridden when dead.
	 */
	public static final SystemMessageId STRIDER_CANT_BE_RIDDEN_WHILE_DEAD;
	
	/**
	 * ID: 1010<br>
	 * Message: A dead strider cannot be ridden.
	 */
	public static final SystemMessageId DEAD_STRIDER_CANT_BE_RIDDEN;
	
	/**
	 * ID: 1011<br>
	 * Message: A strider in battle cannot be ridden.
	 */
	public static final SystemMessageId STRIDER_IN_BATLLE_CANT_BE_RIDDEN;
	
	/**
	 * ID: 1012<br>
	 * Message: A strider cannot be ridden while in battle.
	 */
	public static final SystemMessageId STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE;
	
	/**
	 * ID: 1013<br>
	 * Message: A strider can be ridden only when standing.
	 */
	public static final SystemMessageId STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING;
	
	/**
	 * ID: 1014<br>
	 * Message: Your pet gained $s1 experience points.
	 */
	public static final SystemMessageId PET_EARNED_S1_EXP;
	
	/**
	 * ID: 1015<br>
	 * Message: Your pet hit for $s1 damage.
	 */
	public static final SystemMessageId PET_HIT_FOR_S1_DAMAGE;
	
	/**
	 * ID: 1016<br>
	 * Message: Pet received $s2 damage by $c1.
	 */
	public static final SystemMessageId PET_RECEIVED_S2_DAMAGE_BY_C1;
	
	/**
	 * ID: 1017<br>
	 * Message: Pet's critical hit!
	 */
	public static final SystemMessageId CRITICAL_HIT_BY_PET;
	
	/**
	 * ID: 1018<br>
	 * Message: Your pet uses $s1.
	 */
	public static final SystemMessageId PET_USES_S1;
	
	/**
	 * ID: 1019<br>
	 * Message: Your pet uses $s1.
	 */
	public static final SystemMessageId PET_USES_S1_;
	
	/**
	 * ID: 1020<br>
	 * Message: Your pet picked up $s1.
	 */
	public static final SystemMessageId PET_PICKED_S1;
	
	/**
	 * ID: 1021<br>
	 * Message: Your pet picked up $s2 $s1(s).
	 */
	public static final SystemMessageId PET_PICKED_S2_S1_S;
	
	/**
	 * ID: 1022<br>
	 * Message: Your pet picked up +$s1 $s2.
	 */
	public static final SystemMessageId PET_PICKED_S1_S2;
	
	/**
	 * ID: 1023<br>
	 * Message: Your pet picked up $s1 adena.
	 */
	public static final SystemMessageId PET_PICKED_S1_ADENA;
	
	/**
	 * ID: 1024<br>
	 * Message: Your pet put on $s1.
	 */
	public static final SystemMessageId PET_PUT_ON_S1;
	
	/**
	 * ID: 1025<br>
	 * Message: Your pet took off $s1.
	 */
	public static final SystemMessageId PET_TOOK_OFF_S1;
	
	/**
	 * ID: 1026<br>
	 * Message: The summoned monster gave damage of $s1
	 */
	public static final SystemMessageId SUMMON_GAVE_DAMAGE_S1;
	
	/**
	 * ID: 1027<br>
	 * Message: Servitor received $s2 damage caused by $s1.
	 */
	public static final SystemMessageId SUMMON_RECEIVED_DAMAGE_S2_BY_S1;
	
	/**
	 * ID: 1028<br>
	 * Message: Summoned monster's critical hit!
	 */
	public static final SystemMessageId CRITICAL_HIT_BY_SUMMONED_MOB;
	
	/**
	 * ID: 1029<br>
	 * Message: Summoned monster uses $s1.
	 */
	public static final SystemMessageId SUMMONED_MOB_USES_S1;
	
	/**
	 * ID: 1030<br>
	 * Message: <Party Information>
	 */
	public static final SystemMessageId PARTY_INFORMATION;
	
	/**
	 * ID: 1031<br>
	 * Message: Looting method: Finders keepers
	 */
	public static final SystemMessageId LOOTING_FINDERS_KEEPERS;
	
	/**
	 * ID: 1032<br>
	 * Message: Looting method: Random
	 */
	public static final SystemMessageId LOOTING_RANDOM;
	
	/**
	 * ID: 1033<br>
	 * Message: Looting method: Random including spoil
	 */
	public static final SystemMessageId LOOTING_RANDOM_INCLUDE_SPOIL;
	
	/**
	 * ID: 1034<br>
	 * Message: Looting method: By turn
	 */
	public static final SystemMessageId LOOTING_BY_TURN;
	
	/**
	 * ID: 1035<br>
	 * Message: Looting method: By turn including spoil
	 */
	public static final SystemMessageId LOOTING_BY_TURN_INCLUDE_SPOIL;
	
	/**
	 * ID: 1036<br>
	 * Message: You have exceeded the quantity that can be inputted.
	 */
	public static final SystemMessageId YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED;
	
	/**
	 * ID: 1037<br>
	 * Message: $c1 manufactured $s2.
	 */
	public static final SystemMessageId C1_MANUFACTURED_S2;
	
	/**
	 * ID: 1038<br>
	 * Message: $c1 manufactured $s3 $s2(s).
	 */
	public static final SystemMessageId C1_MANUFACTURED_S3_S2_S;
	
	/**
	 * ID: 1039<br>
	 * Message: Items left at the clan hall warehouse can only be retrieved by the clan leader. Do you want to continue?
	 */
	public static final SystemMessageId ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE;
	
	/**
	 * ID: 1040<br>
	 * Message: Items sent by freight can be picked up from any Warehouse location. Do you want to continue?
	 */
	public static final SystemMessageId ITEMS_SENT_BY_FREIGHT_PICKED_UP_FROM_ANYWHERE;
	
	/**
	 * ID: 1041<br>
	 * Message: The next seed purchase price is $s1 adena.
	 */
	public static final SystemMessageId THE_NEXT_SEED_PURCHASE_PRICE_IS_S1_ADENA;
	
	/**
	 * ID: 1042<br>
	 * Message: The next farm goods purchase price is $s1 adena.
	 */
	public static final SystemMessageId THE_NEXT_FARM_GOODS_PURCHASE_PRICE_IS_S1_ADENA;
	
	/**
	 * ID: 1043<br>
	 * Message: At the current time, the "/unstuck" command cannot be used. Please send in a petition.
	 */
	public static final SystemMessageId NO_UNSTUCK_PLEASE_SEND_PETITION;
	
	/**
	 * ID: 1044<br>
	 * Message: Monster race payout information is not available while tickets are being sold.
	 */
	public static final SystemMessageId MONSRACE_NO_PAYOUT_INFO;
	
	/**
	 * ID: 1046<br>
	 * Message: Monster race tickets are no longer available.
	 */
	public static final SystemMessageId MONSRACE_TICKETS_NOT_AVAILABLE;
	
	/**
	 * ID: 1047<br>
	 * Message: We did not succeed in producing $s1 item.
	 */
	public static final SystemMessageId NOT_SUCCEED_PRODUCING_S1;
	
	/**
	 * ID: 1048<br>
	 * Message: When "blocking" everything, whispering is not possible.
	 */
	public static final SystemMessageId NO_WHISPER_WHEN_BLOCKING;
	
	/**
	 * ID: 1049<br>
	 * Message: When "blocking" everything, it is not possible to send invitations for organizing parties.
	 */
	public static final SystemMessageId NO_PARTY_WHEN_BLOCKING;
	
	/**
	 * ID: 1050<br>
	 * Message: There are no communities in my clan. Clan communities are allowed for clans with skill levels of 2 and higher.
	 */
	public static final SystemMessageId NO_CB_IN_MY_CLAN;
	
	/**
	 * ID: 1051 <br>
	 * Message: Payment for your clan hall has not been made please make payment tomorrow.
	 */
	public static final SystemMessageId PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW;
	
	/**
	 * ID: 1052 <br>
	 * Message: Payment of Clan Hall is overdue the owner loose Clan Hall.
	 */
	public static final SystemMessageId THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED;
	
	/**
	 * ID: 1053<br>
	 * Message: It is not possible to resurrect in battlefields where a siege war is taking place.
	 */
	public static final SystemMessageId CANNOT_BE_RESURRECTED_DURING_SIEGE;
	
	/**
	 * ID: 1054<br>
	 * Message: You have entered a mystical land.
	 */
	public static final SystemMessageId ENTERED_MYSTICAL_LAND;
	
	/**
	 * ID: 1055<br>
	 * Message: You have left a mystical land.
	 */
	public static final SystemMessageId EXITED_MYSTICAL_LAND;
	
	/**
	 * ID: 1056<br>
	 * Message: You have exceeded the storage capacity of the castle's vault.
	 */
	public static final SystemMessageId VAULT_CAPACITY_EXCEEDED;
	
	/**
	 * ID: 1057<br>
	 * Message: This command can only be used in the relax server.
	 */
	public static final SystemMessageId RELAX_SERVER_ONLY;
	
	/**
	 * ID: 1058<br>
	 * Message: The sales price for seeds is $s1 adena.
	 */
	public static final SystemMessageId THE_SALES_PRICE_FOR_SEEDS_IS_S1_ADENA;
	
	/**
	 * ID: 1059<br>
	 * Message: The remaining purchasing amount is $s1 adena.
	 */
	public static final SystemMessageId THE_REMAINING_PURCHASING_IS_S1_ADENA;
	
	/**
	 * ID: 1060<br>
	 * Message: The remainder after selling the seeds is $s1.
	 */
	public static final SystemMessageId THE_REMAINDER_AFTER_SELLING_THE_SEEDS_IS_S1;
	
	/**
	 * ID: 1061<br>
	 * Message: The recipe cannot be registered. You do not have the ability to create items.
	 */
	public static final SystemMessageId CANT_REGISTER_NO_ABILITY_TO_CRAFT;
	
	/**
	 * ID: 1062<br>
	 * Message: Writing something new is possible after level 10.
	 */
	public static final SystemMessageId WRITING_SOMETHING_NEW_POSSIBLE_AFTER_LEVEL_10;
	
	/**
	 * ID: 1063<br>
	 * if you become trapped or unable to move, please use the '/unstuck' command.
	 */
	public static final SystemMessageId PETITION_UNAVAILABLE;
	
	/**
	 * ID: 1064<br>
	 * Message: The equipment, +$s1 $s2, has been removed.
	 */
	public static final SystemMessageId EQUIPMENT_S1_S2_REMOVED;
	
	/**
	 * ID: 1065<br>
	 * Message: While operating a private store or workshop, you cannot discard, destroy, or trade an item.
	 */
	public static final SystemMessageId CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE;
	
	/**
	 * ID: 1066<br>
	 * Message: $s1 HP has been restored.
	 */
	public static final SystemMessageId S1_HP_RESTORED;
	
	/**
	 * ID: 1067<br>
	 * Message: $s2 HP has been restored by $c1
	 */
	public static final SystemMessageId S2_HP_RESTORED_BY_C1;
	
	/**
	 * ID: 1068<br>
	 * Message: $s1 MP has been restored.
	 */
	public static final SystemMessageId S1_MP_RESTORED;
	
	/**
	 * ID: 1069<br>
	 * Message: $s2 MP has been restored by $c1.
	 */
	public static final SystemMessageId S2_MP_RESTORED_BY_C1;
	
	/**
	 * ID: 1070<br>
	 * Message: You do not have 'read' permission.
	 */
	public static final SystemMessageId NO_READ_PERMISSION;
	
	/**
	 * ID: 1071<br>
	 * Message: You do not have 'write' permission.
	 */
	public static final SystemMessageId NO_WRITE_PERMISSION;
	
	/**
	 * ID: 1072<br>
	 * Message: You have obtained a ticket for the Monster Race #$s1 - Single
	 */
	public static final SystemMessageId OBTAINED_TICKET_FOR_MONS_RACE_S1_SINGLE;
	
	/**
	 * ID: 1073<br>
	 * Message: You have obtained a ticket for the Monster Race #$s1 - Single
	 */
	public static final SystemMessageId OBTAINED_TICKET_FOR_MONS_RACE_S1_SINGLE_;
	
	/**
	 * ID: 1074<br>
	 * Message: You do not meet the age requirement to purchase a Monster Race Ticket.
	 */
	public static final SystemMessageId NOT_MEET_AGE_REQUIREMENT_FOR_MONS_RACE;
	
	/**
	 * ID: 1075<br>
	 * Message: The bid amount must be higher than the previous bid.
	 */
	public static final SystemMessageId BID_AMOUNT_HIGHER_THAN_PREVIOUS_BID;
	
	/**
	 * ID: 1076<br>
	 * Message: The game cannot be terminated at this time.
	 */
	public static final SystemMessageId GAME_CANNOT_TERMINATE_NOW;
	
	/**
	 * ID: 1077<br>
	 * Message: A GameGuard Execution error has occurred. Please send the *.erl file(s) located in the GameGuard folder to game@inca.co.kr
	 */
	public static final SystemMessageId GG_EXECUTION_ERROR;
	
	/**
	 * ID: 1078<br>
	 * Message: When a user's keyboard input exceeds a certain cumulative score a chat ban will be applied. This is done to discourage spamming. Please avoid posting the same message multiple times during a short period.
	 */
	public static final SystemMessageId DONT_SPAM;
	
	/**
	 * ID: 1079<br>
	 * Message: The target is currently banend from chatting.
	 */
	public static final SystemMessageId TARGET_IS_CHAT_BANNED;
	
	/**
	 * ID: 1080<br>
	 * Message: Being permanent, are you sure you wish to use the facelift potion - Type A?
	 */
	public static final SystemMessageId FACELIFT_POTION_TYPE_A;
	
	/**
	 * ID: 1081<br>
	 * Message: Being permanent, are you sure you wish to use the hair dye potion - Type A?
	 */
	public static final SystemMessageId HAIRDYE_POTION_TYPE_A;
	
	/**
	 * ID: 1082<br>
	 * Message: Do you wish to use the hair style change potion - Type A? It is permanent.
	 */
	public static final SystemMessageId HAIRSTYLE_POTION_TYPE_A;
	
	/**
	 * ID: 1083<br>
	 * Message: Facelift potion - Type A is being applied.
	 */
	public static final SystemMessageId FACELIFT_POTION_TYPE_A_APPLIED;
	
	/**
	 * ID: 1084<br>
	 * Message: Hair dye potion - Type A is being applied.
	 */
	public static final SystemMessageId HAIRDYE_POTION_TYPE_A_APPLIED;
	
	/**
	 * ID: 1085<br>
	 * Message: The hair style chance potion - Type A is being used.
	 */
	public static final SystemMessageId HAIRSTYLE_POTION_TYPE_A_USED;
	
	/**
	 * ID: 1086<br>
	 * Message: Your facial appearance has been changed.
	 */
	public static final SystemMessageId FACE_APPEARANCE_CHANGED;
	
	/**
	 * ID: 1087<br>
	 * Message: Your hair color has changed.
	 */
	public static final SystemMessageId HAIR_COLOR_CHANGED;
	
	/**
	 * ID: 1088<br>
	 * Message: Your hair style has been changed.
	 */
	public static final SystemMessageId HAIR_STYLE_CHANGED;
	
	/**
	 * ID: 1089<br>
	 * Message: $c1 has obtained a first anniversary commemorative item.
	 */
	public static final SystemMessageId C1_OBTAINED_ANNIVERSARY_ITEM;
	
	/**
	 * ID: 1090<br>
	 * Message: Being permanent, are you sure you wish to use the facelift potion - Type B?
	 */
	public static final SystemMessageId FACELIFT_POTION_TYPE_B;
	
	/**
	 * ID: 1091<br>
	 * Message: Being permanent, are you sure you wish to use the facelift potion - Type C?
	 */
	public static final SystemMessageId FACELIFT_POTION_TYPE_C;
	
	/**
	 * ID: 1092<br>
	 * Message: Being permanent, are you sure you wish to use the hair dye potion - Type B?
	 */
	public static final SystemMessageId HAIRDYE_POTION_TYPE_B;
	
	/**
	 * ID: 1093<br>
	 * Message: Being permanent, are you sure you wish to use the hair dye potion - Type C?
	 */
	public static final SystemMessageId HAIRDYE_POTION_TYPE_C;
	
	/**
	 * ID: 1094<br>
	 * Message: Being permanent, are you sure you wish to use the hair dye potion - Type D?
	 */
	public static final SystemMessageId HAIRDYE_POTION_TYPE_D;
	
	/**
	 * ID: 1095<br>
	 * Message: Do you wish to use the hair style change potion - Type B? It is permanent.
	 */
	public static final SystemMessageId HAIRSTYLE_POTION_TYPE_B;
	
	/**
	 * ID: 1096<br>
	 * Message: Do you wish to use the hair style change potion - Type C? It is permanent.
	 */
	public static final SystemMessageId HAIRSTYLE_POTION_TYPE_C;
	
	/**
	 * ID: 1097<br>
	 * Message: Do you wish to use the hair style change potion - Type D? It is permanent.
	 */
	public static final SystemMessageId HAIRSTYLE_POTION_TYPE_D;
	
	/**
	 * ID: 1098<br>
	 * Message: Do you wish to use the hair style change potion - Type E? It is permanent.
	 */
	public static final SystemMessageId HAIRSTYLE_POTION_TYPE_E;
	
	/**
	 * ID: 1099<br>
	 * Message: Do you wish to use the hair style change potion - Type F? It is permanent.
	 */
	public static final SystemMessageId HAIRSTYLE_POTION_TYPE_F;
	
	/**
	 * ID: 1100<br>
	 * Message: Do you wish to use the hair style change potion - Type G? It is permanent.
	 */
	public static final SystemMessageId HAIRSTYLE_POTION_TYPE_G;
	
	/**
	 * ID: 1101<br>
	 * Message: Facelift potion - Type B is being applied.
	 */
	public static final SystemMessageId FACELIFT_POTION_TYPE_B_APPLIED;
	
	/**
	 * ID: 1102<br>
	 * Message: Facelift potion - Type C is being applied.
	 */
	public static final SystemMessageId FACELIFT_POTION_TYPE_C_APPLIED;
	
	/**
	 * ID: 1103<br>
	 * Message: Hair dye potion - Type B is being applied.
	 */
	public static final SystemMessageId HAIRDYE_POTION_TYPE_B_APPLIED;
	
	/**
	 * ID: 1104<br>
	 * Message: Hair dye potion - Type C is being applied.
	 */
	public static final SystemMessageId HAIRDYE_POTION_TYPE_C_APPLIED;
	
	/**
	 * ID: 1105<br>
	 * Message: Hair dye potion - Type D is being applied.
	 */
	public static final SystemMessageId HAIRDYE_POTION_TYPE_D_APPLIED;
	
	/**
	 * ID: 1106<br>
	 * Message: The hair style chance potion - Type B is being used.
	 */
	public static final SystemMessageId HAIRSTYLE_POTION_TYPE_B_USED;
	
	/**
	 * ID: 1107<br>
	 * Message: The hair style chance potion - Type C is being used.
	 */
	public static final SystemMessageId HAIRSTYLE_POTION_TYPE_C_USED;
	
	/**
	 * ID: 1108<br>
	 * Message: The hair style chance potion - Type D is being used.
	 */
	public static final SystemMessageId HAIRSTYLE_POTION_TYPE_D_USED;
	
	/**
	 * ID: 1109<br>
	 * Message: The hair style chance potion - Type E is being used.
	 */
	public static final SystemMessageId HAIRSTYLE_POTION_TYPE_E_USED;
	
	/**
	 * ID: 1110<br>
	 * Message: The hair style chance potion - Type F is being used.
	 */
	public static final SystemMessageId HAIRSTYLE_POTION_TYPE_F_USED;
	
	/**
	 * ID: 1111<br>
	 * Message: The hair style chance potion - Type G is being used.
	 */
	public static final SystemMessageId HAIRSTYLE_POTION_TYPE_G_USED;
	
	/**
	 * ID: 1112<br>
	 * Message: The prize amount for the winner of Lottery #$s1 is $s2 adena. We have $s3 first prize winners.
	 */
	public static final SystemMessageId AMOUNT_FOR_WINNER_S1_IS_S2_ADENA_WE_HAVE_S3_PRIZE_WINNER;
	
	/**
	 * ID: 1113<br>
	 * Message: The prize amount for Lucky Lottery #$s1 is $s2 adena. There was no first prize winner in this drawing, therefore the jackpot will be added to the next drawing.
	 */
	public static final SystemMessageId AMOUNT_FOR_LOTTERY_S1_IS_S2_ADENA_NO_WINNER;
	
	/**
	 * ID: 1114<br>
	 * Message: Your clan may not register to participate in a siege while under a grace period of the clan's dissolution.
	 */
	public static final SystemMessageId CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS;
	
	/**
	 * ID: 1115<br>
	 * Message: Individuals may not surrender during combat.
	 */
	public static final SystemMessageId INDIVIDUALS_NOT_SURRENDER_DURING_COMBAT;
	
	/**
	 * ID: 1116<br>
	 * Message: One cannot leave one's clan during combat.
	 */
	public static final SystemMessageId YOU_CANNOT_LEAVE_DURING_COMBAT;
	
	/**
	 * ID: 1117<br>
	 * Message: A clan member may not be dismissed during combat.
	 */
	public static final SystemMessageId CLAN_MEMBER_CANNOT_BE_DISMISSED_DURING_COMBAT;
	
	/**
	 * ID: 1118<br>
	 * Message: Progress in a quest is possible only when your inventory's weight and volume are less than 80 percent of capacity.
	 */
	public static final SystemMessageId INVENTORY_LESS_THAN_80_PERCENT;
	
	/**
	 * ID: 1119<br>
	 * Message: Quest was automatically canceled when you attempted to settle the accounts of your quest while your inventory exceeded 80 percent of capacity.
	 */
	public static final SystemMessageId QUEST_CANCELED_INVENTORY_EXCEEDS_80_PERCENT;
	
	/**
	 * ID: 1120<br>
	 * Message: You are still a member of the clan.
	 */
	public static final SystemMessageId STILL_CLAN_MEMBER;
	
	/**
	 * ID: 1121<br>
	 * Message: You do not have the right to vote.
	 */
	public static final SystemMessageId NO_RIGHT_TO_VOTE;
	
	/**
	 * ID: 1122<br>
	 * Message: There is no candidate.
	 */
	public static final SystemMessageId NO_CANDIDATE;
	
	/**
	 * ID: 1123<br>
	 * Message: Weight and volume limit has been exceeded. That skill is currently unavailable.
	 */
	public static final SystemMessageId WEIGHT_EXCEEDED_SKILL_UNAVAILABLE;
	
	/**
	 * ID: 1124<br>
	 * Message: Your recipe book may not be accessed while using a skill.
	 */
	public static final SystemMessageId NO_RECIPE_BOOK_WHILE_CASTING;
	
	/**
	 * ID: 1125<br>
	 * Message: An item may not be created while engaged in trading.
	 */
	public static final SystemMessageId CANNOT_CREATED_WHILE_ENGAGED_IN_TRADING;
	
	/**
	 * ID: 1126<br>
	 * Message: You cannot enter a negative number.
	 */
	public static final SystemMessageId NO_NEGATIVE_NUMBER;
	
	/**
	 * ID: 1127<br>
	 * Message: The reward must be less than 10 times the standard price.
	 */
	public static final SystemMessageId REWARD_LESS_THAN_10_TIMES_STANDARD_PRICE;
	
	/**
	 * ID: 1128<br>
	 * Message: A private store may not be opened while using a skill.
	 */
	public static final SystemMessageId PRIVATE_STORE_NOT_WHILE_CASTING;
	
	/**
	 * ID: 1129<br>
	 * Message: This is not allowed while riding a ferry or boat.
	 */
	public static final SystemMessageId NOT_ALLOWED_ON_BOAT;
	
	/**
	 * ID: 1130<br>
	 * Message: You have given $s1 damage to your target and $s2 damage to the servitor.
	 */
	public static final SystemMessageId GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR;
	
	/**
	 * ID: 1131<br>
	 * Message: It is now midnight and the effect of $s1 can be felt.
	 */
	public static final SystemMessageId NIGHT_EFFECT_APPLIES;
	
	/**
	 * ID: 1132<br>
	 * Message: It is now dawn and the effect of $s1 will now disappear.
	 */
	public static final SystemMessageId DAY_EFFECT_DISAPPEARS;
	
	/**
	 * ID: 1133<br>
	 * Message: Since HP has decreased, the effect of $s1 can be felt.
	 */
	public static final SystemMessageId HP_DECREASED_EFFECT_APPLIES;
	
	/**
	 * ID: 1134<br>
	 * Message: Since HP has increased, the effect of $s1 will disappear.
	 */
	public static final SystemMessageId HP_INCREASED_EFFECT_DISAPPEARS;
	
	/**
	 * ID: 1135<br>
	 * Message: While you are engaged in combat, you cannot operate a private store or private workshop.
	 */
	public static final SystemMessageId CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT;
	
	/**
	 * ID: 1136<br>
	 * Message: Since there was an account that used this IP and attempted to log in illegally, this account is not allowed to connect to the game server for $s1 minutes. Please use another game server.
	 */
	public static final SystemMessageId ACCOUNT_NOT_ALLOWED_TO_CONNECT;
	
	/**
	 * ID: 1137<br>
	 * Message: $c1 harvested $s3 $s2(s).
	 */
	public static final SystemMessageId C1_HARVESTED_S3_S2S;
	
	/**
	 * ID: 1138<br>
	 * Message: $c1 harvested $s2(s).
	 */
	public static final SystemMessageId C1_HARVESTED_S2S;
	
	/**
	 * ID: 1139<br>
	 * Message: The weight and volume limit of your inventory must not be exceeded.
	 */
	public static final SystemMessageId INVENTORY_LIMIT_MUST_NOT_BE_EXCEEDED;
	
	/**
	 * ID: 1140<br>
	 * Message: Would you like to open the gate?
	 */
	public static final SystemMessageId WOULD_YOU_LIKE_TO_OPEN_THE_GATE;
	
	/**
	 * ID: 1141<br>
	 * Message: Would you like to close the gate?
	 */
	public static final SystemMessageId WOULD_YOU_LIKE_TO_CLOSE_THE_GATE;
	
	/**
	 * ID: 1142<br>
	 * Message: Since $s1 already exists nearby, you cannot summon it again.
	 */
	public static final SystemMessageId CANNOT_SUMMON_S1_AGAIN;
	
	/**
	 * ID: 1143<br>
	 * Message: Since you do not have enough items to maintain the servitor's stay, the servitor will disappear.
	 */
	public static final SystemMessageId SERVITOR_DISAPPEARED_NOT_ENOUGH_ITEMS;
	
	/**
	 * ID: 1144<br>
	 * Message: Currently, you don't have anybody to chat with in the game.
	 */
	public static final SystemMessageId NOBODY_IN_GAME_TO_CHAT;
	
	/**
	 * ID: 1145<br>
	 * Message: $s2 has been created for $c1 after the payment of $s3 adena is received.
	 */
	public static final SystemMessageId S2_CREATED_FOR_C1_FOR_S3_ADENA;
	
	/**
	 * ID: 1146<br>
	 * Message: $c1 created $s2 after receiving $s3 adena.
	 */
	public static final SystemMessageId C1_CREATED_S2_FOR_S3_ADENA;
	
	/**
	 * ID: 1147<br>
	 * Message: $s2 $s3 have been created for $c1 at the price of $s4 adena.
	 */
	public static final SystemMessageId S2_S3_S_CREATED_FOR_C1_FOR_S4_ADENA;
	
	/**
	 * ID: 1148<br>
	 * Message: $c1 created $s2 $s3 at the price of $s4 adena.
	 */
	public static final SystemMessageId C1_CREATED_S2_S3_S_FOR_S4_ADENA;
	
	/**
	 * ID: 1149<br>
	 * Message: Your attempt to create $s2 for $c1 at the price of $s3 adena has failed.
	 */
	public static final SystemMessageId CREATION_OF_S2_FOR_C1_AT_S3_ADENA_FAILED;
	
	/**
	 * ID: 1150<br>
	 * Message: $c1 has failed to create $s2 at the price of $s3 adena.
	 */
	public static final SystemMessageId C1_FAILED_TO_CREATE_S2_FOR_S3_ADENA;
	
	/**
	 * ID: 1151<br>
	 * Message: $s2 is sold to $c1 at the price of $s3 adena.
	 */
	public static final SystemMessageId S2_SOLD_TO_C1_FOR_S3_ADENA;
	
	/**
	 * ID: 1152<br>
	 * Message: $s2 $s3 have been sold to $c1 for $s4 adena.
	 */
	public static final SystemMessageId S3_S2_S_SOLD_TO_C1_FOR_S4_ADENA;
	
	/**
	 * ID: 1153<br>
	 * Message: $s2 has been purchased from $c1 at the price of $s3 adena.
	 */
	public static final SystemMessageId S2_PURCHASED_FROM_C1_FOR_S3_ADENA;
	
	/**
	 * ID: 1154<br>
	 * Message: $s3 $s2 has been purchased from $c1 for $s4 adena.
	 */
	public static final SystemMessageId S3_S2_S_PURCHASED_FROM_C1_FOR_S4_ADENA;
	
	/**
	 * ID: 1155<br>
	 * Message: +$s2 $s3 have been sold to $c1 for $s4 adena.
	 */
	public static final SystemMessageId S3_S2_SOLD_TO_C1_FOR_S4_ADENA;
	
	/**
	 * ID: 1156<br>
	 * Message: +$s2 $s3 has been purchased from $c1 for $s4 adena.
	 */
	public static final SystemMessageId S2_S3_PURCHASED_FROM_C1_FOR_S4_ADENA;
	
	/**
	 * ID: 1157<br>
	 * Message: Trying on state lasts for only 5 seconds. When a character's state changes, it can be cancelled.
	 */
	public static final SystemMessageId TRYING_ON_STATE;
	
	/**
	 * ID: 1158<br>
	 * Message: You cannot dismount from this elevation.
	 */
	public static final SystemMessageId CANNOT_DISMOUNT_FROM_ELEVATION;
	
	/**
	 * ID: 1159<br>
	 * Message: The ferry from Talking Island will arrive at Gludin Harbor in approximately 10 minutes.
	 */
	public static final SystemMessageId FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_10_MINUTES;
	
	/**
	 * ID: 1160<br>
	 * Message: The ferry from Talking Island will be arriving at Gludin Harbor in approximately 5 minutes.
	 */
	public static final SystemMessageId FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_5_MINUTES;
	
	/**
	 * ID: 1161<br>
	 * Message: The ferry from Talking Island will be arriving at Gludin Harbor in approximately 1 minute.
	 */
	public static final SystemMessageId FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_1_MINUTE;
	
	/**
	 * ID: 1162<br>
	 * Message: The ferry from Giran Harbor will be arriving at Talking Island in approximately 15 minutes.
	 */
	public static final SystemMessageId FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_15_MINUTES;
	
	/**
	 * ID: 1163<br>
	 * Message: The ferry from Giran Harbor will be arriving at Talking Island in approximately 10 minutes.
	 */
	public static final SystemMessageId FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_10_MINUTES;
	
	/**
	 * ID: 1164<br>
	 * Message: The ferry from Giran Harbor will be arriving at Talking Island in approximately 5 minutes.
	 */
	public static final SystemMessageId FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_5_MINUTES;
	
	/**
	 * ID: 1165<br>
	 * Message: The ferry from Giran Harbor will be arriving at Talking Island in approximately 1 minute.
	 */
	public static final SystemMessageId FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_1_MINUTE;
	
	/**
	 * ID: 1166<br>
	 * Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 20 minutes.
	 */
	public static final SystemMessageId FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_20_MINUTES;
	
	/**
	 * ID: 1167<br>
	 * Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 20 minutes.
	 */
	public static final SystemMessageId FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_15_MINUTES;
	
	/**
	 * ID: 1168<br>
	 * Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 20 minutes.
	 */
	public static final SystemMessageId FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_10_MINUTES;
	
	/**
	 * ID: 1169<br>
	 * Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 20 minutes.
	 */
	public static final SystemMessageId FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_5_MINUTES;
	
	/**
	 * ID: 1170<br>
	 * Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 1 minute.
	 */
	public static final SystemMessageId FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_1_MINUTE;
	
	/**
	 * ID: 1171<br>
	 * Message: The Innadril pleasure boat will arrive in approximately 20 minutes.
	 */
	public static final SystemMessageId INNADRIL_BOAT_ARRIVE_20_MINUTES;
	
	/**
	 * ID: 1172<br>
	 * Message: The Innadril pleasure boat will arrive in approximately 15 minutes.
	 */
	public static final SystemMessageId INNADRIL_BOAT_ARRIVE_15_MINUTES;
	
	/**
	 * ID: 1173<br>
	 * Message: The Innadril pleasure boat will arrive in approximately 10 minutes.
	 */
	public static final SystemMessageId INNADRIL_BOAT_ARRIVE_10_MINUTES;
	
	/**
	 * ID: 1174<br>
	 * Message: The Innadril pleasure boat will arrive in approximately 5 minutes.
	 */
	public static final SystemMessageId INNADRIL_BOAT_ARRIVE_5_MINUTES;
	
	/**
	 * ID: 1175<br>
	 * Message: The Innadril pleasure boat will arrive in approximately 1 minute.
	 */
	public static final SystemMessageId INNADRIL_BOAT_ARRIVE_1_MINUTE;
	
	/**
	 * ID: 1176<br>
	 * Message: The SSQ Competition period is underway.
	 */
	public static final SystemMessageId SSQ_COMPETITION_UNDERWAY;
	
	/**
	 * ID: 1177<br>
	 * Message: This is the seal validation period.
	 */
	public static final SystemMessageId VALIDATION_PERIOD;
	
	/**
	 * ID: 1178<br>
	 * Message: <Seal of Avarice description>
	 */
	public static final SystemMessageId AVARICE_DESCRIPTION;
	
	/**
	 * ID: 1179<br>
	 * Message: <Seal of Gnosis description>
	 */
	public static final SystemMessageId GNOSIS_DESCRIPTION;
	
	/**
	 * ID: 1180<br>
	 * Message: <Seal of Strife description>
	 */
	public static final SystemMessageId STRIFE_DESCRIPTION;
	
	/**
	 * ID: 1181<br>
	 * Message: Do you really wish to change the title?
	 */
	public static final SystemMessageId CHANGE_TITLE_CONFIRM;
	
	/**
	 * ID: 1182<br>
	 * Message: Are you sure you wish to delete the clan crest?
	 */
	public static final SystemMessageId CREST_DELETE_CONFIRM;
	
	/**
	 * ID: 1183<br>
	 * Message: This is the initial period.
	 */
	public static final SystemMessageId INITIAL_PERIOD;
	
	/**
	 * ID: 1184<br>
	 * Message: This is a period of calculating statistics in the server.
	 */
	public static final SystemMessageId RESULTS_PERIOD;
	
	/**
	 * ID: 1185<br>
	 * Message: days left until deletion.
	 */
	public static final SystemMessageId DAYS_LEFT_UNTIL_DELETION;
	
	/**
	 * ID: 1186<br>
	 * Message: To create a new account, please visit the PlayNC website (http://www.plaync.com/us/support/)
	 */
	public static final SystemMessageId TO_CREATE_ACCOUNT_VISIT_WEBSITE;
	
	/**
	 * ID: 1187<br>
	 * Message: If you forgotten your account information or password, please visit the Support Center on the PlayNC website(http://www.plaync.com/us/support/)
	 */
	public static final SystemMessageId ACCOUNT_INFORMATION_FORGOTTON_VISIT_WEBSITE;
	
	/**
	 * ID: 1188<br>
	 * Message: Your selected target can no longer receive a recommendation.
	 */
	public static final SystemMessageId YOUR_TARGET_NO_LONGER_RECEIVE_A_RECOMMENDATION;
	
	/**
	 * ID: 1189<br>
	 * Message: This temporary alliance of the Castle Attacker team is in effect. It will be dissolved when the Castle Lord is replaced.
	 */
	public static final SystemMessageId TEMPORARY_ALLIANCE;
	
	/**
	 * ID: 1189<br>
	 * Message: This temporary alliance of the Castle Attacker team has been dissolved.
	 */
	public static final SystemMessageId TEMPORARY_ALLIANCE_DISSOLVED;
	
	/**
	 * ID: 1191<br>
	 * Message: The ferry from Gludin Harbor will be arriving at Talking Island in approximately 10 minutes.
	 */
	public static final SystemMessageId FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_10_MINUTES;
	
	/**
	 * ID: 1192<br>
	 * Message: The ferry from Gludin Harbor will be arriving at Talking Island in approximately 5 minutes.
	 */
	public static final SystemMessageId FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_5_MINUTES;
	
	/**
	 * ID: 1193<br>
	 * Message: The ferry from Gludin Harbor will be arriving at Talking Island in approximately 1 minute.
	 */
	public static final SystemMessageId FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_1_MINUTE;
	
	/**
	 * ID: 1194<br>
	 * Message: A mercenary can be assigned to a position from the beginning of the Seal Validatio period until the time when a siege starts.
	 */
	public static final SystemMessageId MERC_CAN_BE_ASSIGNED;
	
	/**
	 * ID: 1195<br>
	 * Message: This mercenary cannot be assigned to a position by using the Seal of Strife.
	 */
	public static final SystemMessageId MERC_CANT_BE_ASSIGNED_USING_STRIFE;
	
	/**
	 * ID: 1196<br>
	 * Message: Your force has reached maximum capacity.
	 */
	public static final SystemMessageId FORCE_MAXIMUM;
	
	/**
	 * ID: 1197<br>
	 * Message: Summoning a servitor costs $s2 $s1.
	 */
	public static final SystemMessageId SUMMONING_SERVITOR_COSTS_S2_S1;
	
	/**
	 * ID: 1198<br>
	 * Message: The item has been successfully crystallized.
	 */
	public static final SystemMessageId CRYSTALLIZATION_SUCCESSFUL;
	
	/**
	 * ID: 1199<br>
	 * Message: =======<Clan War Target>=======
	 */
	public static final SystemMessageId CLAN_WAR_HEADER;
	
	/**
	 * ID: 1200<br>
	 * Message:($s1 ($s2 Alliance)
	 */
	public static final SystemMessageId S1_S2_ALLIANCE;
	
	/**
	 * ID: 1201<br>
	 * Message: Please select the quest you wish to abort.
	 */
	public static final SystemMessageId SELECT_QUEST_TO_ABOR;
	
	/**
	 * ID: 1202<br>
	 * Message:($s1 (No alliance exists)
	 */
	public static final SystemMessageId S1_NO_ALLI_EXISTS;
	
	/**
	 * ID: 1203<br>
	 * Message: There is no clan war in progress.
	 */
	public static final SystemMessageId NO_WAR_IN_PROGRESS;
	
	/**
	 * ID: 1204<br>
	 * Message: The screenshot has been saved. ($s1 $s2x$s3)
	 */
	public static final SystemMessageId SCREENSHOT;
	
	/**
	 * ID: 1205<br>
	 * Message: Your mailbox is full. There is a 100 message limit.
	 */
	public static final SystemMessageId MAILBOX_FULL;
	
	/**
	 * ID: 1206<br>
	 * Message: The memo box is full. There is a 100 memo limit.
	 */
	public static final SystemMessageId MEMOBOX_FULL;
	
	/**
	 * ID: 1207<br>
	 * Message: Please make an entry in the field.
	 */
	public static final SystemMessageId MAKE_AN_ENTRY;
	
	/**
	 * ID: 1208<br>
	 * Message: $c1 died and dropped $s3 $s2.
	 */
	public static final SystemMessageId C1_DIED_DROPPED_S3_S2;
	
	/**
	 * ID: 1209<br>
	 * Message: Congratulations. Your raid was successful.
	 */
	public static final SystemMessageId RAID_WAS_SUCCESSFUL;
	
	/**
	 * ID: 1210<br>
	 * Message: Seven Signs: The quest event period has begun. Visit a Priest of Dawn or Priestess of Dusk to participate in the event.
	 */
	public static final SystemMessageId QUEST_EVENT_PERIOD_BEGUN;
	
	/**
	 * ID: 1211<br>
	 * Message: Seven Signs: The quest event period has ended. The next quest event will start in one week.
	 */
	public static final SystemMessageId QUEST_EVENT_PERIOD_ENDED;
	
	/**
	 * ID: 1212<br>
	 * Message: Seven Signs: The Lords of Dawn have obtained the Seal of Avarice.
	 */
	public static final SystemMessageId DAWN_OBTAINED_AVARICE;
	
	/**
	 * ID: 1213<br>
	 * Message: Seven Signs: The Lords of Dawn have obtained the Seal of Gnosis.
	 */
	public static final SystemMessageId DAWN_OBTAINED_GNOSIS;
	
	/**
	 * ID: 1214<br>
	 * Message: Seven Signs: The Lords of Dawn have obtained the Seal of Strife.
	 */
	public static final SystemMessageId DAWN_OBTAINED_STRIFE;
	
	/**
	 * ID: 1215<br>
	 * Message: Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Avarice.
	 */
	public static final SystemMessageId DUSK_OBTAINED_AVARICE;
	
	/**
	 * ID: 1216<br>
	 * Message: Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Gnosis.
	 */
	public static final SystemMessageId DUSK_OBTAINED_GNOSIS;
	
	/**
	 * ID: 1217<br>
	 * Message: Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Strife.
	 */
	public static final SystemMessageId DUSK_OBTAINED_STRIFE;
	
	/**
	 * ID: 1218<br>
	 * Message: Seven Signs: The Seal Validation period has begun.
	 */
	public static final SystemMessageId SEAL_VALIDATION_PERIOD_BEGUN;
	
	/**
	 * ID: 1219<br>
	 * Message: Seven Signs: The Seal Validation period has ended.
	 */
	public static final SystemMessageId SEAL_VALIDATION_PERIOD_ENDED;
	
	/**
	 * ID: 1220<br>
	 * Message: Are you sure you wish to summon it?
	 */
	public static final SystemMessageId SUMMON_CONFIRM;
	
	/**
	 * ID: 1221<br>
	 * Message: Are you sure you wish to return it?
	 */
	public static final SystemMessageId RETURN_CONFIRM;
	
	/**
	 * ID: 1222<br>
	 * Message: Current location : $s1, $s2, $s3 (GM Consultation Service)
	 */
	public static final SystemMessageId LOC_GM_CONSULATION_SERVICE_S1_S2_S3;
	
	/**
	 * ID: 1223<br>
	 * Message: We depart for Talking Island in five minutes.
	 */
	public static final SystemMessageId DEPART_FOR_TALKING_5_MINUTES;
	
	/**
	 * ID: 1224<br>
	 * Message: We depart for Talking Island in one minute.
	 */
	public static final SystemMessageId DEPART_FOR_TALKING_1_MINUTE;
	
	/**
	 * ID: 1225<br>
	 * Message: All aboard for Talking Island
	 */
	public static final SystemMessageId DEPART_FOR_TALKING;
	
	/**
	 * ID: 1226<br>
	 * Message: We are now leaving for Talking Island.
	 */
	public static final SystemMessageId LEAVING_FOR_TALKING;
	
	/**
	 * ID: 1227<br>
	 * Message: You have $s1 unread messages.
	 */
	public static final SystemMessageId S1_UNREAD_MESSAGES;
	
	/**
	 * ID: 1228<br>
	 * Message: $c1 has blocked you. You cannot send mail to $c1.
	 */
	public static final SystemMessageId C1_BLOCKED_YOU_CANNOT_MAIL;
	
	/**
	 * ID: 1229<br>
	 * Message: No more messages may be sent at this time. Each account is allowed 10 messages per day.
	 */
	public static final SystemMessageId NO_MORE_MESSAGES_TODAY;
	
	/**
	 * ID: 1230<br>
	 * Message: You are limited to five recipients at a time.
	 */
	public static final SystemMessageId ONLY_FIVE_RECIPIENTS;
	
	/**
	 * ID: 1231<br>
	 * Message: You've sent mail.
	 */
	public static final SystemMessageId SENT_MAIL;
	
	/**
	 * ID: 1232<br>
	 * Message: The message was not sent.
	 */
	public static final SystemMessageId MESSAGE_NOT_SENT;
	
	/**
	 * ID: 1233<br>
	 * Message: You've got mail.
	 */
	public static final SystemMessageId NEW_MAIL;
	
	/**
	 * ID: 1234<br>
	 * Message: The mail has been stored in your temporary mailbox.
	 */
	public static final SystemMessageId MAIL_STORED_IN_MAILBOX;
	
	/**
	 * ID: 1235<br>
	 * Message: Do you wish to delete all your friends?
	 */
	public static final SystemMessageId ALL_FRIENDS_DELETE_CONFIRM;
	
	/**
	 * ID: 1236<br>
	 * Message: Please enter security card number.
	 */
	public static final SystemMessageId ENTER_SECURITY_CARD_NUMBER;
	
	/**
	 * ID: 1237<br>
	 * Message: Please enter the card number for number $s1.
	 */
	public static final SystemMessageId ENTER_CARD_NUMBER_FOR_S1;
	
	/**
	 * ID: 1238<br>
	 * Message: Your temporary mailbox is full. No more mail can be stored; you have reached the 10 message limit.
	 */
	public static final SystemMessageId TEMP_MAILBOX_FULL;
	
	/**
	 * ID: 1239<br>
	 * Message: The keyboard security module has failed to load. Please exit the game and try again.
	 */
	public static final SystemMessageId KEYBOARD_MODULE_FAILED_LOAD;
	
	/**
	 * ID: 1240<br>
	 * Message: Seven Signs: The Revolutionaries of Dusk have won.
	 */
	public static final SystemMessageId DUSK_WON;
	
	/**
	 * ID: 1241<br>
	 * Message: Seven Signs: The Lords of Dawn have won.
	 */
	public static final SystemMessageId DAWN_WON;
	
	/**
	 * ID: 1242<br>
	 * Message: Users who have not verified their age may not log in between the hours if 10:00 p.m. and 6:00 a.m.
	 */
	public static final SystemMessageId NOT_VERIFIED_AGE_NO_LOGIN;
	
	/**
	 * ID: 1243<br>
	 * Message: The security card number is invalid.
	 */
	public static final SystemMessageId SECURITY_CARD_NUMBER_INVALID;
	
	/**
	 * ID: 1244<br>
	 * Message: Users who have not verified their age may not log in between the hours if 10:00 p.m. and 6:00 a.m. Logging off now
	 */
	public static final SystemMessageId NOT_VERIFIED_AGE_LOG_OFF;
	
	/**
	 * ID: 1245<br>
	 * Message: You will be loged out in $s1 minutes.
	 */
	public static final SystemMessageId LOGOUT_IN_S1_MINUTES;
	
	/**
	 * ID: 1246<br>
	 * Message: $c1 died and has dropped $s2 adena.
	 */
	public static final SystemMessageId C1_DIED_DROPPED_S2_ADENA;
	
	/**
	 * ID: 1247<br>
	 * Message: The corpse is too old. The skill cannot be used.
	 */
	public static final SystemMessageId CORPSE_TOO_OLD_SKILL_NOT_USED;
	
	/**
	 * ID: 1248<br>
	 * Message: You are out of feed. Mount status canceled.
	 */
	public static final SystemMessageId OUT_OF_FEED_MOUNT_CANCELED;
	
	/**
	 * ID: 1249<br>
	 * Message: You may only ride a wyvern while you're riding a strider.
	 */
	public static final SystemMessageId YOU_MAY_ONLY_RIDE_WYVERN_WHILE_RIDING_STRIDER;
	
	/**
	 * ID: 1250<br>
	 * Message: Do you really want to surrender? If you surrender during an alliance war, your Exp will drop the same as if you were to die once.
	 */
	public static final SystemMessageId SURRENDER_ALLY_WAR_CONFIRM;
	
	/**
	 * ID: 1251<br>
	 * Message: Are you sure you want to dismiss the alliance? If you use the /allydismiss command, you will not be able to accept another clan to your alliance for one day.
	 */
	public static final SystemMessageId DISMISS_ALLY_CONFIRM;
	
	/**
	 * ID: 1252<br>
	 * Message: Are you sure you want to surrender? Exp penalty will be the same as death.
	 */
	public static final SystemMessageId SURRENDER_CONFIRM1;
	
	/**
	 * ID: 1253<br>
	 * Message: Are you sure you want to surrender? Exp penalty will be the same as death and you will not be allowed to participate in clan war.
	 */
	public static final SystemMessageId SURRENDER_CONFIRM2;
	
	/**
	 * ID: 1254<br>
	 * Message: Thank you for submitting feedback.
	 */
	public static final SystemMessageId THANKS_FOR_FEEDBACK;
	
	/**
	 * ID: 1255<br>
	 * Message: GM consultation has begun.
	 */
	public static final SystemMessageId GM_CONSULTATION_BEGUN;
	
	/**
	 * ID: 1256<br>
	 * Message: Please write the name after the command.
	 */
	public static final SystemMessageId PLEASE_WRITE_NAME_AFTER_COMMAND;
	
	/**
	 * ID: 1257<br>
	 * Message: The special skill of a servitor or pet cannot be registerd as a macro.
	 */
	public static final SystemMessageId PET_SKILL_NOT_AS_MACRO;
	
	/**
	 * ID: 1258<br>
	 * Message: $s1 has been crystallized
	 */
	public static final SystemMessageId S1_CRYSTALLIZED;
	
	/**
	 * ID: 1259<br>
	 * Message: =======<Alliance Target>=======
	 */
	public static final SystemMessageId ALLIANCE_TARGET_HEADER;
	
	/**
	 * ID: 1260<br>
	 * Message: Seven Signs: Preparations have begun for the next quest event.
	 */
	public static final SystemMessageId PREPARATIONS_PERIOD_BEGUN;
	
	/**
	 * ID: 1261<br>
	 * Message: Seven Signs: The quest event period has begun. Speak with a Priest of Dawn or Dusk Priestess if you wish to participate in the event.
	 */
	public static final SystemMessageId COMPETITION_PERIOD_BEGUN;
	
	/**
	 * ID: 1262<br>
	 * Message: Seven Signs: Quest event has ended. Results are being tallied.
	 */
	public static final SystemMessageId RESULTS_PERIOD_BEGUN;
	
	/**
	 * ID: 1263<br>
	 * Message: Seven Signs: This is the seal validation period. A new quest event period begins next Monday.
	 */
	public static final SystemMessageId VALIDATION_PERIOD_BEGUN;
	
	/**
	 * ID: 1264<br>
	 * Message: This soul stone cannot currently absorb souls. Absorption has failed.
	 */
	public static final SystemMessageId STONE_CANNOT_ABSORB;
	
	/**
	 * ID: 1265<br>
	 * Message: You can't absorb souls without a soul stone.
	 */
	public static final SystemMessageId CANT_ABSORB_WITHOUT_STONE;
	
	/**
	 * ID: 1266<br>
	 * Message: The exchange has ended.
	 */
	public static final SystemMessageId EXCHANGE_HAS_ENDED;
	
	/**
	 * ID: 1267<br>
	 * Message: Your contribution score is increased by $s1.
	 */
	public static final SystemMessageId CONTRIB_SCORE_INCREASED_S1;
	
	/**
	 * ID: 1268<br>
	 * Message: Do you wish to add class as your sub class?
	 */
	public static final SystemMessageId ADD_SUBCLASS_CONFIRM;
	
	/**
	 * ID: 1269<br>
	 * Message: The new sub class has been added.
	 */
	public static final SystemMessageId ADD_NEW_SUBCLASS;
	
	/**
	 * ID: 1270<br>
	 * Message: The transfer of sub class has been completed.
	 */
	public static final SystemMessageId SUBCLASS_TRANSFER_COMPLETED;
	
	/**
	 * ID: 1271<br>
	 * Message: Do you wish to participate? Until the next seal validation period, you are a member of the Lords of Dawn.
	 */
	public static final SystemMessageId DAWN_CONFIRM;
	
	/**
	 * ID: 1271<br>
	 * Message: Do you wish to participate? Until the next seal validation period, you are a member of the Revolutionaries of Dusk.
	 */
	public static final SystemMessageId DUSK_CONFIRM;
	
	/**
	 * ID: 1273<br>
	 * Message: You will participate in the Seven Signs as a member of the Lords of Dawn.
	 */
	public static final SystemMessageId SEVENSIGNS_PARTECIPATION_DAWN;
	
	/**
	 * ID: 1274<br>
	 * Message: You will participate in the Seven Signs as a member of the Revolutionaries of Dusk.
	 */
	public static final SystemMessageId SEVENSIGNS_PARTECIPATION_DUSK;
	
	/**
	 * ID: 1275<br>
	 * Message: You've chosen to fight for the Seal of Avarice during this quest event period.
	 */
	public static final SystemMessageId FIGHT_FOR_AVARICE;
	
	/**
	 * ID: 1276<br>
	 * Message: You've chosen to fight for the Seal of Gnosis during this quest event period.
	 */
	public static final SystemMessageId FIGHT_FOR_GNOSIS;
	
	/**
	 * ID: 1277<br>
	 * Message: You've chosen to fight for the Seal of Strife during this quest event period.
	 */
	public static final SystemMessageId FIGHT_FOR_STRIFE;
	
	/**
	 * ID: 1278<br>
	 * Message: The NPC server is not operating at this time.
	 */
	public static final SystemMessageId NPC_SERVER_NOT_OPERATING;
	
	/**
	 * ID: 1279<br>
	 * Message: Contribution level has exceeded the limit. You may not continue.
	 */
	public static final SystemMessageId CONTRIB_SCORE_EXCEEDED;
	
	/**
	 * ID: 1280<br>
	 * Message: Magic Critical Hit!
	 */
	public static final SystemMessageId CRITICAL_HIT_MAGIC;
	
	/**
	 * ID: 1281<br>
	 * Message: Your excellent shield defense was a success!
	 */
	public static final SystemMessageId YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS;
	
	/**
	 * ID: 1282<br>
	 * Message: Your Karma has been changed to $s1
	 */
	public static final SystemMessageId YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1;
	
	/**
	 * ID: 1283<br>
	 * Message: The minimum frame option has been activated.
	 */
	public static final SystemMessageId MINIMUM_FRAME_ACTIVATED;
	
	/**
	 * ID: 1284<br>
	 * Message: The minimum frame option has been deactivated.
	 */
	public static final SystemMessageId MINIMUM_FRAME_DEACTIVATED;
	
	/**
	 * ID: 1285<br>
	 * Message: No inventory exists: You cannot purchase an item.
	 */
	public static final SystemMessageId NO_INVENTORY_CANNOT_PURCHASE;
	
	/**
	 * ID: 1286<br>
	 * Message: (Until next Monday at 6:00 p.m.)
	 */
	public static final SystemMessageId UNTIL_MONDAY_6PM;
	
	/**
	 * ID: 1287<br>
	 * Message: (Until today at 6:00 p.m.)
	 */
	public static final SystemMessageId UNTIL_TODAY_6PM;
	
	/**
	 * ID: 1288<br>
	 * Message: If trends continue, $s1 will win and the seal will belong to:
	 */
	public static final SystemMessageId S1_WILL_WIN_COMPETITION;
	
	/**
	 * ID: 1289<br>
	 * Message: (Until next Monday at 6:00 p.m.)
	 */
	public static final SystemMessageId SEAL_OWNED_10_MORE_VOTED;
	
	/**
	 * ID: 1290<br>
	 * Message: Although the seal was not owned, since 35 percent or more people have voted.
	 */
	public static final SystemMessageId SEAL_NOT_OWNED_35_MORE_VOTED;
	
	/**
	 * ID: 1291<br>
	 * Message: Although the seal was owned during the previous period, less than 10% of people have voted.
	 */
	public static final SystemMessageId SEAL_OWNED_10_LESS_VOTED;
	
	/**
	 * ID: 1292<br>
	 * Message: Since the seal was not owned during the previous period, and since less than 35 percent of people have voted.
	 */
	public static final SystemMessageId SEAL_NOT_OWNED_35_LESS_VOTED;
	
	/**
	 * ID: 1293<br>
	 * Message: If current trends continue, it will end in a tie.
	 */
	public static final SystemMessageId COMPETITION_WILL_TIE;
	
	/**
	 * ID: 1294<br>
	 * Message: The competition has ended in a tie. Therefore, nobody has been awarded the seal.
	 */
	public static final SystemMessageId COMPETITION_TIE_SEAL_NOT_AWARDED;
	
	/**
	 * ID: 1295<br>
	 * Message: Sub classes may not be created or changed while a skill is in use.
	 */
	public static final SystemMessageId SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE;
	
	/**
	 * ID: 1296<br>
	 * Message: You cannot open a Private Store here.
	 */
	public static final SystemMessageId NO_PRIVATE_STORE_HERE;
	
	/**
	 * ID: 1297<br>
	 * Message: You cannot open a Private Workshop here.
	 */
	public static final SystemMessageId NO_PRIVATE_WORKSHOP_HERE;
	
	/**
	 * ID: 1298<br>
	 * Message: Please confirm that you would like to exit the Monster Race Track.
	 */
	public static final SystemMessageId MONS_EXIT_CONFIRM;
	
	/**
	 * ID: 1299<br>
	 * Message: $c1's casting has been interrupted.
	 */
	public static final SystemMessageId C1_CASTING_INTERRUPTED;
	
	/**
	 * ID: 1300<br>
	 * Message: You are no longer trying on equipment.
	 */
	public static final SystemMessageId WEAR_ITEMS_STOPPED;
	
	/**
	 * ID: 1301<br>
	 * Message: Only a Lord of Dawn may use this.
	 */
	public static final SystemMessageId CAN_BE_USED_BY_DAWN;
	
	/**
	 * ID: 1302<br>
	 * Message: Only a Revolutionary of Dusk may use this.
	 */
	public static final SystemMessageId CAN_BE_USED_BY_DUSK;
	
	/**
	 * ID: 1303<br>
	 * Message: This may only be used during the quest event period.
	 */
	public static final SystemMessageId CAN_BE_USED_DURING_QUEST_EVENT_PERIOD;
	
	/**
	 * ID: 1304<br>
	 * Message: The influence of the Seal of Strife has caused all defensive registrations to be canceled.
	 */
	public static final SystemMessageId STRIFE_CANCELED_DEFENSIVE_REGISTRATION;
	
	/**
	 * ID: 1305<br>
	 * Message: Seal Stones may only be transferred during the quest event period.
	 */
	public static final SystemMessageId SEAL_STONES_ONLY_WHILE_QUEST;
	
	/**
	 * ID: 1306<br>
	 * Message: You are no longer trying on equipment.
	 */
	public static final SystemMessageId NO_LONGER_TRYING_ON;
	
	/**
	 * ID: 1307<br>
	 * Message: Only during the seal validation period may you settle your account.
	 */
	public static final SystemMessageId SETTLE_ACCOUNT_ONLY_IN_SEAL_VALIDATION;
	
	/**
	 * ID: 1308<br>
	 * Message: Congratulations - You've completed a class transfer!
	 */
	public static final SystemMessageId CLASS_TRANSFER;
	
	/**
	 * ID: 1309<br>
	 * Message: To use this option, you must have the lastest version of MSN Messenger installed on your computer.
	 */
	public static final SystemMessageId LATEST_MSN_REQUIRED;
	
	/**
	 * ID: 1310<br>
	 * Message: For full functionality, the latest version of MSN Messenger must be installed on your computer.
	 */
	public static final SystemMessageId LATEST_MSN_RECOMMENDED;
	
	/**
	 * ID: 1311<br>
	 * Message: Previous versions of MSN Messenger only provide the basic features for in-game MSN Messenger Chat. Add/Delete Contacts and other MSN Messenger options are not available
	 */
	public static final SystemMessageId MSN_ONLY_BASIC;
	
	/**
	 * ID: 1312<br>
	 * Message: The latest version of MSN Messenger may be obtained from the MSN web site (http://messenger.msn.com).
	 */
	public static final SystemMessageId MSN_OBTAINED_FROM;
	
	/**
	 * ID: 1313<br>
	 * Message: $s1, to better serve our customers, all chat histories [...]
	 */
	public static final SystemMessageId S1_CHAT_HISTORIES_STORED;
	
	/**
	 * ID: 1314<br>
	 * Message: Please enter the passport ID of the person you wish to add to your contact list.
	 */
	public static final SystemMessageId ENTER_PASSPORT_FOR_ADDING;
	
	/**
	 * ID: 1315<br>
	 * Message: Deleting a contact will remove that contact from MSN Messenger as well. The contact can still check your online status and well not be blocked from sending you a message.
	 */
	public static final SystemMessageId DELETING_A_CONTACT;
	
	/**
	 * ID: 1316<br>
	 * Message: The contact will be deleted and blocked from your contact list.
	 */
	public static final SystemMessageId CONTACT_WILL_DELETED;
	
	/**
	 * ID: 1317<br>
	 * Message: Would you like to delete this contact?
	 */
	public static final SystemMessageId CONTACT_DELETE_CONFIRM;
	
	/**
	 * ID: 1318<br>
	 * Message: Please select the contact you want to block or unblock.
	 */
	public static final SystemMessageId SELECT_CONTACT_FOR_BLOCK_UNBLOCK;
	
	/**
	 * ID: 1319<br>
	 * Message: Please select the name of the contact you wish to change to another group.
	 */
	public static final SystemMessageId SELECT_CONTACT_FOR_CHANGE_GROUP;
	
	/**
	 * ID: 1320<br>
	 * Message: After selecting the group you wish to move your contact to, press the OK button.
	 */
	public static final SystemMessageId SELECT_GROUP_PRESS_OK;
	
	/**
	 * ID: 1321<br>
	 * Message: Enter the name of the group you wish to add.
	 */
	public static final SystemMessageId ENTER_GROUP_NAME;
	
	/**
	 * ID: 1322<br>
	 * Message: Select the group and enter the new name.
	 */
	public static final SystemMessageId SELECT_GROUP_ENTER_NAME;
	
	/**
	 * ID: 1323<br>
	 * Message: Select the group you wish to delete and click the OK button.
	 */
	public static final SystemMessageId SELECT_GROUP_TO_DELETE;
	
	/**
	 * ID: 1324<br>
	 * Message: Signing in...
	 */
	public static final SystemMessageId SIGNING_IN;
	
	/**
	 * ID: 1325<br>
	 * Message: You've logged into another computer and have been logged out of the .NET Messenger Service on this computer.
	 */
	public static final SystemMessageId ANOTHER_COMPUTER_LOGOUT;
	
	/**
	 * ID: 1326<br>
	 * Message: $s1 :
	 */
	public static final SystemMessageId S1_D;
	
	/**
	 * ID: 1327<br>
	 * Message: The following message could not be delivered:
	 */
	public static final SystemMessageId MESSAGE_NOT_DELIVERED;
	
	/**
	 * ID: 1328<br>
	 * Message: Members of the Revolutionaries of Dusk will not be resurrected.
	 */
	public static final SystemMessageId DUSK_NOT_RESURRECTED;
	
	/**
	 * ID: 1329<br>
	 * Message: You are currently blocked from using the Private Store and Private Workshop.
	 */
	public static final SystemMessageId BLOCKED_FROM_USING_STORE;
	
	/**
	 * ID: 1330<br>
	 * Message: You may not open a Private Store or Private Workshop for another $s1 minute(s)
	 */
	public static final SystemMessageId NO_STORE_FOR_S1_MINUTES;
	
	/**
	 * ID: 1331<br>
	 * Message: You are no longer blocked from using the Private Store and Private Workshop
	 */
	public static final SystemMessageId NO_LONGER_BLOCKED_USING_STORE;
	
	/**
	 * ID: 1332<br>
	 * Message: Items may not be used after your character or pet dies.
	 */
	public static final SystemMessageId NO_ITEMS_AFTER_DEATH;
	
	/**
	 * ID: 1333<br>
	 * Message: The replay file is not accessible. Please verify that the replay.ini exists in your Linage 2 directory.
	 */
	public static final SystemMessageId REPLAY_INACCESSIBLE;
	
	/**
	 * ID: 1334<br>
	 * Message: The new camera data has been stored.
	 */
	public static final SystemMessageId NEW_CAMERA_STORED;
	
	/**
	 * ID: 1335<br>
	 * Message: The attempt to store the new camera data has failed.
	 */
	public static final SystemMessageId CAMERA_STORING_FAILED;
	
	/**
	 * ID: 1336<br>
	 * Message: The replay file, $s1.$$s2 has been corrupted, please check the fle.
	 */
	public static final SystemMessageId REPLAY_S1_S2_CORRUPTED;
	
	/**
	 * ID: 1337<br>
	 * Message: This will terminate the replay. Do you wish to continue?
	 */
	public static final SystemMessageId REPLAY_TERMINATE_CONFIRM;
	
	/**
	 * ID: 1338<br>
	 * Message: You have exceeded the maximum amount that may be transferred at one time.
	 */
	public static final SystemMessageId EXCEEDED_MAXIMUM_AMOUNT;
	
	/**
	 * ID: 1339<br>
	 * Message: Once a macro is assigned to a shortcut, it cannot be run as a macro again.
	 */
	public static final SystemMessageId MACRO_SHORTCUT_NOT_RUN;
	
	/**
	 * ID: 1340<br>
	 * Message: This server cannot be accessed by the coupon you are using.
	 */
	public static final SystemMessageId SERVER_NOT_ACCESSED_BY_COUPON;
	
	/**
	 * ID: 1341<br>
	 * Message: Incorrect name and/or email address.
	 */
	public static final SystemMessageId INCORRECT_NAME_OR_ADDRESS;
	
	/**
	 * ID: 1342<br>
	 * Message: You are already logged in.
	 */
	public static final SystemMessageId ALREADY_LOGGED_IN;
	
	/**
	 * ID: 1343<br>
	 * Message: Incorrect email address and/or password. Your attempt to log into .NET Messenger Service has failed.
	 */
	public static final SystemMessageId INCORRECT_ADDRESS_OR_PASSWORD;
	
	/**
	 * ID: 1344<br>
	 * Message: Your request to log into the .NET Messenger service has failed. Please verify that you are currently connected to the internet.
	 */
	public static final SystemMessageId NET_LOGIN_FAILED;
	
	/**
	 * ID: 1345<br>
	 * Message: Click the OK button after you have selected a contact name.
	 */
	public static final SystemMessageId SELECT_CONTACT_CLICK_OK;
	
	/**
	 * ID: 1346<br>
	 * Message: You are currently entering a chat message.
	 */
	public static final SystemMessageId CURRENTLY_ENTERING_CHAT;
	
	/**
	 * ID: 1347<br>
	 * Message: The Linage II messenger could not carry out the task you requested.
	 */
	public static final SystemMessageId MESSENGER_FAILED_CARRYING_OUT_TASK;
	
	/**
	 * ID: 1348<br>
	 * Message: $s1 has entered the chat room.
	 */
	public static final SystemMessageId S1_ENTERED_CHAT_ROOM;
	
	/**
	 * ID: 1349<br>
	 * Message: $s1 has left the chat room.
	 */
	public static final SystemMessageId S1_LEFT_CHAT_ROOM;
	
	/**
	 * ID: 1350<br>
	 * Message: The state will be changed to indicate "off-line." All the chat windows currently opened will be closed.
	 */
	public static final SystemMessageId GOING_OFFLINE;
	
	/**
	 * ID: 1351<br>
	 * Message: Click the Delete button after selecting the contact you wish to remove.
	 */
	public static final SystemMessageId SELECT_CONTACT_CLICK_REMOVE;
	
	/**
	 * ID: 1352<br>
	 * Message: You have been added to $s1 ($s2)'s contact list.
	 */
	public static final SystemMessageId ADDED_TO_S1_S2_CONTACT_LIST;
	
	/**
	 * ID: 1353<br>
	 * Message: You can set the option to show your status as always being off-line to all of your contacts.
	 */
	public static final SystemMessageId CAN_SET_OPTION_TO_ALWAYS_SHOW_OFFLINE;
	
	/**
	 * ID: 1354<br>
	 * Message: You are not allowed to chat with a contact while chatting block is imposed.
	 */
	public static final SystemMessageId NO_CHAT_WHILE_BLOCKED;
	
	/**
	 * ID: 1355<br>
	 * Message: The contact is currently blocked from chatting.
	 */
	public static final SystemMessageId CONTACT_CURRENTLY_BLOCKED;
	
	/**
	 * ID: 1356<br>
	 * Message: The contact is not currently logged in.
	 */
	public static final SystemMessageId CONTACT_CURRENTLY_OFFLINE;
	
	/**
	 * ID: 1357<br>
	 * Message: You have been blocked from chatting with that contact.
	 */
	public static final SystemMessageId YOU_ARE_BLOCKED;
	
	/**
	 * ID: 1358<br>
	 * Message: You are being logged out...
	 */
	public static final SystemMessageId YOU_ARE_LOGGING_OUT;
	
	/**
	 * ID: 1359<br>
	 * Message: $s1 has logged in.
	 */
	public static final SystemMessageId S1_LOGGED_IN2;
	
	/**
	 * ID: 1360<br>
	 * Message: You have received a message from $s1.
	 */
	public static final SystemMessageId GOT_MESSAGE_FROM_S1;
	
	/**
	 * ID: 1361<br>
	 * Message: Due to a system error, you have been logged out of the .NET Messenger Service.
	 */
	public static final SystemMessageId LOGGED_OUT_DUE_TO_ERROR;
	
	/**
	 * ID: 1362<br>
	 * click the button next to My Status and then use the Options menu.
	 */
	public static final SystemMessageId SELECT_CONTACT_TO_DELETE;
	
	/**
	 * ID: 1363<br>
	 * Message: Your request to participate in the alliance war has been denied.
	 */
	public static final SystemMessageId YOUR_REQUEST_ALLIANCE_WAR_DENIED;
	
	/**
	 * ID: 1364<br>
	 * Message: The request for an alliance war has been rejected.
	 */
	public static final SystemMessageId REQUEST_ALLIANCE_WAR_REJECTED;
	
	/**
	 * ID: 1365<br>
	 * Message: $s2 of $s1 clan has surrendered as an individual.
	 */
	public static final SystemMessageId S2_OF_S1_SURRENDERED_AS_INDIVIDUAL;
	
	/**
	 * ID: 1366<br>
	 * Message: In order to delete a group, you must not [...]
	 */
	public static final SystemMessageId DELTE_GROUP_INSTRUCTION;
	
	/**
	 * ID: 1367<br>
	 * Message: Only members of the group are allowed to add records.
	 */
	public static final SystemMessageId ONLY_GROUP_CAN_ADD_RECORDS;
	
	/**
	 * ID: 1368<br>
	 * Message: You can not try those items on at the same time.
	 */
	public static final SystemMessageId YOU_CAN_NOT_TRY_THOSE_ITEMS_ON_AT_THE_SAME_TIME;
	
	/**
	 * ID: 1369<br>
	 * Message: You've exceeded the maximum.
	 */
	public static final SystemMessageId EXCEEDED_THE_MAXIMUM;
	
	/**
	 * ID: 1370<br>
	 * Message: Your message to $c1 did not reach its recipient. You cannot send mail to the GM staff.
	 */
	public static final SystemMessageId CANNOT_MAIL_GM_C1;
	
	/**
	 * ID: 1371<br>
	 * Message: It has been determined that you're not engaged in normal gameplay and a restriction has been imposed upon you. You may not move for $s1 minutes.
	 */
	public static final SystemMessageId GAMEPLAY_RESTRICTION_PENALTY_S1;
	
	/**
	 * ID: 1372<br>
	 * Message: Your punishment will continue for $s1 minutes.
	 */
	public static final SystemMessageId PUNISHMENT_CONTINUE_S1_MINUTES;
	
	/**
	 * ID: 1373<br>
	 * Message: $c1 has picked up $s2 that was dropped by a Raid Boss.
	 */
	public static final SystemMessageId C1_PICKED_UP_S2_FROM_RAIDBOSS;
	
	/**
	 * ID: 1374<br>
	 * Message: $c1 has picked up $s3 $s2(s) that was dropped by a Raid Boss.
	 */
	public static final SystemMessageId C1_PICKED_UP_S3_S2_S_FROM_RAIDBOSS;
	
	/**
	 * ID: 1375<br>
	 * Message: $c1 has picked up $s2 adena that was dropped by a Raid Boss.
	 */
	public static final SystemMessageId C1_PICKED_UP_S2_ADENA_FROM_RAIDBOSS;
	
	/**
	 * ID: 1376<br>
	 * Message: $c1 has picked up $s2 that was dropped by another character.
	 */
	public static final SystemMessageId C1_PICKED_UP_S2_FROM_ANOTHER_CHARACTER;
	
	/**
	 * ID: 1377<br>
	 * Message: $c1 has picked up $s3 $s2(s) that was dropped by a another character.
	 */
	public static final SystemMessageId C1_PICKED_UP_S3_S2_S_FROM_ANOTHER_CHARACTER;
	
	/**
	 * ID: 1378<br>
	 * Message: $c1 has picked up +$s3 $s2 that was dropped by a another character.
	 */
	public static final SystemMessageId C1_PICKED_UP_S3_S2_FROM_ANOTHER_CHARACTER;
	
	/**
	 * ID: 1379<br>
	 * Message: $c1 has obtained $s2 adena.
	 */
	public static final SystemMessageId C1_OBTAINED_S2_ADENA;
	
	/**
	 * ID: 1380<br>
	 * Message: You can't summon a $s1 while on the battleground.
	 */
	public static final SystemMessageId CANT_SUMMON_S1_ON_BATTLEGROUND;
	
	/**
	 * ID: 1381<br>
	 * Message: The party leader has obtained $s2 of $s1.
	 */
	public static final SystemMessageId LEADER_OBTAINED_S2_OF_S1;
	
	/**
	 * ID: 1382<br>
	 * Message: To fulfill the quest, you must bring the chosen weapon. Are you sure you want to choose this weapon?
	 */
	public static final SystemMessageId CHOOSE_WEAPON_CONFIRM;
	
	/**
	 * ID: 1383<br>
	 * Message: Are you sure you want to exchange?
	 */
	public static final SystemMessageId EXCHANGE_CONFIRM;
	
	/**
	 * ID: 1384<br>
	 * Message: $c1 has become the party leader.
	 */
	public static final SystemMessageId C1_HAS_BECOME_A_PARTY_LEADER;
	
	/**
	 * ID: 1385<br>
	 * Message: You are not allowed to dismount at this location.
	 */
	public static final SystemMessageId NO_DISMOUNT_HERE;
	
	/**
	 * ID: 1386<br>
	 * Message: You are no longer held in place.
	 */
	public static final SystemMessageId NO_LONGER_HELD_IN_PLACE;
	
	/**
	 * ID: 1387<br>
	 * Message: Please select the item you would like to try on.
	 */
	public static final SystemMessageId SELECT_ITEM_TO_TRY_ON;
	
	/**
	 * ID: 1388<br>
	 * Message: A party room has been created.
	 */
	public static final SystemMessageId PARTY_ROOM_CREATED;
	
	/**
	 * ID: 1389<br>
	 * Message: The party room's information has been revised.
	 */
	public static final SystemMessageId PARTY_ROOM_REVISED;
	
	/**
	 * ID: 1390<br>
	 * Message: You are not allowed to enter the party room.
	 */
	public static final SystemMessageId PARTY_ROOM_FORBIDDEN;
	
	/**
	 * ID: 1391<br>
	 * Message: You have exited from the party room.
	 */
	public static final SystemMessageId PARTY_ROOM_EXITED;
	
	/**
	 * ID: 1392<br>
	 * Message: $c1 has left the party room.
	 */
	public static final SystemMessageId C1_LEFT_PARTY_ROOM;
	
	/**
	 * ID: 1393<br>
	 * Message: You have been ousted from the party room.
	 */
	public static final SystemMessageId OUSTED_FROM_PARTY_ROOM;
	
	/**
	 * ID: 1394<br>
	 * Message: $c1 has been kicked from the party room.
	 */
	public static final SystemMessageId C1_KICKED_FROM_PARTY_ROOM;
	
	/**
	 * ID: 1395<br>
	 * Message: The party room has been disbanded.
	 */
	public static final SystemMessageId PARTY_ROOM_DISBANDED;
	
	/**
	 * ID: 1396<br>
	 * Message: The list of party rooms can only be viewed by a person who has not joined a party or who is currently the leader of a party.
	 */
	public static final SystemMessageId CANT_VIEW_PARTY_ROOMS;
	
	/**
	 * ID: 1397<br>
	 * Message: The leader of the party room has changed.
	 */
	public static final SystemMessageId PARTY_ROOM_LEADER_CHANGED;
	
	/**
	 * ID: 1398<br>
	 * Message: We are recruiting party members.
	 */
	public static final SystemMessageId RECRUITING_PARTY_MEMBERS;
	
	/**
	 * ID: 1399<br>
	 * Message: Only the leader of the party can transfer party leadership to another player.
	 */
	public static final SystemMessageId ONLY_A_PARTY_LEADER_CAN_TRANSFER_ONES_RIGHTS_TO_ANOTHER_PLAYER;
	
	/**
	 * ID: 1400<br>
	 * Message: Please select the person you wish to make the party leader.
	 */
	public static final SystemMessageId PLEASE_SELECT_THE_PERSON_TO_WHOM_YOU_WOULD_LIKE_TO_TRANSFER_THE_RIGHTS_OF_A_PARTY_LEADER;
	
	/**
	 * ID: 1401<br>
	 * Message: Slow down.you are already the party leader.
	 */
	public static final SystemMessageId YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF;
	
	/**
	 * ID: 1402<br>
	 * Message: You may only transfer party leadership to another member of the party.
	 */
	public static final SystemMessageId YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER;
	
	/**
	 * ID: 1403<br>
	 * Message: You have failed to transfer the party leadership.
	 */
	public static final SystemMessageId YOU_HAVE_FAILED_TO_TRANSFER_THE_PARTY_LEADER_RIGHTS;
	
	/**
	 * ID: 1404<br>
	 * Message: The owner of the private manufacturing store has changed the price for creating this item. Please check the new price before trying again.
	 */
	public static final SystemMessageId MANUFACTURE_PRICE_HAS_CHANGED;
	
	/**
	 * ID: 1405<br>
	 * Message: $s1 CPs have been restored.
	 */
	public static final SystemMessageId S1_CP_WILL_BE_RESTORED;
	
	/**
	 * ID: 1406<br>
	 * Message: $s2 CPs has been restored by $c1.
	 */
	public static final SystemMessageId S2_CP_WILL_BE_RESTORED_BY_C1;
	
	/**
	 * ID: 1407<br>
	 * Message: You are using a computer that does not allow you to log in with two accounts at the same time.
	 */
	public static final SystemMessageId NO_LOGIN_WITH_TWO_ACCOUNTS;
	
	/**
	 * ID: 1408<br>
	 * Message: Your prepaid remaining usage time is $s1 hours and $s2 minutes. You have $s3 paid reservations left.
	 */
	public static final SystemMessageId PREPAID_LEFT_S1_S2_S3;
	
	/**
	 * ID: 1409<br>
	 * Message: Your prepaid usage time has expired. Your new prepaid reservation will be used. The remaining usage time is $s1 hours and $s2 minutes.
	 */
	public static final SystemMessageId PREPAID_EXPIRED_S1_S2;
	
	/**
	 * ID: 1410<br>
	 * Message: Your prepaid usage time has expired. You do not have any more prepaid reservations left.
	 */
	public static final SystemMessageId PREPAID_EXPIRED;
	
	/**
	 * ID: 1411<br>
	 * Message: The number of your prepaid reservations has changed.
	 */
	public static final SystemMessageId PREPAID_CHANGED;
	
	/**
	 * ID: 1412<br>
	 * Message: Your prepaid usage time has $s1 minutes left.
	 */
	public static final SystemMessageId PREPAID_LEFT_S1;
	
	/**
	 * ID: 1413<br>
	 * Message: You do not meet the requirements to enter that party room.
	 */
	public static final SystemMessageId CANT_ENTER_PARTY_ROOM;
	
	/**
	 * ID: 1414<br>
	 * Message: The width and length should be 100 or more grids and less than 5000 grids respectively.
	 */
	public static final SystemMessageId WRONG_GRID_COUNT;
	
	/**
	 * ID: 1415<br>
	 * Message: The command file is not sent.
	 */
	public static final SystemMessageId COMMAND_FILE_NOT_SENT;
	
	/**
	 * ID: 1416<br>
	 * Message: The representative of Team 1 has not been selected.
	 */
	public static final SystemMessageId TEAM_1_NO_REPRESENTATIVE;
	
	/**
	 * ID: 1417<br>
	 * Message: The representative of Team 2 has not been selected.
	 */
	public static final SystemMessageId TEAM_2_NO_REPRESENTATIVE;
	
	/**
	 * ID: 1418<br>
	 * Message: The name of Team 1 has not yet been chosen.
	 */
	public static final SystemMessageId TEAM_1_NO_NAME;
	
	/**
	 * ID: 1419<br>
	 * Message: The name of Team 2 has not yet been chosen.
	 */
	public static final SystemMessageId TEAM_2_NO_NAME;
	
	/**
	 * ID: 1420<br>
	 * Message: The name of Team 1 and the name of Team 2 are identical.
	 */
	public static final SystemMessageId TEAM_NAME_IDENTICAL;
	
	/**
	 * ID: 1421<br>
	 * Message: The race setup file has not been designated.
	 */
	public static final SystemMessageId RACE_SETUP_FILE1;
	
	/**
	 * ID: 1422<br>
	 * Message: Race setup file error - BuffCnt is not specified
	 */
	public static final SystemMessageId RACE_SETUP_FILE2;
	
	/**
	 * ID: 1423<br>
	 * Message: Race setup file error - BuffID$s1 is not specified.
	 */
	public static final SystemMessageId RACE_SETUP_FILE3;
	
	/**
	 * ID: 1424<br>
	 * Message: Race setup file error - BuffLv$s1 is not specified.
	 */
	public static final SystemMessageId RACE_SETUP_FILE4;
	
	/**
	 * ID: 1425<br>
	 * Message: Race setup file error - DefaultAllow is not specified
	 */
	public static final SystemMessageId RACE_SETUP_FILE5;
	
	/**
	 * ID: 1426<br>
	 * Message: Race setup file error - ExpSkillCnt is not specified.
	 */
	public static final SystemMessageId RACE_SETUP_FILE6;
	
	/**
	 * ID: 1427<br>
	 * Message: Race setup file error - ExpSkillID$s1 is not specified.
	 */
	public static final SystemMessageId RACE_SETUP_FILE7;
	
	/**
	 * ID: 1428<br>
	 * Message: Race setup file error - ExpItemCnt is not specified.
	 */
	public static final SystemMessageId RACE_SETUP_FILE8;
	
	/**
	 * ID: 1429<br>
	 * Message: Race setup file error - ExpItemID$s1 is not specified.
	 */
	public static final SystemMessageId RACE_SETUP_FILE9;
	
	/**
	 * ID: 1430<br>
	 * Message: Race setup file error - TeleportDelay is not specified
	 */
	public static final SystemMessageId RACE_SETUP_FILE10;
	
	/**
	 * ID: 1431<br>
	 * Message: The race will be stopped temporarily.
	 */
	public static final SystemMessageId RACE_STOPPED_TEMPORARILY;
	
	/**
	 * ID: 1432<br>
	 * Message: Your opponent is currently in a petrified state.
	 */
	public static final SystemMessageId OPPONENT_PETRIFIED;
	
	/**
	 * ID: 1433<br>
	 * Message: You will now automatically apply $s1 to your target.
	 */
	public static final SystemMessageId USE_OF_S1_WILL_BE_AUTO;
	
	/**
	 * ID: 1434<br>
	 * Message: You will no longer automatically apply $s1 to your weapon.
	 */
	public static final SystemMessageId AUTO_USE_OF_S1_CANCELLED;
	
	/**
	 * ID: 1435<br>
	 * Message: Due to insufficient $s1, the automatic use function has been deactivated.
	 */
	public static final SystemMessageId AUTO_USE_CANCELLED_LACK_OF_S1;
	
	/**
	 * ID: 1436<br>
	 * Message: Due to insufficient $s1, the automatic use function cannot be activated.
	 */
	public static final SystemMessageId CANNOT_AUTO_USE_LACK_OF_S1;
	
	/**
	 * ID: 1437<br>
	 * Message: Players are no longer allowed to play dice. Dice can no longer be purchased from a village store. However, you can still sell them to any village store.
	 */
	public static final SystemMessageId DICE_NO_LONGER_ALLOWED;
	
	/**
	 * ID: 1438<br>
	 * Message: There is no skill that enables enchant.
	 */
	public static final SystemMessageId THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
	
	/**
	 * ID: 1439<br>
	 * Message: You do not have all of the items needed to enchant that skill.
	 */
	public static final SystemMessageId YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL;
	
	/**
	 * ID: 1440<br>
	 * Message: You have succeeded in enchanting the skill $s1.
	 */
	public static final SystemMessageId YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1;
	
	/**
	 * ID: 1441<br>
	 * Message: Skill enchant failed. The skill will be initialized.
	 */
	public static final SystemMessageId YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL_S1;
	
	/**
	 * ID: 1443<br>
	 * Message: You do not have enough SP to enchant that skill.
	 */
	public static final SystemMessageId YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL;
	
	/**
	 * ID: 1444<br>
	 * Message: You do not have enough experience (Exp) to enchant that skill.
	 */
	public static final SystemMessageId YOU_DONT_HAVE_ENOUGH_EXP_TO_ENCHANT_THAT_SKILL;
	
	/**
	 * ID: 1445<br>
	 * Message: Your previous subclass will be removed and replaced with the new subclass at level 40. Do you wish to continue?
	 */
	public static final SystemMessageId REPLACE_SUBCLASS_CONFIRM;
	
	/**
	 * ID: 1446<br>
	 * Message: The ferry from $s1 to $s2 has been delayed.
	 */
	public static final SystemMessageId FERRY_FROM_S1_TO_S2_DELAYED;
	
	/**
	 * ID: 1447<br>
	 * Message: You cannot do that while fishing.
	 */
	public static final SystemMessageId CANNOT_DO_WHILE_FISHING_1;
	
	/**
	 * ID: 1448<br>
	 * Message: Only fishing skills may be used at this time.
	 */
	public static final SystemMessageId ONLY_FISHING_SKILLS_NOW;
	
	/**
	 * ID: 1449<br>
	 * Message: You've got a bite!
	 */
	public static final SystemMessageId GOT_A_BITE;
	
	/**
	 * ID: 1450<br>
	 * Message: That fish is more determined than you are - it spit the hook!
	 */
	public static final SystemMessageId FISH_SPIT_THE_HOOK;
	
	/**
	 * ID: 1451<br>
	 * Message: Your bait was stolen by that fish!
	 */
	public static final SystemMessageId BAIT_STOLEN_BY_FISH;
	
	/**
	 * ID: 1452<br>
	 * Message: Baits have been lost because the fish got away.
	 */
	public static final SystemMessageId BAIT_LOST_FISH_GOT_AWAY;
	
	/**
	 * ID: 1453<br>
	 * Message: You do not have a fishing pole equipped.
	 */
	public static final SystemMessageId FISHING_POLE_NOT_EQUIPPED;
	
	/**
	 * ID: 1454<br>
	 * Message: You must put bait on your hook before you can fish.
	 */
	public static final SystemMessageId BAIT_ON_HOOK_BEFORE_FISHING;
	
	/**
	 * ID: 1455<br>
	 * Message: You cannot fish while under water.
	 */
	public static final SystemMessageId CANNOT_FISH_UNDER_WATER;
	
	/**
	 * ID: 1456<br>
	 * Message: You cannot fish while riding as a passenger of a boat - it's against the rules.
	 */
	public static final SystemMessageId CANNOT_FISH_ON_BOAT;
	
	/**
	 * ID: 1457<br>
	 * Message: You can't fish here.
	 */
	public static final SystemMessageId CANNOT_FISH_HERE;
	
	/**
	 * ID: 1458<br>
	 * Message: Your attempt at fishing has been cancelled.
	 */
	public static final SystemMessageId FISHING_ATTEMPT_CANCELLED;
	
	/**
	 * ID: 1459<br>
	 * Message: You do not have enough bait.
	 */
	public static final SystemMessageId NOT_ENOUGH_BAIT;
	
	/**
	 * ID: 1460<br>
	 * Message: You reel your line in and stop fishing.
	 */
	public static final SystemMessageId REEL_LINE_AND_STOP_FISHING;
	
	/**
	 * ID: 1461<br>
	 * Message: You cast your line and start to fish.
	 */
	public static final SystemMessageId CAST_LINE_AND_START_FISHING;
	
	/**
	 * ID: 1462<br>
	 * Message: You may only use the Pumping skill while you are fishing.
	 */
	public static final SystemMessageId CAN_USE_PUMPING_ONLY_WHILE_FISHING;
	
	/**
	 * ID: 1463<br>
	 * Message: You may only use the Reeling skill while you are fishing.
	 */
	public static final SystemMessageId CAN_USE_REELING_ONLY_WHILE_FISHING;
	
	/**
	 * ID: 1464<br>
	 * Message: The fish has resisted your attempt to bring it in.
	 */
	public static final SystemMessageId FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN;
	
	/**
	 * ID: 1465<br>
	 * Message: Your pumping is successful, causing $s1 damage.
	 */
	public static final SystemMessageId PUMPING_SUCCESFUL_S1_DAMAGE;
	
	/**
	 * ID: 1466<br>
	 * Message: You failed to do anything with the fish and it regains $s1 HP.
	 */
	public static final SystemMessageId FISH_RESISTED_PUMPING_S1_HP_REGAINED;
	
	/**
	 * ID: 1467<br>
	 * Message: You reel that fish in closer and cause $s1 damage.
	 */
	public static final SystemMessageId REELING_SUCCESFUL_S1_DAMAGE;
	
	/**
	 * ID: 1468<br>
	 * Message: You failed to reel that fish in further and it regains $s1 HP.
	 */
	public static final SystemMessageId FISH_RESISTED_REELING_S1_HP_REGAINED;
	
	/**
	 * ID: 1469<br>
	 * Message: You caught something!
	 */
	public static final SystemMessageId YOU_CAUGHT_SOMETHING;
	
	/**
	 * ID: 1470<br>
	 * Message: You cannot do that while fishing.
	 */
	public static final SystemMessageId CANNOT_DO_WHILE_FISHING_2;
	
	/**
	 * ID: 1471<br>
	 * Message: You cannot do that while fishing.
	 */
	public static final SystemMessageId CANNOT_DO_WHILE_FISHING_3;
	
	/**
	 * ID: 1472<br>
	 * Message: You look oddly at the fishing pole in disbelief and realize that you can't attack anything with this.
	 */
	public static final SystemMessageId CANNOT_ATTACK_WITH_FISHING_POLE;
	
	/**
	 * ID: 1473<br>
	 * Message: $s1 is not sufficient.
	 */
	public static final SystemMessageId S1_NOT_SUFFICIENT;
	
	/**
	 * ID: 1474<br>
	 * Message: $s1 is not available.
	 */
	public static final SystemMessageId S1_NOT_AVAILABLE;
	
	/**
	 * ID: 1475<br>
	 * Message: Pet has dropped $s1.
	 */
	public static final SystemMessageId PET_DROPPED_S1;
	
	/**
	 * ID: 1476<br>
	 * Message: Pet has dropped +$s1 $s2.
	 */
	public static final SystemMessageId PET_DROPPED_S1_S2;
	
	/**
	 * ID: 1477<br>
	 * Message: Pet has dropped $s2 of $s1.
	 */
	public static final SystemMessageId PET_DROPPED_S2_S1_S;
	
	/**
	 * ID: 1478<br>
	 * Message: You may only register a 64 x 64 pixel, 256-color BMP.
	 */
	public static final SystemMessageId ONLY_64_PIXEL_256_COLOR_BMP;
	
	/**
	 * ID: 1479<br>
	 * Message: That is the wrong grade of soulshot for that fishing pole.
	 */
	public static final SystemMessageId WRONG_FISHINGSHOT_GRADE;
	
	/**
	 * ID: 1480<br>
	 * Message: Are you sure you want to remove yourself from the Grand Olympiad Games waiting list?
	 */
	public static final SystemMessageId OLYMPIAD_REMOVE_CONFIRM;
	
	/**
	 * ID: 1481<br>
	 * Message: You have selected a class irrelevant individual match. Do you wish to participate?
	 */
	public static final SystemMessageId OLYMPIAD_NON_CLASS_CONFIRM;
	
	/**
	 * ID: 1482<br>
	 * Message: You've selected to join a class specific game. Continue?
	 */
	public static final SystemMessageId OLYMPIAD_CLASS_CONFIRM;
	
	/**
	 * ID: 1483<br>
	 * Message: Are you ready to be a Hero?
	 */
	public static final SystemMessageId HERO_CONFIRM;
	
	/**
	 * ID: 1484<br>
	 * Message: Are you sure this is the Hero weapon you wish to use? Kamael race cannot use this.
	 */
	public static final SystemMessageId HERO_WEAPON_CONFIRM;
	
	/**
	 * ID: 1485<br>
	 * Message: The ferry from Talking Island to Gludin Harbor has been delayed.
	 */
	public static final SystemMessageId FERRY_TALKING_GLUDIN_DELAYED;
	
	/**
	 * ID: 1486<br>
	 * Message: The ferry from Gludin Harbor to Talking Island has been delayed.
	 */
	public static final SystemMessageId FERRY_GLUDIN_TALKING_DELAYED;
	
	/**
	 * ID: 1487<br>
	 * Message: The ferry from Giran Harbor to Talking Island has been delayed.
	 */
	public static final SystemMessageId FERRY_GIRAN_TALKING_DELAYED;
	
	/**
	 * ID: 1488<br>
	 * Message: The ferry from Talking Island to Giran Harbor has been delayed.
	 */
	public static final SystemMessageId FERRY_TALKING_GIRAN_DELAYED;
	
	/**
	 * ID: 1489<br>
	 * Message: Innadril cruise service has been delayed.
	 */
	public static final SystemMessageId INNADRIL_BOAT_DELAYED;
	
	/**
	 * ID: 1490<br>
	 * Message: Traded $s2 of crop $s1.
	 */
	public static final SystemMessageId TRADED_S2_OF_CROP_S1;
	
	/**
	 * ID: 1491<br>
	 * Message: Failed in trading $s2 of crop $s1.
	 */
	public static final SystemMessageId FAILED_IN_TRADING_S2_OF_CROP_S1;
	
	/**
	 * ID: 1492<br>
	 * Message: You will be moved to the Olympiad Stadium in $s1 second(s).
	 */
	public static final SystemMessageId YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S;
	
	/**
	 * ID: 1493<br>
	 * Message: Your opponent made haste with their tail between their legs), the match has been cancelled.
	 */
	public static final SystemMessageId THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME;
	
	/**
	 * ID: 1494<br>
	 * Message: Your opponent does not meet the requirements to do battle), the match has been cancelled.
	 */
	public static final SystemMessageId THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME;
	
	/**
	 * ID: 1495<br>
	 * Message: The match will start in $s1 second(s).
	 */
	public static final SystemMessageId THE_GAME_WILL_START_IN_S1_SECOND_S;
	
	/**
	 * ID: 1496<br>
	 * Message: The match has started, fight!
	 */
	public static final SystemMessageId STARTS_THE_GAME;
	
	/**
	 * ID: 1497<br>
	 * Message: Congratulations, $c1! You win the match!
	 */
	public static final SystemMessageId C1_HAS_WON_THE_GAME;
	
	/**
	 * ID: 1498<br>
	 * Message: There is no victor, the match ends in a tie.
	 */
	public static final SystemMessageId THE_GAME_ENDED_IN_A_TIE;
	
	/**
	 * ID: 1499<br>
	 * Message: You will be moved back to town in $s1 second(s).
	 */
	public static final SystemMessageId YOU_WILL_BE_MOVED_TO_TOWN_IN_S1_SECONDS;
	
	/**
	 * ID: 1500<br>
	 * Message: $c1% does not meet the participation requirements. A sub-class character cannot participate in the Olympiad.
	 */
	public static final SystemMessageId C1_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_CLASS_CHARACTER;
	
	/**
	 * ID: 1501<br>
	 * Message: $c1% does not meet the participation requirements. Only Noblesse can participate in the Olympiad.
	 */
	public static final SystemMessageId C1_DOES_NOT_MEET_REQUIREMENTS_ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD;
	
	/**
	 * ID: 1502<br>
	 * Message: $c1 is already registered on the match waiting list.
	 */
	public static final SystemMessageId C1_IS_ALREADY_REGISTERED_ON_THE_MATCH_WAITING_LIST;
	
	/**
	 * ID: 1503<br>
	 * Message: You have been registered in the Grand Olympiad Games waiting list for a class specific match.
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES;
	
	/**
	 * ID: 1504<br>
	 * Message: You are currently registered for a 1v1 class irrelevant match.
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES;
	
	/**
	 * ID: 1505<br>
	 * Message: You have been removed from the Grand Olympiad Games waiting list.
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME;
	
	/**
	 * ID: 1506<br>
	 * Message: You are not currently registered on any Grand Olympiad Games waiting list.
	 */
	public static final SystemMessageId YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME;
	
	/**
	 * ID: 1507<br>
	 * Message: You cannot equip that item in a Grand Olympiad Games match.
	 */
	public static final SystemMessageId THIS_ITEM_CANT_BE_EQUIPPED_FOR_THE_OLYMPIAD_EVENT;
	
	/**
	 * ID: 1508<br>
	 * Message: You cannot use that item in a Grand Olympiad Games match.
	 */
	public static final SystemMessageId THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT;
	
	/**
	 * ID: 1509<br>
	 * Message: You cannot use that skill in a Grand Olympiad Games match.
	 */
	public static final SystemMessageId THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT;
	
	/**
	 * ID: 1510<br>
	 * Message: $c1 is making an attempt at resurrection with $s2 experience points. Do you want to be resurrected?
	 */
	public static final SystemMessageId RESSURECTION_REQUEST_BY_C1_FOR_S2_XP;
	
	/**
	 * ID: 1511<br>
	 * Message: While a pet is attempting to resurrect, it cannot help in resurrecting its master.
	 */
	public static final SystemMessageId MASTER_CANNOT_RES;
	
	/**
	 * ID: 1512<br>
	 * Message: You cannot resurrect a pet while their owner is being resurrected.
	 */
	public static final SystemMessageId CANNOT_RES_PET;
	
	/**
	 * ID: 1513<br>
	 * Message: Resurrection has already been proposed.
	 */
	public static final SystemMessageId RES_HAS_ALREADY_BEEN_PROPOSED;
	
	/**
	 * ID: 1514<br>
	 * Message: You cannot the owner of a pet while their pet is being resurrected
	 */
	public static final SystemMessageId CANNOT_RES_MASTER;
	
	/**
	 * ID: 1515<br>
	 * Message: A pet cannot be resurrected while it's owner is in the process of resurrecting.
	 */
	public static final SystemMessageId CANNOT_RES_PET2;
	
	/**
	 * ID: 1516<br>
	 * Message: The target is unavailable for seeding.
	 */
	public static final SystemMessageId THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING;
	
	/**
	 * ID: 1517<br>
	 * Message: Failed in Blessed Enchant. The enchant value of the item became 0.
	 */
	public static final SystemMessageId BLESSED_ENCHANT_FAILED;
	
	/**
	 * ID: 1518<br>
	 * Message: You do not meet the required condition to equip that item.
	 */
	public static final SystemMessageId CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION;
	
	/**
	 * ID: 1519<br>
	 * Message: The pet has been killed. If you don't resurrect it within 24 hours, the pet's body will disappear along with all the pet's items.
	 */
	public static final SystemMessageId MAKE_SURE_YOU_RESSURECT_YOUR_PET_WITHIN_24_HOURS;
	
	/**
	 * ID: 1520<br>
	 * Message: Servitor passed away.
	 */
	public static final SystemMessageId SERVITOR_PASSED_AWAY;
	
	/**
	 * ID: 1521<br>
	 * Message: Your servitor has vanished! You'll need to summon a new one.
	 */
	public static final SystemMessageId YOUR_SERVITOR_HAS_VANISHED;
	
	/**
	 * ID: 1522<br>
	 * Message: Your pet's corpse has decayed!
	 */
	public static final SystemMessageId YOUR_PETS_CORPSE_HAS_DECAYED;
	
	/**
	 * ID: 1523<br>
	 * Message: You should release your pet or servitor so that it does not fall off of the boat and drown!
	 */
	public static final SystemMessageId RELEASE_PET_ON_BOAT;
	
	/**
	 * ID: 1524<br>
	 * Message: $c1's pet gained $s2.
	 */
	public static final SystemMessageId C1_PET_GAINED_S2;
	
	/**
	 * ID: 1525<br>
	 * Message: $c1's pet gained $s3 of $s2.
	 */
	public static final SystemMessageId C1_PET_GAINED_S3_S2_S;
	
	/**
	 * ID: 1526<br>
	 * Message: $c1's pet gained +$s2$s3.
	 */
	public static final SystemMessageId C1_PET_GAINED_S2_S3;
	
	/**
	 * ID: 1527<br>
	 * Message: Your pet was hungry so it ate $s1.
	 */
	public static final SystemMessageId PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY;
	
	/**
	 * ID: 1528<br>
	 * Message: You've sent a petition to the GM staff.
	 */
	public static final SystemMessageId SENT_PETITION_TO_GM;
	
	/**
	 * ID: 1529<br>
	 * Message: $c1 is inviting you to the command channel. Do you want accept?
	 */
	public static final SystemMessageId COMMAND_CHANNEL_CONFIRM_FROM_C1;
	
	/**
	 * ID: 1530<br>
	 * Message: Select a target or enter the name.
	 */
	public static final SystemMessageId SELECT_TARGET_OR_ENTER_NAME;
	
	/**
	 * ID: 1531<br>
	 * Message: Enter the name of the clan that you wish to declare war on.
	 */
	public static final SystemMessageId ENTER_CLAN_NAME_TO_DECLARE_WAR2;
	
	/**
	 * ID: 1532<br>
	 * Message: Enter the name of the clan that you wish to have a cease-fire with.
	 */
	public static final SystemMessageId ENTER_CLAN_NAME_TO_CEASE_FIRE;
	
	/**
	 * ID: 1533<br>
	 * Message: Announcement: $c1 has picked up $s2.
	 */
	public static final SystemMessageId ANNOUNCEMENT_C1_PICKED_UP_S2;
	
	/**
	 * ID: 1534<br>
	 * Message: Announcement: $c1 has picked up +$s2$s3.
	 */
	public static final SystemMessageId ANNOUNCEMENT_C1_PICKED_UP_S2_S3;
	
	/**
	 * ID: 1535<br>
	 * Message: Announcement: $c1's pet has picked up $s2.
	 */
	public static final SystemMessageId ANNOUNCEMENT_C1_PET_PICKED_UP_S2;
	
	/**
	 * ID: 1536<br>
	 * Message: Announcement: $c1's pet has picked up +$s2$s3.
	 */
	public static final SystemMessageId ANNOUNCEMENT_C1_PET_PICKED_UP_S2_S3;
	
	/**
	 * ID: 1537<br>
	 * Message: Current Location: $s1, $s2, $s3 (near Rune Village)
	 */
	public static final SystemMessageId LOC_RUNE_S1_S2_S3;
	
	/**
	 * ID: 1538<br>
	 * Message: Current Location: $s1, $s2, $s3 (near the Town of Goddard)
	 */
	public static final SystemMessageId LOC_GODDARD_S1_S2_S3;
	
	/**
	 * ID: 1539<br>
	 * Message: Cargo has arrived at Talking Island Village.
	 */
	public static final SystemMessageId CARGO_AT_TALKING_VILLAGE;
	
	/**
	 * ID: 1540<br>
	 * Message: Cargo has arrived at the Dark Elf Village.
	 */
	public static final SystemMessageId CARGO_AT_DARKELF_VILLAGE;
	
	/**
	 * ID: 1541<br>
	 * Message: Cargo has arrived at Elven Village.
	 */
	public static final SystemMessageId CARGO_AT_ELVEN_VILLAGE;
	
	/**
	 * ID: 1542<br>
	 * Message: Cargo has arrived at Orc Village.
	 */
	public static final SystemMessageId CARGO_AT_ORC_VILLAGE;
	
	/**
	 * ID: 1543<br>
	 * Message: Cargo has arrived at Dwarfen Village.
	 */
	public static final SystemMessageId CARGO_AT_DWARVEN_VILLAGE;
	
	/**
	 * ID: 1544<br>
	 * Message: Cargo has arrived at Aden Castle Town.
	 */
	public static final SystemMessageId CARGO_AT_ADEN;
	
	/**
	 * ID: 1545<br>
	 * Message: Cargo has arrived at Town of Oren.
	 */
	public static final SystemMessageId CARGO_AT_OREN;
	
	/**
	 * ID: 1546<br>
	 * Message: Cargo has arrived at Hunters Village.
	 */
	public static final SystemMessageId CARGO_AT_HUNTERS;
	
	/**
	 * ID: 1547<br>
	 * Message: Cargo has arrived at the Town of Dion.
	 */
	public static final SystemMessageId CARGO_AT_DION;
	
	/**
	 * ID: 1548<br>
	 * Message: Cargo has arrived at Floran Village.
	 */
	public static final SystemMessageId CARGO_AT_FLORAN;
	
	/**
	 * ID: 1549<br>
	 * Message: Cargo has arrived at Gludin Village.
	 */
	public static final SystemMessageId CARGO_AT_GLUDIN;
	
	/**
	 * ID: 1550<br>
	 * Message: Cargo has arrived at the Town of Gludio.
	 */
	public static final SystemMessageId CARGO_AT_GLUDIO;
	
	/**
	 * ID: 1551<br>
	 * Message: Cargo has arrived at Giran Castle Town.
	 */
	public static final SystemMessageId CARGO_AT_GIRAN;
	
	/**
	 * ID: 1552<br>
	 * Message: Cargo has arrived at Heine.
	 */
	public static final SystemMessageId CARGO_AT_HEINE;
	
	/**
	 * ID: 1553<br>
	 * Message: Cargo has arrived at Rune Village.
	 */
	public static final SystemMessageId CARGO_AT_RUNE;
	
	/**
	 * ID: 1554<br>
	 * Message: Cargo has arrived at the Town of Goddard.
	 */
	public static final SystemMessageId CARGO_AT_GODDARD;
	
	/**
	 * ID: 1555<br>
	 * Message: Do you want to cancel character deletion?
	 */
	public static final SystemMessageId CANCEL_CHARACTER_DELETION_CONFIRM;
	
	/**
	 * ID: 1556<br>
	 * Message: Your clan notice has been saved.
	 */
	public static final SystemMessageId CLAN_NOTICE_SAVED;
	
	/**
	 * ID: 1557<br>
	 * Message: Seed price should be more than $s1 and less than $s2.
	 */
	public static final SystemMessageId SEED_PRICE_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2;
	
	/**
	 * ID: 1558<br>
	 * Message: The quantity of seed should be more than $s1 and less than $s2.
	 */
	public static final SystemMessageId THE_QUANTITY_OF_SEED_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2;
	
	/**
	 * ID: 1559<br>
	 * Message: Crop price should be more than $s1 and less than $s2.
	 */
	public static final SystemMessageId CROP_PRICE_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2;
	
	/**
	 * ID: 1560<br>
	 * Message: The quantity of crop should be more than $s1 and less than $s2
	 */
	public static final SystemMessageId THE_QUANTITY_OF_CROP_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2;
	
	/**
	 * ID: 1561<br>
	 * Message: The clan, $s1, has declared a Clan War.
	 */
	public static final SystemMessageId CLAN_S1_DECLARED_WAR;
	
	/**
	 * ID: 1562<br>
	 * Message: A Clan War has been declared against the clan, $s1. you will only lose a quarter of the normal experience from death.
	 */
	public static final SystemMessageId CLAN_WAR_DECLARED_AGAINST_S1_IF_KILLED_LOSE_LOW_EXP;
	
	/**
	 * ID: 1563<br>
	 * or they do not have enough members.
	 */
	public static final SystemMessageId CANNOT_DECLARE_WAR_TOO_LOW_LEVEL_OR_NOT_ENOUGH_MEMBERS;
	
	/**
	 * ID: 1564<br>
	 * Message: A Clan War can be declared only if the clan is level three or above, and the number of clan members is fifteen or greater.
	 */
	public static final SystemMessageId CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER;
	
	/**
	 * ID: 1565<br>
	 * Message: A Clan War cannot be declared against a clan that does not exist!
	 */
	public static final SystemMessageId CLAN_WAR_CANNOT_DECLARED_CLAN_NOT_EXIST;
	
	/**
	 * ID: 1566<br>
	 * Message: The clan, $s1, has decided to stop the war.
	 */
	public static final SystemMessageId CLAN_S1_HAS_DECIDED_TO_STOP;
	
	/**
	 * ID: 1567<br>
	 * Message: The war against $s1 Clan has been stopped.
	 */
	public static final SystemMessageId WAR_AGAINST_S1_HAS_STOPPED;
	
	/**
	 * ID: 1568<br>
	 * Message: The target for declaration is wrong.
	 */
	public static final SystemMessageId WRONG_DECLARATION_TARGET;
	
	/**
	 * ID: 1569<br>
	 * Message: A declaration of Clan War against an allied clan can't be made.
	 */
	public static final SystemMessageId CLAN_WAR_AGAINST_A_ALLIED_CLAN_NOT_WORK;
	
	/**
	 * ID: 1570<br>
	 * Message: A declaration of war against more than 30 Clans can't be made at the same time
	 */
	public static final SystemMessageId TOO_MANY_CLAN_WARS;
	
	/**
	 * ID: 1571<br>
	 * Message: ======<Clans You've Declared War On>======
	 */
	public static final SystemMessageId CLANS_YOU_DECLARED_WAR_ON;
	
	/**
	 * ID: 1572<br>
	 * Message: ======<Clans That Have Declared War On You>======
	 */
	public static final SystemMessageId CLANS_THAT_HAVE_DECLARED_WAR_ON_YOU;
	
	/**
	 * ID: 1573<br>
	 * Message: All is well. There are no clans that have declared war against your clan.
	 */
	public static final SystemMessageId NO_WARS_AGAINST_YOU;
	
	/**
	 * ID: 1574<br>
	 * Message: Command Channels can only be formed by a party leader who is also the leader of a level 5 clan.
	 */
	public static final SystemMessageId COMMAND_CHANNEL_ONLY_BY_LEVEL_5_CLAN_LEADER_PARTY_LEADER;
	
	/**
	 * ID: 1575<br>
	 * Message: Your pet uses spiritshot.
	 */
	public static final SystemMessageId PET_USE_SPIRITSHOT;
	
	/**
	 * ID: 1576<br>
	 * Message: Your servitor uses spiritshot.
	 */
	public static final SystemMessageId SERVITOR_USE_SPIRITSHOT;
	
	/**
	 * ID: 1577<br>
	 * Message: Servitor uses the power of spirit.
	 */
	public static final SystemMessageId SERVITOR_USE_THE_POWER_OF_SPIRIT;
	
	/**
	 * ID: 1578<br>
	 * Message: Items are not available for a private store or a private manufacture.
	 */
	public static final SystemMessageId ITEMS_UNAVAILABLE_FOR_STORE_MANUFACTURE;
	
	/**
	 * ID: 1579<br>
	 * Message: $c1's pet gained $s2 adena.
	 */
	public static final SystemMessageId C1_PET_GAINED_S2_ADENA;
	
	/**
	 * ID: 1580<br>
	 * Message: The Command Channel has been formed.
	 */
	public static final SystemMessageId COMMAND_CHANNEL_FORMED;
	
	/**
	 * ID: 1581<br>
	 * Message: The Command Channel has been disbanded.
	 */
	public static final SystemMessageId COMMAND_CHANNEL_DISBANDED;
	
	/**
	 * ID: 1582<br>
	 * Message: You have joined the Command Channel.
	 */
	public static final SystemMessageId JOINED_COMMAND_CHANNEL;
	
	/**
	 * ID: 1583<br>
	 * Message: You were dismissed from the Command Channel.
	 */
	public static final SystemMessageId DISMISSED_FROM_COMMAND_CHANNEL;
	
	/**
	 * ID: 1584<br>
	 * Message: $c1's party has been dismissed from the Command Channel.
	 */
	public static final SystemMessageId C1_PARTY_DISMISSED_FROM_COMMAND_CHANNEL;
	
	/**
	 * ID: 1585<br>
	 * Message: The Command Channel has been disbanded.
	 */
	public static final SystemMessageId COMMAND_CHANNEL_DISBANDED2;
	
	/**
	 * ID: 1586<br>
	 * Message: You have quit the Command Channel.
	 */
	public static final SystemMessageId LEFT_COMMAND_CHANNEL;
	
	/**
	 * ID: 1587<br>
	 * Message: $c1's party has left the Command Channel.
	 */
	public static final SystemMessageId C1_PARTY_LEFT_COMMAND_CHANNEL;
	
	/**
	 * ID: 1588<br>
	 * Message: The Command Channel is activated only when there are at least 5 parties participating.
	 */
	public static final SystemMessageId COMMAND_CHANNEL_ONLY_AT_LEAST_5_PARTIES;
	
	/**
	 * ID: 1589<br>
	 * Message: Command Channel authority has been transferred to $c1.
	 */
	public static final SystemMessageId COMMAND_CHANNEL_LEADER_NOW_C1;
	
	/**
	 * ID: 1590<br>
	 * Message: ===<Guild Info (Total Parties: $s1)>===
	 */
	public static final SystemMessageId GUILD_INFO_HEADER;
	
	/**
	 * ID: 1591<br>
	 * Message: No user has been invited to the Command Channel.
	 */
	public static final SystemMessageId NO_USER_INVITED_TO_COMMAND_CHANNEL;
	
	/**
	 * ID: 1592<br>
	 * Message: You can no longer set up a Command Channel.
	 */
	public static final SystemMessageId CANNOT_LONGER_SETUP_COMMAND_CHANNEL;
	
	/**
	 * ID: 1593<br>
	 * Message: You do not have authority to invite someone to the Command Channel.
	 */
	public static final SystemMessageId CANNOT_INVITE_TO_COMMAND_CHANNEL;
	
	/**
	 * ID: 1594<br>
	 * Message: $c1's party is already a member of the Command Channel.
	 */
	public static final SystemMessageId C1_ALREADY_MEMBER_OF_COMMAND_CHANNEL;
	
	/**
	 * ID: 1595<br>
	 * Message: $s1 has succeeded.
	 */
	public static final SystemMessageId S1_SUCCEEDED;
	
	/**
	 * ID: 1596<br>
	 * Message: You were hit by $s1!
	 */
	public static final SystemMessageId HIT_BY_S1;
	
	/**
	 * ID: 1597<br>
	 * Message: $s1 has failed.
	 */
	public static final SystemMessageId S1_FAILED;
	
	/**
	 * ID: 1598<br>
	 * Message: Soulshots and spiritshots are not available for a dead pet or servitor. Sad, isn't it?
	 */
	public static final SystemMessageId SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET;
	
	/**
	 * ID: 1599<br>
	 * Message: You cannot observe while you are in combat!
	 */
	public static final SystemMessageId CANNOT_OBSERVE_IN_COMBAT;
	
	/**
	 * ID: 1600<br>
	 * Message: Tomorrow's items will ALL be set to 0. Do you wish to continue?
	 */
	public static final SystemMessageId TOMORROW_ITEM_ZERO_CONFIRM;
	
	/**
	 * ID: 1601<br>
	 * Message: Tomorrow's items will all be set to the same value as today's items. Do you wish to continue?
	 */
	public static final SystemMessageId TOMORROW_ITEM_SAME_CONFIRM;
	
	/**
	 * ID: 1602<br>
	 * Message: Only a party leader can access the Command Channel.
	 */
	public static final SystemMessageId COMMAND_CHANNEL_ONLY_FOR_PARTY_LEADER;
	
	/**
	 * ID: 1603<br>
	 * Message: Only channel operator can give All Command.
	 */
	public static final SystemMessageId ONLY_COMMANDER_GIVE_COMMAND;
	
	/**
	 * ID: 1604<br>
	 * Message: While dressed in formal wear, you can't use items that require all skills and casting operations.
	 */
	public static final SystemMessageId CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR;
	
	/**
	 * ID: 1605<br>
	 * Message: * Here, you can buy only seeds of $s1 Manor.
	 */
	public static final SystemMessageId HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR;
	
	/**
	 * ID: 1606<br>
	 * Message: Congratulations - You've completed the third-class transfer quest!
	 */
	public static final SystemMessageId THIRD_CLASS_TRANSFER;
	
	/**
	 * ID: 1607<br>
	 * Message: $s1 adena has been withdrawn to pay for purchasing fees.
	 */
	public static final SystemMessageId S1_ADENA_HAS_BEEN_WITHDRAWN_TO_PAY_FOR_PURCHASING_FEES;
	
	/**
	 * ID: 1608<br>
	 * Message: Due to insufficient adena you cannot buy another castle.
	 */
	public static final SystemMessageId INSUFFICIENT_ADENA_TO_BUY_CASTLE;
	
	/**
	 * ID: 1609<br>
	 * Message: War has already been declared against that clan... but I'll make note that you really don't like them.
	 */
	public static final SystemMessageId WAR_ALREADY_DECLARED;
	
	/**
	 * ID: 1610<br>
	 * Message: Fool! You cannot declare war against your own clan!
	 */
	public static final SystemMessageId CANNOT_DECLARE_AGAINST_OWN_CLAN;
	
	/**
	 * ID: 1611<br>
	 * Message: Leader: $c1
	 */
	public static final SystemMessageId PARTY_LEADER_C1;
	
	/**
	 * ID: 1612<br>
	 * Message: =====<War List>=====
	 */
	public static final SystemMessageId WAR_LIST;
	
	/**
	 * ID: 1613<br>
	 * Message: There is no clan listed on War List.
	 */
	public static final SystemMessageId NO_CLAN_ON_WAR_LIST;
	
	/**
	 * ID: 1614<br>
	 * Message: You have joined a channel that was already open.
	 */
	public static final SystemMessageId JOINED_CHANNEL_ALREADY_OPEN;
	
	/**
	 * ID: 1615<br>
	 * Message: The number of remaining parties is $s1 until a channel is activated
	 */
	public static final SystemMessageId S1_PARTIES_REMAINING_UNTIL_CHANNEL;
	
	/**
	 * ID: 1616<br>
	 * Message: The Command Channel has been activated.
	 */
	public static final SystemMessageId COMMAND_CHANNEL_ACTIVATED;
	
	/**
	 * ID: 1617<br>
	 * Message: You do not have the authority to use the Command Channel.
	 */
	public static final SystemMessageId CANT_USE_COMMAND_CHANNEL;
	
	/**
	 * ID: 1618<br>
	 * Message: The ferry from Rune Harbor to Gludin Harbor has been delayed.
	 */
	public static final SystemMessageId FERRY_RUNE_GLUDIN_DELAYED;
	
	/**
	 * ID: 1619<br>
	 * Message: The ferry from Gludin Harbor to Rune Harbor has been delayed.
	 */
	public static final SystemMessageId FERRY_GLUDIN_RUNE_DELAYED;
	
	/**
	 * ID: 1620<br>
	 * Message: Arrived at Rune Harbor.
	 */
	public static final SystemMessageId ARRIVED_AT_RUNE;
	
	/**
	 * ID: 1621<br>
	 * Message: Departure for Gludin Harbor will take place in five minutes!
	 */
	public static final SystemMessageId DEPARTURE_FOR_GLUDIN_5_MINUTES;
	
	/**
	 * ID: 1622<br>
	 * Message: Departure for Gludin Harbor will take place in one minute!
	 */
	public static final SystemMessageId DEPARTURE_FOR_GLUDIN_1_MINUTE;
	
	/**
	 * ID: 1623<br>
	 * Message: Make haste! We will be departing for Gludin Harbor shortly...
	 */
	public static final SystemMessageId DEPARTURE_FOR_GLUDIN_SHORTLY;
	
	/**
	 * ID: 1624<br>
	 * Message: We are now departing for Gludin Harbor Hold on and enjoy the ride!
	 */
	public static final SystemMessageId DEPARTURE_FOR_GLUDIN_NOW;
	
	/**
	 * ID: 1625<br>
	 * Message: Departure for Rune Harbor will take place after anchoring for ten minutes.
	 */
	public static final SystemMessageId DEPARTURE_FOR_RUNE_10_MINUTES;
	
	/**
	 * ID: 1626<br>
	 * Message: Departure for Rune Harbor will take place in five minutes!
	 */
	public static final SystemMessageId DEPARTURE_FOR_RUNE_5_MINUTES;
	
	/**
	 * ID: 1627<br>
	 * Message: Departure for Rune Harbor will take place in one minute!
	 */
	public static final SystemMessageId DEPARTURE_FOR_RUNE_1_MINUTE;
	
	/**
	 * ID: 1628<br>
	 * Message: Make haste! We will be departing for Gludin Harbor shortly...
	 */
	public static final SystemMessageId DEPARTURE_FOR_GLUDIN_SHORTLY2;
	
	/**
	 * ID: 1629<br>
	 * Message: We are now departing for Rune Harbor Hold on and enjoy the ride!
	 */
	public static final SystemMessageId DEPARTURE_FOR_RUNE_NOW;
	
	/**
	 * ID: 1630<br>
	 * Message: The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 15 minutes.
	 */
	public static final SystemMessageId FERRY_FROM_RUNE_AT_GLUDIN_15_MINUTES;
	
	/**
	 * ID: 1631<br>
	 * Message: The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 10 minutes.
	 */
	public static final SystemMessageId FERRY_FROM_RUNE_AT_GLUDIN_10_MINUTES;
	
	/**
	 * ID: 1632<br>
	 * Message: The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 10 minutes.
	 */
	public static final SystemMessageId FERRY_FROM_RUNE_AT_GLUDIN_5_MINUTES;
	
	/**
	 * ID: 1633<br>
	 * Message: The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 1 minute.
	 */
	public static final SystemMessageId FERRY_FROM_RUNE_AT_GLUDIN_1_MINUTE;
	
	/**
	 * ID: 1634<br>
	 * Message: The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 15 minutes.
	 */
	public static final SystemMessageId FERRY_FROM_GLUDIN_AT_RUNE_15_MINUTES;
	
	/**
	 * ID: 1635<br>
	 * Message: The ferry from Gludin Harbor will be arriving at Rune harbor in approximately 10 minutes.
	 */
	public static final SystemMessageId FERRY_FROM_GLUDIN_AT_RUNE_10_MINUTES;
	
	/**
	 * ID: 1636<br>
	 * Message: The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 10 minutes.
	 */
	public static final SystemMessageId FERRY_FROM_GLUDIN_AT_RUNE_5_MINUTES;
	
	/**
	 * ID: 1637<br>
	 * Message: The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 1 minute.
	 */
	public static final SystemMessageId FERRY_FROM_GLUDIN_AT_RUNE_1_MINUTE;
	
	/**
	 * ID: 1638<br>
	 * Message: You cannot fish while using a recipe book, private manufacture or private store.
	 */
	public static final SystemMessageId CANNOT_FISH_WHILE_USING_RECIPE_BOOK;
	
	/**
	 * ID: 1639<br>
	 * Message: Period $s1 of the Grand Olympiad Games has started!
	 */
	public static final SystemMessageId OLYMPIAD_PERIOD_S1_HAS_STARTED;
	
	/**
	 * ID: 1640<br>
	 * Message: Period $s1 of the Grand Olympiad Games has now ended.
	 */
	public static final SystemMessageId OLYMPIAD_PERIOD_S1_HAS_ENDED;
	
	/**
	 * ID: 1641<br>
	 * Message: Sharpen your swords, tighten the stitching in your armor, and make haste to a Grand Olympiad Manager! Battles in the Grand Olympiad Games are now taking place!
	 */
	public static final SystemMessageId THE_OLYMPIAD_GAME_HAS_STARTED;
	
	/**
	 * ID: 1642<br>
	 * Message: Much carnage has been left for the cleanup crew of the Olympiad Stadium. Battles in the Grand Olympiad Games are now over!
	 */
	public static final SystemMessageId THE_OLYMPIAD_GAME_HAS_ENDED;
	
	/**
	 * ID: 1643<br>
	 * Message: Current Location: $s1, $s2, $s3 (Dimensional Gap)
	 */
	public static final SystemMessageId LOC_DIMENSIONAL_GAP_S1_S2_S3;
	
	// 1644 - 1648: none
	
	/**
	 * ID: 1649<br>
	 * Message: Play time is now accumulating.
	 */
	public static final SystemMessageId PLAY_TIME_NOW_ACCUMULATING;
	
	/**
	 * ID: 1650<br>
	 * Message: Due to high server traffic, your login attempt has failed. Please try again soon.
	 */
	public static final SystemMessageId TRY_LOGIN_LATER;
	
	/**
	 * ID: 1651<br>
	 * Message: The Grand Olympiad Games are not currently in progress.
	 */
	public static final SystemMessageId THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS;
	
	/**
	 * ID: 1652<br>
	 * Message: You are now recording gameplay.
	 */
	public static final SystemMessageId RECORDING_GAMEPLAY_START;
	
	/**
	 * ID: 1653<br>
	 * Message: Your recording has been successfully stored. ($s1)
	 */
	public static final SystemMessageId RECORDING_GAMEPLAY_STOP_S1;
	
	/**
	 * ID: 1654<br>
	 * Message: Your attempt to record the replay file has failed.
	 */
	public static final SystemMessageId RECORDING_GAMEPLAY_FAILED;
	
	/**
	 * ID: 1655<br>
	 * Message: You caught something smelly and scary, maybe you should throw it back!?
	 */
	public static final SystemMessageId YOU_CAUGHT_SOMETHING_SMELLY_THROW_IT_BACK;
	
	/**
	 * ID: 1656<br>
	 * Message: You have successfully traded the item with the NPC.
	 */
	public static final SystemMessageId SUCCESSFULLY_TRADED_WITH_NPC;
	
	/**
	 * ID: 1657<br>
	 * Message: $c1 has earned $s2 points in the Grand Olympiad Games.
	 */
	public static final SystemMessageId C1_HAS_GAINED_S2_OLYMPIAD_POINTS;
	
	/**
	 * ID: 1658<br>
	 * Message: $c1 has lost $s2 points in the Grand Olympiad Games.
	 */
	public static final SystemMessageId C1_HAS_LOST_S2_OLYMPIAD_POINTS;
	
	/**
	 * ID: 1659<br>
	 * Message: Current Location: $s1, $s2, $s3 (Cemetery of the Empire)
	 */
	public static final SystemMessageId LOC_CEMETARY_OF_THE_EMPIRE_S1_S2_S3;
	
	/**
	 * ID: 1660<br>
	 * Message: Channel Creator: $c1.
	 */
	public static final SystemMessageId CHANNEL_CREATOR_C1;
	
	/**
	 * ID: 1661<br>
	 * Message: $c1 has obtained $s3 $s2s.
	 */
	public static final SystemMessageId C1_OBTAINED_S3_S2_S;
	
	/**
	 * ID: 1662<br>
	 * Message: The fish are no longer biting here because you've caught too many! Try fishing in another location.
	 */
	public static final SystemMessageId FISH_NO_MORE_BITING_TRY_OTHER_LOCATION;
	
	/**
	 * ID: 1663<br>
	 * Message: The clan crest was successfully registered. Remember, only a clan that owns a clan hall or castle can have their crest displayed.
	 */
	public static final SystemMessageId CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED;
	
	/**
	 * ID: 1664<br>
	 * Message: The fish is resisting your efforts to haul it in! Look at that bobber go!
	 */
	public static final SystemMessageId FISH_RESISTING_LOOK_BOBBLER;
	
	/**
	 * ID: 1665<br>
	 * Message: You've worn that fish out! It can't even pull the bobber under the water!
	 */
	public static final SystemMessageId YOU_WORN_FISH_OUT;
	
	/**
	 * ID: 1666<br>
	 * Message: You have obtained +$s1 $s2.
	 */
	public static final SystemMessageId OBTAINED_S1_S2;
	
	/**
	 * ID: 1667<br>
	 * Message: Lethal Strike!
	 */
	public static final SystemMessageId LETHAL_STRIKE;
	
	/**
	 * ID: 1668<br>
	 * Message: Your lethal strike was successful!
	 */
	public static final SystemMessageId LETHAL_STRIKE_SUCCESSFUL;
	
	/**
	 * ID: 1669<br>
	 * Message: There was nothing found inside of that.
	 */
	public static final SystemMessageId NOTHING_INSIDE_THAT;
	
	/**
	 * ID: 1670<br>
	 * Message: Due to your Reeling and/or Pumping skill being three or more levels higher than your Fishing skill, a 50 damage penalty will be applied.
	 */
	public static final SystemMessageId REELING_PUMPING_3_LEVELS_HIGHER_THAN_FISHING_PENALTY;
	
	/**
	 * ID: 1671<br>
	 * Message: Your reeling was successful! (Mastery Penalty:$s1 )
	 */
	public static final SystemMessageId REELING_SUCCESSFUL_PENALTY_S1;
	
	/**
	 * ID: 1672<br>
	 * Message: Your pumping was successful! (Mastery Penalty:$s1 )
	 */
	public static final SystemMessageId PUMPING_SUCCESSFUL_PENALTY_S1;
	
	/**
	 * ID: 1673<br>
	 * Message: Your current record for this Grand Olympiad is $s1 match(es), $s2 win(s) and $s3 defeat(s). You have earned $s4 Olympiad Point(s).
	 */
	public static final SystemMessageId THE_CURRENT_RECORD_FOR_THIS_OLYMPIAD_SESSION_IS_S1_MATCHES_S2_WINS_S3_DEFEATS_YOU_HAVE_EARNED_S4_OLYMPIAD_POINTS;
	
	/**
	 * ID: 1674<br>
	 * Message: This command can only be used by a Noblesse.
	 */
	public static final SystemMessageId NOBLESSE_ONLY;
	
	/**
	 * ID: 1675<br>
	 * Message: A manor cannot be set up between 6 a.m. and 8 p.m.
	 */
	public static final SystemMessageId A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM;
	
	/**
	 * ID: 1676<br>
	 * Message: You do not have a servitor or pet and therefore cannot use the automatic-use function.
	 */
	public static final SystemMessageId NO_SERVITOR_CANNOT_AUTOMATE_USE;
	
	/**
	 * ID: 1677<br>
	 * Message: A cease-fire during a Clan War can not be called while members of your clan are engaged in battle.
	 */
	public static final SystemMessageId CANT_STOP_CLAN_WAR_WHILE_IN_COMBAT;
	
	/**
	 * ID: 1678<br>
	 * Message: You have not declared a Clan War against the clan $s1.
	 */
	public static final SystemMessageId NO_CLAN_WAR_AGAINST_CLAN_S1;
	
	/**
	 * ID: 1679<br>
	 * Message: Only the creator of a channel can issue a global command.
	 */
	public static final SystemMessageId ONLY_CHANNEL_CREATOR_CAN_GLOBAL_COMMAND;
	
	/**
	 * ID: 1680<br>
	 * Message: $c1 has declined the channel invitation.
	 */
	public static final SystemMessageId C1_DECLINED_CHANNEL_INVITATION;
	
	/**
	 * ID: 1681<br>
	 * Message: Since $c1 did not respond, your channel invitation has failed.
	 */
	public static final SystemMessageId C1_DID_NOT_RESPOND_CHANNEL_INVITATION_FAILED;
	
	/**
	 * ID: 1682<br>
	 * Message: Only the creator of a channel can use the channel dismiss command.
	 */
	public static final SystemMessageId ONLY_CHANNEL_CREATOR_CAN_DISMISS;
	
	/**
	 * ID: 1683<br>
	 * Message: Only a party leader can leave a command channel.
	 */
	public static final SystemMessageId ONLY_PARTY_LEADER_CAN_LEAVE_CHANNEL;
	
	/**
	 * ID: 1684<br>
	 * Message: A Clan War can not be declared against a clan that is being dissolved.
	 */
	public static final SystemMessageId NO_CLAN_WAR_AGAINST_DISSOLVING_CLAN;
	
	/**
	 * ID: 1685<br>
	 * Message: You are unable to equip this item when your PK count is greater or equal to one.
	 */
	public static final SystemMessageId YOU_ARE_UNABLE_TO_EQUIP_THIS_ITEM_WHEN_YOUR_PK_COUNT_IS_GREATER_THAN_OR_EQUAL_TO_ONE;
	
	/**
	 * ID: 1686<br>
	 * Message: Stones and mortar tumble to the earth - the castle wall has taken damage!
	 */
	public static final SystemMessageId CASTLE_WALL_DAMAGED;
	
	/**
	 * ID: 1687<br>
	 * Message: This area cannot be entered while mounted atop of a Wyvern. You will be dismounted from your Wyvern if you do not leave!
	 */
	public static final SystemMessageId AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN;
	
	/**
	 * ID: 1688<br>
	 * Message: You cannot enchant while operating a Private Store or Private Workshop.
	 */
	public static final SystemMessageId CANNOT_ENCHANT_WHILE_STORE;
	
	/**
	 * ID: 1689<br>
	 * Message: $c1 is already registered on the class match waiting list.
	 */
	public static final SystemMessageId C1_IS_ALREADY_REGISTERED_ON_THE_CLASS_MATCH_WAITING_LIST;
	
	/**
	 * ID: 1690<br>
	 * Message: $c1 is already registered on the waiting list for the non-class-limited individual match event.
	 */
	public static final SystemMessageId C1_IS_ALREADY_REGISTERED_ON_THE_NON_CLASS_LIMITED_MATCH_WAITING_LIST;
	
	/**
	 * ID: 1691<br>
	 * Message: $c1% does not meet the participation requirements. You cannot participate in the Olympiad because your inventory slot exceeds 80%.
	 */
	public static final SystemMessageId C1_CANNOT_PARTICIPATE_IN_OLYMPIAD_INVENTORY_SLOT_EXCEEDS_80_PERCENT;
	
	/**
	 * ID: 1692<br>
	 * Message: $c1% does not meet the participation requirements. You cannot participate in the Olympiad because you have changed to your sub-class.
	 */
	public static final SystemMessageId C1_CANNOT_PARTICIPATE_IN_OLYMPIAD_WHILE_CHANGED_TO_SUB_CLASS;
	
	/**
	 * ID: 1693<br>
	 * Message: You may not observe a Grand Olympiad Games match while you are on the waiting list.
	 */
	public static final SystemMessageId WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME;
	
	/**
	 * ID: 1694<br>
	 * Message: Only a clan leader that is a Noblesse can view the Siege War Status window during a siege war.
	 */
	public static final SystemMessageId ONLY_NOBLESSE_LEADER_CAN_VIEW_SIEGE_STATUS_WINDOW;
	
	/**
	 * ID: 1695<br>
	 * Message: You can only use that during a Siege War!
	 */
	public static final SystemMessageId ONLY_DURING_SIEGE;
	
	/**
	 * ID: 1696<br>
	 * Message: Your accumulated play time is $s1.
	 */
	public static final SystemMessageId ACCUMULATED_PLAY_TIME_IS_S1;
	
	/**
	 * ID: 1697<br>
	 * Message: Your accumulated play time has reached Fatigue level, so you will receive experience or item drops at only 50 percent [...]
	 */
	public static final SystemMessageId ACCUMULATED_PLAY_TIME_WARNING1;
	
	/**
	 * ID: 1698<br>
	 * Message: Your accumulated play time has reached Ill-health level, so you will no longer gain experience or item drops. [...}
	 */
	public static final SystemMessageId ACCUMULATED_PLAY_TIME_WARNING2;
	
	/**
	 * ID: 1699<br>
	 * Message: You cannot dismiss a party member by force.
	 */
	public static final SystemMessageId CANNOT_DISMISS_PARTY_MEMBER;
	
	/**
	 * ID: 1700<br>
	 * Message: You don't have enough spiritshots needed for a pet/servitor.
	 */
	public static final SystemMessageId NOT_ENOUGH_SPIRITHOTS_FOR_PET;
	
	/**
	 * ID: 1701<br>
	 * Message: You don't have enough soulshots needed for a pet/servitor.
	 */
	public static final SystemMessageId NOT_ENOUGH_SOULSHOTS_FOR_PET;
	
	/**
	 * ID: 1702<br>
	 * Message: $s1 is using a third party program.
	 */
	public static final SystemMessageId S1_USING_THIRD_PARTY_PROGRAM;
	
	/**
	 * ID: 1703<br>
	 * Message: The previous investigated user is not using a third party program
	 */
	public static final SystemMessageId NOT_USING_THIRD_PARTY_PROGRAM;
	
	/**
	 * ID: 1704<br>
	 * Message: Please close the setup window for your private manufacturing store or private store, and try again.
	 */
	public static final SystemMessageId CLOSE_STORE_WINDOW_AND_TRY_AGAIN;
	
	/**
	 * ID: 1705<br>
	 * Message: PC Bang Points acquisition period. Points acquisition period left $s1 hour.
	 */
	public static final SystemMessageId PCPOINT_ACQUISITION_PERIOD;
	
	/**
	 * ID: 1706<br>
	 * Message: PC Bang Points use period. Points acquisition period left $s1 hour.
	 */
	public static final SystemMessageId PCPOINT_USE_PERIOD;
	
	/**
	 * ID: 1707<br>
	 * Message: You acquired $s1 PC Bang Point.
	 */
	public static final SystemMessageId ACQUIRED_S1_PCPOINT;
	
	/**
	 * ID: 1708<br>
	 * Message: Double points! You acquired $s1 PC Bang Point.
	 */
	public static final SystemMessageId ACQUIRED_S1_PCPOINT_DOUBLE;
	
	/**
	 * ID: 1709<br>
	 * Message: You are using $s1 point.
	 */
	public static final SystemMessageId USING_S1_PCPOINT;
	
	/**
	 * ID: 1710<br>
	 * Message: You are short of accumulated points.
	 */
	public static final SystemMessageId SHORT_OF_ACCUMULATED_POINTS;
	
	/**
	 * ID: 1711<br>
	 * Message: PC Bang Points use period has expired.
	 */
	public static final SystemMessageId PCPOINT_USE_PERIOD_EXPIRED;
	
	/**
	 * ID: 1712<br>
	 * Message: The PC Bang Points accumulation period has expired.
	 */
	public static final SystemMessageId PCPOINT_ACCUMULATION_PERIOD_EXPIRED;
	
	/**
	 * ID: 1713<br>
	 * Message: The games may be delayed due to an insufficient number of players waiting.
	 */
	public static final SystemMessageId GAMES_DELAYED;
	
	/**
	 * ID: 1714<br>
	 * Message: Current Location: $s1, $s2, $s3 (Near the Town of Schuttgart)
	 */
	public static final SystemMessageId LOC_SCHUTTGART_S1_S2_S3;
	
	/**
	 * ID: 1715<br>
	 * Message: This is a Peaceful Zone - PvP is not allowed in this area.
	 */
	public static final SystemMessageId PEACEFUL_ZONE;
	
	/**
	 * ID: 1716<br>
	 * Message: Altered Zone
	 */
	public static final SystemMessageId ALTERED_ZONE;
	
	/**
	 * ID: 1717<br>
	 * Message: Siege War Zone - A siege is currently in progress in this area. If a character dies in this zone, their resurrection ability may be restricted.
	 */
	public static final SystemMessageId SIEGE_ZONE;
	
	/**
	 * ID: 1718<br>
	 * Message: General Field
	 */
	public static final SystemMessageId GENERAL_ZONE;
	
	/**
	 * ID: 1719<br>
	 * Message: Seven Signs Zone - Although a character's level may increase while in this area, HP and MP will not be regenerated.
	 */
	public static final SystemMessageId SEVENSIGNS_ZONE;
	
	/**
	 * ID: 1720<br>
	 * Message: ---
	 */
	public static final SystemMessageId UNKNOWN1;
	
	/**
	 * ID: 1721<br>
	 * Message: Combat Zone
	 */
	public static final SystemMessageId COMBAT_ZONE;
	
	/**
	 * ID: 1722<br>
	 * Message: Please enter the name of the item you wish to search for.
	 */
	public static final SystemMessageId ENTER_ITEM_NAME_SEARCH;
	
	/**
	 * ID: 1723<br>
	 * Message: Please take a moment to provide feedback about the petition service.
	 */
	public static final SystemMessageId PLEASE_PROVIDE_PETITION_FEEDBACK;
	
	/**
	 * ID: 1724<br>
	 * Message: A servitor whom is engaged in battle cannot be de-activated.
	 */
	public static final SystemMessageId SERVITOR_NOT_RETURN_IN_BATTLE;
	
	/**
	 * ID: 1725<br>
	 * Message: You have earned $s1 raid point(s).
	 */
	public static final SystemMessageId EARNED_S1_RAID_POINTS;
	
	/**
	 * ID: 1726<br>
	 * Message: $s1 has disappeared because its time period has expired.
	 */
	public static final SystemMessageId S1_PERIOD_EXPIRED_DISAPPEARED;
	
	/**
	 * ID: 1727<br>
	 * Message: $c1 has invited you to a party room. Do you accept?
	 */
	public static final SystemMessageId C1_INVITED_YOU_TO_PARTY_ROOM_CONFIRM;
	
	/**
	 * ID: 1728<br>
	 * Message: The recipient of your invitation did not accept the party matching invitation.
	 */
	public static final SystemMessageId PARTY_MATCHING_REQUEST_NO_RESPONSE;
	
	/**
	 * ID: 1729<br>
	 * Message: You cannot join a Command Channel while teleporting.
	 */
	public static final SystemMessageId NOT_JOIN_CHANNEL_WHILE_TELEPORTING;
	
	/**
	 * ID: 1730<br>
	 * Message: To establish a Clan Academy, your clan must be Level 5 or higher.
	 */
	public static final SystemMessageId YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN_ACADEMY;
	
	/**
	 * ID: 1731<br>
	 * Message: Only the leader can create a Clan Academy.
	 */
	public static final SystemMessageId ONLY_LEADER_CAN_CREATE_ACADEMY;
	
	/**
	 * ID: 1732<br>
	 * Message: To create a Clan Academy, a Blood Mark is needed.
	 */
	public static final SystemMessageId NEED_BLOODMARK_FOR_ACADEMY;
	
	/**
	 * ID: 1733<br>
	 * Message: You do not have enough adena to create a Clan Academy.
	 */
	public static final SystemMessageId NEED_ADENA_FOR_ACADEMY;
	
	/**
	 * ID: 1734<br>
	 * Message: To join a Clan Academy, characters must be Level 40 or below, not belong another clan and not yet completed their 2nd class transfer.
	 */
	public static final SystemMessageId ACADEMY_REQUIREMENTS;
	
	/**
	 * ID: 1735<br>
	 * Message: $s1 does not meet the requirements to join a Clan Academy.
	 */
	public static final SystemMessageId S1_DOESNOT_MEET_REQUIREMENTS_TO_JOIN_ACADEMY;
	
	/**
	 * ID: 1736<br>
	 * Message: The Clan Academy has reached its maximum enrollment.
	 */
	public static final SystemMessageId ACADEMY_MAXIMUM;
	
	/**
	 * ID: 1737<br>
	 * Message: Your clan has not established a Clan Academy but is eligible to do so.
	 */
	public static final SystemMessageId CLAN_CAN_CREATE_ACADEMY;
	
	/**
	 * ID: 1738<br>
	 * Message: Your clan has already established a Clan Academy.
	 */
	public static final SystemMessageId CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY;
	
	/**
	 * ID: 1739<br>
	 * Message: Would you like to create a Clan Academy?
	 */
	public static final SystemMessageId CLAN_ACADEMY_CREATE_CONFIRM;
	
	/**
	 * ID: 1740<br>
	 * Message: Please enter the name of the Clan Academy.
	 */
	public static final SystemMessageId ACADEMY_CREATE_ENTER_NAME;
	
	/**
	 * ID: 1741<br>
	 * Message: Congratulations! The $s1's Clan Academy has been created.
	 */
	public static final SystemMessageId THE_S1S_CLAN_ACADEMY_HAS_BEEN_CREATED;
	
	/**
	 * ID: 1742<br>
	 * Message: A message inviting $s1 to join the Clan Academy is being sent.
	 */
	public static final SystemMessageId ACADEMY_INVITATION_SENT_TO_S1;
	
	/**
	 * ID: 1743<br>
	 * Message: To open a Clan Academy, the leader of a Level 5 clan or above must pay XX Proofs of Blood or a certain amount of adena.
	 */
	public static final SystemMessageId OPEN_ACADEMY_CONDITIONS;
	
	/**
	 * ID: 1744<br>
	 * Message: There was no response to your invitation to join the Clan Academy, so the invitation has been rescinded.
	 */
	public static final SystemMessageId ACADEMY_JOIN_NO_RESPONSE;
	
	/**
	 * ID: 1745<br>
	 * Message: The recipient of your invitation to join the Clan Academy has declined.
	 */
	public static final SystemMessageId ACADEMY_JOIN_DECLINE;
	
	/**
	 * ID: 1746<br>
	 * Message: You have already joined a Clan Academy.
	 */
	public static final SystemMessageId ALREADY_JOINED_ACADEMY;
	
	/**
	 * ID: 1747<br>
	 * Message: $s1 has sent you an invitation to join the Clan Academy belonging to the $s2 clan. Do you accept?
	 */
	public static final SystemMessageId JOIN_ACADEMY_REQUEST_BY_S1_FOR_CLAN_S2;
	
	/**
	 * ID: 1748<br>
	 * Message: Clan Academy member $s1 has successfully completed the 2nd class transfer and obtained $s2 Clan Reputation points.
	 */
	public static final SystemMessageId CLAN_MEMBER_GRADUATED_FROM_ACADEMY;
	
	/**
	 * ID: 1749<br>
	 * Message: Congratulations! You will now graduate from the Clan Academy and leave your current clan. As a graduate of the academy, you can immediately join a clan as a regular member without being subject to any penalties.
	 */
	public static final SystemMessageId ACADEMY_MEMBERSHIP_TERMINATED;
	
	/**
	 * ID: 1750<br>
	 * Message: $c1% does not meet the participation requirements. The owner of $s2 cannot participate in the Olympiad.
	 */
	public static final SystemMessageId C1_CANNOT_JOIN_OLYMPIAD_POSSESSING_S2;
	
	/**
	 * ID: 1751<br>
	 * Message: The Grand Master has given you a commemorative item.
	 */
	public static final SystemMessageId GRAND_MASTER_COMMEMORATIVE_ITEM;
	
	/**
	 * ID: 1752<br>
	 * Message: Since the clan has received a graduate of the Clan Academy, it has earned $s1 points towards its reputation score.
	 */
	public static final SystemMessageId MEMBER_GRADUATED_EARNED_S1_REPU;
	
	/**
	 * ID: 1753<br>
	 * Message: The clan leader has decreed that that particular privilege cannot be granted to a Clan Academy member.
	 */
	public static final SystemMessageId CANT_TRANSFER_PRIVILEGE_TO_ACADEMY_MEMBER;
	
	/**
	 * ID: 1754<br>
	 * Message: That privilege cannot be granted to a Clan Academy member.
	 */
	public static final SystemMessageId RIGHT_CANT_TRANSFERRED_TO_ACADEMY_MEMBER;
	
	/**
	 * ID: 1755<br>
	 * Message: $s2 has been designated as the apprentice of clan member $s1.
	 */
	public static final SystemMessageId S2_HAS_BEEN_DESIGNATED_AS_APPRENTICE_OF_CLAN_MEMBER_S1;
	
	/**
	 * ID: 1756<br>
	 * Message: Your apprentice, $s1, has logged in.
	 */
	public static final SystemMessageId YOUR_APPRENTICE_S1_HAS_LOGGED_IN;
	
	/**
	 * ID: 1757<br>
	 * Message: Your apprentice, $c1, has logged out.
	 */
	public static final SystemMessageId YOUR_APPRENTICE_C1_HAS_LOGGED_OUT;
	
	/**
	 * ID: 1758<br>
	 * Message: Your sponsor, $c1, has logged in.
	 */
	public static final SystemMessageId YOUR_SPONSOR_C1_HAS_LOGGED_IN;
	
	/**
	 * ID: 1759<br>
	 * Message: Your sponsor, $c1, has logged out.
	 */
	public static final SystemMessageId YOUR_SPONSOR_C1_HAS_LOGGED_OUT;
	
	/**
	 * ID: 1760<br>
	 * Message: Clan member $c1's name title has been changed to $2.
	 */
	public static final SystemMessageId CLAN_MEMBER_C1_TITLE_CHANGED_TO_S2;
	
	/**
	 * ID: 1761<br>
	 * Message: Clan member $c1's privilege level has been changed to $s2.
	 */
	public static final SystemMessageId CLAN_MEMBER_C1_PRIVILEGE_CHANGED_TO_S2;
	
	/**
	 * ID: 1762<br>
	 * Message: You do not have the right to dismiss an apprentice.
	 */
	public static final SystemMessageId YOU_DO_NOT_HAVE_THE_RIGHT_TO_DISMISS_AN_APPRENTICE;
	
	/**
	 * ID: 1763<br>
	 * Message: $s2, clan member $c1's apprentice, has been removed.
	 */
	public static final SystemMessageId S2_CLAN_MEMBER_C1_APPRENTICE_HAS_BEEN_REMOVED;
	
	/**
	 * ID: 1764<br>
	 * Message: This item can only be worn by a member of the Clan Academy.
	 */
	public static final SystemMessageId EQUIP_ONLY_FOR_ACADEMY;
	
	/**
	 * ID: 1765<br>
	 * Message: As a graduate of the Clan Academy, you can no longer wear this item.
	 */
	public static final SystemMessageId EQUIP_NOT_FOR_GRADUATES;
	
	/**
	 * ID: 1766<br>
	 * Message: An application to join the clan has been sent to $c1 in $s2.
	 */
	public static final SystemMessageId CLAN_JOIN_APPLICATION_SENT_TO_C1_IN_S2;
	
	/**
	 * ID: 1767<br>
	 * Message: An application to join the clan Academy has been sent to $c1.
	 */
	public static final SystemMessageId ACADEMY_JOIN_APPLICATION_SENT_TO_C1;
	
	/**
	 * ID: 1768<br>
	 * Message: $c1 has invited you to join the Clan Academy of $s2 clan. Would you like to join?
	 */
	public static final SystemMessageId JOIN_REQUEST_BY_C1_TO_CLAN_S2_ACADEMY;
	
	/**
	 * ID: 1769<br>
	 * Message: $c1 has sent you an invitation to join the $s3 Order of Knights under the $s2 clan. Would you like to join?
	 */
	public static final SystemMessageId JOIN_REQUEST_BY_C1_TO_ORDER_OF_KNIGHTS_S3_UNDER_CLAN_S2;
	
	/**
	 * ID: 1770<br>
	 * Message: The clan's reputation score has dropped below 0. The clan may face certain penalties as a result.
	 */
	public static final SystemMessageId CLAN_REPU_0_MAY_FACE_PENALTIES;
	
	/**
	 * ID: 1771<br>
	 * Message: Now that your clan level is above Level 5, it can accumulate clan reputation points.
	 */
	public static final SystemMessageId CLAN_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS;
	
	/**
	 * ID: 1772<br>
	 * Message: Since your clan was defeated in a siege, $s1 points have been deducted from your clan's reputation score and given to the opposing clan.
	 */
	public static final SystemMessageId CLAN_WAS_DEFEATED_IN_SIEGE_AND_LOST_S1_REPUTATION_POINTS;
	
	/**
	 * ID: 1773<br>
	 * Message: Since your clan emerged victorious from the siege, $s1 points have been added to your clan's reputation score.
	 */
	public static final SystemMessageId CLAN_VICTORIOUS_IN_SIEGE_AND_GAINED_S1_REPUTATION_POINTS;
	
	/**
	 * ID: 1774<br>
	 * Message: Your clan's newly acquired contested clan hall has added $s1 points to your clan's reputation score.
	 */
	public static final SystemMessageId CLAN_ACQUIRED_CONTESTED_CLAN_HALL_AND_S1_REPUTATION_POINTS;
	
	/**
	 * ID: 1775<br>
	 * Message: Clan member $c1 was an active member of the highest-ranked party in the Festival of Darkness. $s2 points have been added to your clan's reputation score.
	 */
	public static final SystemMessageId CLAN_MEMBER_C1_WAS_IN_HIGHEST_RANKED_PARTY_IN_FESTIVAL_OF_DARKNESS_AND_GAINED_S2_REPUTATION;
	
	/**
	 * ID: 1776<br>
	 * Message: Clan member $c1 was named a hero. $2s points have been added to your clan's reputation score.
	 */
	public static final SystemMessageId CLAN_MEMBER_C1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS;
	
	/**
	 * ID: 1777<br>
	 * Message: You have successfully completed a clan quest. $s1 points have been added to your clan's reputation score.
	 */
	public static final SystemMessageId CLAN_QUEST_COMPLETED_AND_S1_POINTS_GAINED;
	
	/**
	 * ID: 1778<br>
	 * Message: An opposing clan has captured your clan's contested clan hall. $s1 points have been deducted from your clan's reputation score.
	 */
	public static final SystemMessageId OPPOSING_CLAN_CAPTURED_CLAN_HALL_AND_YOUR_CLAN_LOSES_S1_POINTS;
	
	/**
	 * ID: 1779<br>
	 * Message: After losing the contested clan hall, 300 points have been deducted from your clan's reputation score.
	 */
	public static final SystemMessageId CLAN_LOST_CONTESTED_CLAN_HALL_AND_300_POINTS;
	
	/**
	 * ID: 1780<br>
	 * Message: Your clan has captured your opponent's contested clan hall. $s1 points have been deducted from your opponent's clan reputation score.
	 */
	public static final SystemMessageId CLAN_CAPTURED_CONTESTED_CLAN_HALL_AND_S1_POINTS_DEDUCTED_FROM_OPPONENT;
	
	/**
	 * ID: 1781<br>
	 * Message: Your clan has added $1s points to its clan reputation score.
	 */
	public static final SystemMessageId CLAN_ADDED_S1S_POINTS_TO_REPUTATION_SCORE;
	
	/**
	 * ID: 1782<br>
	 * Message: Your clan member $c1 was killed. $s2 points have been deducted from your clan's reputation score and added to your opponent's clan reputation score.
	 */
	public static final SystemMessageId CLAN_MEMBER_C1_WAS_KILLED_AND_S2_POINTS_DEDUCTED_FROM_REPUTATION;
	
	/**
	 * ID: 1783<br>
	 * Message: For killing an opposing clan member, $s1 points have been deducted from your opponents' clan reputation score.
	 */
	public static final SystemMessageId FOR_KILLING_OPPOSING_MEMBER_S1_POINTS_WERE_DEDUCTED_FROM_OPPONENTS;
	
	/**
	 * ID: 1784<br>
	 * Message: Your clan has failed to defend the castle. $s1 points have been deducted from your clan's reputation score and added to your opponents'.
	 */
	public static final SystemMessageId YOUR_CLAN_FAILED_TO_DEFEND_CASTLE_AND_S1_POINTS_LOST_AND_ADDED_TO_OPPONENT;
	
	/**
	 * ID: 1785<br>
	 * Message: The clan you belong to has been initialized. $s1 points have been deducted from your clan reputation score.
	 */
	public static final SystemMessageId YOUR_CLAN_HAS_BEEN_INITIALIZED_AND_S1_POINTS_LOST;
	
	/**
	 * ID: 1786<br>
	 * Message: Your clan has failed to defend the castle. $s1 points have been deducted from your clan's reputation score.
	 */
	public static final SystemMessageId YOUR_CLAN_FAILED_TO_DEFEND_CASTLE_AND_S1_POINTS_LOST;
	
	/**
	 * ID: 1787<br>
	 * Message: $s1 points have been deducted from the clan's reputation score.
	 */
	public static final SystemMessageId S1_DEDUCTED_FROM_CLAN_REP;
	
	/**
	 * ID: 1788<br>
	 * Message: The clan skill $s1 has been added.
	 */
	public static final SystemMessageId CLAN_SKILL_S1_ADDED;
	
	/**
	 * ID: 1789<br>
	 * Message: Since the Clan Reputation Score has dropped to 0 or lower, your clan skill(s) will be de-activated.
	 */
	public static final SystemMessageId REPUTATION_POINTS_0_OR_LOWER_CLAN_SKILLS_DEACTIVATED;
	
	/**
	 * ID: 1790<br>
	 * Message: The conditions necessary to increase the clan's level have not been met.
	 */
	public static final SystemMessageId FAILED_TO_INCREASE_CLAN_LEVEL;
	
	/**
	 * ID: 1791<br>
	 * Message: The conditions necessary to create a military unit have not been met.
	 */
	public static final SystemMessageId YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_MILITARY_UNIT;
	
	/**
	 * ID: 1792<br>
	 * Message: Please assign a manager for your new Order of Knights.
	 */
	public static final SystemMessageId ASSIGN_MANAGER_FOR_ORDER_OF_KNIGHTS;
	
	/**
	 * ID: 1793<br>
	 * Message: $c1 has been selected as the captain of $s2.
	 */
	public static final SystemMessageId C1_HAS_BEEN_SELECTED_AS_CAPTAIN_OF_S2;
	
	/**
	 * ID: 1794<br>
	 * Message: The Knights of $s1 have been created.
	 */
	public static final SystemMessageId THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED;
	
	/**
	 * ID: 1795<br>
	 * Message: The Royal Guard of $s1 have been created.
	 */
	public static final SystemMessageId THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED;
	
	/**
	 * ID: 1796<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE17;
	
	/**
	 * ID: 1797<br>
	 * Message: $c1 has been promoted to $s2.
	 */
	public static final SystemMessageId C1_PROMOTED_TO_S2;
	
	/**
	 * ID: 1798<br>
	 * Message: Clan lord privileges have been transferred to $c1.
	 */
	public static final SystemMessageId CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_C1;
	
	/**
	 * ID: 1799<br>
	 * Message: We are searching for BOT users. Please try again later.
	 */
	public static final SystemMessageId SEARCHING_FOR_BOT_USERS_TRY_AGAIN_LATER;
	
	/**
	 * ID: 1800<br>
	 * Message: User $c1 has a history of using BOT.
	 */
	public static final SystemMessageId C1_HISTORY_USING_BOT;
	
	/**
	 * ID: 1801<br>
	 * Message: The attempt to sell has failed.
	 */
	public static final SystemMessageId SELL_ATTEMPT_FAILED;
	
	/**
	 * ID: 1802<br>
	 * Message: The attempt to trade has failed.
	 */
	public static final SystemMessageId TRADE_ATTEMPT_FAILED;
	
	/**
	 * ID: 1803<br>
	 * Message: The request to participate in the game cannot be made starting from 10 minutes before the end of the game.
	 */
	public static final SystemMessageId GAME_REQUEST_CANNOT_BE_MADE;
	
	/**
	 * ID: 1804<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE18;
	
	/**
	 * ID: 1805<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE19;
	
	/**
	 * ID: 1806<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE20;
	
	/**
	 * ID: 1807<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE21;
	
	/**
	 * ID: 1808<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE22;
	
	/**
	 * ID: 1809<br>
	 * Message: Your account must be verified. For information on verification procedures, please visit the PlayNC website (http://us.ncsoft.com/support/).
	 */
	public static final SystemMessageId ACCOUNT_MUST_VERIFIED;
	
	/**
	 * ID: 1810<br>
	 * Message: The refuse invitation state has been activated.
	 */
	public static final SystemMessageId REFUSE_INVITATION_ACTIVATED;
	
	/**
	 * ID: 1812<br>
	 * Message: Since the refuse invitation state is currently activated, no invitation can be made
	 */
	public static final SystemMessageId REFUSE_INVITATION_CURRENTLY_ACTIVE;
	
	/**
	 * ID: 1813<br>
	 * Message: $s1 has $s2 hour(s) of usage time remaining.
	 */
	public static final SystemMessageId THERE_IS_S1_HOUR_AND_S2_MINUTE_LEFT_OF_THE_FIXED_USAGE_TIME;
	
	/**
	 * ID: 1814<br>
	 * Message: $s1 has $s2 minute(s) of usage time remaining.
	 */
	public static final SystemMessageId S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1;
	
	/**
	 * ID: 1815<br>
	 * Message: $s2 was dropped in the $s1 region.
	 */
	public static final SystemMessageId S2_WAS_DROPPED_IN_THE_S1_REGION;
	
	/**
	 * ID: 1816<br>
	 * Message: The owner of $s2 has appeared in the $s1 region.
	 */
	public static final SystemMessageId THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION;
	
	/**
	 * ID: 1817<br>
	 * Message: $s2's owner has logged into the $s1 region.
	 */
	public static final SystemMessageId S2_OWNER_HAS_LOGGED_INTO_THE_S1_REGION;
	
	/**
	 * ID: 1818<br>
	 * Message: $s1 has disappeared.
	 */
	public static final SystemMessageId S1_HAS_DISAPPEARED;
	
	/**
	 * ID: 1819<br>
	 * Message: An evil is pulsating from $s2 in $s1.
	 */
	public static final SystemMessageId EVIL_FROM_S2_IN_S1;
	
	/**
	 * ID: 1820<br>
	 * Message: $s1 is currently asleep.
	 */
	public static final SystemMessageId S1_CURRENTLY_SLEEP;
	
	/**
	 * ID: 1821<br>
	 * Message: $s2's evil presence is felt in $s1.
	 */
	public static final SystemMessageId S2_EVIL_PRESENCE_FELT_IN_S1;
	
	/**
	 * ID: 1822<br>
	 * Message: $s1 has been sealed.
	 */
	public static final SystemMessageId S1_SEALED;
	
	/**
	 * ID: 1823<br>
	 * Message: The registration period for a clan hall war has ended.
	 */
	public static final SystemMessageId CLANHALL_WAR_REGISTRATION_PERIOD_ENDED;
	
	/**
	 * ID: 1824<br>
	 * Message: You have been registered for a clan hall war. Please move to the left side of the clan hall's arena and get ready.
	 */
	public static final SystemMessageId REGISTERED_FOR_CLANHALL_WAR;
	
	/**
	 * ID: 1825<br>
	 * Message: You have failed in your attempt to register for the clan hall war. Please try again.
	 */
	public static final SystemMessageId CLANHALL_WAR_REGISTRATION_FAILED;
	
	/**
	 * ID: 1826<br>
	 * Message: In $s1 minute(s), the game will begin. All players must hurry and move to the left side of the clan hall's arena.
	 */
	public static final SystemMessageId CLANHALL_WAR_BEGINS_IN_S1_MINUTES;
	
	/**
	 * ID: 1827<br>
	 * Message: In $s1 minute(s), the game will begin. All players must, please enter the arena now
	 */
	public static final SystemMessageId CLANHALL_WAR_BEGINS_IN_S1_MINUTES_ENTER_NOW;
	
	/**
	 * ID: 1828<br>
	 * Message: In $s1 seconds(s), the game will begin.
	 */
	public static final SystemMessageId CLANHALL_WAR_BEGINS_IN_S1_SECONDS;
	
	/**
	 * ID: 1829<br>
	 * Message: The Command Channel is full.
	 */
	public static final SystemMessageId COMMAND_CHANNEL_FULL;
	
	/**
	 * ID: 1830<br>
	 * Message: $c1 is not allowed to use the party room invite command. Please update the waiting list.
	 */
	public static final SystemMessageId C1_NOT_ALLOWED_INVITE_TO_PARTY_ROOM;
	
	/**
	 * ID: 1831<br>
	 * Message: $c1 does not meet the conditions of the party room. Please update the waiting list.
	 */
	public static final SystemMessageId C1_NOT_MEET_CONDITIONS_FOR_PARTY_ROOM;
	
	/**
	 * ID: 1832<br>
	 * Message: Only a room leader may invite others to a party room.
	 */
	public static final SystemMessageId ONLY_ROOM_LEADER_CAN_INVITE;
	
	/**
	 * ID: 1833<br>
	 * Message: All of $s1 will be dropped. Would you like to continue?
	 */
	public static final SystemMessageId CONFIRM_DROP_ALL_OF_S1;
	
	/**
	 * ID: 1834<br>
	 * Message: The party room is full. No more characters can be invitet in
	 */
	public static final SystemMessageId PARTY_ROOM_FULL;
	
	/**
	 * ID: 1835<br>
	 * Message: $s1 is full and cannot accept additional clan members at this time.
	 */
	public static final SystemMessageId S1_CLAN_IS_FULL;
	
	/**
	 * ID: 1836<br>
	 * Message: You cannot join a Clan Academy because you have successfully completed your 2nd class transfer.
	 */
	public static final SystemMessageId CANNOT_JOIN_ACADEMY_AFTER_2ND_OCCUPATION;
	
	/**
	 * ID: 1837<br>
	 * Message: $c1 has sent you an invitation to join the $s3 Royal Guard under the $s2 clan. Would you like to join?
	 */
	public static final SystemMessageId C1_SENT_INVITATION_TO_ROYAL_GUARD_S3_OF_CLAN_S2;
	
	/**
	 * ID: 1838<br>
	 * Message: 1. The coupon an be used once per character.
	 */
	public static final SystemMessageId COUPON_ONCE_PER_CHARACTER;
	
	/**
	 * ID: 1839<br>
	 * Message: 2. A used serial number may not be used again.
	 */
	public static final SystemMessageId SERIAL_MAY_USED_ONCE;
	
	/**
	 * ID: 1840<br>
	 * Message: 3. If you enter the incorrect serial number more than 5 times, you may use it again after a certain amount of time passes.
	 */
	public static final SystemMessageId SERIAL_INPUT_INCORRECT;
	
	/**
	 * ID: 1841<br>
	 * Message: The clan hall war has been cancelled. Not enough clans have registered.
	 */
	public static final SystemMessageId CLANHALL_WAR_CANCELLED;
	
	/**
	 * ID: 1842<br>
	 * Message: $c1 wishes to summon you from $s2. Do you accept?
	 */
	public static final SystemMessageId C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT;
	
	/**
	 * ID: 1843<br>
	 * Message: $c1 is engaged in combat and cannot be summoned.
	 */
	public static final SystemMessageId C1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED;
	
	/**
	 * ID: 1844<br>
	 * Message: $c1 is dead at the moment and cannot be summoned.
	 */
	public static final SystemMessageId C1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED;
	
	/**
	 * ID: 1845<br>
	 * Message: Hero weapons cannot be destroyed.
	 */
	public static final SystemMessageId HERO_WEAPONS_CANT_DESTROYED;
	
	/**
	 * ID: 1846<br>
	 * Message: You are too far away from the Fenrir to mount it.
	 */
	public static final SystemMessageId TOO_FAR_AWAY_FROM_FENRIR_TO_MOUNT;
	
	/**
	 * ID: 1847<br>
	 * Message: You caught a fish $s1 in length.
	 */
	public static final SystemMessageId CAUGHT_FISH_S1_LENGTH;
	
	/**
	 * ID: 1848<br>
	 * Message: Because of the size of fish caught, you will be registered in the ranking
	 */
	public static final SystemMessageId REGISTERED_IN_FISH_SIZE_RANKING;
	
	/**
	 * ID: 1849<br>
	 * Message: All of $s1 will be discarded. Would you like to continue?
	 */
	public static final SystemMessageId CONFIRM_DISCARD_ALL_OF_S1;
	
	/**
	 * ID: 1850<br>
	 * Message: The Captain of the Order of Knights cannot be appointed.
	 */
	public static final SystemMessageId CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED;
	
	/**
	 * ID: 1851<br>
	 * Message: The Captain of the Royal Guard cannot be appointed.
	 */
	public static final SystemMessageId CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED;
	
	/**
	 * ID: 1852<br>
	 * Message: The attempt to acquire the skill has failed because of an insufficient Clan Reputation Score.
	 */
	public static final SystemMessageId ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE;
	
	/**
	 * ID: 1853<br>
	 * Message: Quantity items of the same type cannot be exchanged at the same time
	 */
	public static final SystemMessageId CANT_EXCHANGE_QUANTITY_ITEMS_OF_SAME_TYPE;
	
	/**
	 * ID: 1854<br>
	 * Message: The item was converted successfully.
	 */
	public static final SystemMessageId ITEM_CONVERTED_SUCCESSFULLY;
	
	/**
	 * ID: 1855<br>
	 * Message: Another military unit is already using that name. Please enter a different name.
	 */
	public static final SystemMessageId ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME;
	
	/**
	 * ID: 1856<br>
	 * Message: Since your opponent is now the owner of $s1, the Olympiad has been cancelled.
	 */
	public static final SystemMessageId OPPONENT_POSSESSES_S1_OLYMPIAD_CANCELLED;
	
	/**
	 * ID: 1857<br>
	 * Message: $c1 is the owner of $s2 and cannot participate in the Olympiad.
	 */
	public static final SystemMessageId C1_OWNS_S2_AND_CANNOT_PARTICIPATE_IN_OLYMPIAD;
	
	/**
	 * ID: 1858<br>
	 * Message: $c1 is currently dead and cannot participate in the Olympiad.
	 */
	public static final SystemMessageId C1_CANNOT_PARTICIPATE_OLYMPIAD_WHILE_DEAD;
	
	/**
	 * ID: 1859<br>
	 * Message: You exceeded the quantity that can be moved at one time.
	 */
	public static final SystemMessageId EXCEEDED_QUANTITY_FOR_MOVED;
	
	/**
	 * ID: 1860<br>
	 * Message: The Clan Reputation Score is too low.
	 */
	public static final SystemMessageId THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW;
	
	/**
	 * ID: 1861<br>
	 * Message: The clan's crest has been deleted.
	 */
	public static final SystemMessageId CLAN_CREST_HAS_BEEN_DELETED;
	
	/**
	 * ID: 1862<br>
	 * Message: Clan skills will now be activated since the clan's reputation score is 0 or higher.
	 */
	public static final SystemMessageId CLAN_SKILLS_WILL_BE_ACTIVATED_SINCE_REPUTATION_IS_0_OR_HIGHER;
	
	/**
	 * ID: 1863<br>
	 * Message: $c1 purchased a clan item, reducing the Clan Reputation by $s2 points.
	 */
	public static final SystemMessageId C1_PURCHASED_CLAN_ITEM_REDUCING_S2_REPU_POINTS;
	
	/**
	 * ID: 1864<br>
	 * Message: Your pet/servitor is unresponsive and will not obey any orders.
	 */
	public static final SystemMessageId PET_REFUSING_ORDER;
	
	/**
	 * ID: 1865<br>
	 * Message: Your pet/servitor is currently in a state of distress.
	 */
	public static final SystemMessageId PET_IN_STATE_OF_DISTRESS;
	
	/**
	 * ID: 1866<br>
	 * Message: MP was reduced by $s1.
	 */
	public static final SystemMessageId MP_REDUCED_BY_S1;
	
	/**
	 * ID: 1867<br>
	 * Message: Your opponent's MP was reduced by $s1.
	 */
	public static final SystemMessageId YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1;
	
	/**
	 * ID: 1868<br>
	 * Message: You cannot exchange an item while it is being used.
	 */
	public static final SystemMessageId CANNOT_EXCHANCE_USED_ITEM;
	
	/**
	 * ID: 1869<br>
	 * Message: $c1 has granted the Command Channel's master party the privilege of item looting.
	 */
	public static final SystemMessageId C1_GRANTED_MASTER_PARTY_LOOTING_RIGHTS;
	
	/**
	 * ID: 1870<br>
	 * Message: A Command Channel with looting rights already exists.
	 */
	public static final SystemMessageId COMMAND_CHANNEL_WITH_LOOTING_RIGHTS_EXISTS;
	
	/**
	 * ID: 1871<br>
	 * Message: Do you want to dismiss $c1 from the clan?
	 */
	public static final SystemMessageId CONFIRM_DISMISS_C1_FROM_CLAN;
	
	/**
	 * ID: 1872<br>
	 * Message: You have $s1 hour(s) and $s2 minute(s) left.
	 */
	public static final SystemMessageId S1_HOURS_S2_MINUTES_LEFT;
	
	/**
	 * ID: 1873<br>
	 * Message: There are $s1 hour(s) and $s2 minute(s) left in the fixed use time for this PC Cafe.
	 */
	public static final SystemMessageId S1_HOURS_S2_MINUTES_LEFT_FOR_THIS_PCCAFE;
	
	/**
	 * ID: 1874<br>
	 * Message: There are $s1 minute(s) left for this individual user.
	 */
	public static final SystemMessageId S1_MINUTES_LEFT_FOR_THIS_USER;
	
	/**
	 * ID: 1875<br>
	 * Message: There are $s1 minute(s) left in the fixed use time for this PC Cafe.
	 */
	public static final SystemMessageId S1_MINUTES_LEFT_FOR_THIS_PCCAFE;
	
	/**
	 * ID: 1876<br>
	 * Message: Do you want to leave $s1 clan?
	 */
	public static final SystemMessageId CONFIRM_LEAVE_S1_CLAN;
	
	/**
	 * ID: 1877<br>
	 * Message: The game will end in $s1 minutes.
	 */
	public static final SystemMessageId GAME_WILL_END_IN_S1_MINUTES;
	
	/**
	 * ID: 1878<br>
	 * Message: The game will end in $s1 seconds.
	 */
	public static final SystemMessageId GAME_WILL_END_IN_S1_SECONDS;
	
	/**
	 * ID: 1879<br>
	 * Message: In $s1 minute(s), you will be teleported outside of the game arena.
	 */
	public static final SystemMessageId IN_S1_MINUTES_TELEPORTED_OUTSIDE_OF_GAME_ARENA;
	
	/**
	 * ID: 1880<br>
	 * Message: In $s1 seconds(s), you will be teleported outside of the game arena.
	 */
	public static final SystemMessageId IN_S1_SECONDS_TELEPORTED_OUTSIDE_OF_GAME_ARENA;
	
	/**
	 * ID: 1881<br>
	 * Message: The preliminary match will begin in $s1 second(s). Prepare yourself.
	 */
	public static final SystemMessageId PRELIMINARY_MATCH_BEGIN_IN_S1_SECONDS;
	
	/**
	 * ID: 1882<br>
	 * Message: Characters cannot be created from this server.
	 */
	public static final SystemMessageId CHARACTERS_NOT_CREATED_FROM_THIS_SERVER;
	
	/**
	 * ID: 1883<br>
	 * Message: There are no offerings I own or I made a bid for.
	 */
	public static final SystemMessageId NO_OFFERINGS_OWN_OR_MADE_BID_FOR;
	
	/**
	 * ID: 1884<br>
	 * Message: Enter the PC Room coupon serial number.
	 */
	public static final SystemMessageId ENTER_PCROOM_SERIAL_NUMBER;
	
	/**
	 * ID: 1885<br>
	 * Message: This serial number cannot be entered. Please try again in minute(s).
	 */
	public static final SystemMessageId SERIAL_NUMBER_CANT_ENTERED;
	
	/**
	 * ID: 1886<br>
	 * Message: This serial has already been used.
	 */
	public static final SystemMessageId SERIAL_NUMBER_ALREADY_USED;
	
	/**
	 * ID: 1887<br>
	 * Message: Invalid serial number. Your attempt to enter the number has failed time(s). You will be allowed to make more attempt(s).
	 */
	public static final SystemMessageId SERIAL_NUMBER_ENTERING_FAILED;
	
	/**
	 * ID: 1888<br>
	 * Message: Invalid serial number. Your attempt to enter the number has failed 5 time(s). Please try again in 4 hours.
	 */
	public static final SystemMessageId SERIAL_NUMBER_ENTERING_FAILED_5_TIMES;
	
	/**
	 * ID: 1889<br>
	 * Message: Congratulations! You have received $s1.
	 */
	public static final SystemMessageId CONGRATULATIONS_RECEIVED_S1;
	
	/**
	 * ID: 1890<br>
	 * Message: Since you have already used this coupon, you may not use this serial number.
	 */
	public static final SystemMessageId ALREADY_USED_COUPON_NOT_USE_SERIAL_NUMBER;
	
	/**
	 * ID: 1891<br>
	 * Message: You may not use items in a private store or private work shop.
	 */
	public static final SystemMessageId NOT_USE_ITEMS_IN_PRIVATE_STORE;
	
	/**
	 * ID: 1892<br>
	 * Message: The replay file for the previous version cannot be played.
	 */
	public static final SystemMessageId REPLAY_FILE_PREVIOUS_VERSION_CANT_PLAYED;
	
	/**
	 * ID: 1893<br>
	 * Message: This file cannot be replayed.
	 */
	public static final SystemMessageId FILE_CANT_REPLAYED;
	
	/**
	 * ID: 1894<br>
	 * Message: A sub-class cannot be created or changed while you are over your weight limit.
	 */
	public static final SystemMessageId NOT_SUBCLASS_WHILE_OVERWEIGHT;
	
	/**
	 * ID: 1895<br>
	 * Message: $c1 is in an area which blocks summoning.
	 */
	public static final SystemMessageId C1_IN_SUMMON_BLOCKING_AREA;
	
	/**
	 * ID: 1896<br>
	 * Message: $c1 has already been summoned.
	 */
	public static final SystemMessageId C1_ALREADY_SUMMONED;
	
	/**
	 * ID: 1897<br>
	 * Message: $s1 is required for summoning.
	 */
	public static final SystemMessageId S1_REQUIRED_FOR_SUMMONING;
	
	/**
	 * ID: 1898<br>
	 * Message: $c1 is currently trading or operating a private store and cannot be summoned.
	 */
	public static final SystemMessageId C1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED;
	
	/**
	 * ID: 1899<br>
	 * Message: Your target is in an area which blocks summoning.
	 */
	public static final SystemMessageId YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING;
	
	/**
	 * ID: 1900<br>
	 * Message: $c1 has entered the party room.
	 */
	public static final SystemMessageId C1_ENTERED_PARTY_ROOM;
	
	/**
	 * ID: 1901<br>
	 * Message: $c1 has invited you to enter the party room.
	 */
	public static final SystemMessageId C1_INVITED_YOU_TO_PARTY_ROOM;
	
	/**
	 * ID: 1902<br>
	 * Message: Incompatible item grade. This item cannot be used.
	 */
	public static final SystemMessageId INCOMPATIBLE_ITEM_GRADE;
	
	/**
	 * ID: 1903<br>
	 * Message: Those of you who have requested NCOTP should run NCOTP by using your cell phone to get the NCOTP password and enter it within 1 minute. If you have not requested NCOTP, leave this field blank and click the Login button.
	 */
	public static final SystemMessageId NCOTP;
	
	/**
	 * ID: 1904<br>
	 * Message: A sub-class may not be created or changed while a servitor or pet is summoned.
	 */
	public static final SystemMessageId CANT_SUBCLASS_WITH_SUMMONED_SERVITOR;
	
	/**
	 * ID: 1905<br>
	 * Message: $s2 of $s1 will be replaced with $s4 of $s3.
	 */
	public static final SystemMessageId S2_OF_S1_WILL_REPLACED_WITH_S4_OF_S3;
	
	/**
	 * ID: 1906<br>
	 * Message: Select the combat unit to transfer to.
	 */
	public static final SystemMessageId SELECT_COMBAT_UNIT;
	
	/**
	 * ID: 1907<br>
	 * Message: Select the character who will replace the current character.
	 */
	public static final SystemMessageId SELECT_CHARACTER_WHO_WILL;
	
	/**
	 * ID: 1908<br>
	 * Message: $c1 in a state which prevents summoning.
	 */
	public static final SystemMessageId C1_STATE_FORBIDS_SUMMONING;
	
	/**
	 * ID: 1909<br>
	 * Message: ==< List of Academy Graduates During the Past Week >==
	 */
	public static final SystemMessageId ACADEMY_LIST_HEADER;
	
	/**
	 * ID: 1910<br>
	 * Message: Graduates: $c1.
	 */
	public static final SystemMessageId GRADUATES_C1;
	
	/**
	 * ID: 1911<br>
	 * Message: You cannot summon players who are currently participating in the Grand Olympiad.
	 */
	public static final SystemMessageId YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD;
	
	/**
	 * ID: 1912<br>
	 * Message: Only those requesting NCOTP should make an entry into this field.
	 */
	public static final SystemMessageId NCOTP2;
	
	/**
	 * ID: 1913<br>
	 * Message: The remaining recycle time for $s1 is $s2 minute(s).
	 */
	public static final SystemMessageId TIME_FOR_S1_IS_S2_MINUTES_REMAINING;
	
	/**
	 * ID: 1914<br>
	 * Message: The remaining recycle time for $s1 is $s2 seconds(s).
	 */
	public static final SystemMessageId TIME_FOR_S1_IS_S2_SECONDS_REMAINING;
	
	/**
	 * ID: 1915<br>
	 * Message: The game will end in $s1 second(s).
	 */
	public static final SystemMessageId GAME_ENDS_IN_S1_SECONDS;
	
	/**
	 * ID: 1916<br>
	 * Message: Your Death Penalty is now level $s1.
	 */
	public static final SystemMessageId DEATH_PENALTY_LEVEL_S1_ADDED;
	
	/**
	 * ID: 1917<br>
	 * Message: Your Death Penalty has been lifted.
	 */
	public static final SystemMessageId DEATH_PENALTY_LIFTED;
	
	/**
	 * ID: 1918<br>
	 * Message: Your pet is too high level to control.
	 */
	public static final SystemMessageId PET_TOO_HIGH_TO_CONTROL;
	
	/**
	 * ID: 1919<br>
	 * Message: The Grand Olympiad registration period has ended.
	 */
	public static final SystemMessageId OLYMPIAD_REGISTRATION_PERIOD_ENDED;
	
	/**
	 * ID: 1920<br>
	 * Message: Your account is currently inactive because you have not logged into the game for some time. You may reactivate your account by visiting the PlayNC website (http://www.plaync.com/us/support/).
	 */
	public static final SystemMessageId ACCOUNT_INACTIVITY;
	
	/**
	 * ID: 1921<br>
	 * Message: $s2 hour(s) and $s3 minute(s) have passed since $s1 has killed.
	 */
	public static final SystemMessageId S2_HOURS_S3_MINUTES_SINCE_S1_KILLED;
	
	/**
	 * ID: 1922<br>
	 * Message: Because $s1 has failed to kill for one full day, it has expired.
	 */
	public static final SystemMessageId S1_FAILED_KILLING_EXPIRED;
	
	/**
	 * ID: 1923<br>
	 * Message: Court Magician: The portal has been created!
	 */
	public static final SystemMessageId COURT_MAGICIAN_CREATED_PORTAL;
	
	/**
	 * ID: 1924<br>
	 * Message: Current Location: $s1, $s2, $s3 (Near the Primeval Isle)
	 */
	public static final SystemMessageId LOC_PRIMEVAL_ISLE_S1_S2_S3;
	
	/**
	 * ID: 1925<br>
	 * Message: Due to the affects of the Seal of Strife, it is not possible to summon at this time.
	 */
	public static final SystemMessageId SEAL_OF_STRIFE_FORBIDS_SUMMONING;
	
	/**
	 * ID: 1926<br>
	 * Message: There is no opponent to receive your challenge for a duel.
	 */
	public static final SystemMessageId THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
	
	/**
	 * ID: 1927<br>
	 * Message: $c1 has been challenged to a duel.
	 */
	public static final SystemMessageId C1_HAS_BEEN_CHALLENGED_TO_A_DUEL;
	
	/**
	 * ID: 1928<br>
	 * Message: $c1's party has been challenged to a duel.
	 */
	public static final SystemMessageId C1_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL;
	
	/**
	 * ID: 1929<br>
	 * Message: $c1 has accepted your challenge to a duel. The duel will begin in a few moments.
	 */
	public static final SystemMessageId C1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS;
	
	/**
	 * ID: 1930<br>
	 * Message: You have accepted $c1's challenge to a duel. The duel will begin in a few moments.
	 */
	public static final SystemMessageId YOU_HAVE_ACCEPTED_C1_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS;
	
	/**
	 * ID: 1931<br>
	 * Message: $c1 has declined your challenge to a duel.
	 */
	public static final SystemMessageId C1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL;
	
	/**
	 * ID: 1932<br>
	 * Message: $c1 has declined your challenge to a duel.
	 */
	public static final SystemMessageId C1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL2;
	
	/**
	 * ID: 1933<br>
	 * Message: You have accepted $c1's challenge to a party duel. The duel will begin in a few moments.
	 */
	public static final SystemMessageId YOU_HAVE_ACCEPTED_C1_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS;
	
	/**
	 * ID: 1934<br>
	 * Message: $s1 has accepted your challenge to duel against their party. The duel will begin in a few moments.
	 */
	public static final SystemMessageId S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS;
	
	/**
	 * ID: 1935<br>
	 * Message: $c1 has declined your challenge to a party duel.
	 */
	public static final SystemMessageId C1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_PARTY_DUEL;
	
	/**
	 * ID: 1936<br>
	 * Message: The opposing party has declined your challenge to a duel.
	 */
	public static final SystemMessageId THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL;
	
	/**
	 * ID: 1937<br>
	 * Message: Since the person you challenged is not currently in a party, they cannot duel against your party.
	 */
	public static final SystemMessageId SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY;
	
	/**
	 * ID: 1938<br>
	 * Message: $c1 has challenged you to a duel.
	 */
	public static final SystemMessageId C1_HAS_CHALLENGED_YOU_TO_A_DUEL;
	
	/**
	 * ID: 1939<br>
	 * Message: $c1's party has challenged your party to a duel.
	 */
	public static final SystemMessageId C1_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL;
	
	/**
	 * ID: 1940<br>
	 * Message: You are unable to request a duel at this time.
	 */
	public static final SystemMessageId YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME;
	
	/**
	 * ID: 1941<br>
	 * Message: This is no suitable place to challenge anyone or party to a duel.
	 */
	public static final SystemMessageId NO_PLACE_FOR_DUEL;
	
	/**
	 * ID: 1942<br>
	 * Message: The opposing party is currently unable to accept a challenge to a duel.
	 */
	public static final SystemMessageId THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL;
	
	/**
	 * ID: 1943<br>
	 * Message: The opposing party is currently not in a suitable location for a duel.
	 */
	public static final SystemMessageId THE_OPPOSING_PARTY_IS_AT_BAD_LOCATION_FOR_A_DUEL;
	
	/**
	 * ID: 1944<br>
	 * Message: In a moment, you will be transported to the site where the duel will take place.
	 */
	public static final SystemMessageId IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE;
	
	/**
	 * ID: 1945<br>
	 * Message: The duel will begin in $s1 second(s).
	 */
	public static final SystemMessageId THE_DUEL_WILL_BEGIN_IN_S1_SECONDS;
	
	/**
	 * ID: 1946<br>
	 * Message: $c1 has challenged you to a duel. Will you accept?
	 */
	public static final SystemMessageId C1_CHALLENGED_YOU_TO_A_DUEL;
	
	/**
	 * ID: 1947<br>
	 * Message: $c1's party has challenged your party to a duel. Will you accept?
	 */
	public static final SystemMessageId C1_CHALLENGED_YOU_TO_A_PARTY_DUEL;
	
	/**
	 * ID: 1948<br>
	 * Message: The duel will begin in $s1 second(s).
	 */
	public static final SystemMessageId THE_DUEL_WILL_BEGIN_IN_S1_SECONDS2;
	
	/**
	 * ID: 1949<br>
	 * Message: Let the duel begin!
	 */
	public static final SystemMessageId LET_THE_DUEL_BEGIN;
	
	/**
	 * ID: 1950<br>
	 * Message: $c1 has won the duel.
	 */
	public static final SystemMessageId C1_HAS_WON_THE_DUEL;
	
	/**
	 * ID: 1951<br>
	 * Message: $c1's party has won the duel.
	 */
	public static final SystemMessageId C1_PARTY_HAS_WON_THE_DUEL;
	
	/**
	 * ID: 1952<br>
	 * Message: The duel has ended in a tie.
	 */
	public static final SystemMessageId THE_DUEL_HAS_ENDED_IN_A_TIE;
	
	/**
	 * ID: 1953<br>
	 * Message: Since $c1 was disqualified, $s2 has won.
	 */
	public static final SystemMessageId SINCE_C1_WAS_DISQUALIFIED_S2_HAS_WON;
	
	/**
	 * ID: 1954<br>
	 * Message: Since $c1's party was disqualified, $s2's party has won.
	 */
	public static final SystemMessageId SINCE_C1_PARTY_WAS_DISQUALIFIED_S2_PARTY_HAS_WON;
	
	/**
	 * ID: 1955<br>
	 * Message: Since $c1 withdrew from the duel, $s2 has won.
	 */
	public static final SystemMessageId SINCE_C1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON;
	
	/**
	 * ID: 1956<br>
	 * Message: Since $c1's party withdrew from the duel, $s2's party has won.
	 */
	public static final SystemMessageId SINCE_C1_PARTY_WITHDREW_FROM_THE_DUEL_S2_PARTY_HAS_WON;
	
	/**
	 * ID: 1957<br>
	 * Message: Select the item to be augmented.
	 */
	public static final SystemMessageId SELECT_THE_ITEM_TO_BE_AUGMENTED;
	
	/**
	 * ID: 1958<br>
	 * Message: Select the catalyst for augmentation.
	 */
	public static final SystemMessageId SELECT_THE_CATALYST_FOR_AUGMENTATION;
	
	/**
	 * ID: 1959<br>
	 * Message: Requires $s1 $s2.
	 */
	public static final SystemMessageId REQUIRES_S1_S2;
	
	/**
	 * ID: 1960<br>
	 * Message: This is not a suitable item.
	 */
	public static final SystemMessageId THIS_IS_NOT_A_SUITABLE_ITEM;
	
	/**
	 * ID: 1961<br>
	 * Message: Gemstone quantity is incorrect.
	 */
	public static final SystemMessageId GEMSTONE_QUANTITY_IS_INCORRECT;
	
	/**
	 * ID: 1962<br>
	 * Message: The item was successfully augmented!
	 */
	public static final SystemMessageId THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED;
	
	/**
	 * ID: 1963<br>
	 * Message: Select the item from which you wish to remove augmentation.
	 */
	public static final SystemMessageId SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION;
	
	/**
	 * ID: 1964<br>
	 * Message: Augmentation removal can only be done on an augmented item.
	 */
	public static final SystemMessageId AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM;
	
	/**
	 * ID: 1965<br>
	 * Message: Augmentation has been successfully removed from your $s1.
	 */
	public static final SystemMessageId AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1;
	
	/**
	 * ID: 1966<br>
	 * Message: Only the clan leader may issue commands.
	 */
	public static final SystemMessageId ONLY_CLAN_LEADER_CAN_ISSUE_COMMANDS;
	
	/**
	 * ID: 1967<br>
	 * Message: The gate is firmly locked. Please try again later.
	 */
	public static final SystemMessageId GATE_LOCKED_TRY_AGAIN_LATER;
	
	/**
	 * ID: 1968<br>
	 * Message: $s1's owner.
	 */
	public static final SystemMessageId S1_OWNER;
	
	/**
	 * ID: 1969<br>
	 * Message: Area where $s1 appears.
	 */
	public static final SystemMessageId AREA_S1_APPEARS;
	
	/**
	 * ID: 1970<br>
	 * Message: Once an item is augmented, it cannot be augmented again.
	 */
	public static final SystemMessageId ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN;
	
	/**
	 * ID: 1971<br>
	 * Message: The level of the hardener is too high to be used.
	 */
	public static final SystemMessageId HARDENER_LEVEL_TOO_HIGH;
	
	/**
	 * ID: 1972<br>
	 * Message: You cannot augment items while a private store or private workshop is in operation.
	 */
	public static final SystemMessageId YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION;
	
	/**
	 * ID: 1973<br>
	 * Message: You cannot augment items while frozen.
	 */
	public static final SystemMessageId YOU_CANNOT_AUGMENT_ITEMS_WHILE_FROZEN;
	
	/**
	 * ID: 1974<br>
	 * Message: You cannot augment items while dead.
	 */
	public static final SystemMessageId YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD;
	
	/**
	 * ID: 1975<br>
	 * Message: You cannot augment items while engaged in trade activities.
	 */
	public static final SystemMessageId YOU_CANNOT_AUGMENT_ITEMS_WHILE_TRADING;
	
	/**
	 * ID: 1976<br>
	 * Message: You cannot augment items while paralyzed.
	 */
	public static final SystemMessageId YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED;
	
	/**
	 * ID: 1977<br>
	 * Message: You cannot augment items while fishing.
	 */
	public static final SystemMessageId YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING;
	
	/**
	 * ID: 1978<br>
	 * Message: You cannot augment items while sitting down.
	 */
	public static final SystemMessageId YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN;
	
	/**
	 * ID: 1979<br>
	 * Message: $s1's remaining Mana is now 10.
	 */
	public static final SystemMessageId S1S_REMAINING_MANA_IS_NOW_10;
	
	/**
	 * ID: 1980<br>
	 * Message: $s1's remaining Mana is now 5.
	 */
	public static final SystemMessageId S1S_REMAINING_MANA_IS_NOW_5;
	
	/**
	 * ID: 1981<br>
	 * Message: $s1's remaining Mana is now 1. It will disappear soon.
	 */
	public static final SystemMessageId S1S_REMAINING_MANA_IS_NOW_1;
	
	/**
	 * ID: 1982<br>
	 * Message: $s1's remaining Mana is now 0, and the item has disappeared.
	 */
	public static final SystemMessageId S1S_REMAINING_MANA_IS_NOW_0;
	
	/**
	 * ID: 1984<br>
	 * Message: Press the Augment button to begin.
	 */
	public static final SystemMessageId PRESS_THE_AUGMENT_BUTTON_TO_BEGIN;
	
	/**
	 * ID: 1985<br>
	 * Message: $s1's drop area ($s2)
	 */
	public static final SystemMessageId S1_DROP_AREA_S2;
	
	/**
	 * ID: 1986<br>
	 * Message: $s1's owner ($s2)
	 */
	public static final SystemMessageId S1_OWNER_S2;
	
	/**
	 * ID: 1987<br>
	 * Message: $s1
	 */
	public static final SystemMessageId S1;
	
	/**
	 * ID: 1988<br>
	 * Message: The ferry has arrived at Primeval Isle.
	 */
	public static final SystemMessageId FERRY_ARRIVED_AT_PRIMEVAL;
	
	/**
	 * ID: 1989<br>
	 * Message: The ferry will leave for Rune Harbor after anchoring for three minutes.
	 */
	public static final SystemMessageId FERRY_LEAVING_FOR_RUNE_3_MINUTES;
	
	/**
	 * ID: 1990<br>
	 * Message: The ferry is now departing Primeval Isle for Rune Harbor.
	 */
	public static final SystemMessageId FERRY_LEAVING_PRIMEVAL_FOR_RUNE_NOW;
	
	/**
	 * ID: 1991<br>
	 * Message: The ferry will leave for Primeval Isle after anchoring for three minutes.
	 */
	public static final SystemMessageId FERRY_LEAVING_FOR_PRIMEVAL_3_MINUTES;
	
	/**
	 * ID: 1992<br>
	 * Message: The ferry is now departing Rune Harbor for Primeval Isle.
	 */
	public static final SystemMessageId FERRY_LEAVING_RUNE_FOR_PRIMEVAL_NOW;
	
	/**
	 * ID: 1993<br>
	 * Message: The ferry from Primeval Isle to Rune Harbor has been delayed.
	 */
	public static final SystemMessageId FERRY_FROM_PRIMEVAL_TO_RUNE_DELAYED;
	
	/**
	 * ID: 1994<br>
	 * Message: The ferry from Rune Harbor to Primeval Isle has been delayed.
	 */
	public static final SystemMessageId FERRY_FROM_RUNE_TO_PRIMEVAL_DELAYED;
	
	/**
	 * ID: 1995<br>
	 * Message: $s1 channel filtering option
	 */
	public static final SystemMessageId S1_CHANNEL_FILTER_OPTION;
	
	/**
	 * ID: 1996<br>
	 * Message: The attack has been blocked.
	 */
	public static final SystemMessageId ATTACK_WAS_BLOCKED;
	
	/**
	 * ID: 1997<br>
	 * Message: $c1 is performing a counterattack.
	 */
	public static final SystemMessageId C1_PERFORMING_COUNTERATTACK;
	
	/**
	 * ID: 1998<br>
	 * Message: You countered $c1's attack.
	 */
	public static final SystemMessageId COUNTERED_C1_ATTACK;
	
	/**
	 * ID: 1999<br>
	 * Message: $c1 dodges the attack.
	 */
	public static final SystemMessageId C1_DODGES_ATTACK;
	
	/**
	 * ID: 2000<br>
	 * Message: You have avoided $c1's attack.
	 */
	public static final SystemMessageId AVOIDED_C1_ATTACK2;
	
	/**
	 * ID: 2001<br>
	 * Message: Augmentation failed due to inappropriate conditions.
	 */
	public static final SystemMessageId AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS;
	
	/**
	 * ID: 2002<br>
	 * Message: Trap failed.
	 */
	public static final SystemMessageId TRAP_FAILED;
	
	/**
	 * ID: 2003<br>
	 * Message: You obtained an ordinary material.
	 */
	public static final SystemMessageId OBTAINED_ORDINARY_MATERIAL;
	
	/**
	 * ID: 2004<br>
	 * Message: You obtained a rare material.
	 */
	public static final SystemMessageId OBTAINED_RATE_MATERIAL;
	
	/**
	 * ID: 2005<br>
	 * Message: You obtained a unique material.
	 */
	public static final SystemMessageId OBTAINED_UNIQUE_MATERIAL;
	
	/**
	 * ID: 2006<br>
	 * Message: You obtained the only material of this kind.
	 */
	public static final SystemMessageId OBTAINED_ONLY_MATERIAL;
	
	/**
	 * ID: 2007<br>
	 * Message: Please enter the recipient's name.
	 */
	public static final SystemMessageId ENTER_RECIPIENTS_NAME;
	
	/**
	 * ID: 2008<br>
	 * Message: Please enter the text.
	 */
	public static final SystemMessageId ENTER_TEXT;
	
	/**
	 * ID: 2009<br>
	 * Message: You cannot exceed 1500 characters.
	 */
	public static final SystemMessageId CANT_EXCEED_1500_CHARACTERS;
	
	/**
	 * ID: 2010<br>
	 * Message: $s2 $s1
	 */
	public static final SystemMessageId S2_S1;
	
	/**
	 * ID: 2011<br>
	 * Message: The augmented item cannot be discarded.
	 */
	public static final SystemMessageId AUGMENTED_ITEM_CANNOT_BE_DISCARDED;
	
	/**
	 * ID: 2012<br>
	 * Message: $s1 has been activated.
	 */
	public static final SystemMessageId S1_HAS_BEEN_ACTIVATED;
	
	/**
	 * ID: 2013<br>
	 * Message: Your seed or remaining purchase amount is inadequate.
	 */
	public static final SystemMessageId YOUR_SEED_OR_REMAINING_PURCHASE_AMOUNT_IS_INADEQUATE;
	
	/**
	 * ID: 2014<br>
	 * Message: You cannot proceed because the manor cannot accept any more crops. All crops have been returned and no adena withdrawn.
	 */
	public static final SystemMessageId MANOR_CANT_ACCEPT_MORE_CROPS;
	
	/**
	 * ID: 2015<br>
	 * Message: A skill is ready to be used again.
	 */
	public static final SystemMessageId SKILL_READY_TO_USE_AGAIN;
	
	/**
	 * ID: 2016<br>
	 * Message: A skill is ready to be used again but its re-use counter time has increased.
	 */
	public static final SystemMessageId SKILL_READY_TO_USE_AGAIN_BUT_TIME_INCREASED;
	
	/**
	 * ID: 2017<br>
	 * Message: $c1 cannot duel because $c1 is currently engaged in a private store or manufacture.
	 */
	public static final SystemMessageId C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
	
	/**
	 * ID: 2018<br>
	 * Message: $c1 cannot duel because $c1 is currently fishing.
	 */
	public static final SystemMessageId C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_FISHING;
	
	/**
	 * ID: 2019<br>
	 * Message: $c1 cannot duel because $c1's HP or MP is below 50%.
	 */
	public static final SystemMessageId C1_CANNOT_DUEL_BECAUSE_C1_HP_OR_MP_IS_BELOW_50_PERCENT;
	
	/**
	 * ID: 2020<br>
	 * Message: $c1 cannot make a challenge to a duel because $c1 is currently in a duel-prohibited area (Peaceful Zone / Seven Signs Zone / Near Water / Restart Prohibited Area).
	 */
	public static final SystemMessageId C1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_C1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA;
	
	/**
	 * ID: 2021<br>
	 * Message: $c1 cannot duel because $c1 is currently engaged in battle.
	 */
	public static final SystemMessageId C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE;
	
	/**
	 * ID: 2022<br>
	 * Message: $c1 cannot duel because $c1 is already engaged in a duel.
	 */
	public static final SystemMessageId C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL;
	
	/**
	 * ID: 2023<br>
	 * Message: $c1 cannot duel because $c1 is in a chaotic state.
	 */
	public static final SystemMessageId C1_CANNOT_DUEL_BECAUSE_C1_IS_IN_A_CHAOTIC_STATE;
	
	/**
	 * ID: 2024<br>
	 * Message: $c1 cannot duel because $c1 is participating in the Olympiad.
	 */
	public static final SystemMessageId C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_THE_OLYMPIAD;
	
	/**
	 * ID: 2025<br>
	 * Message: $c1 cannot duel because $c1 is participating in a clan hall war.
	 */
	public static final SystemMessageId C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_A_CLAN_HALL_WAR;
	
	/**
	 * ID: 2026<br>
	 * Message: $c1 cannot duel because $c1 is participating in a siege war.
	 */
	public static final SystemMessageId C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_A_SIEGE_WAR;
	
	/**
	 * ID: 2027<br>
	 * Message: $c1 cannot duel because $c1 is currently riding a boat, steed, or strider.
	 */
	public static final SystemMessageId C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_RIDING_A_BOAT_STEED_OR_STRIDER;
	
	/**
	 * ID: 2028<br>
	 * Message: $c1 cannot receive a duel challenge because $c1 is too far away.
	 */
	public static final SystemMessageId C1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_C1_IS_TOO_FAR_AWAY;
	
	/**
	 * ID: 2029<br>
	 * Message: $c1 is currently teleporting and cannot participate in the Olympiad.
	 */
	public static final SystemMessageId C1_CANNOT_PARTICIPATE_IN_OLYMPIAD_DURING_TELEPORT;
	
	/**
	 * ID: 2030<br>
	 * Message: You are currently logging in.
	 */
	public static final SystemMessageId CURRENTLY_LOGGING_IN;
	
	/**
	 * ID: 2031<br>
	 * Message: Please wait a moment.
	 */
	public static final SystemMessageId PLEASE_WAIT_A_MOMENT;
	
	/**
	 * ID: 2032<br>
	 * Message: It is not the right time for purchasing the item.
	 */
	public static final SystemMessageId NOT_TIME_TO_PURCHASE_ITEM;
	
	/**
	 * ID: 2033<br>
	 * Message: A sub-class cannot be created or changed because you have exceeded your inventory limit.
	 */
	public static final SystemMessageId NOT_SUBCLASS_WHILE_INVENTORY_FULL;
	
	/**
	 * ID: 2034<br>
	 * Message: There are $s1 hour(s) and $s2 minute(s) remaining until the time when the item can be purchased.
	 */
	public static final SystemMessageId ITEM_PURCHASABLE_IN_S1_HOURS_S2_MINUTES;
	
	/**
	 * ID: 2035<br>
	 * Message: There are $s1 minute(s) remaining until the time when the item can be purchased.
	 */
	public static final SystemMessageId ITEM_PURCHASABLE_IN_S1_MINUTES;
	
	/**
	 * ID: 2036<br>
	 * Message: Unable to invite because the party is locked.
	 */
	public static final SystemMessageId NO_INVITE_PARTY_LOCKED;
	
	/**
	 * ID: 2037<br>
	 * Message: Unable to create character. You are unable to create a new character on the selected server. A restriction is in place which restricts users from creating characters on different servers where no previous characters exists. Please choose another server.
	 */
	public static final SystemMessageId CANT_CREATE_CHARACTER_DURING_RESTRICTION;
	
	/**
	 * ID: 2038<br>
	 * Message: Some Lineage II features have been limited for free trials. Trial accounts aren't allowed to drop items and/or Adena. To unlock all of the features of Lineage II, purchase the full version today.
	 */
	public static final SystemMessageId ACCOUNT_CANT_DROP_ITEMS;
	
	/**
	 * ID: 2039<br>
	 * Message: Some Lineage II features have been limited for free trials. Trial accounts aren't allowed to trade items and/or Adena. To unlock all of the features of Lineage II, purchase the full version today.
	 */
	public static final SystemMessageId ACCOUNT_CANT_TRADE_ITEMS;
	
	/**
	 * ID: 2040<br>
	 * Message: Cannot trade items with the targeted user.
	 */
	public static final SystemMessageId CANT_TRADE_WITH_TARGET;
	
	/**
	 * ID: 2041<br>
	 * Message: Some Lineage II features have been limited for free trials. Trial accounts aren't allowed to setup private stores. To unlock all of the features of Lineage II, purchase the full version today.
	 */
	public static final SystemMessageId CANT_OPEN_PRIVATE_STORE;
	
	/**
	 * ID: 2042<br>
	 * Message: This account has been suspended for non-payment based on the cell phone payment agreement. Please submit proof of payment by fax (02-2186-3499) and contact customer service at 1600-0020.
	 */
	public static final SystemMessageId ILLEGAL_USE23;
	
	/**
	 * ID: 2043<br>
	 * Message: You have exceeded your inventory volume limit and may not take this quest item. Please make room in your inventory and try again
	 */
	public static final SystemMessageId YOU_HAVE_EXCEEDED_YOUR_INVENTORY_VOLUME_LIMIT_AND_CANNOT_TAKE_THIS_QUESTITEM;
	
	/**
	 * ID: 2044<br>
	 * Message: Some Lineage II features have been limited for free trials. Trial accounts aren't allowed to set up private manufacturing stores. To unlock all of the features of Lineage II, purchase the full version today.
	 */
	public static final SystemMessageId CANT_SETUP_PRIVATE_WORKSHOP;
	
	/**
	 * ID: 2045<br>
	 * Message: Some Lineage II features have been limited for free trials. Trial accounts aren't allowed to use private manufacturing stores. To unlock all of the features of Lineage II, purchase the full version today.
	 */
	public static final SystemMessageId CANT_USE_PRIVATE_WORKSHOP;
	
	/**
	 * ID: 2046<br>
	 * Message: Some Lineage II features have been limited for free trials. Trial accounts aren't allowed buy items from private stores. To unlock all of the features of Lineage II, purchase the full version today.
	 */
	public static final SystemMessageId CANT_USE_PRIVATE_STORES;
	
	/**
	 * ID: 2047<br>
	 * Message: Some Lineage II features have been limited for free trials. Trial accounts aren't allowed to access clan warehouses. To unlock all of the features of Lineage II, purchase the full version today.
	 */
	public static final SystemMessageId CANT_USE_CLAN_WH;
	
	/**
	 * ID: 2048<br>
	 * Message: The shortcut in use conflicts with $s1. Do you wish to reset the conflicting shortcuts and use the saved shortcut?
	 */
	public static final SystemMessageId CONFLICTING_SHORTCUT;
	
	/**
	 * ID: 2049<br>
	 * Message: The shortcut will be applied and saved in the server. Will you continue?
	 */
	public static final SystemMessageId CONFIRM_SHORTCUT_WILL_SAVED_ON_SERVER;
	
	/**
	 * ID: 2050<br>
	 * Message: $s1 Blood Pledge is trying to display a flag.
	 */
	public static final SystemMessageId S1_TRYING_RAISE_FLAG;
	
	/**
	 * ID: 2051<br>
	 * Message: You must accept the User Agreement before this account can access Lineage II.
	 */
	public static final SystemMessageId MUST_ACCEPT_AGREEMENT;
	
	/**
	 * ID: 2052<br>
	 * Message: A guardian's consent is required before this account can be used to play Lineage II.
	 */
	public static final SystemMessageId NEED_CONSENT_TO_PLAY_THIS_ACCOUNT;
	
	/**
	 * ID: 2053<br>
	 * Message: This account has declined the User Agreement or is pending a withdrawl request.
	 */
	public static final SystemMessageId ACCOUNT_DECLINED_AGREEMENT_OR_PENDING;
	
	/**
	 * ID: 2054<br>
	 * Message: This account has been suspended.
	 */
	public static final SystemMessageId ACCOUNT_SUSPENDED;
	
	/**
	 * ID: 2055<br>
	 * Message: Your account has been suspended from all game services.
	 */
	public static final SystemMessageId ACCOUNT_SUSPENDED_FROM_ALL_SERVICES;
	
	/**
	 * ID: 2056<br>
	 * Message: Your account has been converted to an integrated account, and is unable to be accessed.
	 */
	public static final SystemMessageId ACCOUNT_CONVERTED;
	
	/**
	 * ID: 2057<br>
	 * Message: You have blocked $c1.
	 */
	public static final SystemMessageId BLOCKED_C1;
	
	/**
	 * ID: 2058<br>
	 * Message: You are already polymorphed and cannot polymorph again.
	 */
	public static final SystemMessageId YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN;
	
	/**
	 * ID: 2059<br>
	 * Message: The nearby area is too narrow for you to polymorph. Please move to another area and try to polymorph again.
	 */
	public static final SystemMessageId AREA_UNSUITABLE_FOR_POLYMORPH;
	
	/**
	 * ID: 2060<br>
	 * Message: You cannot polymorph into the desired form in water.
	 */
	public static final SystemMessageId YOU_CANNOT_POLYMORPH_INTO_THE_DESIRED_FORM_IN_WATER;
	
	/**
	 * ID: 2061<br>
	 * Message: You are still under transform penalty and cannot be polymorphed.
	 */
	public static final SystemMessageId CANT_MORPH_DUE_TO_MORPH_PENALTY;
	
	/**
	 * ID: 2062<br>
	 * Message: You cannot polymorph when you have summoned a servitor/pet.
	 */
	public static final SystemMessageId YOU_CANNOT_POLYMORPH_WHEN_YOU_HAVE_SUMMONED_A_SERVITOR;
	
	/**
	 * ID: 2063<br>
	 * Message: You cannot polymorph while riding a pet.
	 */
	public static final SystemMessageId YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_PET;
	
	/**
	 * ID: 2064<br>
	 * Message: You cannot polymorph while under the effect of a special skill
	 */
	public static final SystemMessageId CANT_MORPH_WHILE_UNDER_SPECIAL_SKILL_EFFECT;
	
	/**
	 * ID: 2065<br>
	 * Message: That item cannot be taken off
	 */
	public static final SystemMessageId ITEM_CANNOT_BE_TAKEN_OFF;
	
	/**
	 * ID: 2066<br>
	 * Message: That weapon cannot perform any attacks.
	 */
	public static final SystemMessageId THAT_WEAPON_CANT_ATTACK;
	
	/**
	 * ID: 2067<br>
	 * Message: That weapon cannot use any other skill except the weapon's skill.
	 */
	public static final SystemMessageId WEAPON_CAN_USE_ONLY_WEAPON_SKILL;
	
	/**
	 * ID: 2068<br>
	 * Message: You do not have all of the items needed to untrain the enchant skill.
	 */
	public static final SystemMessageId YOU_DONT_HAVE_ALL_ITENS_NEEDED_TO_UNTRAIN_SKILL_ENCHANT;
	
	/**
	 * ID: 2069<br>
	 * Message: Untrain of enchant skill was successful. Current level of enchant skill $s1 has been decreased by 1.
	 */
	public static final SystemMessageId UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_DECREASED_BY_ONE;
	
	/**
	 * ID: 2070<br>
	 * Message: Untrain of enchant skill was successful. Current level of enchant skill $s1 became 0 and enchant skill will be initialized.
	 */
	public static final SystemMessageId UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_RESETED;
	
	/**
	 * ID: 2071<br>
	 * Message: You do not have all of the items needed to enchant skill route change.
	 */
	public static final SystemMessageId YOU_DONT_HAVE_ALL_ITENS_NEEDED_TO_CHANGE_SKILL_ENCHANT_ROUTE;
	
	/**
	 * ID: 2072<br>
	 * Message: Enchant skill route change was successful. Lv of enchant skill $s1 has been decreased by $s2.
	 */
	public static final SystemMessageId SKILL_ENCHANT_CHANGE_SUCCESSFUL_S1_LEVEL_WAS_DECREASED_BY_S2;
	
	/**
	 * ID: 2073<br>
	 * Message: Enchant skill route change was successful. Lv of enchant skill $s1 will remain.
	 */
	public static final SystemMessageId SKILL_ENCHANT_CHANGE_SUCCESSFUL_S1_LEVEL_WILL_REMAIN;
	
	/**
	 * ID: 2074<br>
	 * Message: Skill enchant failed. Current level of enchant skill $s1 will remain unchanged.
	 */
	public static final SystemMessageId SKILL_ENCHANT_FAILED_S1_LEVEL_WILL_REMAIN;
	
	/**
	 * ID: 2075<br>
	 * Message: It is not auction period.
	 */
	public static final SystemMessageId NO_AUCTION_PERIOD;
	
	/**
	 * ID: 2076<br>
	 * Message: Bidding is not allowed because the maximum bidding price exceeds 100 billion.
	 */
	public static final SystemMessageId BID_CANT_EXCEED_100_BILLION;
	
	/**
	 * ID: 2077<br>
	 * Message: Your bid must be higher than the current highest bid.
	 */
	public static final SystemMessageId BID_MUST_BE_HIGHER_THAN_CURRENT_BID;
	
	/**
	 * ID: 2078<br>
	 * Message: You do not have enough adena for this bid.
	 */
	public static final SystemMessageId NOT_ENOUGH_ADENA_FOR_THIS_BID;
	
	/**
	 * ID: 2079<br>
	 * Message: You currently have the highest bid, but the reserve has not been met.
	 */
	public static final SystemMessageId HIGHEST_BID_BUT_RESERVE_NOT_MET;
	
	/**
	 * ID: 2080<br>
	 * Message: You have been outbid.
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_OUTBID;
	
	/**
	 * ID: 2081<br>
	 * Message: There are no funds presently due to you.
	 */
	public static final SystemMessageId NO_FUNDS_DUE;
	
	/**
	 * ID: 2082<br>
	 * Message: You have exceeded the total amount of adena allowed in inventory.
	 */
	public static final SystemMessageId EXCEEDED_MAX_ADENA_AMOUNT_IN_INVENTORY;
	
	/**
	 * ID: 2083<br>
	 * Message: The auction has begun.
	 */
	public static final SystemMessageId AUCTION_BEGUN;
	
	/**
	 * ID: 2084<br>
	 * Message: Enemy Blood Pledges have intruded into the fortress.
	 */
	public static final SystemMessageId ENEMIES_INTRUDED_FORTRESS;
	
	/**
	 * ID: 2085<br>
	 * Message: Shout and trade chatting cannot be used while possessing a cursed weapon.
	 */
	public static final SystemMessageId SHOUT_AND_TRADE_CHAT_CANNOT_BE_USED_WHILE_POSSESSING_CURSED_WEAPON;
	
	/**
	 * ID: 2086<br>
	 * Message: Search on user $s2 for third-party program use will be completed in $s1 minute(s).
	 */
	public static final SystemMessageId SEARCH_ON_S2_FOR_BOT_USE_COMPLETED_IN_S1_MINUTES;
	
	/**
	 * ID: 2087<br>
	 * Message: A fortress is under attack!
	 */
	public static final SystemMessageId A_FORTRESS_IS_UNDER_ATTACK;
	
	/**
	 * ID: 2088<br>
	 * Message: $s1 minute(s) until the fortress battle starts.
	 */
	public static final SystemMessageId S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS;
	
	/**
	 * ID: 2089<br>
	 * Message: $s1 minute(s) until the fortress battle starts.
	 */
	public static final SystemMessageId S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS;
	
	/**
	 * ID: 2090<br>
	 * Message: The fortress battle $s1 has begun.
	 */
	public static final SystemMessageId THE_FORTRESS_BATTLE_S1_HAS_BEGUN;
	
	/**
	 * ID: 2091<br>
	 * Message: Your account can only be used after changing your password and quiz.
	 */
	public static final SystemMessageId CHANGE_PASSWORT_FIRST;
	
	/**
	 * ID: 2092<br>
	 * Message: You cannot bid due to a passed-in price.
	 */
	public static final SystemMessageId CANNOT_BID_DUE_TO_PASSED_IN_PRICE;
	
	/**
	 * ID: 2093<br>
	 * Message: The passed-in price is $s1 adena. Would you like to return the passed-in price?
	 */
	public static final SystemMessageId PASSED_IN_PRICE_IS_S1_ADENA_WOULD_YOU_LIKE_TO_RETURN_IT;
	
	/**
	 * ID: 2094<br>
	 * Message: Another user is purchasing. Please try again later.
	 */
	public static final SystemMessageId ANOTHER_USER_PURCHASING_TRY_AGAIN_LATER;
	
	/**
	 * ID: 2095<br>
	 * Message: Some Lineage II features have been limited for free trials. Trial accounts have limited chatting capabilities. To unlock all of the features of Lineage II, purchase the full version today.
	 */
	public static final SystemMessageId ACCOUNT_CANNOT_SHOUT;
	
	/**
	 * ID: 2096<br>
	 * Message: $c1 is in a location which cannot be entered, therefore it cannot be processed.
	 */
	public static final SystemMessageId C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED;
	
	/**
	 * ID: 2097<br>
	 * Message: $c1's level requirement is not sufficient and cannot be entered.
	 */
	public static final SystemMessageId C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT;
	
	/**
	 * ID: 2098<br>
	 * Message: $c1's quest requirement is not sufficient and cannot be entered.
	 */
	public static final SystemMessageId C1_QUEST_REQUIREMENT_NOT_SUFFICIENT;
	
	/**
	 * ID: 2099<br>
	 * Message: $c1's item requirement is not sufficient and cannot be entered.
	 */
	public static final SystemMessageId C1_ITEM_REQUIREMENT_NOT_SUFFICIENT;
	
	/**
	 * ID: 2100<br>
	 * Message: $c1 may not re-enter yet.
	 */
	public static final SystemMessageId C1_MAY_NOT_REENTER_YET;
	
	/**
	 * ID: 2101<br>
	 * Message: You are not currently in a party, so you cannot enter.
	 */
	public static final SystemMessageId NOT_IN_PARTY_CANT_ENTER;
	
	/**
	 * ID: 2102<br>
	 * Message: You cannot enter due to the party having exceeded the limit.
	 */
	public static final SystemMessageId PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER;
	
	/**
	 * ID: 2103<br>
	 * Message: You cannot enter because you are not associated with the current command channel.
	 */
	public static final SystemMessageId NOT_IN_COMMAND_CHANNEL_CANT_ENTER;
	
	/**
	 * ID: 2104<br>
	 * Message: The maximum number of instance zones has been exceeded. You cannot enter.
	 */
	public static final SystemMessageId MAXIMUM_INSTANCE_ZONE_NUMBER_EXCEEDED_CANT_ENTER;
	
	/**
	 * ID: 2105<br>
	 * Message: You have entered another instance zone, therefore you cannot enter corresponding dungeon.
	 */
	public static final SystemMessageId ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER;
	
	/**
	 * ID: 2106<br>
	 * Message: This dungeon will expire in $s1 minute(s). You will be forced out of the dungeon when the time expires.
	 */
	public static final SystemMessageId DUNGEON_EXPIRES_IN_S1_MINUTES;
	
	/**
	 * ID: 2107<br>
	 * Message: This instance zone will be terminated in $s1 minute(s). You will be forced out of the dungeon when the time expires.
	 */
	public static final SystemMessageId INSTANCE_ZONE_TERMINATES_IN_S1_MINUTES;
	
	/**
	 * ID: 2108<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE24;
	
	/**
	 * ID: 2109<br>
	 * Message: The server has been integrated, and your character, $s1, has overlapped with another name. Please enter a new name for your character
	 */
	public static final SystemMessageId CHARACTER_NAME_OVERLAPPING_RENAME_CHARACTER;
	
	/**
	 * ID: 2110<br>
	 * Message: This character name already exists or is an invalid name. Please enter a new name
	 */
	public static final SystemMessageId CHARACTER_NAME_INVALID_RENAME_CHARACTER;
	
	/**
	 * ID: 2111<br>
	 * Message: Enter a shortcut to assign.
	 */
	public static final SystemMessageId ENTER_SHORTCUT_TO_ASSIGN;
	
	/**
	 * ID: 2112<br>
	 * Message: Sub-key can be CTRL, ALT, SHIFT and you may enter two sub-keys at a time.
	 */
	public static final SystemMessageId SUBKEY_EXPLANATION1;
	
	/**
	 * ID: 2113<br>
	 * Message: (Sub key explanation)
	 */
	public static final SystemMessageId SUBKEY_EXPLANATION2;
	
	/**
	 * ID: 2114<br>
	 * Message: Forced attack and stand-in-place attacks assigned previously to Ctrl and Shift will be changed to Alt + Q and Alt + E when set as expanded sub-key mode, and CTRL and SHIFT will be available to assign to another shortcut. Will you continue?
	 */
	public static final SystemMessageId SUBKEY_EXPLANATION3;
	
	/**
	 * ID: 2115<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE25;
	
	/**
	 * ID: 2116<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE26;
	
	/**
	 * ID: 2117<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE27;
	
	/**
	 * ID: 2118<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE28;
	
	/**
	 * ID: 2119<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE29;
	
	/**
	 * ID: 2120<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE30;
	
	/**
	 * ID: 2121<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE31;
	
	/**
	 * ID: 2122<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE32;
	
	/**
	 * ID: 2123<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE33;
	
	/**
	 * ID: 2124<br>
	 * Message: The server has been integrated, and your Clan name, $s1, has been overlapped with another name. Please enter the Clan name to be changed.
	 */
	public static final SystemMessageId CLAN_NAME_OVERLAPPING_RENAME_CLAN;
	
	/**
	 * ID: 2125<br>
	 * Message: This name already exists or is an invalid name. Please enter the Clan name to be changed.
	 */
	public static final SystemMessageId CLAN_NAME_INVALID_RENAME_CLAN;
	
	/**
	 * ID: 2126<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE34;
	
	/**
	 * ID: 2127<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE35;
	
	/**
	 * ID: 2128<br>
	 * Message: Your account has been suspended ...
	 */
	public static final SystemMessageId ILLEGAL_USE36;
	
	/**
	 * ID: 2129<br>
	 * Message: The augmented item cannot be converted. Please convert after the augmentation has been removed.
	 */
	public static final SystemMessageId AUGMENTED_ITEM_CANT_CONVERTED;
	
	/**
	 * ID: 2130<br>
	 * Message: You cannot convert this item.
	 */
	public static final SystemMessageId CANT_CONVERT_THIS_ITEM;
	
	/**
	 * ID: 2131<br>
	 * Message: You have bid the highest price and have won the item. The item can be found in your personal warehouse.
	 */
	public static final SystemMessageId WON_BID_ITEM_CAN_BE_FOUND_IN_WAREHOUSE;
	
	/**
	 * ID: 2132<br>
	 * Message: You have entered a common server.
	 */
	public static final SystemMessageId ENTERED_COMMON_SERVER;
	
	/**
	 * ID: 2133<br>
	 * Message: You have entered an adults-only server.
	 */
	public static final SystemMessageId ENTERED_ADULTS_ONLY_SERVER;
	
	/**
	 * ID: 2134<br>
	 * Message: You have entered a server for juveniles.
	 */
	public static final SystemMessageId ENTERED_JUVENILES_SERVER;
	
	/**
	 * ID: 2135<br>
	 * Message: Because of your Fatigue level, this is not allowed.
	 */
	public static final SystemMessageId NOT_ALLOWED_DUE_TO_FATIGUE_LEVEL;
	
	/**
	 * ID: 2136<br>
	 * Message: A clan name change application has been submitted.
	 */
	public static final SystemMessageId CLAN_NAME_CHANCE_PETITION_SUBMITTED;
	
	/**
	 * ID: 2137<br>
	 * Message: You are about to bid $s1 item with $s2 adena. Will you continue?
	 */
	public static final SystemMessageId CONFIRM_BID_S2_ADENA_FOR_S1_ITEM;
	
	/**
	 * ID: 2138<br>
	 * Message: Please enter a bid price.
	 */
	public static final SystemMessageId ENTER_BID_PRICE;
	
	/**
	 * ID: 2139<br>
	 * Message: $c1's Pet.
	 */
	public static final SystemMessageId C1_PET;
	
	/**
	 * ID: 2140<br>
	 * Message: $c1's Servitor.
	 */
	public static final SystemMessageId C1_SERVITOR;
	
	/**
	 * ID: 2141<br>
	 * Message: You slightly resisted $c1's magic.
	 */
	public static final SystemMessageId SLIGHTLY_RESISTED_C1_MAGICC;
	
	/**
	 * ID: 2142<br>
	 * Message: You cannot expel $c1 because $c1 is not a party member.
	 */
	public static final SystemMessageId CANT_EXPEL_C1_NOT_A_PARTY_MEMBER;
	
	/**
	 * ID: 2143<br>
	 * Message: You cannot add elemental power while operating a Private Store or Private Workshop.
	 */
	public static final SystemMessageId CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_PRIVATE_STORE_OR_WORKSHOP;
	
	/**
	 * ID: 2144<br>
	 * Message: Please select item to add elemental power.
	 */
	public static final SystemMessageId SELECT_ITEM_TO_ADD_ELEMENTAL_POWER;
	
	/**
	 * ID: 2145<br>
	 * Message: Attribute item usage has been cancelled.
	 */
	public static final SystemMessageId ELEMENTAL_ENHANCE_CANCELED;
	
	/**
	 * ID: 2146<br>
	 * Message: Elemental power enhancer usage requirement is not sufficient.
	 */
	public static final SystemMessageId ELEMENTAL_ENHANCE_REQUIREMENT_NOT_SUFFICIENT;
	
	/**
	 * ID: 2147<br>
	 * Message: $s2 elemental power has been added successfully to $s1.
	 */
	public static final SystemMessageId ELEMENTAL_POWER_S2_SUCCESSFULLY_ADDED_TO_S1;
	
	/**
	 * ID: 2148<br>
	 * Message: $s3 elemental power has been added successfully to +$s1 $s2.
	 */
	public static final SystemMessageId ELEMENTAL_POWER_S3_SUCCESSFULLY_ADDED_TO_S1_S2;
	
	/**
	 * ID: 2149<br>
	 * Message: You have failed to add elemental power.
	 */
	public static final SystemMessageId FAILED_ADDING_ELEMENTAL_POWER;
	
	/**
	 * ID: 2150<br>
	 * Message: Another elemental power has already been added. This elemental power cannot be added.
	 */
	public static final SystemMessageId ANOTHER_ELEMENTAL_POWER_ALREADY_ADDED;
	
	/**
	 * ID: 2151<br>
	 * Message: Your opponent has resistance to magic, the damage was decreased.
	 */
	public static final SystemMessageId OPPONENT_HAS_RESISTANCE_MAGIC_DAMAGE_DECREASED;
	
	/**
	 * ID: 2152<br>
	 * Message: The assigned shortcut will be deleted and the initial shortcut setting restored. Will you continue?
	 */
	public static final SystemMessageId CONFIRM_SHORCUT_DELETE;
	
	/**
	 * ID: 2153<br>
	 * Message: You are currently logged into 10 of your accounts and can no longer access your other accounts.
	 */
	public static final SystemMessageId MAXIMUM_ACCOUNT_LOGINS_REACHED;
	
	/**
	 * ID: 2154<br>
	 * Message: The target is not a flagpole so a flag cannot be displayed.
	 */
	public static final SystemMessageId THE_TARGET_IS_NOT_A_FLAGPOLE_SO_A_FLAG_CANNOT_BE_DISPLAYED;
	
	/**
	 * ID: 2155<br>
	 * Message: A flag is already being displayed, another flag cannot be displayed.
	 */
	public static final SystemMessageId A_FLAG_IS_ALREADY_BEING_DISPLAYED_ANOTHER_FLAG_CANNOT_BE_DISPLAYED;
	
	/**
	 * ID: 2156<br>
	 * Message: There are not enough necessary items to use the skill.
	 */
	public static final SystemMessageId THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL;
	
	/**
	 * ID: 2157<br>
	 * Message: Bid will be attempted with $s1 adena.
	 */
	public static final SystemMessageId BID_WILL_BE_ATTEMPTED_WITH_S1_ADENA;
	
	/**
	 * ID: 2158<br>
	 * Message: Force attack is impossible against a temporary allied member during a siege.
	 */
	public static final SystemMessageId FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS;
	
	/**
	 * ID: 2159<br>
	 * Message: Bidder exists, the auction time has been extended by 5 minutes.
	 */
	public static final SystemMessageId BIDDER_EXISTS_AUCTION_TIME_EXTENDED_BY_5_MINUTES;
	
	/**
	 * ID: 2160<br>
	 * Message: Bidder exists, the auction time has been extended by 3 minutes.
	 */
	public static final SystemMessageId BIDDER_EXISTS_AUCTION_TIME_EXTENDED_BY_3_MINUTES;
	
	/**
	 * ID: 2161<br>
	 * Message: There is not enough space to move, the skill cannot be used.
	 */
	public static final SystemMessageId NOT_ENOUGH_SPACE_FOR_SKILL;
	
	/**
	 * ID: 2162<br>
	 * Message: Your soul has increased by $s1, so it is now at $s2.
	 */
	public static final SystemMessageId YOUR_SOUL_HAS_INCREASED_BY_S1_SO_IT_IS_NOW_AT_S2;
	
	/**
	 * ID: 2163<br>
	 * Message: Soul cannot be increased anymore.
	 */
	public static final SystemMessageId SOUL_CANNOT_BE_INCREASED_ANYMORE;
	
	/**
	 * ID: 2164<br>
	 * Message: The barracks have been seized.
	 */
	public static final SystemMessageId SEIZED_BARRACKS;
	
	/**
	 * ID: 2165<br>
	 * Message: The barracks function has been restored.
	 */
	public static final SystemMessageId BARRACKS_FUNCTION_RESTORED;
	
	/**
	 * ID: 2166<br>
	 * Message: All barracks are occupied.
	 */
	public static final SystemMessageId ALL_BARRACKS_OCCUPIED;
	
	/**
	 * ID: 2167<br>
	 * Message: A malicious skill cannot be used in a peace zone.
	 */
	public static final SystemMessageId A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_PEACE_ZONE;
	
	/**
	 * ID: 2168<br>
	 * Message: $c1 has acquired the flag.
	 */
	public static final SystemMessageId C1_ACQUIRED_THE_FLAG;
	
	/**
	 * ID: 2169<br>
	 * Message: Your clan has been registered to $s1's fortress battle.
	 */
	public static final SystemMessageId REGISTERED_TO_S1_FORTRESS_BATTLE;
	
	/**
	 * ID: 2170<br>
	 * Message: A malicious skill cannot be used when an opponent is in the peace zone
	 */
	public static final SystemMessageId CANT_USE_BAD_MAGIC_WHEN_OPPONENT_IN_PEACE_ZONE;
	
	/**
	 * ID: 2171<br>
	 * Message: This item cannot be crystallized.
	 */
	public static final SystemMessageId ITEM_CANNOT_CRYSTALLIZED;
	
	/**
	 * ID: 2172<br>
	 * Message: +$s1 $s2's auction has ended.
	 */
	public static final SystemMessageId S1_S2_AUCTION_ENDED;
	
	/**
	 * ID: 2173<br>
	 * Message: $s1's auction has ended.
	 */
	public static final SystemMessageId S1_AUCTION_ENDED;
	
	/**
	 * ID: 2174<br>
	 * Message: $c1 cannot duel because $c1 is currently polymorphed.
	 */
	public static final SystemMessageId C1_CANNOT_DUEL_WHILE_POLYMORPHED;
	
	/**
	 * ID: 2175<br>
	 * Message: Party duel cannot be initiated due to a polymorphed partymember
	 */
	public static final SystemMessageId CANNOT_PARTY_DUEL_WHILE_A_MEMBER_IS_POLYMORPHED;
	
	/**
	 * ID: 2176<br>
	 * Message: $s1's elemental power has been removed.
	 */
	public static final SystemMessageId S1_ELEMENTAL_POWER_REMOVED;
	
	/**
	 * ID: 2177<br>
	 * Message: +$s1 $s2's elemental power has been removed.
	 */
	public static final SystemMessageId S1_S2_ELEMENTAL_POWER_REMOVED;
	
	/**
	 * ID: 2178<br>
	 * Message: You failed to remove the elemental power.
	 */
	public static final SystemMessageId FAILED_TO_REMOVE_ELEMENTAL_POWER;
	
	/**
	 * ID: 2179<br>
	 * Message: You have the highest bid submitted in Giran Castle Auction.
	 */
	public static final SystemMessageId HIGHEST_BID_FOR_GIRAN_CASTLE;
	
	/**
	 * ID: 2180<br>
	 * Message: You have the highest bid submitted in Aden Castle Auction.
	 */
	public static final SystemMessageId HIGHEST_BID_FOR_ADEN_CASTLE;
	
	/**
	 * ID: 2181<br>
	 * Message: You have the highest bid submitted in Rune Castle Auction.
	 */
	public static final SystemMessageId HIGHEST_BID_FOR_RUNE_CASTLE;
	
	/**
	 * ID: 2182<br>
	 * Message: You cannot polymorph while riding a boat.
	 */
	public static final SystemMessageId CANT_POLYMORPH_ON_BOAT;
	
	/**
	 * ID: 2183<br>
	 * Message: The fortress battle of $s1 has finished.
	 */
	public static final SystemMessageId THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED;
	
	/**
	 * ID: 2184<br>
	 * Message: $s1 clan is victorious in the fortress battle of $s2.
	 */
	public static final SystemMessageId S1_CLAN_IS_VICTORIOUS_IN_THE_FORTRESS_BATTLE_OF_S2;
	
	/**
	 * ID: 2185<br>
	 * Message: Only a party leader can try to enter.
	 */
	public static final SystemMessageId ONLY_PARTY_LEADER_CAN_ENTER;
	
	/**
	 * ID: 2186<br>
	 * Message: Soul cannot be absorbed anymore.
	 */
	public static final SystemMessageId SOUL_CANNOT_BE_ABSORBED_ANYMORE;
	
	/**
	 * ID: 2187<br>
	 * Message: The target is located where you cannot charge.
	 */
	public static final SystemMessageId THE_TARGET_IS_LOCATED_WHERE_YOU_CANNOT_CHARGE;
	
	/**
	 * ID: 2188<br>
	 * Message: Another enchantment is in progress. Please complete previous task and try again.
	 */
	public static final SystemMessageId ENCHANTMENT_ALREADY_IN_PROGRESS;
	
	/**
	 * ID: 2189<br>
	 * Message: Current Location: $s1, $s2, $s3 (near Near Kamael Village)
	 */
	public static final SystemMessageId LOC_KAMAEL_VILLAGE_S1_S2_S3;
	
	/**
	 * ID: 2190<br>
	 * Message: Current Location: $s1, $s2, $s3 (Near south of Wastelands Camp)
	 */
	public static final SystemMessageId LOC_WASTELANDS_CAMP_S1_S2_S3;
	
	/**
	 * ID: 2191<br>
	 * Message: To apply selected options, the game needs to be reloaded. If you don't apply now, it will be applied when you start the game next time. Will you apply now?
	 */
	public static final SystemMessageId CONFIRM_APPLY_SELECTIONS;
	
	/**
	 * ID: 2192<br>
	 * Message: You have bid on an item auction.
	 */
	public static final SystemMessageId BID_ON_ITEM_AUCTION;
	
	/**
	 * ID: 2193<br>
	 * Message: It's too far from the NPC to work.
	 */
	public static final SystemMessageId TOO_FAR_FROM_NPC;
	
	/**
	 * ID: 2194<br>
	 * Message: Current polymorph form cannot be applied with corresponding effects.
	 */
	public static final SystemMessageId CANT_APPLY_CURRENT_POLYMORPH_WITH_CORRESPONDING_EFFECTS;
	
	/**
	 * ID: 2195<br>
	 * Message: There is not enough soul.
	 */
	public static final SystemMessageId THERE_IS_NOT_ENOUGH_SOUL;
	
	/**
	 * ID: 2196<br>
	 * Message: No Owned Clan.
	 */
	public static final SystemMessageId NO_OWNED_CLAN;
	
	/**
	 * ID: 2197<br>
	 * Message: Owned by clan $s1.
	 */
	public static final SystemMessageId OWNED_S1_CLAN;
	
	/**
	 * ID: 2198<br>
	 * Message: You have the highest bid in an item auction.
	 */
	public static final SystemMessageId HIGHEST_BID_IN_ITEM_AUCTION;
	
	/**
	 * ID: 2199<br>
	 * Message: You cannot enter this instance zone while the NPC server is unavailable.
	 */
	public static final SystemMessageId CANT_ENTER_INSTANCE_ZONE_NPC_SERVER_OFFLINE;
	
	/**
	 * ID: 2200<br>
	 * Message: This instance zone will be terminated because the NPC server is unavailable. You will be forcibly removed from the dungeon shortly
	 */
	public static final SystemMessageId INSTANCE_ZONE_TERMINATED_NPC_SERVER_OFFLINE;
	
	/**
	 * ID: 2201<br>
	 * Message: $s1 year(s) $s2 month(s) $s3 day(s)
	 */
	public static final SystemMessageId S1_YEARS_S2_MONTHS_S3_DAYS;
	
	/**
	 * ID: 2202<br>
	 * Message: $s1 hour(s) $s2 minute(s) $s3 second(s)
	 */
	public static final SystemMessageId S1_HOURS_S2_MINUTES_S3_SECONDS;
	
	/**
	 * ID: 2203<br>
	 * Message: $s1 month(s) $s2 day(s)
	 */
	public static final SystemMessageId S1_MONTHS_S2_DAYS;
	
	/**
	 * ID: 2204<br>
	 * Message: $s1 hour(s)
	 */
	public static final SystemMessageId S1_HOURS;
	
	/**
	 * ID: 2205<br>
	 * Message: You have entered an area where the mini map cannot be used. The mini map will be closed.
	 */
	public static final SystemMessageId AREA_FORBIDS_MINIMAP;
	
	/**
	 * ID: 2206<br>
	 * Message: You have entered an area where the mini map can be used.
	 */
	public static final SystemMessageId AREA_ALLOWS_MINIMAP;
	
	/**
	 * ID: 2207<br>
	 * Message: This is an area where you cannot use the mini map. The mini map will not be opened.
	 */
	public static final SystemMessageId CANT_OPEN_MINIMAP;
	
	/**
	 * ID: 2208<br>
	 * Message: You do not meet the skill level requirements.
	 */
	public static final SystemMessageId YOU_DONT_MEET_SKILL_LEVEL_REQUIREMENTS;
	
	/**
	 * ID: 2209<br>
	 * Message: This is an area where radar cannot be used
	 */
	public static final SystemMessageId AREA_WHERE_RADAR_CANNOT_BE_USED;
	
	/**
	 * ID: 2210<br>
	 * Message: It will return to an unenchanted condition.
	 */
	public static final SystemMessageId RETURN_TO_UNENCHANTED_CONDITION;
	
	/**
	 * ID: 2211<br>
	 * Message: You must learn the Onyx Beast skill before you can acquire further skills.
	 */
	public static final SystemMessageId YOU_MUST_LEARN_ONYX_BEAST_SKILL;
	
	/**
	 * ID: 2212<br>
	 * Message: You have not completed the necessary quest for skill acquisition.
	 */
	public static final SystemMessageId NOT_COMPLETED_QUEST_FOR_SKILL_ACQUISITION;
	
	/**
	 * ID: 2213<br>
	 * Message: Cannot board a ship while polymorphed.
	 */
	public static final SystemMessageId CANT_BOARD_SHIP_POLYMORPHED;
	
	/**
	 * ID: 2214<br>
	 * Message: A new character will be created with the current settings. Continue
	 */
	public static final SystemMessageId CONFIRM_CHARACTER_CREATION;
	
	/**
	 * ID: 2215<br>
	 * Message: $s1 P.Def
	 */
	public static final SystemMessageId S1_PDEF;
	
	/**
	 * ID: 2216<br>
	 * Message: The CPU driver is not up to date. Please install an up-to-date CPU driver.
	 */
	public static final SystemMessageId PLEASE_UPDATE_CPU_DRIVER;
	
	/**
	 * ID: 2217<br>
	 * Message: The ballista has been successfully destroyed and the clan's reputation will be increased.
	 */
	public static final SystemMessageId BALLISTA_DESTROYED_CLAN_REPU_INCREASED;
	
	/**
	 * ID: 2218<br>
	 * Message: This is a main class skill only.
	 */
	public static final SystemMessageId MAIN_CLASS_SKILL_ONLY;
	
	/**
	 * ID: 2219<br>
	 * Message: This squad skill has already been acquired.
	 */
	public static final SystemMessageId SQUAD_SKILL_ALREADY_ACQUIRED;
	
	/**
	 * ID: 2220<br>
	 * Message: The previous level skill has not been learned.
	 */
	public static final SystemMessageId PREVIOUS_LEVEL_SKILL_NOT_LEARNED;
	
	/**
	 * ID: 2221<br>
	 * Message: Will you activate the selected functions?
	 */
	public static final SystemMessageId ACTIVATE_SELECTED_FUNTIONS_CONFIRM;
	
	/**
	 * ID: 2222<br>
	 * Message: It will cost 150,000 adena to place scouts. Will you place them.
	 */
	public static final SystemMessageId SCOUT_COSTS_150000_ADENA;
	
	/**
	 * ID: 2223<br>
	 * Message: It will cost 200,000 adena for a fortress gate enhancement. Will you enhance it?
	 */
	public static final SystemMessageId FORTRESS_GATE_COSTS_200000_ADENA;
	
	/**
	 * ID: 2224<br>
	 * Message: Crossbow is preparing to fire.
	 */
	public static final SystemMessageId CROSSBOW_PREPARING_TO_FIRE;
	
	/**
	 * ID: 2225<br>
	 * Message: There are no other skills to learn. Please come back after $s1nd class change.
	 */
	public static final SystemMessageId NO_SKILLS_TO_LEARN_RETURN_AFTER_S1_CLASS_CHANGE;
	
	/**
	 * ID: 2226<br>
	 * Message: Not enough bolts.
	 */
	public static final SystemMessageId NOT_ENOUGH_BOLTS;
	
	/**
	 * ID: 2227<br>
	 * Message: It is not possible to register for the castle siege side or castle siege of a higher castle in the contract
	 */
	public static final SystemMessageId NOT_POSSIBLE_TO_REGISTER_TO_CASTLE_SIEGE;
	
	/**
	 * ID: 2228<br>
	 * Message: Instance zone time limit:
	 */
	public static final SystemMessageId INSTANCE_ZONE_TIME_LIMIT;
	
	/**
	 * ID: 2229<br>
	 * Message: There is no instance zone under a time limit
	 */
	public static final SystemMessageId NO_INSTANCEZONE_TIME_LIMIT;
	
	/**
	 * ID: 2230<br>
	 * Message: Available to use after $s1 $s2hour(s) $s3minute(s).
	 */
	public static final SystemMessageId AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES;
	
	/**
	 * ID: 2231<br>
	 * Message: The reputation score of the upper castle in contract is not enough and supply was not granted.
	 */
	public static final SystemMessageId REPUTATION_SCORE_FOR_CONTRACT_NOT_ENOUGH;
	
	/**
	 * ID: 2232<br>
	 * Message: $s1 will be crystallized before destruction. Will you continue?
	 */
	public static final SystemMessageId S1_CRYSTALLIZED_BEFORE_DESTRUCTION;
	
	/**
	 * ID: 2233<br>
	 * Message: Siege registration is not possible due to a contract with a higher castle.
	 */
	public static final SystemMessageId CANT_REGISTER_TO_SIEGE_DUE_TO_CONTRACT;
	
	/**
	 * ID: 2234<br>
	 * Message: Will you use the selected Kamael-race-only Hero Weapon?
	 */
	public static final SystemMessageId CONFIRM_KAMAEL_HERO_WEAPON;
	
	/**
	 * ID: 2235<br>
	 * Message: The instance zone in use has been deleted and cannot be accessed.
	 */
	public static final SystemMessageId INSTANCE_ZONE_DELETED_CANT_ACCESSED;
	
	/**
	 * ID: 2236<br>
	 * Message: $s1 minute(s) left for wyvern riding.
	 */
	public static final SystemMessageId S1_MINUTES_LEFT_ON_WYVERN;
	
	/**
	 * ID: 2237<br>
	 * Message: $s1 seconds(s) left for wyvern riding.
	 */
	public static final SystemMessageId S1_SECONDS_LEFT_ON_WYVERN;
	
	/**
	 * ID: 2238<br>
	 * Message: You have participated in the siege of $s1. This siege will continue for 2 hours.
	 */
	public static final SystemMessageId PARTICIPATING_IN_SIEGE_OF_S1;
	
	/**
	 * ID: 2239<br>
	 * Message: The siege of $s1, in which you are participating, has finished.
	 */
	public static final SystemMessageId SIEGE_OF_S1_FINIHSED;
	
	/**
	 * ID: 2240<br>
	 * Message: You cannot register for the Team Battle Clan Hall War when your Clan Lord is on the waiting list for a transaction.
	 */
	public static final SystemMessageId CANT_REGISTER_TO_TEAM_BATTLE_CLAN_HALL_WAR_WHILE_LORD_ON_TRANSACTION_WAITING_LIST;
	
	/**
	 * ID: 2241<br>
	 * Message: You cannot apply for a Clan Lord transaction if your clan has registed for the Team Battle Clan Hall War.
	 */
	public static final SystemMessageId CANT_APPLY_ON_LORD_TRANSACTION_WHILE_REGISTERED_TO_TEAM_BATTLE_CLAN_HALL_WAR;
	
	/**
	 * ID: 2242<br>
	 * Message: Clan members cannot leave or be expelled when they are regisered for the Team Battle Clan Hall War.
	 */
	public static final SystemMessageId MEMBERS_CANT_LEAVE_WHEN_REGISTERED_TO_TEAM_BATTLE_CLAN_HALL_WAR;
	
	/**
	 * ID: 2243<br>
	 * Message: During the Bandit Stronghold or Wild Beast Reserve clan hall war, the previous clan lord rather than the new clan lord participates in battle.
	 */
	public static final SystemMessageId WHEN_BANDITSTRONGHOLD_WILDBEASTRESERVRE_CLANLORD_IN_DANGER_PREVIOUS_LORD_PARTICIPATES_IN_BATTLE;
	
	/**
	 * ID: 2244<br>
	 * Message: $s1 minute(s) remaining.
	 */
	public static final SystemMessageId S1_MINUTES_REMAINING;
	
	/**
	 * ID: 2245<br>
	 * Message: $s1 second(s) remaining.
	 */
	public static final SystemMessageId S1_SECONDS_REMAINING;
	
	/**
	 * ID: 2246<br>
	 * Message: The contest will begin in $s1 minute(s).
	 */
	public static final SystemMessageId CONTEST_BEGIN_IN_S1_MINUTES;
	
	/**
	 * ID: 2247<br>
	 * Message: You cannot board an airship while transformed.
	 */
	public static final SystemMessageId YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_TRANSFORMED;
	
	/**
	 * ID: 2248<br>
	 * Message: You cannot board an airship while petrified.
	 */
	public static final SystemMessageId YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_PETRIFIED;
	
	/**
	 * ID: 2249<br>
	 * Message: You cannot board an airship while dead.
	 */
	public static final SystemMessageId YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_DEAD;
	
	/**
	 * ID: 2250<br>
	 * Message: You cannot board an airship while fishing.
	 */
	public static final SystemMessageId YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_FISHING;
	
	/**
	 * ID: 2251<br>
	 * Message: You cannot board an airship while in battle.
	 */
	public static final SystemMessageId YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_IN_BATTLE;
	
	/**
	 * ID: 2252<br>
	 * Message: You cannot board an airship while in a duel.
	 */
	public static final SystemMessageId YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_IN_A_DUEL;
	
	/**
	 * ID: 2253<br>
	 * Message: You cannot board an airship while sitting.
	 */
	public static final SystemMessageId YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_SITTING;
	
	/**
	 * ID: 2254<br>
	 * Message: You cannot board an airship while casting.
	 */
	public static final SystemMessageId YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_CASTING;
	
	/**
	 * ID: 2255<br>
	 * Message: You cannot board an airship when a cursed weapon is equipped.
	 */
	public static final SystemMessageId YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_A_CURSED_WEAPON_IS_EQUIPPED;
	
	/**
	 * ID: 2256<br>
	 * Message: You cannot board an airship while holding a flag.
	 */
	public static final SystemMessageId YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_HOLDING_A_FLAG;
	
	/**
	 * ID: 2257<br>
	 * Message: You cannot board an airship while a pet or a servitor is summoned.
	 */
	public static final SystemMessageId YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_A_PET_OR_A_SERVITOR_IS_SUMMONED;
	
	/**
	 * ID: 2258<br>
	 * Message: You have already boarded another airship.
	 */
	public static final SystemMessageId YOU_HAVE_ALREADY_BOARDED_ANOTHER_AIRSHIP;
	
	/**
	 * ID: 2259<br>
	 * Message: Current Location: $s1, $s2, $s3 (near Fantasy Isle)
	 */
	public static final SystemMessageId LOC_FANTASY_ISLAND_S1_S2_S3;
	
	/**
	 * ID: 2260<br>
	 * Message: A pet can run away if you do not fill its hunger gauge to 10% or above.
	 */
	public static final SystemMessageId PET_CAN_RUN_AWAY_WHEN_HUNGER_BELOW_10_PERCENT;
	
	/**
	 * ID: 2261<br>
	 * Message: $c1 has done $s3 points of damage to $c2.
	 */
	public static final SystemMessageId C1_DONE_S3_DAMAGE_TO_C2;
	
	/**
	 * ID: 2262<br>
	 * Message: $c1 has received $s3 damage from $c2.
	 */
	public static final SystemMessageId C1_RECEIVED_DAMAGE_OF_S3_FROM_C2;
	
	/**
	 * ID: 2263<br>
	 * Message: $c1 has received damage of $s3 through $c2.
	 */
	public static final SystemMessageId C1_RECEIVED_DAMAGE_OF_S3_THROUGH_C2;
	
	/**
	 * ID: 2264<br>
	 * Message: $c1 has evaded $c2's attack.
	 */
	public static final SystemMessageId C1_EVADED_C2_ATTACK;
	
	/**
	 * ID: 2265<br>
	 * Message: $c1's attack went astray.
	 */
	public static final SystemMessageId C1_ATTACK_WENT_ASTRAY;
	
	/**
	 * ID: 2266<br>
	 * Message: $c1 had a critical hit!
	 */
	public static final SystemMessageId C1_HAD_CRITICAL_HIT;
	
	/**
	 * ID: 2267<br>
	 * Message: $c1 resisted $c2's drain.
	 */
	public static final SystemMessageId C1_RESISTED_C2_DRAIN;
	
	/**
	 * ID: 2268<br>
	 * Message: $c1's attack failed.
	 */
	public static final SystemMessageId C1_ATTACK_FAILED;
	
	/**
	 * ID: 2269<br>
	 * Message: $c1 resisted $c2's magic.
	 */
	public static final SystemMessageId C1_RESISTED_C2_DRAIN2;
	
	/**
	 * ID: 2270<br>
	 * Message: $c1 has received damage from $s2 through the fire of magic
	 */
	public static final SystemMessageId C1_RECEIVED_DAMAGE_FROM_S2_THROUGH_FIRE_OF_MAGIC;
	
	/**
	 * ID: 2271<br>
	 * Message: $c1 weakly resisted $c2's magic.
	 */
	public static final SystemMessageId C1_WEAKLY_RESISTED_C2_MAGIC;
	
	/**
	 * ID: 2272<br>
	 * Message: You have selected shortcuts without settings up sub-keys. You can only use the set shortcut in the Enter Chat mode. Do you still wish to use the set shortcuts
	 */
	public static final SystemMessageId USE_SHORTCUT_CONFIRM;
	
	/**
	 * ID: 2273<br>
	 * Message: This skill cannot be learned while in the sub-class state. Please try again after changing to the main class.
	 */
	public static final SystemMessageId SKILL_NOT_FOR_SUBCLASS;
	
	/**
	 * ID: 2274<br>
	 * Message: You entered an area where you cannot throw away items.
	 */
	
	/**
	 * ID: 2275<br>
	 * Message: You are in an area where you cannot cancel pet summoning.
	 */
	
	/**
	 * ID: 2276<br>
	 * Message: The rebel army recaptured the fortress.
	 */
	public static final SystemMessageId NPCS_RECAPTURED_FORTRESS;
	
	/**
	 * ID: 2277<br>
	 * Message: Party of $s1
	 */
	
	/**
	 * ID: 2278<br>
	 * Message: Remaining Time $s1:$s2
	 */
	
	/**
	 * ID: 2279<br>
	 * Message: You can no longer add a quest to the Quest Alterts.
	 */
	
	/**
	 * ID: 2280 Message: Damage is decreased because $c1 resisted $c2's magic.
	 */
	
	/**
	 * ID: 2281 Message: $c1 hit you for $s3 damage and hit your servitor for $s4.
	 */
	
	/**
	 * ID: 2282 Message: Leave Fantasy Isle.
	 */
	
	/**
	 * ID: 2283<br>
	 * Message: You cannot transform while sitting.
	 */
	public static final SystemMessageId CANNOT_TRANSFORM_WHILE_SITTING;
	
	/**
	 * ID: 2291<br>
	 * Message: You can operate the machine when you participate in the party.
	 */
	public static final SystemMessageId CAN_OPERATE_MACHINE_WHEN_IN_PARTY;
	
	/**
	 * ID: 2293<br>
	 * Message: Current location: $s1, $s2, $s3 (inside the Steel Citadel)
	 */
	public static final SystemMessageId LOC_IN_STEEL_CITADEL_S1_S2_S3;
	
	/**
	 * ID: 2296<br>
	 * Message: You have gained Vitality points.
	 */
	public static final SystemMessageId GAINED_VITALITY_POINTS;
	
	/**
	 * ID: 2301<br>
	 * Message: Current location: Steel Citadel
	 */
	public static final SystemMessageId LOC_STEEL_CITADEL;
	
	/**
	 * ID: 2302<br>
	 * Message: Your Vitamin Item has arrived! Visit the Vitamin Manager in any village to obtain it
	 */
	public static final SystemMessageId YOUR_VITAMIN_ITEM_HAS_ARRIVED;
	
	/**
	 * ID: 2303<br>
	 * Message: There are $s2 second(s) remaining in $s1's re-use time.
	 */
	public static final SystemMessageId S2_SECONDS_REMAINING_FOR_REUSE_S1;
	
	/**
	 * ID: 2304<br>
	 * Message: There are $s2 minute(s), $s3 second(s) remaining in $s1's re-use time.
	 */
	public static final SystemMessageId S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1;
	
	/**
	 * ID: 2305<br>
	 * Message: There are $s2 hour(s), $s3 minute(s), and $s4 second(s) remaining in $s1's re-use time.
	 */
	public static final SystemMessageId S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1;
	
	/**
	 * ID: 2306<br>
	 * Message: Resurrection is possible because of the courage charm's effect. Would you like to resurrect now?
	 */
	public static final SystemMessageId RESURRECT_USING_CHARM_OF_COURAGE;
	
	/**
	 * ID: 2311<br>
	 * Message: You do not have a servitor.
	 */
	public static final SystemMessageId DONT_HAVE_SERVITOR;
	
	/**
	 * ID: 2312<br>
	 * Message: You do not have a pet.
	 */
	public static final SystemMessageId DONT_HAVE_PET;
	
	/**
	 * ID: 2314<br>
	 * Message: Your Vitality is at maximum.
	 */
	public static final SystemMessageId VITALITY_IS_AT_MAXIMUM;
	
	/**
	 * ID: 2315<br>
	 * Message: You have gained Vitality points.
	 */
	public static final SystemMessageId VITALITY_HAS_INCREASED;
	
	/**
	 * ID: 2316<br>
	 * Message: You have lost Vitality points.
	 */
	public static final SystemMessageId VITALITY_HAS_DECREASED;
	
	/**
	 * ID: 2317<br>
	 * Message: Your Vitality is fully exhausted.
	 */
	public static final SystemMessageId VITALITY_IS_EXHAUSTED;
	
	/**
	 * ID: 2319<br>
	 * Message: You have acquired $s1 reputation score.
	 */
	public static final SystemMessageId ACQUIRED_S1_REPUTATION_SCORE;
	
	/**
	 * ID: 2321<br>
	 * Message: Current location: Inside Kamaloka
	 */
	public static final SystemMessageId LOC_KAMALOKA;
	
	/**
	 * ID: 2322<br>
	 * Message: Current location: Inside Nia Kamaloka
	 */
	
	public static final SystemMessageId LOC_NIA_KAMALOKA;
	/**
	 * ID: 2323<br>
	 * Message: Current location: Inside Rim Kamaloka
	 */
	public static final SystemMessageId LOC_RIM_KAMALOKA;
	
	/**
	 * ID: 2326<br>
	 * Message: You have acquired 50 Clan's Fame Points..
	 */
	public static final SystemMessageId ACQUIRED_50_CLAN_FAME_POINTS;
	
	/**
	 * ID: 2327<br>
	 * Message: You don't have enough reputation score.
	 */
	public static final SystemMessageId NOT_ENOUGH_FAME_POINTS;
	
	/**
	 * ID: 2333<br>
	 * Message: You cannot receive the vitamin item because you have exceed your inventory weight/quantity limit.
	 */
	public static final SystemMessageId YOU_CANNOT_RECEIVE_THE_VITAMIN_ITEM;
	
	/**
	 * ID: 2335<br>
	 * Message: There are no more vitamin items to be found
	 */
	public static final SystemMessageId THERE_ARE_NO_MORE_VITAMIN_ITEMS_TO_BE_FOUND;
	
	/**
	 * ID: 2336<br>
	 * Message: Half-Kill!
	 */
	public static final SystemMessageId HALF_KILL;
	
	/**
	 * ID: 2337<br>
	 * Message: Your CP was drained because you were hit with a CP siphon skill.
	 */
	public static final SystemMessageId CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL;
	
	/**
	 * ID: 2348<br>
	 * Message: You cannot use My Teleports during a battle.
	 */
	public static final SystemMessageId YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_BATTLE;
	
	/**
	 * ID: 2349<br>
	 * Message: You cannot use My Teleports while participating a large-scale battle such as a castle siege, fortress siege, or hideout siege..
	 */
	public static final SystemMessageId YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING;
	
	/**
	 * ID: 2350<br>
	 * Message: You cannot use My Teleports during a duel
	 */
	public static final SystemMessageId YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_DUEL;
	
	/**
	 * ID: 2351<br>
	 * Message: You cannot use My Teleports while flying
	 */
	public static final SystemMessageId YOU_CANNOT_USE_MY_TELEPORTS_WHILE_FLYING;
	
	/**
	 * ID: 2352<br>
	 * Message: You cannot use My Teleports while participating in an Olympiad match
	 */
	public static final SystemMessageId YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_IN_AN_OLYMPIAD_MATCH;
	
	/**
	 * ID: 2353<br>
	 * Message: You cannot use My Teleports while you are in a flint or paralyzed state
	 */
	public static final SystemMessageId YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_PARALYZED;
	
	/**
	 * ID: 2354<br>
	 * Message: You cannot use My Teleports while you are dead
	 */
	public static final SystemMessageId YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_DEAD;
	
	/**
	 * ID: 2355<br>
	 * Message: You cannot use My Teleports in this area
	 */
	public static final SystemMessageId YOU_CANNOT_USE_MY_TELEPORTS_IN_THIS_AREA;
	
	/**
	 * ID: 2356<br>
	 * Message: You cannot use My Teleports underwater
	 */
	public static final SystemMessageId YOU_CANNOT_USE_MY_TELEPORTS_UNDERWATER;
	
	/**
	 * ID: 2357<br>
	 * Message: You cannot use My Teleports in an instant zone
	 */
	public static final SystemMessageId YOU_CANNOT_USE_MY_TELEPORTS_IN_AN_INSTANT_ZONE;
	
	/**
	 * ID: 2358<br>
	 * Message: You have no space to save the teleport location
	 */
	public static final SystemMessageId YOU_HAVE_NO_SPACE_TO_SAVE_THE_TELEPORT_LOCATION;
	
	/**
	 * ID: 2359<br>
	 * Message: You cannot teleport because you do not have a teleport item
	 */
	public static final SystemMessageId YOU_CANNOT_TELEPORT_BECAUSE_YOU_DO_NOT_HAVE_A_TELEPORT_ITEM;
	
	/**
	 * ID: 2361<br>
	 * Message: Current Location: $s1
	 */
	public static final SystemMessageId CURRENT_LOCATION_S1;
	
	/**
	 * ID: 2366<br>
	 * Message: The limited-time item has been deleted..
	 */
	public static final SystemMessageId TIME_LIMITED_ITEM_DELETED;
	
	/**
	 * ID: 2371<br>
	 * Message: $c1 was reported as a BOT
	 */
	public static final SystemMessageId C1_WAS_REPORTED_AS_BOT;
	
	/**
	 * ID: 2372<br>
	 * Message: There is not much time remaining until the hunting helper pet leaves.
	 */
	public static final SystemMessageId THERE_NOT_MUCH_TIME_REMAINING_UNTIL_HELPER_LEAVES;
	
	/**
	 * ID: 2373<br>
	 * Message: The hunting helper pet is now leaving.
	 */
	public static final SystemMessageId THE_HELPER_PET_LEAVING;
	
	/**
	 * ID: 2375<br>
	 * Message: The hunting helper pet cannot be returned ecause there is not much time remaining until it leaves.
	 */
	public static final SystemMessageId THE_HELPER_PET_CANNOT_BE_RETURNED;
	
	/**
	 * ID: 2376<br>
	 * Message: You cannot receive a vitamin item during an exchange.
	 */
	public static final SystemMessageId YOU_CANNOT_RECEIVE_A_VITAMIN_ITEM_DURING_AN_EXCHANGE;
	
	/**
	 * ID: 2389<br>
	 * Message: The maximum accumulation allowed of PC cafe points has been exceeded. You can no longer acquire PC cafe points.
	 */
	public static final SystemMessageId THE_MAXMIMUM_ACCUMULATION_ALLOWED_OF_PC_CAFE_POINTS_HAS_BEEN_EXCEEDED;
	
	/**
	 * ID: 2377<br>
	 * Message: You cannot report a character who is in a peace zone or a battlefield
	 */
	public static final SystemMessageId YOU_CANNOT_REPORT_CHARACTER_IN_PEACE_OR_BATTLE_ZONE;
	
	/**
	 * ID: 2378<br>
	 * Message: You cannot report when a clan war has been declared
	 */
	public static final SystemMessageId YOU_CANNOT_REPORT_CLAN_WAR_ENEMY;
	
	/**
	 * ID: 2379<br>
	 * Message: You cannot report a character who has not acquired any Exp. after connecting
	 */
	public static final SystemMessageId YOU_CANNOT_REPORT_CHAR_WHO_ACQUIRED_XP;
	
	/**
	 * ID 2380<br>
	 * Message: You cannot report this person again at this time
	 */
	public static final SystemMessageId YOU_CANNOT_REPORT_CHAR_AT_THIS_TIME_1;
	
	/**
	 * ID 2381<br>
	 * Message: You cannot report this person again at this time
	 */
	public static final SystemMessageId YOU_CANNOT_REPORT_CHAR_AT_THIS_TIME_2;
	
	/**
	 * ID 2382<br>
	 * Message: You cannot report this person again at this time
	 */
	public static final SystemMessageId YOU_CANNOT_REPORT_CHAR_AT_THIS_TIME_3;
	
	/**
	 * ID 2383<br>
	 * Message: You cannot report this person again at this time
	 */
	public static final SystemMessageId YOU_CANNOT_REPORT_CHAR_AT_THIS_TIME_4;
	
	/**
	 * ID: 2390<br>
	 * Message: Your number of My Teleports slots has reached its maximum limit.
	 */
	public static final SystemMessageId YOUR_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_REACHED_ITS_MAXIMUM_LIMIT;
	
	/**
	 * ID: 2393<br>
	 * Message: You have acquired $s1 PC Cafe points.
	 */
	public static final SystemMessageId YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS;
	
	/**
	 * ID: 2396<br>
	 * Message: That pet/servitor skill cannot be used because it is recharging.
	 */
	public static final SystemMessageId PET_SKILL_CANNOT_BE_USED_RECHARCHING;
	
	/**
	 * ID: 2398<br>
	 * Message: You have no open My Teleports slots.
	 */
	public static final SystemMessageId YOU_HAVE_NO_OPEN_MY_TELEPORTS_SLOTS;
	
	/**
	 * ID: 2440<br>
	 * Message: $c1 is already registered on the waiting list for the non-class-limited match event.
	 */
	public static final SystemMessageId C1_IS_ALREADY_REGISTERED_NON_CLASS_LIMITED_EVENT_TEAMS;
	
	/**
	 * ID: 2441<br>
	 * Message: Only a party leader can request a team match.
	 */
	public static final SystemMessageId ONLY_PARTY_LEADER_CAN_REQUEST_TEAM_MATCH;
	
	/**
	 * ID: 2442<br>
	 * Message: The request cannot be made because the requirements have not been made. To participate in a team match you must first form a 3-member party.
	 */
	public static final SystemMessageId PARTY_REQUIREMENTS_NOT_MET;
	
	/**
	 * ID: 2936<br>
	 * Message: The disguise scroll cannot be used because it is meant for use in a different territory.
	 */
	public static final SystemMessageId THE_DISGUISE_SCROLL_MEANT_FOR_DIFFERENT_TERRITORY;
	
	/**
	 * ID: 2937<br>
	 * Message: A territory owning clan member cannot use a disguise scroll.
	 */
	public static final SystemMessageId TERRITORY_OWNING_CLAN_CANNOT_USE_DISGUISE_SCROLL;
	
	/**
	 * ID: 2955<br>
	 * Message: The territory war exclusive disguise and transformation can be used 20 minutes before the start of the territory war to 10 minutes after its end.
	 */
	public static final SystemMessageId TERRITORY_WAR_SCROLL_CAN_NOT_USED_NOW;
	
	/**
	 * ID: 2400<br>
	 * Message: Instant Zone currently in use: $s1
	 */
	public static final SystemMessageId INSTANT_ZONE_CURRENTLY_INUSE_S1;
	
	/**
	 * ID: 2402<br>
	 * Message: The Territory War request period has ended.
	 */
	public static final SystemMessageId THE_TERRITORY_WAR_REGISTERING_PERIOD_ENDED;
	
	/**
	 * ID: 2403<br>
	 * Message: Territory War begins in 10 minutes!
	 */
	public static final SystemMessageId TERRITORY_WAR_BEGINS_IN_10_MINUTES;
	
	/**
	 * ID: 2404<br>
	 * Message: Territory War begins in 5 minutes!
	 */
	public static final SystemMessageId TERRITORY_WAR_BEGINS_IN_5_MINUTES;
	
	/**
	 * ID: 2405<br>
	 * Message: Territory War begins in 1 minute!
	 */
	public static final SystemMessageId TERRITORY_WAR_BEGINS_IN_1_MINUTE;
	
	/**
	 * ID: 2408<br>
	 * Message: You have registered on the waiting list for the non-class-limited team match event.
	 */
	public static final SystemMessageId YOU_HAVE_REGISTERED_IN_A_WAITING_LIST_OF_TEAM_GAMES;
	
	/**
	 * ID: 2409<br>
	 * Message: The number of My Teleports slots has been increased.
	 */
	public static final SystemMessageId THE_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_BEEN_INCREASED;
	
	/**
	 * ID: 2410<br>
	 * Message: You cannot use My Teleports to reach this area!
	 */
	public static final SystemMessageId YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA;
	
	/**
	 * ID: 2424<br>
	 * Message: The collection has failed.
	 */
	public static final SystemMessageId THE_COLLECTION_HAS_FAILED;
	
	/**
	 * ID: 2448<br>
	 * Message: Your birthday gift has arrived
	 */
	public static final SystemMessageId YOUR_BIRTHDAY_GIFT_HAS_ARRIVED;
	
	/**
	 * ID: 2449<br>
	 * Message: There are $s1 days until your character's birthday.
	 */
	public static final SystemMessageId THERE_ARE_S1_DAYS_UNTIL_YOUR_CHARACTERS_BIRTHDAY;
	
	/**
	 * ID: 2450<br>
	 * Message: $c1's character birthday is $s3/$s4/$s2.
	 */
	public static final SystemMessageId C1_BIRTHDAY_IS_S3_S4_S2;
	
	/**
	 * ID: 2451<br>
	 * Message: The cloak equip has been removed because the armor set equip has been removed.
	 */
	public static final SystemMessageId CLOAK_REMOVED_BECAUSE_ARMOR_SET_REMOVED;
	
	/**
	 * ID: 2455<br>
	 * Message: The airship must be summoned in order for you to board.
	 */
	public static final SystemMessageId THE_AIRSHIP_MUST_BE_SUMMONED_TO_BOARD;
	
	/**
	 * ID: 2456<br>
	 * Message: In order to acquire an airship, the clan's level must be level 5 or higher.
	 */
	public static final SystemMessageId THE_AIRSHIP_NEED_CLANLVL_5_TO_SUMMON;
	
	/**
	 * ID: 2457<br>
	 * Message: An airship cannot be summoned because either you have not registered your airship license, or the airship has not yet been summoned
	 */
	public static final SystemMessageId THE_AIRSHIP_NEED_LICENSE_TO_SUMMON;
	
	/**
	 * ID: 2458<br>
	 * Message: The airship owned by the clan is already being used by another clan member.
	 */
	public static final SystemMessageId THE_AIRSHIP_ALREADY_USED;
	
	/**
	 * ID: 2459<br>
	 * Message: The Airship Summon License has already been acquired.
	 */
	public static final SystemMessageId THE_AIRSHIP_SUMMON_LICENSE_ALREADY_ACQUIRED;
	
	/**
	 * ID: 2460<br>
	 * Message: The clan owned airship already exists.
	 */
	public static final SystemMessageId THE_AIRSHIP_IS_ALREADY_EXISTS;
	
	/**
	 * ID: 2461<br>
	 * Message: The airship owned by the clan can only be purchased by the clan lord.
	 */
	public static final SystemMessageId THE_AIRSHIP_NO_PRIVILEGES;
	
	/**
	 * ID: 2462<br>
	 * Message: The airship cannot be summoned because you don't have enough $s1%.
	 */
	public static final SystemMessageId THE_AIRSHIP_NEED_MORE_S1;
	
	/**
	 * ID: 2463<br>
	 * Message: The airship's fuel (EP) will soon run out.
	 */
	public static final SystemMessageId THE_AIRSHIP_FUEL_SOON_RUN_OUT;
	
	/**
	 * ID: 2464<br>
	 * Message: The airship's fuel (EP) has run out. The airship's speed will be greatly decreased in this condition.
	 */
	public static final SystemMessageId THE_AIRSHIP_FUEL_RUN_OUT;
	
	/**
	 * ID: 2465<br>
	 * Message: You have selected a 3 vs 3 class irrelevant team match. Do you wish to participate?
	 */
	public static final SystemMessageId OLYMPIAD_3VS3_CONFIRM;
	
	/**
	 * ID: 2466<br>
	 * Message: A pet on auxiliary mode cannot use skills.
	 */
	public static final SystemMessageId PET_AUXILIARY_MODE_CANNOT_USE_SKILLS;
	
	/**
	 * ID: 2468<br>
	 * Message: You have used a report point on $c1. You have $s2 points remaining on this account
	 */
	public static final SystemMessageId YOU_HAVE_USED_REPORT_POINT_ON_C1_YOU_HAVE_C2_POINTS_LEFT;
	
	/**
	 * ID: 2469<br>
	 * Message: You have used all available points. Points are reset everyday at noon
	 */
	public static final SystemMessageId YOU_HAVE_USED_ALL_POINTS_POINTS_ARE_RESET_AT_NOON;
	
	/**
	 * ID: 2470<br>
	 * Message: This character cannot make a report. You cannot make a report while located inside a peace zone or a battlefield, while you are an opposing clan member during a clan war, or while participating in the Olympiad
	 */
	public static final SystemMessageId TARGET_NOT_REPORT_CANNOT_REPORT_PEACE_PVP_ZONE_OR_OLYMPIAD_OR_CLAN_WAR_ENEMY;
	
	/**
	 * ID: 2471<br>
	 * Message: This character cannot make a report. The target has already been reported by either your clan or alliance, or has already been reported from your current IP
	 */
	public static final SystemMessageId CANNOT_REPORT_TARGET_ALREDY_REPORTED_BY_CLAN_ALLY_MEMBER_OR_SAME_IP;
	
	/**
	 * ID: 2472<br>
	 * Message: This character cannot make a report because another character from this account has already done so
	 */
	public static final SystemMessageId CANNOT_REPORT_ALREDY_REPORED_FROM_THIS_ACCOUNT;
	
	/**
	 * ID: 2473<br>
	 * Message: You have been reported as an illegal program user, so your chatting will be blocked for 10 minutes
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_REPORTED_10_MIN_CHAT_BLOCKED;
	
	/**
	 * ID: 2474<br>
	 * Message: You have been reported as an illegal program user, so your party participation will be blocked for 60 minutes
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_REPORTED_60_MIN_PARTY_BLOCKED;
	
	/**
	 * ID: 2475<br>
	 * Message: You have been reported as an illegal program user, so your party participation will be blocked for 120 minutes
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_REPORTED_120_MIN_PARTY_BLOCKED;
	
	/**
	 * ID: 2476<br>
	 * Message: You have been reported as an illegal program user, so your party participation will be blocked for 180 minutes
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_REPORTED_180_MIN_PARTY_BLOCKED;
	
	/**
	 * ID: 2477<br>
	 * Message: You have been reported as an illegal program user, so your actions will be restricted for 120 minutes
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_REPORTED_120_MIN_ACTION_BLOCKED;
	
	/**
	 * ID: 2478<br>
	 * Message: You have been reported as an illegal program user, so your actions will be restricted for 180 minutes
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_REPORTED_180_MIN_ACTION_BLOCKED;
	
	/**
	 * ID: 2479<br>
	 * Message: You have been reported as an illegal program user, so your actions will be restricted for 180 minutes
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_REPORTED_180_MIN_ACTION_BLOCKED_2;
	
	/**
	 * ID: 2480<br>
	 * Message: You have been reported as an illegal program user, so movement is prohibited for 120 minutes
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_REPORTED_120_MIN_MOVEMENT_BLOCKED;
	
	/**
	 * ID: 2481<br>
	 * Message: $c1 has been reported as an illegal program user and is currently being investigated
	 */
	public static final SystemMessageId C1_REPORTED_AND_IS_BEING_INVESTIGATED;
	
	/**
	 * ID: 2482<br>
	 * Message: $c1 has been reported as an illegal program user and cannot join a party
	 */
	public static final SystemMessageId C1_REPORTED_AND_CANNOT_PARTY;
	
	/**
	 * ID: 2483<br>
	 * Message: You have been reported as an illegal program user, so chatting is not allowed
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_REPORTED_SO_CHATTING_NOT_ALLOWED;
	
	/**
	 * ID: 2484<br>
	 * Message: You have been reported as an illegal program user, so participating in a party is not allowed
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_REPORTED_SO_PARTY_NOT_ALLOWED;
	
	/**
	 * ID: 2485<br>
	 * Message: You have been reported as an illegal program user so your actions have been restricted
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_REPORTED_SO_ACTIONS_NOT_ALLOWED;
	
	/**
	 * ID: 2486<br>
	 * Message: You have been blocked due to verification that you are using a third party program. Subsequent violations may result in termination of your account rather than a penalty within the game
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_BLOCKED_SUBSEQUENT_VIOLATIONS_RESULT_ACCOUNT_TERMINATION;
	
	/**
	 * ID: 2487<br>
	 * Message: You have been reported as an illegal program user, and your connection has been ended. Please contact our CS team to confirm your identity.
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_REPORTED_AS_BOT_CONTACT_OUR_CS_TEAM;
	
	/**
	 * ID: 2491<br>
	 * Message: Your ship cannot teleport because it does not have enough fuel for the trip.
	 */
	public static final SystemMessageId THE_AIRSHIP_CANNOT_TELEPORT;
	
	/**
	 * ID: 2492<br>
	 * Message: The airship has been summoned. It will automatically depart in %s minutes.
	 */
	public static final SystemMessageId THE_AIRSHIP_SUMMONED;
	
	/**
	 * ID: 2500<br>
	 * Message: The collection has succeeded.
	 */
	public static final SystemMessageId THE_COLLECTION_HAS_SUCCEEDED;
	
	/**
	 * ID: 2701<br>
	 * Message: The match is being prepared. Please try again later.
	 */
	public static final SystemMessageId MATCH_BEING_PREPARED_TRY_LATER;
	
	/**
	 * ID: 2702<br>
	 * Message: You were excluded from the match because the registration count was not correct.
	 */
	public static final SystemMessageId EXCLUDED_FROM_MATCH_DUE_INCORRECT_COUNT;
	
	/**
	 * ID: 2703<br>
	 * Message: The team was adjusted because the population ratio was not correct.
	 */
	public static final SystemMessageId TEAM_ADJUSTED_BECAUSE_WRONG_POPULATION_RATIO;
	
	/**
	 * ID: 2704<br>
	 * Message: You cannot register because capacity has been exceeded.
	 */
	public static final SystemMessageId CANNOT_REGISTER_CAUSE_QUEUE_FULL;
	
	/**
	 * ID: 2705<br>
	 * Message: The match waiting time was extended by 1 minute.
	 */
	public static final SystemMessageId MATCH_WAITING_TIME_EXTENDED;
	
	/**
	 * ID: 2706<br>
	 * Message: You cannot enter because you do not meet the requirements.
	 */
	public static final SystemMessageId CANNOT_ENTER_CAUSE_DONT_MATCH_REQUIREMENTS;
	
	/**
	 * ID: 2707<br>
	 * Message: You cannot make another request for 10 seconds after cancelling a match registration.
	 */
	public static final SystemMessageId CANNOT_REQUEST_REGISTRATION_10_SECS_AFTER;
	
	/**
	 * ID: 2708<br>
	 * Message: You cannot register while possessing a cursed weapon.
	 */
	public static final SystemMessageId CANNOT_REGISTER_PROCESSING_CURSED_WEAPON;
	
	/**
	 * ID: 2709<br>
	 * Message: Applicants for the Olympiad, Underground Coliseum, or Kratei's Cube matches cannot register.
	 */
	public static final SystemMessageId COLISEUM_OLYMPIAD_KRATEIS_APPLICANTS_CANNOT_PARTICIPATE;
	
	/**
	 * ID: 2710<br>
	 * Message: Current location: $s1, $s2, $s3 (near the Keucereus clan association location)
	 */
	public static final SystemMessageId LOC_KEUCEREUS_S1_S2_S3;
	
	/**
	 * ID: 2711<br>
	 * Message: Current location: $s1, $s2, $s3 (inside the Seed of Infinity)
	 */
	public static final SystemMessageId LOC_IN_SEED_INFINITY_S1_S2_S3;
	
	/**
	 * ID: 2712<br>
	 * Message: Current location: $s1, $s2, $s3 (outside the Seed of Infinity)
	 */
	public static final SystemMessageId LOC_OUT_SEED_INFINITY_S1_S2_S3;
	
	/**
	 * ID: 2716<br>
	 * Message: Current location: $s1, $s2, $s3 (inside Aerial Cleft)
	 */
	public static final SystemMessageId LOC_CLEFT_S1_S2_S3;
	
	/**
	 * ID: 2720<br>
	 * Message: Instant zone from here: $s1's entry has been restricted.
	 */
	public static final SystemMessageId INSTANT_ZONE_S1_RESTRICTED;
	
	/**
	 * ID: 2721<br>
	 * Message: You are too high to perform this action. Please lower your altitude and try again.
	 */
	public static final SystemMessageId TOO_HIGH_TO_PERFORM_THIS_ACTION;
	
	/**
	 * ID: 2722<br>
	 * Message: Another airship has already been summoned at the wharf. Please try again later.
	 */
	public static final SystemMessageId ANOTHER_AIRSHIP_ALREADY_SUMMONED;
	
	/**
	 * ID: 2727<br>
	 * Message: You cannot board because you do not meet the requirements.
	 */
	public static final SystemMessageId YOU_CANNOT_BOARD_NOT_MEET_REQUEIREMENTS;
	
	/**
	 * ID: 2728<br>
	 * Message: This action is prohibited while mounted or on an airship.
	 */
	public static final SystemMessageId ACTION_PROHIBITED_WHILE_MOUNTED_OR_ON_AN_AIRSHIP;
	
	/**
	 * ID: 2729<br>
	 * Message: You cannot control the helm while transformed.
	 */
	public static final SystemMessageId YOU_CANNOT_CONTROL_THE_HELM_WHILE_TRANSFORMED;
	
	/**
	 * ID: 2730<br>
	 * Message: You cannot control the helm while you are petrified.
	 */
	public static final SystemMessageId YOU_CANNOT_CONTROL_THE_HELM_WHILE_YOU_ARE_PETRIFIED;
	
	/**
	 * ID: 2731<br>
	 * Message: You cannot control the helm when you are dead.
	 */
	public static final SystemMessageId YOU_CANNOT_CONTROL_THE_HELM_WHEN_YOU_ARE_DEAD;
	
	/**
	 * ID: 2732<br>
	 * Message: You cannot control the helm while fishing.
	 */
	public static final SystemMessageId YOU_CANNOT_CONTROL_THE_HELM_WHILE_FISHING;
	
	/**
	 * ID: 2733<br>
	 * Message: You cannot control the helm while in a battle.
	 */
	public static final SystemMessageId YOU_CANNOT_CONTROL_THE_HELM_WHILE_IN_A_BATTLE;
	
	/**
	 * ID: 2734<br>
	 * Message: You cannot control the helm while in a duel.
	 */
	public static final SystemMessageId YOU_CANNOT_CONTROL_THE_HELM_WHILE_IN_A_DUEL;
	/**
	 * ID: 2735<br>
	 * Message: You cannot control the helm while in a sitting position.
	 */
	public static final SystemMessageId YOU_CANNOT_CONTROL_THE_HELM_WHILE_IN_A_SITTING_POSITION;
	
	/**
	 * ID: 2736<br>
	 * Message: You cannot control the helm while using a skill.
	 */
	public static final SystemMessageId YOU_CANNOT_CONTROL_THE_HELM_WHILE_USING_A_SKILL;
	
	/**
	 * ID: 2737<br>
	 * Message: You cannot control the helm while a cursed weapon is equipped.
	 */
	public static final SystemMessageId YOU_CANNOT_CONTROL_THE_HELM_WHILE_A_CURSED_WEAPON_IS_EQUIPPED;
	
	/**
	 * ID: 2738<br>
	 * Message: You cannot control the helm while holding a flag.
	 */
	public static final SystemMessageId YOU_CANNOT_CONTROL_THE_HELM_WHILE_HOLDING_A_FLAG;
	
	/**
	 * ID: 2748<br>
	 * Message: You have been reported as an illegal program user and cannot report other users
	 */
	public static final SystemMessageId YOU_HAVE_BEEN_REPORTED_AND_CANNOT_REPORT;
	
	/**
	 * ID: 2750<br>
	 * Message: The $s1 ward has been destroyed! $c2 now has the territory ward.
	 */
	public static final SystemMessageId THE_S1_WARD_HAS_BEEN_DESTROYED_C2_HAS_THE_WARD;
	
	/**
	 * ID: 2751<br>
	 * Message: The character that acquired $s1 ward has been killed.
	 */
	public static final SystemMessageId THE_CHAR_THAT_ACQUIRED_S1_WARD_HAS_BEEN_KILLED;
	
	/**
	 * ID: 2762<br>
	 * Message: You cannot control because you are too far.
	 */
	public static final SystemMessageId CANT_CONTROL_TOO_FAR;
	
	/**
	 * ID: 2764<br>
	 * Message: You cannot enter because the corresponding alliance channel's maximum number of entrants has been reached.
	 */
	public static final SystemMessageId YOU_CANNOT_ENTER_BECAUSE_MAXIMUM_ENTRANTS;
	
	/**
	 * ID: 2765<br>
	 * Message: Only the alliance channel leader can attempt entry.
	 */
	public static final SystemMessageId ONLY_ALLIANCE_CHANNEL_LEADER_CAN_ENTER;
	
	/**
	 * ID: 2766<br>
	 * Message: Seed of Infinity Stage 1 Attack In Progress.
	 */
	public static final SystemMessageId SEED_OF_INFINITY_STAGE_1_ATTACK_IN_PROGRESS;
	
	/**
	 * ID: 2767<br>
	 * Message: Seed of Infinity Stage 2 Attack In Progress.
	 */
	public static final SystemMessageId SEED_OF_INFINITY_STAGE_2_ATTACK_IN_PROGRESS;
	
	/**
	 * ID: 2768<br>
	 * Message: Seed of Infinity Conquest Complete.
	 */
	public static final SystemMessageId SEED_OF_INFINITY_CONQUEST_COMPLETE;
	
	/**
	 * ID: 2769<br>
	 * Message: Seed of Infinity Stage 1 Defense In Progress.
	 */
	public static final SystemMessageId SEED_OF_INFINITY_STAGE_1_DEFENSE_IN_PROGRESS;
	
	/**
	 * ID: 2770<br>
	 * Message: Seed of Infinity Stage 2 Defense In Progress.
	 */
	public static final SystemMessageId SEED_OF_INFINITY_STAGE_2_DEFENSE_IN_PROGRESS;
	
	/**
	 * ID: 2771<br>
	 * Message: Seed of Destruction Attack in Progress.
	 */
	public static final SystemMessageId SEED_OF_DESTRUCTION_ATTACK_IN_PROGRESS;
	
	/**
	 * ID: 2772<br>
	 * Message: Seed of Destruction Conquest Complete.
	 */
	public static final SystemMessageId SEED_OF_DESTRUCTION_CONQUEST_COMPLETE;
	
	/**
	 * ID: 2773<br>
	 * Message: Seed of Destruction Defense in Progress.
	 */
	public static final SystemMessageId SEED_OF_DESTRUCTION_DEFENSE_IN_PROGRESS;
	
	/**
	 * ID: 2447<br>
	 * Message: You can make another report in $s1-minute(s). You have $s2 points remaining on this account
	 */
	public static final SystemMessageId YOU_CAN_REPORT_IN_S1_MINS_YOU_HAVE_S2_POINTS_LEFT;
	
	/**
	 * ID: 2777<br>
	 * Message: The airship's summon license has been entered. Your clan can now summon the airship.
	 */
	public static final SystemMessageId THE_AIRSHIP_SUMMON_LICENSE_ENTERED;
	
	/**
	 * ID: 2778<br>
	 * Message: You cannot teleport while in possession of a ward.
	 */
	public static final SystemMessageId YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD;
	
	/**
	 * ID: 2793<br>
	 * Message: You must have a minimum of ($s1) people to enter this Instant Zone. Your request for entry is denied
	 */
	public static final SystemMessageId YOU_MUST_HAVE_MINIMUM_OF_S1_PEOPLE_TO_ENTER;
	
	/**
	 * ID: 2795<br>
	 * Message: You've already requested a territory war in another territory elsewhere.
	 */
	public static final SystemMessageId YOU_ALREADY_REQUESTED_TW_REGISTRATION;
	
	/**
	 * ID: 2796<br>
	 * Message: The clan who owns the territory cannot participate in the territory war as mercenaries.
	 */
	public static final SystemMessageId THE_TERRITORY_OWNER_CLAN_CANNOT_PARTICIPATE_AS_MERCENARIES;
	
	/**
	 * ID: 2797<br>
	 * Message: It is not a territory war registration period, so a request cannot be made at this time.
	 */
	public static final SystemMessageId NOT_TERRITORY_REGISTRATION_PERIOD;
	
	/**
	 * ID: 2798<br>
	 * Message: The territory war will end in $s1-hour(s).
	 */
	public static final SystemMessageId THE_TERRITORY_WAR_WILL_END_IN_S1_HOURS;
	
	/**
	 * ID: 2799<br>
	 * Message: The territory war will end in $s1-minute(s).
	 */
	public static final SystemMessageId THE_TERRITORY_WAR_WILL_END_IN_S1_MINUTES;
	
	/**
	 * ID: 2900<br>
	 * Message: $s1-second(s) to the end of territory war!
	 */
	public static final SystemMessageId S1_SECONDS_TO_THE_END_OF_TERRITORY_WAR;
	
	/**
	 * ID: 2901<br>
	 * Message: You cannot force attack a member of the same territory.
	 */
	public static final SystemMessageId YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY;
	
	/**
	 * ID: 2902<br>
	 * Message: You've acquired the ward. Move quickly to your forces' outpost.
	 */
	public static final SystemMessageId YOU_VE_ACQUIRED_THE_WARD;
	
	/**
	 * ID: 2903<br>
	 * Message: Territory war has begun.
	 */
	public static final SystemMessageId TERRITORY_WAR_HAS_BEGUN;
	
	/**
	 * ID: 2904<br>
	 * Message: Territory war has ended.
	 */
	public static final SystemMessageId TERRITORY_WAR_HAS_ENDED;
	
	/**
	 * ID: 2911<br>
	 * You've requested $c1 to be on your Friends List.
	 */
	public static final SystemMessageId YOU_REQUESTED_C1_TO_BE_FRIEND;
	
	/**
	 * ID: 2913<br>
	 * Message: Clan $s1 has succeeded in capturing $s2's territory ward.
	 */
	public static final SystemMessageId CLAN_S1_HAS_SUCCEDED_IN_CAPTURING_S2_TERRITORY_WARD;
	
	/**
	 * ID: 2914<br>
	 * Message: The territory war will begin in 20 minutes! Territory related functions (ie: battlefield channel, Disguise Scrolls, Transformations, etc...) can now be used.
	 */
	public static final SystemMessageId TERRITORY_WAR_BEGINS_IN_20_MINUTES;
	
	/**
	 * ID: 2922<br>
	 * Message: Block Checker will end in 5 seconds!
	 */
	public static final SystemMessageId BLOCK_CHECKER_ENDS_5;
	
	/**
	 * ID: 2923<br>
	 * Message: Block Checker will end in 4 seconds!!
	 */
	public static final SystemMessageId BLOCK_CHECKER_ENDS_4;
	
	/**
	 * ID: 2924<br>
	 * Message: You cannot enter a Seed while in a flying transformation state.
	 */
	public static final SystemMessageId YOU_CANNOT_ENTER_SEED_IN_FLYING_TRANSFORM;
	
	/**
	 * ID: 2925<br>
	 * Message: Block Checker will end in 3 seconds!!!
	 */
	public static final SystemMessageId BLOCK_CHECKER_ENDS_3;
	
	/**
	 * ID: 2926<br>
	 * Message: Block Checker will end in 2 seconds!!!!
	 */
	public static final SystemMessageId BLOCK_CHECKER_ENDS_2;
	
	/**
	 * ID: 2927<br>
	 * Message: Block Checker will end in 1 second!!!!!
	 */
	public static final SystemMessageId BLOCK_CHECKER_ENDS_1;
	
	/**
	 * ID: 2928<br>
	 * Message: The $c1 team has won.
	 */
	public static final SystemMessageId TEAM_C1_WON;
	
	/**
	 * ID: 2961<br>
	 * Message: $s2 unit(s) of the item $s1 is/are required.
	 */
	public static final SystemMessageId S2_UNIT_OF_THE_ITEM_S1_REQUIRED;
	
	/**
	 * ID: 2964<br>
	 * Message: Being appointed as a Noblesse will cancel all related quests. Do you wish to continue?
	 */
	public static final SystemMessageId CANCEL_NOBLESSE_QUESTS;
	
	/**
	 * ID: 2966<br>
	 * Message: This is a Payment Request transaction. Please attach the item.
	 */
	public static final SystemMessageId PAYMENT_REQUEST_NO_ITEM;
	
	/**
	 * ID: 2968<br>
	 * Message: The mail limit (240) has been exceeded and this cannot be forwarded.
	 */
	public static final SystemMessageId CANT_FORWARD_MAIL_LIMIT_EXCEEDED;
	
	/**
	 * ID: 2969<br>
	 * Message: The previous mail was forwarded less than 1 minute ago and this cannot be forwarded.
	 */
	public static final SystemMessageId CANT_FORWARD_LESS_THAN_MINUTE;
	
	/**
	 * ID: 2970<br>
	 * Message: You cannot forward in a non-peace zone.
	 */
	public static final SystemMessageId CANT_FORWARD_NOT_IN_PEACE_ZONE;
	
	/**
	 * ID: 2971<br>
	 * Message: You cannot forward during exchange.
	 */
	public static final SystemMessageId CANT_FORWARD_DURING_EXCHANGE;
	
	/**
	 * ID: 2972<br>
	 * Message: You cannot forward because the private shop or workshop is in progress.
	 */
	public static final SystemMessageId CANT_FORWARD_PRIVATE_STORE;
	
	/**
	 * ID: 2973<br>
	 * Message: You cannot forward during an item enhancement or attribute enhancement.
	 */
	public static final SystemMessageId CANT_FORWARD_DURING_ENCHANT;
	
	/**
	 * ID: 2974<br>
	 * Message: The item that you're trying to send cannot be forwarded because it isn't proper.
	 */
	public static final SystemMessageId CANT_FORWARD_BAD_ITEM;
	
	/**
	 * ID: 2975<br>
	 * Message: You cannot forward because you don't have enough adena.
	 */
	public static final SystemMessageId CANT_FORWARD_NO_ADENA;
	
	/**
	 * ID: 2976<br>
	 * Message: You cannot receive in a non-peace zone location.
	 */
	public static final SystemMessageId CANT_RECEIVE_NOT_IN_PEACE_ZONE;
	
	/**
	 * ID: 2977<br>
	 * Message: You cannot receive during an exchange.
	 */
	public static final SystemMessageId CANT_RECEIVE_DURING_EXCHANGE;
	
	/**
	 * ID: 2978<br>
	 * Message: You cannot receive because the private shop or workshop is in progress.
	 */
	public static final SystemMessageId CANT_RECEIVE_PRIVATE_STORE;
	
	/**
	 * ID: 2979<br>
	 * Message: You cannot receive during an item enhancement or attribute enhancement.
	 */
	public static final SystemMessageId CANT_RECEIVE_DURING_ENCHANT;
	
	/**
	 * ID: 2980<br>
	 * Message: You cannot receive because you don't have enough adena.
	 */
	public static final SystemMessageId CANT_RECEIVE_NO_ADENA;
	
	/**
	 * ID: 2981<br>
	 * Message: You cannot receive because your inventory is full.
	 */
	public static final SystemMessageId CANT_RECEIVE_INVENTORY_FULL;
	
	/**
	 * ID: 2982<br>
	 * Message: You cannot cancel in a non-peace zone location.
	 */
	public static final SystemMessageId CANT_CANCEL_NOT_IN_PEACE_ZONE;
	
	/**
	 * ID: 2983<br>
	 * Message: You cannot cancel during an exchange.
	 */
	public static final SystemMessageId CANT_CANCEL_DURING_EXCHANGE;
	
	/**
	 * ID: 2984<br>
	 * Message: You cannot cancel because the private shop or workshop is in progress.
	 */
	public static final SystemMessageId CANT_CANCEL_PRIVATE_STORE;
	
	/**
	 * ID: 2985<br>
	 * Message: You cannot cancel during an item enhancement or attribute enhancement.
	 */
	public static final SystemMessageId CANT_CANCEL_DURING_ENCHANT;
	
	/**
	 * ID: 2988<br>
	 * Message: You could not cancel receipt because your inventory is full.
	 */
	public static final SystemMessageId CANT_CANCEL_INVENTORY_FULL;
	
	/**
	 * ID: 3002<br>
	 * Message: When the recipient doesn't exist or the character is deleted, sending mail is not possible.
	 */
	public static final SystemMessageId RECIPIENT_NOT_EXIST;
	
	/**
	 * ID: 3008<br>
	 * Message: The mail has arrived.
	 */
	public static final SystemMessageId MAIL_ARRIVED;
	
	/**
	 * ID: 3009<br>
	 * Message: Mail successfully sent.
	 */
	public static final SystemMessageId MAIL_SUCCESSFULLY_SENT;
	
	/**
	 * ID: 3010<br>
	 * Message: Mail successfully returned.
	 */
	public static final SystemMessageId MAIL_SUCCESSFULLY_RETURNED;
	
	/**
	 * ID: 3011<br>
	 * Message: Mail successfully cancelled.
	 */
	public static final SystemMessageId MAIL_SUCCESSFULLY_CANCELLED;
	
	/**
	 * ID: 3012<br>
	 * Message: Mail successfully received.
	 */
	public static final SystemMessageId MAIL_SUCCESSFULLY_RECEIVED;
	
	/**
	 * ID: 3013<br>
	 * Message: $c1 has successfuly enchanted a +$s2 $s3.
	 */
	public static final SystemMessageId C1_SUCCESSFULY_ENCHANTED_A_S2_S3;
	
	/**
	 * ID: 3014<br>
	 * Message: Do you wish to erase the selected mail ?
	 */
	public static final SystemMessageId DO_YOU_WISH_TO_ERASE_MAIL;
	
	/**
	 * ID: 3015<br>
	 * Message: Please select the mail to be deleted.
	 */
	public static final SystemMessageId PLEASE_SELECT_MAIL_TO_BE_DELETED;
	
	/**
	 * ID: 3016<br>
	 * Message: Item selection is possible up to 8.
	 */
	public static final SystemMessageId ITEM_SELECTION_POSSIBLE_UP_TO_8;
	
	/**
	 * ID: 3019<br>
	 * Message: You cannot send mail to yourself.
	 */
	public static final SystemMessageId YOU_CANT_SEND_MAIL_TO_YOURSELF;
	
	/**
	 * ID: 3020<br>
	 * Message: When not entering the amount for the payment request, you cannot send any mail.
	 */
	public static final SystemMessageId PAYMENT_AMOUNT_NOT_ENTERED;
	
	/**
	 * ID: 3023<br>
	 * Message: I can feel that the energy being flown in the Kasha's eye is getting stronger rapidly.
	 */
	public static final SystemMessageId I_CAN_FEEL_ENERGY_KASHA_EYE_GETTING_STRONGER_RAPIDLY;
	
	/**
	 * ID: 3024<br>
	 * Message: Kasha's eye pitches and tosses like it's about to explode.
	 */
	public static final SystemMessageId KASHA_EYE_PITCHES_TOSSES_EXPLODE;
	
	/**
	 * ID: 3025<br>
	 * Message: Payment of $s1 Adena was completed by $s2.
	 */
	public static final SystemMessageId PAYMENT_OF_S1_ADENA_COMPLETED_BY_S2;
	
	/**
	 * ID: 3026<br>
	 * Message: You cannot use the skill enhancing function on this level. You can use the corresponding function on levels higher than 76Lv .
	 */
	public static final SystemMessageId YOU_CANNOT_USE_SKILL_ENCHANT_ON_THIS_LEVEL;
	
	/**
	 * ID: 3027<br>
	 * Message: You cannot use the skill enhancing function in this class. You can use corresponding function when completing the third class change.
	 */
	public static final SystemMessageId YOU_CANNOT_USE_SKILL_ENCHANT_IN_THIS_CLASS;
	
	/**
	 * ID: 3028<br>
	 * Message: You cannot use the skill enhancing function in this class. You can use the skill enhancing function under off-battle status, and cannot use the function while transforming, battling and on-board.
	 */
	public static final SystemMessageId YOU_CANNOT_USE_SKILL_ENCHANT_ATTACKING_TRANSFORMED_BOAT;
	
	/**
	 * ID: 3029<br>
	 * Message: $s1 returned the mail.
	 */
	public static final SystemMessageId S1_RETURNED_MAIL;
	
	/**
	 * ID: 3030<br>
	 * Message: You cannot cancel sent mail since the recipient received it.
	 */
	public static final SystemMessageId YOU_CANT_CANCEL_RECEIVED_MAIL;
	
	/**
	 * ID: 3031<br>
	 * Message: By using the skill of Einhasad's holy sword, defeat the evil Lilims!
	 */
	public static final SystemMessageId USING_EINHASAD_HOLY_SWORD_DEFEAT_LILIMS;
	
	/**
	 * ID: 3033<br>
	 * Message: By using the invisible skill, sneak into the Dawn's document storage!
	 */
	public static final SystemMessageId SNEAK_INTO_DAWNS_DOCUMENT_STORAGE;
	
	/**
	 * ID: 3034<br>
	 * Message: The door in front of us is the entrance to the Dawn's document storage! Approach to the Code Input Device!
	 */
	public static final SystemMessageId DOOR_IS_ENTRANCE_APPROACH_DEVICE;
	
	/**
	 * ID: 3037<br>
	 * Message: Male guards can detect the concealment but the female guards cannot.
	 */
	public static final SystemMessageId MALE_GUARDS_CAN_DETECT_FEMALES_DONT;
	
	/**
	 * ID: 3038<br>
	 * Message: Female guards notice the disguises from far away better than the male guards do, so beware.
	 */
	public static final SystemMessageId FEMALE_GUARDS_NOTICE_BETTER_THAN_MALE;
	
	/**
	 * ID: 3039<br>
	 * Message: By using the holy water of Einhasad, open the door possessed by the curse of flames.
	 */
	public static final SystemMessageId USING_EINHASAD_HOLY_WATER_TO_OPEN_DOOR;
	
	/**
	 * ID: 3040<br>
	 * Message: By using the Court Magician's Magic Staff, open the door on which the magician's barrier is placed.
	 */
	public static final SystemMessageId USING_COURT_MAGICIANS_STAFF_TO_OPEN_DOOR;
	
	/**
	 * ID: 3059<br>
	 * Message: $s1 did not receive it during the waiting time, so it was returned automatically.
	 */
	public static final SystemMessageId S1_NOT_RECEIVE_DURING_WAITING_TIME_MAIL_RETURNED;
	
	/**
	 * ID: 3060<br>
	 * Message: The sealing device glitters and moves. Activation complete normally!
	 */
	public static final SystemMessageId THE_SEALING_DEVICE_ACTIVATION_COMPLETE;
	
	/**
	 * ID: 3062<br>
	 * Message: Do you want to pay $s1 Adena ?
	 */
	public static final SystemMessageId DO_YOU_WANT_TO_PAY_S1_ADENA;
	
	/**
	 * ID: 3063<br>
	 * Message: Do you really want to forward ?
	 */
	public static final SystemMessageId DO_YOU_WANT_TO_FORWARD;
	
	/**
	 * ID: 3064<br>
	 * Message: There is an unread mail.
	 */
	public static final SystemMessageId UNREAD_MAIL;
	
	/**
	 * ID: 3065<br>
	 * Message: Current location: Inside the Chamber of Delusion
	 */
	public static final SystemMessageId LOC_DELUSION_CHAMBER;
	
	/**
	 * ID: 3066<br>
	 * Message: You cannot use the mail function outside the Peace Zone.
	 */
	public static final SystemMessageId CANT_USE_MAIL_OUTSIDE_PEACE_ZONE;
	
	/**
	 * ID: 3067<br>
	 * Message: $s1 cancelled the sent mail.
	 */
	public static final SystemMessageId S1_CANCELLED_MAIL;
	
	/**
	 * ID: 3068<br>
	 * Message: The mail was returned due to the exceeded waiting time.
	 */
	public static final SystemMessageId MAIL_RETURNED;
	
	/**
	 * ID: 3069<br>
	 * Message: Do you really want to cancel the transaction ?
	 */
	public static final SystemMessageId DO_YOU_WANT_TO_CANCEL_TRANSACTION;
	
	/**
	 * ID: 3072<br>
	 * Message: $s1 acquired the attached item to your mail.
	 */
	public static final SystemMessageId S1_ACQUIRED_ATTACHED_ITEM;
	
	/**
	 * ID: 3073<br>
	 * Message: You have acquired $s2 $s1.
	 */
	public static final SystemMessageId YOU_ACQUIRED_S2_S1;
	
	/**
	 * ID: 3074<br>
	 * Message: The allowed length for recipient exceeded.
	 */
	public static final SystemMessageId ALLOWED_LENGTH_FOR_RECIPIENT_EXCEEDED;
	
	/**
	 * ID: 3075<br>
	 * Message: The allowed length for a title exceeded.
	 */
	public static final SystemMessageId ALLOWED_LENGTH_FOR_TITLE_EXCEEDED;
	
	/**
	 * ID: 3077<br>
	 * Message: The mail limit (240) of the opponent's character has been exceeded and this cannot be forwarded.
	 */
	public static final SystemMessageId MAIL_LIMIT_EXCEEDED;
	
	/**
	 * ID: 3078<br>
	 * Message: You're making a request for payment. Do you want to proceed ?
	 */
	public static final SystemMessageId YOU_MAKING_PAYMENT_REQUEST;
	
	/**
	 * ID: 3079<br>
	 * Message: There are items in the Pet Inventory so you cannot register as an individual store or drop items. Please empty the items in the Pet Inventory.
	 */
	public static final SystemMessageId ITEMS_IN_PET_INVENTORY;
	
	/**
	 * ID: 3080<br>
	 * Message: You cannot reset the Skill Link because there is not enough Adena.
	 */
	public static final SystemMessageId CANNOT_RESET_SKILL_LINK_BECAUSE_NOT_ENOUGH_ADENA;
	
	/**
	 * ID: 3081<br>
	 * Message: You cannot receive it because you are under condition that the opponent cannot acquire any Adena for payment
	 */
	public static final SystemMessageId YOU_CANNOT_RECEIVE_CONDITION_OPPONENT_CANT_ACQUIRE_ADENA;
	
	/**
	 * ID: 3082<br>
	 * Message: You cannot send mail to any character that has blocked you.
	 */
	public static final SystemMessageId YOU_CANNOT_SEND_MAIL_TO_CHAR_BLOCK_YOU;
	
	/**
	 * ID: 3094<br>
	 * Message: A user currently participating in the Olympiad cannot send party and friend invitations.
	 */
	public static final SystemMessageId A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS;
	
	/**
	 * ID: 3108<br>
	 * Message: You are no longer protected from aggressive monsters.
	 */
	public static final SystemMessageId YOU_ARE_NO_LONGER_PROTECTED_FROM_AGGRESSIVE_MONSTERS;
	
	/**
	 * ID: 3119<br>
	 * Message: The couple action was denied.
	 */
	public static final SystemMessageId COUPLE_ACTION_DENIED;
	
	/**
	 * ID: 3120<br>
	 * Message: The request cannot be completed because the target does not meet location requirements.
	 */
	public static final SystemMessageId TARGET_DO_NOT_MEET_LOC_REQUIREMENTS;
	
	/**
	 * ID: 3121<br>
	 * Message: The couple action was cancelled.
	 */
	public static final SystemMessageId COUPLE_ACTION_CANCELED;
	
	/**
	 * ID: 3122<br>
	 * Message: The size of the uploaded crest or insignia does not meet the standard requirements.
	 */
	public static final SystemMessageId WRONG_SIZE_UPLOADED_CREST;
	
	/**
	 * ID: 3123<br>
	 * Message: $c1 is in Private Shop mode or in a battle and cannot be requested for a couple action.
	 */
	public static final SystemMessageId C1_IS_IN_PRIVATE_SHOP_MODE_OR_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION;
	
	/**
	 * ID: 3124<br>
	 * Message: $c1 is fishing and cannot be requested for a couple action.
	 */
	public static final SystemMessageId C1_IS_FISHING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION;
	
	/**
	 * ID: 3125<br>
	 * Message: $c1 is in a battle and cannot be requested for a couple action.
	 */
	public static final SystemMessageId C1_IS_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION;
	
	/**
	 * ID: 3126<br>
	 * Message: $c1 is already participating in a couple action and cannot be requested for another couple action.
	 */
	public static final SystemMessageId C1_IS_ALREADY_PARTICIPATING_IN_A_COUPLE_ACTION_AND_CANNOT_BE_REQUESTED_FOR_ANOTHER_COUPLE_ACTION;
	
	/**
	 * ID: 3127<br>
	 * Message: $c1 is in a chaotic state and cannot be requested for a couple action.
	 */
	public static final SystemMessageId C1_IS_IN_A_CHAOTIC_STATE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION;
	
	/**
	 * ID: 3128<br>
	 * Message: $c1 is participating in the Olympiad and cannot be requested for a couple action.
	 */
	public static final SystemMessageId C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION;
	
	/**
	 * ID: 3129<br>
	 * Message: $c1 is participating in a hideout siege and cannot be requested for a couple action.
	 */
	public static final SystemMessageId C1_IS_PARTICIPATING_IN_A_HIDEOUT_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION;
	
	/**
	 * ID: 3130<br>
	 * Message: $c1 is in a castle siege and cannot be requested for a couple action.
	 */
	public static final SystemMessageId C1_IS_IN_A_CASTLE_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION;
	
	/**
	 * ID: 3131<br>
	 * Message: $c1 is riding a ship, steed, or strider and cannot be requested for a couple action.
	 */
	public static final SystemMessageId C1_IS_RIDING_A_SHIP_STEED_OR_STRIDER_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION;
	
	/**
	 * ID: 3132<br>
	 * Message: $c1 is currently teleporting and cannot be requested for a couple action.
	 */
	public static final SystemMessageId C1_IS_CURRENTLY_TELEPORTING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION;
	
	/**
	 * ID: 3133<br>
	 * Message: $c1 is currently transforming and cannot be requested for a couple action.
	 */
	public static final SystemMessageId C1_IS_CURRENTLY_TRANSFORMING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION;
	
	/**
	 * ID: 3135<br>
	 * Message: Requesting approval for changing party loot to "$s1".
	 */
	public static final SystemMessageId REQUESTING_APPROVAL_CHANGE_PARTY_LOOT_S1;
	
	/**
	 * ID: 3137<br>
	 * Message: Party loot change was cancelled.
	 */
	public static final SystemMessageId PARTY_LOOT_CHANGE_CANCELLED;
	
	/**
	 * ID: 3138<br>
	 * Message: Party loot was changed to "$s1".
	 */
	public static final SystemMessageId PARTY_LOOT_CHANGED_S1;
	
	/**
	 * ID: 3139<br>
	 * Message: $c1 is currently dead and cannot be requested for a couple action.
	 */
	public static final SystemMessageId C1_IS_CURRENTLY_DEAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION;
	
	/**
	 * ID: 3140<br>
	 * Message: The crest was successfully registered.
	 */
	public static final SystemMessageId CLAN_CREST_WAS_SUCCESSFULLY_REGISTRED;
	
	/**
	 * ID: 3144<br>
	 * Message: The $s2's attribute was successfully bestowed on $s1, and resistance to $s3 was increased.
	 */
	public static final SystemMessageId THE_S2_ATTRIBUTE_WAS_SUCCESSFULLY_BESTOWED_ON_S1_RES_TO_S3_INCREASED;
	
	/**
	 * ID: 3147<br>
	 * Message: If you are not resurrected within $s1 minutes, you will be expelled from the instant zone.
	 */
	public static final SystemMessageId YOU_WILL_BE_EXPELLED_IN_S1;
	
	/**
	 * ID: 3150<br>
	 * Message: You have requested a couple action with $c1.
	 */
	public static final SystemMessageId YOU_HAVE_REQUESTED_COUPLE_ACTION_C1;
	
	/**
	 * ID: 3152<br>
	 * Message: $s1's $s2 attribute was removed, and resistance to $s3 was decreased.
	 */
	public static final SystemMessageId S1_S2_ATTRIBUTE_REMOVED_RESISTANCE_S3_DECREASED;
	
	/**
	 * ID: 3156<br>
	 * Message: You do not have enough funds to cancel this attribute.
	 */
	public static final SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_FUNDS_TO_CANCEL_ATTRIBUTE;
	
	/**
	 * ID: 3160<br>
	 * Message: +$s1$s2's $s3 attribute was removed, so resistance to $s4 was decreased.
	 */
	public static final SystemMessageId S1_S2_S3_ATTRIBUTE_REMOVED_RESISTANCE_TO_S4_DECREASED;
	
	/**
	 * ID: 3163<br>
	 * Message: The $s3's attribute was successfully bestowed on +$s1$s2, and resistance to $s4 was increased.
	 */
	public static final SystemMessageId THE_S3_ATTRIBUTE_BESTOWED_ON_S1_S2_RESISTANCE_TO_S4_INCREASED;
	
	/**
	 * ID: 3164<br>
	 * Message: $c1 is set to refuse couple actions and cannot be requested for a couple action.
	 */
	public static final SystemMessageId C1_IS_SET_TO_REFUSE_COUPLE_ACTIONS;
	
	/**
	 * ID: 3168<br>
	 * Message: $c1 is set to refuse party requests and cannot receive a party request.
	 */
	public static final SystemMessageId C1_IS_SET_TO_REFUSE_PARTY_REQUEST;
	
	/**
	 * ID: 3169<br>
	 * Message: $c1 is set to refuse duel requests and cannot receive a duel request.
	 */
	public static final SystemMessageId C1_IS_SET_TO_REFUSE_DUEL_REQUEST;
	
	/**
	 * ID: 3206<br>
	 * Message: You currently do not have any Recommendations.
	 */
	public static final SystemMessageId YOU_CURRENTLY_DO_NOT_HAVE_ANY_RECOMMENDATIONS;
	
	/**
	 * ID: 3207<br>
	 * Message: You obtained $s1 Recommendations
	 */
	public static final SystemMessageId YOU_OBTAINED_S1_RECOMMENDATIONS;
	
	/**
	 * ID: 3212<br>
	 * Message: When your pet's hunger gauge is at 0%, you cannot use your pet.
	 */
	public static final SystemMessageId WHEN_YOUR_PETS_HUNGER_GAUGE_IS_AT_0_YOU_CANNOT_USE_YOUR_PET;
	
	/**
	 * ID: 3213<br>
	 * Message: Your pet is starving and will not obey until it gets it's food. Feed your pet!
	 */
	public static final SystemMessageId YOUR_PET_IS_STARVING_AND_WILL_NOT_OBEY_UNTIL_IT_GETS_ITS_FOOD_FEED_YOUR_PET;
	
	/**
	 * ID: 3214<br>
	 * Message: $s1 was successfully added to your Contact List.
	 */
	public static final SystemMessageId S1_SUCCESSFULLY_ADDED_TO_CONTACT_LIST;
	
	/**
	 * ID: 3215<br>
	 * Message: The name $s1% doesn't exist. Please try another name.
	 */
	public static final SystemMessageId NAME_S1_NOT_EXIST_TRY_ANOTHER_NAME;
	
	/**
	 * ID: 3216<br>
	 * Message: The name already exists on the added list.
	 */
	public static final SystemMessageId NAME_ALREADY_EXIST_ON_CONTACT_LIST;
	
	/**
	 * ID: 3217<br>
	 * Message: The name is not currently registered.
	 */
	public static final SystemMessageId NAME_NOT_REGISTERED_ON_CONTACT_LIST;
	
	/**
	 * ID: 3219<br>
	 * Message: $s1 was successfully deleted from your Contact List.
	 */
	public static final SystemMessageId S1_SUCCESFULLY_DELETED_FROM_CONTACT_LIST;
	
	/**
	 * ID: 3221<br>
	 * Message: You cannot add your own name.
	 */
	public static final SystemMessageId CANNOT_ADD_YOUR_NAME_ON_CONTACT_LIST;
	
	/**
	 * ID: 3222<br>
	 * Message: The maximum number of names (100) has been reached. You cannot register any more.
	 */
	public static final SystemMessageId CONTACT_LIST_LIMIT_REACHED;
	
	/**
	 * ID: 3224<br>
	 * Message: The maximum matches you can participate in 1 week is 70.
	 */
	public static final SystemMessageId MAX_OLY_WEEKLY_MATCHES_REACHED;
	
	/***
	 * ID: 3225<br>
	 * Message: The total number of matches that can be entered in 1 week is 60 class irrelevant individual matches, 30 specific matches, and 10 team matches.
	 */
	public static final SystemMessageId MAX_OLY_WEEKLY_MATCHES_REACHED_60_NON_CLASSED_30_CLASSED_10_TEAM;
	
	/***
	 * ID: 3226<br>
	 * Message: You cannot move while speaking to an NPC. One moment please.
	 */
	public static final SystemMessageId CANNOT_MOVE_WHILE_SPEAKING_TO_AN_NPC;
	
	/**
	 * ID: 3255<br>
	 * Message: Arcane Shield decreased your MP by $1 instead of HP.
	 */
	public static final SystemMessageId ARCANE_SHIELD_DECREASED_YOUR_MP_BY_S1_INSTEAD_OF_HP;
	
	/**
	 * ID: 3259<br>
	 * Message: You have acquired $s1 EXP (Bonus: $s2) and $s3 SP (Bonus: $s4).
	 */
	public static final SystemMessageId YOU_EARNED_S1_EXP_BONUS_S2_AND_S3_SP_BONUS_S4;
	
	/**
	 * ID: 3256<br>
	 * Message: MP became 0 and the Arcane Shield is disappearing.
	 */
	public static final SystemMessageId MP_BECAME_0_ARCANE_SHIELD_DISAPPEARING;
	
	/**
	 * ID: 3260<br>
	 * Message: You cannot use the skill because the servitor has not been summoned.
	 */
	public static final SystemMessageId CANNOT_USE_SKILL_WITHOUT_SERVITOR;
	
	/**
	 * ID: 3261<br>
	 * Message: You have $s1 match(es) remaining that you can participate in this week ($s2 1 vs 1 Class matches, $s3 1 vs 1 matches, & $s4 3 vs 3 Team matches).
	 */
	public static final SystemMessageId YOU_HAVE_S1_MATCHES_REMAINING_THAT_YOU_CAN_PARTECIPATE_IN_THIS_WEEK_S2_CLASSED_S3_NON_CLASSED_S4_TEAM;
	
	/**
	 * 3266 1 The angel Nevit has blessed you from above. You are imbued with full Vitality as well as a Vitality Replenishing effect. And should you die, you will not lose Exp! 0 FED7A0 ItemSound2.race_start 2 0 5 0 0 Nevit's Advent Blessing is Upon You! none
	 */
	public static final SystemMessageId THE_ANGEL_NEVIT_HAS_BLESSED_YOU_FROM_ABOVE;
	
	/**
	 * ID: 3267<br>
	 * Message: You are starting to feel the effects of Nevit's Advent Blessing.
	 */
	public static final SystemMessageId YOU_ARE_STARTING_TO_FEEL_THE_EFFECTS_OF_NEVITS_ADVENT_BLESSING;
	
	/**
	 * ID: 3268<br>
	 * Message: You are further infused with the blessings of Nevit!
	 */
	public static final SystemMessageId YOU_ARE_FURTHER_INFUSED_WITH_THE_BLESSINGS_OF_NEVIT;
	
	/**
	 * ID: 3269<br>
	 * Message: Nevit's Advent Blessing shines strongly from above.
	 */
	public static final SystemMessageId NEVITS_ADVENT_BLESSING_SHINES_STRONGLY_FROM_ABOVE;
	
	/**
	 * ID: 3275<br>
	 * Message: Nevit's Advent Blessing has ended.
	 */
	public static final SystemMessageId NEVITS_ADVENT_BLESSING_HAS_ENDED;
	
	/**
	 * ID: 6004<br>
	 * Message: Enchant failed. The enchant level for the corresponding item will be exactly retained.
	 */
	public static final SystemMessageId SAFE_ENCHANT_FAILED;
	
	/**
	 * ID: 6501<br>
	 * Message: You cannot bookmark this location because you do not have a My Teleport Flag.
	 */
	public static final SystemMessageId YOU_CANNOT_BOOKMARK_THIS_LOCATION_BECAUSE_YOU_DO_NOT_HAVE_A_MY_TELEPORT_FLAG;
	
	/**
	 * ID: 6503<br>
	 * Message: The evil Thomas D. Turkey has appeared. Please save Santa.
	 */
	public static final SystemMessageId THOMAS_D_TURKEY_APPEARED;
	
	/**
	 * ID: 6504<br>
	 * Message: You won the battle against Thomas D. Turkey. Santa has been rescued.
	 */
	public static final SystemMessageId THOMAS_D_TURKEY_DEFETED;
	
	/**
	 * ID: 6505<br>
	 * Message: You did not rescue Santa, and Thomas D. Turkey has disappeared.
	 */
	public static final SystemMessageId THOMAS_D_TURKEY_DISAPPEARED;
	
	/**
	 * ID: 6506<br>
	 * Message: You cannot mount a Steed while holding a flag!
	 */
	public static SystemMessageId YOU_CANNOT_MOUNT_A_STEED_WHILE_HOLDING_A_FLAG = null;
	
	/**
	 * ID: 6510<br>
	 * Message: Chaotic Zone
	 */
	public static final SystemMessageId CHAOTIC_ZONE;
	
	/**
	 * Map containing all SystemMessageIds<br>
	 */
	private static Map<Integer, SystemMessageId> VALUES = new HashMap<>();
	
	static
	{
		YOU_HAVE_BEEN_DISCONNECTED = new SystemMessageId(0);
		THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS = new SystemMessageId(1);
		S1_DOES_NOT_EXIST = new SystemMessageId(2);
		S1_IS_NOT_ONLINE = new SystemMessageId(3);
		CANNOT_INVITE_YOURSELF = new SystemMessageId(4);
		S1_ALREADY_EXISTS = new SystemMessageId(5);
		S1_DOES_NOT_EXIST2 = new SystemMessageId(6);
		ALREADY_MEMBER_OF_S1 = new SystemMessageId(7);
		YOU_ARE_WORKING_WITH_ANOTHER_CLAN = new SystemMessageId(8);
		S1_IS_NOT_A_CLAN_LEADER = new SystemMessageId(9);
		S1_WORKING_WITH_ANOTHER_CLAN = new SystemMessageId(10);
		NO_APPLICANTS_FOR_THIS_CLAN = new SystemMessageId(11);
		APPLICANT_INFORMATION_INCORRECT = new SystemMessageId(12);
		CANNOT_DISSOLVE_CAUSE_CLAN_WILL_PARTICIPATE_IN_CASTLE_SIEGE = new SystemMessageId(13);
		CANNOT_DISSOLVE_CAUSE_CLAN_OWNS_CASTLES_HIDEOUTS = new SystemMessageId(14);
		YOU_ARE_IN_SIEGE = new SystemMessageId(15);
		YOU_ARE_NOT_IN_SIEGE = new SystemMessageId(16);
		CASTLE_SIEGE_HAS_BEGUN = new SystemMessageId(17);
		CASTLE_SIEGE_HAS_ENDED = new SystemMessageId(18);
		NEW_CASTLE_LORD = new SystemMessageId(19);
		GATE_IS_OPENING = new SystemMessageId(20);
		GATE_IS_DESTROYED = new SystemMessageId(21);
		TARGET_TOO_FAR = new SystemMessageId(22);
		NOT_ENOUGH_HP = new SystemMessageId(23);
		NOT_ENOUGH_MP = new SystemMessageId(24);
		REJUVENATING_HP = new SystemMessageId(25);
		REJUVENATING_MP = new SystemMessageId(26);
		CASTING_INTERRUPTED = new SystemMessageId(27);
		YOU_PICKED_UP_S1_ADENA = new SystemMessageId(28);
		YOU_PICKED_UP_S1_S2 = new SystemMessageId(29);
		YOU_PICKED_UP_S1 = new SystemMessageId(30);
		CANT_MOVE_SITTING = new SystemMessageId(31);
		UNABLE_COMBAT_PLEASE_GO_RESTART = new SystemMessageId(32);
		CANT_MOVE_CASTING = new SystemMessageId(33);
		WELCOME_TO_LINEAGE = new SystemMessageId(34);
		YOU_DID_S1_DMG = new SystemMessageId(35);
		C1_GAVE_YOU_S2_DMG = new SystemMessageId(36);
		C1_GAVE_YOU_S2_DMG2 = new SystemMessageId(37);
		GETTING_READY_TO_SHOOT_AN_ARROW = new SystemMessageId(41);
		AVOIDED_C1_ATTACK = new SystemMessageId(42);
		MISSED_TARGET = new SystemMessageId(43);
		CRITICAL_HIT = new SystemMessageId(44);
		EARNED_S1_EXPERIENCE = new SystemMessageId(45);
		USE_S1 = new SystemMessageId(46);
		BEGIN_TO_USE_S1 = new SystemMessageId(47);
		S1_PREPARED_FOR_REUSE = new SystemMessageId(48);
		S1_EQUIPPED = new SystemMessageId(49);
		TARGET_CANT_FOUND = new SystemMessageId(50);
		CANNOT_USE_ON_YOURSELF = new SystemMessageId(51);
		EARNED_S1_ADENA = new SystemMessageId(52);
		EARNED_S2_S1_S = new SystemMessageId(53);
		EARNED_ITEM_S1 = new SystemMessageId(54);
		FAILED_TO_PICKUP_S1_ADENA = new SystemMessageId(55);
		FAILED_TO_PICKUP_S1 = new SystemMessageId(56);
		FAILED_TO_PICKUP_S2_S1_S = new SystemMessageId(57);
		FAILED_TO_EARN_S1_ADENA = new SystemMessageId(58);
		FAILED_TO_EARN_S1 = new SystemMessageId(59);
		FAILED_TO_EARN_S2_S1_S = new SystemMessageId(60);
		NOTHING_HAPPENED = new SystemMessageId(61);
		S1_SUCCESSFULLY_ENCHANTED = new SystemMessageId(62);
		S1_S2_SUCCESSFULLY_ENCHANTED = new SystemMessageId(63);
		ENCHANTMENT_FAILED_S1_EVAPORATED = new SystemMessageId(64);
		ENCHANTMENT_FAILED_S1_S2_EVAPORATED = new SystemMessageId(65);
		C1_INVITED_YOU_TO_PARTY = new SystemMessageId(66);
		S1_HAS_INVITED_YOU_TO_JOIN_THE_CLAN_S2 = new SystemMessageId(67);
		WOULD_YOU_LIKE_TO_WITHDRAW_FROM_THE_S1_CLAN = new SystemMessageId(68);
		WOULD_YOU_LIKE_TO_DISMISS_S1_FROM_THE_CLAN = new SystemMessageId(69);
		DO_YOU_WISH_TO_DISPERSE_THE_CLAN_S1 = new SystemMessageId(70);
		HOW_MANY_S1_DISCARD = new SystemMessageId(71);
		HOW_MANY_S1_MOVE = new SystemMessageId(72);
		HOW_MANY_S1_DESTROY = new SystemMessageId(73);
		WISH_DESTROY_S1 = new SystemMessageId(74);
		ID_NOT_EXIST = new SystemMessageId(75);
		INCORRECT_PASSWORD = new SystemMessageId(76);
		CANNOT_CREATE_CHARACTER = new SystemMessageId(77);
		WISH_DELETE_S1 = new SystemMessageId(78);
		NAMING_NAME_ALREADY_EXISTS = new SystemMessageId(79);
		NAMING_CHARNAME_UP_TO_16CHARS = new SystemMessageId(80);
		PLEASE_SELECT_RACE = new SystemMessageId(81);
		PLEASE_SELECT_OCCUPATION = new SystemMessageId(82);
		PLEASE_SELECT_GENDER = new SystemMessageId(83);
		CANT_ATK_PEACEZONE = new SystemMessageId(84);
		TARGET_IN_PEACEZONE = new SystemMessageId(85);
		PLEASE_ENTER_ID = new SystemMessageId(86);
		PLEASE_ENTER_PASSWORD = new SystemMessageId(87);
		WRONG_PROTOCOL_CHECK = new SystemMessageId(88);
		WRONG_PROTOCOL_CONTINUE = new SystemMessageId(89);
		UNABLE_TO_CONNECT = new SystemMessageId(90);
		PLEASE_SELECT_HAIRSTYLE = new SystemMessageId(91);
		S1_HAS_WORN_OFF = new SystemMessageId(92);
		NOT_ENOUGH_SP = new SystemMessageId(93);
		COPYRIGHT = new SystemMessageId(94);
		YOU_EARNED_S1_EXP_AND_S2_SP = new SystemMessageId(95);
		YOU_INCREASED_YOUR_LEVEL = new SystemMessageId(96);
		CANNOT_MOVE_THIS_ITEM = new SystemMessageId(97);
		CANNOT_DISCARD_THIS_ITEM = new SystemMessageId(98);
		CANNOT_TRADE_THIS_ITEM = new SystemMessageId(99);
		C1_REQUESTS_TRADE = new SystemMessageId(100);
		CANT_LOGOUT_WHILE_FIGHTING = new SystemMessageId(101);
		CANT_RESTART_WHILE_FIGHTING = new SystemMessageId(102);
		ID_LOGGED_IN = new SystemMessageId(103);
		CANNOT_CHANGE_WEAPON_DURING_AN_ATTACK = new SystemMessageId(104);
		C1_INVITED_TO_PARTY = new SystemMessageId(105);
		YOU_JOINED_S1_PARTY = new SystemMessageId(106);
		C1_JOINED_PARTY = new SystemMessageId(107);
		C1_LEFT_PARTY = new SystemMessageId(108);
		INCORRECT_TARGET = new SystemMessageId(109);
		YOU_FEEL_S1_EFFECT = new SystemMessageId(110);
		SHIELD_DEFENCE_SUCCESSFULL = new SystemMessageId(111);
		NOT_ENOUGH_ARROWS = new SystemMessageId(112);
		S1_CANNOT_BE_USED = new SystemMessageId(113);
		ENTER_SHADOW_MOTHER_TREE = new SystemMessageId(114);
		EXIT_SHADOW_MOTHER_TREE = new SystemMessageId(115);
		ENTER_PEACEFUL_ZONE = new SystemMessageId(116);
		EXIT_PEACEFUL_ZONE = new SystemMessageId(117);
		REQUEST_C1_FOR_TRADE = new SystemMessageId(118);
		C1_DENIED_TRADE_REQUEST = new SystemMessageId(119);
		BEGIN_TRADE_WITH_C1 = new SystemMessageId(120);
		C1_CONFIRMED_TRADE = new SystemMessageId(121);
		CANNOT_ADJUST_ITEMS_AFTER_TRADE_CONFIRMED = new SystemMessageId(122);
		TRADE_SUCCESSFUL = new SystemMessageId(123);
		C1_CANCELED_TRADE = new SystemMessageId(124);
		WISH_EXIT_GAME = new SystemMessageId(125);
		WISH_RESTART_GAME = new SystemMessageId(126);
		DISCONNECTED_FROM_SERVER = new SystemMessageId(127);
		CHARACTER_CREATION_FAILED = new SystemMessageId(128);
		SLOTS_FULL = new SystemMessageId(129);
		WAREHOUSE_FULL = new SystemMessageId(130);
		S1_LOGGED_IN = new SystemMessageId(131);
		S1_ADDED_TO_FRIENDS = new SystemMessageId(132);
		S1_REMOVED_FROM_YOUR_FRIENDS_LIST = new SystemMessageId(133);
		PLEACE_CHECK_YOUR_FRIEND_LIST_AGAIN = new SystemMessageId(134);
		C1_DID_NOT_REPLY_TO_YOUR_INVITE = new SystemMessageId(135);
		YOU_DID_NOT_REPLY_TO_C1_INVITE = new SystemMessageId(136);
		NO_MORE_ITEMS_SHORTCUT = new SystemMessageId(137);
		DESIGNATE_SHORTCUT = new SystemMessageId(138);
		C1_RESISTED_YOUR_S2 = new SystemMessageId(139);
		SKILL_REMOVED_DUE_LACK_MP = new SystemMessageId(140);
		ONCE_THE_TRADE_IS_CONFIRMED_THE_ITEM_CANNOT_BE_MOVED_AGAIN = new SystemMessageId(141);
		ALREADY_TRADING = new SystemMessageId(142);
		C1_ALREADY_TRADING = new SystemMessageId(143);
		TARGET_IS_INCORRECT = new SystemMessageId(144);
		TARGET_IS_NOT_FOUND_IN_THE_GAME = new SystemMessageId(145);
		CHATTING_PERMITTED = new SystemMessageId(146);
		CHATTING_PROHIBITED = new SystemMessageId(147);
		CANNOT_USE_QUEST_ITEMS = new SystemMessageId(148);
		CANNOT_USE_ITEM_WHILE_TRADING = new SystemMessageId(149);
		CANNOT_DISCARD_OR_DESTROY_ITEM_WHILE_TRADING = new SystemMessageId(150);
		CANNOT_DISCARD_DISTANCE_TOO_FAR = new SystemMessageId(151);
		YOU_HAVE_INVITED_THE_WRONG_TARGET = new SystemMessageId(152);
		C1_IS_BUSY_TRY_LATER = new SystemMessageId(153);
		ONLY_LEADER_CAN_INVITE = new SystemMessageId(154);
		PARTY_FULL = new SystemMessageId(155);
		DRAIN_HALF_SUCCESFUL = new SystemMessageId(156);
		RESISTED_C1_DRAIN = new SystemMessageId(157);
		ATTACK_FAILED = new SystemMessageId(158);
		RESISTED_C1_MAGIC = new SystemMessageId(159);
		C1_IS_ALREADY_IN_PARTY = new SystemMessageId(160);
		INVITED_USER_NOT_ONLINE = new SystemMessageId(161);
		WAREHOUSE_TOO_FAR = new SystemMessageId(162);
		CANNOT_DESTROY_NUMBER_INCORRECT = new SystemMessageId(163);
		WAITING_FOR_ANOTHER_REPLY = new SystemMessageId(164);
		YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST = new SystemMessageId(165);
		FRIEND_LIST_NOT_READY_YET_REGISTER_LATER = new SystemMessageId(166);
		C1_ALREADY_ON_FRIEND_LIST = new SystemMessageId(167);
		C1_REQUESTED_TO_BECOME_FRIENDS = new SystemMessageId(168);
		ACCEPT_THE_FRIENDSHIP = new SystemMessageId(169);
		THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME = new SystemMessageId(170);
		C1_NOT_ON_YOUR_FRIENDS_LIST = new SystemMessageId(171);
		LACK_FUNDS_FOR_TRANSACTION1 = new SystemMessageId(172);
		LACK_FUNDS_FOR_TRANSACTION2 = new SystemMessageId(173);
		OTHER_INVENTORY_FULL = new SystemMessageId(174);
		SKILL_DEACTIVATED_HP_FULL = new SystemMessageId(175);
		THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE = new SystemMessageId(176);
		MESSAGE_REFUSAL_MODE = new SystemMessageId(177);
		MESSAGE_ACCEPTANCE_MODE = new SystemMessageId(178);
		CANT_DISCARD_HERE = new SystemMessageId(179);
		S1_DAYS_LEFT_CANCEL_ACTION = new SystemMessageId(180);
		CANT_SEE_TARGET = new SystemMessageId(181);
		WANT_QUIT_CURRENT_QUEST = new SystemMessageId(182);
		TOO_MANY_USERS = new SystemMessageId(183);
		TRY_AGAIN_LATER = new SystemMessageId(184);
		FIRST_SELECT_USER_TO_INVITE_TO_PARTY = new SystemMessageId(185);
		FIRST_SELECT_USER_TO_INVITE_TO_CLAN = new SystemMessageId(186);
		SELECT_USER_TO_EXPEL = new SystemMessageId(187);
		PLEASE_CREATE_CLAN_NAME = new SystemMessageId(188);
		CLAN_CREATED = new SystemMessageId(189);
		FAILED_TO_CREATE_CLAN = new SystemMessageId(190);
		CLAN_MEMBER_S1_EXPELLED = new SystemMessageId(191);
		FAILED_EXPEL_S1 = new SystemMessageId(192);
		CLAN_HAS_DISPERSED = new SystemMessageId(193);
		FAILED_TO_DISPERSE_CLAN = new SystemMessageId(194);
		ENTERED_THE_CLAN = new SystemMessageId(195);
		S1_REFUSED_TO_JOIN_CLAN = new SystemMessageId(196);
		YOU_HAVE_WITHDRAWN_FROM_CLAN = new SystemMessageId(197);
		FAILED_TO_WITHDRAW_FROM_S1_CLAN = new SystemMessageId(198);
		CLAN_MEMBERSHIP_TERMINATED = new SystemMessageId(199);
		YOU_LEFT_PARTY = new SystemMessageId(200);
		C1_WAS_EXPELLED_FROM_PARTY = new SystemMessageId(201);
		HAVE_BEEN_EXPELLED_FROM_PARTY = new SystemMessageId(202);
		PARTY_DISPERSED = new SystemMessageId(203);
		INCORRECT_NAME_TRY_AGAIN = new SystemMessageId(204);
		INCORRECT_CHARACTER_NAME_TRY_AGAIN = new SystemMessageId(205);
		ENTER_CLAN_NAME_TO_DECLARE_WAR = new SystemMessageId(206);
		S2_OF_THE_CLAN_S1_REQUESTS_WAR = new SystemMessageId(207);
		THE_SIZE_OF_THE_IMAGE_FILE_IS_INAPPROPRIATE = new SystemMessageId(209);
		YOU_ARE_NOT_A_CLAN_MEMBER = new SystemMessageId(212);
		NOT_WORKING_PLEASE_TRY_AGAIN_LATER = new SystemMessageId(213);
		TITLE_CHANGED = new SystemMessageId(214);
		WAR_WITH_THE_S1_CLAN_HAS_BEGUN = new SystemMessageId(215);
		WAR_WITH_THE_S1_CLAN_HAS_ENDED = new SystemMessageId(216);
		YOU_HAVE_WON_THE_WAR_OVER_THE_S1_CLAN = new SystemMessageId(217);
		YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN = new SystemMessageId(218);
		YOU_WERE_DEFEATED_BY_S1_CLAN = new SystemMessageId(219);
		S1_MINUTES_LEFT_UNTIL_CLAN_WAR_ENDS = new SystemMessageId(220);
		CLAN_WAR_WITH_S1_CLAN_HAS_ENDED = new SystemMessageId(221);
		S1_HAS_JOINED_CLAN = new SystemMessageId(222);
		S1_HAS_WITHDRAWN_FROM_THE_CLAN = new SystemMessageId(223);
		S1_DID_NOT_RESPOND_TO_CLAN_INVITATION = new SystemMessageId(224);
		YOU_DID_NOT_RESPOND_TO_S1_CLAN_INVITATION = new SystemMessageId(225);
		S1_CLAN_DID_NOT_RESPOND = new SystemMessageId(226);
		CLAN_WAR_REFUSED_YOU_DID_NOT_RESPOND_TO_S1 = new SystemMessageId(227);
		REQUEST_TO_END_WAR_HAS_BEEN_DENIED = new SystemMessageId(228);
		YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN = new SystemMessageId(229);
		YOU_MUST_WAIT_XX_DAYS_BEFORE_CREATING_A_NEW_CLAN = new SystemMessageId(230);
		YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER = new SystemMessageId(231);
		YOU_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN = new SystemMessageId(232);
		SUBCLAN_IS_FULL = new SystemMessageId(233);
		TARGET_MUST_BE_IN_CLAN = new SystemMessageId(234);
		NOT_AUTHORIZED_TO_BESTOW_RIGHTS = new SystemMessageId(235);
		ONLY_THE_CLAN_LEADER_IS_ENABLED = new SystemMessageId(236);
		CLAN_LEADER_NOT_FOUND = new SystemMessageId(237);
		NOT_JOINED_IN_ANY_CLAN = new SystemMessageId(238);
		CLAN_LEADER_CANNOT_WITHDRAW = new SystemMessageId(239);
		CURRENTLY_INVOLVED_IN_CLAN_WAR = new SystemMessageId(240);
		LEADER_OF_S1_CLAN_NOT_FOUND = new SystemMessageId(241);
		SELECT_TARGET = new SystemMessageId(242);
		CANNOT_DECLARE_WAR_ON_ALLIED_CLAN = new SystemMessageId(243);
		NOT_ALLOWED_TO_CHALLENGE = new SystemMessageId(244);
		FIVE_DAYS_NOT_PASSED_SINCE_REFUSED_WAR = new SystemMessageId(245);
		CLAN_CURRENTLY_AT_WAR = new SystemMessageId(246);
		FIVE_DAYS_MUST_PASS_BEFORE_CHALLENGE_AGAIN = new SystemMessageId(247);
		S1_CLAN_NOT_ENOUGH_MEMBERS_FOR_WAR = new SystemMessageId(248);
		WISH_SURRENDER_TO_S1_CLAN = new SystemMessageId(249);
		YOU_HAVE_PERSONALLY_SURRENDERED_TO_THE_S1_CLAN = new SystemMessageId(250);
		ALREADY_AT_WAR_WITH_ANOTHER_CLAN = new SystemMessageId(251);
		ENTER_CLAN_NAME_TO_SURRENDER_TO = new SystemMessageId(252);
		ENTER_CLAN_NAME_TO_END_WAR = new SystemMessageId(253);
		LEADER_CANT_PERSONALLY_SURRENDER = new SystemMessageId(254);
		S1_CLAN_REQUESTED_END_WAR = new SystemMessageId(255);
		ENTER_TITLE = new SystemMessageId(256);
		DO_YOU_OFFER_S1_CLAN_END_WAR = new SystemMessageId(257);
		NOT_INVOLVED_CLAN_WAR = new SystemMessageId(258);
		SELECT_MEMBERS_FROM_LIST = new SystemMessageId(259);
		FIVE_DAYS_NOT_PASSED_SINCE_YOU_WERE_REFUSED_WAR = new SystemMessageId(260);
		CLAN_NAME_INCORRECT = new SystemMessageId(261);
		CLAN_NAME_TOO_LONG = new SystemMessageId(262);
		DISSOLUTION_IN_PROGRESS = new SystemMessageId(263);
		CANNOT_DISSOLVE_WHILE_IN_WAR = new SystemMessageId(264);
		CANNOT_DISSOLVE_WHILE_IN_SIEGE = new SystemMessageId(265);
		CANNOT_DISSOLVE_WHILE_OWNING_CLAN_HALL_OR_CASTLE = new SystemMessageId(266);
		NO_REQUESTS_TO_DISPERSE = new SystemMessageId(267);
		PLAYER_ALREADY_ANOTHER_CLAN = new SystemMessageId(268);
		YOU_CANNOT_DISMISS_YOURSELF = new SystemMessageId(269);
		YOU_HAVE_ALREADY_SURRENDERED = new SystemMessageId(270);
		CLAN_LVL_3_NEEDED_TO_ENDOWE_TITLE = new SystemMessageId(271);
		CLAN_LVL_3_NEEDED_TO_SET_CREST = new SystemMessageId(272);
		CLAN_LVL_3_NEEDED_TO_DECLARE_WAR = new SystemMessageId(273);
		CLAN_LEVEL_INCREASED = new SystemMessageId(274);
		CLAN_LEVEL_INCREASE_FAILED = new SystemMessageId(275);
		ITEM_OR_PREREQUISITES_MISSING_TO_LEARN_SKILL = new SystemMessageId(276);
		LEARNED_SKILL_S1 = new SystemMessageId(277);
		NOT_ENOUGH_SP_TO_LEARN_SKILL = new SystemMessageId(278);
		YOU_NOT_ENOUGH_ADENA = new SystemMessageId(279);
		NO_ITEMS_TO_SELL = new SystemMessageId(280);
		YOU_NOT_ENOUGH_ADENA_PAY_FEE = new SystemMessageId(281);
		NO_ITEM_DEPOSITED_IN_WH = new SystemMessageId(282);
		ENTERED_COMBAT_ZONE = new SystemMessageId(283);
		LEFT_COMBAT_ZONE = new SystemMessageId(284);
		CLAN_S1_ENGRAVED_RULER = new SystemMessageId(285);
		BASE_UNDER_ATTACK = new SystemMessageId(286);
		OPPONENT_STARTED_ENGRAVING = new SystemMessageId(287);
		CASTLE_GATE_BROKEN_DOWN = new SystemMessageId(288);
		NOT_ANOTHER_HEADQUARTERS = new SystemMessageId(289);
		NOT_SET_UP_BASE_HERE = new SystemMessageId(290);
		CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE = new SystemMessageId(291);
		S1_ANNOUNCED_SIEGE_TIME = new SystemMessageId(292);
		REGISTRATION_TERM_FOR_S1_ENDED = new SystemMessageId(293);
		BECAUSE_YOUR_CLAN_IS_NOT_CURRENTLY_ON_THE_OFFENSIVE_IN_A_CLAN_HALL_SIEGE_WAR_IT_CANNOT_SUMMON_ITS_BASE_CAMP = new SystemMessageId(294);
		S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED = new SystemMessageId(295);
		FALL_DAMAGE_S1 = new SystemMessageId(296);
		DROWN_DAMAGE_S1 = new SystemMessageId(297);
		YOU_DROPPED_S1 = new SystemMessageId(298);
		C1_OBTAINED_S3_S2 = new SystemMessageId(299);
		C1_OBTAINED_S2 = new SystemMessageId(300);
		S2_S1_DISAPPEARED = new SystemMessageId(301);
		S1_DISAPPEARED = new SystemMessageId(302);
		SELECT_ITEM_TO_ENCHANT = new SystemMessageId(303);
		CLAN_MEMBER_S1_LOGGED_IN = new SystemMessageId(304);
		PLAYER_DECLINED = new SystemMessageId(305);
		FAILED_TO_DELETE_CHAR = new SystemMessageId(306);
		CANNOT_TRADE_WAREHOUSE_KEEPER = new SystemMessageId(307);
		PLAYER_DECLINED_CLAN_INVITATION = new SystemMessageId(308);
		YOU_HAVE_SUCCEEDED_IN_EXPELLING_CLAN_MEMBER = new SystemMessageId(309);
		FAILED_TO_EXPEL_CLAN_MEMBER = new SystemMessageId(310);
		CLAN_WAR_DECLARATION_ACCEPTED = new SystemMessageId(311);
		CLAN_WAR_DECLARATION_REFUSED = new SystemMessageId(312);
		CEASE_WAR_REQUEST_ACCEPTED = new SystemMessageId(313);
		FAILED_TO_SURRENDER = new SystemMessageId(314);
		FAILED_TO_PERSONALLY_SURRENDER = new SystemMessageId(315);
		FAILED_TO_WITHDRAW_FROM_THE_PARTY = new SystemMessageId(316);
		FAILED_TO_EXPEL_THE_PARTY_MEMBER = new SystemMessageId(317);
		FAILED_TO_DISPERSE_THE_PARTY = new SystemMessageId(318);
		UNABLE_TO_UNLOCK_DOOR = new SystemMessageId(319);
		FAILED_TO_UNLOCK_DOOR = new SystemMessageId(320);
		ITS_NOT_LOCKED = new SystemMessageId(321);
		DECIDE_SALES_PRICE = new SystemMessageId(322);
		FORCE_INCREASED_TO_S1 = new SystemMessageId(323);
		FORCE_MAXLEVEL_REACHED = new SystemMessageId(324);
		CORPSE_ALREADY_DISAPPEARED = new SystemMessageId(325);
		SELECT_TARGET_FROM_LIST = new SystemMessageId(326);
		CANNOT_EXCEED_80_CHARACTERS = new SystemMessageId(327);
		PLEASE_INPUT_TITLE_LESS_128_CHARACTERS = new SystemMessageId(328);
		PLEASE_INPUT_CONTENT_LESS_3000_CHARACTERS = new SystemMessageId(329);
		ONE_LINE_RESPONSE_NOT_EXCEED_128_CHARACTERS = new SystemMessageId(330);
		ACQUIRED_S1_SP = new SystemMessageId(331);
		DO_YOU_WANT_TO_BE_RESTORED = new SystemMessageId(332);
		S1_DAMAGE_BY_CORE_BARRIER = new SystemMessageId(333);
		ENTER_PRIVATE_STORE_MESSAGE = new SystemMessageId(334);
		S1_HAS_BEEN_ABORTED = new SystemMessageId(335);
		WISH_TO_CRYSTALLIZE_S1 = new SystemMessageId(336);
		SOULSHOTS_GRADE_MISMATCH = new SystemMessageId(337);
		NOT_ENOUGH_SOULSHOTS = new SystemMessageId(338);
		CANNOT_USE_SOULSHOTS = new SystemMessageId(339);
		PRIVATE_STORE_UNDER_WAY = new SystemMessageId(340);
		NOT_ENOUGH_MATERIALS = new SystemMessageId(341);
		ENABLED_SOULSHOT = new SystemMessageId(342);
		SWEEPER_FAILED_TARGET_NOT_SPOILED = new SystemMessageId(343);
		SOULSHOTS_DISABLED = new SystemMessageId(344);
		CHAT_ENABLED = new SystemMessageId(345);
		CHAT_DISABLED = new SystemMessageId(346);
		INCORRECT_ITEM_COUNT = new SystemMessageId(347);
		INCORRECT_ITEM_PRICE = new SystemMessageId(348);
		PRIVATE_STORE_ALREADY_CLOSED = new SystemMessageId(349);
		ITEM_OUT_OF_STOCK = new SystemMessageId(350);
		NOT_ENOUGH_ITEMS = new SystemMessageId(351);
		INCORRECT_ITEM = new SystemMessageId(352);
		CANNOT_PURCHASE = new SystemMessageId(353);
		CANCEL_ENCHANT = new SystemMessageId(354);
		INAPPROPRIATE_ENCHANT_CONDITION = new SystemMessageId(355);
		REJECT_RESURRECTION = new SystemMessageId(356);
		ALREADY_SPOILED = new SystemMessageId(357);
		S1_HOURS_UNTIL_SIEGE_CONCLUSION = new SystemMessageId(358);
		S1_MINUTES_UNTIL_SIEGE_CONCLUSION = new SystemMessageId(359);
		CASTLE_SIEGE_S1_SECONDS_LEFT = new SystemMessageId(360);
		OVER_HIT = new SystemMessageId(361);
		ACQUIRED_BONUS_EXPERIENCE_THROUGH_OVER_HIT = new SystemMessageId(362);
		CHAT_AVAILABLE_S1_MINUTE = new SystemMessageId(363);
		ENTER_USER_NAME_TO_SEARCH = new SystemMessageId(364);
		ARE_YOU_SURE = new SystemMessageId(365);
		PLEASE_SELECT_HAIR_COLOR = new SystemMessageId(366);
		CANNOT_REMOVE_CLAN_CHARACTER = new SystemMessageId(367);
		S1_S2_EQUIPPED = new SystemMessageId(368);
		YOU_PICKED_UP_A_S1_S2 = new SystemMessageId(369);
		FAILED_PICKUP_S1 = new SystemMessageId(370);
		ACQUIRED_S1_S2 = new SystemMessageId(371);
		FAILED_EARN_S1 = new SystemMessageId(372);
		WISH_DESTROY_S1_S2 = new SystemMessageId(373);
		WISH_CRYSTALLIZE_S1_S2 = new SystemMessageId(374);
		DROPPED_S1_S2 = new SystemMessageId(375);
		C1_OBTAINED_S2_S3 = new SystemMessageId(376);
		S1_S2_DISAPPEARED = new SystemMessageId(377);
		C1_PURCHASED_S2 = new SystemMessageId(378);
		C1_PURCHASED_S2_S3 = new SystemMessageId(379);
		C1_PURCHASED_S3_S2_S = new SystemMessageId(380);
		GAME_CLIENT_UNABLE_TO_CONNECT_TO_PETITION_SERVER = new SystemMessageId(381);
		NO_USERS_CHECKED_OUT_GM_ID = new SystemMessageId(382);
		REQUEST_CONFIRMED_TO_END_CONSULTATION = new SystemMessageId(383);
		CLIENT_NOT_LOGGED_ONTO_GAME_SERVER = new SystemMessageId(384);
		REQUEST_CONFIRMED_TO_BEGIN_CONSULTATION = new SystemMessageId(385);
		PETITION_MORE_THAN_FIVE_CHARACTERS = new SystemMessageId(386);
		THIS_END_THE_PETITION_PLEASE_PROVIDE_FEEDBACK = new SystemMessageId(387);
		NOT_UNDER_PETITION_CONSULTATION = new SystemMessageId(388);
		PETITION_ACCEPTED_RECENT_NO_S1 = new SystemMessageId(389);
		ONLY_ONE_ACTIVE_PETITION_AT_TIME = new SystemMessageId(390);
		RECENT_NO_S1_CANCELED = new SystemMessageId(391);
		UNDER_PETITION_ADVICE = new SystemMessageId(392);
		FAILED_CANCEL_PETITION_TRY_LATER = new SystemMessageId(393);
		STARTING_PETITION_WITH_C1 = new SystemMessageId(394);
		PETITION_ENDED_WITH_C1 = new SystemMessageId(395);
		TRY_AGAIN_AFTER_CHANGING_PASSWORD = new SystemMessageId(396);
		NO_PAID_ACCOUNT = new SystemMessageId(397);
		NO_TIME_LEFT_ON_ACCOUNT = new SystemMessageId(398);
		SYSTEM_ERROR = new SystemMessageId(399);
		WISH_TO_DROP_S1 = new SystemMessageId(400);
		TOO_MANY_QUESTS = new SystemMessageId(401);
		NOT_CORRECT_BOAT_TICKET = new SystemMessageId(402);
		EXCEECED_POCKET_ADENA_LIMIT = new SystemMessageId(403);
		CREATE_LVL_TOO_LOW_TO_REGISTER = new SystemMessageId(404);
		TOTAL_PRICE_TOO_HIGH = new SystemMessageId(405);
		PETITION_APP_ACCEPTED = new SystemMessageId(406);
		PETITION_UNDER_PROCESS = new SystemMessageId(407);
		SET_PERIOD = new SystemMessageId(408);
		SET_TIME_S1_S2_S3 = new SystemMessageId(409);
		REGISTRATION_PERIOD = new SystemMessageId(410);
		REGISTRATION_TIME_S1_S2_S3 = new SystemMessageId(411);
		BATTLE_BEGINS_S1_S2_S3 = new SystemMessageId(412);
		BATTLE_ENDS_S1_S2_S3 = new SystemMessageId(413);
		STANDBY = new SystemMessageId(414);
		UNDER_SIEGE = new SystemMessageId(415);
		ITEM_CANNOT_EXCHANGE = new SystemMessageId(416);
		S1_DISARMED = new SystemMessageId(417);
		S1_MINUTES_USAGE_LEFT = new SystemMessageId(419);
		TIME_EXPIRED = new SystemMessageId(420);
		ANOTHER_LOGIN_WITH_ACCOUNT = new SystemMessageId(421);
		WEIGHT_LIMIT_EXCEEDED = new SystemMessageId(422);
		ENCHANT_SCROLL_CANCELLED = new SystemMessageId(423);
		DOES_NOT_FIT_SCROLL_CONDITIONS = new SystemMessageId(424);
		CREATE_LVL_TOO_LOW_TO_REGISTER2 = new SystemMessageId(425);
		REFERENCE_MEMBERSHIP_WITHDRAWAL_S1 = new SystemMessageId(445);
		DOT = new SystemMessageId(447);
		SYSTEM_ERROR_LOGIN_LATER = new SystemMessageId(448);
		PASSWORD_ENTERED_INCORRECT1 = new SystemMessageId(449);
		CONFIRM_ACCOUNT_LOGIN_LATER = new SystemMessageId(450);
		PASSWORD_ENTERED_INCORRECT2 = new SystemMessageId(451);
		PLEASE_CONFIRM_ACCOUNT_LOGIN_LATER = new SystemMessageId(452);
		ACCOUNT_INFORMATION_INCORRECT = new SystemMessageId(453);
		ACCOUNT_IN_USE = new SystemMessageId(455);
		LINAGE_MINIMUM_AGE = new SystemMessageId(456);
		SERVER_MAINTENANCE = new SystemMessageId(457);
		USAGE_TERM_EXPIRED = new SystemMessageId(458);
		TO_REACTIVATE_YOUR_ACCOUNT = new SystemMessageId(460);
		ACCESS_FAILED = new SystemMessageId(461);
		PLEASE_TRY_AGAIN_LATER = new SystemMessageId(462);
		FEATURE_ONLY_FOR_ALLIANCE_LEADER = new SystemMessageId(464);
		NO_CURRENT_ALLIANCES = new SystemMessageId(465);
		YOU_HAVE_EXCEEDED_THE_LIMIT = new SystemMessageId(466);
		CANT_INVITE_CLAN_WITHIN_1_DAY = new SystemMessageId(467);
		CANT_ENTER_ALLIANCE_WITHIN_1_DAY = new SystemMessageId(468);
		MAY_NOT_ALLY_CLAN_BATTLE = new SystemMessageId(469);
		ONLY_CLAN_LEADER_WITHDRAW_ALLY = new SystemMessageId(470);
		ALLIANCE_LEADER_CANT_WITHDRAW = new SystemMessageId(471);
		CANNOT_EXPEL_YOURSELF = new SystemMessageId(472);
		DIFFERENT_ALLIANCE = new SystemMessageId(473);
		CLAN_DOESNT_EXISTS = new SystemMessageId(474);
		DIFFERENT_ALLIANCE2 = new SystemMessageId(475);
		ADJUST_IMAGE_8_12 = new SystemMessageId(476);
		NO_RESPONSE_TO_ALLY_INVITATION = new SystemMessageId(477);
		YOU_DID_NOT_RESPOND_TO_ALLY_INVITATION = new SystemMessageId(478);
		S1_JOINED_AS_FRIEND = new SystemMessageId(479);
		PLEASE_CHECK_YOUR_FRIENDS_LIST = new SystemMessageId(480);
		S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST = new SystemMessageId(481);
		YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIENDS_LIST = new SystemMessageId(482);
		FUNCTION_INACCESSIBLE_NOW = new SystemMessageId(483);
		S1_ALREADY_IN_FRIENDS_LIST = new SystemMessageId(484);
		NO_NEW_INVITATIONS_ACCEPTED = new SystemMessageId(485);
		THE_USER_NOT_IN_FRIENDS_LIST = new SystemMessageId(486);
		FRIEND_LIST_HEADER = new SystemMessageId(487);
		S1_ONLINE = new SystemMessageId(488);
		S1_OFFLINE = new SystemMessageId(489);
		FRIEND_LIST_FOOTER = new SystemMessageId(490);
		ALLIANCE_INFO_HEAD = new SystemMessageId(491);
		ALLIANCE_NAME_S1 = new SystemMessageId(492);
		CONNECTION_S1_TOTAL_S2 = new SystemMessageId(493);
		ALLIANCE_LEADER_S2_OF_S1 = new SystemMessageId(494);
		ALLIANCE_CLAN_TOTAL_S1 = new SystemMessageId(495);
		CLAN_INFO_HEAD = new SystemMessageId(496);
		CLAN_INFO_NAME_S1 = new SystemMessageId(497);
		CLAN_INFO_LEADER_S1 = new SystemMessageId(498);
		CLAN_INFO_LEVEL_S1 = new SystemMessageId(499);
		CLAN_INFO_SEPARATOR = new SystemMessageId(500);
		CLAN_INFO_FOOT = new SystemMessageId(501);
		ALREADY_JOINED_ALLIANCE = new SystemMessageId(502);
		FRIEND_S1_HAS_LOGGED_IN = new SystemMessageId(503);
		ONLY_CLAN_LEADER_CREATE_ALLIANCE = new SystemMessageId(504);
		CANT_CREATE_ALLIANCE_10_DAYS_DISOLUTION = new SystemMessageId(505);
		INCORRECT_ALLIANCE_NAME = new SystemMessageId(506);
		INCORRECT_ALLIANCE_NAME_LENGTH = new SystemMessageId(507);
		ALLIANCE_ALREADY_EXISTS = new SystemMessageId(508);
		CANT_ACCEPT_ALLY_ENEMY_FOR_SIEGE = new SystemMessageId(509);
		YOU_INVITED_FOR_ALLIANCE = new SystemMessageId(510);
		SELECT_USER_TO_INVITE = new SystemMessageId(511);
		DO_YOU_WISH_TO_WITHDRW = new SystemMessageId(512);
		ENTER_NAME_CLAN_TO_EXPEL = new SystemMessageId(513);
		DO_YOU_WISH_TO_DISOLVE = new SystemMessageId(514);
		SI_INVITED_YOU_AS_FRIEND = new SystemMessageId(516);
		YOU_ACCEPTED_ALLIANCE = new SystemMessageId(517);
		FAILED_TO_INVITE_CLAN_IN_ALLIANCE = new SystemMessageId(518);
		YOU_HAVE_WITHDRAWN_FROM_ALLIANCE = new SystemMessageId(519);
		YOU_HAVE_FAILED_TO_WITHDRAWN_FROM_ALLIANCE = new SystemMessageId(520);
		YOU_HAVE_EXPELED_A_CLAN = new SystemMessageId(521);
		FAILED_TO_EXPELED_A_CLAN = new SystemMessageId(522);
		ALLIANCE_DISOLVED = new SystemMessageId(523);
		FAILED_TO_DISOLVE_ALLIANCE = new SystemMessageId(524);
		YOU_HAVE_SUCCEEDED_INVITING_FRIEND = new SystemMessageId(525);
		FAILED_TO_INVITE_A_FRIEND = new SystemMessageId(526);
		S2_ALLIANCE_LEADER_OF_S1_REQUESTED_ALLIANCE = new SystemMessageId(527);
		SPIRITSHOTS_GRADE_MISMATCH = new SystemMessageId(530);
		NOT_ENOUGH_SPIRITSHOTS = new SystemMessageId(531);
		CANNOT_USE_SPIRITSHOTS = new SystemMessageId(532);
		ENABLED_SPIRITSHOT = new SystemMessageId(533);
		DISABLED_SPIRITSHOT = new SystemMessageId(534);
		HOW_MUCH_ADENA_TRANSFER = new SystemMessageId(536);
		HOW_MUCH_TRANSFER = new SystemMessageId(537);
		SP_DECREASED_S1 = new SystemMessageId(538);
		EXP_DECREASED_BY_S1 = new SystemMessageId(539);
		CLAN_LEADERS_MAY_NOT_BE_DELETED = new SystemMessageId(540);
		CLAN_MEMBER_MAY_NOT_BE_DELETED = new SystemMessageId(541);
		THE_NPC_SERVER_IS_CURRENTLY_DOWN = new SystemMessageId(542);
		YOU_ALREADY_HAVE_A_PET = new SystemMessageId(543);
		ITEM_NOT_FOR_PETS = new SystemMessageId(544);
		YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS = new SystemMessageId(545);
		UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED = new SystemMessageId(546);
		SUMMON_A_PET = new SystemMessageId(547);
		NAMING_PETNAME_UP_TO_8CHARS = new SystemMessageId(548);
		TO_CREATE_AN_ALLY_YOU_CLAN_MUST_BE_LEVEL_5_OR_HIGHER = new SystemMessageId(549);
		YOU_MAY_NOT_CREATE_ALLY_WHILE_DISSOLVING = new SystemMessageId(550);
		CANNOT_RISE_LEVEL_WHILE_DISSOLUTION_IN_PROGRESS = new SystemMessageId(551);
		CANNOT_SET_CREST_WHILE_DISSOLUTION_IN_PROGRESS = new SystemMessageId(552);
		OPPOSING_CLAN_APPLIED_DISPERSION = new SystemMessageId(553);
		CANNOT_DISPERSE_THE_CLANS_IN_ALLY = new SystemMessageId(554);
		CANT_MOVE_TOO_ENCUMBERED = new SystemMessageId(555);
		CANT_MOVE_IN_THIS_STATE = new SystemMessageId(556);
		PET_SUMMONED_MAY_NOT_DESTROYED = new SystemMessageId(557);
		PET_SUMMONED_MAY_NOT_LET_GO = new SystemMessageId(558);
		PURCHASED_S2_FROM_C1 = new SystemMessageId(559);
		PURCHASED_S2_S3_FROM_C1 = new SystemMessageId(560);
		PURCHASED_S3_S2_S_FROM_C1 = new SystemMessageId(561);
		CRYSTALLIZE_LEVEL_TOO_LOW = new SystemMessageId(562);
		FAILED_DISABLE_TARGET = new SystemMessageId(563);
		FAILED_CHANGE_TARGET = new SystemMessageId(564);
		NOT_ENOUGH_LUCK = new SystemMessageId(565);
		CONFUSION_FAILED = new SystemMessageId(566);
		FEAR_FAILED = new SystemMessageId(567);
		CUBIC_SUMMONING_FAILED = new SystemMessageId(568);
		C1_INVITED_YOU_TO_PARTY_FINDERS_KEEPERS = new SystemMessageId(572);
		C1_INVITED_YOU_TO_PARTY_RANDOM = new SystemMessageId(573);
		PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME = new SystemMessageId(574);
		HOW_MUCH_ADENA_TRANSFER_TO_PET = new SystemMessageId(575);
		HOW_MUCH_TRANSFER2 = new SystemMessageId(576);
		CANNOT_SUMMON_DURING_TRADE_SHOP = new SystemMessageId(577);
		YOU_CANNOT_SUMMON_IN_COMBAT = new SystemMessageId(578);
		PET_CANNOT_SENT_BACK_DURING_BATTLE = new SystemMessageId(579);
		SUMMON_ONLY_ONE = new SystemMessageId(580);
		NAMING_THERE_IS_A_SPACE = new SystemMessageId(581);
		NAMING_INAPPROPRIATE_CHARACTER_NAME = new SystemMessageId(582);
		NAMING_INCLUDES_FORBIDDEN_WORDS = new SystemMessageId(583);
		NAMING_ALREADY_IN_USE_BY_ANOTHER_PET = new SystemMessageId(584);
		DECIDE_ON_PRICE = new SystemMessageId(585);
		PET_NO_SHORTCUT = new SystemMessageId(586);
		PET_INVENTORY_FULL = new SystemMessageId(588);
		DEAD_PET_CANNOT_BE_RETURNED = new SystemMessageId(589);
		CANNOT_GIVE_ITEMS_TO_DEAD_PET = new SystemMessageId(590);
		NAMING_PETNAME_CONTAINS_INVALID_CHARS = new SystemMessageId(591);
		WISH_TO_DISMISS_PET = new SystemMessageId(592);
		STARVING_GRUMPY_AND_FED_UP_YOUR_PET_HAS_LEFT = new SystemMessageId(593);
		YOU_CANNOT_RESTORE_HUNGRY_PETS = new SystemMessageId(594);
		YOUR_PET_IS_VERY_HUNGRY = new SystemMessageId(595);
		YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY = new SystemMessageId(596);
		YOUR_PET_IS_VERY_HUNGRY_PLEASE_BE_CAREFULL = new SystemMessageId(597);
		NOT_CHAT_WHILE_INVISIBLE = new SystemMessageId(598);
		GM_NOTICE_CHAT_DISABLED = new SystemMessageId(599);
		CANNOT_EQUIP_PET_ITEM = new SystemMessageId(600);
		S1_PETITION_ON_WAITING_LIST = new SystemMessageId(601);
		PETITION_SYSTEM_CURRENT_UNAVAILABLE = new SystemMessageId(602);
		CANNOT_DISCARD_EXCHANGE_ITEM = new SystemMessageId(603);
		NOT_CALL_PET_FROM_THIS_LOCATION = new SystemMessageId(604);
		MAY_REGISTER_UP_TO_64_PEOPLE = new SystemMessageId(605);
		OTHER_PERSON_ALREADY_64_PEOPLE = new SystemMessageId(606);
		DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1 = new SystemMessageId(607);
		C1_SWEEPED_UP_S3_S2 = new SystemMessageId(608);
		C1_SWEEPED_UP_S2 = new SystemMessageId(609);
		SKILL_REMOVED_DUE_LACK_HP = new SystemMessageId(610);
		CONFUSING_SUCCEEDED = new SystemMessageId(611);
		SPOIL_SUCCESS = new SystemMessageId(612);
		BLOCK_LIST_HEADER = new SystemMessageId(613);
		C1_D_C2 = new SystemMessageId(614);
		FAILED_TO_REGISTER_TO_IGNORE_LIST = new SystemMessageId(615);
		FAILED_TO_DELETE_CHARACTER = new SystemMessageId(616);
		S1_WAS_ADDED_TO_YOUR_IGNORE_LIST = new SystemMessageId(617);
		S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST = new SystemMessageId(618);
		S1_HAS_ADDED_YOU_TO_IGNORE_LIST = new SystemMessageId(619);
		S1_HAS_ADDED_YOU_TO_IGNORE_LIST2 = new SystemMessageId(620);
		CONNECTION_RESTRICTED_IP = new SystemMessageId(621);
		NO_WAR_DURING_ALLY_BATTLE = new SystemMessageId(622);
		OPPONENT_TOO_MUCH_ALLY_BATTLES1 = new SystemMessageId(623);
		S1_LEADER_NOT_CONNECTED = new SystemMessageId(624);
		ALLY_BATTLE_TRUCE_DENIED = new SystemMessageId(625);
		WAR_PROCLAMATION_HAS_BEEN_REFUSED = new SystemMessageId(626);
		YOU_REFUSED_CLAN_WAR_PROCLAMATION = new SystemMessageId(627);
		ALREADY_AT_WAR_WITH_S1_WAIT_5_DAYS = new SystemMessageId(628);
		OPPONENT_TOO_MUCH_ALLY_BATTLES2 = new SystemMessageId(629);
		WAR_WITH_CLAN_BEGUN = new SystemMessageId(630);
		WAR_WITH_CLAN_ENDED = new SystemMessageId(631);
		WON_WAR_OVER_CLAN = new SystemMessageId(632);
		SURRENDERED_TO_CLAN = new SystemMessageId(633);
		DEFEATED_BY_CLAN = new SystemMessageId(634);
		TIME_UP_WAR_OVER = new SystemMessageId(635);
		NOT_INVOLVED_IN_WAR = new SystemMessageId(636);
		ALLY_REGISTERED_SELF_TO_OPPONENT = new SystemMessageId(637);
		ALREADY_REQUESTED_SIEGE_BATTLE = new SystemMessageId(638);
		APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE = new SystemMessageId(639);
		FAILED_TO_REFUSE_CASTLE_DEFENSE_AID = new SystemMessageId(640);
		FAILED_TO_APPROVE_CASTLE_DEFENSE_AID = new SystemMessageId(641);
		ALREADY_ATTACKER_NOT_CANCEL = new SystemMessageId(642);
		ALREADY_DEFENDER_NOT_CANCEL = new SystemMessageId(643);
		NOT_REGISTERED_FOR_SIEGE = new SystemMessageId(644);
		ONLY_CLAN_LEVEL_5_ABOVE_MAY_SIEGE = new SystemMessageId(645);
		DO_NOT_HAVE_AUTHORITY_TO_MODIFY_CASTLE_DEFENDER_LIST = new SystemMessageId(646);
		DO_NOT_HAVE_AUTHORITY_TO_MODIFY_SIEGE_TIME = new SystemMessageId(647);
		ATTACKER_SIDE_FULL = new SystemMessageId(648);
		DEFENDER_SIDE_FULL = new SystemMessageId(649);
		YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION = new SystemMessageId(650);
		PLACE_CURRENT_LOCATION_DIRECTION = new SystemMessageId(651);
		TARGET_OF_SUMMON_WRONG = new SystemMessageId(652);
		YOU_DO_NOT_HAVE_AUTHORITY_TO_POSITION_MERCENARIES = new SystemMessageId(653);
		YOU_DO_NOT_HAVE_AUTHORITY_TO_CANCEL_MERCENARY_POSITIONING = new SystemMessageId(654);
		MERCENARIES_CANNOT_BE_POSITIONED_HERE = new SystemMessageId(655);
		THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE = new SystemMessageId(656);
		POSITIONING_CANNOT_BE_DONE_BECAUSE_DISTANCE_BETWEEN_MERCENARIES_TOO_SHORT = new SystemMessageId(657);
		THIS_IS_NOT_A_MERCENARY_OF_A_CASTLE_THAT_YOU_OWN_AND_SO_CANNOT_CANCEL_POSITIONING = new SystemMessageId(658);
		NOT_SIEGE_REGISTRATION_TIME1 = new SystemMessageId(659);
		NOT_SIEGE_REGISTRATION_TIME2 = new SystemMessageId(660);
		SPOIL_CANNOT_USE = new SystemMessageId(661);
		THE_PLAYER_IS_REJECTING_FRIEND_INVITATIONS = new SystemMessageId(662);
		SIEGE_TIME_DECLARED_FOR_S1 = new SystemMessageId(663);
		CHOOSE_PERSON_TO_RECEIVE = new SystemMessageId(664);
		APPLYING_ALLIANCE_WAR = new SystemMessageId(665);
		REQUEST_FOR_CEASEFIRE = new SystemMessageId(666);
		REGISTERING_ON_ATTACKING_SIDE = new SystemMessageId(667);
		REGISTERING_ON_DEFENDING_SIDE = new SystemMessageId(668);
		CANCELING_REGISTRATION = new SystemMessageId(669);
		REFUSING_REGISTRATION = new SystemMessageId(670);
		AGREEING_REGISTRATION = new SystemMessageId(671);
		S1_DISAPPEARED_ADENA = new SystemMessageId(672);
		AUCTION_ONLY_CLAN_LEVEL_2_HIGHER = new SystemMessageId(673);
		NOT_SEVEN_DAYS_SINCE_CANCELING_AUCTION = new SystemMessageId(674);
		NO_CLAN_HALLS_UP_FOR_AUCTION = new SystemMessageId(675);
		ALREADY_SUBMITTED_BID = new SystemMessageId(676);
		BID_PRICE_MUST_BE_HIGHER = new SystemMessageId(677);
		SUBMITTED_A_BID_OF_S1 = new SystemMessageId(678);
		CANCELED_BID = new SystemMessageId(679);
		CANNOT_PARTICIPATE_IN_AN_AUCTION = new SystemMessageId(680);
		CLAN_HAS_NO_CLAN_HALL = new SystemMessageId(681);
		MOVING_TO_ANOTHER_VILLAGE = new SystemMessageId(681);
		SWEEP_NOT_ALLOWED = new SystemMessageId(683);
		CANNOT_POSITION_MERCS_DURING_SIEGE = new SystemMessageId(684);
		CANNOT_DECLARE_WAR_ON_ALLY = new SystemMessageId(685);
		S1_DAMAGE_FROM_FIRE_MAGIC = new SystemMessageId(686);
		CANNOT_MOVE_FROZEN = new SystemMessageId(687);
		CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING = new SystemMessageId(688);
		CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE = new SystemMessageId(689);
		CANNOT_ATTACK_ALLIANCE_CASTLE = new SystemMessageId(690);
		S1_CLAN_ALREADY_MEMBER_OF_S2_ALLIANCE = new SystemMessageId(691);
		OTHER_PARTY_IS_FROZEN = new SystemMessageId(692);
		PACKAGE_IN_ANOTHER_WAREHOUSE = new SystemMessageId(693);
		NO_PACKAGES_ARRIVED = new SystemMessageId(694);
		NAMING_YOU_CANNOT_SET_NAME_OF_THE_PET = new SystemMessageId(695);
		ITEM_ENCHANT_VALUE_STRANGE = new SystemMessageId(697);
		PRICE_DIFFERENT_FROM_SALES_LIST = new SystemMessageId(698);
		CURRENTLY_NOT_PURCHASING = new SystemMessageId(699);
		THE_PURCHASE_IS_COMPLETE = new SystemMessageId(700);
		NOT_ENOUGH_REQUIRED_ITEMS = new SystemMessageId(701);
		NO_GM_PROVIDING_SERVICE_NOW = new SystemMessageId(702);
		GM_LIST = new SystemMessageId(703);
		GM_C1 = new SystemMessageId(704);
		CANNOT_EXCLUDE_SELF = new SystemMessageId(705);
		ONLY_64_NAMES_ON_EXCLUDE_LIST = new SystemMessageId(706);
		NO_PORT_THAT_IS_IN_SIGE = new SystemMessageId(707);
		YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CASTLE_WAREHOUSE = new SystemMessageId(708);
		YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE = new SystemMessageId(709);
		ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE = new SystemMessageId(710);
		SIEGE_OF_S1_HAS_STARTED = new SystemMessageId(711);
		SIEGE_OF_S1_HAS_ENDED = new SystemMessageId(712);
		S1_S2_S3_D = new SystemMessageId(713);
		A_TRAP_DEVICE_HAS_BEEN_TRIPPED = new SystemMessageId(714);
		A_TRAP_DEVICE_HAS_BEEN_STOPPED = new SystemMessageId(715);
		NO_RESURRECTION_WITHOUT_BASE_CAMP = new SystemMessageId(716);
		TOWER_DESTROYED_NO_RESURRECTION = new SystemMessageId(717);
		GATES_NOT_OPENED_CLOSED_DURING_SIEGE = new SystemMessageId(718);
		ITEM_MIXING_FAILED = new SystemMessageId(719);
		THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY = new SystemMessageId(720);
		NO_ALLY_CREATION_WHILE_SIEGE = new SystemMessageId(721);
		CANNOT_DISSOLVE_ALLY_WHILE_IN_SIEGE = new SystemMessageId(722);
		OPPOSING_CLAN_IS_PARTICIPATING_IN_SIEGE = new SystemMessageId(723);
		CANNOT_LEAVE_WHILE_SIEGE = new SystemMessageId(724);
		CANNOT_DISMISS_WHILE_SIEGE = new SystemMessageId(725);
		FROZEN_CONDITION_STARTED = new SystemMessageId(726);
		FROZEN_CONDITION_REMOVED = new SystemMessageId(727);
		CANNOT_APPLY_DISSOLUTION_AGAIN = new SystemMessageId(728);
		ITEM_NOT_DISCARDED = new SystemMessageId(729);
		SUBMITTED_YOU_S1_TH_PETITION_S2_LEFT = new SystemMessageId(730);
		PETITION_S1_RECEIVED_CODE_IS_S2 = new SystemMessageId(731);
		C1_RECEIVED_CONSULTATION_REQUEST = new SystemMessageId(732);
		WE_HAVE_RECEIVED_S1_PETITIONS_TODAY = new SystemMessageId(733);
		PETITION_FAILED_C1_ALREADY_SUBMITTED = new SystemMessageId(734);
		PETITION_FAILED_FOR_C1_ERROR_NUMBER_S2 = new SystemMessageId(735);
		PETITION_CANCELED_SUBMIT_S1_MORE_TODAY = new SystemMessageId(736);
		CANCELED_PETITION_ON_S1 = new SystemMessageId(737);
		PETITION_NOT_SUBMITTED = new SystemMessageId(738);
		PETITION_CANCEL_FAILED_FOR_C1_ERROR_NUMBER_S2 = new SystemMessageId(739);
		C1_PARTICIPATE_PETITION = new SystemMessageId(740);
		FAILED_ADDING_C1_TO_PETITION = new SystemMessageId(741);
		PETITION_ADDING_C1_FAILED_ERROR_NUMBER_S2 = new SystemMessageId(742);
		C1_LEFT_PETITION_CHAT = new SystemMessageId(743);
		PETITION_REMOVING_S1_FAILED_ERROR_NUMBER_S2 = new SystemMessageId(744);
		YOU_ARE_NOT_IN_PETITION_CHAT = new SystemMessageId(745);
		CURRENTLY_NO_PETITION = new SystemMessageId(746);
		DIST_TOO_FAR_CASTING_STOPPED = new SystemMessageId(748);
		EFFECT_S1_DISAPPEARED = new SystemMessageId(749);
		NO_MORE_SKILLS_TO_LEARN = new SystemMessageId(750);
		CANNOT_INVITE_CONFLICT_CLAN = new SystemMessageId(751);
		CANNOT_USE_NAME = new SystemMessageId(752);
		NO_MERCS_HERE = new SystemMessageId(753);
		S1_HOURS_S2_MINUTES_LEFT_THIS_WEEK = new SystemMessageId(754);
		S1_MINUTES_LEFT_THIS_WEEK = new SystemMessageId(755);
		WEEKS_USAGE_TIME_FINISHED = new SystemMessageId(756);
		S1_HOURS_S2_MINUTES_LEFT_IN_TIME = new SystemMessageId(757);
		S1_HOURS_S2_MINUTES_LEFT_THIS_WEEKS_PLAY_TIME = new SystemMessageId(758);
		S1_MINUTES_LEFT_THIS_WEEKS_PLAY_TIME = new SystemMessageId(759);
		C1_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN = new SystemMessageId(760);
		S1_CANT_ENTER_ALLIANCE_WITHIN_1_DAY = new SystemMessageId(761);
		C1_ROLLED_S2_S3_EYE_CAME_OUT = new SystemMessageId(762);
		FAILED_SENDING_PACKAGE_TOO_FAR = new SystemMessageId(763);
		PLAYING_FOR_LONG_TIME = new SystemMessageId(764);
		HACKING_TOOL = new SystemMessageId(769);
		PLAY_TIME_NO_LONGER_ACCUMULATING = new SystemMessageId(774);
		PLAY_TIME_EXPENDED = new SystemMessageId(775);
		CLANHALL_AWARDED_TO_CLAN = new SystemMessageId(776);
		CLANHALL_NOT_SOLD = new SystemMessageId(777);
		NO_LOGOUT_HERE = new SystemMessageId(778);
		NO_RESTART_HERE = new SystemMessageId(779);
		ONLY_VIEW_SIEGE = new SystemMessageId(780);
		OBSERVERS_CANNOT_PARTICIPATE = new SystemMessageId(781);
		NO_OBSERVE_WITH_PET = new SystemMessageId(782);
		LOTTERY_TICKET_SALES_TEMP_SUSPENDED = new SystemMessageId(783);
		NO_LOTTERY_TICKETS_AVAILABLE = new SystemMessageId(784);
		LOTTERY_S1_RESULT_NOT_PUBLISHED = new SystemMessageId(785);
		INCORRECT_SYNTAX = new SystemMessageId(786);
		CLANHALL_SIEGE_TRYOUTS_FINISHED = new SystemMessageId(787);
		CLANHALL_SIEGE_FINALS_FINISHED = new SystemMessageId(788);
		CLANHALL_SIEGE_TRYOUTS_BEGUN = new SystemMessageId(789);
		CLANHALL_SIEGE_FINALS_BEGUN = new SystemMessageId(790);
		FINAL_MATCH_BEGIN = new SystemMessageId(791);
		CLANHALL_SIEGE_ENDED = new SystemMessageId(792);
		CLANHALL_SIEGE_BEGUN = new SystemMessageId(793);
		YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT = new SystemMessageId(794);
		ONLY_LEADERS_CAN_SET_RIGHTS = new SystemMessageId(795);
		REMAINING_OBSERVATION_TIME = new SystemMessageId(796);
		YOU_MAY_CREATE_UP_TO_48_MACROS = new SystemMessageId(797);
		ITEM_REGISTRATION_IRREVERSIBLE = new SystemMessageId(798);
		OBSERVATION_TIME_EXPIRED = new SystemMessageId(799);
		REGISTRATION_PERIOD_OVER = new SystemMessageId(800);
		REGISTRATION_CLOSED = new SystemMessageId(801);
		PETITION_NOT_ACCEPTED_NOW = new SystemMessageId(802);
		PETITION_NOT_SPECIFIED = new SystemMessageId(803);
		SELECT_TYPE = new SystemMessageId(804);
		PETITION_NOT_ACCEPTED_SUBMIT_AT_S1 = new SystemMessageId(805);
		TRY_UNSTUCK_WHEN_TRAPPED = new SystemMessageId(806);
		STUCK_PREPARE_FOR_TRANSPORT = new SystemMessageId(807);
		STUCK_SUBMIT_PETITION = new SystemMessageId(808);
		STUCK_TRANSPORT_IN_FIVE_MINUTES = new SystemMessageId(809);
		INVALID_MACRO = new SystemMessageId(810);
		WILL_BE_MOVED = new SystemMessageId(811);
		TRAP_DID_S1_DAMAGE = new SystemMessageId(812);
		POISONED_BY_TRAP = new SystemMessageId(813);
		SLOWED_BY_TRAP = new SystemMessageId(814);
		TRYOUTS_ABOUT_TO_BEGIN = new SystemMessageId(815);
		MONSRACE_TICKETS_AVAILABLE_FOR_S1_RACE = new SystemMessageId(816);
		MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE = new SystemMessageId(817);
		MONSRACE_TICKETS_STOP_IN_S1_MINUTES = new SystemMessageId(818);
		MONSRACE_S1_TICKET_SALES_CLOSED = new SystemMessageId(819);
		MONSRACE_S2_BEGINS_IN_S1_MINUTES = new SystemMessageId(820);
		MONSRACE_S1_BEGINS_IN_30_SECONDS = new SystemMessageId(821);
		MONSRACE_S1_COUNTDOWN_IN_FIVE_SECONDS = new SystemMessageId(822);
		MONSRACE_BEGINS_IN_S1_SECONDS = new SystemMessageId(823);
		MONSRACE_RACE_START = new SystemMessageId(824);
		MONSRACE_S1_RACE_END = new SystemMessageId(825);
		MONSRACE_FIRST_PLACE_S1_SECOND_S2 = new SystemMessageId(826);
		YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_GM = new SystemMessageId(827);
		WISH_TO_DELETE_S1_MACRO = new SystemMessageId(828);
		YOU_CANNOT_RECOMMEND_YOURSELF = new SystemMessageId(829);
		YOU_HAVE_RECOMMENDED_C1_YOU_HAVE_S2_RECOMMENDATIONS_LEFT = new SystemMessageId(830);
		YOU_HAVE_BEEN_RECOMMENDED_BY_C1 = new SystemMessageId(831);
		THAT_CHARACTER_IS_RECOMMENDED = new SystemMessageId(832);
		NO_MORE_RECOMMENDATIONS_TO_HAVE = new SystemMessageId(833);
		C1_ROLLED_S2 = new SystemMessageId(834);
		YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER = new SystemMessageId(835);
		YOU_HAVE_EXCEEDED_YOUR_INVENTORY_VOLUME_LIMIT_AND_CANNOT_TAKE_THIS_ITEM = new SystemMessageId(836);
		MACRO_DESCRIPTION_MAX_32_CHARS = new SystemMessageId(837);
		ENTER_THE_MACRO_NAME = new SystemMessageId(838);
		MACRO_NAME_ALREADY_USED = new SystemMessageId(839);
		RECIPE_ALREADY_REGISTERED = new SystemMessageId(840);
		NO_FUTHER_RECIPES_CAN_BE_ADDED = new SystemMessageId(841);
		NOT_AUTHORIZED_REGISTER_RECIPE = new SystemMessageId(842);
		SIEGE_OF_S1_FINISHED = new SystemMessageId(843);
		SIEGE_OF_S1_BEGUN = new SystemMessageId(844);
		DEADLINE_FOR_SIEGE_S1_PASSED = new SystemMessageId(845);
		SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST = new SystemMessageId(846);
		CLAN_OWNING_CLANHALL_MAY_NOT_SIEGE_CLANHALL = new SystemMessageId(847);
		S1_HAS_BEEN_DELETED = new SystemMessageId(848);
		S1_NOT_FOUND = new SystemMessageId(849);
		S1_ALREADY_EXISTS2 = new SystemMessageId(850);
		S1_ADDED = new SystemMessageId(851);
		RECIPE_INCORRECT = new SystemMessageId(852);
		CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING = new SystemMessageId(853);
		MISSING_S2_S1_TO_CREATE = new SystemMessageId(854);
		S1_CLAN_DEFEATED_S2 = new SystemMessageId(855);
		SIEGE_S1_DRAW = new SystemMessageId(856);
		S1_CLAN_WON_MATCH_S2 = new SystemMessageId(857);
		MATCH_OF_S1_DRAW = new SystemMessageId(858);
		PLEASE_REGISTER_RECIPE = new SystemMessageId(859);
		HEADQUARTERS_TOO_CLOSE = new SystemMessageId(860);
		TOO_MANY_MEMOS = new SystemMessageId(861);
		ODDS_NOT_POSTED = new SystemMessageId(862);
		FEEL_ENERGY_FIRE = new SystemMessageId(863);
		FEEL_ENERGY_WATER = new SystemMessageId(864);
		FEEL_ENERGY_WIND = new SystemMessageId(865);
		NO_LONGER_ENERGY = new SystemMessageId(866);
		ENERGY_DEPLETED = new SystemMessageId(867);
		ENERGY_FIRE_DELIVERED = new SystemMessageId(868);
		ENERGY_WATER_DELIVERED = new SystemMessageId(869);
		ENERGY_WIND_DELIVERED = new SystemMessageId(870);
		THE_SEED_HAS_BEEN_SOWN = new SystemMessageId(871);
		THIS_SEED_MAY_NOT_BE_SOWN_HERE = new SystemMessageId(872);
		CHARACTER_DOES_NOT_EXIST = new SystemMessageId(873);
		WAREHOUSE_CAPACITY_EXCEEDED = new SystemMessageId(874);
		CARGO_CANCELED = new SystemMessageId(875);
		CARGO_NOT_DELIVERED = new SystemMessageId(876);
		SYMBOL_ADDED = new SystemMessageId(877);
		SYMBOL_DELETED = new SystemMessageId(878);
		THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE = new SystemMessageId(879);
		THE_TRANSACTION_IS_COMPLETE = new SystemMessageId(880);
		THERE_IS_A_DISCREPANCY_ON_THE_INVOICE = new SystemMessageId(881);
		THE_SEED_QUANTITY_IS_INCORRECT = new SystemMessageId(882);
		THE_SEED_INFORMATION_IS_INCORRECT = new SystemMessageId(883);
		THE_MANOR_INFORMATION_HAS_BEEN_UPDATED = new SystemMessageId(884);
		THE_NUMBER_OF_CROPS_IS_INCORRECT = new SystemMessageId(885);
		THE_CROPS_ARE_PRICED_INCORRECTLY = new SystemMessageId(886);
		THE_TYPE_IS_INCORRECT = new SystemMessageId(887);
		NO_CROPS_CAN_BE_PURCHASED_AT_THIS_TIME = new SystemMessageId(888);
		THE_SEED_WAS_SUCCESSFULLY_SOWN = new SystemMessageId(889);
		THE_SEED_WAS_NOT_SOWN = new SystemMessageId(890);
		YOU_ARE_NOT_AUTHORIZED_TO_HARVEST = new SystemMessageId(891);
		THE_HARVEST_HAS_FAILED = new SystemMessageId(892);
		THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN = new SystemMessageId(893);
		UP_TO_S1_RECIPES_CAN_REGISTER = new SystemMessageId(894);
		NO_RECIPES_REGISTERED = new SystemMessageId(895);
		FERRY_AT_GLUDIN = new SystemMessageId(896);
		FERRY_LEAVE_TALKING = new SystemMessageId(897);
		ONLY_LEVEL_SUP_10_CAN_RECOMMEND = new SystemMessageId(898);
		CANT_DRAW_SYMBOL = new SystemMessageId(899);
		SYMBOLS_FULL = new SystemMessageId(900);
		SYMBOL_NOT_FOUND = new SystemMessageId(901);
		NUMBER_INCORRECT = new SystemMessageId(902);
		NO_PETITION_WHILE_FROZEN = new SystemMessageId(903);
		NO_DISCARD_WHILE_PRIVATE_STORE = new SystemMessageId(904);
		HUMAN_SCORE_S1 = new SystemMessageId(905);
		ELVES_SCORE_S1 = new SystemMessageId(906);
		DARK_ELVES_SCORE_S1 = new SystemMessageId(907);
		ORCS_SCORE_S1 = new SystemMessageId(908);
		DWARVEN_SCORE_S1 = new SystemMessageId(909);
		LOC_TI_S1_S2_S3 = new SystemMessageId(910);
		LOC_GLUDIN_S1_S2_S3 = new SystemMessageId(911);
		LOC_GLUDIO_S1_S2_S3 = new SystemMessageId(912);
		LOC_NEUTRAL_ZONE_S1_S2_S3 = new SystemMessageId(913);
		LOC_ELVEN_S1_S2_S3 = new SystemMessageId(914);
		LOC_DARK_ELVEN_S1_S2_S3 = new SystemMessageId(915);
		LOC_DION_S1_S2_S3 = new SystemMessageId(916);
		LOC_FLORAN_S1_S2_S3 = new SystemMessageId(917);
		LOC_GIRAN_S1_S2_S3 = new SystemMessageId(918);
		LOC_GIRAN_HARBOR_S1_S2_S3 = new SystemMessageId(919);
		LOC_ORC_S1_S2_S3 = new SystemMessageId(920);
		LOC_DWARVEN_S1_S2_S3 = new SystemMessageId(921);
		LOC_OREN_S1_S2_S3 = new SystemMessageId(922);
		LOC_HUNTER_S1_S2_S3 = new SystemMessageId(923);
		LOC_ADEN_S1_S2_S3 = new SystemMessageId(924);
		LOC_COLISEUM_S1_S2_S3 = new SystemMessageId(925);
		LOC_HEINE_S1_S2_S3 = new SystemMessageId(926);
		TIME_S1_S2_IN_THE_DAY = new SystemMessageId(927);
		TIME_S1_S2_IN_THE_NIGHT = new SystemMessageId(928);
		NO_COMPENSATION_FOR_FARM_PRODUCTS = new SystemMessageId(929);
		NO_LOTTERY_TICKETS_CURRENT_SOLD = new SystemMessageId(930);
		LOTTERY_WINNERS_NOT_ANNOUNCED_YET = new SystemMessageId(931);
		NO_ALLCHAT_WHILE_OBSERVING = new SystemMessageId(932);
		THE_SEED_PRICING_GREATLY_DIFFERS_FROM_STANDARD_SEED_PRICES = new SystemMessageId(933);
		A_DELETED_RECIPE = new SystemMessageId(934);
		THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION = new SystemMessageId(935);
		USE_S1_ = new SystemMessageId(936);
		PREPARING_PRIVATE_WORKSHOP = new SystemMessageId(937);
		CB_OFFLINE = new SystemMessageId(938);
		NO_EXCHANGE_WHILE_BLOCKING = new SystemMessageId(939);
		S1_BLOCKED_EVERYTHING = new SystemMessageId(940);
		RESTART_AT_TI = new SystemMessageId(941);
		RESTART_AT_GLUDIN = new SystemMessageId(942);
		RESTART_AT_GLUDIO = new SystemMessageId(943);
		RESTART_AT_NEUTRAL_ZONE = new SystemMessageId(944);
		RESTART_AT_ELFEN_VILLAGE = new SystemMessageId(945);
		RESTART_AT_DARKELF_VILLAGE = new SystemMessageId(946);
		RESTART_AT_DION = new SystemMessageId(947);
		RESTART_AT_FLORAN = new SystemMessageId(948);
		RESTART_AT_GIRAN = new SystemMessageId(949);
		RESTART_AT_GIRAN_HARBOR = new SystemMessageId(950);
		RESTART_AT_ORC_VILLAGE = new SystemMessageId(951);
		RESTART_AT_DWARFEN_VILLAGE = new SystemMessageId(952);
		RESTART_AT_OREN = new SystemMessageId(953);
		RESTART_AT_HUNTERS_VILLAGE = new SystemMessageId(954);
		RESTART_AT_ADEN = new SystemMessageId(955);
		RESTART_AT_COLISEUM = new SystemMessageId(956);
		RESTART_AT_HEINE = new SystemMessageId(957);
		ITEMS_CANNOT_BE_DISCARDED_OR_DESTROYED_WHILE_OPERATING_PRIVATE_STORE_OR_WORKSHOP = new SystemMessageId(958);
		S1_S2_MANUFACTURED_SUCCESSFULLY = new SystemMessageId(959);
		S1_MANUFACTURE_FAILURE = new SystemMessageId(960);
		BLOCKING_ALL = new SystemMessageId(961);
		NOT_BLOCKING_ALL = new SystemMessageId(962);
		DETERMINE_MANUFACTURE_PRICE = new SystemMessageId(963);
		CHATBAN_FOR_1_MINUTE = new SystemMessageId(964);
		CHATBAN_REMOVED = new SystemMessageId(965);
		CHATTING_IS_CURRENTLY_PROHIBITED = new SystemMessageId(966);
		C1_PARTY_INVITE_RANDOM_INCLUDING_SPOIL = new SystemMessageId(967);
		C1_PARTY_INVITE_BY_TURN = new SystemMessageId(968);
		C1_PARTY_INVITE_BY_TURN_INCLUDING_SPOIL = new SystemMessageId(969);
		S2_MP_HAS_BEEN_DRAINED_BY_C1 = new SystemMessageId(970);
		PETITION_MAX_CHARS_255 = new SystemMessageId(971);
		PET_CANNOT_USE_ITEM = new SystemMessageId(972);
		INPUT_NO_MORE_YOU_HAVE = new SystemMessageId(973);
		SOUL_CRYSTAL_ABSORBING_SUCCEEDED = new SystemMessageId(974);
		SOUL_CRYSTAL_ABSORBING_FAILED = new SystemMessageId(975);
		SOUL_CRYSTAL_BROKE = new SystemMessageId(976);
		SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION = new SystemMessageId(977);
		SOUL_CRYSTAL_ABSORBING_REFUSED = new SystemMessageId(978);
		FERRY_ARRIVED_AT_TALKING = new SystemMessageId(979);
		FERRY_LEAVE_FOR_GLUDIN_AFTER_10_MINUTES = new SystemMessageId(980);
		FERRY_LEAVE_FOR_GLUDIN_IN_5_MINUTES = new SystemMessageId(981);
		FERRY_LEAVE_FOR_GLUDIN_IN_1_MINUTE = new SystemMessageId(982);
		MAKE_HASTE_GET_ON_BOAT = new SystemMessageId(983);
		FERRY_LEAVE_SOON_FOR_GLUDIN = new SystemMessageId(984);
		FERRY_LEAVING_FOR_GLUDIN = new SystemMessageId(985);
		FERRY_ARRIVED_AT_GLUDIN = new SystemMessageId(986);
		FERRY_LEAVE_FOR_TALKING_AFTER_10_MINUTES = new SystemMessageId(987);
		FERRY_LEAVE_FOR_TALKING_IN_5_MINUTES = new SystemMessageId(988);
		FERRY_LEAVE_FOR_TALKING_IN_1_MINUTE = new SystemMessageId(989);
		FERRY_LEAVE_SOON_FOR_TALKING = new SystemMessageId(990);
		FERRY_LEAVING_FOR_TALKING = new SystemMessageId(991);
		FERRY_ARRIVED_AT_GIRAN = new SystemMessageId(992);
		FERRY_LEAVE_FOR_GIRAN_AFTER_10_MINUTES = new SystemMessageId(993);
		FERRY_LEAVE_FOR_GIRAN_IN_5_MINUTES = new SystemMessageId(994);
		FERRY_LEAVE_FOR_GIRAN_IN_1_MINUTE = new SystemMessageId(995);
		FERRY_LEAVE_SOON_FOR_GIRAN = new SystemMessageId(996);
		FERRY_LEAVING_FOR_GIRAN = new SystemMessageId(997);
		INNADRIL_BOAT_ANCHOR_10_MINUTES = new SystemMessageId(998);
		INNADRIL_BOAT_LEAVE_IN_5_MINUTES = new SystemMessageId(999);
		INNADRIL_BOAT_LEAVE_IN_1_MINUTE = new SystemMessageId(1000);
		INNADRIL_BOAT_LEAVE_SOON = new SystemMessageId(1001);
		INNADRIL_BOAT_LEAVING = new SystemMessageId(1002);
		CANNOT_POSSES_MONS_TICKET = new SystemMessageId(1003);
		REGISTERED_FOR_CLANHALL = new SystemMessageId(1004);
		NOT_ENOUGH_ADENA_IN_CWH = new SystemMessageId(1005);
		BID_IN_CLANHALL_AUCTION = new SystemMessageId(1006);
		PRELIMINARY_REGISTRATION_OF_S1_FINISHED = new SystemMessageId(1007);
		HUNGRY_STRIDER_NOT_MOUNT = new SystemMessageId(1008);
		STRIDER_CANT_BE_RIDDEN_WHILE_DEAD = new SystemMessageId(1009);
		DEAD_STRIDER_CANT_BE_RIDDEN = new SystemMessageId(1010);
		STRIDER_IN_BATLLE_CANT_BE_RIDDEN = new SystemMessageId(1011);
		STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE = new SystemMessageId(1012);
		STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING = new SystemMessageId(1013);
		PET_EARNED_S1_EXP = new SystemMessageId(1014);
		PET_HIT_FOR_S1_DAMAGE = new SystemMessageId(1015);
		PET_RECEIVED_S2_DAMAGE_BY_C1 = new SystemMessageId(1016);
		CRITICAL_HIT_BY_PET = new SystemMessageId(1017);
		PET_USES_S1 = new SystemMessageId(1018);
		PET_USES_S1_ = new SystemMessageId(1019);
		PET_PICKED_S1 = new SystemMessageId(1020);
		PET_PICKED_S2_S1_S = new SystemMessageId(1021);
		PET_PICKED_S1_S2 = new SystemMessageId(1022);
		PET_PICKED_S1_ADENA = new SystemMessageId(1023);
		PET_PUT_ON_S1 = new SystemMessageId(1024);
		PET_TOOK_OFF_S1 = new SystemMessageId(1025);
		SUMMON_GAVE_DAMAGE_S1 = new SystemMessageId(1026);
		SUMMON_RECEIVED_DAMAGE_S2_BY_S1 = new SystemMessageId(1027);
		CRITICAL_HIT_BY_SUMMONED_MOB = new SystemMessageId(1028);
		SUMMONED_MOB_USES_S1 = new SystemMessageId(1029);
		PARTY_INFORMATION = new SystemMessageId(1030);
		LOOTING_FINDERS_KEEPERS = new SystemMessageId(1031);
		LOOTING_RANDOM = new SystemMessageId(1032);
		LOOTING_RANDOM_INCLUDE_SPOIL = new SystemMessageId(1033);
		LOOTING_BY_TURN = new SystemMessageId(1034);
		LOOTING_BY_TURN_INCLUDE_SPOIL = new SystemMessageId(1035);
		YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED = new SystemMessageId(1036);
		C1_MANUFACTURED_S2 = new SystemMessageId(1037);
		C1_MANUFACTURED_S3_S2_S = new SystemMessageId(1038);
		ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE = new SystemMessageId(1039);
		ITEMS_SENT_BY_FREIGHT_PICKED_UP_FROM_ANYWHERE = new SystemMessageId(1040);
		THE_NEXT_SEED_PURCHASE_PRICE_IS_S1_ADENA = new SystemMessageId(1041);
		THE_NEXT_FARM_GOODS_PURCHASE_PRICE_IS_S1_ADENA = new SystemMessageId(1042);
		NO_UNSTUCK_PLEASE_SEND_PETITION = new SystemMessageId(1043);
		MONSRACE_NO_PAYOUT_INFO = new SystemMessageId(1044);
		MONSRACE_TICKETS_NOT_AVAILABLE = new SystemMessageId(1046);
		NOT_SUCCEED_PRODUCING_S1 = new SystemMessageId(1047);
		NO_WHISPER_WHEN_BLOCKING = new SystemMessageId(1048);
		NO_PARTY_WHEN_BLOCKING = new SystemMessageId(1049);
		NO_CB_IN_MY_CLAN = new SystemMessageId(1050);
		PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW = new SystemMessageId(1051);
		THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED = new SystemMessageId(1052);
		CANNOT_BE_RESURRECTED_DURING_SIEGE = new SystemMessageId(1053);
		ENTERED_MYSTICAL_LAND = new SystemMessageId(1054);
		EXITED_MYSTICAL_LAND = new SystemMessageId(1055);
		VAULT_CAPACITY_EXCEEDED = new SystemMessageId(1056);
		RELAX_SERVER_ONLY = new SystemMessageId(1057);
		THE_SALES_PRICE_FOR_SEEDS_IS_S1_ADENA = new SystemMessageId(1058);
		THE_REMAINING_PURCHASING_IS_S1_ADENA = new SystemMessageId(1059);
		THE_REMAINDER_AFTER_SELLING_THE_SEEDS_IS_S1 = new SystemMessageId(1060);
		CANT_REGISTER_NO_ABILITY_TO_CRAFT = new SystemMessageId(1061);
		WRITING_SOMETHING_NEW_POSSIBLE_AFTER_LEVEL_10 = new SystemMessageId(1062);
		PETITION_UNAVAILABLE = new SystemMessageId(1063);
		EQUIPMENT_S1_S2_REMOVED = new SystemMessageId(1064);
		CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE = new SystemMessageId(1065);
		S1_HP_RESTORED = new SystemMessageId(1066);
		S2_HP_RESTORED_BY_C1 = new SystemMessageId(1067);
		S1_MP_RESTORED = new SystemMessageId(1068);
		S2_MP_RESTORED_BY_C1 = new SystemMessageId(1069);
		NO_READ_PERMISSION = new SystemMessageId(1070);
		NO_WRITE_PERMISSION = new SystemMessageId(1071);
		OBTAINED_TICKET_FOR_MONS_RACE_S1_SINGLE = new SystemMessageId(1072);
		OBTAINED_TICKET_FOR_MONS_RACE_S1_SINGLE_ = new SystemMessageId(1073);
		NOT_MEET_AGE_REQUIREMENT_FOR_MONS_RACE = new SystemMessageId(1074);
		BID_AMOUNT_HIGHER_THAN_PREVIOUS_BID = new SystemMessageId(1075);
		GAME_CANNOT_TERMINATE_NOW = new SystemMessageId(1076);
		GG_EXECUTION_ERROR = new SystemMessageId(1077);
		DONT_SPAM = new SystemMessageId(1078);
		TARGET_IS_CHAT_BANNED = new SystemMessageId(1079);
		FACELIFT_POTION_TYPE_A = new SystemMessageId(1080);
		HAIRDYE_POTION_TYPE_A = new SystemMessageId(1081);
		HAIRSTYLE_POTION_TYPE_A = new SystemMessageId(1082);
		FACELIFT_POTION_TYPE_A_APPLIED = new SystemMessageId(1083);
		HAIRDYE_POTION_TYPE_A_APPLIED = new SystemMessageId(1084);
		HAIRSTYLE_POTION_TYPE_A_USED = new SystemMessageId(1085);
		FACE_APPEARANCE_CHANGED = new SystemMessageId(1086);
		HAIR_COLOR_CHANGED = new SystemMessageId(1087);
		HAIR_STYLE_CHANGED = new SystemMessageId(1088);
		C1_OBTAINED_ANNIVERSARY_ITEM = new SystemMessageId(1089);
		FACELIFT_POTION_TYPE_B = new SystemMessageId(1090);
		FACELIFT_POTION_TYPE_C = new SystemMessageId(1091);
		HAIRDYE_POTION_TYPE_B = new SystemMessageId(1092);
		HAIRDYE_POTION_TYPE_C = new SystemMessageId(1093);
		HAIRDYE_POTION_TYPE_D = new SystemMessageId(1094);
		HAIRSTYLE_POTION_TYPE_B = new SystemMessageId(1095);
		HAIRSTYLE_POTION_TYPE_C = new SystemMessageId(1096);
		HAIRSTYLE_POTION_TYPE_D = new SystemMessageId(1097);
		HAIRSTYLE_POTION_TYPE_E = new SystemMessageId(1098);
		HAIRSTYLE_POTION_TYPE_F = new SystemMessageId(1099);
		HAIRSTYLE_POTION_TYPE_G = new SystemMessageId(1100);
		FACELIFT_POTION_TYPE_B_APPLIED = new SystemMessageId(1101);
		FACELIFT_POTION_TYPE_C_APPLIED = new SystemMessageId(1102);
		HAIRDYE_POTION_TYPE_B_APPLIED = new SystemMessageId(1103);
		HAIRDYE_POTION_TYPE_C_APPLIED = new SystemMessageId(1104);
		HAIRDYE_POTION_TYPE_D_APPLIED = new SystemMessageId(1105);
		HAIRSTYLE_POTION_TYPE_B_USED = new SystemMessageId(1106);
		HAIRSTYLE_POTION_TYPE_C_USED = new SystemMessageId(1107);
		HAIRSTYLE_POTION_TYPE_D_USED = new SystemMessageId(1108);
		HAIRSTYLE_POTION_TYPE_E_USED = new SystemMessageId(1109);
		HAIRSTYLE_POTION_TYPE_F_USED = new SystemMessageId(1110);
		HAIRSTYLE_POTION_TYPE_G_USED = new SystemMessageId(1111);
		AMOUNT_FOR_WINNER_S1_IS_S2_ADENA_WE_HAVE_S3_PRIZE_WINNER = new SystemMessageId(1112);
		AMOUNT_FOR_LOTTERY_S1_IS_S2_ADENA_NO_WINNER = new SystemMessageId(1113);
		CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS = new SystemMessageId(1114);
		INDIVIDUALS_NOT_SURRENDER_DURING_COMBAT = new SystemMessageId(1115);
		YOU_CANNOT_LEAVE_DURING_COMBAT = new SystemMessageId(1116);
		CLAN_MEMBER_CANNOT_BE_DISMISSED_DURING_COMBAT = new SystemMessageId(1117);
		INVENTORY_LESS_THAN_80_PERCENT = new SystemMessageId(1118);
		QUEST_CANCELED_INVENTORY_EXCEEDS_80_PERCENT = new SystemMessageId(1119);
		STILL_CLAN_MEMBER = new SystemMessageId(1120);
		NO_RIGHT_TO_VOTE = new SystemMessageId(1121);
		NO_CANDIDATE = new SystemMessageId(1122);
		WEIGHT_EXCEEDED_SKILL_UNAVAILABLE = new SystemMessageId(1123);
		NO_RECIPE_BOOK_WHILE_CASTING = new SystemMessageId(1124);
		CANNOT_CREATED_WHILE_ENGAGED_IN_TRADING = new SystemMessageId(1125);
		NO_NEGATIVE_NUMBER = new SystemMessageId(1126);
		REWARD_LESS_THAN_10_TIMES_STANDARD_PRICE = new SystemMessageId(1127);
		PRIVATE_STORE_NOT_WHILE_CASTING = new SystemMessageId(1128);
		NOT_ALLOWED_ON_BOAT = new SystemMessageId(1129);
		GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR = new SystemMessageId(1130);
		NIGHT_EFFECT_APPLIES = new SystemMessageId(1131);
		DAY_EFFECT_DISAPPEARS = new SystemMessageId(1132);
		HP_DECREASED_EFFECT_APPLIES = new SystemMessageId(1133);
		HP_INCREASED_EFFECT_DISAPPEARS = new SystemMessageId(1134);
		CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT = new SystemMessageId(1135);
		ACCOUNT_NOT_ALLOWED_TO_CONNECT = new SystemMessageId(1136);
		C1_HARVESTED_S3_S2S = new SystemMessageId(1137);
		C1_HARVESTED_S2S = new SystemMessageId(1138);
		INVENTORY_LIMIT_MUST_NOT_BE_EXCEEDED = new SystemMessageId(1139);
		WOULD_YOU_LIKE_TO_OPEN_THE_GATE = new SystemMessageId(1140);
		WOULD_YOU_LIKE_TO_CLOSE_THE_GATE = new SystemMessageId(1141);
		CANNOT_SUMMON_S1_AGAIN = new SystemMessageId(1142);
		SERVITOR_DISAPPEARED_NOT_ENOUGH_ITEMS = new SystemMessageId(1143);
		NOBODY_IN_GAME_TO_CHAT = new SystemMessageId(1144);
		S2_CREATED_FOR_C1_FOR_S3_ADENA = new SystemMessageId(1145);
		C1_CREATED_S2_FOR_S3_ADENA = new SystemMessageId(1146);
		S2_S3_S_CREATED_FOR_C1_FOR_S4_ADENA = new SystemMessageId(1147);
		C1_CREATED_S2_S3_S_FOR_S4_ADENA = new SystemMessageId(1148);
		CREATION_OF_S2_FOR_C1_AT_S3_ADENA_FAILED = new SystemMessageId(1149);
		C1_FAILED_TO_CREATE_S2_FOR_S3_ADENA = new SystemMessageId(1150);
		S2_SOLD_TO_C1_FOR_S3_ADENA = new SystemMessageId(1151);
		S3_S2_S_SOLD_TO_C1_FOR_S4_ADENA = new SystemMessageId(1152);
		S2_PURCHASED_FROM_C1_FOR_S3_ADENA = new SystemMessageId(1153);
		S3_S2_S_PURCHASED_FROM_C1_FOR_S4_ADENA = new SystemMessageId(1154);
		S3_S2_SOLD_TO_C1_FOR_S4_ADENA = new SystemMessageId(1155);
		S2_S3_PURCHASED_FROM_C1_FOR_S4_ADENA = new SystemMessageId(1156);
		TRYING_ON_STATE = new SystemMessageId(1157);
		CANNOT_DISMOUNT_FROM_ELEVATION = new SystemMessageId(1158);
		FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_10_MINUTES = new SystemMessageId(1159);
		FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_5_MINUTES = new SystemMessageId(1160);
		FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_1_MINUTE = new SystemMessageId(1161);
		FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_15_MINUTES = new SystemMessageId(1162);
		FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_10_MINUTES = new SystemMessageId(1163);
		FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_5_MINUTES = new SystemMessageId(1164);
		FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_1_MINUTE = new SystemMessageId(1165);
		FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_20_MINUTES = new SystemMessageId(1166);
		FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_15_MINUTES = new SystemMessageId(1167);
		FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_10_MINUTES = new SystemMessageId(1168);
		FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_5_MINUTES = new SystemMessageId(1169);
		FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_1_MINUTE = new SystemMessageId(1170);
		INNADRIL_BOAT_ARRIVE_20_MINUTES = new SystemMessageId(1171);
		INNADRIL_BOAT_ARRIVE_15_MINUTES = new SystemMessageId(1172);
		INNADRIL_BOAT_ARRIVE_10_MINUTES = new SystemMessageId(1173);
		INNADRIL_BOAT_ARRIVE_5_MINUTES = new SystemMessageId(1174);
		INNADRIL_BOAT_ARRIVE_1_MINUTE = new SystemMessageId(1175);
		SSQ_COMPETITION_UNDERWAY = new SystemMessageId(1176);
		VALIDATION_PERIOD = new SystemMessageId(1177);
		AVARICE_DESCRIPTION = new SystemMessageId(1178);
		GNOSIS_DESCRIPTION = new SystemMessageId(1179);
		STRIFE_DESCRIPTION = new SystemMessageId(1180);
		CHANGE_TITLE_CONFIRM = new SystemMessageId(1181);
		CREST_DELETE_CONFIRM = new SystemMessageId(1182);
		INITIAL_PERIOD = new SystemMessageId(1183);
		RESULTS_PERIOD = new SystemMessageId(1184);
		DAYS_LEFT_UNTIL_DELETION = new SystemMessageId(1185);
		TO_CREATE_ACCOUNT_VISIT_WEBSITE = new SystemMessageId(1186);
		ACCOUNT_INFORMATION_FORGOTTON_VISIT_WEBSITE = new SystemMessageId(1187);
		YOUR_TARGET_NO_LONGER_RECEIVE_A_RECOMMENDATION = new SystemMessageId(1188);
		TEMPORARY_ALLIANCE = new SystemMessageId(1189);
		TEMPORARY_ALLIANCE_DISSOLVED = new SystemMessageId(1190);
		FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_10_MINUTES = new SystemMessageId(1191);
		FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_5_MINUTES = new SystemMessageId(1192);
		FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_1_MINUTE = new SystemMessageId(1193);
		MERC_CAN_BE_ASSIGNED = new SystemMessageId(1194);
		MERC_CANT_BE_ASSIGNED_USING_STRIFE = new SystemMessageId(1195);
		FORCE_MAXIMUM = new SystemMessageId(1196);
		SUMMONING_SERVITOR_COSTS_S2_S1 = new SystemMessageId(1197);
		CRYSTALLIZATION_SUCCESSFUL = new SystemMessageId(1198);
		CLAN_WAR_HEADER = new SystemMessageId(1199);
		S1_S2_ALLIANCE = new SystemMessageId(1200);
		SELECT_QUEST_TO_ABOR = new SystemMessageId(1201);
		S1_NO_ALLI_EXISTS = new SystemMessageId(1202);
		NO_WAR_IN_PROGRESS = new SystemMessageId(1203);
		SCREENSHOT = new SystemMessageId(1204);
		MAILBOX_FULL = new SystemMessageId(1205);
		MEMOBOX_FULL = new SystemMessageId(1206);
		MAKE_AN_ENTRY = new SystemMessageId(1207);
		C1_DIED_DROPPED_S3_S2 = new SystemMessageId(1208);
		RAID_WAS_SUCCESSFUL = new SystemMessageId(1209);
		QUEST_EVENT_PERIOD_BEGUN = new SystemMessageId(1210);
		QUEST_EVENT_PERIOD_ENDED = new SystemMessageId(1211);
		DAWN_OBTAINED_AVARICE = new SystemMessageId(1212);
		DAWN_OBTAINED_GNOSIS = new SystemMessageId(1213);
		DAWN_OBTAINED_STRIFE = new SystemMessageId(1214);
		DUSK_OBTAINED_AVARICE = new SystemMessageId(1215);
		DUSK_OBTAINED_GNOSIS = new SystemMessageId(1216);
		DUSK_OBTAINED_STRIFE = new SystemMessageId(1217);
		SEAL_VALIDATION_PERIOD_BEGUN = new SystemMessageId(1218);
		SEAL_VALIDATION_PERIOD_ENDED = new SystemMessageId(1219);
		SUMMON_CONFIRM = new SystemMessageId(1220);
		RETURN_CONFIRM = new SystemMessageId(1221);
		LOC_GM_CONSULATION_SERVICE_S1_S2_S3 = new SystemMessageId(1222);
		DEPART_FOR_TALKING_5_MINUTES = new SystemMessageId(1223);
		DEPART_FOR_TALKING_1_MINUTE = new SystemMessageId(1224);
		DEPART_FOR_TALKING = new SystemMessageId(1225);
		LEAVING_FOR_TALKING = new SystemMessageId(1226);
		S1_UNREAD_MESSAGES = new SystemMessageId(1227);
		C1_BLOCKED_YOU_CANNOT_MAIL = new SystemMessageId(1228);
		NO_MORE_MESSAGES_TODAY = new SystemMessageId(1229);
		ONLY_FIVE_RECIPIENTS = new SystemMessageId(1230);
		SENT_MAIL = new SystemMessageId(1231);
		MESSAGE_NOT_SENT = new SystemMessageId(1232);
		NEW_MAIL = new SystemMessageId(1233);
		MAIL_STORED_IN_MAILBOX = new SystemMessageId(1234);
		ALL_FRIENDS_DELETE_CONFIRM = new SystemMessageId(1235);
		ENTER_SECURITY_CARD_NUMBER = new SystemMessageId(1236);
		ENTER_CARD_NUMBER_FOR_S1 = new SystemMessageId(1237);
		TEMP_MAILBOX_FULL = new SystemMessageId(1238);
		KEYBOARD_MODULE_FAILED_LOAD = new SystemMessageId(1239);
		DUSK_WON = new SystemMessageId(1240);
		DAWN_WON = new SystemMessageId(1241);
		NOT_VERIFIED_AGE_NO_LOGIN = new SystemMessageId(1242);
		SECURITY_CARD_NUMBER_INVALID = new SystemMessageId(1243);
		NOT_VERIFIED_AGE_LOG_OFF = new SystemMessageId(1244);
		LOGOUT_IN_S1_MINUTES = new SystemMessageId(1245);
		C1_DIED_DROPPED_S2_ADENA = new SystemMessageId(1246);
		CORPSE_TOO_OLD_SKILL_NOT_USED = new SystemMessageId(1247);
		OUT_OF_FEED_MOUNT_CANCELED = new SystemMessageId(1248);
		YOU_MAY_ONLY_RIDE_WYVERN_WHILE_RIDING_STRIDER = new SystemMessageId(1249);
		SURRENDER_ALLY_WAR_CONFIRM = new SystemMessageId(1250);
		DISMISS_ALLY_CONFIRM = new SystemMessageId(1251);
		SURRENDER_CONFIRM1 = new SystemMessageId(1252);
		SURRENDER_CONFIRM2 = new SystemMessageId(1253);
		THANKS_FOR_FEEDBACK = new SystemMessageId(1254);
		GM_CONSULTATION_BEGUN = new SystemMessageId(1255);
		PLEASE_WRITE_NAME_AFTER_COMMAND = new SystemMessageId(1256);
		PET_SKILL_NOT_AS_MACRO = new SystemMessageId(1257);
		S1_CRYSTALLIZED = new SystemMessageId(1258);
		ALLIANCE_TARGET_HEADER = new SystemMessageId(1259);
		PREPARATIONS_PERIOD_BEGUN = new SystemMessageId(1260);
		COMPETITION_PERIOD_BEGUN = new SystemMessageId(1261);
		RESULTS_PERIOD_BEGUN = new SystemMessageId(1262);
		VALIDATION_PERIOD_BEGUN = new SystemMessageId(1263);
		STONE_CANNOT_ABSORB = new SystemMessageId(1264);
		CANT_ABSORB_WITHOUT_STONE = new SystemMessageId(1265);
		EXCHANGE_HAS_ENDED = new SystemMessageId(1266);
		CONTRIB_SCORE_INCREASED_S1 = new SystemMessageId(1267);
		ADD_SUBCLASS_CONFIRM = new SystemMessageId(1268);
		ADD_NEW_SUBCLASS = new SystemMessageId(1269);
		SUBCLASS_TRANSFER_COMPLETED = new SystemMessageId(1270);
		DAWN_CONFIRM = new SystemMessageId(1271);
		DUSK_CONFIRM = new SystemMessageId(1272);
		SEVENSIGNS_PARTECIPATION_DAWN = new SystemMessageId(1273);
		SEVENSIGNS_PARTECIPATION_DUSK = new SystemMessageId(1274);
		FIGHT_FOR_AVARICE = new SystemMessageId(1275);
		FIGHT_FOR_GNOSIS = new SystemMessageId(1276);
		FIGHT_FOR_STRIFE = new SystemMessageId(1277);
		NPC_SERVER_NOT_OPERATING = new SystemMessageId(1278);
		CONTRIB_SCORE_EXCEEDED = new SystemMessageId(1279);
		CRITICAL_HIT_MAGIC = new SystemMessageId(1280);
		YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS = new SystemMessageId(1281);
		YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1 = new SystemMessageId(1282);
		MINIMUM_FRAME_ACTIVATED = new SystemMessageId(1283);
		MINIMUM_FRAME_DEACTIVATED = new SystemMessageId(1284);
		NO_INVENTORY_CANNOT_PURCHASE = new SystemMessageId(1285);
		UNTIL_MONDAY_6PM = new SystemMessageId(1286);
		UNTIL_TODAY_6PM = new SystemMessageId(1287);
		S1_WILL_WIN_COMPETITION = new SystemMessageId(1288);
		SEAL_OWNED_10_MORE_VOTED = new SystemMessageId(1289);
		SEAL_NOT_OWNED_35_MORE_VOTED = new SystemMessageId(1290);
		SEAL_OWNED_10_LESS_VOTED = new SystemMessageId(1291);
		SEAL_NOT_OWNED_35_LESS_VOTED = new SystemMessageId(1292);
		COMPETITION_WILL_TIE = new SystemMessageId(1293);
		COMPETITION_TIE_SEAL_NOT_AWARDED = new SystemMessageId(1294);
		SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE = new SystemMessageId(1295);
		NO_PRIVATE_STORE_HERE = new SystemMessageId(1296);
		NO_PRIVATE_WORKSHOP_HERE = new SystemMessageId(1297);
		MONS_EXIT_CONFIRM = new SystemMessageId(1298);
		C1_CASTING_INTERRUPTED = new SystemMessageId(1299);
		WEAR_ITEMS_STOPPED = new SystemMessageId(1300);
		CAN_BE_USED_BY_DAWN = new SystemMessageId(1301);
		CAN_BE_USED_BY_DUSK = new SystemMessageId(1302);
		CAN_BE_USED_DURING_QUEST_EVENT_PERIOD = new SystemMessageId(1303);
		STRIFE_CANCELED_DEFENSIVE_REGISTRATION = new SystemMessageId(1304);
		SEAL_STONES_ONLY_WHILE_QUEST = new SystemMessageId(1305);
		NO_LONGER_TRYING_ON = new SystemMessageId(1306);
		SETTLE_ACCOUNT_ONLY_IN_SEAL_VALIDATION = new SystemMessageId(1307);
		CLASS_TRANSFER = new SystemMessageId(1308);
		LATEST_MSN_REQUIRED = new SystemMessageId(1309);
		LATEST_MSN_RECOMMENDED = new SystemMessageId(1310);
		MSN_ONLY_BASIC = new SystemMessageId(1311);
		MSN_OBTAINED_FROM = new SystemMessageId(1312);
		S1_CHAT_HISTORIES_STORED = new SystemMessageId(1313);
		ENTER_PASSPORT_FOR_ADDING = new SystemMessageId(1314);
		DELETING_A_CONTACT = new SystemMessageId(1315);
		CONTACT_WILL_DELETED = new SystemMessageId(1316);
		CONTACT_DELETE_CONFIRM = new SystemMessageId(1317);
		SELECT_CONTACT_FOR_BLOCK_UNBLOCK = new SystemMessageId(1318);
		SELECT_CONTACT_FOR_CHANGE_GROUP = new SystemMessageId(1319);
		SELECT_GROUP_PRESS_OK = new SystemMessageId(1320);
		ENTER_GROUP_NAME = new SystemMessageId(1321);
		SELECT_GROUP_ENTER_NAME = new SystemMessageId(1322);
		SELECT_GROUP_TO_DELETE = new SystemMessageId(1323);
		SIGNING_IN = new SystemMessageId(1324);
		ANOTHER_COMPUTER_LOGOUT = new SystemMessageId(1325);
		S1_D = new SystemMessageId(1326);
		MESSAGE_NOT_DELIVERED = new SystemMessageId(1327);
		DUSK_NOT_RESURRECTED = new SystemMessageId(1328);
		BLOCKED_FROM_USING_STORE = new SystemMessageId(1329);
		NO_STORE_FOR_S1_MINUTES = new SystemMessageId(1330);
		NO_LONGER_BLOCKED_USING_STORE = new SystemMessageId(1331);
		NO_ITEMS_AFTER_DEATH = new SystemMessageId(1332);
		REPLAY_INACCESSIBLE = new SystemMessageId(1333);
		NEW_CAMERA_STORED = new SystemMessageId(1334);
		CAMERA_STORING_FAILED = new SystemMessageId(1335);
		REPLAY_S1_S2_CORRUPTED = new SystemMessageId(1336);
		REPLAY_TERMINATE_CONFIRM = new SystemMessageId(1337);
		EXCEEDED_MAXIMUM_AMOUNT = new SystemMessageId(1338);
		MACRO_SHORTCUT_NOT_RUN = new SystemMessageId(1339);
		SERVER_NOT_ACCESSED_BY_COUPON = new SystemMessageId(1340);
		INCORRECT_NAME_OR_ADDRESS = new SystemMessageId(1341);
		ALREADY_LOGGED_IN = new SystemMessageId(1342);
		INCORRECT_ADDRESS_OR_PASSWORD = new SystemMessageId(1343);
		NET_LOGIN_FAILED = new SystemMessageId(1344);
		SELECT_CONTACT_CLICK_OK = new SystemMessageId(1345);
		CURRENTLY_ENTERING_CHAT = new SystemMessageId(1346);
		MESSENGER_FAILED_CARRYING_OUT_TASK = new SystemMessageId(1347);
		S1_ENTERED_CHAT_ROOM = new SystemMessageId(1348);
		S1_LEFT_CHAT_ROOM = new SystemMessageId(1349);
		GOING_OFFLINE = new SystemMessageId(1350);
		SELECT_CONTACT_CLICK_REMOVE = new SystemMessageId(1351);
		ADDED_TO_S1_S2_CONTACT_LIST = new SystemMessageId(1352);
		CAN_SET_OPTION_TO_ALWAYS_SHOW_OFFLINE = new SystemMessageId(1353);
		NO_CHAT_WHILE_BLOCKED = new SystemMessageId(1354);
		CONTACT_CURRENTLY_BLOCKED = new SystemMessageId(1355);
		CONTACT_CURRENTLY_OFFLINE = new SystemMessageId(1356);
		YOU_ARE_BLOCKED = new SystemMessageId(1357);
		YOU_ARE_LOGGING_OUT = new SystemMessageId(1358);
		S1_LOGGED_IN2 = new SystemMessageId(1359);
		GOT_MESSAGE_FROM_S1 = new SystemMessageId(1360);
		LOGGED_OUT_DUE_TO_ERROR = new SystemMessageId(1361);
		SELECT_CONTACT_TO_DELETE = new SystemMessageId(1362);
		YOUR_REQUEST_ALLIANCE_WAR_DENIED = new SystemMessageId(1363);
		REQUEST_ALLIANCE_WAR_REJECTED = new SystemMessageId(1364);
		S2_OF_S1_SURRENDERED_AS_INDIVIDUAL = new SystemMessageId(1365);
		DELTE_GROUP_INSTRUCTION = new SystemMessageId(1366);
		ONLY_GROUP_CAN_ADD_RECORDS = new SystemMessageId(1367);
		YOU_CAN_NOT_TRY_THOSE_ITEMS_ON_AT_THE_SAME_TIME = new SystemMessageId(1368);
		EXCEEDED_THE_MAXIMUM = new SystemMessageId(1369);
		CANNOT_MAIL_GM_C1 = new SystemMessageId(1370);
		GAMEPLAY_RESTRICTION_PENALTY_S1 = new SystemMessageId(1371);
		PUNISHMENT_CONTINUE_S1_MINUTES = new SystemMessageId(1372);
		C1_PICKED_UP_S2_FROM_RAIDBOSS = new SystemMessageId(1373);
		C1_PICKED_UP_S3_S2_S_FROM_RAIDBOSS = new SystemMessageId(1374);
		C1_PICKED_UP_S2_ADENA_FROM_RAIDBOSS = new SystemMessageId(1375);
		C1_PICKED_UP_S2_FROM_ANOTHER_CHARACTER = new SystemMessageId(1376);
		C1_PICKED_UP_S3_S2_S_FROM_ANOTHER_CHARACTER = new SystemMessageId(1377);
		C1_PICKED_UP_S3_S2_FROM_ANOTHER_CHARACTER = new SystemMessageId(1378);
		C1_OBTAINED_S2_ADENA = new SystemMessageId(1379);
		CANT_SUMMON_S1_ON_BATTLEGROUND = new SystemMessageId(1380);
		LEADER_OBTAINED_S2_OF_S1 = new SystemMessageId(1381);
		CHOOSE_WEAPON_CONFIRM = new SystemMessageId(1382);
		EXCHANGE_CONFIRM = new SystemMessageId(1383);
		C1_HAS_BECOME_A_PARTY_LEADER = new SystemMessageId(1384);
		NO_DISMOUNT_HERE = new SystemMessageId(1385);
		NO_LONGER_HELD_IN_PLACE = new SystemMessageId(1386);
		SELECT_ITEM_TO_TRY_ON = new SystemMessageId(1387);
		PARTY_ROOM_CREATED = new SystemMessageId(1388);
		PARTY_ROOM_REVISED = new SystemMessageId(1389);
		PARTY_ROOM_FORBIDDEN = new SystemMessageId(1390);
		PARTY_ROOM_EXITED = new SystemMessageId(1391);
		C1_LEFT_PARTY_ROOM = new SystemMessageId(1392);
		OUSTED_FROM_PARTY_ROOM = new SystemMessageId(1393);
		C1_KICKED_FROM_PARTY_ROOM = new SystemMessageId(1394);
		PARTY_ROOM_DISBANDED = new SystemMessageId(1395);
		CANT_VIEW_PARTY_ROOMS = new SystemMessageId(1396);
		PARTY_ROOM_LEADER_CHANGED = new SystemMessageId(1397);
		RECRUITING_PARTY_MEMBERS = new SystemMessageId(1398);
		ONLY_A_PARTY_LEADER_CAN_TRANSFER_ONES_RIGHTS_TO_ANOTHER_PLAYER = new SystemMessageId(1399);
		PLEASE_SELECT_THE_PERSON_TO_WHOM_YOU_WOULD_LIKE_TO_TRANSFER_THE_RIGHTS_OF_A_PARTY_LEADER = new SystemMessageId(1400);
		YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF = new SystemMessageId(1401);
		YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER = new SystemMessageId(1402);
		YOU_HAVE_FAILED_TO_TRANSFER_THE_PARTY_LEADER_RIGHTS = new SystemMessageId(1403);
		MANUFACTURE_PRICE_HAS_CHANGED = new SystemMessageId(1404);
		S1_CP_WILL_BE_RESTORED = new SystemMessageId(1405);
		S2_CP_WILL_BE_RESTORED_BY_C1 = new SystemMessageId(1406);
		NO_LOGIN_WITH_TWO_ACCOUNTS = new SystemMessageId(1407);
		PREPAID_LEFT_S1_S2_S3 = new SystemMessageId(1408);
		PREPAID_EXPIRED_S1_S2 = new SystemMessageId(1409);
		PREPAID_EXPIRED = new SystemMessageId(1410);
		PREPAID_CHANGED = new SystemMessageId(1411);
		PREPAID_LEFT_S1 = new SystemMessageId(1412);
		CANT_ENTER_PARTY_ROOM = new SystemMessageId(1413);
		WRONG_GRID_COUNT = new SystemMessageId(1414);
		COMMAND_FILE_NOT_SENT = new SystemMessageId(1415);
		TEAM_1_NO_REPRESENTATIVE = new SystemMessageId(1416);
		TEAM_2_NO_REPRESENTATIVE = new SystemMessageId(1417);
		TEAM_1_NO_NAME = new SystemMessageId(1418);
		TEAM_2_NO_NAME = new SystemMessageId(1419);
		TEAM_NAME_IDENTICAL = new SystemMessageId(1420);
		RACE_SETUP_FILE1 = new SystemMessageId(1421);
		RACE_SETUP_FILE2 = new SystemMessageId(1422);
		RACE_SETUP_FILE3 = new SystemMessageId(1423);
		RACE_SETUP_FILE4 = new SystemMessageId(1424);
		RACE_SETUP_FILE5 = new SystemMessageId(1425);
		RACE_SETUP_FILE6 = new SystemMessageId(1426);
		RACE_SETUP_FILE7 = new SystemMessageId(1427);
		RACE_SETUP_FILE8 = new SystemMessageId(1428);
		RACE_SETUP_FILE9 = new SystemMessageId(1429);
		RACE_SETUP_FILE10 = new SystemMessageId(1430);
		RACE_STOPPED_TEMPORARILY = new SystemMessageId(1431);
		OPPONENT_PETRIFIED = new SystemMessageId(1432);
		USE_OF_S1_WILL_BE_AUTO = new SystemMessageId(1433);
		AUTO_USE_OF_S1_CANCELLED = new SystemMessageId(1434);
		AUTO_USE_CANCELLED_LACK_OF_S1 = new SystemMessageId(1435);
		CANNOT_AUTO_USE_LACK_OF_S1 = new SystemMessageId(1436);
		DICE_NO_LONGER_ALLOWED = new SystemMessageId(1437);
		THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT = new SystemMessageId(1438);
		YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL = new SystemMessageId(1439);
		YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1 = new SystemMessageId(1440);
		YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL_S1 = new SystemMessageId(1441);
		YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL = new SystemMessageId(1443);
		YOU_DONT_HAVE_ENOUGH_EXP_TO_ENCHANT_THAT_SKILL = new SystemMessageId(1444);
		REPLACE_SUBCLASS_CONFIRM = new SystemMessageId(1445);
		FERRY_FROM_S1_TO_S2_DELAYED = new SystemMessageId(1446);
		CANNOT_DO_WHILE_FISHING_1 = new SystemMessageId(1447);
		ONLY_FISHING_SKILLS_NOW = new SystemMessageId(1448);
		GOT_A_BITE = new SystemMessageId(1449);
		FISH_SPIT_THE_HOOK = new SystemMessageId(1450);
		BAIT_STOLEN_BY_FISH = new SystemMessageId(1451);
		BAIT_LOST_FISH_GOT_AWAY = new SystemMessageId(1452);
		FISHING_POLE_NOT_EQUIPPED = new SystemMessageId(1453);
		BAIT_ON_HOOK_BEFORE_FISHING = new SystemMessageId(1454);
		CANNOT_FISH_UNDER_WATER = new SystemMessageId(1455);
		CANNOT_FISH_ON_BOAT = new SystemMessageId(1456);
		CANNOT_FISH_HERE = new SystemMessageId(1457);
		FISHING_ATTEMPT_CANCELLED = new SystemMessageId(1458);
		NOT_ENOUGH_BAIT = new SystemMessageId(1459);
		REEL_LINE_AND_STOP_FISHING = new SystemMessageId(1460);
		CAST_LINE_AND_START_FISHING = new SystemMessageId(1461);
		CAN_USE_PUMPING_ONLY_WHILE_FISHING = new SystemMessageId(1462);
		CAN_USE_REELING_ONLY_WHILE_FISHING = new SystemMessageId(1463);
		FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN = new SystemMessageId(1464);
		PUMPING_SUCCESFUL_S1_DAMAGE = new SystemMessageId(1465);
		FISH_RESISTED_PUMPING_S1_HP_REGAINED = new SystemMessageId(1466);
		REELING_SUCCESFUL_S1_DAMAGE = new SystemMessageId(1467);
		FISH_RESISTED_REELING_S1_HP_REGAINED = new SystemMessageId(1468);
		YOU_CAUGHT_SOMETHING = new SystemMessageId(1469);
		CANNOT_DO_WHILE_FISHING_2 = new SystemMessageId(1470);
		CANNOT_DO_WHILE_FISHING_3 = new SystemMessageId(1471);
		CANNOT_ATTACK_WITH_FISHING_POLE = new SystemMessageId(1472);
		S1_NOT_SUFFICIENT = new SystemMessageId(1473);
		S1_NOT_AVAILABLE = new SystemMessageId(1474);
		PET_DROPPED_S1 = new SystemMessageId(1475);
		PET_DROPPED_S1_S2 = new SystemMessageId(1476);
		PET_DROPPED_S2_S1_S = new SystemMessageId(1477);
		ONLY_64_PIXEL_256_COLOR_BMP = new SystemMessageId(1478);
		WRONG_FISHINGSHOT_GRADE = new SystemMessageId(1479);
		OLYMPIAD_REMOVE_CONFIRM = new SystemMessageId(1480);
		OLYMPIAD_NON_CLASS_CONFIRM = new SystemMessageId(1481);
		OLYMPIAD_CLASS_CONFIRM = new SystemMessageId(1482);
		HERO_CONFIRM = new SystemMessageId(1483);
		HERO_WEAPON_CONFIRM = new SystemMessageId(1484);
		FERRY_TALKING_GLUDIN_DELAYED = new SystemMessageId(1485);
		FERRY_GLUDIN_TALKING_DELAYED = new SystemMessageId(1486);
		FERRY_GIRAN_TALKING_DELAYED = new SystemMessageId(1487);
		FERRY_TALKING_GIRAN_DELAYED = new SystemMessageId(1488);
		INNADRIL_BOAT_DELAYED = new SystemMessageId(1489);
		TRADED_S2_OF_CROP_S1 = new SystemMessageId(1490);
		FAILED_IN_TRADING_S2_OF_CROP_S1 = new SystemMessageId(1491);
		YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S = new SystemMessageId(1492);
		THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME = new SystemMessageId(1493);
		THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME = new SystemMessageId(1494);
		THE_GAME_WILL_START_IN_S1_SECOND_S = new SystemMessageId(1495);
		STARTS_THE_GAME = new SystemMessageId(1496);
		C1_HAS_WON_THE_GAME = new SystemMessageId(1497);
		THE_GAME_ENDED_IN_A_TIE = new SystemMessageId(1498);
		YOU_WILL_BE_MOVED_TO_TOWN_IN_S1_SECONDS = new SystemMessageId(1499);
		C1_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_CLASS_CHARACTER = new SystemMessageId(1500);
		C1_DOES_NOT_MEET_REQUIREMENTS_ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD = new SystemMessageId(1501);
		C1_IS_ALREADY_REGISTERED_ON_THE_MATCH_WAITING_LIST = new SystemMessageId(1502);
		YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES = new SystemMessageId(1503);
		YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES = new SystemMessageId(1504);
		YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME = new SystemMessageId(1505);
		YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME = new SystemMessageId(1506);
		THIS_ITEM_CANT_BE_EQUIPPED_FOR_THE_OLYMPIAD_EVENT = new SystemMessageId(1507);
		THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT = new SystemMessageId(1508);
		THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT = new SystemMessageId(1509);
		RESSURECTION_REQUEST_BY_C1_FOR_S2_XP = new SystemMessageId(1510);
		MASTER_CANNOT_RES = new SystemMessageId(1511);
		CANNOT_RES_PET = new SystemMessageId(1512);
		RES_HAS_ALREADY_BEEN_PROPOSED = new SystemMessageId(1513);
		CANNOT_RES_MASTER = new SystemMessageId(1514);
		CANNOT_RES_PET2 = new SystemMessageId(1515);
		THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING = new SystemMessageId(1516);
		BLESSED_ENCHANT_FAILED = new SystemMessageId(1517);
		CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION = new SystemMessageId(1518);
		MAKE_SURE_YOU_RESSURECT_YOUR_PET_WITHIN_24_HOURS = new SystemMessageId(1519);
		SERVITOR_PASSED_AWAY = new SystemMessageId(1520);
		YOUR_SERVITOR_HAS_VANISHED = new SystemMessageId(1521);
		YOUR_PETS_CORPSE_HAS_DECAYED = new SystemMessageId(1522);
		RELEASE_PET_ON_BOAT = new SystemMessageId(1523);
		C1_PET_GAINED_S2 = new SystemMessageId(1524);
		C1_PET_GAINED_S3_S2_S = new SystemMessageId(1525);
		C1_PET_GAINED_S2_S3 = new SystemMessageId(1526);
		PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY = new SystemMessageId(1527);
		SENT_PETITION_TO_GM = new SystemMessageId(1528);
		COMMAND_CHANNEL_CONFIRM_FROM_C1 = new SystemMessageId(1529);
		SELECT_TARGET_OR_ENTER_NAME = new SystemMessageId(1530);
		ENTER_CLAN_NAME_TO_DECLARE_WAR2 = new SystemMessageId(1531);
		ENTER_CLAN_NAME_TO_CEASE_FIRE = new SystemMessageId(1532);
		ANNOUNCEMENT_C1_PICKED_UP_S2 = new SystemMessageId(1533);
		ANNOUNCEMENT_C1_PICKED_UP_S2_S3 = new SystemMessageId(1534);
		ANNOUNCEMENT_C1_PET_PICKED_UP_S2 = new SystemMessageId(1535);
		ANNOUNCEMENT_C1_PET_PICKED_UP_S2_S3 = new SystemMessageId(1536);
		LOC_RUNE_S1_S2_S3 = new SystemMessageId(1537);
		LOC_GODDARD_S1_S2_S3 = new SystemMessageId(1538);
		CARGO_AT_TALKING_VILLAGE = new SystemMessageId(1539);
		CARGO_AT_DARKELF_VILLAGE = new SystemMessageId(1540);
		CARGO_AT_ELVEN_VILLAGE = new SystemMessageId(1541);
		CARGO_AT_ORC_VILLAGE = new SystemMessageId(1542);
		CARGO_AT_DWARVEN_VILLAGE = new SystemMessageId(1543);
		CARGO_AT_ADEN = new SystemMessageId(1544);
		CARGO_AT_OREN = new SystemMessageId(1545);
		CARGO_AT_HUNTERS = new SystemMessageId(1546);
		CARGO_AT_DION = new SystemMessageId(1547);
		CARGO_AT_FLORAN = new SystemMessageId(1548);
		CARGO_AT_GLUDIN = new SystemMessageId(1549);
		CARGO_AT_GLUDIO = new SystemMessageId(1550);
		CARGO_AT_GIRAN = new SystemMessageId(1551);
		CARGO_AT_HEINE = new SystemMessageId(1552);
		CARGO_AT_RUNE = new SystemMessageId(1553);
		CARGO_AT_GODDARD = new SystemMessageId(1554);
		CANCEL_CHARACTER_DELETION_CONFIRM = new SystemMessageId(1555);
		CLAN_NOTICE_SAVED = new SystemMessageId(1556);
		SEED_PRICE_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2 = new SystemMessageId(1557);
		THE_QUANTITY_OF_SEED_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2 = new SystemMessageId(1558);
		CROP_PRICE_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2 = new SystemMessageId(1559);
		THE_QUANTITY_OF_CROP_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2 = new SystemMessageId(1560);
		CLAN_S1_DECLARED_WAR = new SystemMessageId(1561);
		CLAN_WAR_DECLARED_AGAINST_S1_IF_KILLED_LOSE_LOW_EXP = new SystemMessageId(1562);
		CANNOT_DECLARE_WAR_TOO_LOW_LEVEL_OR_NOT_ENOUGH_MEMBERS = new SystemMessageId(1563);
		CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER = new SystemMessageId(1564);
		CLAN_WAR_CANNOT_DECLARED_CLAN_NOT_EXIST = new SystemMessageId(1565);
		CLAN_S1_HAS_DECIDED_TO_STOP = new SystemMessageId(1566);
		WAR_AGAINST_S1_HAS_STOPPED = new SystemMessageId(1567);
		WRONG_DECLARATION_TARGET = new SystemMessageId(1568);
		CLAN_WAR_AGAINST_A_ALLIED_CLAN_NOT_WORK = new SystemMessageId(1569);
		TOO_MANY_CLAN_WARS = new SystemMessageId(1570);
		CLANS_YOU_DECLARED_WAR_ON = new SystemMessageId(1571);
		CLANS_THAT_HAVE_DECLARED_WAR_ON_YOU = new SystemMessageId(1572);
		NO_WARS_AGAINST_YOU = new SystemMessageId(1573);
		COMMAND_CHANNEL_ONLY_BY_LEVEL_5_CLAN_LEADER_PARTY_LEADER = new SystemMessageId(1574);
		PET_USE_SPIRITSHOT = new SystemMessageId(1575);
		SERVITOR_USE_SPIRITSHOT = new SystemMessageId(1576);
		SERVITOR_USE_THE_POWER_OF_SPIRIT = new SystemMessageId(1577);
		ITEMS_UNAVAILABLE_FOR_STORE_MANUFACTURE = new SystemMessageId(1578);
		C1_PET_GAINED_S2_ADENA = new SystemMessageId(1579);
		COMMAND_CHANNEL_FORMED = new SystemMessageId(1580);
		COMMAND_CHANNEL_DISBANDED = new SystemMessageId(1581);
		JOINED_COMMAND_CHANNEL = new SystemMessageId(1582);
		DISMISSED_FROM_COMMAND_CHANNEL = new SystemMessageId(1583);
		C1_PARTY_DISMISSED_FROM_COMMAND_CHANNEL = new SystemMessageId(1584);
		COMMAND_CHANNEL_DISBANDED2 = new SystemMessageId(1585);
		LEFT_COMMAND_CHANNEL = new SystemMessageId(1586);
		C1_PARTY_LEFT_COMMAND_CHANNEL = new SystemMessageId(1587);
		COMMAND_CHANNEL_ONLY_AT_LEAST_5_PARTIES = new SystemMessageId(1588);
		COMMAND_CHANNEL_LEADER_NOW_C1 = new SystemMessageId(1589);
		GUILD_INFO_HEADER = new SystemMessageId(1590);
		NO_USER_INVITED_TO_COMMAND_CHANNEL = new SystemMessageId(1591);
		CANNOT_LONGER_SETUP_COMMAND_CHANNEL = new SystemMessageId(1592);
		CANNOT_INVITE_TO_COMMAND_CHANNEL = new SystemMessageId(1593);
		C1_ALREADY_MEMBER_OF_COMMAND_CHANNEL = new SystemMessageId(1594);
		S1_SUCCEEDED = new SystemMessageId(1595);
		HIT_BY_S1 = new SystemMessageId(1596);
		S1_FAILED = new SystemMessageId(1597);
		SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET = new SystemMessageId(1598);
		CANNOT_OBSERVE_IN_COMBAT = new SystemMessageId(1599);
		TOMORROW_ITEM_ZERO_CONFIRM = new SystemMessageId(1600);
		TOMORROW_ITEM_SAME_CONFIRM = new SystemMessageId(1601);
		COMMAND_CHANNEL_ONLY_FOR_PARTY_LEADER = new SystemMessageId(1602);
		ONLY_COMMANDER_GIVE_COMMAND = new SystemMessageId(1603);
		CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR = new SystemMessageId(1604);
		HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR = new SystemMessageId(1605);
		THIRD_CLASS_TRANSFER = new SystemMessageId(1606);
		S1_ADENA_HAS_BEEN_WITHDRAWN_TO_PAY_FOR_PURCHASING_FEES = new SystemMessageId(1607);
		INSUFFICIENT_ADENA_TO_BUY_CASTLE = new SystemMessageId(1608);
		WAR_ALREADY_DECLARED = new SystemMessageId(1609);
		CANNOT_DECLARE_AGAINST_OWN_CLAN = new SystemMessageId(1610);
		PARTY_LEADER_C1 = new SystemMessageId(1611);
		WAR_LIST = new SystemMessageId(1612);
		NO_CLAN_ON_WAR_LIST = new SystemMessageId(1613);
		JOINED_CHANNEL_ALREADY_OPEN = new SystemMessageId(1614);
		S1_PARTIES_REMAINING_UNTIL_CHANNEL = new SystemMessageId(1615);
		COMMAND_CHANNEL_ACTIVATED = new SystemMessageId(1616);
		CANT_USE_COMMAND_CHANNEL = new SystemMessageId(1617);
		FERRY_RUNE_GLUDIN_DELAYED = new SystemMessageId(1618);
		FERRY_GLUDIN_RUNE_DELAYED = new SystemMessageId(1619);
		ARRIVED_AT_RUNE = new SystemMessageId(1620);
		DEPARTURE_FOR_GLUDIN_5_MINUTES = new SystemMessageId(1621);
		DEPARTURE_FOR_GLUDIN_1_MINUTE = new SystemMessageId(1622);
		DEPARTURE_FOR_GLUDIN_SHORTLY = new SystemMessageId(1623);
		DEPARTURE_FOR_GLUDIN_NOW = new SystemMessageId(1624);
		DEPARTURE_FOR_RUNE_10_MINUTES = new SystemMessageId(1625);
		DEPARTURE_FOR_RUNE_5_MINUTES = new SystemMessageId(1626);
		DEPARTURE_FOR_RUNE_1_MINUTE = new SystemMessageId(1627);
		DEPARTURE_FOR_GLUDIN_SHORTLY2 = new SystemMessageId(1628);
		DEPARTURE_FOR_RUNE_NOW = new SystemMessageId(1629);
		FERRY_FROM_RUNE_AT_GLUDIN_15_MINUTES = new SystemMessageId(1630);
		FERRY_FROM_RUNE_AT_GLUDIN_10_MINUTES = new SystemMessageId(1631);
		FERRY_FROM_RUNE_AT_GLUDIN_5_MINUTES = new SystemMessageId(1632);
		FERRY_FROM_RUNE_AT_GLUDIN_1_MINUTE = new SystemMessageId(1633);
		FERRY_FROM_GLUDIN_AT_RUNE_15_MINUTES = new SystemMessageId(1634);
		FERRY_FROM_GLUDIN_AT_RUNE_10_MINUTES = new SystemMessageId(1635);
		FERRY_FROM_GLUDIN_AT_RUNE_5_MINUTES = new SystemMessageId(1636);
		FERRY_FROM_GLUDIN_AT_RUNE_1_MINUTE = new SystemMessageId(1637);
		CANNOT_FISH_WHILE_USING_RECIPE_BOOK = new SystemMessageId(1638);
		OLYMPIAD_PERIOD_S1_HAS_STARTED = new SystemMessageId(1639);
		OLYMPIAD_PERIOD_S1_HAS_ENDED = new SystemMessageId(1640);
		THE_OLYMPIAD_GAME_HAS_STARTED = new SystemMessageId(1641);
		THE_OLYMPIAD_GAME_HAS_ENDED = new SystemMessageId(1642);
		LOC_DIMENSIONAL_GAP_S1_S2_S3 = new SystemMessageId(1643);
		PLAY_TIME_NOW_ACCUMULATING = new SystemMessageId(1649);
		TRY_LOGIN_LATER = new SystemMessageId(1650);
		THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS = new SystemMessageId(1651);
		RECORDING_GAMEPLAY_START = new SystemMessageId(1652);
		RECORDING_GAMEPLAY_STOP_S1 = new SystemMessageId(1653);
		RECORDING_GAMEPLAY_FAILED = new SystemMessageId(1654);
		YOU_CAUGHT_SOMETHING_SMELLY_THROW_IT_BACK = new SystemMessageId(1655);
		SUCCESSFULLY_TRADED_WITH_NPC = new SystemMessageId(1656);
		C1_HAS_GAINED_S2_OLYMPIAD_POINTS = new SystemMessageId(1657);
		C1_HAS_LOST_S2_OLYMPIAD_POINTS = new SystemMessageId(1658);
		LOC_CEMETARY_OF_THE_EMPIRE_S1_S2_S3 = new SystemMessageId(1659);
		CHANNEL_CREATOR_C1 = new SystemMessageId(1660);
		C1_OBTAINED_S3_S2_S = new SystemMessageId(1661);
		FISH_NO_MORE_BITING_TRY_OTHER_LOCATION = new SystemMessageId(1662);
		CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED = new SystemMessageId(1663);
		FISH_RESISTING_LOOK_BOBBLER = new SystemMessageId(1664);
		YOU_WORN_FISH_OUT = new SystemMessageId(1665);
		OBTAINED_S1_S2 = new SystemMessageId(1666);
		LETHAL_STRIKE = new SystemMessageId(1667);
		LETHAL_STRIKE_SUCCESSFUL = new SystemMessageId(1668);
		NOTHING_INSIDE_THAT = new SystemMessageId(1669);
		REELING_PUMPING_3_LEVELS_HIGHER_THAN_FISHING_PENALTY = new SystemMessageId(1670);
		REELING_SUCCESSFUL_PENALTY_S1 = new SystemMessageId(1671);
		PUMPING_SUCCESSFUL_PENALTY_S1 = new SystemMessageId(1672);
		THE_CURRENT_RECORD_FOR_THIS_OLYMPIAD_SESSION_IS_S1_MATCHES_S2_WINS_S3_DEFEATS_YOU_HAVE_EARNED_S4_OLYMPIAD_POINTS = new SystemMessageId(1673);
		NOBLESSE_ONLY = new SystemMessageId(1674);
		A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM = new SystemMessageId(1675);
		NO_SERVITOR_CANNOT_AUTOMATE_USE = new SystemMessageId(1676);
		CANT_STOP_CLAN_WAR_WHILE_IN_COMBAT = new SystemMessageId(1677);
		NO_CLAN_WAR_AGAINST_CLAN_S1 = new SystemMessageId(1678);
		ONLY_CHANNEL_CREATOR_CAN_GLOBAL_COMMAND = new SystemMessageId(1679);
		C1_DECLINED_CHANNEL_INVITATION = new SystemMessageId(1680);
		C1_DID_NOT_RESPOND_CHANNEL_INVITATION_FAILED = new SystemMessageId(1681);
		ONLY_CHANNEL_CREATOR_CAN_DISMISS = new SystemMessageId(1682);
		ONLY_PARTY_LEADER_CAN_LEAVE_CHANNEL = new SystemMessageId(1683);
		NO_CLAN_WAR_AGAINST_DISSOLVING_CLAN = new SystemMessageId(1684);
		YOU_ARE_UNABLE_TO_EQUIP_THIS_ITEM_WHEN_YOUR_PK_COUNT_IS_GREATER_THAN_OR_EQUAL_TO_ONE = new SystemMessageId(1685);
		CASTLE_WALL_DAMAGED = new SystemMessageId(1686);
		AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN = new SystemMessageId(1687);
		CANNOT_ENCHANT_WHILE_STORE = new SystemMessageId(1688);
		C1_IS_ALREADY_REGISTERED_ON_THE_CLASS_MATCH_WAITING_LIST = new SystemMessageId(1689);
		C1_IS_ALREADY_REGISTERED_ON_THE_NON_CLASS_LIMITED_MATCH_WAITING_LIST = new SystemMessageId(1690);
		C1_CANNOT_PARTICIPATE_IN_OLYMPIAD_INVENTORY_SLOT_EXCEEDS_80_PERCENT = new SystemMessageId(1691);
		C1_CANNOT_PARTICIPATE_IN_OLYMPIAD_WHILE_CHANGED_TO_SUB_CLASS = new SystemMessageId(1692);
		WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME = new SystemMessageId(1693);
		ONLY_NOBLESSE_LEADER_CAN_VIEW_SIEGE_STATUS_WINDOW = new SystemMessageId(1694);
		ONLY_DURING_SIEGE = new SystemMessageId(1695);
		ACCUMULATED_PLAY_TIME_IS_S1 = new SystemMessageId(1696);
		ACCUMULATED_PLAY_TIME_WARNING1 = new SystemMessageId(1697);
		ACCUMULATED_PLAY_TIME_WARNING2 = new SystemMessageId(1698);
		CANNOT_DISMISS_PARTY_MEMBER = new SystemMessageId(1699);
		NOT_ENOUGH_SPIRITHOTS_FOR_PET = new SystemMessageId(1700);
		NOT_ENOUGH_SOULSHOTS_FOR_PET = new SystemMessageId(1701);
		S1_USING_THIRD_PARTY_PROGRAM = new SystemMessageId(1702);
		NOT_USING_THIRD_PARTY_PROGRAM = new SystemMessageId(1703);
		CLOSE_STORE_WINDOW_AND_TRY_AGAIN = new SystemMessageId(1704);
		PCPOINT_ACQUISITION_PERIOD = new SystemMessageId(1705);
		PCPOINT_USE_PERIOD = new SystemMessageId(1706);
		ACQUIRED_S1_PCPOINT = new SystemMessageId(1707);
		ACQUIRED_S1_PCPOINT_DOUBLE = new SystemMessageId(1708);
		USING_S1_PCPOINT = new SystemMessageId(1709);
		SHORT_OF_ACCUMULATED_POINTS = new SystemMessageId(1710);
		PCPOINT_USE_PERIOD_EXPIRED = new SystemMessageId(1711);
		PCPOINT_ACCUMULATION_PERIOD_EXPIRED = new SystemMessageId(1712);
		GAMES_DELAYED = new SystemMessageId(1713);
		LOC_SCHUTTGART_S1_S2_S3 = new SystemMessageId(1714);
		PEACEFUL_ZONE = new SystemMessageId(1715);
		ALTERED_ZONE = new SystemMessageId(1716);
		SIEGE_ZONE = new SystemMessageId(1717);
		GENERAL_ZONE = new SystemMessageId(1718);
		SEVENSIGNS_ZONE = new SystemMessageId(1719);
		UNKNOWN1 = new SystemMessageId(1720);
		COMBAT_ZONE = new SystemMessageId(1721);
		ENTER_ITEM_NAME_SEARCH = new SystemMessageId(1722);
		PLEASE_PROVIDE_PETITION_FEEDBACK = new SystemMessageId(1723);
		SERVITOR_NOT_RETURN_IN_BATTLE = new SystemMessageId(1724);
		EARNED_S1_RAID_POINTS = new SystemMessageId(1725);
		S1_PERIOD_EXPIRED_DISAPPEARED = new SystemMessageId(1726);
		C1_INVITED_YOU_TO_PARTY_ROOM_CONFIRM = new SystemMessageId(1727);
		PARTY_MATCHING_REQUEST_NO_RESPONSE = new SystemMessageId(1728);
		NOT_JOIN_CHANNEL_WHILE_TELEPORTING = new SystemMessageId(1729);
		YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN_ACADEMY = new SystemMessageId(1730);
		ONLY_LEADER_CAN_CREATE_ACADEMY = new SystemMessageId(1731);
		NEED_BLOODMARK_FOR_ACADEMY = new SystemMessageId(1732);
		NEED_ADENA_FOR_ACADEMY = new SystemMessageId(1733);
		ACADEMY_REQUIREMENTS = new SystemMessageId(1734);
		S1_DOESNOT_MEET_REQUIREMENTS_TO_JOIN_ACADEMY = new SystemMessageId(1735);
		ACADEMY_MAXIMUM = new SystemMessageId(1736);
		CLAN_CAN_CREATE_ACADEMY = new SystemMessageId(1737);
		CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY = new SystemMessageId(1738);
		CLAN_ACADEMY_CREATE_CONFIRM = new SystemMessageId(1739);
		ACADEMY_CREATE_ENTER_NAME = new SystemMessageId(1740);
		THE_S1S_CLAN_ACADEMY_HAS_BEEN_CREATED = new SystemMessageId(1741);
		ACADEMY_INVITATION_SENT_TO_S1 = new SystemMessageId(1742);
		OPEN_ACADEMY_CONDITIONS = new SystemMessageId(1743);
		ACADEMY_JOIN_NO_RESPONSE = new SystemMessageId(1744);
		ACADEMY_JOIN_DECLINE = new SystemMessageId(1745);
		ALREADY_JOINED_ACADEMY = new SystemMessageId(1746);
		JOIN_ACADEMY_REQUEST_BY_S1_FOR_CLAN_S2 = new SystemMessageId(1747);
		CLAN_MEMBER_GRADUATED_FROM_ACADEMY = new SystemMessageId(1748);
		ACADEMY_MEMBERSHIP_TERMINATED = new SystemMessageId(1749);
		C1_CANNOT_JOIN_OLYMPIAD_POSSESSING_S2 = new SystemMessageId(1750);
		GRAND_MASTER_COMMEMORATIVE_ITEM = new SystemMessageId(1751);
		MEMBER_GRADUATED_EARNED_S1_REPU = new SystemMessageId(1752);
		CANT_TRANSFER_PRIVILEGE_TO_ACADEMY_MEMBER = new SystemMessageId(1753);
		RIGHT_CANT_TRANSFERRED_TO_ACADEMY_MEMBER = new SystemMessageId(1754);
		S2_HAS_BEEN_DESIGNATED_AS_APPRENTICE_OF_CLAN_MEMBER_S1 = new SystemMessageId(1755);
		YOUR_APPRENTICE_S1_HAS_LOGGED_IN = new SystemMessageId(1756);
		YOUR_APPRENTICE_C1_HAS_LOGGED_OUT = new SystemMessageId(1757);
		YOUR_SPONSOR_C1_HAS_LOGGED_IN = new SystemMessageId(1758);
		YOUR_SPONSOR_C1_HAS_LOGGED_OUT = new SystemMessageId(1759);
		CLAN_MEMBER_C1_TITLE_CHANGED_TO_S2 = new SystemMessageId(1760);
		CLAN_MEMBER_C1_PRIVILEGE_CHANGED_TO_S2 = new SystemMessageId(1761);
		YOU_DO_NOT_HAVE_THE_RIGHT_TO_DISMISS_AN_APPRENTICE = new SystemMessageId(1762);
		S2_CLAN_MEMBER_C1_APPRENTICE_HAS_BEEN_REMOVED = new SystemMessageId(1763);
		EQUIP_ONLY_FOR_ACADEMY = new SystemMessageId(1764);
		EQUIP_NOT_FOR_GRADUATES = new SystemMessageId(1765);
		CLAN_JOIN_APPLICATION_SENT_TO_C1_IN_S2 = new SystemMessageId(1766);
		ACADEMY_JOIN_APPLICATION_SENT_TO_C1 = new SystemMessageId(1767);
		JOIN_REQUEST_BY_C1_TO_CLAN_S2_ACADEMY = new SystemMessageId(1768);
		JOIN_REQUEST_BY_C1_TO_ORDER_OF_KNIGHTS_S3_UNDER_CLAN_S2 = new SystemMessageId(1769);
		CLAN_REPU_0_MAY_FACE_PENALTIES = new SystemMessageId(1770);
		CLAN_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS = new SystemMessageId(1771);
		CLAN_WAS_DEFEATED_IN_SIEGE_AND_LOST_S1_REPUTATION_POINTS = new SystemMessageId(1772);
		CLAN_VICTORIOUS_IN_SIEGE_AND_GAINED_S1_REPUTATION_POINTS = new SystemMessageId(1773);
		CLAN_ACQUIRED_CONTESTED_CLAN_HALL_AND_S1_REPUTATION_POINTS = new SystemMessageId(1774);
		CLAN_MEMBER_C1_WAS_IN_HIGHEST_RANKED_PARTY_IN_FESTIVAL_OF_DARKNESS_AND_GAINED_S2_REPUTATION = new SystemMessageId(1775);
		CLAN_MEMBER_C1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS = new SystemMessageId(1776);
		CLAN_QUEST_COMPLETED_AND_S1_POINTS_GAINED = new SystemMessageId(1777);
		OPPOSING_CLAN_CAPTURED_CLAN_HALL_AND_YOUR_CLAN_LOSES_S1_POINTS = new SystemMessageId(1778);
		CLAN_LOST_CONTESTED_CLAN_HALL_AND_300_POINTS = new SystemMessageId(1779);
		CLAN_CAPTURED_CONTESTED_CLAN_HALL_AND_S1_POINTS_DEDUCTED_FROM_OPPONENT = new SystemMessageId(1780);
		CLAN_ADDED_S1S_POINTS_TO_REPUTATION_SCORE = new SystemMessageId(1781);
		CLAN_MEMBER_C1_WAS_KILLED_AND_S2_POINTS_DEDUCTED_FROM_REPUTATION = new SystemMessageId(1782);
		FOR_KILLING_OPPOSING_MEMBER_S1_POINTS_WERE_DEDUCTED_FROM_OPPONENTS = new SystemMessageId(1783);
		YOUR_CLAN_FAILED_TO_DEFEND_CASTLE_AND_S1_POINTS_LOST_AND_ADDED_TO_OPPONENT = new SystemMessageId(1784);
		YOUR_CLAN_HAS_BEEN_INITIALIZED_AND_S1_POINTS_LOST = new SystemMessageId(1785);
		YOUR_CLAN_FAILED_TO_DEFEND_CASTLE_AND_S1_POINTS_LOST = new SystemMessageId(1786);
		S1_DEDUCTED_FROM_CLAN_REP = new SystemMessageId(1787);
		CLAN_SKILL_S1_ADDED = new SystemMessageId(1788);
		REPUTATION_POINTS_0_OR_LOWER_CLAN_SKILLS_DEACTIVATED = new SystemMessageId(1789);
		FAILED_TO_INCREASE_CLAN_LEVEL = new SystemMessageId(1790);
		YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_MILITARY_UNIT = new SystemMessageId(1791);
		ASSIGN_MANAGER_FOR_ORDER_OF_KNIGHTS = new SystemMessageId(1792);
		C1_HAS_BEEN_SELECTED_AS_CAPTAIN_OF_S2 = new SystemMessageId(1793);
		THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED = new SystemMessageId(1794);
		THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED = new SystemMessageId(1795);
		ILLEGAL_USE17 = new SystemMessageId(1796);
		C1_PROMOTED_TO_S2 = new SystemMessageId(1797);
		CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_C1 = new SystemMessageId(1798);
		SEARCHING_FOR_BOT_USERS_TRY_AGAIN_LATER = new SystemMessageId(1799);
		C1_HISTORY_USING_BOT = new SystemMessageId(1800);
		SELL_ATTEMPT_FAILED = new SystemMessageId(1801);
		TRADE_ATTEMPT_FAILED = new SystemMessageId(1802);
		GAME_REQUEST_CANNOT_BE_MADE = new SystemMessageId(1803);
		ILLEGAL_USE18 = new SystemMessageId(1804);
		ILLEGAL_USE19 = new SystemMessageId(1805);
		ILLEGAL_USE20 = new SystemMessageId(1806);
		ILLEGAL_USE21 = new SystemMessageId(1807);
		ILLEGAL_USE22 = new SystemMessageId(1808);
		ACCOUNT_MUST_VERIFIED = new SystemMessageId(1809);
		REFUSE_INVITATION_ACTIVATED = new SystemMessageId(1810);
		REFUSE_INVITATION_CURRENTLY_ACTIVE = new SystemMessageId(1812);
		THERE_IS_S1_HOUR_AND_S2_MINUTE_LEFT_OF_THE_FIXED_USAGE_TIME = new SystemMessageId(1813);
		S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1 = new SystemMessageId(1814);
		S2_WAS_DROPPED_IN_THE_S1_REGION = new SystemMessageId(1815);
		THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION = new SystemMessageId(1816);
		S2_OWNER_HAS_LOGGED_INTO_THE_S1_REGION = new SystemMessageId(1817);
		S1_HAS_DISAPPEARED = new SystemMessageId(1818);
		EVIL_FROM_S2_IN_S1 = new SystemMessageId(1819);
		S1_CURRENTLY_SLEEP = new SystemMessageId(1820);
		S2_EVIL_PRESENCE_FELT_IN_S1 = new SystemMessageId(1821);
		S1_SEALED = new SystemMessageId(1822);
		CLANHALL_WAR_REGISTRATION_PERIOD_ENDED = new SystemMessageId(1823);
		REGISTERED_FOR_CLANHALL_WAR = new SystemMessageId(1824);
		CLANHALL_WAR_REGISTRATION_FAILED = new SystemMessageId(1825);
		CLANHALL_WAR_BEGINS_IN_S1_MINUTES = new SystemMessageId(1826);
		CLANHALL_WAR_BEGINS_IN_S1_MINUTES_ENTER_NOW = new SystemMessageId(1827);
		CLANHALL_WAR_BEGINS_IN_S1_SECONDS = new SystemMessageId(1828);
		COMMAND_CHANNEL_FULL = new SystemMessageId(1829);
		C1_NOT_ALLOWED_INVITE_TO_PARTY_ROOM = new SystemMessageId(1830);
		C1_NOT_MEET_CONDITIONS_FOR_PARTY_ROOM = new SystemMessageId(1831);
		ONLY_ROOM_LEADER_CAN_INVITE = new SystemMessageId(1832);
		CONFIRM_DROP_ALL_OF_S1 = new SystemMessageId(1833);
		PARTY_ROOM_FULL = new SystemMessageId(1834);
		S1_CLAN_IS_FULL = new SystemMessageId(1835);
		CANNOT_JOIN_ACADEMY_AFTER_2ND_OCCUPATION = new SystemMessageId(1836);
		C1_SENT_INVITATION_TO_ROYAL_GUARD_S3_OF_CLAN_S2 = new SystemMessageId(1837);
		COUPON_ONCE_PER_CHARACTER = new SystemMessageId(1838);
		SERIAL_MAY_USED_ONCE = new SystemMessageId(1839);
		SERIAL_INPUT_INCORRECT = new SystemMessageId(1840);
		CLANHALL_WAR_CANCELLED = new SystemMessageId(1841);
		C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT = new SystemMessageId(1842);
		C1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED = new SystemMessageId(1843);
		C1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED = new SystemMessageId(1844);
		HERO_WEAPONS_CANT_DESTROYED = new SystemMessageId(1845);
		TOO_FAR_AWAY_FROM_FENRIR_TO_MOUNT = new SystemMessageId(1846);
		CAUGHT_FISH_S1_LENGTH = new SystemMessageId(1847);
		REGISTERED_IN_FISH_SIZE_RANKING = new SystemMessageId(1848);
		CONFIRM_DISCARD_ALL_OF_S1 = new SystemMessageId(1849);
		CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED = new SystemMessageId(1850);
		CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED = new SystemMessageId(1851);
		ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE = new SystemMessageId(1852);
		CANT_EXCHANGE_QUANTITY_ITEMS_OF_SAME_TYPE = new SystemMessageId(1853);
		ITEM_CONVERTED_SUCCESSFULLY = new SystemMessageId(1854);
		ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME = new SystemMessageId(1855);
		OPPONENT_POSSESSES_S1_OLYMPIAD_CANCELLED = new SystemMessageId(1856);
		C1_OWNS_S2_AND_CANNOT_PARTICIPATE_IN_OLYMPIAD = new SystemMessageId(1857);
		C1_CANNOT_PARTICIPATE_OLYMPIAD_WHILE_DEAD = new SystemMessageId(1858);
		EXCEEDED_QUANTITY_FOR_MOVED = new SystemMessageId(1859);
		THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW = new SystemMessageId(1860);
		CLAN_CREST_HAS_BEEN_DELETED = new SystemMessageId(1861);
		CLAN_SKILLS_WILL_BE_ACTIVATED_SINCE_REPUTATION_IS_0_OR_HIGHER = new SystemMessageId(1862);
		C1_PURCHASED_CLAN_ITEM_REDUCING_S2_REPU_POINTS = new SystemMessageId(1863);
		PET_REFUSING_ORDER = new SystemMessageId(1864);
		PET_IN_STATE_OF_DISTRESS = new SystemMessageId(1865);
		MP_REDUCED_BY_S1 = new SystemMessageId(1866);
		YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1 = new SystemMessageId(1867);
		CANNOT_EXCHANCE_USED_ITEM = new SystemMessageId(1868);
		C1_GRANTED_MASTER_PARTY_LOOTING_RIGHTS = new SystemMessageId(1869);
		COMMAND_CHANNEL_WITH_LOOTING_RIGHTS_EXISTS = new SystemMessageId(1870);
		CONFIRM_DISMISS_C1_FROM_CLAN = new SystemMessageId(1871);
		S1_HOURS_S2_MINUTES_LEFT = new SystemMessageId(1872);
		S1_HOURS_S2_MINUTES_LEFT_FOR_THIS_PCCAFE = new SystemMessageId(1873);
		S1_MINUTES_LEFT_FOR_THIS_USER = new SystemMessageId(1874);
		S1_MINUTES_LEFT_FOR_THIS_PCCAFE = new SystemMessageId(1875);
		CONFIRM_LEAVE_S1_CLAN = new SystemMessageId(1876);
		GAME_WILL_END_IN_S1_MINUTES = new SystemMessageId(1877);
		GAME_WILL_END_IN_S1_SECONDS = new SystemMessageId(1878);
		IN_S1_MINUTES_TELEPORTED_OUTSIDE_OF_GAME_ARENA = new SystemMessageId(1879);
		IN_S1_SECONDS_TELEPORTED_OUTSIDE_OF_GAME_ARENA = new SystemMessageId(1880);
		PRELIMINARY_MATCH_BEGIN_IN_S1_SECONDS = new SystemMessageId(1881);
		CHARACTERS_NOT_CREATED_FROM_THIS_SERVER = new SystemMessageId(1882);
		NO_OFFERINGS_OWN_OR_MADE_BID_FOR = new SystemMessageId(1883);
		ENTER_PCROOM_SERIAL_NUMBER = new SystemMessageId(1884);
		SERIAL_NUMBER_CANT_ENTERED = new SystemMessageId(1885);
		SERIAL_NUMBER_ALREADY_USED = new SystemMessageId(1886);
		SERIAL_NUMBER_ENTERING_FAILED = new SystemMessageId(1887);
		SERIAL_NUMBER_ENTERING_FAILED_5_TIMES = new SystemMessageId(1888);
		CONGRATULATIONS_RECEIVED_S1 = new SystemMessageId(1889);
		ALREADY_USED_COUPON_NOT_USE_SERIAL_NUMBER = new SystemMessageId(1890);
		NOT_USE_ITEMS_IN_PRIVATE_STORE = new SystemMessageId(1891);
		REPLAY_FILE_PREVIOUS_VERSION_CANT_PLAYED = new SystemMessageId(1892);
		FILE_CANT_REPLAYED = new SystemMessageId(1893);
		NOT_SUBCLASS_WHILE_OVERWEIGHT = new SystemMessageId(1894);
		C1_IN_SUMMON_BLOCKING_AREA = new SystemMessageId(1895);
		C1_ALREADY_SUMMONED = new SystemMessageId(1896);
		S1_REQUIRED_FOR_SUMMONING = new SystemMessageId(1897);
		C1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED = new SystemMessageId(1898);
		YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING = new SystemMessageId(1899);
		C1_ENTERED_PARTY_ROOM = new SystemMessageId(1900);
		C1_INVITED_YOU_TO_PARTY_ROOM = new SystemMessageId(1901);
		INCOMPATIBLE_ITEM_GRADE = new SystemMessageId(1902);
		NCOTP = new SystemMessageId(1903);
		CANT_SUBCLASS_WITH_SUMMONED_SERVITOR = new SystemMessageId(1904);
		S2_OF_S1_WILL_REPLACED_WITH_S4_OF_S3 = new SystemMessageId(1905);
		SELECT_COMBAT_UNIT = new SystemMessageId(1906);
		SELECT_CHARACTER_WHO_WILL = new SystemMessageId(1907);
		C1_STATE_FORBIDS_SUMMONING = new SystemMessageId(1908);
		ACADEMY_LIST_HEADER = new SystemMessageId(1909);
		GRADUATES_C1 = new SystemMessageId(1910);
		YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD = new SystemMessageId(1911);
		NCOTP2 = new SystemMessageId(1912);
		TIME_FOR_S1_IS_S2_MINUTES_REMAINING = new SystemMessageId(1913);
		TIME_FOR_S1_IS_S2_SECONDS_REMAINING = new SystemMessageId(1914);
		GAME_ENDS_IN_S1_SECONDS = new SystemMessageId(1915);
		DEATH_PENALTY_LEVEL_S1_ADDED = new SystemMessageId(1916);
		DEATH_PENALTY_LIFTED = new SystemMessageId(1917);
		PET_TOO_HIGH_TO_CONTROL = new SystemMessageId(1918);
		OLYMPIAD_REGISTRATION_PERIOD_ENDED = new SystemMessageId(1919);
		ACCOUNT_INACTIVITY = new SystemMessageId(1920);
		S2_HOURS_S3_MINUTES_SINCE_S1_KILLED = new SystemMessageId(1921);
		S1_FAILED_KILLING_EXPIRED = new SystemMessageId(1922);
		COURT_MAGICIAN_CREATED_PORTAL = new SystemMessageId(1923);
		LOC_PRIMEVAL_ISLE_S1_S2_S3 = new SystemMessageId(1924);
		SEAL_OF_STRIFE_FORBIDS_SUMMONING = new SystemMessageId(1925);
		THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL = new SystemMessageId(1926);
		C1_HAS_BEEN_CHALLENGED_TO_A_DUEL = new SystemMessageId(1927);
		C1_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL = new SystemMessageId(1928);
		C1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS = new SystemMessageId(1929);
		YOU_HAVE_ACCEPTED_C1_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS = new SystemMessageId(1930);
		C1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL = new SystemMessageId(1931);
		C1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL2 = new SystemMessageId(1932);
		YOU_HAVE_ACCEPTED_C1_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS = new SystemMessageId(1933);
		S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS = new SystemMessageId(1934);
		C1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_PARTY_DUEL = new SystemMessageId(1935);
		THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL = new SystemMessageId(1936);
		SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY = new SystemMessageId(1937);
		C1_HAS_CHALLENGED_YOU_TO_A_DUEL = new SystemMessageId(1938);
		C1_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL = new SystemMessageId(1939);
		YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME = new SystemMessageId(1940);
		NO_PLACE_FOR_DUEL = new SystemMessageId(1941);
		THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL = new SystemMessageId(1942);
		THE_OPPOSING_PARTY_IS_AT_BAD_LOCATION_FOR_A_DUEL = new SystemMessageId(1943);
		IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE = new SystemMessageId(1944);
		THE_DUEL_WILL_BEGIN_IN_S1_SECONDS = new SystemMessageId(1945);
		C1_CHALLENGED_YOU_TO_A_DUEL = new SystemMessageId(1946);
		C1_CHALLENGED_YOU_TO_A_PARTY_DUEL = new SystemMessageId(1947);
		THE_DUEL_WILL_BEGIN_IN_S1_SECONDS2 = new SystemMessageId(1948);
		LET_THE_DUEL_BEGIN = new SystemMessageId(1949);
		C1_HAS_WON_THE_DUEL = new SystemMessageId(1950);
		C1_PARTY_HAS_WON_THE_DUEL = new SystemMessageId(1951);
		THE_DUEL_HAS_ENDED_IN_A_TIE = new SystemMessageId(1952);
		SINCE_C1_WAS_DISQUALIFIED_S2_HAS_WON = new SystemMessageId(1953);
		SINCE_C1_PARTY_WAS_DISQUALIFIED_S2_PARTY_HAS_WON = new SystemMessageId(1954);
		SINCE_C1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON = new SystemMessageId(1955);
		SINCE_C1_PARTY_WITHDREW_FROM_THE_DUEL_S2_PARTY_HAS_WON = new SystemMessageId(1956);
		SELECT_THE_ITEM_TO_BE_AUGMENTED = new SystemMessageId(1957);
		SELECT_THE_CATALYST_FOR_AUGMENTATION = new SystemMessageId(1958);
		REQUIRES_S1_S2 = new SystemMessageId(1959);
		THIS_IS_NOT_A_SUITABLE_ITEM = new SystemMessageId(1960);
		GEMSTONE_QUANTITY_IS_INCORRECT = new SystemMessageId(1961);
		THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED = new SystemMessageId(1962);
		SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION = new SystemMessageId(1963);
		AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM = new SystemMessageId(1964);
		AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1 = new SystemMessageId(1965);
		ONLY_CLAN_LEADER_CAN_ISSUE_COMMANDS = new SystemMessageId(1966);
		GATE_LOCKED_TRY_AGAIN_LATER = new SystemMessageId(1967);
		S1_OWNER = new SystemMessageId(1968);
		AREA_S1_APPEARS = new SystemMessageId(1969);
		ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN = new SystemMessageId(1970);
		HARDENER_LEVEL_TOO_HIGH = new SystemMessageId(1971);
		YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION = new SystemMessageId(1972);
		YOU_CANNOT_AUGMENT_ITEMS_WHILE_FROZEN = new SystemMessageId(1973);
		YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD = new SystemMessageId(1974);
		YOU_CANNOT_AUGMENT_ITEMS_WHILE_TRADING = new SystemMessageId(1975);
		YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED = new SystemMessageId(1976);
		YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING = new SystemMessageId(1977);
		YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN = new SystemMessageId(1978);
		S1S_REMAINING_MANA_IS_NOW_10 = new SystemMessageId(1979);
		S1S_REMAINING_MANA_IS_NOW_5 = new SystemMessageId(1980);
		S1S_REMAINING_MANA_IS_NOW_1 = new SystemMessageId(1981);
		S1S_REMAINING_MANA_IS_NOW_0 = new SystemMessageId(1982);
		PRESS_THE_AUGMENT_BUTTON_TO_BEGIN = new SystemMessageId(1984);
		S1_DROP_AREA_S2 = new SystemMessageId(1985);
		S1_OWNER_S2 = new SystemMessageId(1986);
		S1 = new SystemMessageId(1987);
		FERRY_ARRIVED_AT_PRIMEVAL = new SystemMessageId(1988);
		FERRY_LEAVING_FOR_RUNE_3_MINUTES = new SystemMessageId(1989);
		FERRY_LEAVING_PRIMEVAL_FOR_RUNE_NOW = new SystemMessageId(1990);
		FERRY_LEAVING_FOR_PRIMEVAL_3_MINUTES = new SystemMessageId(1991);
		FERRY_LEAVING_RUNE_FOR_PRIMEVAL_NOW = new SystemMessageId(1992);
		FERRY_FROM_PRIMEVAL_TO_RUNE_DELAYED = new SystemMessageId(1993);
		FERRY_FROM_RUNE_TO_PRIMEVAL_DELAYED = new SystemMessageId(1994);
		S1_CHANNEL_FILTER_OPTION = new SystemMessageId(1995);
		ATTACK_WAS_BLOCKED = new SystemMessageId(1996);
		C1_PERFORMING_COUNTERATTACK = new SystemMessageId(1997);
		COUNTERED_C1_ATTACK = new SystemMessageId(1998);
		C1_DODGES_ATTACK = new SystemMessageId(1999);
		AVOIDED_C1_ATTACK2 = new SystemMessageId(2000);
		AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS = new SystemMessageId(2001);
		TRAP_FAILED = new SystemMessageId(2002);
		OBTAINED_ORDINARY_MATERIAL = new SystemMessageId(2003);
		OBTAINED_RATE_MATERIAL = new SystemMessageId(2004);
		OBTAINED_UNIQUE_MATERIAL = new SystemMessageId(2005);
		OBTAINED_ONLY_MATERIAL = new SystemMessageId(2006);
		ENTER_RECIPIENTS_NAME = new SystemMessageId(2007);
		ENTER_TEXT = new SystemMessageId(2008);
		CANT_EXCEED_1500_CHARACTERS = new SystemMessageId(2009);
		S2_S1 = new SystemMessageId(2010);
		AUGMENTED_ITEM_CANNOT_BE_DISCARDED = new SystemMessageId(2011);
		S1_HAS_BEEN_ACTIVATED = new SystemMessageId(2012);
		YOUR_SEED_OR_REMAINING_PURCHASE_AMOUNT_IS_INADEQUATE = new SystemMessageId(2013);
		MANOR_CANT_ACCEPT_MORE_CROPS = new SystemMessageId(2014);
		SKILL_READY_TO_USE_AGAIN = new SystemMessageId(2015);
		SKILL_READY_TO_USE_AGAIN_BUT_TIME_INCREASED = new SystemMessageId(2016);
		C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE = new SystemMessageId(2017);
		C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_FISHING = new SystemMessageId(2018);
		C1_CANNOT_DUEL_BECAUSE_C1_HP_OR_MP_IS_BELOW_50_PERCENT = new SystemMessageId(2019);
		C1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_C1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA = new SystemMessageId(2020);
		C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE = new SystemMessageId(2021);
		C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL = new SystemMessageId(2022);
		C1_CANNOT_DUEL_BECAUSE_C1_IS_IN_A_CHAOTIC_STATE = new SystemMessageId(2023);
		C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_THE_OLYMPIAD = new SystemMessageId(2024);
		C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_A_CLAN_HALL_WAR = new SystemMessageId(2025);
		C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_A_SIEGE_WAR = new SystemMessageId(2026);
		C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_RIDING_A_BOAT_STEED_OR_STRIDER = new SystemMessageId(2027);
		C1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_C1_IS_TOO_FAR_AWAY = new SystemMessageId(2028);
		C1_CANNOT_PARTICIPATE_IN_OLYMPIAD_DURING_TELEPORT = new SystemMessageId(2029);
		CURRENTLY_LOGGING_IN = new SystemMessageId(2030);
		PLEASE_WAIT_A_MOMENT = new SystemMessageId(2031);
		NOT_TIME_TO_PURCHASE_ITEM = new SystemMessageId(2032);
		NOT_SUBCLASS_WHILE_INVENTORY_FULL = new SystemMessageId(2033);
		ITEM_PURCHASABLE_IN_S1_HOURS_S2_MINUTES = new SystemMessageId(2034);
		ITEM_PURCHASABLE_IN_S1_MINUTES = new SystemMessageId(2035);
		NO_INVITE_PARTY_LOCKED = new SystemMessageId(2036);
		CANT_CREATE_CHARACTER_DURING_RESTRICTION = new SystemMessageId(2037);
		ACCOUNT_CANT_DROP_ITEMS = new SystemMessageId(2038);
		ACCOUNT_CANT_TRADE_ITEMS = new SystemMessageId(2039);
		CANT_TRADE_WITH_TARGET = new SystemMessageId(2040);
		CANT_OPEN_PRIVATE_STORE = new SystemMessageId(2041);
		ILLEGAL_USE23 = new SystemMessageId(2042);
		YOU_HAVE_EXCEEDED_YOUR_INVENTORY_VOLUME_LIMIT_AND_CANNOT_TAKE_THIS_QUESTITEM = new SystemMessageId(2043);
		CANT_SETUP_PRIVATE_WORKSHOP = new SystemMessageId(2044);
		CANT_USE_PRIVATE_WORKSHOP = new SystemMessageId(2045);
		CANT_USE_PRIVATE_STORES = new SystemMessageId(2046);
		CANT_USE_CLAN_WH = new SystemMessageId(2047);
		CONFLICTING_SHORTCUT = new SystemMessageId(2048);
		CONFIRM_SHORTCUT_WILL_SAVED_ON_SERVER = new SystemMessageId(2049);
		S1_TRYING_RAISE_FLAG = new SystemMessageId(2050);
		MUST_ACCEPT_AGREEMENT = new SystemMessageId(2051);
		NEED_CONSENT_TO_PLAY_THIS_ACCOUNT = new SystemMessageId(2052);
		ACCOUNT_DECLINED_AGREEMENT_OR_PENDING = new SystemMessageId(2053);
		ACCOUNT_SUSPENDED = new SystemMessageId(2054);
		ACCOUNT_SUSPENDED_FROM_ALL_SERVICES = new SystemMessageId(2055);
		ACCOUNT_CONVERTED = new SystemMessageId(2056);
		BLOCKED_C1 = new SystemMessageId(2057);
		YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN = new SystemMessageId(2058);
		AREA_UNSUITABLE_FOR_POLYMORPH = new SystemMessageId(2059);
		YOU_CANNOT_POLYMORPH_INTO_THE_DESIRED_FORM_IN_WATER = new SystemMessageId(2060);
		CANT_MORPH_DUE_TO_MORPH_PENALTY = new SystemMessageId(2061);
		YOU_CANNOT_POLYMORPH_WHEN_YOU_HAVE_SUMMONED_A_SERVITOR = new SystemMessageId(2062);
		YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_PET = new SystemMessageId(2063);
		CANT_MORPH_WHILE_UNDER_SPECIAL_SKILL_EFFECT = new SystemMessageId(2064);
		ITEM_CANNOT_BE_TAKEN_OFF = new SystemMessageId(2065);
		THAT_WEAPON_CANT_ATTACK = new SystemMessageId(2066);
		WEAPON_CAN_USE_ONLY_WEAPON_SKILL = new SystemMessageId(2067);
		YOU_DONT_HAVE_ALL_ITENS_NEEDED_TO_UNTRAIN_SKILL_ENCHANT = new SystemMessageId(2068);
		UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_DECREASED_BY_ONE = new SystemMessageId(2069);
		UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_RESETED = new SystemMessageId(2070);
		YOU_DONT_HAVE_ALL_ITENS_NEEDED_TO_CHANGE_SKILL_ENCHANT_ROUTE = new SystemMessageId(2071);
		SKILL_ENCHANT_CHANGE_SUCCESSFUL_S1_LEVEL_WAS_DECREASED_BY_S2 = new SystemMessageId(2072);
		SKILL_ENCHANT_CHANGE_SUCCESSFUL_S1_LEVEL_WILL_REMAIN = new SystemMessageId(2073);
		SKILL_ENCHANT_FAILED_S1_LEVEL_WILL_REMAIN = new SystemMessageId(2074);
		NO_AUCTION_PERIOD = new SystemMessageId(2075);
		BID_CANT_EXCEED_100_BILLION = new SystemMessageId(2076);
		BID_MUST_BE_HIGHER_THAN_CURRENT_BID = new SystemMessageId(2077);
		NOT_ENOUGH_ADENA_FOR_THIS_BID = new SystemMessageId(2078);
		HIGHEST_BID_BUT_RESERVE_NOT_MET = new SystemMessageId(2079);
		YOU_HAVE_BEEN_OUTBID = new SystemMessageId(2080);
		NO_FUNDS_DUE = new SystemMessageId(2081);
		EXCEEDED_MAX_ADENA_AMOUNT_IN_INVENTORY = new SystemMessageId(2082);
		AUCTION_BEGUN = new SystemMessageId(2083);
		ENEMIES_INTRUDED_FORTRESS = new SystemMessageId(2084);
		SHOUT_AND_TRADE_CHAT_CANNOT_BE_USED_WHILE_POSSESSING_CURSED_WEAPON = new SystemMessageId(2085);
		SEARCH_ON_S2_FOR_BOT_USE_COMPLETED_IN_S1_MINUTES = new SystemMessageId(2086);
		A_FORTRESS_IS_UNDER_ATTACK = new SystemMessageId(2087);
		S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS = new SystemMessageId(2088);
		S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS = new SystemMessageId(2089);
		THE_FORTRESS_BATTLE_S1_HAS_BEGUN = new SystemMessageId(2090);
		CHANGE_PASSWORT_FIRST = new SystemMessageId(2091);
		CANNOT_BID_DUE_TO_PASSED_IN_PRICE = new SystemMessageId(2092);
		PASSED_IN_PRICE_IS_S1_ADENA_WOULD_YOU_LIKE_TO_RETURN_IT = new SystemMessageId(2093);
		ANOTHER_USER_PURCHASING_TRY_AGAIN_LATER = new SystemMessageId(2094);
		ACCOUNT_CANNOT_SHOUT = new SystemMessageId(2095);
		C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED = new SystemMessageId(2096);
		C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT = new SystemMessageId(2097);
		C1_QUEST_REQUIREMENT_NOT_SUFFICIENT = new SystemMessageId(2098);
		C1_ITEM_REQUIREMENT_NOT_SUFFICIENT = new SystemMessageId(2099);
		C1_MAY_NOT_REENTER_YET = new SystemMessageId(2100);
		NOT_IN_PARTY_CANT_ENTER = new SystemMessageId(2101);
		PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER = new SystemMessageId(2102);
		NOT_IN_COMMAND_CHANNEL_CANT_ENTER = new SystemMessageId(2103);
		MAXIMUM_INSTANCE_ZONE_NUMBER_EXCEEDED_CANT_ENTER = new SystemMessageId(2104);
		ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER = new SystemMessageId(2105);
		DUNGEON_EXPIRES_IN_S1_MINUTES = new SystemMessageId(2106);
		INSTANCE_ZONE_TERMINATES_IN_S1_MINUTES = new SystemMessageId(2107);
		ILLEGAL_USE24 = new SystemMessageId(2108);
		CHARACTER_NAME_OVERLAPPING_RENAME_CHARACTER = new SystemMessageId(2109);
		CHARACTER_NAME_INVALID_RENAME_CHARACTER = new SystemMessageId(2110);
		ENTER_SHORTCUT_TO_ASSIGN = new SystemMessageId(2111);
		SUBKEY_EXPLANATION1 = new SystemMessageId(2112);
		SUBKEY_EXPLANATION2 = new SystemMessageId(2113);
		SUBKEY_EXPLANATION3 = new SystemMessageId(2114);
		ILLEGAL_USE25 = new SystemMessageId(2115);
		ILLEGAL_USE26 = new SystemMessageId(2116);
		ILLEGAL_USE27 = new SystemMessageId(2117);
		ILLEGAL_USE28 = new SystemMessageId(2118);
		ILLEGAL_USE29 = new SystemMessageId(2119);
		ILLEGAL_USE30 = new SystemMessageId(2120);
		ILLEGAL_USE31 = new SystemMessageId(2121);
		ILLEGAL_USE32 = new SystemMessageId(2122);
		ILLEGAL_USE33 = new SystemMessageId(2123);
		CLAN_NAME_OVERLAPPING_RENAME_CLAN = new SystemMessageId(2124);
		CLAN_NAME_INVALID_RENAME_CLAN = new SystemMessageId(2125);
		ILLEGAL_USE34 = new SystemMessageId(2126);
		ILLEGAL_USE35 = new SystemMessageId(2127);
		ILLEGAL_USE36 = new SystemMessageId(2128);
		AUGMENTED_ITEM_CANT_CONVERTED = new SystemMessageId(2129);
		CANT_CONVERT_THIS_ITEM = new SystemMessageId(2130);
		WON_BID_ITEM_CAN_BE_FOUND_IN_WAREHOUSE = new SystemMessageId(2131);
		ENTERED_COMMON_SERVER = new SystemMessageId(2132);
		ENTERED_ADULTS_ONLY_SERVER = new SystemMessageId(2133);
		ENTERED_JUVENILES_SERVER = new SystemMessageId(2134);
		NOT_ALLOWED_DUE_TO_FATIGUE_LEVEL = new SystemMessageId(2135);
		CLAN_NAME_CHANCE_PETITION_SUBMITTED = new SystemMessageId(2136);
		CONFIRM_BID_S2_ADENA_FOR_S1_ITEM = new SystemMessageId(2137);
		ENTER_BID_PRICE = new SystemMessageId(2138);
		C1_PET = new SystemMessageId(2139);
		C1_SERVITOR = new SystemMessageId(2140);
		SLIGHTLY_RESISTED_C1_MAGICC = new SystemMessageId(2141);
		CANT_EXPEL_C1_NOT_A_PARTY_MEMBER = new SystemMessageId(2142);
		CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_PRIVATE_STORE_OR_WORKSHOP = new SystemMessageId(2143);
		SELECT_ITEM_TO_ADD_ELEMENTAL_POWER = new SystemMessageId(2144);
		ELEMENTAL_ENHANCE_CANCELED = new SystemMessageId(2145);
		ELEMENTAL_ENHANCE_REQUIREMENT_NOT_SUFFICIENT = new SystemMessageId(2146);
		ELEMENTAL_POWER_S2_SUCCESSFULLY_ADDED_TO_S1 = new SystemMessageId(2147);
		ELEMENTAL_POWER_S3_SUCCESSFULLY_ADDED_TO_S1_S2 = new SystemMessageId(2148);
		FAILED_ADDING_ELEMENTAL_POWER = new SystemMessageId(2149);
		ANOTHER_ELEMENTAL_POWER_ALREADY_ADDED = new SystemMessageId(2150);
		OPPONENT_HAS_RESISTANCE_MAGIC_DAMAGE_DECREASED = new SystemMessageId(2151);
		CONFIRM_SHORCUT_DELETE = new SystemMessageId(2152);
		MAXIMUM_ACCOUNT_LOGINS_REACHED = new SystemMessageId(2153);
		THE_TARGET_IS_NOT_A_FLAGPOLE_SO_A_FLAG_CANNOT_BE_DISPLAYED = new SystemMessageId(2154);
		A_FLAG_IS_ALREADY_BEING_DISPLAYED_ANOTHER_FLAG_CANNOT_BE_DISPLAYED = new SystemMessageId(2155);
		THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL = new SystemMessageId(2156);
		BID_WILL_BE_ATTEMPTED_WITH_S1_ADENA = new SystemMessageId(2157);
		FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS = new SystemMessageId(2158);
		BIDDER_EXISTS_AUCTION_TIME_EXTENDED_BY_5_MINUTES = new SystemMessageId(2159);
		BIDDER_EXISTS_AUCTION_TIME_EXTENDED_BY_3_MINUTES = new SystemMessageId(2160);
		NOT_ENOUGH_SPACE_FOR_SKILL = new SystemMessageId(2161);
		YOUR_SOUL_HAS_INCREASED_BY_S1_SO_IT_IS_NOW_AT_S2 = new SystemMessageId(2162);
		SOUL_CANNOT_BE_INCREASED_ANYMORE = new SystemMessageId(2163);
		SEIZED_BARRACKS = new SystemMessageId(2164);
		BARRACKS_FUNCTION_RESTORED = new SystemMessageId(2165);
		ALL_BARRACKS_OCCUPIED = new SystemMessageId(2166);
		A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_PEACE_ZONE = new SystemMessageId(2167);
		C1_ACQUIRED_THE_FLAG = new SystemMessageId(2168);
		REGISTERED_TO_S1_FORTRESS_BATTLE = new SystemMessageId(2169);
		CANT_USE_BAD_MAGIC_WHEN_OPPONENT_IN_PEACE_ZONE = new SystemMessageId(2170);
		ITEM_CANNOT_CRYSTALLIZED = new SystemMessageId(2171);
		S1_S2_AUCTION_ENDED = new SystemMessageId(2172);
		S1_AUCTION_ENDED = new SystemMessageId(2173);
		C1_CANNOT_DUEL_WHILE_POLYMORPHED = new SystemMessageId(2174);
		CANNOT_PARTY_DUEL_WHILE_A_MEMBER_IS_POLYMORPHED = new SystemMessageId(2175);
		S1_ELEMENTAL_POWER_REMOVED = new SystemMessageId(2176);
		S1_S2_ELEMENTAL_POWER_REMOVED = new SystemMessageId(2177);
		FAILED_TO_REMOVE_ELEMENTAL_POWER = new SystemMessageId(2178);
		HIGHEST_BID_FOR_GIRAN_CASTLE = new SystemMessageId(2179);
		HIGHEST_BID_FOR_ADEN_CASTLE = new SystemMessageId(2180);
		HIGHEST_BID_FOR_RUNE_CASTLE = new SystemMessageId(2181);
		CANT_POLYMORPH_ON_BOAT = new SystemMessageId(2182);
		THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED = new SystemMessageId(2183);
		S1_CLAN_IS_VICTORIOUS_IN_THE_FORTRESS_BATTLE_OF_S2 = new SystemMessageId(2184);
		ONLY_PARTY_LEADER_CAN_ENTER = new SystemMessageId(2185);
		SOUL_CANNOT_BE_ABSORBED_ANYMORE = new SystemMessageId(2186);
		THE_TARGET_IS_LOCATED_WHERE_YOU_CANNOT_CHARGE = new SystemMessageId(2187);
		ENCHANTMENT_ALREADY_IN_PROGRESS = new SystemMessageId(2188);
		LOC_KAMAEL_VILLAGE_S1_S2_S3 = new SystemMessageId(2189);
		LOC_WASTELANDS_CAMP_S1_S2_S3 = new SystemMessageId(2190);
		CONFIRM_APPLY_SELECTIONS = new SystemMessageId(2191);
		BID_ON_ITEM_AUCTION = new SystemMessageId(2192);
		TOO_FAR_FROM_NPC = new SystemMessageId(2193);
		CANT_APPLY_CURRENT_POLYMORPH_WITH_CORRESPONDING_EFFECTS = new SystemMessageId(2194);
		THERE_IS_NOT_ENOUGH_SOUL = new SystemMessageId(2195);
		NO_OWNED_CLAN = new SystemMessageId(2196);
		OWNED_S1_CLAN = new SystemMessageId(2197);
		HIGHEST_BID_IN_ITEM_AUCTION = new SystemMessageId(2198);
		CANT_ENTER_INSTANCE_ZONE_NPC_SERVER_OFFLINE = new SystemMessageId(2199);
		INSTANCE_ZONE_TERMINATED_NPC_SERVER_OFFLINE = new SystemMessageId(2200);
		S1_YEARS_S2_MONTHS_S3_DAYS = new SystemMessageId(2201);
		S1_HOURS_S2_MINUTES_S3_SECONDS = new SystemMessageId(2202);
		S1_MONTHS_S2_DAYS = new SystemMessageId(2203);
		S1_HOURS = new SystemMessageId(2204);
		AREA_FORBIDS_MINIMAP = new SystemMessageId(2205);
		AREA_ALLOWS_MINIMAP = new SystemMessageId(2206);
		CANT_OPEN_MINIMAP = new SystemMessageId(2207);
		YOU_DONT_MEET_SKILL_LEVEL_REQUIREMENTS = new SystemMessageId(2208);
		AREA_WHERE_RADAR_CANNOT_BE_USED = new SystemMessageId(2209);
		RETURN_TO_UNENCHANTED_CONDITION = new SystemMessageId(2210);
		YOU_MUST_LEARN_ONYX_BEAST_SKILL = new SystemMessageId(2211);
		NOT_COMPLETED_QUEST_FOR_SKILL_ACQUISITION = new SystemMessageId(2212);
		CANT_BOARD_SHIP_POLYMORPHED = new SystemMessageId(2213);
		CONFIRM_CHARACTER_CREATION = new SystemMessageId(2214);
		S1_PDEF = new SystemMessageId(2215);
		PLEASE_UPDATE_CPU_DRIVER = new SystemMessageId(2216);
		BALLISTA_DESTROYED_CLAN_REPU_INCREASED = new SystemMessageId(2217);
		MAIN_CLASS_SKILL_ONLY = new SystemMessageId(2218);
		SQUAD_SKILL_ALREADY_ACQUIRED = new SystemMessageId(2219);
		PREVIOUS_LEVEL_SKILL_NOT_LEARNED = new SystemMessageId(2220);
		ACTIVATE_SELECTED_FUNTIONS_CONFIRM = new SystemMessageId(2221);
		SCOUT_COSTS_150000_ADENA = new SystemMessageId(2222);
		FORTRESS_GATE_COSTS_200000_ADENA = new SystemMessageId(2223);
		CROSSBOW_PREPARING_TO_FIRE = new SystemMessageId(2224);
		NO_SKILLS_TO_LEARN_RETURN_AFTER_S1_CLASS_CHANGE = new SystemMessageId(2225);
		NOT_ENOUGH_BOLTS = new SystemMessageId(2226);
		NOT_POSSIBLE_TO_REGISTER_TO_CASTLE_SIEGE = new SystemMessageId(2227);
		INSTANCE_ZONE_TIME_LIMIT = new SystemMessageId(2228);
		NO_INSTANCEZONE_TIME_LIMIT = new SystemMessageId(2229);
		AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES = new SystemMessageId(2230);
		REPUTATION_SCORE_FOR_CONTRACT_NOT_ENOUGH = new SystemMessageId(2231);
		S1_CRYSTALLIZED_BEFORE_DESTRUCTION = new SystemMessageId(2232);
		CANT_REGISTER_TO_SIEGE_DUE_TO_CONTRACT = new SystemMessageId(2233);
		CONFIRM_KAMAEL_HERO_WEAPON = new SystemMessageId(2234);
		INSTANCE_ZONE_DELETED_CANT_ACCESSED = new SystemMessageId(2235);
		S1_MINUTES_LEFT_ON_WYVERN = new SystemMessageId(2236);
		S1_SECONDS_LEFT_ON_WYVERN = new SystemMessageId(2237);
		PARTICIPATING_IN_SIEGE_OF_S1 = new SystemMessageId(2238);
		SIEGE_OF_S1_FINIHSED = new SystemMessageId(2239);
		CANT_REGISTER_TO_TEAM_BATTLE_CLAN_HALL_WAR_WHILE_LORD_ON_TRANSACTION_WAITING_LIST = new SystemMessageId(2240);
		CANT_APPLY_ON_LORD_TRANSACTION_WHILE_REGISTERED_TO_TEAM_BATTLE_CLAN_HALL_WAR = new SystemMessageId(2241);
		MEMBERS_CANT_LEAVE_WHEN_REGISTERED_TO_TEAM_BATTLE_CLAN_HALL_WAR = new SystemMessageId(2242);
		WHEN_BANDITSTRONGHOLD_WILDBEASTRESERVRE_CLANLORD_IN_DANGER_PREVIOUS_LORD_PARTICIPATES_IN_BATTLE = new SystemMessageId(2243);
		S1_MINUTES_REMAINING = new SystemMessageId(2244);
		S1_SECONDS_REMAINING = new SystemMessageId(2245);
		CONTEST_BEGIN_IN_S1_MINUTES = new SystemMessageId(2246);
		YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_TRANSFORMED = new SystemMessageId(2247);
		YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_PETRIFIED = new SystemMessageId(2248);
		YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_DEAD = new SystemMessageId(2249);
		YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_FISHING = new SystemMessageId(2250);
		YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_IN_BATTLE = new SystemMessageId(2251);
		YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_IN_A_DUEL = new SystemMessageId(2252);
		YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_SITTING = new SystemMessageId(2253);
		YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_CASTING = new SystemMessageId(2254);
		YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_A_CURSED_WEAPON_IS_EQUIPPED = new SystemMessageId(2255);
		YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_HOLDING_A_FLAG = new SystemMessageId(2256);
		YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_A_PET_OR_A_SERVITOR_IS_SUMMONED = new SystemMessageId(2257);
		YOU_HAVE_ALREADY_BOARDED_ANOTHER_AIRSHIP = new SystemMessageId(2258);
		LOC_FANTASY_ISLAND_S1_S2_S3 = new SystemMessageId(2259);
		PET_CAN_RUN_AWAY_WHEN_HUNGER_BELOW_10_PERCENT = new SystemMessageId(2260);
		C1_DONE_S3_DAMAGE_TO_C2 = new SystemMessageId(2261);
		C1_RECEIVED_DAMAGE_OF_S3_FROM_C2 = new SystemMessageId(2262);
		C1_RECEIVED_DAMAGE_OF_S3_THROUGH_C2 = new SystemMessageId(2263);
		C1_EVADED_C2_ATTACK = new SystemMessageId(2264);
		C1_ATTACK_WENT_ASTRAY = new SystemMessageId(2265);
		C1_HAD_CRITICAL_HIT = new SystemMessageId(2266);
		C1_RESISTED_C2_DRAIN = new SystemMessageId(2267);
		C1_ATTACK_FAILED = new SystemMessageId(2268);
		C1_RESISTED_C2_DRAIN2 = new SystemMessageId(2269);
		C1_RECEIVED_DAMAGE_FROM_S2_THROUGH_FIRE_OF_MAGIC = new SystemMessageId(2270);
		C1_WEAKLY_RESISTED_C2_MAGIC = new SystemMessageId(2271);
		USE_SHORTCUT_CONFIRM = new SystemMessageId(2272);
		SKILL_NOT_FOR_SUBCLASS = new SystemMessageId(2273);
		NPCS_RECAPTURED_FORTRESS = new SystemMessageId(2276);
		CANNOT_TRANSFORM_WHILE_SITTING = new SystemMessageId(2283);
		CAN_OPERATE_MACHINE_WHEN_IN_PARTY = new SystemMessageId(2291);
		LOC_IN_STEEL_CITADEL_S1_S2_S3 = new SystemMessageId(2293);
		GAINED_VITALITY_POINTS = new SystemMessageId(2296);
		LOC_STEEL_CITADEL = new SystemMessageId(2301);
		YOUR_VITAMIN_ITEM_HAS_ARRIVED = new SystemMessageId(2302);
		S2_SECONDS_REMAINING_FOR_REUSE_S1 = new SystemMessageId(2303);
		S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1 = new SystemMessageId(2304);
		S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1 = new SystemMessageId(2305);
		RESURRECT_USING_CHARM_OF_COURAGE = new SystemMessageId(2306);
		DONT_HAVE_SERVITOR = new SystemMessageId(2311);
		DONT_HAVE_PET = new SystemMessageId(2312);
		VITALITY_IS_AT_MAXIMUM = new SystemMessageId(2314);
		VITALITY_HAS_INCREASED = new SystemMessageId(2315);
		VITALITY_HAS_DECREASED = new SystemMessageId(2316);
		VITALITY_IS_EXHAUSTED = new SystemMessageId(2317);
		ACQUIRED_S1_REPUTATION_SCORE = new SystemMessageId(2319);
		LOC_KAMALOKA = new SystemMessageId(2321);
		LOC_NIA_KAMALOKA = new SystemMessageId(2322);
		LOC_RIM_KAMALOKA = new SystemMessageId(2323);
		ACQUIRED_50_CLAN_FAME_POINTS = new SystemMessageId(2326);
		NOT_ENOUGH_FAME_POINTS = new SystemMessageId(2327);
		YOU_CANNOT_RECEIVE_THE_VITAMIN_ITEM = new SystemMessageId(2333);
		THERE_ARE_NO_MORE_VITAMIN_ITEMS_TO_BE_FOUND = new SystemMessageId(2335);
		HALF_KILL = new SystemMessageId(2336);
		CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL = new SystemMessageId(2337);
		YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_BATTLE = new SystemMessageId(2348);
		YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING = new SystemMessageId(2349);
		YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_DUEL = new SystemMessageId(2350);
		YOU_CANNOT_USE_MY_TELEPORTS_WHILE_FLYING = new SystemMessageId(2351);
		YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_IN_AN_OLYMPIAD_MATCH = new SystemMessageId(2352);
		YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_PARALYZED = new SystemMessageId(2353);
		YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_DEAD = new SystemMessageId(2354);
		YOU_CANNOT_USE_MY_TELEPORTS_IN_THIS_AREA = new SystemMessageId(2355);
		YOU_CANNOT_USE_MY_TELEPORTS_UNDERWATER = new SystemMessageId(2356);
		YOU_CANNOT_USE_MY_TELEPORTS_IN_AN_INSTANT_ZONE = new SystemMessageId(2357);
		YOU_HAVE_NO_SPACE_TO_SAVE_THE_TELEPORT_LOCATION = new SystemMessageId(2358);
		YOU_CANNOT_TELEPORT_BECAUSE_YOU_DO_NOT_HAVE_A_TELEPORT_ITEM = new SystemMessageId(2359);
		CURRENT_LOCATION_S1 = new SystemMessageId(2361);
		TIME_LIMITED_ITEM_DELETED = new SystemMessageId(2366);
		C1_WAS_REPORTED_AS_BOT = new SystemMessageId(2371);
		THERE_NOT_MUCH_TIME_REMAINING_UNTIL_HELPER_LEAVES = new SystemMessageId(2372);
		THE_HELPER_PET_LEAVING = new SystemMessageId(2373);
		THE_HELPER_PET_CANNOT_BE_RETURNED = new SystemMessageId(2375);
		YOU_CANNOT_RECEIVE_A_VITAMIN_ITEM_DURING_AN_EXCHANGE = new SystemMessageId(2376);
		YOU_CANNOT_REPORT_CHARACTER_IN_PEACE_OR_BATTLE_ZONE = new SystemMessageId(2377);
		YOU_CANNOT_REPORT_CLAN_WAR_ENEMY = new SystemMessageId(2378);
		YOU_CANNOT_REPORT_CHAR_WHO_ACQUIRED_XP = new SystemMessageId(2379);
		YOU_CANNOT_REPORT_CHAR_AT_THIS_TIME_1 = new SystemMessageId(2380);
		YOU_CANNOT_REPORT_CHAR_AT_THIS_TIME_2 = new SystemMessageId(2381);
		YOU_CANNOT_REPORT_CHAR_AT_THIS_TIME_3 = new SystemMessageId(2382);
		YOU_CANNOT_REPORT_CHAR_AT_THIS_TIME_4 = new SystemMessageId(2383);
		
		THE_MAXMIMUM_ACCUMULATION_ALLOWED_OF_PC_CAFE_POINTS_HAS_BEEN_EXCEEDED = new SystemMessageId(2389);
		YOUR_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_REACHED_ITS_MAXIMUM_LIMIT = new SystemMessageId(2390);
		YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS = new SystemMessageId(2393);
		PET_SKILL_CANNOT_BE_USED_RECHARCHING = new SystemMessageId(2396);
		YOU_HAVE_NO_OPEN_MY_TELEPORTS_SLOTS = new SystemMessageId(2398);
		C1_IS_ALREADY_REGISTERED_NON_CLASS_LIMITED_EVENT_TEAMS = new SystemMessageId(2440);
		ONLY_PARTY_LEADER_CAN_REQUEST_TEAM_MATCH = new SystemMessageId(2441);
		PARTY_REQUIREMENTS_NOT_MET = new SystemMessageId(2442);
		THE_DISGUISE_SCROLL_MEANT_FOR_DIFFERENT_TERRITORY = new SystemMessageId(2936);
		TERRITORY_OWNING_CLAN_CANNOT_USE_DISGUISE_SCROLL = new SystemMessageId(2937);
		TERRITORY_WAR_SCROLL_CAN_NOT_USED_NOW = new SystemMessageId(2955);
		INSTANT_ZONE_CURRENTLY_INUSE_S1 = new SystemMessageId(2400);
		THE_TERRITORY_WAR_REGISTERING_PERIOD_ENDED = new SystemMessageId(2402);
		TERRITORY_WAR_BEGINS_IN_10_MINUTES = new SystemMessageId(2403);
		TERRITORY_WAR_BEGINS_IN_5_MINUTES = new SystemMessageId(2404);
		TERRITORY_WAR_BEGINS_IN_1_MINUTE = new SystemMessageId(2405);
		YOU_HAVE_REGISTERED_IN_A_WAITING_LIST_OF_TEAM_GAMES = new SystemMessageId(2408);
		THE_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_BEEN_INCREASED = new SystemMessageId(2409);
		YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA = new SystemMessageId(2410);
		THE_COLLECTION_HAS_FAILED = new SystemMessageId(2424);
		YOUR_BIRTHDAY_GIFT_HAS_ARRIVED = new SystemMessageId(2448);
		THERE_ARE_S1_DAYS_UNTIL_YOUR_CHARACTERS_BIRTHDAY = new SystemMessageId(2449);
		C1_BIRTHDAY_IS_S3_S4_S2 = new SystemMessageId(2450);
		CLOAK_REMOVED_BECAUSE_ARMOR_SET_REMOVED = new SystemMessageId(2451);
		THE_AIRSHIP_MUST_BE_SUMMONED_TO_BOARD = new SystemMessageId(2455);
		THE_AIRSHIP_NEED_CLANLVL_5_TO_SUMMON = new SystemMessageId(2456);
		THE_AIRSHIP_NEED_LICENSE_TO_SUMMON = new SystemMessageId(2457);
		THE_AIRSHIP_ALREADY_USED = new SystemMessageId(2458);
		THE_AIRSHIP_SUMMON_LICENSE_ALREADY_ACQUIRED = new SystemMessageId(2459);
		THE_AIRSHIP_IS_ALREADY_EXISTS = new SystemMessageId(2460);
		THE_AIRSHIP_NO_PRIVILEGES = new SystemMessageId(2461);
		THE_AIRSHIP_NEED_MORE_S1 = new SystemMessageId(2462);
		THE_AIRSHIP_FUEL_SOON_RUN_OUT = new SystemMessageId(2463);
		THE_AIRSHIP_FUEL_RUN_OUT = new SystemMessageId(2464);
		OLYMPIAD_3VS3_CONFIRM = new SystemMessageId(2465);
		PET_AUXILIARY_MODE_CANNOT_USE_SKILLS = new SystemMessageId(2466);
		YOU_HAVE_USED_REPORT_POINT_ON_C1_YOU_HAVE_C2_POINTS_LEFT = new SystemMessageId(2468);
		YOU_HAVE_USED_ALL_POINTS_POINTS_ARE_RESET_AT_NOON = new SystemMessageId(2469);
		TARGET_NOT_REPORT_CANNOT_REPORT_PEACE_PVP_ZONE_OR_OLYMPIAD_OR_CLAN_WAR_ENEMY = new SystemMessageId(2470);
		CANNOT_REPORT_TARGET_ALREDY_REPORTED_BY_CLAN_ALLY_MEMBER_OR_SAME_IP = new SystemMessageId(2471);
		CANNOT_REPORT_ALREDY_REPORED_FROM_THIS_ACCOUNT = new SystemMessageId(2472);
		YOU_HAVE_BEEN_REPORTED_10_MIN_CHAT_BLOCKED = new SystemMessageId(2473);
		YOU_HAVE_BEEN_REPORTED_60_MIN_PARTY_BLOCKED = new SystemMessageId(2474);
		YOU_HAVE_BEEN_REPORTED_120_MIN_PARTY_BLOCKED = new SystemMessageId(2475);
		YOU_HAVE_BEEN_REPORTED_180_MIN_PARTY_BLOCKED = new SystemMessageId(2476);
		YOU_HAVE_BEEN_REPORTED_120_MIN_ACTION_BLOCKED = new SystemMessageId(2477);
		YOU_HAVE_BEEN_REPORTED_180_MIN_ACTION_BLOCKED = new SystemMessageId(2478);
		YOU_HAVE_BEEN_REPORTED_180_MIN_ACTION_BLOCKED_2 = new SystemMessageId(2479);
		YOU_HAVE_BEEN_REPORTED_120_MIN_MOVEMENT_BLOCKED = new SystemMessageId(2480);
		C1_REPORTED_AND_IS_BEING_INVESTIGATED = new SystemMessageId(2481);
		C1_REPORTED_AND_CANNOT_PARTY = new SystemMessageId(2482);
		YOU_HAVE_BEEN_REPORTED_SO_CHATTING_NOT_ALLOWED = new SystemMessageId(2483);
		YOU_HAVE_BEEN_REPORTED_SO_PARTY_NOT_ALLOWED = new SystemMessageId(2484);
		YOU_HAVE_BEEN_REPORTED_SO_ACTIONS_NOT_ALLOWED = new SystemMessageId(2485);
		YOU_HAVE_BEEN_BLOCKED_SUBSEQUENT_VIOLATIONS_RESULT_ACCOUNT_TERMINATION = new SystemMessageId(2486);
		YOU_HAVE_BEEN_REPORTED_AS_BOT_CONTACT_OUR_CS_TEAM = new SystemMessageId(2487);
		THE_AIRSHIP_CANNOT_TELEPORT = new SystemMessageId(2491);
		THE_AIRSHIP_SUMMONED = new SystemMessageId(2492);
		THE_COLLECTION_HAS_SUCCEEDED = new SystemMessageId(2500);
		MATCH_BEING_PREPARED_TRY_LATER = new SystemMessageId(2701);
		EXCLUDED_FROM_MATCH_DUE_INCORRECT_COUNT = new SystemMessageId(2702);
		TEAM_ADJUSTED_BECAUSE_WRONG_POPULATION_RATIO = new SystemMessageId(2703);
		CANNOT_REGISTER_CAUSE_QUEUE_FULL = new SystemMessageId(2704);
		MATCH_WAITING_TIME_EXTENDED = new SystemMessageId(2705);
		CANNOT_ENTER_CAUSE_DONT_MATCH_REQUIREMENTS = new SystemMessageId(2706);
		CANNOT_REQUEST_REGISTRATION_10_SECS_AFTER = new SystemMessageId(2707);
		CANNOT_REGISTER_PROCESSING_CURSED_WEAPON = new SystemMessageId(2708);
		COLISEUM_OLYMPIAD_KRATEIS_APPLICANTS_CANNOT_PARTICIPATE = new SystemMessageId(2709);
		LOC_KEUCEREUS_S1_S2_S3 = new SystemMessageId(2710);
		LOC_IN_SEED_INFINITY_S1_S2_S3 = new SystemMessageId(2711);
		LOC_OUT_SEED_INFINITY_S1_S2_S3 = new SystemMessageId(2712);
		LOC_CLEFT_S1_S2_S3 = new SystemMessageId(2716);
		INSTANT_ZONE_S1_RESTRICTED = new SystemMessageId(2720);
		TOO_HIGH_TO_PERFORM_THIS_ACTION = new SystemMessageId(2721);
		ANOTHER_AIRSHIP_ALREADY_SUMMONED = new SystemMessageId(2722);
		YOU_CANNOT_BOARD_NOT_MEET_REQUEIREMENTS = new SystemMessageId(2727);
		ACTION_PROHIBITED_WHILE_MOUNTED_OR_ON_AN_AIRSHIP = new SystemMessageId(2728);
		YOU_CANNOT_CONTROL_THE_HELM_WHILE_TRANSFORMED = new SystemMessageId(2729);
		YOU_CANNOT_CONTROL_THE_HELM_WHILE_YOU_ARE_PETRIFIED = new SystemMessageId(2730);
		YOU_CANNOT_CONTROL_THE_HELM_WHEN_YOU_ARE_DEAD = new SystemMessageId(2731);
		YOU_CANNOT_CONTROL_THE_HELM_WHILE_FISHING = new SystemMessageId(2732);
		YOU_CANNOT_CONTROL_THE_HELM_WHILE_IN_A_BATTLE = new SystemMessageId(2733);
		YOU_CANNOT_CONTROL_THE_HELM_WHILE_IN_A_DUEL = new SystemMessageId(2734);
		YOU_CANNOT_CONTROL_THE_HELM_WHILE_IN_A_SITTING_POSITION = new SystemMessageId(2735);
		YOU_CANNOT_CONTROL_THE_HELM_WHILE_USING_A_SKILL = new SystemMessageId(2736);
		YOU_CANNOT_CONTROL_THE_HELM_WHILE_A_CURSED_WEAPON_IS_EQUIPPED = new SystemMessageId(2737);
		YOU_CANNOT_CONTROL_THE_HELM_WHILE_HOLDING_A_FLAG = new SystemMessageId(2738);
		YOU_HAVE_BEEN_REPORTED_AND_CANNOT_REPORT = new SystemMessageId(2748);
		THE_S1_WARD_HAS_BEEN_DESTROYED_C2_HAS_THE_WARD = new SystemMessageId(2750);
		THE_CHAR_THAT_ACQUIRED_S1_WARD_HAS_BEEN_KILLED = new SystemMessageId(2751);
		CANT_CONTROL_TOO_FAR = new SystemMessageId(2762);
		YOU_CANNOT_ENTER_BECAUSE_MAXIMUM_ENTRANTS = new SystemMessageId(2764);
		ONLY_ALLIANCE_CHANNEL_LEADER_CAN_ENTER = new SystemMessageId(2765);
		SEED_OF_INFINITY_STAGE_1_ATTACK_IN_PROGRESS = new SystemMessageId(2766);
		SEED_OF_INFINITY_STAGE_2_ATTACK_IN_PROGRESS = new SystemMessageId(2767);
		SEED_OF_INFINITY_CONQUEST_COMPLETE = new SystemMessageId(2768);
		SEED_OF_INFINITY_STAGE_1_DEFENSE_IN_PROGRESS = new SystemMessageId(2769);
		SEED_OF_INFINITY_STAGE_2_DEFENSE_IN_PROGRESS = new SystemMessageId(2770);
		SEED_OF_DESTRUCTION_ATTACK_IN_PROGRESS = new SystemMessageId(2771);
		SEED_OF_DESTRUCTION_CONQUEST_COMPLETE = new SystemMessageId(2772);
		SEED_OF_DESTRUCTION_DEFENSE_IN_PROGRESS = new SystemMessageId(2773);
		YOU_CAN_REPORT_IN_S1_MINS_YOU_HAVE_S2_POINTS_LEFT = new SystemMessageId(2774);
		THE_AIRSHIP_SUMMON_LICENSE_ENTERED = new SystemMessageId(2777);
		YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD = new SystemMessageId(2778);
		YOU_MUST_HAVE_MINIMUM_OF_S1_PEOPLE_TO_ENTER = new SystemMessageId(2793);
		YOU_ALREADY_REQUESTED_TW_REGISTRATION = new SystemMessageId(2795);
		THE_TERRITORY_OWNER_CLAN_CANNOT_PARTICIPATE_AS_MERCENARIES = new SystemMessageId(2796);
		NOT_TERRITORY_REGISTRATION_PERIOD = new SystemMessageId(2797);
		THE_TERRITORY_WAR_WILL_END_IN_S1_HOURS = new SystemMessageId(2798);
		THE_TERRITORY_WAR_WILL_END_IN_S1_MINUTES = new SystemMessageId(2799);
		S1_SECONDS_TO_THE_END_OF_TERRITORY_WAR = new SystemMessageId(2900);
		YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY = new SystemMessageId(2901);
		YOU_VE_ACQUIRED_THE_WARD = new SystemMessageId(2902);
		TERRITORY_WAR_HAS_BEGUN = new SystemMessageId(2903);
		TERRITORY_WAR_HAS_ENDED = new SystemMessageId(2904);
		YOU_REQUESTED_C1_TO_BE_FRIEND = new SystemMessageId(2911);
		CLAN_S1_HAS_SUCCEDED_IN_CAPTURING_S2_TERRITORY_WARD = new SystemMessageId(2913);
		TERRITORY_WAR_BEGINS_IN_20_MINUTES = new SystemMessageId(2914);
		BLOCK_CHECKER_ENDS_5 = new SystemMessageId(2922);
		BLOCK_CHECKER_ENDS_4 = new SystemMessageId(2923);
		YOU_CANNOT_ENTER_SEED_IN_FLYING_TRANSFORM = new SystemMessageId(2924);
		BLOCK_CHECKER_ENDS_3 = new SystemMessageId(2925);
		BLOCK_CHECKER_ENDS_2 = new SystemMessageId(2926);
		BLOCK_CHECKER_ENDS_1 = new SystemMessageId(2927);
		TEAM_C1_WON = new SystemMessageId(2928);
		S2_UNIT_OF_THE_ITEM_S1_REQUIRED = new SystemMessageId(2961);
		CANCEL_NOBLESSE_QUESTS = new SystemMessageId(2964);
		PAYMENT_REQUEST_NO_ITEM = new SystemMessageId(2966);
		CANT_FORWARD_MAIL_LIMIT_EXCEEDED = new SystemMessageId(2968);
		CANT_FORWARD_LESS_THAN_MINUTE = new SystemMessageId(2969);
		CANT_FORWARD_NOT_IN_PEACE_ZONE = new SystemMessageId(2970);
		CANT_FORWARD_DURING_EXCHANGE = new SystemMessageId(2971);
		CANT_FORWARD_PRIVATE_STORE = new SystemMessageId(2972);
		CANT_FORWARD_DURING_ENCHANT = new SystemMessageId(2973);
		CANT_FORWARD_BAD_ITEM = new SystemMessageId(2974);
		CANT_FORWARD_NO_ADENA = new SystemMessageId(2975);
		CANT_RECEIVE_NOT_IN_PEACE_ZONE = new SystemMessageId(2976);
		CANT_RECEIVE_DURING_EXCHANGE = new SystemMessageId(2977);
		CANT_RECEIVE_PRIVATE_STORE = new SystemMessageId(2978);
		CANT_RECEIVE_DURING_ENCHANT = new SystemMessageId(2979);
		CANT_RECEIVE_NO_ADENA = new SystemMessageId(2980);
		CANT_RECEIVE_INVENTORY_FULL = new SystemMessageId(2981);
		CANT_CANCEL_NOT_IN_PEACE_ZONE = new SystemMessageId(2982);
		CANT_CANCEL_DURING_EXCHANGE = new SystemMessageId(2983);
		CANT_CANCEL_PRIVATE_STORE = new SystemMessageId(2984);
		CANT_CANCEL_DURING_ENCHANT = new SystemMessageId(2985);
		CANT_CANCEL_INVENTORY_FULL = new SystemMessageId(2988);
		RECIPIENT_NOT_EXIST = new SystemMessageId(3002);
		MAIL_ARRIVED = new SystemMessageId(3008);
		MAIL_SUCCESSFULLY_SENT = new SystemMessageId(3009);
		MAIL_SUCCESSFULLY_RETURNED = new SystemMessageId(3010);
		MAIL_SUCCESSFULLY_CANCELLED = new SystemMessageId(3011);
		MAIL_SUCCESSFULLY_RECEIVED = new SystemMessageId(3012);
		C1_SUCCESSFULY_ENCHANTED_A_S2_S3 = new SystemMessageId(3013);
		DO_YOU_WISH_TO_ERASE_MAIL = new SystemMessageId(3014);
		PLEASE_SELECT_MAIL_TO_BE_DELETED = new SystemMessageId(3015);
		ITEM_SELECTION_POSSIBLE_UP_TO_8 = new SystemMessageId(3016);
		YOU_CANT_SEND_MAIL_TO_YOURSELF = new SystemMessageId(3019);
		PAYMENT_AMOUNT_NOT_ENTERED = new SystemMessageId(3020);
		I_CAN_FEEL_ENERGY_KASHA_EYE_GETTING_STRONGER_RAPIDLY = new SystemMessageId(3023);
		KASHA_EYE_PITCHES_TOSSES_EXPLODE = new SystemMessageId(3024);
		PAYMENT_OF_S1_ADENA_COMPLETED_BY_S2 = new SystemMessageId(3025);
		YOU_CANNOT_USE_SKILL_ENCHANT_ON_THIS_LEVEL = new SystemMessageId(3026);
		YOU_CANNOT_USE_SKILL_ENCHANT_IN_THIS_CLASS = new SystemMessageId(3027);
		YOU_CANNOT_USE_SKILL_ENCHANT_ATTACKING_TRANSFORMED_BOAT = new SystemMessageId(3028);
		S1_RETURNED_MAIL = new SystemMessageId(3029);
		YOU_CANT_CANCEL_RECEIVED_MAIL = new SystemMessageId(3030);
		USING_EINHASAD_HOLY_SWORD_DEFEAT_LILIMS = new SystemMessageId(3031);
		SNEAK_INTO_DAWNS_DOCUMENT_STORAGE = new SystemMessageId(3033);
		DOOR_IS_ENTRANCE_APPROACH_DEVICE = new SystemMessageId(3034);
		MALE_GUARDS_CAN_DETECT_FEMALES_DONT = new SystemMessageId(3037);
		FEMALE_GUARDS_NOTICE_BETTER_THAN_MALE = new SystemMessageId(3038);
		USING_EINHASAD_HOLY_WATER_TO_OPEN_DOOR = new SystemMessageId(3039);
		USING_COURT_MAGICIANS_STAFF_TO_OPEN_DOOR = new SystemMessageId(3040);
		S1_NOT_RECEIVE_DURING_WAITING_TIME_MAIL_RETURNED = new SystemMessageId(3059);
		THE_SEALING_DEVICE_ACTIVATION_COMPLETE = new SystemMessageId(3060);
		DO_YOU_WANT_TO_PAY_S1_ADENA = new SystemMessageId(3062);
		DO_YOU_WANT_TO_FORWARD = new SystemMessageId(3063);
		UNREAD_MAIL = new SystemMessageId(3064);
		LOC_DELUSION_CHAMBER = new SystemMessageId(3065);
		CANT_USE_MAIL_OUTSIDE_PEACE_ZONE = new SystemMessageId(3066);
		S1_CANCELLED_MAIL = new SystemMessageId(3067);
		MAIL_RETURNED = new SystemMessageId(3068);
		DO_YOU_WANT_TO_CANCEL_TRANSACTION = new SystemMessageId(3069);
		S1_ACQUIRED_ATTACHED_ITEM = new SystemMessageId(3072);
		YOU_ACQUIRED_S2_S1 = new SystemMessageId(3073);
		ALLOWED_LENGTH_FOR_RECIPIENT_EXCEEDED = new SystemMessageId(3074);
		ALLOWED_LENGTH_FOR_TITLE_EXCEEDED = new SystemMessageId(3075);
		MAIL_LIMIT_EXCEEDED = new SystemMessageId(3077);
		YOU_MAKING_PAYMENT_REQUEST = new SystemMessageId(3078);
		ITEMS_IN_PET_INVENTORY = new SystemMessageId(3079);
		CANNOT_RESET_SKILL_LINK_BECAUSE_NOT_ENOUGH_ADENA = new SystemMessageId(3080);
		YOU_CANNOT_RECEIVE_CONDITION_OPPONENT_CANT_ACQUIRE_ADENA = new SystemMessageId(3081);
		YOU_CANNOT_SEND_MAIL_TO_CHAR_BLOCK_YOU = new SystemMessageId(3082);
		A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS = new SystemMessageId(3094);
		YOU_ARE_NO_LONGER_PROTECTED_FROM_AGGRESSIVE_MONSTERS = new SystemMessageId(3108);
		COUPLE_ACTION_DENIED = new SystemMessageId(3119);
		TARGET_DO_NOT_MEET_LOC_REQUIREMENTS = new SystemMessageId(3120);
		COUPLE_ACTION_CANCELED = new SystemMessageId(3121);
		WRONG_SIZE_UPLOADED_CREST = new SystemMessageId(3122);
		C1_IS_IN_PRIVATE_SHOP_MODE_OR_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION = new SystemMessageId(3123);
		C1_IS_FISHING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION = new SystemMessageId(3124);
		C1_IS_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION = new SystemMessageId(3125);
		C1_IS_ALREADY_PARTICIPATING_IN_A_COUPLE_ACTION_AND_CANNOT_BE_REQUESTED_FOR_ANOTHER_COUPLE_ACTION = new SystemMessageId(3126);
		C1_IS_IN_A_CHAOTIC_STATE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION = new SystemMessageId(3127);
		C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION = new SystemMessageId(3128);
		C1_IS_PARTICIPATING_IN_A_HIDEOUT_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION = new SystemMessageId(3129);
		C1_IS_IN_A_CASTLE_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION = new SystemMessageId(3130);
		C1_IS_RIDING_A_SHIP_STEED_OR_STRIDER_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION = new SystemMessageId(3131);
		C1_IS_CURRENTLY_TELEPORTING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION = new SystemMessageId(3132);
		C1_IS_CURRENTLY_TRANSFORMING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION = new SystemMessageId(3133);
		REQUESTING_APPROVAL_CHANGE_PARTY_LOOT_S1 = new SystemMessageId(3135);
		PARTY_LOOT_CHANGE_CANCELLED = new SystemMessageId(3137);
		PARTY_LOOT_CHANGED_S1 = new SystemMessageId(3138);
		C1_IS_CURRENTLY_DEAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION = new SystemMessageId(3139);
		CLAN_CREST_WAS_SUCCESSFULLY_REGISTRED = new SystemMessageId(3140);
		THE_S2_ATTRIBUTE_WAS_SUCCESSFULLY_BESTOWED_ON_S1_RES_TO_S3_INCREASED = new SystemMessageId(3144);
		YOU_WILL_BE_EXPELLED_IN_S1 = new SystemMessageId(3147);
		YOU_HAVE_REQUESTED_COUPLE_ACTION_C1 = new SystemMessageId(3150);
		S1_S2_ATTRIBUTE_REMOVED_RESISTANCE_S3_DECREASED = new SystemMessageId(3152);
		YOU_DO_NOT_HAVE_ENOUGH_FUNDS_TO_CANCEL_ATTRIBUTE = new SystemMessageId(3156);
		S1_S2_S3_ATTRIBUTE_REMOVED_RESISTANCE_TO_S4_DECREASED = new SystemMessageId(3160);
		THE_S3_ATTRIBUTE_BESTOWED_ON_S1_S2_RESISTANCE_TO_S4_INCREASED = new SystemMessageId(3163);
		C1_IS_SET_TO_REFUSE_COUPLE_ACTIONS = new SystemMessageId(3164);
		C1_IS_SET_TO_REFUSE_PARTY_REQUEST = new SystemMessageId(3168);
		C1_IS_SET_TO_REFUSE_DUEL_REQUEST = new SystemMessageId(3169);
		YOU_CURRENTLY_DO_NOT_HAVE_ANY_RECOMMENDATIONS = new SystemMessageId(3206);
		YOU_OBTAINED_S1_RECOMMENDATIONS = new SystemMessageId(3207);
		WHEN_YOUR_PETS_HUNGER_GAUGE_IS_AT_0_YOU_CANNOT_USE_YOUR_PET = new SystemMessageId(3212);
		YOUR_PET_IS_STARVING_AND_WILL_NOT_OBEY_UNTIL_IT_GETS_ITS_FOOD_FEED_YOUR_PET = new SystemMessageId(3213);
		S1_SUCCESSFULLY_ADDED_TO_CONTACT_LIST = new SystemMessageId(3214);
		NAME_S1_NOT_EXIST_TRY_ANOTHER_NAME = new SystemMessageId(3215);
		NAME_ALREADY_EXIST_ON_CONTACT_LIST = new SystemMessageId(3216);
		NAME_NOT_REGISTERED_ON_CONTACT_LIST = new SystemMessageId(3217);
		S1_SUCCESFULLY_DELETED_FROM_CONTACT_LIST = new SystemMessageId(3219);
		CANNOT_ADD_YOUR_NAME_ON_CONTACT_LIST = new SystemMessageId(3221);
		CONTACT_LIST_LIMIT_REACHED = new SystemMessageId(3222);
		MAX_OLY_WEEKLY_MATCHES_REACHED = new SystemMessageId(3224);
		MAX_OLY_WEEKLY_MATCHES_REACHED_60_NON_CLASSED_30_CLASSED_10_TEAM = new SystemMessageId(3225);
		CANNOT_MOVE_WHILE_SPEAKING_TO_AN_NPC = new SystemMessageId(3226);
		ARCANE_SHIELD_DECREASED_YOUR_MP_BY_S1_INSTEAD_OF_HP = new SystemMessageId(3255);
		YOU_EARNED_S1_EXP_BONUS_S2_AND_S3_SP_BONUS_S4 = new SystemMessageId(3259);
		MP_BECAME_0_ARCANE_SHIELD_DISAPPEARING = new SystemMessageId(3256);
		CANNOT_USE_SKILL_WITHOUT_SERVITOR = new SystemMessageId(3260);
		YOU_HAVE_S1_MATCHES_REMAINING_THAT_YOU_CAN_PARTECIPATE_IN_THIS_WEEK_S2_CLASSED_S3_NON_CLASSED_S4_TEAM = new SystemMessageId(3261);
		THE_ANGEL_NEVIT_HAS_BLESSED_YOU_FROM_ABOVE = new SystemMessageId(3266);
		YOU_ARE_STARTING_TO_FEEL_THE_EFFECTS_OF_NEVITS_ADVENT_BLESSING = new SystemMessageId(3267);
		YOU_ARE_FURTHER_INFUSED_WITH_THE_BLESSINGS_OF_NEVIT = new SystemMessageId(3268);
		NEVITS_ADVENT_BLESSING_SHINES_STRONGLY_FROM_ABOVE = new SystemMessageId(3269);
		NEVITS_ADVENT_BLESSING_HAS_ENDED = new SystemMessageId(3275);
		SAFE_ENCHANT_FAILED = new SystemMessageId(6004);
		YOU_CANNOT_BOOKMARK_THIS_LOCATION_BECAUSE_YOU_DO_NOT_HAVE_A_MY_TELEPORT_FLAG = new SystemMessageId(6501);
		THOMAS_D_TURKEY_APPEARED = new SystemMessageId(6503);
		THOMAS_D_TURKEY_DEFETED = new SystemMessageId(6504);
		THOMAS_D_TURKEY_DISAPPEARED = new SystemMessageId(6505);
		YOU_CANNOT_MOUNT_A_STEED_WHILE_HOLDING_A_FLAG = new SystemMessageId(6506);
		CHAOTIC_ZONE = new SystemMessageId(6510);
		
		buildFastLookupTable();
	}
	
	private static final void buildFastLookupTable()
	{
		final Field[] fields = SystemMessageId.class.getDeclaredFields();
		
		int mod;
		SystemMessageId smId;
		for (final Field field : fields)
		{
			mod = field.getModifiers();
			if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && Modifier.isFinal(mod) && field.getType().equals(SystemMessageId.class))
			{
				try
				{
					smId = (SystemMessageId) field.get(null);
					smId.setName(field.getName());
					smId.setParamCount(parseMessageParameters(field.getName()));
					VALUES.put(smId.getId(), smId);
				}
				catch (final Exception e)
				{
					_log.warn("SystemMessageId: Failed field access for '" + field.getName() + "'", e);
				}
			}
		}
	}
	
	private static final int parseMessageParameters(final String name)
	{
		int paramCount = 0;
		char c1, c2;
		for (int i = 0; i < (name.length() - 1); i++)
		{
			c1 = name.charAt(i);
			if ((c1 == 'C') || (c1 == 'S'))
			{
				c2 = name.charAt(i + 1);
				if (Character.isDigit(c2))
				{
					paramCount = Math.max(paramCount, Character.getNumericValue(c2));
					i++;
				}
			}
		}
		return paramCount;
	}
	
	public static final SystemMessageId getSystemMessageId(final int id)
	{
		final SystemMessageId smi = getSystemMessageIdInternal(id);
		return smi == null ? new SystemMessageId(id) : smi;
	}
	
	private static final SystemMessageId getSystemMessageIdInternal(final int id)
	{
		return VALUES.get(id);
	}
	
	public static final SystemMessageId getSystemMessageId(final String name)
	{
		try
		{
			return (SystemMessageId) SystemMessageId.class.getField(name).get(null);
		}
		catch (final Exception e)
		{
			return null;
		}
	}
	
	public static final void reloadLocalisations()
	{
		for (final SystemMessageId smId : VALUES.values())
		{
			if (smId != null)
			{
				smId.removeAllLocalisations();
			}
		}
		
		if (!Config.L2JMOD_MULTILANG_SM_ENABLE)
		{
			_log.info("SystemMessageId: MultiLanguage disabled.");
			return;
		}
		
		final List<String> languages = Config.L2JMOD_MULTILANG_SM_ALLOWED;
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		
		File file;
		Node node;
		Document doc;
		NamedNodeMap nnmb;
		SystemMessageId smId;
		String text;
		for (final String lang : languages)
		{
			file = new File(Config.DATAPACK_ROOT, "/data/lang/" + lang + "/sm/SystemMessageLocalisation.xml");
			if (!file.isFile())
			{
				continue;
			}
			
			_log.info("SystemMessageId: Loading localisation for '" + lang + "'");
			
			try
			{
				doc = factory.newDocumentBuilder().parse(file);
				for (Node na = doc.getFirstChild(); na != null; na = na.getNextSibling())
				{
					if ("list".equals(na.getNodeName()))
					{
						for (Node nb = na.getFirstChild(); nb != null; nb = nb.getNextSibling())
						{
							if ("sm".equals(nb.getNodeName()))
							{
								nnmb = nb.getAttributes();
								node = nnmb.getNamedItem("id");
								if (node != null)
								{
									smId = getSystemMessageId(Integer.parseInt(node.getNodeValue()));
									if (smId == null)
									{
										_log.warn("SystemMessageId: Unknown SMID '" + node.getNodeValue() + "', lang '" + lang + "'.");
										continue;
									}
								}
								else
								{
									node = nnmb.getNamedItem("name");
									smId = getSystemMessageId(node.getNodeValue());
									if (smId == null)
									{
										_log.warn("SystemMessageId: Unknown SMID '" + node.getNodeValue() + "', lang '" + lang + "'.");
										continue;
									}
								}
								
								node = nnmb.getNamedItem("text");
								if (node == null)
								{
									_log.warn("SystemMessageId: No text defined for SMID '" + smId + "', lang '" + lang + "'.");
									continue;
								}
								
								text = node.getNodeValue();
								if (text.isEmpty() || (text.length() > 255))
								{
									_log.warn("SystemMessageId: Invalid text defined for SMID '" + smId + "' (to long or empty), lang '" + lang + "'.");
									continue;
								}
								
								smId.attachLocalizedText(lang, text);
							}
						}
					}
				}
			}
			catch (final Exception e)
			{
				_log.error("SystemMessageId: Failed loading '" + file + "'", e);
			}
		}
	}
	
	private final int _id;
	private String _name;
	private byte _params;
	private SMLocalisation[] _localisations;
	private SystemMessage _staticSystemMessage;
	
	private SystemMessageId(final int id)
	{
		_id = id;
		_localisations = EMPTY_SML_ARRAY;
	}
	
	public final int getId()
	{
		return _id;
	}
	
	private final void setName(final String name)
	{
		_name = name;
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public final int getParamCount()
	{
		return _params;
	}
	
	/**
	 * You better don`t touch this!
	 * @param params
	 */
	public final void setParamCount(final int params)
	{
		if (params < 0)
		{
			throw new IllegalArgumentException("Invalid negative param count: " + params);
		}
		
		if (params > 10)
		{
			throw new IllegalArgumentException("Maximum param count exceeded: " + params);
		}
		
		if (params != 0)
		{
			_staticSystemMessage = null;
		}
		
		_params = (byte) params;
	}
	
	public final SMLocalisation getLocalisation(final String lang)
	{
		SMLocalisation sml;
		for (int i = _localisations.length; i-- > 0;)
		{
			sml = _localisations[i];
			if (sml.getLanguage().hashCode() == lang.hashCode())
			{
				return sml;
			}
		}
		return null;
	}
	
	public final void attachLocalizedText(final String lang, final String text)
	{
		final int length = _localisations.length;
		final SMLocalisation[] localisations = Arrays.copyOf(_localisations, length + 1);
		localisations[length] = new SMLocalisation(lang, text);
		_localisations = localisations;
	}
	
	public final void removeAllLocalisations()
	{
		_localisations = EMPTY_SML_ARRAY;
	}
	
	public final SystemMessage getStaticSystemMessage()
	{
		return _staticSystemMessage;
	}
	
	public final void setStaticSystemMessage(final SystemMessage sm)
	{
		_staticSystemMessage = sm;
	}
	
	@Override
	public final String toString()
	{
		return "SM[" + getId() + ":" + getName() + "]";
	}
	
	public static final class SMLocalisation
	{
		private final String _lang;
		private final Builder _builder;
		
		public SMLocalisation(final String lang, final String text)
		{
			_lang = lang;
			_builder = Builder.newBuilder(text);
		}
		
		public final String getLanguage()
		{
			return _lang;
		}
		
		public final String getLocalisation(final Object... params)
		{
			return _builder.toString(params);
		}
	}
}