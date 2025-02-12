package com.cloudComputing.collaborativeNotes.services;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import java.util.HashSet;
import java.util.Set;

@Component
public class NoteUpdateSubscriber implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(NoteUpdateSubscriber.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NoteService noteService;

    @Value("${server.instance.id}") // Cada instancia debe tener un ID único
    private String instanceId;

    private final Set<String> processedMessages = new HashSet<>(); // Para evitar duplicados

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageBody = new String(message.getBody());
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> messageData = objectMapper.readValue(messageBody, Map.class);

            Long noteId = ((Number) messageData.get("noteId")).longValue();
            Long userId = ((Number) messageData.get("userId")).longValue();
            String diff = (String) messageData.get("diff");
            String senderInstanceId = (String) messageData.get("instanceId");
            String messageId = (String) messageData.get("messageId");

            logger.info("🔵 Recibido en Redis Pub/Sub para noteId {} por userId {} desde instancia {}", noteId, userId, senderInstanceId);

            // 🚨 FILTRO: Si el mensaje proviene de la misma instancia, lo ignoramos
            if (senderInstanceId != null && senderInstanceId.equals(instanceId)) {
                logger.info("🛑 Mensaje ignorado porque proviene de la misma instancia.");
                return;
            }

            // 🚨 FILTRO: Si el mensaje ya fue procesado, lo ignoramos
            if (processedMessages.contains(messageId)) {
                logger.info("🛑 Mensaje duplicado ignorado (messageId: {})", messageId);
                return;
            }

            // ✅ Marcar mensaje como procesado
            processedMessages.add(messageId);

            // ✅ Obtener la nota y aplicar el diff
            Note note = noteService.getNoteWithCachedDiffs(noteId);
            String updatedContent = note.getContent();

            LinkedList<DiffMatchPatch.Patch> patches = (LinkedList<DiffMatchPatch.Patch>) noteService.dmp.patchFromText(diff);
            Object[] result = noteService.dmp.patchApply(patches, updatedContent);
            updatedContent = (String) result[0];

            logger.info("🟢 Contenido actualizado después de aplicar el diff en la instancia remota: {}", updatedContent);

            // ✅ Enviar solo el `diff` a WebSockets
            Map<String, Object> webSocketMessage = new HashMap<>();
            webSocketMessage.put("noteId", noteId);
            webSocketMessage.put("userId", userId);
            webSocketMessage.put("diff", diff);

            messagingTemplate.convertAndSend("/topic/notes/" + noteId, webSocketMessage);
            logger.info("🟢 Enviado a WebSockets para noteId {} con solo el diff", noteId);

        } catch (Exception e) {
            logger.error("❌ Error al procesar la actualización desde Redis: {}", e.getMessage());
        }
    }
}
