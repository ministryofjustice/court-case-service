# Court Case Service
### Service to access court cases imported from HMCTS Libra court lists


---


## Setup your machine

In the root folder of your machine start by checking if HomeBrew is installed on your computer with the command : `brew -version` 
If you have HomeBrew and Java set up already, go to the Dependencies section. 
:::info 
⚠️ This project run with ==JAVA 11== and NOT the last version!
:::


#### 1. If after running the brew version nothing appears do the following :
~1.1.~ `$ ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"`
~1.2.~ `$ brew install`
~1.3.~ `$ brew tap homebrew/cask`
~1.4.~ `$ brew tap homebrew/cask-versions`

#### 2. Check if the version 11 of Java is here :
~2.1.~ `$ brew cask info java`

If nothing appears, run :
~2.2.~ `$ brew cask install java11`

#### 3. If you have others/multiple versions of Java and you need to switch in between/manage them, then you have to install another tool called jEnv (or SDKMAN - see links in Resources section) : 
~3.1.~ `$ brew install jenv`

Depending if you use Bash or Zsh, run the right command for you :
> Bash
`$ echo 'export PATH="$HOME/.jenv/bin:$PATH"' >> ~/.bash_profile`
`$ echo 'eval "$(jenv init -)"' >> ~/.bash_profile`

> Zsh (The default interactive shell for docker)
`$ echo 'export PATH="$HOME/.jenv/bin:$PATH"' >> ~/.zshrc`
`$ echo 'eval "$(jenv init -)"' >> ~/.zshrc`

#### 4. Close your terminal to finalise jEnv set up.

#### 5. Reopen a new window and run :   
~5.1.~ `$ /usr/libexec/java_home -V` 
~5.2.~ `$ jenv add <choose one/all the versions you have found when you ran the previous command>`
*E.g.: `jenv add /Library/Java/JavaVirtualMachines/openjdk-11.0.2.jdk/Contents/Home`*


## Dependencies

The service has an attached Postgres database as well as several back-ends. The details of which can be found in the `docker-compose.yml` file.
:::info
⚠️ To open all of these, complete the Building and Running section.
:::

**Default port**
Starts the application on port '8080'. To override, set server.port (eg SERVER_PORT=8099 java -jar etc etc)

**Swagger UI**
The project builds swagger specifications which can be examined and tested via the Swagger UI. http://localhost:8080/swagger-ui.html

**Application health** : `$ curl -X GET http://localhost:8080/health`
**Application info** : `$ curl -X GET http://localhost:8080/info`
**Application Ping** : `$ curl -X GET http://localhost:8080/ping`
**Application Feature Flags** : `$ curl -X GET http://localhost:8080/feature-flags`
**To Check dependency versions** : `$ ./gradlew dependencyUpdates`

#### *Flyway commands*
**Migrate database** : `$ gradle flywayMigrate -i`
**View details and status information about all migrations** : `$ ./gradlew flywayInfo`
**Baseline an existing database, excluding all migrations up to and including baselineVersion** :  `$ ./gradlew flywayBaseline`
**Clean schema** : `$ ./gradlew flywayClean`


There are also Wiremock stubs for each of the back-end calls which the test Spring profile runs against, so in order to run these, use the following command along with `docker-compose up` : 
`$ bash runMocks.sh`


## Building and running

As we said before, this service is built using Gradle. In order to run the service locally, a Postgres database and some other backend services are required. The easiest way to run locally is using the `docker-compose.yml` file which will pull down the latest version. 

**1.** To do it, first assure you to have the Docker extension installed on your computer. If not, click on this link and upload it https://hub.docker.com/editions/community/docker-ce-desktop-mac/.

**2.** When installed, run the command `$ env | grep DOCKER`. Make sure that this command doesn’t output anything and you are ready to go! (If you have something, refer to the resources below Docker desktop / Docker toolbox)

**3.** Than run the command : `$ docker-compose up`

**4.** In order for the authentication to function between services locally you must add the following line to your `/etc/hosts` file before starting:
~4.1.~ `127.0.0.1 oauth`
~4.2.~ If you don’t have this file run this command in your terminal `$ sudo nano /etc/hosts` and write the previous line in the file.

**5.** The service uses Lombok and so annotation processors must be turned on within the IDE.

**6.** In order to build the project from the command line, run the command :
    `$ ./gradlew build`

**7.** To run the service, ensure there is an instance of Postgres running and then run :
    `$ SPRING_PROFILES_ACTIVE=local ./gradlew bootRun`


## Deployment

Builds and deployments are setup in `Circle CI` and configured in the config file.
Helm is used to deploy the service to a Kubernetes Cluster using templates in the `helm_deploy` folder.


## Resources

[Java 11 by Oracle](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
[Java 11 by OpenJDK](https://developers.redhat.com/products/openjdk/download)

[jEnv installation blog post](https://medium.com/@brunofrascino/working-with-multiple-java-versions-in-macos-9a9c4f15615a)
[jEnv manual](https://www.jenv.be/)

[SDKMAN installation blog post](https://hackernoon.com/using-sdkman-to-manage-java-versions-7fde0d38c501)
[SDKMAN manual](https://sdkman.io/)

[gradle](https://gradle.org/install/)
[Lombok for VSCode](https://marketplace.visualstudio.com/items?itemName=GabrielBB.vscode-lombok)

[Docker desktop / Docker toolbox](https://docs.docker.com/docker-for-mac/docker-toolbox/) *- which one to choose depending on your environment*
[Docker-compose](https://docs.docker.com/compose/reference/up/)
