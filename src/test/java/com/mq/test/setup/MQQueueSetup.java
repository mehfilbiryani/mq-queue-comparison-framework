package com.mq.test.setup;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.PCFException;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.pcf.PCFMessageAgent;
import com.mq.test.config.MQConnectionConfig;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Hashtable;
import java.util.UUID;

/**
 * Ensures two local queues exist, optionally clears them, and optionally seeds N identical messages
 * into both queues to enable 1:1 comparisons.
 *
 * Usage in @BeforeAll:
 *   MQQueueSetup.bootstrap(config, "QUEUE1", "QUEUE2", true, 10);
 */
public final class MQQueueSetup {

    private MQQueueSetup() {}

    // ---- PCF reason codes (avoid depending on CMQCFC to prevent symbol issues) ----
    private static final int RC_OBJECT_NOT_FOUND      = 3065; // MQRCCF_OBJECT_NOT_FOUND
    private static final int RC_OBJECT_ALREADY_EXISTS = 3061; // MQRCCF_OBJECT_ALREADY_EXISTS

    /**
     * Ensure queues exist, optionally clear them, and seed the same messages on both.
     */
    public static void bootstrap(MQConnectionConfig cfg,
                                 String queue1,
                                 String queue2,
                                 boolean clear,
                                 int seedCount) throws Exception {
        try (MQAdmin admin = MQAdmin.connect(cfg)) {
            admin.ensureLocalQueue(queue1, 5000);
            admin.ensureLocalQueue(queue2, 5000);

            if (clear) {
                admin.clearQueue(queue1);
                admin.clearQueue(queue2);
            }

            if (seedCount > 0) {
                for (int i = 1; i <= seedCount; i++) {
                    String id = UUID.randomUUID().toString();
                    String payloadJson = String.format(
                        "{\"index\":%d,\"id\":\"%s\",\"source\":\"seed\",\"ts\":\"%s\",\"payload\":\"Hello MQ %d\"}",
                        i, id, Instant.now(), i
                    );

                    MQMessage msg1 = MQAdmin.buildTextMessage(payloadJson, i, id);
                    MQMessage msg2 = MQAdmin.buildTextMessage(payloadJson, i, id);

                    admin.put(queue1, msg1);
                    admin.put(queue2, msg2);
                }
            }
        }
    }

    /**
     * Seed different payloads into the two queues (for negative/variance tests).
     */
    public static void seedDifferent(MQConnectionConfig cfg,
                                     String queue1, String payload1,
                                     String queue2, String payload2) throws Exception {
        try (MQAdmin admin = MQAdmin.connect(cfg)) {
            admin.ensureLocalQueue(queue1, 5000);
            admin.ensureLocalQueue(queue2, 5000);

            String c1 = UUID.randomUUID().toString();
            String c2 = UUID.randomUUID().toString();

            admin.put(queue1, MQAdmin.buildTextMessage(payload1, 5, c1));
            admin.put(queue2, MQAdmin.buildTextMessage(payload2, 5, c2));
        }
    }

    /** Internal admin/helper (PCF + basic put) */
    static final class MQAdmin implements AutoCloseable {
        private final MQQueueManager qmgr;
        private final PCFMessageAgent pcf;

        private MQAdmin(MQQueueManager qmgr) throws MQException {
            this.qmgr = qmgr;
            this.pcf = new PCFMessageAgent(qmgr);
        }

