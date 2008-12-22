//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jan 27: Implement EventProcessor interface. - dj@opennms.org
// 2008 Jan 06: Make static methods non-static, allow us to dependency
//              inject EventConfDao and use that instead of EventConfigurationManager.
//              - dj@opennms.org
// 2007 Aug 03: New Castor methods names: clearX -> removeAllX. - dj@opennms.org
// 2003 Aug 21: Changes made to support ScriptD.
// 2002 Nov 10: Removed "http://" from UEIs as well as references to bluebird.
// 2002 Oct 22: Added threshold rearm events.
// 2002 Oct 21: Default SNMP events no longer hard coded, and event order now functional.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.eventd.processor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Autoaction;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Header;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Operaction;
import org.opennms.netmgt.xml.event.Tticket;
import org.opennms.netmgt.xml.eventconf.Decode;
import org.opennms.netmgt.xml.eventconf.Varbindsdecode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <P>
 * This class is responsible for looking up the matching evenconf entry for an
 * event and loading info from the eventconf match to the event. This class is
 * also responsible for the event parm expansion
 * </P>
 * 
 * <P>
 * Notes on event parm expansion:
 * </P>
 * <P>
 * The list of elements that can have a %element% or %parms[*]% in their value
 * are : descr, logmsg, operinstr, autoaction, operaction(/menu), tticket
 * </P>
 * 
 * <P>
 * The list of elements that can occur as a %element% are : uei, source, nodeid,
 * time, host, interface, snmphost, service, snmp, id, idtext, version,
 * specific, generic, community, severity, operinstr, mouseovertext,
 * parm[values-all], parm[names-all], parm[all], parm[ <name>], parm[##], parm[#
 * <num>]
 * </P>
 * 
 * <pre>
 * 
 *  Expansions are made so that 
 *  - %element% is replaced by the value of the element
 *    -i.e a 'xxx %uei%'  would expand to 'xxx &lt;eventuei&gt;'
 *  - %parm[values-all]% is replaced by a space-separated list of all parm values
 *    -i.e a 'xxx %parm[values-all]%'  would expand to 'xxx parmVal1 parmVal2 ..'
 *  - %parm[names-all]% is replaced by a space-separated list of all parm names 
 *    -i.e a 'xxx %parm[names-all]%'  would expand to 'xxx parmName1 parmName2 ..'
 *  - %parm[all]% is replaced by a space-separated list of all parmName=&quot;parmValue&quot;
 *     -i.e a 'xxx %parm[all]%'  would expand to 'xxx parmName1=&quot;parmVal1&quot; parmName2=&quot;parmVal2&quot; ..'
 *  - %parm[&lt;name&gt;]% is replaced by the value of the parameter named 'name', if present
 *  - %parm[#&lt;num&gt;]% is replaced by the value of the parameter number 'num', if present
 *  - %parm[##]% is replaced by the number of parameters 
 *  
 * </pre>
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class EventExpander implements EventProcessor, InitializingBean {
    private EventConfDao m_eventConfDao;
    
    /**
     * The enterprise ID prefix - incoming events and the events in event.conf
     * can have EIDs that have the partial EIDs as in '18.1.1.6' instead of
     * '.1.3.6.1.4.1.18.1.1.6'. When a event lookup is done based on the EID, a
     * lookup with both the partial and the full EID is done
     */
/*
 * This is never used
 * TODO: delete this code
    private final static String ENTERPRISE_PRE = ".1.3.6.1.4.1";
*/
    /**
     * The default event UEI - if the event lookup into the 'event.conf' fails,
     * the event is loaded with information from this default UEI
     */
    private final static String DEFAULT_EVENT_UEI = "uei.opennms.org/default/event";

    /**
     * The default trap UEI - if the trap lookup into the 'event.conf' fails,
     * the trap event is loaded with information from this default UEI
     */
/*
 * This is never used
 * TODO: delete this code soon
    private final static String DEFAULT_TRAP_UEI = "uei.opennms.org/default/trap";
*/

    public EventExpander() {
    }

    public void afterPropertiesSet() {
        Assert.state(m_eventConfDao != null, "property eventConfDao must be set");
    }

    /**
     * This method is used to transform an event configuration mask instance
     * into an event mask instance. This is used when the incomming event does
     * not have a mask and the information from the configuration object is
     * copied.
     * 
     * @param src
     *            The configuration source to transform.
     * 
     * @return The transformed mask information.
     * 
     */
    private org.opennms.netmgt.xml.event.Mask transform(org.opennms.netmgt.xml.eventconf.Mask src) {
        org.opennms.netmgt.xml.event.Mask dest = new org.opennms.netmgt.xml.event.Mask();

        Enumeration en = src.enumerateMaskelement();
        while (en.hasMoreElements()) {
            org.opennms.netmgt.xml.eventconf.Maskelement confme = (org.opennms.netmgt.xml.eventconf.Maskelement) en.nextElement();

            // create new mask element
            org.opennms.netmgt.xml.event.Maskelement me = new org.opennms.netmgt.xml.event.Maskelement();
            // set name
            me.setMename(confme.getMename());
            // set values
            String[] confmevalues = confme.getMevalue();
            for (int index = 0; index < confmevalues.length; index++) {
                me.addMevalue(confmevalues[index]);
            }

            dest.addMaskelement(me);
        }

        return dest;
    }

    /**
     * This method is used to transform an SNMP event configuration instance
     * into an SNMP event instance. This is used when the incomming event does
     * not have any SNMP information and the information from the configuration
     * object is copied.
     * 
     * @param src
     *            The configuration source to transform.
     * 
     * @return The transformed SNMP information.
     * 
     */
    private org.opennms.netmgt.xml.event.Snmp transform(org.opennms.netmgt.xml.eventconf.Snmp src) {
        org.opennms.netmgt.xml.event.Snmp dest = new org.opennms.netmgt.xml.event.Snmp();

        dest.setId(src.getId());
        dest.setIdtext(src.getIdtext());
        dest.setVersion(src.getVersion());
        dest.setCommunity(src.getCommunity());

        if (src.hasGeneric())
            dest.setGeneric(src.getGeneric());
        if (src.hasSpecific())
            dest.setSpecific(src.getSpecific());

        return dest;
    }

    /**
     * This method is used to transform a log message event configuration
     * instance into a log message event instance. This is used when the
     * incomming event does not have any log message information and the
     * information from the configuration object is copied.
     * 
     * @param src
     *            The configuration source to transform.
     * 
     * @return The transformed log message information.
     * 
     */
    private org.opennms.netmgt.xml.event.Logmsg transform(org.opennms.netmgt.xml.eventconf.Logmsg src) {
        org.opennms.netmgt.xml.event.Logmsg dest = new org.opennms.netmgt.xml.event.Logmsg();

        dest.setContent(src.getContent());
        dest.setDest(src.getDest());
        dest.setNotify(src.getNotify());

        return dest;
    }

    /**
     * This method is used to transform a correlation event configuration
     * instance into a correlation event instance. This is used when the
     * incomming event does not have any correlation information and the
     * information from the configuration object is copied.
     * 
     * @param src
     *            The configuration source to transform.
     * 
     * @return The transformed correlation information.
     * 
     */
    private org.opennms.netmgt.xml.event.Correlation transform(org.opennms.netmgt.xml.eventconf.Correlation src) {
        org.opennms.netmgt.xml.event.Correlation dest = new org.opennms.netmgt.xml.event.Correlation();

        dest.setCuei(src.getCuei());
        dest.setCmin(src.getCmin());
        dest.setCmax(src.getCmax());
        dest.setCtime(src.getCtime());
        dest.setState(src.getState());
        dest.setPath(src.getPath());

        return dest;
    }

    /**
     * This method is used to transform an auto action event configuration
     * instance into an auto action event instance. This is used when the
     * incomming event does not have any auto action information and the
     * information from the configuration object is copied.
     * 
     * @param src
     *            The configuration source to transform.
     * 
     * @return The transformed auto action information.
     * 
     */
    private org.opennms.netmgt.xml.event.Autoaction transform(org.opennms.netmgt.xml.eventconf.Autoaction src) {
        org.opennms.netmgt.xml.event.Autoaction dest = new org.opennms.netmgt.xml.event.Autoaction();

        dest.setContent(src.getContent());
        dest.setState(src.getState());

        return dest;
    }

    /**
     * This method is used to transform an operator action event configuration
     * instance into an operator action event instance. This is used when the
     * incomming event does not have any operator action information and the
     * information from the configuration object is copied.
     * 
     * @param src
     *            The configuration source to transform.
     * 
     * @return The transformed operator action information.
     * 
     */
    private org.opennms.netmgt.xml.event.Operaction transform(org.opennms.netmgt.xml.eventconf.Operaction src) {
        org.opennms.netmgt.xml.event.Operaction dest = new org.opennms.netmgt.xml.event.Operaction();

        dest.setContent(src.getContent());
        dest.setState(src.getState());
        dest.setMenutext(src.getMenutext());

        return dest;
    }

    /**
     * This method is used to transform an auto acknowledgement event
     * configuration instance into an auto acknowledgement event instance. This
     * is used when the incomming event does not have any auto acknowledgement
     * information and the information from the configuration object is copied.
     * 
     * @param src
     *            The configuration source to transform.
     * 
     * @return The transformed auto acknowledgement information.
     * 
     */
    private org.opennms.netmgt.xml.event.Autoacknowledge transform(org.opennms.netmgt.xml.eventconf.Autoacknowledge src) {
        org.opennms.netmgt.xml.event.Autoacknowledge dest = new org.opennms.netmgt.xml.event.Autoacknowledge();

        dest.setContent(src.getContent());
        dest.setState(src.getState());

        return dest;
    }

    /**
     * This method is used to transform a trouble ticket event configuration
     * instance into a trouble ticket event instance. This is used when the
     * incomming event does not have any trouble ticket information and the
     * information from the configuration object is copied.
     * 
     * @param src
     *            The configuration source to transform.
     * 
     * @return The transformed trouble ticket information.
     * 
     */
    private org.opennms.netmgt.xml.event.Tticket transform(org.opennms.netmgt.xml.eventconf.Tticket src) {
        org.opennms.netmgt.xml.event.Tticket dest = new org.opennms.netmgt.xml.event.Tticket();

        dest.setContent(src.getContent());
        dest.setState(src.getState());

        return dest;
    }

    /**
     * This method is used to transform a forward event configuration instance
     * into a forward event instance. This is used when the incomming event does
     * not have any forward information and the information from the
     * configuration object is copied.
     * 
     * @param src
     *            The configuration source to transform.
     * 
     * @return The transformed forward information.
     * 
     */
    private org.opennms.netmgt.xml.event.Forward transform(org.opennms.netmgt.xml.eventconf.Forward src) {
        org.opennms.netmgt.xml.event.Forward dest = new org.opennms.netmgt.xml.event.Forward();

        dest.setContent(src.getContent());
        dest.setState(src.getState());
        dest.setMechanism(src.getMechanism());

        return dest;
    }

    /**
     * This method is used to transform a script event configuration instance
     * into a script event instance. This is used when the incoming event does
     * not have any script information and the information from the
     * configuration object is copied.
     * 
     * @param src
     *            The configuration source to transform.
     * 
     * @return The transformed script information.
     * 
     */
    private org.opennms.netmgt.xml.event.Script transform(org.opennms.netmgt.xml.eventconf.Script src) {
        org.opennms.netmgt.xml.event.Script dest = new org.opennms.netmgt.xml.event.Script();

        dest.setContent(src.getContent());
        dest.setLanguage(src.getLanguage());

        return dest;
    }

    /**
     * <p>
     * This method is used to lookup the event configuration object based upon
     * information in the passed information. The
     * {@link EventConfDao EventConfDao} instance is
     * consulted to find a matching configured event. The lookup algorithm
     * favors SNMP information if avaialable, and then defaults to the event's
     * Universal Event Identfier.
     * </p>
     * 
     * @param event
     *            The event to find a configuration for.
     * 
     * @return The matching configuration event, if any.
     * 
     * @exception java.lang.NullPointerException
     *                Thrown if the event parameter that was passed is null.
     * 
     */
    private org.opennms.netmgt.xml.eventconf.Event lookup(Event event) {
        if (event == null)
            throw new NullPointerException("Invalid argument, the event parameter must not be null");

        //
        // The event configuration that matches the lookup
        // for the passed event
        //
        org.opennms.netmgt.xml.eventconf.Event eConf = null;

        //
        // lookup based on the event mask, (defaults to UEI
        // if there is no mask specified)
        //
        eConf = m_eventConfDao.findByEvent(event);

        if (eConf == null) {
            //
            // take the configuration of the default event
            //
            eConf = m_eventConfDao.findByUei(DEFAULT_EVENT_UEI);
        }

        return eConf;
    }

    /**
     * Expand parms in the event logmsg
     */
    private void expandParms(Logmsg logmsg, Event event, Map<String, Map<String, String>> decode) {
        String strRet = EventUtil.expandParms(logmsg.getContent(), event, decode);
        if (strRet != null) {
            logmsg.setContent(strRet);
        }
    }

    /**
     * Expand parms in the event autoaction(s)
     */
    private void expandParms(Autoaction[] autoactions, Event event) {
        boolean expanded = false;

        for (int index = 0; index < autoactions.length; index++) {
            Autoaction action = autoactions[index];

            String strRet = EventUtil.expandParms(action.getContent(), event);
            if (strRet != null) {
                action.setContent(strRet);
                expanded = true;
            }
        }

        if (expanded)
            event.setAutoaction(autoactions);
    }

    /**
     * Expand parms in the event operaction(s)
     */
    private void expandParms(Operaction[] operactions, Event event) {
        boolean expanded = false;

        for (int index = 0; index < operactions.length; index++) {
            Operaction action = operactions[index];

            String strRet = EventUtil.expandParms(action.getContent(), event);
            if (strRet != null) {
                action.setContent(strRet);
                expanded = true;
            }
        }

        if (expanded)
            event.setOperaction(operactions);
    }

    /**
     * Expand parms in the event tticket
     */
    private void expandParms(Tticket tticket, Event event) {
        String strRet = EventUtil.expandParms(tticket.getContent(), event);
        if (strRet != null) {
            tticket.setContent(strRet);
        }
    }

    /**
     * Expand the element values if they have parms in one of the following
     * formats
     *  - %element% values are expanded to have the value of the element where
     * 'element' is an element in the event DTD - %parm[values-all]% is expanded
     * to a delimited list of all parmblock values - %parm[names-all]% is
     * expanded to a list of all parm names - %parm[all]% is expanded to a full
     * dump of all parmblocks - %parm[ <name>]% is replaced by the value of the
     * parameter named 'name', if present - %parm[# <num>]% is replaced by the
     * value of the parameter number 'num', if present - %parm[##]% is replaced
     * by the number of parameters
     */
    private void expandParms(Event event, Map<String, Map<String, String>> decode) {
        String strRet = null;

        // description
        if (event.getDescr() != null) {
            strRet = EventUtil.expandParms(event.getDescr(), event);
            if (strRet != null) {
                event.setDescr(strRet);
                strRet = null;
            }
        }

        // logmsg
        if (event.getLogmsg() != null) {
            expandParms(event.getLogmsg(), event, decode);
        }

        // operinstr
        if (event.getOperinstruct() != null) {
            strRet = EventUtil.expandParms(event.getOperinstruct(), event);
            if (strRet != null) {
                event.setOperinstruct(strRet);
                strRet = null;
            }
        }

        // autoaction
        if (event.getAutoaction() != null) {
            expandParms(event.getAutoaction(), event);
        }

        // operaction
        if (event.getOperaction() != null) {
            expandParms(event.getOperaction(), event);
        }

        // tticket
        if (event.getTticket() != null) {
            expandParms(event.getTticket(), event);
        }
        
        // reductionKey
        if (event.getAlarmData() != null) {
            strRet = EventUtil.expandParms(event.getAlarmData().getReductionKey(), event);
            if (strRet != null) {
                event.getAlarmData().setReductionKey(strRet);
            }
            strRet = null;
            strRet = EventUtil.expandParms(event.getAlarmData().getClearKey(), event);
            if (strRet != null) {
            	event.getAlarmData().setClearKey(strRet);
            }
        }

    }

    /**
     * <p>
     * This method is invoked to check and configure a received event. The event
     * configuration manager is consulted to find the appropriate configuration
     * that is used to expand the event. In addition, the security parameters
     * from the configuration manager is consulted to ensure that secure files
     * are cleared out if necessary.
     * </p>
     * 
     * <p>
     * Any secure fields that exists in the incomming event are cleared during
     * expansion.
     * </p>
     * 
     * @param e
     *            The event to expand if necesary.
     * 
     */
    public synchronized void expandEvent(Event e) {
        org.opennms.netmgt.xml.eventconf.Event econf = lookup(e);

        if (econf != null) {
            if (m_eventConfDao.isSecureTag("mask"))
                e.setMask(null);
            if (e.getMask() == null && econf.getMask() != null) {
                e.setMask(transform(econf.getMask()));
            }

            // Copy the UEI
            //
            if (e.getUei() == null)
                e.setUei(econf.getUei());

            // Copy the Snmp Information
            //
            if (e.getSnmp() == null && econf.getSnmp() != null)
                e.setSnmp(transform(econf.getSnmp()));

            // Copy the description
            //
            if (m_eventConfDao.isSecureTag("descr"))
                e.setDescr(null);
            if (e.getDescr() == null && econf.getDescr() != null)
                e.setDescr(econf.getDescr());

            // Copy the log message if any
            //
            if (m_eventConfDao.isSecureTag("logmsg"))
                e.setLogmsg(null);
            if (e.getLogmsg() == null && econf.getLogmsg() != null)
                e.setLogmsg(transform(econf.getLogmsg()));

            // Copy the severity
            //
            if (m_eventConfDao.isSecureTag("severity"))
                e.setSeverity(null);
            if (e.getSeverity() == null && econf.getSeverity() != null)
                e.setSeverity(econf.getSeverity());

            // Set the correlation information
            //
            if (m_eventConfDao.isSecureTag("correlation"))
                e.setCorrelation(null);
            if (e.getCorrelation() == null && econf.getCorrelation() != null)
                e.setCorrelation(transform(econf.getCorrelation()));

            // Copy the operator instruction
            //
            if (m_eventConfDao.isSecureTag("operinstruct"))
                e.setOperinstruct(null);
            if (e.getOperinstruct() == null && econf.getOperinstruct() != null)
                e.setOperinstruct(econf.getOperinstruct());

            // Copy the auto actions.
            //
            if (m_eventConfDao.isSecureTag("autoaction"))
                e.removeAllAutoaction();
            if (e.getAutoactionCount() == 0 && econf.getAutoactionCount() > 0) {
                Enumeration eter = econf.enumerateAutoaction();
                while (eter.hasMoreElements()) {
                    org.opennms.netmgt.xml.eventconf.Autoaction src = (org.opennms.netmgt.xml.eventconf.Autoaction) eter.nextElement();
                    e.addAutoaction(transform(src));
                }
            }

            // Convert the operator actions
            //
            if (m_eventConfDao.isSecureTag("operaction"))
                e.removeAllOperaction();
            if (e.getOperactionCount() == 0 && econf.getOperactionCount() > 0) {
                Enumeration eter = econf.enumerateOperaction();
                while (eter.hasMoreElements()) {
                    org.opennms.netmgt.xml.eventconf.Operaction src = (org.opennms.netmgt.xml.eventconf.Operaction) eter.nextElement();
                    e.addOperaction(transform(src));
                }
            }

            // Convert the auto acknowledgement
            //
            if (m_eventConfDao.isSecureTag("autoacknowledge"))
                e.setAutoacknowledge(null);
            if (e.getAutoacknowledge() == null && econf.getAutoacknowledge() != null)
                e.setAutoacknowledge(transform(econf.getAutoacknowledge()));

            // Convert the log group information
            //
            if (m_eventConfDao.isSecureTag("loggroup"))
                e.removeAllLoggroup();
            if (e.getLoggroupCount() == 0 && econf.getLoggroupCount() > 0)
                e.setLoggroup(econf.getLoggroup());

            // Convert the trouble tickets.
            //
            if (m_eventConfDao.isSecureTag("tticket"))
                e.setTticket(null);
            if (e.getTticket() == null && econf.getTticket() != null)
                e.setTticket(transform(econf.getTticket()));

            // Convert the forward entry
            //
            if (m_eventConfDao.isSecureTag("forward"))
                e.removeAllForward();
            if (e.getForwardCount() == 0 && econf.getForwardCount() > 0) {
                Enumeration eter = econf.enumerateForward();
                while (eter.hasMoreElements()) {
                    org.opennms.netmgt.xml.eventconf.Forward src = (org.opennms.netmgt.xml.eventconf.Forward) eter.nextElement();
                    e.addForward(transform(src));
                }
            }

            // Convert the script entry
            //
            if (m_eventConfDao.isSecureTag("script"))
                e.removeAllScript();
            if (e.getScriptCount() == 0 && econf.getScriptCount() > 0) {
                Enumeration eter = econf.enumerateScript();
                while (eter.hasMoreElements()) {
                    org.opennms.netmgt.xml.eventconf.Script src = (org.opennms.netmgt.xml.eventconf.Script) eter.nextElement();
                    e.addScript(transform(src));
                }
            }

            // Copy the mouse over text
            //
            if (m_eventConfDao.isSecureTag("mouseovertext"))
                e.setMouseovertext(null);
            if (e.getMouseovertext() == null && econf.getMouseovertext() != null)
                e.setMouseovertext(econf.getMouseovertext());

            // Copy the reductionKey
            // TODO:  This isSecureTag call needs some research.  "You keep using that word.  I don't think it means what you think it means."
            //        It looks to me like there should be an else here.  I could be wrong.
            if (m_eventConfDao.isSecureTag("reduction-key") && m_eventConfDao.isSecureTag("alarm-type") && m_eventConfDao.isSecureTag("clear-uei"))
                e.setAlarmData(null);
            
            if (e.getAlarmData() == null && econf.getAlarmData() != null) {
                AlarmData alarmData = new AlarmData();
                alarmData.setAlarmType(econf.getAlarmData().getAlarmType());
                alarmData.setReductionKey(econf.getAlarmData().getReductionKey());
                alarmData.setClearUei(econf.getAlarmData().getClearUei());
                alarmData.setAutoClean(econf.getAlarmData().getAutoClean());
                alarmData.setX733AlarmType(econf.getAlarmData().getX733AlarmType());
                alarmData.setX733ProbableCause(econf.getAlarmData().getX733ProbableCause());
                alarmData.setClearKey(econf.getAlarmData().getClearKey());
                e.setAlarmData(alarmData);
            }

        } 
        
        Map<String, Map<String, String>> decode = new HashMap<String, Map<String,String>>();
        if (econf.getVarbindsdecode() != null) {
           Varbindsdecode[] vardecodeArray = econf.getVarbindsdecode();
           for (int i=0; i<vardecodeArray.length; i++) {
               Decode[] decodeArray = vardecodeArray[i].getDecode();
               Map<String, String> valueMap = new HashMap<String, String>();
               for (int j=0; j<decodeArray.length;j++) {
                   valueMap.put(decodeArray[j].getVarbindvalue(), decodeArray[j].getVarbinddecodedstring());
               }
               decode.put(vardecodeArray[i].getParmid(), valueMap);
           }
        }// end fill of event using econf

        // do the event parm expansion
        expandParms(e, decode);

    } // end expandEvent()


    public void process(Header eventHeader, Event event) {
        expandEvent(event);
    }

    public EventConfDao getEventConfDao() {
        return m_eventConfDao;
    }

    public void setEventConfDao(EventConfDao eventConfDao) {
        m_eventConfDao = eventConfDao;
    }
}
