package com.parcial.notifiers.application.port.out;

import com.parcial.notifiers.domain.model.EmailDestinationModel;

public interface EmailSenderPort {
  void send(EmailDestinationModel destination);
}
