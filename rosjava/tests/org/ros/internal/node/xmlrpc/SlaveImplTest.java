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

package org.ros.internal.node.xmlrpc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.junit.Test;
import org.mockito.Matchers;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.response.StatusCode;
import org.ros.internal.node.server.ServerException;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.topic.Publisher;
import org.ros.internal.transport.ProtocolNames;
import org.ros.internal.transport.tcp.TcpRosProtocolDescription;

import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class SlaveImplTest {

  @Test
  public void testGetPublicationsEmptyList() {
    SlaveServer mockSlave = mock(SlaveServer.class);
    when(mockSlave.getPublications()).thenReturn(Lists.<Publisher<?>>newArrayList());
    SlaveImpl slave = new SlaveImpl(mockSlave);
    List<Object> response = slave.getPublications("/foo");
    assertEquals(response.get(0), StatusCode.SUCCESS.toInt());
    assertEquals(response.get(2), Lists.newArrayList());
  }

  @Test
  public void testGetPublications() {
    SlaveServer mockSlave = mock(SlaveServer.class);
    Publisher<?> mockPublisher = mock(Publisher.class);
    when(mockSlave.getPublications()).thenReturn(Lists.<Publisher<?>>newArrayList(mockPublisher));
    when(mockPublisher.getTopicName()).thenReturn(new GraphName("/bar"));
    when(mockPublisher.getTopicMessageType()).thenReturn("/baz");
    when(mockPublisher.getTopicDefinitionAsList()).thenReturn(Lists.newArrayList("/bar", "/baz"));
    SlaveImpl slave = new SlaveImpl(mockSlave);
    List<Object> response = slave.getPublications("/foo");
    assertEquals(StatusCode.SUCCESS.toInt(), response.get(0));
    List<List<String>> protocols = Lists.newArrayList();
    protocols.add(Lists.newArrayList("/bar", "/baz"));
    assertEquals(protocols, response.get(2));
  }

  @Test
  public void testRequestTopic() throws ServerException {
    SlaveServer mockSlave = mock(SlaveServer.class);
    AdvertiseAddress address = AdvertiseAddress.createPrivate();
    address.setStaticPort(1234);
    TcpRosProtocolDescription protocol = new TcpRosProtocolDescription(address);
    when(
        mockSlave.requestTopic(Matchers.<String>any(),
            Matchers.eq(Sets.newHashSet(ProtocolNames.TCPROS, ProtocolNames.UDPROS)))).thenReturn(
        protocol);
    SlaveImpl slave = new SlaveImpl(mockSlave);
    Object[][] protocols = new Object[][] { {ProtocolNames.TCPROS}, {ProtocolNames.UDPROS}};
    List<Object> response = slave.requestTopic("/foo", "/bar", protocols);
    assertEquals(response.get(0), StatusCode.SUCCESS.toInt());
    assertEquals(response.get(2), protocol.toList());
  }

  @Test
  public void testGetPid() {
    SlaveServer mockSlave = mock(SlaveServer.class);
    AdvertiseAddress address = AdvertiseAddress.createPrivate();
    address.setStaticPort(1234);
    when(mockSlave.getPid()).thenReturn(1234);
    SlaveImpl slave = new SlaveImpl(mockSlave);
    List<Object> response = slave.getPid("/foo");
    assertEquals(response.get(0), StatusCode.SUCCESS.toInt());
    assertEquals(response.get(2), 1234);
  }

  @Test
  public void testGetPidNotSupported() {
    SlaveServer mockSlave = mock(SlaveServer.class);
    AdvertiseAddress address = AdvertiseAddress.createPrivate();
    address.setStaticPort(1234);
    when(mockSlave.getPid()).thenThrow(new UnsupportedOperationException());
    SlaveImpl slave = new SlaveImpl(mockSlave);
    List<Object> response = slave.getPid("/foo");
    assertEquals(response.get(0), StatusCode.FAILURE.toInt());
  }

}
