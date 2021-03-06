/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import java.util.Date;
import java.util.TimeZone;

import org.opennms.core.utils.TimeKeeper;

/**
 * The Class ConstantTimeKeeper.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class ConstantTimeKeeper implements TimeKeeper {

    /** The Fixed Date. */
    private Date m_date;
    
    /**
     * Instantiates a new constant time keeper.
     *
     * @param date the date
     */
    public ConstantTimeKeeper(final Date date) {
        m_date = date;
    }
    
    @Override
    public long getCurrentTime() {
        return m_date.getTime();
    }

    @Override
    public Date getCurrentDate() {
        return m_date;
    }

    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }
  
}
