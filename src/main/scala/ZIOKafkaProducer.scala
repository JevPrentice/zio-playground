import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde
import zio.{ExitCode, URIO, ZIO, ZLayer}

import java.util.UUID

/**
 * @author Jev Prentice
 * @since 28 October 2021
 */
object ZIOKafkaProducer extends zio.App {

  import ZioKafka._

  private val producerSettings = ProducerSettings(List("localhost:9092"))

  private val producer = ZLayer.fromManaged(Producer.make[Any, UUID, Match](producerSettings, Serde.uuid, matchSerde))

  private val finalScore = Match(Array(
    MatchPlayer("ITA", 1),
    MatchPlayer("ENG", 2)
  ))

  private val record = new ProducerRecord[UUID, Match]("updates", UUID.randomUUID(), finalScore)

  private val producerEffect: ZIO[Producer[Any, UUID, Match], Throwable, RecordMetadata] = Producer.produce(record)

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    producerEffect.provideSomeLayer(producer).exitCode
}