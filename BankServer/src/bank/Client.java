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



    private void disconnectClient() {
        try {
            clientChannel.close();
            handler.removeClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



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


    private synchronized void view(CommandDTO commandDTO) {
        CustomerVO customer = this.customerList.stream().filter(customerVO -> Objects.equals(customerVO.getId(), commandDTO.getId())).findFirst().get();
        // 데이터 전송
        commandDTO.setBalance(customer.getAccount().getBalance());
        commandDTO.setUserAccountNo(customer.getAccount().getAccountNo());
        handler.displayInfo(customer.getAccount().getOwner() + "님의 계좌 잔액은 " + customer.getAccount().getBalance() + "원 입니다.");
        send(commandDTO);
    }


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


    private synchronized void deposit(CommandDTO commandDTO) {
        CustomerVO user = this.customerList.stream().filter(customerVO -> Objects.equals(customerVO.getAccount().getAccountNo(), commandDTO.getUserAccountNo())).findFirst().get();
        user.getAccount().setBalance(user.getAccount().getBalance() + commandDTO.getAmount());
        commandDTO.setResponseType(ResponseType.SUCCESS);
        handler.displayInfo(user.getName() + "님이 " + user.getAccount().getAccountNo() + " 계좌에 " + commandDTO.getAmount() + "원 입금하였습니다.");
        send(commandDTO);
    }


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
