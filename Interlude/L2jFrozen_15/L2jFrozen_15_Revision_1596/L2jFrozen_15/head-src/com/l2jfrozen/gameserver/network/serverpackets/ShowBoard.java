package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.List;

public class ShowBoard extends L2GameServerPacket
{
	private final String htmlCode;
	private final String id;
	private List<String> arg;
	
	public ShowBoard(final String htmlCode, final String id)
	{
		this.id = id;
		this.htmlCode = htmlCode; // html code must not exceed 8192 bytes
	}
	
	public ShowBoard(final List<String> arg)
	{
		id = "1002";
		htmlCode = null;
		this.arg = arg;
		
	}
	
	private byte[] get1002()
	{
		int len = id.getBytes().length * 2 + 2;
		for (final String arg : arg)
		{
			len += (arg.getBytes().length + 4) * 2;
		}
		final byte data[] = new byte[len];
		int i = 0;
		for (int j = 0; j < id.getBytes().length; j++, i += 2)
		{
			data[i] = id.getBytes()[j];
			data[i + 1] = 0;
		}
		data[i] = 8;
		i++;
		data[i] = 0;
		i++;
		for (final String arg : arg)
		{
			for (int j = 0; j < arg.getBytes().length; j++, i += 2)
			{
				data[i] = arg.getBytes()[j];
				data[i + 1] = 0;
			}
			data[i] = 0x20;
			i++;
			data[i] = 0x0;
			i++;
			data[i] = 0x8;
			i++;
			data[i] = 0x0;
			i++;
		}
		return data;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x6e);
		writeC(0x01); // c4 1 to show community 00 to hide
		writeS("bypass _bbshome"); // top
		writeS("bypass _bbsgetfav"); // favorite
		writeS("bypass _bbsloc"); // region
		writeS("bypass _bbsclan"); // clan
		writeS("bypass _bbsmemo"); // memo
		writeS("bypass _bbsmail"); // mail
		writeS("bypass _bbsfriends"); // friends
		writeS("bypass bbs_add_fav"); // add fav.
		if (!id.equals("1002"))
		{
			// getBytes is a very costy operation, and should only be called once
			byte htmlBytes[] = null;
			if (htmlCode != null)
			{
				htmlBytes = htmlCode.getBytes();
			}
			final byte data[] = new byte[2 + 2 + 2 + id.getBytes().length * 2 + 2 * (htmlBytes != null ? htmlBytes.length : 0)];
			int i = 0;
			for (int j = 0; j < id.getBytes().length; j++, i += 2)
			{
				data[i] = id.getBytes()[j];
				data[i + 1] = 0;
			}
			data[i] = 8;
			i++;
			data[i] = 0;
			i++;
			if (htmlBytes != null)
			{
				for (int j = 0; j < htmlBytes.length; i += 2, j++)
				{
					data[i] = htmlBytes[j];
					data[i + 1] = 0;
				}
			}
			data[i] = 0;
			i++;
			data[i] = 0;
			// writeS(_htmlCode); // current page
			writeB(data);
		}
		else
		{
			writeB(get1002());
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 6e ShowBoard";
	}
}
