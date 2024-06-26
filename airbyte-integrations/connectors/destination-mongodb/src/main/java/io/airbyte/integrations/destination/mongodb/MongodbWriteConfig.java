/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.integrations.destination.mongodb;

import com.mongodb.client.MongoCollection;
import io.airbyte.protocol.models.DestinationSyncMode;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.bson.Document;

class MongodbWriteConfig {

  private final String collectionName;
  private final String tmpCollectionName;
  private final DestinationSyncMode syncMode;
  private final MongoCollection<Document> collection;
  private final Set<String> documentsHash = new HashSet<>();

  MongodbWriteConfig(String collectionName,
                     String tmpCollectionName,
                     DestinationSyncMode syncMode,
                     MongoCollection<Document> collection,
                     Collection<String> documentsHash) {
    this.collectionName = collectionName;
    this.tmpCollectionName = tmpCollectionName;
    this.syncMode = syncMode;
    this.collection = collection;
    this.documentsHash.addAll(documentsHash);
  }

  public String getCollectionName() {
    return collectionName;
  }

  public String getTmpCollectionName() {
    return tmpCollectionName;
  }

  public DestinationSyncMode getSyncMode() {
    return syncMode;
  }

  public MongoCollection<Document> getCollection() {
    return collection;
  }

  public Set<String> getDocumentsHash() {
    return documentsHash;
  }

}
