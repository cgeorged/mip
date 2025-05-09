package aero.sita.pts.gsl.tools.mip.config;


import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;



@Configuration
@ConfigurationProperties("")
public class CustomProperties {

    private Map<String, String> connections = new LinkedHashMap <>();

    public Map<String, String> getConnections() {
        return connections;
    }

    /**
     * @param connections the connections to set
     */
    public void setConnections(Map<String, String> connections) {
        this.connections = connections;
        
    }
    
 

}