/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.jraft.rpc.impl.core;

import java.util.concurrent.Executor;

import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.NodeManager;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.error.RaftError;
import com.alipay.sofa.jraft.rpc.RaftServerService;
import com.alipay.sofa.jraft.rpc.RpcRequestClosure;
import com.alipay.sofa.jraft.rpc.RpcRequestProcessor;
import com.alipay.sofa.jraft.rpc.RpcResponseFactory;
import com.google.protobuf.Message;

/**
 * Node handle requests processor template.
 *
 * @author boyan (boyan@alibaba-inc.com)
 *
 * 2018-Apr-08 6:03:25 PM 
 * @param <T>
 */
public abstract class NodeRequestProcessor<T extends Message> extends RpcRequestProcessor<T> {

    public NodeRequestProcessor(Executor executor) {
        super(executor);
    }

    protected abstract Message processRequest0(RaftServerService serviceService, T request, RpcRequestClosure done);

    protected abstract String getPeerId(T request);

    protected abstract String getGroupId(T request);

    @Override
    public Message processRequest(T request, RpcRequestClosure done) {
        PeerId peer = new PeerId();
        // 客户端的对端，则指本端
        String peerIdStr = getPeerId(request);
        if (peer.parse(peerIdStr)) {
            Node node = NodeManager.getInstance().get(getGroupId(request), peer);
            if (node != null) {
                return processRequest0((RaftServerService) node, request, done);
            } else {
                return RpcResponseFactory.newResponse(RaftError.ENOENT, "Peer id not found: %s", peerIdStr);
            }
        } else {
            return RpcResponseFactory.newResponse(RaftError.EINVAL, "Fail to parse peerId: %s", peerIdStr);
        }
    }
}
