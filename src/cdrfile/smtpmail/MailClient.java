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


public class MailClient {
  private static final String SMTP_HOST_NAME = "smtp.vms.com.vn";
  private static final String SMTP_AUTH_USER = "huonghh@vms.com.vn";
  private static final String SMTP_AUTH_PWD = "hoanghuong";
  private static final String MAIL_TO = "khaint@ekgis.com.vn";
  private static final String MAIL_FROM = "huonghh@vms.com.vn";
  private static final String MAIL_CC = "toantn@vms.com.vn";
  private static final String MAIL_SUBJ = "Test mail with  authentication";
  private static final String MAIL_TEXT = "Test mail with  authentication! Hello welcome!";

  public MailClient() {
  }

  private void sendMail() throws Exception {
    java.util.Properties props = new java.util.Properties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.starttls.enable","true");
    props.put("mail.smtp.host", SMTP_HOST_NAME);
    props.put("mail.smtp.auth", "false");
    Authenticator auth = new SMTPAuthenticator();
    Session mailSession = Session.getDefaultInstance(props, null);
    //Store store = mailSession.getStore("pop3");
    //store.connect(SMTP_HOST_NAME, SMTP_AUTH_PWD, SMTP_AUTH_PWD);
    Message msg = new MimeMessage(mailSession);
    InternetAddress[] toAddrs = null, ccAddrs = null;
    if (MAIL_TO != null) {
      toAddrs = InternetAddress.parse(MAIL_TO, false);
      msg.setRecipients(Message.RecipientType.TO, toAddrs);
    }
    else {
      throw new MessagingException("No \"To\" address specified");
    }

    if (MAIL_CC != null) {
      ccAddrs = InternetAddress.parse(MAIL_CC, false);
      msg.setRecipients(Message.RecipientType.CC, ccAddrs);
    }

    if (MAIL_SUBJ != null) {
      msg.setSubject(MAIL_SUBJ);
    }
    msg.setFrom(new InternetAddress(MAIL_FROM));

    if (MAIL_TEXT != null) {
      msg.setText(MAIL_TEXT);
    }
    Transport.send(msg);
  }

  public static void main(String[] agrs) {

    MailClient client = new MailClient();
    try {
      client.sendMail();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

  }
  private class SMTPAuthenticator extends Authenticator {

       public PasswordAuthentication getPasswordAuthentication()
       {
          return new PasswordAuthentication(SMTP_AUTH_USER, SMTP_AUTH_PWD);

       }

    }

}
