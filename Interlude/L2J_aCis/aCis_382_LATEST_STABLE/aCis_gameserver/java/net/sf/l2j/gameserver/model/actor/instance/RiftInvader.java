package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

public class RiftInvader extends Monster
{
	// Not longer needed since rift monster targeting control now is handled by the room zones for any mob
	public RiftInvader(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
}
