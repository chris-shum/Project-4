package com.showme.android.finalproject.ChatRoomActivityStuff;

/**
 * @author greg
 * @since 6/21/13
 */
public class Chat {

    private String message;
    private String author;
//    private String time;

    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    private Chat() {
    }

    public Chat(String message, String author) {
        this.message = message;
        this.author = author;
//        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }

//    public String getTime() {
//        return time;
////    }
}
