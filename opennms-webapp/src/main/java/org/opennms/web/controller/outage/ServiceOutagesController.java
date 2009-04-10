package org.opennms.web.controller.outage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.element.ElementUtil;
import org.opennms.web.element.Service;
import org.opennms.web.outage.Outage;
import org.opennms.web.outage.WebOutageRepository;
import org.opennms.web.outage.filter.ConditionalFilter;
import org.opennms.web.outage.filter.CurrentOutageFilter;
import org.opennms.web.outage.filter.Filter;
import org.opennms.web.outage.filter.InterfaceFilter;
import org.opennms.web.outage.filter.NodeFilter;
import org.opennms.web.outage.filter.OutageCriteria;
import org.opennms.web.outage.filter.RegainedServiceDateAfterFilter;
import org.opennms.web.outage.filter.ServiceFilter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class ServiceOutagesController extends AbstractController implements InitializingBean {

    private String m_successView;
    private WebOutageRepository m_webOutageRepository;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Service service = ElementUtil.getServiceByParams(request);

        Outage[] outages = new Outage[0];

        if (service.getNodeId() > 0 && service.getIpAddress() != null && service.getServiceId() > 0) {
            List<Filter> filters = new ArrayList<Filter>();

            filters.add(new InterfaceFilter(service.getIpAddress()));
            filters.add(new NodeFilter(service.getNodeId()));
            filters.add(new ServiceFilter(service.getServiceId()));

            Calendar cal = new GregorianCalendar();
            cal.add( Calendar.DATE, -1 );
            Date yesterday = cal.getTime();
            ConditionalFilter cf = new ConditionalFilter("OR", new RegainedServiceDateAfterFilter(yesterday), new CurrentOutageFilter());
            filters.add(cf);

            OutageCriteria criteria = new OutageCriteria(filters.toArray(new Filter[0]));
            outages = m_webOutageRepository.getMatchingOutages(criteria);
        }

        ModelAndView modelAndView = new ModelAndView(getSuccessView());
        modelAndView.addObject("outages", outages);
        return modelAndView;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_successView, "property successView must be set");
        Assert.notNull(m_webOutageRepository, "webOutageRepository must be set");
    }

    private String getSuccessView() {
        return m_successView;
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }
    
    public void setWebOutageRepository(WebOutageRepository webOutageRepository) {
        m_webOutageRepository = webOutageRepository;
    }

}