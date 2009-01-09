/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2004-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 9, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.notifd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opennms.core.utils.Argument;
import org.opennms.netmgt.config.NotificationManager;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class IrcCatNotificationStrategyTest {
    /**
     * This doesn't really do anything, but it's a placeholder so that the testSend() test can be left disabled.
     */
    @Test
    public void testInstantiate() {
        new IrcCatNotificationStrategy();
    }
    
    //@Test
    public void testSend() throws UnknownHostException {
        IrcCatNotificationStrategy strategy = new IrcCatNotificationStrategy();
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new Argument(NotificationManager.PARAM_EMAIL, null, "#opennms-test", false));
        arguments.add(new Argument(NotificationManager.PARAM_TEXT_MSG, null, "Test notification from " + getClass() + " from " + InetAddress.getLocalHost(), false));
        strategy.send(arguments);
    }
}
 