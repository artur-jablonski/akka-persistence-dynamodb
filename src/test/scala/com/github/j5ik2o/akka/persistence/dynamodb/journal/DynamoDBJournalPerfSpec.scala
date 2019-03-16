package com.github.j5ik2o.akka.persistence.dynamodb.journal
import java.net.URI

import akka.persistence.CapabilityFlag
import akka.persistence.journal.JournalPerfSpec
import com.github.j5ik2o.akka.persistence.dynamodb.utils.DynamoDBSpecSupport
import com.github.j5ik2o.reactive.aws.dynamodb.DynamoDBAsyncClientV2
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

import scala.concurrent.duration._

class DynamoDBJournalPerfSpec
    extends JournalPerfSpec(ConfigFactory.load("default.conf"))
    with ScalaFutures
    with DynamoDBSpecSupport {
  override protected def supportsRejectingNonSerializableObjects: CapabilityFlag = false

  // override def awaitDurationMillis: Long = 1.minutes.toMillis
  override def eventsCount: Int = 10

  implicit val pc: PatienceConfig = PatienceConfig(1 minutes, 1 seconds)

  override protected lazy val dynamoDBPort: Int = 8000

  val underlying: DynamoDbAsyncClient = DynamoDbAsyncClient
    .builder()
    .credentialsProvider(
      StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey))
    )
    .endpointOverride(URI.create(dynamoDBEndpoint))
    .build()

  import scala.concurrent.ExecutionContext.Implicits.global

  override def asyncClient: DynamoDBAsyncClientV2 = DynamoDBAsyncClientV2(underlying)

  before { createTable }

  after { deleteTable }
}