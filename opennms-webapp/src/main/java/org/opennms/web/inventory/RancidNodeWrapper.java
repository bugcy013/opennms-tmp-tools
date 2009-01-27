package org.opennms.web.inventory;

import java.util.Date;

public class RancidNodeWrapper {
    
    private String deviceName;
    private String group;
    private String deviceType;
    private String comment;
    private String headRevision;
    private int totalRevisions;
    private Date expirationDate;
    private String rootConfigurationUrl;
    
    RancidNodeWrapper(String _deviceName, String _group, String _deviceType, String _comment, String _headRevision,
                      int _totalRevision, Date _expirationDate, String _rootConfigurationUrl) {
         deviceName=_deviceName;
         group=_group;
         deviceType=deviceType;
         comment=_comment;
         headRevision=_headRevision;
         totalRevisions=_totalRevision;
         expirationDate=_expirationDate;
         rootConfigurationUrl=_rootConfigurationUrl;
    }
            
    public String getDeviceName(){
        return deviceName;
    }
    public String getGroup(){
        return group;
    }
    public String getDeviceType(){
        return deviceType;
    }
    public String getComment(){
        return comment;
    }
    public String getHeadRevision(){
        return headRevision;
    }
    public int getTotalRevisions(){
        return totalRevisions;
    }
    public Date getExpirationDate(){
        return expirationDate;
    }
    public String getRootConfigurationUrl(){
        return rootConfigurationUrl;
    }

}