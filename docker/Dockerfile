FROM gradle:jdk14

RUN apt install -y libplist-utils

WORKDIR /usr/slack_assessment

ENTRYPOINT "./gradlew updateReleaseVersion"
