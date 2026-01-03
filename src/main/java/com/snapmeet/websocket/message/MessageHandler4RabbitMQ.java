package com.snapmeet.websocket.message;

import com.alibaba.fastjson2.JSON;
import com.rabbitmq.client.*;
import com.snapmeet.constants.Constants;
import com.snapmeet.entity.dto.MessageSendDto;
import com.snapmeet.websocket.ChannelContextUtils;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@ConditionalOnProperty(name = Constants.MESSAGEING_HANDLE_CHANNEL_KEY,havingValue = Constants.MESSAGEING_HANDLE_CHANNEL_RABBITMQ)
@Slf4j
public class MessageHandler4RabbitMQ implements MessageHandler{

    private static final String EXCHANGE_NAME = "fanout_exchange";
    private static final Integer MAX_RETRYTIME = 3;
    private static final String RETRY_COUNT_KEY = "retryCount";

    @Resource
    private ChannelContextUtils channelContextUtils;

    @Value("${spring.rabbitmq.host}")
    private String host;
    @Value("${spring.rabbitmq.port}")
    private Integer port;

    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;


    @Override
    public void listenMessage() {
        factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName,EXCHANGE_NAME,"");

            Boolean autoAck = false;
            DeliverCallback deliverCallback = (consumerTag, dellivery)->{
                try{
                    String message = new String(dellivery.getBody(),"UTF-8");
                    log.info("RabbitMQ收到消息:{}",message);
                    channelContextUtils.sendMessage(JSON.parseObject(message,MessageSendDto.class));
                    channel.basicAck(dellivery.getEnvelope().getDeliveryTag(),false);
                }catch(Exception e){
                    log.info("处理消息失败",e);
                    handleFailMessage(channel,dellivery,queueName);
                }
            };
            channel.basicConsume(queueName,autoAck,deliverCallback,consumeTag->{

            });
        }catch (Exception e){
            log.error("rabbitmq监听消息失败");
        }

    }

    private static void handleFailMessage(Channel channel,Delivery delivery,String queueName)throws IOException {
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

    @Override
    public void sendMessage(MessageSendDto messageSendDto) {
        try(Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            String message = "这是我发布的一条消息("+System.currentTimeMillis()+")";
            channel.basicPublish(EXCHANGE_NAME,"",null,message.getBytes());
        }catch (Exception e){
            log.error("rabbitmq发送消息失败");
        }
    }

    @PreDestroy
    public void destroy() throws IOException, TimeoutException {
        if(channel != null && channel.isOpen()){
            channel.close();
        }
        if(connection!=null&&channel.isOpen()){
            connection.close();
        }
    }
}
