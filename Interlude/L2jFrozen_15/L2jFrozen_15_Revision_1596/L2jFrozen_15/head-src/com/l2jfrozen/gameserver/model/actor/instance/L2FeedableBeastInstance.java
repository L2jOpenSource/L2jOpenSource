package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

// This class is here mostly for convinience and for avoidance of hardcoded IDs.
// It refers to Beast (mobs) that can be attacked but can also be fed
// For example, the Beast Farm's Alpen Buffalo.
// This class is only trully used by the handlers in order to check the correctness
// of the target.  However, no additional tasks are needed, since they are all
// handled by scripted AI.
public class L2FeedableBeastInstance extends L2MonsterInstance
{
	public L2FeedableBeastInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
	}
}
