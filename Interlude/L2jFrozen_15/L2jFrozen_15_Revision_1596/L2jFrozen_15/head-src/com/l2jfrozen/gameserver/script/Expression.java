package com.l2jfrozen.gameserver.script;

import javax.script.ScriptContext;

import com.l2jfrozen.gameserver.scripting.L2ScriptEngineManager;

public class Expression
{
	private final ScriptContext context;
	
	public static Object eval(final String lang, final String code)
	{
		try
		{
			return L2ScriptEngineManager.getInstance().eval(lang, code);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static Object eval(final ScriptContext context, final String lang, final String code)
	{
		try
		{
			return L2ScriptEngineManager.getInstance().eval(lang, code, context);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static Expression create(final ScriptContext context/* , String lang, String code */)
	{
		try
		{
			return new Expression(context/* , lang, code */);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private Expression(final ScriptContext pContext/* , String pLang, String pCode */)
	{
		context = pContext;
	}
	
	public <T> void addDynamicVariable(final String name, final T value)
	{
		try
		{
			context.setAttribute(name, value, ScriptContext.ENGINE_SCOPE);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void removeDynamicVariable(final String name)
	{
		try
		{
			context.removeAttribute(name, ScriptContext.ENGINE_SCOPE);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
