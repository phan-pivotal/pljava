/*
 * Copyright (c) 2003, 2004 TADA AB - Taby Sweden
 * Distributed under the terms shown in the file COPYRIGHT.
 */
package org.postgresql.pljava.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.pljava.internal.Tuple;
import org.postgresql.pljava.internal.TupleDesc;
import org.postgresql.pljava.internal.TupleTableSlot;

/**
 * A single row, read-only ResultSet, specially made for functions and
 * procedures that takes complex types as arguments.
 *
 * @author Thomas Hallgren
 */
public class SingleRowReader extends SingleRowResultSet
{
	private final TupleDesc m_tupleDesc;
	private final Tuple m_tuple;

	public SingleRowReader(TupleTableSlot tableSlot)
	throws SQLException
	{
		m_tupleDesc = tableSlot.getTupleDesc();
		m_tuple = tableSlot.getTuple();
	}

	protected Object getObjectValue(int columnIndex)
	throws SQLException
	{
		return m_tuple.getObject(this.getTupleDesc(), columnIndex);
	}

	/**
	 * Returns {@link ResultSet#CONCUR_READ_ONLY}.
	 */
	public int getConcurrency()
	throws SQLException
	{
		return ResultSet.CONCUR_READ_ONLY;
	}

	/**
	 * This feature is not supported on a <code>ReadOnlyResultSet</code>.
	 * @throws SQLException indicating that this feature is not supported.
	 */
	public void cancelRowUpdates()
	throws SQLException
	{
		throw readOnlyException();
	}

	/**
	 * This feature is not supported on a <code>ReadOnlyResultSet</code>.
	 * @throws SQLException indicating that this feature is not supported.
	 */
	public void deleteRow()
	throws SQLException
	{
		throw readOnlyException();
	}

	/**
	 * This feature is not supported on a <code>ReadOnlyResultSet</code>.
	 * @throws SQLException indicating that this feature is not supported.
	 */
	public void insertRow()
	throws SQLException
	{
		throw readOnlyException();
	}

	/**
	 * This feature is not supported on a <code>ReadOnlyResultSet</code>.
	 * @throws SQLException indicating that this feature is not supported.
	 */
	public void moveToInsertRow()
	throws SQLException
	{
		throw readOnlyException();
	}

	/**
	 * This feature is not supported on a <code>ReadOnlyResultSet</code>.
	 * @throws SQLException indicating that this feature is not supported.
	 */
	public void updateRow()
	throws SQLException
	{
		throw readOnlyException();
	}

	/**
	 * Always returns false.
	 */
	public boolean rowUpdated()
	throws SQLException
	{
		return false;
	}

	/**
	 * This feature is not supported on a <code>ReadOnlyResultSet</code>.
	 * @throws SQLException indicating that this feature is not supported.
	 */
	public void updateObject(int columnIndex, Object x) throws SQLException
	{
		throw readOnlyException();
	}

	/**
	 * This feature is not supported on a <code>ReadOnlyResultSet</code>.
	 * @throws SQLException indicating that this feature is not supported.
	 */
	public void updateObject(int columnIndex, Object x, int scale)
	throws SQLException
	{
		throw readOnlyException();
	}

	protected final TupleDesc getTupleDesc()
	{
		return m_tupleDesc;
	}

	private static SQLException readOnlyException()
	{
		return new UnsupportedFeatureException("ResultSet is read-only");
	}
}