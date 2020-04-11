package com.l2jfrozen.gameserver.network.serverpackets;

/**
 */
public class PetDelete extends L2GameServerPacket
{
	private final int petId;
	private final int petObjId;
	
	public PetDelete(final int petId, final int petObjId)
	{
		this.petId = petId; // summonType?
		this.petObjId = petObjId; // objectId
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb6);
		writeD(petId);// dont really know what these two are since i never needed them
		writeD(petObjId);// objectId
	}
	
	@Override
	public String getType()
	{
		return "[S] b6 PetDelete";
	}
}
