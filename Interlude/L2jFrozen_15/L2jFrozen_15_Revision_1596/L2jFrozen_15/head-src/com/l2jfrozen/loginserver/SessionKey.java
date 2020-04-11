package com.l2jfrozen.loginserver;

import com.l2jfrozen.Config;

/**
 * <p>
 * This class is used to represent session keys used by the client to authenticate in the gameserver
 * </p>
 * <p>
 * A SessionKey is made up of two 8 bytes keys. One is send in the com.l2jfrozen.loginserver.serverpacket.LoginOk packet and the other is sent in com.l2jfrozen.loginserver.serverpacket.PlayOk
 * </p>
 * @author -Wooden-
 */
public class SessionKey
{
	public int playOkID1;
	public int playOkID2;
	public int loginOkID1;
	public int loginOkID2;
	
	public SessionKey(final int loginOK1, final int loginOK2, final int playOK1, final int playOK2)
	{
		playOkID1 = playOK1;
		playOkID2 = playOK2;
		loginOkID1 = loginOK1;
		loginOkID2 = loginOK2;
	}
	
	@Override
	public String toString()
	{
		return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " " + loginOkID2;
	}
	
	public boolean checkLoginPair(final int loginOk1, final int loginOk2)
	{
		return loginOkID1 == loginOk1 && loginOkID2 == loginOk2;
	}
	
	/**
	 * <p>
	 * Returns true if keys are equal.
	 * </p>
	 * <p>
	 * Only checks the PlayOk part of the session key if server doesn't show the license when player logs in.
	 * </p>
	 * @param  key
	 * @return
	 */
	public boolean equals(final SessionKey key)
	{
		// when server doesn't show license it doesn't send the LoginOk packet, client doesn't have this part of the key then.
		if (Config.SHOW_LICENCE)
		{
			return playOkID1 == key.playOkID1 && loginOkID1 == key.loginOkID1 && playOkID2 == key.playOkID2 && loginOkID2 == key.loginOkID2;
		}
		return playOkID1 == key.playOkID1 && playOkID2 == key.playOkID2;
	}
}
