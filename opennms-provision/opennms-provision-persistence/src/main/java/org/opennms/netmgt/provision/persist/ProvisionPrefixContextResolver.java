package org.opennms.netmgt.provision.persist;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.opennms.netmgt.provision.persist.requisition.Requisition;

@Provider
public class ProvisionPrefixContextResolver implements ContextResolver<JAXBContext> {
    private final Map<Class<?>,JAXBContext> m_contexts = new HashMap<Class<?>,JAXBContext>();
    
    public ProvisionPrefixContextResolver() throws JAXBException {
        m_contexts.put(Requisition.class, new ProvisionJAXBContext(JAXBContext.newInstance(Requisition.class), "http://xmlns.opennms.org/xsd/config/model-import"));
    }

    public JAXBContext getContext(Class<?> objectType) {
        return (m_contexts.get(objectType));
    }

}