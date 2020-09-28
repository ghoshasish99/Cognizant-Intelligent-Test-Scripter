/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cognizant.cognizantits.datalib.settings;

/**
 *
 * @author 576250
 */
public class ReportPortalSettings extends AbstractPropSettings {
    
    public ReportPortalSettings(String location) {
        super(location, "reportportal");
        if (isEmpty()) {
            loadDefault();
        }
    }

    private void loadDefault() {
        put("rp.endpoint", "http://localhost:8080");
        put("rp.uuid", "");
        put("rp.project", "");
    }
    
}
