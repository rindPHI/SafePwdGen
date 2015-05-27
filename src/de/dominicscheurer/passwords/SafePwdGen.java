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
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import uk.co.maxant.util.BaseX;

/**
 * Deterministic generation of passwords from a (secret) seed password and a
 * (potentially public) identifier. Provides choices for password length and
 * occurrence of special chars in the generated password.
 * <p>
 * 
 * Use this class either inside a given project by calling the method
 * {@link #createPwd(String, String, int, boolean)}, or run it as a standalone
 * command line program. Options will be displayed to command line by calling
 * "java -jar SafePwdGen.jar" without arguments.
 * 
 * @author Dominic Scheurer
 */
public class SafePwdGen {

    private final static int STD_PWD_LENGTH = 20;
    private final static int MAX_PWD_LENGTH_64 = 86;
    private final static int MAX_PWD_LENGTH_71 = 84;

    // Some message
    private static final String CLIPBOARD_COPIED_MSG =
            "\nI tried to copy the generated password to your system clipboard.\n"
            + "This may have failed if you are yousing Gnome or some derivative of it.";
    private static final String GENERATED_PASSWORD = "\nGenerated Password: ";
    private static final String PASSWORD_SIZE_TOO_BIG = "Requested password size too big, reset to ";

    /**
     * contains alphanumerics, including both capitals and smalls, and the
     * special signs !-_?=@/+*.
     */
    private static final char[] DICTIONARY_71 = new char[] { '!', '-', '_',
            '?', '=', '/', '+', '*', '@', '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
            'x', 'y', 'z' };

    /**
     * Computes an SHA256 hash from the given input.
     * 
     * @param input
     *            Input to compute a hash from.
     * @return The hashed input.
     * @throws NoSuchAlgorithmException
     *             Thrown if the algorithm "SHA-256" is not present in the
     *             system.
     */
    private static String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer
                    .toString((result[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }

        return sb.toString();
    }

    /**
     * Converts an input to a Base64 encoded String value.
     * 
     * @param input
     *            Input to encode.
     * @return Base64-encoded input value.
     */
    private static String getBase64(String input)
            throws UnsupportedEncodingException {
        return DatatypeConverter.printBase64Binary(input.getBytes("UTF-8"));
    }

    /**
     * Same as {@link #getBase64(String)}, but for the base "X" instead of 64.
     * 
     * @param input
     *            Input to encode.
     * @param dictionary
     *            The characters corresponding to the base "X".
     * @return Base64-encoded input value.
     * @throws UnsupportedEncodingException
     */
    private static String getBaseX(String input, char[] dictionary)
            throws UnsupportedEncodingException {
        BaseX encoder = new BaseX(dictionary);
        return encoder.encode(new BigInteger(input.getBytes()));
    }

    /**
     * Prints the help message for the given command line options.
     * 
     * @param options
     *            Command line options to print the help message for.
     */
    private static void printHelp(Options options) {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("java -jar SavePwdGen.jar [options]", options);
    }

    /**
     * Deterministically creates a password for the given seed and identifier
     * ("service"), of the given length and containing special chars depending
     * on the corresponding flag.
     * 
     * @param seed
     *            Secret seed password.
     * @param service
     *            Identifier, may be public (like "facebook.com")
     * @param length
     *            Desired password length. Maximum length depends on the
     *            underlying dictionary, that is on the specialChars flag (see
     *            options explanation by {@link #main(String[])}.
     * @param specialChars
     *            True iff special chars are desired.
     * @return The generated password.
     */
    public static String createPwd(
            String seed,
            String service,
            int length,
            boolean specialChars)
            throws UnsupportedEncodingException,
            NoSuchAlgorithmException {

        String hash = sha256(seed + service);
        String encoding;

        if (specialChars) {
            encoding = getBaseX(hash, DICTIONARY_71);
        } else {
            encoding = getBase64(hash);
        }

        return encoding.substring(
                0,
                encoding.length() < length ? encoding.length() : length);
    }

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
                printHelp(options);
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

            String pwd = createPwd(
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
            printHelp(options);
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getLocalizedMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
}
