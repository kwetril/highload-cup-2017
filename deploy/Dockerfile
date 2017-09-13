FROM openjdk:8-jre-alpine

# set working directory
WORKDIR /root

# copy jar to container
ADD highload-cup-2017-netty-1.0-SNAPSHOT-jar-with-dependencies.jar /root/highload-cup-2017-netty-1.0-SNAPSHOT-jar-with-dependencies.jar

# open port for external access
EXPOSE 80

# start server
CMD java -server \
    -XX:+PrintGC \
    -XX:-PrintGCDetails -XX:+PrintGCTimeStamps \
    -Xms3G -Xmx3700M -XX:MaxMetaspaceSize=200M \
    -jar ./highload-cup-2017-netty-1.0-SNAPSHOT-jar-with-dependencies.jar prod