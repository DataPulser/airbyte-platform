/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.integrations.destination.dynamodb;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.*;
import io.airbyte.integrations.base.JavaBaseConstants;
import java.util.Arrays;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamodbChecker {

  private static final Logger LOGGER = LoggerFactory.getLogger(DynamodbChecker.class);

  public static void attemptDynamodbWriteAndDelete(DynamodbDestinationConfig dynamodbDestinationConfig) throws Exception {
    var prefix = dynamodbDestinationConfig.getTableName();
    final String outputTableName = prefix + "_airbyte_connection_test_" + UUID.randomUUID().toString().replaceAll("-", "");
    attemptWriteAndDeleteDynamodbItem(dynamodbDestinationConfig, outputTableName);
  }

  private static void attemptWriteAndDeleteDynamodbItem(DynamodbDestinationConfig dynamodbDestinationConfig, String outputTableName)
      throws Exception {
    DynamoDB dynamoDB = new DynamoDB(getAmazonDynamoDB(dynamodbDestinationConfig));
    Table table = dynamoDB.createTable(outputTableName, // create table
        Arrays.asList(new KeySchemaElement(JavaBaseConstants.COLUMN_NAME_AB_ID, KeyType.HASH), new KeySchemaElement("sync_time", KeyType.RANGE)),
        Arrays.asList(new AttributeDefinition(JavaBaseConstants.COLUMN_NAME_AB_ID, ScalarAttributeType.S),
            new AttributeDefinition("sync_time", ScalarAttributeType.N)),
        new ProvisionedThroughput(1L, 1L));
    table.waitForActive();

    try {
      PutItemOutcome outcome = table
          .putItem(
              new Item().withPrimaryKey(JavaBaseConstants.COLUMN_NAME_AB_ID, UUID.randomUUID().toString(), "sync_time", System.currentTimeMillis()));
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
    }

    table.delete(); // delete table
    table.waitForDelete();
  }

  public static AmazonDynamoDB getAmazonDynamoDB(DynamodbDestinationConfig dynamodbDestinationConfig) {
    var endpoint = dynamodbDestinationConfig.getEndpoint();
    var region = dynamodbDestinationConfig.getRegion();
    var accessKeyId = dynamodbDestinationConfig.getAccessKeyId();
    var secretAccessKey = dynamodbDestinationConfig.getSecretAccessKey();

    var awsCreds = new BasicAWSCredentials(accessKeyId, secretAccessKey);

    if (endpoint.isEmpty()) {
      return AmazonDynamoDBClientBuilder.standard()
          .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
          .withRegion(dynamodbDestinationConfig.getRegion())
          .build();

    } else {
      ClientConfiguration clientConfiguration = new ClientConfiguration();
      clientConfiguration.setSignerOverride("AWSDynamodbSignerType");

      return AmazonDynamoDBClientBuilder
          .standard()
          .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
          .withClientConfiguration(clientConfiguration)
          .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
          .build();
    }
  }

}
