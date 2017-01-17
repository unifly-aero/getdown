//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2014 Three Rings Design, Inc.
// https://raw.github.com/threerings/getdown/master/LICENSE

package com.threerings.getdown.launcher;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.*;

import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.Spacer;
import com.samskivert.swing.VGroupLayout;
import com.samskivert.text.MessageUtil;
import com.samskivert.util.StringUtil;

import static com.threerings.getdown.Log.log;
import static com.threerings.getdown.launcher.ProxyInfo.ProxyInfoBuilder.aProxyInfo;

/**
 * Displays an interface with which the user can configure their proxy
 * settings.
 */
public class ProxyPanel extends JPanel
    implements ActionListener
{
    public ProxyPanel (Getdown getdown, ResourceBundle msgs)
    {
        _getdown = getdown;
        _msgs = msgs;

        setLayout(new VGroupLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(new JLabel(get("m.configure_proxy")));
        add(new Spacer(5, 5));

        JPanel row = createTextPanel("m.proxy_host");
        row.add(_host = new SaneTextField());
        add(row);

        row = createTextPanel("m.proxy_port");
        row.add(_port = new SaneTextField());
        add(row);

        row = createTextPanel("m.proxy_user");
        row.add(_user = new SaneTextField());
        add(row);

        row = createTextPanel("m.proxy_password");
        row.add(_password = new JPasswordField(){
            @Override
            public Dimension getPreferredSize () {
                return setMaxWidth(super.getPreferredSize());
            }
        });
        add(row);

        add(new Spacer(5, 5));
        add(new JLabel(get("m.proxy_extra")));

        row = GroupLayout.makeButtonBox(GroupLayout.CENTER);
        JButton button;
        row.add(button = new JButton(get("m.proxy_ok")));
        button.setActionCommand("ok");
        button.addActionListener(this);
        row.add(button = new JButton(get("m.proxy_cancel")));
        button.setActionCommand("cancel");
        button.addActionListener(this);
        add(row);

        // set up any existing proxy defaults
        if(getdown.proxyInfo != null){
            _host.setText(getdown.proxyInfo.getHost());
            _port.setText(""+getdown.proxyInfo.getPort());
            _user.setText(getdown.proxyInfo.getUser());
        }
    }

    private JPanel createTextPanel(String key){
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(createLabel(key));
        return row;
    }

    private JLabel createLabel(String key){
        JLabel label = new JLabel(get(key));
        label.setPreferredSize(new Dimension(100, label.getPreferredSize().height));
        return label;
    }

    // documentation inherited
    @Override
    public void addNotify  ()
    {
        super.addNotify();
        _host.requestFocusInWindow();
    }

    // documentation inherited
    @Override
    public Dimension getPreferredSize ()
    {
        // this is annoyingly hardcoded, but we can't just force the width
        // or the JLabel will claim a bogus height thinking it can lay its
        // text out all on one line which will booch the whole UI's
        // preferred size
        return new Dimension(500, 450);
    }

    // documentation inherited from interface
    public void actionPerformed (ActionEvent e)
    {
        String cmd = e.getActionCommand();
        if (cmd.equals("ok")) {
            // communicate this info back to getdown
            if(!StringUtil.isBlank(_host.getText()) && StringUtil.isBlank(_port.getText())){
                JOptionPane.showMessageDialog(this,
                        get("m.noPort.message"),
                        get("m.noPort.title"),
                        JOptionPane.ERROR_MESSAGE);
            }
            else{
                Integer port ;
                try{
                    port = Integer.valueOf(_port.getText());
                    _getdown.configureProxy(aProxyInfo()
                            .withHost(_host.getText())
                            .withPort(port)
                            .withUser(_user.getText())
                            .withPassword(_password.getPassword()!=null && _password.getPassword().length > 0 ? String.valueOf(_password.getPassword()) : null)
                            .build());
                }
                catch(NumberFormatException nfe){
                    JOptionPane.showMessageDialog(this,
                            get("m.invalidPort.message"),
                            get("m.invalidPort.title"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        } else {
            // they canceled, we're outta here
            System.exit(0);
        }
    }

    /** Used to look up localized messages. */
    protected String get (String key)
    {
        // if this string is tainted, we don't translate it, instead we
        // simply remove the taint character and return it to the caller
        if (MessageUtil.isTainted(key)) {
            return MessageUtil.untaint(key);
        }
        try {
            return _msgs.getString(key);
        } catch (MissingResourceException mre) {
            log.warning("Missing translation message '" + key + "'.");
            return key;
        }
    }

    protected static class SaneTextField extends JTextField
    {
        @Override
        public Dimension getPreferredSize () {
            return setMaxWidth(super.getPreferredSize());
        }
    }

    private static Dimension setMaxWidth(Dimension d) {
        d.width = Math.max(d.width, 150);
        return d;
    }

    protected Getdown _getdown;
    protected ResourceBundle _msgs;

    protected JTextField _host;
    protected JTextField _port;
    protected JTextField _user;
    protected JPasswordField _password;
}
