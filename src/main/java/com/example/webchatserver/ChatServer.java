package com.example.webchatserver;


import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * This class represents a web socket server, a new connection is created and it receives a roomID as a parameter
 * **/
@ServerEndpoint(value="/ws/{roomID}")
public class ChatServer {

    // contains a static List of ChatRoom used to control the existing rooms and their users
    private static List<ChatRoom> roomList = new ArrayList<>();

    // you may add other attributes as you see fit



    @OnOpen
    public void open(@PathParam("roomID") String roomID, Session session) throws IOException, EncodeException {

        session.getBasicRemote().sendText("First sample message to the client");
//        accessing the roomID parameter
        System.out.println(roomID);
        boolean flag = false; // used for checking if room exists
        for (ChatRoom room : roomList){
            //room found
            if(room.getCode() == roomID){
                flag = true;
                room.setUserName(session.getId(), null);
            }
        }
        if(!flag) {
            roomList.add(new ChatRoom(roomID, session.getId()));
        }
        session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server ): Welcome to the chat room. Please state your username to begin.\"}");






    }

    @OnClose
    public void close(Session session) throws IOException, EncodeException {
        String userId = session.getId();
        // do things for when the connection closes

        // search through room
        for (ChatRoom room : roomList){
            //check for user in room

            if (room.getUsers().containsKey(userId)){
                String roomId = room.getCode();
                String username = room.getUsers().get(userId);

                //remove user form room
                room.removeUser(userId);
            }
        }




    }

    @OnMessage
    public void handleMessage(String comm, Session session) throws IOException, EncodeException {
//        example getting unique userID that sent this message
        String userId = session.getId();

//        Example conversion of json messages from the client
        //        JSONObject jsonmsg = new JSONObject(comm);
//        String val1 = (String) jsonmsg.get("attribute1");
//        String val2 = (String) jsonmsg.get("attribute2");

        // handle the messages
        for(ChatRoom room : roomList){
            //get chatroom user is using
            if(room.getUsers().containsKey(userId)){
                String roomId = room.getCode();
                JSONObject jsonmsg = new JSONObject(comm);
                String type = (String) jsonmsg.get("type");
                String message = (String) jsonmsg.get("msg");
                //user list
                Map<String, String> users = room.getUsers();

                if(users.get(userId) != ""){ // not their first message
                    String username = users.get(userId);
                    System.out.println(username);



                    // broadcasting it to peers in the same room
                    for(Session peer: session.getOpenSessions()){
                        // only send my messages to those in the same room
                        if(room.inRoom(peer.getId())) {
                            peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" + username + "): " + message + "\"}");
                        }
                    }
                }else{ //first message is their username
                    users.put(userId, message);
                    session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server ): Welcome, " + message + "!\"}");



                    // broadcasting it to peers in the same room
                    for(Session peer: session.getOpenSessions()){
                        // only announce to those in the same room as me, excluding myself
                        if((!peer.getId().equals(userId)) && room.inRoom(peer.getId())){
                            peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): " + message + " joined the chat room.\"}");
                        }
                    }
                }

            }
        }


    }


}