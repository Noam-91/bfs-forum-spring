package com.bfsforum.messageservice.service;

import com.bfsforum.messageservice.repository.MessageDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@Slf4j
public class MessageService {
  private final MessageDao messageDao;
  public MessageService(MessageDao messageDao) {
    this.messageDao = messageDao;
  }


}
