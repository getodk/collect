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

import com.google.gson.annotations.SerializedName;

import org.odk.collect.android.database.TaskAssignment;
import org.odk.collect.android.loaders.PointEntry;

import java.util.ArrayList;
import java.util.List;

public class TaskResponse {

	public String message;
	public String status;
    public int version;     // Manage progressive enhancement of this service by incrementing version
	public String deviceId;
	@SerializedName("data")
	public List<TaskAssignment> taskAssignments;
	public List<FormLocator> forms;
    public FieldTaskSettings settings;
    public List<TaskCompletionInfo> taskCompletionInfo;
    public ArrayList<PointEntry> userTrail;
}
