package handlers;

import gr.sr.handler.ABLoader;

import ai.npc.VillageMasters.Alliance.Alliance;
import ai.npc.VillageMasters.Clan.Clan;
import ai.npc.VillageMasters.DarkElvenChange1.DarkElvenChange1;
import ai.npc.VillageMasters.DarkElvenChange2.DarkElvenChange2;
import ai.npc.VillageMasters.DwarvenOccupationChange.DwarvenOccupationChange;
import ai.npc.VillageMasters.ElvenHumanBuffers2.ElvenHumanBuffers2;
import ai.npc.VillageMasters.ElvenHumanFighters1.ElvenHumanFighters1;
import ai.npc.VillageMasters.ElvenHumanFighters2.ElvenHumanFighters2;
import ai.npc.VillageMasters.ElvenHumanMystics1.ElvenHumanMystics1;
import ai.npc.VillageMasters.ElvenHumanMystics2.ElvenHumanMystics2;
import ai.npc.VillageMasters.FirstClassTransferTalk.FirstClassTransferTalk;
import ai.npc.VillageMasters.KamaelChange1.KamaelChange1;
import ai.npc.VillageMasters.KamaelChange2.KamaelChange2;
import ai.npc.VillageMasters.OrcOccupationChange1.OrcOccupationChange1;
import ai.npc.VillageMasters.OrcOccupationChange2.OrcOccupationChange2;
import ai.npc.VillageMasters.SubclassCertification.SubclassCertification;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class VillageMastersLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		Alliance.class,
		Clan.class,
		DarkElvenChange1.class,
		DarkElvenChange2.class,
		DwarvenOccupationChange.class,
		ElvenHumanBuffers2.class,
		ElvenHumanFighters1.class,
		ElvenHumanFighters2.class,
		ElvenHumanMystics1.class,
		ElvenHumanMystics2.class,
		FirstClassTransferTalk.class,
		KamaelChange1.class,
		KamaelChange2.class,
		OrcOccupationChange1.class,
		OrcOccupationChange2.class,
		SubclassCertification.class,
	};
	
	public VillageMastersLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
