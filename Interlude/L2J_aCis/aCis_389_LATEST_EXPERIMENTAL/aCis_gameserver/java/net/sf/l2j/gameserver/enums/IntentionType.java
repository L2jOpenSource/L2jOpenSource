package net.sf.l2j.gameserver.enums;

/**
 * Enumeration of generic intentions of an actor.
 */
public enum IntentionType
{
	/** Stop all actions and do nothing. In case of Npc, disconnect AI if no players around. */
	IDLE,
	/** Alerted state without goal : scan attackable targets, random walk, etc. */
	ACTIVE,
	/** Rest (sit until attacked). */
	SIT,
	/** Stand Up. */
	STAND,
	/** Move to target if too far, then attack it - may be ignored (another target, invalid zoning, etc). */
	ATTACK,
	/** Move to target if too far, then cast a spell. */
	CAST,
	/** Move to another location. */
	MOVE_TO,
	/** Check target's movement and follow it. */
	FOLLOW,
	/** Move to target if too far, then pick up the item. */
	PICK_UP,
	/** Move to target if too far, then interact. */
	INTERACT,
	/** Use an Item. */
	USE_ITEM,
	/** Fake death. */
	FAKE_DEATH;
	
	/**
	 * This method holds behavioral information on which Intentions are scheduled and which are cast immediately.
	 * <ul>
	 * <li>Nothing is scheduled after FOLLOW, PICK_UP, INTERACT, ATTACK. They can all be interrupted (isAttackingNow is another matter, it's never interrupted).</li>
	 * <li>Only STAND is scheduled after SIT and FAKE_DEATH. Anything else is illegal and is cast immediately so it can be rejected.</li>
	 * <li>Only SIT is scheduled after MOVE_TO. Anything else is cast immediately.</li>
	 * <li>All possible intentions are scheduled after STAND and CAST.</li>
	 * </ul>
	 * @param newIntention : The {@link IntentionType} to test.
	 * @return True if the {@link IntentionType} set as parameter can be sheduled after this {@link IntentionType}, otherwise cast it immediately.
	 */
	public boolean canScheduleAfter(IntentionType newIntention)
	{
		switch (this)
		{
			case SIT:
			case FAKE_DEATH:
				return newIntention == IntentionType.STAND;
			
			case STAND:
			case CAST:
				return true;
			
			case MOVE_TO:
				return newIntention == IntentionType.SIT;
		}
		return false;
	}
}