        static MQAdmin connect(MQConnectionConfig cfg) throws MQException {
            Hashtable<String, Object> props = new Hashtable<>();
            props.put(MQConstants.HOST_NAME_PROPERTY, cfg.getHost());
            props.put(MQConstants.PORT_PROPERTY, cfg.getPort());
            props.put(MQConstants.CHANNEL_PROPERTY, cfg.getChannel());
            props.put(MQConstants.TRANSPORT_PROPERTY, MQConstants.TRANSPORT_MQSERIES_CLIENT);

            if (cfg.getUsername() != null && !cfg.getUsername().isEmpty()) {
                props.put(MQConstants.USER_ID_PROPERTY, cfg.getUsername());
            }
            if (cfg.getPassword() != null && !cfg.getPassword().isEmpty()) {
                props.put(MQConstants.PASSWORD_PROPERTY, cfg.getPassword());
                props.put(MQConstants.USE_MQCSP_AUTHENTICATION_PROPERTY, true);
            }

            MQQueueManager qm = new MQQueueManager(cfg.getQueueManager(), props);
            return new MQAdmin(qm);
        }

        void ensureLocalQueue(String qName, int maxDepth) throws Exception {
            if (!existsQueue(qName)) {
                createLocalQueue(qName, maxDepth);
            }
        }

        boolean existsQueue(String qName) throws Exception {
            PCFMessage inq = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q);
            inq.addParameter(MQConstants.MQCA_Q_NAME, qName);
            inq.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_LOCAL);
            try {
                pcf.send(inq);
                return true;
            } catch (PCFException pcfEx) {
                if (pcfEx.getReason() == RC_OBJECT_NOT_FOUND) {
                    return false;
                }
                throw pcfEx;
            } catch (MQException mqe) {
                if (mqe.reasonCode == MQConstants.MQRC_UNKNOWN_OBJECT_NAME) {
                    return false;
                }
                throw mqe;
            }
        }

        void createLocalQueue(String qName, int maxDepth) throws Exception {
            PCFMessage cmd = new PCFMessage(MQConstants.MQCMD_CREATE_Q);
            cmd.addParameter(MQConstants.MQCA_Q_NAME, qName);
            cmd.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_LOCAL);
            cmd.addParameter(MQConstants.MQIA_MAX_Q_DEPTH, maxDepth);
            try {
                pcf.send(cmd);
            } catch (PCFException pcfEx) {
                if (pcfEx.getReason() == RC_OBJECT_ALREADY_EXISTS) {
                    return; // OK
                }
                throw pcfEx;
            }
        }

        void clearQueue(String qName) throws Exception {
            PCFMessage cmd = new PCFMessage(MQConstants.MQCMD_CLEAR_Q);
            cmd.addParameter(MQConstants.MQCA_Q_NAME, qName);
            pcf.send(cmd);
        }

        void put(String qName, MQMessage msg) throws Exception {
            MQQueue q = qmgr.accessQueue(qName, MQConstants.MQOO_OUTPUT);
            try {
                q.put(msg, new MQPutMessageOptions());
            } finally {
                q.close();
            }
        }

        /** Builds a STRING message with priority + correlationId + a few custom properties */
        static MQMessage buildTextMessage(String text, int priority, String correlIdUtf8) throws Exception {
            MQMessage m = new MQMessage();

            // MQMD
            m.format = MQConstants.MQFMT_STRING;
            m.messageFlags = MQConstants.MQMF_NONE;
            m.messageId = MQConstants.MQMI_NONE;

            // Priority (0-9 typical)
            m.priority = Math.max(0, Math.min(9, priority % 10));

            // 24-byte CorrelId
            byte[] cid = new byte[MQConstants.MQ_CORREL_ID_LENGTH];
            byte[] src = correlIdUtf8.getBytes(StandardCharsets.UTF_8);
            System.arraycopy(src, 0, cid, 0, Math.min(src.length, cid.length));
            m.correlationId = cid;

            // Custom properties (visible to property-aware consumers)
            m.setStringProperty("app", "mq-seed");
            m.setStringProperty("env", "local");
            m.setIntProperty("index", priority);

            // Payload
            m.writeString(text);
            return m;
        }

        @Override
        public void close() {
            try { if (pcf != null) pcf.disconnect(); } catch (Exception ignored) {}
            try { if (qmgr != null) qmgr.disconnect(); } catch (Exception ignored) {}
        }
    }
}
