package at.tfr.pfad.view;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;

import at.tfr.pfad.dao.MailMessageRepository;
import at.tfr.pfad.model.MailMessage;
import at.tfr.pfad.view.MailerBean.MailConfig;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class SmsSender {

	private Logger log = Logger.getLogger(getClass());

	@Inject
	private MailMessageRepository messageRepo;

	public MailMessage sendMail(MailMessage msg, MailConfig config, boolean saveText)
			throws MessagingException, ClientProtocolException, IOException, CloneNotSupportedException {

		msg.setSms(true);
		MailMessage msgOrig = msg;
		msg = msg.getClone();
		if (!saveText) {
			msg.setText(null);
		}

		msg = messageRepo.saveAndFlush(msg);
		try {

			CredentialsProvider creds = new BasicCredentialsProvider();
			creds.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(config.getSmsUsername(), config.getSmsPassword()));
			CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(creds).build();

			try {
				HttpPost httpPost = new HttpPost(config.getSmsService());
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("recipients", msgOrig.getReceiver()));
				nvps.add(new BasicNameValuePair("msgtext", msgOrig.getText().replaceAll("\t","")));
				httpPost.setEntity(new UrlEncodedFormEntity(nvps));
				CloseableHttpResponse response = httpclient.execute(httpPost);

				try {
					log.info(response.getStatusLine());
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						log.info("sent SMS: " + IOUtils.toString(response.getEntity().getContent()));
					} else {
						log.info("failed to send SMS: " + IOUtils.toString(response.getEntity().getContent()));
						throw new MessagingException(
								"failed to send SMS for: " + msg.getReceiver() + " : " + response.getStatusLine());
					}
				} finally {
					response.close();
				}
			} finally {
				httpclient.close();
			}

			msg.setCreated(new Date());
			return messageRepo.saveAndFlushAndRefresh(msg);

		} catch (Exception e) {
			log.warn("cannot send: " + msg + " : " + e);
			messageRepo.removeAndFlush(msg);
			throw e;
		}
	}
}
