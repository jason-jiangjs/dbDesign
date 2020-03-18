package org.dbm.dbd.web.login;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 扩展spring security的User对象
 */
public class CustomerUserDetails extends User {

    private static final long serialVersionUID = 1L;

    // 用户ID(主键)
    private Long userId;
    // 登录帐号
    private String account;
    private int status;
    private Long favorite;
    private String fromSrc;
    private boolean registered;

    public CustomerUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, true, true, true, true, authorities);
    }

    public CustomerUserDetails(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }

    public long getUserId() {
        return userId;
    }
    public void setUserId(Long innId) {
        this.userId = innId;
    }

    public String getAccount() {
        return account;
    }
    public void setAccount(String account) {
        this.account = account;
    }

    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

    public Long getFavorite() {
        return favorite;
    }
    public void setFavorite(Long favorite) {
        this.favorite = favorite;
    }

    public String getFromSrc() {
        return fromSrc;
    }
    public void setFromSrc(String fromSrc) {
        this.fromSrc = fromSrc;
    }

    public boolean isRegistered() {
        return registered;
    }
    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

}
