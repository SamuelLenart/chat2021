package sk.kosickaakademia.lenart.chat;

import sk.kosickaakademia.lenart.chat.Util;
import sk.kosickaakademia.lenart.entity.Message;
import sk.kosickaakademia.lenart.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private String url = "jdbc:mysql://itsovy.sk:3306/chat2021";
    private String username = "mysqluser";
    private String password = "Kosice2021!";
    private final String insertNewUser = "INSERT INTO user (login, password) VALUES (?,?)";
    private final String loginUser = "Select * FROM user WHERE login LIKE ? and password LIKE ?";
    private final String newMessage = "INSERT INTO message( fromUser, toUser, text) VALUES (?,?,?)";
    private final String getId = "SELECT id FROM user WHERE login LIKE ?";
    private final String delMessages = "DELETE FROM message WHERE toUser = ?";
    private final String getMessages = "SELECT * FROM message  " +
            " INNER JOIN user ON user.id=message.fromUser " +
            " WHERE toUser = ?";

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(url, username, password);
        return conn;
    }

    public void test() {
        try {
            Connection con = getConnection();
            if (con==null) {
                System.out.println("Error");
            } else {
                System.out.println("Success");
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean insertNewUser(String login, String password) {
        if (login == null || login.equals("") || password==null || password.length()<6)
            return false;
        String hashPassword = new Util().getMd5(password);
        try {
            Connection con = getConnection();
            if (con==null) {
                System.out.println("Connection error !");
                return false;
            }
            PreparedStatement ps = con.prepareStatement(insertNewUser);
            ps.setString(1, login);
            ps.setString(2, hashPassword);
            int result = ps.executeUpdate();
            con.close();
            if (result==0)
                return false;
            else {
                System.out.println("User " + login + " has been added to the database !");
                return true;
            }
        } catch (Exception ex) {
            System.out.println("This user already exists!");
        }
        return true;
    }

    public User loginUser(String login, String password) {
        if (login==null || login.equals("") || password==null || password.length() < 6)
            return null;

        String hashPassword = new Util().getMd5(password);
        try {
            Connection con = getConnection();
            if (con==null) {
                System.out.println("Connection error!");
                return null;
            }
            PreparedStatement ps = con.prepareStatement(loginUser);
            ps.setString(1, login);
            ps.setString(2, hashPassword);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Success login!");
                int id = rs.getInt("id");
                User user = new User(id, login, hashPassword);
                con.close();
                return user;
            } else {
                con.close();
                System.out.println("Incorrect credentials!");
                return null;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public boolean changePassword(String login, String oldPassword, String newPassword){
        return false;
    }

    public boolean sendMessage(int from, String toUser, String text){
        if(text==null || text.equals(""))
            return false;
        int to = getUserId(toUser);
        if(to==-1)
            return false;
        try {
            Connection con = getConnection();
            if(con ==null){
                System.out.println("Connection issue!");
                return false;
            }
            PreparedStatement ps = con.prepareStatement(newMessage);
            ps.setInt(1,from);
            ps.setInt(2, to);
            ps.setString(3, text);
            int result = ps.executeUpdate();
            con.close();
            if(result<1){
                System.out.println("Message not delivered!");
                return false;
            }
            else{
                System.out.println("Message sent successfully!");
                return true;
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public int getUserId(String login){
        if(login==null || login.equals(""))
            return -1;
        int id=-1;
        try {
            Connection con = getConnection();
            if(con!=null){
                PreparedStatement ps=con.prepareStatement(getId);
                ps.setString(1,login);
                ResultSet rs= ps.executeQuery();

                if(rs.next()){
                    id=rs.getInt("id");
                }
                con.close();
                return id;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    public List<Message> getMyMessages(String login){
        if(login==null  || login.equals(""))
            return null;
        int id = getUserId(login);
        if(id!=-1){
            try {
                Connection con = getConnection();
                if(con==null)
                    return null;
                PreparedStatement ps = con.prepareStatement(getMessages);
                ps.setInt(1,id);
                List<Message> list= new ArrayList<>();
                ResultSet rs= ps.executeQuery();
                while(rs.next()){
                    String from = rs.getString("user.login");
                    int idMessage = rs.getInt("message.id");
                    String text=rs.getString("text");
                    Date dt = rs.getDate("dt");
                    Message m = new Message(idMessage, from, login,dt,text);
                    list.add(m);
                }
                con.close();
                deleteAllMyMessages(login);
                return list;
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void deleteAllMyMessages(String login){
        if(login==null || login.equals(""))
            return;
        try {
            int id = getUserId(login);
            if(id==-1)
                return;
            Connection con = getConnection();
            if(con==null) return;
            PreparedStatement ps = con.prepareStatement(delMessages);
            ps.setInt(1,id);
            ps.executeUpdate();
            con.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }


}





