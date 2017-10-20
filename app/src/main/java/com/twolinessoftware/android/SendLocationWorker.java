/*
 * Copyright (c) 2011 2linessoftware.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.twolinessoftware.android;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.twolinessoftware.android.framework.service.comms.Worker;
import com.twolinessoftware.android.framework.service.comms.gpx.GpxTrackPoint;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SendLocationWorker extends Worker {

    private long gpsPointTime = 0;
    private GpxTrackPoint point;
    private String providerName;
    private LocationManager mLocationManager;
    private long sendTime;

    public SendLocationWorker(LocationManager mLocationManager,
                              GpxTrackPoint point, String providerName, long localSendTime) {
        super();
        this.point = point;
        this.providerName = providerName;
        this.mLocationManager = mLocationManager;
        this.sendTime = localSendTime;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    @Override
    public void run() {
        sendLocation(point);
    }


    private void sendLocation(GpxTrackPoint point) {
        parseData(point);
        Location loc = new Location(providerName);
        loc.setLatitude(point.getLat());
        loc.setLongitude(point.getLon());
        loc.setTime(gpsPointTime);
        loc.setBearing((float) point.getHeading());
        loc.setAccuracy(1.0f);
        loc.setSpeed((float) point.getSpeed());
        loc.setAltitude(100.0);



        Method method;
        try {
            method = Location.class.getMethod("makeComplete", new Class[0]);
            if (method != null) {
                try {
                    method.invoke(loc, new Object[0]);
                } catch (Exception exception) {
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }


        try {
            Log.d("SendLocation", "Sending update for " + providerName);
            mLocationManager.setTestProviderLocation(providerName, loc);
        } catch (Exception e) {
            Log.d("stop", e.toString());
        }

    }

    private void parseData(GpxTrackPoint point) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date gpsDate = format.parse(point.getTime());
            gpsPointTime = gpsDate.getTime();
        } catch (ParseException e) {
            Log.e("timeparse", "Unable to parse time:" + point.getTime());
        }

    }


}
