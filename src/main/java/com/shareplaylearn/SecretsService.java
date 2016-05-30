/**
 * Copyright 2015-2016 Stuart Smith
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with the paho MQTT client library (or a modified version of that library),
 * containing parts covered by the terms of EPL,
 * the licensors of this Program grant you additional permission to convey the resulting work.
 *
 */
package com.shareplaylearn;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;

/**
 * Created by stu on 4/19/15.
 */
public class SecretsService {
    public static String googleClientId;
    public static String googleClientSecret;
    public static String amazonClientId;
    public static String amazonClientSecret;
    public static String testOauthUsername;
    public static String testOauthPassword;
    public static String testDaemonUsername;
    public static String testDaemonPassword;
    public static String testClientId;
    public static String testClientSecret;

    private static final String GOOGLE_ID_KEY = "GoogleId";
    private static final String GOOGLE_SECRET_KEY = "GoogleSecret";
    private static final String AMAZON_ID_KEY = "AmazonId";
    private static final String AMAZON_SECRET = "AmazonSecret";
    private static final String TEST_OAUTH_USER_KEY = "TestOauthUser";
    private static final String TEST_OAUTH_PASSWORD_KEY = "TestOauthPassword";
    private static final String TEST_DAEMON_USER_KEY = "TestStormpathId";
    private static final String TEST_DAEMON_PASSWORD_KEY = "TestStormpathSecret";
    private static final String TEST_CLIENT_ID_KEY = "TestStormpathClientId";
    private static final String TEST_CLIENT_SECRET_KEY = "TestStormpathClientSecret";

    static {
        java.nio.file.Path secretsFile = FileSystems.getDefault().getPath("/etc/shareplaylearn.secrets");
        try {
            List<String> lines = Files.readAllLines(secretsFile, StandardCharsets.UTF_8);
            for( String line : lines ) {
                if( line.startsWith(GOOGLE_ID_KEY) ) {
                    googleClientId = getConfigValue(line);
                }
                else if( line.startsWith(GOOGLE_SECRET_KEY) ) {
                    googleClientSecret = getConfigValue(line);
                }
                else if(line.startsWith(AMAZON_ID_KEY)){
                    amazonClientId = getConfigValue(line);
                }
                else if(line.startsWith(AMAZON_SECRET)){
                    amazonClientSecret = getConfigValue(line);
                }
                else if(line.startsWith(TEST_OAUTH_USER_KEY)){
                    testOauthUsername = getConfigValue(line);
                }
                else if(line.startsWith(TEST_OAUTH_PASSWORD_KEY)){
                    testOauthPassword = getConfigValue(line);
                }
                else if(line.startsWith(TEST_DAEMON_USER_KEY)){
                    testDaemonUsername = getConfigValue(line);
                }
                else if(line.startsWith(TEST_DAEMON_PASSWORD_KEY)){
                    testDaemonPassword = getConfigValue(line);
                }
                else if(line.startsWith(TEST_CLIENT_ID_KEY)){
                    testClientId = getConfigValue(line);
                }
                else if(line.startsWith(TEST_CLIENT_SECRET_KEY)){
                    testClientSecret = getConfigValue(line);
                }

            }
            if( googleClientId == null ) {
                System.out.println("Warning: google client id not read in, oauth callback will fail");
            }
            if( googleClientSecret == null ) {
                System.out.println("Warning: google secret not read in, oauth callback will fail");
            }
            if( amazonClientId == null ) {
                System.out.println("Warning: Amazon client id not read in, data access will fail");
            }
            if( amazonClientSecret == null ) {
                System.out.println("Warning: Amazon client secret not read in, data access will fail");
            }
            //just to confirm this is the code that is indeed running on the server now.
            System.out.println("Secrets file read in.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Warning, failed to read secrets file, will not be able to login users or access stored data." + e.getMessage());
        }
    }

    public static String getConfigValue( String line )
    {
        String[] kv = line.split("=");
        if( kv.length != 2 ) {
            System.err.println("Warning: badly formatted line: " + line );
            return "";
        }
        return kv[1].trim();
    }

}