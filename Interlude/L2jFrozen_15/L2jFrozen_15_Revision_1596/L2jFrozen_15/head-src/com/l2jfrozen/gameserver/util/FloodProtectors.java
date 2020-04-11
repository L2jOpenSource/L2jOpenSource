package com.l2jfrozen.gameserver.util;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.network.L2GameClient;

/**
 * Collection of flood protectors for single player.
 * @author fordfrog
 */
public final class FloodProtectors
{
	/**
	 * Use-item flood protector.
	 */
	private final FloodProtectorAction useItem;
	/**
	 * Roll-dice flood protector.
	 */
	private final FloodProtectorAction rollDice;
	/**
	 * Firework flood protector.
	 */
	private final FloodProtectorAction firework;
	/**
	 * Item-pet-summon flood protector.
	 */
	private final FloodProtectorAction itemPetSummon;
	/**
	 * Hero-voice flood protector.
	 */
	private final FloodProtectorAction heroVoice;
	/**
	 * Global-chat flood protector.
	 */
	private final FloodProtectorAction globalChat;
	/**
	 * Subclass flood protector.
	 */
	private final FloodProtectorAction subclass;
	/**
	 * Drop-item flood protector.
	 */
	private final FloodProtectorAction dropItem;
	/**
	 * Server-bypass flood protector.
	 */
	private final FloodProtectorAction serverBypass;
	/**
	 * Multisell flood protector.
	 */
	private final FloodProtectorAction multiSell;
	/**
	 * Transaction flood protector.
	 */
	private final FloodProtectorAction transaction;
	/**
	 * Manufacture flood protector.
	 */
	private final FloodProtectorAction manufacture;
	/**
	 * Manor flood protector.
	 */
	private final FloodProtectorAction manor;
	/**
	 * Character Select protector
	 */
	private final FloodProtectorAction characterSelect;
	/**
	 * Unknown Packets protector
	 */
	private final FloodProtectorAction unknownPackets;
	/**
	 * Party Invitation flood protector.
	 */
	private final FloodProtectorAction partyInvitation;
	/**
	 * Say Action protector
	 */
	private final FloodProtectorAction sayAction;
	/**
	 * Move Action protector
	 */
	private final FloodProtectorAction moveAction;
	/**
	 * Generic Action protector
	 */
	private final FloodProtectorAction genericAction;
	/**
	 * Macro protector
	 */
	private final FloodProtectorAction macro;
	/**
	 * Potion protector
	 */
	private final FloodProtectorAction potion;
	
	/**
	 * Creates new instance of FloodProtectors.
	 * @param client for which the collection of flood protectors is being created.
	 */
	public FloodProtectors(final L2GameClient client)
	{
		super();
		useItem = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_USE_ITEM);
		rollDice = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_ROLL_DICE);
		firework = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_FIREWORK);
		itemPetSummon = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_ITEM_PET_SUMMON);
		heroVoice = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_HERO_VOICE);
		globalChat = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_GLOBAL_CHAT);
		subclass = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_SUBCLASS);
		dropItem = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_DROP_ITEM);
		serverBypass = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_SERVER_BYPASS);
		multiSell = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MULTISELL);
		transaction = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_TRANSACTION);
		manufacture = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MANUFACTURE);
		manor = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MANOR);
		characterSelect = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_CHARACTER_SELECT);
		
		unknownPackets = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_UNKNOWN_PACKETS);
		partyInvitation = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_PARTY_INVITATION);
		sayAction = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_SAY_ACTION);
		moveAction = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MOVE_ACTION);
		genericAction = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_GENERIC_ACTION);
		macro = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MACRO);
		potion = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_POTION);
	}
	
	/**
	 * Returns {@link #useItem}.
	 * @return {@link #useItem}
	 */
	public FloodProtectorAction getUseItem()
	{
		return useItem;
	}
	
	/**
	 * Returns {@link #rollDice}.
	 * @return {@link #rollDice}
	 */
	public FloodProtectorAction getRollDice()
	{
		return rollDice;
	}
	
	/**
	 * Returns {@link #firework}.
	 * @return {@link #firework}
	 */
	public FloodProtectorAction getFirework()
	{
		return firework;
	}
	
	/**
	 * Returns {@link #itemPetSummon}.
	 * @return {@link #itemPetSummon}
	 */
	public FloodProtectorAction getItemPetSummon()
	{
		return itemPetSummon;
	}
	
	/**
	 * Returns {@link #heroVoice}.
	 * @return {@link #heroVoice}
	 */
	public FloodProtectorAction getHeroVoice()
	{
		return heroVoice;
	}
	
	/**
	 * Returns {@link #globalChat}.
	 * @return {@link #globalChat}
	 */
	public FloodProtectorAction getGlobalChat()
	{
		return globalChat;
	}
	
	/**
	 * Returns {@link #subclass}.
	 * @return {@link #subclass}
	 */
	public FloodProtectorAction getSubclass()
	{
		return subclass;
	}
	
	/**
	 * Returns {@link #dropItem}.
	 * @return {@link #dropItem}
	 */
	public FloodProtectorAction getDropItem()
	{
		return dropItem;
	}
	
	/**
	 * Returns {@link #serverBypass}.
	 * @return {@link #serverBypass}
	 */
	public FloodProtectorAction getServerBypass()
	{
		return serverBypass;
	}
	
	public FloodProtectorAction getMultiSell()
	{
		return multiSell;
	}
	
	/**
	 * Returns {@link #transaction}.
	 * @return {@link #transaction}
	 */
	public FloodProtectorAction getTransaction()
	{
		return transaction;
	}
	
	/**
	 * Returns {@link #manufacture}.
	 * @return {@link #manufacture}
	 */
	public FloodProtectorAction getManufacture()
	{
		return manufacture;
	}
	
	/**
	 * Returns {@link #manor}.
	 * @return {@link #manor}
	 */
	public FloodProtectorAction getManor()
	{
		return manor;
	}
	
	/**
	 * Returns {@link #characterSelect}.
	 * @return {@link #characterSelect}
	 */
	public FloodProtectorAction getCharacterSelect()
	{
		return characterSelect;
	}
	
	/**
	 * Returns {@link #unknownPackets}.
	 * @return {@link #unknownPackets}
	 */
	public FloodProtectorAction getUnknownPackets()
	{
		return unknownPackets;
	}
	
	/**
	 * Returns {@link #partyInvitation}.
	 * @return {@link #partyInvitation}
	 */
	public FloodProtectorAction getPartyInvitation()
	{
		return partyInvitation;
	}
	
	/**
	 * Returns {@link #sayAction}.
	 * @return {@link #sayAction}
	 */
	public FloodProtectorAction getSayAction()
	{
		return sayAction;
	}
	
	/**
	 * Returns {@link #moveAction}.
	 * @return {@link #moveAction}
	 */
	public FloodProtectorAction getMoveAction()
	{
		return moveAction;
	}
	
	/**
	 * Returns {@link #genericAction}.
	 * @return {@link #genericAction}
	 */
	public FloodProtectorAction getGenericAction()
	{
		return genericAction;
	}
	
	/**
	 * Returns {@link #macro}.
	 * @return {@link #macro}
	 */
	public FloodProtectorAction getMacro()
	{
		return macro;
	}
	
	/**
	 * Returns {@link #potion}.
	 * @return {@link #potion}
	 */
	public FloodProtectorAction getUsePotion()
	{
		return potion;
	}
}