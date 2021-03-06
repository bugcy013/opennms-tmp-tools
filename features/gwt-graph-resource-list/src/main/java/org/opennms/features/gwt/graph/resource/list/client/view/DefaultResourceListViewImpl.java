package org.opennms.features.gwt.graph.resource.list.client.view;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

public class DefaultResourceListViewImpl extends Composite implements DefaultResourceListView<ResourceListItem> {

    private static DefaultResourceListViewImplUiBinder uiBinder = GWT.create(DefaultResourceListViewImplUiBinder.class);

    interface DefaultResourceListViewImplUiBinder extends UiBinder<Widget, DefaultResourceListViewImpl> { }
    
    @UiField
    LayoutPanel m_layoutPanel;
    
    @UiField
    ResourceTable m_resourceTable;
    
    @UiField
    Button m_searchBtn;
    
    @UiField
    FlowPanel m_simplePagerContainer;
    
    private ListDataProvider<ResourceListItem> m_dataProvider;
    private Presenter<ResourceListItem> m_presenter;
    
    public DefaultResourceListViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        
        m_layoutPanel.setSize("100%", "335px");
        
        m_dataProvider = new ListDataProvider<ResourceListItem>();
        m_dataProvider.addDataDisplay(m_resourceTable);
        
        SimplePager pager = new SimplePager(TextLocation.CENTER);
        pager.setStyleName("onms-table-no-borders-margin");
        pager.getElement().getStyle().setBackgroundColor("#E0E0E0");
        pager.getElement().getStyle().setWidth(100, Unit.PCT);
        pager.setDisplay(m_resourceTable);
        m_simplePagerContainer.add(pager);
        
        m_resourceTable.getSelectionModel().addSelectionChangeHandler(new Handler() {
            
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                m_presenter.onResourceItemSelected();
            }
        });
        
        m_resourceTable.setWidth("100%");
    }
    
    @UiHandler("m_searchBtn")
    public void onSearchButtonClick(ClickEvent event) {
        m_presenter.onSearchButtonClicked();
    }
    
    @Override
    public void setDataList(List<ResourceListItem> dataList) {
        m_dataProvider.setList(dataList);
    }

    @Override
    public void setPresenter(Presenter<ResourceListItem> presenter) {
        m_presenter = presenter;
    }

    @Override
    public void showWarning() {
        Window.alert("Please Choose A Resource");
    }

    @Override
    public ResourceListItem getSelectedResource() {
        return m_resourceTable.getSelectedResourceItem();
    }

}
