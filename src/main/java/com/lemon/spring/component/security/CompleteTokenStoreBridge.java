package com.lemon.spring.component.security;

import com.lemon.framework.springsecurity.auth.AuthenticationToken;
import com.lemon.framework.springsecurity.jwt.bridge.TokenStoreBridge;
import com.lemon.framework.web.data.UserInfo;
import com.lemon.spring.component.audit.AuditAware;
import com.lemon.spring.domain.TokenStore;
import com.lemon.spring.domain.User;
import com.lemon.spring.repository.TokenStoreRepository;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@DependsOn("tokenStoreRepository")
@Component
public class CompleteTokenStoreBridge implements TokenStoreBridge {

    @Inject
    private TokenStoreRepository tokenStoreRepository;

    @Inject
    private AuditAware auditAware;

    @Override
    public String tokenByUsername(String username) {
        TokenStore tokenStore = tokenStoreRepository.findByUsername(username);
        if(tokenStore !=null) return tokenStore.getToken();
        return null;
    }

    @Override
    public boolean isActiveToken(String token) {
        return tokenStoreRepository.findByActiveStatus(token,true)!=null;
    }

    @Override
    public boolean isActiveToken(String token,String remoteIpAddress) {
        return tokenStoreRepository.findByTokenIpAndStatus(token,remoteIpAddress,true)!=null;
    }

    @Override
    public boolean isActiveToken(String token, Date validateDate) {
        TokenStore tokenStore=tokenStoreRepository.findByValidateDate(token,validateDate);
        return tokenStore!=null;
    }

    @Override
    public void saveToken(String token, AuthenticationToken authenticationToken, Date validateDate, UserInfo userInfo) {
        TokenStore tokenStore=new TokenStore();
        tokenStore.setToken(token);
        tokenStore.setUser(new User(authenticationToken.getUserId()));
        tokenStore.setValidateDate(validateDate);
        tokenStore.setActive(true);
        tokenStore.setIpAddress(userInfo.getIpAddress());
        auditAware.awareCreate(tokenStore);
        tokenStoreRepository.save(tokenStore);
    }

    @Override
    public void updateTokenActiveStatus(String token, boolean activeStatus) {
        TokenStore tokenStore=tokenStoreRepository.findByToken(token);
        if(tokenStore !=null) {
            tokenStore.setActive(activeStatus);
            auditAware.awareUpdate(tokenStore);
            tokenStoreRepository.save(tokenStore);
        }
    }

    @Override
    public void deactivateAllByUidAndIpAddress(BigInteger userId, String ipAddress) {
        deactivate(tokenStoreRepository.findAllByUidAndIp(userId,ipAddress));
    }

    @Override
    public int totalTokenByActiveStatus(BigInteger userId, boolean activeStatus) {
        return (int) tokenStoreRepository.countByUidAndActiveStatus(userId,activeStatus);
    }

    @Override
    public void deactivateAllByUid(BigInteger userId) {
        deactivate(tokenStoreRepository.findAllByUid(userId));
    }

    @Override
    public void deleteToken(String token) {
        TokenStore tokenStore=tokenStoreRepository.findByToken(token);
        if(tokenStore!=null) tokenStoreRepository.delete(tokenStore);
    }

    @Override
    public int countTokenByDate(LocalDate fromDate, LocalDate toDate,BigInteger userId){
        return (int)tokenStoreRepository.countTokenByDate(fromDate,toDate,userId);
    }

    @Override
    public int countTokenByDate(LocalDate now,BigInteger userId){
        return (int)tokenStoreRepository.countTokenByDate(now,userId);
    }

    @Override
    public int countTokenByUserIdAndStatus(BigInteger userId, boolean activeStatus){
        return (int) tokenStoreRepository.countByUidAndActiveStatus(userId,activeStatus);
    }

    @Override
    public void deactivateOlderTokenCountByUid(int count, BigInteger userId){
        deactivate(tokenStoreRepository.findAllByUid(new PageRequest(0,count),userId,true));
    }

    @Override
    public int countTokenByUserIdIpAndStatus(BigInteger userId, String ipAddress, boolean activeStatus){
        return (int) tokenStoreRepository.countByUidIpAndActiveStatus(userId,ipAddress,activeStatus);
    }

    @Override
    public void deactivateOlderTokenCountByUidAndIp(int count, BigInteger userId, String ipAddress){
        deactivate(tokenStoreRepository.findAllByUidAndIp(new PageRequest(0,count),userId,ipAddress,true));
    }

    private void deactivate(List<TokenStore> tokenStores) {
        tokenStores.forEach(tokenStore->{
            tokenStore.setActive(false);
            auditAware.awareUpdate(tokenStore);
            tokenStoreRepository.save(tokenStore);
        });
    }
}
