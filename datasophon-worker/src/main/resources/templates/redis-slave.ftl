bind 0.0.0.0
daemonize yes
protected-mode no
port ${redisSlavePort}
logfile "/opt/datasophon/redis_cluster/cluster/log/cluster-slave.log"
pidfile /opt/datasophon/redis_cluster/cluster/pid/cluster-slave.pid
dir /opt/datasophon/redis_cluster/cluster
dbfilename dump-slave.rdb
appendonly yes
appendfilename "appendonly-slave.aof"

cluster-enabled yes
cluster-config-file /opt/datasophon/redis_cluster/cluster/conf/nodes-slave.conf
cluster-node-timeout 5000

<#list itemList as item>
    ${item.name} ${item.value}
</#list>