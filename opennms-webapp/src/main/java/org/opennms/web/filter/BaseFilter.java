/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.web.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.opennms.netmgt.model.OnmsCriteria;

/**
 * BaseFilter
 *
 * @author brozow
 */
public abstract class BaseFilter<T> implements Filter {
    
    protected String m_filterName;
    protected SQLType<T> m_sqlType;
    private String m_fieldName;
    private String[] m_propertyPath;
    private String m_propertyName;
    
    public BaseFilter(String filterType, SQLType<T> sqlType, String fieldName, String propertyName) {
        m_filterName = filterType;
        m_sqlType = sqlType;
        m_fieldName = fieldName;
        
        String[] propPath = propertyName.split("\\.");
        
        m_propertyPath = new String[propPath.length-1];
        for(int i = 0; i < propPath.length-1; i++) {
            m_propertyPath[i] = propPath[i];
        }
                
        m_propertyName = propPath[propPath.length-1];

    }


    public String getSQLFieldName() {
        return m_fieldName;
    }
    
    public String[] getPropertyPath() {
        return m_propertyPath;
    }

    public String getPropertyName() {
        return m_propertyName;
    }

    public final String getDescription() {
        return m_filterName+"="+getValueString();
    }
    
    final public void bindValue(PreparedStatement ps, int parameterIndex, T value) throws SQLException {
        m_sqlType.bindParam(ps, parameterIndex, value);
    }
    
    public String formatValue(T value) {
        return m_sqlType.formatValue(value);
    }
    
    final public String getValueAsString(T value) {
        return m_sqlType.getValueAsString(value);
    }
    
    public abstract String getValueString();
    
    public abstract void applyCriteria(OnmsCriteria criteria);

    public abstract int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException;

    public abstract String getParamSql();

    public abstract String getSql();

    public abstract String getTextDescription();


    public OnmsCriteria createAssociationCriteria(OnmsCriteria criteria) {
        String[] propertyPath = getPropertyPath();
        OnmsCriteria resultCriteria = criteria;
        for(String pathElement : propertyPath) {
            resultCriteria = resultCriteria.createCriteria(pathElement);
        }
        return resultCriteria;
    }

    public OnmsCriteria createAssociationCriteria(OnmsCriteria criteria, int joinType) {
        String[] propertyPath = getPropertyPath();
        OnmsCriteria resultCriteria = criteria;
        for(String pathElement : propertyPath) {
            resultCriteria = resultCriteria.createCriteria(pathElement, joinType);
        }
        return resultCriteria;
    }

}