package br.com.minimadev.starlove.Objects;


public class MatchesObject {
    private String  lastMessage,
                    chatId;

    UserObject mUser;

    public MatchesObject(UserObject mUser, String chatId, String lastMessage){
        this.mUser = mUser;
        this.lastMessage = lastMessage;
        this.chatId = chatId;
    }

    public UserObject getUser() {
        return mUser;
    }

    public String getLastMessage(){
        return lastMessage;
    }
    public String getChatId(){
        return chatId;
    }


    public void setLastMessage(String lastMessage){
        this.lastMessage = lastMessage;
    }
    public void setChatId(String chatId){
        this.chatId = chatId;
    }
}
