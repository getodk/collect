/*
 * Copyright (C) 2011 Smap Consulting Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.taskModel;

/**
 * Created by neilpenman on 14/11/2014.
 */
public class FieldTaskSettings {
    public String ft_delete;
    public boolean ft_delete_submitted;
    public String ft_send_location;
    public boolean ft_location_trigger;
    public boolean ft_odk_style_menus;
    public boolean ft_specify_instancename;
    public boolean ft_mark_finalized;
    public boolean ft_prevent_disable_track;
    public String ft_enable_geofence;   // Not boolean so it can be backward compatible with servers that do not set it
    public boolean ft_admin_menu;
    public boolean ft_server_menu;
    public boolean ft_meta_menu;
    public boolean ft_exit_track_menu;
    public boolean ft_bg_stop_menu;
    public boolean ft_review_final;
    public String ft_high_res_video;
    public String ft_guidance;
    public String ft_send;
    public boolean ft_send_wifi;
    public boolean ft_send_wifi_cell;
    public String ft_image_size;
    public int ft_pw_policy;
    public String ft_backward_navigation;
    public String ft_navigation;
    public String ft_input_method;
    public int ft_im_ri;
    public int ft_im_acc;
}
