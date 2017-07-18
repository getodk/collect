package org.odk.collect.android.database;

import java.util.Date;

public class Task {
	public int id;
	public String type;
	public String title;
	public String url;
	public String form_id;
	public int form_version;
	public String initial_data;
    public String update_id;
	public String assignment_mode;
	public Date scheduled_at;
	public String location_trigger;
	public boolean repeat;			// Task can be completed multiple times
	public String address;			// Key value pairs representing an unstructured address
	public String status;
}
