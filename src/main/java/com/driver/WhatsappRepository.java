package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private HashMap<User,Group> userGroupMap;
    private int customGroupCount;
    private int messageId;
//    private HashMap<User, Group>userGroupHashMap ;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userGroupMap=new HashMap<User, Group>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }
    public String  createUser(String name, String mobile) throws Exception
    {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"
        if(userMobile.contains(mobile))
        {
            throw new Exception("User already exists");
        }
        else
        {
            User user=new User(name,mobile);
            userMobile.add(mobile);
            return "SUCCESS";
        }
    }
    public Group createGroup(List<User> users)
    {
        Group group;
        for(User user:users)
        {
            if(!userMobile.contains(user.getMobile()))
            {
                userMobile.add((user.getMobile()));
            }
        }
      if(users.size()>2)
      {
        customGroupCount++;
        String name="Group "+Integer.toString(customGroupCount);
        group=new Group(name,users.size());
       groupUserMap.put(group,users);
      }
      else
      {
          User user=users.get(1);
          String name=user.getName();
           group=new Group(name,users.size());
          groupUserMap.put(group,users);
      }
      User user=users.get(0);
      adminMap.put(group,user);
      for(User user1:users)
      {
          userGroupMap.put(user1,group);
      }
      return group;
    }
    public int createMessage(String content)
    {
        messageId++;
        Message message=new Message(messageId,content);
        return messageId;
    }
    public int sendMessage(Message message, User sender, Group group) throws Exception {
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        if(!groupUserMap.containsKey(group))
            throw new Exception("Group does not exist");
        else {
            boolean flag=false;
            for(User user:groupUserMap.get(group))
            {
                if(user.getMobile().equals(sender.getMobile()))
                {
                   flag=true;
                   break;
                }
            }
            if(flag==false)
                throw new Exception("You are not allowed to send message");
        }
        List<Message>messages;
        if(groupMessageMap.get(group)==null)
        {
          messages=new ArrayList<>();
          messages.add(message);

          groupMessageMap.put(group,messages);
        }
        else {
            messages=groupMessageMap.get(group);
            messages.add(message);
           groupMessageMap.put(group,messages);
        }
        senderMap.put(message,sender);
return groupMessageMap.get(group).size();

    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user.

        if(!adminMap.containsKey(group)) {
            throw new Exception( "Group does not exist");
        }
        else
        {
            if (adminMap.get(group) != approver)
                throw new Exception("Approver does not have rights");
            boolean flag=false;
             for(User user1:groupUserMap.get(group))
             {
                 if(user.getMobile().equals(user1.getMobile()))
                     flag=true;

             }
             if(flag==false)
                 throw new Exception("User is not a participant");
             adminMap.put(group,user);
           return "SUCCESS";
        }

    }

    public int removeUser(User user) throws Exception
    {

        //This is a bonus problem and does not contains any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        if(userGroupMap.get(user)==null)
            throw new Exception("User not found!!");
        else {
            Group group = userGroupMap.get(user);
              if(adminMap.containsKey(group))
               throw new Exception("Cannot remove admin");
              userGroupMap.remove(user);
             List<User>userList=groupUserMap.get(group);

                  if(userList.contains(user))
                  {
                      userList.remove(user);
                      groupUserMap.put(group,userList);
                  }
                  List<Message>messagelist=new ArrayList<>();
                  for(Message message:senderMap.keySet())
                  {
                        if(senderMap.get(message).equals(user))
                        {
                            messagelist.add(message);
                            senderMap.remove(message);
                        }
                  }
                  for(Message message:groupMessageMap.get(group))
                  {
                      if(messagelist.contains(message))
                          groupMessageMap.get(group).remove(message);

                  }


            return groupUserMap.get(group).size()+groupMessageMap.get(group).size()+senderMap.size();
        }

    }

    public String findMessage(Date start, Date end, int K) throws Exception
    {
        //This is a bonus problem and does not contains any marks
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
   List<Message>messageList=new ArrayList<>();
    for(Message message:senderMap.keySet())
    {
        Date date=message.getTimestamp();
        if(start.before(date) && end.after(date))
        {
            messageList.add(message);

        }
    }
    if(messageList.size()<K)
        throw new Exception("K is greater than the number of messages");
    Collections.sort(messageList, new Comparator<Message>() {
        @Override
        public int compare(Message o1, Message o2) {
            return o1.getTimestamp().compareTo(o2.getTimestamp());
        }
    });
    Message ans=messageList.get(K-1);
    return ans.getContent();



    }

    }

