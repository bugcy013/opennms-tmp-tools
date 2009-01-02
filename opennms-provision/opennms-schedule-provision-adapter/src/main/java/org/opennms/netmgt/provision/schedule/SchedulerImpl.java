/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
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
package org.opennms.netmgt.provision.schedule;

import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * @author thedesloge
 *
 */
public class SchedulerImpl implements Scheduler {
    
    private final PeriodFormatter m_fmt;
    private String m_schedule;
    
    public SchedulerImpl(String schedule) {
        m_schedule = schedule;
        
        m_fmt = new PeriodFormatterBuilder()
        .appendYears().appendSuffix("y").appendSeparator(" ")
        .appendMonths().appendSuffix("M").appendSeparator(" ")
        .appendDays().appendSuffix("d").appendSeparator(" ")
        .appendHours().appendSuffix("h").appendSeparator(" ")
        .appendMinutes().appendSuffix("m").appendSeparator(" ")
        .appendSeconds().appendSuffix("s").appendSeparator(" ")
        .appendMillis().appendSuffix("ms")
        .toFormatter();
    }
    
    public Long getSchedule() {
        return m_fmt.parsePeriod(m_schedule).normalizedStandard().toStandardDuration().getMillis();
    }

    public void setSchedule(String schedule) {
        m_schedule = schedule;
    }

}