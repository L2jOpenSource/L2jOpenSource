package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.entity.Message;
import l2r.gameserver.model.itemcontainer.ItemContainer;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public class ExReplySentPost extends AbstractItemPacket
{
	private final Message _msg;
	private L2ItemInstance[] _items = null;
	
	public ExReplySentPost(Message msg)
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
		writeH(0xAD);
		
		writeD(_msg.getId());
		writeD(_msg.isLocked() ? 1 : 0);
		writeS(_msg.getReceiverName());
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
			
			writeQ(_msg.getReqAdena());
			writeD(_msg.getSendBySystem());
		}
		else
		{
			writeD(0x00);
			writeQ(_msg.getReqAdena());
		}
	}
}
