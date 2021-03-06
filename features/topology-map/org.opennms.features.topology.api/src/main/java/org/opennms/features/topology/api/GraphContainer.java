package org.opennms.features.topology.api;

import java.util.Collection;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;

public interface GraphContainer extends DisplayState {
	
	public VertexContainer<?, ?> getVertexContainer();
	
	public BeanContainer<?, ?> getEdgeContainer();
	
	public Collection<?> getVertexIds();
	
	public Collection<?> getEdgeIds();
	
	public Item getVertexItem(Object vertexId);
	
	public Item getEdgeItem(Object edgeId);
	
	public Collection<?> getEndPointIdsForEdge(Object edgeId);
	
	public Collection<?> getEdgeIdsForVertex(Object vertexId);

    public Object getVertexItemIdForVertexKey(Object key);
	
}
