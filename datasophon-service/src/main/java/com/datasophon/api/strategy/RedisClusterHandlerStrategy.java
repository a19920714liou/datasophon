package com.datasophon.api.strategy;

import com.datasophon.api.load.GlobalVariables;
import com.datasophon.api.utils.ProcessUtils;
import com.datasophon.common.Constants;
import com.datasophon.common.cache.CacheUtils;
import com.datasophon.common.model.ServiceConfig;
import com.datasophon.common.model.ServiceRoleInfo;
import com.datasophon.dao.entity.ClusterInfoEntity;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RedisClusterHandlerStrategy extends ServiceHandlerAbstract implements ServiceRoleStrategy {
    private static final Logger logger = LoggerFactory.getLogger(RedisClusterHandlerStrategy.class);

    @Override
    public void handler(Integer clusterId, List<String> hosts, String serviceName) {

    }

    @Override
    public void handlerConfig(Integer clusterId, List<ServiceConfig> list, String serviceName) {
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        for (ServiceConfig serviceConfig : list) {
            logger.info("serviceConfig:{}", serviceConfig.getName() + ":" + serviceConfig.getValue());
            if ("redisMasterPort".equals(serviceConfig.getName())) {
                ProcessUtils.generateClusterVariable(globalVariables, clusterId, serviceName,
                        "${redisMasterPort}",
                        String.valueOf(serviceConfig.getValue()));
            } else if ("redisSlavePort".equals(serviceConfig.getName())) {
                ProcessUtils.generateClusterVariable(globalVariables, clusterId, serviceName,
                        "${redisSlavePort}",
                        String.valueOf(serviceConfig.getValue()));
            }
        }
    }

    @Override
    public void getConfig(Integer clusterId, List<ServiceConfig> list) {
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        String masterPort = globalVariables.get("${redisMasterPort}");
        String slavePort = globalVariables.get("${redisSlavePort}");

        ClusterInfoEntity clusterInfo = ProcessUtils.getClusterInfo(clusterId);
        String hostMapKey =
                clusterInfo.getClusterCode()
                        + Constants.UNDERLINE
                        + Constants.SERVICE_ROLE_HOST_MAPPING;
        HashMap<String, List<String>> map = (HashMap<String, List<String>>) CacheUtils.get(hostMapKey);
        List<String> masterHostList = map.get("RedisMaster");
        List<String> workerHostList = map.get("RedisWorker");

        for (ServiceConfig serviceConfig : list) {
            if ("RedisMasterAddr".equals(serviceConfig.getName())) {
                String masterAddr = masterHostList.stream()
                        .map(t -> t + ":" + masterPort)
                        .collect(Collectors.joining(" "));
                serviceConfig.setRequired(true);
                serviceConfig.setValue(masterAddr);
            } else if ("RedisSlaveAddr".equals(serviceConfig.getName())) {
                String workerAddr = workerHostList.stream()
                        .map(t -> t + ":" + slavePort)
                        .collect(Collectors.joining(" "));
                serviceConfig.setRequired(true);
                serviceConfig.setValue(workerAddr);
            }
        }
    }

    @Override
    public void handlerServiceRoleInfo(ServiceRoleInfo serviceRoleInfo, String hostname) {

    }

    @Override
    public void handlerServiceRoleCheck(ClusterServiceRoleInstanceEntity roleInstanceEntity, Map<String, ClusterServiceRoleInstanceEntity> map) {

    }
}