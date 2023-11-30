package bank;

import common.AccountType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;





class ServerMain
        extends JFrame
        implements ActionListener, ClientHandler {
    private JLabel Label_UserCount;
    private JLabel Label_UserCount_2;
    private JToggleButton Btn_StartStop;
    private JButton Btn_Reset;
    private JTextArea TextArea_Log;
    private JScrollPane sp;


    private static AsynchronousChannelGroup channelGroup;
    private static AsynchronousServerSocketChannel serverSocketChannel;
    private List<CustomerVO> customerList;
    private List<Client> clientList = new Vector<>();



    public ServerMain()
    {

        InitGui();
        customerList = ReadCustomerFile("./Account.txt");
        setVisible(true);

        // WindowListener 추가
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 프레임이 종료될 때 SaveCustomerFile 메서드 호출
                SaveCustomerFile(customerList, "./Account.txt");
            }
        });

    }


    private static List<CustomerVO> GetDefaultCustomers() {
        List<CustomerVO> customerList = new Vector<>();
        customerList.add(new CustomerVO("202300001","광수","202300001", new AccountVO("광수", "202300001", AccountType.CHECKING, 100_000_000, Date.valueOf(LocalDate.now()))));
        customerList.add(new CustomerVO("202300002","영철","202300002", new AccountVO("영철", "202300002", AccountType.CHECKING, 10_000_000, Date.valueOf(LocalDate.now()))));
        customerList.add(new CustomerVO("202300003","영숙","202300003", new AccountVO("영숙", "202300003", AccountType.CHECKING, 5_000_000, Date.valueOf(LocalDate.now()))));
        customerList.add(new CustomerVO("202300004","옥순","202300004", new AccountVO("옥순", "202300004", AccountType.CHECKING, 1_000_000, Date.valueOf(LocalDate.now()))));
        return customerList;
    }



    public void SaveCustomerFile(List<CustomerVO> customers, String filePath)
    {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath)))
        {
            oos.writeObject(customers);
            System.out.println("Objects saved to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public List<CustomerVO> ReadCustomerFile(String filePath)
    {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("./Account.txt")))
        {
            List<CustomerVO> customers = (List<CustomerVO>) ois.readObject();
            System.out.println("Objects read from " + filePath);
            return customers;
        }
        catch (IOException | ClassNotFoundException e)
        {
            // 파일이 존재하지 않을 때 초기 데이터로 리스트를 초기화
            System.out.println("File not found. Initializing with default data.");
            List<CustomerVO> defaultCustomers = GetDefaultCustomers();
            SaveCustomerFile(defaultCustomers, filePath);
            return defaultCustomers;
        }
    }


    private void InitGui()
    {
        setTitle("서버 GUI");
        setSize(480, 320);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        Label_UserCount = new JLabel("현재 유저 수: ");
        topPanel.add(Label_UserCount);

        Label_UserCount_2 = new JLabel("0");
        topPanel.add(Label_UserCount_2);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        TextArea_Log = new JTextArea();
        TextArea_Log.setEditable(false);
        sp = new JScrollPane(TextArea_Log);
        mainPanel.add(sp, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        Btn_StartStop = new JToggleButton("시작");
        Btn_StartStop.addActionListener(this);
        bottomPanel.add(Btn_StartStop);

        Btn_Reset = new JButton("텍스트 창 초기화");
        Btn_Reset.addActionListener(this);
        bottomPanel.add(Btn_Reset);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        setLocationRelativeTo(null);
        setVisible(true);

    }


    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == Btn_StartStop)
        {
            if (Btn_StartStop.isSelected())
            {
                startServer();
            } else
            {
                stopServer();
            }
        }

        else if (e.getSource() == Btn_Reset)
        {
           // TextArea_Log.removeAll();
            TextArea_Log.setText(null);
        }
    }





    public void startServer()
    {
        try
        {
            channelGroup = AsynchronousChannelGroup.withFixedThreadPool(Runtime.getRuntime().availableProcessors(), Executors.defaultThreadFactory());
            serverSocketChannel = AsynchronousServerSocketChannel.open(channelGroup);
            serverSocketChannel.bind(new InetSocketAddress(5001));
            SwingUtilities.invokeLater(() ->
            {
                addMsg("서버 시작");
                Btn_StartStop.setText("정지");
            });
            serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>()
            {
                @Override
                public void completed(AsynchronousSocketChannel socketChannel, Void attachment)
                {
                    try
                    {
                        addMsg(socketChannel.getRemoteAddress() + " 접속");
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    Client client = new Client(socketChannel, ServerMain.this, customerList);
                    clientList.add(client);
                    // 현재 접속자수 업데이트
                    SwingUtilities.invokeLater(() -> Label_UserCount_2.setText(String.valueOf(clientList.size())));
                    serverSocketChannel.accept(null, this);
                }

                @Override
                public void failed(Throwable exc, Void attachment)
                {
                    if (serverSocketChannel.isOpen())
                    {
                        stopServer();
                    }
                }
            });
        }
        catch (IOException e)
        {
            if (serverSocketChannel.isOpen())
            {
                stopServer();
            }
        }
    }


    public void stopServer()
    {
        clientList.clear();
        if (channelGroup != null && !channelGroup.isShutdown())
        {
            try
            {
                channelGroup.shutdownNow();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                SwingUtilities.invokeLater(() ->
                {
                    addMsg("서버 정지");
                    Btn_StartStop.setText("시작");
                });
            }
        }
    }



    @Override
    public void removeClient(Client client)
    {
        clientList.remove(client);
        addMsg(client + "제거됨");
        // 현재 접속자수 업데이트
        SwingUtilities.invokeLater(() -> Label_UserCount_2.setText(String.valueOf(clientList.size())));
    }

    @Override
    public void displayInfo(String msg)
    {
        addMsg(msg);
    }
    public void addMsg(String data)
    {
        TextArea_Log.append(data + "\n");
    }

    public static void main(String[] args) throws Exception
    {
        ServerMain f = new ServerMain();
    }
}

