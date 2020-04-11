package net.sf.l2j.commons.mmocore;

public interface IClientFactory<T extends MMOClient<?>>
{
	public T create(final MMOConnection<T> con);
}