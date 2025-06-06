package com.bfsforum.messageservice.controller;

import com.bfsforum.messageservice.domain.Message;
import com.bfsforum.messageservice.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
@Slf4j
public class MessageController {
  private final MessageService messageService;
  public MessageController(MessageService messageService) {
    this.messageService = messageService;
  }

  /** send message to admin, VISITOR allowed
   * @param message
   * @return void
   * */
  @PostMapping("/")
  public void createMessage(@RequestBody Message message) {
    log.info("create message");
  }

  @GetMapping("/{id}")
  public void getMessage(@PathVariable String id) {
    log.info("get message");
  }
}
