# pix-sispm-bridge

Quarkus 3 / JDK 21. Consome do Kafka de **origem** mensagens no layout posicional SISPM (487 posicoes),
converte para JSON e publica no Kafka de **destino**. Mensagens invalidas vao para uma DLQ no broker de origem.

## Rodar
    docker compose up -d
    # criar topicos (se auto-create estiver desligado)
    mvn quarkus:dev

## Testar
    # produzir uma linha do mock no broker de origem
    head -1 src/test/resources/mock_pix_estatico_01_comercio.txt | \
      docker compose exec -T kafka-origem /opt/kafka/bin/kafka-console-producer.sh \
      --bootstrap-server localhost:9092 --topic sispm.posicional.entrada
    # consumir o JSON no broker de destino
    docker compose exec kafka-destino /opt/kafka/bin/kafka-console-consumer.sh \
      --bootstrap-server localhost:9093 --topic sispm.json.saida --from-beginning

## Variaveis
KAFKA_ORIGEM_BOOTSTRAP, KAFKA_ORIGEM_TOPICO, KAFKA_ORIGEM_GROUP, KAFKA_ORIGEM_DLQ,
KAFKA_DESTINO_BOOTSTRAP, KAFKA_DESTINO_TOPICO
