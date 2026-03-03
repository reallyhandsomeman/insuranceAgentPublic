package com.hi.insurance_agent.ssh2server;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class SSHTunnelConfig {
    @Value("${ssh.tunnel.enabled}")
    private boolean enabled;

    @Value("${ssh.tunnel.user}")
    private String sshUser;

    @Value("${ssh.tunnel.host}")
    private String sshHost;

    @Value("${ssh.tunnel.port}")
    private int sshPort;

    @Value("${ssh.tunnel.local-port}")
    private int localPort;

    @Value("${ssh.tunnel.remote-host}")
    private String remoteHost;

    @Value("${ssh.tunnel.remote-port}")
    private int remotePort;

    /**
     * 注册 SSH 隧道 Bean，并确保在应用启动时立即执行
     */
    @Bean(initMethod = "connect", destroyMethod = "disconnect")
    public JSchSession jschSession() throws JSchException {
        if (!enabled) {
            return null; // 如果未启用 SSH 隧道，返回空 Bean
        }

        JSch jsch = new JSch();
        jsch.addIdentity("~/.ssh/id_ed25519");
        Session session = jsch.getSession(sshUser, sshHost, sshPort);
        session.setConfig("StrictHostKeyChecking", "no");

        return new JSchSession(session, localPort, remoteHost, remotePort);
    }

    /**
     * 包装类，管理 Session 的生命周期
     */
    public static class JSchSession {
        private final Session session;
        private final int localPort;
        private final String remoteHost;
        private final int remotePort;

        public JSchSession(Session session, int localPort, String remoteHost, int remotePort) {
            this.session = session;
            this.localPort = localPort;
            this.remoteHost = remoteHost;
            this.remotePort = remotePort;
        }

        public void connect() throws JSchException {
            session.connect();
            session.setPortForwardingL(localPort, remoteHost, remotePort);
            System.out.printf("SSH 隧道已建立: localhost:%d -> %s:%d%n", localPort, remoteHost, remotePort);
        }

        public void disconnect() {
            if (session != null && session.isConnected()) {
                session.disconnect();
                System.out.println("SSH 隧道已关闭");
            }
        }
    }
}