package com.snapmeet.websocket.test;

import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RabbitMqSubscriberAutoAck {
    private static final String EXCHANGE_NAME = "fanout_exchange";

    public static void main(String[] args) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        Connection connection = factory.newConnection(); Channel channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName,EXCHANGE_NAME,"");
            DeliverCallback deliverCallback = (consumerTag,dellivery)->{
                try{
                    String message = new String(dellivery.getBody(),"UTF-8");
                    log.info("收到消息->{}",message+System.currentTimeMillis());
                }catch(Exception e){

                }
            };
            channel.basicConsume(queueName,true,deliverCallback,consumeTag->{

            });
    }
}
