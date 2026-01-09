package com.snapmeet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snapmeet.entity.dto.MessageSendDto;
import com.snapmeet.entity.po.UserContact;
import com.snapmeet.entity.po.UserContactApply;
import com.snapmeet.enums.*;
import com.snapmeet.exception.BusinessException;
import com.snapmeet.mapper.UserContactApplyMapper;
import com.snapmeet.service.IUserContactApplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snapmeet.websocket.message.MessageHandler;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sam
 * @since 2026-01-07
 */
@Service
public class UserContactApplyServiceImpl extends ServiceImpl<UserContactApplyMapper, UserContactApply> implements IUserContactApplyService {
    @Resource
    private UserContactServiceImpl userContactService;
    @Resource
    private MessageHandler messageHandler;

    @Override
    public Integer saveUserContactApply(UserContactApply userContactApply) {
        UserContact userContact = userContactService.getOne(new LambdaQueryWrapper<UserContact>()
                .eq(UserContact::getContactId,userContactApply.getReceiveUserId())
                .eq(UserContact::getUserId,userContactApply.getApplyId()));
        if(userContact!=null&& UserContactStatusEnum.BLACKLIST.getStatus().equals(userContact.getStatus())){
            throw new BusinessException("对方将你拉黑");
        }
        if(userContact!=null&&UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())){
            UserContact updateInfo = new UserContact();
            updateInfo.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContactService.update(updateInfo,new LambdaQueryWrapper<UserContact>()
                    .eq(UserContact::getUserId,userContactApply.getApplyUserId())
                    .eq(UserContact::getContactId,userContactApply.getReceiveUserId()));
            return UserContactStatusEnum.FRIEND.getStatus();
        }

        UserContactApply apply = this.getOne(new LambdaQueryWrapper<UserContactApply>()
                .eq(UserContactApply::getApplyUserId,userContactApply.getApplyUserId())
                .eq(UserContactApply::getReceiveUserId,userContactApply.getReceiveUserId()));
        if(apply==null){
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            userContactApply.setLastApplyTime(LocalDateTime.now());
            this.save(userContactApply);
        }else {
            UserContactApply update = new UserContactApply();
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            userContactApply.setLastApplyTime(LocalDateTime.now());
            this.update(update,new LambdaQueryWrapper<UserContactApply>()
                    .eq(UserContactApply::getApplyUserId,userContactApply.getApplyId())
                    .eq(UserContactApply::getReceiveUserId, userContactApply.getReceiveUserId()));
        }

        MessageSendDto messageSendDto = new MessageSendDto<>();
        messageSendDto.setMessageSend2Type(MessageSend2TypeEnum.USER.getType());
        messageSendDto.setMessageType(MessageTypeEnum.USER_CONTACT_APPLY.getType());
        messageSendDto.setReceiveUserId(userContactApply.getReceiveUserId());
        messageHandler.sendMessage(messageSendDto);
        return UserContactApplyStatusEnum.INIT.getStatus();
    }

    @Override
    public void dealWithApply(String applyUserId, String userId, String nickName, Integer status) {
        UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(status);
        if(statusEnum==null||UserContactApplyStatusEnum.INIT == statusEnum){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserContactApply apply = this.getOne(new LambdaQueryWrapper<UserContactApply>()
                .eq(UserContactApply::getApplyUserId,applyUserId)
                .eq(UserContactApply::getReceiveUserId,userId));
        if(apply==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        if(UserContactApplyStatusEnum.PASS == statusEnum){
            UserContact userContact = new UserContact();
            userContact.setContactId(userId);
            userContact.setUserId(applyUserId);
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContact.setLastUpdateTime(LocalDateTime.now());
            userContactService.saveOrUpdate(userContact);

            userContact.setUserId(userId);
            userContact.setContactId(applyUserId);
            userContactService.saveOrUpdate(userContact);
        }

        UserContactApply updateApply = new UserContactApply();
        updateApply.setStatus(status);
        this.update(updateApply,new LambdaQueryWrapper<UserContactApply>()
                .eq(UserContactApply::getApplyUserId,applyUserId)
                .eq(UserContactApply::getReceiveUserId, userId));

        MessageSendDto sendDto = new MessageSendDto();
        sendDto.setMessageSend2Type(MessageSend2TypeEnum.USER.getType());
        sendDto.setMessageType(MessageTypeEnum.USER_CONTACT_DEAL_WITH.getType());
        sendDto.setReceiveUserId(applyUserId);
        sendDto.setSendUserNickName(nickName);
        sendDto.setMessageContent(status);
        messageHandler.sendMessage(sendDto);
    }


}
