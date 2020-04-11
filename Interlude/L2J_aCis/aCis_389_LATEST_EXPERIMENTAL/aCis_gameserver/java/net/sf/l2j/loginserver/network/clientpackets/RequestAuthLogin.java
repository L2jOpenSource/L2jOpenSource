package net.sf.l2j.loginserver.network.clientpackets;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import net.sf.l2j.loginserver.LoginController;
import net.sf.l2j.loginserver.network.LoginClient;
import net.sf.l2j.loginserver.network.serverpackets.LoginFail;

public class RequestAuthLogin extends L2LoginClientPacket
{
	private final byte[] _raw = new byte[128];
	
	@Override
	public boolean readImpl()
	{
		if (super._buf.remaining() >= 128)
		{
			readB(_raw);
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		final LoginClient client = getClient();
		
		byte[] decrypted = null;
		try
		{
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, client.getRSAPrivateKey());
			decrypted = rsaCipher.doFinal(_raw, 0x00, 0x80);
		}
		catch (GeneralSecurityException e)
		{
			LOGGER.error("Failed to generate a cipher.", e);
			client.close(LoginFail.REASON_ACCESS_FAILED);
			return;
		}
		
		try
		{
			final String user = new String(decrypted, 0x5E, 14).trim().toLowerCase();
			final String password = new String(decrypted, 0x6C, 16).trim();
			
			LoginController.getInstance().retrieveAccountInfo(client, user, password);
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to decrypt user/password.", e);
			client.close(LoginFail.REASON_ACCESS_FAILED);
		}
	}
}