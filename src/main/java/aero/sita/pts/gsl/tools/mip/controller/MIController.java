package aero.sita.pts.gsl.tools.mip.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.jms.JmsConstants;

import aero.sita.pts.gsl.tools.mip.config.CustomProperties;
import aero.sita.pts.gsl.tools.mip.controller.helper.ApisContent;
import aero.sita.pts.gsl.tools.mip.model.ConnectionDetails;
import aero.sita.pts.gsl.tools.mip.model.MessageReq;
import aero.sita.pts.gsl.tools.mip.model.PageTemplate;

@RestController
@EnableJms
public class MIController {

  
    private static Logger log = LogManager.getLogger(MIController.class);
    private static final CharSequence SWITCH_INJECT = "MessageSwitch";

    @Autowired
    CustomProperties customProperties;
    
    private Map<String, ConnectionDetails> connectionMap = new HashMap<>();
    private Map<String, ConnectionFactory> connectionFctryMap = new HashMap<>();
    PageTemplate pageValues = new PageTemplate();
    
    
    @PostMapping("mip/sendx")
    String sendxx(@RequestBody MessageReq msg){
        try{
            String type = msg.getType();
            if(!(type == null || type.isEmpty()) && type.contains(SWITCH_INJECT)) {
                injectByteMessage(msg);
            }
            else {
                injectTextMessage(msg);
            }
            
            return getReturnMessage("OK");
        }catch(JmsException ex){
            ex.printStackTrace();
            return getReturnMessage("FAIL");
        }
        catch(Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }
    
    private void injectTextMessage(MessageReq msg) throws Exception {
        log.info("Injecting TextMessage..");
        String queuename = "queue:///"+msg.getQueue()+"?targetClient=1";
        getTemplate(msg.getQm()).convertAndSend(queuename, msg.getBody(), new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws JMSException {
                message.clearProperties();
                try {
                    Map<String, String> headers = getHeaderMap(msg.getHeader());
                    if (headers != null) {
                        headers.forEach((x,y) -> {
                            try {
                                message.setStringProperty(x, y.toString());
                            } catch (JMSException e1) {
                                e1.printStackTrace();
                            }
                        });
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                
                return message;
            }});
        
        
    }
    
    private void injectByteMessage(MessageReq msg) throws Exception {
        log.info("Injecting ByteMessage..");
        String queuename = "queue:///"+msg.getQueue()+"?targetClient=1";
        String [] bmsg = ApisContent.getApiPartsFromString(msg.getBody());
        for(String ms : bmsg) {
            
            getTemplate(msg.getQm()).convertAndSend(queuename, ms.getBytes(), new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws JMSException {
                    message.clearProperties();
                    try {
                        Map<String, String> headers = getHeaderMap(msg.getHeader());
                        if (headers != null) {
                            headers.forEach((x,y) -> {
                                try {
                                    message.setStringProperty(x, y.toString());
                                } catch (JMSException e1) {
                                    e1.printStackTrace();
                                }
                            });
                        }
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    
                    return message;
                }});
        }
        
    }

    @GetMapping("miui/load")
    String initComponents() throws JsonProcessingException{
        String qm = new JsonMapper().writeValueAsString(pageValues);
        return qm;
        
    }
    
    private String getReturnMessage(String msg) {
        return new Date().toString() +" - " +msg;
    }


    @SuppressWarnings("unchecked")
    public Map<String,String> getHeaderMap(String header) throws JsonMappingException, JsonProcessingException {
        Map<String, String> response = null;
        if (!(header == null || header.isEmpty()))
        {
            response =  new ObjectMapper().readValue(header, HashMap.class);
        }
        return response;
    }
    
    
    
    
    JmsTemplate getTemplate(String qm) {
        
        return new JmsTemplate(getConnectionFactory(qm));
        
        
    }


    private ConnectionFactory getConnectionFactory(String qm) {
        
        return connectionFctryMap.get(qm);
    }
    
    @PostConstruct
    synchronized void init() throws JMSException {
        loadConnectionFactories();
    }


    private void loadConnectionFactories() throws JMSException {
        
        loadConnectionDetails();
        generateConnectionFactories();
        generateTemplate();
        log.info("Connection details loaded.!");
        
    }


    private void generateTemplate() {

        Map<String,List<String>> queues = new HashMap<>();
        connectionMap.forEach((x,y) -> queues.put(x,Stream.of(y.getQueues().split(",",-1)).collect(Collectors.toList())
                ));
        pageValues.setQmanagers(queues);
        /*for (Entry<String, ConnectionDetails> entry :connectionMap.entrySet() ) {
            queues.put(entry.getKey(),entry.getValue().getQueues());
        }*/
    }

    private void loadConnectionDetails() {
        String conkey = null;
        for (Entry<String, String> entry :customProperties.getConnections().entrySet() ) {
            try {
                if(entry.getKey().contains("mq.queueManager")) {
                    conkey = entry.getValue(); 
                    if(!connectionMap.containsKey(conkey)) {
                        ConnectionDetails con = new ConnectionDetails();
                        con.setQm(entry.getValue());;
                        connectionMap.put(conkey, con);
                    }else {
                        
                    }
                }else {
                  ConnectionDetails conn = connectionMap.get(conkey);
                  if(entry.getKey().contains("mq.connName")) {
                      conn.setConnName(entry.getValue());
                  }
                  else if(entry.getKey().contains("mq.channel")) {
                      conn.setChannel(entry.getValue());
                  }else if(entry.getKey().contains("mq.user")) {
                      conn.setUsername(entry.getValue());
                  }else if(entry.getKey().contains("mq.password")) {
                      conn.setPassword(entry.getValue());
                  }else if(entry.getKey().contains("mq.queues")) {
                      conn.setQueues(entry.getValue());
                  }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
       
    }


    private void generateConnectionFactories()  {
        
            try {
                log.info("Creating connection factories.");
                for ( Entry<String, ConnectionDetails> entry :connectionMap.entrySet() ) {
                    MQConnectionFactory connectionFactory = new MQConnectionFactory();
                    ConnectionDetails con = entry.getValue();
                    connectionFactory.setConnectionNameList(con.getConnName());
                    connectionFactory.setQueueManager(con.getQm());
                    connectionFactory.setChannel(con.getChannel());
                    connectionFactory.setTransportType(1);
                    connectionFactory.setBooleanProperty(JmsConstants.USER_AUTHENTICATION_MQCSP, false);
                    connectionFactory.setStringProperty(JmsConstants.PASSWORD,con.getPassword());
                    connectionFactory.setStringProperty(JmsConstants.USERID, con.getUsername());
                    connectionFctryMap.put(entry.getKey(), connectionFactory);
                    
                }
            } catch (Exception e) {
                
                e.printStackTrace();
            }
            
        }
}

