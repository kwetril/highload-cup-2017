# Наследуемся от CentOS 7
FROM centos:7

# Install prepare infrastructure
RUN yum -y update && \
 yum -y install wget && \
 yum -y install tar

 # Prepare environment 
#ENV JAVA_HOME /opt/java
#ENV PATH $PATH:$JAVA_HOME/bin

ENV JRE_VERSION 8u131
ENV JRE_BUILD_NUM b11
ENV JRE_DOWNLOAD_HASH d54c1d3a095b4ff2b6607d096fa80163
ENV JRE_CHECKSUM ebebfd327e67c4bbe47dabe6b9f6e961
ENV JAVA_HOME /usr/java/latest

# Download and install the required version of Oracle's JDK
RUN wget --quiet --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/$JRE_VERSION-$JRE_BUILD_NUM/$JRE_DOWNLOAD_HASH/jre-$JRE_VERSION-linux-x64.rpm" \
  && echo "$JRE_CHECKSUM  jre-$JRE_VERSION-linux-x64.rpm" >> MD5SUM \
  && md5sum -c MD5SUM \
  && yum install -y "jre-$JRE_VERSION-linux-x64.rpm" \
  && yum clean all \
  && yum -y remove wget \
  && rm -rf "jre-$JRE_VERSION-linux-x64.rpm" \
            MD5SUM \
  && update-alternatives --install /usr/bin/java java $JAVA_HOME/bin/java 999999
# Выбираем рабочую папку
WORKDIR /root

# Копируем наш исходный main.go внутрь контейнера, в папку go/src/dumb
ADD highload-cup-2017-1.0-SNAPSHOT-jar-with-dependencies.jar /root/highload-cup-2017-1.0-SNAPSHOT-jar-with-dependencies.jar

# Открываем 80-й порт наружу
EXPOSE 80

# Запускаем наш сервер
CMD java -jar -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -Xmx4000m ./highload-cup-2017-1.0-SNAPSHOT-jar-with-dependencies.jar
