FROM gradle:7.1.1-jdk11
ENV APP_HOME=/usr/app/gps
WORKDIR $APP_HOME
COPY settings.gradle $APP_HOME
COPY build.gradle $APP_HOME
COPY --chown=gradle:gradle src/ $APP_HOME/src
RUN gradle clean build -x test
RUN rm $APP_HOME/build/libs/gps-app-1.0-plain.war
RUN mv $APP_HOME/build/libs/gps-app-1.0.war $APP_HOME/build/libs/app.war
