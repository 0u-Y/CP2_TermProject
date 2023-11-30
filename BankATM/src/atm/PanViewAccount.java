package atm;

import common.CommandDTO;
import common.RequestType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;


public class PanViewAccount extends JPanel implements ActionListener
{
    private JLabel Label_Account;
    private  JTextArea Text_Account;
    private JLabel Label_balance;
    private  JTextArea Text_balance;

    private JButton Btn_Close;

    ATMMain MainFrame;
    

    public PanViewAccount(ATMMain parent)
    {
        MainFrame = parent;
        InitGUI();
    }
    

    private void InitGUI()
    {
        setLayout(null);
        setBounds(0,0,480,320);

        Label_Account = new JLabel("°èÁÂ ¹øÈ£");
        Label_Account.setBounds(0,70,100,20);
        Label_Account.setHorizontalAlignment(JLabel.LEFT);
        add(Label_Account);

        Text_Account = new JTextArea();
        Text_Account.setBounds(100,70,350,20);
        Text_Account.setEditable(false);
        add(Text_Account);

        Label_balance = new JLabel("ÀÜ¾×");
        Label_balance.setBounds(0,120,100,20);
        Label_balance.setHorizontalAlignment(JLabel.LEFT);
        add(Label_balance);

        Text_balance = new JTextArea();
        Text_balance.setBounds(100,120,350,20);
        Text_balance.setEditable(false);
        add(Text_balance);

        Btn_Close = new JButton("´Ý±â");
        Btn_Close.setBounds(250,250,70,20);
        Btn_Close.addActionListener(this);
        add(Btn_Close);
    }


    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == Btn_Close)
        {
            this.setVisible(false);
            MainFrame.display("Main");
        }
    } 
    

    public void GetBalance()
    {
        MainFrame.send(new CommandDTO(RequestType.VIEW), new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (result == -1) {
                    return;
                }
                attachment.flip();
                try {
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(attachment.array());
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    CommandDTO command = (CommandDTO) objectInputStream.readObject();
                    SwingUtilities.invokeLater(() -> {
                        String accountNumber = BankUtils.displayAccountNo(command.getUserAccountNo());
                        Text_Account.setText(accountNumber);
                        String balance = BankUtils.displayBalance(command.getBalance());
                        Text_balance.setText(balance + "¿ø");
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
            }
        });
    }

}
