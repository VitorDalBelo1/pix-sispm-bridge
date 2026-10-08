package br.gov.sispm.bridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * entrada (Kafka origem, posicional, 1..N registros por mensagem)
 *   -> parse -> saida (Kafka destino, 1 JSON por registro).
 *
 * A mensagem de origem so recebe ACK depois que TODOS os JSONs dela foram confirmados
 * pelo broker de destino. Se o layout for invalido (nada e publicado) ou se alguma
 * publicacao falhar, ela recebe NACK e vai para a DLQ do canal de entrada.
 */
@ApplicationScoped
public class ConversorProcessor {

    private static final Logger LOG = Logger.getLogger(ConversorProcessor.class);

    @Inject PosicionalParser parser;
    @Inject ObjectMapper mapper;

    @Incoming("entrada")
    @Outgoing("saida")
    public Multi<Message<String>> converter(Multi<Message<String>> entrada) {
        return entrada
                .onItem().transformToMulti(msg -> {
                    try {
                        List<PixTransacao> registros = parser.parseLote(msg.getPayload());
                        AtomicInteger pendentes = new AtomicInteger(registros.size());
                        AtomicBoolean falhou = new AtomicBoolean(false);

                        List<Message<String>> saidas = new ArrayList<>(registros.size());
                        for (PixTransacao t : registros) {
                            String json = mapper.writeValueAsString(t);
                            var meta = OutgoingKafkaRecordMetadata.<String>builder()
                                    .withKey(t.identificadorTransacao())
                                    .build();
                            saidas.add(Message.of(json, Metadata.of(meta),
                                    () -> (pendentes.decrementAndGet() == 0 && !falhou.get())
                                            ? msg.ack()
                                            : CompletableFuture.<Void>completedFuture(null),
                                    e -> falhou.compareAndSet(false, true)
                                            ? msg.nack(e)
                                            : CompletableFuture.<Void>completedFuture(null)));
                        }
                        LOG.debugf("Mensagem convertida em %d registro(s)", saidas.size());
                        return Multi.createFrom().iterable(saidas);
                    } catch (Exception e) {
                        LOG.errorf(e, "Falha ao converter mensagem, enviando para DLQ");
                        msg.nack(e);
                        return Multi.createFrom().<Message<String>>empty();
                    }
                })
                .concatenate();
    }
}
