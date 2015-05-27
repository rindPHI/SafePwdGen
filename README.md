# SafePwdGen
SafePwdGen is a deterministic password generator. It lets you generate passwords
based on a seed password and a service identifier. If used correctly, you no longer
need to store / remember passwords; just generate them every time with this little tool.
You only need to remember the (secred) seed password and the way you generate identifiers,
e.g. from URLs like "myfancyplatform.com".

## Build and run

Just run `ant` in the main directory to compile the project.
Start SafePwdGen by calling `java -jar SafePwdGen.jar [options]`.
An empty list of arguments results in the self-explaining output

```
Missing required options: s, i
usage: java -jar SavePwdGen.jar [options]
 -c,--special-chars <With special chars (TRUE|false)>   Set to true if
                                                        special chars
                                                        !-_?=@/+* are
                                                        desired, else
                                                        false
 -h,--help                                              Prints this help
                                                        message
 -i,--service-identifier <Service Identifier>           The service that
                                                        the password is
                                                        created for, e.g.
                                                        facebook.com
 -l,--pwd-length <Password Length>                      Length of the
                                                        password in
                                                        characters
 -s,--seed-password <Seed Password>                     Password used as a
                                                        seed
```

## Example usage

```
$ ./SafePwdGen.jar -s 'mySecretPassword123!!!' -i 'mymailprovider.com'

Generated Password: Yo?-I0Mb1vTHIb+xxcm7

I tried to copy the generated password to your system clipboard.
This may have failed if you are yousing Gnome or some derivative of it.
```
