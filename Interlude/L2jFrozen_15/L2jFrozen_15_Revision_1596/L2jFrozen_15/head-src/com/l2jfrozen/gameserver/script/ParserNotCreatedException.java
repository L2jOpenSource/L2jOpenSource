package com.l2jfrozen.gameserver.script;

public class ParserNotCreatedException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public ParserNotCreatedException()
	{
		super("Parser could not be created!");
	}
}
