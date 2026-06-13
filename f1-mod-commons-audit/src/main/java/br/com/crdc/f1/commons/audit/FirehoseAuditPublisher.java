package br.com.crdc.f1.commons.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.firehose.FirehoseAsyncClient;
import software.amazon.awssdk.services.firehose.model.PutRecordRequest;
import software.amazon.awssdk.services.firehose.model.Record;

public class FirehoseAuditPublisher implements AuditPublisher {
    private static final Logger log = LoggerFactory.getLogger(FirehoseAuditPublisher.class);
    private final FirehoseAsyncClient firehose;
    private final ObjectMapper objectMapper;
    private final String streamName;

    public FirehoseAuditPublisher(FirehoseAsyncClient firehose, ObjectMapper objectMapper, String streamName) {
        this.firehose = Objects.requireNonNull(firehose, "firehose");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.streamName = Objects.requireNonNull(streamName, "streamName");
    }

    @Override
    public void publish(AuditEvent event) {
        Objects.requireNonNull(event, "event");
        byte[] payload;
        try {
            String json = objectMapper.writeValueAsString(event) + "\n";
            payload = json.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException ex) {
            log.warn("failed to serialize audit event id={} type={} — DROPPED: {}", event.eventId(), event.eventType(), ex.getMessage());
            return;
        }
        Record record = Record.builder().data(SdkBytes.fromByteArray(payload)).build();
        PutRecordRequest req = PutRecordRequest.builder().deliveryStreamName(streamName).record(record).build();
        firehose.putRecord(req).whenComplete((resp, err) -> {
            if (err != null) log.warn("firehose putRecord failed for audit event id={} type={}: {}", event.eventId(), event.eventType(), err.getMessage());
            else if (log.isDebugEnabled()) log.debug("audit event published id={} type={} recordId={}", event.eventId(), event.eventType(), resp.recordId());
        });
    }
}
