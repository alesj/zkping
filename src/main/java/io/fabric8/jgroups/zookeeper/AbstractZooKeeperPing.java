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

package io.fabric8.jgroups.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.jgroups.Address;
import org.jgroups.Event;
import org.jgroups.protocols.FILE_PING;
import org.jgroups.protocols.PingData;
import org.jgroups.util.Responses;
import org.jgroups.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author Ioannis Canellos
 * @author Bela Ban
 */
public abstract class AbstractZooKeeperPing extends FILE_PING {
    private static final String ROOT_PATH = "/fabric/registry/jgroups/";

    private String discoveryPath;
    private String localNodePath;

    protected CuratorFramework curator;

    protected abstract CuratorFramework createCurator();

    @Override
    public void init() throws Exception {
        curator = createCurator();
        super.init();
    }

    @Override
    public void stop() {
        try {
            removeNode(localNodePath);
        } finally {
            super.stop();
        }
    }

    public Object down(Event evt) {
        switch (evt.getType()) {
            case Event.CONNECT:
            case Event.CONNECT_USE_FLUSH:
            case Event.CONNECT_WITH_STATE_TRANSFER:
            case Event.CONNECT_WITH_STATE_TRANSFER_USE_FLUSH:
                discoveryPath = ROOT_PATH + evt.getArg();
                localNodePath = discoveryPath + "/" + addressAsString(local_addr);
                _createRootDir();
                break;
        }
        return super.down(evt);
    }

    /**
     * Creates the root node in ZooKeeper (/fabric/registry/jgroups
     */
    @Override
    protected void createRootDir() {
        // empty on purpose to prevent dir from being created in the local file system
    }

    protected void _createRootDir() {
        try {
            if (curator.checkExists().forPath(localNodePath) == null) {
                curator.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(localNodePath);
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to create dir %s in ZooKeeper.", localNodePath), e);
        }
    }

    /**
     * Reads all information from the given directory under clustername
     *
     * @return all data
     */
    @Override
    protected synchronized void readAll(List<Address> members, String clustername, Responses responses) {
        List<PingData> retval = new ArrayList<>();
        try {
            String clusterPath = discoveryPath;
            for (String node : curator.getChildren().forPath(clusterPath)) {
                String nodePath = ZKPaths.makePath(clusterPath, node);
                PingData data = readPingData(nodePath);
                if(data != null) {
                    responses.addResponse(data,true);
                    if(local_addr != null && !local_addr.equals(data.getAddress()))
                        addDiscoveryResponseToCaches(data.getAddress(),data.getLogicalName(),data.getPhysicalAddr());
                }
            }

        } catch (Exception e) {
            log.debug(String.format("Failed to read ping data from ZooKeeper for cluster: %s", clustername), e);
        }
    }

    @Override
    protected synchronized void writeToFile(PingData data, String clustername) {
        writePingData(data);
    }

    protected synchronized PingData readPingData(String path) {
        PingData retval = null;
        DataInputStream in = null;
        try {
            byte[] bytes = curator.getData().forPath(path);
            in = new DataInputStream(new ByteArrayInputStream(bytes));
            PingData tmp = new PingData();
            tmp.readFrom(in);
            return tmp;
        } catch (Exception e) {
            log.debug(String.format("Failed to read ZooKeeper znode: %s", path), e);
        } finally {
            Util.close(in);
        }
        return retval;
    }

    protected synchronized void writePingData(PingData data) {
        String nodePath = localNodePath;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(baos);

            data.writeTo(dos);

            if (curator.checkExists().forPath(nodePath) == null) {
                curator.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(localNodePath, baos.toByteArray());
            } else {
                curator.setData().forPath(localNodePath, baos.toByteArray());
            }
        } catch (Exception ex) {
            log.error("Error saving ping data", ex);
        } finally {
            Util.close(dos);
            Util.close(baos);
        }
    }

    protected void removeNode(String path) {
        try {
            curator.delete().forPath(path);
        } catch (Exception e) {
            log.error(String.format("Failed removing %s", path), e);
        }
    }

    protected void remove(String clustername, Address addr) {
        removeNode(discoveryPath + "/" + addressAsString(addr));
    }
}
