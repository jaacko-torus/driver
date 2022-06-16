FROM openjdk:11

ENV SCALA_VERSION 2.13.8
ENV MILL_VERSION 0.10.4

WORKDIR /root

# scala
RUN \
    curl -fsL https://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
	echo >> /root/.bashrc && \
	echo "export PATH=~/scala-$SCALA_VERSION/bin:$PATH" >> /root/.bashrc

# mill
RUN \
	curl -L -o /usr/local/bin/mill https://github.com/lihaoyi/mill/releases/download/$MILL_VERSION/$MILL_VERSION && \
	chmod +x /usr/local/bin/mill && \
	touch build.sc && \
	mill -i resolve _ && \
	rm build.sc

COPY . .

ENV HTTP_PORT=9000
ENV WS_PORT=9001

CMD [ "mill", "driver" ]