package l2r.gameserver.model.entity.olympiad.enums;

/**
 * @author vGodFather
 */
public enum CompetitionType
{
	CLASSED("classed"),
	NON_CLASSED("non-classed"),
	TEAMS("teams"),
	OTHER("other");
	
	private final String _name;
	
	private CompetitionType(String name)
	{
		_name = name;
	}
	
	@Override
	public final String toString()
	{
		return _name;
	}
}