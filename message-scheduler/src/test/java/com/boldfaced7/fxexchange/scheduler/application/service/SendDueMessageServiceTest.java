package com.boldfaced7.fxexchange.scheduler.application.service;

import com.boldfaced7.fxexchange.scheduler.application.port.in.SendDueMessageCommand;
import com.boldfaced7.fxexchange.scheduler.application.port.out.DeleteScheduledMessagePort;
import com.boldfaced7.fxexchange.scheduler.application.port.out.LoadScheduledMessagePort;
import com.boldfaced7.fxexchange.scheduler.application.port.out.SendScheduledMessagePort;
import com.boldfaced7.fxexchange.scheduler.domain.model.ScheduledMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendDueMessageServiceTest {

    @InjectMocks
    private SendDueMessageService sendDueMessageService;

    @Mock private LoadScheduledMessagePort loadScheduledMessagePort;
    @Mock private SendScheduledMessagePort sendScheduledMessagePort;
    @Mock private DeleteScheduledMessagePort deleteScheduledMessagePort;

    private SendDueMessageCommand command;
    private Set<ScheduledMessage> dueMessages;

    @BeforeEach
    void setUp() {
        command = new SendDueMessageCommand(System.currentTimeMillis());
        dueMessages = Set.of(
                new ScheduledMessage("message1",  System.currentTimeMillis(), "payload1"),
                new ScheduledMessage("message2", System.currentTimeMillis(), "payload2")
        );
    }

    @Test
    @DisplayName("만료된 메시지를 로드하고 전송한 후 삭제한다")
    void sendDueScheduledMessages() {
        // given
        when(loadScheduledMessagePort.loadDueMessages(command.currentTimeMillis()))
                .thenReturn(dueMessages);

        // when
        sendDueMessageService.sendDueScheduledMessages(command);

        // then
        verify(loadScheduledMessagePort).loadDueMessages(command.currentTimeMillis());
        verify(sendScheduledMessagePort).sendScheduledMessages(dueMessages);
        verify(deleteScheduledMessagePort).deleteAll(dueMessages);
    }

    @Test
    @DisplayName("만료된 메시지가 없으면 아무 작업도 하지 않는다")
    void sendDueScheduledMessages_NoDueMessages() {
        // given
        when(loadScheduledMessagePort.loadDueMessages(command.currentTimeMillis()))
                .thenReturn(Set.of());

        // when
        sendDueMessageService.sendDueScheduledMessages(command);

        // then
        verify(loadScheduledMessagePort).loadDueMessages(command.currentTimeMillis());
        verify(sendScheduledMessagePort).sendScheduledMessages(Set.of());
        verify(deleteScheduledMessagePort).deleteAll(Set.of());
    }

    @Test
    @DisplayName("메시지 처리 순서가 올바르다")
    void sendDueScheduledMessages_Order() {
        // given
        when(loadScheduledMessagePort.loadDueMessages(command.currentTimeMillis()))
                .thenReturn(dueMessages);

        // when
        sendDueMessageService.sendDueScheduledMessages(command);

        // then
        var inOrder = inOrder(loadScheduledMessagePort, sendScheduledMessagePort, deleteScheduledMessagePort);
        inOrder.verify(loadScheduledMessagePort).loadDueMessages(command.currentTimeMillis());
        inOrder.verify(sendScheduledMessagePort).sendScheduledMessages(dueMessages);
        inOrder.verify(deleteScheduledMessagePort).deleteAll(dueMessages);
        inOrder.verifyNoMoreInteractions();
    }
} 