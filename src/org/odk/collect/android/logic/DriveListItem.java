/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.logic;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.client.util.DateTime;

public class DriveListItem implements Comparable<DriveListItem>, Parcelable {
    private String name;
    private String data;
    private String path;
    private String image;
    private String driveId;
    private String parentId;

    private DateTime date;
    private int type;


    public static final int FILE = 1;
    public static final int DIR = 2;
    public static final int UP = 3;
    public static final int MY_DRIVE = 4;
    public static final int SHARED_WITH_ME = 5;

    public DriveListItem(String n, String d, DateTime dt, String p, String img, int type, String driveId, String parentId) {
        name = n;
        data = d;
        date = dt;
        path = p;
        image = img;
        this.type = type;
        this.driveId = driveId;
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public DateTime getDate() {
        return date;
    }

    public String getPath() {
        return path;
    }

    public String getImage() {
        return image;
    }

    public int getType() {
        return type;
    }

    public String getDriveId() {
        return driveId;
    }

    public String getParentId() {
        return parentId;
    }

    public int compareTo(DriveListItem o) {
        if (this.name != null)
            return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
        else
            throw new IllegalArgumentException();
    }

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.name);
		dest.writeString(this.data);
		dest.writeString(this.path);
		dest.writeString(this.image);
		dest.writeString(this.driveId);
		dest.writeString(this.parentId);

		 dest.writeLong(date.getValue());
		 dest.writeInt(this.type);		
	}
	
	public DriveListItem(Parcel pc) {
		name = pc.readString();
		data = pc.readString();
		path = pc.readString();
		image = pc.readString();
		driveId = pc.readString();
		parentId = pc.readString();
		date = new DateTime(pc.readLong());
		type = pc.readInt();
	}
	
	public static final Parcelable.Creator<DriveListItem> CREATOR = new Parcelable.Creator<DriveListItem>() {
		
		public DriveListItem createFromParcel(Parcel pc) {
			return new DriveListItem(pc);
		}
		
		public DriveListItem[] newArray(int size) {
			return new DriveListItem[size];
		}
	};
}