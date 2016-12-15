package org.odk.collect.android.utilities;

import android.text.format.Time;

import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.STFileUtils;

import java.io.File;
import java.io.FileOutputStream;

public final class FileLogger {
	
	private static final void m(String event, String taskName, String taskSource,
			long scheduledStart, String template, String status) {
		try {
			boolean headers = false;
			if(FileUtils.createFolder(STFileUtils.LOGS_PATH)) {
				File f = new File(STFileUtils.METRIC_FILE);
				if(!f.exists()) {
					f.createNewFile();
					headers = true;
				}
				FileOutputStream os = new FileOutputStream(f, true);
				
                Time t = new Time();
                t.setToNow();
               
                byte[] buffer;
                String record;
                
                if(headers) {
                	record = "Time, Event, Task, Source, Scheduled Start, Template, Status\n";
                	buffer = record.getBytes();
                	os.write(buffer);
                }
                
				record = STFileUtils.getLogTime(t.toMillis(false)) + "," +
						event + "," + taskName + "," + taskSource +
						"," + STFileUtils.getLogTime(scheduledStart) + "," + template + "," + status + "\n";
				buffer = record.getBytes();
				os.write(buffer);
				
				os.close();
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/*
	 * Write the status of a task to the metrics file after an event
	 */
	public synchronized static final void metric(String event, long taskId)  {
        
		try {
            /*
			c = tda.fetchTaskForId(assignment_id);
	        c.moveToFirst();
	
	        String name = c.getString(c.getColumnIndex(FileDbAdapter.KEY_T_TITLE));
	        String source = c.getString(c.getColumnIndex(FileDbAdapter.KEY_T_SOURCE));
	        long scheduledStart = c.getLong(c.getColumnIndex(FileDbAdapter.KEY_T_SCHEDULED_START));
	        String taskForm = c.getString(c.getColumnIndex(FileDbAdapter.KEY_T_TASKFORM));
	        String status = c.getString(c.getColumnIndex(FileDbAdapter.KEY_T_STATUS));
	
	      	m(event, name, source, scheduledStart, taskForm, status);	
	      	*/

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
