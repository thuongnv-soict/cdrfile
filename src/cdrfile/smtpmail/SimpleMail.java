package cdrfile.smtpmail;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import javax.mail.*;

import javax.mail.internet.*;

import javax.mail.Authenticator;

import javax.mail.PasswordAuthentication;



import java.util.Properties;





/*public class SimpleMail {



    private static final String SMTP_HOST_NAME = "smtp.gmail.com";

    private static final String SMTP_AUTH_USER = "ntkhaiedu0108";

    private static final String SMTP_AUTH_PWD  = "nquyen";



    public static void main(String[] args) throws Exception{

       new SimpleMail().test();

    }



    public void test() throws Exception{

        Properties props = new Properties();

        props.put("mail.transport.protocol", "smtp");

        props.put("mail.smtp.host", SMTP_HOST_NAME);
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.auth", "true");



        Authenticator auth = new SMTPAuthenticator();

        Session mailSession = Session.getDefaultInstance(props, auth);

        // uncomment for debugging infos to stdout

         mailSession.setDebug(true);

        Transport transport = mailSession.getTransport();



        MimeMessage message = new MimeMessage(mailSession);

        message.setContent("This is a test", "text/plain");

        message.setFrom(new InternetAddress("khaint@ekgis.com.vn"));

        message.addRecipient(Message.RecipientType.TO,

             new InternetAddress("khaint@ekgis.com.vn"));



        transport.connect();

        transport.sendMessage(message,

        message.getRecipients(Message.RecipientType.TO));

        transport.close();

    }



    private class SMTPAuthenticator extends javax.mail.Authenticator {

        public PasswordAuthentication getPasswordAuthentication() {
           String username = SMTP_AUTH_USER;
           String password = SMTP_AUTH_PWD;
           return new PasswordAuthentication(username, password);

        }

    }

}*/
import java.security.Security;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class SimpleMail
{
        private String mailhost = "smtp.gmail.com";

        public synchronized void sendMail(String subject, String body, String sender, String recipients)
                                                                                                                                                                   throws Exception
        {

                Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

                Properties props = new Properties();
                props.setProperty("mail.transport.protocol", "smtp");
                props.setProperty("mail.host", mailhost);
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", "465");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.fallback", "false");
                props.setProperty("mail.smtp.quitwait", "false");

                Session session = Session.getDefaultInstance(props,
                                new javax.mail.Authenticator()
                {
                        protected PasswordAuthentication getPasswordAuthentication()
                        { return new PasswordAuthentication("ntkhaiedu0108","nquyen");	}
                });

                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(sender));
                message.setSubject(subject);
                message.setContent(body, "text/plain");
                if (recipients.indexOf(',') > 0)
                                        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
                else
                                        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));


                Transport.send(message);

        }


        public static void main(String args[]) throws Exception
        {
                SimpleMail mailutils = new SimpleMail();
                mailutils.sendMail("test", "test", "ntkhaiedu0108@gmail.com", "khaint@ekgis.com.vn");

        }

}

