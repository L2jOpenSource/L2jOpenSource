package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;

/**
 * Changes the sky color depending on the outcome of the Seven Signs competition. packet type id 0xf8 format: c h
 * @author Tempy
 */
public class SignsSky extends L2GameServerPacket
{
	private int state = 0;
	
	public SignsSky()
	{
		final int compWinner = SevenSigns.getInstance().getCabalHighestScore();
		
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			if (compWinner == SevenSigns.CABAL_DAWN)
			{
				state = 2;
			}
			else if (compWinner == SevenSigns.CABAL_DUSK)
			{
				state = 1;
			}
		}
	}
	
	public SignsSky(final int state)
	{
		this.state = state;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xf8);
		
		if (state == 2)
		{
			writeH(258);
		}
		else if (state == 1)
		{
			writeH(257);
			// else
			// writeH(256);
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] F8 SignsSky";
	}
}
