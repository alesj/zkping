/**
 *  Copyright 2014 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package io.fabric8.test.jgroups.zookeeper;

import io.fabric8.jgroups.zookeeper.ConfigurableZooKeeperPing;
import io.fabric8.test.jgroups.zookeeper.support.ZooKeeperUtils;
import org.jgroups.JChannel;
import org.jgroups.protocols.TCP;
import org.jgroups.protocols.UNICAST3;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.util.Util;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class TestBase {
    private static final int NUM = 2;

    protected JChannel[] channels;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ZooKeeperUtils.startServer();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        ZooKeeperUtils.stopServer();
    }

    @Before
    public void setUp() throws Exception {
        channels = new JChannel[NUM];

        for (int i = 0; i < NUM; i++) {
            ConfigurableZooKeeperPing zkPing = new ConfigurableZooKeeperPing();
            zkPing.setConnection("localhost:2181");

            channels[i] = new JChannel(
                new TCP(),
                zkPing,
                new NAKACK2(),
                new UNICAST3(),
                new STABLE(),
                new GMS()
            );
            channels[i].setName(Character.toString((char) ('A' + i)));
            channels[i].connect("test");
        }
    }

    @After
    public void tearDown() throws Exception {
        for (int i = 0; i < NUM; i++) {
            Util.close(channels[i]);
        }
    }
}