import zio._
import zio.json._
import zio.kafka.consumer._
import zio.kafka.serde.Serde
import zio.stream._

/**
 * @author Jev Prentice
 * @since 28 October 2021
 */
object ZioKafka extends zio.App {

  case class MatchPlayer(name: String, score: Int) {
    override def toString: String = s"$name: $score"
  }

  object MatchPlayer {
    implicit val encoder: JsonEncoder[MatchPlayer] = DeriveJsonEncoder.gen[MatchPlayer]
    implicit val decoder: JsonDecoder[MatchPlayer] = DeriveJsonDecoder.gen[MatchPlayer]
  }

  case class Match(players: Array[MatchPlayer]) {
    def score: String = s"${players(0)} - ${players(1)}"
  }

  object Match {
    implicit val encoder: JsonEncoder[Match] = DeriveJsonEncoder.gen[Match]
    implicit val decoder: JsonDecoder[Match] = DeriveJsonDecoder.gen[Match]
  }

  val matchSerde: Serde[Any, Match] = Serde.string.inmapM { string =>
    // deserialization
    ZIO.fromEither(string.fromJson[Match].left.map(errorMessage => new RuntimeException(errorMessage)))
  } { theMatch =>
    // serialization
    ZIO.effect(theMatch.toJson)
  }

  private val consumerSettings = ConsumerSettings(List("localhost:9092"))
    .withGroupId("update-consumer")

  private val managedConsumer
  = Consumer.make(consumerSettings) // effectful resource

  private val consumer =
    ZLayer.fromManaged(managedConsumer)

  private val matchesStreams =
    Consumer.subscribeAnd(Subscription.topics("updates"))
      .plainStream(Serde.uuid, matchSerde)
      .map(cr => (cr.value.score, cr.offset))
      .tap { case (score, _) => console.putStrLn(s"| $score |") }
      .map { case (_, offset) => offset }
      .aggregateAsync(Consumer.offsetBatches)
      .run(ZSink.foreach(_.commit))

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    matchesStreams.provideSomeLayer(consumer ++ zio.console.Console.live).exitCode
  }
}