package com.champions_Call.Helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

  private String content;

  private MessageType type = MessageType.blue;

  public String getContent() {
    return content;
}

public void setContent(String content) {
    this.content = content;
}

public MessageType getType() {
    return type;
}

public void setType(MessageType type) {
    this.type = type;
}

}
