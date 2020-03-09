/*Для сборки проекта через maven необходимо:
1)Перейти в директорию проекта;
2)Выполнить команду в cmd: maven compile.
Для компиляции проекта необходимо:
1)Выполнить команду в cmd:java -jar your directory\jar_name.jar
 */

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Console;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.*;

public class Publisher {

    JFrame frame;
    JPanel panel;
    JTextField ipField, portField;
    JTextArea area;
    JList archiveList;
    DefaultListModel listModel;
    JScrollPane pane, pane2;
    JLabel ipLabel, portLabel, archiveLabel;
    JButton button;


    public Publisher() throws SQLException {
        //Создание окна
        frame = new JFrame("Publisher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setUndecorated(true);
        frame.getRootPane().
                setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
        panel = new JPanel();
        panel.setLayout(null);
        ipLabel = new JLabel("Ip:");
        ipLabel.setBounds(10, 20, 100, 30);
        panel.add(ipLabel);
        ipField = new JTextField();
        ipField.setBounds(125, 25, 150, 20);
        //Сбрасываю сообщения всем ip на порте
        ipField.setText("255.255.255.255");
        ipField.disable();
        panel.add(ipField);
        portLabel = new JLabel("Порт:");
        portLabel.setBounds(10, 50, 100, 30);
        panel.add(portLabel);
        portField = new JTextField();
        portField.setBounds(125, 55, 100, 20);
        portField.disable();
        archiveLabel = new JLabel("News archive");
        archiveLabel.setBounds(400, 70, 100, 20);
        panel.add(archiveLabel);
        archiveList = new JList();
        listModel = new DefaultListModel();
        archiveList.setModel(listModel);
        pane2 = new JScrollPane(archiveList);
        pane2.setBounds(400, 100, 200, 300);
        panel.add(pane2);
        ResultSet resultSet = LoadNews();
        int columns = resultSet.getMetaData().getColumnCount();
        while (resultSet.next()) {
            for (int i = 1; i <= columns; i++) {
                listModel.addElement(resultSet.getString(i));
            }
        }
        panel.add(portField);
        area = new JTextArea();
        area.setLineWrap(true);
        pane = new JScrollPane(area);
        pane.setBounds(10, 100, 300, 300);
        panel.add(pane);
        button = new JButton("Publish");
        button.setBounds(160, 410, 150, 30);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!area.getText().isEmpty()) {
                    SaveNews();
                    new SendRequest();
                }
            }
        });
        panel.add(button);
        frame.add(panel);
        frame.setSize(650, 600);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws SQLException {
        //работа с консолью
        Console console = System.console();
        String port = console.readLine("The default port is 8080. Press Enter to continue. To change the port," +
                " enter a new value: " + "enter new value:");
        Publisher u = new Publisher();
        if (port.isEmpty()) {
            u.portField.setText("8080");
        } else {
            u.portField.setText(port);
        }
    }

    //Использование PostgreSql для хранения старых новостей
    public void SaveNews() {
        final String DB_URL = "jdbc:postgresql://127.0.0.1:5433/oldnews";
        final String USER = "postgres";
        final String PASS = "1994";
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Connection connection = null;
        try {
            connection = DriverManager
                    .getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            System.out.println("Connection error.");
            e.printStackTrace();
            return;
        }
        if (connection != null) {
            try {
                String query = " insert into news VALUES(?)";
                PreparedStatement preparedStmt = connection.prepareStatement(query);
                preparedStmt.setString(1, area.getText());
                System.out.println(area.getText());
                preparedStmt.execute();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Failed to establish database connection.");
        }
    }

    //Использование PostgreSql для загрузки старых новостей
    public ResultSet LoadNews() {
        ResultSet resultSet = null;
        final String DB_URL = "jdbc:postgresql://127.0.0.1:5433/oldnews";
        final String USER = "postgres";
        final String PASS = "1994";
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = null;
        try {
            connection = DriverManager
                    .getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            System.out.println("Connection error");
            e.printStackTrace();
        }
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                resultSet = statement.executeQuery
                        ("SELECT * FROM news");
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Failed to establish database connection.");
        }
        return resultSet;
    }

    //Отправка новости
    public class SendRequest {
        SendRequest() {
            try {
                DatagramSocket socket;
                DatagramPacket packet;
                InetAddress address;
                socket = new DatagramSocket();
                String dip = ipField.getText();
                address = InetAddress.getByName(dip);
                String port = portField.getText();
                int pnum = Integer.parseInt(port);
                String mess = area.getText();
                byte message[] = mess.getBytes();
                packet =
                        new DatagramPacket(message, message.length, address, pnum);
                socket.send(packet);
                area.setText("");
                listModel.addElement(mess);
                socket.close();
            } catch (IOException io) {
            }
        }
    }
}