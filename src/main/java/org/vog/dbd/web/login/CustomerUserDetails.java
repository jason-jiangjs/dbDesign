/*
 * #%L
 * BroadleafCommerce Profile
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.vog.dbd.web.login;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created in order to utilize the Customer's primary key to salt passwords with. This allows username changes without
 * requiring a password reset since the primary key should never change.
 */
public class CustomerUserDetails extends User {

    private static final long serialVersionUID = 1L;

    private long innId;
    private String account;
    private int status;
    private Long favorite;
    private Map<String, Object> context = new HashMap<>();

    public CustomerUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, true, true, true, true, authorities);
    }

    public CustomerUserDetails(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }

    public long getId() {
        return innId;
    }
    public void setId(long innId) {
        this.innId = innId;
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

    public void putContext(String key, Object val) {
        context.put(key, val);
    }
    public Object getContextAttr(String key) {
        return context.get(key);
    }
}
