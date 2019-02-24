/*
 * Copyright (c) 2004-2019 Tada AB and other contributors, as listed below.
 * Copyright (c) 2010, 2011 PostgreSQL Global Development Group
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the The BSD 3-Clause License
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 *   Tada AB
 *   Chapman Flack
 */
package org.postgresql.pljava.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.pljava.internal.Backend;
import org.postgresql.pljava.internal.DualState;
import org.postgresql.pljava.internal.TupleDesc;

/**
 * A single row, read-only ResultSet, specially made for functions and
 * procedures that takes complex types as arguments (PostgreSQL 7.5
 * and later).
 *
 * @author Thomas Hallgren
 */
public class SingleRowReader extends SingleRowResultSet
{
	private final TupleDesc m_tupleDesc;
	private final State m_state;

	private static class State
	extends DualState.SingleGuardedLong<SingleRowReader>
	{
		private State(
			DualState.Key cookie, SingleRowReader srr, long ro, long hth)
		{
			super(cookie, srr, ro, hth);
		}

		/**
		 * Return the HeapTupleHeader pointer.
		 *<p>
		 * As long as this value is used in instance methods on SingleRowReader
		 * (or subclasses) and only while they hold Backend.THREADLOCK, it isn't
		 * necessary to also hold the monitor on this State object. The state
		 * can't go java-unreachable while an instance method's on the stack,
		 * and as long as we're on the thread that's in PG, the Invocation that
		 * state is scoped to can't be popped before we return.
		 */
		private long getHeapTupleHeaderPtr() throws SQLException
		{
			return getValue();
		}
	}

	/**
	 * Construct a {@code SingleRowReader} from a {@code HeapTupleHeader}
	 * and a {@link TupleDesc TupleDesc}.
	 * @param cookie Capability obtained from native code to construct a
	 * {@code SingleRowReader} instance.
	 * @param resourceOwner Value identifying a scope in PostgreSQL during which
	 * the native state encapsulated here will be valid.
	 * @param hth Native pointer to a PG {@code HeapTupleHeader}
	 * @param tupleDesc A {@code TupleDesc}; the Java class this time.
	 */
	public SingleRowReader(DualState.Key cookie, long resourceOwner, long hth,
		TupleDesc tupleDesc)
	throws SQLException
	{
		m_state = new State(cookie, this, resourceOwner, hth);
		m_tupleDesc = tupleDesc;
	}

	@Override
	public void close()
	{
	}

	@Override // defined in ObjectResultSet
	protected Object getObjectValue(int columnIndex, Class<?> type)
	throws SQLException
	{
		synchronized(Backend.THREADLOCK)
		{
			return _getObject(
				m_state.getHeapTupleHeaderPtr(), m_tupleDesc.getNativePointer(),
				columnIndex, type);
		}
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

	private static SQLException readOnlyException()
	{
		return new UnsupportedFeatureException("ResultSet is read-only");
	}

	// ************************************************************
	// Implementation of JDBC 4 methods.
	// ************************************************************

	public boolean isClosed()
		throws SQLException
	{
		return false;
	}

	// ************************************************************
	// End of implementation of JDBC 4 methods.
	// ************************************************************

	@Override // defined in SingleRowResultSet
	protected final TupleDesc getTupleDesc()
	{
		return m_tupleDesc;
	}

	private static native Object _getObject(
		long pointer, long tupleDescPointer, int index, Class<?> type)
	throws SQLException;
}
