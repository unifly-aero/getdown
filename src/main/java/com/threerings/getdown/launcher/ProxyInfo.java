package com.threerings.getdown.launcher;


import com.samskivert.util.StringUtil;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ProxyInfo {

    private String host;
    private int port;
    private String user;
    private String password;
    private Proxy.Type type;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Proxy.Type getType() {
        return type;
    }

    public void setType(Proxy.Type type) {
        this.type = type;
    }

    public Proxy getProxy(){
        if(type == Proxy.Type.DIRECT){
            return Proxy.NO_PROXY;
        }
        return new Proxy(type, new InetSocketAddress(host, port));
    }

    public boolean hasAuthentication(){
        return getUser() != null && getUser().trim().length() > 0;
    }

    public Authenticator getAuthenticator(){
        return new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user,
                        password.toCharArray());
            }
        };
    }

    public Map<String, String> getProxySystemProperties(){
        if(type == Proxy.Type.DIRECT){
            return Collections.emptyMap();
        }
        Map<String, String> proxyProperties = new HashMap<String, String>();
        proxyProperties.put("proxySet", "true");
        if(type == Proxy.Type.HTTP){
            proxyProperties.put("http.proxyHost", host);
            proxyProperties.put("https.proxyHost", host);
            proxyProperties.put("http.proxyPort", ""+port);
            proxyProperties.put("https.proxyPort", ""+port);
            if(hasAuthentication()){
                proxyProperties.put("http.proxyUser", user);
                proxyProperties.put("https.proxyUser", user);
                if(!StringUtil.isBlank(password)){
                    proxyProperties.put("http.proxyPassword", password);
                    proxyProperties.put("https.proxyPassword", password);
                }
            }
        }
        else{
            proxyProperties.put("socksProxyHost", host);
            proxyProperties.put("socksProxyPort", ""+port);
            if(hasAuthentication()) {
                proxyProperties.put("java.net.socks.username", user);
                if(!StringUtil.isBlank(password)) {
                    proxyProperties.put("java.net.socks.password", password);
                }
            }
        }
        return proxyProperties;
    }

    public static final class ProxyInfoBuilder {
        private String host;
        private int port;
        private String user;
        private String password;
        private Proxy.Type type;

        private ProxyInfoBuilder() {
        }

        public static ProxyInfoBuilder aProxyInfo() {
            return new ProxyInfoBuilder();
        }

        public ProxyInfoBuilder withHost(String host) {
            this.host = host;
            return this;
        }

        public ProxyInfoBuilder withPort(int port) {
            this.port = port;
            return this;
        }

        public ProxyInfoBuilder withUser(String user) {
            this.user = user;
            return this;
        }

        public ProxyInfoBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public ProxyInfoBuilder withType(Proxy.Type type) {
            this.type = type;
            return this;
        }

        public ProxyInfo build() {
            ProxyInfo proxyInfo = new ProxyInfo();
            proxyInfo.setHost(host);
            proxyInfo.setPort(port);
            proxyInfo.setUser(user);
            proxyInfo.setPassword(password);
            proxyInfo.setType(type);
            return proxyInfo;
        }
    }
}
