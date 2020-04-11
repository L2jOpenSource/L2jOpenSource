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
package l2r.gameserver.model.actor.instance;

import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;

// This class is here mostly for convenience and for avoidance of hardcoded IDs.
// It refers to Beast (mobs) that can be attacked but can also be fed
// For example, the Beast Farm's Alpen Buffalo.
// This class is only truly used by the handlers in order to check the correctness
// of the target.  However, no additional tasks are needed, since they are all
// handled by scripted AI.
public class L2FeedableBeastInstance extends L2MonsterInstance
{
	public L2FeedableBeastInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2FeedableBeastInstance);
	}
}
