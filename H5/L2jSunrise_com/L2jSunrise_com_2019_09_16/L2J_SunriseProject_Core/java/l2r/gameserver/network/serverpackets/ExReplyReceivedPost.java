package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.entity.Message;
import l2r.gameserver.model.itemcontainer.ItemContainer;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public class ExReplyReceivedPost extends AbstractItemPacket
{
	private final Message _msg;
	private L2ItemInstance[] _items = null;
	
	public ExReplyReceivedPost(Message msg)
	{
		_msg = msg;
		if (msg.hasAttachments())
		{
			final ItemContainer attachments = msg.getAttachments();
			if ((attachments != null) && (attachments.getSize() > 0))
			{
				_items = attachments.getItems();
			}
			else
			{
				_log.warn("Message " + msg.getId() + " has attachments but itemcontainer is empty.");
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xAB);
		
		writeD(_msg.getId());
		writeD(_msg.isLocked() ? 1 : 0);
		writeD(0x00); // Unknown
		writeS(_msg.getSenderName());
		writeS(_msg.getSubject());
		writeS(_msg.getContent());
		
		if ((_items != null) && (_items.length > 0))
		{
			writeD(_items.length);
			for (L2ItemInstance item : _items)
			{
				writeItem(item);
				writeD(item.getObjectId());
			}
		}
		else
		{
			writeD(0x00);
		}
		
		writeQ(_msg.getReqAdena());
		writeD(_msg.hasAttachments() ? 1 : 0);
		writeD(_msg.getSendBySystem());
	}
}
