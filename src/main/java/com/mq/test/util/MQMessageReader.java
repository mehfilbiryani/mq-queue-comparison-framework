package com.mq.test.util;

import com.ibm.mq.*;
import com.ibm.mq.constants.MQConstants;
import com.mq.test.config.MQConnectionConfig;
import com.mq.test.model.MQMessage;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Utility class to read messages from IBM MQ queues
 */
public class MQMessageReader {
    
    /**
     * Reads messages from an IBM MQ queue
     * 
     * @param config MQ connection configuration
     * @param queueName Name of the queue to read from
     * @param maxMessages Maximum number of messages to read
     * @param browse If true, browse messages without removing them
     * @return List of MQMessage objects
     * @throws Exception if connection or read fails
     */
    public static List<MQMessage> readMessages(MQConnectionConfig config, String queueName, 
                                               int maxMessages, boolean browse) throws Exception {
        List<MQMessage> messages = new ArrayList<>();
        MQQueueManager qMgr = null;
        MQQueue queue = null;
        
        try {
            Hashtable<String, Object> props = new Hashtable<>();
            props.put(MQConstants.HOST_NAME_PROPERTY, config.getHost());
            props.put(MQConstants.PORT_PROPERTY, config.getPort());
            props.put(MQConstants.CHANNEL_PROPERTY, config.getChannel());
            props.put(MQConstants.USER_ID_PROPERTY, config.getUsername());
            props.put(MQConstants.PASSWORD_PROPERTY, config.getPassword());
            
            qMgr = new MQQueueManager(config.getQueueManager(), props);
            
            int openOptions = browse ? 
                MQConstants.MQOO_BROWSE | MQConstants.MQOO_INQUIRE :
                MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_INQUIRE;
            
            queue = qMgr.accessQueue(queueName, openOptions);
            
            MQGetMessageOptions gmo = new MQGetMessageOptions();
            gmo.options = browse ? 
                MQConstants.MQGMO_BROWSE_FIRST | MQConstants.MQGMO_NO_WAIT :
                MQConstants.MQGMO_NO_WAIT;
            
            for (int i = 0; i < maxMessages; i++) {
                try {
                    com.ibm.mq.MQMessage mqMsg = new com.ibm.mq.MQMessage();
                    queue.get(mqMsg, gmo);
                    
                    if (browse && i > 0) {
                        gmo.options = MQConstants.MQGMO_BROWSE_NEXT | MQConstants.MQGMO_NO_WAIT;
                    }
                    
                    MQMessage msg = convertMQMessage(mqMsg);
                    messages.add(msg);
                } catch (MQException mqe) {
                    if (mqe.reasonCode == MQConstants.MQRC_NO_MSG_AVAILABLE) {
                        break;
                    }
                    throw mqe;
                }
            }
        } finally {
            if (queue != null) {
                try {
                    queue.close();
                } catch (Exception e) {
                    // Log but don't throw
                }
            }
            if (qMgr != null) {
                try {
                    qMgr.disconnect();
                } catch (Exception e) {
                    // Log but don't throw
                }
            }
        }
        
        return messages;
    }
    
    private static MQMessage convertMQMessage(com.ibm.mq.MQMessage mqMsg) throws Exception {
        MQMessage msg = new MQMessage();
        
        msg.setMessageId(bytesToHex(mqMsg.messageId));
        msg.setCorrelationId(bytesToHex(mqMsg.correlationId));
        msg.setPriority(mqMsg.priority);
        msg.setFormat(mqMsg.format);
        msg.setTimestamp(mqMsg.putDateTime.getTimeInMillis());
        
        int dataLength = mqMsg.getDataLength();
        byte[] buffer = new byte[dataLength];
        mqMsg.readFully(buffer);
        msg.setPayload(new String(buffer, "UTF-8"));
        
        return msg;
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}