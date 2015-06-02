/* This file is part of SafePwdGen, a deterministic password generator.
 * 
 * Copyright 2014-2015 by Dominic Scheurer <dom.scheurer@gmail.com>
 * 
 * SafePwdGen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SafePwdGen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SafePwdGen. If not, see <http://www.gnu.org/licenses/>.
 */

package de.dominicscheurer.passwords;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.dominicscheurer.passwords.util.SystemClipboardInterface;

/**
 * Deterministic generation of passwords from a (secret) seed password and a
 * (potentially public) identifier. Provides choices for password length and
 * occurrence of special chars in the generated password.
 * <p>
 * 
 * This class enables the use of SafePwdGen as a standalone
 * command line program. Options will be displayed to command
 * line by calling "java -jar SafePwdGen.jar" without arguments.
 * 
 * @author Dominic Scheurer
 */
public class Main {

    private final static int STD_PWD_LENGTH = 20;
    private final static int MAX_PWD_LENGTH_64 = 86;
    private final static int MAX_PWD_LENGTH_71 = 84;

    // Some message
    private static final String CLIPBOARD_COPIED_MSG = "\nI tried to copy the generated password to your system clipboard.\n"
            + "This may have failed if you are using Gnome or some derivative of it.";
    private static final String GENERATED_PASSWORD = "\nGenerated Password: ";
    private static final String PASSWORD_SIZE_TOO_BIG = "Requested password size too big, reset to ";

    /**
     * @param args
     *            Command line arguments (see code or output of program when
     *            started with no arguments).
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        Options options = new Options();

        Option seedPwdOpt = OptionBuilder
                .withArgName("Seed Password")
                .isRequired()
                .hasArg()
                .withDescription("Password used as a seed")
                .withLongOpt("seed-password")
                .create("s");

        Option serviceIdOpt = OptionBuilder
                .withArgName("Service Identifier")
                .isRequired()
                .hasArg()
                .withDescription(
                        "The service that the password is created for, e.g. facebook.com")
                .withLongOpt("service-identifier")
                .create("i");

        Option pwdLengthOpt = OptionBuilder
                .withArgName("Password Length")
                .withType(Integer.class)
                .hasArg()
                .withDescription("Length of the password in characters")
                .withLongOpt("pwd-length")
                .create("l");

        Option specialChars = OptionBuilder
                .withArgName("With special chars (TRUE|false)")
                .withType(Boolean.class)
                .hasArg()
                .withDescription(
                        "Set to true if special chars !-_?=@/+* are desired, else false")
                .withLongOpt("special-chars")
                .create("c");

        Option helpOpt = OptionBuilder
                .withDescription("Prints this help message")
                .withLongOpt("help")
                .create("h");

        options.addOption(seedPwdOpt);
        options.addOption(serviceIdOpt);
        options.addOption(pwdLengthOpt);
        options.addOption(specialChars);
        options.addOption(helpOpt);

        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                SafePwdGen.printHelp(options);
                System.exit(0);
            }

            int pwdLength = STD_PWD_LENGTH;
            if (cmd.hasOption("l")) {
                pwdLength = new Integer(cmd.getOptionValue("l"));
            }

            boolean useSpecialChars = true;
            if (cmd.hasOption("c")) {
                useSpecialChars = new Boolean(cmd.getOptionValue("c"));
            }

            if (pwdLength > MAX_PWD_LENGTH_64 && !useSpecialChars) {
                System.out.println(PASSWORD_SIZE_TOO_BIG + MAX_PWD_LENGTH_64);
            }

            if (pwdLength > MAX_PWD_LENGTH_71 && useSpecialChars) {
                System.out.println(PASSWORD_SIZE_TOO_BIG + MAX_PWD_LENGTH_71);
            }

            String pwd = SafePwdGen.createPwd(
                    cmd.getOptionValue("s"),
                    cmd.getOptionValue("i"),
                    pwdLength,
                    useSpecialChars);

            System.out.print(GENERATED_PASSWORD);
            System.out.println(pwd);
            System.out.println(CLIPBOARD_COPIED_MSG);
            SystemClipboardInterface.copy(pwd);
        } catch (ParseException e) {
            System.out.println(e.getLocalizedMessage());
            SafePwdGen.printHelp(options);
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getLocalizedMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

}
