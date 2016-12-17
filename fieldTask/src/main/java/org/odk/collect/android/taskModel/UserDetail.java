package org.odk.collect.android.taskModel;

import com.google.gson.annotations.SerializedName;

public class UserDetail {
	
	public int id;
	
	public String name;
	
	public String location;
	
	@SerializedName("has_gps")
	public boolean hasGps;
	
	@SerializedName("has_camera")
	public boolean hasCamer;
	
	@SerializedName("has_data")
	public boolean hasData;
	
	@SerializedName("has_sms")
	public boolean hasSms;
	
	@SerializedName("phone_number")
	public String phoneNumber;
	
	@SerializedName("device_id")
	public String deviceId;
	
	@SerializedName("max_dist_km")
	public double maxDist;
	
	@SerializedName("user_role")
	public String userRole;
	
	public String team;
}
