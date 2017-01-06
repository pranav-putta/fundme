package net.codealizer.fundme.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import net.codealizer.fundme.Config;
import net.codealizer.fundme.assets.DatabaseItem;
import net.codealizer.fundme.assets.DatabaseUser;
import net.codealizer.fundme.assets.Item;
import net.codealizer.fundme.assets.Organization;
import net.codealizer.fundme.assets.User;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail extends AsyncTask<Void, Void, Void> {

    //Declaring Variables
    private Context context;
    private Session session;

    //Information to send email
    private String email;
    private String subject;
    private String message;

    //Progressdialog to show while sending email
    private ProgressDialog progressDialog;

    //Class Constructor
    public SendMail(Context context, User user, DatabaseUser user2, Organization organization, DatabaseItem item, boolean user1) {
        //Initializing variables
        this.context = context;
        if (user1) {
            this.email = user.getEmail();
            this.subject = "Shipping address";
            this.message = "Hello " + user.getName() + ", \nYour item, \"" + item.title + "\" has been sold to " + user2.firstName + " " + user2.lastName + " for $" + item.price + ". " +
                    "The money received has all been donated to your preferred organization - " + organization.getTitle() + ". \n\n\n RECIPIENT NAME: " + user2.firstName + " " + user2.lastName
                    + "\n ADDRESS: " + user2.address;
        } else {
            this.email = user2.email;
            this.subject = "Receipt";
            this.message = "Hello " + user2.firstName + " " + user2.lastName + ", \n Thank you for purchasing \"" + item.title + "\". Your total purchasing cost was $" +
                    item.price + ". The item will be shipped to you" +
                    " soon. Thank you.";
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Showing progress dialog while sending email
        progressDialog = ProgressDialog.show(context, "Sending message", "Please wait...", false, false);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        //Dismissing the progress dialog
        progressDialog.dismiss();
        //Showing a success message
        Toast.makeText(context, "Message Sent", Toast.LENGTH_LONG).show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        try {
            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(Config.EMAIL, Config.PASSWORD);
                        }
                    });


            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(Config.EMAIL));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(email));
            message.setSubject(subject);
            message.setText(this.message);

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }
}