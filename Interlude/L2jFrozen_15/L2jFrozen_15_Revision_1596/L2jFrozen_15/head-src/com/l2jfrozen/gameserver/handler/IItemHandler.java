package com.l2jfrozen.gameserver.handler;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;

/**
 * Mother class of all itemHandlers.<BR>
 * <BR>
 * an IItemHandler implementation has to be stateless
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:30:09 $
 */

public interface IItemHandler
{
	/**
	 * Launch task associated to the item.
	 * @param playable : L2PlayableInstance designating the player
	 * @param item     : L2ItemInstance designating the item to use
	 */
	public void useItem(L2PlayableInstance playable, L2ItemInstance item);
	
	/**
	 * Returns the list of item IDs corresponding to the type of item.<BR>
	 * <BR>
	 * <B><I>Use :</I></U><BR>
	 * This method is called at initialization to register all the item IDs automatically
	 * @return int[] designating all itemIds for a type of item.
	 */
	public int[] getItemIds();
}
