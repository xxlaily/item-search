package cn.dm.es.common;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.io.Serializable;

@Component
@ConfigurationProperties(prefix="elasticsearch")
public class EsConnection implements Serializable{
    // http请求的IP
    private String ip;
    // http请求的端口是9200，客户端是9300
    private int port;
    //集群名称
    private String clusterName;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
