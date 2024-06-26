/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.integrations.destination.gcs;

import com.amazonaws.services.s3.AmazonS3;
import io.airbyte.commons.json.Jsons;
import io.airbyte.integrations.base.AirbyteStreamNameNamespacePair;
import io.airbyte.integrations.base.FailureTrackingAirbyteMessageConsumer;
import io.airbyte.integrations.destination.gcs.writer.GcsWriterFactory;
import io.airbyte.integrations.destination.s3.writer.S3Writer;
import io.airbyte.protocol.models.AirbyteMessage;
import io.airbyte.protocol.models.AirbyteMessage.Type;
import io.airbyte.protocol.models.AirbyteRecordMessage;
import io.airbyte.protocol.models.AirbyteStream;
import io.airbyte.protocol.models.ConfiguredAirbyteCatalog;
import io.airbyte.protocol.models.ConfiguredAirbyteStream;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class GcsConsumer extends FailureTrackingAirbyteMessageConsumer {

  private final GcsDestinationConfig gcsDestinationConfig;
  private final ConfiguredAirbyteCatalog configuredCatalog;
  private final GcsWriterFactory writerFactory;
  private final Consumer<AirbyteMessage> outputRecordCollector;
  private final Map<AirbyteStreamNameNamespacePair, S3Writer> streamNameAndNamespaceToWriters;

  private AirbyteMessage lastStateMessage = null;

  public GcsConsumer(GcsDestinationConfig gcsDestinationConfig,
                     ConfiguredAirbyteCatalog configuredCatalog,
                     GcsWriterFactory writerFactory,
                     Consumer<AirbyteMessage> outputRecordCollector) {
    this.gcsDestinationConfig = gcsDestinationConfig;
    this.configuredCatalog = configuredCatalog;
    this.writerFactory = writerFactory;
    this.outputRecordCollector = outputRecordCollector;
    this.streamNameAndNamespaceToWriters = new HashMap<>(configuredCatalog.getStreams().size());
  }

  @Override
  protected void startTracked() throws Exception {
    AmazonS3 s3Client = GcsS3Helper.getGcsS3Client(gcsDestinationConfig);

    Timestamp uploadTimestamp = new Timestamp(System.currentTimeMillis());

    for (ConfiguredAirbyteStream configuredStream : configuredCatalog.getStreams()) {
      S3Writer writer = writerFactory
          .create(gcsDestinationConfig, s3Client, configuredStream, uploadTimestamp);
      writer.initialize();

      AirbyteStream stream = configuredStream.getStream();
      AirbyteStreamNameNamespacePair streamNamePair = AirbyteStreamNameNamespacePair
          .fromAirbyteSteam(stream);
      streamNameAndNamespaceToWriters.put(streamNamePair, writer);
    }
  }

  @Override
  protected void acceptTracked(AirbyteMessage airbyteMessage) throws Exception {
    if (airbyteMessage.getType() == Type.STATE) {
      this.lastStateMessage = airbyteMessage;
      return;
    } else if (airbyteMessage.getType() != Type.RECORD) {
      return;
    }

    AirbyteRecordMessage recordMessage = airbyteMessage.getRecord();
    AirbyteStreamNameNamespacePair pair = AirbyteStreamNameNamespacePair
        .fromRecordMessage(recordMessage);

    if (!streamNameAndNamespaceToWriters.containsKey(pair)) {
      throw new IllegalArgumentException(
          String.format(
              "Message contained record from a stream that was not in the catalog. \ncatalog: %s , \nmessage: %s",
              Jsons.serialize(configuredCatalog), Jsons.serialize(recordMessage)));
    }

    UUID id = UUID.randomUUID();
    streamNameAndNamespaceToWriters.get(pair).write(id, recordMessage);
  }

  @Override
  protected void close(boolean hasFailed) throws Exception {
    for (S3Writer handler : streamNameAndNamespaceToWriters.values()) {
      handler.close(hasFailed);
    }
    // Gcs stream uploader is all or nothing if a failure happens in the destination.
    if (!hasFailed) {
      outputRecordCollector.accept(lastStateMessage);
    }
  }

}
