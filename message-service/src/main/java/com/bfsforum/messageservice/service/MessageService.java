package com.bfsforum.messageservice.service;

import com.bfsforum.messageservice.domain.Message;
import com.bfsforum.messageservice.domain.Status;
import com.bfsforum.messageservice.exception.BadRequestException;
import com.bfsforum.messageservice.exception.NotAuthorizedException;
import com.bfsforum.messageservice.exception.NotFoundException;
import com.bfsforum.messageservice.repository.MessageDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

  /** Create a message.
   * @param message message
   * */
  public void createMessage(Message message) {
    messageDao.save(message);
  }

  /** Get all messages in page;
   * Sort order: UNSOLVED -> updated_at -> created_at;
   * Only admins can get all messages
   * @param page page number
   * @param size page size
   * @param userRole user role
   * @return list of messages
   * */
  @Transactional(readOnly = true)
  public Page<Message> getAllMessages(int page, int size, String userRole) {
    if (!userRole.equals("ADMIN") && !userRole.equals("SUPER_ADMIN")) {
      throw new NotAuthorizedException("User is not authorized to get all messages");
    }
    // sort order: UNSOLVED -> updated_at -> created_at
    Sort sort = Sort.by(
        Sort.Order.asc("status"),
        Sort.Order.desc("updatedAt"),
        Sort.Order.desc("createdAt")
    );
    Pageable pageable = PageRequest.of(page, size, sort);
    return messageDao.findAll(pageable);
  }

  /** Solve a message. Only admins can solve messages.
   * @param id message id
   * @param userId user id
   * @param userRole user role
   * */
  public void solveMessage(String id, String userId, String userRole) {
    Message message = messageDao.findById(id)
        .orElseThrow(() -> new NotFoundException("Message not found"));
    if(message.getStatus().equals(Status.SOLVED)){
      throw new BadRequestException("Message has been solved");
    }
    if (!userRole.equals("ADMIN") && !userRole.equals("SUPER_ADMIN")) {
      throw new NotAuthorizedException("Only Admins can solve messages");
    }
    message.setStatus(Status.SOLVED);
    message.setUpdatedBy(userId);
    messageDao.save(message);
  }

  /** Get a message by id. Only admins can get messages.
   * @param id message id
   * @param userRole user role
   * @return message
   * */
  @Transactional(readOnly = true)
  public Message getMessageById(String id, String userRole) {
    if (!userRole.equals("ADMIN") && !userRole.equals("SUPER_ADMIN")) {
      throw new NotAuthorizedException("Only Admins can get messages");
    }
    return messageDao.findById(id)
        .orElseThrow(() -> new NotFoundException("Message not found"));
  }
}
