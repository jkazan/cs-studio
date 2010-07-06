package org.csstudio.email;

import org.junit.Test;

/** JUnit test of the Mailer
 *  <p>
 *  Will not work without adjusting the host, image file name etc.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EMailSenderTest
{
    /**
     * Please, overwrite these settings to your host and email address.
     */
    final private static String HOST = "krynfs.desy.de";
    final private static String FROM = "Bastian Knerr <bastian.knerr@desy.de>";
    final private static String TO = FROM;


    @Test
    public void testMailer() throws Exception
    {
        /**
         * Please, overwrite these settings to your host and email address.
         */
        final EMailSender mailer = new EMailSender(HOST, FROM, TO, "Test Subject");

        mailer.addText("Hello, this is a test");
        mailer.attachText("../org.csstudio.email/testfile.txt");
        mailer.attachImage("../org.csstudio.email/test.jpg");
        mailer.attachText("../org.csstudio.email/src/org/csstudio/email/EMailSender.java");
        mailer.close();
    }
}
