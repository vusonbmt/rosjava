/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.tutorials.orientation_publisher;

import com.google.common.collect.Lists;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import org.ros.NodeRunner;
import org.ros.rosjava.android.MessageCallable;
import org.ros.rosjava.android.OrientationPublisher;
import org.ros.rosjava.android.tutorials.orientation_publisher.R;
import org.ros.rosjava.android.views.RosTextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MainActivity extends Activity {

  private final NodeRunner nodeRunner;

  public MainActivity() {
    super();
    nodeRunner = NodeRunner.createDefault();
  }

  @Override
  protected void onPause() {
    super.onPause();
    finish();
  }

  // TODO(damonkohler): Pull this out some place.s
  private static String getNonLoopbackHostName() {
    try {
      String address = null;
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
          .hasMoreElements();) {
        NetworkInterface intf = en.nextElement();
        Log.i("RosAndroid", "Interface: " + intf.getName());
        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
            .hasMoreElements();) {
          InetAddress inetAddress = enumIpAddr.nextElement();
          Log.i("RosAndroid", "Address: " + inetAddress.getHostAddress().toString());
          // IPv4 only for now
          if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4) {
            if (address == null)
              address = inetAddress.getHostAddress().toString();
          }
        }
      }
      if (address != null)
        return address;
    } catch (SocketException ex) {
      Log.i("RosAndroid", "SocketException: " + ex.getMessage());
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    RosTextView<org.ros.message.geometry_msgs.PoseStamped> rosTextView =
        (RosTextView<org.ros.message.geometry_msgs.PoseStamped>) findViewById(R.id.text);
    rosTextView.setTopicName("/android/orientation");
    rosTextView.setMessageClass(org.ros.message.geometry_msgs.PoseStamped.class);
    rosTextView
        .setMessageToStringCallable(new MessageCallable<String, org.ros.message.geometry_msgs.PoseStamped>() {
          @Override
          public String call(org.ros.message.geometry_msgs.PoseStamped message) {
            return "x: " + message.pose.orientation.x + "\ny: " + message.pose.orientation.y
                + "\nz: " + message.pose.orientation.z + "\nw: " + message.pose.orientation.w;
          }
        });
    try {
      // TODO(damonkohler): The master needs to be set via some sort of
      // configuration builder.
      String uri = "__master:=http://10.68.0.1:11311";
      nodeRunner.run(new OrientationPublisher((SensorManager) getSystemService(SENSOR_SERVICE)),
          Lists.newArrayList("Orientation", uri, "__ip:=" + getNonLoopbackHostName()));
      nodeRunner.run(rosTextView, Lists.newArrayList("Text", uri));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
