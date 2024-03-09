package pl.mwasyluk.ouroom_server.services.chat;

import java.util.Collection;
import java.util.UUID;

import lombok.NonNull;

import pl.mwasyluk.ouroom_server.dto.chat.ChatDetailsView;
import pl.mwasyluk.ouroom_server.dto.chat.ChatForm;
import pl.mwasyluk.ouroom_server.dto.chat.ChatPresentableView;

public interface ChatService {
    @NonNull Collection<ChatPresentableView> readAllWithPrincipal();
    @NonNull ChatPresentableView create(@NonNull ChatForm chatForm);
    @NonNull ChatDetailsView read(@NonNull UUID chatId);
    @NonNull ChatPresentableView update(@NonNull ChatForm chatForm);
    void delete(@NonNull UUID chatId);
}
