package com.shomop.crm.model.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

public class BigIntTimeType implements UserType {

    @Override
    public int[] sqlTypes() {
        return new int[] { Types.BIGINT };
    }

    @Override
    public Class<Date> returnedClass() {
        return Date.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return ObjectUtils.equals(x, y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names,
            SessionImplementor session, Object owner)
            throws HibernateException, SQLException {
        final long time = rs.getLong( names[0] );
        if ( rs.wasNull() ) {
            return null;
        }
        final Date date = new Date(time);
        return date;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index,
            SessionImplementor session) throws HibernateException, SQLException {
        final Object jdbcValue = value == null ? null : ((Date) value).getTime();
        if ( jdbcValue == null ) {
            st.setNull( index, Types.BIGINT );
            return;
        }
        st.setObject( index, jdbcValue, Types.BIGINT );
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value == null ? null : ((Date) value).clone();
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) deepCopy(value);
    }

    @Override
    public Object assemble(Serializable cached, Object owner)
            throws HibernateException {
        return deepCopy(cached);
    }

    @Override
    public Object replace(Object original, Object target, Object owner)
            throws HibernateException {
        return deepCopy(original);
    }

}
