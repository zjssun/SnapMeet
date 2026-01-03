package com.snapmeet.websocket.test;

import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class RabbitMqSubscriber {
    private static final String EXCHANGE_NAME = "fanout_exchange";
    private static final Integer MAX_RETRYTIME = 3;
    private static final String RETRY_COUNT_KEY = "retryCount";

    public static void main(String[] args) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        Connection connection = factory.newConnection(); Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName,EXCHANGE_NAME,"");
        DeliverCallback deliverCallback = (consumerTag, dellivery)->{
            try{
                String message = new String(dellivery.getBody(),"UTF-8");
                log.info("收到消息->{}",message+System.currentTimeMillis());
                if(Math.random()>0.3){
                    throw new RuntimeException("模拟处理失败");
                }
                channel.basicAck(dellivery.getEnvelope().getDeliveryTag(),false);
            }catch(Exception e){
                log.info("处理消息失败",e);

            }
        };
        channel.basicConsume(queueName,false,deliverCallback,consumeTag->{

        });
    }

    private static void handleFailMessage(Channel channel,Delivery delivery,String queueName)throws Exception{
        Map<String, Object> headers = delivery.getProperties().getHeaders();
        if(headers==null){
            headers = new HashMap<>();
        }
        Integer retryCount = 0;
        if(headers.containsKey(RETRY_COUNT_KEY)){
            retryCount = (Integer) headers.get(RETRY_COUNT_KEY);
        }
        if(retryCount<MAX_RETRYTIME){
            headers.put(RETRY_COUNT_KEY,retryCount+1);
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().headers(headers).build();
            channel.basicPublish("",queueName,properties,delivery.getBody());
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
        }else {
            log.info("超过最大重试次数");
            channel.basicReject(delivery.getEnvelope().getDeliveryTag(),false);
        }
    }
}
