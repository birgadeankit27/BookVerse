package com.bookverser.BookVerse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
	
	 private Long id;
	    private Long senderId;
	    private Long receiverId;
	    private String content;
	    private String timestamp;

}
