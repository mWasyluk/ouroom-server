package pl.wasyluva.spring_messengerapi.data.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.wasyluva.spring_messengerapi.data.repository.ConversationRepository;
import pl.wasyluva.spring_messengerapi.data.repository.MessageRepository;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;
import pl.wasyluva.spring_messengerapi.domain.message.Conversation;
import pl.wasyluva.spring_messengerapi.domain.message.Message;
import pl.wasyluva.spring_messengerapi.util.UuidUtils;

import java.util.Optional;
import java.util.UUID;

import static pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponseMessages.*;

@RequiredArgsConstructor

@Service
@Slf4j
public class MessageService {
    private final MessageRepository messageRepository;
//    private final ProfileRepository profileRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;

    public ServiceResponse<?> getAllPersistedMessages(){
        return new ServiceResponse<>(
                messageRepository.findAll(),
                HttpStatus.OK);
    }

    public ServiceResponse<?> updateMessage(@NonNull UUID requestingProfileId, @NonNull Message updatedMessage){
        if (updatedMessage.getId() == null){
            log.debug("Message provided as updated has to have an ID");

            return new ServiceResponse<>(
                    ID_REQUIRED,
                    HttpStatus.BAD_REQUEST);
        }

        Optional<Message> optionalPersistedMessage = messageRepository.findById(updatedMessage.getId());

        if (!optionalPersistedMessage.isPresent()) {
            log.debug("Message with ID " + updatedMessage.getId() + " does not exist");

            return new ServiceResponse<>(
                    EXISTING_ID_REQUIRED,
                    HttpStatus.NOT_FOUND);
        }

        Message persistedMessage = optionalPersistedMessage.get();

        if (!persistedMessage.getSourceUserId().equals(requestingProfileId)){
            log.debug("Requesting User does not have permission to update the message");

            return new ServiceResponse<>(
                    UNAUTHORIZED,
                    HttpStatus.UNAUTHORIZED);
        }

        if (updatedMessage.getContent() != null)
            persistedMessage.setContent(updatedMessage.getContent());
        if (updatedMessage.getSentDate() != null && persistedMessage.getSentDate() == null)
            persistedMessage.setSentDate(updatedMessage.getSentDate());
        if (updatedMessage.getDeliveryDate() != null && persistedMessage.getDeliveryDate() == null)
            persistedMessage.setDeliveryDate(updatedMessage.getDeliveryDate());
        if (updatedMessage.getReadDate() != null && persistedMessage.getReadDate() == null)
            persistedMessage.setReadDate(updatedMessage.getReadDate());

        Message updatedPersistedMessage = messageRepository.save(persistedMessage);
        log.debug("Message updated");

        return new ServiceResponse<>(
                updatedPersistedMessage,
                HttpStatus.OK);
    }

    public ServiceResponse<?> deleteMessage(UUID requestingProfileUuid, UUID messageUuid){
        Optional<Message> byId = messageRepository.findById(messageUuid);
        if (!byId.isPresent()){
            return ServiceResponse.INCORRECT_ID;
        }

        Message message = byId.get();

        if (!message.getSourceUserId().equals(requestingProfileUuid)){
            return ServiceResponse.UNAUTHORIZED;
        }

        Optional<Conversation> conversationByMessage = conversationRepository.findById(message.getConversation().getId());
        if (!conversationByMessage.isPresent()){
            return ServiceResponse.INCORRECT_ID;
        }

        Conversation conversation = conversationByMessage.get();
        if (!conversation.removeMessageById(messageUuid)){
            return ServiceResponse.INCORRECT_ID;
        }
        conversationRepository.save(conversation);
        messageRepository.deleteById(messageUuid);
        return ServiceResponse.OK;
    }

    public ServiceResponse<?> deleteMessage(UUID requestingUserId, String messageUuid){
        if (!UuidUtils.isStringCorrectUuid(messageUuid)){
            return ServiceResponse.INCORRECT_ID;
        }
        return deleteMessage(requestingUserId, UUID.fromString(messageUuid));

    }

    public ServiceResponse<?> getAllMessagesByConversationId(UUID requestingProfileId, UUID conversationId, Pageable pageable) {
        ServiceResponse<?> byId = conversationService.getById(requestingProfileId, conversationId);

        if (!(byId.getBody() instanceof Conversation)){
            return byId;
        }

        return new ServiceResponse<>(
                ((Conversation)byId.getBody()).getMessages(),
                HttpStatus.OK);
    }

    public ServiceResponse<?> getAllMessagesByConversationId(UUID requestingProfileId, String conversationStringUuid, Pageable pageable) {
        if (!UuidUtils.isStringCorrectUuid(conversationStringUuid)){
            return ServiceResponse.INCORRECT_ID;
        }
        return getAllMessagesByConversationId(requestingProfileId, UUID.fromString(conversationStringUuid), pageable);
    }

//    public ServiceResponse<?> getLatestMessageWithLatestContacts(@NonNull UUID forProfileId, int howManyProfiles, int profileOffset){
//        if (howManyProfiles < 1 || profileOffset < 1){
//            log.debug("Profile range is incorrect");
//            return new ServiceResponse<>(
//                    Arrays.asList(howManyProfiles, profileOffset),
//                    HttpStatus.BAD_REQUEST,
//                    INCORRECT_RANGE);
//        }
//
//        Optional<Profile> byId = profileRepository.findById(forProfileId);
//        if (!byId.isPresent()){
//            log.debug("User with ID " + forProfileId + " does not exist");
//
//            return new ServiceResponse<>(
//                    forProfileId,
//                    HttpStatus.BAD_REQUEST,
//                    TARGET_USER_DOES_NOT_EXIST);
//        }
//
////        return new ServiceResponse<>(
////                forProfileId,
////                HttpStatus.BAD_REQUEST,
////                TARGET_USER_DOES_NOT_EXIST);
////        return messageRepository.findAllByProfileId(forProfileId);
//    }
}
