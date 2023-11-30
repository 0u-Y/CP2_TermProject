package atm;

import common.CommandDTO;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//*******************************************************************
// # 01
//*******************************************************************
// Name : ATMMain
// Type : Class
// Description :  ATM기기의 GUI 프레임이며, 서버와의 소켓통신을 담당한다
//*******************************************************************
public class ATMMain
        extends JFrame
        implements ActionListener, BankServiceHandler {

    private JLabel Label_Title;
    private JButton Btn_ViewAccount;
    private JButton Btn_Transfer;
    private JButton Btn_Login;
    private JButton Btn_Deposite;
    private JButton Btn_Withdrawal;
    private JButton Btn_Exit;
    private ImageIcon IconCNU;
    private JLabel Label_Image;


    PanViewAccount Pan_ViewAccount;
    PanTransfer Pan_Transfer;
    PanDeposite Pan_Deposite;
    PanWithdrawal Pan_Withdrawal;
    PanLogin Pan_Login;


    public static String userId;
    private AsynchronousChannelGroup channelGroup;
    private AsynchronousSocketChannel channel;


    //*******************************************************************
    // # 01-01
    //*******************************************************************
    // Name : ATMMain()
    // Type : 생성자
    // Description :  ATMMain Class의 생성자로서, 소켓통신을 시작하고 GUI를 초기화한다
    //*******************************************************************
    public ATMMain()
    {
        startClient();
        InitGui();
        setVisible(true);
    }

    //*******************************************************************
    // # 01-02 (GUI)
    //*******************************************************************
    // Name : InitGui
    // Type : Method
    // Description :  ATMMain Class의 GUI 컴포넌트를 할당하고 초기화한다.
    //                ATMMain Frame은 각 화면에 해당하는 패널들을 가지고있다.
    //*******************************************************************
    private void InitGui()
    {
        setLayout(null);

        setTitle("ATM GUI");
        setBounds(0, 0, 480, 320);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);


        try
        {
            Image Img_CNULogo = ImageIO.read(new File("BankATM/res/cnu.jpg"));

            IconCNU = new ImageIcon(Img_CNULogo.getScaledInstance(200,200,Image.SCALE_SMOOTH));
            Label_Image = new JLabel();
            Label_Image.setIcon(IconCNU);
            Label_Image.setBounds(135, 70, IconCNU.getIconWidth(), IconCNU.getIconHeight());
            add(Label_Image);
        }
        catch (IOException e){ }


        Label_Title = new JLabel("CNU Bank ATM");
        Label_Title.setFont(new Font("Arial", Font.PLAIN, 30));
        Label_Title.setSize(getWidth(), 60);
        Label_Title.setLocation(0, 0);
        Label_Title.setHorizontalAlignment(JLabel.CENTER);
        add(Label_Title);

        Btn_ViewAccount = new JButton("계좌 조회");
        Btn_ViewAccount.setSize(100,70);
        Btn_ViewAccount.setLocation(0, 60);
        Btn_ViewAccount.addActionListener(this);
        add(Btn_ViewAccount);

        Btn_Transfer = new JButton("계좌 이체");
        Btn_Transfer.setSize(100,70);
        Btn_Transfer.setLocation(0, 130);
        Btn_Transfer.addActionListener(this);
        add(Btn_Transfer);

        Btn_Login = new JButton("로그인");
        Btn_Login.setSize(100,70);
        Btn_Login.setLocation(0, 200);
        Btn_Login.addActionListener(this);
        add(Btn_Login);

        Btn_Deposite = new JButton("입금");
        Btn_Deposite.setSize(100,70);
        Btn_Deposite.setLocation(365, 60);
        Btn_Deposite.addActionListener(this);
        add(Btn_Deposite);

        Btn_Withdrawal = new JButton("출금");
        Btn_Withdrawal.setSize(100,70);
        Btn_Withdrawal.setLocation(365, 130);
        Btn_Withdrawal.addActionListener(this);
        add(Btn_Withdrawal);

        Btn_Exit = new JButton("종료");
        Btn_Exit.setSize(100,70);
        Btn_Exit.setLocation(365, 200);
        Btn_Exit.addActionListener(this);
        add(Btn_Exit);

        Pan_ViewAccount = new PanViewAccount(this);
        add(Pan_ViewAccount);
        Pan_ViewAccount.setVisible(false);

        Pan_Transfer = new PanTransfer(this);
        add(Pan_Transfer);
        Pan_Transfer.setVisible(false);

        Pan_Deposite = new PanDeposite(this);
        add(Pan_Deposite);
        Pan_Deposite.setVisible(false);

        Pan_Withdrawal = new PanWithdrawal(this);
        add(Pan_Withdrawal);
        Pan_Withdrawal.setVisible(false);

        Pan_Login = new PanLogin(this);
        add(Pan_Login);
        Pan_Login.setVisible(false);
    }


    //*******************************************************************
    // # 01-02-01
    //*******************************************************************
    // Name : actionPerformed
    // Type : Listener
    // Description :  ATMMain Frame의 버튼 컴포넌트들의 동작을 구현한 부분
    //                아래 코드에서는 각 기능별 화면으로 전환하는 코드가 작성되어있다.
    //*******************************************************************
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == Btn_ViewAccount)
        {
            // 계좌 조회
            display("ViewAccount");
            Pan_ViewAccount.GetBalance();
        }
        else if (e.getSource() == Btn_Transfer)
        {
            // 계좌 이체
            display("Transfer");
        }
        else if (e.getSource() == Btn_Login)
        {
            // 로그인
            display("Login");
        }
        else if (e.getSource() == Btn_Deposite)
        {
            // 입금
            display("Deposite");
        }
        else if (e.getSource() == Btn_Withdrawal)
        {
            // 출금
            display("Withdrawal");
        }
        else if (e.getSource() == Btn_Exit)
        {
            // 종료
            dispose();
        }
    }
    //*******************************************************************
    // # 01-02-02
    //*******************************************************************
    // Name : disply() , SetFrameUI()
    // Type : Method
    // Description :  각 화면별 패널의 Visible 여부를 바꾸어주어 화면 전환의 효과를 준다.
    //*******************************************************************
    public void display(String viewName) {
        if (userId == null)
        {
            if(viewName.equals("Login") != true && viewName.equals("Main") != true)
            {
                JOptionPane.showMessageDialog(null, "카드를 투입하거나 로그인하세요.", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if(viewName.equals("ViewAccount") == true)
        {
            SetFrameUI(false);
            Pan_ViewAccount.setVisible(true);
        }
        if(viewName.equals("Transfer") == true)
        {
            SetFrameUI(false);
            Pan_Transfer.setVisible(true);
        }
        if(viewName.equals("Deposite") == true)
        {
            SetFrameUI(false);
            Pan_Deposite.setVisible(true);
        }
        if(viewName.equals("Withdrawal") == true)
        {
            SetFrameUI(false);
            Pan_Withdrawal.setVisible(true);
        }
        if(viewName.equals("Login") == true)
        {
            SetFrameUI(false);
            Pan_Login.setVisible(true);
        }
        if(viewName.equals("Main") == true)
        {
            SetFrameUI(true);
        }

        //setContentPane(pan1);
    }

    void SetFrameUI(Boolean bOn)
    {
        if(bOn == true)
        {
            Label_Title.setVisible(true);
            Btn_ViewAccount.setVisible(true);
            Btn_Transfer.setVisible(true);
            Btn_Login.setVisible(true);
            Btn_Deposite.setVisible(true);
            Btn_Withdrawal.setVisible(true);
            Btn_Exit.setVisible(true);
            Label_Image.setVisible(true);
        }
        else {

            Label_Title.setVisible(false);
            Btn_ViewAccount.setVisible(false);
            Btn_Transfer.setVisible(false);
            Btn_Login.setVisible(false);
            Btn_Deposite.setVisible(false);
            Btn_Withdrawal.setVisible(false);
            Btn_Exit.setVisible(false);
            Label_Image.setVisible(false);
        }
    }



    //*******************************************************************
    // # 01-03-01
    //*******************************************************************
    // Name : startClient()
    // Type : Method
    // Description :  ATMMain Class 가 가지고있는 소켓을 서버소켓에 접속시킨다
    //*******************************************************************
    private void startClient() {
        try {
            channelGroup = AsynchronousChannelGroup.withFixedThreadPool(Runtime.getRuntime().availableProcessors(), Executors.defaultThreadFactory());
            channel = AsynchronousSocketChannel.open(channelGroup);
            channel.connect(new InetSocketAddress("localhost", 5001), null, new CompletionHandler<Void, Void>() {
                @Override
                public void completed(Void result, Void attachment) {
                    System.out.println("뱅크 서버 접속");
                }
                @Override
                public void failed(Throwable exc, Void attachment) {
                    disconnectServer();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //*******************************************************************
    // # 01-03-02
    //*******************************************************************
    // Name : stopClient(), disconnectServer()
    // Type : Method
    // Description :  ATMMain Class 가 가지고있는 소켓의 연결을 해제한다.
    //*******************************************************************
    private void stopClient() {
        try {
            if (channelGroup != null && !channelGroup.isShutdown()) {
                channelGroup.shutdownNow();
                channelGroup.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS); // 기다리도록 추가
            }
            System.out.println("연결 종료");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void disconnectServer() {
        stopClient();
    }

    //*******************************************************************
    // # 01-03-03
    //*******************************************************************
    // Name : send()
    // Type : Method
    // Description :  CommandDTO를 매개변수로 하여 서버에 요청 메시지를 전달하는 메소드
    //                CommandDTO Class 에는 ATM 서비스 요청에 필요한 데이터들이 정의 되어 있다.
    //                ATMMain Class는 BankServiceHandler 인터페이스를 상속하였다.
    //*******************************************************************
    @Override
    public void send(CommandDTO commandDTO, CompletionHandler<Integer, ByteBuffer> handlers) {
        commandDTO.setId(userId);
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(commandDTO);
            objectOutputStream.flush();
            channel.write(ByteBuffer.wrap(byteArrayOutputStream.toByteArray()), null, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(512);
                    channel.read(byteBuffer, byteBuffer, handlers);
                }
                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    disconnectServer();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception
    {
        ATMMain my = new ATMMain();
    }
}
