package bank;

import common.CommandDTO;
import common.ResponseType;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


//*******************************************************************
// # 52
//*******************************************************************
// Name : Client
// Type : Class
// Description :  BankServer에 접속 요청한 ATM 소켓과의 통신을 담당하는 소켓 Class 구현.
//                메시지 Receive, Send 및 예금, 출금, 계좌 이체 등의 기능이 구현 되어 있다.
//*******************************************************************
public class Client {
    private AsynchronousSocketChannel clientChannel;
    private ClientHandler handler;
    private List<CustomerVO> customerList;


    public Client(AsynchronousSocketChannel clientChannel, ClientHandler handler, List<CustomerVO> customerList) {
        this.clientChannel = clientChannel;
        this.handler = handler;
        this.customerList = customerList;
        receive();
    }

    //*******************************************************************
    // # 52-01
    //*******************************************************************
    // Name : receive()
    // Type : method
    // Description :  수신한 메시지의 버퍼를 CommandDTO Class 형태로 형변환 후
    //                요청 정보를 파싱하여 각 요청에 대한 기능을 구현
    //*******************************************************************
    private void receive() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(512);
        clientChannel.read(byteBuffer, byteBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (result == -1) {
                    disconnectClient();
                    return;
                }
                attachment.flip();
                try {
                    // 읽은 데이터 파싱
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(attachment.array());
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    CommandDTO command = (CommandDTO) objectInputStream.readObject();
                    switch (command.getRequestType()) {
                        case VIEW:
                            view(command);
                            break;
                        case LOGIN:
                            login(command);
                            break;
                        case TRANSFER:
                            transfer(command);
                            break;
                        case DEPOSIT:
                            deposit(command);
                            break;
                        case WITHDRAW:
                            withdraw(command);
                            break;
                        default:
                            break;
                    }
                    // 데이터 보낸 후 다시 읽기 모드로
                    ByteBuffer byteBuffer = ByteBuffer.allocate(512);
                    clientChannel.read(byteBuffer, byteBuffer, this);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                disconnectClient();
            }
        });
    }

    //*******************************************************************
    // # 52-02
    //*******************************************************************
    // Name : send()
    // Type : method
    // Description :  연결된 ATM 소켓에 CommandDTO 형태로 메시지를 전달
    //                CommandDTO 내부에 요청에대한 결과 정보가 존재 해야 한다
    //*******************************************************************
    private void send(CommandDTO commandDTO) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(commandDTO);
            objectOutputStream.flush();
            clientChannel.write(ByteBuffer.wrap(byteArrayOutputStream.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //*******************************************************************
    // # 52-03
    //*******************************************************************
    // Name : disconnectClient()
    // Type : method
    // Description :  해당 소켓의 예외발생 혹은 통신 실패시 소켓 해제 기능 구현
    //*******************************************************************
    private void disconnectClient() {
        try {
            clientChannel.close();
            handler.removeClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //*******************************************************************
    // # 52-01-01
    //*******************************************************************
    // Name : login()
    // Type : method
    // Description : ATM의 Loging 요청 기능 구현
    //               성공, 실패 여부를 응답메시지에 전달
    //*******************************************************************
    private synchronized void login(CommandDTO commandDTO) {
        Optional<CustomerVO> customer = this.customerList.stream().filter(customerVO -> Objects.equals(customerVO.getId(), commandDTO.getId()) && Objects.equals(customerVO.getPassword(), commandDTO.getPassword())).findFirst();

        if (customer.isPresent()) {
            commandDTO.setResponseType(ResponseType.SUCCESS);
            String text = customer.get().getName();
            String text2 = "님이 로그인하였습니다.";
            String text3 = text + text2;
            handler.displayInfo(customer.get().getName() + "님이 로그인하였습니다.");
        } else {
            commandDTO.setResponseType(ResponseType.FAILURE);
        }
        send(commandDTO);
    }

    //*******************************************************************
    // # 52-01-02
    //*******************************************************************
    // Name : view()
    // Type : method
    // Description : ATM의 계좌 조회 요청 기능 구현
    //               계좌 잔액을 응답메시지에 전달
    //*******************************************************************
    private synchronized void view(CommandDTO commandDTO) {
        CustomerVO customer = this.customerList.stream().filter(customerVO -> Objects.equals(customerVO.getId(), commandDTO.getId())).findFirst().get();
        // 데이터 전송
        commandDTO.setBalance(customer.getAccount().getBalance());
        commandDTO.setUserAccountNo(customer.getAccount().getAccountNo());
        handler.displayInfo(customer.getAccount().getOwner() + "님의 계좌 잔액은 " + customer.getAccount().getBalance() + "원 입니다.");
        send(commandDTO);
    }

    //*******************************************************************
    // # 52-01-03
    //*******************************************************************
    // Name : transfer()
    // Type : method
    // Description : ATM의 계좌 이체기능 구현
    //               성공, 실패 여부를 응답메시지에 전달
    //*******************************************************************
    private synchronized void transfer(CommandDTO commandDTO) {
        CustomerVO user = this.customerList.stream().filter(customerVO -> Objects.equals(customerVO.getAccount().getAccountNo(), commandDTO.getUserAccountNo())).findFirst().get();
        Optional<CustomerVO> receiverOptional = this.customerList.stream().filter(customerVO -> Objects.equals(customerVO.getAccount().getAccountNo(), commandDTO.getReceivedAccountNo())).findFirst();
        if (!receiverOptional.isPresent()) {
            commandDTO.setResponseType(ResponseType.WRONG_ACCOUNT_NO);
        } else if (receiverOptional.get().getAccount().getAccountNo().equals(user.getAccount().getAccountNo())) {
            commandDTO.setResponseType(ResponseType.WRONG_ACCOUNT_NO);
        } else if (!user.getPassword().equals(commandDTO.getPassword())) {
            commandDTO.setResponseType(ResponseType.WRONG_PASSWORD);
        } else if (user.getAccount().getBalance() < commandDTO.getAmount()) {
            commandDTO.setResponseType(ResponseType.INSUFFICIENT);
        } else {
            CustomerVO receiver = receiverOptional.get();
            commandDTO.setResponseType(ResponseType.SUCCESS);
            user.getAccount().setBalance(user.getAccount().getBalance() - commandDTO.getAmount());
            receiver.getAccount().setBalance(receiver.getAccount().getBalance() + commandDTO.getAmount());
            handler.displayInfo(user.getAccount().getAccountNo() + " 계좌에서 " + receiver.getAccount().getAccountNo() + "계좌로 " + commandDTO.getAmount() + "원 이체하였습니다.");
        }
        send(commandDTO);
    }

    //*******************************************************************
    // # 52-01-04
    //*******************************************************************
    // Name : deposit()
    // Type : method
    // Description : ATM의 계좌 입금 기능 구현
    //               성공, 실패 여부를 응답메시지에 전달
    //*******************************************************************
    private synchronized void deposit(CommandDTO commandDTO) {
        CustomerVO user = this.customerList.stream().filter(customerVO -> Objects.equals(customerVO.getAccount().getAccountNo(), commandDTO.getUserAccountNo())).findFirst().get();
        user.getAccount().setBalance(user.getAccount().getBalance() + commandDTO.getAmount());
        commandDTO.setResponseType(ResponseType.SUCCESS);
        handler.displayInfo(user.getName() + "님이 " + user.getAccount().getAccountNo() + " 계좌에 " + commandDTO.getAmount() + "원 입금하였습니다.");
        send(commandDTO);
    }

    //*******************************************************************
    // # 52-01-05
    //*******************************************************************
    // Name : deposit()
    // Type : method
    // Description : ATM의 계좌 출금 기능 구현
    //               성공, 실패 여부를 응답메시지에 전달
    //*******************************************************************
    private synchronized void withdraw(CommandDTO commandDTO) {
        CustomerVO user = this.customerList.stream().filter(customerVO -> Objects.equals(customerVO.getAccount().getAccountNo(), commandDTO.getUserAccountNo())).findFirst().get();
        if (user.getAccount().getBalance() < commandDTO.getAmount()) {
            commandDTO.setResponseType(ResponseType.INSUFFICIENT);
        } else {
            commandDTO.setResponseType(ResponseType.SUCCESS);
            user.getAccount().setBalance(user.getAccount().getBalance() - commandDTO.getAmount());
            handler.displayInfo(user.getName() + "님이 " + user.getAccount().getAccountNo() + " 계좌에서 " + commandDTO.getAmount() + "원 출금하였습니다.");
        }
        send(commandDTO);
    }
}
