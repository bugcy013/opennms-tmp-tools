/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.filter;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public abstract class EqualsFilter<T> extends OneArgFilter<T> {
    
    public EqualsFilter(final String filterType, final SQLType<T> type, final String fieldName, final String propertyName, final T value){
        super(filterType, type, fieldName, propertyName, value);
    }
    
    @Override
    public Criterion getCriterion() {
        return Restrictions.eq(getPropertyName(), getValue());
    }

    @Override
    public String getSQLTemplate() {
        return " " + getSQLFieldName() + " =  %s ";
    }

}
