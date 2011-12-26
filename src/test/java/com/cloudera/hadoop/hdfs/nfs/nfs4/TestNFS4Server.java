/**
 * Copyright 2011 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.hadoop.hdfs.nfs.nfs4;

import static com.cloudera.hadoop.hdfs.nfs.nfs4.Constants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

import org.apache.hadoop.conf.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cloudera.hadoop.hdfs.nfs.rpc.RPCBuffer;
import com.cloudera.hadoop.hdfs.nfs.rpc.RPCRequest;
import com.cloudera.hadoop.hdfs.nfs.rpc.RPCResponse;
import com.cloudera.hadoop.hdfs.nfs.rpc.RPCTestUtil;

public class TestNFS4Server {

  NFS4Server mNFS4Server;
  int mPort;
  
  @Before
  public void setup() throws Exception {
    Configuration conf = new Configuration();
    mNFS4Server = new NFS4Server();
    mNFS4Server.setConf(conf);
    mNFS4Server.start(0);
    mPort = mNFS4Server.getPort();
  }
  
  @After
  public void cleanup() {
    if(mNFS4Server != null) {
      mNFS4Server.shutdown();
    }
  }
  @Test
  public void testNull() throws UnknownHostException, IOException {
    assertTrue(mNFS4Server.isAlive());
    RPCRequest request = RPCTestUtil.createRequest();
    request.setProcedure(NFS_PROC_NULL);
    RPCBuffer buffer = new RPCBuffer();
    request.write(buffer);
    
    Socket socket = new Socket("localhost", mPort);
    try {
      OutputStream out = socket.getOutputStream();
      InputStream in = socket.getInputStream();
      
      buffer.write(out);
      
      buffer = RPCBuffer.from(in);
      RPCResponse response = new RPCResponse();
      response.read(buffer);
      assertEquals(request.getXid(), response.getXid());
      assertEquals(RPC_REPLY_STATE_ACCEPT, response.getReplyState());
      assertEquals(RPC_ACCEPT_SUCCESS, response.getAcceptState());
    } finally {
      try {
        socket.close();
      } catch(Exception ex) {}
    }
  }
}