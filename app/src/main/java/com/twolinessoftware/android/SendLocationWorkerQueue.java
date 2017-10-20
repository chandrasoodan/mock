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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.twolinessoftware.android.framework.util.Logger;

import java.util.LinkedList;

public class SendLocationWorkerQueue {

    private LinkedList<SendLocationWorker> queue;
    private boolean running;
    private WorkerThread thread;
    private static int locCount=0;


    private Object lock = new Object();

    public SendLocationWorkerQueue() {
        queue = new LinkedList<SendLocationWorker>();
        running = false;
    }


    public void addToQueue(SendLocationWorker worker) {
        synchronized (queue) {
            queue.addLast(worker);
        }

    }

    public synchronized void start(long delayTimeOnReplay, Context c) {
        locCount=0;
        running = true;
        thread = new WorkerThread(delayTimeOnReplay,c);
        thread.start();
    }

    public synchronized void stop() {
        /*
		 * synchronized(lock){ lock.notify(); }
		 */
        running = false;
    }

    public void reset() {
        stop();
        queue = new LinkedList<SendLocationWorker>();

        stopThread();
    }

    public void stopThread() {
        if (thread != null) {
            try {locCount=0;
                thread.interrupt();
            } catch (Exception e) {
                Logger.i("SendLocationWorkerQueue.stopThread() - exception", "" + e.getMessage());
            }
            this.thread = null;
        }
    }
    private class WorkerThread extends Thread {

        private long TIME_BETWEEN_SENDS = 10000; // milliseconds
        private Context c;
        WorkerThread(long delayTimeOnReplay,Context c) {
            TIME_BETWEEN_SENDS = delayTimeOnReplay;
            this.c=c;
        }
        public void toast(final Context context, final String text) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                }
            });
        }

        public void run() {
            while (running) {

                if (queue.size() > 0) {

                    SendLocationWorker  worker = queue.pop();

                    synchronized (lock) {
                        try {
                            lock.wait(TIME_BETWEEN_SENDS);
                            locCount++;
                           // Toast.makeText(c,"Loc no"+locCount,Toast.LENGTH_LONG).show();
                         //   MainActivity.showToast("hi");
                            toast(c,"sent location"+locCount);

                            Logger.i("SendLocationWorkerQueue.running - TIME_BETWEEN_SENDS : " + TIME_BETWEEN_SENDS,"  "+ locCount);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // Executing each worker in the current thread. Multiple threads NOT created.
                    worker.run();
                }
            }
        }
    }

}
