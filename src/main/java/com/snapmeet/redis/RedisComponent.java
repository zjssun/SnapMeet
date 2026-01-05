package com.snapmeet.redis;

import com.snapmeet.constants.Constants;
import com.snapmeet.entity.dto.MeetingJoinDto;
import com.snapmeet.entity.dto.MeetingMemberDTO;
import com.snapmeet.entity.dto.TokenUserInfoDto;
import com.snapmeet.enums.MeetingMemberStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;

    //存储验证码答案
    public String saveCheckCode(String code){
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey,code,Constants.REDIS_KEY_EXPIRES_ONE_MIN);
        return  checkCodeKey;
    }
    //获取验证码答案
    public String getCheckCode(String checkCodeKey){
        return (String)redisUtils.get(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey);
    }
    //清除验证码
    public void cleanCheckCode(String checkCodeKey){
        redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey);
    }

    //保存TokenUserInfoDto
    public void saveTokenUserInfoDto(TokenUserInfoDto tokenUserInfoDto){
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN+tokenUserInfoDto.getToken(),tokenUserInfoDto,Constants.REDIS_KEY_EXPIRES_DAY);
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN_USERID+tokenUserInfoDto.getUserId(),tokenUserInfoDto.getToken(),Constants.REDIS_KEY_EXPIRES_DAY);
    }

    //根据token获取TokenUserInfoDto
    public TokenUserInfoDto getTokenUserInfoDto(String token){
        return (TokenUserInfoDto)redisUtils.get(Constants.REDIS_KEY_WS_TOKEN+token);
    }

    //根据用户id获取TokenUserInfo
    public TokenUserInfoDto getTokenUserInfoDtoByUserId(String UserId){
        String token = (String) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID+UserId);
        return getTokenUserInfoDto(token);
    }

    public void add2Meeting(String meetingId, MeetingMemberDTO meetingMemberDTO) {
        redisUtils.hmSet(Constants.REDIS_KEY_MEETING_ROOM+meetingId,meetingMemberDTO.getUserId(),meetingMemberDTO);
    }

    public List<MeetingMemberDTO> getMeetingMemberList(String meetingId){
        List<MeetingMemberDTO>  meetingMemberDTOList = redisUtils.hvals(Constants.REDIS_KEY_MEETING_ROOM+meetingId);
        meetingMemberDTOList = meetingMemberDTOList.stream().sorted(Comparator.comparing(MeetingMemberDTO::getJoinTime)).collect(Collectors.toList());
        return meetingMemberDTOList;
    }

    public MeetingMemberDTO getMeetingMember(String meetingId, String userId){
        return (MeetingMemberDTO)redisUtils.hmGet(Constants.REDIS_KEY_MEETING_ROOM+meetingId,userId);
    }

    public Boolean exitMeeting(String meetingId, String userId, MeetingMemberStatusEnum statusEnum) {
        MeetingMemberDTO meetingMemberDTO = getMeetingMember(meetingId, userId);
        if(meetingMemberDTO == null){
            return false;
        }
        meetingMemberDTO.setStatus(statusEnum.getStatus());
        add2Meeting(meetingId,meetingMemberDTO);
        return true;
    }
}
